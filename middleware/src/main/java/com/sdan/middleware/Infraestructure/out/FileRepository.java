package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Infraestructure.out.Entities.FileEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FileTypeEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.ChunkMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.FileMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.FileTypeMapper;
import com.sdan.middleware.Infraestructure.out.Mappers.FolderMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataChunkRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileTypeRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFolderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileRepository implements IFileRepository {

    private final SpringDataFileRepository springDataFileRepository;
    private final SpringDataFolderRepository springDataFolderRepository;
    private final SpringDataFileTypeRepository springDataFileTypeRepository;
    private final SpringDataChunkRepository springDataChunkRepository;
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;
    private final FileTypeMapper fileTypeMapper;
    private final ChunkMapper chunkMapper;

    public FileRepository(
            SpringDataFileRepository springDataFileRepository,
            SpringDataFolderRepository springDataFolderRepository,
            SpringDataFileTypeRepository springDataFileTypeRepository,
            SpringDataChunkRepository springDataChunkRepository,
            FileMapper fileMapper,
            FolderMapper folderMapper,
            FileTypeMapper fileTypeMapper,
            ChunkMapper chunkMapper) {
        this.springDataFileRepository = springDataFileRepository;
        this.springDataFolderRepository = springDataFolderRepository;
        this.springDataFileTypeRepository = springDataFileTypeRepository;
        this.springDataChunkRepository = springDataChunkRepository;
        this.fileMapper = fileMapper;
        this.folderMapper = folderMapper;
        this.fileTypeMapper = fileTypeMapper;
        this.chunkMapper = chunkMapper;
    }

    @Override
    public Optional<File> getById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataFileRepository.findById(id).map(fileMapper::toDomain);
    }

    @Override
    public List<File> getAll() {
        return springDataFileRepository.findAll().stream()
                .map(fileMapper::toDomain)
                .toList();
    }

    @Override
    public List<File> findPage(int page, int size) {
        if (page < 0 || size <= 0) {
            return Collections.emptyList();
        }
        return springDataFileRepository.findPagedFiles(PageRequest.of(page, size)).stream()
                .map(fileMapper::toDomain)
                .toList();
    }

    @Override
    public List<Chunk> getOriginalsChunks(UUID fileId) {
        if (fileId == null) {
            return Collections.emptyList();
        }
        return springDataChunkRepository.findOriginalChunksByFileId(fileId).stream()
                .map(chunkMapper::toDomain)
                .toList();
    }

    @Override
    public void save(File file) {
        if (file == null) {
            return;
        }

        FolderEntity folderEntity = null;
        if (file.getFolder() != null && file.getFolder().getFolderId() != null) {
            folderEntity = springDataFolderRepository.findById(file.getFolder().getFolderId())
                    .orElse(null);
        }

        FileTypeEntity fileTypeEntity = null;
        if (file.getFileType() != null && file.getFileType().getId() != null) {
            fileTypeEntity = springDataFileTypeRepository.findById(file.getFileType().getId())
                    .orElseGet(() -> springDataFileTypeRepository.save(fileTypeMapper.toEntity(file.getFileType())));
        }

        FileEntity entity = fileMapper.toEntity(file, folderEntity, fileTypeEntity);
        springDataFileRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        springDataFileRepository.deleteById(id);
    }
}

