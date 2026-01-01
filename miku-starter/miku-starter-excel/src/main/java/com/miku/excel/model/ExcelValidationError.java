package com.miku.excel.model;

import java.util.Objects;

/**
 * Excel 导入校验错误。
 */
public class ExcelValidationError {

    private int rowIndex;
    private final String field;
    private final String message;
    private final Object rejectedValue;

    private ExcelValidationError(Builder builder) {
        this.rowIndex = builder.rowIndex;
        this.field = builder.field;
        this.message = builder.message;
        this.rejectedValue = builder.rejectedValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public static class Builder {
        private int rowIndex;
        private String field;
        private String message;
        private Object rejectedValue;

        private Builder() {
        }

        public Builder rowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
            return this;
        }

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder rejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
            return this;
        }

        public ExcelValidationError build() {
            Objects.requireNonNull(message, "message must not be null");
            return new ExcelValidationError(this);
        }
    }
}

