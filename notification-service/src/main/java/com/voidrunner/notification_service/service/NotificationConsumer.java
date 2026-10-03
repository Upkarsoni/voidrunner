package com.voidrunner.notification_service.service;

import com.voidrunner.common.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "order.created", groupId = "notification-service-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("📧 Notification: Order {} created for customer {}", event.getOrderId(), event.getCustomerName());
    }

    @KafkaListener(topics = "payment.completed", groupId = "notification-service-group")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("📧 Notification: Payment completed for order {}", event.getOrderId());
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service-group")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("📧 Notification: Payment failed for order {} - {}", event.getOrderId(), event.getReason());
    }

    @KafkaListener(topics = "shipment.dispatched", groupId = "notification-service-group")
    public void onShipmentDispatched(ShipmentDispatchedEvent event) {
        log.info("📧 Notification: Order {} has been shipped!", event.getOrderId());
    }
}