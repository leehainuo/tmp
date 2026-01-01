package com.miku.redis.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Redisson 限流器工具类
 *
 * @author miku
 */
@Slf4j
public class RedissonRateLimiterUtil {

    private final Object redissonClient; // 使用 Object 类型避免类加载依赖

    public RedissonRateLimiterUtil(Object redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 获取限流器
     *
     * @param rateLimiterKey 限流器的key
     * @return 限流器对象
     */
    public Object getRateLimiter(String rateLimiterKey) {
        return RedissonReflectionUtil.getRateLimiter(redissonClient, rateLimiterKey);
    }

    /**
     * 设置限流规则并尝试获取许可
     *
     * @param rateLimiterKey 限流器的key
     * @param rateType 限流类型（OVERALL-全局限流，PER_CLIENT-单机限流）
     * @param rate 速率（每秒允许的次数）
     * @param rateInterval 时间间隔
     * @param rateIntervalUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryAcquire(String rateLimiterKey, Object rateType, long rate,
                              long rateInterval, Object rateIntervalUnit) {
        Object rateLimiter = RedissonReflectionUtil.getRateLimiter(redissonClient, rateLimiterKey);
        if (rateLimiter == null) {
            return false;
        }
        
        try {
            RedissonReflectionUtil.rateLimiterTrySetRate(rateLimiter, (int) rate, (int) rateInterval);
            return RedissonReflectionUtil.rateLimiterTryAcquire(rateLimiter);
        } catch (Exception e) {
            log.error("[RedissonRateLimiterUtil] 限流异常: rateLimiterKey={}", rateLimiterKey, e);
            return false;
        }
    }

    /**
     * 尝试获取许可（不设置限流规则，使用已存在的规则）
     *
     * @param rateLimiterKey 限流器的key
     * @return 是否获取成功
     */
    public boolean tryAcquire(String rateLimiterKey) {
        Object rateLimiter = RedissonReflectionUtil.getRateLimiter(redissonClient, rateLimiterKey);
        if (rateLimiter == null) {
            return false;
        }
        return RedissonReflectionUtil.rateLimiterTryAcquire(rateLimiter);
    }
}

