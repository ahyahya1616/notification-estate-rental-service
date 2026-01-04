package ma.fstt.notificationservice.repositories;

import ma.fstt.notificationservice.dto.NotificationDTO;
import ma.fstt.notificationservice.entities.UserNotification;
import ma.fstt.notificationservice.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserNotificationRepository extends JpaRepository<UserNotification , Long> {

    Long countByUserIdAndStatus(Long userId, Status status);

    /**
     * ✅ AJOUTÉ : Récupère toutes les notifications d'un utilisateur triées par date (plus récentes en premier)
     */
    List<UserNotification> findByUserIdOrderBySentAtDesc(Long userId);

    /**
     * OPTIONNEL : Récupère les notifications non lues uniquement
     */
    List<UserNotification> findByUserIdAndStatusOrderBySentAtDesc(Long userId, Status status);

}
