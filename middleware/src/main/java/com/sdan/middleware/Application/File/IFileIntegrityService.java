package com.sdan.middleware.Application.File;

import com.sdan.middleware.Domain.Entities.File;

public interface IFileIntegrityService {
    FileAuditStatus verifyAndHealFile(File file);
}

