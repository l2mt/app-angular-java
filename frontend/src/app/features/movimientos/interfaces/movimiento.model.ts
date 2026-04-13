export type TipoMovimiento = 'CREDITO' | 'DEBITO';

export interface Movimiento {
  movimientoId: number;
  cuentaId: number;
  fecha: string;
  tipoMovimiento: TipoMovimiento;
  valor: number;
  saldo: number;
}

export interface MovimientoCreatePayload {
  cuentaId: number;
  fecha?: string | null;
  tipoMovimiento: TipoMovimiento;
  valor: number;
}

export type MovimientoUpdatePayload = Partial<MovimientoCreatePayload>;
