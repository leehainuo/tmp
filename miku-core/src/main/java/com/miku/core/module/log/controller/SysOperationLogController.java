package com.miku.core.module.log.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.miku.core.module.log.dto.OperationLogQueryRequest;
import com.miku.core.module.log.entity.SysOperationLog;
import com.miku.core.module.log.service.ISysOperationLogService;
import com.miku.pkg.PageResult;
import com.miku.pkg.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final ISysOperationLogService operationLogService;

    @Operation(summary = "查询操作日志列表")
    @GetMapping("/list")
    @PreAuthorize("@perm.has('system:log:list')")
    public Result<PageResult<SysOperationLog>> list(OperationLogQueryRequest request) {
        IPage<SysOperationLog> page = operationLogService.queryOperationLog(request);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取操作日志详情")
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:log:query')")
    public Result<SysOperationLog> getById(@PathVariable Long id) {
        SysOperationLog log = operationLogService.getById(id);
        if (log == null) {
            return Result.error("操作日志不存在");
        }
        return Result.success(log);
    }

    @Operation(summary = "统计操作日志数量")
    @GetMapping("/count")
    @PreAuthorize("@perm.has('system:log:list')")
    public Result<Long> count(OperationLogQueryRequest queryDTO) {
        Long count = operationLogService.countOperationLogs(queryDTO);
        return Result.success(count);
    }

    @Operation(summary = "清理历史日志数据")
    @DeleteMapping("/clean")
    @PreAuthorize("@perm.has('system:log:remove')")
    public Result<Integer> cleanHistoryLogs(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false, defaultValue = "6") Integer months) {

        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = null;
        if (endDate != null) {
            endTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);
        }

        int cleanedCount = operationLogService.cleanHistoryLogs(months, startTime, endTime);
        return Result.success("清理完成", cleanedCount);
    }

    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:log:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = operationLogService.removeById(id);
        return success ? Result.success() : Result.error("删除操作日志失败");
    }

    @Operation(summary = "批量删除操作日志")
    @DeleteMapping("/batch")
    @PreAuthorize("@perm.has('system:log:remove')")
    public Result<Void> batchDelete(@RequestBody java.util.List<Long> ids) {
        boolean success = operationLogService.removeByIds(ids);
        return success ? Result.success() : Result.error("批量删除操作日志失败");
    }
}


