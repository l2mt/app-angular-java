import { ChangeDetectionStrategy, Component, input } from '@angular/core';

type AlertTone = 'error' | 'info';

@Component({
  selector: 'app-alert',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p class="alert" [class.alert--info]="tone() === 'info'" role="alert">
      {{ message() }}
    </p>
  `,
  styles: `
    .alert {
      padding: var(--space-3);
      border-radius: var(--radius-sm);
      background-color: #fef2f2;
      color: var(--color-danger);
      border: 1px solid #fecaca;
      margin: 0;
    }

    .alert--info {
      background-color: var(--color-surface);
      color: var(--color-fg);
      border-color: var(--color-border);
    }
  `,
})
export class Alert {
  readonly message = input.required<string>();
  readonly tone = input<AlertTone>('error');
}
