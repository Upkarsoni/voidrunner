package com.voidrunner.inventory_service.service;

import com.voidrunner.common.events.InventoryFailedEvent;
import com.voidrunner.common.events.InventoryReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);

    private static final String RESERVED_TOPIC = "inventory.reserved";
    private static final String FAILED_TOPIC = "inventory.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReserved(InventoryReservedEvent event) {
        kafkaTemplate.send(RESERVED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish inventory.reserved for order {}: {}",
                                event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Published inventory.reserved for order {}", event.getOrderId());
                    }
                });
    }

    public void publishFailed(InventoryFailedEvent event) {
        kafkaTemplate.send(FAILED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish inventory.failed for order {}: {}",
                                event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Published inventory.failed for order {}", event.getOrderId());
                    }
                });
    }
}