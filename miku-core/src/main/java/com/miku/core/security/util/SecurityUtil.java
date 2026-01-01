package com.miku.core.security.util;

import com.miku.core.security.model.LoginUser;
import com.miku.pkg.constants.Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security工具类
 */
public class SecurityUtil {
    
    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }
    
    /**
     * 判断是否是超级管理员
     */
    public static boolean isSuperAdmin() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        // 判断是否有ROLE_ADMIN角色
        return loginUser.getRoles() != null && 
               loginUser.getRoles().contains(Constants.Permission.SUPER_ADMIN_ROLE);
    }
    
    /**
     * 判断是否有某个权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.getPermissions() != null && 
               loginUser.getPermissions().contains(permission);
    }
    
    /**
     * 判断是否有某个角色
     */
    public static boolean hasRole(String role) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.getRoles() != null && 
               loginUser.getRoles().contains(role);
    }
}

