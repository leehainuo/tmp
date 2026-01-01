package com.miku.pkg.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常
 *
 * @author lihainuo.com
 */
@Getter
public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法
     *
     * @param message 错误消息
     * @param cause   错误异常
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 500;
    }
}

