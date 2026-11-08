package com.sdan.middleware.Application.Ports.Storage;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;

import java.io.InputStream;

public interface IStorageService {
    void uploadObject(String nodeName, String region, String key, InputStream content, long contentLength, String contentType);

    void copyToAnotherNode(CopyToAnotherNodeCommand dto);

    InputStream getObject(String nodeName, String region, String key);

    void deleteFileByKey(String nodeName, String region, String key);
}
