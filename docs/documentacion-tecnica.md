# Documentacion Tecnica — GameVerse

> **Proyecto:** GameVerse — Tienda de Videojuegos Online
>
> **Asignatura:** DSY1103 — Desarrollo Full Stack
>
> **Version:** 1.0 — Cierre Semestral 2025

---

## 1. Arquitectura General y Comunicacion

### 1.1 Vision General

GameVerse implementa una arquitectura de **microservicios con acoplamiento reactivo sincrono** compuesta por 12 servicios independientes:

```
                          CLIENTE
                             |
                             v
                   +-------------------+
                   | API GATEWAY (M11) |  Puerto 8080
                   | Spring Cloud GW   |  Punto de entrada unico
                   +--------+----------+
                            |
              +-------------+-------------+
              |                           |
              v                           v
     +----------------+         +------------------+
     | EUREKA SERVER  |         | Microservicios   |
     | (M12) :8761    |<------->| M1-M10           |
     | Descubrimiento |  registrar| (registrados     |
     +----------------+         |  como clientes)   |
                                +------------------+
```

### 1.2 Patron de Comunicacion

- **Sincrona (REST):** Los servicios se comunican entre si via HTTP REST usando `RestTemplate` (M4, M6) y `RestClient` (M3).
- **API Gateway:** Todas las peticiones externas pasan por Spring Cloud Gateway (M11) que enruta hacia los servicios usando el filtro `StripPrefix=2`.
- **Service Discovery:** Netflix Eureka (M12) permite resolver servicios por nombre (`lb://service-name`) en lugar de URLs hardcodeadas.
- **Load Balancing:** En perfil render, el Gateway usa `lb://` para balanceo de carga via Eureka. En local, usa URLs directas `http://localhost:{puerto}`.

### 1.3 Comunicacion Inter-Servicios

| Servicio Origen | Servicio Destino | Protocolo | URL | Propósito |
|----------------|-----------------|-----------|-----|-----------|
| Order_Service_M4 | Product_Service_M2 | RestTemplate | `http://localhost:8082/products/{id}` | Validar existencia del producto antes de crear orden |
| Inventory_Service_M6 | Product_Service_M2 | RestTemplate | `http://localhost:8082/products/{id}` | Validar existencia del producto antes de actualizar stock |
| Cart_Service_M3 | Notification_Service_M8 | RestClient | `http://localhost:8081/api/notifications` | Enviar notificacion al agregar item al carrito |

---

## 2. Responsabilidades por Servicio

| Modulo | Servicio | Puerto | Responsabilidad Tecnica |
|--------|----------|--------|------------------------|
| M12 | Discovery_Server_M12 | 8761 | Eureka Server con `@EnableEurekaServer`. Registra y descubre servicios. Sin base de datos propia. |
| M11 | Gateway_Service_M11 | 8080 | Spring Cloud Gateway con 10 rutas `StripPrefix=2`. CORS global. Perfil render usa `lb://` via Eureka. Sin base de datos. |
| M1 | User_Service_M1 | 8081 | CRUD de usuarios con `BCryptPasswordEncoder`. Validacion `@Email`, `@NotBlank`. Email unico via `existsByEmail()`. |
| M2 | Product_Service_M2 | 8082 | CRUD completo (GET/POST/PUT/DELETE). Modelo: name, price (`@Min(0)`), stock (`@Min(0)`). |
| M3 | Cart_Service_M3 | 8083 | Carrito con seguridad por roles (ADMIN/USER/AGENT). `@EnableMethodSecurity`, `@PreAuthorize`. Historial de cambios. Integracion con Notification via RestClient. |
| M4 | Order_Service_M4 | 8084 | Creacion de ordenes. Valida producto existente via RestTemplate hacia M2. Estado inicial: CREATED. |
| M5 | Payment_Service_M5 | 8085 | Procesamiento de pagos. Bean Validation JSR 380. `PaymentBusinessException` con codigos de error. Limite de transaccion $1.000.000. HTTP 422 para violaciones de negocio. |
| M6 | Inventory_Service_M6 | 8086 | Gestion de stock. Valida producto via RestTemplate hacia M2. Acumula stock (soporta decremento con cantidades negativas). |
| M7 | Review_Service_M7 | 8087 | Resenas y calificaciones. Rating 1-5 (`@Min/@Max`). Comentario 10-500 chars (`@Size`). `ErrorResponse.FieldError` estructurado. |
| M8 | Notification_Service_M8 | 8088 | Notificaciones EMAIL/SMS/PUSH. Validacion `@Email` en recipient. Mensaje 5-1000 chars. Enum `NotificationType`. |
| M9 | Category_Service_M9 | 8089 | CRUD de categorias. Nombre unico (`unique=true`). Captura `DataIntegrityViolationException` (HTTP 409). |
| M10 | Auth_Service_M10 | 8090 | Autenticacion. Registro con `@Size(min=3, max=50)`. Login con token mock. `SessionCreationPolicy.STATELESS`. |

---

## 3. Modelo de Datos y Relaciones

### 3.1 Persistencia

Todos los servicios usan **Spring Data JPA** con **Hibernate** como proveedor de ORM:

- **Perfil local:** H2 Database en memoria (`jdbc:h2:mem:{service}_db`), `ddl-auto: create-drop` o `update`.
- **Perfil render:** MySQL/PostgreSQL via variable `${DATABASE_URL}`, `ddl-auto: update`.

### 3.2 Modelos de Datos por Servicio

**User (M1):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| name | String | `@NotBlank` |
| email | String | `@NotBlank @Email` |
| password | String | `@NotBlank` |

**Product (M2):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| name | String | `@NotBlank` |
| price | Double | `@NotNull @Min(0)` |
| stock | Integer | `@NotNull @Min(0)` |

**CartItem (M3):**
| Campo | Tipo | Relacion |
|-------|------|----------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| productName | String | `@Column(nullable=false, length=100)` |
| price | double | — |
| quantity | int | — |
| user | User | `@ManyToOne(fetch=LAZY)` |
| couponCode | String | — |
| status | String | — |
| history | List | `@OneToMany(mappedBy="cartItem", cascade=ALL)` |

**Order (M4):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| productId | Long | `@NotNull` |
| quantity | Integer | `@NotNull @Min(1)` |
| status | String | Default "PENDING" |

**Payment (M5):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| orderId | Long | `@NotNull @Positive` |
| amount | Double | `@NotNull @DecimalMin("0.01")` |
| status | PaymentStatus | `@Enumerated(STRING)`, Default PENDING |
| paymentMethod | PaymentMethod | `@Enumerated(STRING)` |
| createdAt | LocalDateTime | `@CreationTimestamp` |

Enums M5: `PaymentMethod` (CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CASH), `PaymentStatus` (PENDING, SUCCESS, FAILED, REJECTED)

**Review (M7):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| productId | Long | `@NotNull` |
| userId | Long | `@NotNull` |
| rating | Integer | `@NotNull @Min(1) @Max(5)` |
| comment | String | `@NotBlank @Size(min=10, max=500)` |

**Notification (M8):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| userId | Long | `@NotNull` |
| recipient | String | `@NotBlank @Email` |
| message | String | `@NotBlank @Size(min=5, max=1000)` |
| type | NotificationType | `@Enumerated(STRING)` |

Enum M8: `NotificationType` (EMAIL, SMS, PUSH)

**Category (M9):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| name | String | `@NotBlank @Column(unique=true)` |
| description | String | `@Size(max=255)` |

**AuthUser (M10):**
| Campo | Tipo | Constraints |
|-------|------|-------------|
| id | Long | `@Id @GeneratedValue(IDENTITY)` |
| username | String | `@NotBlank @Column(unique=true)` |
| password | String | `@NotBlank` |
| role | String | Default "ROLE_USER" |

---

## 4. Configuracion Multi-Entorno, Perfiles y Variables

### 4.1 Estructura de Perfiles

Cada servicio define 2 perfiles en `application.yml`:

**Perfil `local`** (desarrollo):
```yaml
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:h2:mem:{service}_db
server:
  port: {puerto_fijo}
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

**Perfil `render`** (produccion):
```yaml
spring:
  config:
    activate:
      on-profile: render
  datasource:
    url: ${DATABASE_URL}
server:
  port: ${PORT:8080}
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
```

### 4.2 Variables de Entorno

| Variable | Perfil | Descripcion | Ejemplo |
|----------|--------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Ambos | Perfil activo | `local` o `render` |
| `PORT` | render | Puerto del servicio | `8085` |
| `EUREKA_URI` | render | URL del Eureka Server | `http://discovery-server-m12:8761/eureka` |
| `DATABASE_URL` | render | URL de conexion a BD | `jdbc:mysql://host:3306/videogames` |
| `DB_USERNAME` | render | Usuario de BD | `videogames_user` |
| `DB_PASSWORD` | render | Password de BD | `[secreto]` |
| `JAVA_VERSION` | render | Version de Java | `17` |

### 4.3 Mapa de Puertos

| Servicio | Local | Render |
|----------|-------|--------|
| Discovery_Server_M12 | 8761 | `${PORT}` |
| Gateway_Service_M11 | 8080 | `${PORT}` |
| User_Service_M1 | 8081 | `${PORT}` |
| Product_Service_M2 | 8082 | `${PORT}` |
| Cart_Service_M3 | 8083 | `${PORT}` |
| Order_Service_M4 | 8084 | `${PORT}` |
| Payment_Service_M5 | 8085 | `${PORT}` |
| Inventory_Service_M6 | 8086 | `${PORT}` |
| Review_Service_M7 | 8087 | `${PORT}` |
| Notification_Service_M8 | 8088 | `${PORT}` |
| Category_Service_M9 | 8089 | `${PORT}` |
| Auth_Service_M10 | 8090 | `${PORT}` |

---

## 5. Seguridad y Manejo de Errores

### 5.1 Seguridad por Roles (Cart_Service_M3)

Cart_Service_M3 es el unico servicio con seguridad completa:

- `@EnableWebSecurity` + `@EnableMethodSecurity`
- HTTP Basic Authentication
- `SessionCreationPolicy.STATELESS`
- Roles: `ADMIN`, `USER`, `AGENT`
- `@PreAuthorize("@cartSecurity.canEdit(#id, authentication)")` en PUT y PATCH
- `BCryptPasswordEncoder` para hashing de contrasenas
- `DataInitializer` siembra 3 usuarios de prueba en perfil H2

### 5.2 GlobalExceptionHandler

Todos los servicios (M1-M10) implementan `@RestControllerAdvice` con `GlobalExceptionHandler`:

| Excepcion | HTTP | Servicios |
|-----------|------|-----------|
| `IllegalArgumentException` | 400 | M1-M10 |
| `EntityNotFoundException` | 404 | M1-M9 |
| `MethodArgumentNotValidException` | 400 | M1-M10 |
| `Exception` (generica) | 500 | M3-M10 |
| `PaymentBusinessException` | 422 | M5 |
| `DataIntegrityViolationException` | 409 | M9, M10 |
| `BadCredentialsException` | 401 | M3 |
| `AccessDeniedException` | 403 | M3 |
| `BadRequestException` | 400 | M3 |

### 5.3 Bean Validation (JSR 380)

Anotaciones Jakarta utilizadas en DTOs de entrada:

| Anotacion | Servicios | Uso |
|-----------|-----------|-----|
| `@NotNull` | M1-M10 | Campo obligatorio |
| `@NotBlank` | M1-M10 | String no vacio |
| `@Positive` | M5 | Numero positivo |
| `@DecimalMin` | M5 | Valor minimo decimal |
| `@Min` / `@Max` | M2, M7 | Rango numerico |
| `@Size` | M7, M8, M10 | Longitud de string |
| `@Email` | M1, M3, M8, M10 | Formato de correo |

---

## 6. Pruebas y Logs

### 6.1 Estrategia de Pruebas

Cada servicio incluye pruebas unitarias en `src/test/java` usando:

- **JUnit 5** como framework de pruebas
- **Mockito** para mockear repositorios y dependencias
- Estructura Given-When-Then con comentarios

### 6.2 Cobertura por Servicio

| Servicio | Archivo de Test | Tests Unitarios | Total |
|----------|----------------|----------------|-------|
| User_Service_M1 | `UserServiceTest` | 4 | 5 |
| Product_Service_M2 | `ProductServiceTest` | 4 | 5 |
| Cart_Service_M3 | `CartServiceTest` | 4 | 5 |
| Order_Service_M4 | `OrderServiceTest` | 4 | 5 |
| Payment_Service_M5 | `PaymentServiceTest` | 8 | 9 |
| Inventory_Service_M6 | `InventoryServiceTest` | 4 | 5 |
| Review_Service_M7 | `ReviewServiceTest` | 7 | 8 |
| Notification_Service_M8 | `NotificationServiceTest` | 4 | 5 |
| Category_Service_M9 | `CategoryServiceTest` | 7 | 8 |
| Auth_Service_M10 | `AuthServiceTest` | 4 | 5 |
| Gateway_Service_M11 | `GatewayServiceM11ApplicationTests` | 0 | 1 |
| Discovery_Server_M12 | `DiscoveryServerM12ApplicationTests` | 0 | 1 |
| **TOTAL** | | **50** | **62** |

### 6.3 Configuracion de Logs

Cada servicio configura logs en `application.yml`:

```yaml
logging:
  level:
    root: INFO
    cl.duoc.fullstack.{service_name}: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%-5level] %logger{0} - %msg%n"
  file:
    name: logs/{service-name}.log
```

---

## 7. Estructura del Repositorio

```
desarrollo_fullstack_evaluacion_final_transversal/
|
|-- docs/
|   |-- matriz-requerimientos.md
|   |-- plan-cierre-feedback.md
|   |-- documentacion-funcional.md
|   |-- documentacion-tecnica.md
|
|-- Discovery_Server_M12/
|   |-- pom.xml
|   |-- Dockerfile
|   |-- src/main/java/.../DiscoveryServerM12Application.java
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../DiscoveryServerM12ApplicationTests.java
|
|-- Gateway_Service_M11/
|   |-- pom.xml
|   |-- src/main/java/.../GatewayServiceM11Application.java
|   |-- src/main/resources/application.yml
|
|-- User_Service_M1/
|   |-- pom.xml
|   |-- Dockerfile
|   |-- src/main/java/.../
|   |   |-- controller/UserController.java
|   |   |-- service/UserService.java
|   |   |-- repository/UserRepository.java
|   |   |-- model/User.java
|   |   |-- config/SecurityConfig.java
|   |   |-- config/GlobalExceptionHandler.java
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../UserServiceTest.java
|
|-- Product_Service_M2/
|   |-- pom.xml
|   |-- Dockerfile
|   |-- src/main/java/.../
|   |   |-- controller/ProductController.java
|   |   |-- service/ProductService.java
|   |   |-- repository/ProductRepository.java
|   |   |-- model/Product.java
|   |   |-- config/GlobalExceptionHandler.java
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../ProductServiceTest.java
|
|-- Cart_Service_M3/
|   |-- pom.xml
|   |-- Dockerfile
|   |-- src/main/java/.../
|   |   |-- controller/CartController.java
|   |   |-- controller/UserController.java
|   |   |-- service/CartService.java
|   |   |-- service/UserService.java
|   |   |-- repository/CartRepository.java
|   |   |-- repository/CartHistoryRepository.java
|   |   |-- repository/UserRepository.java
|   |   |-- model/CartItem.java
|   |   |-- model/CartHistory.java
|   |   |-- model/User.java
|   |   |-- dto/CartItemRequest.java
|   |   |-- dto/CartItemCommand.java
|   |   |-- dto/CartItemResponse.java
|   |   |-- dto/CartItemResult.java
|   |   |-- dto/AssignUserRequest.java
|   |   |-- dto/UserCreateDTO.java
|   |   |-- client/NotificationClient.java
|   |   |-- exception/BadRequestException.java
|   |   |-- config/SecurityConfig.java
|   |   |-- config/GlobalExceptionHandler.java
|   |   |-- config/RestClientConfig.java
|   |   |-- config/DataInitializer.java
|   |   |-- config/CustomUserDetailsService.java
|   |   |-- config/CartSecurity.java
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../CartServiceTest.java
|
|-- Order_Service_M4/
|   |-- pom.xml, Dockerfile
|   |-- src/main/java/.../ (OrderController, OrderService, Order, OrderRequest, OrderResponse, RestTemplateConfig)
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../OrderServiceTest.java
|
|-- Payment_Service_M5/
|   |-- pom.xml, Dockerfile
|   |-- src/main/java/.../ (PaymentController, PaymentService, Payment, PaymentMethod, PaymentStatus, PaymentRequest, PaymentResponse, PaymentBusinessException)
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../PaymentServiceTest.java
|
|-- Inventory_Service_M6/
|   |-- pom.xml, Dockerfile
|   |-- src/main/java/.../ (InventoryController, InventoryService, Inventory, InventoryResponse, RestTemplateConfig)
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../InventoryServiceTest.java
|
|-- Review_Service_M7/
|   |-- pom.xml, Dockerfile
|   |-- src/main/java/.../ (ReviewController, ReviewService, Review, ReviewRequest, ReviewResponse, ErrorResponse con FieldError)
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../ReviewServiceTest.java
|
|-- Notification_Service_M8/
|   |-- pom.xml, Dockerfile
|   |-- src/main/java/.../ (NotificationController, NotificationService, Notification, NotificationType, NotificationRequest, NotificationResponse)
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../NotificationServiceTest.java
|
|-- Category_Service_M9/
|   |-- pom.xml, Dockerfile
|   |-- src/main/java/.../ (CategoryController, CategoryService, Category, CategoryRequest, CategoryResponse)
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../CategoryServiceTest.java
|
|-- Auth_Service_M10/
|   |-- pom.xml, Dockerfile
|   |-- src/main/java/.../ (AuthController, AuthService, AuthUser, AuthRegisterRequest, AuthLoginRequest, AuthRegisterResponse, AuthLoginResponse)
|   |-- src/main/resources/application.yml
|   |-- src/test/java/.../AuthServiceTest.java
|
|-- render.yaml
|-- README.md
```

---

## 8. Ejecucion desde Cero

### 8.1 Requisitos Previos

| Requisito | Version Minima | Verificacion |
|-----------|---------------|--------------|
| Java JDK | 17 LTS | `java -version` |
| Maven | 3.8+ (o Maven Wrapper incluido) | `mvn -version` |
| Git | 2.x+ | `git --version` |
| Git Bash / Terminal | Cualquier shell | — |

> **Nota:** Docker/Docker Compose es opcional. El proyecto incluye Dockerfiles multi-stage pero puede ejecutarse directamente con Maven.

### 8.2 Configuracion del Entorno Local

No se requiere archivo `.env` para desarrollo local. Todos los servicios usan configuracion hardcoded en el perfil `local` de `application.yml`:

- Base de datos H2 en memoria (sin instalacion externa)
- Puertos fijos (8080-8090)
- Eureka en `http://localhost:8761/eureka`

### 8.3 Clonar el Repositorio

```bash
git clone https://github.com/jonasvalenciat/desarrollo_fullstack_evaluacion_final_transversal.git
cd desarrollo_fullstack_evaluacion_final_transversal
```

### 8.4 Orden Exacto de Arranque

> **CRITICO:** El Discovery Server (Eureka) debe iniciar PRIMERO. Todos los demas servicios intentan registrarse en el al arrancar.

#### Paso 1 — Discovery Server (Eureka) — Puerto 8761

**Linux/macOS:**
```bash
cd Discovery_Server_M12
chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Windows (PowerShell):**
```cmd
cd Discovery_Server_M12
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

**Verificacion:** Abrir http://localhost:8761 — el panel de Eureka debe mostrarse indicando que no hay instancias registradas aun.

#### Paso 2 — API Gateway — Puerto 8080

**Linux/macOS:**
```bash
cd Gateway_Service_M11
chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Windows (PowerShell):**
```cmd
cd Gateway_Service_M11
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

**Verificacion:** Abrir http://localhost:8080 — no retornara pagina, pero el Gateway esta activo.

#### Paso 3 — Microservicios de Negocio (en cualquier orden)

Abrir una terminal separada para cada servicio:

**User_Service_M1 (Puerto 8081):**
```bash
# Linux/macOS
cd User_Service_M1 && chmod +x mvnw && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Windows
cd User_Service_M1 && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

**Product_Service_M2 (Puerto 8082):**
```bash
cd Product_Service_M2 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Cart_Service_M3 (Puerto 8083):**
```bash
cd Cart_Service_M3 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Order_Service_M4 (Puerto 8084):**
```bash
cd Order_Service_M4 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Payment_Service_M5 (Puerto 8085):**
```bash
cd Payment_Service_M5 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Inventory_Service_M6 (Puerto 8086):**
```bash
cd Inventory_Service_M6 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Review_Service_M7 (Puerto 8087):**
```bash
cd Review_Service_M7 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Notification_Service_M8 (Puerto 8088):**
```bash
cd Notification_Service_M8 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Category_Service_M9 (Puerto 8089):**
```bash
cd Category_Service_M9 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Auth_Service_M10 (Puerto 8090):**
```bash
cd Auth_Service_M10 && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 8.5 Verificacion del Sistema

**1. Verificar Eureka:**
Abrir http://localhost:8761 — todos los servicios deben aparecer con status **UP**.

**2. Verificar Gateway + Swagger:**
```
http://localhost:8080/swagger-ui.html    (no aplica — Gateway no expone Swagger)
```

Los Swagger UI de cada servicio se acceden directamente por su puerto:
```
http://localhost:8081/swagger-ui.html    (User M1)
http://localhost:8082/swagger-ui.html    (Product M2)
http://localhost:8083/swagger-ui.html    (Cart M3)
http://localhost:8084/swagger-ui.html    (Order M4)
http://localhost:8085/swagger-ui.html    (Payment M5)
http://localhost:8086/swagger-ui.html    (Inventory M6)
http://localhost:8087/swagger-ui.html    (Review M7)
http://localhost:8088/swagger-ui.html    (Notification M8)
http://localhost:8089/swagger-ui.html    (Category M9)
http://localhost:8090/swagger-ui.html    (Auth M10)
```

**3. Probar via Gateway:**
```bash
# Listar productos
curl http://localhost:8080/api/v1/products

# Crear pago (ejemplo)
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":1,"amount":59.99,"paymentMethod":"CREDIT_CARD"}'
```

### 8.6 Ejecucion de Pruebas Unitarias

**Un servicio:**
```bash
cd Payment_Service_M5
./mvnw clean test           # Linux/macOS
mvnw.cmd clean test          # Windows
```

**Todos los servicios (Linux/macOS):**
```bash
for dir in Discovery_Server_M12 User_Service_M1 Product_Service_M2 \
           Cart_Service_M3 Order_Service_M4 Payment_Service_M5 \
           Inventory_Service_M6 Review_Service_M7 Notification_Service_M8 \
           Category_Service_M9 Auth_Service_M10 Gateway_Service_M11; do
  echo "=== $dir ==="
  cd $dir && ./mvnw clean test && cd ..
done
```

**Todos los servicios (Windows PowerShell):**
```powershell
$dirs = @(
  "Discovery_Server_M12","User_Service_M1","Product_Service_M2",
  "Cart_Service_M3","Order_Service_M4","Payment_Service_M5",
  "Inventory_Service_M6","Review_Service_M7","Notification_Service_M8",
  "Category_Service_M9","Auth_Service_M10","Gateway_Service_M11"
)
foreach ($d in $dirs) {
  Write-Host "=== $d ===" -ForegroundColor Cyan
  Set-Location $d
  .\mvnw.cmd clean test
  Set-Location ..
}
```

**Salida esperada:**
```
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 8.7 Despliegue en Render

El archivo `render.yaml` automatiza el despliegue de los 12 servicios + 1 base de datos MySQL:

1. Vincular el repositorio GitHub en Render
2. Render detecta `render.yaml` automaticamente
3. `discovery-server-m12` se despliega primero (dependencia raiz)
4. Los demas servicios esperan a que Eureka este activo (`dependsOn`)
5. `gateway-service-m11` se despliega ultimo

**Variables configuradas automaticamente:** PORT, EUREKA_URI, DATABASE_URL, DB_USERNAME, DB_PASSWORD.

---

> **Duoc UC — Evaluacion Final Transversal 2025**
