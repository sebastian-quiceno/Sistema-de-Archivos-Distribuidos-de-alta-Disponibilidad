package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;

import java.util.UUID;

public class CreateFileUseCase {
    private static final Long MAX_SIZE_PER_CHUNK = 32L;

    private final IFileRepository fileRepository;
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;
    private final IChunkRepository chunkRepository;
    private final IStorageService storageService;
    private final INodeRepository nodeRepository;

    public CreateFileUseCase(IFileRepository fileRepository, IFolderRepository folderRepository, ICurrentUser currentUser, IChunkRepository chunkRepository, IStorageService storageService, INodeRepository nodeRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
        this.chunkRepository = chunkRepository;
        this.storageService = storageService;
        this.nodeRepository = nodeRepository;
    }

    public void execute(CreateFileCommand dto){
        UUID loggedUser = currentUser.getUserId();

        Folder folder = folderRepository.getById(dto.folderId());

        if(folder == null)
            throw new FolderNotFoundException("La carpeta con el id: '"+dto.folderId()+" no existe");

        long totalChunks = Math.ceilDiv(dto.size(), MAX_SIZE_PER_CHUNK);


        storageService.generateUploadUrl

    }
}
