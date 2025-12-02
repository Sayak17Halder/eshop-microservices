package com.eshop.order_service.service;

import com.eshop.order_service.dto.InventoryResponse;
import com.eshop.order_service.dto.OrderRequest;
import com.eshop.order_service.event.OrderCreatedEvent;
import com.eshop.order_service.exception.ProductOutOfStockException;
import com.eshop.order_service.model.*;
import com.eshop.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {

        List<String> skus = orderRequest.getOrderItems().stream()
                .map(i -> i.getSkuCode())
                .collect(Collectors.toList());

        // single request to inventory-service bulk endpoint using service discovery
        Mono<List<InventoryResponse>> inventoryMono = webClientBuilder.build()
                .post()
                .uri("lb://inventory-service/api/inventory/availability")
                .bodyValue(skus)
                .retrieve()
                .bodyToFlux(InventoryResponse.class)
                .timeout(Duration.ofSeconds(10))
                .collectList();
                // apply timeout (reactor)
//                .timeout(java.time.Duration.ofSeconds(3));

        // Block once here after all reactive operators: keeps synchronous repository save while avoiding multiple blocks
        List<InventoryResponse> inventoryList = inventoryMono.block();

        // Validate all items present in inventory
        boolean allInStock = orderRequest.getOrderItems().stream().allMatch(item -> {
            return inventoryList.stream()
                    .filter(ir -> ir.getSkuCode().equalsIgnoreCase(item.getSkuCode()))
                    .findFirst()
                    .map(InventoryResponse::isInStock)
                    .orElse(false);
        });

        if (!allInStock) {
            throw new ProductOutOfStockException("One or more items are out of stock");
        }

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .amount(orderRequest.getAmount())
                .orderItems(
                        orderRequest.getOrderItems().stream().map(
                                item -> OrderLineItem.builder()
                                        .skuCode(item.getSkuCode())
                                        .price(item.getPrice())
                                        .quantity(item.getQuantity())
                                        .build()
                        ).collect(Collectors.toList())
                )
                .build();

        orderRepository.save(order);
        OrderCreatedEvent event = buildEventFromOrder(order);
        kafkaTemplate.send("order-created", order.getOrderNumber(), event);
        return order.getOrderNumber();
    }

    private OrderCreatedEvent buildEventFromOrder(Order order) {
        List<OrderCreatedEvent.OrderLineItemEvent> orderItemEvents = order.getOrderItems()
                .stream()
                .map(item -> new OrderCreatedEvent.OrderLineItemEvent(
                        item.getSkuCode(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        return new OrderCreatedEvent(
                order.getOrderNumber(),
                order.getAmount(),
                orderItemEvents
        );
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

}
