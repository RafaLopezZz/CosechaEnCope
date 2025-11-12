# Cosecha en Cope - AI Agent Instructions

## Project Overview
Plataforma web de comercio agrícola local conectando productores y consumidores. TFC (Final Course Project) en desarrollo.

**Stack**: Spring Boot 3.5.2 + Java 17 | Angular 20 | PostgreSQL 15 | JWT Auth | AWS S3 | Thymeleaf SSR

## Architecture: Hybrid SSR + SPA

### Landing Pages (Thymeleaf SSR)
- **Why**: SEO optimization para páginas públicas (`/`, `/nosotros`, `/contacto`)
- **Location**: `backend/src/main/resources/templates/`
- **Controller**: `HomeController.java` maneja rendering server-side
- **Key**: Landing carga categorías/artículos dinámicamente para SEO

### SPA Application (Angular)
- **Routes**: Todas bajo `/app/**` (auth, productor, cliente, articulos, categorías)
- **Build Output**: `ng build --configuration production` → `backend/src/main/resources/static/app/`
- **Routing**: `AngularSpaController.java` sirve `index.html` para rutas Angular
- **Critical**: NO crear forwards en `/app/**` - causa StackOverflowError (ver comentarios en HomeController)

### API Backend
- **Base URL**: `/cosechaencope/**` (REST API)
- **Public Endpoints**: `/auth/**`, GET `/articulos`, GET `/categorias`
- **Protected**: Requieren JWT Bearer token en header Authorization
- **Controllers**: `controller/` (REST) vs `controller/web/` (Thymeleaf)

## Authentication & Authorization

### JWT Flow
1. Login: `POST /cosechaencope/auth/login` → returns JWT + user data
2. Frontend: Store token in `sessionStorage.authToken`
3. Requests: `tokenInterceptor` añade `Authorization: Bearer {token}`
4. Backend: `AuthTokenFilter` valida JWT, carga UserDetails

### User Types
- **Usuario** base entity con `tipoUsuario`: CLIENTE | PRODUCTOR
- **Roles**: USER, ADMIN, SUPERADMIN (campo opcional `rol`)
- **Guards**: `authGuard` (logged in) + `roleGuard` (specific tipo)
- **Key Pattern**: Cada tipo usuario tiene su propia jerarquía de rutas (`/cliente/**`, `/productor/**`)

### Security Config
- CSRF disabled (stateless API)
- CORS enabled for local dev
- `shouldNotFilter()` excluye archivos estáticos (extensiones + Angular chunks)
- Public: `/`, `/nosotros`, `/contacto`, `/api-docs`, `/swagger-ui/**`

## Development Workflows

### Backend (Maven + Spring Boot)
```powershell
cd backend/cosechaencope
./mvnw spring-boot:run  # Runs on port 8081
```
- **Lombok**: Requiere annotation processing en IDE
- **Logs**: `logs/cosechaencope.log` (nivel DEBUG para security/web)
- **API Docs**: http://localhost:8081/swagger-ui.html
- **Actuator**: `/actuator/health`, `/actuator/metrics`

### Frontend (Angular)
```powershell
cd frontend/cosechaencope-frontend
npm start  # Dev server port 4200 (usa proxy.conf.json si existe)
ng build --configuration production  # Output to backend/static/app/
```
- **Angular 20** con standalone components (no NgModules)
- **Prettier Config**: printWidth 100, singleQuote true
- **Imports**: Barrel exports en `core/services/index.ts`
- **Styles**: SCSS global + component-scoped

### Database
- **Schema**: `docs/database/cosechaencope.sql`
- **JPA**: `spring.jpa.hibernate.ddl-auto=update` (auto-schema sync)
- **Env Vars**: `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD`
- **Config**: `.env` file con `spring.config.import=optional:file:.env[.properties]`

### Image Upload (AWS S3)
- **Env Vars**: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `AWS_S3_BUCKET_NAME`
- **Service**: `ImagenController.java` + `ImagenService.java`
- **Frontend**: `ImagenService` (Angular) - NO establecer Content-Type en FormData (browser auto-maneja)
- **Max Size**: 10MB (`spring.servlet.multipart.max-file-size`)

## Code Conventions

### Backend
- **Package Structure**: `com.rlp.cosechaencope.{model, repository, service, controller, dto, security, config, exception}`
- **DTOs**: Separados en `dto/request/` y `dto/response/`
- **Error Handling**: `GlobalExceptionHandler` con `@RestControllerAdvice`
- **Swagger**: Añadir `@Hidden` a controllers Thymeleaf (no son API)

### Frontend
- **Structure**: `features/{module}/{component}/` + `core/{services, guards, interceptors}/` + `shared/{components, models}/`
- **Lazy Loading**: Dashboard productor usa `loadComponent()`
- **HTTP**: `HttpClient` con functional interceptors (`withInterceptors([tokenInterceptor])`)
- **State**: `sessionStorage` para token, servicios singleton para data

## Common Pitfalls

1. **StackOverflowError en `/app/**`**: NO usar `forward:` - AngularSpaController retorna HTML directo
2. **CORS en producción**: Actualizar `allowedOrigins` en SecurityConfig
3. **JWT expiration**: `app.jwt.expirationMs=86400000` (24h) - refresh no implementado aún
4. **FormData uploads**: NO establecer Content-Type manualmente - causa errores de boundary
5. **Route precedence**: Spring MVC `@GetMapping` es order-sensitive - específicas antes que wildcards

## Key Files Reference
- Auth flow: `backend/security/AuthTokenFilter.java` ↔ `frontend/core/interceptors/token.interceptor.ts`
- Routing: `backend/controller/web/HomeController.java` + `AngularSpaController.java` ↔ `frontend/app.routes.ts`
- Environment: `backend/src/main/resources/application.properties` ↔ `frontend/src/environments/environment.ts`
