package com.voidrunner.inventory_service.service;

import com.voidrunner.common.events.OrderCreatedEvent;
import com.voidrunner.inventory_service.dto.ReservationResponse;
import com.voidrunner.inventory_service.dto.ReserveInventoryRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    public OrderEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "order.created", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {

        System.out.println("Received order.created event for order: " + event.getOrderId());

        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {

            ReserveInventoryRequest request = new ReserveInventoryRequest();
            request.setIdempotencyKey(event.getOrderId().toString() + "-" + item.getSku());
            request.setSku(item.getSku());
            request.setWarehouseId(item.getWarehouseId());
            request.setQuantity(item.getQuantity());

            ReservationResponse response = inventoryService.reserveStock(request);

            System.out.println("Reservation for SKU " + item.getSku() + ": " + response.isSuccess()
                    + " - " + response.getMessage());
        }
    }
}