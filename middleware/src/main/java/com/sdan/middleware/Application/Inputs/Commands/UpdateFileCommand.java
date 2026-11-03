package com.sdan.middleware.Application.Inputs.Commands;

import java.io.InputStream;
import java.util.UUID;

public record UpdateFileCommand(
        UUID fileId,
        InputStream newFile,
        Long newSize
) {}

