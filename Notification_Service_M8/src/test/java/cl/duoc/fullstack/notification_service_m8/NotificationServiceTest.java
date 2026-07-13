package cl.duoc.fullstack.notification_service_m8;

import cl.duoc.fullstack.notification_service_m8.dto.NotificationRequest;
import cl.duoc.fullstack.notification_service_m8.dto.NotificationResponse;
import cl.duoc.fullstack.notification_service_m8.model.Notification;
import cl.duoc.fullstack.notification_service_m8.model.NotificationType;
import cl.duoc.fullstack.notification_service_m8.repository.NotificationRepository;
import cl.duoc.fullstack.notification_service_m8.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void sendNotification_WithValidRequest_ShouldReturnNotificationResponse() {
        // Given
        NotificationRequest request = new NotificationRequest(1L, "usuario@test.com", "Producto agregado", NotificationType.EMAIL);
        Notification saved = new Notification(1L, 1L, "usuario@test.com", "Producto agregado", NotificationType.EMAIL);
        when(notificationRepository.save(any())).thenReturn(saved);

        // When
        NotificationResponse response = notificationService.sendNotification(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.userId());
        assertEquals("usuario@test.com", response.recipient());
        assertEquals("Producto agregado", response.message());
        assertEquals(NotificationType.EMAIL, response.type());
        verify(notificationRepository).save(any());
    }

    @Test
    void sendNotification_ShouldSetAllFieldsCorrectly() {
        // Given
        NotificationRequest request = new NotificationRequest(2L, "admin@empresa.cl", "Pago recibido", NotificationType.SMS);
        Notification saved = new Notification(2L, 2L, "admin@empresa.cl", "Pago recibido", NotificationType.SMS);
        when(notificationRepository.save(any())).thenReturn(saved);

        // When
        NotificationResponse response = notificationService.sendNotification(request);

        // Then
        assertEquals(2L, response.userId());
        assertEquals("admin@empresa.cl", response.recipient());
        assertEquals("Pago recibido", response.message());
        assertEquals(NotificationType.SMS, response.type());
    }

    @Test
    void getNotificationsByUser_ShouldReturnList() {
        // Given
        Notification notif1 = new Notification(1L, 1L, "user1@test.com", "Mensaje 1", NotificationType.PUSH);
        Notification notif2 = new Notification(2L, 1L, "user1@test.com", "Mensaje 2", NotificationType.EMAIL);
        when(notificationRepository.findByUserId(1L)).thenReturn(List.of(notif1, notif2));

        // When
        List<NotificationResponse> responses = notificationService.getNotificationsByUser(1L);

        // Then
        assertEquals(2, responses.size());
        assertEquals("Mensaje 1", responses.get(0).message());
        assertEquals(NotificationType.EMAIL, responses.get(1).type());
    }

    @Test
    void getNotificationsByUser_WithNoNotifications_ShouldReturnEmptyList() {
        // Given
        when(notificationRepository.findByUserId(999L)).thenReturn(List.of());

        // When
        List<NotificationResponse> responses = notificationService.getNotificationsByUser(999L);

        // Then
        assertTrue(responses.isEmpty());
    }
}
