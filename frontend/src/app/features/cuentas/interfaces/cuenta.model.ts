export type TipoCuenta = 'AHORROS' | 'CORRIENTE';

export interface Cuenta {
  cuentaId: number;
  clienteId: number;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
}

export interface CuentaCreatePayload {
  clienteId: number;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
}

export type CuentaUpdatePayload = Partial<CuentaCreatePayload>;
