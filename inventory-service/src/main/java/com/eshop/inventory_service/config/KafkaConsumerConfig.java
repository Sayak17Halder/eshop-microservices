package com.eshop.inventory_service.config;

import com.eshop.inventory_service.event.incoming.OrderCreatedEvent;
import com.eshop.inventory_service.event.incoming.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {

        // Configure exponential retries
        ExponentialBackOffWithMaxRetries backoff =
                new ExponentialBackOffWithMaxRetries(5);     // retry 5 times

        backoff.setInitialInterval(2000);                 // 2 seconds
        backoff.setMultiplier(2.0);                      // exponential
        backoff.setMaxInterval(30000);                   // 30 seconds

        DefaultErrorHandler handler = new DefaultErrorHandler(backoff);

        // Log failures
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {

            log.warn(
                    "Kafka retry attempt={} topic={} partition={} offset={} key={} error={}",
                    deliveryAttempt,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    ex.getMessage(),
                    ex
            );
        });

        return handler;
    }

    // ---------------- ORDER CREATED EVENT ---------------- (@KafkaListener + containerFactory)

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderCreatedConsumerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderCreatedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
    orderCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    // ---------------- PAYMENT FAILED EVENT ---------------- (@KafkaListener + containerFactory)

    @Bean
    public ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(PaymentFailedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent>
    paymentFailedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentFailedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}