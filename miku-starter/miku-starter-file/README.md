# Miku File Storage Starter

> 优雅的文件存储 Starter，支持本地、阿里云 OSS、MinIO 多种存储策略

## ✨ 特性

- 🎯 **策略模式**: 统一接口，灵活切换存储策略
- 🔌 **即插即用**: 引入依赖即可使用，零配置启动
- 🚀 **多种存储**: 支持本地、OSS、MinIO
- 📦 **自动管理**: 自动创建目录、生成文件名
- 🛡️ **类型安全**: 完整的类型定义
- 📝 **详细文档**: 完善的使用示例

---

## 📦 安装

### Maven

```xml
<dependency>
    <groupId>com.miku</groupId>
    <artifactId>miku-starter-file</artifactId>
    <version>0.0.1</version>
</dependency>
```

---

## 🚀 快速开始

### 1. 本地存储（默认）

**配置文件 `application.yml`:**

```yaml
miku:
  file:
    type: local # 可选: local, oss, minio
    local:
      path: ./uploads # 本地存储路径
      prefix: /files # 访问前缀
      enable-direct-access: true # 是否启用直接访问
```

**使用示例:**

```java
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorage fileStorage;

    @PostMapping
    public Result<FileInfo> upload(@RequestParam("file") MultipartFile file) throws IOException {
        FileInfo fileInfo = fileStorage.upload(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType()
        );
        return Result.success(fileInfo);
    }

    @GetMapping("/download/{path}")
    public void download(@PathVariable String path, HttpServletResponse response) throws IOException {
        InputStream inputStream = fileStorage.download(path);
        // 将流写入响应
        IOUtils.copy(inputStream, response.getOutputStream());
    }
}
```

---

### 2. 阿里云 OSS 存储

**添加依赖:**

```xml
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
</dependency>
```

**配置文件:**

```yaml
miku:
  file:
    type: oss
    oss:
      access-key-id: your-access-key-id
      access-key-secret: your-access-key-secret
      endpoint: oss-cn-hangzhou.aliyuncs.com
      bucket-name: your-bucket-name
      custom-domain: https://cdn.example.com # 可选：自定义域名
      prefix: miku/ # 文件前缀路径
```

**使用示例:**

```java
// 使用方式与本地存储完全相同
FileInfo fileInfo = fileStorage.upload(
    inputStream,
    "avatar.jpg",
    "image/jpeg"
);

System.out.println("文件 URL: " + fileInfo.getUrl());
// 输出: https://your-bucket-name.oss-cn-hangzhou.aliyuncs.com/miku/2025/11/03/xxx.jpg
```

---

### 3. MinIO 存储

**添加依赖:**

```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
</dependency>
```

**配置文件:**

```yaml
miku:
  file:
    type: minio
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: miku
      prefix: miku/
```

**使用示例:**

```java
// 使用方式与本地存储完全相同
FileInfo fileInfo = fileStorage.upload(
    inputStream,
    "document.pdf",
    "application/pdf"
);

System.out.println("文件 URL: " + fileInfo.getUrl());
// 输出: 预签名 URL（7天有效）
```

---

## 📖 API 文档

### FileStorage 接口

#### 1. 上传文件

```java
/**
 * 上传文件（自动生成文件名和路径）
 */
FileInfo upload(InputStream inputStream, String originalFileName, String contentType);

/**
 * 上传文件（指定路径）
 */
FileInfo upload(InputStream inputStream, String path, String originalFileName, String contentType);
```

**示例:**

```java
// 方式1: 自动生成路径
FileInfo fileInfo = fileStorage.upload(
    inputStream,
    "avatar.jpg",
    "image/jpeg"
);
// 生成路径: 2025/11/03/uuid.jpg

// 方式2: 指定路径
FileInfo fileInfo = fileStorage.upload(
    inputStream,
    "avatars/user-123.jpg",
    "avatar.jpg",
    "image/jpeg"
);
```

#### 2. 下载文件

```java
InputStream download(String path);
```

**示例:**

```java
InputStream inputStream = fileStorage.download("2025/11/03/xxx.jpg");
```

#### 3. 删除文件

```java
boolean delete(String path);
```

**示例:**

```java
boolean success = fileStorage.delete("2025/11/03/xxx.jpg");
```

#### 4. 检查文件是否存在

```java
boolean exists(String path);
```

**示例:**

```java
boolean exists = fileStorage.exists("2025/11/03/xxx.jpg");
```

#### 5. 获取文件 URL

```java
String getUrl(String path);
```

**示例:**

```java
String url = fileStorage.getUrl("2025/11/03/xxx.jpg");
```

#### 6. 获取文件信息

```java
FileInfo getFileInfo(String path);
```

**示例:**

```java
FileInfo fileInfo = fileStorage.getFileInfo("2025/11/03/xxx.jpg");
System.out.println("文件大小: " + fileInfo.getSize());
System.out.println("文件类型: " + fileInfo.getContentType());
```

---

## 🎨 FileInfo 对象

```java
public class FileInfo {
    private String fileName;           // 文件名
    private String originalFileName;   // 原始文件名
    private String extension;          // 扩展名
    private Long size;                 // 文件大小（字节）
    private String contentType;        // 内容类型
    private String path;               // 存储路径
    private String url;                // 访问 URL
    private String storageType;        // 存储类型: local, oss, minio
    private Long uploadTime;           // 上传时间戳
}
```

---

## 🔧 高级用法

### 1. 文件类型校验

```java
@PostMapping("/upload/image")
public Result<FileInfo> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
    // 校验文件扩展名
    if (!FileUtils.isAllowedExtension(file.getOriginalFilename(), "jpg", "jpeg", "png", "gif")) {
        return Result.error("只支持图片格式");
    }

    // 校验文件大小（2MB）
    if (file.getSize() > 2 * 1024 * 1024) {
        return Result.error("文件大小不能超过2MB");
    }

    FileInfo fileInfo = fileStorage.upload(
        file.getInputStream(),
        file.getOriginalFilename(),
        file.getContentType()
    );

    return Result.success(fileInfo);
}
```

### 2. 分类存储

```java
@PostMapping("/upload/avatar")
public Result<FileInfo> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
    // 指定存储在 avatars 目录下
    String path = "avatars/" + FileUtils.generateFileName(file.getOriginalFilename());

    FileInfo fileInfo = fileStorage.upload(
        file.getInputStream(),
        path,
        file.getOriginalFilename(),
        file.getContentType()
    );

    return Result.success(fileInfo);
}
```

### 3. 文件大小格式化

```java
FileInfo fileInfo = fileStorage.getFileInfo("2025/11/03/xxx.jpg");
String sizeStr = FileUtils.formatSize(fileInfo.getSize());
System.out.println("文件大小: " + sizeStr);  // 输出: 1.23 MB
```

---

## ⚙️ 配置参数

### 本地存储配置

| 参数                                   | 说明             | 默认值      |
| -------------------------------------- | ---------------- | ----------- |
| `miku.file.local.path`                 | 本地存储路径     | `./uploads` |
| `miku.file.local.prefix`               | 访问前缀         | `/files`    |
| `miku.file.local.enable-direct-access` | 是否启用直接访问 | `true`      |

### 阿里云 OSS 配置

| 参数                              | 说明         | 必填              |
| --------------------------------- | ------------ | ----------------- |
| `miku.file.oss.access-key-id`     | 访问密钥 ID  | ✅                |
| `miku.file.oss.access-key-secret` | 访问密钥密码 | ✅                |
| `miku.file.oss.endpoint`          | Endpoint     | ✅                |
| `miku.file.oss.bucket-name`       | Bucket 名称  | ✅                |
| `miku.file.oss.custom-domain`     | 自定义域名   | ❌                |
| `miku.file.oss.prefix`            | 文件前缀路径 | ❌ (默认 `miku/`) |

### MinIO 配置

| 参数                          | 说明           | 必填              |
| ----------------------------- | -------------- | ----------------- |
| `miku.file.minio.endpoint`    | MinIO 服务地址 | ✅                |
| `miku.file.minio.access-key`  | 访问密钥       | ✅                |
| `miku.file.minio.secret-key`  | 访问密钥密码   | ✅                |
| `miku.file.minio.bucket-name` | Bucket 名称    | ✅                |
| `miku.file.minio.prefix`      | 文件前缀路径   | ❌ (默认 `miku/`) |

---

## 💡 最佳实践

### 1. 开发环境使用本地存储

```yaml
# application-dev.yml
miku:
  file:
    type: local
    local:
      path: ./dev-uploads
```

### 2. 生产环境使用 OSS

```yaml
# application-prod.yml
miku:
  file:
    type: oss
    oss:
      access-key-id: ${OSS_ACCESS_KEY_ID}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET}
      endpoint: ${OSS_ENDPOINT}
      bucket-name: ${OSS_BUCKET_NAME}
```

### 3. 存储文件记录到数据库

```java
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorage fileStorage;
    private final FileMapper fileMapper;

    public SysFile uploadAndSave(MultipartFile file) throws IOException {
        // 1. 上传文件
        FileInfo fileInfo = fileStorage.upload(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType()
        );

        // 2. 保存到数据库
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileInfo.getFileName());
        sysFile.setOriginalFileName(fileInfo.getOriginalFileName());
        sysFile.setFilePath(fileInfo.getPath());
        sysFile.setFileUrl(fileInfo.getUrl());
        sysFile.setFileSize(fileInfo.getSize());
        sysFile.setFileType(fileInfo.getExtension());
        sysFile.setStorageType(fileInfo.getStorageType());

        fileMapper.insert(sysFile);

        return sysFile;
    }
}
```

---

## 🔄 切换存储策略

只需修改配置文件中的 `miku.file.type`，无需修改代码：

```yaml
# 切换到 OSS
miku:
  file:
    type: oss
```

```yaml
# 切换回本地
miku:
  file:
    type: local
```

---

## 📝 许可证

[Apache License 2.0](LICENSE)

---

**Miku File Storage Starter** - 让文件存储变得优雅简单 🚀
