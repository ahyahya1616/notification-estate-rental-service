package ma.fstt.notificationservice.exceptions;

import ma.fstt.notificationservice.enums.ErrorCode;

public class InvalidNotificationDataException extends BusinessException {

    public InvalidNotificationDataException(String technicalMessage) {
        super(ErrorCode.INVALID_NOTIFICATION_DATA, technicalMessage);
    }

    public InvalidNotificationDataException(String technicalMessage, Throwable cause) {
        super(ErrorCode.INVALID_NOTIFICATION_DATA, technicalMessage, cause);
    }
}