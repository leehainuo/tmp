package com.miku.core.security.service;

import com.miku.core.security.constant.JwtSecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT黑名单服务
 * <p>
 * 注意：JWT 黑名单需要分布式存储（多实例共享），因此必须使用 Redis
 * 如果 Redis 不可用，黑名单功能将不可用（但不会影响应用启动）
 */
@Slf4j
@Service
public class JwtBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 使用 ObjectProvider 处理可选依赖
     * <p>
     * 这是 Spring 官方推荐的方式，比 @Autowired(required = false) 更优雅
     * - 如果 Redis 可用，注入 RedisTemplate
     * - 如果 Redis 不可用，redisTemplate 为 null
     */
    public JwtBlacklistService(ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider) {
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        if (this.redisTemplate == null) {
            log.warn("[Miku-Security] Redis 不可用，JWT 黑名单功能将不可用（需要分布式存储）");
        }
    }

    private static final String BLACKLIST_PREFIX = JwtSecurityConstants.BLACKLIST_PREFIX;

    /**
     * 将Token加入黑名单
     *
     * @param token          Token
     * @param expirationTime 过期时间（毫秒）
     */
    public void addToBlacklist(String token, long expirationTime) {
        if (redisTemplate == null) {
            log.warn("[Miku-Security] Redis 不可用，无法将Token加入黑名单（黑名单功能需要 Redis）");
            return;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, JwtSecurityConstants.BLACKLIST_VALUE, expirationTime, TimeUnit.MILLISECONDS);
            log.debug("[Miku-Security] Token已加入黑名单: {}...", token.substring(0, Math.min(20, token.length())));
        } catch (Exception e) {
            log.error("[Miku-Security] 将Token加入黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token Token
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (redisTemplate == null) {
            log.warn("[Miku-Security] Redis 不可用，无法检查Token黑名单状态（默认返回false）");
            return false;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("[Miku-Security] 检查Token黑名单状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从黑名单中移除Token
     *
     * @param token Token
     */
    public void removeFromBlacklist(String token) {
        if (redisTemplate == null) {
            log.warn("[Miku-Security] Redis 不可用，无法从黑名单中移除Token");
            return;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
            log.debug("[Miku-Security] Token已从黑名单中移除: {}...", token.substring(0, Math.min(20, token.length())));
        } catch (Exception e) {
            log.error("[Miku-Security] 从黑名单中移除Token失败: {}", e.getMessage());
        }
    }

    /**
     * 将用户的所有Token加入黑名单（用于强制登出）
     *
     * @param userId 用户ID
     */
    public void blacklistUserTokens(Long userId) {
        if (redisTemplate == null) {
            log.warn("[Miku-Security] Redis 不可用，无法将用户Token加入黑名单");
            return;
        }
        try {
            String userBlacklistKey = JwtSecurityConstants.USER_BLACKLIST_PREFIX + userId;
            redisTemplate.opsForValue().set(userBlacklistKey, JwtSecurityConstants.BLACKLIST_VALUE, 7, TimeUnit.DAYS);
            log.info("[Miku-Security] 用户 {} 的所有Token已加入黑名单", userId);
        } catch (Exception e) {
            log.error("[Miku-Security] 将用户Token加入黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 检查用户Token是否被全局黑名单
     *
     * @param userId 用户ID
     * @return 是否被黑名单
     */
    public boolean isUserBlacklisted(Long userId) {
        if (redisTemplate == null) {
            log.warn("[Miku-Security] Redis 不可用，无法检查用户黑名单状态（默认返回false）");
            return false;
        }
        try {
            String userBlacklistKey = JwtSecurityConstants.USER_BLACKLIST_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(userBlacklistKey));
        } catch (Exception e) {
            log.error("[Miku-Security] 检查用户黑名单状态失败: {}", e.getMessage());
            return false;
        }
    }
}

