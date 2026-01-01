package com.miku.core.security;

import com.miku.core.module.user.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 权限服务测试
 *
 * @author lihainuo.com
 */
@DisplayName("权限服务测试")
class PermissionServiceTest {

    private PermissionService permissionService;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();

        // 创建测试用户
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");

        Set<String> permissions = Set.of(
                "system:user:list",
                "system:user:add",
                "system:role:*"
        );

        Set<String> roles = Set.of("ROLE_USER");

        loginUser = new LoginUser(user, permissions, roles);

        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("检查权限 - 拥有权限")
    void testHasPermission_HasPermission() {
        assertTrue(permissionService.has("system:user:list"));
    }

    @Test
    @DisplayName("检查权限 - 没有权限")
    void testHasPermission_NoPermission() {
        assertFalse(permissionService.has("system:dept:list"));
    }

    @Test
    @DisplayName("检查权限 - 通配符匹配")
    void testHasPermission_WildcardMatch() {
        assertTrue(permissionService.has("system:role:list"));
        assertTrue(permissionService.has("system:role:add"));
    }

    @Test
    @DisplayName("检查权限 - 空权限字符串")
    void testHasPermission_EmptyPermission() {
        assertFalse(permissionService.has(""));
        assertFalse(permissionService.has(null));
    }

    @Test
    @DisplayName("检查权限 - 取反")
    void testNotPermission() {
        assertTrue(permissionService.not("system:dept:list"));
        assertFalse(permissionService.not("system:user:list"));
    }

    @Test
    @DisplayName("检查权限 - 任意一个（OR）")
    void testAnyPermission() {
        assertTrue(permissionService.any("system:user:list", "system:dept:list"));
        assertFalse(permissionService.any("system:dept:list", "system:dept:add"));
    }

    @Test
    @DisplayName("检查权限 - 所有（AND）")
    void testAllPermission() {
        assertTrue(permissionService.all("system:user:list", "system:user:add"));
        assertFalse(permissionService.all("system:user:list", "system:dept:list"));
    }

    @Test
    @DisplayName("超级管理员 - 拥有所有权限")
    void testSuperAdmin_HasAllPermissions() {
        // 创建超级管理员
        SysUser adminUser = new SysUser();
        adminUser.setId(2L);
        adminUser.setUsername("admin");

        Set<String> adminRoles = Set.of("ROLE_ADMIN");
        Set<String> adminPermissions = Set.of("*:*:*");

        LoginUser adminLoginUser = new LoginUser(adminUser, adminPermissions, adminRoles);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminLoginUser);
        SecurityContextHolder.setContext(securityContext);

        // 验证超级管理员拥有任意权限
        assertTrue(permissionService.has("system:user:list"));
        assertTrue(permissionService.has("system:dept:delete"));
        assertTrue(permissionService.has("any:permission:here"));
    }
}

