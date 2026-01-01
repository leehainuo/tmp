package com.miku.core.module.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miku.core.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色权限字符串
     */
    private String roleCode;
    
    /**
     * 显示顺序
     */
    private Integer roleSort;
    
    /**
     * 数据权限（1全部 2自定义 3本部门 4本部门及以下 5仅本人）
     */
    private Integer dataScope;
    
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
     * 权限ID列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> permissionIds;
    
    /**
     * 部门ID列表（自定义数据权限时使用，非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> deptIds;
}

