package cl.duoc.fullstack.notification_service_m8.controller;

import cl.duoc.fullstack.notification_service_m8.dto.NotificationRequest;
import cl.duoc.fullstack.notification_service_m8.dto.NotificationResponse;
import cl.duoc.fullstack.notification_service_m8.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notificaciones", description = "API para gestionar notificaciones")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Enviar una notificacion", description = "Crea y envia una notificacion a un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificacion creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos")
    })
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse created = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener notificaciones por usuario", description = "Retorna todas las notificaciones de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida exitosamente")
    })
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(@PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }
}
