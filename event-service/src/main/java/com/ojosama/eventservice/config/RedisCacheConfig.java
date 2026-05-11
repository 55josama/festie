package com.ojosama.eventservice.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration(after = RedisAutoConfiguration.class)
@EnableCaching
@Slf4j
public class RedisCacheConfig {

    public RedisCacheConfig() {
        log.debug("RedisCacheConfig 빈 생성됨");
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("Redis용 LettuceConnectionFactory 생성 중");
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        log.info("LettuceConnectionFactory 생성 완료");
        return factory;
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("RedisCacheManager 생성 시작 - RedisConnectionFactory: {}",
                 connectionFactory.getClass().getSimpleName());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        log.info("Redis 캐시 설정 - GenericJackson2JsonRedisSerializer 사용");

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        RedisCacheConfiguration eventConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        RedisCacheConfiguration eventAllConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        RedisCacheConfiguration eventIdsConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        log.info("RedisCacheManager 생성 중 (커스텀 직렬화)");
        RedisCacheManager manager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("event", eventConfig)
                .withCacheConfiguration("event-all", eventAllConfig)
                .withCacheConfiguration("event-ids", eventIdsConfig)
                .build();

        log.info("Redis 캐시 매니저 설정 완료 - event 캐시(10분), event-all 캐시(5분), event-ids 캐시(10분)");
        return manager;
    }
}
