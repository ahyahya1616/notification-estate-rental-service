package ma.fstt.notificationservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.NotificationDTO;
import ma.fstt.notificationservice.entities.UserNotification;
import ma.fstt.notificationservice.exceptions.WebSocketException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service d'envoi de notifications Push via WebSocket.
 *
 * Gestion des erreurs:
 * - Try/catch local pour capturer toutes les exceptions WebSocket
 * - Logs détaillés des erreurs
 * - Throw WebSocketException pour signaler l'échec au service appelant
 *
 * ⚠️ Les exceptions WebSocket ne remontent JAMAIS au GlobalExceptionHandler REST
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envoie une notification push via WebSocket.
     *
     * @throws WebSocketException si l'envoi échoue
     */
    public void sendPushNotification(UserNotification userNotification, Map<String, String> metadata) {
        Long userId = userNotification.getUserId();
        String destination = "/topic/notifications/" + userId;

        try {
            log.info("📤 Sending push notification: userId={}, destination={}", userId, destination);

            NotificationDTO dto = NotificationDTO.builder()
                    .id(userNotification.getId())
                    .userId(userId)
                    .eventType(userNotification.getNotification().getEventType())
                    .title(userNotification.getNotification().getTitle())
                    .message(userNotification.getNotification().getMessage())
                    .status(userNotification.getStatus())
                    .sentAt(userNotification.getSentAt())
                    .metadata(metadata)
                    .build();

            messagingTemplate.convertAndSend(destination, dto);

            log.info("✅ Push notification sent successfully: userId={}, notificationId={}",
                    userId, userNotification.getId());

        } catch (MessagingException e) {
            log.error("❌ WebSocket messaging error: userId={}, destination={}, error={}",
                    userId, destination, e.getMessage(), e);

            throw new WebSocketException(
                    String.format("Failed to send push notification to userId=%d via destination=%s",
                            userId, destination),
                    e
            );

        } catch (Exception e) {
            log.error("❌ Unexpected error sending push notification: userId={}, error={}",
                    userId, e.getMessage(), e);

            throw new WebSocketException(
                    String.format("Unexpected error sending push notification to userId=%d", userId),
                    e
            );
        }
    }

    /**
     * Envoie une notification à tous les utilisateurs connectés (broadcast).
     */
    public void broadcastNotification(NotificationDTO notification) {
        String destination = "/topic/notifications/broadcast";

        try {
            log.info("📡 Broadcasting notification: destination={}, eventType={}",
                    destination, notification.getEventType());

            messagingTemplate.convertAndSend(destination, notification);

            log.info("✅ Broadcast notification sent successfully");

        } catch (Exception e) {
            log.error("❌ Error broadcasting notification: destination={}, error={}",
                    destination, e.getMessage(), e);

            throw new WebSocketException(
                    String.format("Failed to broadcast notification to destination=%s", destination),
                    e
            );
        }
    }
}