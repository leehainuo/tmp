package com.miku.excel.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Excel Starter 属性配置。
 */
@ConfigurationProperties(prefix = "miku.excel")
public class ExcelProperties {

    /**
     * 默认 sheet 名称。
     */
    private String defaultSheetName = "Sheet1";

    private final Import importConfig = new Import();
    private final Export export = new Export();
    private final Template template = new Template();

    public String getDefaultSheetName() {
        return defaultSheetName;
    }

    public void setDefaultSheetName(String defaultSheetName) {
        this.defaultSheetName = defaultSheetName;
    }

    public Import getImport() {
        return importConfig;
    }

    public Export getExport() {
        return export;
    }

    public Template getTemplate() {
        return template;
    }

    public static class Import {

        /**
         * 导入批处理大小，结合 batch consumer 一起使用。
         */
        private int batchSize = 500;

        /**
         * 是否在遇到第一条错误时立刻终止。
         */
        private boolean failFast = false;

        /**
         * 是否启用 Bean Validation 校验。
         */
        private boolean enableBeanValidation = true;

        /**
         * 是否在内存中保留成功的数据。
         */
        private boolean storeData = false;

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public boolean isFailFast() {
            return failFast;
        }

        public void setFailFast(boolean failFast) {
            this.failFast = failFast;
        }

        public boolean isEnableBeanValidation() {
            return enableBeanValidation;
        }

        public void setEnableBeanValidation(boolean enableBeanValidation) {
            this.enableBeanValidation = enableBeanValidation;
        }

        public boolean isStoreData() {
            return storeData;
        }

        public void setStoreData(boolean storeData) {
            this.storeData = storeData;
        }
    }

    public static class Export {

        /**
         * 响应 content-type。
         */
        private String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        /**
         * 默认文件名前缀。
         */
        private String defaultFileNamePrefix = "export";

        /**
         * 时间戳格式。
         */
        private String fileNameDatePattern = "yyyyMMddHHmmss";

        /**
         * 文件名模板，支持 {prefix} 和 {timestamp} 占位符。
         */
        private String fileNamePattern = "{prefix}_{timestamp}.xlsx";

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getDefaultFileNamePrefix() {
            return defaultFileNamePrefix;
        }

        public void setDefaultFileNamePrefix(String defaultFileNamePrefix) {
            this.defaultFileNamePrefix = defaultFileNamePrefix;
        }

        public String getFileNameDatePattern() {
            return fileNameDatePattern;
        }

        public void setFileNameDatePattern(String fileNameDatePattern) {
            this.fileNameDatePattern = fileNameDatePattern;
        }

        public String getFileNamePattern() {
            return fileNamePattern;
        }

        public void setFileNamePattern(String fileNamePattern) {
            this.fileNamePattern = fileNamePattern;
        }

        public String resolveFileName(String preferredName) {
            if (StringUtils.hasText(preferredName)) {
                return preferredName.toLowerCase().endsWith(".xlsx") ? preferredName : preferredName + ".xlsx";
            }
            String prefix = StringUtils.hasText(defaultFileNamePrefix) ? defaultFileNamePrefix : "export";
            String timestamp = DateTimeFormatter.ofPattern(fileNameDatePattern).format(LocalDateTime.now());
            String resolved = fileNamePattern
                .replace("{prefix}", prefix)
                .replace("{timestamp}", timestamp);
            return resolved.toLowerCase().endsWith(".xlsx") ? resolved : resolved + ".xlsx";
        }
    }

    public static class Template {

        /**
         * 是否启用模板下载功能。
         */
        private boolean enabled = true;

        /**
         * 默认模板文件名前缀。
         */
        private String defaultFileNamePrefix = "template";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDefaultFileNamePrefix() {
            return defaultFileNamePrefix;
        }

        public void setDefaultFileNamePrefix(String defaultFileNamePrefix) {
            this.defaultFileNamePrefix = defaultFileNamePrefix;
        }
    }
}

