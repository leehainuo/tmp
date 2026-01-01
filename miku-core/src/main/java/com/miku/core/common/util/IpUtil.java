package com.miku.core.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 工具类
 * <p>
 * 优先代理头，支持多级 X-Forwarded-For，最后回退 remoteAddr。
 * </p>
 */
public final class IpUtil {

    private IpUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";

    private static final String[] IP_HEADER_NAMES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    /**
     * 从请求中解析客户端 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = null;
        for (String header : IP_HEADER_NAMES) {
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
            return UNKNOWN;
        }

        return LOCALHOST_IPV6.equals(ip) ? LOCALHOST_IPV4 : ip;
    }

    private static String getFirstValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
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
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }

}


