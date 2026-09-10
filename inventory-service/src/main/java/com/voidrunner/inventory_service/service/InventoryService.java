package com.voidrunner.inventory_service.service;

import com.voidrunner.inventory_service.dto.AddStockRequest;
import com.voidrunner.inventory_service.dto.ReservationResponse;
import com.voidrunner.inventory_service.dto.ReserveInventoryRequest;
import com.voidrunner.inventory_service.entity.Inventory;
import com.voidrunner.inventory_service.entity.ReservationRequest;
import com.voidrunner.inventory_service.exception.InsufficientStockException;
import com.voidrunner.inventory_service.repository.InventoryRepository;
import com.voidrunner.inventory_service.repository.ReservationRequestRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryService {

    private static final int MAX_RETRIES = 3;

    private final InventoryRepository inventoryRepository;
    private final ReservationRequestRepository reservationRequestRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                             ReservationRequestRepository reservationRequestRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRequestRepository = reservationRequestRepository;
    }

    @Transactional
    public Inventory addStock(AddStockRequest request) {
        Inventory inventory = inventoryRepository
                .findBySkuAndWarehouseId(request.getSku(), request.getWarehouseId())
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setSku(request.getSku());
                    newInventory.setWarehouseId(request.getWarehouseId());
                    newInventory.setAvailableQty(0);
                    newInventory.setReservedQty(0);
                    return newInventory;
                });

        inventory.setAvailableQty(inventory.getAvailableQty() + request.getQuantity());
        return inventoryRepository.save(inventory);
    }

    public ReservationResponse reserveStock(ReserveInventoryRequest request) {

        // Step 1: Idempotency check — same request pehle already process hui?
        var existing = reservationRequestRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            ReservationRequest prev = existing.get();
            boolean wasSuccess = "SUCCESS".equals(prev.getStatus());
            return new ReservationResponse(
                    wasSuccess,
                    wasSuccess ? "Already reserved (idempotent replay)" : "Previously failed (idempotent replay)",
                    prev.getSku(),
                    wasSuccess ? prev.getQuantity() : 0,
                    null
            );
        }

        // Step 2: Retry loop — optimistic locking conflicts ke liye
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            attempts++;
            try {
                return attemptReservation(request);
            } catch (OptimisticLockingFailureException e) {
                // Kisi doosre thread ne beech me update kar diya — retry karo
                if (attempts >= MAX_RETRIES) {
                    return recordAndReturn(request, false, "Reservation failed after retries due to high contention", 0);
                }
                // Chhota sa wait karke retry (backoff)
                try {
                    Thread.sleep(20L * attempts);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return recordAndReturn(request, false, "Reservation failed", 0);
    }

    @Transactional
    protected ReservationResponse attemptReservation(ReserveInventoryRequest request) {

        Inventory inventory = inventoryRepository
                .findBySkuAndWarehouseId(request.getSku(), request.getWarehouseId())
                .orElseThrow(() -> new InsufficientStockException(
                        "No inventory found for SKU: " + request.getSku()));

        if (inventory.getAvailableQty() < request.getQuantity()) {
            return recordAndReturn(request, false, "Insufficient stock", inventory.getAvailableQty());
        }

        inventory.setAvailableQty(inventory.getAvailableQty() - request.getQuantity());
        inventory.setReservedQty(inventory.getReservedQty() + request.getQuantity());

        // Yahi save() call hai jahan @Version check hoga —
        // agar version mismatch hua, OptimisticLockingFailureException throw hoga
        Inventory saved = inventoryRepository.save(inventory);

        return recordAndReturn(request, true, "Reservation successful", saved.getAvailableQty());
    }

    private ReservationResponse recordAndReturn(ReserveInventoryRequest request, boolean success,
                                                  String message, int remainingAvailable) {
        ReservationRequest record = new ReservationRequest();
        record.setIdempotencyKey(request.getIdempotencyKey());
        record.setSku(request.getSku());
        record.setWarehouseId(request.getWarehouseId());
        record.setQuantity(request.getQuantity());
        record.setStatus(success ? "SUCCESS" : "FAILED");
        reservationRequestRepository.save(record);

        return new ReservationResponse(success, message, request.getSku(),
                success ? request.getQuantity() : 0, remainingAvailable);
    }
}