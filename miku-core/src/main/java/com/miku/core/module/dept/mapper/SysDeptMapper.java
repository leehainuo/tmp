package com.miku.core.module.dept.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miku.core.module.dept.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门Mapper接口
 *
 * @author lihainuo.com
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 查询部门及其所有子部门ID
     *
     * @param deptId 部门ID
     * @return 部门ID列表
     */
    List<Long> selectDeptAndChildrenIds(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查询部门列表
     *
     * @param roleId 角色ID
     * @return 部门列表
     */
    List<SysDept> selectDeptListByRoleId(@Param("roleId") Long roleId);

    /**
     * 分页查询部门列表（包含父部门信息）
     *
     * @param page     分页参数
     * @param deptName 部门名称
     * @param status   状态
     * @param parentId 父部门ID
     * @return 分页结果
     */
    IPage<SysDept> selectDeptPageWithParent(Page<SysDept> page, @Param("deptName") String deptName,
                                            @Param("status") Integer status, @Param("parentId") Long parentId);
}

