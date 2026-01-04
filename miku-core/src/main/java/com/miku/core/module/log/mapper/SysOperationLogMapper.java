package com.miku.core.module.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miku.core.module.log.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 操作日志Mapper层
 *
 * @author lihainuo.com
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    IPage<SysOperationLog> selectOperationLogPage(
            Page<SysOperationLog> page,
            @Param("operName") String operName,
            @Param("businessType") Integer businessType,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("title") String title
    );

    Long countOperationLogs(
            @Param("operName") String operName,
            @Param("businessType") Integer businessType,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("title") String title
    );
}


