# Miku Excel Starter

基于 EasyExcel 的 Excel 导入 / 导出 / 模板下载能力，统一封装为 Spring Boot Starter，开箱即用。

## 能力概览

- `ExcelImporter`：支持批量导入、Bean Validation 校验、自定义行级校验、批量回调写库、失败明细收集、Fail Fast。
- `ExcelExporter`：统一 `content-type`、文件命名策略，一行代码导出到 `HttpServletResponse` 或字节流。
- `ExcelTemplateService`：根据实体头信息快速生成空模板（可用于“批量导入用户”模板下载）。
- `ExcelProperties`：`miku.excel.*` 配置项，统一 sheet、批处理大小、导出文件名模式等。
- 自动配置：引入依赖即可使用，可通过自定义 Bean 覆盖内置实现。

## 快速上手

```xml
<dependency>
  <groupId>com.miku</groupId>
  <artifactId>miku-starter-excel</artifactId>
</dependency>
```

```java
@RestController
@RequestMapping("/excel")
public class ExcelController {

    private final ExcelImporter excelImporter;
    private final ExcelExporter excelExporter;
    private final ExcelTemplateService excelTemplateService;

    public ExcelController(ExcelImporter excelImporter,
                           ExcelExporter excelExporter,
                           ExcelTemplateService excelTemplateService) {
        this.excelImporter = excelImporter;
        this.excelExporter = excelExporter;
        this.excelTemplateService = excelTemplateService;
    }

    @PostMapping("/import/users")
    public ExcelImportResult<UserImportRow> importUsers(@RequestPart MultipartFile file) throws IOException {
        ExcelImportRequest<UserImportRow> request = ExcelImportRequest
            .builder(UserImportRow.class, file.getInputStream())
            .rowValidator((row, rowIndex) -> {
                if (Objects.isNull(row.getPhone())) {
                    return List.of(ExcelValidationError.builder()
                        .rowIndex(rowIndex)
                        .field("phone")
                        .message("手机号必填")
                        .build());
                }
                return Collections.emptyList();
            })
            .batchConsumer(batch -> userService.saveBatch(batch))
            .storeData(false)
            .build();
        return excelImporter.importExcel(request);
    }

    @GetMapping("/export/users")
    public void exportUsers(HttpServletResponse response) {
        List<UserExportRow> rows = userService.listForExport();
        excelExporter.export(response, rows, UserExportRow.class, "用户报表.xlsx", "用户");
    }

    @GetMapping("/template/users")
    public void downloadTemplate(HttpServletResponse response) {
        excelTemplateService.downloadTemplate(response, UserImportRow.class, "用户导入模板.xlsx", "用户");
    }
}
```

## 关键配置

```yaml
miku:
  excel:
    default-sheet-name: Sheet1
    import:
      batch-size: 1000
      fail-fast: false
      enable-bean-validation: true
      store-data: false
    export:
      default-file-name-prefix: report
      file-name-pattern: "{prefix}_{timestamp}.xlsx"
      file-name-date-pattern: yyyyMMddHHmmss
    template:
      enabled: true
      default-file-name-prefix: template
```

## 扩展点

- 自定义 `ExcelRowValidator` 处理复杂校验（跨字段校验、数据库校验等）。
- 可通过声明同名 Bean 覆盖 `ExcelImporter` / `ExcelExporter` / `ExcelTemplateService`。
- `ExcelImportRequest` 支持批量回调、Fail Fast、是否保留内存数据等细粒度控制。

