# Documentación Funcional — GameVerse

> **Proyecto:** GameVerse — Tienda de Videojuegos Online
>
> **Asignatura:** DSY1103 — Desarrollo Full Stack
>
> **Versión:** 1.1 — Actualización documental 2026

---

## 1. Problema que Resuelve

El mercado de videojuegos digitales presenta una fragmentación significativa en la experiencia de compra de licencias. Los usuarios deben interactuar con múltiples plataformas aisladas para adquirir juegos, gestionar inventario, procesar pagos y recibir notificaciones, lo que genera:

- **Fragmentación en la compra de licencias digitales:** No existía un sistema unificado que conecte catálogo, carrito, pago y entrega en un solo flujo.
- **Falta de unificación en pasarelas de pago:** Cada dominio manejaba validaciones de pago de forma independiente, sin límites de monto ni detección de transacciones sospechosas.
- **Gestión ineficiente de reseñas y notificaciones:** Los usuarios no podían evaluar productos ni recibir confirmaciones estructuradas de sus transacciones.

**GameVerse** resuelve estos problemas mediante un ecosistema de 12 microservicios independientes, comunicados a través de un API Gateway centralizado, con service discovery vía Netflix Eureka, reglas de negocio robustecidas y despliegue automatizado en la nube.

---

## 2. Actores o Perfiles

| Actor | Descripción | Permisos Principales |
|-------|-------------|---------------------|
| **Cliente (Comprador)** | Usuario registrado que navega el catálogo, agrega productos al carrito, realiza pagos y publica reseñas. | Ver catálogo, gestionar carrito propio, crear pagos, crear/actualizar reseñas, recibir notificaciones. |
| **Administrador (Gestor)** | Usuario con privilegios elevados que gestiona usuarios, categorías, visualiza historiales y administra el sistema. | CRUD completo de usuarios y categorías, ver historial de carrito, acceder a todos los datos. |
| **Agente de Soporte** | Usuario con permisos intermedios que puede modificar carritos de clientes para asistencia. | Modificar/eliminar items de carrito (mismo alcance que USER en operaciones de carrito). |
| **Sistema (Moderador)** | Componente automatizado que valida integridad de datos entre servicios, procesa notificaciones y verifica existencia de entidades cruzadas. | Validar productos antes de crear órdenes, acumular/descontar inventario, enviar notificaciones automáticas. |

### Matriz de Permisos por Endpoint

| Endpoint | Cliente | Administrador | Agente | Sistema |
|----------|---------|---------------|--------|---------|
| `GET /api/v1/products/**` | Si | Si | Si | Si |
| `POST /api/v1/cart/add` | Si | Si | Si | No |
| `PUT /api/v1/cart/by-id/{id}` | Si* | Si | Si* | No |
| `DELETE /api/v1/cart/by-id/{id}` | Si | Si | Si | No |
| `GET /api/v1/cart/by-id/{id}/history` | No | Si | No | No |
| `POST /api/v1/orders` | Si | Si | Si | Si |
| `POST /api/v1/payments` | Si | Si | Si | No |
| `POST /api/v1/reviews` | Si | Si | Si | No |
| `POST /api/v1/notifications` | No | No | No | Si |
| `GET /api/v1/users/**` | No | Si | No | No |
| `POST /api/v1/categories` | No | Si | No | No |

> *Sujeto a `@PreAuthorize("@cartSecurity.canEdit(#id, authentication)")` — solo puede modificar carritos propios.

---

## 3. Requerimientos Funcionales

### 3.1 Gestión de Usuarios (User_Service_M1 / Auth_Service_M10)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-01 | Registro de usuario | `POST /api/v1/auth/register` | Crear cuenta con username unico y password (min. 4 caracteres). |
| RF-02 | Inicio de sesion | `POST /api/v1/auth/login` | Autenticar credenciales y retornar token de sesion. |
| RF-03 | Crear usuario | `POST /api/v1/users` | Registrar usuario con nombre, email valido y password encriptada. |
| RF-04 | Consultar usuario | `GET /api/v1/users/{id}` | Obtener datos de un usuario por su ID. |

### 3.2 Catálogo de Videojuegos (Product_Service_M2 / Category_Service_M9)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-05 | Listar productos | `GET /api/v1/products` | Obtener todos los videojuegos disponibles. |
| RF-06 | Consultar producto | `GET /api/v1/products/{id}` | Obtener detalle de un videojuego especifico. |
| RF-07 | Crear producto | `POST /api/v1/products` | Agregar videojuego al catalogo (nombre, precio >= 0, stock >= 0). |
| RF-08 | Actualizar producto | `PUT /api/v1/products/{id}` | Modificar nombre, precio o stock de un videojuego. |
| RF-09 | Eliminar producto | `DELETE /api/v1/products/{id}` | Remover videojuego del catalogo. |
| RF-10 | Gestion de categorias | `POST/GET/PUT/DELETE /api/v1/categories` | CRUD completo de categorias con nombre unico. |

### 3.3 Carrito de Compras (Cart_Service_M3)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-11 | Agregar al carrito | `POST /api/v1/cart/add` | Agregar videojuego al carrito. Valida duplicados y existencia de usuario. |
| RF-12 | Modificar carrito | `PUT /api/v1/cart/by-id/{id}` | Actualizar cantidad, precio o cupon de un item. Solo el propietario. |
| RF-13 | Asignar usuario | `PATCH /api/v1/cart/by-id/{id}/assign` | Reasignar un item del carrito a otro usuario. |
| RF-14 | Eliminar del carrito | `DELETE /api/v1/cart/by-id/{id}` | Remover item del carrito. |
| RF-15 | Historial de cambios | `GET /api/v1/cart/by-id/{id}/history` | Ver historial de modificaciones de un item (solo ADMIN). |

### 3.4 Procesamiento de Órdenes (Order_Service_M4)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-16 | Crear orden | `POST /api/v1/orders` | Generar orden de compra. Valida existencia del producto via Product_Service_M2. |
| RF-17 | Consultar orden | `GET /api/v1/orders/{id}` | Obtener estado y detalle de una orden. |

### 3.5 Pasarela de Pagos (Payment_Service_M5)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-18 | Procesar pago | `POST /api/v1/payments` | Registrar pago con monto, metodo y orden asociada. Bean Validation + reglas de negocio. |
| RF-19 | Consultar pago | `GET /api/v1/payments/{id}` | Obtener estado y detalle de un pago procesado. |

### 3.6 Inventario (Inventory_Service_M6)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-20 | Actualizar stock | `PUT /api/v1/inventory/{productId}?quantity={qty}` | Modificar stock de un producto. Cantidades positivas incrementan, negativas decrementan. Valida existencia del producto. |

### 3.7 Reseñas y Calificaciones (Review_Service_M7)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-21 | Crear resena | `POST /api/v1/reviews` | Publicar resena con calificacion (1-5) y comentario (10-500 chars). |
| RF-22 | Actualizar resena | `PUT /api/v1/reviews/{id}` | Modificar calificacion o comentario de una resena existente. |
| RF-23 | Consultar resenas | `GET /api/v1/reviews/product/{productId}` | Listar todas las resenas de un producto especifico. |

### 3.8 Notificaciones (Notification_Service_M8)

| ID | Requerimiento | Endpoint | Descripción |
|----|--------------|----------|-------------|
| RF-24 | Enviar notificacion | `POST /api/v1/notifications` | Enviar notificacion por EMAIL, SMS o PUSH con formato de correo valido. |
| RF-25 | Consultar notificaciones | `GET /api/v1/notifications/user/{userId}` | Listar todas las notificaciones de un usuario. |

---

## 4. Flujos Principales

### 4.1 Flujo de Compra Exitosa

```
Cliente                Gateway(:8080)          Product_Svc(:8082)       Cart_Svc(:8083)         Order_Svc(:8084)        Payment_Svc(:8085)      Inventory_Svc(:8086)
  |                         |                         |                       |                       |                       |                       |
  | 1. GET /api/v1/products |                         |                       |                       |                       |                       |
  |------------------------>|------------------------>|                       |                       |                       |                       |
  |<-- 200 [catalogo] ------|<------------------------|                       |                       |                       |                       |
  |                         |                         |                       |                       |                       |                       |
  | 3. POST /api/v1/cart/add                         |                       |                       |                       |                       |
  |------------------------>|------------------------>|                       |                       |                       |                       |
  |                         | 4. Valida duplicado y   |                       |                       |                       |                       |
  |                         |    usuario existente    |                       |                       |                       |                       |
  |<-- 201 [item carrito] --|<------------------------|                       |                       |                       |                       |
  |                         |                         |                       |                       |                       |                       |
  | 5. POST /api/v1/orders  |                         |                       |                       |                       |                       |
  |------------------------>|------------------------>|                       |                       |                       |                       |
  |                         | 6. Valida producto via  |                       |                       |                       |                       |
  |                         |    RestTemplate GET     |                       |                       |                       |                       |
  |<-- 201 [orden CREATED] -|<------------------------|                       |                       |                       |                       |
  |                         |                         |                       |                       |                       |                       |
  | 7. POST /api/v1/payments                         |                       |                       |                       |                       |
  |------------------------>|------------------------>|                       |                       |                       |                       |
  |                         | 8. Valida monto > 0,    |                       |                       |                       |                       |
  |                         |    <= $1.000.000        |                       |                       |                       |                       |
  |<-- 201 [pago SUCCESS] --|<------------------------|                       |                       |                       |                       |
  |                         |                         |                       |                       |                       |                       |
  | 9. PUT /api/v1/inventory/{productId}?quantity=-1 |                       |                       |                       |                       |
  |------------------------>|------------------------>|                       |                       |                       |                       |
  |<-- 200 [stock actualizado]                       |                       |                       |                       |                       |
```

**Pasos del flujo:**

1. El cliente consulta el catalogo de videojuegos disponibles.
2. El Gateway enruta la peticion a Product_Service_M2 via `lb://product-service`.
3. El cliente agrega un videojuego al carrito con su email.
4. Cart_Service_M3 valida que el producto no este duplicado y que el usuario exista.
5. El cliente crea una orden de compra indicando el productId y quantity.
6. Order_Service_M4 valida la existencia del producto consultando Product_Service_M2 via RestTemplate.
7. El cliente procesa el pago con el orderId, amount y paymentMethod.
8. Payment_Service_M5 valida reglas de negocio (monto > 0, limite $1.000.000) y registra el pago como SUCCESS.
9. Se decrementa el stock del producto en Inventory_Service_M6.

### 4.2 Flujo de Publicación de Reseña

```
Cliente                Gateway(:8080)          Review_Svc(:8087)        Notification_Svc(:8088)
  |                         |                         |                         |
  | 1. POST /api/v1/reviews |                         |                         |
  |------------------------>|------------------------>|                         |
  |                         | 2. Valida: rating 1-5,  |                         |
  |                         |    comment 10-500 chars |                         |
  |<-- 201 [resena creada] -|<------------------------|                         |
  |                         |                         |                         |
  | 3. POST /api/v1/notifications                      |                         |
  |------------------------>|------------------------>|                         |
  |                         | 4. Valida: email formato,|                         |
  |                         |    message 5-1000 chars |                         |
  |<-- 201 [notificacion] --|<------------------------|                         |
```

**Pasos del flujo:**

1. El cliente publica una resena para un videojuego con calificacion y comentario.
2. Review_Service_M7 valida las restricciones Bean Validation (rating 1-5, comentario 10-500 caracteres).
3. Se envia una notificacion de confirmacion al usuario.
4. Notification_Service_M8 valida el formato del correo electronico y la longitud del mensaje.

---

## 5. Reglas de Negocio y Restricciones del Dominio

### 5.1 Pasarela de Pagos (Payment_Service_M5)

| Regla | Restriccion Bean Validation | Excepcion | HTTP |
|-------|---------------------------|-----------|------|
| El ID de la orden es obligatorio | `@NotNull` en `orderId` | `MethodArgumentNotValidException` | 400 |
| El ID de la orden debe ser positivo | `@Positive` en `orderId` | `PaymentBusinessException(INVALID_ORDER_ID)` | 422 |
| El monto es obligatorio | `@NotNull` en `amount` | `MethodArgumentNotValidException` | 400 |
| El monto debe ser mayor a $0.01 | `@DecimalMin("0.01")` en `amount` | `PaymentBusinessException(INVALID_AMOUNT)` | 422 |
| El metodo de pago es obligatorio | `@NotNull` en `paymentMethod` | `MethodArgumentNotValidException` | 400 |
| El monto no puede exceder $1.000.000 | Logica en `PaymentService.validateBusinessRules()` | `PaymentBusinessException(AMOUNT_EXCEEDS_LIMIT)` | 422 |

**Metodos de pago aceptados:** `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `CASH`

### 5.2 Reseñas y Calificaciones (Review_Service_M7)

| Regla | Restriccion Bean Validation | Excepcion | HTTP |
|-------|---------------------------|-----------|------|
| El ID del producto es obligatorio | `@NotNull` en `productId` | `MethodArgumentNotValidException` | 400 |
| El ID del usuario es obligatorio | `@NotNull` en `userId` | `MethodArgumentNotValidException` | 400 |
| La calificacion es obligatoria | `@NotNull` en `rating` | `MethodArgumentNotValidException` | 400 |
| La calificacion minima es 1 estrella | `@Min(1)` en `rating` | `MethodArgumentNotValidException` | 400 |
| La calificacion maxima es 5 estrellas | `@Max(5)` en `rating` | `MethodArgumentNotValidException` | 400 |
| El comentario es obligatorio | `@NotBlank` en `comment` | `MethodArgumentNotValidException` | 400 |
| El comentario debe tener entre 10 y 500 caracteres | `@Size(min=10, max=500)` en `comment` | `MethodArgumentNotValidException` | 400 |
| El ID del producto debe ser positivo | Logica en `ReviewService.validateProductId()` | `IllegalArgumentException` | 400 |

### 5.3 Notificaciones (Notification_Service_M8)

| Regla | Restriccion Bean Validation | Excepcion | HTTP |
|-------|---------------------------|-----------|------|
| El ID del usuario es obligatorio | `@NotNull` en `userId` | `MethodArgumentNotValidException` | 400 |
| El destinatario es obligatorio | `@NotBlank` en `recipient` | `MethodArgumentNotValidException` | 400 |
| El correo debe tener formato valido | `@Email` en `recipient` | `MethodArgumentNotValidException` | 400 |
| El mensaje es obligatorio | `@NotBlank` en `message` | `MethodArgumentNotValidException` | 400 |
| El mensaje debe tener entre 5 y 1000 caracteres | `@Size(min=5, max=1000)` en `message` | `MethodArgumentNotValidException` | 400 |
| El tipo de notificacion es obligatorio | `@NotNull` en `type` | `MethodArgumentNotValidException` | 400 |

**Tipos de notificacion:** `EMAIL`, `SMS`, `PUSH`

### 5.4 Carrito de Compras (Cart_Service_M3)

| Regla | Implementacion | Excepcion | HTTP |
|-------|---------------|-----------|------|
| No se pueden agregar productos duplicados | `CartRepository.existsByProductNameIgnoreCase()` | `IllegalArgumentException` | 400 |
| El usuario del carrito debe existir | `UserService.getByEmail()` | `BadRequestException` | 400 |
| El codigo de cupon no puede ser igual al email del usuario | Validacion en `CartService.addToCart()` | `IllegalArgumentException` | 400 |
| Solo el propietario puede modificar su carrito | `@PreAuthorize("@cartSecurity.canEdit(#id, authentication)")` | `AccessDeniedException` | 403 |

### 5.5 Productos (Product_Service_M2)

| Regla | Restriccion | Excepcion | HTTP |
|-------|------------|-----------|------|
| El nombre es obligatorio | `@NotBlank` en `name` | `MethodArgumentNotValidException` | 400 |
| El precio es obligatorio y >= 0 | `@NotNull`, `@Min(0)` en `price` | `MethodArgumentNotValidException` | 400 |
| El stock es obligatorio y >= 0 | `@NotNull`, `@Min(0)` en `stock` | `MethodArgumentNotValidException` | 400 |

---

## 6. Estados Relevantes

### 6.1 Estados de una Orden (Order_Service_M4)

```
  [CREATED] ──────────▶ [PENDING] ──────────▶ [PAID]
                          │
                          ▼
                      [CANCELLED]
```

| Estado | Descripcion | Transicion |
|--------|-------------|------------|
| `CREATED` | Orden recien creada, esperando pago | Estado inicial al crear la orden |
| `PENDING` | Orden en espera de confirmacion de pago | Despues de crear la orden |
| `PAID` | Pago confirmado exitosamente | Despues de procesar pago exitoso en Payment_Service_M5 |
| `CANCELLED` | Orden cancelada por el usuario o el sistema | Cancelacion manual o timeout de pago |

### 6.2 Estados de Pago (Payment_Service_M5)

```
  [PENDING] ──────────▶ [SUCCESS]
       │
       ▼
    [FAILED]
       │
       ▼
   [REJECTED]
```

| Estado | Descripcion | Transicion |
|--------|-------------|------------|
| `PENDING` | Pago registrado, esperando procesamiento | Estado inicial en la entidad |
| `SUCCESS` | Pago procesado exitosamente | Despues de validar todas las reglas de negocio |
| `FAILED` | Pago fallido por error del sistema | Error interno durante procesamiento |
| `REJECTED` | Pago rechazado por reglas de negocio | Monto excede limite, ID invalido, etc. |

### 6.3 Estados del Carrito (Cart_Service_M3)

| Estado | Descripcion |
|--------|-------------|
| `PENDING` | Item en el carrito, esperando checkout |
| `COMPLETED` | Item procesado y convertido en orden |
| `CANCELLED` | Item removido del carrito por el usuario |

---

## 7. Ejemplos de Uso y Datos de Prueba Sugeridos

> Todos los endpoints se acceden a traves del API Gateway en **http://localhost:8080**

### 7.1 Autenticacion

**Registrar usuario:**
```json
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "username": "player1",
  "password": "game1234"
}
```

**Iniciar sesion:**
```json
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "player1",
  "password": "game1234"
}
```

### 7.2 Catalogo de Productos

**Crear videojuego (requiere auth ADMIN):**
```json
POST http://localhost:8080/api/v1/products
Content-Type: application/json

{
  "name": "The Legend of Zelda: Tears of the Kingdom",
  "price": 69.99,
  "stock": 150
}
```

**Crear videojuego con datos invalidos (prueba de validacion):**
```json
POST http://localhost:8080/api/v1/products
Content-Type: application/json

{
  "name": "",
  "price": -10,
  "stock": -5
}
```
**Respuesta esperada:** HTTP 400 con errores de validacion concatenados.

### 7.3 Carrito de Compras

**Agregar al carrito:**
```json
POST http://localhost:8080/api/v1/cart/add
Content-Type: application/json

{
  "productName": "The Legend of Zelda: Tears of the Kingdom",
  "price": 69.99,
  "quantity": 1,
  "userEmail": "ana.garcia@empresa.com"
}
```

**Agregar producto duplicado (prueba de regla de negocio):**
```json
POST http://localhost:8080/api/v1/cart/add
Content-Type: application/json

{
  "productName": "The Legend of Zelda: Tears of the Kingdom",
  "price": 69.99,
  "quantity": 1,
  "userEmail": "ana.garcia@empresa.com"
}
```
**Respuesta esperada:** HTTP 400 — "El producto The Legend of Zelda: Tears of the Kingdom ya esta en el carrito".

### 7.4 Crear Orden

**Crear orden de compra:**
```json
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "productId": 1,
  "quantity": 1
}
```

**Crear orden con producto inexistente (prueba de validacion inter-service):**
```json
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "productId": 9999,
  "quantity": 1
}
```
**Respuesta esperada:** HTTP 400 — "El producto con ID 9999 no existe."

### 7.5 Procesar Pago

**Pago exitoso:**
```json
POST http://localhost:8080/api/v1/payments
Content-Type: application/json

{
  "orderId": 1,
  "amount": 69.99,
  "paymentMethod": "CREDIT_CARD"
}
```

**Pago con monto excesivo (prueba de regla de negocio):**
```json
POST http://localhost:8080/api/v1/payments
Content-Type: application/json

{
  "orderId": 1,
  "amount": 2000000.00,
  "paymentMethod": "CREDIT_CARD"
}
```
**Respuesta esperada:** HTTP 422 — "Transaccion rechazada: el monto excede el limite de $1.000.000."

**Pago con monto invalido (prueba de Bean Validation):**
```json
POST http://localhost:8080/api/v1/payments
Content-Type: application/json

{
  "orderId": null,
  "amount": -5,
  "paymentMethod": null
}
```
**Respuesta esperada:** HTTP 400 con errores: "orderId: El ID de la orden es requerido; amount: El monto debe ser mayor a $0.01; paymentMethod: El metodo de pago es requerido".

### 7.6 Actualizar Inventario

**Decrementar stock:**
```
PUT http://localhost:8080/api/v1/inventory/1?quantity=-10
```

**Incrementar stock:**
```
PUT http://localhost:8080/api/v1/inventory/1?quantity=50
```

### 7.7 Publicar Resena

**Crear resena exitosa:**
```json
POST http://localhost:8080/api/v1/reviews
Content-Type: application/json

{
  "productId": 1,
  "userId": 1,
  "rating": 5,
  "comment": "Excelente juego, los graficos son impresionantes y la historia es muy envolvente."
}
```

**Resena con calificacion fuera de rango (prueba de validacion):**
```json
POST http://localhost:8080/api/v1/reviews
Content-Type: application/json

{
  "productId": 1,
  "userId": 1,
  "rating": 10,
  "comment": "Muy corto"
}
```
**Respuesta esperada:** HTTP 400 — "rating: La calificacion maxima es 5 estrellas; comment: El comentario debe tener entre 10 y 500 caracteres".

### 7.8 Enviar Notificación

**Notificacion por email:**
```json
POST http://localhost:8080/api/v1/notifications
Content-Type: application/json

{
  "userId": 1,
  "recipient": "ana.garcia@empresa.com",
  "message": "Su pago de $69.99 por The Legend of Zelda ha sido procesado exitosamente.",
  "type": "EMAIL"
}
```

**Notificacion con email invalido (prueba de validacion):**
```json
POST http://localhost:8080/api/v1/notifications
Content-Type: application/json

{
  "userId": 1,
  "recipient": "no-es-email",
  "message": "Hi",
  "type": "EMAIL"
}
```
**Respuesta esperada:** HTTP 400 — "recipient: El correo del destinatario debe tener un formato valido; message: El mensaje debe tener entre 5 y 1000 caracteres".

---

> **Duoc UC — Evaluación Final Transversal (actualizado 2026)**