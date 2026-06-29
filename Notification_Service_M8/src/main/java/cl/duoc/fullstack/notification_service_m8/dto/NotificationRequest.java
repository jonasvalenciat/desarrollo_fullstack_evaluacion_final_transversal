package cl.duoc.fullstack.notification_service_m8.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull(message = "El ID del usuario es requerido")
        Long userId,

        @NotBlank(message = "El mensaje es requerido")
        String message,

        @NotBlank(message = "El tipo es requerido")
        String type
) {}
