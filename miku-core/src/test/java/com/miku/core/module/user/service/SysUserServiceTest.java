package com.miku.core.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miku.core.module.user.dto.UserQueryRequest;
import com.miku.core.module.user.entity.SysUser;
import com.miku.core.module.user.mapper.SysUserMapper;
import com.miku.core.module.user.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 *
 * @author lihainuo.com
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class SysUserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @InjectMocks
    private SysUserServiceImpl userService;

    private SysUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setPassword("$2a$10$test");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setDeptId(1L);
        testUser.setStatus(1);
        testUser.setDelFlag(0);
        testUser.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据用户名查询用户 - 成功")
    void testGetUserByUsername_Success() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // When
        SysUser result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getNickname());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据用户名查询用户 - 用户不存在")
    void testGetUserByUsername_NotFound() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        SysUser result = userService.getUserByUsername("nonexistent");

        // Then
        assertNull(result);
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void testGetUserById_Success() {
        // Given
        when(userMapper.selectUserById(1L)).thenReturn(testUser);

        // When
        SysUser result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).selectUserById(1L);
    }

    @Test
    @DisplayName("新增用户 - 成功")
    void testAddUser_Success() {
        // Given
        when(userMapper.insert(any(SysUser.class))).thenReturn(1);

        // When
        boolean result = userService.addUser(testUser);

        // Then
        assertTrue(result);
        verify(userMapper, times(1)).insert(testUser);
    }

    @Test
    @DisplayName("新增用户 - 失败")
    void testAddUser_Failure() {
        // Given
        when(userMapper.insert(any(SysUser.class))).thenReturn(0);

        // When
        boolean result = userService.addUser(testUser);

        // Then
        assertFalse(result);
        verify(userMapper, times(1)).insert(testUser);
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void testUpdateUser_Success() {
        // Given
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        // When
        boolean result = userService.updateUser(testUser);

        // Then
        assertTrue(result);
        verify(userMapper, times(1)).updateById(testUser);
    }

    @Test
    @DisplayName("删除用户（逻辑删除）- 成功")
    void testDeleteUser_Success() {
        // Given
        testUser.setDelFlag(1);
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertTrue(result);
        verify(userMapper, times(1)).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("更新登录信息")
    void testUpdateLoginInfo() {
        // Given
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        // When
        userService.updateLoginInfo(1L, "192.168.1.1");

        // Then
        verify(userMapper, times(1)).updateById(any(SysUser.class));
    }
}

