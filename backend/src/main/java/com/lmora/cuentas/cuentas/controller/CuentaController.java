package com.lmora.cuentas.cuentas.controller;

import com.lmora.cuentas.cuentas.dto.CuentaPatchRequestDto;
import com.lmora.cuentas.cuentas.dto.CuentaRequestDto;
import com.lmora.cuentas.cuentas.dto.CuentaResponseDto;
import com.lmora.cuentas.cuentas.mapper.CuentaMapper;
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.service.CuentaService;
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
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;
    private final CuentaMapper cuentaMapper;

    @GetMapping
    public ResponseEntity<List<CuentaResponseDto>> listarCuentas() {
        List<CuentaResponseDto> cuentas = cuentaService.listarCuentas().stream()
                .map(cuentaMapper::toResponse)
                .toList();

        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponseDto> obtenerCuentaPorId(@PathVariable Long cuentaId) {
        Cuenta cuenta = cuentaService.obtenerCuentaPorId(cuentaId);
        return ResponseEntity.ok(cuentaMapper.toResponse(cuenta));
    }

    @PostMapping
    public ResponseEntity<CuentaResponseDto> crearCuenta(@Valid @RequestBody CuentaRequestDto requestDto) {
        Cuenta cuentaCreada = cuentaService.crearCuenta(requestDto.clienteId(), cuentaMapper.toEntity(requestDto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{cuentaId}")
                .buildAndExpand(cuentaCreada.getCuentaId())
                .toUri();

        return ResponseEntity.created(location).body(cuentaMapper.toResponse(cuentaCreada));
    }

    @PutMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponseDto> actualizarCuenta(
            @PathVariable Long cuentaId,
            @Valid @RequestBody CuentaRequestDto requestDto
    ) {
        Cuenta cuentaActualizada = cuentaService.actualizarCuenta(
                cuentaId,
                requestDto.clienteId(),
                cuentaMapper.toEntity(requestDto)
        );
        return ResponseEntity.ok(cuentaMapper.toResponse(cuentaActualizada));
    }

    @PatchMapping("/{cuentaId}")
    public ResponseEntity<CuentaResponseDto> actualizarParcialCuenta(
            @PathVariable Long cuentaId,
            @Valid @RequestBody CuentaPatchRequestDto requestDto
    ) {
        Cuenta cuentaActualizada = cuentaService.actualizarParcialCuenta(
                cuentaId,
                requestDto.clienteId(),
                cuentaMapper.toEntity(requestDto)
        );
        return ResponseEntity.ok(cuentaMapper.toResponse(cuentaActualizada));
    }

    @DeleteMapping("/{cuentaId}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long cuentaId) {
        cuentaService.eliminarCuenta(cuentaId);
        return ResponseEntity.noContent().build();
    }
}
