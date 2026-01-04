package com.miku.core.module.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.miku.core.module.log.dto.OperationLogQueryRequest;
import com.miku.core.module.log.entity.SysOperationLog;

import java.time.LocalDateTime;

public interface ISysOperationLogService extends IService<SysOperationLog> {

    IPage<SysOperationLog> queryOperationLog(OperationLogQueryRequest request);

    Long countOperationLogs(OperationLogQueryRequest request);

    int cleanHistoryLogs(Integer months, LocalDateTime startTime, LocalDateTime endTime);
}


