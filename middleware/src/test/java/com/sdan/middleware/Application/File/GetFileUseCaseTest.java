package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Domain.ValueObjects.FileTypeName;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFileUseCaseTest {

    @Mock
    private ICurrentUser currentUser;
    @Mock
    private IFileRepository fileRepository;
    @Mock
    private IFileReconstructionService fileReconstructionService;

    private GetFileUseCase getFileUseCase;

    private UUID loggedUserId;
    private User ownerUser;
    private User otherUser;
    private Folder ownerFolder;
    private FileType fileType;

    @BeforeEach
    void setUp() {
        getFileUseCase = new GetFileUseCase(currentUser, fileRepository, fileReconstructionService);

        loggedUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        ownerUser = User.reCreate(loggedUserId, new UserName("Owner"), new PasswordHash("pass123"), new java.util.Date());
        otherUser = User.reCreate(otherUserId, new UserName("Other"), new PasswordHash("pass123"), new java.util.Date());

        ownerFolder = Folder.create(ownerUser, null, new FolderName("Documents"));
        fileType = FileType.create(new FileTypeName("text/plain"), "Text document");
    }

    @Test
    void testExecute_fileNotFound_throwsException() {
        UUID fileId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(loggedUserId);
        when(fileRepository.getById(fileId)).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> getFileUseCase.execute(fileId));
        verify(fileReconstructionService, never()).mergeFile(any());
    }

    @Test
    void testExecute_fileBelongsToAnotherUser_throwsFileDoesNotBelongToUserException() {
        UUID fileId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(loggedUserId);

        Folder otherFolder = Folder.create(otherUser, null, new FolderName("OtherDocs"));
        File otherFile = File.create(otherFolder, fileType, new FileName("secret.txt"), 100L, 1);

        when(fileRepository.getById(fileId)).thenReturn(Optional.of(otherFile));

        assertThrows(FileDoesNotBelongToUserException.class, () -> getFileUseCase.execute(fileId));
        verify(fileReconstructionService, never()).mergeFile(any());
    }

    @Test
    void testExecute_fileWithoutFolderOrOwner_doesNotThrowNPE_andRejects() {
        UUID fileId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(loggedUserId);

        File fileWithoutFolder = File.create(null, fileType, new FileName("orphan.txt"), 100L, 1);
        when(fileRepository.getById(fileId)).thenReturn(Optional.of(fileWithoutFolder));

        assertThrows(FileDoesNotBelongToUserException.class, () -> getFileUseCase.execute(fileId));
        verify(fileReconstructionService, never()).mergeFile(any());
    }

    @Test
    void testExecute_fileBelongsToUser_success() {
        UUID fileId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(loggedUserId);

        File myFile = File.create(ownerFolder, fileType, new FileName("my_file.txt"), 100L, 1);
        when(fileRepository.getById(fileId)).thenReturn(Optional.of(myFile));

        InputStream expectedStream = new ByteArrayInputStream("hello world".getBytes());
        when(fileReconstructionService.mergeFile(myFile)).thenReturn(expectedStream);

        InputStream result = getFileUseCase.execute(fileId);

        assertNotNull(result);
        assertEquals(expectedStream, result);
        verify(fileReconstructionService, times(1)).mergeFile(myFile);
    }
}
