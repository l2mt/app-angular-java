import { TipoCuenta } from '../../cuentas/interfaces/cuenta.model';
import { TipoMovimiento } from '../../movimientos/interfaces/movimiento.model';

export interface ReporteCliente {
  clienteId: number;
  nombre: string;
  identificacion: string;
}

export interface ReporteMovimiento {
  movimientoId: number;
  fecha: string;
  tipoMovimiento: TipoMovimiento;
  valor: number;
  saldo: number;
}

export interface ReporteCuenta {
  cuentaId: number;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  saldoDisponible: number;
  totalCreditos: number;
  totalDebitos: number;
  movimientos: ReporteMovimiento[];
}

export interface ReporteEstadoCuenta {
  cliente: ReporteCliente;
  fechaDesde: string;
  fechaHasta: string;
  totalCreditos: number;
  totalDebitos: number;
  cuentas: ReporteCuenta[];
  pdfBase64: string;
}

export interface ReporteFiltros {
  clienteId: number;
  fechaDesde: string;
  fechaHasta: string;
}
