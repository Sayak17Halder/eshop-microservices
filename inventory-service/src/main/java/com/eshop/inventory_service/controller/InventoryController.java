package com.eshop.inventory_service.controller;

import com.eshop.inventory_service.exception.InventoryNotFoundException;
import com.eshop.inventory_service.model.Inventory;
import com.eshop.inventory_service.model.InventoryResponse;
import com.eshop.inventory_service.repository.InventoryRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryRepository repo;

    public InventoryController(InventoryRepository repo) {
        this.repo = repo;
    }

    // CHECK STOCK BY SKU CODE
    @GetMapping("/{skuCode}")
    public boolean isInStock(@PathVariable String skuCode) {
        return repo.findBySkuCode(skuCode)
                .map(inv -> inv.getQuantity() > 0)
                .orElse(false);
    }

    // New bulk endpoint (POST or GET) — we'll use POST for long lists
    @PostMapping("/availability")
    public List<InventoryResponse> checkAvailability(@RequestBody List<String> skuCodes) {

//        return skuCodes.stream()
//                .map(sku -> repo.findBySkuCode(sku)
//                        .map(inv -> new InventoryResponse(sku, inv.getQuantity() > 0, inv.getQuantity()))
//                        .orElse(new InventoryResponse(sku, false, 0)))
//                .collect(Collectors.toList());

        List<Inventory> inventories = repo.findBySkuCodeIn(skuCodes);
        Map<String, Inventory> map = inventories.stream().collect(Collectors.toMap(Inventory::getSkuCode, Function.identity()));
        return skuCodes.stream()
                .map(sku -> {
                    Inventory inv = map.get(sku);
                    if (inv == null) return new InventoryResponse(sku, false, 0);
                    return new InventoryResponse(sku, inv.getQuantity() > 0, inv.getQuantity());
                })
                .collect(Collectors.toList());
    }

    // GET FULL INVENTORY BY SKU — RETURN OBJECT
    @GetMapping("/detail/{skuCode}")
    public Inventory getInventory(@PathVariable String skuCode) {
        return repo.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("SKU not found: " + skuCode));
    }

    // Create a new inventory entry
    @PostMapping
    public Inventory createInventory(@RequestBody Inventory request) {
        return repo.save(request);
    }

    // Update/set inventory quantity
    @PutMapping("/{skuCode}/set/{quantity}")
    public Inventory setQuantity(@PathVariable String skuCode, @PathVariable Integer quantity) {
        Inventory inv = repo.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("SKU not found: " + skuCode));
        inv.setQuantity(quantity);
        return repo.save(inv);
    }

    // Increase quantity
    @PutMapping("/{skuCode}/increase/{amount}")
    public Inventory increaseQuantity(@PathVariable String skuCode, @PathVariable Integer amount) {
        Inventory inv = repo.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("SKU not found: " + skuCode));
        inv.setQuantity(inv.getQuantity() + amount);
        return repo.save(inv);
    }

    // Decrease quantity (validates stock)
    @PutMapping("/{skuCode}/decrease/{amount}")
    public Inventory decreaseQuantity(@PathVariable String skuCode, @PathVariable Integer amount) {
        Inventory inv = repo.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("SKU not found: " + skuCode));

        if (inv.getQuantity() < amount) {
            throw new RuntimeException("Not enough stock for SKU: " + skuCode);
        }

        inv.setQuantity(inv.getQuantity() - amount);
        return repo.save(inv);
    }

    // NEW: List all in-stock items
    @GetMapping("/in-stock")
    public List<Inventory> getAllInStock() {
        return repo.findAll().stream()
                .filter(inv -> inv.getQuantity() > 0)
                .toList();
    }

    // List all out-of-stock items
    @GetMapping("/out-of-stock")
    public List<Inventory> getOutOfStock() {
        return repo.findAll().stream()
                .filter(inv -> inv.getQuantity() <= 0)
                .toList();
    }

    // Delete an inventory entry by SKU
    @DeleteMapping("/{skuCode}")
    public ResponseEntity<String> deleteSku(@PathVariable String skuCode) {
        Inventory inv = repo.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException("SKU not found: " + skuCode));
        repo.delete(inv);
        return ResponseEntity.ok("Deleted SKU: " + skuCode);
    }
}