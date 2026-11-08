package com.sdan.middleware.Application.Outputs;

import java.util.Date;
import java.util.UUID;

public record FolderOutput(
        UUID folderId,
        UUID fatherFolderId,
        String name,
        Date createdDate
){}
