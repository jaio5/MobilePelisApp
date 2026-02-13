# Documentación de la API REST de PelisApp

Esta documentación describe todos los endpoints públicos de la API REST de PelisApp, orientada a desarrolladores que deseen crear aplicaciones cliente (por ejemplo, en Kotlin).

## Autenticación
- **Registro:** `POST /api/auth/register`
- **Login:** `POST /api/auth/login`
- **Refresh Token:** `POST /api/auth/refresh`
- El login devuelve un token JWT que debe enviarse en el header `Authorization: Bearer <token>` en las peticiones protegidas.

---

## Endpoints principales

### Usuarios

#### Obtener datos del usuario autenticado
- **GET** `/api/users/me`
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
```json
{
  "id": 1,
  "username": "usuario1",
  "email": "usuario1@email.com",
  ...
}
```

#### Obtener reviews del usuario autenticado (paginado)
- **GET** `/api/users/me/reviews?page=0&size=10`
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
```json
{
  "content": [ { "id": 1, "movieId": 2, "text": "Muy buena", ... }, ... ],
  "totalElements": 20,
  "totalPages": 2,
  "number": 0
}
```

---

### Autenticación

#### Registro
- **POST** `/api/auth/register`
- **Body:**
```json
{
  "username": "nuevoUsuario",
  "email": "correo@dominio.com",
  "password": "claveSegura"
}
```
- **Response:**
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "user": { ... }
}
```

#### Login
- **POST** `/api/auth/login`
- **Body:**
```json
{
  "username": "usuario1",
  "password": "clave"
}
```
- **Response:** igual que registro.

#### Refresh Token
- **POST** `/api/auth/refresh`
- **Body:**
```json
{ "refreshToken": "..." }
```
- **Response:** igual que login.

---

### Películas

#### Listar películas (paginado)
- **GET** `/api/movies?page=0&size=12`
- **Response:**
```json
{
  "content": [ { "id": 1, "title": "Matrix", ... }, ... ],
  "totalElements": 100,
  "totalPages": 9,
  "number": 0
}
```

#### Detalles de una película
- **GET** `/api/movies/{id}/details`
- **Response:**
```json
{
  "id": 1,
  "title": "Matrix",
  "overview": "...",
  ...
}
```

#### Archivos de una película
- **GET** `/api/movies/{id}/files`
- **Response:**
```json
{
  "movieId": 1,
  "files": [
    { "name": "matrix.mp4", "size": 123456789, "downloadUrl": "/movies/download/1/matrix.mp4", "streamUrl": "/movies/stream/1/matrix.mp4" }
  ],
  "totalFiles": 1
}
```

#### Películas por categoría
- **GET** `/api/movies/by-category?category=accion&page=0&size=12`

---

### Reviews

#### Crear review
- **POST** `/api/reviews`
- **Body:**
```json
{
  "userId": 1,
  "movieId": 2,
  "text": "Muy buena",
  "stars": 5
}
```
- **Response:**
```json
{
  "id": 10,
  "userId": 1,
  "movieId": 2,
  "text": "Muy buena",
  "stars": 5,
  ...
}
```

#### Like a una review
- **POST** `/api/reviews/{id}/like?userId=1`

---

### Salud del sistema

#### Estado general
- **GET** `/api/system/health`
- **Response:**
```json
{
  "database": { "connected": true, "message": "Base de datos conectada", ... },
  "tmdb": { "connected": true, ... },
  ...
}
```

#### Estado de un servicio
- **GET** `/api/system/health/{service}`

---

## Errores
- Los errores se devuelven con códigos HTTP estándar (400, 401, 403, 404, 500).
- El cuerpo suele tener `{ "success": false, "message": "..." }` o `{ "error": "..." }`.

---

## Paginación
- Los endpoints que devuelven listas usan paginación Spring: parámetros `page` y `size`.
- El objeto de respuesta incluye `content`, `totalElements`, `totalPages`, `number`.

---

## Recomendaciones para clientes Kotlin
- Usar librerías como Ktor o Retrofit para consumir la API.
- Serializar/deserializar JSON con kotlinx.serialization o Moshi.
- Añadir el header `Authorization: Bearer <token>` tras login.
- Manejar errores HTTP y estructura de error JSON.

---

## Ejemplo de flujo de autenticación en Kotlin (pseudocódigo)
```kotlin
val loginResp = post("/api/auth/login", LoginRequest(...))
val token = loginResp.accessToken
val user = get("/api/users/me") { header("Authorization", "Bearer $token") }
```

---

Para dudas o detalles adicionales, consulta el código fuente o contacta con el equipo de backend.

