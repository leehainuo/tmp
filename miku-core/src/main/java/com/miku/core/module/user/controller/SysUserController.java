package com.miku.core.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.miku.core.common.annotation.DataScope;
import com.miku.core.common.annotation.Log;
import com.miku.core.module.user.dto.UserQueryRequest;
import com.miku.core.module.user.entity.SysUser;
import com.miku.core.module.user.service.ISysUserService;
import com.miku.pkg.PageResult;
import com.miku.pkg.Result;
import com.miku.file.service.FileStorage;
import com.miku.file.entity.FileInfo;
import com.miku.file.util.FileUtils;
import com.miku.file.config.FileProperties;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.miku.core.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorage fileStorage;
    private final FileProperties fileProperties;

    /**
     * 查询用户列表（带数据权限）
     */
    @Operation(summary = "查询用户列表")
    @GetMapping("/list")
    @PreAuthorize("@perm.has('system:user:list')")
    @DataScope(table = "u", deptColumn = "dept_id", userColumn = "id")
    public Result<PageResult<SysUser>> list(UserQueryRequest request) {
        // 使用优化的查询方法，避免N+1问题
        IPage<SysUser> page = userService.queryUser(request);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:query')")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("@perm.has('system:user:add')")
    @Log(title = "用户管理", businessType = Log.BusinessType.INSERT)
    public Result<Void> add(@RequestBody SysUser user) {
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        boolean success = userService.addUser(user);
        return success ? Result.success() : Result.error("新增用户失败");
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户")
    @PutMapping
    @PreAuthorize("@perm.has('system:user:edit')")
    @Log(title = "用户管理", businessType = Log.BusinessType.UPDATE)
    public Result<Void> update(@RequestBody SysUser user) {
        boolean success = userService.updateUser(user);
        return success ? Result.success() : Result.error("修改用户失败");
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('system:user:remove')")
    @Log(title = "用户管理", businessType = Log.BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return success ? Result.success() : Result.error("删除用户失败");
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码")
    @PutMapping("/resetPassword/{id}")
    @PreAuthorize("@perm.has('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = Log.BusinessType.UPDATE, isSaveRequestData = false)
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestParam String newPassword) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean success = userService.updateById(user);
        return success ? Result.success() : Result.error("重置密码失败");
    }

    /**
     * 上传并设置当前用户头像
     * 原子操作：上传 -> 更新用户记录；若更新失败则回滚删除文件
     */
    @Operation(summary = "上传并更新当前用户头像")
    @PostMapping("/avatar")
    @PreAuthorize("@perm.has('system:user:edit')")
    @Log(title = "用户管理", businessType = Log.BusinessType.UPDATE)
    public Result<FileInfo> uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        try {
            java.io.InputStream is = file.getInputStream();
            String targetPath = "avatars/" + FileUtils.getDatePath() + "/" + FileUtils.generateFileName(file.getOriginalFilename());
            FileInfo fileInfo = fileStorage.uploadWithHash(is, "avatars", file.getOriginalFilename(), file.getContentType());
            // Prefer configured baseUrl if present; otherwise keep relative URL (prefix + path)
            String relative = fileInfo.getUrl();
            // Always generate absolute URL from current request context
            String abs = ServletUriComponentsBuilder.fromCurrentContextPath().path(relative).toUriString();
            fileInfo.setUrl(abs);

            // update user record
            SysUser toUpdate = new SysUser();
            toUpdate.setId(userId);
            toUpdate.setAvatar(fileInfo.getUrl());
            boolean updated = userService.updateUser(toUpdate);
            if (!updated) {
                // rollback: delete uploaded file
                try {
                    fileStorage.delete(fileInfo.getPath());
                } catch (Exception ex) {
                    // log and continue
                }
                return Result.error("更新用户头像失败");
            }

            return Result.success(fileInfo);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}

