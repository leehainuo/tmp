package com.miku.core.security.constant;

/**
 * JWT安全相关常量
 */
public final class JwtSecurityConstants {

    /**
     * Token类型常量
     */
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * 黑名单相关常量
     */
    public static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    public static final String USER_BLACKLIST_PREFIX = "jwt:blacklist:user:";
    public static final String BLACKLIST_VALUE = "blacklisted";

    /**
     * 时间常量（毫秒）
     */
    public static final long MIN_ACCESS_TOKEN_EXPIRATION = 300000L; // 5分钟
    public static final long MAX_ACCESS_TOKEN_EXPIRATION = 86400000L; // 24小时
    public static final long MIN_REFRESH_TOKEN_EXPIRATION = 86400000L; // 1天
    public static final long MAX_REFRESH_TOKEN_EXPIRATION = 2592000000L; // 30天

    /**
     * 密钥相关常量
     */
    public static final int MIN_SECRET_KEY_LENGTH = 32; // 256 bits
    public static final String DEFAULT_SECRET_KEY = "your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";

}

