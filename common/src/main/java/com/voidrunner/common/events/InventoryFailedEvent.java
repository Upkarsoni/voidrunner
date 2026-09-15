package com.voidrunner.common.events;

import java.util.UUID;

public class InventoryFailedEvent {

    private UUID orderId;
    private String sku;
    private String reason;

    public InventoryFailedEvent() {}

    public InventoryFailedEvent(UUID orderId, String sku, String reason) {
        this.orderId = orderId;
        this.sku = sku;
        this.reason = reason;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}