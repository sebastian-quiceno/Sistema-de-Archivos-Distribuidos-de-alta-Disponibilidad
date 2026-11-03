package com.sdan.middleware.Infraestructure.In.DTOs.Request;

import java.util.UUID;

public record CreateFolderRequest(
        UUID fatherId,
        String folderName
) {
}

