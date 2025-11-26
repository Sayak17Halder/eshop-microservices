package com.eshop.inventory_service.controller;

import com.eshop.inventory_service.model.Inventory;
import com.eshop.inventory_service.repository.InventoryRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryRepository repo;

    public InventoryController(InventoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{skuCode}")
    public boolean isInStock(@PathVariable String skuCode) {
        return repo.findBySkuCode(skuCode)
                .map(inv -> inv.getQuantity() > 0)
                .orElse(false);
    }
}