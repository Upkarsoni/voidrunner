package com.voidrunner.inventory_service.repository;

import com.voidrunner.inventory_service.entity.ReservationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRequestRepository extends JpaRepository<ReservationRequest, UUID> {

    Optional<ReservationRequest> findByIdempotencyKey(String idempotencyKey);
}