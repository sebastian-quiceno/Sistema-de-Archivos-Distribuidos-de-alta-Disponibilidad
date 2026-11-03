package com.sdan.middleware.Infraestructure.Config;

import com.sdan.middleware.Application.Folder.CreateFolderUseCase;
import com.sdan.middleware.Application.Folder.DeleteFolderUseCase;
import com.sdan.middleware.Application.Folder.GetFoldersByFatherFolderUseCase;
import com.sdan.middleware.Application.Folder.MoveFolderUserCase;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FolderUseCaseConfig {

    @Bean
    public CreateFolderUseCase createFolderUseCase(
            IUserRepository userRepository,
            IFolderRepository folderRepository,
            ICurrentUser currentUser) {
        return new CreateFolderUseCase(userRepository, folderRepository, currentUser);
    }

    @Bean
    public DeleteFolderUseCase deleteFolderUseCase(
            IFolderRepository folderRepository,
            ICurrentUser currentUser) {
        return new DeleteFolderUseCase(folderRepository, currentUser);
    }

    @Bean
    public GetFoldersByFatherFolderUseCase getFoldersByFatherFolderUseCase(
            IFolderRepository folderRepository,
            ICurrentUser currentUser) {
        return new GetFoldersByFatherFolderUseCase(folderRepository, currentUser);
    }

    @Bean
    public MoveFolderUserCase moveFolderUserCase(
            IFolderRepository folderRepository,
            ICurrentUser currentUser) {
        return new MoveFolderUserCase(folderRepository, currentUser);
    }
}

