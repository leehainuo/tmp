package com.miku.core.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miku.core.common.base.BaseEntity;
import com.miku.core.module.dept.entity.SysDept;
import com.miku.core.module.role.entity.SysRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户账号
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 密码
     */
    @JsonIgnore
    private String password;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 性别（0未知 1男 2女）
     */
    private Integer sex;
    
    /**
     * 头像路径
     */
    private String avatar;
    
    /**
     * 状态（1正常 0停用）
     */
    private Integer status;
    
    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private Integer delFlag;
    
    /**
     * 最后登录IP
     */
    private String loginIp;
    
    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    
    /**
     * 用户角色列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysRole> roles;
    
    /**
     * 角色ID列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> roleIds;
    
    /**
     * 部门对象（非数据库字段）
     */
    @TableField(exist = false)
    private SysDept dept;
}

