package com.lmora.cuentas.shared.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object value) {
        super(resourceName + " no encontrado con " + fieldName + " " + value);
    }
}
