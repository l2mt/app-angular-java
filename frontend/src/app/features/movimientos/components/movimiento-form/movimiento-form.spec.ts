import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Cuenta } from '../../../cuentas/interfaces/cuenta.model';
import { CuentasService } from '../../../cuentas/services/cuentas/cuentas.service';
import { Movimiento } from '../../interfaces/movimiento.model';
import { MovimientosService } from '../../services/movimientos/movimientos.service';
import { MovimientoForm } from './movimiento-form';

describe('MovimientoForm', () => {
  const cuentas: Cuenta[] = [
    {
      cuentaId: 1,
      clienteId: 10,
      numeroCuenta: '12345',
      tipoCuenta: 'AHORROS',
      saldoInicial: 1000,
      estado: true,
    },
  ];

  const routeStub = {
    snapshot: {
      paramMap: convertToParamMap({}),
    },
  };

  let cuentasService: { list: jest.Mock };
  let movimientosService: {
    create: jest.Mock;
    update: jest.Mock;
    getById: jest.Mock;
  };

  beforeEach(async () => {
    routeStub.snapshot.paramMap = convertToParamMap({});

    cuentasService = {
      list: jest.fn(() => of(cuentas)),
    };

    movimientosService = {
      create: jest.fn(() => of({})),
      update: jest.fn(() => of({})),
      getById: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [MovimientoForm],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: routeStub },
        { provide: CuentasService, useValue: cuentasService },
        { provide: MovimientosService, useValue: movimientosService },
      ],
    }).compileComponents();
  });

  it('sends debitos as negative values while keeping a positive form value', () => {
    const fixture = TestBed.createComponent(MovimientoForm);
    const router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate').mockResolvedValue(true);

    const component = fixture.componentInstance as any;
    component.form.setValue({
      cuentaId: 1,
      tipoMovimiento: 'DEBITO',
      valor: 120,
      fecha: '',
    });

    component.onSubmit();

    expect(movimientosService.create).toHaveBeenCalledWith({
      cuentaId: 1,
      tipoMovimiento: 'DEBITO',
      valor: -120,
      fecha: null,
    });
    expect(component.form.controls.valor.value).toBe(120);
  });

  it('loads debit movements as positive values in the form', () => {
    const movimiento: Movimiento = {
      movimientoId: 99,
      cuentaId: 1,
      fecha: '2026-04-12T20:00:00',
      tipoMovimiento: 'DEBITO',
      valor: -75,
      saldo: 925,
    };

    routeStub.snapshot.paramMap = convertToParamMap({ id: '99' });
    movimientosService.getById.mockReturnValue(of(movimiento));

    const fixture = TestBed.createComponent(MovimientoForm);
    const component = fixture.componentInstance as any;

    expect(component.form.controls.tipoMovimiento.value).toBe('DEBITO');
    expect(component.form.controls.valor.value).toBe(75);
  });

  it('marks negative values as invalid in the form', () => {
    const fixture = TestBed.createComponent(MovimientoForm);
    const component = fixture.componentInstance as any;

    component.form.controls.valor.setValue(-1);

    expect(component.form.controls.valor.hasError('min')).toBe(true);
  });
});
