package cl.duoc.fullstack.cart_service_m3.controller;

import cl.duoc.fullstack.cart_service_m3.dto.AssignUserRequest;
import cl.duoc.fullstack.cart_service_m3.dto.CartHistoryResult;
import cl.duoc.fullstack.cart_service_m3.dto.CartItemCommand;
import cl.duoc.fullstack.cart_service_m3.dto.CartItemRequest;
import cl.duoc.fullstack.cart_service_m3.dto.CartItemResponse;
import cl.duoc.fullstack.cart_service_m3.dto.CartItemResult;
import cl.duoc.fullstack.cart_service_m3.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
@Tag(name = "Carrito", description = "API para gestionar items del carrito de compras")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar items del carrito", description = "Retorna todos los items del carrito, opcionalmente filtrados por estado")
    @ApiResponse(responseCode = "200", description = "Lista de items obtenida exitosamente")
    public ResponseEntity<List<CartItemResponse>> getAllItems(@RequestParam(required = false) String status) {
        List<CartItemResult> results = (status != null)
                ? this.service.getCartContent(status)
                : this.service.getCartContent();
        List<CartItemResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    @Operation(summary = "Obtener item por ID", description = "Retorna un item del carrito segun su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<CartItemResponse> getById(@PathVariable Long id) {
        return this.service.getById(id)
                .map(result -> ResponseEntity.ok(toResponse(result)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @Operation(summary = "Agregar item al carrito", description = "Agrega un nuevo producto al carrito de compras")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o producto duplicado")
    })
    public ResponseEntity<CartItemResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        CartItemResult result = this.service.addToCart(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("@cartSecurity.canEdit(#id, authentication)")
    @Operation(summary = "Actualizar item del carrito", description = "Actualiza un item existente del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<CartItemResponse> updateById(@PathVariable Long id, @Valid @RequestBody CartItemRequest request) {
        return this.service.updateById(id, toCommand(request))
                .map(result -> ResponseEntity.ok(toResponse(result)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/by-id/{id}/assign")
    @PreAuthorize("@cartSecurity.canEdit(#id, authentication)")
    @Operation(summary = "Asignar usuario a item", description = "Asigna un usuario a un item del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario asignado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<CartItemResponse> assignUser(@PathVariable Long id, @Valid @RequestBody AssignUserRequest request) {
        return this.service.assignUser(id, request.userEmail())
                .map(result -> ResponseEntity.ok(toResponse(result)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @Operation(summary = "Eliminar item del carrito", description = "Elimina un item del carrito por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (this.service.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/by-id/{id}/history")
    @Operation(summary = "Obtener historial de un item", description = "Retorna el historial de cambios de un item del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<List<CartHistoryResult>> getHistory(@PathVariable Long id) {
        return this.service.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private CartItemCommand toCommand(CartItemRequest request) {
        return new CartItemCommand(
                request.productName(),
                request.price(),
                request.quantity(),
                request.userEmail(),
                request.couponCode()
        );
    }

    private CartItemResponse toResponse(CartItemResult result) {
        return new CartItemResponse(
                result.id(),
                result.productName(),
                result.price(),
                result.quantity(),
                result.couponCode(),
                result.status(),
                result.user()
        );
    }
}
