package com.lmora.cuentas.cuentas.service;

import com.lmora.cuentas.clientes.model.Cliente;
import com.lmora.cuentas.clientes.repository.ClienteRepository;
import com.lmora.cuentas.cuentas.exception.CuentaNumeroDuplicadoException;
import com.lmora.cuentas.cuentas.model.Cuenta;
import com.lmora.cuentas.cuentas.repository.CuentaRepository;
import com.lmora.cuentas.movimientos.repository.MovimientoRepository;
import com.lmora.cuentas.shared.exception.BusinessConflictException;
import com.lmora.cuentas.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoRepository movimientoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> listarCuentas() {
        return cuentaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta obtenerCuentaPorId(Long cuentaId) {
        return buscarCuentaPorId(cuentaId);
    }

    @Override
    @Transactional
    public Cuenta crearCuenta(Long clienteId, Cuenta cuenta) {
        validarNumeroCuentaDisponible(cuenta.getNumeroCuenta());
        cuenta.setCliente(buscarClientePorId(clienteId));
        return cuentaRepository.save(cuenta);
    }

    @Override
    @Transactional
    public Cuenta actualizarCuenta(Long cuentaId, Long clienteId, Cuenta cuenta) {
        Cuenta cuentaExistente = buscarCuentaPorId(cuentaId);
        validarNumeroCuentaDisponibleParaActualizacion(cuenta.getNumeroCuenta(), cuentaId);
        validarCambioSaldoInicial(cuentaExistente, cuenta.getSaldoInicial());

        cuentaExistente.setCliente(buscarClientePorId(clienteId));
        actualizarDatosCompletos(cuentaExistente, cuenta);

        return cuentaRepository.save(cuentaExistente);
    }

    @Override
    @Transactional
    public Cuenta actualizarParcialCuenta(Long cuentaId, Long clienteId, Cuenta cuenta) {
        Cuenta cuentaExistente = buscarCuentaPorId(cuentaId);

        if (cuenta.getNumeroCuenta() != null) {
            validarNumeroCuentaDisponibleParaActualizacion(cuenta.getNumeroCuenta(), cuentaId);
            cuentaExistente.setNumeroCuenta(cuenta.getNumeroCuenta());
        }

        if (clienteId != null) {
            cuentaExistente.setCliente(buscarClientePorId(clienteId));
        }

        validarCambioSaldoInicial(cuentaExistente, cuenta.getSaldoInicial());
        actualizarDatosParciales(cuentaExistente, cuenta);

        return cuentaRepository.save(cuentaExistente);
    }

    @Override
    @Transactional
    public void eliminarCuenta(Long cuentaId) {
        Cuenta cuenta = buscarCuentaPorId(cuentaId);
        if (movimientoRepository.existsByCuentaCuentaId(cuentaId)) {
            throw new BusinessConflictException("No se puede eliminar la cuenta porque tiene movimientos asociados");
        }
        cuentaRepository.delete(cuenta);
    }

    private Cuenta buscarCuentaPorId(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta", "id", cuentaId));
    }

    private Cliente buscarClientePorId(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
    }

    private void validarNumeroCuentaDisponible(String numeroCuenta) {
        if (cuentaRepository.existsByNumeroCuenta(numeroCuenta)) {
            throw new CuentaNumeroDuplicadoException(numeroCuenta);
        }
    }

    private void validarNumeroCuentaDisponibleParaActualizacion(String numeroCuenta, Long cuentaId) {
        if (cuentaRepository.existsByNumeroCuentaAndCuentaIdNot(numeroCuenta, cuentaId)) {
            throw new CuentaNumeroDuplicadoException(numeroCuenta);
        }
    }

    private void validarCambioSaldoInicial(Cuenta cuentaExistente, BigDecimal nuevoSaldoInicial) {
        if (nuevoSaldoInicial == null) {
            return;
        }

        if (cuentaExistente.getSaldoInicial().compareTo(nuevoSaldoInicial) != 0
                && movimientoRepository.existsByCuentaCuentaId(cuentaExistente.getCuentaId())) {
            throw new BusinessConflictException(
                    "No se puede modificar el saldo inicial de una cuenta con movimientos registrados"
            );
        }
    }

    private void actualizarDatosCompletos(Cuenta cuentaExistente, Cuenta cuenta) {
        cuentaExistente.setNumeroCuenta(cuenta.getNumeroCuenta());
        cuentaExistente.setTipoCuenta(cuenta.getTipoCuenta());
        cuentaExistente.setSaldoInicial(cuenta.getSaldoInicial());
        cuentaExistente.setEstado(cuenta.getEstado());
    }

    private void actualizarDatosParciales(Cuenta cuentaExistente, Cuenta cuenta) {
        if (cuenta.getTipoCuenta() != null) {
            cuentaExistente.setTipoCuenta(cuenta.getTipoCuenta());
        }

        if (cuenta.getSaldoInicial() != null) {
            cuentaExistente.setSaldoInicial(cuenta.getSaldoInicial());
        }

        if (cuenta.getEstado() != null) {
            cuentaExistente.setEstado(cuenta.getEstado());
        }
    }
}
