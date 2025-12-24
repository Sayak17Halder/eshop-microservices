package com.eshop.inventory_service.event.outgoing;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReservedEvent {

    private String eventId;
    private Instant eventTimestamp;
    private String orderNumber;
    private BigDecimal amount;
}
