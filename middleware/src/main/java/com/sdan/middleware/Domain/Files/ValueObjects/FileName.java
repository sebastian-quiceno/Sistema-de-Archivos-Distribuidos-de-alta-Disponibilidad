package com.sdan.middleware.Domain.Files.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;

public class FileName {
    private static final int MAX_CHARACTERS = 50;

    private final String value;

    public FileName(String value) {
        Guard.validText(value, "file_name", MAX_CHARACTERS);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileName userName = (FileName) o;
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
