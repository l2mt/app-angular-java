package com.lmora.cuentas.movimientos.exception;

public class SaldoNoDisponibleException extends RuntimeException {

    public SaldoNoDisponibleException() {
        super("Saldo no disponible");
    }
}
