import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { Movimiento, MovimientoCreatePayload } from '../../interfaces/movimiento.model';
import { MovimientosService } from './movimientos.service';

describe('MovimientosService', () => {
  let service: MovimientosService;
  let http: HttpTestingController;

  const sample: Movimiento = {
    movimientoId: 1,
    cuentaId: 10,
    fecha: '2026-04-12T10:00:00',
    tipoMovimiento: 'CREDITO',
    valor: 500,
    saldo: 2500,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MovimientosService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('GET /api/movimientos returns a list', () => {
    service.list().subscribe((list) => expect(list).toEqual([sample]));
    const req = http.expectOne('/api/movimientos');
    expect(req.request.method).toBe('GET');
    req.flush([sample]);
  });

  it('GET /api/movimientos/:id returns a single movimiento', () => {
    service.getById(1).subscribe((m) => expect(m).toEqual(sample));
    const req = http.expectOne('/api/movimientos/1');
    expect(req.request.method).toBe('GET');
    req.flush(sample);
  });

  it('POST /api/movimientos sends the create payload', () => {
    const payload: MovimientoCreatePayload = {
      cuentaId: 10,
      tipoMovimiento: 'CREDITO',
      valor: 500,
    };
    service.create(payload).subscribe((m) => expect(m).toEqual(sample));
    const req = http.expectOne('/api/movimientos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(sample);
  });

  it('PATCH /api/movimientos/:id sends the update payload', () => {
    service.update(1, { valor: 750 }).subscribe((m) => expect(m).toEqual(sample));
    const req = http.expectOne('/api/movimientos/1');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ valor: 750 });
    req.flush(sample);
  });

  it('DELETE /api/movimientos/:id issues a delete', () => {
    service.delete(1).subscribe((res) => expect(res).toBeNull());
    const req = http.expectOne('/api/movimientos/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
