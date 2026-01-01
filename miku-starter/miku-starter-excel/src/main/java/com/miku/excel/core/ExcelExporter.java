package com.miku.excel.core;

import com.alibaba.excel.EasyExcel;
import com.miku.excel.config.ExcelProperties;
import com.miku.excel.exception.ExcelProcessingException;
import com.miku.excel.model.ExcelExportRequest;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;

/**
 * Excel 导出服务。
 */
public class ExcelExporter {

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    private final ExcelProperties properties;

    public ExcelExporter(ExcelProperties properties) {
        this.properties = properties;
    }

    /**
     * 导出为二进制数据。
     */
    public <T> byte[] exportToBytes(ExcelExportRequest<T> request) {
        Objects.requireNonNull(request, "ExcelExportRequest must not be null");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeInternal(outputStream, request);
        return outputStream.toByteArray();
    }

    /**
     * 写入 HTTP 响应。
     */
    public <T> void writeToResponse(HttpServletResponse response, ExcelExportRequest<T> request) {
        Objects.requireNonNull(response, "response must not be null");
        Objects.requireNonNull(request, "ExcelExportRequest must not be null");
        byte[] bytes = exportToBytes(request);
        writeResponse(response, bytes, resolveFileName(request.getFileName(), properties.getExport().getDefaultFileNamePrefix()));
    }

    /**
     * 导出通用列表。
     */
    public <T> void export(HttpServletResponse response, List<T> data, Class<T> headClass, String fileName, String sheetName) {
        ExcelExportRequest<T> request = ExcelExportRequest.<T>builder(headClass)
            .data(data)
            .fileName(fileName)
            .sheetName(sheetName)
            .build();
        writeToResponse(response, request);
    }

    private <T> void writeInternal(ByteArrayOutputStream outputStream, ExcelExportRequest<T> request) {
        try {
            EasyExcel.write(outputStream, request.getHeadClass())
                .autoCloseStream(true)
                .sheet(resolveSheetName(request.getSheetName()))
                .doWrite(request.getData());
        } catch (RuntimeException ex) {
            throw new ExcelProcessingException("写入 Excel 失败", ex);
        }
    }

    private void writeResponse(HttpServletResponse response, byte[] bytes, String fileName) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(properties.getExport().getContentType());
        response.setHeader(HEADER_CONTENT_DISPOSITION, buildContentDisposition(fileName));
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new ExcelProcessingException("写出 Excel 响应失败", e);
        }
    }

    private String buildContentDisposition(String fileName) {
        try {
            return "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            throw new ExcelProcessingException("文件名编码失败", ex);
        }
    }

    private String resolveSheetName(String preferredSheet) {
        return StringUtils.hasText(preferredSheet) ? preferredSheet : properties.getDefaultSheetName();
    }

    private String resolveFileName(String preferredName, String fallbackPrefix) {
        if (StringUtils.hasText(preferredName)) {
            return preferredName.toLowerCase().endsWith(".xlsx") ? preferredName : preferredName + ".xlsx";
        }
        ExcelProperties.Export export = properties.getExport();
        String prefix = StringUtils.hasText(export.getDefaultFileNamePrefix()) ? export.getDefaultFileNamePrefix() : fallbackPrefix;
        String timestamp = DateTimeFormatter.ofPattern(export.getFileNameDatePattern()).format(LocalDateTime.now());
        String template = export.getFileNamePattern();
        String resolved = template
            .replace("{prefix}", prefix)
            .replace("{timestamp}", timestamp);
        return resolved.toLowerCase().endsWith(".xlsx") ? resolved : resolved + ".xlsx";
    }
}

