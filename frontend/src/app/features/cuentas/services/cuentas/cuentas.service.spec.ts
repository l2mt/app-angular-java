import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { Cuenta, CuentaCreatePayload } from '../../interfaces/cuenta.model';
import { CuentasService } from './cuentas.service';

describe('CuentasService', () => {
  let service: CuentasService;
  let http: HttpTestingController;

  const sample: Cuenta = {
    cuentaId: 1,
    clienteId: 10,
    numeroCuenta: '478758',
    tipoCuenta: 'AHORROS',
    saldoInicial: 2000,
    estado: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CuentasService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('GET /api/cuentas returns a list', () => {
    service.list().subscribe((list) => expect(list).toEqual([sample]));
    const req = http.expectOne('/api/cuentas');
    expect(req.request.method).toBe('GET');
    req.flush([sample]);
  });

  it('GET /api/cuentas/:id returns a single cuenta', () => {
    service.getById(1).subscribe((c) => expect(c).toEqual(sample));
    const req = http.expectOne('/api/cuentas/1');
    expect(req.request.method).toBe('GET');
    req.flush(sample);
  });

  it('POST /api/cuentas sends the create payload', () => {
    const payload: CuentaCreatePayload = {
      clienteId: 10,
      numeroCuenta: '478758',
      tipoCuenta: 'AHORROS',
      saldoInicial: 2000,
      estado: true,
    };
    service.create(payload).subscribe((c) => expect(c).toEqual(sample));
    const req = http.expectOne('/api/cuentas');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(sample);
  });

  it('PATCH /api/cuentas/:id sends the update payload', () => {
    service.update(1, { saldoInicial: 3500 }).subscribe((c) => expect(c).toEqual(sample));
    const req = http.expectOne('/api/cuentas/1');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ saldoInicial: 3500 });
    req.flush(sample);
  });

  it('DELETE /api/cuentas/:id issues a delete', () => {
    service.delete(1).subscribe((res) => expect(res).toBeNull());
    const req = http.expectOne('/api/cuentas/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
