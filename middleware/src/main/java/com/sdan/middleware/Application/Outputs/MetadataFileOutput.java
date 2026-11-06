package com.sdan.middleware.Application.Outputs;

import java.util.Date;
import java.util.UUID;

public record MetadataFileOutput(
        UUID id,
        UUID folderId,
        String fileTypeName,
        String fileName,
        Long bytesSize,
        Integer totalPartitions,
        Date lastTimeModified
) {}
