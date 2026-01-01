package com.miku.core.module.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.miku.core.module.permission.entity.SysPermission;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface ISysPermissionService extends IService<SysPermission> {

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> getPermissionsByUserId(Long userId);

    /**
     * 根据用户ID查询权限标识集合
     *
     * @param userId 用户ID
     * @return 权限标识集合
     */
    Set<String> getPermissionCodesByUserId(Long userId);

    /**
     * 根据用户ID查询菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<SysPermission> getMenuTreeByUserId(Long userId);

    /**
     * 查询所有菜单树
     *
     * @return 菜单树
     */
    List<SysPermission> getAllMenuTree();

    /**
     * 权限树查询
     *
     * @param visible 可见性（1显示 0隐藏）
     * @param status 状态（1正常 0停用）
     * @param permissionName 权限名称（模糊查询）
     * @return 权限树
     */
    List<SysPermission> getPermissionTree(Integer visible, Integer status, String permissionName);

    /**
     * 用户权限树查询
     *
     * @param userId          用户ID
     * @param permissionTypes 权限类型列表
     * @return 权限树
     */
    List<SysPermission> getUserPermissionTree(Long userId, List<Integer> permissionTypes);

    /**
     * 构建树形结构
     *
     * @param permissions 权限列表
     * @return 树形结构
     */
    List<SysPermission> buildTree(List<SysPermission> permissions);

    /**
     * 校验权限是否可以删除
     *
     * @param permissionId 权限ID
     * @return 错误信息，如果为null表示可以删除
     */
    String validateBeforeDelete(Long permissionId);
}

