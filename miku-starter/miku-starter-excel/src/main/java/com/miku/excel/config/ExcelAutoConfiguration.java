package com.miku.excel.config;

import com.miku.excel.core.ExcelExporter;
import com.miku.excel.core.ExcelImporter;
import com.miku.excel.core.ExcelTemplateService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Excel Starter 自动配置。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.alibaba.excel.EasyExcel")
@EnableConfigurationProperties(ExcelProperties.class)
public class ExcelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExcelImporter excelImporter(ExcelProperties excelProperties,
                                       ObjectProvider<Validator> validatorProvider) {
        return new ExcelImporter(excelProperties, validatorProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public ExcelExporter excelExporter(ExcelProperties excelProperties) {
        return new ExcelExporter(excelProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExcelTemplateService excelTemplateService(ExcelProperties excelProperties,
                                                     ExcelExporter excelExporter) {
        return new ExcelTemplateService(excelProperties, excelExporter);
    }
}

