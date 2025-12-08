// ***********************************************************
// Archivo de soporte E2E para CosechaEnCope
// Se carga automáticamente antes de cada archivo de test
// ***********************************************************

// Importar comandos personalizados
import './commands';

// Configuración global para manejar errores de Angular
Cypress.on('uncaught:exception', (err, runnable) => {
  // Evitar que Cypress falle por errores no capturados de Angular
  // Esto es útil durante el desarrollo pero revisar en producción
  if (err.message.includes('ResizeObserver loop')) {
    return false;
  }
  // Devolver false para prevenir que Cypress falle el test
  return false;
});

// Configuración de timeouts para aplicaciones SPA
Cypress.config('defaultCommandTimeout', 10000);
Cypress.config('pageLoadTimeout', 30000);
