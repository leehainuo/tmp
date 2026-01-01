package com.miku.redis.aspect;

import com.miku.redis.annotation.RateLimiter;
import com.miku.redis.config.ImplementationType;
import com.miku.redis.config.RedisProperties;
import com.miku.redis.constant.RedisConstants;
import com.miku.redis.exception.RedisServiceException;
import com.miku.redis.util.RequestIpUtil;
import com.miku.redis.util.RedissonReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 限流切面
 * <p>
 * 支持两种实现方式：
 * 1. RedisTemplate + Lua 脚本（默认）
 * 2. Redisson 限流器（可选）
 *
 * @author miku
 */
@Slf4j
@Aspect
@ConditionalOnClass(RedisTemplate.class)
public class RateLimiterAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisProperties properties;
    private final Object redissonClient; // 可选，可能为 null，使用 Object 类型避免类加载依赖

    public RateLimiterAspect(RedisTemplate<String, Object> redisTemplate,
                             RedisProperties properties,
                             Object redissonClient) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.redissonClient = redissonClient;
    }


    /**
     * Lua 脚本：限流逻辑
     * 返回值：1 表示允许访问，0 表示限流
     */
    private static final String LIMIT_LUA_SCRIPT = """
            local key = KEYS[1]
            local count = tonumber(ARGV[1])
            local time = tonumber(ARGV[2])
            local current = redis.call('get', key)
            if current and tonumber(current) > count then
                return 0
            end
            current = redis.call('incr', key)
            if tonumber(current) == 1 then
                redis.call('expire', key, time)
            end
            return 1
            """;

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        String key = getCombineKey(rateLimiter, point);
        int count = rateLimiter.count();
        int time = rateLimiter.time();

        try {
            // 根据配置选择实现方式
            ImplementationType implementationType = properties.getRateLimit().getImplementationType();
            if (implementationType.isRedisson() && redissonClient != null) {
                // 使用 Redisson 限流器
                useRedissonRateLimiter(key, count, time);
            } else {
                // 使用 RedisTemplate + Lua
                useRedisTemplateRateLimiter(key, count, time);
            }

            log.debug("[Miku-RateLimiter] 限流检查通过: key={}, count={}, time={}", key, count, time);

        } catch (RedisServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Miku-RateLimiter] 限流检查异常: {}", e.getMessage(), e);
            // 限流异常时，默认允许通过（可根据业务需求调整）
        }
    }

    /**
     * 使用 Redisson 限流器
     */
    private void useRedissonRateLimiter(String key, int count, int time) {
        if (redissonClient == null) {
            log.warn("[Miku-RateLimiter] RedissonClient 不可用，回退到 RedisTemplate 实现");
            useRedisTemplateRateLimiter(key, count, time);
            return;
        }
        
        try {
            Object rateLimiter = RedissonReflectionUtil.getRateLimiter(redissonClient, key);
            if (rateLimiter == null) {
                throw new IllegalStateException("无法获取限流器: " + key);
            }
            
            // 设置限流规则：总速率，count次/time秒
            RedissonReflectionUtil.rateLimiterTrySetRate(rateLimiter, count, time);
            
            // 尝试获取许可
            boolean acquired = RedissonReflectionUtil.rateLimiterTryAcquire(rateLimiter);
            
            if (!acquired) {
                log.warn("[Miku-RateLimiter] 请求被限流: key={}, count={}, time={}", key, count, time);
                throw new RedisServiceException(429, "访问过于频繁，请稍后再试");
            }
        } catch (RedisServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Miku-RateLimiter] Redisson限流异常: {}", e.getMessage(), e);
            // 异常时回退到 RedisTemplate 实现
            useRedisTemplateRateLimiter(key, count, time);
        }
    }

    /**
     * 使用 RedisTemplate + Lua 脚本限流
     */
    private void useRedisTemplateRateLimiter(String key, int count, int time) {
        try {
            RedisScript<Long> redisScript = RedisScript.of(LIMIT_LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), count, time);

            if (result == null || result == 0) {
                log.warn("[Miku-RateLimiter] 请求被限流: key={}, count={}, time={}", key, count, time);
                throw new RedisServiceException(429, "访问过于频繁，请稍后再试");
            }
        } catch (RedisServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Miku-RateLimiter] RedisTemplate限流异常: {}", e.getMessage(), e);
            throw new RedisServiceException(429, "访问过于频繁，请稍后再试");
        }
    }


    /**
     * 组合限流key
     */
    private String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        StringBuilder key = new StringBuilder(RedisConstants.RATE_LIMITER_PREFIX);

        // 添加自定义key
        if (!rateLimiter.key().isEmpty()) {
            key.append(rateLimiter.key()).append(":");
        }

        // 根据限流类型添加标识
        switch (rateLimiter.limitType()) {
            case IP -> {
                key.append(RequestIpUtil.getClientIp());
            }
            case USER -> {
                // 仅按 IP 维度限流，USER 模式退化为 IP 模式
                key.append(RequestIpUtil.getClientIp());
            }
            default -> {
                // 默认使用方法签名
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                String className = method.getDeclaringClass().getName();
                String methodName = method.getName();
                key.append(className).append(".").append(methodName);
            }
        }

        return key.toString();
    }

}

