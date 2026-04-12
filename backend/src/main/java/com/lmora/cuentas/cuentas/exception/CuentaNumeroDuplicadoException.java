package com.lmora.cuentas.cuentas.exception;

public class CuentaNumeroDuplicadoException extends RuntimeException {

    public CuentaNumeroDuplicadoException(String numeroCuenta) {
        super("Ya existe una cuenta con el numero " + numeroCuenta);
    }
}
