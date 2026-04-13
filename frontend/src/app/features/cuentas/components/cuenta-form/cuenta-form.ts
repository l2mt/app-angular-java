import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ApiError } from '../../../../core/http/api-error';
import { getErrorMessage } from '../../../../shared/forms/form-errors.util';
import { Alert } from '../../../../shared/ui/atoms/alert/alert';
import { FormField } from '../../../../shared/ui/molecules/form-field/form-field';
import { Cliente } from '../../../clientes/interfaces/cliente.model';
import { ClientesService } from '../../../clientes/services/clientes/clientes.service';
import {
  Cuenta,
  CuentaCreatePayload,
  CuentaUpdatePayload,
  TipoCuenta,
} from '../../interfaces/cuenta.model';
import { CuentasService } from '../../services/cuentas/cuentas.service';

const TIPOS: TipoCuenta[] = ['AHORROS', 'CORRIENTE'];

@Component({
  selector: 'app-cuenta-form',
  imports: [ReactiveFormsModule, RouterLink, Alert, FormField],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__header">
        <h2>{{ isEdit() ? 'Editar cuenta' : 'Nueva cuenta' }}</h2>
        <a routerLink="/cuentas">Volver</a>
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

        <app-form-field label="Número de cuenta" controlName="numeroCuenta" />

        <div class="field">
          <label for="tipoCuenta">Tipo</label>
          <select id="tipoCuenta" formControlName="tipoCuenta">
            <option value="" disabled>Seleccionar…</option>
            @for (t of tipos; track t) {
              <option [value]="t">{{ t }}</option>
            }
          </select>
          @if (msg('tipoCuenta'); as m) { <small class="error" role="alert">{{ m }}</small> }
        </div>

        <app-form-field
          label="Saldo inicial"
          controlName="saldoInicial"
          type="number"
          [min]="0"
          step="0.01"
        />

        <div class="field field--inline">
          <label>
            <input type="checkbox" formControlName="estado" />
            Activa
          </label>
        </div>

        <div class="actions">
          <button type="submit" class="btn btn--primary" [disabled]="submitting()">
            {{ submitting() ? 'Guardando…' : 'Guardar' }}
          </button>
        </div>
      </form>
    </section>
  `,
  styleUrl: './cuenta-form.scss',
})
export class CuentaForm {
  private readonly service = inject(CuentasService);
  private readonly clientesService = inject(ClientesService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  protected readonly tipos = TIPOS;
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly cuentaId = signal<number | null>(this.readId());
  protected readonly isEdit = computed(() => this.cuentaId() !== null);

  protected readonly form = this.fb.nonNullable.group({
    clienteId: this.fb.nonNullable.control<number | null>(null, {
      validators: [Validators.required],
    }),
    numeroCuenta: ['', [Validators.required, Validators.maxLength(30)]],
    tipoCuenta: this.fb.nonNullable.control<TipoCuenta | ''>('', {
      validators: [Validators.required],
    }),
    saldoInicial: this.fb.nonNullable.control<number | null>(null, {
      validators: [Validators.required, Validators.min(0)],
    }),
    estado: [true, [Validators.required]],
  });

  constructor() {
    this.loadClientes();
    const id = this.cuentaId();
    if (id !== null) {
      this.loadCuenta(id);
    }
  }

  protected msg(name: string): string | null {
    return getErrorMessage(this.form.get(name));
  }

  protected onSubmit(): void {
    if (this.submitting()) {
      return;
    }
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    const id = this.cuentaId();
    const request$ = id === null
      ? this.service.create(this.toCreatePayload())
      : this.service.update(id, this.toUpdatePayload());

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/cuentas']);
      },
      error: (err) => {
        this.submitting.set(false);
        this.handleError(err);
      },
    });
  }

  private loadClientes(): void {
    this.clientesService.list().subscribe({
      next: (list) => this.clientes.set(list),
      error: (err) => this.handleError(err),
    });
  }

  private loadCuenta(id: number): void {
    this.service.getById(id).subscribe({
      next: (cuenta) => this.patchFromCuenta(cuenta),
      error: (err) => this.handleError(err),
    });
  }

  private patchFromCuenta(cuenta: Cuenta): void {
    this.form.patchValue({
      clienteId: cuenta.clienteId,
      numeroCuenta: cuenta.numeroCuenta,
      tipoCuenta: cuenta.tipoCuenta,
      saldoInicial: cuenta.saldoInicial,
      estado: cuenta.estado,
    });
  }

  private toCreatePayload(): CuentaCreatePayload {
    const v = this.form.getRawValue();
    return {
      clienteId: v.clienteId as number,
      numeroCuenta: v.numeroCuenta,
      tipoCuenta: v.tipoCuenta as TipoCuenta,
      saldoInicial: v.saldoInicial as number,
      estado: v.estado,
    };
  }

  private toUpdatePayload(): CuentaUpdatePayload {
    const v = this.form.getRawValue();
    return {
      clienteId: v.clienteId as number,
      numeroCuenta: v.numeroCuenta,
      tipoCuenta: v.tipoCuenta as TipoCuenta,
      saldoInicial: v.saldoInicial as number,
      estado: v.estado,
    };
  }

  private handleError(err: unknown): void {
    if (err instanceof ApiError) {
      this.applyFieldErrors(err.validationErrors);
      this.error.set(err.message);
    } else {
      this.error.set('Ocurrió un error al guardar.');
    }
  }

  private applyFieldErrors(validationErrors?: Record<string, string>): void {
    if (!validationErrors) {
      return;
    }
    for (const [field, message] of Object.entries(validationErrors)) {
      const control = this.form.get(field) as FormControl | null;
      if (control) {
        control.setErrors({ server: message });
        control.markAsTouched();
      }
    }
  }

  private readId(): number | null {
    const raw = this.route.snapshot.paramMap.get('id');
    if (!raw) {
      return null;
    }
    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : null;
  }
}
