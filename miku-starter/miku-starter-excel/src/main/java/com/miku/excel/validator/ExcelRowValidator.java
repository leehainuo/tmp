package com.miku.excel.validator;

import com.miku.excel.model.ExcelValidationError;
import java.util.Collections;
import java.util.List;

/**
 * 自定义 Excel 行级校验。
 */
@FunctionalInterface
public interface ExcelRowValidator<T> {

    /**
     * 校验单行数据。
     *
     * @param data     行数据
     * @param rowIndex 行号（从 1 开始）
     * @return 错误集合，如无错误可返回 {@link Collections#emptyList()}
     */
    List<ExcelValidationError> validate(T data, int rowIndex);
}

