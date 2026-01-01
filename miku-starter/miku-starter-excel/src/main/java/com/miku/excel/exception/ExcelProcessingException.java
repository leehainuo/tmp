package com.miku.excel.exception;

/**
 * Excel 处理异常。
 */
public class ExcelProcessingException extends RuntimeException {

    public ExcelProcessingException(String message) {
        super(message);
    }

    public ExcelProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

