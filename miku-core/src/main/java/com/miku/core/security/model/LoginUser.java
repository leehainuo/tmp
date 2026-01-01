package com.miku.core.security.model;

import com.miku.pkg.constants.Constants;
import com.miku.core.module.user.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户信息
 */
@Data
public class LoginUser implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户对象
     */
    private SysUser user;
    
    /**
     * 权限标识集合
     */
    private Set<String> permissions;
    
    /**
     * 角色编码集合
     */
    private Set<String> roles;
    
    public LoginUser(SysUser user, Set<String> permissions, Set<String> roles) {
        this.user = user;
        this.permissions = permissions;
        this.roles = roles;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色和权限都转换为GrantedAuthority
        Set<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        
        authorities.addAll(permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()));
        
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() == Constants.Status.ENABLE;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getStatus() == Constants.Status.ENABLE && user.getDelFlag() == 0;
    }
    
    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return user.getId();
    }
}

