// Comandos personalizados para CosechaEnCope
//
// ARQUITECTURA HÍBRIDA SSR + SPA:
// - Landing (/, /nosotros): Thymeleaf SSR
// - App Angular: /app/** (base href="/app/")
//
// Todos los comandos usan getAppUrl() para construir
// rutas correctas según la arquitectura.

declare namespace Cypress {
  interface Chainable<Subject = any> {
    /**
     * Construye la URL completa para rutas de la SPA Angular
     * Tiene en cuenta el base href="/app/"
     * @example cy.getAppUrl('/cliente/login') → '/app/cliente/login'
     */
    getAppUrl(path: string): string;

    /**
     * Navega a una ruta de la SPA Angular
     * @example cy.visitApp('/cliente/login')
     */
    visitApp(path: string): Chainable<Cypress.AUTWindow>;

    /**
     * Login como cliente con email y password
     * @example cy.loginAsCliente('cliente@test.com', 'password123')
     */
    loginAsCliente(email: string, password: string): Chainable<void>;

    /**
     * Añadir artículo al carrito desde el listado
     * @example cy.addToCart(0) // Primer artículo
     */
    addToCart(index: number): Chainable<void>;

    /**
     * Limpiar sessionStorage y localStorage (logout)
     */
    clearAuth(): Chainable<void>;

    /**
     * Navegar a la landing page SSR (Thymeleaf)
     * @example cy.visitLanding('/nosotros')
     */
    visitLanding(path: string): Chainable<Cypress.AUTWindow>;

    /**
     * Vaciar el carrito del usuario autenticado
     * Requiere que haya un token JWT válido en sessionStorage
     * @example cy.vaciarCarrito()
     */
    vaciarCarrito(): Chainable<void>;
  }
}

/**
 * Función helper para construir URLs de la SPA
 * Considera que Angular usa base href="/app/"
 */
function getAppUrl(path: string): string {
  // Normalizar path (asegurar que empieza con /)
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  // En desarrollo con ng serve, el base href /app/ ya está configurado
  // En producción con Spring Boot, las rutas están bajo /app/
  return `/app${normalizedPath}`;
}

/**
 * Comando para navegar a rutas de la SPA Angular
 */
Cypress.Commands.add('visitApp', (path: string) => {
  const fullUrl = getAppUrl(path);
  cy.log(`Navegando a SPA: ${fullUrl}`);
  return cy.visit(fullUrl);
});

/**
 * Comando para navegar a landing pages SSR (Thymeleaf)
 * Estas rutas NO tienen el prefijo /app/
 */
Cypress.Commands.add('visitLanding', (path: string) => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  cy.log(`Navegando a Landing SSR: ${normalizedPath}`);
  return cy.visit(normalizedPath);
});

/**
 * Comando personalizado para login como cliente
 * NOTA: Se usa selector específico '.auth form.form' para evitar
 * conflicto con el formulario de newsletter en el footer
 */
Cypress.Commands.add('loginAsCliente', (email: string, password: string) => {
  cy.visitApp('/cliente/login');
  cy.get('input#email').type(email);
  cy.get('input#password').type(password);
  // Selector específico: formulario dentro de la sección auth
  cy.get('.auth form.form').submit();
  // Esperar redirección después del login (a /app/articulos)
  cy.url().should('include', '/app/');
  cy.url().should('not.include', '/login');
});

/**
 * Comando para añadir un artículo al carrito
 */
Cypress.Commands.add('addToCart', (index: number) => {
  cy.get('.btn-add-cart').eq(index).click();
});

/**
 * Comando para limpiar autenticación
 */
Cypress.Commands.add('clearAuth', () => {
  cy.window().then((win) => {
    win.sessionStorage.clear();
    win.localStorage.clear();
  });
});

/**
 * Comando para vaciar el carrito vía API
 * Requiere autenticación previa (token en sessionStorage)
 */
Cypress.Commands.add('vaciarCarrito', () => {
  cy.window().then((win) => {
    const token = win.sessionStorage.getItem('authToken');
    if (token) {
      cy.request({
        method: 'DELETE',
        url: '/cosechaencope/carrito/vaciar',
        headers: {
          Authorization: `Bearer ${token}`,
        },
        failOnStatusCode: false, // No fallar si el carrito ya está vacío
      });
    }
  });
});
