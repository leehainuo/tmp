package com.miku.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 配置属性
 *
 * @author miku
 */
@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    /**
     * 限流功能配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 防重复提交功能配置
     */
    private RepeatSubmit repeatSubmit = new RepeatSubmit();

    /**
     * Redisson 配置
     */
    private Redisson redisson = new Redisson();

    @Data
    public static class RateLimit {
        /**
         * 是否启用限流功能
         */
        private boolean enabled = true;

        /**
         * 限流实现方式：redis-template（Lua脚本）或 redisson
         */
        private String implementation = ImplementationType.REDIS_TEMPLATE.getValue();

        /**
         * 获取实现方式枚举
         */
        public ImplementationType getImplementationType() {
            return ImplementationType.fromValue(implementation);
        }
    }

    @Data
    public static class RepeatSubmit {
        /**
         * 是否启用防重复提交功能
         */
        private boolean enabled = true;

        /**
         * 实现方式：redis-template（setIfAbsent）或 redisson（分布式锁）
         */
        private String implementation = ImplementationType.REDIS_TEMPLATE.getValue();

        /**
         * 获取实现方式枚举
         */
        public ImplementationType getImplementationType() {
            return ImplementationType.fromValue(implementation);
        }
    }

    @Data
    public static class Redisson {
        /**
         * 是否启用 Redisson
         */
        private boolean enabled = false;
    }
}

