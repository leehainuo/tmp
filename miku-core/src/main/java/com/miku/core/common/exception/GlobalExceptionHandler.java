package com.miku.core.common.exception;

import com.miku.pkg.Result;
import com.miku.pkg.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.List;

/**
 * 全局异常处理器
 *
 * @author lihainuo.com
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error("[Miku-Exception] 业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("[Miku-Exception] 认证失败: {} - {}", request.getRequestURI(), e.getMessage());
        if (e instanceof BadCredentialsException) {
            return Result.error(401, "用户名或密码错误");
        }
        return Result.error(401, "认证失败: " + e.getMessage());
    }

    /**
     * 权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("[Miku-Exception] 权限不足: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(403, "权限不足");
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception e, HttpServletRequest request) {
        log.error("[Miku-Exception] 参数校验失败: {} - {}", request.getRequestURI(), e.getMessage());

        List<FieldError> fieldErrors;
        if (e instanceof MethodArgumentNotValidException) {
            fieldErrors = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
        } else {
            fieldErrors = ((BindException) e).getBindingResult().getFieldErrors();
        }

        StringBuilder sb = new StringBuilder();
        for (FieldError error : fieldErrors) {
            sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        }

        return Result.error(400, sb.toString());
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[Miku-Exception] 系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统异常: " + e.getMessage());
    }
}

