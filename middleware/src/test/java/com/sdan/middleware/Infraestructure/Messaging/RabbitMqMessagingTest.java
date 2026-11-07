package com.sdan.middleware.Infraestructure.Messaging;

import com.sdan.middleware.Application.File.ChunkReplicationService;
import com.sdan.middleware.Application.File.ChunkStorageService;
import com.sdan.middleware.Application.File.FileDeletionService;
import com.sdan.middleware.Application.File.FilePartitionService;
import com.sdan.middleware.Application.File.FileReconstructionService;
import com.sdan.middleware.Application.File.FileService;
import com.sdan.middleware.Application.File.NodeAssignmentService;
import com.sdan.middleware.Application.Inputs.Messages.HealChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.PurgeChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.ReplicateChunkMessage;
import com.sdan.middleware.Application.Outputs.ChunkGetMethodOutput;
import com.sdan.middleware.Application.Ports.Messaging.IMessageQueuePort;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Domain.ValueObjects.FileTypeName;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.NodeName;
import com.sdan.middleware.Domain.ValueObjects.RegionName;
import com.sdan.middleware.Domain.ValueObjects.Version;
import com.sdan.middleware.Infraestructure.Config.RabbitMqConfig;
import com.sdan.middleware.Infraestructure.In.Messaging.RabbitMqTaskListener;
import com.sdan.middleware.Infraestructure.out.Messaging.RabbitMqTaskPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMqMessagingTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private IChunkRepository chunkRepository;
    @Mock
    private IFileRepository fileRepository;
    @Mock
    private ChunkReplicationService chunkReplicationService;
    @Mock
    private ChunkStorageService chunkStorageService;
    @Mock
    private FilePartitionService filePartitionService;
    @Mock
    private NodeAssignmentService nodeAssignmentService;
    @Mock
    private IStorageService storageService;
    @Mock
    private IMessageQueuePort messageQueuePort;

    private RabbitMqTaskPublisher publisher;
    private RabbitMqTaskListener listener;

    private File sampleFile;
    private Node nodeA;
    private Node nodeB;
    private FileHash sampleHash;
    private FileKey sampleKey;

    @BeforeEach
    void setUp() {
        publisher = new RabbitMqTaskPublisher(rabbitTemplate);
        listener = new RabbitMqTaskListener(chunkRepository, chunkReplicationService, storageService);

        Folder folder = Folder.create(null, null, new FolderName("Root"));
        FileType fileType = FileType.create(new FileTypeName("text/plain"), "Plain text");
        sampleFile = File.create(folder, fileType, new FileName("distributed.txt"), 64L, 1);

        nodeA = Node.create(new NodeName("node-a"), new RegionName("us-east-1"), 1000L, 800L);
        nodeB = Node.create(new NodeName("node-b"), new RegionName("us-east-2"), 1000L, 800L);
        sampleHash = new FileHash("a".repeat(64));
        sampleKey = new FileKey("k".repeat(116));
    }

    @Test
    @DisplayName("RabbitMqConfig configura Exchange, 3 Colas durables, Bindings y convertidor JSON")
    void testRabbitMqConfigBeansAndJsonSerialization() {
        RabbitMqConfig config = new RabbitMqConfig();

        TopicExchange exchange = config.dfshaTasksExchange();
        Queue repQueue = config.replicationQueue();
        Queue purgeQueue = config.purgeQueue();
        Queue healQueue = config.healingQueue();

        assertEquals(RabbitMqConfig.EXCHANGE_NAME, exchange.getName());
        assertTrue(exchange.isDurable());

        assertTrue(repQueue.isDurable());
        assertTrue(purgeQueue.isDurable());
        assertTrue(healQueue.isDurable());

        Binding repBinding = config.replicationBinding(repQueue, exchange);
        Binding purgeBinding = config.purgeBinding(purgeQueue, exchange);
        Binding healBinding = config.healingBinding(healQueue, exchange);

        assertEquals(RabbitMqConfig.ROUTING_REPLICATION, repBinding.getRoutingKey());
        assertEquals(RabbitMqConfig.ROUTING_PURGE, purgeBinding.getRoutingKey());
        assertEquals(RabbitMqConfig.ROUTING_HEALING, healBinding.getRoutingKey());

        MessageConverter converter = config.jsonMessageConverter();
        ReplicateChunkMessage originalMsg = new ReplicateChunkMessage(UUID.randomUUID(), 3);
        Message amqpMessage = converter.toMessage(originalMsg, new MessageProperties());
        Object deserialized = converter.fromMessage(amqpMessage);

        assertEquals(originalMsg, deserialized);
    }

    @Test
    @DisplayName("RabbitMqTaskPublisher publica tareas de replicación, purga y auto-sanación al Exchange con su routing key")
    void testPublisherSendsMessagesToExchange() {
        UUID chunkId = UUID.randomUUID();
        UUID healthyId = UUID.randomUUID();

        ReplicateChunkMessage repMsg = new ReplicateChunkMessage(chunkId, 3);
        PurgeChunkMessage purgeMsg = new PurgeChunkMessage("node-a", "us-east-1", "key-123");
        HealChunkMessage healMsg = new HealChunkMessage(chunkId, healthyId);

        publisher.publishReplicationTask(repMsg);
        publisher.publishPurgeTask(purgeMsg);
        publisher.publishHealTask(healMsg);

        verify(rabbitTemplate).convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_REPLICATION, repMsg);
        verify(rabbitTemplate).convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_PURGE, purgeMsg);
        verify(rabbitTemplate).convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_HEALING, healMsg);
    }

    @Test
    @DisplayName("RabbitMqTaskListener procesa ReplicateChunkMessage y persiste las réplicas creadas")
    void testListenerHandlesReplicationTask() {
        Chunk original = Chunk.create(sampleFile, nodeA, false, 64L, sampleHash, sampleKey, new Version("zz"), 1);
        Chunk replica = Chunk.create(sampleFile, nodeB, true, 64L, sampleHash, sampleKey, new Version("zz2"), 1);

        when(chunkRepository.getById(original.getId())).thenReturn(Optional.of(original));
        when(chunkReplicationService.replicateFile(original, 3)).thenReturn(List.of(replica));

        listener.handleReplicationTask(new ReplicateChunkMessage(original.getId(), 3));

        verify(chunkReplicationService).replicateFile(original, 3);
        verify(chunkRepository).save(replica);
    }

    @Test
    @DisplayName("RabbitMqTaskListener procesa PurgeChunkMessage eliminando el bloque físico del nodo")
    void testListenerHandlesPurgeTask() {
        PurgeChunkMessage msg = new PurgeChunkMessage("node-a", "us-east-1", "chunk-key-99");

        listener.handlePurgeTask(msg);

        verify(storageService).deleteFileByKey("node-a", "us-east-1", "chunk-key-99");
    }

    @Test
    @DisplayName("RabbitMqTaskListener procesa HealChunkMessage reparando el chunk corrupto desde la réplica sana")
    void testListenerHandlesHealTask() {
        Chunk corrupted = Chunk.create(sampleFile, nodeA, false, 64L, sampleHash, sampleKey, new Version("zz"), 1);
        Chunk healthy = Chunk.create(sampleFile, nodeB, true, 64L, sampleHash, sampleKey, new Version("zz2"), 1);

        when(chunkRepository.getById(corrupted.getId())).thenReturn(Optional.of(corrupted));
        when(chunkRepository.getById(healthy.getId())).thenReturn(Optional.of(healthy));

        listener.handleHealTask(new HealChunkMessage(corrupted.getId(), healthy.getId()));

        verify(chunkReplicationService).healChunk(corrupted, healthy);
    }

    @Test
    @DisplayName("FileService con MOM encola replicación asíncrona tras guardar los chunks primarios")
    void testFileServiceEnqueuesAsyncReplicationViaMom() {
        FileService fileService = new FileService(
                fileRepository,
                chunkRepository,
                filePartitionService,
                nodeAssignmentService,
                chunkStorageService,
                chunkReplicationService,
                messageQueuePort
        );

        when(filePartitionService.calculateTotalPartitions(64L)).thenReturn(1);
        when(nodeAssignmentService.getRandomNodeAssignments(1)).thenReturn(new ArrayDeque<>(List.of(nodeA.getId())));

        Chunk uploadedChunk = Chunk.create(sampleFile, nodeA, false, 64L, sampleHash, sampleKey, new Version("zz"), 1);

        doAnswer(inv -> {
            Consumer<byte[]> consumer = inv.getArgument(2);
            consumer.accept(new byte[64]);
            return null;
        }).when(filePartitionService).partition(any(InputStream.class), eq(64L), any());

        when(chunkStorageService.uploadToNode(eq(nodeA.getId()), any(File.class), any(byte[].class), eq(false), eq(1)))
                .thenReturn(uploadedChunk);

        fileService.uploadFile(
                sampleFile.getName(),
                sampleFile.getFileType(),
                sampleFile.getFolder(),
                new ByteArrayInputStream(new byte[64]),
                64L
        );

        verify(fileRepository).save(any(File.class));
        verify(chunkRepository).save(uploadedChunk);
        // No se replica sincrónicamente en el hilo HTTP
        verify(chunkReplicationService, never()).replicateFile(any(), anyInt());
        // Se publica en la cola MOM de RabbitMQ
        verify(messageQueuePort).publishReplicationTask(new ReplicateChunkMessage(uploadedChunk.getId(), FileService.REPLICATION_FACTOR));
    }

    @Test
    @DisplayName("FileService con MOM ejecuta compensación SAGA encolando purga si falla la subida de un chunk intermedio")
    void testFileServiceSagaRollbackViaMomOnFailure() {
        FileService fileService = new FileService(
                fileRepository,
                chunkRepository,
                filePartitionService,
                nodeAssignmentService,
                chunkStorageService,
                chunkReplicationService,
                messageQueuePort
        );

        when(filePartitionService.calculateTotalPartitions(128L)).thenReturn(2);
        when(nodeAssignmentService.getRandomNodeAssignments(2))
                .thenReturn(new ArrayDeque<>(List.of(nodeA.getId(), nodeB.getId())));

        Chunk chunk1 = Chunk.create(sampleFile, nodeA, false, 64L, sampleHash, sampleKey, new Version("zz"), 1);

        doAnswer(inv -> {
            Consumer<byte[]> consumer = inv.getArgument(2);
            consumer.accept(new byte[64]); // Chunk 1 sube bien
            consumer.accept(new byte[64]); // Chunk 2 falla
            return null;
        }).when(filePartitionService).partition(any(InputStream.class), eq(128L), any());

        when(chunkStorageService.uploadToNode(eq(nodeA.getId()), any(File.class), any(byte[].class), eq(false), eq(1)))
                .thenReturn(chunk1);
        when(chunkStorageService.uploadToNode(eq(nodeB.getId()), any(File.class), any(byte[].class), eq(false), eq(2)))
                .thenThrow(new RuntimeException("Nodo B caído"));

        assertThrows(RuntimeException.class, () -> fileService.uploadFile(
                sampleFile.getName(),
                sampleFile.getFileType(),
                sampleFile.getFolder(),
                new ByteArrayInputStream(new byte[128]),
                128L
        ));

        // SAGA: Chunk 1 se manda a purgar por MOM y no se guarda metadata huérfana
        verify(messageQueuePort).publishPurgeTask(new PurgeChunkMessage(
                nodeA.getName().getValue(),
                nodeA.getRegionName().getValue(),
                sampleKey.getValue()
        ));
        verify(fileRepository, never()).save(any());
    }

    @Test
    @DisplayName("FileDeletionService con MOM elimina metadata primero y encola la purga física en RabbitMQ")
    void testFileDeletionServicePublishesPurgeTasksViaMom() {
        FileDeletionService deletionService = new FileDeletionService(
                fileRepository,
                chunkRepository,
                chunkStorageService,
                messageQueuePort
        );

        Chunk original = Chunk.create(sampleFile, nodeA, false, 64L, sampleHash, sampleKey, new Version("zz"), 1);
        when(chunkRepository.getFileChunks(sampleFile.getId())).thenReturn(List.of(original));
        when(chunkRepository.getReplicaNodes(original.getId())).thenReturn(List.of());

        deletionService.deleteFile(sampleFile);

        verify(chunkRepository).deleteById(original.getId());
        verify(fileRepository).deleteById(sampleFile.getId());
        verify(messageQueuePort).publishPurgeTask(new PurgeChunkMessage(
                nodeA.getName().getValue(),
                nodeA.getRegionName().getValue(),
                sampleKey.getValue()
        ));
    }

    @Test
    @DisplayName("FileReconstructionService con MOM entrega la réplica sana al usuario y encola HealChunkMessage en RabbitMQ")
    void testFileReconstructionServiceEnqueuesHealTaskWhenPrimaryCorrupted() throws Exception {
        FileReconstructionService reconstructionService = new FileReconstructionService(
                chunkRepository,
                fileRepository,
                chunkStorageService,
                messageQueuePort
        );

        Chunk corruptedPrimary = Chunk.create(sampleFile, nodeA, false, 10L, sampleHash, sampleKey, new Version("zz"), 1);
        Chunk healthyReplica = Chunk.create(sampleFile, nodeB, true, 10L, sampleHash, sampleKey, new Version("zz2"), 1);

        when(fileRepository.getOriginalsChunks(sampleFile.getId())).thenReturn(List.of(corruptedPrimary));
        when(chunkStorageService.getChunk(corruptedPrimary))
                .thenReturn(new ChunkGetMethodOutput(corruptedPrimary.getId(), null, true));
        when(chunkRepository.getReplicaNodes(corruptedPrimary.getId())).thenReturn(List.of(healthyReplica));
        when(chunkStorageService.getChunk(healthyReplica))
                .thenReturn(new ChunkGetMethodOutput(healthyReplica.getId(), new ByteArrayInputStream("OK_CONTENT".getBytes(StandardCharsets.UTF_8)), false));

        InputStream result = reconstructionService.mergeFile(sampleFile);

        assertEquals("OK_CONTENT", new String(result.readAllBytes(), StandardCharsets.UTF_8));
        verify(messageQueuePort).publishHealTask(new HealChunkMessage(corruptedPrimary.getId(), healthyReplica.getId()));
    }
}

