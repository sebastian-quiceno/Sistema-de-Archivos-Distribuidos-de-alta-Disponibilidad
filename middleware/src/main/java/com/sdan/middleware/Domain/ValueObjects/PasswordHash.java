package com.sdan.middleware.Domain.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;

public class PasswordHash {
    private static final int MAX_CHARACTERS = 255;

    private final String value;

    public PasswordHash(String value) {
        Guard.validText(value, "passwordHash", MAX_CHARACTERS);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordHash userName = (PasswordHash) o;
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
