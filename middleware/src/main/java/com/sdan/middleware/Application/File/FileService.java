package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;
import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.*;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.*;
import com.sdan.middleware.Domain.Exceptions.File.FileProcessingException;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Node.InsufficientNodesForReplicationException;
import com.sdan.middleware.Domain.Exceptions.Node.NoAvailableNodesException;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Domain.ValueObjects.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class FileService {
    private static final int MAX_SIZE_PER_CHUNK = 32 * 1024 * 1024;
    private static final int REPLICAITION_FACTOR = 3;

    private final IFileRepository fileRepository;
    private final IFolderRepository folderRepository;
    private final IFileTypeRepository fileTypeRepository;
    private final IChunkRepository chunkRepository;
    private final INodeRepository nodeRepository;
    private final IStorageService storageService;
    private final IFileHasher fileHasher;

    public FileService(
            IFileRepository fileRepository,
            IFolderRepository folderRepository,
            IFileTypeRepository fileTypeRepository,
            IChunkRepository chunkRepository,
            INodeRepository nodeRepository,
            IStorageService storageService,
            IFileHasher fileHasher) {

        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.fileTypeRepository = fileTypeRepository;
        this.chunkRepository = chunkRepository;
        this.nodeRepository = nodeRepository;
        this.storageService = storageService;
        this.fileHasher = fileHasher;
    }

    public void partitionFile(CreateFileCommand command) {

        validateCommand(command);

        FileType fileType = fileTypeRepository.getById(command.fileTypeId()).orElseThrow(() -> new FileTypeNotFoundException(
                        "No se encontró el tipo de archivo con id: " + command.fileTypeId())
        );

        Folder folder = folderRepository.getById(command.folderId());

        if (folder == null)
            throw new FolderNotFoundException("No se encontró la carpeta con id: "+ command.folderId());

        int totalChunks = calculateTotalPartitions(command.size());

        Queue<UUID> nodeIds = getRandomNodeAssignments(totalChunks);

        File newFile = File.create(folder, fileType, new FileName(command.name()), command.size(), Math.toIntExact(totalChunks));

        List<Chunk> chunks = new ArrayList<>();

        long totalBytesRead = 0;
        int nodeIndex = 0;
        UUID nodeId;

        try (InputStream input = command.file()) {
            byte[] chunkBytes;

            while ((chunkBytes = input.readNBytes(MAX_SIZE_PER_CHUNK)).length > 0) {

                totalBytesRead += chunkBytes.length;

                // Evita almacenar un archivo distinto al declarado.
                if (totalBytesRead > command.size()) {
                    throw new FileProcessingException("El archivo contiene más bytes de los declarados");
                }

                nodeId = nodeIds.poll();
                nodeIndex++;

                Chunk chunk = uploadToNode(nodeId, newFile, chunkBytes, false);

                chunks.add(chunk);

                replicateFile(nodeId, chunk);
            }

            if (totalBytesRead != command.size()) {
                throw new FileProcessingException(
                        "El tamaño real del archivo no coincide con el tamaño declarado. Esperado: " + command.size() + ", leído: " + totalBytesRead
                );
            }

        } catch (IOException e) {
            throw new FileProcessingException("Error al leer o procesar el archivo"+ "\n" +e);
        }

        // Persistir la metadata después de procesar todos los fragmentos.
        fileRepository.save(newFile);

        for (Chunk chunk : chunks) {
            chunkRepository.save(chunk);
        }
    }

    private Chunk uploadToNode(UUID nodeId, File file, byte[] chunkBytes, boolean isReplica) {

        Node node = nodeRepository.getById(nodeId).orElseThrow(
                () -> new NodeNotFoundException("No se encontró el nodo con id: " + nodeId)
        );

        // La longitud y el hash corresponden al fragmento real.
        long chunkSize = chunkBytes.length;

        FileHash hash = new FileHash(fileHasher.Hash(new ByteArrayInputStream(chunkBytes)));

        // La clave no depende del nombre original del archivo.
        String key = UUID.randomUUID().toString();

        try (InputStream uploadStream =new ByteArrayInputStream(chunkBytes)) {

            storageService.uploadObject(key, uploadStream, chunkSize, file.getFileType().getName().getValue());


        } catch (IOException e) {
            throw new FileProcessingException("No fue posible subir el fragmento al nodo "+ nodeId + "\n" +e);
        }

        return Chunk.create(
                file,
                node,
                isReplica,
                chunkSize,
                hash,
                new FileKey(key),
                new Version("zz"));
    }

    private int calculateTotalPartitions(long size) {

        if (size < 0) {
            throw new IllegalArgumentException("El tamaño del archivo no puede ser negativo");
        }

        return (int)Math.ceilDiv(size, MAX_SIZE_PER_CHUNK);
    }

    //TODO: Revisar si hace falta pedir el Nodo al que se quiere replicar (Es mejor que lo haga el mismo)
    private void replicateFile(UUID originalNodeId, Chunk chunk){

        Node arrivalNode;
        File file;
        Chunk newCkunk;

        CopyToAnotherNodeCommand command;


        Node originalNode = nodeRepository.getById(originalNodeId).orElseThrow(
                ()-> new NodeNotFoundException("No se encontro el nodo con el ID: '"+originalNodeId+"'")
        );

        List<UUID> arrivalNodesIds = getDifferentsNodesIds(originalNodeId);

        for (UUID arrivalNodeId : arrivalNodesIds) {

            arrivalNode = nodeRepository.getById(arrivalNodeId)
                    .orElseThrow(() -> new NodeNotFoundException(
                            "No se encontro el nodo con el ID: '" + arrivalNodeId + "'"
                    ));


            command = new CopyToAnotherNodeCommand(
                    originalNode.getRegionName().getValue(),
                    originalNode.getName().getValue(),
                    chunk.getKey().getValue(),
                    arrivalNode.getRegionName().getValue(),
                    arrivalNode.getName().getValue()
            );

            storageService.copyToAnotherNode(command);

            Chunk.create(
                    chunk.getFile(),
                    arrivalNode,
                    true,
                    chunk.getBytesSize(),
                    chunk.getFileHash(),
                    chunk.getKey(),
                    new Version("zz2"));
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

    private List<UUID> getDifferentsNodesIds(UUID nodeId){
        List<UUID> nodeIds = nodeRepository.getIdsOnline();

        if(nodeIds.size() < REPLICAITION_FACTOR - 1)
            throw new InsufficientNodesForReplicationException("No hay suficientes nodos para cumplir con el factor de replicacion. \n Nodos activos: "+nodeIds.size()+"\n Factor de replicacion: "+REPLICAITION_FACTOR);

        Collections.shuffle(nodeIds);

        return nodeIds.stream().filter((id)-> !nodeId.equals(id)).limit(REPLICAITION_FACTOR).toList();
    }

    private Queue<UUID> getRandomNodeAssignments(int length) {
        List<UUID> originalIds = nodeRepository.getIdsOnline();

        if (originalIds.isEmpty()) {
            throw new NoAvailableNodesException("No hay nodos disponibles para almacenar el archivo");
        }

        // Mezclar todos los nodos disponibles de forma aleatoria
        Collections.shuffle(originalIds);

        List<UUID> nodeIds = new ArrayList<>();
        int originalSize = originalIds.size();

        // Se llena la lista combinando los nodos disponibles en orden cíclico
        for (int i = 0; i < length; i++) {
            nodeIds.add(originalIds.get(i % originalSize));
        }

        // Retornar directamente como una Queue (ArrayDeque)
        return new ArrayDeque<>(nodeIds);
    }
}
