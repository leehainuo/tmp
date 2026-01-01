package com.miku.redis.constant;

/**
 * Redis 相关常量
 *
 * @author miku
 */
public final class RedisConstants {

    private RedisConstants() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * Redis Key 前缀
     */
    public static final String RATE_LIMITER_PREFIX = "rate_limiter:";
    public static final String REPEAT_SUBMIT_PREFIX = "repeat_submit:";

    /**
     * 实现方式常量
     */
    public static final String IMPLEMENTATION_REDIS_TEMPLATE = "redis-template";
    public static final String IMPLEMENTATION_REDISSON = "redisson";

    /**
     * IP 请求头
     */
    public static final String[] IP_HEADER_NAMES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    /**
     * 默认值
     */
    public static final String DEFAULT_IP = "unknown";
    public static final String DEFAULT_ANONYMOUS = "anonymous";
    public static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    public static final String LOCALHOST_IPV4 = "127.0.0.1";
}

