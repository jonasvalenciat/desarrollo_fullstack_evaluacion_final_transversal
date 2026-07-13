package cl.duoc.fullstack.notification_service_m8.dto;

import cl.duoc.fullstack.notification_service_m8.model.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotNull(message = "El ID del usuario es requerido")
        Long userId,

        @NotBlank(message = "El destinatario es requerido")
        @Email(message = "El correo del destinatario debe tener un formato valido")
        String recipient,

        @NotBlank(message = "El mensaje es requerido")
        @Size(min = 5, max = 1000, message = "El mensaje debe tener entre 5 y 1000 caracteres")
        String message,

        @NotNull(message = "El tipo de notificacion es requerido")
        NotificationType type
) {}
