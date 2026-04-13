import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ApiError } from '../../../../core/http/api-error';
import { Alert } from '../../../../shared/ui/atoms/alert/alert';
import { Cliente } from '../../interfaces/cliente.model';
import { ClientesService } from '../../services/clientes/clientes.service';

@Component({
  selector: 'app-clientes-list',
  imports: [FormsModule, RouterLink, Alert],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__header">
        <h2 class="page__title">Clientes</h2>
        <a routerLink="/clientes/nuevo" class="btn btn--primary">Nuevo</a>
      </header>

      <label class="search">
        <span class="visually-hidden">Buscar clientes</span>
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
              <th>Nombre</th>
              <th>Identificación</th>
              <th>Edad</th>
              <th>Teléfono</th>
              <th>Estado</th>
              <th class="table__actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (cliente of filtered(); track cliente.clienteId) {
              <tr>
                <td>{{ cliente.nombre }}</td>
                <td>{{ cliente.identificacion }}</td>
                <td>{{ cliente.edad }}</td>
                <td>{{ cliente.telefono }}</td>
                <td>{{ cliente.estado ? 'Activo' : 'Inactivo' }}</td>
                <td class="table__actions">
                  <a [routerLink]="['/clientes', cliente.clienteId, 'editar']">Editar</a>
                  <button type="button" (click)="onDelete(cliente)">Eliminar</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
  styleUrl: './clientes-list.scss',
})
export class ClientesList {
  private readonly service = inject(ClientesService);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly query = signal('');

  protected readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    if (!q) {
      return this.clientes();
    }
    return this.clientes().filter((c) =>
      [c.nombre, c.identificacion, c.telefono ?? ''].some((v) => v.toLowerCase().includes(q)),
    );
  });

  constructor() {
    this.load();
  }

  protected onDelete(cliente: Cliente): void {
    if (!confirm(`¿Eliminar al cliente "${cliente.nombre}"?`)) {
      return;
    }
    this.error.set(null);
    this.service.delete(cliente.clienteId).subscribe({
      next: () => this.clientes.update((list) => list.filter((c) => c.clienteId !== cliente.clienteId)),
      error: (err) => this.error.set(err instanceof ApiError ? err.message : 'No se pudo eliminar.'),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list().subscribe({
      next: (data) => {
        this.clientes.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err instanceof ApiError ? err.message : 'No se pudo cargar la lista.');
        this.loading.set(false);
      },
    });
  }
}
