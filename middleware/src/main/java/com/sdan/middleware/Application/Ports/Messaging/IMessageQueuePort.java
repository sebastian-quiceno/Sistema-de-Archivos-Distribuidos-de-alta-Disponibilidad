package com.sdan.middleware.Application.Ports.Messaging;

import com.sdan.middleware.Application.Inputs.Messages.HealChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.PurgeChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.ReplicateChunkMessage;

public interface IMessageQueuePort {
    void publishReplicationTask(ReplicateChunkMessage message);

    void publishPurgeTask(PurgeChunkMessage message);

    void publishHealTask(HealChunkMessage message);
}

