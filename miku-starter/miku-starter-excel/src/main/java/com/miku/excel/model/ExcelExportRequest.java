package com.miku.excel.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Excel 导出请求。
 *
 * @param <T> 数据类型
 */
public final class ExcelExportRequest<T> {

    private final List<T> data;
    private final Class<T> headClass;
    private final String sheetName;
    private final String fileName;

    private ExcelExportRequest(Builder<T> builder) {
        this.data = Collections.unmodifiableList(new ArrayList<>(builder.data));
        this.headClass = builder.headClass;
        this.sheetName = builder.sheetName;
        this.fileName = builder.fileName;
    }

    public static <T> Builder<T> builder(Class<T> headClass) {
        return new Builder<>(headClass);
    }

    public List<T> getData() {
        return data;
    }

    public Class<T> getHeadClass() {
        return headClass;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getFileName() {
        return fileName;
    }

    public static final class Builder<T> {
        private final Class<T> headClass;
        private List<T> data = Collections.emptyList();
        private String sheetName;
        private String fileName;

        private Builder(Class<T> headClass) {
            this.headClass = Objects.requireNonNull(headClass, "headClass must not be null");
        }

        public Builder<T> data(List<T> data) {
            this.data = data != null ? data : Collections.emptyList();
            return this;
        }

        public Builder<T> sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public Builder<T> fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public ExcelExportRequest<T> build() {
            return new ExcelExportRequest<>(this);
        }
    }
}

