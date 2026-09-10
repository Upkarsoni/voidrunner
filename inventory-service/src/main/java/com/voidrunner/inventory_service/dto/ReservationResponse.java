package com.voidrunner.inventory_service.dto;

public class ReservationResponse {

    private boolean success;
    private String message;
    private String sku;
    private Integer reservedQuantity;
    private Integer remainingAvailable;

    public ReservationResponse() {}

    public ReservationResponse(boolean success, String message, String sku, Integer reservedQuantity, Integer remainingAvailable) {
        this.success = success;
        this.message = message;
        this.sku = sku;
        this.reservedQuantity = reservedQuantity;
        this.remainingAvailable = remainingAvailable;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public Integer getRemainingAvailable() { return remainingAvailable; }
    public void setRemainingAvailable(Integer remainingAvailable) { this.remainingAvailable = remainingAvailable; }
}