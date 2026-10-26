package com.sdan.middleware.Domain.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;

public class FileTypeName {
    private static final int MAX_CHARACTERS = 20;

    private final String value;

    public FileTypeName(String value) {
        Guard.validText(value, "FileTypeName", MAX_CHARACTERS);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileTypeName fileTypeName = (FileTypeName) o;
        return java.util.Objects.equals(value, fileTypeName.value);
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
