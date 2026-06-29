# Plataforma de Microservicios — Estandarización Técnica

> **Duoc UC — Evaluación 3 / Full Stack**
>
> Estandarización de 10 microservicios Spring Boot a una base técnica común:
> Java 17, Spring Boot 3.4.4, Swagger con `springdoc-openapi`, DTOs,
> manejo global de excepciones, pruebas unitarias con Mockito y
> contenedorización multi-stage con Alpine Linux.

---

## 1. Justificación Técnica

### 1.1 Java 17 en lugar de Java 21

Si bien Java 21 es la versión LTS más reciente, se optó por **Java 17**
por las siguientes razones:

- **Compatibilidad global probada**: Spring Boot 3.x tiene soporte
  oficial y maduro para Java 17; el ecosistema de libraries
  (Lombok, MapStruct, Testcontainers) está extensivamente validado
  sobre esta versión.
- **Disponibilidad en entornos productivos**: La mayoría de los
  proveedores cloud (AWS ECS, Azure Spring Apps, GCP Cloud Run) y
  contenedores base (`eclipse-temurin:17-jre-alpine`) tienen imágenes
  Java 17 ampliamente adoptadas y optimizadas.
- **Estabilidad del tooling**: Maven wrapper y el plugin de
  compilación (`maven-compiler-plugin`) presentan un comportamiento
  predecible con Java 17, evitando problemas de
  `annotationProcessorPaths` observados con `<release>21</release>`.

### 1.2 Spring Boot 3.4.4 en lugar de 4.0.6

El `pom.xml` original declaraba `spring-boot-starter-parent:4.0.6`,
una versión que no corresponde a ninguna release oficial de Spring
Boot (la línea 3.x es la actual activa). Spring Boot 4.x **no existe
como versión estable**. Además, dicha declaración arrastraba starters
inexistentes como:

| Artifact inválido (SB 4.0.6) | Reemplazo correcto |
|---|---|
| `spring-boot-starter-webmvc` | `spring-boot-starter-web` |
| `spring-boot-h2console` | Eliminado (se auto-configura con H2 en classpath) |
| `spring-boot-starter-webmvc-test` | `spring-boot-starter-test` |
| `spring-boot-starter-data-jpa-test` | `spring-boot-starter-test` |
| `spring-boot-starter-validation-test` | `spring-boot-starter-test` |
| `spring-boot-starter-security-test` | Eliminado (no necesario en servicios sin auth) |

Se migró a **Spring Boot 3.4.4**, la versión más reciente y estable
de la línea 3.x, con soporte completo para Java 17 y el ecosistema
`springdoc-openapi` 2.8.6.

---

## 2. Cumplimiento de la Rúbrica

### 2.1 Configuración YAML (IE 3.3.4) ✅

Todos los microservicios refactorizados reemplazaron el plano
`application.properties` por **YAML estructurado** con dos perfiles:

```yaml
# application.yml — configuración base
spring:
  application:
    name: order-service
  profiles:
    active: h2

server:
  port: 8083

logging:
  level:
    cl.duoc.fullstack.order_service_m4: DEBUG
```

```yaml
# application-h2.yml — perfil de desarrollo
spring:
  datasource:
    url: jdbc:h2:mem:order_db
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    open-in-view: false
  h2:
    console:
      enabled: true
```

La separación por perfiles permite activar `h2` en desarrollo y
preparar un perfil `mysql` para producción sin modificar la
configuración base.

### 2.2 Protección de Datos con DTOs (IE 3.3.4) ✅

Cada microservicio implementa **tres capas de DTOs** que aislan
completamente la entidad JPA de la capa de presentación:

- `*Request.java` — DTO de entrada con validaciones Jakarta
  (`@NotBlank`, `@Size`, etc.)
- `*Response.java` — DTO de salida que expone solo los campos
  necesarios (nunca contraseñas, datos internos ni relaciones completas)
- `ErrorResponse.java` — `record` inmutable con un único campo
  `message` para todas las respuestas de error

**Ejemplo (AuthService)**:
```java
// Entidad JPA (NUNCA expuesta al exterior)
@Entity
@Table(name = "auth_users")
public class AuthUser {
    @Id private Long id;
    private String username;
    private String password; // ← oculto en el Response
    private String role;
}

// DTO de salida (SIN password)
public class AuthRegisterResponse {
    private Long id;
    private String username;
    private String role;
}
```

### 2.3 Documentación Viva con Swagger (IE 3.2.1) ✅

Cada `@RestController` está anotado con `@Tag`, `@Operation` y
`@ApiResponses` de `springdoc-openapi`:

```java
@RestController
@RequestMapping("/orders")
@Tag(name = "Órdenes", description = "API para gestión de órdenes de compra")
public class OrderController {

    @PostMapping
    @Operation(summary = "Crear una nueva orden")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Orden creada",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) { ... }
}
```

La UI de Swagger está disponible en `http://localhost:{puerto}/swagger-ui.html`.

### 2.4 Manejo Global de Excepciones (IE 2.4.1) ✅

Todos los servicios implementan `@RestControllerAdvice` con un
`GlobalExceptionHandler` que captura y normaliza las excepciones:

| Excepción | Código HTTP | Mensaje |
|---|---|---|
| `EntityNotFoundException` | 404 | "Categoría no encontrada con ID: X" |
| `IllegalArgumentException` | 400 / 401 | Mensaje descriptivo |
| `MethodArgumentNotValidException` | 400 | Errores de validación concatenados |
| `DataIntegrityViolationException` | 409 | "El nombre ya existe" |
| `Exception` (fallback) | 500 | "Error interno del servidor" |

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }
    // ... resto de handlers
}
```

### 2.5 Pruebas Unitarias con // Given — // When — // Then (IE 3.1.1) ✅

Cada microservicio refactorizado incluye **al menos 4 pruebas
unitarias** sobre la capa de servicio usando **Mockito** y la
estructura estricta de comentarios:

```java
@Test
void getOrderById_WhenExists_ShouldReturnOrder() {
    // Given
    Order order = new Order(1L, 1L, ...);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    // When
    OrderResponse response = orderService.getOrderById(1L);

    // Then
    assertNotNull(response);
    assertEquals(1L, response.getId());
    verify(orderRepository).findById(1L);
}
```

| Servicio | Tests |
|---|---|
| CartServiceTest | 4 |
| OrderServiceTest | 5 |
| PaymentServiceTest | 5 |
| InventoryServiceTest | 5 |
| ReviewServiceTest | 5 |
| NotificationServiceTest | 4 |
| CategoryServiceTest | 7 |
| AuthServiceTest | 4 |

### 2.6 Dockerfiles Multi-Stage Alpine (IE 3.3.1) ✅

Cada microservicio incluye un `Dockerfile` multi-stage que minimiza
el tamaño de la imagen final:

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE {puerto}
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Stage 1 (build)**: Imagen `maven:3.9.6-eclipse-temurin-17-alpine`
  que compila el proyecto y genera el `.jar`.
- **Stage 2 (runtime)**: Imagen `eclipse-temurin:17-jre-alpine` que
  solo contiene la JRE, reduciendo drásticamente el tamaño final.

---

## 3. Mapa de Microservicios

| # | Directorio | Puerto | Artefacto |
|---|---|---|---|
| M1 | `User_Service_M1` | 8081 | `User_Service_M1` |
| M2 | `Product_Service_M2` | 8082 | `Product_Service_M2` |
| M3 | `Cart_Service_M3` | 8080 | `Cart_Service_M3` |
| M4 | `Order_Service_M4` | 8083 | `Order_Service_M4` |
| M5 | `Payment_Service_M5` | 8085 | `Payment_Service_M5` |
| M6 | `Inventory_Service_M6` | 8086 | `Inventory_Service_M6` |
| M7 | `Review_Service_M7` | 8087 | `Review_Service_M7` |
| M8 | `Notification_Service_M8` | 8088 | `Notification_Service_M8` |
| M9 | `Category_Service_M9` | 8089 | `Category_Service_M9` |
| M10 | `Auth_Service_M10` | 8090 | `Auth_Service_M10` |

---

## 4. Ejecución en Local

Cada microservicio se ejecuta de forma independiente. Asegúrate de
tener **Java 17** y **Maven** instalados (o usa el Maven wrapper
incluido).

### 4.1 Compilar y ejecutar pruebas

```bash
cd Cart_Service_M3
./mvnw clean test
```

Salida esperada:

```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 4.2 Ejecutar el servicio

```bash
cd Cart_Service_M3
./mvnw spring-boot:run
```

### 4.3 Construir imagen Docker

```bash
cd Cart_Service_M3
docker build -t cart-service-m3 .
docker run -p 8080:8080 cart-service-m3
```

### 4.4 Acceder a Swagger UI

```
http://localhost:{puerto}/swagger-ui.html
```

---

## 5. Estructura Interna Refactorizada

```
Cart_Service_M3/
├── Dockerfile
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/.../
│   │   │   ├── config/
│   │   │   │   └── GlobalExceptionHandler.java    # @RestControllerAdvice
│   │   │   ├── controller/
│   │   │   │   └── CartController.java             # @Tag + @Operation
│   │   │   ├── dto/
│   │   │   │   ├── CartRequest.java                # DTO entrada
│   │   │   │   ├── CartResponse.java               # DTO salida
│   │   │   │   └── ErrorResponse.java              # record inmutable
│   │   │   ├── model/
│   │   │   │   └── Cart.java                       # Entidad JPA
│   │   │   ├── repository/
│   │   │   │   └── CartRepository.java
│   │   │   └── service/
│   │   │       └── CartService.java
│   │   └── resources/
│   │       ├── application.yml                     # Config base YAML
│   │       └── application-h2.yml                  # Perfil H2
│   └── test/
│       └── java/.../
│           └── CartServiceTest.java                # Mockito tests
```

---

## 6. Commits de Referencia

| Módulo | Commit | Descripción |
|---|---|---|
| M3 | `5a2242a` | Migración Cart Service |
| M4 | `f67c7dc` | Migración Order Service |
| M5 | `a2e2824` | Migración Payment Service |
| M6 | `4f7c9bb` | Migración Inventory Service |
| M7 | `1574113` | Migración Review Service |
| M8 | `aebd7b5` | Migración Notification Service |
| M9 | `47e5447` | Migración Category Service |
| M10 | `d73856a` | Migración Auth Service |

---

> **Nota:** Los microservicios M1 (`User_Service_M1`) y M2
> (`Product_Service_M2`) mantienen su configuración original y no
> forman parte del alcance de esta estandarización.
