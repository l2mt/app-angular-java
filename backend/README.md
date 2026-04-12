# Backend — Cuentas API

Spring Boot con Java 21 y Gradle.

## Desarrollo local

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Disponible en http://localhost:8080
En Windows, usa `.\gradlew.bat bootRun`.

Perfil `dev`:
- Usa H2 en memoria.

Perfil `prod`:
- Espera PostgreSQL mediante `SPRING_DATASOURCE_*`.
- Se activa automáticamente dentro de `docker-compose`.

## Tests

```bash
./gradlew test
```

En Windows, usa `.\gradlew.bat test`.
