package ma.fstt.notificationservice.exceptions;

import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.ErrorResponse;
import ma.fstt.notificationservice.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.LocalDateTime;

/**
 * Gestionnaire global d'exceptions pour les controllers REST UNIQUEMENT.

 * ⚠️ IMPORTANT:
 * - Ce handler gère UNIQUEMENT les erreurs REST
 * - Les erreurs Kafka sont gérées dans NotificationConsumer avec try/catch + DLQ
 * - Les erreurs WebSocket sont gérées dans PushNotificationService avec try/catch + logs

 * Principes:
 * - Jamais exposer de messages techniques au client
 * - Jamais exposer de stacktrace au client
 * - Les messages techniques sont uniquement dans les logs
 * - Utiliser des codes d'erreur stables et standardisés
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Gestion des exceptions métier (BusinessException).
     * Ces exceptions contiennent déjà un ErrorCode standardisé.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            WebRequest request) {

        // Log technique avec le message détaillé
        log.error("Business exception: code={}, message={}",
                ex.getErrorCode().getCode(),
                ex.getTechnicalMessage(),
                ex);

        // Réponse client avec message générique
        ErrorResponse errorResponse = buildErrorResponse(
                ex.getErrorCode(),
                request
        );

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    /**
     * Gestion des exceptions UserNotFoundException.
     * (déjà héritée de BusinessException, mais peut être surchargée si besoin)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex,
            WebRequest request) {

        log.error("User not found: {}", ex.getTechnicalMessage());

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getErrorCode(),
                request
        );

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    /**
     * Gestion des exceptions NotificationNotFoundException.
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFoundException(
            NotificationNotFoundException ex,
            WebRequest request) {

        log.error("Notification not found: {}", ex.getTechnicalMessage());

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getErrorCode(),
                request
        );

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    /**
     * Gestion des exceptions InvalidNotificationDataException.
     */
    @ExceptionHandler(InvalidNotificationDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNotificationDataException(
            InvalidNotificationDataException ex,
            WebRequest request) {

        log.error("Invalid notification data: {}", ex.getTechnicalMessage());

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getErrorCode(),
                request
        );

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    /**
     * Gestion des exceptions NotificationSendException.
     */
    @ExceptionHandler(NotificationSendException.class)
    public ResponseEntity<ErrorResponse> handleNotificationSendException(
            NotificationSendException ex,
            WebRequest request) {

        log.error("Notification send error: {}", ex.getTechnicalMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getErrorCode(),
                request
        );

        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    /**
     * ⚠️ ATTENTION: KafkaProcessingException et WebSocketException
     * ne devraient JAMAIS atteindre ce handler.

     * Si elles l'atteignent, c'est qu'il y a un problème d'architecture.
     * On les gère quand même par sécurité avec un log de warning.
     */
    @ExceptionHandler({KafkaProcessingException.class, WebSocketException.class})
    public ResponseEntity<ErrorResponse> handleInfrastructureException(
            BusinessException ex,
            WebRequest request) {

        log.warn("⚠️ Infrastructure exception reached REST handler (should not happen): code={}, message={}",
                ex.getErrorCode().getCode(),
                ex.getTechnicalMessage(),
                ex);

        // On retourne une erreur générique sans détails
        ErrorResponse errorResponse = buildErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                request
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gestion de toutes les autres exceptions non prévues.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                request
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Construction d'une réponse d'erreur standardisée.
     *
     * @param errorCode le code d'erreur standardisé
     * @param request la requête web
     * @return ErrorResponse avec structure standardisée
     */
    private ErrorResponse buildErrorResponse(ErrorCode errorCode, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage()) // Message générique, jamais technique
                .path(extractPath(request))
                .build();
    }

    /**
     * Extraction du path de la requête.
     */
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace("uri=", "");
    }
}