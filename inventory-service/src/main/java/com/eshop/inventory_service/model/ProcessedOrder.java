package com.eshop.inventory_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
    // newly added
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<OrderLineItemEvent> orderItems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderLineItemEvent {
        private String skuCode;
        private Integer quantity;
        private BigDecimal price;
    }
}
