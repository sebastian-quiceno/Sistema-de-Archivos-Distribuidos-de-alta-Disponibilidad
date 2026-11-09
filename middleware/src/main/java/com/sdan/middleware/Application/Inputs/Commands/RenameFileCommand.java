package com.sdan.middleware.Application.Inputs.Commands;

import java.util.UUID;

public record RenameFileCommand(
        UUID fileId,
        String newFileName
) {}

