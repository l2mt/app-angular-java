import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ApiError } from '../../../../core/http/api-error';
import { Alert } from '../../../../shared/ui/atoms/alert/alert';
import { Cuenta } from '../../interfaces/cuenta.model';
import { CuentasService } from '../../services/cuentas/cuentas.service';

@Component({
  selector: 'app-cuentas-list',
  imports: [FormsModule, RouterLink, Alert],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__header">
        <h2 class="page__title">Cuentas</h2>
        <a routerLink="/cuentas/nuevo" class="btn btn--primary">Nueva</a>
      </header>

      <label class="search">
        <span class="visually-hidden">Buscar cuentas</span>
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
              <th>Número</th>
              <th>Tipo</th>
              <th>Saldo inicial</th>
              <th>Cliente</th>
              <th>Estado</th>
              <th class="table__actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (cuenta of filtered(); track cuenta.cuentaId) {
              <tr>
                <td>{{ cuenta.numeroCuenta }}</td>
                <td>{{ cuenta.tipoCuenta }}</td>
                <td>{{ cuenta.saldoInicial }}</td>
                <td>{{ cuenta.clienteId }}</td>
                <td>{{ cuenta.estado ? 'Activo' : 'Inactivo' }}</td>
                <td class="table__actions">
                  <a [routerLink]="['/cuentas', cuenta.cuentaId, 'editar']">Editar</a>
                  <button type="button" (click)="onDelete(cuenta)">Eliminar</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
  styleUrl: './cuentas-list.scss',
})
export class CuentasList {
  private readonly service = inject(CuentasService);

  protected readonly cuentas = signal<Cuenta[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly query = signal('');

  protected readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    if (!q) {
      return this.cuentas();
    }
    return this.cuentas().filter((c) =>
      [c.numeroCuenta, c.tipoCuenta, String(c.clienteId)].some((v) =>
        v.toLowerCase().includes(q),
      ),
    );
  });

  constructor() {
    this.load();
  }

  protected onDelete(cuenta: Cuenta): void {
    if (!confirm(`¿Eliminar la cuenta ${cuenta.numeroCuenta}?`)) {
      return;
    }
    this.error.set(null);
    this.service.delete(cuenta.cuentaId).subscribe({
      next: () =>
        this.cuentas.update((list) => list.filter((c) => c.cuentaId !== cuenta.cuentaId)),
      error: (err) => this.error.set(err instanceof ApiError ? err.message : 'No se pudo eliminar.'),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list().subscribe({
      next: (data) => {
        this.cuentas.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err instanceof ApiError ? err.message : 'No se pudo cargar la lista.');
        this.loading.set(false);
      },
    });
  }
}
