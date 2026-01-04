package com.miku.core.module.log.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志查询D请求
 *
 * @author lihainuo.com
 */
@Data
public class OperationLogQueryRequest {

    private Long current = 1L;

    private Long size = 10L;

    private String operName;

    private Integer businessType;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String title;
}


