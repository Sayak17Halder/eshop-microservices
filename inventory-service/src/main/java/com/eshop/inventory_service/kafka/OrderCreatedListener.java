package com.eshop.inventory_service.kafka;

import com.eshop.inventory_service.event.OrderCreatedEvent; // or your package
import com.eshop.inventory_service.event.OrderFailedDomainEvent;
import com.eshop.inventory_service.event.OrderReservedDomainEvent;
import com.eshop.inventory_service.model.Inventory;
import com.eshop.inventory_service.model.ProcessedOrder;
import com.eshop.inventory_service.repository.InventoryRepository;
import com.eshop.inventory_service.repository.ProcessedOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private final ProcessedOrderRepository processedOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryEventProducer inventoryEventProducer;

    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("⬅ OrderCreatedEvent received | orderNumber={}", event.getOrderNumber());

        // 1️⃣ Idempotency
        if (processedOrderRepository.existsById(event.getOrderNumber())) {
            log.warn("⚠ Order {} already processed. Skipping.", event.getOrderNumber());
            return;
        }

        // 2️⃣ Extract SKUs
        List<String> skuCodes = event.getOrderItems()
                .stream()
                .map(OrderCreatedEvent.OrderLineItemEvent::getSkuCode)
                .toList();

        // 3️⃣ Lock inventory rows (PESSIMISTIC_WRITE)
        List<Inventory> inventories =
                inventoryRepository.findBySkuCodeInForUpdate(skuCodes);

        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getSkuCode, i -> i));

        // 4️⃣ Validate ALL items first
        for (OrderCreatedEvent.OrderLineItemEvent item : event.getOrderItems()) {
            Inventory inv = inventoryMap.get(item.getSkuCode());
            if (inv == null || inv.getQuantity() < item.getQuantity()) {

                log.error("❌ Insufficient stock | order={} sku={}",
                        event.getOrderNumber(), item.getSkuCode());

                processedOrderRepository.save(
                        ProcessedOrder.builder()
                                .orderNumber(event.getOrderNumber())
                                .status("FAILED")
                                .processedAt(Instant.now())
                                .build()
                );

                // Publish domain event (not Kafka!)
                inventoryEventProducer.publishOrderFailedEvent(
                        new OrderFailedDomainEvent(
                                event.getOrderNumber(),
                                "OUT_OF_STOCK"
                        )
                );

                return; // NO EXCEPTION → no retry, no partial decrement
            }
        }

        // 5️⃣ Decrement ALL SKUs
        for (OrderCreatedEvent.OrderLineItemEvent item : event.getOrderItems()) {
            inventoryMap.get(item.getSkuCode())
                    .decrease(item.getQuantity());
        }

        // 6️⃣ Mark as RESERVED
        processedOrderRepository.save(
                ProcessedOrder.builder()
                        .orderNumber(event.getOrderNumber())
                        .status("RESERVED")
                        .processedAt(Instant.now())
                        .build()
        );

        // Publish domain event
        inventoryEventProducer.publishOrderReservedEvent(new OrderReservedDomainEvent(event.getOrderNumber()));

        log.info("✔ Inventory reserved | orderNumber={}", event.getOrderNumber());

    }
}