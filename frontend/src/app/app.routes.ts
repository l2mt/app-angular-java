import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clientes' },
  {
    path: 'clientes',
    loadComponent: () =>
      import('./features/clientes/components/clientes-list/clientes-list').then(
        (m) => m.ClientesList,
      ),
    title: 'Clientes',
  },
  {
    path: 'clientes/nuevo',
    loadComponent: () =>
      import('./features/clientes/components/cliente-form/cliente-form').then(
        (m) => m.ClienteForm,
      ),
    title: 'Nuevo cliente',
  },
  {
    path: 'clientes/:id/editar',
    loadComponent: () =>
      import('./features/clientes/components/cliente-form/cliente-form').then(
        (m) => m.ClienteForm,
      ),
    title: 'Editar cliente',
  },
  { path: '**', redirectTo: 'clientes' },
];
