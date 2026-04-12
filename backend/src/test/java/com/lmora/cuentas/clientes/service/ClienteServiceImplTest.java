package com.lmora.cuentas.clientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lmora.cuentas.clientes.exception.ClienteIdentificacionDuplicadaException;
import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.model.Genero;
import com.lmora.cuentas.clientes.repository.ClienteRepository;
import com.lmora.cuentas.cuentas.repository.CuentaRepository;
import com.lmora.cuentas.shared.exception.BusinessConflictException;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Test
    void crearCliente_cuandoIdentificacionExiste_lanzaExcepcion() {
        Cliente cliente = crearCliente();

        when(clienteRepository.existsByIdentificacion("1234567890")).thenReturn(true);

        assertThrows(ClienteIdentificacionDuplicadaException.class, () -> clienteService.crearCliente(cliente));

        verify(clienteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void crearCliente_cuandoIdentificacionEsNueva_guardaConContrasenaCodificada() {
        Cliente cliente = crearCliente();

        when(clienteRepository.existsByIdentificacion("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hash-1234");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente clienteGuardado = clienteService.crearCliente(cliente);

        assertEquals("hash-1234", clienteGuardado.getContrasena());
        assertNotEquals("1234", clienteGuardado.getContrasena());
        verify(passwordEncoder).encode("1234");
        verify(clienteRepository).save(cliente);
    }

    @Test
    void obtenerClientePorId_cuandoNoExiste_lanzaExcepcion() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteService.obtenerClientePorId(99L)
        );

        assertEquals("Cliente no encontrado con id 99", exception.getMessage());
    }

    @Test
    void actualizarParcialCliente_cuandoNoLlegaContrasena_noModificaContrasena() {
        Cliente clienteExistente = crearCliente();
        clienteExistente.setPersonaId(1L);
        clienteExistente.setContrasena("hash-anterior");

        Cliente patch = new Cliente();
        patch.setTelefono("0970001111");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente clienteActualizado = clienteService.actualizarParcialCliente(1L, patch);

        assertEquals("0970001111", clienteActualizado.getTelefono());
        assertEquals("hash-anterior", clienteActualizado.getContrasena());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void eliminarCliente_cuandoTieneCuentasAsociadas_lanzaExcepcion() {
        Cliente cliente = crearCliente();
        cliente.setPersonaId(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.existsByClientePersonaId(1L)).thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> clienteService.eliminarCliente(1L)
        );

        assertEquals("No se puede eliminar el cliente porque tiene cuentas asociadas", exception.getMessage());
        verify(clienteRepository, never()).delete(any());
    }

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Jose Lema");
        cliente.setGenero(Genero.MASCULINO);
        cliente.setEdad(30);
        cliente.setIdentificacion("1234567890");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("0987654321");
        cliente.setContrasena("1234");
        cliente.setEstado(true);
        return cliente;
    }
}
