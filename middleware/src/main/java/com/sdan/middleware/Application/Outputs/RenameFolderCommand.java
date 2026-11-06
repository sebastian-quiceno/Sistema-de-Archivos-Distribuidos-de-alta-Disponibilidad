package com.sdan.middleware.Application.Outputs;

import java.util.UUID;

public record RenameFolderCommand(
        UUID folderId,
        String newForlderName
) {}
