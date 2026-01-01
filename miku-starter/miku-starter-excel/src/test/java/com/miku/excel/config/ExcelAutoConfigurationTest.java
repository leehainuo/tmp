package com.miku.excel.config;

import com.miku.excel.core.ExcelExporter;
import com.miku.excel.core.ExcelImporter;
import com.miku.excel.core.ExcelTemplateService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Excel 自动配置测试
 */
@SpringBootTest(classes = ExcelAutoConfigurationTest.TestConfig.class)
class ExcelAutoConfigurationTest {

    @Configuration
    @ImportAutoConfiguration(ExcelAutoConfiguration.class)
    static class TestConfig {

        @Bean
        Validator validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        ExcelProperties excelProperties() {
            return new ExcelProperties();
        }
    }

    @Autowired
    private ExcelImporter importer;

    @Autowired
    private ExcelExporter exporter;

    @Autowired
    private ExcelTemplateService templateService;

    @Test
    void shouldCreateExcelBeans() {
        assertThat(importer).isNotNull();
        assertThat(exporter).isNotNull();
        assertThat(templateService).isNotNull();
    }
}


