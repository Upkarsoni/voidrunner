package com.voidrunner.order_service.service;

import com.voidrunner.common.events.ShipmentDispatchedEvent;
import com.voidrunner.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ShippingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShippingEventConsumer.class);

    private final OrderRepository orderRepository;

    public ShippingEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "shipment.dispatched", groupId = "order-service-group")
    @Transactional
    public void handleShipmentDispatched(ShipmentDispatchedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("SHIPPED");
            orderRepository.save(order);
            log.info("Order {} status updated to SHIPPED", event.getOrderId());
        });
    }
}