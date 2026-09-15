package com.voidrunner.common.events;

import java.util.UUID;

public class InventoryReservedEvent {

    private UUID orderId;
    private String sku;
    private Integer quantity;

    public InventoryReservedEvent() {}

    public InventoryReservedEvent(UUID orderId, String sku, Integer quantity) {
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}