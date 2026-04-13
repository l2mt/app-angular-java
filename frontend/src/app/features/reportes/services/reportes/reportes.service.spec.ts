import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { ReporteEstadoCuenta } from '../../interfaces/reporte.model';
import { ReportesService } from './reportes.service';

describe('ReportesService', () => {
  let service: ReportesService;
  let http: HttpTestingController;

  const sample: ReporteEstadoCuenta = {
    cliente: { clienteId: 1, nombre: 'Jose Lema', identificacion: '0102030405' },
    fechaDesde: '2026-01-01',
    fechaHasta: '2026-01-31',
    totalCreditos: 1000,
    totalDebitos: 250,
    cuentas: [],
    pdfBase64: 'JVBERi0x',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ReportesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('GET /api/reportes pasa los filtros como query params', () => {
    service
      .generar({ clienteId: 1, fechaDesde: '2026-01-01', fechaHasta: '2026-01-31' })
      .subscribe((r) => expect(r).toEqual(sample));

    const req = http.expectOne(
      (r) =>
        r.url === '/api/reportes' &&
        r.params.get('clienteId') === '1' &&
        r.params.get('fechaDesde') === '2026-01-01' &&
        r.params.get('fechaHasta') === '2026-01-31',
    );
    expect(req.request.method).toBe('GET');
    req.flush(sample);
  });
});
