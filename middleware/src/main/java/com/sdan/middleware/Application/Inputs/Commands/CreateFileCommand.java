package com.sdan.middleware.Application.Inputs.Commands;

import java.io.InputStream;
import java.util.UUID;

public record CreateFileCommand(
        UUID folderId,
        String fileTypeName,
        String name,
        InputStream file,
        Long size

){}
