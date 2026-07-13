package cl.duoc.fullstack.payment_service_m5.controller;

import cl.duoc.fullstack.payment_service_m5.dto.PaymentRequest;
import cl.duoc.fullstack.payment_service_m5.dto.PaymentResponse;
import cl.duoc.fullstack.payment_service_m5.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Pagos", description = "API para procesar pagos de ordenes")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Procesar un pago", description = "Procesa un pago para una orden y retorna el resultado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago procesado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada")
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago por ID", description = "Retorna los detalles de un pago registrado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "422", description = "Pago no encontrado")
    })
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }
}
