package com.voidrunner.fulfillment_service.service;

import com.voidrunner.common.events.FulfillmentCompletedEvent;
import com.voidrunner.common.events.PaymentCompletedEvent;
import com.voidrunner.fulfillment_service.entity.FulfillmentTask;
import com.voidrunner.fulfillment_service.repository.FulfillmentTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedConsumer.class);

    private final FulfillmentTaskRepository fulfillmentTaskRepository;
    private final FulfillmentEventProducer fulfillmentEventProducer;

    public PaymentCompletedConsumer(FulfillmentTaskRepository fulfillmentTaskRepository,
                                     FulfillmentEventProducer fulfillmentEventProducer) {
        this.fulfillmentTaskRepository = fulfillmentTaskRepository;
        this.fulfillmentEventProducer = fulfillmentEventProducer;
    }

    @KafkaListener(topics = "payment.completed", groupId = "fulfillment-service-group")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info("Received payment.completed for order: {}", event.getOrderId());

        if (fulfillmentTaskRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("Fulfillment already processed for order {}, skipping", event.getOrderId());
            return;
        }

        FulfillmentTask task = new FulfillmentTask();
        task.setOrderId(event.getOrderId());
        task.setStatus("COMPLETED");
        fulfillmentTaskRepository.save(task);

        log.info("Fulfillment completed for order {}", event.getOrderId());
        fulfillmentEventProducer.publishCompleted(new FulfillmentCompletedEvent(event.getOrderId()));
    }
}