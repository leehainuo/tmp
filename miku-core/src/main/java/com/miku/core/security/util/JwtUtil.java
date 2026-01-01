package com.miku.core.security.util;

import com.miku.core.security.service.JwtBlacklistService;
import com.miku.core.security.constant.JwtSecurityConstants;
import com.miku.core.security.model.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret:your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 默认7天
    private Long refreshExpiration;

    private final JwtBlacklistService blacklistService;

    /**
     * 生成访问Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Token
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", JwtSecurityConstants.TOKEN_TYPE_ACCESS);
        return createToken(claims, username, expiration);
    }

    /**
     * 生成刷新Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", JwtSecurityConstants.TOKEN_TYPE_REFRESH);
        return createToken(claims, username, refreshExpiration);
    }

    /**
     * 生成Token对（访问Token + 刷新Token）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Token对
     */
    public TokenPair generateTokenPair(Long userId, String username) {
        String accessToken = generateAccessToken(userId, username);
        String refreshToken = generateRefreshToken(userId, username);
        return new TokenPair(accessToken, refreshToken, expiration / 1000, refreshExpiration / 1000);
    }

    /**
     * 创建Token
     *
     * @param claims  数据
     * @param subject 主题
     * @param expirationTime 过期时间（毫秒）
     * @return Token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                /*
                  自动推断 HMAC 算法
                  256 bits (32 bytes) → 自动使用 HS256
                  384 bits (48 bytes) → 自动使用 HS384
                  512 bits (64 bytes) → 自动使用 HS512
                 */
                .signWith(key)
                .compact();
    }

    /**
     * 从Token中获取用户名
     *
     * @param token Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从Token中获取Claims
     *
     * @param token Token
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否过期
     *
     * @param token Token
     * @return 是否过期 true:过期 false:未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token
     *
     * @param token Token
     * @return 是否验证通过 true:验证通过 false:验证失败
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证访问Token
     *
     * @param token Token
     * @return 是否验证通过
     */
    public boolean validateAccessToken(String token) {
        try {
            // 检查是否在黑名单中
            if (blacklistService.isBlacklisted(token)) {
                return false;
            }
            
            Claims claims = getClaimsFromToken(token);
            String type = claims.get("type", String.class);
            
            // 检查Token类型和过期时间
            if (!JwtSecurityConstants.TOKEN_TYPE_ACCESS.equals(type) || isTokenExpired(token)) {
                return false;
            }
            
            // 检查用户是否被全局黑名单
            Long userId = getUserIdFromToken(token);
            if (blacklistService.isUserBlacklisted(userId)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证刷新Token
     *
     * @param token Token
     * @return 是否验证通过
     */
    public boolean validateRefreshToken(String token) {
        try {
            // 检查是否在黑名单中
            if (blacklistService.isBlacklisted(token)) {
                return false;
            }
            
            Claims claims = getClaimsFromToken(token);
            String type = claims.get("type", String.class);
            
            // 检查Token类型和过期时间
            if (!JwtSecurityConstants.TOKEN_TYPE_REFRESH.equals(type) || isTokenExpired(token)) {
                return false;
            }
            
            // 检查用户是否被全局黑名单
            Long userId = getUserIdFromToken(token);
            if (blacklistService.isUserBlacklisted(userId)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 撤销Token（加入黑名单）
     *
     * @param token Token
     */
    public void revokeToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long expirationTime = expiration.getTime() - System.currentTimeMillis();
            
            if (expirationTime > 0) {
                blacklistService.addToBlacklist(token, expirationTime);
            }
        } catch (Exception e) {
            // Token无效，无需处理
        }
    }
    
    /**
     * 撤销用户所有Token
     *
     * @param userId 用户ID
     */
    public void revokeUserTokens(Long userId) {
        blacklistService.blacklistUserTokens(userId);
    }

    /**
     * 生成HS256密钥字符串（Base64编码）
     * 可以将生成的密钥配置到 application.yml 的 jwt.secret 中
     *
     * @return Base64编码的密钥字符串
     */
    public static String generateHS256SecretKey() {
        // HS256 需要至少 256 bits (32 bytes) 的密钥
        byte[] keyBytes = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * 主函数，用于生成密钥（仅供测试使用）
     * 运行此类可以生成一个新的 HS256 密钥
     */
    public static void main(String[] args) {
        String secretKey = generateHS256SecretKey();

        System.out.println("生成的 HS256 密钥（Base64编码）：");
        System.out.println(secretKey);
    }
}

