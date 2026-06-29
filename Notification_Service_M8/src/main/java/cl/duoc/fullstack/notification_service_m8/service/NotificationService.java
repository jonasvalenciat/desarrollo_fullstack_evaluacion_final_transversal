package cl.duoc.fullstack.notification_service_m8.service;

import cl.duoc.fullstack.notification_service_m8.dto.NotificationRequest;
import cl.duoc.fullstack.notification_service_m8.dto.NotificationResponse;
import cl.duoc.fullstack.notification_service_m8.model.Notification;
import cl.duoc.fullstack.notification_service_m8.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Enviando notificacion de tipo [{}] al usuario ID: {}", request.type(), request.userId());

        Notification notification = new Notification();
        notification.setUserId(request.userId());
        notification.setMessage(request.message());
        notification.setType(request.type());

        Notification saved = notificationRepository.save(notification);
        log.info("Notificacion guardada exitosamente con ID: {}", saved.getId());
        return toResponse(saved);
    }

    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getMessage(),
                notification.getType()
        );
    }
}
