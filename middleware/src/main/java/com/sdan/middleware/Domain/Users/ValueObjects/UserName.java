package com.sdan.middleware.Domain.Users.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;
import com.sdan.middleware.BuildingBlocks.Domain.Guard;
import com.sdan.middleware.Domain.Users.User;

public class UserName {
    private static final int MAX_CHARACTERS = 50;

    private final String value;

    public UserName(String value) {
        Guard.validText(value, "username", MAX_CHARACTERS);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserName userName = (UserName) o;
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
