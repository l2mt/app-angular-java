package com.lmora.cuentas.clientes.service;

import com.lmora.cuentas.clientes.exception.ClienteIdentificacionDuplicadaException;
import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.repository.ClienteRepository;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerClientePorId(Long clienteId) {
        return buscarClientePorId(clienteId);
    }

    @Override
    @Transactional
    public Cliente crearCliente(Cliente cliente) {
        validarIdentificacionDisponible(cliente.getIdentificacion());
        cliente.setContrasena(passwordEncoder.encode(cliente.getContrasena()));
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente actualizarCliente(Long clienteId, Cliente cliente) {
        Cliente clienteExistente = buscarClientePorId(clienteId);
        validarIdentificacionDisponibleParaActualizacion(cliente.getIdentificacion(), clienteId);

        actualizarDatosCompletos(clienteExistente, cliente);

        return clienteRepository.save(clienteExistente);
    }

    @Override
    @Transactional
    public Cliente actualizarParcialCliente(Long clienteId, Cliente cliente) {
        Cliente clienteExistente = buscarClientePorId(clienteId);

        if (cliente.getIdentificacion() != null) {
            validarIdentificacionDisponibleParaActualizacion(cliente.getIdentificacion(), clienteId);
        }

        actualizarDatosParciales(clienteExistente, cliente);

        return clienteRepository.save(clienteExistente);
    }

    @Override
    @Transactional
    public void eliminarCliente(Long clienteId) {
        Cliente cliente = buscarClientePorId(clienteId);
        clienteRepository.delete(cliente);
    }

    private Cliente buscarClientePorId(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
    }

    private void validarIdentificacionDisponible(String identificacion) {
        if (clienteRepository.existsByIdentificacion(identificacion)) {
            throw new ClienteIdentificacionDuplicadaException(identificacion);
        }
    }

    private void validarIdentificacionDisponibleParaActualizacion(String identificacion, Long clienteId) {
        if (clienteRepository.existsByIdentificacionAndPersonaIdNot(identificacion, clienteId)) {
            throw new ClienteIdentificacionDuplicadaException(identificacion);
        }
    }

    private void actualizarDatosCompletos(Cliente clienteExistente, Cliente cliente) {
        clienteExistente.setNombre(cliente.getNombre());
        clienteExistente.setGenero(cliente.getGenero());
        clienteExistente.setEdad(cliente.getEdad());
        clienteExistente.setIdentificacion(cliente.getIdentificacion());
        clienteExistente.setDireccion(cliente.getDireccion());
        clienteExistente.setTelefono(cliente.getTelefono());
        clienteExistente.setContrasena(passwordEncoder.encode(cliente.getContrasena()));
        clienteExistente.setEstado(cliente.getEstado());
    }

    private void actualizarDatosParciales(Cliente clienteExistente, Cliente cliente) {
        if (cliente.getNombre() != null) {
            clienteExistente.setNombre(cliente.getNombre());
        }

        if (cliente.getGenero() != null) {
            clienteExistente.setGenero(cliente.getGenero());
        }

        if (cliente.getEdad() != null) {
            clienteExistente.setEdad(cliente.getEdad());
        }

        if (cliente.getIdentificacion() != null) {
            clienteExistente.setIdentificacion(cliente.getIdentificacion());
        }

        if (cliente.getDireccion() != null) {
            clienteExistente.setDireccion(cliente.getDireccion());
        }

        if (cliente.getTelefono() != null) {
            clienteExistente.setTelefono(cliente.getTelefono());
        }

        if (cliente.getContrasena() != null) {
            clienteExistente.setContrasena(passwordEncoder.encode(cliente.getContrasena()));
        }

        if (cliente.getEstado() != null) {
            clienteExistente.setEstado(cliente.getEstado());
        }
    }
}
