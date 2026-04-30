package com.ojosama.common.config.kafka;

import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

//Kafka 공통 설정
//String key + String value (JSON 페이로드는 OutboxMessage 단계에서 직렬화)
//Producer: acks=all + idempotence=true → 중복 발행 방지, 손실 최소화
//Consumer: 수동 ack는 사용 안 함 — inbox 패턴으로 멱등성 확보 + auto commit 비활성
//에러 핸들러: 기본 재시도 3회 + 1초 백오프. 초과 시 컨슈머 측 로깅 (DLQ는 추후 도입)
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:default-group}")
    private String groupId;

    // ── Producer 설정 (공통 프로퍼티) ──────────────────────────────
    private Map<String, Object> commonProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 안정성 핵심: acks=all + 멱등성 활성화
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // 지적사항 반영: 멱등성 사용 시 retries는 설정하지 않고 delivery.timeout.ms로 제어
        // 기본값 120000ms(2분) 동안 프로듀서가 전송 성공을 위해 무한 재시도합니다.
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        return props;
    }

    // ── 1. String 전용 KafkaTemplate (Outbox Poller 등에서 사용) ──
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> props = commonProducerProps();
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    //비즈니스 로직에서 kafkaTemplate.send(...)를 호출
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    // ── 2. Object(JSON) 전용 KafkaTemplate (일반 이벤트 직접 발행 시 사용) ──
    @Bean
    public ProducerFactory<String, Object> jsonProducerFactory() {
        Map<String, Object> props = commonProducerProps();
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> jsonKafkaTemplate() {
        return new KafkaTemplate<>(jsonProducerFactory());
    }

    // ── Consumer ────────────────────────────────────────────────
//    DefaultKafkaConsumerFactory: 메시지를 읽어올 컨슈머를 찍어내는 공장
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); //직접 로직(Inbox 등)을 다 마친 뒤에 확정 짓기 위해서
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // 1초 간격으로 3번 재시도 후 SKIP (DLQ 도입 시 여기에 DeadLetterPublishingRecoverer 연결)
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

}
