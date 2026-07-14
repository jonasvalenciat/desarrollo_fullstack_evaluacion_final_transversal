# Matriz de Requerimientos

> **Proyecto:** GameVerse — Tienda de Videojuegos Online
>
> **Asignatura:** DSY1103 — Desarrollo Full Stack
>
> **Fecha de generación:** 2026-07-13

---

## Matriz de Trazabilidad

| ID | Requerimiento declarado por el equipo | Tipo | Estado | Endpoint o evidencia | Prueba asociada |
|----|--------------------------------------|------|--------|---------------------|-----------------|
| R01 | Servicio de descubrimiento para registro y localización de microservicios | Infraestructura | Agregado por feedback docente | `GET http://localhost:8761` — Panel Eureka | `DiscoveryServerM12ApplicationTests.contextLoads` |
| R02 | API Gateway como punto de entrada único de la plataforma | Infraestructura | Agregado por feedback docente | `http://localhost:8080/api/v1/{recurso}/**` — Spring Cloud Gateway con StripPrefix=2 y rutas `lb://` en perfil render | `GatewayServiceM11ApplicationTests.contextLoads` |
| R03 | Registro de usuarios con validación de email único y encriptación de contraseña | Negocio | Estándarizado | `POST /api/v1/users` | `UserServiceTest.createUser_whenValidUser_shouldHashPasswordAndSave`, `UserServiceTest.createUser_whenDuplicateEmail_shouldThrowException` |
| R04 | Consulta de usuario por ID | Negocio | Estándarizado | `GET /api/v1/users/{id}` | `UserServiceTest.getUserById_whenUserExists_shouldReturnUser`, `UserServiceTest.getUserById_whenUserNotFound_shouldThrowEntityNotFoundException` |
| R05 | CRUD completo de productos (crear, listar, actualizar, eliminar) | Negocio | Estándarizado | `GET /api/v1/products`, `GET /api/v1/products/{id}`, `POST /api/v1/products`, `PUT /api/v1/products/{id}`, `DELETE /api/v1/products/{id}` | `ProductServiceTest.createProduct_whenValidProduct_shouldSaveAndReturn`, `ProductServiceTest.getProductById_whenProductExists_shouldReturnProduct`, `ProductServiceTest.getProductById_whenProductNotFound_shouldThrowEntityNotFoundException` |
| R06 | Agregar items al carrito con validación de duplicados y existencia de usuario | Negocio | Estándarizado | `POST /api/v1/cart/add` | `CartServiceTest.addToCart_WithValidCommand_ShouldReturnCartItemResult`, `CartServiceTest.addToCart_WithDuplicateProduct_ShouldThrowException`, `CartServiceTest.addToCart_WithNonExistentUser_ShouldThrowBadRequestException` |
| R07 | Regla de negocio: coupon code no puede ser igual al email del usuario | Negocio | Estándarizado | `POST /api/v1/cart/add` | `CartServiceTest.addToCart_WithCouponCodeEqualToUserEmail_ShouldThrowException` |
| R08 | Seguridad por roles (ADMIN, USER, AGENT) con HTTP Basic y @PreAuthorize | Transversal | Estándarizado | `PUT /api/v1/cart/by-id/{id}` — `@PreAuthorize("@cartSecurity.canEdit(#id, authentication)")`, `GET /api/v1/cart/by-id/{id}/history` — Solo ADMIN | `CartServiceTest` (ejecución con contexto de seguridad activo) |
| R09 | Gestión de usuarios internos del carrito con roles | Negocio | Estándarizado | `GET /api/v1/users` (Admin), `GET /api/v1/users/{id}` (Admin), `POST /api/v1/users` (Admin) | `CartServiceTest` (integración con UserService interno) |
| R10 | Creación de órdenes con validación de existencia de producto vía inter-service | Negocio | Estándarizado | `POST /api/v1/orders` — Valida producto contra Product_Service_M2 vía RestTemplate | `OrderServiceTest.createOrder_WithValidProduct_ShouldReturnOrderResponse`, `OrderServiceTest.createOrder_WithNonExistentProduct_ShouldThrowException` |
| R11 | Consulta de orden por ID | Negocio | Estándarizado | `GET /api/v1/orders/{id}` | `OrderServiceTest.getOrderById_WhenExists_ShouldReturnOrderResponse`, `OrderServiceTest.getOrderById_WhenNotExists_ShouldThrowEntityNotFoundException` |
| R12 | Procesamiento de pagos con Bean Validation JSR 380 (@NotNull, @Positive, @DecimalMin) | Negocio | Agregado por feedback docente | `POST /api/v1/payments` — Validaciones en `PaymentRequest`: orderId (@NotNull, @Positive), amount (@NotNull, @DecimalMin("0.01")), paymentMethod (@NotNull) | `PaymentServiceTest.processPayment_WithValidRequest_ShouldReturnPaymentResponse`, `PaymentServiceTest.processPayment_ShouldSetStatusToSuccess`, `PaymentServiceTest.processPayment_ShouldSaveAndReturnNonNullId` |
| R13 | Regla de negocio: monto máximo de transacción de $1.000.000 | Negocio | Agregado por feedback docente | `POST /api/v1/payments` — `PaymentBusinessException` con código `AMOUNT_EXCEEDS_LIMIT` y HTTP 422 | `PaymentServiceTest.processPayment_WithAmountExceedingLimit_ShouldThrowBusinessException` |
| R14 | Regla de negocio: orderId debe ser positivo y monto mayor a $0.01 | Negocio | Agregado por feedback docente | `POST /api/v1/payments` — Códigos `INVALID_ORDER_ID`, `INVALID_AMOUNT` | `PaymentServiceTest.processPayment_WithNegativeOrderId_ShouldThrowBusinessException`, `PaymentServiceTest.processPayment_WithZeroAmount_ShouldThrowBusinessException` |
| R15 | Consulta de pago por ID | Negocio | Estándarizado | `GET /api/v1/payments/{id}` | `PaymentServiceTest.getPaymentById_WhenFound_ShouldReturnResponse`, `PaymentServiceTest.getPaymentById_WhenNotFound_ShouldThrowBusinessException` |
| R16 | Actualización de stock de inventario con validación de producto vía inter-service | Negocio | Estándarizado | `PUT /api/v1/inventory/{productId}?quantity={qty}` — Valida producto contra Product_Service_M2 vía RestTemplate | `InventoryServiceTest.updateStock_WithNewProduct_ShouldCreateAndReturnStock`, `InventoryServiceTest.updateStock_WithExistingProduct_ShouldAccumulateStock`, `InventoryServiceTest.updateStock_WithNonExistentProduct_ShouldThrowException` |
| R17 | Regla de negocio: cantidades negativas decrementan el stock | Negocio | Estándarizado | `PUT /api/v1/inventory/{productId}?quantity=-5` | `InventoryServiceTest.updateStock_WithNegativeQuantity_ShouldDecreaseStock` |
| R18 | Creación de reseñas con Bean Validation JSR 380 (@Min, @Max, @Size, @NotBlank) | Negocio | Agregado por feedback docente | `POST /api/v1/reviews` — Validaciones en `ReviewRequest`: rating (@Min(1), @Max(5)), comment (@NotBlank, @Size(min=10, max=500)) | `ReviewServiceTest.createReview_WithValidRequest_ShouldReturnReviewResponse`, `ReviewServiceTest.createReview_ShouldSetAllFieldsCorrectly` |
| R19 | Regla de negocio: ID del producto debe ser positivo | Negocio | Agregado por feedback docente | `POST /api/v1/reviews` — `IllegalArgumentException` si productId < 1 | `ReviewServiceTest.createReview_WithInvalidProductId_ShouldThrowException`, `ReviewServiceTest.createReview_WithNullProductId_ShouldThrowException` |
| R20 | Actualización de reseñas existentes | Negocio | Estándarizado | `PUT /api/v1/reviews/{id}` | `ReviewServiceTest.updateReview_WithValidRequest_ShouldReturnUpdatedReview`, `ReviewServiceTest.updateReview_WithNonExistentId_ShouldThrowException` |
| R21 | Consulta de reseñas por producto | Negocio | Estándarizado | `GET /api/v1/reviews/product/{productId}` | `ReviewServiceTest.getReviewsByProduct_ShouldReturnListOfReviews` |
| R22 | Envío de notificaciones con Bean Validation JSR 380 (@Email, @Size) | Negocio | Agregado por feedback docente | `POST /api/v1/notifications` — Validaciones en `NotificationRequest`: recipient (@NotBlank, @Email), message (@NotBlank, @Size(min=5, max=1000)) | `NotificationServiceTest.sendNotification_WithValidRequest_ShouldReturnNotificationResponse`, `NotificationServiceTest.sendNotification_ShouldSetAllFieldsCorrectly` |
| R23 | Consulta de notificaciones por usuario | Negocio | Estándarizado | `GET /api/v1/notifications/user/{userId}` | `NotificationServiceTest.getNotificationsByUser_ShouldReturnList`, `NotificationServiceTest.getNotificationsByUser_WithNoNotifications_ShouldReturnEmptyList` |
| R24 | CRUD completo de categorías con protección de integridad (unique constraint) | Negocio | Estándarizado | `POST /api/v1/categories`, `GET /api/v1/categories`, `GET /api/v1/categories/{id}`, `PUT /api/v1/categories/{id}`, `DELETE /api/v1/categories/{id}` | `CategoryServiceTest.createCategory_ShouldReturnCategoryResponse`, `CategoryServiceTest.getCategoryById_WhenExists_ShouldReturnCategory`, `CategoryServiceTest.getCategoryById_WhenNotExists_ShouldThrowException`, `CategoryServiceTest.deleteCategory_WhenNotExists_ShouldThrowException` |
| R25 | Autenticación de usuarios con registro y login | Negocio | Estándarizado | `POST /api/v1/auth/register` — Registro con `@Size(min=3, max=50)` en username, `POST /api/v1/auth/login` — Retorna token mock | `AuthServiceTest.register_ShouldReturnResponseWithoutPassword`, `AuthServiceTest.login_WithValidCredentials_ShouldReturnToken`, `AuthServiceTest.login_WithInvalidUsername_ShouldThrowException`, `AuthServiceTest.login_WithWrongPassword_ShouldThrowException` |
| R26 | Manejo centralizado de excepciones con @RestControllerAdvice en todos los servicios | Transversal | Estándarizado | `GlobalExceptionHandler` en cada servicio: `IllegalArgumentException` → 400, `EntityNotFoundException` → 404, `MethodArgumentNotValidException` → 400, `PaymentBusinessException` → 422 (M5), `DataIntegrityViolationException` → 409 (M9, M10) | Verificados en las pruebas que lanzan excepciones de cada servicio |
| R27 | Documentación API viva con Springdoc OpenAPI (Swagger UI) | Transversal | Estándarizado | `http://localhost:{puerto}/swagger-ui.html` — Anotaciones `@Tag`, `@Operation`, `@ApiResponses` en cada controller | Verificado visualmente en cada servicio |
| R28 | Configuración YAML con perfiles local/render para separar desarrollo y producción | Transversal | Agregado por feedback docente | `application.yml` con `on-profile: local` (H2, puertos fijos) y `on-profile: render` (MySQL, ${PORT}, ${EUREKA_URI}) | Configuración verificada en los 12 servicios |
| R29 | Despliegue automatizado en Render con render.yaml | Infraestructura | Agregado por feedback docente | `render.yaml` — 12 servicios web + 1 base de datos MySQL con `dependsOn` para orden de despliegue | Archivo `render.yaml` en raíz del repositorio |
| R30 | Contenedores Docker multi-stage con Alpine Linux | Transversal | Estándarizado | `Dockerfile` en cada servicio: Stage 1 `maven:3.9.6-eclipse-temurin-17-alpine` (build), Stage 2 `eclipse-temurin:17-jre-alpine` (runtime) | Dockerfiles verificados en M1–M10 y M12 |
| R31 | Comunicación entre servicios vía RestTemplate / RestClient | Transversal | Estándarizado | Order_M4 → Product_M2 (`RestTemplate`), Inventory_M6 → Product_M2 (`RestTemplate`), Cart_M3 → Notification_M8 (`RestClient`) | `OrderServiceTest.createOrder_WithNonExistentProduct_ShouldThrowException`, `InventoryServiceTest.updateStock_WithNonExistentProduct_ShouldThrowException` |

---

## Resumen de Cobertura

| Categoría | Total | Detalle |
|-----------|-------|---------|
| Requerimientos de Negocio | 23 | R03–R07, R09–R17, R18–R25 |
| Requerimientos de Infraestructura | 4 | R01, R02, R29, R30 |
| Requerimientos Transversales | 4 | R08, R26, R27, R28, R31 |
| **Total** | **31** | |
| Pruebas unitarias totales | 62 | 50 unitarias + 12 contextLoads |
| Servicios con Bean Validation robustecido | 3 | M5 (Payment), M7 (Review), M8 (Notification) |
| Servicios con GlobalExceptionHandler | 10 | M1–M10 |
| Servicios registrados en Eureka | 11 | M1–M11 |
