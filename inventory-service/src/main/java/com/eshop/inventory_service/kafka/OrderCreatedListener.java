package com.eshop.inventory_service.kafka;

import com.eshop.inventory_service.event.OrderCreatedEvent; // or your package
import com.eshop.inventory_service.model.ProcessedOrder;
import com.eshop.inventory_service.repository.InventoryRepository;
import com.eshop.inventory_service.repository.ProcessedOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final InventoryRepository repo;
    private final ProcessedOrderRepository processedOrderRepository;

    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Idempotency: if order already processed, skip
        boolean already = processedOrderRepository.existsById(event.getOrderNumber());
        if (already) {
            // already processed - ack and return
            return;
        }

        // Try to decrement for all items atomically (per item)
        for (OrderCreatedEvent.OrderLineItemEvent item : event.getOrderItems()) {
            int updated = repo.decrementQuantityIfAvailable(item.getSkuCode(), item.getQuantity());
            if (updated == 0) {
                // insufficient stock: mark processed as FAILED and throw so error handler can route to DLQ or retry
                processedOrderRepository.save(ProcessedOrder.builder()
                        .orderNumber(event.getOrderNumber())
                        .status("FAILED")
                        .processedAt(Instant.now())
                        .build());
                throw new RuntimeException("Insufficient stock for SKU: " + item.getSkuCode());
            }
        }

        // All decrements succeeded — save processed marker
        processedOrderRepository.save(ProcessedOrder.builder()
                .orderNumber(event.getOrderNumber())
                .status("RESERVED")
                .processedAt(Instant.now())
                .build());
    }
}