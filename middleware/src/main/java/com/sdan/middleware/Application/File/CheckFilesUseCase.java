package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Domain.Entities.File;

import java.util.List;

public class CheckFilesUseCase {

    private final IFileRepository fileRepository;
    private final ChunkStorageService chunkStorageService;

    public CheckFilesUseCase(IFileRepository fileRepository, ChunkStorageService chunkStorageService) {
        this.fileRepository = fileRepository;
        this.chunkStorageService = chunkStorageService;
    }

    public void execute(){
        List<File> files = fileRepository.getAll();

        for(File file : files){
            chunkStorageService.lookOverIntegrity(file);
        }
    }

}
