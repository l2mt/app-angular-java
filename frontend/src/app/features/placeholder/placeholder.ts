import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="placeholder" [attr.aria-labelledby]="headingId">
      <h2 [id]="headingId" class="placeholder__title">{{ titulo() }}</h2>

      <label class="placeholder__search">
        <span class="visually-hidden">Buscar en {{ titulo() }}</span>
        <input type="search" placeholder="Buscar" disabled aria-disabled="true" />
      </label>

      <ul class="placeholder__list" aria-label="Contenido pendiente">
        @for (row of rows; track row) {
          <li class="placeholder__row"></li>
        }
      </ul>
    </section>
  `,
  styles: `
    .placeholder {
      display: flex;
      flex-direction: column;
      gap: var(--space-4);
      max-width: 720px;
    }

    .placeholder__title {
      font-size: 1.75rem;
      font-weight: 500;
      color: var(--color-fg);
    }

    .placeholder__search input {
      width: 100%;
      max-width: 280px;
      padding: var(--space-2) var(--space-3);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-sm);
      background-color: var(--color-bg);
      font: inherit;
      color: var(--color-muted);
    }

    .placeholder__list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: var(--space-3);
    }

    .placeholder__row {
      height: 40px;
      background-color: var(--color-surface);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-sm);
    }

    .visually-hidden {
      position: absolute;
      width: 1px;
      height: 1px;
      padding: 0;
      margin: -1px;
      overflow: hidden;
      clip: rect(0, 0, 0, 0);
      white-space: nowrap;
      border: 0;
    }
  `,
})
export class Placeholder {
  private readonly route = inject(ActivatedRoute);
  protected readonly titulo = signal(this.route.snapshot.title ?? 'Sección');
  protected readonly headingId = `placeholder-title-${Math.random().toString(36).slice(2, 8)}`;
  protected readonly rows = [0, 1, 2, 3, 4];
}
