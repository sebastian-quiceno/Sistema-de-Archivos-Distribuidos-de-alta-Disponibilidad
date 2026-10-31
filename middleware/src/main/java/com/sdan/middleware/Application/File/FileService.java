package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileTypeRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFolderRepository;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileName;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class FileService {

    private final IFileRepository fileRepository;
    private final IFolderRepository folderRepository;
    private final IFileTypeRepository fileTypeRepository;
    private final IChunkRepository chunkRepository;
    private final FilePartitionService filePartitionService;
    private final NodeAssignmentService nodeAssignmentService;
    private final ChunkStorageService chunkStorageService;
    private final ChunkReplicationService chunkReplicationService;

    public FileService(
            IFileRepository fileRepository,
            IFolderRepository folderRepository,
            IFileTypeRepository fileTypeRepository,
            IChunkRepository chunkRepository,
            FilePartitionService filePartitionService,
            NodeAssignmentService nodeAssignmentService,
            ChunkStorageService chunkStorageService,
            ChunkReplicationService chunkReplicationService) {

        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.fileTypeRepository = fileTypeRepository;
        this.chunkRepository = chunkRepository;
        this.filePartitionService = filePartitionService;
        this.nodeAssignmentService = nodeAssignmentService;
        this.chunkStorageService = chunkStorageService;
        this.chunkReplicationService = chunkReplicationService;
    }


    public void partitionFile(CreateFileCommand command) {

        validateCommand(command);

        FileType fileType = fileTypeRepository.getById(command.fileTypeId()).orElseThrow(() -> new FileTypeNotFoundException(
                        "No se encontró el tipo de archivo con id: " + command.fileTypeId())
        );

        Folder folder = folderRepository.getById(command.folderId());

        if (folder == null)
            throw new FolderNotFoundException("No se encontró la carpeta con id: "+ command.folderId());

        int totalChunks = filePartitionService.calculateTotalPartitions(command.size());

        Queue<UUID> nodeIds = nodeAssignmentService.getRandomNodeAssignments(totalChunks);

        File newFile = File.create(folder, fileType, new FileName(command.name()), command.size(), Math.toIntExact(totalChunks));

        List<Chunk> chunks = new ArrayList<>();

        filePartitionService.partition(command.file(), command.size(), chunkBytes -> {
            UUID nodeId = nodeIds.poll();

            Chunk chunk = chunkStorageService.uploadToNode(nodeId, newFile, chunkBytes, false);

            chunks.add(chunk);

            chunkReplicationService.replicateFile(nodeId, chunk);
        });

        // Persistir la metadata después de procesar todos los fragmentos.
        fileRepository.save(newFile);

        for (Chunk chunk : chunks) {
            chunkRepository.save(chunk);
        }
    }

    //TODO: revisar si es mejor sacarlo de la clase, e implementarlo en otra
    private void validateCommand(CreateFileCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser null");
        }

        if (command.size() < 0) {
            throw new IllegalArgumentException("El tamaño del archivo no puede ser negativo");
        }

        if (command.file() == null) {
            throw new IllegalArgumentException("El archivo no puede ser null");
        }
    }
}
