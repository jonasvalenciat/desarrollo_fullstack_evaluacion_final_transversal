package cl.duoc.fullstack.notification_service_m8.dto;

import cl.duoc.fullstack.notification_service_m8.model.NotificationType;

public record NotificationResponse(
        Long id,
        Long userId,
        String recipient,
        String message,
        NotificationType type
) {}
