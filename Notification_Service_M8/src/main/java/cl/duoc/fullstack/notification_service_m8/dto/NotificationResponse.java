package cl.duoc.fullstack.notification_service_m8.dto;

public record NotificationResponse(
        Long id,
        Long userId,
        String message,
        String type
) {}
