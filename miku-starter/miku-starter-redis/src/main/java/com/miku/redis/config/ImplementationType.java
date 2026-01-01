package com.miku.redis.config;

import com.miku.redis.constant.RedisConstants;
import lombok.Getter;

/**
 * 实现方式枚举
 *
 * @author miku
 */
@Getter
public enum ImplementationType {
    /**
     * RedisTemplate 实现（Lua 脚本 / setIfAbsent）
     */
    REDIS_TEMPLATE(RedisConstants.IMPLEMENTATION_REDIS_TEMPLATE),

    /**
     * Redisson 实现（限流器 / 分布式锁）
     */
    REDISSON(RedisConstants.IMPLEMENTATION_REDISSON);

    private final String value;

    ImplementationType(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取枚举
     *
     * @param value 字符串值
     * @return 枚举值，如果不存在则返回 REDIS_TEMPLATE
     */
    public static ImplementationType fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return REDIS_TEMPLATE;
        }
        for (ImplementationType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return REDIS_TEMPLATE; // 默认值
    }

    /**
     * 判断是否为 Redisson 实现
     */
    public boolean isRedisson() {
        return this == REDISSON;
    }

    /**
     * 判断是否为 RedisTemplate 实现
     */
    public boolean isRedisTemplate() {
        return this == REDIS_TEMPLATE;
    }
}

