package com.voidrunner.fulfillment_service.repository;

import com.voidrunner.fulfillment_service.entity.FulfillmentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FulfillmentTaskRepository extends JpaRepository<FulfillmentTask, UUID> {
    Optional<FulfillmentTask> findByOrderId(UUID orderId);
}