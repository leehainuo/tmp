package com.miku.redis.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Redisson 信号量工具类
 *
 * @author miku
 */
@Slf4j
public class RedissonSemaphoreUtil {

    private final Object redissonClient; // 使用 Object 类型避免类加载依赖

    public RedissonSemaphoreUtil(Object redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 获取信号量
     *
     * @param semaphoreKey 信号量的key
     * @return 信号量对象
     */
    public Object getSemaphore(String semaphoreKey) {
        return RedissonReflectionUtil.getSemaphore(redissonClient, semaphoreKey);
    }

    /**
     * 尝试获取许可
     *
     * @param semaphoreKey 信号量的key
     * @param permits 许可数量
     * @param waitTime 等待时间（毫秒）
     * @return 是否获取成功
     */
    public boolean tryAcquire(String semaphoreKey, int permits, long waitTime) {
        Object semaphore = RedissonReflectionUtil.getSemaphore(redissonClient, semaphoreKey);
        if (semaphore == null) {
            return false;
        }
        return RedissonReflectionUtil.semaphoreTryAcquire(semaphore, permits, waitTime);
    }

    /**
     * 释放许可
     *
     * @param semaphoreKey 信号量的key
     * @param permits 许可数量
     */
    public void release(String semaphoreKey, int permits) {
        Object semaphore = RedissonReflectionUtil.getSemaphore(redissonClient, semaphoreKey);
        if (semaphore != null) {
            RedissonReflectionUtil.semaphoreRelease(semaphore, permits);
        }
    }

    /**
     * 设置许可数量
     *
     * @param semaphoreKey 信号量的key
     * @param permits 许可数量
     * @return 是否设置成功
     */
    public boolean trySetPermits(String semaphoreKey, int permits) {
        Object semaphore = RedissonReflectionUtil.getSemaphore(redissonClient, semaphoreKey);
        if (semaphore == null) {
            return false;
        }
        return RedissonReflectionUtil.semaphoreTrySetPermits(semaphore, permits);
    }
}

