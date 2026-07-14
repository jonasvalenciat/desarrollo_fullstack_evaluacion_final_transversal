# Plan de Cierre y Resolución de Feedback

> **Proyecto:** GameVerse — Tienda de Videojuegos Online
>
> **Asignatura:** DSY1103 — Desarrollo Full Stack
>
> **Fecha de cierre:** 2026-07-13

---

## Registro de Observaciones y Acciones Implementadas

| ID | Observación o feedback recibido | Acción realizada | Archivo(s) modificados | Evidencia de verificación | Estado |
|----|--------------------------------|-----------------|----------------------|--------------------------|--------|
| FB-01 | Microservicios funcionando de forma aislada sin un ecosistema unificado. No existía punto de entrada único ni mecanismo de descubrimiento de servicios. | Implementación de **Spring Cloud Gateway** como API Gateway centralizado (`Gateway_Service_M11`, puerto 8080) con 10 rutas configuradas usando `StripPrefix=2`. Implementación de **Netflix Eureka Server** (`Discovery_Server_M12`, puerto 8761) para registro y balanceo dinámico de carga. Todos los servicios registrados como clientes Eureka con `spring-cloud-starter-netflix-eureka-client`. Rutas en perfil render actualizadas a formato `lb://` para resolver servicios por nombre vía Eureka. | `Discovery_Server_M12/` (nuevo: pom.xml, DiscoveryServerM12Application.java, application.yml, Dockerfile), `Gateway_Service_M11/pom.xml`, `Gateway_Service_M11/src/main/resources/application.yml`, `pom.xml` de los 10 servicios M1–M10 (dependencia eureka-client + Spring Cloud BOM) | `DiscoveryServerM12Application.java:8` — `@EnableEurekaServer` confirmado. `application.yml` del Gateway perfil render: 10 rutas `lb://` verificadas. Eureka Client dependency agregada en los 11 `pom.xml`. Panel Eureka disponible en `http://localhost:8761`. | **Corregido** |
| FB-02 | Validaciones débiles o ausentes en reglas de negocio críticas de pagos, reseñas y notificaciones. Sin límites de monto, sin validación de formato de email, sin control centralizado de excepciones en estos servicios. | Robustecimiento de Bean Validation (JSR 380) en **Payment_Service_M5**: `@NotNull`, `@Positive`, `@DecimalMin("0.01")` en `PaymentRequest`. Creación de `PaymentBusinessException` con códigos de error (`INVALID_ORDER_ID`, `INVALID_AMOUNT`, `AMOUNT_EXCEEDS_LIMIT`, `PAYMENT_NOT_FOUND`) y respuesta HTTP 422. Límite de transacción de $1.000.000 para detectar transacciones sospechosas. Robustecimiento en **Review_Service_M7**: `@Min(1)`, `@Max(5)` en rating, `@NotBlank`, `@Size(min=10, max=500)` en comment, `ErrorResponse` con inner record `FieldError`. Robustecimiento en **Notification_Service_M8**: `@Email` en recipient, `@Size(min=5, max=1000)` en message. `GlobalExceptionHandler` centralizado en los 3 servicios concatenando errores de campo. 19 pruebas unitarias新增adas en M5+M7+M8 verificando Happy Path y escenarios de error. | `Payment_Service_M5/src/main/java/.../dto/PaymentRequest.java`, `Payment_Service_M5/src/main/java/.../exception/PaymentBusinessException.java`, `Payment_Service_M5/src/main/java/.../service/PaymentService.java`, `Payment_Service_M5/src/main/java/.../config/GlobalExceptionHandler.java`, `Payment_Service_M5/src/test/java/.../PaymentServiceTest.java` (8 tests), `Review_Service_M7/src/main/java/.../dto/ReviewRequest.java`, `Review_Service_M7/src/main/java/.../dto/ErrorResponse.java`, `Review_Service_M7/src/main/java/.../config/GlobalExceptionHandler.java`, `Review_Service_M7/src/test/java/.../ReviewServiceTest.java` (7 tests), `Notification_Service_M8/src/main/java/.../dto/NotificationRequest.java`, `Notification_Service_M8/src/main/java/.../config/GlobalExceptionHandler.java`, `Notification_Service_M8/src/test/java/.../NotificationServiceTest.java` (4 tests) | `PaymentServiceTest.processPayment_WithAmountExceedingLimit_ShouldThrowBusinessException` — PASS. `PaymentServiceTest.processPayment_WithZeroAmount_ShouldThrowBusinessException` — PASS. `PaymentServiceTest.processPayment_WithNegativeOrderId_ShouldThrowBusinessException` — PASS. `ReviewServiceTest.createReview_WithInvalidProductId_ShouldThrowException` — PASS. `ReviewServiceTest.createReview_WithNullProductId_ShouldThrowException` — PASS. `NotificationServiceTest.sendNotification_WithValidRequest_ShouldReturnNotificationResponse` — PASS. Total: 19/19 tests unitarios en M5+M7+M8 exitosos. | **Corregido** |
| FB-03 | Inconsistencia en configuraciones para despliegue y entornos. Algunos servicios usaban `application.properties` (M1), otros `application.yml` sin perfiles, sin variables de entorno para producción. No existía mecanismo de separación local/producción. | Estandarización completa de configuración: Migración de `User_Service_M1` de `application.properties` a `application.yml`. Creación de perfiles `local` (H2 en memoria, puertos fijos, Eureka localhost) y `render` (MySQL via `${DATABASE_URL}`, puertos dinámicos `${PORT}`, Eureka remoto `${EUREKA_URI}`, credenciales `${DB_USERNAME}/${DB_PASSWORD}`) en los 12 servicios. Creación de `render.yaml` con 12 servicios web + 1 base de datos MySQL, definición de dependencias (`dependsOn`) para orden de despliegue y variables de entorno inyectadas automáticamente. Eliminación de 8 archivos `application-h2.yml` redundantes. | `User_Service_M1/src/main/resources/application.yml` (nuevo, reemplaza `application.properties`), `application.yml` de los 11 servicios restantes (M2–M11, M12), eliminación de `Auth_Service_M10/.../application-h2.yml`, `Cart_Service_M3/.../application-h2.yml`, `Category_Service_M9/.../application-h2.yml`, `Notification_Service_M8/.../application-h2.yml`, `Inventory_Service_M6/.../application-h2.yml`, `Payment_Service_M5/.../application-h2.yml`, `Order_Service_M4/.../application-h2.yml`, `Review_Service_M7/.../application-h2.yml`, `render.yaml` (nuevo) | Verificación de 12 archivos `application.yml` con estructura de perfiles correcta (2 `---` separadores, `on-profile: local` y `on-profile: render`). `render.yaml` contiene 12 servicios con `envVars` configuradas (PORT, EUREKA_URI, DATABASE_URL, DB_USERNAME, DB_PASSWORD). Perfil local verificado: todos los servicios levantan con H2 y se registran en Eureka. | **Corregido** |
| FB-04 | Falta de documentación técnica completa que describa la arquitectura del sistema, instrucciones de ejecución y configuración de variables de entorno. | Reescritura completa de `README.md` con 11 secciones: nombre del proyecto, descripción del problema, arquitectura general, tabla de microservicios, variables de entorno, instrucciones de ejecución local, comandos de pruebas, documentación API y rutas del Gateway, matriz de usuarios de prueba, sección de despliegue en Render y enlace a herramienta de gestión. | `README.md` (reescritura completa: 475 líneas) | `README.md:1` — Título del proyecto. `README.md:48` — Diagrama de arquitectura. `README.md:80` — Tabla de 12 servicios. `README.md:100` — Variables de entorno. `README.md:115` — Instrucciones paso a paso. `README.md:150` — Tabla de rutas del Gateway. | **Corregido** |
| FB-05 | Ausencia de matriz de trazabilidad de requerimientos que mapee cada funcionalidad implementada a sus endpoints y pruebas asociadas. | Creación de `docs/matriz-requerimientos.md` con 31 requerimientos mapeados a endpoints reales del API Gateway, nombres exactos de pruebas unitarias y evidencia de verificación por cada item. Reubicación de la matriz en la ruta `docs/` exigida por la rúbrica. | `docs/matriz-requerimientos.md` (nuevo), eliminación de `MATRIZ_REQUERIMIENTOS.md` (raíz) | `docs/matriz-requerimientos.md:1` — 31 filas en tabla principal. Columnas: ID, Requerimiento declarado por el equipo, Tipo, Estado, Endpoint o evidencia, Prueba asociada. Todos los endpoints verificados contra código fuente real. | **Corregido** |

---

## Resumen de Estado Final

| Categoría | Observaciones | Corregidas | Pendientes |
|-----------|--------------|------------|------------|
| Arquitectura y ecosistema | 1 | 1 | 0 |
| Validaciones y reglas de negocio | 1 | 1 | 0 |
| Configuración y despliegue | 1 | 1 | 0 |
| Documentación técnica | 1 | 1 | 0 |
| Trazabilidad de requerimientos | 1 | 1 | 0 |
| **Total** | **5** | **5** | **0** |

---

## Servicios del Ecosistema — Estado Final

| # | Servicio | Estado | Último cambio |
|---|---------|--------|---------------|
| M12 | Discovery_Server_M12 | Corregido | Nuevo servicio Eureka Server |
| M11 | Gateway_Service_M11 | Corregido | Rutas lb:// + perfiles local/render |
| M1 | User_Service_M1 | Corregido | Migrado a YAML + perfiles |
| M2 | Product_Service_M2 | Corregido | Eureka Client + perfiles |
| M3 | Cart_Service_M3 | Corregido | Eureka Client + perfiles |
| M4 | Order_Service_M4 | Corregido | Eureka Client + perfiles |
| M5 | Payment_Service_M5 | Corregido | Bean Validation + Business Exception + Eureka |
| M6 | Inventory_Service_M6 | Corregido | Eureka Client + perfiles |
| M7 | Review_Service_M7 | Corregido | Bean Validation + ErrorResponse mejorado + Eureka |
| M8 | Notification_Service_M8 | Corregido | Bean Validation + Eureka |
| M9 | Category_Service_M9 | Corregido | Eureka Client + perfiles |
| M10 | Auth_Service_M10 | Corregido | Eureka Client + perfiles |

---

> **Resultado:** Todas las observaciones de feedback han sido
> resueltas y verificadas. El ecosistema de 12 microservicios
> se encuentra listo para la evaluación final.
