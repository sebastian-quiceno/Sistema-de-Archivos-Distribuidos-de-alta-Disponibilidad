package com.sdan.middleware.Application.Inputs.Commands;

import java.util.UUID;

public record CopyToAnotherNodeCommand(
        String region,
        String bucketName,
        String key,
        String arriveRegion,
        String arriveBucketName
) {
}
