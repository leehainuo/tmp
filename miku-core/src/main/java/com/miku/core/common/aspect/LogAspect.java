package com.miku.core.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miku.core.common.annotation.Log;
import com.miku.core.module.log.entity.SysOperationLog;
import com.miku.core.module.log.service.ISysOperationLogService;
import com.miku.core.common.util.IpUtil;
import com.miku.core.security.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志记录切面（迁移到 module.log 包）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ISysOperationLogService.class)
public class LogAspect {

    private final ISysOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(pointcut = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    @Async
    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            SysOperationLog operLog = new SysOperationLog();
            operLog.setStatus(e == null ? 1 : 0);

            String ip = IpUtil.getClientIp(request);
            operLog.setOperIp(ip);
            operLog.setOperUrl(request.getRequestURI());
            operLog.setRequestMethod(request.getMethod());

            try {
                String username = SecurityUtil.getUsername();
                operLog.setOperName(username);
            } catch (Exception ex) {
                operLog.setOperName("匿名用户");
            }

            if (e != null) {
                operLog.setErrorMsg(truncate(e.getMessage(), 2000));
            }

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            operLog.setTitle(controllerLog.title());
            operLog.setBusinessType(controllerLog.businessType().getValue());
            operLog.setOperatorType(controllerLog.operatorType().getValue());

            if (controllerLog.isSaveRequestData()) {
                setRequestValue(joinPoint, operLog);
            }

            if (controllerLog.isSaveResponseData() && jsonResult != null) {
                try {
                    operLog.setJsonResult(truncate(objectMapper.writeValueAsString(jsonResult), 2000));
                } catch (Exception ex) {
                    log.warn("[Miku-Log] 序列化响应参数异常: {}", ex.getMessage());
                }
            }

            operLog.setOperTime(LocalDateTime.now());

            operationLogService.save(operLog);

        } catch (Exception exp) {
            log.error("[Miku-Log] 记录操作日志异常: {}", exp.getMessage(), exp);
        }
    }

    private void setRequestValue(JoinPoint joinPoint, SysOperationLog operLog) {
        try {
            Object[] args = joinPoint.getArgs();
            String params = Arrays.stream(args)
                    .filter(arg -> arg != null &&
                            !arg.getClass().getName().startsWith("jakarta.servlet") &&
                            !arg.getClass().getName().startsWith("org.springframework"))
                    .map(arg -> {
                        try {
                            return objectMapper.writeValueAsString(arg);
                        } catch (Exception e) {
                            return arg.toString();
                        }
                    })
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            operLog.setOperParam(truncate(params, 2000));
        } catch (Exception e) {
            log.warn("[Miku-Log] 获取请求参数异常: {}", e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}


