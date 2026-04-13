import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Cuenta,
  CuentaCreatePayload,
  CuentaUpdatePayload,
} from '../../interfaces/cuenta.model';

const BASE_URL = '/api/cuentas';

@Injectable({ providedIn: 'root' })
export class CuentasService {
  private readonly http = inject(HttpClient);

  list(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(BASE_URL);
  }

  getById(cuentaId: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${BASE_URL}/${cuentaId}`);
  }

  create(payload: CuentaCreatePayload): Observable<Cuenta> {
    return this.http.post<Cuenta>(BASE_URL, payload);
  }

  update(cuentaId: number, payload: CuentaUpdatePayload): Observable<Cuenta> {
    return this.http.patch<Cuenta>(`${BASE_URL}/${cuentaId}`, payload);
  }

  delete(cuentaId: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${cuentaId}`);
  }
}
