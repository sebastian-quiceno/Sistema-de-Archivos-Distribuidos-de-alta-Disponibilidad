package com.sdan.middleware.Infraestructure.In.Messaging;

import com.sdan.middleware.Application.File.ChunkReplicationService;
import com.sdan.middleware.Application.Inputs.Messages.HealChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.PurgeChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.ReplicateChunkMessage;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Infraestructure.Config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RabbitMqTaskListener {

    private final IChunkRepository chunkRepository;
    private final ChunkReplicationService chunkReplicationService;
    private final IStorageService storageService;

    public RabbitMqTaskListener(
            IChunkRepository chunkRepository,
            ChunkReplicationService chunkReplicationService,
            @Autowired(required = false) IStorageService storageService) {
        this.chunkRepository = chunkRepository;
        this.chunkReplicationService = chunkReplicationService;
        this.storageService = storageService;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_REPLICATION)
    public void handleReplicationTask(ReplicateChunkMessage message) {
        if (message == null || message.chunkId() == null) {
            return;
        }

        Optional<Chunk> chunkOpt = chunkRepository.getById(message.chunkId());
        if (chunkOpt.isEmpty()) {
            return;
        }

        List<Chunk> replicas = chunkReplicationService.replicateFile(
                chunkOpt.get(),
                message.replicationFactor() > 0 ? message.replicationFactor() : ChunkReplicationService.REPLICATION_FACTOR
        );

        if (replicas != null) {
            for (Chunk replica : replicas) {
                chunkRepository.save(replica);
            }
        }
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_PURGE)
    public void handlePurgeTask(PurgeChunkMessage message) {
        if (message == null || storageService == null) {
            return;
        }

        storageService.deleteFileByKey(
                message.nodeName(),
                message.regionName(),
                message.fileKey()
        );
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_HEALING)
    public void handleHealTask(HealChunkMessage message) {
        if (message == null || message.corruptedChunkId() == null || message.healthyChunkId() == null) {
            return;
        }

        Optional<Chunk> corruptedOpt = chunkRepository.getById(message.corruptedChunkId());
        Optional<Chunk> healthyOpt = chunkRepository.getById(message.healthyChunkId());

        if (corruptedOpt.isPresent() && healthyOpt.isPresent()) {
            chunkReplicationService.healChunk(corruptedOpt.get(), healthyOpt.get());
        }
    }
}

