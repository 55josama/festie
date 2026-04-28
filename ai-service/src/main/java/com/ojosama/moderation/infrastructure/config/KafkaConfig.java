package com.ojosama.moderation.infrastructure.config;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration("aiModerationKafkaConfig")
public class KafkaConfig {
    // 채팅 데이터 처리를 위한 팩토리(빈번하게 일어남)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> chatBatchFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(new StringJsonMessageConverter()));

        // 최대 100개의 데이터를 가져오고, 데이터가 다 안 차더라도 0.1초가 지나면 가져오도록 설정
        Properties props = new Properties();
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10240);
        factory.getContainerProperties().setKafkaConsumerProperties(props);

        return factory;
    }

    // 게시글/댓글 데이터 처리를 위한 팩토리(덜 빈번하게 일어남)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> communityBatchFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(new StringJsonMessageConverter()));

        // 최대 50개의 데이터를 가져오고, 데이터가 다 안 차더라도 2초가 지나면 가져오도록 설정
        Properties props = new Properties();
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 2000);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10240);
        factory.getContainerProperties().setKafkaConsumerProperties(props);

        return factory;
    }
}
