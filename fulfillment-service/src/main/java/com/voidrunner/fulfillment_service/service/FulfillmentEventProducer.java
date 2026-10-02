package com.voidrunner.fulfillment_service.service;

import com.voidrunner.common.events.FulfillmentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentEventProducer.class);
    private static final String TOPIC = "fulfillment.completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FulfillmentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCompleted(FulfillmentCompletedEvent event) {
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish fulfillment.completed for order {}: {}", event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Published fulfillment.completed for order {}", event.getOrderId());
                    }
                });
    }
}