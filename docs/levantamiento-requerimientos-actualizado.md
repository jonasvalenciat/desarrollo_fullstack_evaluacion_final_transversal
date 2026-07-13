# Levantamiento de Requerimientos Actualizado — GameVerse

> **Proyecto:** GameVerse — Tienda de Videojuegos Online
>
> **Asignatura:** DSY1103 — Desarrollo Full Stack
>
> **Versión:** 1.1 — Actualización documental 2026 (hitos 2025)
>
> **Sección de Rúbrica:** 4.2.6 — Levantamiento de Requerimientos Actualizado

---

## 1. Contexto del Levantamiento

Este documento registra la **evolución real** de los requerimientos funcionales del proyecto GameVerse, desde su planteamiento inicial hasta su cierre semestral. Incluye los requerimientos que se mantuvieron, los que se ajustaron tras la retroalimentación intermedia, los que se descartaron por alcance y los que se incorporaron para cumplir los objetivos arquitectónicos de despliegue en la nube.

> **Fecha de creación:** Junio 2025 (hito histórico)
>
> **Fecha de actualización:** Julio 2025 (cierre semestral, hito histórico)
>
> **Fuente de retroalimentación:** Feedback intermedio del profesor + autoevaluación del equipo

---

## 2. Tabla Principal de Evolución de Requerimientos

| ID | Requerimiento Original | Cambio Realizado | Justificación | Estado Final | Evidencia en Repositorio |
|----|----------------------|------------------|---------------|-------------|--------------------------|
| RF-01 | Gestion de Videojuegos / Catalogo de productos | Se mantuvo y se implemento con persistencia completa (CRUD) y validacion de datos | El requerimiento era solido desde el inicio. Se mantuvo sin cambios significativos y se implemento con Spring Data JPA + Bean Validation. Incluye creacion, lectura, actualizacion y eliminacion de productos. | **Mantenido** | `Product_Service_M2/src/main/java/.../controller/ProductController.java`, `Product.java`, `ProductServiceTest.java` |
| RF-02 | Autenticacion de Usuarios | Se mantuvo incorporando roles basicos (ADMIN, USER,_AGENT) y autenticacion por contrasena hasheada con BCrypt | El alcance original de autenticacion con JWT se ajusto a un enfoque simplificado pero funcional: BCrypt para hashing, roles basicos, y login con token mock. Se priorizo la funcionalidad sobre la complejidad del token. | **Mantenido** | `User_Service_M1/src/main/java/.../controller/UserController.java`, `Auth_Service_M10/src/main/java/.../controller/AuthController.java`, `UserServiceTest.java`, `AuthServiceTest.java` |
| RF-03 | Pasarela de Pagos (Servicio de Pagos) | Se modifico para robustecer las reglas de negocio con validaciones JSR 380 y manejo de excepciones de negocio personalizadas | Tras el feedback intermedio, se identifico que el servicio necesitaba validaciones mas estrictas: `@DecimalMin("0.01")`, `@Positive`, limiter de transaccion a $1.000.000 CLP, y excepcion custom `PaymentBusinessException` con HTTP 422 para violaciones de reglas de negocio. | **Modificado** | `Payment_Service_M5/src/main/java/.../controller/PaymentController.java`, `PaymentBusinessException.java`, `PaymentServiceTest.java` (8 tests unitarios con escenarios de error) |
| RF-04 | Notificaciones Externas por SMS/Email/Push | Se elimino el alcance externo (integracion con proveedores reales) y se centralizo en un microservicio interno de registro de eventos | El equipo determino que integrar con servicios externos de notificacion (Twilio, SendGrid) excedia el alcance y los tiempos del semestre. Se rediseño como un microservicio interno (Notification_Service_M8) que registra y almacena notificaciones, pero no las envia a servicios externos. Esto mantiene la arquitectura modular sin dependencias de APIs de terceros. | **Eliminado** (alcance externo) | Registro en `docs/matriz-requerimientos.md` (Estado: Eliminado), decision documentada en `docs/plan-cierre-feedback.md` (Item 4: Eliminacion de integracion externa) |
| RF-05 | Ecosistema Unificado con API Gateway y Service Discovery | Se agrego como nuevo requerimiento para cumplir con el diseno arquitectonico de produccion en la nube | El feedback del profesor indico la necesidad de un punto de entrada unico (Gateway) y un servidor de descubrimiento (Eureka) para que los microservicios pudieran comunicarse en un entorno de produccion real. Se creo `Discovery_Server_M12` como Eureka Server y `Gateway_Service_M11` como Spring Cloud Gateway con 10 rutas configuradas. | **Agregado** | `Discovery_Server_M12/` (Eureka Server, puerto 8761), `Gateway_Service_M11/` (10 rutas StripPrefix=2, perfil render con lb://), `render.yaml` (orquestacion con dependsOn) |
| RF-06 | Gestion de Categorias de Videojuegos | Se mantuvo con persistencia y validacion de nombre unico | Requerimiento del catalogo que se implemento con validacion `@Column(unique=true)` y manejo de `DataIntegrityViolationException` (HTTP 409) para duplicados. Sin cambios significativos desde el planteamiento original. | **Mantenido** | `Category_Service_M9/src/main/java/.../controller/CategoryController.java`, `Category.java`, `CategoryServiceTest.java` |
| RF-07 | Sistema de Resenas y Calificaciones | Se mantuvo con validacion estricta de rating (1-5) y comentario (10-500 chars) | Requerimiento estable. Se implemento con Bean Validation `@Min(1) @Max(5)` para rating y `@Size(min=10, max=500)` para comentario. Incluye `ErrorResponse.FieldError` estructurado para errores de validacion. | **Mantenido** | `Review_Service_M7/src/main/java/.../controller/ReviewController.java`, `Review.java`, `ErrorResponse.java`, `ReviewServiceTest.java` |
| RF-08 | Gestion de Inventario/Stock | Se mantuvo con validacion de producto existente via RestTemplate | Requerimiento que depende de Product_Service_M2. Se implemento con comunicacion sincrona via `RestTemplate` para validar que el producto exista antes de actualizar stock. Soporta decremento con cantidades negativas. | **Mantenido** | `Inventory_Service_M6/src/main/java/.../controller/InventoryController.java`, `InventoryService.java`, `RestTemplateConfig.java`, `InventoryServiceTest.java` |
| RF-09 | Gestion de Carrito de Compras | Se mantuvo con seguridad por roles (ADMIN/USER/AGENT) y registro de historial | Requerimiento que evoluciono significativamente en complejidad: se agrego seguridad HTTP Basic con `@EnableMethodSecurity`, `@PreAuthorize` para edicion por propietario, historial de cambios (`CartHistory`), y notificacion al agregar items via `RestClient`. | **Mantenido** | `Cart_Service_M3/src/main/java/.../controller/CartController.java`, `CartSecurity.java`, `SecurityConfig.java`, `CartHistory.java`, `CartServiceTest.java` |
| RF-10 | Gestion de Pedidos/Ordenes | Se mantuvo con validacion de producto existente via RestTemplate | Similar a Inventory, valida existencia del producto antes de crear la orden. Estado inicial: CREATED. Comunicacion sincrona con Product_Service_M2. | **Mantenido** | `Order_Service_M4/src/main/java/.../controller/OrderController.java`, `OrderService.java`, `RestTemplateConfig.java`, `OrderServiceTest.java` |

---

## 3. Resumen de Cambios

### 3.1 Requerimientos Mantenidos (5)

| ID | Servicio | Cambio |
|----|----------|--------|
| RF-01 | Product_Service_M2 | Sin cambios significativos |
| RF-02 | User_Service_M1 + Auth_Service_M10 | Sin cambios significativos |
| RF-06 | Category_Service_M9 | Sin cambios significativos |
| RF-07 | Review_Service_M7 | Sin cambios significativos |
| RF-08 | Inventory_Service_M6 | Sin cambios significativos |
| RF-09 | Cart_Service_M3 | Sin cambios significativos |
| RF-10 | Order_Service_M4 | Sin cambios significativos |

### 3.2 Requerimientos Modificados (1)

| ID | Servicio | Cambio Aplicado |
|----|----------|-----------------|
| RF-03 | Payment_Service_M5 | Validaciones JSR 380 reforzadas + `PaymentBusinessException` + HTTP 422 |

### 3.3 Requerimientos Eliminados (1)

| ID | Justificacion |
|----|---------------|
| RF-04 | Integracion externa con proveedores de notificacion (Twilio/SendGrid) excedia el alcance semestral. Se mantuvo el microservicio interno de registro. |

### 3.4 Requerimientos Agregados (1)

| ID | Justificacion |
|----|---------------|
| RF-05 | Necesidad de un punto de entrada unico (Gateway) y descubrimiento de servicios (Eureka) para arquitectura de produccion en Render. |

---

## 4. Matriz de Trazabilidad

| Requerimiento | Servicio Asociado | Endpoint Principal | Tests Unitarios | Estado |
|---------------|-------------------|-------------------|-----------------|--------|
| RF-01 | Product_Service_M2 | `GET/POST/PUT/DELETE /api/v1/products` | ProductServiceTest (4+) | Implementado |
| RF-02 | User_Service_M1 + Auth_Service_M10 | `POST /api/v1/users`, `POST /api/v1/auth/login` | UserServiceTest (4+), AuthServiceTest (4+) | Implementado |
| RF-03 | Payment_Service_M5 | `POST /api/v1/payments` | PaymentServiceTest (8) | Implementado |
| RF-04 | Notification_Service_M8 | `POST /api/v1/notifications` | NotificationServiceTest (4+) | Implementado (interno) |
| RF-05 | Gateway_Service_M11 + Discovery_Server_M12 | `http://localhost:8080/api/v1/*` (rutas) | GatewayServiceM11ApplicationTests, DiscoveryServerM12ApplicationTests | Implementado |
| RF-06 | Category_Service_M9 | `GET/POST/PUT/DELETE /api/v1/categories` | CategoryServiceTest (7) | Implementado |
| RF-07 | Review_Service_M7 | `POST /api/v1/reviews` | ReviewServiceTest (7) | Implementado |
| RF-08 | Inventory_Service_M6 | `POST /api/v1/inventory` | InventoryServiceTest (4+) | Implementado |
| RF-09 | Cart_Service_M3 | `GET/POST/PATCH/DELETE /api/v1/cart` | CartServiceTest (4+) | Implementado |
| RF-10 | Order_Service_M4 | `POST /api/v1/orders` | OrderServiceTest (4+) | Implementado |

---

## 5. Decisiones Clave del Equipo (registro histórico 2025)

| Decisión | Fecha | Justificación |
|----------|-------|---------------|
| Eliminar integracion externa de notificaciones | Junio 2025 | Complejidad de APIs de terceros + restricciones de tiempo del semestre |
| Agregar Eureka + Gateway | Junio 2025 | Feedback del profesor: necesidad de arquitectura de producción en la nube |
| Simplificar JWT a BCrypt + token mock | Junio 2025 | Priorizar funcionalidad sobre complejidad de implementacion |
| Agregar `PaymentBusinessException` | Junio 2025 | Feedback intermedio: manejo de errores mas robusto en pagos |
| Crear CartHistory para auditoria | Junio 2025 | Necesidad de trazabilidad en cambios del carrito |

---

> **Duoc UC — Evaluación Final Transversal (hitos 2025, actualizado 2026)**
