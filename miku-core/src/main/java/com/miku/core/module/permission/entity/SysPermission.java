package com.miku.core.module.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miku.core.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 权限实体（菜单+按钮+接口）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限类型（1目录 2菜单 3按钮 4接口）
     */
    private Integer permissionType;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 是否可见（1是 0否）
     */
    private Integer visible;
    
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
     * API方法（GET/POST/PUT/DELETE）
     */
    private String apiMethod;
    
    /**
     * API路径
     */
    private String apiPath;
    
    /**
     * 子权限列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysPermission> children;
}

