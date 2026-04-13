import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { Cliente, ClienteCreatePayload } from '../../interfaces/cliente.model';
import { ClientesService } from './clientes.service';

describe('ClientesService', () => {
  let service: ClientesService;
  let http: HttpTestingController;

  const sample: Cliente = {
    clienteId: 1,
    nombre: 'Ana',
    genero: 'FEMENINO',
    edad: 30,
    identificacion: '1234',
    direccion: 'Calle 1',
    telefono: '555',
    estado: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ClientesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('GET /api/clientes returns a list', () => {
    service.list().subscribe((list) => expect(list).toEqual([sample]));
    const req = http.expectOne('/api/clientes');
    expect(req.request.method).toBe('GET');
    req.flush([sample]);
  });

  it('GET /api/clientes/:id returns a single cliente', () => {
    service.getById(1).subscribe((c) => expect(c).toEqual(sample));
    const req = http.expectOne('/api/clientes/1');
    expect(req.request.method).toBe('GET');
    req.flush(sample);
  });

  it('POST /api/clientes sends the create payload', () => {
    const payload: ClienteCreatePayload = {
      nombre: 'Ana',
      genero: 'FEMENINO',
      edad: 30,
      identificacion: '1234',
      direccion: 'Calle 1',
      telefono: '555',
      contrasena: 'secret',
      estado: true,
    };
    service.create(payload).subscribe((c) => expect(c).toEqual(sample));
    const req = http.expectOne('/api/clientes');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(sample);
  });

  it('PATCH /api/clientes/:id sends the update payload', () => {
    service.update(1, { telefono: '999' }).subscribe((c) => expect(c).toEqual(sample));
    const req = http.expectOne('/api/clientes/1');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ telefono: '999' });
    req.flush(sample);
  });

  it('DELETE /api/clientes/:id issues a delete', () => {
    service.delete(1).subscribe((res) => expect(res).toBeNull());
    const req = http.expectOne('/api/clientes/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
