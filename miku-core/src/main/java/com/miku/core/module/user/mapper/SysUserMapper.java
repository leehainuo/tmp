package com.miku.core.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miku.core.module.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户（包含角色和部门信息）
     *
     * @param username 用户名
     * @return 用户对象
     */
    SysUser selectUserByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户（包含角色和部门信息）
     *
     * @param userId 用户ID
     * @return 用户对象
     */
    SysUser selectUserById(@Param("userId") Long userId);

    /**
     * 分页查询用户列表（包含角色和部门信息）
     *
     * @param page     分页参数
     * @param username 用户名
     * @param phone    手机号码
     * @param status   状态
     * @return 分页结果
     */
    IPage<SysUser> selectUserPageWithDeptAndRoles(Page<SysUser> page, @Param("username") String username,
                                                  @Param("phone") String phone, @Param("status") Integer status);
}

