package com.sdan.middleware.Application.Folder;

import com.sdan.middleware.Application.Outputs.MetadataFileOutput;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Folder;

import java.util.List;
import java.util.UUID;

public class GetFilesByFolderIdUseCase {
    private final IFolderRepository folderRepository;
    private final ICurrentUser currentUser;

    public GetFilesByFolderIdUseCase(IFolderRepository folderRepository, ICurrentUser currentUser) {
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
    }

    public List<MetadataFileOutput> execute(UUID folderId){
        UUID loggedUser = currentUser.getUserId();
        List<File> files;

        if(folderId == null){
            files = folderRepository.getRelatedFileByUserId(loggedUser);
        }else{
            files = folderRepository.getRelatedFiles(folderId);
        }

        return files.stream().map(
                (f)-> new MetadataFileOutput(
                        f.getId(),
                        f.getFolder().getFolderId(),
                        f.getFileType().getName().getValue(),
                        f.getName().getValue(),
                        f.getBytesSize(),
                        f.getTotalPartitions(),
                        f.getLastTimeModified()
                )).toList();
    }

}
