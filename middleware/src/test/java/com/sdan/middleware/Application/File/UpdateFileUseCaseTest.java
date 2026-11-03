package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.UpdateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Application.Ports.Storage.IFileUpdateService;
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
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFileUseCaseTest {

    @Mock
    private ICurrentUser currentUser;
    @Mock
    private IFileRepository fileRepository;
    @Mock
    private IFileUpdateService fileUpdateService;

    private UpdateFileUseCase updateFileUseCase;

    private UUID loggedUserId;
    private User ownerUser;
    private User otherUser;
    private Folder ownerFolder;
    private FileType fileType;

    @BeforeEach
    void setUp() {
        updateFileUseCase = new UpdateFileUseCase(currentUser, fileRepository, fileUpdateService);

        loggedUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        ownerUser = User.reCreate(loggedUserId, new UserName("Owner"), new PasswordHash("pass123"), new Date());
        otherUser = User.reCreate(otherUserId, new UserName("Other"), new PasswordHash("pass123"), new Date());

        ownerFolder = Folder.create(ownerUser, null, new FolderName("Documents"));
        fileType = FileType.create(new FileTypeName("text/plain"), "Text document");
    }

    @Test
    void testExecute_nullCommand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> updateFileUseCase.execute(null));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_nullFileId_throwsIllegalArgumentException() {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        UpdateFileCommand cmd = new UpdateFileCommand(null, stream, 3L);

        assertThrows(IllegalArgumentException.class, () -> updateFileUseCase.execute(cmd));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_nullNewFile_throwsIllegalArgumentException() {
        UpdateFileCommand cmd = new UpdateFileCommand(UUID.randomUUID(), null, 3L);

        assertThrows(IllegalArgumentException.class, () -> updateFileUseCase.execute(cmd));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_nullNewSize_throwsIllegalArgumentException() {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        UpdateFileCommand cmd = new UpdateFileCommand(UUID.randomUUID(), stream, null);

        assertThrows(IllegalArgumentException.class, () -> updateFileUseCase.execute(cmd));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_negativeNewSize_throwsIllegalArgumentException() {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        UpdateFileCommand cmd = new UpdateFileCommand(UUID.randomUUID(), stream, -1L);

        assertThrows(IllegalArgumentException.class, () -> updateFileUseCase.execute(cmd));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_fileNotFound_throwsFileNotFoundException() {
        UUID fileId = UUID.randomUUID();
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        UpdateFileCommand cmd = new UpdateFileCommand(fileId, stream, 3L);

        when(currentUser.getUserId()).thenReturn(loggedUserId);
        when(fileRepository.getById(fileId)).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> updateFileUseCase.execute(cmd));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_fileBelongsToAnotherUser_throwsFileDoesNotBelongToUserException() {
        UUID fileId = UUID.randomUUID();
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        UpdateFileCommand cmd = new UpdateFileCommand(fileId, stream, 3L);

        when(currentUser.getUserId()).thenReturn(loggedUserId);

        Folder otherFolder = Folder.create(otherUser, null, new FolderName("OtherDocs"));
        File otherFile = File.create(otherFolder, fileType, new FileName("secret.txt"), 100L, 1);
        when(fileRepository.getById(fileId)).thenReturn(Optional.of(otherFile));

        assertThrows(FileDoesNotBelongToUserException.class, () -> updateFileUseCase.execute(cmd));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_fileWithoutFolder_throwsFileDoesNotBelongToUserException() {
        UUID fileId = UUID.randomUUID();
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        UpdateFileCommand cmd = new UpdateFileCommand(fileId, stream, 3L);

        when(currentUser.getUserId()).thenReturn(loggedUserId);

        File orphanFile = File.create(null, fileType, new FileName("orphan.txt"), 100L, 1);
        when(fileRepository.getById(fileId)).thenReturn(Optional.of(orphanFile));

        assertThrows(FileDoesNotBelongToUserException.class, () -> updateFileUseCase.execute(cmd));
        verifyNoInteractions(fileUpdateService);
    }

    @Test
    void testExecute_fileBelongsToUser_success_delegatesToFileUpdateService() {
        UUID fileId = UUID.randomUUID();
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        UpdateFileCommand cmd = new UpdateFileCommand(fileId, stream, 3L);

        when(currentUser.getUserId()).thenReturn(loggedUserId);

        File myFile = File.create(ownerFolder, fileType, new FileName("my_file.txt"), 100L, 1);
        when(fileRepository.getById(fileId)).thenReturn(Optional.of(myFile));

        assertDoesNotThrow(() -> updateFileUseCase.execute(cmd));

        verify(fileUpdateService, times(1)).updateFile(myFile, stream, 3L);
    }
}

