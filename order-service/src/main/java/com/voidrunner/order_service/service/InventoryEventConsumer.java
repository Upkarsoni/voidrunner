package com.voidrunner.order_service.service;

import com.voidrunner.common.events.InventoryFailedEvent;
import com.voidrunner.common.events.InventoryReservedEvent;
import com.voidrunner.order_service.entity.Order;
import com.voidrunner.order_service.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryEventConsumer {

    private final OrderRepository orderRepository;

    public InventoryEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "order-service-group")
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
            System.out.println("Order " + event.getOrderId() + " status updated to CONFIRMED");
        });
    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-service-group")
    @Transactional
    public void handleInventoryFailed(InventoryFailedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            System.out.println("Order " + event.getOrderId() + " status updated to CANCELLED - reason: " + event.getReason());
        });
    }
}