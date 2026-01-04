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
import com.miku.core.module.permission.mapper.SysPermissionMapper;
import com.miku.core.module.permission.entity.SysPermission;
import com.miku.core.module.role.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
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

        // 如果没有传入权限列表，直接返回（已删除所有关联）
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }

        // 确保如果分配了某个菜单下的按钮/接口权限，父菜单（目录/菜单类型）也一并分配。
        // 收集需要保存的权限ID（包含原始以及父级菜单）
        Set<Long> toSaveIds = new HashSet<>(permissionIds);

        // 批量查询并构建 id -> SysPermission 映射，避免 N+1 查询
        java.util.Map<Long, SysPermission> permMap = new java.util.HashMap<>();
        java.util.Set<Long> fetchIds = new java.util.HashSet<>(permissionIds);

        // 逐层向上批量拉取父节点，直到没有新的父节点需要拉取
        while (!fetchIds.isEmpty()) {
            List<SysPermission> fetched = permissionMapper.selectBatchIds(new java.util.ArrayList<>(fetchIds));
            fetchIds.clear();
            if (fetched == null || fetched.isEmpty()) break;
            for (SysPermission p : fetched) {
                if (p == null || p.getId() == null) continue;
                permMap.put(p.getId(), p);
                Long parentId = p.getParentId();
                if (parentId != null && parentId != 0 && !permMap.containsKey(parentId)) {
                    fetchIds.add(parentId);
                }
            }
        }

        // 使用内存中的 permMap 向上查找父节点，补齐目录/菜单类型的父权限
        for (Long pid : permissionIds) {
            SysPermission current = permMap.get(pid);
            Long parentId = current != null ? current.getParentId() : null;
            while (parentId != null && parentId != 0 && !toSaveIds.contains(parentId)) {
                SysPermission parentPerm = permMap.get(parentId);
                if (parentPerm == null) break;
                Integer parentType = parentPerm.getPermissionType();
                if (parentType != null && (parentType == 1 || parentType == 2)) {
                    toSaveIds.add(parentId);
                }
                parentId = parentPerm.getParentId();
            }
        }

        // 保存新的权限关联
        List<SysRolePermission> rolePermissions = toSaveIds.stream()
                .map(permissionId -> {
                    SysRolePermission rolePermission = new SysRolePermission();
                    rolePermission.setRoleId(roleId);
                    rolePermission.setPermissionId(permissionId);
                    rolePermission.setCreateTime(LocalDateTime.now());
                    return rolePermission;
                })
                .toList();
        rolePermissionMapper.insert(rolePermissions);
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

