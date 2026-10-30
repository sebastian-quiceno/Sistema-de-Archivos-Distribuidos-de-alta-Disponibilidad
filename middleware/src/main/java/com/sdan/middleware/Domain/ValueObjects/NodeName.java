package com.sdan.middleware.Domain.ValueObjects;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;
import com.sdan.middleware.BuildingBlocks.Domain.Guard;

import java.util.Set;

public class NodeName {
    private static final int MAX_CHARACTERS = 50;
    private static final Set<Character> CARACTERES_INVALIDOS = Set.of('<', '>', ':', '"', '/', '\\', '|', '?', '*');


    private final String value;

    public NodeName(String value) {
        Guard.validText(value, "node_name", MAX_CHARACTERS);

        if(tieneCaracteresInvalidos(value))
            throw new DomainException("El campo 'node_name' no puede contener caracteres de control");

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean tieneCaracteresInvalidos(String texto) {
        return texto != null
                && texto.chars()
                .mapToObj(c -> (char) c)
                .anyMatch(CARACTERES_INVALIDOS::contains);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeName nodeName = (NodeName) o;
        return java.util.Objects.equals(value, nodeName.value);
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
