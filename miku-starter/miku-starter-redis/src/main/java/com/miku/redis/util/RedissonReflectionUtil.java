package com.miku.redis.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 反射工具类
 * <p>
 * 统一封装 Redisson 反射调用，减少代码重复
 *
 * @author miku
 */
@Slf4j
public final class RedissonReflectionUtil {

    private RedissonReflectionUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 获取锁对象
     */
    public static Object getLock(Object redissonClient, String lockKey) {
        if (redissonClient == null) {
            throw new IllegalStateException("RedissonClient 未配置，请检查 Redisson 依赖和配置");
        }
        try {
            Method getLockMethod = redissonClient.getClass().getMethod("getLock", String.class);
            return getLockMethod.invoke(redissonClient, lockKey);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 获取锁异常: lockKey={}", lockKey, e);
            throw new RuntimeException("获取锁异常: " + lockKey, e);
        }
    }

    /**
     * 尝试获取锁
     */
    public static boolean tryLock(Object lock, long waitTime, long leaseTime) {
        try {
            Method tryLockMethod = lock.getClass().getMethod("tryLock", long.class, long.class, TimeUnit.class);
            return (Boolean) tryLockMethod.invoke(lock, waitTime, leaseTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 尝试获取锁异常", e);
            return false;
        }
    }

    /**
     * 阻塞获取锁
     */
    public static void lock(Object lock, long leaseTime) {
        try {
            Method lockMethod = lock.getClass().getMethod("lock", long.class, TimeUnit.class);
            lockMethod.invoke(lock, leaseTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 阻塞获取锁异常", e);
            throw new RuntimeException("阻塞获取锁异常", e);
        }
    }

    /**
     * 释放锁
     */
    public static void unlock(Object lock) {
        try {
            Method isHeldByCurrentThreadMethod = lock.getClass().getMethod("isHeldByCurrentThread");
            Boolean isHeld = (Boolean) isHeldByCurrentThreadMethod.invoke(lock);
            if (isHeld) {
                Method unlockMethod = lock.getClass().getMethod("unlock");
                unlockMethod.invoke(lock);
            }
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 释放锁异常", e);
        }
    }

    /**
     * 检查锁是否被当前线程持有
     */
    public static boolean isHeldByCurrentThread(Object lock) {
        try {
            Method isHeldByCurrentThreadMethod = lock.getClass().getMethod("isHeldByCurrentThread");
            return (Boolean) isHeldByCurrentThreadMethod.invoke(lock);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 检查锁状态异常", e);
            return false;
        }
    }

    /**
     * 获取信号量
     */
    public static Object getSemaphore(Object redissonClient, String semaphoreKey) {
        if (redissonClient == null) {
            log.warn("[RedissonReflectionUtil] RedissonClient 未配置，无法获取信号量");
            return null;
        }
        try {
            Method getSemaphoreMethod = redissonClient.getClass().getMethod("getSemaphore", String.class);
            return getSemaphoreMethod.invoke(redissonClient, semaphoreKey);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 获取信号量异常: semaphoreKey={}", semaphoreKey, e);
            return null;
        }
    }

    /**
     * 信号量尝试获取许可
     */
    public static boolean semaphoreTryAcquire(Object semaphore, int permits, long waitTime) {
        try {
            Method tryAcquireMethod = semaphore.getClass().getMethod("tryAcquire", int.class, long.class, TimeUnit.class);
            return (Boolean) tryAcquireMethod.invoke(semaphore, permits, waitTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 信号量获取许可异常", e);
            return false;
        }
    }

    /**
     * 信号量释放许可
     */
    public static void semaphoreRelease(Object semaphore, int permits) {
        try {
            Method releaseMethod = semaphore.getClass().getMethod("release", int.class);
            releaseMethod.invoke(semaphore, permits);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 信号量释放许可异常", e);
        }
    }

    /**
     * 信号量设置许可数量
     */
    public static boolean semaphoreTrySetPermits(Object semaphore, int permits) {
        try {
            Method trySetPermitsMethod = semaphore.getClass().getMethod("trySetPermits", int.class);
            return (Boolean) trySetPermitsMethod.invoke(semaphore, permits);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 信号量设置许可数量异常", e);
            return false;
        }
    }

    /**
     * 获取限流器
     */
    public static Object getRateLimiter(Object redissonClient, String rateLimiterKey) {
        if (redissonClient == null) {
            log.warn("[RedissonReflectionUtil] RedissonClient 未配置，无法获取限流器");
            return null;
        }
        try {
            Method getRateLimiterMethod = redissonClient.getClass().getMethod("getRateLimiter", String.class);
            return getRateLimiterMethod.invoke(redissonClient, rateLimiterKey);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 获取限流器异常: rateLimiterKey={}", rateLimiterKey, e);
            return null;
        }
    }

    /**
     * 限流器设置速率
     */
    public static void rateLimiterTrySetRate(Object rateLimiter, int count, int time) {
        try {
            Class<?> rateTypeClass = Class.forName("org.redisson.api.RateType");
            Class<?> rateIntervalUnitClass = Class.forName("org.redisson.api.RateIntervalUnit");
            Object overall = rateTypeClass.getField("OVERALL").get(null);
            Object seconds = rateIntervalUnitClass.getField("SECONDS").get(null);

            Method trySetRateMethod = rateLimiter.getClass().getMethod("trySetRate",
                    rateTypeClass, long.class, long.class, rateIntervalUnitClass);
            trySetRateMethod.invoke(rateLimiter, overall, (long) count, (long) time, seconds);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 限流器设置速率异常", e);
            throw new RuntimeException("限流器设置速率异常", e);
        }
    }

    /**
     * 限流器尝试获取许可
     */
    public static boolean rateLimiterTryAcquire(Object rateLimiter) {
        try {
            Method tryAcquireMethod = rateLimiter.getClass().getMethod("tryAcquire");
            return (Boolean) tryAcquireMethod.invoke(rateLimiter);
        } catch (Exception e) {
            log.error("[RedissonReflectionUtil] 限流器获取许可异常", e);
            return false;
        }
    }
}

