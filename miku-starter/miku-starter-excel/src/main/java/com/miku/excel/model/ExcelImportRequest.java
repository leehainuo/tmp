package com.miku.excel.model;

import com.miku.excel.validator.ExcelRowValidator;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Excel 导入请求。
 */
public final class ExcelImportRequest<T> {

    private final InputStream inputStream;
    private final Class<T> headClass;
    private final String sheetName;
    private final Integer batchSize;
    private final Boolean failFast;
    private final Boolean enableBeanValidation;
    private final Boolean storeData;
    private final String fileName;
    private final Consumer<List<T>> batchConsumer;
    private final ExcelRowValidator<T> rowValidator;

    private ExcelImportRequest(Builder<T> builder) {
        this.inputStream = builder.inputStream;
        this.headClass = builder.headClass;
        this.sheetName = builder.sheetName;
        this.batchSize = builder.batchSize;
        this.failFast = builder.failFast;
        this.enableBeanValidation = builder.enableBeanValidation;
        this.storeData = builder.storeData;
        this.fileName = builder.fileName;
        this.batchConsumer = builder.batchConsumer;
        this.rowValidator = builder.rowValidator;
    }

    public static <T> Builder<T> builder(Class<T> headClass, InputStream inputStream) {
        return new Builder<>(headClass, inputStream);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Class<T> getHeadClass() {
        return headClass;
    }

    public String getSheetName() {
        return sheetName;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public Boolean getFailFast() {
        return failFast;
    }

    public Boolean getEnableBeanValidation() {
        return enableBeanValidation;
    }

    public Boolean getStoreData() {
        return storeData;
    }

    public String getFileName() {
        return fileName;
    }

    public Consumer<List<T>> getBatchConsumer() {
        return batchConsumer;
    }

    public ExcelRowValidator<T> getRowValidator() {
        return rowValidator;
    }

    public static final class Builder<T> {
        private final InputStream inputStream;
        private final Class<T> headClass;
        private String sheetName;
        private Integer batchSize;
        private Boolean failFast;
        private Boolean enableBeanValidation;
        private Boolean storeData;
        private String fileName;
        private Consumer<List<T>> batchConsumer;
        private ExcelRowValidator<T> rowValidator;

        private Builder(Class<T> headClass, InputStream inputStream) {
            this.headClass = Objects.requireNonNull(headClass, "headClass must not be null");
            this.inputStream = Objects.requireNonNull(inputStream, "inputStream must not be null");
        }

        public Builder<T> sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public Builder<T> batchSize(Integer batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder<T> failFast(Boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        public Builder<T> enableBeanValidation(Boolean enableBeanValidation) {
            this.enableBeanValidation = enableBeanValidation;
            return this;
        }

        public Builder<T> storeData(Boolean storeData) {
            this.storeData = storeData;
            return this;
        }

        public Builder<T> fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder<T> batchConsumer(Consumer<List<T>> batchConsumer) {
            this.batchConsumer = batchConsumer;
            return this;
        }

        public Builder<T> rowValidator(ExcelRowValidator<T> rowValidator) {
            this.rowValidator = rowValidator;
            return this;
        }

        public ExcelImportRequest<T> build() {
            return new ExcelImportRequest<>(this);
        }
    }
}

