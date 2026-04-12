# Cuentas App

Aplicación fullstack con Angular (frontend) y Spring Boot (backend).

## Requisitos

- [Docker](https://docs.docker.com/get-docker/)
- Node.js 22+ para desarrollo local del frontend
- Java 21 para desarrollo local del backend

## Desarrollo local

Backend:

```bash
cd backend
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Frontend:

```bash
cd frontend
npm install
npm start
```

En local, Angular sirve la app en `http://localhost:4200` y redirige `/api` al backend en `http://localhost:8080` mediante `proxy.conf.json`.
En Windows, el backend se arranca con `.\gradlew.bat bootRun`.

## Docker Compose

```bash
docker compose up --build
```

Para detener los servicios:

```bash
docker compose down
```

## URLs

| Servicio | URL |
|---|---|
| Frontend (Docker) | http://localhost:4800 |
| Frontend (dev) | http://localhost:4200 |
| Backend | interno, accesible desde el frontend en `/api` |

## Estructura del proyecto

```
├── frontend/    # Angular + Nginx
├── backend/     # Spring Boot + Gradle
└── docker-compose.yml
```
