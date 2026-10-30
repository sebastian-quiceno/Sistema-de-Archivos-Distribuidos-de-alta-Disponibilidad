package com.sdan.middleware.Application.Ports.Security;

import java.io.InputStream;

public interface IFileHasher {
    String Hash(InputStream file);
    Boolean Verify(InputStream file, String fileHash);
}
