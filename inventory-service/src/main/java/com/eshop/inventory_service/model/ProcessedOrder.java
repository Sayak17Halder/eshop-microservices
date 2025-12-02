package com.eshop.inventory_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "processed_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedOrder {
    @Id
    private String orderNumber;
    private String status; // e.g. RESERVED, FAILED
    private Instant processedAt;
}
