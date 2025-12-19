package com.eshop.inventory_service.repository;

import com.eshop.inventory_service.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.skuCode IN :skuCodes ORDER BY i.skuCode")
    List<Inventory> findBySkuCodeInForUpdate(List<String> skuCodes);

    List<Inventory> findBySkuCodeIn(List<String> skuCodes);
}