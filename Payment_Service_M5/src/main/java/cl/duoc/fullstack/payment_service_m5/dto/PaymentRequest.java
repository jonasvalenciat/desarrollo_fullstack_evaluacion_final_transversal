package cl.duoc.fullstack.payment_service_m5.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "El ID de la orden es requerido")
        Long orderId,

        @NotNull(message = "El monto es requerido")
        @DecimalMin(value = "0.1", message = "El monto debe ser mayor a 0")
        Double amount
) {}
