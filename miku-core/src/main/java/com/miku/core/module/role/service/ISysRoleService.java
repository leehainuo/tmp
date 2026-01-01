package com.miku.core.module.role.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miku.core.module.role.dto.RoleQueryRequest;
import com.miku.core.module.role.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);

    /**
     * 根据角色ID查询角色（包含权限）
     *
     * @param roleId 角色ID
     * @return 角色对象
     */
    SysRole getRoleById(Long roleId);

    /**
     * 新增角色
     *
     * @param role 角色对象
     * @return 是否成功
     */
    boolean addRole(SysRole role);

    /**
     * 更新角色
     *
     * @param role 角色对象
     * @return 是否成功
     */
    boolean updateRole(SysRole role);

    /**
     * 删除角色（逻辑删除）
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);

    /**
     * 分配角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 分配角色数据权限（自定义部门）
     *
     * @param roleId  角色ID
     * @param deptIds 部门ID列表
     */
    void assignDataScope(Long roleId, List<Long> deptIds);

    /**
     * 查询角色列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    IPage<SysRole> queryRole(RoleQueryRequest request);
}

