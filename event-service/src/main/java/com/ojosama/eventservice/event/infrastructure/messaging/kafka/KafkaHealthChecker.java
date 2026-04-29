package com.ojosama.eventservice.event.infrastructure.messaging.kafka;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHealthChecker {

    private static final int CONNECT_TIMEOUT_SECONDS = 5;

    private final KafkaAdmin kafkaAdmin;

    @Value("${spring.kafka.topic.event-created}")
    private String eventCreatedTopic;

    @Value("${spring.kafka.topic.event-deleted}")
    private String eventDeletedTopic;

    @Value("${spring.kafka.topic.event-updated}")
    private String eventUpdatedTopic;

    @Value("${spring.kafka.topic.schedule-changed}")
    private String scheduleChangedTopic;

    @EventListener(ApplicationReadyEvent.class)
    public void checkOnStartup() {
        String bootstrapServers = String.valueOf(
                kafkaAdmin.getConfigurationProperties().get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));

        log.info("[Kafka] 브로커 연결 확인 중... bootstrapServers={}", bootstrapServers);

        Map<String, Object> config = Map.copyOf(kafkaAdmin.getConfigurationProperties());
        try (AdminClient adminClient = AdminClient.create(config)) {
            Set<String> existingTopics = adminClient.listTopics()
                    .names()
                    .get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("[Kafka] 브로커 연결 성공. 전체 토픽 수={}", existingTopics.size());
            checkRequiredTopics(existingTopics);

        } catch (TimeoutException e) {
            throw new IllegalStateException(
                    "[Kafka] 브로커 연결 타임아웃 (" + CONNECT_TIMEOUT_SECONDS + "s), bootstrapServers=" + bootstrapServers, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[Kafka] 브로커 연결 확인 중 인터럽트 발생", e);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "[Kafka] 브로커 연결 실패, bootstrapServers=" + bootstrapServers, e);
        }
    }

    private void checkRequiredTopics(Set<String> existingTopics) {
        List<String> required = List.of(
                eventCreatedTopic,
                eventDeletedTopic,
                eventUpdatedTopic,
                scheduleChangedTopic
        );

        List<String> missing = required.stream()
                .filter(t -> !existingTopics.contains(t))
                .toList();

        if (missing.isEmpty()) {
            log.info("[Kafka] 필수 토픽 확인 완료: {}", required);
        } else {
            throw new IllegalStateException("[Kafka] 필수 토픽 누락: " + missing);
        }
    }
}
