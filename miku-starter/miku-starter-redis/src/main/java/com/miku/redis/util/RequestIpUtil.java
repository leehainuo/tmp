package com.miku.redis.util;

import com.miku.redis.constant.RedisConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * IP 工具类（starter 内部使用）
 */
public final class RequestIpUtil {

    private RequestIpUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 从当前请求上下文解析客户端 IP（无请求上下文时返回 {@link RedisConstants#DEFAULT_IP}）
     */
    public static String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return RedisConstants.DEFAULT_IP;
            }
            return getClientIp(attributes.getRequest());
        } catch (Exception e) {
            return RedisConstants.DEFAULT_IP;
        }
    }

    /**
     * 从请求中解析客户端 IP（按常见代理头优先，最后回退 remoteAddr）
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return RedisConstants.DEFAULT_IP;
        }

        String ip = null;
        for (String header : RedisConstants.IP_HEADER_NAMES) {
            ip = request.getHeader(header);
            ip = getFirstValidIp(ip);
            if (isValidIp(ip)) {
                break;
            }
        }

        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        if (!isValidIp(ip)) {
            return RedisConstants.DEFAULT_IP;
        }

        return RedisConstants.LOCALHOST_IPV6.equals(ip) ? RedisConstants.LOCALHOST_IPV4 : ip;
    }

    private static String getFirstValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        // X-Forwarded-For 可能是多级代理 "client, proxy1, proxy2"
        if (ip.indexOf(',') > 0) {
            String[] ips = ip.split(",");
            for (String subIp : ips) {
                String trimmed = subIp.trim();
                if (isValidIp(trimmed)) {
                    return trimmed;
                }
            }
        }
        return ip.trim();
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}


