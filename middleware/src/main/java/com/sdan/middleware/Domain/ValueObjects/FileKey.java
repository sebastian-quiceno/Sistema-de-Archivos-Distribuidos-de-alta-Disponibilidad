package com.sdan.middleware.Domain.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;

public class FileKey {
    private static final int MIN_CHARACTERS = 36;
    private static final int MAX_CHARACTERS = 116;

    private final String value;

    public FileKey(String value) {
        Guard.notNull(value, "file_key");
        Guard.onRange(value.length(), MAX_CHARACTERS, MIN_CHARACTERS, "file_key");

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKey fileKey = (FileKey) o;
        return java.util.Objects.equals(value, fileKey.value);
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
