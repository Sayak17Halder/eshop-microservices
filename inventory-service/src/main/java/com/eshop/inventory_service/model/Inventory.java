package com.eshop.inventory_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skuCode;

    private Integer quantity;

    /**
     * Domain behavior – safe stock decrease
     */
    public void decrease(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.quantity < qty) {
            throw new IllegalStateException("Insufficient stock for SKU " + skuCode);
        }
        this.quantity -= qty;
    }
}