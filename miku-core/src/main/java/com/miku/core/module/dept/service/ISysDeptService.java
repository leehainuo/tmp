package com.miku.core.module.dept.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miku.core.module.dept.dto.DeptQueryRequest;
import com.miku.core.module.dept.entity.SysDept;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author lihainuo.com
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 查询部门树
     *
     * @return 部门树
     */
    List<SysDept> getDeptTree();

    /**
     * 根据用户ID查询数据权限部门ID列表
     *
     * @param userId 用户ID
     * @return 部门ID列表
     */
    List<Long> getDataScopeDeptIds(Long userId);

    /**
     * 查询部门及其所有子部门ID
     *
     * @param deptId 部门ID
     * @return 部门ID列表
     */
    List<Long> getDeptAndChildrenIds(Long deptId);

    /**
     * 构建部门树
     *
     * @param depts 部门列表
     * @return 树形结构
     */
    List<SysDept> buildTree(List<SysDept> depts);

    /**
     * 查询部门列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    IPage<SysDept> queryDept(DeptQueryRequest request);

    /**
     * 校验部门是否可以删除
     *
     * @param deptId 部门ID
     * @return 错误信息，如果为null表示可以删除
     */
    String validateBeforeDelete(Long deptId);
}

