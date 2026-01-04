package ma.fstt.notificationservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.NotificationDTO;
import ma.fstt.notificationservice.dto.NotificationEvent;
import ma.fstt.notificationservice.entities.Notification;
import ma.fstt.notificationservice.entities.UserNotification;
import ma.fstt.notificationservice.enums.Channel;
import ma.fstt.notificationservice.enums.Status;
import ma.fstt.notificationservice.exceptions.InvalidNotificationDataException;
import ma.fstt.notificationservice.exceptions.NotificationSendException;
import ma.fstt.notificationservice.repositories.NotificationRepository;
import ma.fstt.notificationservice.repositories.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public void processNotification(NotificationEvent event) {

        if (event == null || event.getUserIds() == null || event.getUserIds().isEmpty()) {
            throw new InvalidNotificationDataException("L'événement de notification ou les utilisateurs sont invalides.");
        }

        log.info("Processing notification event: type={}, users={}",
                event.getEventType(), event.getUserIds().size());

        Notification notification = Notification.builder()
                .eventType(event.getEventType())
                .title(event.getTitle())
                .message(event.getMessage())
                .metadata(event.getMetadata())
                .build();

        List<UserNotification> userNotifications = new ArrayList<>();

        for (Long userId : event.getUserIds()) {
            for (Channel channel : event.getChannels()) {

                UserNotification un = UserNotification.builder()
                        .notification(notification)
                        .userId(userId)
                        .channel(channel)
                        .status(Status.UNREAD)
                        .build();

                userNotifications.add(un);
            }
        }

        notification.setUserNotifications(userNotifications);
        notificationRepository.save(notification);

        try {
            sendNotifications(event, userNotifications);
        } catch (Exception e) {
            throw new NotificationSendException("Erreur lors de l'envoi des notifications.", e);
        }
    }

    public Long countUnreadByUserId(Long userId) {
        return userNotificationRepository.countByUserIdAndStatus(userId, Status.UNREAD);
    }

    /**
     * ✅ MÉTHODE AJOUTÉE : Récupère toutes les notifications d'un utilisateur
     */
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        log.info("Fetching notifications for userId={}", userId);

        List<UserNotification> userNotifications = userNotificationRepository
                .findByUserIdOrderBySentAtDesc(userId);

        return userNotifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ MÉTHODE AJOUTÉE : Convertit UserNotification en NotificationDTO
     */
    private NotificationDTO convertToDTO(UserNotification userNotification) {
        Notification notification = userNotification.getNotification();

        return NotificationDTO.builder()
                .id(userNotification.getId())
                .userId(userNotification.getUserId())
                .eventType(notification.getEventType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(userNotification.getStatus())
                .sentAt(userNotification.getSentAt())
                .metadata(notification.getMetadata()) // ✅ CORRECTION : Récupérer les metadata de la BDD
                .build();
    }

    private void sendNotifications(NotificationEvent event, List<UserNotification> userNotifications) {

        for (UserNotification un : userNotifications) {
            try {
                if (un.getChannel() == Channel.PUSH) {
                    // ✅ CORRECTION : Passer les metadata de l'événement
                    pushNotificationService.sendPushNotification(un, event.getMetadata());
                }

                un.setStatus(Status.UNREAD);
                un.setSentAt(LocalDateTime.now());

                log.info("Notification sent: userId={}, channel={}", un.getUserId(), un.getChannel());

            } catch (Exception e) {
                un.setStatus(Status.FAILED);
                log.error("Failed to send notification userId={} channel={}", un.getUserId(), un.getChannel(), e);
            }
        }

        userNotificationRepository.saveAll(userNotifications);
    }

    @Transactional
    public void markAsRead(Long userNotificationId) {
        userNotificationRepository.findById(userNotificationId)
                .ifPresent(userNotification -> {
                    userNotification.setStatus(Status.READ);
                    userNotificationRepository.save(userNotification); // ✅ AJOUTÉ : Sauvegarder explicitement
                    log.info("Notification marked as read: id={}", userNotificationId);
                });
    }
}