package ma.fstt.notificationservice.exceptions;

import ma.fstt.notificationservice.enums.ErrorCode;

/**
 * Exception pour les erreurs WebSocket.
 * ⚠️ Cette exception ne doit JAMAIS être exposée au client REST.
 * Elle est gérée uniquement dans le service WebSocket avec try/catch + logs.
 */
public class WebSocketException extends BusinessException {

    public WebSocketException(String technicalMessage) {
        super(ErrorCode.WEBSOCKET_ERROR, technicalMessage);
    }

    public WebSocketException(String technicalMessage, Throwable cause) {
        super(ErrorCode.WEBSOCKET_ERROR, technicalMessage, cause);
    }
}