import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Cliente,
  ClienteCreatePayload,
  ClienteUpdatePayload,
} from '../../interfaces/cliente.model';

const BASE_URL = '/api/clientes';

@Injectable({ providedIn: 'root' })
export class ClientesService {
  private readonly http = inject(HttpClient);

  list(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(BASE_URL);
  }

  getById(clienteId: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${BASE_URL}/${clienteId}`);
  }

  create(payload: ClienteCreatePayload): Observable<Cliente> {
    return this.http.post<Cliente>(BASE_URL, payload);
  }

  update(clienteId: number, payload: ClienteUpdatePayload): Observable<Cliente> {
    return this.http.patch<Cliente>(`${BASE_URL}/${clienteId}`, payload);
  }

  delete(clienteId: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${clienteId}`);
  }
}
