package com.sdan.middleware.Domain.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;

public class Version {
    private static final int MAX_CHARACTERS = 10;

    private final String value;

    public Version(String value) {
        Guard.validText(value, "version", MAX_CHARACTERS);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return java.util.Objects.equals(value, version.value);
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
