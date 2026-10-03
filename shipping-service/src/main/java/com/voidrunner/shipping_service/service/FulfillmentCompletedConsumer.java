package com.voidrunner.shipping_service.service;

import com.voidrunner.common.events.FulfillmentCompletedEvent;
import com.voidrunner.common.events.ShipmentDispatchedEvent;
import com.voidrunner.shipping_service.entity.Shipment;
import com.voidrunner.shipping_service.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FulfillmentCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentCompletedConsumer.class);

    private final ShipmentRepository shipmentRepository;
    private final ShippingEventProducer shippingEventProducer;

    public FulfillmentCompletedConsumer(ShipmentRepository shipmentRepository,
                                         ShippingEventProducer shippingEventProducer) {
        this.shipmentRepository = shipmentRepository;
        this.shippingEventProducer = shippingEventProducer;
    }

    @KafkaListener(topics = "fulfillment.completed", groupId = "shipping-service-group")
    @Transactional
    public void handleFulfillmentCompleted(FulfillmentCompletedEvent event) {

        log.info("Received fulfillment.completed for order: {}", event.getOrderId());

        if (shipmentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("Shipment already processed for order {}, skipping", event.getOrderId());
            return;
        }

        Shipment shipment = new Shipment();
        shipment.setOrderId(event.getOrderId());
        shipment.setStatus("DISPATCHED");
        shipmentRepository.save(shipment);

        log.info("Shipment dispatched for order {}", event.getOrderId());
        shippingEventProducer.publishDispatched(new ShipmentDispatchedEvent(event.getOrderId()));
    }
}