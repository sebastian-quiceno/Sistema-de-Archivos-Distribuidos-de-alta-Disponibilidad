package com.sdan.middleware.Infraestructure.out.Messaging;

import com.sdan.middleware.Application.Inputs.Messages.HealChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.PurgeChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.ReplicateChunkMessage;
import com.sdan.middleware.Application.Ports.Messaging.IMessageQueuePort;
import com.sdan.middleware.Infraestructure.Config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqTaskPublisher implements IMessageQueuePort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqTaskPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishReplicationTask(ReplicateChunkMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("El mensaje de replicación no puede ser nulo");
        }
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_REPLICATION,
                message
        );
    }

    @Override
    public void publishPurgeTask(PurgeChunkMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("El mensaje de purga no puede ser nulo");
        }
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_PURGE,
                message
        );
    }

    @Override
    public void publishHealTask(HealChunkMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("El mensaje de auto-sanación no puede ser nulo");
        }
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_HEALING,
                message
        );
    }
}

