package com.miku.redis.exception;

import java.io.Serial;

/**
 * Starter 内部使用的业务异常
 * <p>
 * 用于在限流、防重复提交等场景中抛出带有简单错误码的运行时异常，
 * 避免依赖外部的业务异常类（例如 com.miku.pkg.exception.ServiceException）。
 */
public class RedisServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    public RedisServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}


