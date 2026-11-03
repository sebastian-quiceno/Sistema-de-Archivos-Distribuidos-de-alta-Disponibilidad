package com.sdan.middleware.Application.Ports.Storage;

import com.sdan.middleware.Domain.Entities.File;

import java.io.InputStream;

public interface IFileUpdateService {
    void updateFile(File file, InputStream newFile, Long newSize);
}

