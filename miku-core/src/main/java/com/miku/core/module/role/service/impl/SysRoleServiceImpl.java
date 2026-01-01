package com.miku.core.module.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miku.core.module.role.dto.RoleQueryRequest;
import com.miku.core.module.role.entity.SysRole;
import com.miku.core.module.role.entity.SysRoleDept;
import com.miku.core.module.role.entity.SysRolePermission;
import com.miku.core.module.role.mapper.SysRoleDeptMapper;
import com.miku.core.module.role.mapper.SysRoleMapper;
import com.miku.core.module.role.mapper.SysRolePermissionMapper;
import com.miku.core.module.role.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public SysRole getRoleById(Long roleId) {
        return roleMapper.selectRoleById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRole(SysRole role) {
        // 保存角色基本信息
        boolean success = save(role);
        if (success && role.getId() != null && role.getId() > 0) {
            // 分配权限
            if (role.getPermissionIds() != null && !role.getPermissionIds().isEmpty()) {
                assignPermissions(role.getId(), role.getPermissionIds());
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        // 保存权限ID列表（在更新前保存，因为 updateById 可能会清空非数据库字段）
        List<Long> permissionIds = role.getPermissionIds();
        
        // 更新角色基本信息
        boolean success = updateById(role);
        if (success && role.getId() != null && role.getId() > 0) {
            // 分配权限（如果提供了权限列表，包括空列表）
            if (permissionIds != null) {
                assignPermissions(role.getId(), permissionIds);
            }
        }
        return success;
    }

    @Override
    public boolean deleteRole(Long roleId) {
        // 逻辑删除
        SysRole role = new SysRole();
        role.setId(roleId);
        role.setDelFlag(1);
        return updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限关联
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(wrapper);

        // 保存新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> rolePermissions = permissionIds.stream()
                    .map(permissionId -> {
                        SysRolePermission rolePermission = new SysRolePermission();
                        rolePermission.setRoleId(roleId);
                        rolePermission.setPermissionId(permissionId);
                        rolePermission.setCreateTime(LocalDateTime.now());
                        return rolePermission;
                    })
                    .toList();
            rolePermissionMapper.insert(rolePermissions);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDataScope(Long roleId, List<Long> deptIds) {
        // 删除原有部门关联
        LambdaQueryWrapper<SysRoleDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleDept::getRoleId, roleId);
        roleDeptMapper.delete(wrapper);

        // 保存新的部门关联
        if (deptIds != null && !deptIds.isEmpty()) {
            List<SysRoleDept> roleDepts = deptIds.stream()
                    .map(deptId -> {
                        SysRoleDept roleDept = new SysRoleDept();
                        roleDept.setRoleId(roleId);
                        roleDept.setDeptId(deptId);
                        roleDept.setCreateTime(LocalDateTime.now());
                        return roleDept;
                    })
                    .toList();
            roleDeptMapper.insert(roleDepts);
        }
    }

    /**
     * 查询角色列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<SysRole> queryRole(RoleQueryRequest request) {
        Page<SysRole> page = new Page<>(request.getCurrent(), request.getSize());
        return roleMapper.selectRolePageWithPermissions(page, request.getRoleName(),
                request.getRoleCode(), request.getStatus());
    }
}

