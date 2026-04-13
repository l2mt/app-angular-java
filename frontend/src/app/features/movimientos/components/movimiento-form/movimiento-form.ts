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
import { Cuenta } from '../../../cuentas/interfaces/cuenta.model';
import { CuentasService } from '../../../cuentas/services/cuentas/cuentas.service';
import {
  Movimiento,
  MovimientoCreatePayload,
  MovimientoUpdatePayload,
  TipoMovimiento,
} from '../../interfaces/movimiento.model';
import { MovimientosService } from '../../services/movimientos/movimientos.service';

const TIPOS: TipoMovimiento[] = ['CREDITO', 'DEBITO'];

@Component({
  selector: 'app-movimiento-form',
  imports: [ReactiveFormsModule, RouterLink, Alert, FormField],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__header">
        <h2>{{ isEdit() ? 'Editar movimiento' : 'Nuevo movimiento' }}</h2>
        <a routerLink="/movimientos">Volver</a>
      </header>

      @if (error(); as msg) {
        <app-alert [message]="msg" />
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()" novalidate>
        <div class="field">
          <label for="cuentaId">Cuenta</label>
          <select id="cuentaId" formControlName="cuentaId">
            <option [ngValue]="null" disabled>Seleccionar…</option>
            @for (c of cuentas(); track c.cuentaId) {
              <option [ngValue]="c.cuentaId">{{ c.numeroCuenta }} ({{ c.tipoCuenta }})</option>
            }
          </select>
          @if (msg('cuentaId'); as m) { <small class="error" role="alert">{{ m }}</small> }
        </div>

        <div class="field">
          <label for="tipoMovimiento">Tipo</label>
          <select id="tipoMovimiento" formControlName="tipoMovimiento">
            <option value="" disabled>Seleccionar…</option>
            @for (t of tipos; track t) {
              <option [value]="t">{{ t }}</option>
            }
          </select>
          @if (msg('tipoMovimiento'); as m) { <small class="error" role="alert">{{ m }}</small> }
        </div>

        <app-form-field
          label="Valor"
          controlName="valor"
          type="number"
          [min]="0.01"
          step="0.01"
        />

        <app-form-field
          label="Fecha"
          hint="opcional"
          controlName="fecha"
          type="datetime-local"
        />

        <div class="actions">
          <button type="submit" class="btn btn--primary" [disabled]="submitting()">
            {{ submitting() ? 'Guardando…' : 'Guardar' }}
          </button>
        </div>
      </form>
    </section>
  `,
  styleUrl: './movimiento-form.scss',
})
export class MovimientoForm {
  private readonly service = inject(MovimientosService);
  private readonly cuentasService = inject(CuentasService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  protected readonly tipos = TIPOS;
  protected readonly cuentas = signal<Cuenta[]>([]);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly movimientoId = signal<number | null>(this.readId());
  protected readonly isEdit = computed(() => this.movimientoId() !== null);

  protected readonly form = this.fb.nonNullable.group({
    cuentaId: this.fb.nonNullable.control<number | null>(null, {
      validators: [Validators.required],
    }),
    tipoMovimiento: this.fb.nonNullable.control<TipoMovimiento | ''>('', {
      validators: [Validators.required],
    }),
    valor: this.fb.nonNullable.control<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
    fecha: this.fb.nonNullable.control<string>(''),
  });

  constructor() {
    this.loadCuentas();
    const id = this.movimientoId();
    if (id !== null) {
      this.loadMovimiento(id);
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

    const id = this.movimientoId();
    const request$ = id === null
      ? this.service.create(this.toCreatePayload())
      : this.service.update(id, this.toUpdatePayload());

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/movimientos']);
      },
      error: (err) => {
        this.submitting.set(false);
        this.handleError(err);
      },
    });
  }

  private loadCuentas(): void {
    this.cuentasService.list().subscribe({
      next: (list) => this.cuentas.set(list),
      error: (err) => this.handleError(err),
    });
  }

  private loadMovimiento(id: number): void {
    this.service.getById(id).subscribe({
      next: (m) => this.patchFromMovimiento(m),
      error: (err) => this.handleError(err),
    });
  }

  private patchFromMovimiento(m: Movimiento): void {
    this.form.patchValue({
      cuentaId: m.cuentaId,
      tipoMovimiento: m.tipoMovimiento,
      valor: Math.abs(m.valor),
      fecha: m.fecha ? m.fecha.slice(0, 16) : '',
    });
  }

  private toCreatePayload(): MovimientoCreatePayload {
    const v = this.form.getRawValue();
    return {
      cuentaId: v.cuentaId as number,
      tipoMovimiento: v.tipoMovimiento as TipoMovimiento,
      valor: this.toSignedValue(v.valor as number, v.tipoMovimiento as TipoMovimiento),
      fecha: v.fecha ? v.fecha : null,
    };
  }

  private toUpdatePayload(): MovimientoUpdatePayload {
    const v = this.form.getRawValue();
    return {
      cuentaId: v.cuentaId as number,
      tipoMovimiento: v.tipoMovimiento as TipoMovimiento,
      valor: this.toSignedValue(v.valor as number, v.tipoMovimiento as TipoMovimiento),
      fecha: v.fecha ? v.fecha : null,
    };
  }

  private toSignedValue(valor: number, tipoMovimiento: TipoMovimiento): number {
    const absoluteValue = Math.abs(valor);
    return tipoMovimiento === 'DEBITO' ? -absoluteValue : absoluteValue;
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
