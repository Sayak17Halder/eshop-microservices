package com.eshop.inventory_service.event.outgoing;

import lombok.*;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderFailedEvent {

    private String eventId;
    private Instant eventTimestamp;

    private String orderNumber;
    private String reason; // e.g. OUT_OF_STOCK
}
