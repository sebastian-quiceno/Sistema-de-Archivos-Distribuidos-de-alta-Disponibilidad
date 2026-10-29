package com.sdan.middleware.Application.Inputs.Commands;

import java.util.UUID;

public record MoveFolderCommand(
        UUID folderId,
        UUID newFatherFolderId
) {}
