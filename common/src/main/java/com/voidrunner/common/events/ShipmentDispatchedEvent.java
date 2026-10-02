package com.voidrunner.common.events;

import java.util.UUID;

public class ShipmentDispatchedEvent {

    private UUID orderId;

    public ShipmentDispatchedEvent() {}

    public ShipmentDispatchedEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
}