package com.voidrunner.order_service.service;

import com.voidrunner.common.events.FulfillmentCompletedEvent;
import com.voidrunner.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FulfillmentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentEventConsumer.class);

    private final OrderRepository orderRepository;

    public FulfillmentEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "fulfillment.completed", groupId = "order-service-group")
    @Transactional
    public void handleFulfillmentCompleted(FulfillmentCompletedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("FULFILLED");
            orderRepository.save(order);
            log.info("Order {} status updated to FULFILLED", event.getOrderId());
        });
    }
}