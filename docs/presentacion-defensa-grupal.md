# Presentacion de Defensa Grupal — Ecosystem VJ

---

## 1. Informacion General

| Campo | Detalle                                                           |
|-------|-------------------------------------------------------------------|
| **Nombre del Proyecto** | GameVerse — E-Commerce de Videojuegos Online                      |
| **Asignatura** | DSY1103 — Desarrollo de Software y Arquitectura de Microservicios |
| **Profesor** | Prof. Martínez                                                    |
| **Institucion** | Duoc UC — Examen Final Transversal (EFT) Semestral 2025           |
| **Integrante 1** | Jonás Valencia — Ingeniero DevOps & QA                            |
| **Integrante 2** | Martín Soto — Arquitecto de Software Full-Stack                   |

---

## 2. Problema Abordado

### 2.1 Contexto del Problema

Las tiendas de videojuegos en linea enfrentan picos de concurrencia masiva durante eventos criticos: lanzamientos de juegos AAA, ofertas de temporada, y eventos de streaming en vivo. Durante estos picos, una arquitectura monolitica tradicional presenta fragilidad significativa:

- **Bloqueos concurrentes en inventario:** Multiples usuarios intentan comprar el mismo juego simultáneamente, generando condiciones de carrera en la tabla de stock. Un monolito tradicional procesa estas peticiones de forma secuencial, causando tiempos de respuesta inaceptables y perdida de ventas.
- **Puntos unicos de fallo:** Un error en el modulo de pagos puede colapsar todo el sistema, incluyendo catalogo, usuarios y resenas.
- **Escalabilidad limitada:** No es posible escalar solo el modulo de pagos durante un lanzamiento sin escalar toda la aplicacion, generando desperdicio de recursos.
- **Despliegue acoplado:** Una actualizacion en el modulo de notificaciones requiere re-desplegar toda la aplicacion, aumentando el riesgo de regresiones.

### 2.2 Impacto en el Negocio

Durante el lanzamiento de un titulo como "Elden Ring Nightreign", cientos de usuarios simultaneos intentan:
1. Consultar el catalogo de juegos
2. Agregar productos al carrito
3. Procesar pagos
4. Recibir confirmaciones por email

En un monolito, estos 4 pasos compiten por los mismos recursos del servidor, generando cuellos de botella que degradan la experiencia del usuario y reducen las ventas.

---

## 3. Solucion Propuesta y Alcance

### 3.1 Arquitectura Microservicios

Ecosystem VJ implementa una arquitectura de **12 microservicios independientes** que se comunican de forma reactiva y sincrona:

| Capa | Servicios | Tecnologia |
|------|-----------|------------|
| Infraestructura | Gateway (M11), Discovery Server (M12) | Spring Cloud Gateway, Netflix Eureka |
| Negocio Core | User (M1), Product (M2), Category (M9) | Spring Boot 3.4.4, Spring Data JPA |
| Transaccional | Cart (M3), Order (M4), Payment (M5) | Spring Boot 3.4.4, Bean Validation |
| Soporte | Inventory (M6), Review (M7), Notification (M8) | Spring Boot 3.4.4, RestTemplate/RestClient |
| Autenticacion | Auth (M10) | Spring Security, BCrypt |

### 3.2 Stack Tecnologico

| Componente | Tecnologia | Version |
|------------|------------|---------|
| Lenguaje | Java | 17 LTS |
| Framework | Spring Boot | 3.4.4 |
| Cloud | Spring Cloud | 2024.0.1 |
| Build Tool | Maven | 3.9+ |
| ORM | Hibernate (JPA) | 3.1.0 |
| Seguridad | Spring Security | 6.2.1 |
| Gateway | Spring Cloud Gateway | 4.1.1 |
| Discovery | Netflix Eureka | 4.1.1 |
| Base de datos (local) | H2 Database | En memoria |
| Base de datos (produccion) | PostgreSQL / MySQL | Render |

### 3.3 Estrategia Multi-Entorno

- **Perfil `local`:** H2 en memoria, puertos fijos (8080-8090), Eureka en localhost:8761. Sin configuracion externa requerida.
- **Perfil `render`:** Variables de entorno (`${PORT}`, `${DATABASE_URL}`, `${EUREKA_URI}`), Eureka centralizado, PostgreSQL via Render.

---

## 4. Principales Requerimientos Cumplidos

### 4.1 Endpoints Funcionales via Gateway (Puerto 8080)

| Modulo | Endpoint | Metodo | Funcionalidad | Validaciones |
|--------|----------|--------|---------------|-------------|
| User (M1) | `/api/v1/users` | GET/POST | CRUD de usuarios | `@NotBlank`, `@Email` unico |
| Product (M2) | `/api/v1/products` | GET/POST/PUT/DELETE | Catalogo completo | `@NotBlank`, `@Min(0)` precio y stock |
| Cart (M3) | `/api/v1/cart` | GET/POST/PATCH/DELETE | Carrito con seguridad | `@PreAuthorize`, roles ADMIN/USER/AGENT |
| Order (M4) | `/api/v1/orders` | POST | Creacion de ordenes | Validacion de producto via RestTemplate |
| Payment (M5) | `/api/v1/payments` | POST | Procesamiento de pagos | `@Positive`, `@DecimalMin("0.01")`, limite $1.000.000 |
| Inventory (M6) | `/api/v1/inventory` | POST | Gestion de stock | Validacion de producto via RestTemplate |
| Review (M7) | `/api/v1/reviews` | GET/POST | Resenas y calificaciones | `@Min(1) @Max(5)` rating, `@Size(10-500)` comentario |
| Notification (M8) | `/api/v1/notifications` | POST | Registro de notificaciones | `@Email`, `@Size(5-1000)` mensaje |
| Category (M9) | `/api/v1/categories` | GET/POST/PUT/DELETE | Categorias | Nombre unico (`unique=true`) |
| Auth (M10) | `/api/v1/auth` | POST | Registro y login | `@Size(3-50)` username |

### 4.2 Destacados de Implementacion

- **Payment_Service_M5:** Blindaje completo con `PaymentBusinessException` personalizada, HTTP 422 para violaciones de reglas de negocio, y limiter de transaccion a $1.000.000 CLP.
- **Review_Service_M7:** Rating estricto entre 1 y 5 (`@Min/@Max`), comentario con longitud controlada, y `ErrorResponse.FieldError` estructurado.
- **Cart_Service_M3:** Unico servicio con seguridad HTTP Basic completa: `@EnableMethodSecurity`, `@PreAuthorize` para edicion por propietario, e historial de cambios (`CartHistory`).

---

## 5. Feedback de la Ultima Evaluacion y Correcciones

### 5.1 FB-01: GlobalExceptionHandler (@RestControllerAdvice)

**Problema identificado:** Los controladores devolvian mensajes de error inconsistentes (strings plain text, objetos ad-hoc) que dificultaban el consumo por parte del frontend.

**Solucion implementada:** Se creo `GlobalExceptionHandler.java` en cada microservicio con `@RestControllerAdvice` que intercepta excepciones y devuelve JSON uniformes:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", 400);
        errors.put("error", "Bad Request");
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.toList());
        errors.put("details", details);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        Map<String, Object> error = Map.of("status", 404, "error", "Not Found", "message", ex.getMessage());
        return ResponseEntity.status(404).body(error);
    }
}
```

**Resultado:** Todas las respuestas de error siguen el mismo formato JSON con `status`, `error` y `details`, facilitando el manejo en el frontend.

### 5.2 FB-02: Bean Validation (JSR 380) con @Valid

**Problema identificado:** Los controladores no validaban los payloads de entrada, permitiendo que datos inconsistentes llegaran a la capa de persistencia.

**Solucion implementada:** Se anoto cada DTO de entrada con anotaciones Jakarta Validation y se agrego `@Valid` en los parametros de los controladores:

```java
@PostMapping
public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
    return ResponseEntity.ok(paymentService.createPayment(request));
}
```

**Anotaciones utilizadas:**

| Anotacion | Uso | Ejemplo |
|-----------|-----|---------|
| `@NotNull` | Campo obligatorio | `@NotNull Double amount` |
| `@NotBlank` | String no vacio | `@NotBlank String name` |
| `@Positive` | Numero positivo | `@Positive Long orderId` |
| `@DecimalMin` | Valor minimo decimal | `@DecimalMin("0.01") Double amount` |
| `@Min` / `@Max` | Rango numerico | `@Min(1) @Max(5) Integer rating` |
| `@Size` | Longitud de string | `@Size(min=10, max=500) String comment` |
| `@Email` | Formato de correo | `@NotBlank @Email String recipient` |

---

## 6. Arquitectura e Infraestructura

### 6.1 API Gateway (Gateway_Service_M11 — Puerto 8080)

Spring Cloud Gateway actua como el **punto de entrada unico** de todo el ecosistema. Todas las peticiones externas pasan por el Gateway, que enruta hacia los microservicios correspondientes:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE-M1    # En perfil render usa Eureka
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=2
        - id: product-service
          uri: lb://PRODUCT-SERVICE-M2
          predicates:
            - Path=/api/v1/products/**
          filters:
            - StripPrefix=2
```

**Caracteristicas clave:**
- 10 rutas configuradas con `StripPrefix=2` para limpiar prefijos
- Perfil render usa `lb://` para balanceo de carga via Eureka
- Perfil local usa URLs directas `http://localhost:{puerto}`
- CORS global configurado para permitir origenes externos

### 6.2 Discovery Server (Discovery_Server_M12 — Puerto 8761)

Netflix Eureka Server permite el **descubrimiento automatico** de servicios:

- Los 10 microservicios de negocio se registran automaticamente al iniciar
- El Gateway resuelve servicios por nombre (`lb://USER-SERVICE-M1`) en lugar de URLs hardcodeadas
- Health checks automaticos detectan servicios caidos
- Panel de monitoreo disponible en `http://localhost:8761`

### 6.3 Comunicacion Inter-Servicio

| Servicio Origen | Servicio Destino | Mecanismo | Proposito |
|----------------|-----------------|-----------|-----------|
| Order_M4 | Product_M2 | RestTemplate | Validar existencia del producto |
| Inventory_M6 | Product_M2 | RestTemplate | Validar existencia del producto |
| Cart_M3 | Notification_M8 | RestClient | Notificar al agregar item al carrito |

---

## 7. Flujo Funcional y Tecnico (Patron CSR)

### 7.1 Arquitectura en Capas

Cada microservicio sigue el patron **Controller → Service → Repository** (CSR):

```
Cliente (Frontend/Mobile)
    │
    ▼
┌─────────────────────────────────────────────┐
│  API GATEWAY (M11) — Puerto 8080            │
│  Spring Cloud Gateway + StripPrefix=2       │
│  Resolucion: lb://SERVICE-NAME via Eureka   │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  @RestController — Capa de Transporte       │
│  Recibe HTTP, valida con @Valid,            │
│  delega a @Service                          │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  @Service — Capa de Logica de Negocio       │
│  Reglas de negocio, validaciones cruzadas,  │
│  logs con SLF4J para trazabilidad           │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  @Repository (JpaRepository) — Capa de      │
│  Persistencia con JPA/Hibernate             │
│  H2 (local) / PostgreSQL (render)           │
└─────────────────────────────────────────────┘
```

### 7.2 Ejemplo: Crear un Pago (Paso a Paso)

```
1. POST /api/v1/payments llega al Gateway (M11:8080)
2. Gateway enruta hacia Payment_Service_M5 via lb://PAYMENT-SERVICE-M5
3. StripPrefix=2 elimina "/api/v1" → queda "/payments"
4. PaymentController.createPayment(@Valid @RequestBody PaymentRequest) recibe la peticion
5. @Valid activa las validaciones JSR 380 del DTO (amount @Positive, @DecimalMin)
6. Si hay errores de validacion → GlobalExceptionHandler retorna 400 Bad Request
7. Si es valido → PaymentService.createPayment() ejecuta la logica:
   a. Verifica que amount <= 1.000.000 (regla de negocio)
   b. Si excede → lanza PaymentBusinessException → retorna 422
   c. Si es valido → PaymentRepository.save() persiste en la BD
   d. Log: log.info("Pago creado: id={}, amount={}", payment.getId(), payment.getAmount())
8. Respuesta 200 OK con PaymentResponse serializado a JSON
```

### 7.3 Trazabilidad con SLF4J

Cada servicio configura logs estructurados en `application.yml`:

```yaml
logging:
  level:
    root: INFO
    cl.duoc.fullstack.payment_service_m5: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%-5level] %logger{0} - %msg%n"
```

---

## 8. Estrategia QA y Pruebas Unitarias

### 8.1 Framework y Herramientas

| Herramienta | Uso | Version |
|-------------|-----|---------|
| JUnit 5 | Framework de pruebas | 5.10.2 |
| Mockito | Mocking de dependencias | 5.10.0 |
| AssertJ | Aserciones expresivas | 3.25.3 |

### 8.2 Estructura Given-When-Then

Cada prueba sigue el patron **Arrange-Act-Assert** con comentarios explicitos:

```java
@Test
void shouldCreatePaymentSuccessfully() {
    // Given
    PaymentRequest request = new PaymentRequest(1L, 59990.0, PaymentMethod.CREDIT_CARD);
    Payment payment = new Payment(1L, 1L, 59990.0, PaymentStatus.PENDING, PaymentMethod.CREDIT_CARD);
    when(paymentRepository.save(any())).thenReturn(payment);

    // When
    PaymentResponse response = paymentService.createPayment(request);

    // Then
    assertNotNull(response);
    assertEquals(59990.0, response.getAmount());
    verify(paymentRepository, times(1)).save(any());
}
```

### 8.3 Cobertura por Servicio

| Servicio | Tests | Cobertura Estimada |
|----------|-------|-------------------|
| User_Service_M1 | 5 | ~85% |
| Product_Service_M2 | 5 | ~85% |
| Cart_Service_M3 | 5 | ~80% |
| Order_Service_M4 | 5 | ~85% |
| Payment_Service_M5 | 9 | ~90% |
| Inventory_Service_M6 | 5 | ~85% |
| Review_Service_M7 | 8 | ~88% |
| Notification_Service_M8 | 5 | ~85% |
| Category_Service_M9 | 8 | ~90% |
| Auth_Service_M10 | 5 | ~85% |
| Gateway_Service_M11 | 1 | Smoke test |
| Discovery_Server_M12 | 1 | Smoke test |
| **TOTAL** | **62** | **~85% promedio** |

### 8.4 Suite de Pruebas REST

El archivo `docs/pruebas-rest/casos-prueba.http` contiene **46 escenarios** de prueba en formato IntelliJ HTTP Client:

- 14 peticiones exitosas (CRUD completo)
- 15 casos de datos invalidos (Bean Validation)
- 5 recursos inexistentes (404)
- 4 permisos insuficientes (401/403)
- 1 flujo de negocio completo (8 pasos)
- 4 health checks del sistema

---

## 9. Swagger y Despliegue Cloud (Render)

### 9.1 Documentacion API con SpringDoc OpenAPI

Cada microservicio incluye `springdoc-openapi-starter-webmvc-ui` que expone la documentacion interactiva:

| Servicio | URL Swagger UI (Local) |
|----------|----------------------|
| User_Service_M1 | http://localhost:8081/swagger-ui.html |
| Product_Service_M2 | http://localhost:8082/swagger-ui.html |
| Cart_Service_M3 | http://localhost:8083/swagger-ui.html |
| Order_Service_M4 | http://localhost:8084/swagger-ui.html |
| Payment_Service_M5 | http://localhost:8085/swagger-ui.html |
| Inventory_Service_M6 | http://localhost:8086/swagger-ui.html |
| Review_Service_M7 | http://localhost:8087/swagger-ui.html |
| Notification_Service_M8 | http://localhost:8088/swagger-ui.html |
| Category_Service_M9 | http://localhost:8089/swagger-ui.html |
| Auth_Service_M10 | http://localhost:8090/swagger-ui.html |

### 9.2 Despliegue en Render

El archivo `render.yaml` define la infraestructura como codigo:

```yaml
services:
  - type: web
    name: discovery-server-m12
    runtime: java
    plan: free
    envVars:
      - key: JAVA_VERSION
        value: "17"
      - key: SPRING_PROFILES_ACTIVE
        value: render
      - key: PORT
        value: 8761
  - type: web
    name: gateway-service-m11
    runtime: java
    plan: free
    envVars:
      - key: EUREKA_URI
        fromService:
          name: discovery-server-m12
          property: host
    dependsOn:
      - discovery-server-m12
```

**URLs simuladas de produccion:**

| Servicio | URL Render |
|----------|-----------|
| Discovery Server | https://discovery-server-m12-ecosystem.onrender.com |
| API Gateway | https://gateway-service-m11-ecosystem.onrender.com |
| User Service | https://user-service-m1-ecosystem.onrender.com |
| Product Service | https://product-service-m2-ecosystem.onrender.com |
| Payment Service | https://payment-service-m5-ecosystem.onrender.com |

**Orden de despliegue automatizado:**
1. `discovery-server-m12` arranca primero (Eureka Server)
2. Todos los demas servicios esperan a que Eureka este activo (`dependsOn`)
3. `gateway-service-m11` se despliega ultimo (necesita Eureka para resolver `lb://`)

---

## 10. Distribucion de Trabajo

### 10.1 Jonás Valencia — Ingeniero DevOps & QA

| Responsabilidad | Detalle |
|----------------|---------|
| API Gateway (M11) | Configuracion de Spring Cloud Gateway, 10 rutas `StripPrefix=2`, CORS global, perfiles local/render |
| Discovery Server (M12) | Implementacion de Netflix Eureka Server con `@EnableEurekaServer`, configuracion de registro |
| render.yaml | Orquestacion completa de 12 servicios + MySQL en Render con `dependsOn` |
| Eureka Client | Agregacion de `spring-cloud-starter-netflix-eureka-client` a los 11 pom.xml |
| Perfiles de Entorno | Configuracion de `application.yml` con perfiles `local` y `render` en los 12 servicios |
| Pruebas Unitarias | Suite completa JUnit 5 + Mockito en los 12 servicios (62 tests totales) |
| Suite REST | Archivo `docs/pruebas-rest/casos-prueba.http` con 46 escenarios de prueba |
| Documentacion Tecnica | `docs/documentacion-tecnica.md`, `docs/levantamiento-requerimientos-actualizado.md` |
| Dockerfiles | Archivos Docker multi-stage para despliegue en contenedores |

### 10.2 Martín Soto — Arquitecto de Software Full-Stack

| Responsabilidad | Detalle |
|----------------|---------|
| Modelamiento JPA/Hibernate | 10 modelos de dominio (User, Product, CartItem, Order, Payment, Review, Notification, Category, AuthUser, CartHistory) |
| Logica Core (M1-M10) | Controladores, servicios y repositorios de los 10 microservicios de negocio |
| DTOs y Mapeo | Request/Response DTOs para cada servicio (PaymentRequest, ReviewRequest, etc.) |
| Bean Validation | Implementacion de anotaciones JSR 380 en todos los DTOs de entrada |
| GlobalExceptionHandler | `@RestControllerAdvice` uniforme en los 10 servicios con manejo de excepciones |
| Seguridad (M3) | `@EnableMethodSecurity`, `@PreAuthorize`, `BCryptPasswordEncoder`, `SecurityConfig` |
| Comunicacion Inter-Servicio | `RestTemplate` (M4, M6) y `RestClient` (M3) para llamadas entre servicios |
| README.md | Documentacion completa del proyecto con arquitectura, servicios y endpoints |
| Documentacion Funcional | `docs/documentacion-funcional.md` con flujos, reglas de negocio y payloads |

---

## 11. Conclusiones

Ecosystem VJ demuestra que la arquitectura de microservicios es una solucion viable y escalable para el comercio electronico de videojuegos. Los 12 microservicios implementados cubren los requerimientos fundamentales de un e-commerce real:

- **Catalogo completo** con CRUD de productos, categorias y usuarios
- **Seguridad por roles** con autenticacion basica y autorizacion por endpoints
- **Validacion robusta** con Bean Validation JSR 380 en todos los puntos de entrada
- **Manejo uniforme de errores** con GlobalExceptionHandler en todos los servicios
- **Infraestructura cloud** lista para despliegue en Render con orquestacion automatica
- **Pruebas automatizadas** con 62 pruebas unitarias y 46 escenarios REST

El trabajo colaborativo entre DevOps y Arquitecto Full-Stack permitio entregar un proyecto completo, documentado y funcionando en entorno local y preparado para produccion.

---

> **Duoc UC — Examen Final Transversal 2026**
>
> **Integrantes:** Jonás Valencia | Martín Soto