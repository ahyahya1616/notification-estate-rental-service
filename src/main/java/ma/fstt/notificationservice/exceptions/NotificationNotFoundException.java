
package ma.fstt.notificationservice.exceptions;

import ma.fstt.notificationservice.enums.ErrorCode;

public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException(Long id) {
        super(
                ErrorCode.NOTIFICATION_NOT_FOUND,
                String.format("Notification not found with ID: %d", id)
        );
    }

    public NotificationNotFoundException(String technicalMessage) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, technicalMessage);
    }
}