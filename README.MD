# Cosecha en Cope

Prototipo de marketplace agrícola: los productores publican sus productos y los clientes compran a varios productores en un mismo pedido.

Es mi proyecto final de DAM, presentado y aprobado. Es un prototipo académico: no está desplegado ni lo ha usado nadie fuera de las pruebas. Lo mantengo como proyecto de práctica de Java, Spring Boot y Angular.

**Stack:** Java 17 · Spring Boot 3.5 · Spring Security (JWT) · JPA/Hibernate · PostgreSQL 16 · Flyway · Thymeleaf · Angular 20 · AWS S3 · Docker

[Caso explicado en mi portfolio](https://rafalopezzz.github.io/web-site/work/cosecha-en-cope/)

## Qué hace

- **Cliente:** consulta el catálogo, llena un carrito y confirma un pedido.
- **Productor:** gestiona sus artículos y categorías, y atiende las órdenes de venta que le llegan.
- **Páginas públicas:** portada, categorías y productos, servidas como HTML desde el servidor.

## Decisiones y su coste

### Un pedido del cliente, una orden por productor

Un carrito puede mezclar productos de varios productores. Al confirmar, `PedidoService.crearPedido` comprueba el stock, crea el pedido, descuenta el stock y genera una orden de venta por cada productor. Todo ocurre en un método transaccional: si algo falla, no queda un pedido a medias.

Coste: más entidades y más estados que coordinar que con un pedido único.

### Thymeleaf para lo público, Angular para la aplicación

Las páginas públicas se renderizan en el servidor con Thymeleaf. La aplicación con sesión (carrito, pedidos, paneles) es una SPA de Angular servida bajo `/app` desde el mismo Spring Boot.

Lo elegí para que el contenido público fuera HTML indexable sin renunciar a una aplicación fluida. No he medido ningún efecto en posicionamiento. Coste: dos formas de renderizar, dos builds acoplados y rutas que mantener en dos sitios.

### JWT sin estado

La API autentica con JWT firmado; no hay sesión en el servidor. La clave se lee de una variable de entorno.

### Imágenes en S3

Las imágenes de los artículos se suben a AWS S3 y la base de datos guarda la referencia. Lo probé a mano con subidas y borrados reales; no hay tests automáticos de esa parte.

### Esquema con Flyway

El esquema se crea con una migración de Flyway y Hibernate solo lo valida al arrancar.

## Limitaciones conocidas

- **La autorización es incompleta.** El servidor distingue entre público y autenticado, pero casi no comprueba el rol. Las rutas de artículos y categorías están abiertas para cualquier método HTTP, y falta verificar que un productor solo toque sus propios datos.
- **Un `@PreAuthorize` incorrecto** en `UsuarioController` (`hasRole` con dos roles).
- **Actuator es público** y el log de seguridad está en nivel `DEBUG`.
- **Sin control de concurrencia en el stock.** Dos compras simultáneas del mismo artículo podrían vender más unidades de las que hay.
- **Sin integración continua.** Los tests se ejecutan a mano.
- **El build de Angular está versionado** dentro de los recursos del backend.
- **El historial de Git contiene una clave JWT antigua**, ya retirada del código. Era de desarrollo y no debe reutilizarse.

## Qué cambiaría hoy

1. Reglas de autorización por rol y por propietario en el servidor, con tests.
2. Bloqueo optimista (`@Version`) en el stock.
3. CI que ejecute los tests del backend y el build del frontend en cada pull request.
4. Sacar el build de Angular del repositorio y generarlo en el empaquetado.

## Tests

```bash
cd backend/cosechaencope
./mvnw test
```

95 tests de backend con JUnit 5, sobre los servicios y el manejador de errores. Usan H2 en memoria.

El frontend tiene specs de componentes y un flujo de compra de extremo a extremo con Cypress (`npm run cypress:run`), que necesita la aplicación arrancada.

## Ejecutarlo

Con Docker:

```bash
cp .env.docker.example .env
# edita .env y define JWT_SECRET (openssl rand -base64 48)
docker compose up --build
```

| Qué | Dónde |
| --- | --- |
| Páginas públicas | http://localhost:8081 |
| Aplicación Angular | http://localhost:8081/app |
| Swagger UI | http://localhost:8081/swagger-ui.html |

Sin credenciales reales de AWS, la subida de imágenes no funciona; el resto sí.

## Estructura

```text
backend/cosechaencope/              API REST, seguridad, páginas Thymeleaf y migraciones
frontend/cosechaencope-frontend/    Aplicación Angular y tests Cypress
docs/database/                      Esquema SQL de referencia
```

## Cómo se hizo

Lo diseñé y desarrollé yo desde cero: casos de uso, modelo de datos, arquitectura e implementación. Me apoyé en documentación técnica, Stack Overflow y GPT-3.
