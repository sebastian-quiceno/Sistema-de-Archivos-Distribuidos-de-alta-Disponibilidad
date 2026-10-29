package com.sdan.middleware.Application.Ports.Storage;

import com.sdan.middleware.Application.Outputs.PresignedUrlOutput;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.FileName;

public interface IStorageService {
    PresignedUrlOutput generateUploadUrl(FileName fileName);

    PresignedUrlOutput generateDownLoadUrl(FileKey key);

    //TODO: Mirar si Actualizar los archivos se cambia lo nunevo o se borran y vuelven a subir

    void deleteFileByKey(FileKey fileKey);

}
