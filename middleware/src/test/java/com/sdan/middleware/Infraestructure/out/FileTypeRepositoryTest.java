package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.ValueObjects.FileTypeName;
import com.sdan.middleware.Infraestructure.out.Entities.FileTypeEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.FileTypeMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileTypeRepositoryTest {

    @Mock
    private SpringDataFileTypeRepository springDataFileTypeRepository;

    private FileTypeMapper fileTypeMapper;
    private FileTypeRepository fileTypeRepository;

    @BeforeEach
    void setUp() {
        fileTypeMapper = new FileTypeMapper();
        fileTypeRepository = new FileTypeRepository(springDataFileTypeRepository, fileTypeMapper);
    }

    @Test
    void testGetById_found() {
        UUID id = UUID.randomUUID();
        FileTypeEntity entity = new FileTypeEntity(id, "text/plain", "Text file");
        when(springDataFileTypeRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<FileType> result = fileTypeRepository.getById(id);

        assertTrue(result.isPresent());
        assertEquals("text/plain", result.get().getName().getValue());
    }

    @Test
    void testSave() {
        FileType fileType = FileType.create(new FileTypeName("image/png"), "PNG image");

        fileTypeRepository.save(fileType);

        verify(springDataFileTypeRepository, times(1)).save(any(FileTypeEntity.class));
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        fileTypeRepository.deleteById(id);

        verify(springDataFileTypeRepository, times(1)).deleteById(id);
    }
}

