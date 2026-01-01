package com.miku.core.module.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.miku.core.module.permission.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有菜单权限（用于构建路由）
     *
     * @return 权限列表
     */
    List<SysPermission> selectMenuList();

    /**
     * 查询权限树
     *
     * @param permissionType  权限类型
     * @param permissionTypes 权限类型列表
     * @param visible 可见性（1显示 0隐藏）
     * @param status 状态（1正常 0停用）
     * @param permissionName 权限名称（模糊查询）
     * @return 权限列表
     */
    List<SysPermission> selectPermissionTree(@Param("permissionType") Integer permissionType,
                                             @Param("permissionTypes") List<Integer> permissionTypes,
                                             @Param("visible") Integer visible,
                                             @Param("status") Integer status,
                                             @Param("permissionName") String permissionName);

    /**
     * 查询用户权限树
     *
     * @param userId          用户ID
     * @param permissionTypes 权限类型列表
     * @return 权限列表
     */
    List<SysPermission> selectUserPermissionTree(@Param("userId") Long userId,
                                                 @Param("permissionTypes") List<Integer> permissionTypes);
}

