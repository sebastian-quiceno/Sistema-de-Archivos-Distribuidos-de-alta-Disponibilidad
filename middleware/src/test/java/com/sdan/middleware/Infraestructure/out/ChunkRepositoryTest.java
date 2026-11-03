package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.ValueObjects.*;
import com.sdan.middleware.Infraestructure.out.Entities.*;
import com.sdan.middleware.Infraestructure.out.Mappers.*;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataChunkRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChunkRepositoryTest {

    @Mock
    private SpringDataChunkRepository springDataChunkRepository;
    @Mock
    private SpringDataFileRepository springDataFileRepository;
    @Mock
    private SpringDataNodeRepository springDataNodeRepository;

    private UserMapper userMapper;
    private FolderMapper folderMapper;
    private FileTypeMapper fileTypeMapper;
    private NodeMapper nodeMapper;
    private FileMapper fileMapper;
    private ChunkMapper chunkMapper;
    private ChunkRepository chunkRepository;

    private ChunkEntity chunkEntity;
    private UUID chunkId;
    private UUID fileId;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        folderMapper = new FolderMapper(userMapper);
        fileTypeMapper = new FileTypeMapper();
        nodeMapper = new NodeMapper();
        fileMapper = new FileMapper(folderMapper, fileTypeMapper);
        chunkMapper = new ChunkMapper(fileMapper, nodeMapper);

        chunkRepository = new ChunkRepository(
                springDataChunkRepository,
                springDataFileRepository,
                springDataNodeRepository,
                chunkMapper
        );

        chunkId = UUID.randomUUID();
        fileId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity(UUID.randomUUID(), "john", "hash", new Date());
        FolderEntity folderEntity = new FolderEntity(UUID.randomUUID(), userEntity, null, "Docs", new Date());
        FileTypeEntity fileTypeEntity = new FileTypeEntity(UUID.randomUUID(), "text/plain", "Text");
        FileEntity fileEntity = new FileEntity(fileId, folderEntity, fileTypeEntity, "notes.txt", 100L, new Date(), new Date(), 1);
        StatusEntity statusEntity = new StatusEntity(UUID.randomUUID(), "ONLINE", "Online");
        NodeEntity nodeEntity = new NodeEntity(UUID.randomUUID(), statusEntity, "node-1", "us-east-1", 1000L, 500L, new Date());

        chunkEntity = new ChunkEntity(
                chunkId,
                fileEntity,
                nodeEntity,
                1,
                false,
                100L,
                "a".repeat(64),
                "k".repeat(116),
                "zz"
        );
    }

    @Test
    void testGetById_found() {
        when(springDataChunkRepository.findById(chunkId)).thenReturn(Optional.of(chunkEntity));

        Optional<Chunk> result = chunkRepository.getById(chunkId);

        assertTrue(result.isPresent());
        assertEquals(chunkId, result.get().getId());
        assertEquals("a".repeat(64), result.get().getFileHash().getValue());
    }

    @Test
    void testGetFileHash() {
        when(springDataChunkRepository.findFileHashByChunkId(chunkId)).thenReturn(Optional.of("a".repeat(64)));

        FileHash hash = chunkRepository.getFileHash(chunkId);

        assertNotNull(hash);
        assertEquals("a".repeat(64), hash.getValue());
    }

    @Test
    void testGetFileChunks() {
        when(springDataChunkRepository.findOriginalChunksByFileId(fileId)).thenReturn(List.of(chunkEntity));

        List<Chunk> chunks = chunkRepository.getFileChunks(fileId);

        assertEquals(1, chunks.size());
        assertEquals(1, chunks.get(0).getPartNumber());
    }

    @Test
    void testGetReplicaNodes() {
        ChunkEntity replicaEntity = new ChunkEntity(
                UUID.randomUUID(),
                chunkEntity.getFile(),
                chunkEntity.getNode(),
                1,
                true,
                100L,
                "a".repeat(64),
                "k".repeat(116),
                "zz2"
        );
        when(springDataChunkRepository.findReplicaChunksByChunkId(chunkId)).thenReturn(List.of(replicaEntity));

        List<Chunk> replicas = chunkRepository.getReplicaNodes(chunkId);

        assertEquals(1, replicas.size());
        assertTrue(replicas.get(0).getReplica());
    }

    @Test
    void testSave() {
        Folder folder = Folder.create(null, null, new FolderName("Folder"));
        FileType fileType = FileType.create(new FileTypeName("text/plain"), "Text");
        File file = File.create(folder, fileType, new FileName("sample.txt"), 100L, 1);
        Node node = Node.create(new NodeName("node-1"), new RegionName("us-east-1"), 1000L, 500L);
        Chunk chunk = Chunk.create(file, node, false, 100L, new FileHash("a".repeat(64)), new FileKey("k".repeat(116)), new Version("zz"), 1);

        chunkRepository.save(chunk);

        verify(springDataChunkRepository, times(1)).save(any(ChunkEntity.class));
    }

    @Test
    void testDeleteById() {
        chunkRepository.deleteById(chunkId);

        verify(springDataChunkRepository, times(1)).deleteById(chunkId);
    }
}

