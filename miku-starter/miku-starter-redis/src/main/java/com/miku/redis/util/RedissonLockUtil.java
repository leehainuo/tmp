package com.miku.redis.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * Redisson 分布式锁工具类
 *
 * @author miku
 */
@Slf4j
public class RedissonLockUtil {

    private final Object redissonClient; // 使用 Object 类型避免类加载依赖

    public RedissonLockUtil(Object redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取锁并执行，如果获取失败则抛出异常
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间（毫秒）
     * @param leaseTime 锁的持有时间（毫秒），-1表示使用看门狗机制
     * @param supplier 要执行的逻辑
     * @param <T> 返回值类型
     * @return 执行结果
     */
    public <T> T tryLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        Object lock = RedissonReflectionUtil.getLock(redissonClient, lockKey);
        boolean acquired = RedissonReflectionUtil.tryLock(lock, waitTime, leaseTime);
            
            if (!acquired) {
                throw new RuntimeException("获取锁失败: " + lockKey);
            }
        
            try {
                return supplier.get();
            } finally {
            RedissonReflectionUtil.unlock(lock);
        }
    }

    /**
     * 尝试获取锁并执行，如果获取失败则返回null
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间（毫秒）
     * @param leaseTime 锁的持有时间（毫秒），-1表示使用看门狗机制
     * @param supplier 要执行的逻辑
     * @param <T> 返回值类型
     * @return 执行结果，如果获取锁失败则返回null
     */
    public <T> T tryLockOrNull(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        try {
            Object lock = RedissonReflectionUtil.getLock(redissonClient, lockKey);
            boolean acquired = RedissonReflectionUtil.tryLock(lock, waitTime, leaseTime);
            
            if (!acquired) {
                return null;
            }
            
            try {
                return supplier.get();
            } finally {
                RedissonReflectionUtil.unlock(lock);
            }
        } catch (Exception e) {
            log.error("[RedissonLockUtil] 执行异常: lockKey={}", lockKey, e);
            return null;
        }
    }

    /**
     * 获取锁并执行（阻塞直到获取成功）
     *
     * @param lockKey 锁的key
     * @param leaseTime 锁的持有时间（毫秒），-1表示使用看门狗机制
     * @param supplier 要执行的逻辑
     * @param <T> 返回值类型
     * @return 执行结果
     */
    public <T> T lock(String lockKey, long leaseTime, Supplier<T> supplier) {
        Object lock = RedissonReflectionUtil.getLock(redissonClient, lockKey);
        RedissonReflectionUtil.lock(lock, leaseTime);
            
            try {
                return supplier.get();
            } finally {
            RedissonReflectionUtil.unlock(lock);
        }
    }
}

