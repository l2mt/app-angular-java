import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ApiError } from '../../../../core/http/api-error';
import { Alert } from '../../../../shared/ui/atoms/alert/alert';
import { Movimiento } from '../../interfaces/movimiento.model';
import { MovimientosService } from '../../services/movimientos/movimientos.service';

@Component({
  selector: 'app-movimientos-list',
  imports: [FormsModule, RouterLink, Alert],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__header">
        <h2 class="page__title">Movimientos</h2>
        <a routerLink="/movimientos/nuevo" class="btn btn--primary">Nuevo</a>
      </header>

      <label class="search">
        <span class="visually-hidden">Buscar movimientos</span>
        <input
          type="search"
          placeholder="Buscar"
          [ngModel]="query()"
          (ngModelChange)="query.set($event)"
          name="search"
        />
      </label>

      @if (error(); as msg) {
        <app-alert [message]="msg" />
      }

      @if (loading()) {
        <p>Cargando…</p>
      } @else if (filtered().length === 0) {
        <p class="empty">Sin resultados.</p>
      } @else {
        <table class="table">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Cuenta</th>
              <th>Tipo</th>
              <th>Valor</th>
              <th>Saldo</th>
              <th class="table__actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (m of filtered(); track m.movimientoId) {
              <tr>
                <td>{{ m.fecha }}</td>
                <td>{{ m.cuentaId }}</td>
                <td>{{ m.tipoMovimiento }}</td>
                <td>{{ m.valor }}</td>
                <td>{{ m.saldo }}</td>
                <td class="table__actions">
                  <a [routerLink]="['/movimientos', m.movimientoId, 'editar']">Editar</a>
                  <button type="button" (click)="onDelete(m)">Eliminar</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
  styleUrl: './movimientos-list.scss',
})
export class MovimientosList {
  private readonly service = inject(MovimientosService);

  protected readonly movimientos = signal<Movimiento[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly query = signal('');

  protected readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    if (!q) {
      return this.movimientos();
    }
    return this.movimientos().filter((m) =>
      [m.fecha, m.tipoMovimiento, String(m.cuentaId), String(m.valor)].some((v) =>
        v.toLowerCase().includes(q),
      ),
    );
  });

  constructor() {
    this.load();
  }

  protected onDelete(m: Movimiento): void {
    if (!confirm(`¿Eliminar el movimiento #${m.movimientoId}?`)) {
      return;
    }
    this.error.set(null);
    this.service.delete(m.movimientoId).subscribe({
      next: () =>
        this.movimientos.update((list) =>
          list.filter((x) => x.movimientoId !== m.movimientoId),
        ),
      error: (err) =>
        this.error.set(err instanceof ApiError ? err.message : 'No se pudo eliminar.'),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list().subscribe({
      next: (data) => {
        this.movimientos.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err instanceof ApiError ? err.message : 'No se pudo cargar la lista.');
        this.loading.set(false);
      },
    });
  }
}
