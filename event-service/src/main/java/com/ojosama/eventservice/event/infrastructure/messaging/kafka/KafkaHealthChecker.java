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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class KafkaHealthChecker {

    private static final int CONNECT_TIMEOUT_SECONDS = 5;

    private final KafkaAdmin kafkaAdmin;
    private final KafkaTopicProperties topics;

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
            log.error("[Kafka] 브로커 연결 타임아웃 ({}s). Kafka가 실행 중인지 확인하세요. bootstrapServers={}",
                    CONNECT_TIMEOUT_SECONDS, bootstrapServers);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Kafka] 브로커 연결 확인 중 인터럽트 발생");
        } catch (Exception e) {
            log.error("[Kafka] 브로커 연결 실패: {} | bootstrapServers={}", e.getMessage(), bootstrapServers);
        }
    }

    private void checkRequiredTopics(Set<String> existingTopics) {
        List<String> required = List.of(
                topics.eventCreated(),
                topics.eventDeleted(),
                topics.eventUpdated(),
                topics.scheduleChanged()
        );

        List<String> missing = required.stream()
                .filter(t -> !existingTopics.contains(t))
                .toList();

        if (missing.isEmpty()) {
            log.info("[Kafka] 필수 토픽 확인 완료: {}", required);
        } else {
            log.warn("[Kafka] 미생성 토픽 발견: {} — auto.create.topics.enable=true 이거나 수동 생성이 필요합니다", missing);
        }
    }
}
