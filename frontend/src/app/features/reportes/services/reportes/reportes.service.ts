import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ReporteEstadoCuenta, ReporteFiltros } from '../../interfaces/reporte.model';

const BASE_URL = '/api/reportes';

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);

  generar(filtros: ReporteFiltros): Observable<ReporteEstadoCuenta> {
    const params = new HttpParams()
      .set('clienteId', filtros.clienteId)
      .set('fechaDesde', filtros.fechaDesde)
      .set('fechaHasta', filtros.fechaHasta);
    return this.http.get<ReporteEstadoCuenta>(BASE_URL, { params });
  }
}
