package com.miku.core.module.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.miku.core.module.role.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-部门关联Mapper接口
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
}

