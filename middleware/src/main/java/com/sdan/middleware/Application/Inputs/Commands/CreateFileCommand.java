package com.sdan.middleware.Application.Inputs.Commands;

import java.io.InputStream;
import java.util.UUID;

public record CreateFileCommand(
        UUID folderId,
        UUID fileTypeId,
        String name,
        InputStream file,
        Long size

){}
