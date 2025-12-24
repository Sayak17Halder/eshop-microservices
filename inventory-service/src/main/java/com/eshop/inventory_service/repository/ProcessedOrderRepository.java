package com.eshop.inventory_service.repository;

import com.eshop.inventory_service.model.ProcessedOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM ProcessedOrder p
        WHERE p.orderNumber = :orderNumber
    """)
    Optional<ProcessedOrder> findByOrderNumberForUpdate(String orderNumber);

    boolean existsByOrderNumberAndStatus(
            String orderNumber,
            String status
    );
}
