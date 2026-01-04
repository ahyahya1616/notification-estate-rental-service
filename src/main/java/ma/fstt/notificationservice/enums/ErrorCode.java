package ma.fstt.notificationservice.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum standardisé des codes d'erreur pour l'API REST.
 * Ces codes sont stables, versionnés et indépendants des messages techniques.
 */
@Getter
public enum ErrorCode {

    // Erreurs liées aux utilisateurs (4xx)
    USER_NOT_FOUND(
            "USER_NOT_FOUND",
            "User not found",
            HttpStatus.NOT_FOUND
    ),

    // Erreurs liées aux notifications (4xx)
    NOTIFICATION_NOT_FOUND(
            "NOTIFICATION_NOT_FOUND",
            "Notification not found",
            HttpStatus.NOT_FOUND
    ),

    INVALID_NOTIFICATION_DATA(
            "INVALID_NOTIFICATION_DATA",
            "Invalid notification data provided",
            HttpStatus.BAD_REQUEST
    ),

    // Erreurs serveur (5xx)
    NOTIFICATION_SEND_ERROR(
            "NOTIFICATION_SEND_ERROR",
            "Failed to send notification",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    NOTIFICATION_PROCESSING_ERROR(
            "NOTIFICATION_PROCESSING_ERROR",
            "Error occurred while processing notification",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    // Erreurs Kafka (5xx) - Ne devraient jamais atteindre le client REST
    KAFKA_PROCESSING_ERROR(
            "KAFKA_PROCESSING_ERROR",
            "Internal message processing error",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    // Erreurs WebSocket (5xx) - Ne devraient jamais atteindre le client REST
    WEBSOCKET_ERROR(
            "WEBSOCKET_ERROR",
            "Real-time communication error",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    // Erreur générique (5xx)
    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}