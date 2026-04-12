package com.lmora.cuentas.movimientos.mapper;

import com.lmora.cuentas.movimientos.dto.MovimientoPatchRequestDto;
import com.lmora.cuentas.movimientos.dto.MovimientoRequestDto;
import com.lmora.cuentas.movimientos.dto.MovimientoResponseDto;
import com.lmora.cuentas.movimientos.model.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public Movimiento toEntity(MovimientoRequestDto requestDto) {
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(requestDto.fecha());
        movimiento.setTipoMovimiento(requestDto.tipoMovimiento());
        movimiento.setValor(requestDto.valor());
        return movimiento;
    }

    public Movimiento toEntity(MovimientoPatchRequestDto requestDto) {
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(requestDto.fecha());
        movimiento.setTipoMovimiento(requestDto.tipoMovimiento());
        movimiento.setValor(requestDto.valor());
        return movimiento;
    }

    public MovimientoResponseDto toResponse(Movimiento movimiento) {
        return new MovimientoResponseDto(
                movimiento.getMovimientoId(),
                movimiento.getCuenta().getCuentaId(),
                movimiento.getFecha(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo()
        );
    }
}
