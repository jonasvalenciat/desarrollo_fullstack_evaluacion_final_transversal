# MATRIZ DE REQUERIMIENTOS — Cierre Semestral DSY1103

> **Duoc UC — Evaluación Final / Full Stack — Estandarización Técnica**
>
> Documento de trazabilidad que registra el estado final de cada
> requerimiento implementado en el ecosistema de 11 microservicios
> Spring Boot, incluyendo los agregados por feedback docente.

---

## 1. Introducción y Contexto del Cierre Semestral

El presente documento consolida la evolución del ecosistema de
microservicios desarrollado durante el semestre en la asignatura
DSY1103 — Desarrollo Full Stack. El proyecto partió desde
**endpoints aislados e independientes** por cada estudiante,
donde cada microservicio operaba de forma fragmentada sin una
capa técnica común.

A partir de la evaluación 2, se inició un proceso de
**estandarización técnica** que unificó la base tecnológica
de los 10 microservicios originales bajo:

- **Java 17** y **Spring Boot 3.4.4** como base común.
- **Swagger (springdoc-openapi 2.8.6)** para documentación viva.
- **DTOs** con validaciones **JSR 380 (Bean Validation / Jakarta)**.
- **Manejo global de excepciones** con `@RestControllerAdvice`.
- **Pruebas unitarias** con **Mockito** y estructura
  Given–When–Then.
- **Dockerfiles multi-stage** con Alpine Linux.
- **Configuración YAML** con perfiles de base de datos.

En la etapa de cierre semestral (evaluacion_final_transversal), el sistema
evolucionó hacia un **backend real y unificado** con:

- **API Gateway centralizado** (`Gateway_Service_M11`) como
  punto de entrada único en el puerto 8080, usando
  **Spring Cloud Gateway**.
- **Robustecimiento de reglas de negocio** en los servicios
  de **Payment (M5)**, **Review (M7)** y **Notification (M8)**
  con validaciones Bean Validation reforzadas, excepciones
  de negocio personalizadas y manejo centralizado de errores.
- **Seguridad por roles** en Cart_Service_M3 con HTTP Basic,
  `@PreAuthorize` y validación de propiedad sobre recursos.
- **Comunicación entre servicios** vía `RestTemplate` y
  `RestClient` para validación de entidades cruzadas.

---

## 2. Matriz de Trazabilidad y Estado Final

| ID | Microservicio / Módulo | Requerimiento Implementado | Estado Final | Trazabilidad en Código | Justificación Técnica |
|----|----------------------|--------------------------|-------------|----------------------|---------------------|
| R01 | **Gateway_Service_M11** | API Gateway centralizado como punto de entrada único en puerto 8080 | **AGREGADO POR FEEDBACK DOCENTE** | Paquete: `cl.duoc.fullstack.gateway_service_m11`<br>Clase: `GatewayServiceM11Application`<br>Config: `application.yml` (routes con StripPrefix=2, CORS global) | Spring Cloud Gateway 2024.0.1 enruta peticiones `/api/v1/{recurso}/**` hacia los 10 microservicios backend. Configura CORS global `allowedOrigins: *` y filtro `StripPrefix=2` para eliminar prefijos antes del proxy. No requiere base de datos ni capa de negocio. |
| R02 | **User_Service_M1** | CRUD de usuarios con registro y consulta por ID | **ESTANDARIZADO** | Paquete: `cl.duoc.fullstack.user_service_m1`<br>Controller: `UserController` (`/users`)<br>Service: `UserService`<br>Model: `User` (name, email, password)<br>Config: `SecurityConfig`, `GlobalExceptionHandler`<br>Test: `UserServiceTest` (4 tests) | `createUser()` valida email duplicado vía `existsByEmail()` y hashea contraseñas con `BCryptPasswordEncoder`. `getUserById()` lanza `EntityNotFoundException`. Validaciones en modelo: `@NotBlank`, `@Email`. |
| R03 | **Product_Service_M2** | CRUD completo de productos (GET, POST, PUT, DELETE) | **ESTANDARIZADO** | Paquete: `cl.duoc.fullstack.product_service`<br>Controller: `ProductController` (`/products`)<br>Service: `ProductService`<br>Model: `Product` (name, price, stock)<br>Config: `SecurityConfig`, `GlobalExceptionHandler`<br>Test: `ProductServiceTest` (4 tests) | Soporta los 4 verbos HTTP REST. Modelo con validaciones `@NotBlank`, `@Min(0)` en precio y stock. Service valida existencia antes de actualizar/eliminar, lanza `EntityNotFoundException`. |
| R04 | **Cart_Service_M3** | Carrito de compras con seguridad por roles, historial y notificaciones | **ESTANDARIZADO** | Paquete: `cl.duoc.fullstack.cart_service_m3`<br>Controllers: `CartController` (`/cart`), `UserController` (`/users`)<br>Services: `CartService`, `UserService`<br>Models: `CartItem`, `CartHistory`, `User`<br>DTOs: `CartItemRequest`, `CartItemCommand`, `CartItemResponse`, `CartItemResult`, `AssignUserRequest`, `UserCreateDTO`, `UserResponseDTO`, `CartHistoryResult`, `NotificationRequest`<br>Config: `SecurityConfig`, `GlobalExceptionHandler`, `RestClientConfig`, `DataInitializer`, `CustomUserDetailsService`, `CartSecurity`<br>Client: `NotificationClient`<br>Exception: `BadRequestException`<br>Repo: `CartRepository`, `CartHistoryRepository`, `UserRepository`<br>Test: `CartServiceTest` (4 tests) | Único servicio con **seguridad completa**: HTTP Basic, `@EnableMethodSecurity`, `@PreAuthorize("@cartSecurity.canEdit(#id, authentication)")`, roles USER/AGENT/ADMIN. `DataInitializer` siembra datos en perfil H2. `CartService.addToCart()` valida: duplicado de producto, existencia de usuario, coupon ≠ email. Registra historial de cambios de estado. `NotificationClient` notifica vía `RestClient`. |
| R05 | **Order_Service_M4** | Creación de órdenes con validación de producto existente | **ESTANDARIZADO** | Paquete: `cl.duoc.fullstack.order_service_m4`<br>Controller: `OrderController` (`/orders`)<br>Service: `OrderService`<br>Model: `Order` (productId, quantity, status)<br>DTOs: `OrderRequest`, `OrderResponse`, `ProductDTO`<br>Config: `SecurityConfig`, `GlobalExceptionHandler`, `RestTemplateConfig`<br>Test: `OrderServiceTest` (4 tests) | `createOrder()` valida la existencia del producto consultando `Product_Service_M2` vía `RestTemplate.getForObject("http://localhost:8082/products/{id}")`. Si el producto no existe, lanza `IllegalArgumentException`. DTO `OrderRequest` con `@NotNull` y `@Min(1)` en quantity. |
| R06 | **Payment_Service_M5** | Procesamiento de pagos con reglas de negocio robustecidas | **AGREGADO POR FEEDBACK DOCENTE** | Paquete: `cl.duoc.fullstack.payment_service_m5`<br>Controller: `PaymentController` (`/payments`)<br>Service: `PaymentService`<br>Model: `Payment`, `PaymentMethod` (enum), `PaymentStatus` (enum)<br>DTOs: `PaymentRequest`, `PaymentResponse`, `ErrorResponse`<br>Exception: `PaymentBusinessException` (code)<br>Config: `SecurityConfig`, `GlobalExceptionHandler`<br>Test: `PaymentServiceTest` (8 tests) | **Robustecido con Bean Validation JSR 380:** `PaymentRequest` con `@NotNull`, `@Positive` (orderId), `@DecimalMin("0.01")` (amount). **Reglas de negocio en `PaymentService`:** orderId positivo, monto > 0, monto ≤ $1.000.000 (limite transaccion sospechosa). **Excepción personalizada** `PaymentBusinessException` con códigos: `INVALID_ORDER_ID`, `INVALID_AMOUNT`, `AMOUNT_EXCEEDS_LIMIT`, `PAYMENT_NOT_FOUND`. **GlobalExceptionHandler** retorna HTTP 422 para violaciones de negocio. `ErrorResponse` con campo `code` para debugging estructurado. |
| R07 | **Inventory_Service_M6** | Actualización de stock con validación de producto | **ESTANDARIZADO** | Paquete: `cl.duoc.fullstack.inventory_service_m6`<br>Controller: `InventoryController` (`/inventory`)<br>Service: `InventoryService`<br>Model: `Inventory` (productId, stock)<br>DTOs: `InventoryResponse`, `ProductDTO`, `ErrorResponse`<br>Config: `SecurityConfig`, `GlobalExceptionHandler`, `RestTemplateConfig`<br>Test: `InventoryServiceTest` (4 tests) | `updateStock()` valida existencia del producto vía `RestTemplate` hacia `Product_Service_M2`. Acumula stock (soporta cantidades negativas para decremento). Crea registro nuevo si no existe inventario para el producto. Modelo con `@NotNull`, `@Min(0)` en stock. |
| R08 | **Review_Service_M7** | Gestión de reseñas con validaciones robustecidas | **AGREGADO POR FEEDBACK DOCENTE** | Paquete: `cl.duoc.fullstack.review_service_m7`<br>Controller: `ReviewController` (`/reviews`)<br>Service: `ReviewService`<br>Model: `Review` (productId, userId, rating, comment)<br>DTOs: `ReviewRequest`, `ReviewResponse`, `ErrorResponse` (con inner `FieldError`)<br>Config: `SecurityConfig`, `GlobalExceptionHandler`<br>Test: `ReviewServiceTest` (7 tests) | **Robustecido con Bean Validation JSR 380:** `ReviewRequest` con `@NotNull`, `@Min(1)`, `@Max(5)` (rating 1-5 estrellas), `@NotBlank`, `@Size(min=10, max=500)` (comment). **Regla de negocio:** `validateProductId()` verifica productId > 0, lanza `IllegalArgumentException`. **ErrorResponse mejorado** con inner record `FieldError(field, message)` y factory methods `of()` para respuesta estructurada. `GlobalExceptionHandler` concatena errores de campo individuales. |
| R09 | **Notification_Service_M8** | Envío de notificaciones con validaciones robustecidas | **AGREGADO POR FEEDBACK DOCENTE** | Paquete: `cl.duoc.fullstack.notification_service_m8`<br>Controller: `NotificationController` (`/notifications`)<br>Service: `NotificationService`<br>Model: `Notification`, `NotificationType` (enum: EMAIL, SMS, PUSH)<br>DTOs: `NotificationRequest`, `NotificationResponse`, `ErrorResponse`<br>Config: `SecurityConfig`, `GlobalExceptionHandler`<br>Test: `NotificationServiceTest` (4 tests) | **Robustecido con Bean Validation JSR 380:** `NotificationRequest` con `@NotNull` (userId, type), `@NotBlank`, `@Email` (recipient — validación de formato correo), `@NotBlank`, `@Size(min=5, max=1000)` (message). Modelo con `@Enumerated(STRING)` para tipo. `GlobalExceptionHandler` centraliza `MethodArgumentNotValidException` concatenando errores de campo. |
| R10 | **Category_Service_M9** | CRUD completo de categorías con protección de integridad | **ESTANDARIZADO** | Paquete: `cl.duoc.fullstack.category_service_m9`<br>Controller: `CategoryController` (`/categories`)<br>Service: `CategoryService`<br>Model: `Category` (name, description)<br>DTOs: `CategoryRequest`, `CategoryResponse`, `ErrorResponse`<br>Config: `SecurityConfig`, `GlobalExceptionHandler`<br>Test: `CategoryServiceTest` (7 tests) | Soporta los 4 verbos HTTP REST. `CategoryRequest` con `@NotBlank`, `@Size(max=100)` en nombre y `@Size(max=255)` en descripción. Columna `unique=true` en nombre. `GlobalExceptionHandler` captura `DataIntegrityViolationException` (HTTP 409) con mensaje "El nombre de la categoria ya existe". |
| R11 | **Auth_Service_M10** | Servicio de autenticación con registro y login | **ESTANDARIZADO** | Paquete: `cl.duoc.fullstack.auth_service_m10`<br>Controller: `AuthController` (`/auth`)<br>Service: `AuthService`<br>Model: `AuthUser` (username, password, role)<br>DTOs: `AuthRegisterRequest`, `AuthLoginRequest`, `AuthRegisterResponse`, `AuthLoginResponse`, `ErrorResponse`<br>Config: `SecurityConfig`, `GlobalExceptionHandler`<br>Test: `AuthServiceTest` (4 tests) | `register()` guarda usuario con `@Column(unique=true)` para username. `login()` valida credenciales y retorna token mock (`MOCK_JWT_TOKEN_FOR_{username}`). `AuthRegisterRequest` con `@Size(min=3, max=50)` en username y `@Size(min=4, max=100)` en password. `GlobalExceptionHandler` captura `DataIntegrityViolationException` (409) y retorna `IllegalArgumentException` (401) para credenciales inválidas. `SessionCreationPolicy.STATELESS`. |

---

## 3. Resumen de Capacidades Técnicas Implementadas

| Capacidad Técnica | Alcance | Servicios |
|-------------------|---------|-----------|
| **API Gateway (Spring Cloud Gateway)** | Punto de entrada único, puerto 8080, 10 rutas con StripPrefix=2, CORS global | M11 |
| **Configuración YAML con perfiles** | `application.yml` + `application-h2.yml` (desarrollo) | M2–M11 |
| **DTOs (Request / Response / ErrorResponse)** | Aislamiento entidad JPA ↔ capa presentación | M1–M10 |
| **Bean Validation (JSR 380 / Jakarta)** | `@NotNull`, `@NotBlank`, `@Positive`, `@DecimalMin`, `@Min`, `@Max`, `@Size`, `@Email` | M1–M10 |
| **Manejo global de excepciones** | `@RestControllerAdvice` con `GlobalExceptionHandler` | M1–M10 |
| **Excepciones de negocio personalizadas** | `PaymentBusinessException` con códigos de error | M5 |
| **Seguridad por roles (HTTP Basic + @PreAuthorize)** | USER / AGENT / ADMIN, `@EnableMethodSecurity`, `CartSecurity` | M3 |
| **Comunicación inter-servicios** | `RestTemplate` (M4, M6 → M2), `RestClient` (M3 → M8) | M3, M4, M6 |
| **Pruebas unitarias con Mockito** | Estructura Given–When–Then | M1–M10 |
| **Dockerfile multi-stage Alpine** | Build con Maven + runtime JRE Alpine | M1–M10 |
| **Documentación Swagger** | `@Tag`, `@Operation`, `@ApiResponses` con springdoc-openapi | M1–M10 |

---

## 4. Evidencia de Ejecución y Reproducibilidad

### 4.1 Comunicación a través del API Gateway

Todos los microservicios son accesibles exclusivamente a través del
`Gateway_Service_M11` en el puerto **8080**. Las peticiones externas
se enrutan de la siguiente forma:

```
Cliente → http://localhost:8080/api/v1/{recurso}/**
         ↓ StripPrefix=2
Gateway_Service_M11 → http://localhost:{puerto}/{recurso}/**
```

**Ejemplo de flujo completo (Compra de un producto):**

```
1. POST /api/v1/auth/login           → Auth_Service_M10    (puerto 8090)
2. GET  /api/v1/products/1           → Product_Service_M2   (puerto 8082)
3. POST /api/v1/cart/add             → Cart_Service_M3      (puerto 8083)
   └─ Interno: POST localhost:8081/api/notifications       → Notification_Service_M8
4. POST /api/v1/orders               → Order_Service_M4     (puerto 8084)
   └─ Interno: GET localhost:8082/products/{id}            → Product_Service_M2
5. PUT  /api/v1/inventory/{id}?qty= → Inventory_Service_M6 (puerto 8086)
   └─ Interno: GET localhost:8082/products/{id}            → Product_Service_M2
6. POST /api/v1/payments             → Payment_Service_M5   (puerto 8085)
7. POST /api/v1/reviews              → Review_Service_M7    (puerto 8087)
```

### 4.2 Comunicación Directa entre Microservicios

| Servicio Origen | Servicio Destino | Protocolo | URL Destino | Propósito |
|----------------|-----------------|-----------|-------------|-----------|
| Order_Service_M4 | Product_Service_M2 | `RestTemplate` | `http://localhost:8082/products/{id}` | Validar existencia del producto antes de crear orden |
| Inventory_Service_M6 | Product_Service_M2 | `RestTemplate` | `http://localhost:8082/products/{id}` | Validar existencia del producto antes de actualizar stock |
| Cart_Service_M3 | Notification_Service_M8 | `RestClient` | `http://localhost:8081/api/notifications` | Enviar notificación al agregar item al carrito |

### 4.3 Ejecución Local

```bash
# 1. Iniciar todos los microservicios (en terminales separadas)
cd User_Service_M1        && ./mvnw spring-boot:run   # Puerto 8081
cd Product_Service_M2     && ./mvnw spring-boot:run   # Puerto 8082
cd Cart_Service_M3        && ./mvnw spring-boot:run   # Puerto 8083
cd Order_Service_M4       && ./mvnw spring-boot:run   # Puerto 8084
cd Payment_Service_M5     && ./mvnw spring-boot:run   # Puerto 8085
cd Inventory_Service_M6   && ./mvnw spring-boot:run   # Puerto 8086
cd Review_Service_M7      && ./mvnw spring-boot:run   # Puerto 8087
cd Notification_Service_M8 && ./mvnw spring-boot:run   # Puerto 8088
cd Category_Service_M9    && ./mvnw spring-boot:run   # Puerto 8089
cd Auth_Service_M10       && ./mvnw spring-boot:run   # Puerto 8090

# 2. Iniciar el Gateway (después de los demás servicios)
cd Gateway_Service_M11    && ./mvnw spring-boot:run   # Puerto 8080

# 3. Ejecutar pruebas unitarias
cd Cart_Service_M3 && ./mvnw clean test    # Tests: 4 + 1 contextLoads
cd Payment_Service_M5 && ./mvnw clean test # Tests: 8 + 1 contextLoads
cd Review_Service_M7 && ./mvnw clean test  # Tests: 7 + 1 contextLoads
```

### 4.4 Pruebas Unitarias por Servicio

| Servicio | Archivo de Test | Tests Unitarios | Tests de Contexto | Total |
|----------|----------------|----------------|-------------------|-------|
| User_Service_M1 | `UserServiceTest` | 4 | 1 | 5 |
| Product_Service_M2 | `ProductServiceTest` | 4 | 1 | 5 |
| Cart_Service_M3 | `CartServiceTest` | 4 | 1 | 5 |
| Order_Service_M4 | `OrderServiceTest` | 4 | 1 | 5 |
| **Payment_Service_M5** | **`PaymentServiceTest`** | **8** | **1** | **9** |
| Inventory_Service_M6 | `InventoryServiceTest` | 4 | 1 | 5 |
| **Review_Service_M7** | **`ReviewServiceTest`** | **7** | **1** | **8** |
| Notification_Service_M8 | `NotificationServiceTest` | 4 | 1 | 5 |
| Category_Service_M9 | `CategoryServiceTest` | 7 | 1 | 8 |
| Auth_Service_M10 | `AuthServiceTest` | 4 | 1 | 5 |
| Gateway_Service_M11 | `GatewayServiceM11ApplicationTests` | 0 | 1 | 1 |
| **TOTAL** | | **50** | **11** | **61** |

### 4.5 Contenedorización (Docker)

Todos los servicios (M1–M10) incluyen `Dockerfile` multi-stage:

```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE {puerto}
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 5. Evolución del Proyecto — Resumen Visual

```
EVALUACIÓN 2 (Aislado)          EVALUACIÓN 3 (Estandarizado)       evaluacion_final_transversal(Unificado)
┌──────────────────┐           ┌──────────────────────┐            ┌──────────────────────────┐
│ Endpoints sueltos │    →     │ Base técnica común    │     →     │ Backend real y unificado  │
│ Sin DTOs          │           │ Java 17 + SB 3.4.4   │            │ API Gateway (M11)         │
│ Sin validaciones  │           │ DTOs + JSR 380        │            │ Bean Validation robusto   │
│ Sin tests         │           │ GlobalExceptionHandler │           │ Reglas de negocio M5/M7/M8│
│ Sin Docker        │           │ Mockito tests         │            │ Comunicación inter-service │
│ Sin Swagger       │           │ Dockerfiles Alpine    │            │ Seguridad por roles (M3)  │
│                   │           │ Swagger + YAML        │            │ 11 servicios unificados   │
└──────────────────┘           └──────────────────────┘            └──────────────────────────┘
```
