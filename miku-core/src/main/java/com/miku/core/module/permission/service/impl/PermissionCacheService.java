package com.miku.core.module.permission.service.impl;

import com.miku.core.module.role.entity.SysRole;
import com.miku.core.module.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 权限缓存服务
 * <p>
 * 使用 Spring Cache 抽象层，支持 Redis 和 Caffeine 自动降级
 * - 如果 Redis 可用，使用 Redis（分布式缓存）
 * - 如果 Redis 不可用，自动使用 Caffeine（本地缓存）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final CacheManager cacheManager;

    /**
     * 缓存用户信息
     */
    public void cacheUser(Long userId, SysUser user) {
        Cache cache = cacheManager.getCache("userCache");
        if (cache != null) {
            cache.put(userId, user);
            log.debug("Cache: 缓存用户信息: userId={}", userId);
        }
    }

    /**
     * 获取缓存的用户信息
     */
    public SysUser getCachedUser(Long userId) {
        Cache cache = cacheManager.getCache("userCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(userId);
            if (wrapper != null) {
                return (SysUser) wrapper.get();
            }
        }
        return null;
    }

    /**
     * 删除用户缓存
     */
    public void removeCachedUser(Long userId) {
        Cache cache = cacheManager.getCache("userCache");
        if (cache != null) {
            cache.evict(userId);
            log.debug("Cache: 删除用户缓存: userId={}", userId);
        }
    }

    /**
     * 缓存用户角色列表
     */
    public void cacheUserRoles(Long userId, List<SysRole> roles) {
        Cache cache = cacheManager.getCache("roleCache");
        if (cache != null) {
            cache.put(userId, roles);
            log.debug("Cache: 缓存用户角色: userId={}", userId);
        }
    }

    /**
     * 获取缓存的用户角色列表
     */
    @SuppressWarnings("unchecked")
    public List<SysRole> getCachedUserRoles(Long userId) {
        Cache cache = cacheManager.getCache("roleCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(userId);
            if (wrapper != null) {
                return (List<SysRole>) wrapper.get();
            }
        }
        return null;
    }

    /**
     * 删除用户角色缓存
     */
    public void removeCachedUserRoles(Long userId) {
        Cache cache = cacheManager.getCache("roleCache");
        if (cache != null) {
            cache.evict(userId);
            log.debug("Cache: 删除用户角色缓存: userId={}", userId);
        }
    }

    /**
     * 缓存用户权限集合
     */
    public void cacheUserPermissions(Long userId, Set<String> permissions) {
        Cache cache = cacheManager.getCache("permissionCache");
        if (cache != null) {
            cache.put(userId, permissions);
            log.debug("Cache: 缓存用户权限: userId={}", userId);
        }
    }

    /**
     * 获取缓存的用户权限集合
     */
    @SuppressWarnings("unchecked")
    public Set<String> getCachedUserPermissions(Long userId) {
        Cache cache = cacheManager.getCache("permissionCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(userId);
            if (wrapper != null) {
                return (Set<String>) wrapper.get();
            }
        }
        return null;
    }

    /**
     * 删除用户权限缓存
     */
    public void removeCachedUserPermissions(Long userId) {
        Cache cache = cacheManager.getCache("permissionCache");
        if (cache != null) {
            cache.evict(userId);
            log.debug("Cache: 删除用户权限缓存: userId={}", userId);
        }
    }

    /**
     * 清空用户所有缓存
     */
    public void clearUserCache(Long userId) {
        removeCachedUser(userId);
        removeCachedUserRoles(userId);
        removeCachedUserPermissions(userId);
        log.info("Cache: 清空用户缓存: userId={}", userId);
    }

    /**
     * 批量清空用户缓存
     */
    public void clearUserCacheBatch(List<Long> userIds) {
        userIds.forEach(this::clearUserCache);
    }

    /**
     * 清空所有权限缓存
     */
    public void clearAllCache() {
        Cache userCache = cacheManager.getCache("userCache");
        Cache roleCache = cacheManager.getCache("roleCache");
        Cache permissionCache = cacheManager.getCache("permissionCache");

        if (userCache != null) {
            userCache.clear();
        }
        if (roleCache != null) {
            roleCache.clear();
        }
        if (permissionCache != null) {
            permissionCache.clear();
        }

        log.info("Cache: 清空所有权限缓存");
    }
}

