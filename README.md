# Cuentas App

Aplicación fullstack con Angular en el frontend y Spring Boot en el backend para la gestión de clientes, cuentas, movimientos y reporte de estado de cuenta.

## Requisitos

- Docker
- Node.js 22+
- Java 21

## Estructura

```text
├── BaseDatos.sql
├── frontend/
├── backend/
└── docker-compose.yml
```

## Base de datos

- El archivo requerido por la consigna es `BaseDatos.sql`.
- En producción el backend usa `spring.jpa.hibernate.ddl-auto=validate`.
- Eso significa que Spring Boot valida el esquema, pero no crea tablas.
- El esquema debe existir previamente y se inicializa desde `BaseDatos.sql`.

## Ejecución en desarrollo

En desarrollo el backend usa el perfil `dev` y levanta una base H2 en memoria.

### 1. Backend

Linux/macOS:

```bash
cd backend
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Windows:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="dev"
.\gradlew.bat bootRun
```

### 2. Frontend

```bash
cd frontend
npm install
npm start
```

### 3. URLs en desarrollo

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend | http://localhost:8080 |
| API vía proxy Angular | http://localhost:4200/api |

### 4. Postman en desarrollo

En el ambiente de Postman define la variable `baseUrl`.

Puedes usar cualquiera de estas opciones:

- `http://localhost:8080`
  cuando quieras consumir el backend directamente
- `http://localhost:4200/api`
  cuando tengas el frontend levantado y quieras pasar por el proxy de Angular

## Ejecución en producción

En producción la aplicación se despliega con Docker Compose:

- `postgres` como base de datos relacional
- `backend` con Spring Boot en perfil `prod`
- `frontend` con Angular compilado y servido por Nginx

### 1. Levantar el ambiente

Para un arranque limpio:

```bash
docker compose down -v
docker compose up --build -d
```

### 2. Importante sobre `BaseDatos.sql`

- `docker compose down -v` elimina el volumen de PostgreSQL.
- En el siguiente arranque, PostgreSQL ejecuta `BaseDatos.sql` automáticamente.
- Si no eliminas el volumen, el script de inicialización no se vuelve a ejecutar.

### 3. URLs en producción

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4800 |
| API visible al usuario | http://localhost:4800/api |
| Backend | interno dentro de Docker |

### 4. Postman en producción

En el ambiente de Postman configura la variable:

```text
baseUrl = http://localhost:4800/api
```

## Verificación rápida

### Desarrollo

Prueba básica del backend:

```text
GET http://localhost:8080/clientes
```

Si usas el frontend con proxy:

```text
GET http://localhost:4200/api/clientes
```

### Producción

Prueba básica de la API publicada:

```text
GET http://localhost:4800/api/clientes
```

Reporte:

```text
GET http://localhost:4800/api/reportes?clienteId=1&fechaDesde=2026-04-01&fechaHasta=2026-04-30
```

## Endpoints disponibles

| Recurso | Método | URL |
|---|---|---|
| Clientes | `GET`, `POST` | `/clientes` |
| Cliente por id | `GET`, `PUT`, `PATCH`, `DELETE` | `/clientes/{clienteId}` |
| Cuentas | `GET`, `POST` | `/cuentas` |
| Cuenta por id | `GET`, `PUT`, `PATCH`, `DELETE` | `/cuentas/{cuentaId}` |
| Movimientos | `GET`, `POST` | `/movimientos` |
| Movimiento por id | `GET`, `PUT`, `PATCH`, `DELETE` | `/movimientos/{movimientoId}` |
| Reporte | `GET` | `/reportes?clienteId={id}&fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD` |

## Payloads de ejemplo

`POST /clientes`

```json
{
  "nombre": "Jose Lema",
  "genero": "MASCULINO",
  "edad": 30,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "contrasena": "1234",
  "estado": true
}
```

`POST /cuentas`

```json
{
  "clienteId": 1,
  "numeroCuenta": "478758",
  "tipoCuenta": "AHORROS",
  "saldoInicial": 2000.00,
  "estado": true
}
```

`POST /movimientos`

```json
{
  "cuentaId": 1,
  "fecha": "2026-04-13T10:00:00",
  "tipoMovimiento": "DEBITO",
  "valor": -575.00
}
```

## Pruebas

Backend:

```bash
cd backend
./gradlew test
```

En Windows:

```powershell
cd backend
.\gradlew.bat test
```

Frontend:

```bash
cd frontend
npm test
```
