package com.sdan.middleware.Application.Ports.Storage;

import com.sdan.middleware.Domain.Entities.File;

public interface IFileDeletionService {
    void deleteFile(File file);
}

