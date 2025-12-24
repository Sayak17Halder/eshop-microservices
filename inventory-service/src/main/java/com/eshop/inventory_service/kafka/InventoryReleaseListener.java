package com.eshop.inventory_service.kafka;

import com.eshop.inventory_service.event.domain.InventoryReleasedDomainEvent;
import com.eshop.inventory_service.event.incoming.PaymentFailedEvent;
import com.eshop.inventory_service.model.ProcessedOrder;
import com.eshop.inventory_service.repository.InventoryRepository;
import com.eshop.inventory_service.repository.ProcessedOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReleaseListener {

    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderRepository processedOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @KafkaListener(
            topics = "payment-failed",
            groupId = "inventory-release-group",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.info("Processing inventory release for order {}", event.getOrderNumber());

        // Strong idempotency
        if (processedOrderRepository
                .existsByOrderNumberAndStatus(event.getOrderNumber(), "RELEASED")) {
            log.warn("Inventory already released for order {}", event.getOrderNumber());
            return;
        }

        ProcessedOrder processedOrder =
                processedOrderRepository
                        .findByOrderNumberForUpdate(event.getOrderNumber())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "ProcessedOrder not found: " + event.getOrderNumber()
                                )
                        );

        List<ProcessedOrder.OrderLineItemEvent> items =
                processedOrder.getOrderItems();

        // Lock inventory rows in consistent order (deadlock safe)
        List<String> skuCodes = items.stream()
                .map(ProcessedOrder.OrderLineItemEvent::getSkuCode)
                .sorted()
                .toList();

        inventoryRepository.findBySkuCodeInForUpdate(skuCodes);

        // Atomic inventory release
        for (ProcessedOrder.OrderLineItemEvent item : items) {

            int updated =
                    inventoryRepository.incrementQuantity(
                            item.getSkuCode(),
                            item.getQuantity()
                    );

            if (updated != 1) {
                throw new IllegalStateException(
                        "Failed to release inventory for SKU " + item.getSkuCode()
                );
            }
        }

        // Saga state transition
        processedOrder.setStatus("RELEASED");
        processedOrder.setProcessedAt(Instant.now());

        log.info("Inventory released successfully for order {}", event.getOrderNumber());

        // AFTER COMMIT
        eventPublisher.publishEvent(
                new InventoryReleasedDomainEvent(event.getOrderNumber())
        );
    }
}

