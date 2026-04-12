package com.lmora.cuentas.clientes.exception;

public class ClienteIdentificacionDuplicadaException extends RuntimeException {

    public ClienteIdentificacionDuplicadaException(String identificacion) {
        super("Ya existe un cliente con la identificacion " + identificacion);
    }
}
