package com.miku.core.module.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miku.core.module.user.dto.UserQueryRequest;
import com.miku.core.module.user.entity.SysUser;

/**
 * 用户服务接口
 */
public interface ISysUserService extends IService<SysUser> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    SysUser getUserByUsername(String username);
    
    /**
     * 根据用户ID查询用户（包含角色）
     * @param userId 用户ID
     * @return 用户对象
     */
    SysUser getUserById(Long userId);
    
    /**
     * 新增用户
     * @param user 用户对象
     * @return 是否成功
     */
    boolean addUser(SysUser user);
    
    /**
     * 更新用户
     * @param user 用户对象
     * @return 是否成功
     */
    boolean updateUser(SysUser user);
    
    /**
     * 删除用户（逻辑删除）
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);
    
    /**
     * 更新用户登录信息
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    void updateLoginInfo(Long userId, String loginIp);

    /**
     * 查询用户列表
     * @param request 查询条件
     * @return 分页结果
     */
    IPage<SysUser> queryUser(UserQueryRequest request);
}

