package com.sdan.middleware.BuildingBlocks.Domain;

import java.util.UUID;

public class Guard {
    public static final int LongitudMaximaPorDefecto = 250;

    public static <T> T notNull(T valor, String field){
        if(valor == null)
            throw new DomainException("El valor del campo "+field+" no puede ser nulo");
        return valor;
    }

    public static String validText(String value, String field, int longitudMaxima)
    {
        if(value == null)
            throw new DomainException("El campo "+field+" no puede ser nulo");

        value = value.strip();

        if (value.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");

        if (value.length() > longitudMaxima)
            throw new DomainException("El campo "+field+" admite máximo "+longitudMaxima+" caracteres.");

        return value;
    }

    public static UUID validUUID(UUID valor, String field){
        if(valor.toString().isEmpty())
            throw new DomainException("El campo "+field+" es obligatorio.");
        return valor;
    }

    public static <T extends Comparable<? super T>> T onRange(T value, T top, T bottom, String field) {

        if (value == null || top == null || bottom == null) {
            throw new DomainException("Los valores a comparar en el campo " + field + " no pueden ser nulos");
        }

        if (bottom.compareTo(top) > 0) {
            throw new DomainException("El límite inferior no puede ser mayor que el límite superior en el campo " + field);
        }

        if (value.compareTo(bottom) < 0 || value.compareTo(top) > 0) {
            throw new DomainException("El campo " + field + " debe estar entre " + bottom + " y " + top);
        }

        return value;
    }

    public static<T extends Number & Comparable<T>> T positive(T value, String field){
        if (value == null) {
            throw new DomainException("El campo " + field + " no puede ser nulo");
        }

        if(value.doubleValue() <= 0)
            throw new DomainException("El campo "+ field+" no puede ser negativo");

        return value;
    }

}
