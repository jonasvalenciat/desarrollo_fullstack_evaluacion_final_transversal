package cl.duoc.fullstack.payment_service_m5.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String code,
        LocalDateTime timestamp
) {
    public ErrorResponse(String message) {
        this(message, null, LocalDateTime.now());
    }

    public ErrorResponse(String message, String code) {
        this(message, code, LocalDateTime.now());
    }
}
