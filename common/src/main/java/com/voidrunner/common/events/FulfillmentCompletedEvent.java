package com.voidrunner.common.events;

import java.util.UUID;

public class FulfillmentCompletedEvent {

    private UUID orderId;

    public FulfillmentCompletedEvent() {}

    public FulfillmentCompletedEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
}