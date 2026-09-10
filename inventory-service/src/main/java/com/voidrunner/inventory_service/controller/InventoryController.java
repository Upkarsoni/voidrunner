package com.voidrunner.inventory_service.controller;

import com.voidrunner.inventory_service.dto.AddStockRequest;
import com.voidrunner.inventory_service.dto.ReservationResponse;
import com.voidrunner.inventory_service.dto.ReserveInventoryRequest;
import com.voidrunner.inventory_service.entity.Inventory;
import com.voidrunner.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock")
    public ResponseEntity<Inventory> addStock(@Valid @RequestBody AddStockRequest request) {
        Inventory inventory = inventoryService.addStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReserveInventoryRequest request) {
        ReservationResponse response = inventoryService.reserveStock(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
}