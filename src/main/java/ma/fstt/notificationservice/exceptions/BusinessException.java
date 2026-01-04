package ma.fstt.notificationservice.exceptions;

import lombok.Getter;
import ma.fstt.notificationservice.enums.ErrorCode;

/**
 * Exception de base pour toutes les exceptions métier.
 * Contient un ErrorCode et un message technique pour les logs.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String technicalMessage;

    protected BusinessException(ErrorCode errorCode, String technicalMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.technicalMessage = technicalMessage;
    }

    protected BusinessException(ErrorCode errorCode, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
        this.technicalMessage = technicalMessage;
    }
}