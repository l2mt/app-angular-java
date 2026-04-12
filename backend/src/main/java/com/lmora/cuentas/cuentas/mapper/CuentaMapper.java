package com.lmora.cuentas.cuentas.mapper;

import com.lmora.cuentas.cuentas.dto.CuentaPatchRequestDto;
import com.lmora.cuentas.cuentas.dto.CuentaRequestDto;
import com.lmora.cuentas.cuentas.dto.CuentaResponseDto;
import com.lmora.cuentas.cuentas.model.Cuenta;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaRequestDto requestDto) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(requestDto.numeroCuenta());
        cuenta.setTipoCuenta(requestDto.tipoCuenta());
        cuenta.setSaldoInicial(requestDto.saldoInicial());
        cuenta.setEstado(requestDto.estado());
        return cuenta;
    }

    public Cuenta toEntity(CuentaPatchRequestDto requestDto) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(requestDto.numeroCuenta());
        cuenta.setTipoCuenta(requestDto.tipoCuenta());
        cuenta.setSaldoInicial(requestDto.saldoInicial());
        cuenta.setEstado(requestDto.estado());
        return cuenta;
    }

    public CuentaResponseDto toResponse(Cuenta cuenta) {
        return new CuentaResponseDto(
                cuenta.getCuentaId(),
                cuenta.getCliente().getPersonaId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getEstado()
        );
    }
}
