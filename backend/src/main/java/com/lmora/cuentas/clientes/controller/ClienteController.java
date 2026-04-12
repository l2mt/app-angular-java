package com.lmora.cuentas.clientes.controller;

import com.lmora.cuentas.clientes.dto.ClientePatchRequestDto;
import com.lmora.cuentas.clientes.dto.ClienteRequestDto;
import com.lmora.cuentas.clientes.dto.ClienteResponseDto;
import com.lmora.cuentas.clientes.mapper.ClienteMapper;
import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.service.ClienteService;
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
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDto>> listarClientes() {
        List<ClienteResponseDto> clientes = clienteService.listarClientes().stream()
                .map(clienteMapper::toResponse)
                .toList();

        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDto> obtenerClientePorId(@PathVariable Long clienteId) {
        Cliente cliente = clienteService.obtenerClientePorId(clienteId);
        return ResponseEntity.ok(clienteMapper.toResponse(cliente));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDto> crearCliente(@Valid @RequestBody ClienteRequestDto requestDto) {
        Cliente clienteCreado = clienteService.crearCliente(clienteMapper.toEntity(requestDto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{clienteId}")
                .buildAndExpand(clienteCreado.getPersonaId())
                .toUri();

        return ResponseEntity.created(location).body(clienteMapper.toResponse(clienteCreado));
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDto> actualizarCliente(
            @PathVariable Long clienteId,
            @Valid @RequestBody ClienteRequestDto requestDto
    ) {
        Cliente clienteActualizado = clienteService.actualizarCliente(clienteId, clienteMapper.toEntity(requestDto));
        return ResponseEntity.ok(clienteMapper.toResponse(clienteActualizado));
    }

    @PatchMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDto> actualizarParcialCliente(
            @PathVariable Long clienteId,
            @Valid @RequestBody ClientePatchRequestDto requestDto
    ) {
        Cliente clienteActualizado = clienteService.actualizarParcialCliente(
                clienteId,
                clienteMapper.toEntity(requestDto)
        );
        return ResponseEntity.ok(clienteMapper.toResponse(clienteActualizado));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long clienteId) {
        clienteService.eliminarCliente(clienteId);
        return ResponseEntity.noContent().build();
    }
}
