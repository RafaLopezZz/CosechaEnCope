import { defineConfig } from 'cypress';

/**
 * Configuración de Cypress para CosechaEnCope
 *
 * ARQUITECTURA HÍBRIDA SSR + SPA:
 * - Landing pages (/, /nosotros, /contacto): Thymeleaf SSR en Spring Boot
 * - Aplicación Angular: Servida bajo /app/** con base href="/app/"
 *
 * MODOS DE EJECUCIÓN:
 * 1. Desarrollo aislado (ng serve): baseUrl = http://localhost:4200
 *    - Angular sirve directamente, rutas relativas a /app/
 *    - Requiere proxy o mock para API calls
 *
 * 2. Integración completa (Spring Boot): baseUrl = http://localhost:8081
 *    - Backend sirve Angular desde /app/**
 *    - API real disponible en /cosechaencope/**
 *    - Thymeleaf SSR para landing pages
 *
 * Para cambiar el modo, modificar baseUrl o usar variable de entorno:
 * CYPRESS_BASE_URL=http://localhost:8081 npx cypress open
 */
export default defineConfig({
  e2e: {
    // Por defecto: desarrollo con ng serve
    // Para integración: usar http://localhost:8081
    baseUrl: 'http://localhost:4200',

    // Timeouts adaptados para SPA Angular
    defaultCommandTimeout: 10000,
    pageLoadTimeout: 30000,
    requestTimeout: 10000,

    // Configuración de vídeo y screenshots
    video: true,
    screenshotOnRunFailure: true,

    // Tamaño de viewport (desktop)
    viewportWidth: 1280,
    viewportHeight: 720,

    // Reintentos en CI
    retries: {
      runMode: 2, // Reintentos en modo CLI (CI/CD)
      openMode: 0 // Sin reintentos en modo interactivo
    },

    // Experimental: mejor manejo de SPA
    experimentalMemoryManagement: true,

    // Variables de entorno para tests
    env: {
      // Prefijo de rutas Angular (base href)
      appPrefix: '/app',
      // URL base de la API
      apiUrl: '/cosechaencope'
    }
  },

  component: {
    devServer: {
      framework: 'angular',
      bundler: 'webpack'
    },
    specPattern: '**/*.cy.ts'
  }
});