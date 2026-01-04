package com.miku.file.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @author lihainuo.com
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功");
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功返回（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败");
    }

    /**
     * 失败返回（带消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message);
    }

    /**
     * 失败返回（带状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 权限不足
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message);
    }

    /**
     * 未认证
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message);
    }
}

