/**
 * Test E2E - Flujo de Compra Completo
 *
 * ARQUITECTURA HÍBRIDA SSR + SPA:
 * - Landing pages (/, /nosotros, /contacto): Thymeleaf SSR (Spring Boot)
 * - Aplicación Angular: /app/** con base href="/app/"
 *
 * Escenario: Un cliente autenticado realiza una compra desde el catálogo
 * hasta la confirmación del pedido.
 *
 * Flujo dentro de la SPA (/app/**):
 * 1. Login → /app/cliente/login
 * 2. Catálogo → /app/articulos
 * 3. Carrito → /app/carrito
 * 4. Checkout → /app/checkout (4 pasos)
 *
 * REQUISITOS:
 * - Desarrollo: `ng serve` en localhost:4200
 * - Integración: Spring Boot en localhost:8081 + backend running
 *
 * @author rafalopezzz
 */
describe('Flujo de Compra E2E', () => {
  // Prefijo para rutas de la SPA Angular
  const APP_PREFIX = '/app';

  beforeEach(() => {
    // Limpiar estado antes de cada test
    cy.clearAuth();
  });

  /**
   * Test principal: Flujo completo de compra
   */
  it('debería completar el flujo de compra desde login hasta confirmación', () => {
    // PASO 1: LOGIN
    cy.log('**PASO 1: Login como cliente**');

    cy.fixture('cliente').then((cliente) => {
      // Navegar a login usando comando adaptado a arquitectura híbrida
      cy.visitApp('/cliente/login');
      cy.url().should('include', `${APP_PREFIX}/cliente/login`);

      // Completar formulario de login
      cy.get('input#email').should('be.visible').type(cliente.email);
      cy.get('input#password').should('be.visible').type(cliente.password);

      // Submit con selector específico (evita conflicto con form newsletter del footer)
      cy.get('.auth form.form').submit();
      cy.url().should('include', `${APP_PREFIX}/articulos`);
    });

    // PASO 2: CATÁLOGO - AÑADIR AL CARRITO
    cy.log('**PASO 2: Añadir productos al carrito**');

    // Esperar a que carguen los artículos
    cy.get('.gallery__item', { timeout: 15000 }).should('have.length.greaterThan', 0);

    // Añadir primer artículo al carrito
    // Nota: El botón tiene opacity:0 hasta hover, usamos force:true para el test
    cy.get('.gallery__item').first().find('.btn-add-cart').click({ force: true });

    // Verificar feedback visual (toast o mensaje)
    cy.wait(500); // Pequeña espera para animación

    // Añadir segundo artículo (si existe)
    cy.get('.gallery__item').then(($items) => {
      if ($items.length > 1) {
        cy.wrap($items[1]).find('.btn-add-cart').click({ force: true });
        cy.wait(500);
      }
    });

    // PASO 3: CARRITO - REVISAR ITEMS
    cy.log('**PASO 3: Revisar carrito**');

    // Navegar al carrito
    cy.visitApp('/carrito');
    cy.url().should('include', `${APP_PREFIX}/carrito`);

    // Verificar que hay items en el carrito (clase: .carrito-item en carrito-view)
    cy.get('.carrito-item', { timeout: 10000 }).should('have.length.greaterThan', 0);

    // Verificar que se muestran los totales
    cy.contains('Subtotal').should('be.visible');
    cy.contains('IVA').should('be.visible');
    cy.contains('Total').should('be.visible');

    // Proceder al checkout
    cy.get('.btn-checkout').should('be.visible').should('not.be.disabled').click();

    // PASO 4: CHECKOUT - RESUMEN
    cy.log('**PASO 4.1: Checkout - Resumen del pedido**');

    cy.url().should('include', `${APP_PREFIX}/checkout`);

    // Verificar que estamos en el paso de resumen
    cy.contains('Resumen', { timeout: 10000 }).should('be.visible');
    cy.get('.checkout-summary', { timeout: 10000 }).should('be.visible');

    // Verificar items en el resumen
    cy.get('.cart-item').should('have.length.greaterThan', 0);

    // Continuar al siguiente paso
    cy.get('.btn-continue').should('be.visible').should('not.be.disabled').click();

    // PASO 5: CHECKOUT - DATOS DE ENVÍO O PAGO
    // El checkout puede mostrar DATOS o saltar directamente a PAGO si el cliente tiene datos completos
    cy.log('**PASO 4.2: Checkout - Datos de envío / Pago**');

    // Esperar a que cargue el siguiente paso (puede ser DATOS o PAGO)
    cy.get('.checkout-title', { timeout: 10000 }).should('be.visible');

    // Verificar en qué paso estamos y actuar en consecuencia
    cy.get('.checkout-title').invoke('text').then((title) => {
      const titleText = title.trim();
      cy.log(`Paso actual: ${titleText}`);

      if (titleText.includes('Datos') || titleText.includes('Envío')) {
        // Estamos en el paso de DATOS
        cy.log('**En paso DATOS - completando formulario si es necesario**');
        
        // Si el formulario está visible, completar datos
        cy.get('body').then(($body) => {
          if ($body.find('#nombre').length > 0) {
            cy.fixture('cliente').then((cliente) => {
              cy.get('#nombre').clear().type(cliente.nombre);
              cy.get('#telefono').clear().type(cliente.telefono);
              cy.get('input[formControlName="calle"]').clear().type(cliente.direccion.calle);
              cy.get('input[formControlName="cp"]').clear().type(cliente.direccion.cp);
              cy.get('input[formControlName="ciudad"]').clear().type(cliente.direccion.ciudad);
              cy.get('input[formControlName="provincia"]').clear().type(cliente.direccion.provincia);

              // Enviar formulario
              cy.get('.checkout-form-container form').submit();
            });
          } else {
            // Datos ya completos, continuar al siguiente paso
            cy.get('.btn-continue, button[type="submit"]').first().click();
          }
        });

        // Esperar transición al paso de PAGO
        cy.contains('Método de Pago', { timeout: 10000 }).should('be.visible');
      }
      // Si ya está en PAGO, continuar directamente
    });

    // PASO 6: CHECKOUT - MÉTODO DE PAGO
    cy.log('**PASO 4.3: Checkout - Método de pago**');

    // Verificar que estamos en el paso de pago
    cy.get('.checkout-title').should('contain', 'Pago');

    // Verificar que se muestran las opciones de pago
    cy.get('.payment-option', { timeout: 10000 }).should('have.length.greaterThan', 0);

    // Seleccionar método de pago (contraentrega por defecto o tarjeta)
    cy.get('.payment-option').contains('contraentrega').click();

    // Verificar que se muestra el total
    cy.get('.total-amount').should('be.visible');

    // Confirmar pedido
    cy.get('button[type="submit"]').contains('Confirmar').should('not.be.disabled').click();

    // PASO 7: CONFIRMACIÓN
    cy.log('**PASO 4.4: Confirmación del pedido**');

    // Verificar página de confirmación
    cy.contains('¡Pedido confirmado!', { timeout: 15000 }).should('be.visible');

    // Verificar que se muestra el número de pedido
    cy.get('.order-number').should('be.visible');

    // Verificar que se muestra el resumen
    cy.contains('Resumen del pedido').should('be.visible');
    cy.get('.total-amount').should('be.visible');

    // Verificar botones de acción
    cy.contains('Ver mis pedidos').should('be.visible');
  });

  /**
   * Test: Agregar producto al carrito sin estar autenticado
   */
  it('debería permitir agregar al carrito como invitado y pedir login en checkout', () => {
    cy.log('**Test: Carrito como invitado**');

    // Navegar directamente al catálogo sin login
    cy.visitApp('/articulos');

    // Esperar carga de artículos
    cy.get('.gallery__item', { timeout: 15000 }).should('have.length.greaterThan', 0);

    // Añadir artículo al carrito
    cy.get('.btn-add-cart').first().click();
    
    // Esperar a que se guarde en localStorage
    cy.wait(1000);

    // Ir al carrito (clase: .carrito-item en carrito-view)
    cy.visitApp('/carrito');
    cy.get('.carrito-item', { timeout: 10000 }).should('have.length.greaterThan', 0);

    // Configurar handler para el confirm() que aparece al hacer checkout como invitado
    cy.on('window:confirm', () => true); // Aceptar ir al login

    // Intentar checkout (como invitado, mostrará confirm y redirigirá a /auth)
    cy.get('.btn-checkout').click();

    // Debería redirigir a la página de auth/login
    cy.url({ timeout: 10000 }).should('include', '/auth');
  });

  /**
   * Test: Validación de carrito vacío
   */
  it('debería redirigir a artículos si el carrito está vacío al ir a checkout', () => {
    cy.log('**Test: Carrito vacío**');

    // Login primero
    cy.fixture('cliente').then((cliente) => {
      cy.loginAsCliente(cliente.email, cliente.password);
    });

    // Vaciar el carrito para asegurar que está vacío (puede tener items de tests anteriores)
    cy.vaciarCarrito();
    cy.wait(300); // Esperar a que se procese

    // Intentar ir directamente a checkout con carrito vacío
    cy.visitApp('/checkout');

    // Debería mostrar alerta y redirigir
    cy.on('window:alert', (text) => {
      expect(text).to.contain('carrito');
    });

    // Verificar redirección a artículos o carrito
    cy.url({ timeout: 10000 }).should('satisfy', (url: string) => {
      return url.includes(`${APP_PREFIX}/articulos`) || url.includes(`${APP_PREFIX}/carrito`);
    });
  });

  /**
   * Test: Modificar cantidad en el carrito
   */
  it('debería permitir modificar cantidades en el carrito', () => {
    cy.log('**Test: Modificar cantidades**');

    // Login
    cy.fixture('cliente').then((cliente) => {
      cy.loginAsCliente(cliente.email, cliente.password);
    });

    // Vaciar carrito primero para tener estado limpio
    cy.vaciarCarrito();
    cy.wait(500);

    // Añadir producto - interceptar request para confirmar que se envía
    cy.intercept('POST', '/cosechaencope/carrito/agregar').as('agregarCarrito');
    
    cy.visitApp('/articulos');
    cy.get('.gallery__item', { timeout: 15000 }).should('have.length.greaterThan', 0);
    cy.get('.gallery__item').first().find('.btn-add-cart').click({ force: true });
    
    // Esperar a que el POST se complete exitosamente
    cy.wait('@agregarCarrito').its('response.statusCode').should('eq', 200);

    // Ir al carrito (clase: .carrito-item en carrito-view)
    cy.visitApp('/carrito');
    cy.get('.carrito-item', { timeout: 10000 }).should('have.length.greaterThan', 0);

    // Guardar cantidad inicial del PRIMER item (clase: .cantidad en carrito-view)
    cy.get('.carrito-item').first().find('.cantidad')
      .invoke('text')
      .then((text) => {
        const cantidadInicial = parseInt(text.trim());

        // Incrementar cantidad - botón + es el SEGUNDO dentro del item
        cy.get('.carrito-item').first().find('.btn-cantidad').eq(1).click();
        cy.wait(500);

        // Verificar que la cantidad aumentó
        cy.get('.carrito-item').first().find('.cantidad')
          .invoke('text')
          .should((newText) => {
            const nuevaCantidad = parseInt(newText.trim());
            expect(nuevaCantidad).to.equal(cantidadInicial + 1);
          });

        // Decrementar cantidad - botón - es el PRIMERO dentro del item
        cy.get('.carrito-item').first().find('.btn-cantidad').eq(0).click();
        cy.wait(500);

        // Verificar que volvió a la cantidad original
        cy.get('.carrito-item').first().find('.cantidad')
          .invoke('text')
          .should((finalText) => {
            const cantidadFinal = parseInt(finalText.trim());
            expect(cantidadFinal).to.equal(cantidadInicial);
          });
      });
  });
});

/**
 * Test E2E - Navegación Landing SSR ↔ SPA
 *
 * Verifica la transición entre páginas SSR (Thymeleaf) y la SPA Angular.
 * Este test requiere el backend Spring Boot corriendo.
 */
describe('Navegación Híbrida SSR ↔ SPA', () => {
  /**
   * Test: Transición desde landing (SSR) a la aplicación (SPA)
   * Requiere: Spring Boot corriendo en localhost:8081
   */
  it.skip('debería navegar desde landing SSR a la SPA Angular', () => {
    // Visitar landing page (SSR - Thymeleaf)
    cy.visitLanding('/');

    // Verificar que estamos en la landing SSR
    cy.contains('Cosecha en Cope').should('be.visible');

    // Buscar enlace a la tienda/catálogo
    cy.get('a[href*="/app/articulos"], a[href*="articulos"]')
      .first()
      .click();

    // Verificar transición a la SPA
    cy.url().should('include', '/app/');

    // Verificar que Angular ha cargado
    cy.get('app-root', { timeout: 10000 }).should('exist');
  });

  /**
   * Test: Verificar que las páginas SSR cargan correctamente
   * Requiere: Spring Boot corriendo
   */
  it.skip('debería cargar las landing pages SSR', () => {
    // Landing principal
    cy.visitLanding('/');
    cy.get('body').should('be.visible');

    // Página "Nosotros"
    cy.visitLanding('/nosotros');
    cy.get('body').should('be.visible');

    // Página "Contacto"
    cy.visitLanding('/contacto');
    cy.get('body').should('be.visible');
  });
});
