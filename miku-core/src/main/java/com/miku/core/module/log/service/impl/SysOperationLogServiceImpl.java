package com.miku.core.module.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.miku.core.module.log.dto.OperationLogQueryRequest;
import com.miku.core.module.log.entity.SysOperationLog;
import com.miku.core.module.log.mapper.SysOperationLogMapper;
import com.miku.core.module.log.service.ISysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 *
 * @author lihainuo.com
 */
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements ISysOperationLogService {

    private final SysOperationLogMapper operationLogMapper;

    @Override
    public IPage<SysOperationLog> queryOperationLog(OperationLogQueryRequest request) {
        Page<SysOperationLog> page = new Page<>(request.getCurrent(), request.getSize());
        return operationLogMapper.selectOperationLogPage(
                page,
                request.getOperName(),
                request.getBusinessType(),
                request.getStatus(),
                request.getStartTime(),
                request.getEndTime(),
                request.getTitle()
        );
    }

    @Override
    public Long countOperationLogs(OperationLogQueryRequest request) {
        return operationLogMapper.countOperationLogs(
                request.getOperName(),
                request.getBusinessType(),
                request.getStatus(),
                request.getStartTime(),
                request.getEndTime(),
                request.getTitle()
        );
    }

    @Override
    public int cleanHistoryLogs(Integer months, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (startTime != null) {
            wrapper.ge(SysOperationLog::getOperTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperationLog::getOperTime, endTime);
        }

        if (startTime == null && endTime == null) {
            int keepMonths = (months == null || months <= 0) ? 6 : months;
            LocalDateTime threshold = LocalDateTime.now().minusMonths(keepMonths);
            wrapper.le(SysOperationLog::getOperTime, threshold);
        }

        if (wrapper.getExpression().getNormal().isEmpty()) {
            return 0;
        }

        return operationLogMapper.delete(wrapper);
    }
}


