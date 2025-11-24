# Refactor UI/UX - Páginas About y Contact

**Objetivo:** Simplificar el diseño de `about.html` y `contact.html` eliminando elementos decorativos innecesarios, aplicando minimalismo funcional y manteniendo coherencia con la paleta de colores del proyecto.

---

## 1. Contexto

**Archivos a refactorizar:**
- `backend/src/main/resources/templates/about.html`
- `backend/src/main/resources/templates/contact.html`
- CSS inline o archivos asociados

**Tecnología:** Thymeleaf (SSR)

---

## 2. Paleta de Colores (mantener consistencia)

```css
:root {
  --color-primary: #574b51;      /* Marrón terroso */
  --color-secondary: #2e8a99;    /* Verde azulado */
  --color-tertiary: #222222;     /* Gris oscuro */
  
  --text-primary: #222222;
  --text-secondary: rgba(34, 34, 34, 0.7);
  --text-on-primary: #ffffff;
  
  --bg-primary: #ffffff;
  --bg-secondary: #f5f5f5;
  
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  --spacing-2xl: 48px;
  
  --border-radius-md: 8px;
  --border-radius-lg: 12px;
  --border-color: rgba(34, 34, 34, 0.12);
  
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.07);
}
```

---

## 3. Principios de Diseño

### ✅ Hacer
- **Colores sólidos** (sin gradientes)
- **Espaciado consistente** (múltiplos de 8px)
- **Tipografía clara** con jerarquía mediante tamaños y pesos
- **Iconos funcionales únicamente** (ubicación, teléfono, email)
- **Sombras sutiles** (máximo 2 niveles)
- **Diseño responsive** simple

### ❌ Evitar
- Gradientes complejos o decorativos
- Iconos sin función clara
- Múltiples box-shadows apiladas
- Animaciones innecesarias
- Overlays con transparencias complejas
- Text-shadows decorativos

---

## 4. About.html - Estructura Simplificada

### Contenido mínimo necesario:
1. **Hero section** con título y subtítulo
2. **Sección de misión/valores** (texto + opcional 1 imagen)
3. **Equipo** (opcional, si aplica)
4. **Call-to-action** simple

### Ejemplo de estructura HTML:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Nosotros - Cosecha en Cope</title>
    <style>
        /* CSS crítico inline */
        body {
            font-family: 'Inter', system-ui, sans-serif;
            color: var(--text-primary);
            background-color: var(--bg-primary);
            margin: 0;
            line-height: 1.6;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 var(--spacing-md);
        }
        
        /* Hero simple */
        .hero-about {
            background-color: var(--color-primary);
            color: var(--text-on-primary);
            padding: var(--spacing-2xl) 0;
            text-align: center;
        }
        
        .hero-about h1 {
            font-size: 2.5rem;
            font-weight: 700;
            margin: 0 0 var(--spacing-md);
        }
        
        .hero-about p {
            font-size: 1.125rem;
            opacity: 0.9;
            max-width: 600px;
            margin: 0 auto;
        }
        
        /* Sección de contenido */
        .content-section {
            padding: var(--spacing-2xl) 0;
        }
        
        .content-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: var(--spacing-xl);
        }
        
        @media (min-width: 768px) {
            .content-grid {
                grid-template-columns: 1fr 1fr;
            }
        }
        
        .content-card {
            background: var(--bg-primary);
            border: 1px solid var(--border-color);
            border-radius: var(--border-radius-lg);
            padding: var(--spacing-lg);
        }
        
        .content-card h2 {
            color: var(--color-primary);
            font-size: 1.5rem;
            margin: 0 0 var(--spacing-md);
        }
        
        /* CTA simple */
        .cta-section {
            background-color: var(--bg-secondary);
            padding: var(--spacing-2xl) 0;
            text-align: center;
        }
        
        .btn-primary {
            display: inline-block;
            background-color: var(--color-primary);
            color: var(--text-on-primary);
            padding: var(--spacing-md) var(--spacing-xl);
            border-radius: var(--border-radius-md);
            text-decoration: none;
            font-weight: 600;
            transition: opacity 0.25s;
        }
        
        .btn-primary:hover {
            opacity: 0.9;
        }
    </style>
</head>
<body>
    <!-- Header incluido via Thymeleaf -->
    <div th:replace="~{fragments/header :: header}"></div>
    
    <!-- Hero -->
    <section class="hero-about">
        <div class="container">
            <h1>Sobre Nosotros</h1>
            <p>Productos locales de calidad, directamente del campo a tu mesa</p>
        </div>
    </section>
    
    <!-- Contenido -->
    <section class="content-section">
        <div class="container">
            <div class="content-grid">
                <div class="content-card">
                    <h2>Nuestra Misión</h2>
                    <p>Conectar a productores locales con consumidores conscientes, promoviendo la agricultura sostenible y el consumo responsable.</p>
                </div>
                
                <div class="content-card">
                    <h2>Nuestros Valores</h2>
                    <ul>
                        <li>Transparencia en origen</li>
                        <li>Calidad garantizada</li>
                        <li>Sostenibilidad ambiental</li>
                        <li>Apoyo a la economía local</li>
                    </ul>
                </div>
            </div>
        </div>
    </section>
    
    <!-- CTA -->
    <section class="cta-section">
        <div class="container">
            <h2>¿Listo para probar nuestros productos?</h2>
            <a href="/app/productos" class="btn-primary">Ver Catálogo</a>
        </div>
    </section>
    
    <!-- Footer incluido via Thymeleaf -->
    <div th:replace="~{fragments/footer :: footer}"></div>
</body>
</html>
```

### Cambios clave en About:
- ✅ **Eliminar:** Hero con gradientes, overlays, múltiples imágenes de fondo
- ✅ **Simplificar:** Cards con bordes simples, sin sombras complejas
- ✅ **Reducir:** Animaciones innecesarias, iconos decorativos
- ✅ **Mantener:** Estructura de contenido, jerarquía clara

---

## 5. Contact.html - Estructura Simplificada

### Contenido mínimo necesario:
1. **Hero section** con título
2. **Formulario de contacto** funcional
3. **Información de contacto** (dirección, teléfono, email)
4. **Mapa** (opcional, si aplica)

### Ejemplo de estructura HTML:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Contacto - Cosecha en Cope</title>
    <style>
        /* CSS crítico inline (reutilizar variables de About) */
        
        /* Formulario */
        .contact-section {
            padding: var(--spacing-2xl) 0;
        }
        
        .contact-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: var(--spacing-xl);
        }
        
        @media (min-width: 768px) {
            .contact-grid {
                grid-template-columns: 1fr 1fr;
            }
        }
        
        .form-group {
            margin-bottom: var(--spacing-lg);
        }
        
        .form-label {
            display: block;
            margin-bottom: var(--spacing-sm);
            font-weight: 600;
            color: var(--text-primary);
        }
        
        .form-input {
            width: 100%;
            padding: var(--spacing-md);
            border: 1px solid var(--border-color);
            border-radius: var(--border-radius-md);
            font-size: 1rem;
            font-family: inherit;
        }
        
        .form-input:focus {
            outline: none;
            border-color: var(--color-primary);
            box-shadow: 0 0 0 3px rgba(87, 75, 81, 0.1);
        }
        
        textarea.form-input {
            min-height: 120px;
            resize: vertical;
        }
        
        .btn-submit {
            background-color: var(--color-primary);
            color: var(--text-on-primary);
            padding: var(--spacing-md) var(--spacing-xl);
            border: none;
            border-radius: var(--border-radius-md);
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: opacity 0.25s;
        }
        
        .btn-submit:hover {
            opacity: 0.9;
        }
        
        /* Info de contacto */
        .contact-info {
            background-color: var(--bg-secondary);
            border-radius: var(--border-radius-lg);
            padding: var(--spacing-lg);
        }
        
        .contact-item {
            display: flex;
            align-items: flex-start;
            gap: var(--spacing-md);
            margin-bottom: var(--spacing-lg);
        }
        
        .contact-item:last-child {
            margin-bottom: 0;
        }
        
        .contact-icon {
            color: var(--color-primary);
            font-size: 1.25rem;
            flex-shrink: 0;
        }
        
        .contact-text h3 {
            margin: 0 0 var(--spacing-sm);
            font-size: 1rem;
            font-weight: 600;
        }
        
        .contact-text p {
            margin: 0;
            color: var(--text-secondary);
        }
    </style>
</head>
<body>
    <div th:replace="~{fragments/header :: header}"></div>
    
    <!-- Hero -->
    <section class="hero-about">
        <div class="container">
            <h1>Contacto</h1>
            <p>Estamos aquí para ayudarte</p>
        </div>
    </section>
    
    <!-- Formulario y Info -->
    <section class="contact-section">
        <div class="container">
            <div class="contact-grid">
                <!-- Formulario -->
                <div>
                    <h2>Envíanos un mensaje</h2>
                    <form th:action="@{/contacto}" method="post">
                        <div class="form-group">
                            <label class="form-label" for="nombre">Nombre</label>
                            <input type="text" id="nombre" name="nombre" class="form-input" required>
                        </div>
                        
                        <div class="form-group">
                            <label class="form-label" for="email">Email</label>
                            <input type="email" id="email" name="email" class="form-input" required>
                        </div>
                        
                        <div class="form-group">
                            <label class="form-label" for="asunto">Asunto</label>
                            <input type="text" id="asunto" name="asunto" class="form-input" required>
                        </div>
                        
                        <div class="form-group">
                            <label class="form-label" for="mensaje">Mensaje</label>
                            <textarea id="mensaje" name="mensaje" class="form-input" required></textarea>
                        </div>
                        
                        <button type="submit" class="btn-submit">Enviar Mensaje</button>
                    </form>
                </div>
                
                <!-- Información de contacto -->
                <div class="contact-info">
                    <h2>Información de Contacto</h2>
                    
                    <div class="contact-item">
                        <span class="contact-icon">📍</span>
                        <div class="contact-text">
                            <h3>Dirección</h3>
                            <p>La Rinconada, Andalucía<br>España</p>
                        </div>
                    </div>
                    
                    <div class="contact-item">
                        <span class="contact-icon">📞</span>
                        <div class="contact-text">
                            <h3>Teléfono</h3>
                            <p>+34 XXX XXX XXX</p>
                        </div>
                    </div>
                    
                    <div class="contact-item">
                        <span class="contact-icon">✉️</span>
                        <div class="contact-text">
                            <h3>Email</h3>
                            <p>info@cosechaencope.es</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
    
    <div th:replace="~{fragments/footer :: footer}"></div>
</body>
</html>
```

### Cambios clave en Contact:
- ✅ **Eliminar:** Gradientes en formulario, sombras múltiples, iconos decorativos
- ✅ **Simplificar:** Campos de formulario con bordes simples, focus state claro
- ✅ **Iconos funcionales:** Solo ubicación, teléfono, email (usar emojis o SVG simple)
- ✅ **Validación visual:** Border color change en :focus, sin efectos complejos

---

## 6. Checklist de Refactorización

### About.html
- [ ] Eliminar gradientes en hero section
- [ ] Simplificar cards (border simple, sin sombras complejas)
- [ ] Reducir iconos a solo funcionales
- [ ] Aplicar paleta de colores consistente
- [ ] Espaciado basado en múltiplos de 8px
- [ ] Validar responsive (mobile, tablet, desktop)
- [ ] Test de contraste de texto (mínimo 4.5:1)

### Contact.html
- [ ] Eliminar gradientes en formulario
- [ ] Simplificar inputs (border simple, focus state claro)
- [ ] Iconos funcionales únicamente (ubicación, teléfono, email)
- [ ] Botón submit sin efectos innecesarios
- [ ] Card de info de contacto con diseño limpio
- [ ] Validar formulario accesible (labels, required, aria-*)
- [ ] Test de navegación por teclado

---

## 7. Ejemplos de Transformación

### ANTES: Hero con gradiente complejo
```css
.hero-about {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
    box-shadow: inset 0 0 100px rgba(0,0,0,0.3);
    position: relative;
}

.hero-about::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url('pattern.png');
    opacity: 0.1;
}
```

### DESPUÉS: Hero con color sólido
```css
.hero-about {
    background-color: var(--color-primary);
    padding: var(--spacing-2xl) 0;
}
```

---

### ANTES: Input con efectos múltiples
```css
.form-input {
    background: linear-gradient(to bottom, #f9f9f9, #ffffff);
    border: 2px solid transparent;
    border-image: linear-gradient(45deg, #667eea, #764ba2) 1;
    box-shadow: 0 2px 5px rgba(0,0,0,0.1), inset 0 1px 3px rgba(0,0,0,0.05);
}

.form-input:focus {
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3),
                0 5px 15px rgba(118, 75, 162, 0.2);
}
```

### DESPUÉS: Input limpio
```css
.form-input {
    background-color: var(--bg-primary);
    border: 1px solid var(--border-color);
    padding: var(--spacing-md);
}

.form-input:focus {
    outline: none;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(87, 75, 81, 0.1);
}
```

---

## 8. Notas Finales

### Prioridades:
1. **Funcionalidad** > Estética
2. **Legibilidad** > Efectos visuales
3. **Consistencia** > Originalidad
4. **Accesibilidad** > Diseño complejo

### Validaciones necesarias:
- ✅ Contraste de colores WCAG AA (4.5:1 mínimo)
- ✅ Navegación por teclado funcional
- ✅ Formularios con labels adecuados
- ✅ Responsive en móvil (375px), tablet (768px), desktop (1024px+)

### Comandos útiles:
```bash
# Validar HTML
npm run validate:html

# Test de accesibilidad
npm run test:a11y

# Preview en servidor local
npm run serve
```

---

**Versión:** 1.0  
**Enfoque:** Minimalismo funcional  
**Colores:** #574b51, #2e8a99, #222222  
**Principio:** Sin gradientes, máxima simplicidad