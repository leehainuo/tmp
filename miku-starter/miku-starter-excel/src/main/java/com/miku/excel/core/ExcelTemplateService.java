package com.miku.excel.core;

import com.miku.excel.config.ExcelProperties;
import com.miku.excel.model.ExcelExportRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Excel 模板服务。
 */
public class ExcelTemplateService {

    private final ExcelProperties properties;
    private final ExcelExporter excelExporter;

    public ExcelTemplateService(ExcelProperties properties, ExcelExporter excelExporter) {
        this.properties = properties;
        this.excelExporter = excelExporter;
    }

    /**
     * 生成模板二进制。
     */
    public <T> byte[] generateTemplate(Class<T> headClass, String sheetName) {
        Assert.notNull(headClass, "headClass must not be null");
        ExcelExportRequest<T> request = ExcelExportRequest.<T>builder(headClass)
            .sheetName(sheetName)
            .data(Collections.emptyList())
            .build();
        return excelExporter.exportToBytes(request);
    }

    /**
     * 直接通过 HTTP 下载模板。
     */
    public <T> void downloadTemplate(HttpServletResponse response, Class<T> headClass, String fileName, String sheetName) {
        Assert.state(properties.getTemplate().isEnabled(), "Excel 模板功能已关闭");
        ExcelExportRequest<T> request = ExcelExportRequest.<T>builder(headClass)
            .sheetName(sheetName)
            .fileName(resolveTemplateFileName(fileName, headClass))
            .data(Collections.emptyList())
            .build();
        excelExporter.writeToResponse(response, request);
    }

    private <T> String resolveTemplateFileName(String preferredName, Class<T> headClass) {
        if (StringUtils.hasText(preferredName)) {
            return preferredName.toLowerCase().endsWith(".xlsx") ? preferredName : preferredName + ".xlsx";
        }
        String prefix = properties.getTemplate().getDefaultFileNamePrefix();
        String fallback = headClass != null ? headClass.getSimpleName() : "template";
        String result = StringUtils.hasText(prefix) ? prefix : fallback;
        return result.toLowerCase().endsWith(".xlsx") ? result : result + ".xlsx";
    }
}

