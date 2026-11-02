package com.sdan.middleware.Application.Outputs;

import java.util.List;
import java.util.UUID;

public record IntegrityCheckReport(
        int totalFilesChecked,
        int healthyFilesCount,
        int healedFilesCount,
        int corruptedFilesCount,
        List<FileAuditIssue> issues
) {
    public record FileAuditIssue(
            UUID fileId,
            String fileName,
            String errorMessage
    ) {}
}

