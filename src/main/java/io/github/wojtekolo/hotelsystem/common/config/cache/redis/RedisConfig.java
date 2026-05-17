package io.github.wojtekolo.hotelsystem.common.config.cache.redis;

import io.lettuce.core.ClientOptions;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisConfig implements CachingConfigurer {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        RedisSerializer<Object> serializer = RedisSerializer.json();
        return RedisCacheConfiguration.defaultCacheConfig()
                                      .entryTtl(Duration.ofMinutes(5))
                                      .disableCachingNullValues()
                                      .serializeKeysWith(
                                              RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string())
                                      )
                                      .serializeValuesWith(
                                              RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                                      );
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory, RedisCacheConfiguration configuration) {
        return RedisCacheManager.builder(factory).cacheDefaults(configuration).build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(DataRedisProperties properties) {
        ClientOptions clientOptions = ClientOptions.builder()
                                                   .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                                                   .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                                                                            .commandTimeout(Duration.ofMillis(500))
                                                                            .clientOptions(clientOptions)
                                                                            .build();

        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(properties.getHost(), properties.getPort()), clientConfig);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }
}
