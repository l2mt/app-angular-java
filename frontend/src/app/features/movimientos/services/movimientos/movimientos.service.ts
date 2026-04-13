import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Movimiento,
  MovimientoCreatePayload,
  MovimientoUpdatePayload,
} from '../../interfaces/movimiento.model';

const BASE_URL = '/api/movimientos';

@Injectable({ providedIn: 'root' })
export class MovimientosService {
  private readonly http = inject(HttpClient);

  list(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(BASE_URL);
  }

  getById(id: number): Observable<Movimiento> {
    return this.http.get<Movimiento>(`${BASE_URL}/${id}`);
  }

  create(payload: MovimientoCreatePayload): Observable<Movimiento> {
    return this.http.post<Movimiento>(BASE_URL, payload);
  }

  update(id: number, payload: MovimientoUpdatePayload): Observable<Movimiento> {
    return this.http.patch<Movimiento>(`${BASE_URL}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }
}
