package com.miku.excel.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Excel 配置属性绑定测试。
 *
 * 验证 application 配置能正确绑定到 {@link ExcelProperties}。
 */
@SpringBootTest(
        classes = ExcelPropertiesBindingTest.TestConfig.class,
        properties = {
                "miku.excel.default-sheet-name=测试Sheet",
                "miku.excel.import.batch-size=1000",
                "miku.excel.import.fail-fast=true",
                "miku.excel.import.enable-bean-validation=false",
                "miku.excel.import.store-data=true",
                "miku.excel.export.content-type=application/vnd.ms-excel",
                "miku.excel.export.default-file-name-prefix=my-export",
                "miku.excel.export.file-name-date-pattern=yyyyMMdd",
                "miku.excel.export.file-name-pattern={prefix}-{timestamp}.xlsx",
                "miku.excel.template.enabled=false",
                "miku.excel.template.default-file-name-prefix=my-template"
        }
)
class ExcelPropertiesBindingTest {

    @Configuration
    @ImportAutoConfiguration(ExcelAutoConfiguration.class)
    static class TestConfig {
        // 仅导入自动配置与配置属性
    }

    @Autowired
    private ExcelProperties properties;

    @Test
    void shouldBindExcelPropertiesFromConfiguration() {
        // 顶层属性
        assertThat(properties.getDefaultSheetName()).isEqualTo("测试Sheet");

        // 导入配置
        assertThat(properties.getImport().getBatchSize()).isEqualTo(1000);
        assertThat(properties.getImport().isFailFast()).isTrue();
        assertThat(properties.getImport().isEnableBeanValidation()).isFalse();
        assertThat(properties.getImport().isStoreData()).isTrue();

        // 导出配置
        assertThat(properties.getExport().getContentType()).isEqualTo("application/vnd.ms-excel");
        assertThat(properties.getExport().getDefaultFileNamePrefix()).isEqualTo("my-export");
        assertThat(properties.getExport().getFileNameDatePattern()).isEqualTo("yyyyMMdd");
        assertThat(properties.getExport().getFileNamePattern()).isEqualTo("{prefix}-{timestamp}.xlsx");

        // 模板配置
        assertThat(properties.getTemplate().isEnabled()).isFalse();
        assertThat(properties.getTemplate().getDefaultFileNamePrefix()).isEqualTo("my-template");
    }
}


