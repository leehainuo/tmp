package com.miku.core.module.dept.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miku.pkg.constants.Constants;
import com.miku.core.module.dept.dto.DeptQueryRequest;
import com.miku.core.module.dept.entity.SysDept;
import com.miku.core.module.dept.mapper.SysDeptMapper;
import com.miku.core.module.dept.service.ISysDeptService;
import com.miku.core.module.role.entity.SysRole;
import com.miku.core.module.role.entity.SysRoleDept;
import com.miku.core.module.role.mapper.SysRoleDeptMapper;
import com.miku.core.module.role.service.ISysRoleService;
import com.miku.core.module.user.entity.SysUser;
import com.miku.core.module.user.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 *
 * @author lihainuo.com
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysDeptMapper deptMapper;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final SysRoleDeptMapper roleDeptMapper;

    @Override
    public List<SysDept> getDeptTree() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getDelFlag, 0)
                .eq(SysDept::getStatus, Constants.Status.ENABLE)
                .orderByAsc(SysDept::getOrderNum);
        List<SysDept> depts = list(wrapper);
        return buildTree(depts);
    }

    @Override
    public List<Long> getDataScopeDeptIds(Long userId) {
        Set<Long> deptIds = new HashSet<>();

        // 获取用户信息
        SysUser user = userService.getUserById(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        // 获取用户角色列表
        List<SysRole> roles = roleService.getRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据角色的数据权限范围计算部门ID列表
        for (SysRole role : roles) {
            Integer dataScope = role.getDataScope();
            if (dataScope == null) {
                continue;
            }

            switch (dataScope) {
                case Constants.DataScope.ALL: // 全部数据权限
                    LambdaQueryWrapper<SysDept> allWrapper = new LambdaQueryWrapper<>();
                    allWrapper.eq(SysDept::getDelFlag, 0)
                            .eq(SysDept::getStatus, Constants.Status.ENABLE);
                    return list(allWrapper).stream()
                            .map(SysDept::getId)
                            .collect(Collectors.toList());
                case Constants.DataScope.CUSTOM: // 自定义数据权限
                    SysRole roleWithDepts = roleService.getRoleById(role.getId());
                    if (roleWithDepts.getDeptIds() != null) {
                        deptIds.addAll(roleWithDepts.getDeptIds());
                    }
                    break;

                case Constants.DataScope.DEPT: // 本部门数据权限
                    if (user.getDeptId() != null) {
                        deptIds.add(user.getDeptId());
                    }
                    break;

                case Constants.DataScope.DEPT_AND_CHILD: // 本部门及以下数据权限
                    if (user.getDeptId() != null) {
                        List<Long> childDeptIds = getDeptAndChildrenIds(user.getDeptId());
                        deptIds.addAll(childDeptIds);
                    }
                    break;

                case Constants.DataScope.SELF: // 仅本人数据权限
                    // 返回空列表，在业务层需要额外判断 userId
                    break;

                default:
                    break;
            }
        }

        return new ArrayList<>(deptIds);
    }

    @Override
    public List<Long> getDeptAndChildrenIds(Long deptId) {
        return deptMapper.selectDeptAndChildrenIds(deptId);
    }

    @Override
    public List<SysDept> buildTree(List<SysDept> depts) {
        if (depts == null || depts.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建部门ID映射，用于快速查找
        Set<Long> deptIdSet = depts.stream()
                .map(SysDept::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : depts) {
            // 如果是根节点（parent_id == 0），或者父节点不在列表中（数据权限过滤导致），则作为根节点
            if (dept.getParentId() == 0 || !deptIdSet.contains(dept.getParentId())) {
                tree.add(dept);
                dept.setChildren(getChildren(dept.getId(), depts));
            }
        }
        return tree;
    }

    /**
     * 查询部门列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<SysDept> queryDept(DeptQueryRequest request) {
        Page<SysDept> page = new Page<>(request.getCurrent(), request.getSize());
        return deptMapper.selectDeptPageWithParent(page, request.getDeptName(),
                request.getStatus(), request.getParentId());
    }

    /**
     * 递归查询子节点
     */
    private List<SysDept> getChildren(Long parentId, List<SysDept> depts) {
        List<SysDept> children = new ArrayList<>();
        for (SysDept dept : depts) {
            if (parentId.equals(dept.getParentId())) {
                children.add(dept);
                dept.setChildren(getChildren(dept.getId(), depts));
            }
        }
        return children;
    }

    @Override
    public String validateBeforeDelete(Long deptId) {
        // 检查是否有子部门
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, deptId)
                .eq(SysDept::getDelFlag, 0);
        long childCount = count(wrapper);
        if (childCount > 0) {
            return String.format("存在 %d 个子部门，无法删除", childCount);
        }

        // 检查部门下是否有用户
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getDeptId, deptId)
                .eq(SysUser::getDelFlag, 0);
        long userCount = userService.count(userWrapper);
        if (userCount > 0) {
            return String.format("该部门下存在 %d 个用户，无法删除", userCount);
        }

        // 检查是否被角色用于数据权限
        LambdaQueryWrapper<SysRoleDept> roleDeptWrapper = new LambdaQueryWrapper<>();
        roleDeptWrapper.eq(SysRoleDept::getDeptId, deptId);
        long roleCount = roleDeptMapper.selectCount(roleDeptWrapper);
        if (roleCount > 0) {
            return String.format("该部门已被 %d 个角色用于数据权限，无法删除", roleCount);
        }

        return null;
    }
}

