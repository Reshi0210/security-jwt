# Spring Security + JWT - Proyecto de Ejemplo

## Descripcion General

Este proyecto es una API REST construida con **Spring Boot 4.1** que implementa autenticacion y autorizacion usando **JSON Web Tokens (JWT)**. Utiliza una base de datos en memoria **H2** para almacenar usuarios.

---

## Arquitectura del Proyecto

```
com.rafael.securityjwt
├── config/
│   ├── SecurityConfig.java          # Configuracion de Spring Security
│   └── DataInitializer.java         # Carga inicial del usuario admin
├── controller/
│   ├── AuthController.java          # Endpoints de login y registro
│   └── HelloController.java         # Endpoints protegidos de ejemplo
├── dto/
│   ├── LoginRequest.java            # DTO para peticiones de login
│   ├── RegisterRequest.java         # DTO para peticiones de registro
│   └── AuthResponse.java           # DTO para respuestas con token
├── entity/
│   └── User.java                    # Entidad JPA de usuario
├── repository/
│   └── UserRepository.java          # Acceso a datos de usuario
├── security/
│   ├── JwtUtil.java                 # Utilidad para generar/validar tokens
│   └── JwtAuthenticationFilter.java # Filtro que intercepta cada request
└── service/
    └── UserDetailsServiceImpl.java  # Carga usuarios desde la BD para Spring Security
```

---

## Como Funciona JWT (Flujo Completo)

### 1. Registro de usuario (`POST /auth/register`)
```
Cliente                          Servidor
  |                                 |
  |-- POST /auth/register -------->|
  |   { username, password }        |
  |                                 |-- Verifica que no exista el usuario
  |                                 |-- Encripta password con BCrypt
  |                                 |-- Guarda en BD
  |                                 |-- Genera token JWT
  |<-------- 200 OK ---------------|
  |   { token, username, role }     |
```

### 2. Login (`POST /auth/login`)
```
Cliente                          Servidor
  |                                 |
  |-- POST /auth/login ----------->|
  |   { username, password }        |
  |                                 |-- AuthenticationManager valida credenciales
  |                                 |-- Si son correctas, genera token JWT
  |<-------- 200 OK ---------------|
  |   { token, username, role }     |
  |                                 |
  | Si las credenciales son malas:  |
  |<-------- 401 Unauthorized -----|
```

### 3. Acceso a endpoints protegidos
```
Cliente                          Servidor
  |                                 |
  |-- GET /hello ----------------->|
  |   Header: Authorization:        |
  |   Bearer eyJhbGciOi...         |
  |                                 |
  |                                 |-- JwtAuthenticationFilter intercepta
  |                                 |-- Extrae token del header "Authorization"
  |                                 |-- Valida firma y expiracion
  |                                 |-- Extrae username del token
  |                                 |-- Carga UserDetails desde la BD
  |                                 |-- Establece autenticacion en SecurityContext
  |                                 |-- El request continua al controller
  |                                 |
  |<-------- 200 OK ---------------|
  |   { message, role }             |
```

### 4. Que contiene un token JWT?

Un JWT tiene 3 partes separadas por puntos: `header.payload.signature`

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTcxOTAwMDAwMCwiZXhwIjoxNzE5MDg2NDAwfQ.firma_aqui
```

- **Header**: Algoritmo de firma (HS256)
- **Payload**: Datos del usuario (username, role, fecha de emision, fecha de expiracion)
- **Signature**: Firma digital creada con la clave secreta del servidor. Garantiza que nadie altero el token.

---

## Endpoints

| Metodo | URL               | Acceso        | Descripcion                    |
|--------|-------------------|---------------|--------------------------------|
| POST   | `/auth/login`     | Publico       | Autenticarse y obtener token   |
| POST   | `/auth/register`  | Publico       | Crear nuevo usuario (rol USER) |
| GET    | `/hello`          | Autenticado   | Saludo con info del usuario    |
| GET    | `/admin/dashboard`| Solo ADMIN    | Panel solo para administradores|
| -      | `/h2-console`     | Publico       | Consola web de la BD H2        |

---

## Como Probarlo

### 1. Levantar la aplicacion
```bash
./gradlew bootRun
```
Al arrancar se crea automaticamente el usuario admin:
- **Username:** `admin`
- **Password:** `admin123`
- **Rol:** `ADMIN`

### 2. Login con admin
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Respuesta:
```json
{
  "token": "eyJhbGciOi...",
  "username": "admin",
  "role": "ADMIN"
}
```

### 3. Registrar un nuevo usuario
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"rafael","password":"mipassword"}'
```

### 4. Acceder a endpoint protegido
```bash
curl http://localhost:8080/hello \
  -H "Authorization: Bearer eyJhbGciOi..."
```

### 5. Acceder al panel admin (solo con token de admin)
```bash
curl http://localhost:8080/admin/dashboard \
  -H "Authorization: Bearer TOKEN_DEL_ADMIN"
```

Un usuario con rol USER recibira un **403 Forbidden** en este endpoint.

### 6. Consola H2
Accede a `http://localhost:8080/h2-console` con:
- JDBC URL: `jdbc:h2:mem:securitydb`
- User: `sa`
- Password: *(vacio)*

---

## Conceptos Clave Explicados

### BCrypt
Algoritmo de hash para passwords. Nunca se guarda la contraseña en texto plano. BCrypt genera un hash diferente cada vez (por el salt), pero puede verificar si un password coincide con su hash.

### SecurityFilterChain
Cadena de filtros que Spring Security aplica a cada peticion HTTP. Aqui definimos:
- Que rutas son publicas (`/auth/**`)
- Que rutas requieren un rol especifico (`/admin/**` requiere `ROLE_ADMIN`)
- Que las sesiones son stateless (no se usan cookies de sesion, solo JWT)

### OncePerRequestFilter
Clase base que garantiza que nuestro filtro JWT se ejecute exactamente una vez por cada peticion HTTP. Hereda de ella `JwtAuthenticationFilter`.

### AuthenticationManager
Componente de Spring Security que coordina la autenticacion. Recibe username/password y delega la validacion al `UserDetailsService` y al `PasswordEncoder`.

### SecurityContextHolder
Almacen thread-local donde Spring Security guarda la informacion del usuario autenticado durante el procesamiento de una peticion. Cualquier parte del codigo puede consultar quien esta autenticado.

---

## Tecnologias Usadas

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Security** - Framework de seguridad
- **JJWT 0.12.6** - Libreria para generar y validar tokens JWT
- **H2 Database** - Base de datos en memoria para desarrollo
- **Spring Data JPA** - Acceso a datos con repositorios
- **Lombok** - Reduccion de boilerplate (getters, setters, constructores)
- **Gradle** - Build tool

---

## Estructura de la Base de Datos

### Tabla `users`
| Columna    | Tipo         | Restricciones       |
|------------|-------------|----------------------|
| id         | BIGINT (PK) | Auto-incremento      |
| username   | VARCHAR     | NOT NULL, UNIQUE     |
| password   | VARCHAR     | NOT NULL (hash BCrypt)|
| role       | VARCHAR     | NOT NULL             |

---

## Posibles Mejoras

- Agregar refresh tokens para renovar el JWT sin re-login
- Validacion de DTOs con `@Valid` y Bean Validation
- Roles multiples (muchos-a-muchos) en lugar de un solo String
- Manejo global de excepciones con `@ControllerAdvice`
- Tests unitarios e integracion
- Swagger/OpenAPI para documentacion interactiva
