package com.miku.core.module.dept.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.miku.core.common.annotation.DataScope;
import com.miku.core.module.dept.dto.DeptQueryRequest;
import com.miku.core.module.dept.entity.SysDept;
import com.miku.core.module.dept.service.ISysDeptService;
import com.miku.pkg.PageResult;
import com.miku.pkg.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 *
 * @author lihainuo.com
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final ISysDeptService deptService;

    /**
     * 查询部门列表
     */
    @Operation(summary = "查询部门列表")
    @GetMapping("/list")
    @PreAuthorize("@perm.has('system:dept:list')")
    @DataScope(table = "", deptColumn = "id")
    public Result<PageResult<SysDept>> list(DeptQueryRequest request) {
        IPage<SysDept> page = deptService.queryDept(request);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询部门列表（树形）
     */
    @Operation(summary = "查询部门列表（树形）")
    @GetMapping("/list-tree")
    @PreAuthorize("@perm.has('system:dept:list')")
    @DataScope(table = "", deptColumn = "id")
    public Result<List<SysDept>> listTree(String deptName, Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(deptName != null, SysDept::getDeptName, deptName)
                .eq(status != null, SysDept::getStatus, status)
                .eq(SysDept::getDelFlag, 0)
                .orderByAsc(SysDept::getOrderNum);

        List<SysDept> depts = deptService.list(wrapper);
        return Result.success(deptService.buildTree(depts));
    }

    /**
     * 查询部门树
     */
    @Operation(summary = "查询部门树")
    @GetMapping("/tree")
    @PreAuthorize("@perm.has('system:dept:query')")
    @DataScope(table = "", deptColumn = "id")
    public Result<List<SysDept>> getDeptTree() {
        List<SysDept> depts = deptService.getDeptTree();
        return Result.success(depts);
    }

    /**
     * 获取部门详情
     */
    @Operation(summary = "获取部门详情")
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:dept:query')")
    public Result<SysDept> getById(@PathVariable Long id) {
        SysDept dept = deptService.getById(id);
        if (dept == null) {
            return Result.error("部门不存在");
        }
        return Result.success(dept);
    }

    /**
     * 新增部门
     */
    @Operation(summary = "新增部门")
    @PostMapping
    @PreAuthorize("@perm.has('system:dept:add')")
    public Result<Void> add(@RequestBody SysDept dept) {
        boolean success = deptService.save(dept);
        return success ? Result.success() : Result.error("新增部门失败");
    }

    /**
     * 修改部门
     */
    @Operation(summary = "修改部门")
    @PutMapping
    @PreAuthorize("@perm.has('system:dept:edit')")
    public Result<Void> update(@RequestBody SysDept dept) {
        boolean success = deptService.updateById(dept);
        return success ? Result.success() : Result.error("修改部门失败");
    }

    /**
     * 删除部门
     */
    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:dept:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        // 校验是否可以删除
        String errorMsg = deptService.validateBeforeDelete(id);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }

        boolean success = deptService.removeById(id);
        return success ? Result.success() : Result.error("删除部门失败");
    }
}