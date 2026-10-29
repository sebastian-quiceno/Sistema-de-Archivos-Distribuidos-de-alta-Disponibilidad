package com.sdan.middleware.Application.Inputs.Commands;

import java.util.UUID;

public record CreateFileCommand(
        UUID folderId,
        UUID fileType,
        String name,
        Long size

){}
