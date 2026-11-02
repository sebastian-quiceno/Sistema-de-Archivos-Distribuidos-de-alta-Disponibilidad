package com.sdan.middleware.Application.Ports.Storage;

import com.sdan.middleware.Domain.Entities.File;

import java.io.InputStream;

public interface IFileReconstructionService {
    InputStream mergeFile(File file);
}

