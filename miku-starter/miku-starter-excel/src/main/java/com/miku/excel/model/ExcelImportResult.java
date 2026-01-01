package com.miku.excel.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Excel 导入结果。
 *
 * @param <T> 数据类型
 */
public class ExcelImportResult<T> {

    private final List<T> data = new ArrayList<>();
    private final List<ExcelValidationError> errors = new ArrayList<>();
    private int totalRows;
    private int successRows;
    private int failedRows;

    public void increaseTotal() {
        this.totalRows++;
    }

    public void increaseSuccess() {
        this.successRows++;
    }

    public void increaseFailed() {
        this.failedRows++;
    }

    public void addData(T row) {
        this.data.add(row);
    }

    public void addError(ExcelValidationError error) {
        this.errors.add(error);
    }

    public void addErrors(List<ExcelValidationError> errorList) {
        if (errorList != null) {
            this.errors.addAll(errorList);
        }
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    public List<T> getData() {
        return Collections.unmodifiableList(data);
    }

    public List<ExcelValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getSuccessRows() {
        return successRows;
    }

    public int getFailedRows() {
        return failedRows;
    }
}

