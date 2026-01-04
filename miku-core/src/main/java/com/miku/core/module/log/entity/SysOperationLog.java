package com.miku.core.module.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author lihainuo.com
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private Integer businessType;

    private String method;

    private String requestMethod;

    private Integer operatorType;

    private String operName;

    private String deptName;

    private String operUrl;

    private String operIp;

    private String operLocation;

    private String operParam;

    private String jsonResult;

    private Integer status;

    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;
}


