package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Domain.Exceptions.File.FileProcessingException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class Sha256FileHasher implements IFileHasher {

    private static final String ALGORITHM = "SHA-256";

    @Override
    public String Hash(InputStream file) {
        if (file == null) {
            throw new IllegalArgumentException("El flujo de archivo no puede ser nulo");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = file.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash no soportado: " + ALGORITHM, e);
        } catch (IOException e) {
            throw new FileProcessingException("Error al procesar el archivo para calcular el hash: " + e.getMessage());
        }
    }

    @Override
    public Boolean Verify(InputStream file, String fileHash) {
        if (file == null || fileHash == null || fileHash.isBlank()) {
            return false;
        }
        try {
            String calculatedHash = Hash(file);
            return calculatedHash.equalsIgnoreCase(fileHash.trim());
        } catch (Exception e) {
            return false;
        }
    }
}

