package com.lmora.cuentas.movimientos.controller;

import com.lmora.cuentas.movimientos.dto.MovimientoPatchRequestDto;
import com.lmora.cuentas.movimientos.dto.MovimientoRequestDto;
import com.lmora.cuentas.movimientos.dto.MovimientoResponseDto;
import com.lmora.cuentas.movimientos.mapper.MovimientoMapper;
import com.lmora.cuentas.movimientos.model.Movimiento;
import com.lmora.cuentas.movimientos.service.MovimientoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final MovimientoMapper movimientoMapper;

    @GetMapping
    public ResponseEntity<List<MovimientoResponseDto>> listarMovimientos() {
        List<MovimientoResponseDto> movimientos = movimientoService.listarMovimientos().stream()
                .map(movimientoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/{movimientoId}")
    public ResponseEntity<MovimientoResponseDto> obtenerMovimientoPorId(@PathVariable Long movimientoId) {
        Movimiento movimiento = movimientoService.obtenerMovimientoPorId(movimientoId);
        return ResponseEntity.ok(movimientoMapper.toResponse(movimiento));
    }

    @PostMapping
    public ResponseEntity<MovimientoResponseDto> crearMovimiento(@Valid @RequestBody MovimientoRequestDto requestDto) {
        Movimiento movimientoCreado = movimientoService.crearMovimiento(
                requestDto.cuentaId(),
                movimientoMapper.toEntity(requestDto)
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{movimientoId}")
                .buildAndExpand(movimientoCreado.getMovimientoId())
                .toUri();

        return ResponseEntity.created(location).body(movimientoMapper.toResponse(movimientoCreado));
    }

    @PutMapping("/{movimientoId}")
    public ResponseEntity<MovimientoResponseDto> actualizarMovimiento(
            @PathVariable Long movimientoId,
            @Valid @RequestBody MovimientoRequestDto requestDto
    ) {
        Movimiento movimientoActualizado = movimientoService.actualizarMovimiento(
                movimientoId,
                requestDto.cuentaId(),
                movimientoMapper.toEntity(requestDto)
        );
        return ResponseEntity.ok(movimientoMapper.toResponse(movimientoActualizado));
    }

    @PatchMapping("/{movimientoId}")
    public ResponseEntity<MovimientoResponseDto> actualizarParcialMovimiento(
            @PathVariable Long movimientoId,
            @Valid @RequestBody MovimientoPatchRequestDto requestDto
    ) {
        Movimiento movimientoActualizado = movimientoService.actualizarParcialMovimiento(
                movimientoId,
                requestDto.cuentaId(),
                movimientoMapper.toEntity(requestDto)
        );
        return ResponseEntity.ok(movimientoMapper.toResponse(movimientoActualizado));
    }

    @DeleteMapping("/{movimientoId}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long movimientoId) {
        movimientoService.eliminarMovimiento(movimientoId);
        return ResponseEntity.noContent().build();
    }
}
