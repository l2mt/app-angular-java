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
  {
    path: 'cuentas',
    loadComponent: () =>
      import('./features/cuentas/components/cuentas-list/cuentas-list').then(
        (m) => m.CuentasList,
      ),
    title: 'Cuentas',
  },
  {
    path: 'cuentas/nuevo',
    loadComponent: () =>
      import('./features/cuentas/components/cuenta-form/cuenta-form').then(
        (m) => m.CuentaForm,
      ),
    title: 'Nueva cuenta',
  },
  {
    path: 'cuentas/:id/editar',
    loadComponent: () =>
      import('./features/cuentas/components/cuenta-form/cuenta-form').then(
        (m) => m.CuentaForm,
      ),
    title: 'Editar cuenta',
  },
  { path: '**', redirectTo: 'clientes' },
];
