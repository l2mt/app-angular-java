# Frontend — Cuentas Web

Angular 21 servido con Nginx en contenedores.

## Desarrollo local

```bash
npm install
npm start
```

Disponible en http://localhost:4200

El script `npm start` usa `proxy.conf.json` para redirigir `/api` al backend local en `http://localhost:8080`.

## Tests

```bash
npm test
```
