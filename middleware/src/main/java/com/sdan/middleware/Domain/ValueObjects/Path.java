package com.sdan.middleware.Domain.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;

public class Path {
    private static final int MAX_CHARACTERS = 50;

    private final String value;

    public Path(String value) {
        if(value.isBlank())
            throw new DomainException("El valor del campo path no puede estar vacio");

        if(!value.startsWith("/"))
            throw new DomainException("El path tiene que ser una ruta absoluta");

        if(containsControlCharacters(value))
            throw new DomainException("El path no puede contener caracteres de control");

        if(!value.equals("/") && value.endsWith("/"))
            throw new DomainException("El path no puede terminar con '/'");

        if(value.contains("//"))
            throw new DomainException("El path no puede contener segmentos vacios");

        for(String segment : value.split("/")){
            if(segment.equals("..") || segment.equals("."))
                throw new DomainException("El path no puede tener segmentos con '.' o '..'");
        }

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static boolean containsControlCharacters(String path) {
        return path.codePoints().anyMatch(Character::isISOControl);
    }

    public static Path of(String value) {
        return new Path(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path userName = (Path) o;
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
