import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { ControlContainer, ReactiveFormsModule } from '@angular/forms';

import { getErrorMessage } from '../../../forms/form-errors.util';

type FieldType = 'text' | 'number' | 'password' | 'email' | 'tel' | 'search';

@Component({
  selector: 'app-form-field',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  viewProviders: [
    {
      provide: ControlContainer,
      useFactory: () => inject(ControlContainer, { skipSelf: true }),
    },
  ],
  template: `
    <div class="field">
      <label [attr.for]="fieldId()">
        {{ label() }}
        @if (hint(); as h) {
          <span class="hint">{{ h }}</span>
        }
      </label>
      <input
        [id]="fieldId()"
        [type]="type()"
        [formControlName]="controlName()"
        [attr.autocomplete]="autocomplete()"
        [attr.min]="min()"
        [attr.max]="max()"
        [attr.step]="step()"
      />
      @if (errorMessage(); as m) {
        <small class="error" role="alert">{{ m }}</small>
      }
    </div>
  `,
  styles: `
    .field {
      display: flex;
      flex-direction: column;
      gap: var(--space-1);
    }

    label {
      font-weight: 500;
    }

    .hint {
      color: var(--color-muted);
      font-weight: 400;
      font-size: 0.85rem;
      margin-left: var(--space-1);
    }

    input {
      padding: var(--space-2) var(--space-3);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-sm);
      font: inherit;
    }

    .error {
      color: var(--color-danger);
      font-size: 0.85rem;
    }
  `,
})
export class FormField {
  private readonly container = inject(ControlContainer);

  readonly label = input.required<string>();
  readonly controlName = input.required<string>();
  readonly type = input<FieldType>('text');
  readonly hint = input<string | null>(null);
  readonly autocomplete = input<string | null>(null);
  readonly min = input<number | null>(null);
  readonly max = input<number | null>(null);
  readonly step = input<number | string | null>(null);

  protected readonly fieldId = computed(() => `ff-${this.controlName()}`);

  protected errorMessage(): string | null {
    const form = this.container.control;
    return form ? getErrorMessage(form.get(this.controlName())) : null;
  }
}
