package com.miku.core.module.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miku.core.module.permission.entity.SysPermission;
import com.miku.core.module.permission.mapper.SysPermissionMapper;
import com.miku.core.module.permission.service.ISysPermissionService;
import com.miku.core.module.role.entity.SysRolePermission;
import com.miku.core.module.role.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    @Override
    public List<SysPermission> getPermissionsByUserId(Long userId) {
        return permissionMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public Set<String> getPermissionCodesByUserId(Long userId) {
        List<SysPermission> permissions = getPermissionsByUserId(userId);
        return permissions.stream()
                .map(SysPermission::getPerms)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    @Override
    public List<SysPermission> getMenuTreeByUserId(Long userId) {
        // 查询用户直接拥有的菜单权限（目录和菜单类型）
        List<Integer> menuTypes = List.of(1, 2);
        List<SysPermission> userMenus = permissionMapper.selectUserPermissionTree(userId, menuTypes)
                .stream()
                .filter(p -> p.getVisible() == 1)
                .collect(Collectors.toList());
        
        if (userMenus.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有父节点ID并批量查询
        Set<Long> allParentIds = collectAllParentIds(userMenus);
        if (!allParentIds.isEmpty()) {
            // 批量查询所有父节点（只查询可见的目录和菜单）
            List<SysPermission> parentMenus = list(new LambdaQueryWrapper<SysPermission>()
                    .in(SysPermission::getId, allParentIds)
                    .eq(SysPermission::getStatus, 1)
                    .eq(SysPermission::getDelFlag, 0)
                    .eq(SysPermission::getVisible, 1)
                    .in(SysPermission::getPermissionType, menuTypes)
            );
            
            // 合并父节点（去重）
            Set<Long> existingIds = userMenus.stream()
                    .map(SysPermission::getId)
                    .collect(Collectors.toSet());
            
            parentMenus.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .forEach(userMenus::add);
        }
        
        return buildTree(userMenus);
    }
    
    /**
     * 递归收集所有父节点ID（使用迭代方式，避免N+1问题）
     * 
     * @param menus 菜单列表
     * @return 所有父节点ID集合
     */
    private Set<Long> collectAllParentIds(List<SysPermission> menus) {
        Set<Long> allParentIds = new HashSet<>();
        Set<Long> currentLevelIds = menus.stream()
                .map(SysPermission::getParentId)
                .filter(pid -> pid != null && pid != 0)
                .collect(Collectors.toSet());
        
        // 迭代查询，直到没有新的父节点
        while (!currentLevelIds.isEmpty()) {
            allParentIds.addAll(currentLevelIds);
            
            // 批量查询当前层级的父节点
            List<SysPermission> parents = list(new LambdaQueryWrapper<SysPermission>()
                    .in(SysPermission::getId, currentLevelIds)
                    .eq(SysPermission::getStatus, 1)
                    .eq(SysPermission::getDelFlag, 0)
            );
            
            // 收集下一层级的父节点ID
            currentLevelIds = parents.stream()
                    .map(SysPermission::getParentId)
                    .filter(pid -> pid != null && pid != 0 && !allParentIds.contains(pid))
                    .collect(Collectors.toSet());
        }
        
        return allParentIds;
    }

    @Override
    public List<SysPermission> getAllMenuTree() {
        // 使用优化的查询方法
        List<Integer> menuTypes = List.of(1, 2); // 目录和菜单
        List<SysPermission> menus = permissionMapper.selectPermissionTree(null, menuTypes, 1, 1, null);
        return buildTree(menus);
    }

    /**
     * 权限树查询
     *
     * @param visible 可见性（1显示 0隐藏）
     * @param status 状态（1正常 0停用）
     * @param permissionName 权限名称（模糊查询）
     * @return 权限树
     */
    public List<SysPermission> getPermissionTree(Integer visible, Integer status, String permissionName) {
        List<SysPermission> permissions = permissionMapper.selectPermissionTree(null, null, visible, status, permissionName);
        return buildTree(permissions);
    }

    /**
     * 用户权限树查询
     *
     * @param userId          用户ID
     * @param permissionTypes 权限类型列表
     * @return 权限树
     */
    public List<SysPermission> getUserPermissionTree(Long userId, List<Integer> permissionTypes) {
        List<SysPermission> permissions = permissionMapper.selectUserPermissionTree(userId, permissionTypes);
        return buildTree(permissions);
    }

    @Override
    public List<SysPermission> buildTree(List<SysPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建权限ID映射，用于快速查找
        Set<Long> permissionIdSet = permissions.stream()
                .map(SysPermission::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        List<SysPermission> tree = new ArrayList<>();
        for (SysPermission permission : permissions) {
            // 如果是根节点（parent_id == 0），或者父节点不在列表中（状态过滤导致），则作为根节点
            if (permission.getParentId() == 0 || !permissionIdSet.contains(permission.getParentId())) {
                tree.add(permission);
                permission.setChildren(getChildren(permission.getId(), permissions));
            }
        }
        return tree;
    }

    /**
     * 递归查询子节点
     */
    private List<SysPermission> getChildren(Long parentId, List<SysPermission> permissions) {
        List<SysPermission> children = new ArrayList<>();
        for (SysPermission permission : permissions) {
            if (parentId.equals(permission.getParentId())) {
                children.add(permission);
                permission.setChildren(getChildren(permission.getId(), permissions));
            }
        }
        return children;
    }

    @Override
    public String validateBeforeDelete(Long permissionId) {
        // 检查是否有子权限（排除已删除的）
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getParentId, permissionId)
                .eq(SysPermission::getDelFlag, 0);
        long childCount = count(wrapper);
        if (childCount > 0) {
            return String.format("存在 %d 个子权限，无法删除", childCount);
        }

        // 检查是否被角色使用
        LambdaQueryWrapper<SysRolePermission> rolePermissionWrapper = new LambdaQueryWrapper<>();
        rolePermissionWrapper.eq(SysRolePermission::getPermissionId, permissionId);
        long roleCount = rolePermissionMapper.selectCount(rolePermissionWrapper);
        if (roleCount > 0) {
            return String.format("该权限已被 %d 个角色使用，无法删除", roleCount);
        }

        return null; // 可以删除
    }
}

