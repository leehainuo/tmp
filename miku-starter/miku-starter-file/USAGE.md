# miku-starter-file — 文件上传使用说明

本 starter 提供统一的文件存储能力（本地 / OSS / MinIO），并包含一组可配置的 HTTP 接口以便开箱即用。为了避免“上传后未完成业务逻辑导致的孤立文件（orphan）”问题，starter 默认采用两阶段上传（temporary → finalize），并提供可选的便捷原子接口用于头像等小文件场景。

## 默认 API（两阶段上传）

- `POST /upload`  
  描述：将文件保存到临时目录（`temp/{date}/{uuid.ext}`），返回 `FileInfo`（包含 `path`，例如 `temp/2025/12/24/xxxxx.jpg`）。  
  说明：文件仅为临时保存，随后应调用 finalize 将其移动到永久目录。

- `POST /upload/finalize`  
  描述：将临时文件移动到永久目录。请求体示例：  

  ```json
  {
    "tempPath": "temp/2025/12/24/xxxxx.jpg",
    "targetDir": "avatars" // 可选
  }
  ```

  返回：最终 `FileInfo`（包含 `url` / `path`）。

- `GET /upload/download/{path}`  
  描述：下载文件（适配所有存储实现）。

优点：两阶段上传能防止业务失败后遗留永久文件；适合大文件或需要审核/多步骤确认的场景。

## 可选：atomic 便捷端点（头像场景）

针对头像等小文件（需要一次性完成上传并写入用户资料），建议在应用层实现一个“原子”端点（例如 `POST /users/me/avatar`），在同一次请求内完成文件存储与数据库更新；若数据库更新失败，后端应删除刚写入的文件以回滚，保证无 orphan。starter 不默认启用此端点（由应用自己实现），但提供 `UploadApi` 接口以及 `DefaultUploadController`（starter 提供的默认实现）。

## 配置

在 `application.yml`（或 `application.properties`）中可以配置：

```yaml
miku:
  file:
    type: local # 或 oss/minio
    local:
      path: ./uploads
      prefix: /file
      enableDirectAccess: true
      tempRetentionHours: 24
      enableTempCleanup: false
    # 可选：miku.file.expose-controller=false 可关闭默认 controller
```

- `tempRetentionHours`：临时文件保留小时数，默认 24h。  
- `enableTempCleanup`：是否启用自动清理任务（需要应用启用 `@EnableScheduling`）；默认关闭。  
- `miku.file.expose-controller`（application property）：可控制是否暴露默认 controller（starter 中 `DefaultUploadController` 用了该属性作为保护）；在某些项目中可能使用网关或另一路由，这时可关闭。

## 覆盖默认 Controller

如果应用需要自定义上传行为（如不同路由、权限控制、一次性原子更新用户等），只需在应用模块中添加一个实现 `com.miku.file.web.UploadApi` 的 `@RestController` Bean。starter 的默认 controller 使用 `@ConditionalOnMissingBean(UploadApi.class)`，因此应用提供的实现会自动覆盖默认实现。

示例（伪代码）：

```java
@RestController
public class MyUploadController implements UploadApi {
    @PostMapping("/users/me/avatar")
    public Result<FileInfo> uploadAvatar(MultipartFile file, Principal user) {
        // 1) upload -> permanent path
        // 2) update user profile in DB
        // 3) if DB update fails -> delete file
    }
}
```

## 定期清理（可选）

starter 提供了 `TempFileCleanupTask`（当 `miku.file.local.enableTempCleanup=true` 时启用），它会定期清理 `temp/` 下超过 `tempRetentionHours` 的文件。注意：启用任务需要在应用中启用 Scheduling（在主应用类或配置类上加 `@EnableScheduling`）。

## 开发建议

- 头像类小文件：在应用层实现原子端点；或在客户端使用 finalize 流程。  
- 大文件/导入类场景：使用两阶段上传（先 `/upload`，处理完成后 `/upload/finalize`）。  
- 保留清理策略：即便使用原子端点，建议启用定期清理以防异常情况。


