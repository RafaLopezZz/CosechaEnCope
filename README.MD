# 🌾 Cosecha en Cope - eCommerce B2B2C Escalable

> **Plataforma de comercio electrónico con arquitectura híbrida (Spring Boot + Angular 19) y gestión de medios en Cloud.**

![Estado del Proyecto](https://img.shields.io/badge/Estado-MVP%20Funcional-green)
![Tech](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen)
![Tech](https://img.shields.io/badge/Frontend-Angular%2019-red)
![Infra](https://img.shields.io/badge/Cloud-AWS%20S3-orange)

## 📋 Visión del Proyecto (El Reto de Negocio)
Este proyecto nace como respuesta a la necesidad de digitalizar la venta de productos agrícolas, gestionando dos canales simultáneos: **Venta directa al consumidor (B2C)** y **distribución mayorista (B2B)**.

El desafío técnico principal fue crear una plataforma que garantizara la **indexación SEO** (crítica para ventas) sin sacrificar la **fluidez de navegación** de una Single Page Application (SPA), asegurando a la vez que el crecimiento del catálogo de imágenes no degradara el rendimiento del servidor.

## 🏗️ Arquitectura y Decisiones Técnicas
La solución implementa una arquitectura desacoplada para garantizar escalabilidad y mantenibilidad:

### Diagrama de Arquitectura (Conceptual)
`Angular (Cliente) <--> API REST (Spring Boot) <--> PostgreSQL`
`       |                                            ^`
`       v                                            |`
`    AWS S3 (Imágenes) ------------------------------+`

### Decisiones Clave:
1.  **Backend Robusto (Spring Boot 3):** Elegido para garantizar la seguridad transaccional en los pagos y la gestión de roles complejos (Admin vs Cliente).
2.  **Frontend Reactivo (Angular 19):** Implementado con Standalone Components para optimizar el rendimiento y modularidad.
3.  **Almacenamiento Cloud (AWS S3):** **Decisión crítica.** Se desacoplaron los activos estáticos (imágenes de productos) del servidor de aplicación.
    *   *Impacto:* Permite escalar el catálogo a miles de productos sin aumentar la carga ni el coste de almacenamiento del servidor backend.
4.  **Testing E2E (Cypress):** Enfoque en la fiabilidad de las ventas. Se automatizaron flujos críticos de compra para evitar regresiones en producción.

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Propósito |
|---|---|---|
| **Backend** | Java 17, Spring Boot 3 | API REST, Seguridad (Spring Security + JWT) |
| **Frontend** | Angular 19, TypeScript | Interfaz de usuario, Gestión de estado |
| **Base de Datos** | PostgreSQL | Persistencia relacional robusta |
| **Cloud / Infra** | AWS S3 SDK | Almacenamiento escalable de objetos |
| **Testing** | JUnit 5, Cypress | Tests Unitarios y End-to-End |
| **Herramientas** | Docker, Maven | Contenedorización y gestión de dependencias |

## 🚀 Funcionalidades Principales
- **Gestión de Catálogo Dinámico:** CRUD completo con subida de imágenes optimizada a AWS.
- **Carrito de Compras Persistente:** Lógica de negocio para cálculo de totales e impuestos.
- **Pasarela de Pagos (Simulación):** Integración preparada para Stripe/PayPal.
- **Panel de Administración:** Gestión de pedidos y usuarios securizada.

## ⚙️ Instalación y Despliegue Local

### Prerrequisitos
- Java JDK 17+
- Node.js 20+ & Angular CLI
- PostgreSQL
- Cuenta AWS (para credenciales S3)

### 1. Clonar el repositorio
git clone https://github.com/RafaLopezZz/CosechaEnCope.git

### 2. Configuración del backend
- Navegar a la carpeta `backend` y configurar el archivo `application.properties` con las credenciales de PostgreSQL y AWS S3.
spring.datasource.url=jdbc:postgresql://localhost:5432/cosechadb
spring.datasource.username=tu_usuario
aws.access.key=tu_aws_key
aws.secret.key=tu_aws_secret

### 3. Configuración del frontend
- Navegar a la carpeta `frontend` y configurar el archivo `environment.ts` con la URL del backend.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};

- Ejecutar  ng build --configuration production para compilar el frontend.

### 4. Ejecutar la aplicación
- Iniciar el backend: ./mvnw spring-boot:run


