import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ApiError } from '../../../../core/http/api-error';
import { getErrorMessage } from '../../../../shared/forms/form-errors.util';
import { Alert } from '../../../../shared/ui/atoms/alert/alert';
import { FormField } from '../../../../shared/ui/molecules/form-field/form-field';
import { Cliente } from '../../../clientes/interfaces/cliente.model';
import { ClientesService } from '../../../clientes/services/clientes/clientes.service';
import { ReporteEstadoCuenta } from '../../interfaces/reporte.model';
import { ReportesService } from '../../services/reportes/reportes.service';

@Component({
  selector: 'app-reportes-page',
  imports: [ReactiveFormsModule, Alert, FormField],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__header">
        <h2 class="page__title">Estado de cuenta</h2>
      </header>

      @if (error(); as msg) {
        <app-alert [message]="msg" />
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()" novalidate>
        <div class="field">
          <label for="clienteId">Cliente</label>
          <select id="clienteId" formControlName="clienteId">
            <option [ngValue]="null" disabled>Seleccionar…</option>
            @for (c of clientes(); track c.clienteId) {
              <option [ngValue]="c.clienteId">{{ c.nombre }} ({{ c.identificacion }})</option>
            }
          </select>
          @if (msg('clienteId'); as m) { <small class="error" role="alert">{{ m }}</small> }
        </div>

        <app-form-field label="Fecha desde" controlName="fechaDesde" type="date" />
        <app-form-field label="Fecha hasta" controlName="fechaHasta" type="date" />

        <div class="actions">
          <button type="submit" class="btn btn--primary" [disabled]="loading()">
            {{ loading() ? 'Generando…' : 'Generar' }}
          </button>
        </div>
      </form>

      @if (reporte(); as r) {
        <article class="report">
          <header class="report__header">
            <div>
              <h3>{{ r.cliente.nombre }}</h3>
              <p class="report__meta">
                {{ r.cliente.identificacion }} · {{ r.fechaDesde }} a {{ r.fechaHasta }}
              </p>
            </div>
            <button type="button" class="btn" (click)="downloadPdf(r)">Descargar PDF</button>
          </header>

          <dl class="report__totals">
            <div><dt>Total créditos</dt><dd>{{ r.totalCreditos }}</dd></div>
            <div><dt>Total débitos</dt><dd>{{ r.totalDebitos }}</dd></div>
          </dl>

          @if (r.cuentas.length === 0) {
            <p class="empty">Sin cuentas para el rango seleccionado.</p>
          } @else {
            @for (cuenta of r.cuentas; track cuenta.cuentaId) {
              <section class="account">
                <header class="account__header">
                  <h4>{{ cuenta.numeroCuenta }} ({{ cuenta.tipoCuenta }})</h4>
                  <span>Saldo disponible: {{ cuenta.saldoDisponible }}</span>
                </header>

                @if (cuenta.movimientos.length === 0) {
                  <p class="empty">Sin movimientos.</p>
                } @else {
                  <table class="table">
                    <thead>
                      <tr>
                        <th>Fecha</th>
                        <th>Tipo</th>
                        <th>Valor</th>
                        <th>Saldo</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (m of cuenta.movimientos; track m.movimientoId) {
                        <tr>
                          <td>{{ m.fecha }}</td>
                          <td>{{ m.tipoMovimiento }}</td>
                          <td>{{ m.valor }}</td>
                          <td>{{ m.saldo }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                }
              </section>
            }
          }
        </article>
      }
    </section>
  `,
  styleUrl: './reportes-page.scss',
})
export class ReportesPage {
  private readonly service = inject(ReportesService);
  private readonly clientesService = inject(ClientesService);
  private readonly fb = inject(FormBuilder);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly reporte = signal<ReporteEstadoCuenta | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    clienteId: this.fb.nonNullable.control<number | null>(null, {
      validators: [Validators.required],
    }),
    fechaDesde: ['', [Validators.required]],
    fechaHasta: ['', [Validators.required]],
  });

  constructor() {
    this.loadClientes();
  }

  protected msg(name: string): string | null {
    return getErrorMessage(this.form.get(name));
  }

  protected onSubmit(): void {
    if (this.loading()) {
      return;
    }
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    const v = this.form.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.reporte.set(null);

    this.service
      .generar({
        clienteId: v.clienteId as number,
        fechaDesde: v.fechaDesde,
        fechaHasta: v.fechaHasta,
      })
      .subscribe({
        next: (r) => {
          this.reporte.set(r);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(err instanceof ApiError ? err.message : 'No se pudo generar el reporte.');
        },
      });
  }

  protected downloadPdf(r: ReporteEstadoCuenta): void {
    const bytes = this.base64ToBytes(r.pdfBase64);
    const blob = new Blob([bytes.buffer as ArrayBuffer], { type: 'application/pdf' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `estado-cuenta-${r.cliente.identificacion}-${r.fechaDesde}-${r.fechaHasta}.pdf`;
    link.click();
    URL.revokeObjectURL(url);
  }

  private loadClientes(): void {
    this.clientesService.list().subscribe({
      next: (list) => this.clientes.set(list),
      error: (err) =>
        this.error.set(err instanceof ApiError ? err.message : 'No se pudo cargar los clientes.'),
    });
  }

  private base64ToBytes(base64: string): Uint8Array {
    const binary = atob(base64);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes;
  }
}
