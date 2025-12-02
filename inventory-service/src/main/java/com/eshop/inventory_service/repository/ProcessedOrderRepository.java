package com.eshop.inventory_service.repository;

import com.eshop.inventory_service.model.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, String> {
}
