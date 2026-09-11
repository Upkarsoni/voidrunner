package com.voidrunner.common.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrderCreatedEvent {

    private UUID orderId;
    private String customerName;
    private List<OrderItemEvent> items;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(UUID orderId, String customerName, List<OrderItemEvent> items) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.items = items;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<OrderItemEvent> getItems() { return items; }
    public void setItems(List<OrderItemEvent> items) { this.items = items; }

    public static class OrderItemEvent {
        private String sku;
        private UUID warehouseId;
        private Integer quantity;

        public OrderItemEvent() {}

        public OrderItemEvent(String sku, UUID warehouseId, Integer quantity) {
            this.sku = sku;
            this.warehouseId = warehouseId;
            this.quantity = quantity;
        }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public UUID getWarehouseId() { return warehouseId; }
        public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}