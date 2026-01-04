package ma.fstt.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.NotificationEvent;
import ma.fstt.notificationservice.services.NotificationService;
import ma.fstt.notificationservice.services.DeadLetterQueueService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumer Kafka pour le traitement des événements de notification.
 *
 * Gestion des erreurs:
 * - Try/catch local pour capturer toutes les exceptions
 * - Logs détaillés des erreurs (stacktrace complet)
 * - Envoi vers DLQ en cas d'échec
 * - Acknowledgment manuel pour contrôler le commit
 *
 * ⚠️ Les exceptions Kafka ne remontent JAMAIS au GlobalExceptionHandler REST
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final DeadLetterQueueService dlqService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.notification}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(
            ConsumerRecord<String, NotificationEvent> record,
            Acknowledgment acknowledgment) {

        String topic = record.topic();
        int partition = record.partition();
        long offset = record.offset();
        String key = record.key();

        log.info("Kafka message received: topic={}, partition={}, offset={}, key={}",
                topic, partition, offset, key);

        try {
            NotificationEvent event = record.value();

            // Validation de base
            if (event == null) {
                log.warn("Null notification event received, skipping. topic={}, offset={}",
                        topic, offset);
                acknowledgment.acknowledge();
                return;
            }

            if (event.getUserIds() == null || event.getUserIds().isEmpty()) {
                log.warn("Invalid notification event (no users), skipping. topic={}, offset={}, event={}",
                        topic, offset, event);
                acknowledgment.acknowledge();
                return;
            }

            // Traitement de la notification
            log.info("Processing notification: eventType={}, users={}, channels={}",
                    event.getEventType(),
                    event.getUserIds().size(),
                    event.getChannels());

            notificationService.processNotification(event);

            // Acknowledge après traitement réussi
            acknowledgment.acknowledge();

            log.info("Notification processed successfully: topic={}, offset={}, users={}",
                    topic, offset, event.getUserIds().size());

        } catch (Exception e) {
            // Log complet de l'erreur avec stacktrace
            log.error("Error processing Kafka message: topic={}, partition={}, offset={}, key={}",
                    topic, partition, offset, key, e);

            log.error("Exception details: message={}, cause={}",
                    e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "N/A");

            // Tentative d'envoi vers DLQ
            try {
                String payload = objectMapper.writeValueAsString(record.value());
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                String stackTrace = buildStackTrace(e);

                dlqService.saveToDeadLetterQueue(
                        topic,
                        partition,
                        offset,
                        payload,
                        errorMessage,
                        stackTrace
                );

                // Acknowledge pour ne pas bloquer le consumer
                acknowledgment.acknowledge();

                log.info("📨 Message sent to DLQ: topic={}, partition={}, offset={}",
                        topic, partition, offset);

            } catch (Exception dlqException) {
                // Échec d'envoi vers DLQ - c'est critique
                log.error("🚨 CRITICAL: Failed to save message to DLQ: topic={}, offset={}",
                        topic, offset, dlqException);

                // Ne pas acknowledger pour que Kafka retry
                log.warn("⏳ Message will be retried by Kafka: topic={}, offset={}", topic, offset);
            }
        }
    }

    /**
     * Construction de la stacktrace complète pour les logs et la DLQ.
     */
    private String buildStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        // Ajouter la cause si elle existe
        if (e.getCause() != null) {
            sb.append("Caused by: ");
            sb.append(buildStackTrace((Exception) e.getCause()));
        }

        return sb.toString();
    }
}