package com.sdan.middleware.Infraestructure.Config;

import com.sdan.middleware.Application.File.CheckFilesUseCase;
import com.sdan.middleware.Application.File.ChunkReplicationService;
import com.sdan.middleware.Application.File.ChunkStorageService;
import com.sdan.middleware.Application.File.DeleteFileUseCase;
import com.sdan.middleware.Application.File.FileDeletionService;
import com.sdan.middleware.Application.File.FileIntegrityService;
import com.sdan.middleware.Application.File.FilePartitionService;
import com.sdan.middleware.Application.File.FileReconstructionService;
import com.sdan.middleware.Application.File.FileService;
import com.sdan.middleware.Application.File.FileUpdateService;
import com.sdan.middleware.Application.File.GetFileUseCase;
import com.sdan.middleware.Application.File.NodeAssignmentService;
import com.sdan.middleware.Application.File.UpdateFileUseCase;
import com.sdan.middleware.Application.File.UploadFileUseCase;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileTypeRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IFileDeletionService;
import com.sdan.middleware.Application.Ports.Storage.IFileIntegrityService;
import com.sdan.middleware.Application.Ports.Storage.IFileReconstructionService;
import com.sdan.middleware.Application.Ports.Storage.IFileUpdateService;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileUseCaseConfig {

    // --- Servicios de soporte / dominio para archivos ---

    @Bean
    public FilePartitionService filePartitionService() {
        return new FilePartitionService();
    }

    @Bean
    public NodeAssignmentService nodeAssignmentService(INodeRepository nodeRepository) {
        return new NodeAssignmentService(nodeRepository);
    }

    @Bean
    public ChunkStorageService chunkStorageService(
            INodeRepository nodeRepository,
            @Autowired(required = false) IStorageService storageService,
            IFileHasher fileHasher) {
        return new ChunkStorageService(nodeRepository, storageService, fileHasher);
    }

    @Bean
    public ChunkReplicationService chunkReplicationService(
            INodeRepository nodeRepository,
            @Autowired(required = false) IStorageService storageService,
            NodeAssignmentService nodeAssignmentService) {
        return new ChunkReplicationService(nodeRepository, storageService, nodeAssignmentService);
    }

    @Bean
    public FileService fileService(
            IFileRepository fileRepository,
            IChunkRepository chunkRepository,
            FilePartitionService filePartitionService,
            NodeAssignmentService nodeAssignmentService,
            ChunkStorageService chunkStorageService,
            ChunkReplicationService chunkReplicationService) {
        return new FileService(
                fileRepository,
                chunkRepository,
                filePartitionService,
                nodeAssignmentService,
                chunkStorageService,
                chunkReplicationService
        );
    }

    @Bean
    public FileReconstructionService fileReconstructionService(
            IChunkRepository chunkRepository,
            IFileRepository fileRepository,
            ChunkStorageService chunkStorageService) {
        return new FileReconstructionService(chunkRepository, fileRepository, chunkStorageService);
    }

    @Bean
    public FileUpdateService fileUpdateService(
            IFileRepository fileRepository,
            IChunkRepository chunkRepository,
            FilePartitionService filePartitionService,
            NodeAssignmentService nodeAssignmentService,
            ChunkStorageService chunkStorageService,
            ChunkReplicationService chunkReplicationService) {
        return new FileUpdateService(
                fileRepository,
                chunkRepository,
                filePartitionService,
                nodeAssignmentService,
                chunkStorageService,
                chunkReplicationService
        );
    }

    @Bean
    public FileDeletionService fileDeletionService(
            IFileRepository fileRepository,
            IChunkRepository chunkRepository,
            ChunkStorageService chunkStorageService) {
        return new FileDeletionService(fileRepository, chunkRepository, chunkStorageService);
    }

    @Bean
    public FileIntegrityService fileIntegrityService(
            IChunkRepository chunkRepository,
            @Autowired(required = false) IStorageService storageService,
            IFileHasher fileHasher,
            ChunkReplicationService chunkReplicationService) {
        return new FileIntegrityService(chunkRepository, storageService, fileHasher, chunkReplicationService);
    }

    // --- Casos de uso de archivos ---

    @Bean
    public UploadFileUseCase uploadFileUseCase(
            IFileTypeRepository fileTypeRepository,
            IFolderRepository folderRepository,
            ICurrentUser currentUser,
            FileService fileService) {
        return new UploadFileUseCase(fileTypeRepository, folderRepository, currentUser, fileService);
    }

    @Bean
    public GetFileUseCase getFileUseCase(
            ICurrentUser currentUser,
            IFileRepository fileRepository,
            IFileReconstructionService fileReconstructionService) {
        return new GetFileUseCase(currentUser, fileRepository, fileReconstructionService);
    }

    @Bean
    public UpdateFileUseCase updateFileUseCase(
            ICurrentUser currentUser,
            IFileRepository fileRepository,
            IFileUpdateService fileUpdateService) {
        return new UpdateFileUseCase(currentUser, fileRepository, fileUpdateService);
    }

    @Bean
    public DeleteFileUseCase deleteFileUseCase(
            ICurrentUser currentUser,
            IFileRepository fileRepository,
            IFileDeletionService fileDeletionService) {
        return new DeleteFileUseCase(currentUser, fileRepository, fileDeletionService);
    }

    @Bean
    public CheckFilesUseCase checkFilesUseCase(
            IFileRepository fileRepository,
            IFileIntegrityService fileIntegrityService) {
        return new CheckFilesUseCase(fileRepository, fileIntegrityService);
    }
}

