package com.miku.core.security.config;

import com.miku.core.security.constant.JwtSecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * JWT安全配置
 */
@Slf4j
@Component
public class JwtSecurityConfig {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * 应用启动时检查JWT配置安全性
     */
    @EventListener(ApplicationReadyEvent.class)
    public void checkJwtSecurity() {
        // 检查密钥强度
        checkSecretStrength();

        // 检查过期时间配置
        checkExpirationConfig();
    }

    /**
     * 检查密钥强度
     */
    private void checkSecretStrength() {
        if (secret == null || secret.trim().isEmpty()) {
            log.warn("[Miku-Security] JWT密钥未配置，使用默认密钥，生产环境请配置强密钥！");
            return;
        }

        if (secret.equals(JwtSecurityConstants.DEFAULT_SECRET_KEY)) {
            log.warn("[Miku-Security] 检测到默认JWT密钥，生产环境请更换为强密钥！");
            log.info("[Miku-Security] 建议运行 JwtUtil.main() 生成新的强密钥");
            return;
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            if (keyBytes.length < JwtSecurityConstants.MIN_SECRET_KEY_LENGTH) {
                log.warn("[Miku-Security] JWT密钥长度不足256位，建议使用更长的密钥");
            } else {
                log.info("[Miku-Security] JWT密钥强度检查通过");
            }
        } catch (Exception e) {
            log.warn("[Miku-Security] JWT密钥格式可能不正确，请检查Base64编码");
        }
    }

    /**
     * 检查过期时间配置
     */
    private void checkExpirationConfig() {
        // 统一转换单位，便于日志展示
        long accessMinutes = expiration / 60000;
        double accessHours = expiration / 3600000.0;
        long refreshDays = refreshExpiration / 86400000;

        // 检查访问Token过期时间
        if (expiration < JwtSecurityConstants.MIN_ACCESS_TOKEN_EXPIRATION) { // 5分钟
            log.warn("[Miku-Security] 访问Token过期时间过短（{}分钟），可能影响用户体验", accessMinutes);
        } else if (expiration > JwtSecurityConstants.MAX_ACCESS_TOKEN_EXPIRATION) { // 24小时
            log.warn("[Miku-Security] 访问Token过期时间过长（{}小时），存在安全风险", String.format("%.1f", accessHours));
        } else {
            // 使用一位小数展示小时，避免 30 分钟显示为 0 小时的误解
            log.info("[Miku-Security] 访问Token过期时间配置合理（{}小时，约{}分钟）",
                    String.format("%.1f", accessHours), accessMinutes);
        }

        // 检查刷新Token过期时间
        if (refreshExpiration < JwtSecurityConstants.MIN_REFRESH_TOKEN_EXPIRATION) { // 1天
            log.warn("[Miku-Security] 刷新Token过期时间过短（{}天），可能影响用户体验", refreshDays);
        } else if (refreshExpiration > JwtSecurityConstants.MAX_REFRESH_TOKEN_EXPIRATION) { // 30天
            log.warn("[Miku-Security] 刷新Token过期时间过长（{}天），存在安全风险", refreshDays);
        } else {
            log.info("[Miku-Security] 刷新Token过期时间配置合理（{}天）", refreshDays);
        }
    }

}

