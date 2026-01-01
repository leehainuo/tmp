package com.miku.excel.core;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.miku.excel.config.ExcelProperties;
import com.miku.excel.exception.ExcelProcessingException;
import com.miku.excel.model.ExcelImportRequest;
import com.miku.excel.model.ExcelImportResult;
import com.miku.excel.model.ExcelValidationError;
import com.miku.excel.validator.ExcelRowValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Excel 导入服务。
 */
public class ExcelImporter {

    private final ExcelProperties properties;
    private final Validator validator;

    public ExcelImporter(ExcelProperties properties, @Nullable Validator validator) {
        this.properties = properties;
        this.validator = validator;
    }

    public <T> ExcelImportResult<T> importExcel(ExcelImportRequest<T> request) {
        Objects.requireNonNull(request, "ExcelImportRequest must not be null");
        ExcelImportResult<T> result = new ExcelImportResult<>();

        int batchSize = request.getBatchSize() != null ? request.getBatchSize() : properties.getImport().getBatchSize();
        boolean failFast = request.getFailFast() != null ? request.getFailFast() : properties.getImport().isFailFast();
        boolean beanValidationEnabled = request.getEnableBeanValidation() != null ? request.getEnableBeanValidation() : properties.getImport().isEnableBeanValidation();
        boolean storeData = request.getStoreData() != null ? request.getStoreData()
            : (request.getBatchConsumer() == null ? properties.getImport().isStoreData() : false);

        ValidatingEventListener<T> listener = new ValidatingEventListener<>(
            result,
            batchSize,
            failFast,
            beanValidationEnabled,
            storeData,
            request.getBatchConsumer(),
            request.getRowValidator(),
            validator
        );

        try (InputStream inputStream = request.getInputStream()) {
            ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream, request.getHeadClass(), listener)
                .autoCloseStream(true);
            String sheetName = StringUtils.hasText(request.getSheetName()) ? request.getSheetName() : properties.getDefaultSheetName();
            if (StringUtils.hasText(sheetName)) {
                readerBuilder.sheet(sheetName).doRead();
            } else {
                readerBuilder.sheet().doRead();
            }
        } catch (IOException e) {
            throw new ExcelProcessingException("读取 Excel 失败", e);
        } catch (ExcelProcessingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ExcelProcessingException("解析 Excel 过程中发生异常", e);
        }
        return result;
    }

    private static final class ValidatingEventListener<T> extends com.alibaba.excel.event.AnalysisEventListener<T> {

        private final ExcelImportResult<T> result;
        private final int batchSize;
        private final boolean failFast;
        private final boolean enableBeanValidation;
        private final boolean storeData;
        private final Consumer<List<T>> batchConsumer;
        private final ExcelRowValidator<T> rowValidator;
        private final Validator validator;

        private final List<T> buffer;

        private ValidatingEventListener(ExcelImportResult<T> result,
                                        int batchSize,
                                        boolean failFast,
                                        boolean enableBeanValidation,
                                        boolean storeData,
                                        Consumer<List<T>> batchConsumer,
                                        ExcelRowValidator<T> rowValidator,
                                        Validator validator) {
            this.result = result;
            this.batchSize = Math.max(1, batchSize);
            this.failFast = failFast;
            this.enableBeanValidation = enableBeanValidation;
            this.storeData = storeData;
            this.batchConsumer = batchConsumer;
            this.rowValidator = rowValidator;
            this.validator = validator;
            this.buffer = batchConsumer != null ? new ArrayList<>(this.batchSize) : null;
        }

        @Override
        public void invoke(T data, AnalysisContext context) {
            int rowIndex = context.readRowHolder().getRowIndex() + 1;
            result.increaseTotal();

            List<ExcelValidationError> errors = new ArrayList<>();
            if (enableBeanValidation && validator != null) {
                Set<ConstraintViolation<T>> violations = validator.validate(data);
                for (ConstraintViolation<T> violation : violations) {
                    errors.add(ExcelValidationError.builder()
                        .rowIndex(rowIndex)
                        .field(violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : null)
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build());
                }
            }

            if (rowValidator != null) {
                List<ExcelValidationError> customErrors = rowValidator.validate(data, rowIndex);
                if (!CollectionUtils.isEmpty(customErrors)) {
                    errors.addAll(customErrors);
                }
            }

            if (errors.isEmpty()) {
                result.increaseSuccess();
                if (storeData) {
                    result.addData(data);
                }
                if (batchConsumer != null) {
                    buffer.add(data);
                    if (buffer.size() >= batchSize) {
                        flushBuffer();
                    }
                }
            } else {
                errors.forEach(error -> {
                    if (error.getRowIndex() <= 0) {
                        error.setRowIndex(rowIndex);
                    }
                });
                result.addErrors(errors);
                result.increaseFailed();
                if (failFast) {
                    throw new ExcelProcessingException("第 " + rowIndex + " 行数据校验失败");
                }
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            flushBuffer();
        }

        private void flushBuffer() {
            if (batchConsumer != null && !CollectionUtils.isEmpty(buffer)) {
                batchConsumer.accept(new ArrayList<>(buffer));
                buffer.clear();
            }
        }
    }
}

