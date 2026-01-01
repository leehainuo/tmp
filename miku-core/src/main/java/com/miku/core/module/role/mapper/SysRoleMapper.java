package com.miku.core.module.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miku.core.module.role.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询角色（包含权限ID列表）
     * @param roleId 角色ID
     * @return 角色对象
     */
    SysRole selectRoleById(@Param("roleId") Long roleId);
    
    /**
     * 分页查询角色列表（包含权限信息，避免N+1问题）
     * @param page 分页参数
     * @param roleName 角色名称
     * @param roleCode 角色编码
     * @param status 状态
     * @return 分页结果
     */
    IPage<SysRole> selectRolePageWithPermissions(Page<SysRole> page, @Param("roleName") String roleName,
                                                @Param("roleCode") String roleCode, @Param("status") Integer status);
}

