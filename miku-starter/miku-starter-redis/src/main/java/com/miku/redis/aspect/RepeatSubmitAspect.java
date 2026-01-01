package com.miku.redis.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miku.redis.annotation.RepeatSubmit;
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
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面
 * <p>
 * 支持两种实现方式：
 * 1. RedisTemplate setIfAbsent（默认）
 * 2. Redisson 分布式锁（可选）
 *
 * @author miku
 */
@Slf4j
@Aspect
@ConditionalOnClass(RedisTemplate.class)
public class RepeatSubmitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisProperties properties;
    private final Object redissonClient; // 可选，可能为 null，使用 Object 类型避免类加载依赖

    public RepeatSubmitAspect(RedisTemplate<String, Object> redisTemplate,
                              ObjectMapper objectMapper,
                              RedisProperties properties,
                              Object redissonClient) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.redissonClient = redissonClient;
    }


    @Before("@annotation(repeatSubmit)")
    public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) {
        // 生成唯一key
        String key = generateKey(point, repeatSubmit);
        int interval = repeatSubmit.interval();

        try {
            // 根据配置选择实现方式
            ImplementationType implementationType = properties.getRepeatSubmit().getImplementationType();
            if (implementationType.isRedisson() && redissonClient != null) {
                // 使用 Redisson 分布式锁
                useRedissonLock(key, interval, repeatSubmit.message());
            } else {
                // 使用 RedisTemplate setIfAbsent
                useRedisTemplateSetIfAbsent(key, interval, repeatSubmit.message());
            }

            log.debug("[Miku-RepeatSubmit] 防重复检查通过: key={}, interval={}ms", key, interval);

        } catch (RedisServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Miku-RepeatSubmit] 防重复检查异常: {}", e.getMessage(), e);
            throw new RedisServiceException(400, repeatSubmit.message());
        }
    }

    /**
     * 使用 Redisson 分布式锁
     */
    private void useRedissonLock(String key, int interval, String message) {
        if (redissonClient == null) {
            log.warn("[Miku-RepeatSubmit] RedissonClient 不可用，回退到 RedisTemplate 实现");
            useRedisTemplateSetIfAbsent(key, interval, message);
            return;
        }
        
        try {
            Object lock = RedissonReflectionUtil.getLock(redissonClient, key);

            // 尝试获取锁，如果获取失败（锁已存在）则表示重复提交
            boolean acquired = RedissonReflectionUtil.tryLock(lock, 0L, interval);

            if (!acquired) {
                log.warn("[Miku-RepeatSubmit] 检测到重复提交: key={}", key);
                throw new RedisServiceException(400, message);
            }
            // 锁会在 interval 时间后自动释放
        } catch (RedisServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Miku-RepeatSubmit] Redisson锁异常: {}", e.getMessage(), e);
            // 异常时回退到 RedisTemplate 实现
            useRedisTemplateSetIfAbsent(key, interval, message);
        }
    }

    /**
     * 使用 RedisTemplate setIfAbsent
     */
    private void useRedisTemplateSetIfAbsent(String key, int interval, String message) {
        // 尝试设置Redis key，如果已存在则表示重复提交
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", interval, TimeUnit.MILLISECONDS);

        if (success == null || !success) {
            log.warn("[Miku-RepeatSubmit] 检测到重复提交: key={}", key);
            throw new RedisServiceException(400, message);
        }
    }

    /**
     * 生成唯一key
     * 格式：repeat_submit:{username}:{method}:{paramsHash}
     */
    private String generateKey(JoinPoint point, RepeatSubmit repeatSubmit) {
        StringBuilder key = new StringBuilder(RedisConstants.REPEAT_SUBMIT_PREFIX);

        // 仅使用 IP 作为维度标识
        key.append(RequestIpUtil.getClientIp()).append(":");

        // 添加方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        key.append(className).append(".").append(methodName);

        // 是否包含请求参数
        if (repeatSubmit.includeParams()) {
            // 获取请求参数并计算MD5
            String paramsHash = getParamsHash(point);
            key.append(":").append(paramsHash);
        }

        return key.toString();
    }

    /**
     * 计算请求参数的哈希值
     */
    private String getParamsHash(JoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return "none";
            }

            // 过滤掉 HttpServletRequest、HttpServletResponse 等参数
            StringBuilder params = new StringBuilder();
            for (Object arg : args) {
                if (arg != null &&
                        !arg.getClass().getName().startsWith("jakarta.servlet") &&
                        !arg.getClass().getName().startsWith("org.springframework")) {
                    try {
                        params.append(objectMapper.writeValueAsString(arg));
                    } catch (Exception e) {
                        log.warn("[Miku-RepeatSubmit] 序列化参数失败: {}", e.getMessage());
                        params.append(arg.toString());
                    }
                }
            }

            // 计算MD5
            String paramsStr = params.toString();
            if (paramsStr.isEmpty()) {
                return "none";
            }
            return DigestUtils.md5DigestAsHex(paramsStr.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.warn("[Miku-RepeatSubmit] 计算参数哈希失败: {}", e.getMessage());
            return "error";
        }
    }

}

