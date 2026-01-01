package com.miku.core.module.dept.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.miku.core.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 部门实体
 *
 * @author lihainuo.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 父部门ID
     */
    private Long parentId;
    
    /**
     * 祖级列表
     */
    private String ancestors;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 负责人
     */
    private String leader;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
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
     * 子部门列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysDept> children;
}

