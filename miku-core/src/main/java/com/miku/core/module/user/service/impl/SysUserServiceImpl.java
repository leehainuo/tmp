package com.miku.core.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miku.core.module.user.dto.UserQueryRequest;
import com.miku.core.module.user.entity.SysUser;
import com.miku.core.module.user.entity.SysUserRole;
import com.miku.core.module.user.mapper.SysUserMapper;
import com.miku.core.module.user.mapper.SysUserRoleMapper;
import com.miku.core.module.user.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public SysUser getUserByUsername(String username) {
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public SysUser getUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(SysUser user) {
        // 保存用户
        boolean success = save(user);
        if (success && user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            // 保存用户-角色关联
            List<SysUserRole> userRoles = user.getRoleIds().stream()
                    .map(roleId -> {
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(user.getId());
                        userRole.setRoleId(roleId);
                        userRole.setCreateTime(LocalDateTime.now());
                        return userRole;
                    })
                    .toList();
            if (!userRoles.isEmpty()) {
                userRoleMapper.insert(userRoles);
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        // 更新用户
        boolean success = updateById(user);
        if (success && user.getRoleIds() != null) {
            // 删除原有角色关联
            LambdaUpdateWrapper<SysUserRole> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SysUserRole::getUserId, user.getId());
            userRoleMapper.delete(wrapper);

            // 保存新的角色关联
            if (!user.getRoleIds().isEmpty()) {
                List<SysUserRole> userRoles = user.getRoleIds().stream()
                        .map(roleId -> {
                            SysUserRole userRole = new SysUserRole();
                            userRole.setUserId(user.getId());
                            userRole.setRoleId(roleId);
                            userRole.setCreateTime(LocalDateTime.now());
                            return userRole;
                        })
                        .toList();
                userRoleMapper.insert(userRoles);
            }
        }
        return success;
    }

    @Override
    public boolean deleteUser(Long userId) {
        // 逻辑删除（MyBatis-Plus会自动处理@TableLogic注解）
        return removeById(userId);
    }

    @Override
    public void updateLoginInfo(Long userId, String loginIp) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLoginIp(loginIp);
        user.setLoginTime(LocalDateTime.now());
        updateById(user);
    }

    /**
     * 查询用户列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<SysUser> queryUser(UserQueryRequest request) {
        Page<SysUser> page = new Page<>(request.getCurrent(), request.getSize());
        return userMapper.selectUserPageWithDeptAndRoles(page, request.getUsername(),
                request.getPhone(), request.getStatus());
    }
}

