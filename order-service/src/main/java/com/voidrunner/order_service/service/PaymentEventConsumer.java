package com.voidrunner.order_service.service;

import com.voidrunner.common.events.PaymentCompletedEvent;
import com.voidrunner.common.events.PaymentFailedEvent;
import com.voidrunner.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository orderRepository;

    public PaymentEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "payment.completed", groupId = "order-service-group")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("PAID");
            orderRepository.save(order);
            log.info("Order {} status updated to PAID", event.getOrderId());
        });
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service-group")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("PAYMENT_FAILED");
            orderRepository.save(order);
            log.info("Order {} status updated to PAYMENT_FAILED - reason: {}", event.getOrderId(), event.getReason());
        });
    }
}