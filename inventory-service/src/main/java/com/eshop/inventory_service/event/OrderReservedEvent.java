package com.eshop.inventory_service.event;

import lombok.*;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReservedEvent {

    private String eventId;
    private Instant eventTimestamp;

    private String orderNumber;
}
