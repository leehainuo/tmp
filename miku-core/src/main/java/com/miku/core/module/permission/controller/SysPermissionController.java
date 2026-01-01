package com.miku.core.module.permission.controller;

import com.miku.core.module.permission.entity.SysPermission;
import com.miku.core.module.permission.service.ISysPermissionService;
import com.miku.pkg.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限（菜单）管理控制器
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final ISysPermissionService permissionService;

    /**
     * 查询权限列表（树形）
     */
    @Operation(summary = "查询权限列表")
    @GetMapping("/list")
    @PreAuthorize("@perm.has('system:permission:list')")
    public Result<List<SysPermission>> list(
            @RequestParam(required = false) Integer visible,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String permissionName) {
        List<SysPermission> permissions = permissionService.getPermissionTree(visible, status, permissionName);
        return Result.success(permissions);
    }

    /**
     * 获取权限详情
     */
    @Operation(summary = "获取权限详情")
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:permission:query')")
    public Result<SysPermission> getById(@PathVariable Long id) {
        SysPermission permission = permissionService.getById(id);
        if (permission == null) {
            return Result.error("权限不存在");
        }
        return Result.success(permission);
    }

    /**
     * 新增权限
     */
    @Operation(summary = "新增权限")
    @PostMapping
    @PreAuthorize("@perm.has('system:permission:add')")
    public Result<Void> add(@RequestBody SysPermission permission) {
        boolean success = permissionService.save(permission);
        return success ? Result.success() : Result.error("新增权限失败");
    }

    /**
     * 修改权限
     */
    @Operation(summary = "修改权限")
    @PutMapping
    @PreAuthorize("@perm.has('system:permission:edit')")
    public Result<Void> update(@RequestBody SysPermission permission) {
        boolean success = permissionService.updateById(permission);
        return success ? Result.success() : Result.error("修改权限失败");
    }

    /**
     * 删除权限
     */
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:permission:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        // 校验是否可以删除
        String errorMsg = permissionService.validateBeforeDelete(id);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }

        boolean success = permissionService.removeById(id);
        return success ? Result.success() : Result.error("删除权限失败");
    }

    /**
     * 获取菜单树（用于前端路由）
     */
    @Operation(summary = "获取菜单树")
    @GetMapping("/menuTree")
    public Result<List<SysPermission>> getMenuTree() {
        // 使用优化的查询方法
        List<SysPermission> menus = permissionService.getAllMenuTree();
        return Result.success(menus);
    }

    /**
     * 获取用户菜单树
     */
    @Operation(summary = "获取用户菜单树")
    @GetMapping("/userMenuTree")
    public Result<List<SysPermission>> getUserMenuTree(@RequestParam Long userId) {
        // 使用优化的查询方法，避免N+1问题
        List<Integer> menuTypes = List.of(1, 2); // 目录和菜单
        List<SysPermission> menus = permissionService.getUserPermissionTree(userId, menuTypes);
        return Result.success(menus);
    }
}