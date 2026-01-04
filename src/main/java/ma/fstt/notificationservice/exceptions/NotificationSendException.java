package ma.fstt.notificationservice.exceptions;

import ma.fstt.notificationservice.enums.ErrorCode;

public class NotificationSendException extends BusinessException {

    public NotificationSendException(String technicalMessage) {
        super(ErrorCode.NOTIFICATION_SEND_ERROR, technicalMessage);
    }

    public NotificationSendException(String technicalMessage, Throwable cause) {
        super(ErrorCode.NOTIFICATION_SEND_ERROR, technicalMessage, cause);
    }
}
