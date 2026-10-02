package com.voidrunner.shipping_service.service;

import com.voidrunner.common.events.ShipmentDispatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShippingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ShippingEventProducer.class);
    private static final String TOPIC = "shipment.dispatched";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShippingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDispatched(ShipmentDispatchedEvent event) {
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish shipment.dispatched for order {}: {}", event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Published shipment.dispatched for order {}", event.getOrderId());
                    }
                });
    }
}