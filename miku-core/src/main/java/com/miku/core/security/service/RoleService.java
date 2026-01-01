package com.miku.core.security.service;

import com.miku.core.security.model.LoginUser;
import com.miku.core.security.util.SecurityUtil;
import com.miku.pkg.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;

/**
 * 角色验证服务
 * 用于 @PreAuthorize 注解中调用
 * <p>
 * 使用示例：
 * | @PreAuthorize("@role.has('admin')") - 拥有某角色
 * | @PreAuthorize("@role.not('guest')") - 不具备某角色
 * | @PreAuthorize("@role.any('admin','manager')") - 任意一个角色（OR）
 * | @PreAuthorize("@role.all('user','verified')") - 所有角色（AND）
 * <p>
 * 组合使用：
 * | @PreAuthorize("@role.has('admin') or @perm.has('system:user:special')")
 */
@Slf4j
@Service("role")
public class RoleService {

    /**
     * 判断用户是否拥有某个角色
     *
     * @param role 角色标识
     * @return 是否拥有该角色
     */
    public boolean has(String role) {
        if (!StringUtils.hasText(role)) {
            return false;
        }

        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null) {
            return false;
        }

        Set<String> roles = loginUser.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        // 超级管理员拥有所有角色
        if (roles.contains(Constants.Permission.SUPER_ADMIN_ROLE)) {
            return true;
        }

        return roles.contains(role.trim());
    }

    /**
     * 验证用户是否不具备某角色（取反）
     *
     * @param role 角色标识
     * @return 是否不具备该角色
     */
    public boolean not(String role) {
        return !has(role);
    }

    /**
     * 验证用户是否具有任意一个角色（OR 逻辑）
     *
     * @param roles 角色标识数组
     * @return 是否具有任意一个角色
     */
    public boolean any(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(this::has);
    }

    /**
     * 验证用户是否具有所有角色（AND 逻辑）
     *
     * @param roles 角色标识数组
     * @return 是否具有所有角色
     */
    public boolean all(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.stream(roles).allMatch(this::has);
    }

    /**
     * 判断当前用户是否是超级管理员
     *
     * @return 是否是超级管理员
     */
    public boolean isAdmin() {
        return SecurityUtil.isSuperAdmin();
    }
}

