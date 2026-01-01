package com.miku.core.module.auth.controller;

import com.miku.core.common.annotation.Log;
import com.miku.core.security.util.JwtUtil;
import com.miku.core.security.util.SecurityUtil;
import com.miku.redis.annotation.RateLimiter;
import com.miku.redis.annotation.RepeatSubmit;
import com.miku.core.module.auth.dto.LoginRequest;
import com.miku.core.module.auth.dto.LoginResponse;
import com.miku.core.module.auth.dto.RefreshTokenRequest;
import com.miku.core.module.auth.dto.UserInfoResponse;
import com.miku.core.module.permission.entity.SysPermission;
import com.miku.core.module.permission.service.ISysPermissionService;
import com.miku.core.module.user.entity.SysUser;
import com.miku.core.module.user.service.ISysUserService;
import com.miku.core.security.model.LoginUser;
import com.miku.core.security.model.TokenPair;
import com.miku.core.common.util.IpUtil;
import com.miku.pkg.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 *
 * @author lihainuo.com
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ISysUserService userService;
    private final ISysPermissionService permissionService;

    /**
     * 登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @RateLimiter(key = "login", time = 60, count = 10, limitType = RateLimiter.LimitType.IP)
    @RepeatSubmit(interval = 3000, message = "请勿频繁登录")
    @Log(title = "用户登录", businessType = Log.BusinessType.OTHER, isSaveRequestData = false)
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        try {
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取登录用户
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            // 生成Token对
            TokenPair tokenPair = jwtUtil.generateTokenPair(
                    loginUser.getUserId(),
                    loginUser.getUsername()
            );

            // 更新登录信息
            String clientIp = IpUtil.getClientIp(httpRequest);
            userService.updateLoginInfo(loginUser.getUserId(), clientIp);

            log.info("[Miku-Security] 用户 {} 登录成功，IP: {}", loginUser.getUsername(), clientIp);

            return Result.success(new LoginResponse(
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken(),
                    tokenPair.getAccessTokenExpiresIn(),
                    tokenPair.getRefreshTokenExpiresIn()
            ));
        } catch (Exception e) {
            log.error("[Miku-Security] 登录失败: {}", e.getMessage());
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null) {
            return Result.error("未登录");
        }

        // 查询用户完整信息
        SysUser user = userService.getUserById(loginUser.getUserId());

        // 查询菜单列表
        List<SysPermission> menus = permissionService.getMenuTreeByUserId(user.getId());

        // 构建响应
        UserInfoResponse response = getUserInfoResponse(user, loginUser, menus);

        return Result.success(response);
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            // 验证刷新Token
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                return Result.error("刷新Token无效或已过期");
            }
            
            // 从刷新Token中获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            
            // 生成新的Token对
            TokenPair tokenPair = jwtUtil.generateTokenPair(userId, username);
            
            // 撤销旧的刷新Token
            jwtUtil.revokeToken(refreshToken);
            
            log.info("[Miku-Security] 用户 {} 刷新Token成功", username);
            
            return Result.success(new LoginResponse(
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken(),
                    tokenPair.getAccessTokenExpiresIn(),
                    tokenPair.getRefreshTokenExpiresIn()
            ));
        } catch (Exception e) {
            log.error("[Miku-Security] 刷新Token失败: {}", e.getMessage());
            return Result.error("刷新Token失败");
        }
    }

    /**
     * 登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            LoginUser loginUser = SecurityUtil.getLoginUser();
            if (loginUser != null) {
                // 获取当前Token并加入黑名单
                String token = getTokenFromRequest(request);
                if (token != null) {
                    jwtUtil.revokeToken(token);
                }
                
                log.info("[Miku-Security] 用户 {} 登出", loginUser.getUsername());
            }
            SecurityContextHolder.clearContext();
            return Result.success();
        } catch (Exception e) {
            log.error("[Miku-Security] 登出失败: {}", e.getMessage());
            return Result.error("登出失败");
        }
    }

    /**
     * 构建用户信息响应
     */
    private static UserInfoResponse getUserInfoResponse(SysUser user, LoginUser loginUser, List<SysPermission> menus) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setDeptId(user.getDeptId());
        if (user.getDept() != null) {
            response.setDeptName(user.getDept().getDeptName());
        }
        response.setRoles(loginUser.getRoles());
        response.setPermissions(loginUser.getPermissions());
        response.setMenus(menus);
        return response;
    }

    /**
     * 从请求头中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}

