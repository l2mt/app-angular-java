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
import {
  Cliente,
  ClienteCreatePayload,
  ClienteUpdatePayload,
  Genero,
} from '../../interfaces/cliente.model';
import { ClientesService } from '../../services/clientes/clientes.service';

const GENEROS: Genero[] = ['MASCULINO', 'FEMENINO', 'OTRO'];

@Component({
  selector: 'app-cliente-form',
  imports: [ReactiveFormsModule, RouterLink, Alert, FormField],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__header">
        <h2>{{ isEdit() ? 'Editar cliente' : 'Nuevo cliente' }}</h2>
        <a routerLink="/clientes">Volver</a>
      </header>

      @if (error(); as msg) {
        <app-alert [message]="msg" />
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()" novalidate>
        <app-form-field label="Nombre" controlName="nombre" autocomplete="name" />

        <div class="field">
          <label for="genero">Género</label>
          <select id="genero" formControlName="genero">
            <option value="" disabled>Seleccionar…</option>
            @for (g of generos; track g) {
              <option [value]="g">{{ g }}</option>
            }
          </select>
          @if (msg('genero'); as m) {
            <small class="error" role="alert">{{ m }}</small>
          }
        </div>

        <app-form-field
          label="Edad"
          controlName="edad"
          type="number"
          [min]="0"
          [max]="120"
        />

        <app-form-field label="Identificación" controlName="identificacion" />
        <app-form-field label="Dirección" controlName="direccion" />
        <app-form-field label="Teléfono" controlName="telefono" type="tel" />

        <app-form-field
          label="Contraseña"
          controlName="contrasena"
          type="password"
          autocomplete="new-password"
          [hint]="isEdit() ? '(dejar en blanco para no cambiar)' : null"
        />

        <div class="field field--inline">
          <label>
            <input type="checkbox" formControlName="estado" />
            Activo
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
  styleUrl: './cliente-form.scss',
})
export class ClienteForm {
  private readonly service = inject(ClientesService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  protected readonly generos = GENEROS;
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly clienteId = signal<number | null>(this.readId());
  protected readonly isEdit = computed(() => this.clienteId() !== null);

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    genero: this.fb.nonNullable.control<Genero | ''>('', { validators: [Validators.required] }),
    edad: this.fb.nonNullable.control<number | null>(null, {
      validators: [Validators.required, Validators.min(0), Validators.max(120)],
    }),
    identificacion: ['', [Validators.required, Validators.maxLength(20)]],
    direccion: ['', [Validators.maxLength(200)]],
    telefono: ['', [Validators.maxLength(20)]],
    contrasena: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(255)]],
    estado: [true, [Validators.required]],
  });

  constructor() {
    const id = this.clienteId();
    if (id !== null) {
      this.adjustForEdit();
      this.loadCliente(id);
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

    const id = this.clienteId();
    const request$ = id === null
      ? this.service.create(this.toCreatePayload())
      : this.service.update(id, this.toUpdatePayload());

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/clientes']);
      },
      error: (err) => {
        this.submitting.set(false);
        this.handleError(err);
      },
    });
  }

  private adjustForEdit(): void {
    const control = this.form.controls.contrasena;
    control.clearValidators();
    control.addValidators([Validators.minLength(4), Validators.maxLength(255)]);
    control.updateValueAndValidity();
  }

  private loadCliente(id: number): void {
    this.service.getById(id).subscribe({
      next: (cliente) => this.patchFromCliente(cliente),
      error: (err) => this.handleError(err),
    });
  }

  private patchFromCliente(cliente: Cliente): void {
    this.form.patchValue({
      nombre: cliente.nombre,
      genero: cliente.genero,
      edad: cliente.edad,
      identificacion: cliente.identificacion,
      direccion: cliente.direccion ?? '',
      telefono: cliente.telefono ?? '',
      estado: cliente.estado,
    });
  }

  private toCreatePayload(): ClienteCreatePayload {
    const v = this.form.getRawValue();
    return {
      nombre: v.nombre,
      genero: v.genero as Genero,
      edad: v.edad as number,
      identificacion: v.identificacion,
      direccion: v.direccion,
      telefono: v.telefono,
      contrasena: v.contrasena,
      estado: v.estado,
    };
  }

  private toUpdatePayload(): ClienteUpdatePayload {
    const v = this.form.getRawValue();
    const payload: ClienteUpdatePayload = {
      nombre: v.nombre,
      genero: v.genero as Genero,
      edad: v.edad as number,
      identificacion: v.identificacion,
      direccion: v.direccion,
      telefono: v.telefono,
      estado: v.estado,
    };
    if (v.contrasena.trim()) {
      payload.contrasena = v.contrasena;
    }
    return payload;
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
