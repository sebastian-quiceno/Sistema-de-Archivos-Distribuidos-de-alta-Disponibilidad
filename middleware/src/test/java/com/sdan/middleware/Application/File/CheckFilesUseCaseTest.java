package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.IntegrityCheckReport;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Storage.IFileIntegrityService;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Domain.ValueObjects.FileTypeName;
import com.sdan.middleware.Domain.ValueObjects.FolderName;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.BuildingBlocks.Application.FileAuditStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckFilesUseCaseTest {

    @Mock
    private IFileRepository fileRepository;
    @Mock
    private IFileIntegrityService fileIntegrityService;

    private CheckFilesUseCase checkFilesUseCase;
    private FileType fileType;
    private Folder folder;

    @BeforeEach
    void setUp() {
        checkFilesUseCase = new CheckFilesUseCase(fileRepository, fileIntegrityService);

        User user = User.reCreate(UUID.randomUUID(), new UserName("User"), new PasswordHash("pass123"), new java.util.Date());
        folder = Folder.create(user, null, new FolderName("Folder"));
        fileType = FileType.create(new FileTypeName("text/plain"), "doc");
    }

    @Test
    void testExecute_emptyRepository_returnsEmptyReport() {
        when(fileRepository.findPage(eq(0), anyInt())).thenReturn(Collections.emptyList());

        IntegrityCheckReport report = checkFilesUseCase.execute();

        assertEquals(0, report.totalFilesChecked());
        assertEquals(0, report.healthyFilesCount());
        assertEquals(0, report.healedFilesCount());
        assertEquals(0, report.corruptedFilesCount());
        assertTrue(report.issues().isEmpty());
    }

    @Test
    void testExecute_pagedProcessing_collectsStatsAccurately() {
        File file1 = File.create(folder, fileType, new FileName("file1.txt"), 100L, 1);
        File file2 = File.create(folder, fileType, new FileName("file2.txt"), 200L, 1);
        File file3 = File.create(folder, fileType, new FileName("file3.txt"), 300L, 1);

        // Page size = 2: Page 0 returns [file1, file2], Page 1 returns [file3]
        when(fileRepository.findPage(0, 2)).thenReturn(List.of(file1, file2));
        when(fileRepository.findPage(1, 2)).thenReturn(List.of(file3));

        when(fileIntegrityService.verifyAndHealFile(file1)).thenReturn(FileAuditStatus.HEALTHY);
        when(fileIntegrityService.verifyAndHealFile(file2)).thenReturn(FileAuditStatus.HEALED);
        when(fileIntegrityService.verifyAndHealFile(file3)).thenReturn(FileAuditStatus.UNRECOVERABLE);

        IntegrityCheckReport report = checkFilesUseCase.execute(2);

        assertEquals(3, report.totalFilesChecked());
        assertEquals(1, report.healthyFilesCount());
        assertEquals(1, report.healedFilesCount());
        assertEquals(1, report.corruptedFilesCount());
        assertEquals(1, report.issues().size());
        assertEquals(file3.getId(), report.issues().get(0).fileId());
    }

    @Test
    void testExecute_individualFileError_doesNotAbortExecution() {
        File file1 = File.create(folder, fileType, new FileName("bad.txt"), 100L, 1);
        File file2 = File.create(folder, fileType, new FileName("good.txt"), 200L, 1);

        when(fileRepository.findPage(0, 50)).thenReturn(List.of(file1, file2));

        when(fileIntegrityService.verifyAndHealFile(file1)).thenThrow(new RuntimeException("Simulated disk error"));
        when(fileIntegrityService.verifyAndHealFile(file2)).thenReturn(FileAuditStatus.HEALTHY);

        IntegrityCheckReport report = checkFilesUseCase.execute(50);

        assertEquals(2, report.totalFilesChecked());
        assertEquals(1, report.healthyFilesCount());
        assertEquals(1, report.corruptedFilesCount());
        assertEquals(1, report.issues().size());
        assertTrue(report.issues().get(0).errorMessage().contains("Simulated disk error"));
    }
}
