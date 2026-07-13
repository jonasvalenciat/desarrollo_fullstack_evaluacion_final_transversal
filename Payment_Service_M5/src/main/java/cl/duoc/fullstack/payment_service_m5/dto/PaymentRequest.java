package cl.duoc.fullstack.payment_service_m5.dto;

import cl.duoc.fullstack.payment_service_m5.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotNull(message = "El ID de la orden es requerido")
        @Positive(message = "El ID de la orden debe ser un número positivo")
        Long orderId,

        @NotNull(message = "El monto es requerido")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a $0.01")
        Double amount,

        @NotNull(message = "El método de pago es requerido")
        PaymentMethod paymentMethod
) {}
