package com.eshop.inventory_service.repository;

import com.eshop.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySkuCode(String skuCode);

    @Transactional
    @Modifying
    @Query("update Inventory i set i.quantity = i.quantity - :qty where i.skuCode = :sku and i.quantity >= :qty")
    int decrementQuantityIfAvailable(@Param("sku") String sku, @Param("qty") int qty);

    List<Inventory> findBySkuCodeIn(List<String> skuCodes);
}