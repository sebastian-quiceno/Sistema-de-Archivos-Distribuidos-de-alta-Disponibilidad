package com.sdan.middleware.Application.Ports.Storage;

import com.sdan.middleware.BuildingBlocks.Application.FileAuditStatus;
import com.sdan.middleware.Domain.Entities.File;

public interface IFileIntegrityService {
    FileAuditStatus verifyAndHealFile(File file);
}

