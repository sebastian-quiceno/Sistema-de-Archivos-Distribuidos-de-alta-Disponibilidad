package com.sdan.middleware.Application.Ports.Security;

import java.io.FileInputStream;

public interface IFileHasher {
    String Hash(FileInputStream file);
    Boolean Verify(FileInputStream file, String fileHash);
}
