package com.acuver.order_demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaTopicInitializer {

    private final KafkaProperties kafkaProperties;

    @Value("${kafka.topics.order:order-topic}")
    private String orderTopic;

    @Value("${kafka.topic.partitions:3}")
    private int partitions;

    @Value("${kafka.topic.replication-factor:1}")
    private short replicationFactor;

    /**
     * AdminClient bean based on Spring Boot's KafkaProperties
     */
    @Bean
    public AdminClient kafkaAdminClient() {
        java.util.Map<String, Object> cfg = new java.util.HashMap<>();
        cfg.put(org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                String.join(",", kafkaProperties.getBootstrapServers()));
        return AdminClient.create(cfg);
    }

    /**
     * Runner that checks & creates required topics
     */
    @Bean
    public ApplicationRunner topicCreator(AdminClient adminClient) {
        return args -> {
            try {
                Set<String> existing = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
                if (!existing.contains(orderTopic)) {
                    log.info("Creating missing Kafka topic: {} (partitions={}, replicationFactor={})", orderTopic, partitions, replicationFactor);
                    NewTopic newTopic = new NewTopic(orderTopic, partitions, replicationFactor);
                    adminClient.createTopics(Collections.singleton(newTopic)).all().get(10, TimeUnit.SECONDS);
                    log.info("Topic {} created", orderTopic);
                } else {
                    log.info("Kafka topic {} already exists", orderTopic);
                }
            } catch (Exception e) {
                log.error("Failed to create Kafka topics", e);
            }
        };
    }
} 