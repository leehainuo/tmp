package com.miku.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.miku.pkg.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 * <p>
 * 支持 Redis 和 Caffeine 两种缓存实现：
 * - 当 spring.data.redis.enabled=true（默认）时，使用 Redis（分布式缓存）
 * - 当 spring.data.redis.enabled=false 时，使用 Caffeine（本地缓存）
 * <p>
 * 注：这里的“降级”由配置开关控制，不做运行时连通性探测（避免启动期引入外部依赖/阻塞）。
 *
 * @author lihainuo.com
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis 缓存管理器（优先使用）
     * 当 Redis 可用时，使用 Redis 作为缓存
     */
    @Bean
    @Primary
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("[Miku-Cache] 已启用 Redis 缓存（spring.data.redis.enabled=true）");

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(Constants.Cache.EXPIRE_TIME))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("userCache", config)
                .withCacheConfiguration("roleCache", config)
                .withCacheConfiguration("permissionCache", config)
                .transactionAware()
                .build();
    }

    /**
     * Caffeine 本地缓存管理器（降级方案）
     * 当 Redis 不可用时，自动使用 Caffeine 作为缓存
     */
    @Bean
    @ConditionalOnClass(Caffeine.class)
    @ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "false")
    public CacheManager caffeineCacheManager() {
        log.info("[Miku-Cache] 已禁用 Redis 缓存（spring.data.redis.enabled=false），使用 Caffeine（本地缓存）");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "userCache", "roleCache", "permissionCache"
        );
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(10_000)  // 最大缓存条目数
                        .expireAfterWrite(Constants.Cache.EXPIRE_TIME, TimeUnit.MINUTES)  // 写入后过期时间
                        .recordStats()  // 启用统计
        );

        return cacheManager;
    }
}

