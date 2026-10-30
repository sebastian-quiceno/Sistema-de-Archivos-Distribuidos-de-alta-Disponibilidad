package com.sdan.middleware.Application.Ports.Storage;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;
import com.sdan.middleware.Application.Outputs.PresignedUrlOutput;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.FileName;

import java.io.InputStream;
import java.util.UUID;

public interface IStorageService {
    PresignedUrlOutput generateUploadUrl(FileName fileName);

    PresignedUrlOutput generateDownLoadUrl(FileKey key);

    //TODO: Mirar si Actualizar los archivos se cambia lo nunevo o se borran y vuelven a subir
    void uploadObject(String key, InputStream content, long contentLength, String contentType);

    void copyToAnotherNode(CopyToAnotherNodeCommand dto);

    InputStream getObject(String key);

    void deleteFileByKey(FileKey fileKey);

}
