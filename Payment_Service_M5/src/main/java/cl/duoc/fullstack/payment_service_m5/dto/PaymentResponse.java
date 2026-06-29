package cl.duoc.fullstack.payment_service_m5.dto;

public record PaymentResponse(
        Long id,
        Long orderId,
        Double amount,
        String status
) {}
