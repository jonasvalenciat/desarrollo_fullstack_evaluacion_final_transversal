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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos")
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
