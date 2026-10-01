package com.voidrunner.payment_service.service;

import com.voidrunner.common.events.InventoryReservedEvent;
import com.voidrunner.common.events.PaymentCompletedEvent;
import com.voidrunner.common.events.PaymentFailedEvent;
import com.voidrunner.payment_service.entity.Payment;
import com.voidrunner.payment_service.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Component
public class InventoryReservedConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservedConsumer.class);
    private final Random random = new Random();

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public InventoryReservedConsumer(PaymentRepository paymentRepository,
                                      PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "payment-service-group")
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {

        log.info("Received inventory.reserved for order: {}", event.getOrderId());

        // Idempotency check
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("Payment already processed for order {}, skipping", event.getOrderId());
            return;
        }

        // Mock payment simulation — 90% success rate
        boolean success = random.nextInt(100) < 90;

        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setAmount(BigDecimal.valueOf(event.getQuantity()).multiply(BigDecimal.valueOf(100))); // mock amount
        payment.setStatus(success ? "COMPLETED" : "FAILED");
        paymentRepository.save(payment);

        if (success) {
            log.info("Payment successful for order {}", event.getOrderId());
            paymentEventProducer.publishCompleted(new PaymentCompletedEvent(event.getOrderId()));
        } else {
            log.info("Payment failed for order {}", event.getOrderId());
            paymentEventProducer.publishFailed(new PaymentFailedEvent(event.getOrderId(), "Mock payment gateway declined"));
        }
    }
}