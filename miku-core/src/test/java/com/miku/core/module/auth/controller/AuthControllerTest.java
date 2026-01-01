package com.miku.core.module.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miku.core.module.auth.dto.LoginRequest;
import com.miku.core.module.user.entity.SysUser;
import com.miku.core.module.user.service.ISysUserService;
import com.miku.core.security.model.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证控制器集成测试
 *
 * @author lihainuo.com
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private ISysUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private SysUser testUser;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setNickname("管理员");
        testUser.setPassword("$2a$10$test");
        testUser.setDeptId(1L);

        loginUser = new LoginUser(
                testUser,
                Collections.singleton("system:user:list"),
                Collections.singleton("ROLE_ADMIN")
        );
    }

    @Test
    @DisplayName("登录 - 成功")
    void testLogin_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.updateLoginInfo(any(), any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("登录 - 用户名或密码错误")
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("获取用户信息 - 需要认证")
    void testGetUserInfo_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/info"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("登出 - 成功")
    void testLogout_Success() throws Exception {
        // Given
        String token = jwtUtil.generateToken(1L, "admin");

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

