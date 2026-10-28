package com.sdan.middleware.Domain.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;
import com.sdan.middleware.BuildingBlocks.Domain.Guard;


public class FileHash {
    private static final int MAX_CHARACTERS = 64;

    private final String value;

    public FileHash(String value) {
        Guard.validText(value, "file_hash", MAX_CHARACTERS);

        if(value.length() != MAX_CHARACTERS)
            throw new DomainException("El campo 'FileHash' debe tener una longitud de 64 caracteres");

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileHash fileHash = (FileHash) o;
        return java.util.Objects.equals(value, fileHash.value);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
