package com.miku.core.security.service;

import com.miku.core.module.role.entity.SysRole;
import com.miku.core.module.user.entity.SysUser;
import com.miku.core.module.permission.service.ISysPermissionService;
import com.miku.core.module.role.service.ISysRoleService;
import com.miku.core.module.user.service.ISysUserService;
import com.miku.core.security.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        SysUser user = userService.getUserByUsername(username);
        if (user == null) {
            log.error("[Miku-Security] 用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 查询角色
        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        Set<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
        
        // 查询权限
        Set<String> permissions = permissionService.getPermissionCodesByUserId(user.getId());
        
        return new LoginUser(user, permissions, roleCodes);
    }
}

