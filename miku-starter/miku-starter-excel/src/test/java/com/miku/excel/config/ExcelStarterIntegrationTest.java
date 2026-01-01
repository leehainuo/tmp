package com.miku.excel.config;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.miku.excel.core.ExcelExporter;
import com.miku.excel.core.ExcelImporter;
import com.miku.excel.core.ExcelTemplateService;
import com.miku.excel.model.ExcelExportRequest;
import com.miku.excel.model.ExcelImportRequest;
import com.miku.excel.model.ExcelImportResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Excel Starter 端到端集成测试。
 *
 * 覆盖导入、导出以及模板下载的核心链路，确保在生产环境下可用。
 */
@SpringBootTest(classes = ExcelStarterIntegrationTest.TestConfig.class)
class ExcelStarterIntegrationTest {

    @Configuration
    @ImportAutoConfiguration(ExcelAutoConfiguration.class)
    static class TestConfig {
        // 仅导入 ExcelAutoConfiguration，让自动配置按真实场景生效
    }

    @Autowired
    private ExcelImporter importer;

    @Autowired
    private ExcelExporter exporter;

    @Autowired
    private ExcelTemplateService templateService;

    /**
     * 用于测试的简单 Excel 行模型。
     */
    public static class UserRow {

        @ExcelProperty("ID")
        private Long id;

        @ExcelProperty("姓名")
        private String name;

        public UserRow() {
        }

        public UserRow(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void shouldExportToBytesSuccessfully() {
        List<UserRow> rows = List.of(
                new UserRow(1L, "张三"),
                new UserRow(2L, "李四")
        );

        ExcelExportRequest<UserRow> request = ExcelExportRequest.<UserRow>builder(UserRow.class)
                .data(rows)
                .sheetName("用户")
                .fileName("user-list.xlsx")
                .build();

        byte[] bytes = exporter.exportToBytes(request);

        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(0);
    }

    @Test
    void shouldImportExportRoundTripSuccessfully() {
        // 1. 先用 EasyExcel 写出一份合法的 Excel
        List<UserRow> source = List.of(
                new UserRow(100L, "Alice"),
                new UserRow(200L, "Bob")
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, UserRow.class)
                .autoCloseStream(true)
                .sheet("Sheet1")
                .doWrite(source);

        // 2. 再通过 ExcelImporter 导入回来
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ExcelImportRequest<UserRow> importRequest = ExcelImportRequest
                .builder(UserRow.class, in)
                .sheetName("Sheet1")
                .storeData(true)
                .build();

        ExcelImportResult<UserRow> result = importer.importExcel(importRequest);

        assertThat(result.getTotalRows()).isEqualTo(2);
        assertThat(result.getSuccessRows()).isEqualTo(2);
        assertThat(result.getFailedRows()).isZero();
        assertThat(result.getData())
                .extracting(UserRow::getId)
                .containsExactly(100L, 200L);
    }

    @Test
    void shouldDownloadTemplateThroughHttpResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        templateService.downloadTemplate(response, UserRow.class, null, "模板Sheet");

        byte[] content = response.getContentAsByteArray();

        assertThat(content).isNotNull();
        assertThat(content.length).isGreaterThan(0);
        assertThat(response.getContentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        assertThat(response.getHeader("Content-Disposition")).isNotBlank();
    }
}


