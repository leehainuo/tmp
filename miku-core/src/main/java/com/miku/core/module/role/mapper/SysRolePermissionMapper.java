package com.miku.core.module.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.miku.core.module.role.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}

