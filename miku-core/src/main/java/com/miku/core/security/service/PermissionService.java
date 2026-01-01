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
 * 权限验证服务
 * 用于 @PreAuthorize 注解中调用
 * <p>
 * 使用示例：
 * | @PreAuthorize("@perm.has('system:user:list')") - 拥有某权限
 * | @PreAuthorize("@perm.not('system:user:export')") - 不具备某权限
 * | @PreAuthorize("@perm.any('system:user:edit','system:user:add')") - 任意一个权限（OR）
 * | @PreAuthorize("@perm.all('system:user:edit','system:user:query')") - 所有权限（AND）
 * <p>
 * 组合使用：
 * | @PreAuthorize("@role.has('admin') or @perm.has('system:user:special')")
 * <p>
 * 注：角色验证请使用 @role 服务，参见 {@link RoleService}
 */
@Slf4j
@Service("perm")
public class PermissionService {

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 是否具备权限
     */
    public boolean has(String permission) {
        if (!StringUtils.hasText(permission)) {
            return false;
        }

        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null) {
            return false;
        }

        // 超级管理员拥有所有权限
        if (SecurityUtil.isSuperAdmin()) {
            log.debug("[Miku-Security] 超级管理员用户 {} 访问权限: {}", loginUser.getUsername(), permission);
            return true;
        }

        Set<String> permissions = loginUser.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            log.warn("[Miku-Security] 用户 {} 没有任何权限", loginUser.getUsername());
            return false;
        }

        // 检查是否拥有全部权限
        if (permissions.contains(Constants.Permission.ALL_PERMISSION)) {
            return true;
        }

        // 精确匹配
        if (permissions.contains(permission.trim())) {
            return true;
        }

        // 通配符匹配
        return hasWildcardPermission(permissions, permission);
    }

    /**
     * 验证用户是否不具备某权限（取反）
     *
     * @param permission 权限字符串
     * @return 是否不具备权限
     */
    public boolean not(String permission) {
        return !has(permission);
    }

    /**
     * 验证用户是否具有任意一个权限（OR 逻辑）
     *
     * @param permissions 权限字符串数组
     * @return 是否具有任意一个权限
     */
    public boolean any(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(this::has);
    }

    /**
     * 验证用户是否具有所有权限（AND 逻辑）
     *
     * @param permissions 权限字符串数组
     * @return 是否具有所有权限
     */
    public boolean all(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions).allMatch(this::has);
    }

    /**
     * 通配符权限匹配
     * 支持格式：system:user:* 或 system:*:* 等
     *
     * @param permissions 用户拥有的权限集合
     * @param permission  需要验证的权限
     * @return 是否匹配
     */
    private boolean hasWildcardPermission(Set<String> permissions, String permission) {
        String[] targetParts = permission.split(":");

        for (String perm : permissions) {
            if (perm.contains("*")) {
                String[] permParts = perm.split(":");
                if (matchWildcard(permParts, targetParts)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 通配符匹配逻辑
     *
     * @param pattern 包含通配符的模式
     * @param target  目标字符串数组
     * @return 是否匹配
     */
    private boolean matchWildcard(String[] pattern, String[] target) {
        // 长度不同，无法匹配
        if (pattern.length != target.length) {
            return false;
        }

        // 逐段比较
        for (int i = 0; i < pattern.length; i++) {
            if (!"*".equals(pattern[i]) && !pattern[i].equals(target[i])) {
                return false;
            }
        }

        return true;
    }
}

