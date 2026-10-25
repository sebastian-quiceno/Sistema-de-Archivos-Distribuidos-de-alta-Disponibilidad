package com.sdan.middleware.Domain.Folders.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;

public class FolderName {
    private static final int MAX_CHARACTERS = 20;

    private final String value;

    public FolderName(String value) {
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
        FolderName userName = (FolderName) o;
        return java.util.Objects.equals(value, userName.value);
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
