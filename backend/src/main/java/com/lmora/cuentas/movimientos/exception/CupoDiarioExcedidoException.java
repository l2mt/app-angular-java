package com.lmora.cuentas.movimientos.exception;

public class CupoDiarioExcedidoException extends RuntimeException {

    public CupoDiarioExcedidoException() {
        super("Cupo diario excedido");
    }
}
