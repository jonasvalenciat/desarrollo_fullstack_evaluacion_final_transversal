package cl.duoc.fullstack.payment_service_m5.dto;

import cl.duoc.fullstack.payment_service_m5.model.PaymentMethod;
import cl.duoc.fullstack.payment_service_m5.model.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Double amount,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        LocalDateTime createdAt
) {}
