package com.miku.core.module.auth.dto;


import com.miku.core.module.permission.entity.SysPermission;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 用户信息响应DTO
 *
 * @author lihainuo.com
 */
@Data
public class UserInfoResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 角色列表
     */
    private Set<String> roles;
    
    /**
     * 权限列表
     */
    private Set<String> permissions;
    
    /**
     * 菜单列表
     */
    private List<SysPermission> menus;
}

