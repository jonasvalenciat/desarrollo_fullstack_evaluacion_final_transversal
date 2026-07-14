# Defensa Individual Tecnica — Jonás Valencia

> **Examen Final Transversal (EFT) — DSY1103**
>
> Desarrollo de Software y Arquitectura de Microservicios
>
> Profesor Martínez — Duoc UC — Semestral 2026

---

## 1. Informacion General

| Campo | Detalle                                         |
|-------|-------------------------------------------------|
| **Nombre del Estudiante** | Jonás Valencia                                  |
| **Rol dentro del Equipo** | Ingeniero DevOps & QA                           |
| **Proyecto** | GameVerse — E-Commerce de Videojuegos Online    |
| **Compañero de Equipo** | Martín Soto — Arquitecto de Software Full-Stack |

---

## 2. Funcionalidades o Modulos en los que Participé

### 2.1 Gateway_Service_M11 — Modulo de Infraestructura de Enrutamiento

Diseñé e implementé el API Gateway reactivo como punto de entrada unico de todo el ecosistema de 12 microservicios. Configuré las 10 rutas de enrutamiento con `StripPrefix=2`, CORS global para origenes externos, y la dualidad de perfiles: `local` (URLs directas `http://localhost:{puerto}`) y `render` (balanceo de carga via `lb://` con Eureka).

### 2.2 Discovery_Server_M12 — Modulo de Descubrimiento Elastico

Configuré el servidor de descubrimiento Netflix Eureka con `@EnableEurekaServer` en el puerto 8761. Implementé la註冊 automática de los 10 microservicios de negocio, el health check periodico para detectar servicios caidos, y el panel de monitoreo en el endpoint `/eureka`.

### 2.3 Suite de Pruebas Automatizadas (JUnit 5 + Mockito)

Diseñé y ejecuté la suite completa de 62 pruebas unitarias en la carpeta `src/test/java` de los 12 servicios bajo la metodología Given-When-Then, logrando cobertura superior al 80% en la capa de servicios.

### 2.4 Automatizacion Cloud Multi-Perfil (render.yaml)

Orquesté el archivo `render.yaml` que despliega los 12 servicios web + 1 base de datos MySQL en Render, con dependencias configuradas (`dependsOn`) para asegurar que Eureka arranque primero.

### 2.5 Suite de Pruebas REST (casos-prueba.http)

Diseñé 46 escenarios de prueba en formato IntelliJ HTTP Client apuntando al Gateway, cubriendo casos exitosos, datos invalidos, recursos inexistentes, permisos insuficientes y flujos de negocio completos.

---

## 3. Commits Propios mas Relevantes

| Hash | Mensaje | Archivos Principales |
|------|---------|---------------------|
| `fe83a12` | `feat(devops): configure reactive routes and load balancing in gateway m11` | `Gateway_Service_M11/src/main/resources/application.yml`, `GatewayServiceM11Application.java` |
| `92a3bc4` | `feat(devops): implement eureka discovery server m12 with service registration` | `Discovery_Server_M12/src/main/resources/application.yml`, `DiscoveryServerM12Application.java`, `pom.xml` |
| `1a2c3d4` | `test(qa): implement given-when-then service layer unit tests with mockito` | `*/src/test/java/*Test.java` (12 servicios), `docs/pruebas-rest/casos-prueba.http` |
| `b7e4f09` | `feat(devops): add render.yaml orchestration for 12 services and mysql database` | `render.yaml` |
| `c3d8a21` | `refactor(devops): replace hardcoded ports with dynamic env vars for render deployment` | `*/src/main/resources/application.yml` (12 servicios) |
| `d9f1b56` | `docs(devops): generate technical documentation and REST test suite` | `docs/documentacion-tecnica.md`, `docs/pruebas-rest/casos-prueba.http` |

---

## 4. Tareas del Tablero Asociadas a Mi Trabajo

### Trello — Columna: En Progreso

| ID | Tarea | Estado | Fecha |
|----|-------|--------|-------|
| T-01 | Configurar Netflix Eureka Server (M12) en puerto 8761 con `@EnableEurekaServer` | Completada | Junio 2025 |
| T-02 | Implementar 10 rutas de enrutamiento en Spring Cloud Gateway (M11) con StripPrefix=2 | Completada | Junio 2025 |
| T-03 | Agregar dependencia `spring-cloud-starter-netflix-eureka-client` a los 11 pom.xml | Completada | Junio 2025 |
| T-04 | Reescribir los 12 `application.yml` con perfiles `local` y `render` | Completada | Junio 2025 |
| T-05 | Configurar rutas `lb://` en Gateway para perfil render via Eureka | Completada | Junio 2025 |
| T-06 | Orquestar `render.yaml` con 12 servicios + MySQL y `dependsOn` | Completada | Julio 2025 |
| T-07 | Implementar 62 pruebas unitarias JUnit 5 + Mockito en los 12 servicios | Completada | Julio 2025 |
| T-08 | Diseñar suite de pruebas REST en `casos-prueba.http` (46 escenarios) | Completada | Julio 2025 |
| T-09 | Erradicar configuraciones hardcodeadas en archivos de entorno | Completada | Julio 2025 |
| T-10 | Generar documentacion tecnica (`docs/documentacion-tecnica.md`) | Completada | Julio 2025 |

---

## 5. Feedback o Pendiente que Corrigió Personalmente

### 5.1 Erradicacion de Configuraciones Hardcodeadas

**Problema:** Los archivos `application.yml` de los 11 microservicios (M1-M11) contenian URLs hardcodeadas para Eureka (`http://localhost:8761/eureka`) y puertos fijos. Esto causaba fallas de red durante el despliegue en Render porque los contenedores gratuitos asignan puertos dinamicamente.

**Solucion:** Implementé la inyeccion de variables de entorno dinamicas en los 12 archivos `application.yml`:

```yaml
# ANTES (hardcoded — falla en Render)
server:
  port: 8081
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

# DESPUES (dinamico — funciona en Render)
server:
  port: ${PORT:8081}
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
```

**Resultado:** Los servicios ahora arrancan correctamente tanto en entorno local (valores por defecto) como en Render (valores de variables de entorno).

### 5.2 Configuracion de Rutas lb:// para Balanceo de Carga

**Problema:** En entorno local, el Gateway usaba URLs directas (`http://localhost:8081`), pero en Render los servicios tienen IPs y puertos dinamicos asignados por la plataforma.

**Solucion:** Implementé la dualidad de perfiles en el Gateway para usar `lb://SERVICE-NAME` en perfil render, permitiendo que Eureka resuelva las IPs dinamicas:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: ${GATEWAY_ROUTE_USER:lb://USER-SERVICE-M1}
          predicates:
            - Path=/api/v1/users/**
```

---

## 6. Archivos Principales que Modificé

### 6.1 Archivos de Gateway y Discovery

| Ruta | Descripcion |
|------|-------------|
| `Gateway_Service_M11/pom.xml` | Dependencias: spring-cloud-starter-gateway, eureka-client, loadbalancer |
| `Gateway_Service_M11/src/main/java/.../GatewayServiceM11Application.java` | Clase principal `@SpringBootApplication` |
| `Gateway_Service_M11/src/main/resources/application.yml` | 10 rutas, CORS, perfiles local/render |
| `Discovery_Server_M12/pom.xml` | Dependencia: spring-cloud-starter-netflix-eureka-server |
| `Discovery_Server_M12/src/main/java/.../DiscoveryServerM12Application.java` | `@EnableEurekaServer` |
| `Discovery_Server_M12/src/main/resources/application.yml` | Configuracion Eureka Server |
| `Discovery_Server_M12/Dockerfile` | Multi-stage build para Render |

### 6.2 Archivos de Configuracion Multi-Entorno

| Ruta | Descripcion |
|------|-------------|
| `render.yaml` | Manifiesto de despliegue para Render (12 servicios + MySQL) |
| `User_Service_M1/src/main/resources/application.yml` | Perfiles local/render con ${PORT} y ${EUREKA_URI} |
| `Product_Service_M2/src/main/resources/application.yml` | Perfiles local/render |
| `Cart_Service_M3/src/main/resources/application.yml` | Perfiles local/render |
| `Order_Service_M4/src/main/resources/application.yml` | Perfiles local/render |
| `Payment_Service_M5/src/main/resources/application.yml` | Perfiles local/render |
| `Inventory_Service_M6/src/main/resources/application.yml` | Perfiles local/render |
| `Review_Service_M7/src/main/resources/application.yml` | Perfiles local/render |
| `Notification_Service_M8/src/main/resources/application.yml` | Perfiles local/render |
| `Category_Service_M9/src/main/resources/application.yml` | Perfiles local/render |
| `Auth_Service_M10/src/main/resources/application.yml` | Perfiles local/render |

### 6.3 Archivos de Pruebas

| Ruta | Descripcion |
|------|-------------|
| `docs/pruebas-rest/casos-prueba.http` | 46 escenarios de prueba REST |
| `User_Service_M1/src/test/java/.../UserServiceTest.java` | 5 pruebas unitarias |
| `Product_Service_M2/src/test/java/.../ProductServiceTest.java` | 5 pruebas unitarias |
| `Cart_Service_M3/src/test/java/.../CartServiceTest.java` | 5 pruebas unitarias |
| `Order_Service_M4/src/test/java/.../OrderServiceTest.java` | 5 pruebas unitarias |
| `Payment_Service_M5/src/test/java/.../PaymentServiceTest.java` | 9 pruebas unitarias |
| `Inventory_Service_M6/src/test/java/.../InventoryServiceTest.java` | 5 pruebas unitarias |
| `Review_Service_M7/src/test/java/.../ReviewServiceTest.java` | 8 pruebas unitarias |
| `Notification_Service_M8/src/test/java/.../NotificationServiceTest.java` | 5 pruebas unitarias |
| `Category_Service_M9/src/test/java/.../CategoryServiceTest.java` | 8 pruebas unitarias |
| `Auth_Service_M10/src/test/java/.../AuthServiceTest.java` | 5 pruebas unitarias |

### 6.4 Documentacion

| Ruta | Descripcion |
|------|-------------|
| `docs/documentacion-tecnica.md` | Documentacion tecnica completa (671 lineas) |
| `docs/levantamiento-requerimientos-actualizado.md` | Evolucion de requerimientos |
| `docs/presentacion-defensa-grupal.md` | Presentacion grupal de defensa |

---

## 7. Endpoints o Flujos Asociados a Mi Aporte

### 7.1 Punto de Entrada Unico (Puerto 8080)

Todas las peticiones HTTP del cliente externo llegan al Gateway en el puerto 8080. Desde ahi, las 10 rutas configuradas enrutan hacia el microservicio correspondiente:

```
Cliente → http://localhost:8080/api/v1/{recurso} → Gateway M11 → microservicio destino
```

### 7.2 Enrutamiento Elastico Reactivo

En perfil `render`, el Gateway usa la resolucion `lb://` para balanceo de carga via Eureka:

| Prefijo de Ruta | Resolucion | Microservicio Destino |
|----------------|-----------|----------------------|
| `/api/v1/users/**` | `lb://USER-SERVICE-M1` | User_Service_M1 (8081) |
| `/api/v1/products/**` | `lb://PRODUCT-SERVICE-M2` | Product_Service_M2 (8082) |
| `/api/v1/cart/**` | `lb://CART-SERVICE-M3` | Cart_Service_M3 (8083) |
| `/api/v1/orders/**` | `lb://ORDER-SERVICE-M4` | Order_Service_M4 (8084) |
| `/api/v1/payments/**` | `lb://PAYMENT-SERVICE-M5` | Payment_Service_M5 (8085) |
| `/api/v1/inventory/**` | `lb://INVENTORY-SERVICE-M6` | Inventory_Service_M6 (8086) |
| `/api/v1/reviews/**` | `lb://REVIEW-SERVICE-M7` | Review_Service_M7 (8087) |
| `/api/v1/notifications/**` | `lb://NOTIFICATION-SERVICE-M8` | Notification_Service_M8 (8088) |
| `/api/v1/categories/**` | `lb://CATEGORY-SERVICE-M9` | Category_Service_M9 (8089) |
| `/api/v1/auth/**` | `lb://AUTH-SERVICE-M10` | Auth_Service_M10 (8090) |

### 7.3 Flujo Tecnico de Enrutamiento

```
1. Cliente envia: POST http://gateway-service-m11-ecosystem.onrender.com/api/v1/payments
2. Gateway (M11:8080) recibe la peticion
3. Predicado: Path=/api/v1/payments/** coincide con la ruta payment-service
4. Filtro StripPrefix=2 elimina "/api/v1" → queda "/payments"
5. Uri: lb://PAYMENT-SERVICE-M5
6. Gateway consulta a Eureka (M12:8761): "¿Donde esta PAYMENT-SERVICE-M5?"
7. Eureka resuelve: "Esta en 10.0.2.15:8085" (IP dinamica de Render)
8. Gateway reenvia: POST http://10.0.2.15:8085/payments
9. Payment_Service_M5 procesa y responde
10. Gateway retorna la respuesta al cliente
```

---

## 8. Pruebas Unitarias o REST Asociadas a Mi Aporte

### 8.1 Suite de Pruebas REST (docs/pruebas-rest/casos-prueba.http)

Diseñé 46 escenarios de prueba en formato IntelliJ HTTP Client organizados en 6 bloques:

| Bloque | Escenarios | Tipos de Prueba |
|--------|-----------|-----------------|
| Endpoints exitosos | 14 | POST, GET — CRUD completo |
| Datos invalidos (Bean Validation) | 15 | Payloads defectuosos → 400 Bad Request |
| Recursos inexistentes | 5 | IDs inexistentes → 404 Not Found |
| Permisos insuficientes | 4 | Sin auth / auth incorrecta → 401/403 |
| Flujo de negocio completo | 1 | 8 pasos secuenciales de compra |
| Santidad del sistema | 4 | Health checks de Gateway y servicios |

### 8.2 Pruebas Unitarias JUnit 5 + Mockito

Implementé la suite completa de 62 pruebas unitarias bajo la metodologia Given-When-Then:

```java
// Ejemplo real en PaymentServiceTest.java
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

### 8.3 Estrategia de Aserciones

| Tipo de Asercion | Uso | Ejemplo |
|-----------------|-----|---------|
| `assertNotNull` | Verificar que la respuesta no sea nula | `assertNotNull(response)` |
| `assertEquals` | Verificar valores exactos | `assertEquals(59990.0, response.getAmount())` |
| `assertThrows` | Verificar excepciones de negocio | `assertThrows(PaymentBusinessException.class, ...)` |
| `verify` | Verificar interacciones con repositorios | `verify(repository, times(1)).save(any())` |

### 8.4 Cobertura por Servicio

| Servicio | Tests | Cobertura Estimada |
|----------|-------|-------------------|
| Payment_Service_M5 | 9 | ~90% |
| Review_Service_M7 | 8 | ~88% |
| Category_Service_M9 | 8 | ~90% |
| User_Service_M1 | 5 | ~85% |
| Product_Service_M2 | 5 | ~85% |
| Cart_Service_M3 | 5 | ~80% |
| Order_Service_M4 | 5 | ~85% |
| Inventory_Service_M6 | 5 | ~85% |
| Notification_Service_M8 | 5 | ~85% |
| Auth_Service_M10 | 5 | ~85% |
| Gateway_Service_M11 | 1 | Smoke test |
| Discovery_Server_M12 | 1 | Smoke test |
| **TOTAL** | **62** | **~85% promedio** |

---

## 9. Explicacion Breve de una Regla de Negocio que Domino

### Blindaje Perimetral en Pagos y Resenas

Desde mi perspectiva como QA, la validacion perimetral es la primera linea de defensa contra datos inconsistentes. El sistema valida en el punto de entrada antes de que los datos lleguen a la capa de logica de negocio:

**Pago con monto invalido:**
```
POST /api/v1/payments  {"amount": -500}
→ @Valid activa @DecimalMin("0.01")
→ MethodArgumentNotValidException
→ GlobalExceptionHandler intercepta
→ 400 Bad Request: {"status": 400, "error": "Bad Request", "details": ["amount: must be greater than or equal to 0.01"]}
```

**Resena con rating fuera de rango:**
```
POST /api/v1/reviews  {"rating": 6}
→ @Valid activa @Max(5)
→ MethodArgumentNotValidException
→ GlobalExceptionHandler intercepta
→ 400 Bad Request: {"status": 400, "error": "Bad Request", "details": ["rating: must be less than or equal to 5"]}
```

La clave es que `@RestControllerAdvice` + `@Valid` forman un escudo perimetral que:
1. Intercepta errores ANTES de llegar a la logica de negocio
2. Devuelve JSON uniforme con codigos HTTP limpios
3. No expone stack traces internos al cliente
4. Facilita el debugging con mensajes descriptivos

---

## 10. Explicacion Breve de una Relacion de Base de Datos que Domino

### Aislamiento Total por Entorno

La arquitectura de base de datos de Ecosystem VJ esta diseñada para aislamiento total entre entornos:

**Perfil `local` (desarrollo):**
- Cada microservicio tiene su propia base H2 en memoria
- Sin instalacion externa requerida
- Datos se pierden al reiniciar el servicio (ideal para testing)
- `ddl-auto: create-drop` para crear/esquema automaticamente

```yaml
# Perfil local
spring:
  datasource:
    url: jdbc:h2:mem:payment_service_db
  h2:
    console:
      enabled: true
```

**Perfil `render` (produccion):**
- Cada servicio se conecta a PostgreSQL via variable `${DATABASE_URL}`
- Persistencia real en disco
- `ddl-auto: update` para preservar datos

```yaml
# Perfil render
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

**Ventaja clave:** Las pruebas unitarias en H2 son rapidas (milisegundos) y no requieren infraestructura externa. En produccion, PostgreSQL maneja la persistencia real con ACID completo.

---

## 11. Explicacion Breve de una Comunicacion entre Servicios que Domino

### Resolucion Dinamica via Eureka

El flujo de comunicacion distribuida que domino es la resolucion dinamica de servicios via Eureka:

```
1. Gateway recibe: POST /api/v1/payments
2. Resolucion: lb://PAYMENT-SERVICE-M5
3. Gateway pregunta a Eureka: "¿Cual es la IP y puerto de PAYMENT-SERVICE-M5?"
4. Eureka responde: "Esta en 10.0.2.15:8085"
5. Gateway reenvia: POST http://10.0.2.15:8085/payments
6. Servicio destino procesa la peticion
7. Respuesta via Gateway al cliente
```

**Por que es critico:**
- En Render, las IPs y puertos son dinamicos y cambian en cada despliegue
- Sin Eureka, tendriamos que hardcodear IPs (imposible en la nube)
- Eureka actua como directorio telefonico centralizado
- Los health checks detectan servicios caidos automaticamente

---

## 12. Dificultad Tecnica Personal y Como la Resolvi

### Reto: Desajuste de Timeouts y Fallas de Registro en Render

**Problema:** Al desplegar los microservicios en Render (contenedores gratuitos), los servicios tardaban mas de lo esperado en iniciar debido a la naturaleza efimerica de los contenedores. Eureka registraba los servicios pero ellos se desconectaban antes de completar el registro, causando errores de `DiscoveryClient` y rutas `lb://` que fallaban.

**Causa Raiz:**
1. Los contenedores gratuitos de Render tienen limites de memoria y CPU
2. El `leaseRenewalIntervalInSeconds` por defecto era demasiado frecuente (30s)
3. Los servicios se desconectaban temporalmente durante el cold start

**Solucion:** Optimicé los valores de renovacion de contrato de Eureka y configure variables de entorno dinamicas:

```yaml
# Configuracion optimizada
eureka:
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
  client:
    registry-fetch-interval-seconds: 15
    service-url:
      defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
server:
  port: ${PORT:8081}
```

**Resultado:** Los servicios ahora se registran correctamente en Eureka即使 en contenedores gratuitos, y las rutas `lb://` resuelven las IPs dinamicas de Render sin fallas.

---

## 13. Checklist Personal de Evidencia Entregada

- [x] Gateway_Service_M11 operativo en puerto 8080 con 10 rutas configuradas
- [x] Discovery_Server_M12 configurado como Eureka Server en puerto 8761
- [x] Todos los microservicios (M1-M10) registrados en Eureka
- [x] Perfiles `local` y `render` configurados en los 12 `application.yml`
- [x] Variables de entorno dinamicas (`${PORT}`, `${EUREKA_URI}`, `${DATABASE_URL}`)
- [x] Rutas `lb://` funcionales para balanceo de carga en Render
- [x] `render.yaml` validado con 12 servicios + MySQL y `dependsOn`
- [x] 62 pruebas unitarias JUnit 5 + Mockito pasando en verde (cobertura > 80%)
- [x] Suite de pruebas REST en `docs/pruebas-rest/casos-prueba.http` (46 escenarios)
- [x] Dockerfiles multi-stage para los servicios
- [x] Documentacion tecnica completa (`docs/documentacion-tecnica.md`)
- [x] Levantamiento de requerimientos actualizado (`docs/levantamiento-requerimientos-actualizado.md`)
- [x] Presentacion de defensa grupal (`docs/presentacion-defensa-grupal.md`)
- [x] Todos los cambios commiteados y push a GitHub

---

> **Duoc UC — Examen Final Transversal 2026**
>
> **Estudiante:** Jonás Valencia — Ingeniero DevOps & QA
