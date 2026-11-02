package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.IntegrityCheckReport;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Storage.IFileIntegrityService;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.BuildingBlocks.Application.FileAuditStatus;

import java.util.ArrayList;
import java.util.List;

public class CheckFilesUseCase {
    public static final int DEFAULT_PAGE_SIZE = 50;

    private final IFileRepository fileRepository;
    private final IFileIntegrityService fileIntegrityService;

    public CheckFilesUseCase(IFileRepository fileRepository, IFileIntegrityService fileIntegrityService) {
        this.fileRepository = fileRepository;
        this.fileIntegrityService = fileIntegrityService;
    }

    public IntegrityCheckReport execute() {
        return execute(DEFAULT_PAGE_SIZE);
    }

    public IntegrityCheckReport execute(int pageSize) {
        int effectivePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        int totalFilesChecked = 0;
        int healthyFilesCount = 0;
        int healedFilesCount = 0;
        int corruptedFilesCount = 0;
        List<IntegrityCheckReport.FileAuditIssue> issues = new ArrayList<>();

        int currentPage = 0;
        List<File> pageFiles;

        do {
            pageFiles = fileRepository.findPage(currentPage, effectivePageSize);
            if (pageFiles == null || pageFiles.isEmpty()) {
                break;
            }

            for (File file : pageFiles) {
                totalFilesChecked++;
                try {
                    FileAuditStatus status = fileIntegrityService.verifyAndHealFile(file);
                    switch (status) {
                        case HEALTHY -> healthyFilesCount++;
                        case HEALED -> healedFilesCount++;
                        case UNRECOVERABLE -> {
                            corruptedFilesCount++;
                            issues.add(new IntegrityCheckReport.FileAuditIssue(
                                    file.getId(),
                                    file.getName() != null ? file.getName().getValue() : "Desconocido",
                                    "El archivo contiene fragmentos corruptos que no pudieron recuperarse"
                            ));
                        }
                    }
                } catch (Exception e) {
                    corruptedFilesCount++;
                    issues.add(new IntegrityCheckReport.FileAuditIssue(
                            file.getId(),
                            file.getName() != null ? file.getName().getValue() : "Desconocido",
                            e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()
                    ));
                }
            }

            currentPage++;
        } while (pageFiles.size() == effectivePageSize);

        return new IntegrityCheckReport(
                totalFilesChecked,
                healthyFilesCount,
                healedFilesCount,
                corruptedFilesCount,
                issues
        );
    }
}
