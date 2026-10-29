package com.sdan.middleware.Application.Inputs.Commands;

import java.util.UUID;

public record CreateFolderCommand(
        UUID fatherId,
        String folderName
) {}
