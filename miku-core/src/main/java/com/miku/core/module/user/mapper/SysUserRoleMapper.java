package com.miku.core.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.miku.core.module.user.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联Mapper接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}

