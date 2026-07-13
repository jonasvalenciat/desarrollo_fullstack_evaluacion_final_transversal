# GameVerse — Plataforma de Microservicios para Tienda de Videojuegos Online

> **Duoc UC — Evaluación Final Transversal / DSY1103 Desarrollo Full Stack**
>
> Sistema backend distribuido basado en 12 microservicios Spring Boot,
> service discovery con Netflix Eureka, API Gateway con Spring Cloud
> Gateway y despliegue automatizado en Render.

---

## 1. Nombre del Proyecto e Integrantes

| Campo | Valor |
|-------|-------|
| **Proyecto** | GameVerse — Tienda de Videojuegos Online |
| **Asignatura** | DSY1103 — Desarrollo Full Stack |
| **Institución** | Duoc UC |
| **Semestre (actualización documental)** | 2026 |

| # | Integrante | Rol |
|---|-----------|-----|
| 1 | [Nombre del Integrante 1] | [Rol / Especialidad] |
| 2 | [Nombre del Integrante 2] | [Rol / Especialidad] |
| 3 | [Nombre del Integrante 3] | [Rol / Especialidad] |

---

## 2. Descripción del Problema y Solución

### Problema

Las tiendas de videojuegos online requieren una infraestructura backend
que soporte múltiples dominios de negocio — catálogo de productos,
carrito de compras, pagos, reseñas, notificaciones y autenticación —
de forma independiente, escalable y desplegable de manera aislada.

### Solución

Se implementó un **ecosistema de 12 microservicios** construidos con
**Java 17** y **Spring Boot 3.4.4**, comunicados a través de un
**API Gateway centralizado** (Spring Cloud Gateway) y registrados en
un **servidor de descubrimiento** (Netflix Eureka). Cada microservicio
es un proyecto Maven independiente con su propio Dockerfile,
configuración YAML por perfiles (local/render), base de datos
aislada y pruebas unitarias con Mockito.

---

## 3. Arquitectura General y Estructura del Repositorio

### Flujo de Petición

```
                         ┌─────────────────────────────┐
                         │      Cliente (Frontend)      │
                         │   https://gameverse.onrender  │
                         └──────────────┬──────────────┘
                                        │
                                        ▼
                         ┌─────────────────────────────┐
                         │   API Gateway (:8080)         │
                         │   Spring Cloud Gateway        │
                         │   /api/v1/{recurso}/**        │
                         └──────────────┬──────────────┘
                                        │  Consulta servicios registrados
                                        ▼
                         ┌─────────────────────────────┐
                         │   Eureka Discovery (:8761)    │
                         │   Registro y balanceo         │
                         └──────────────┬──────────────┘
                                        │
                    ┌───────┬───────┬───┴───┬───────┬───────┐
                    ▼       ▼       ▼       ▼       ▼       ▼
                  M1:M10 — Microservicios de negocio
                  (cada uno con su propia BD y puerto)
```

### Estructura del Repositorio

```
desarrollo_fullstack_evaluacion_final_transversal/
├── Discovery_Server_M12/          # Eureka Server (:8761)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/.../DiscoveryServerM12Application.java
│
├── Gateway_Service_M11/           # API Gateway (:8080)
│   ├── pom.xml
│   └── src/main/resources/application.yml  (rutas lb://)
│
├── User_Service_M1/               # Usuarios (:8081)
├── Product_Service_M2/            # Productos (:8082)
├── Cart_Service_M3/               # Carrito (:8083) — Seguridad por roles
├── Order_Service_M4/              # Órdenes (:8084)
├── Payment_Service_M5/            # Pagos (:8085)
├── Inventory_Service_M6/          # Inventario (:8086)
├── Review_Service_M7/             # Reseñas (:8087)
├── Notification_Service_M8/       # Notificaciones (:8088)
├── Category_Service_M9/           # Categorías (:8089)
├── Auth_Service_M10/              # Autenticación (:8090)
│
├── render.yaml                    # Despliegue en Render
├── docs/matriz-requerimientos.md      # Trazabilidad de requerimientos
└── README.md                      # Este archivo
```

### Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.4.4 |
| Service Discovery | Netflix Eureka (Spring Cloud 2024.0.1) |
| API Gateway | Spring Cloud Gateway |
| Base de datos (local) | H2 In-Memory |
| Base de datos (producción) | MySQL (Render) |
| Seguridad | Spring Security / HTTP Basic / @PreAuthorize |
| Validaciones | Jakarta Bean Validation (JSR 380) |
| Documentación API | Springdoc OpenAPI 2.8.6 (Swagger UI) |
| Pruebas | JUnit 5 + Mockito |
| Contenedores | Docker multi-stage (Alpine Linux) |
| Despliegue | Render (Infrastructure as Code — render.yaml) |

---

## 4. Tabla de Microservicios, Puertos y Bases de Datos

| Módulo | Servicio | Spring Name | Puerto Local | Puerto Render | Tipo de BD | Descripción |
|--------|----------|-------------|-------------|---------------|------------|-------------|
| M12 | Discovery_Server_M12 | `discovery-server` | 8761 | `${PORT}` | Sin BD | Eureka Server — descubrimiento de servicios |
| M11 | Gateway_Service_M11 | `gateway-service` | 8080 | `${PORT}` | Sin BD | API Gateway — punto de entrada único |
| M1 | User_Service_M1 | `user-service` | 8081 | `${PORT}` | H2 / MySQL | Gestión de usuarios |
| M2 | Product_Service_M2 | `product-service` | 8082 | `${PORT}` | H2 / MySQL | Catálogo de videojuegos |
| M3 | Cart_Service_M3 | `cart-service` | 8083 | `${PORT}` | H2 / MySQL | Carrito de compras (con roles) |
| M4 | Order_Service_M4 | `order-service` | 8084 | `${PORT}` | H2 / MySQL | Gestión de órdenes |
| M5 | Payment_Service_M5 | `payment-service` | 8085 | `${PORT}` | H2 / MySQL | Procesamiento de pagos |
| M6 | Inventory_Service_M6 | `inventory-service` | 8086 | `${PORT}` | H2 / MySQL | Control de inventario |
| M7 | Review_Service_M7 | `review-service` | 8087 | `${PORT}` | H2 / MySQL | Reseñas y calificaciones |
| M8 | Notification_Service_M8 | `notification-service` | 8088 | `${PORT}` | H2 / MySQL | Notificaciones (EMAIL, SMS, PUSH) |
| M9 | Category_Service_M9 | `category-service` | 8089 | `${PORT}` | H2 / MySQL | Categorías de productos |
| M10 | Auth_Service_M10 | `auth-service` | 8090 | `${PORT}` | H2 / MySQL | Autenticación y registro |

---

## 5. Variables de Entorno

### Perfil `local` (desarrollo)

Todos los servicios usan configuración hardcoded en `application.yml`:
puertos fijos, H2 en memoria y Eureka en `http://localhost:8761/eureka`.

### Perfil `render` (producción)

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `render` |
| `JAVA_VERSION` | Versión de Java | `17` |
| `PORT` | Puerto asignado por Render | `8080` |
| `EUREKA_URI` | URL del Eureka Server | `http://discovery-server-m12:8761/eureka` |
| `DATABASE_URL` | URL de conexión MySQL | `jdbc:mysql://...videogames?...` |
| `DB_USERNAME` | Usuario de la base de datos | `videogames_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | `[secreto]` |

> **Nota:** En Render, las variables `DATABASE_URL`, `DB_USERNAME` y
> `DB_PASSWORD` se inyectan automáticamente desde el recurso de base
> de datos `videogames-db` definido en `render.yaml`.

---

## 6. Instrucciones de Ejecución Local sin IDE

### Prerrequisitos

- **Java 17** instalado (`java -version`)
- **Maven** o el Maven Wrapper incluido (`./mvnw` o `mvnw.cmd`)

### Orden estricto de arranque

> **IMPORTANTE:** El Discovery Server debe iniciarse primero, ya que
> todos los demás servicios intentan registrarse en él al arrancar.

#### Paso 1 — Discovery Server (Eureka)

```bash
cd Discovery_Server_M12
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

> Verificar en http://localhost:8761 que el panel de Eureka esté activo.

#### Paso 2 — API Gateway

```bash
cd Gateway_Service_M11
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### Paso 3 — Microservicios de negocio (en cualquier orden)

```bash
# Terminal 1
cd User_Service_M1
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 2
cd Product_Service_M2
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 3
cd Cart_Service_M3
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 4
cd Order_Service_M4
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 5
cd Payment_Service_M5
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 6
cd Inventory_Service_M6
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 7
cd Review_Service_M7
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 8
cd Notification_Service_M8
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 9
cd Category_Service_M9
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 10
cd Auth_Service_M10
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### En Windows (sin bash)

```cmd
cd Discovery_Server_M12
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### Verificación

Abrir http://localhost:8761 — el panel de Eureka debe mostrar
todos los servicios registrados con status **UP**.

---

## 7. Comandos de Pruebas

### Ejecutar tests de un servicio específico

```bash
cd Cart_Service_M3
./mvnw clean test
```

### Ejecutar tests de todos los servicios

```bash
for dir in Discovery_Server_M12 User_Service_M1 Product_Service_M2 \
           Cart_Service_M3 Order_Service_M4 Payment_Service_M5 \
           Inventory_Service_M6 Review_Service_M7 Notification_Service_M8 \
           Category_Service_M9 Auth_Service_M10 Gateway_Service_M11; do
  echo "=== $dir ==="
  cd $dir && ./mvnw clean test && cd ..
done
```

### En Windows (PowerShell)

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

### Cobertura de pruebas por servicio

| Servicio | Tests Unitarios | Tests Context | Total |
|----------|----------------|---------------|-------|
| Discovery_Server_M12 | 0 | 1 | 1 |
| Gateway_Service_M11 | 0 | 1 | 1 |
| User_Service_M1 | 4 | 1 | 5 |
| Product_Service_M2 | 4 | 1 | 5 |
| Cart_Service_M3 | 4 | 1 | 5 |
| Order_Service_M4 | 4 | 1 | 5 |
| Payment_Service_M5 | 8 | 1 | 9 |
| Inventory_Service_M6 | 4 | 1 | 5 |
| Review_Service_M7 | 7 | 1 | 8 |
| Notification_Service_M8 | 4 | 1 | 5 |
| Category_Service_M9 | 7 | 1 | 8 |
| Auth_Service_M10 | 4 | 1 | 5 |
| **TOTAL** | **50** | **12** | **62** |

---

## 8. Documentación API y Rutas del Gateway

### Rutas del API Gateway (Puerto 8080)

Todas las peticiones externas ingresan por el Gateway y se enrutan
hacia los microservicios usando el filtro `StripPrefix=2`:

| Prefijo de Ruta | Servicio Destino | URL Interna (local) | URL Interna (render) |
|-----------------|-----------------|---------------------|----------------------|
| `/api/v1/auth/**` | Auth_Service_M10 | `http://localhost:8090` | `lb://auth-service` |
| `/api/v1/users/**` | User_Service_M1 | `http://localhost:8081` | `lb://user-service` |
| `/api/v1/products/**` | Product_Service_M2 | `http://localhost:8082` | `lb://product-service` |
| `/api/v1/cart/**` | Cart_Service_M3 | `http://localhost:8083` | `lb://cart-service` |
| `/api/v1/orders/**` | Order_Service_M4 | `http://localhost:8084` | `lb://order-service` |
| `/api/v1/payments/**` | Payment_Service_M5 | `http://localhost:8085` | `lb://payment-service` |
| `/api/v1/inventory/**` | Inventory_Service_M6 | `http://localhost:8086` | `lb://inventory-service` |
| `/api/v1/reviews/**` | Review_Service_M7 | `http://localhost:8087` | `lb://review-service` |
| `/api/v1/notifications/**` | Notification_Service_M8 | `http://localhost:8088` | `lb://notification-service` |
| `/api/v1/categories/**` | Category_Service_M9 | `http://localhost:8089` | `lb://category-service` |

**Ejemplo:** `POST http://localhost:8080/api/v1/payments` se redirige
internamente a `POST http://localhost:8085/payments`.

### Swagger UI

Cada microservicio expone documentación Swagger en:

```
http://localhost:{puerto}/swagger-ui.html
```

| Servicio | Swagger URL |
|----------|-------------|
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

---

## 9. Matriz de Usuarios de Prueba y Roles

### Roles del Sistema

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| `ADMIN` | Administrador del sistema | Acceso total: CRUD de usuarios, historial de carrito, todas las operaciones |
| `USER` | Cliente registrado | Agregar/modificar/eliminar items del propio carrito |
| `AGENT` | Agente de soporte | Modificar/eliminar items del propio carrito (igual que USER) |

### Usuarios de Prueba (perfil `local`)

Los siguientes usuarios se crean automáticamente al iniciar
`Cart_Service_M3` con el perfil `h2`/`local`:

| Nombre | Email | Contraseña | Rol | Activo |
|--------|-------|-----------|-----|--------|
| Administrador | `admin@empresa.com` | `pass123` | ADMIN | true |
| Ana Garcia | `ana.garcia@empresa.com` | `user123` | USER | true |
| Carlos Lopez | `carlos.lopez@empresa.com` | `user123` | AGENT | true |

### Endpoints Protegidos por Rol

| Endpoint | Método | Acceso |
|----------|--------|--------|
| `GET /cart`, `GET /cart/by-id/**` | GET | Público (sin auth) |
| `POST /cart/add` | POST | USER, AGENT, ADMIN |
| `PUT /cart/by-id/{id}` | PUT | USER, AGENT, ADMIN + `@PreAuthorize` (propiedad) |
| `PATCH /cart/by-id/{id}/assign` | PATCH | USER, AGENT, ADMIN + `@PreAuthorize` (propiedad) |
| `DELETE /cart/by-id/{id}` | DELETE | Autenticado |
| `GET /cart/by-id/{id}/history` | GET | Solo ADMIN |
| `/users/**` | * | Solo ADMIN |

### Ejemplo de Autenticación (HTTP Basic)

```bash
# Como ADMIN
curl -u admin@empresa.com:pass123 http://localhost:8080/api/v1/cart

# Como USER
curl -u ana.garcia@empresa.com:user123 -X POST \
  -H "Content-Type: application/json" \
  -d '{"productName":"Halo Infinite","price":59.99,"quantity":1,"userEmail":"ana.garcia@empresa.com"}' \
  http://localhost:8080/api/v1/cart/add
```

---

## 10. Despliegue en Render

### Cómo funciona

El archivo `render.yaml` define la infraestructura completa como código.
Al vincular el repositorio GitHub en Render, se despliegan
automáticamente **12 servicios web** y **1 base de datos MySQL**:

```
render.yaml
├── 12 × services (web, java, plan: free)
│   ├── discovery-server-m12  → :8761
│   ├── gateway-service-m11   → :8080
│   ├── user-service-m1       → :8081
│   ├── product-service-m2    → :8082
│   ├── cart-service-m3       → :8083
│   ├── order-service-m4      → :8084
│   ├── payment-service-m5    → :8085
│   ├── inventory-service-m6  → :8086
│   ├── review-service-m7     → :8087
│   ├── notification-service-m8 → :8088
│   ├── category-service-m9   → :8089
│   └── auth-service-m10      → :8090
│
└── 1 × database
    └── videogames-db (MySQL, plan: free)
```

### Orden de despliegue

Render respeta las dependencias definidas en `dependsOn`:

1. **`discovery-server-m12`** se despliega primero
2. **Todos los servicios M1–M10** esperan a que Eureka esté activo
3. **`gateway-service-m11`** se despliega último (depende de Eureka)

### URLs Públicas de Render

| Servicio | URL |
|----------|-----|
| Discovery Server | [https://discovery-server-m12.onrender.com](https://discovery-server-m12.onrender.com) |
| **API Gateway** | **[https://gameverse.onrender.com](https://gameverse.onrender.com)** |
| User Service | [https://user-service-m1.onrender.com](https://user-service-m1.onrender.com) |
| Product Service | [https://product-service-m2.onrender.com](https://product-service-m2.onrender.com) |
| Cart Service | [https://cart-service-m3.onrender.com](https://cart-service-m3.onrender.com) |
| Order Service | [https://order-service-m4.onrender.com](https://order-service-m4.onrender.com) |
| Payment Service | [https://payment-service-m5.onrender.com](https://payment-service-m5.onrender.com) |
| Inventory Service | [https://inventory-service-m6.onrender.com](https://inventory-service-m6.onrender.com) |
| Review Service | [https://review-service-m7.onrender.com](https://review-service-m7.onrender.com) |
| Notification Service | [https://notification-service-m8.onrender.com](https://notification-service-m8.onrender.com) |
| Category Service | [https://category-service-m9.onrender.com](https://category-service-m9.onrender.com) |
| Auth Service | [https://auth-service-m10.onrender.com](https://auth-service-m10.onrender.com) |

> **Todas las peticiones de negocio se realizan a través del Gateway:**
> `https://gameverse.onrender.com/api/v1/{recurso}/**`

---

## 11. Enlace a Herramienta de Gestión

| Herramienta | Enlace |
|-------------|--------|
| GitHub Projects | [https://github.com/jonasvalenciat/desarrollo_fullstack_evaluacion_final_transversal/projects](https://github.com/jonasvalenciat/desarrollo_fullstack_evaluacion_final_transversal/projects) |
| Trello | [https://trello.com/b/[ID_DEL_TABLERO]/gameverse](https://trello.com/b/[ID_DEL_TABLERO]/gameverse) |

---

> **Duoc UC — Evaluación Final Transversal (actualizado 2026)**
