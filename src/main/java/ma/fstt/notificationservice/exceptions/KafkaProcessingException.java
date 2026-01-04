package ma.fstt.notificationservice.exceptions;

import ma.fstt.notificationservice.enums.ErrorCode;

/**
 * Exception pour les erreurs Kafka.
 * ⚠️ Cette exception ne doit JAMAIS être exposée au client REST.
 * Elle est gérée uniquement dans le consumer Kafka avec try/catch + logs + DLQ.
 */
public class KafkaProcessingException extends BusinessException {

    public KafkaProcessingException(String technicalMessage) {
        super(ErrorCode.KAFKA_PROCESSING_ERROR, technicalMessage);
    }

    public KafkaProcessingException(String technicalMessage, Throwable cause) {
        super(ErrorCode.KAFKA_PROCESSING_ERROR, technicalMessage, cause);
    }
}