package com.voidrunner.payment_service.service;

import com.voidrunner.common.events.PaymentCompletedEvent;
import com.voidrunner.common.events.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String COMPLETED_TOPIC = "payment.completed";
    private static final String FAILED_TOPIC = "payment.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCompleted(PaymentCompletedEvent event) {
        kafkaTemplate.send(COMPLETED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish payment.completed for order {}: {}", event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Published payment.completed for order {}", event.getOrderId());
                    }
                });
    }

    public void publishFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(FAILED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish payment.failed for order {}: {}", event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Published payment.failed for order {}", event.getOrderId());
                    }
                });
    }
}