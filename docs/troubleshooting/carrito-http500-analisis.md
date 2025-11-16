# Análisis y Solución de Errores HTTP 500 en Endpoint de Carrito

## 📋 Información del Documento

**Proyecto:** Cosecha en Cope  
**Módulo:** Sistema de Carrito de Compras  
**Fecha:** 16 de noviembre de 2025  
**Versión:** 1.0  
**Autor:** Equipo de Desarrollo

---

## 1. Introducción

### 1.1 Naturaleza del Error HTTP 500

Un error **HTTP 500 (Internal Server Error)** indica que el servidor ha encontrado una situación inesperada que le impide completar la solicitud. En el contexto de una aplicación e-commerce basada en Spring Boot, este tipo de errores en el endpoint de carrito suelen ser críticos, ya que:

- **Interrumpen el flujo de compra** del usuario, generando frustración y posible pérdida de ventas
- **Ocultan el problema real** al cliente, mostrando solo un mensaje genérico
- **Pueden indicar múltiples fallos** subyacentes: desde validaciones fallidas hasta problemas de base de datos

### 1.2 Contexto del Sistema

En una arquitectura típica de carrito de compras con Spring Boot + JPA + PostgreSQL, el proceso de agregar ítems involucra:

1. **Validación de la solicitud** (autenticación JWT, validación de datos)
2. **Consultas a base de datos** (verificación de artículo, stock, carrito existente)
3. **Lógica de negocio** (cálculo de totales, manejo de cantidades)
4. **Persistencia transaccional** (guardar/actualizar registros)
5. **Construcción de respuesta** (serialización de DTOs)

Cualquier fallo en estos puntos puede resultar en un error 500 si no está correctamente manejado.

---

## 2. Análisis de Causas Frecuentes

### 2.1 Manejo Deficiente de Excepciones

**Descripción:**  
Las excepciones no capturadas o mal gestionadas se propagan hasta el controlador, resultando en respuestas HTTP 500 sin información útil.

**Ejemplo sintético:**

```java
// ❌ PROBLEMA: Sin manejo de excepciones
@PostMapping("/carrito/agregar")
public ResponseEntity<?> agregarItem(@RequestBody ItemRequest request) {
    // Si articuloRepository.findById() lanza NoSuchElementException
    // el usuario recibe HTTP 500 sin detalles
    Articulo articulo = articuloRepository.findById(request.getArticuloId()).get();
    carritoService.agregarItem(articulo, request.getCantidad());
    return ResponseEntity.ok().build();
}

// ✅ SOLUCIÓN: Manejo explícito con Optional
@PostMapping("/carrito/agregar")
public ResponseEntity<?> agregarItem(@RequestBody ItemRequest request) {
    Articulo articulo = articuloRepository.findById(request.getArticuloId())
        .orElseThrow(() -> new ArticuloNotFoundException(
            "Artículo no encontrado: " + request.getArticuloId()));
    
    carritoService.agregarItem(articulo, request.getCantidad());
    return ResponseEntity.ok().build();
}
```

**Consecuencias:**
- Logs crípticos sin traza útil
- Imposibilidad de debugging efectivo
- Experiencia de usuario deficiente

---

### 2.2 Problemas de Persistencia y Transacciones

**Descripción:**  
Fallos en operaciones JPA/Hibernate por restricciones de base de datos, transacciones mal configuradas o entidades en estado inconsistente.

**Ejemplo sintético:**

```java
// ❌ PROBLEMA: Violación de constraint de clave foránea
@Transactional
public void agregarItem(Long usuarioId, Long articuloId, Integer cantidad) {
    CarritoItem item = new CarritoItem();
    item.setArticuloId(999999L); // ID inexistente
    item.setCantidad(cantidad);
    carritoItemRepository.save(item); // SQL error: FK constraint violation
}

// ✅ SOLUCIÓN: Validación previa y manejo transaccional robusto
@Transactional(rollbackFor = Exception.class)
public CarritoItemDTO agregarItem(Long usuarioId, Long articuloId, Integer cantidad) {
    // Validar existencia del artículo
    Articulo articulo = articuloRepository.findById(articuloId)
        .orElseThrow(() -> new ArticuloNotFoundException(articuloId));
    
    // Validar stock disponible
    if (articulo.getStock() < cantidad) {
        throw new StockInsuficienteException(
            String.format("Stock insuficiente. Disponible: %d, Solicitado: %d", 
                articulo.getStock(), cantidad));
    }
    
    // Operación segura
    CarritoItem item = new CarritoItem();
    item.setArticulo(articulo);
    item.setCantidad(cantidad);
    return carritoItemMapper.toDTO(carritoItemRepository.save(item));
}
```

**Indicadores comunes:**
- `ConstraintViolationException`
- `DataIntegrityViolationException`
- `TransactionSystemException`

---

### 2.3 Datos Nulos o Valores Incorrectos

**Descripción:**  
Ausencia de validaciones en DTOs de entrada permite que datos inválidos lleguen a la lógica de negocio.

**Ejemplo sintético:**

```java
// ❌ PROBLEMA: Sin validaciones
public class AgregarItemRequest {
    private Long articuloId;      // Puede ser null
    private Integer cantidad;      // Puede ser null, 0 o negativo
    // getters/setters
}

@PostMapping("/carrito/agregar")
public ResponseEntity<?> agregarItem(@RequestBody AgregarItemRequest request) {
    // Si cantidad es null -> NullPointerException
    // Si cantidad es 0 o negativo -> Lógica de negocio inválida
    carritoService.agregarItem(request.getArticuloId(), request.getCantidad());
}

// ✅ SOLUCIÓN: Validaciones con Bean Validation
public class AgregarItemRequest {
    @NotNull(message = "El ID del artículo es obligatorio")
    @Positive(message = "El ID del artículo debe ser positivo")
    private Long articuloId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    @Max(value = 999, message = "La cantidad máxima es 999")
    private Integer cantidad;
}

@PostMapping("/carrito/agregar")
public ResponseEntity<?> agregarItem(@Valid @RequestBody AgregarItemRequest request) {
    // Spring valida automáticamente antes de entrar al método
    return ResponseEntity.ok(carritoService.agregarItem(
        request.getArticuloId(), 
        request.getCantidad()
    ));
}
```

**Señales de advertencia:**
- `NullPointerException` en logs
- Errores de aritmética (división por cero)
- Valores fuera de rango esperado

---

### 2.4 Configuración Incorrecta de Beans y Dependencias

**Descripción:**  
Problemas de inyección de dependencias, beans no encontrados o ciclos de dependencias circulares.

**Ejemplo sintético:**

```java
// ❌ PROBLEMA: Dependencia circular
@Service
public class CarritoService {
    @Autowired
    private PedidoService pedidoService; // PedidoService también depende de CarritoService
    
    public void agregarItem(...) {
        pedidoService.validarDisponibilidad(...);
    }
}

// ✅ SOLUCIÓN: Refactorizar usando eventos o extraer lógica común
@Service
public class CarritoService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private DisponibilidadService disponibilidadService; // Servicio independiente
    
    public void agregarItem(Long articuloId, Integer cantidad) {
        disponibilidadService.verificarStock(articuloId, cantidad);
        // ... lógica de agregar
        eventPublisher.publishEvent(new ItemAgregadoEvent(this, carritoItem));
    }
}
```

**Errores típicos:**
- `NoSuchBeanDefinitionException`
- `BeanCurrentlyInCreationException`
- `UnsatisfiedDependencyException`

---

### 2.5 Problemas de Serialización JSON

**Descripción:**  
Errores al convertir entidades JPA a JSON por referencias circulares, lazy loading o campos problemáticos.

**Ejemplo sintético:**

```java
// ❌ PROBLEMA: Referencia circular Carrito ↔ CarritoItem
@Entity
public class Carrito {
    @OneToMany(mappedBy = "carrito")
    private List<CarritoItem> items; // Se serializa
}

@Entity
public class CarritoItem {
    @ManyToOne
    private Carrito carrito; // También se serializa -> loop infinito
}

// ✅ SOLUCIÓN 1: Usar DTOs en lugar de entidades
public class CarritoResponseDTO {
    private Long id;
    private BigDecimal total;
    private List<CarritoItemDTO> items; // Solo datos necesarios
}

// ✅ SOLUCIÓN 2: Anotaciones Jackson (menos recomendado)
@Entity
public class Carrito {
    @OneToMany(mappedBy = "carrito")
    @JsonManagedReference
    private List<CarritoItem> items;
}

@Entity
public class CarritoItem {
    @ManyToOne
    @JsonBackReference
    private Carrito carrito;
}
```

**Síntomas:**
- `StackOverflowError` en serialización
- `HttpMessageNotWritableException`
- Respuestas JSON incompletas o corruptas

---

### 2.6 Errores de Concurrencia y Estado Inconsistente

**Descripción:**  
Múltiples peticiones simultáneas modificando el mismo carrito sin control de concurrencia adecuado.

**Ejemplo sintético:**

```java
// ❌ PROBLEMA: Race condition al actualizar cantidad
@Transactional
public void agregarItem(Long carritoId, Long articuloId, Integer cantidad) {
    Carrito carrito = carritoRepository.findById(carritoId).orElseThrow();
    CarritoItem existente = carrito.getItems().stream()
        .filter(i -> i.getArticuloId().equals(articuloId))
        .findFirst()
        .orElse(null);
    
    if (existente != null) {
        // Si 2 peticiones llegan simultáneamente, ambas leen el mismo valor
        existente.setCantidad(existente.getCantidad() + cantidad);
    }
}

// ✅ SOLUCIÓN: Versionado optimista con @Version
@Entity
public class CarritoItem {
    @Id
    private Long id;
    
    @Version
    private Long version; // Hibernate controla concurrencia
    
    private Integer cantidad;
}

// El método de servicio lanza OptimisticLockException si hay conflicto
@Transactional
public CarritoItemDTO agregarItem(...) {
    try {
        // ... lógica de actualización
        return mapper.toDTO(carritoItemRepository.save(item));
    } catch (OptimisticLockException e) {
        throw new ConcurrenciaException("El carrito fue modificado por otro proceso");
    }
}
```

---

## 3. Tabla Resumen de Problemas y Soluciones

| **Problema** | **Síntoma** | **Propuesta de Solución** | **Impacto Esperado** |
|--------------|-------------|---------------------------|----------------------|
| **Excepciones no manejadas** | HTTP 500 genérico sin detalles, `NoSuchElementException` en logs | Implementar `@RestControllerAdvice` con manejadores específicos para cada tipo de excepción | Respuestas HTTP semánticas (404, 400, 409), mensajes claros al frontend |
| **Violaciones de integridad BD** | `ConstraintViolationException`, FK violations | Validar existencia de entidades relacionadas antes de persistir, usar DTOs con validaciones | Prevención de errores SQL, transacciones más robustas |
| **Validaciones ausentes en DTOs** | `NullPointerException`, valores negativos/inválidos | Agregar anotaciones `@Valid`, `@NotNull`, `@Min`, `@Max` en request DTOs | Rechazo temprano de datos inválidos (fail-fast), mejor experiencia de usuario |
| **Dependencias circulares** | `BeanCurrentlyInCreationException` al iniciar app | Refactorizar usando eventos Spring, extraer lógica común a servicios independientes | Aplicación arranca correctamente, arquitectura más desacoplada |
| **Referencias circulares JSON** | `StackOverflowError`, respuestas JSON vacías | Usar DTOs de respuesta en lugar de entidades JPA, aplicar `@JsonIgnore` estratégicamente | Serialización correcta, payloads optimizados |
| **Race conditions en concurrencia** | Cantidades incorrectas, datos duplicados | Implementar versionado optimista con `@Version`, manejo de `OptimisticLockException` | Consistencia de datos en alta concurrencia, integridad del carrito |

---

## 4. Buenas Prácticas para Mantenimiento y Escalabilidad

### 4.1 Documentación de Análisis de Errores

Cada error crítico debe documentarse siguiendo esta estructura:

```markdown
## Error: [Descripción breve]

**Fecha:** YYYY-MM-DD  
**Severidad:** Crítica | Alta | Media | Baja  
**Módulo afectado:** [Nombre del módulo]  
**Endpoint:** `POST /api/carrito/agregar`

### Reproducción
1. Pasos para reproducir el error
2. Datos de entrada que causan el fallo
3. Estado esperado vs. estado actual

### Diagnóstico
- **Log relevante:**
  ```
  [Fragmento del stacktrace más significativo]
  ```
- **Causa raíz identificada:** [Explicación]
- **Componentes afectados:** [Lista de clases/servicios]

### Solución Implementada
- **Cambios realizados:** [Descripción de modificaciones]
- **Archivos modificados:**
  - `CarritoService.java` (líneas 45-67)
  - `GlobalExceptionHandler.java` (nuevo handler)
- **Pruebas añadidas:** [Tests unitarios/integración]

### Prevención
- [Medidas para evitar recurrencia]
- [Refactorizaciones adicionales recomendadas]
```

### 4.2 Estructura de Proyecto Limpia

**Organización recomendada para manejo de errores:**

```
src/main/java/com/rlp/cosechaencope/
├── exception/
│   ├── custom/
│   │   ├── ArticuloNotFoundException.java
│   │   ├── StockInsuficienteException.java
│   │   ├── CarritoVacioException.java
│   │   └── ConcurrenciaException.java
│   ├── handler/
│   │   └── GlobalExceptionHandler.java
│   └── dto/
│       └── ErrorResponse.java
├── controller/
│   └── CarritoController.java
├── service/
│   ├── CarritoService.java
│   └── impl/
│       └── CarritoServiceImpl.java
└── validation/
    ├── CarritoValidator.java
    └── StockValidator.java
```

### 4.3 Logging Efectivo

**Niveles de log según situación:**

```java
@Slf4j
@Service
public class CarritoService {
    
    public CarritoDTO agregarItem(Long usuarioId, Long articuloId, Integer cantidad) {
        // DEBUG: Operaciones normales detalladas
        log.debug("Agregando item al carrito. Usuario: {}, Artículo: {}, Cantidad: {}", 
            usuarioId, articuloId, cantidad);
        
        try {
            // INFO: Hitos importantes
            log.info("Item agregado exitosamente al carrito del usuario {}", usuarioId);
            return carritoDTO;
            
        } catch (ArticuloNotFoundException e) {
            // WARN: Errores esperados, recuperables
            log.warn("Intento de agregar artículo inexistente. ArticuloId: {}", articuloId);
            throw e;
            
        } catch (StockInsuficienteException e) {
            // WARN: Problemas de negocio
            log.warn("Stock insuficiente para artículo {}. Solicitado: {}, Disponible: {}", 
                articuloId, cantidad, e.getStockDisponible());
            throw e;
            
        } catch (Exception e) {
            // ERROR: Errores inesperados que requieren investigación
            log.error("Error inesperado al agregar item al carrito. Usuario: {}, Artículo: {}", 
                usuarioId, articuloId, e);
            throw new InternalServerException("Error al procesar el carrito", e);
        }
    }
}
```

### 4.4 Testing Exhaustivo

**Casos de prueba esenciales:**

```java
@SpringBootTest
class CarritoServiceIntegrationTest {
    
    @Test
    void agregarItem_ArticuloInexistente_LanzaExcepcion() {
        // Verificar que se lanza ArticuloNotFoundException con ID inválido
    }
    
    @Test
    void agregarItem_StockInsuficiente_LanzaExcepcion() {
        // Verificar que se rechaza cuando cantidad > stock
    }
    
    @Test
    void agregarItem_CantidadNegativa_LanzaExcepcion() {
        // Verificar validación de cantidad mínima
    }
    
    @Test
    void agregarItem_ConcurrenciaSobreItem_ManejaOptimisticLock() {
        // Simular 2 peticiones simultáneas al mismo item
    }
    
    @Test
    void agregarItem_DatosValidos_ActualizaCorrectamente() {
        // Caso feliz: verificar que se persiste y calcula total
    }
}
```

---

## 5. Metodología de Diagnóstico

### 5.1 Análisis de Logs

**Pasos sistemáticos:**

1. **Identificar el stacktrace completo**
   ```bash
   # Filtrar logs del módulo carrito
   cat logs/cosechaencope.log | grep -A 50 "CarritoController"
   
   # PowerShell
   Select-String -Path "logs\cosechaencope.log" -Pattern "CarritoController" -Context 0,50
   ```

2. **Localizar la excepción raíz**
   - Buscar `Caused by:` en el stacktrace
   - Identificar la primera excepción en la cadena
   - Anotar la clase y línea exacta del error

3. **Correlacionar con petición HTTP**
   - Verificar request body recibido
   - Comprobar headers (Authorization, Content-Type)
   - Revisar parámetros de path/query

### 5.2 Herramientas de Análisis

| **Herramienta** | **Uso** | **Comando/Configuración** |
|-----------------|---------|---------------------------|
| **Spring Boot Actuator** | Monitoreo de salud, métricas | Activar en `application.properties`: `management.endpoints.web.exposure.include=health,metrics,loggers` |
| **Postman/Insomnia** | Testing de endpoints | Colecciones con casos edge (datos nulos, IDs inválidos) |
| **PgAdmin / DBeaver** | Inspección de BD | Verificar constraints, índices, datos huérfanos |
| **IntelliJ Debugger** | Debugging en tiempo real | Breakpoints en `CarritoService`, evaluación de expresiones |
| **SonarLint** | Análisis estático | Detectar code smells, null pointer risks |

### 5.3 Pruebas Automatizadas

**Estrategia de testing:**

```java
// 1. Tests unitarios (mocks, velocidad)
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {
    @Mock private CarritoRepository carritoRepository;
    @Mock private ArticuloRepository articuloRepository;
    @InjectMocks private CarritoServiceImpl carritoService;
    
    @Test
    void agregarItem_ArticuloNoExiste_LanzaArticuloNotFoundException() {
        when(articuloRepository.findById(anyLong()))
            .thenReturn(Optional.empty());
        
        assertThrows(ArticuloNotFoundException.class, 
            () -> carritoService.agregarItem(1L, 999L, 1));
    }
}

// 2. Tests de integración (BD real, transacciones)
@SpringBootTest
@Transactional
class CarritoIntegrationTest {
    @Autowired private CarritoService carritoService;
    @Autowired private TestEntityManager entityManager;
    
    @Test
    void agregarItem_IntegridadReferencial_SeValidaCorrectamente() {
        // Test con BD H2/PostgreSQL de prueba
    }
}

// 3. Tests de API (endpoint completo)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CarritoControllerApiTest {
    @Autowired private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "CLIENTE")
    void postAgregarItem_RequestInvalido_Devuelve400() throws Exception {
        mockMvc.perform(post("/cosechaencope/carrito/agregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"articuloId\": null, \"cantidad\": -1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }
}
```

### 5.4 Control de Excepciones Customizadas

**Jerarquía de excepciones:**

```java
// Excepción base del dominio
public abstract class CosechaEnCopeException extends RuntimeException {
    private final ErrorCode errorCode;
    
    protected CosechaEnCopeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

// Excepciones específicas
public class ArticuloNotFoundException extends CosechaEnCopeException {
    public ArticuloNotFoundException(Long articuloId) {
        super(ErrorCode.ARTICULO_NOT_FOUND, 
            "No se encontró el artículo con ID: " + articuloId);
    }
}

public class StockInsuficienteException extends CosechaEnCopeException {
    private final Integer stockDisponible;
    
    public StockInsuficienteException(Integer disponible, Integer solicitado) {
        super(ErrorCode.STOCK_INSUFICIENTE,
            String.format("Stock insuficiente. Disponible: %d, Solicitado: %d", 
                disponible, solicitado));
        this.stockDisponible = disponible;
    }
    
    public Integer getStockDisponible() {
        return stockDisponible;
    }
}

// Enum de códigos de error
public enum ErrorCode {
    ARTICULO_NOT_FOUND("ERR_ARTICULO_001", HttpStatus.NOT_FOUND),
    STOCK_INSUFICIENTE("ERR_STOCK_001", HttpStatus.CONFLICT),
    CARRITO_VACIO("ERR_CARRITO_001", HttpStatus.BAD_REQUEST),
    VALIDACION_FALLO("ERR_VAL_001", HttpStatus.BAD_REQUEST);
    
    private final String code;
    private final HttpStatus status;
    
    // constructor, getters
}

// Handler global
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CosechaEnCopeException.class)
    public ResponseEntity<ErrorResponse> handleCosechaEnCopeException(
            CosechaEnCopeException ex) {
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .code(ex.getErrorCode().getCode())
            .message(ex.getMessage())
            .status(ex.getErrorCode().getStatus().value())
            .build();
        
        return ResponseEntity
            .status(ex.getErrorCode().getStatus())
            .body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .code("ERR_VAL_001")
            .message("Errores de validación")
            .status(HttpStatus.BAD_REQUEST.value())
            .errors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

---

## 6. Recomendaciones para Prevención de Errores Futuros

### 6.1 Validaciones en Múltiples Capas

**Defensa en profundidad:**

```java
// CAPA 1: Validación de entrada (DTO)
public class AgregarItemRequest {
    @NotNull(message = "ERR_VAL_001: ID de artículo requerido")
    @Positive
    private Long articuloId;
    
    @NotNull
    @Min(1)
    @Max(999)
    private Integer cantidad;
}

// CAPA 2: Validación de negocio (Service)
@Service
public class CarritoService {
    
    public void agregarItem(Long usuarioId, AgregarItemRequest request) {
        // Validar existencia
        Articulo articulo = articuloRepository.findById(request.getArticuloId())
            .orElseThrow(() -> new ArticuloNotFoundException(request.getArticuloId()));
        
        // Validar estado
        if (!articulo.isActivo()) {
            throw new ArticuloInactivoException(articulo.getId());
        }
        
        // Validar stock
        if (articulo.getStock() < request.getCantidad()) {
            throw new StockInsuficienteException(
                articulo.getStock(), 
                request.getCantidad()
            );
        }
        
        // Procesar...
    }
}

// CAPA 3: Restricciones de BD
-- Base de datos (constraints)
ALTER TABLE carrito_items
ADD CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0);

ALTER TABLE carrito_items
ADD CONSTRAINT fk_articulo FOREIGN KEY (articulo_id) 
    REFERENCES articulos(id) ON DELETE CASCADE;
```

### 6.2 Control Centralizado de Errores

**Estructura recomendada:**

```
exception/
├── custom/
│   ├── domain/
│   │   ├── ArticuloNotFoundException.java
│   │   ├── StockInsuficienteException.java
│   │   └── CarritoException.java
│   ├── security/
│   │   ├── TokenExpiredException.java
│   │   └── UnauthorizedException.java
│   └── CosechaEnCopeException.java (base)
├── handler/
│   ├── GlobalExceptionHandler.java
│   └── SecurityExceptionHandler.java
├── dto/
│   ├── ErrorResponse.java
│   └── ValidationErrorResponse.java
└── ErrorCode.java (enum)
```

### 6.3 Documentación Continua

**Plantilla para documentar cambios:**

```markdown
# Changelog - Módulo Carrito

## [v1.2.0] - 2025-11-16

### 🐛 Bugs Corregidos
- **[CRIT-045]** Solucionado error HTTP 500 al agregar artículos con stock 0
  - Causa: Falta de validación de stock antes de persistir
  - Solución: Agregada validación en `CarritoServiceImpl.agregarItem()`
  - Archivos: `CarritoServiceImpl.java`, `StockValidator.java`
  - Tests: `CarritoServiceTest.agregarItem_StockCero_LanzaExcepcion()`

### ✨ Mejoras
- Implementado versionado optimista en `CarritoItem` para prevenir race conditions
- Agregado handler específico para `OptimisticLockException`

### 📚 Documentación
- Creado `docs/troubleshooting/carrito-http500-analisis.md`
- Actualizado diagrama de arquitectura con flujo de validaciones

### ⚠️ Breaking Changes
- Ninguno

### 🔄 Migraciones
- Ninguna requerida
```

### 6.4 Pruebas de Integración Robustas

**Casos de prueba críticos:**

```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class CarritoIntegrationFlowTest {
    
    @Test
    @Order(1)
    void flujoCompleto_AgregarMultiplesItems_CalculaTotalCorrectamente() {
        // Test de flujo happy path completo
    }
    
    @Test
    @Order(2)
    void flujoError_ArticuloEliminadoEntrePeticiones_ManejaGracefully() {
        // Simular artículo eliminado mientras usuario lo agrega
    }
    
    @Test
    @Order(3)
    void flujoConflicto_DosUsuariosCompranUltimoStock_UnoFalla() {
        // Test de concurrencia real con transacciones
    }
    
    @Test
    @Order(4)
    void flujoRollback_ErrorEnPago_CarritoSeRestauraCorrectamente() {
        // Verificar integridad transaccional en cascada
    }
}
```

### 6.5 Monitoreo Proactivo

**Configuración de alertas:**

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: cosechaencope
      module: carrito

# Métricas custom
@Service
public class CarritoService {
    private final Counter itemsAgregados;
    private final Counter erroresStock;
    
    public CarritoService(MeterRegistry registry) {
        this.itemsAgregados = Counter.builder("carrito.items.agregados")
            .description("Total de items agregados al carrito")
            .register(registry);
            
        this.erroresStock = Counter.builder("carrito.errores.stock")
            .description("Errores de stock insuficiente")
            .tag("severidad", "warn")
            .register(registry);
    }
    
    public void agregarItem(...) {
        try {
            // ... lógica
            itemsAgregados.increment();
        } catch (StockInsuficienteException e) {
            erroresStock.increment();
            throw e;
        }
    }
}
```

**Dashboards sugeridos:**
- Tasa de errores HTTP 500 por endpoint
- Latencia P95/P99 de operaciones de carrito
- Ratio de excepciones por tipo (negocio vs. técnicas)
- Alertas si tasa de error > 5% en 5 minutos

---

## 7. Pautas para Redacción de Documentación Técnica

### 7.1 Principios de Claridad

1. **Usar lenguaje preciso pero accesible**
   - ✅ "El método lanza `ArticuloNotFoundException` cuando el ID no existe en BD"
   - ❌ "Puede que haya un problema si el artículo no está"

2. **Estructurar información jerárquicamente**
   - Resumen ejecutivo al inicio
   - Detalles técnicos en secciones expandibles
   - Ejemplos de código con contexto

3. **Incluir diagramas cuando añadan valor**
   ```
   [Cliente] --POST /carrito/agregar--> [Controller]
                                            |
                                            v
                                      [@Valid DTO]
                                            |
                                            v
                                        [Service]
                                            |
                    +-----------------------+----------------------+
                    |                       |                      |
                    v                       v                      v
            [Validar Artículo]      [Verificar Stock]     [Guardar Item]
                    |                       |                      |
                    v                       v                      v
            [Repository]              [Business Logic]      [JPA Save]
   ```

4. **Mantener ejemplos actualizados**
   - Revisar código de ejemplo cada trimestre
   - Marcar ejemplos obsoletos con ⚠️
   - Referenciar versiones específicas

### 7.2 Formato Estándar para Equipos

**Template de documento de error:**

```markdown
# [CÓDIGO-ERROR] Título Descriptivo

**Fecha detección:** YYYY-MM-DD  
**Prioridad:** P0 (Crítico) | P1 (Alto) | P2 (Medio) | P3 (Bajo)  
**Estado:** 🔴 Abierto | 🟡 En progreso | 🟢 Resuelto  
**Responsable:** @username

## Resumen Ejecutivo
[2-3 líneas describiendo el problema y su impacto]

## Reproducción
**Ambiente:** Desarrollo | Staging | Producción  
**Pasos:**
1. [Paso 1]
2. [Paso 2]

**Request ejemplo:**
```json
{
  "articuloId": 123,
  "cantidad": 5
}
```

**Respuesta actual:**
```json
{
  "timestamp": "2025-11-16T10:30:00",
  "status": 500,
  "error": "Internal Server Error"
}
```

## Análisis Técnico
[Detalles de la causa raíz]

## Solución
[Descripción de cambios implementados]

## Testing
- [ ] Tests unitarios agregados
- [ ] Tests de integración actualizados
- [ ] Regression testing completado

## Prevención
[Medidas para evitar recurrencia]
```

### 7.3 Comunicación con No-Técnicos

**Ejemplo de resumen para stakeholders:**

> **Problema:** Algunos usuarios experimentaban errores al agregar productos al carrito.
> 
> **Causa:** El sistema no validaba correctamente la disponibilidad de stock antes de agregar items.
> 
> **Solución:** Se implementaron validaciones adicionales y mensajes de error claros que informan al usuario cuando un producto no tiene stock suficiente.
> 
> **Resultado:** Reducción del 95% en errores HTTP 500 en el módulo carrito. Los usuarios ahora reciben mensajes informativos como "Lo sentimos, solo quedan 3 unidades disponibles" en lugar de errores genéricos.

---

## 8. Checklist de Implementación

Al trabajar en la solución de errores HTTP 500 en carrito, verificar:

- [ ] **Excepciones manejadas**
  - [ ] Todos los métodos de servicio tienen try-catch o lanzan excepciones custom
  - [ ] `GlobalExceptionHandler` cubre todos los tipos de excepción
  - [ ] Respuestas HTTP usan status codes semánticos

- [ ] **Validaciones completas**
  - [ ] DTOs tienen anotaciones `@Valid`
  - [ ] Lógica de negocio valida invariantes
  - [ ] Base de datos tiene constraints apropiadas

- [ ] **Logs informativos**
  - [ ] Nivel DEBUG para operaciones normales
  - [ ] Nivel WARN para errores esperados
  - [ ] Nivel ERROR solo para situaciones críticas
  - [ ] Contexto suficiente (IDs, valores) en cada log

- [ ] **Tests exhaustivos**
  - [ ] Cobertura > 80% en servicios críticos
  - [ ] Tests de casos edge (nulos, negativos, límites)
  - [ ] Tests de integración para flujos completos
  - [ ] Tests de concurrencia si aplica

- [ ] **Documentación actualizada**
  - [ ] README con setup actualizado
  - [ ] Changelog con cambios significativos
  - [ ] Comentarios JavaDoc en métodos públicos
  - [ ] Documento de troubleshooting con soluciones

- [ ] **Monitoreo configurado**
  - [ ] Métricas custom en Actuator
  - [ ] Alertas para errores críticos
  - [ ] Logs centralizados accesibles

---

## 9. Recursos Adicionales

### 9.1 Referencias Oficiales
- [Spring Boot Error Handling](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc)
- [Bean Validation Specification](https://beanvalidation.org/2.0/spec/)
- [JPA Locking Mechanisms](https://docs.oracle.com/javaee/7/tutorial/persistence-locking.htm)

### 9.2 Herramientas Recomendadas
- **SonarQube**: Análisis estático de código
- **JaCoCo**: Cobertura de tests
- **Sentry/Rollbar**: Tracking de errores en producción
- **ELK Stack**: Centralización de logs

### 9.3 Lecturas Complementarias
- *Effective Java* (Joshua Bloch) - Capítulos sobre excepciones
- *Release It!* (Michael Nygard) - Patrones de estabilidad
- *Clean Code* (Robert C. Martin) - Manejo de errores

---

## 10. Conclusiones

La resolución efectiva de errores HTTP 500 en endpoints críticos como el carrito de compras requiere un enfoque sistemático que combine:

1. **Diagnóstico preciso** mediante análisis de logs y herramientas de debugging
2. **Soluciones robustas** con validaciones multicapa y manejo explícito de excepciones
3. **Prevención proactiva** a través de tests exhaustivos y monitoreo continuo
4. **Documentación clara** que facilite el mantenimiento y la transferencia de conocimiento

La inversión en estas prácticas no solo resuelve problemas puntuales, sino que construye una base sólida para la escalabilidad y mantenibilidad del proyecto a largo plazo.

---

**Última actualización:** 16 de noviembre de 2025  
**Próxima revisión:** Trimestral o tras incidentes críticos  
**Contacto:** Equipo de Desarrollo - Cosecha en Cope
