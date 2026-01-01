package com.miku.core.module.role.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.miku.pkg.constants.Constants;
import com.miku.core.module.role.dto.RoleQueryRequest;
import com.miku.core.module.role.entity.SysRole;
import com.miku.core.module.role.service.ISysRoleService;
import com.miku.pkg.PageResult;
import com.miku.pkg.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;

    /**
     * 查询角色列表
     */
    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    @PreAuthorize("@perm.has('system:role:list')")
    public Result<PageResult<SysRole>> list(RoleQueryRequest request) {
        // 使用优化的查询方法，避免N+1问题
        IPage<SysRole> page = roleService.queryRole(request);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取角色详情
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:role:query')")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleService.getRoleById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("@perm.has('system:role:add')")
    public Result<Void> add(@RequestBody SysRole role) {
        boolean success = roleService.addRole(role);
        return success ? Result.success() : Result.error("新增角色失败");
    }

    /**
     * 修改角色
     */
    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("@perm.has('system:role:edit')")
    public Result<Void> update(@RequestBody SysRole role) {
        boolean success = roleService.updateRole(role);
        return success ? Result.success() : Result.error("修改角色失败");
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:role:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        return success ? Result.success() : Result.error("删除角色失败");
    }

    /**
     * 分配权限
     */
    @Operation(summary = "分配权限")
    @PutMapping("/assignPermissions/{roleId}")
    @PreAuthorize("@perm.has('system:role:edit')")
    public Result<Void> assignPermissions(@PathVariable Long roleId,
                                          @RequestBody List<Long> permissionIds) {
        boolean success = roleService.assignPermissions(roleId, permissionIds);
        return success ? Result.success() : Result.error("分配权限失败");
    }

    /**
     * 分配数据权限
     */
    @Operation(summary = "分配数据权限")
    @PutMapping("/assignDataScope/{roleId}")
    @PreAuthorize("@perm.has('system:role:edit')")
    public Result<Void> assignDataScope(@PathVariable Long roleId,
                                        @RequestParam Integer dataScope,
                                        @RequestBody(required = false) List<Long> deptIds) {
        // 更新数据权限范围
        SysRole role = new SysRole();
        role.setId(roleId);
        role.setDataScope(dataScope);
        roleService.updateById(role);

        // 如果是自定义数据权限，则保存部门关联
        if (dataScope == Constants.DataScope.CUSTOM && deptIds != null) {
            roleService.assignDataScope(roleId, deptIds);
        }

        return Result.success();
    }
}