# Miku 管理后台框架

> 一个优雅、高扩展、高可用的企业级后台管理框架

## 📚 项目介绍

Miku 是一个基于 **Spring Boot 3.5.6 + MyBatis-Plus + Spring Security + JWT** 的企业级权限管理框架，采用 **RBAC（基于角色的访问控制）+ 数据权限** 模型，提供完整的用户、角色、权限、部门管理功能。

### ✨ 核心特性

#### 权限安全

- 🔐 **完整的 RBAC 模型**: 用户 → 角色 → 权限三级管理，支持菜单、按钮、接口三级权限控制
- 📊 **灵活的数据权限**: 全部/自定义/本部门/本部门及以下/仅本人五级数据权限
- 🚀 **高性能缓存**: Redis 缓存用户权限信息，大幅提升查询性能
- 🛡️ **安全可靠**: JWT 无状态认证、BCrypt 密码加密、全局异常处理
- 📝 **注解式鉴权**: 支持 `@PreAuthorize` 和 `@RequiresPermission` 注解
- 🎯 **自动数据过滤**: `@DataScope` 注解实现 MyBatis 拦截器自动注入数据权限 SQL

#### 开发效率

- ⚡ **代码生成器**: 可视化界面，支持指定模块，一键生成 Entity/Mapper/Service/Controller/Vue/API
- 📦 **模块化设计**: 多模块 Maven 项目，职责清晰，易于扩展
- 🎨 **优雅的 Starter**: 文件存储 Starter，支持本地/OSS/MinIO，即插即用

#### 功能完善

- 📄 **操作日志**: 异步记录，不影响性能
- 🚦 **API 限流**: 基于 Redis + Lua，防止接口被恶意刷调用
- 🔒 **防重复提交**: 分布式锁，防止重复下单、重复支付
- 📖 **完善的文档**: 详细的使用指南、API 测试示例、实现总结

#### 技术栈

- ☕ **Java 21**: 现代化 JDK
- 🍃 **Spring Boot 3.5.6**: 最新稳定版
- 🔒 **Spring Security 6.x**: 安全框架
- 📊 **MyBatis-Plus 3.5.x**: ORM 框架
- 💾 **Redis**: 缓存 + 分布式锁
- 🎨 **Vue 3 + TypeScript**: 前端框架

---

## 🏗️ 项目结构

```
miku/
├── miku-cmd/               # 启动模块（包含配置文件和启动类）
│   ├── src/main/
│   │   ├── java/
│   │   │   └── com/miku/cmd/
│   │   │       └── MikuApplication.java
│   │   └── resources/
│   │       ├── application.yml         # 主配置文件
│   │       ├── application-dev.yml     # 开发环境配置
│   │       ├── application-prod.yml    # 生产环境配置
│   │       └── banner.txt              # 启动图标
│   └── pom.xml
│
├── miku-core/              # 核心业务模块
│   └── src/main/java/com/miku/core/
│       ├── common/         # 通用类（注解、切面、异常、工具）
│       ├── config/         # 配置类（MyBatis-Plus、Redis、Knife4j）
│       ├── security/       # 安全相关（JWT、Security 配置）
│       └── module/         # 业务模块
│           ├── auth/       # 认证模块
│           ├── user/       # 用户管理
│           ├── role/       # 角色管理
│           ├── permission/ # 权限管理
│           ├── dept/       # 部门管理
│           └── log/        # 操作日志
│
├── miku-gen/               # 代码生成器 ⭐NEW
│   └── src/main/
│       ├── java/com/miku/gen/
│       │   ├── controller/  # 生成器控制器
│       │   ├── service/     # 生成器服务
│       │   ├── entity/      # 实体类
│       │   └── util/        # 工具类
│       └── resources/
│           └── templates/gen/  # Velocity 模板
│               ├── entity.java.vm
│               ├── mapper.java.vm
│               ├── service.java.vm
│               ├── controller.java.vm
│               ├── index.vue.vm
│               └── api.ts.vm
│
├── miku-starter-file/      # 文件存储 Starter ⭐NEW
│   └── src/main/java/com/miku/file/
│       ├── config/         # 自动配置
│       ├── service/        # 存储接口和实现
│       │   └── impl/
│       │       ├── LocalFileStorage.java
│       │       ├── OssFileStorage.java
│       │       └── MinioFileStorage.java
│       └── util/           # 工具类
│
├── miku-mod/               # 业务模块目录
│   ├── miku-demo/          # 示例模块
│   └── pom.xml
│
├── miku-pkg/               # 公共包
│   └── src/main/java/com/miku/pkg/
│       ├── Result.java     # 统一响应
│       └── PageResult.java # 分页响应
│
├── miku-ui/                # 前端项目 (Vue 3 + TypeScript)
│   ├── src/
│   │   ├── api/            # API 接口
│   │   ├── views/          # 页面
│   │   │   ├── Login.vue
│   │   │   ├── Home.vue
│   │   │   └── gen/        # 代码生成器页面 ⭐NEW
│   │   │       └── index.vue
│   │   ├── router/         # 路由
│   │   ├── store/          # Pinia 状态管理
│   │   └── utils/          # 工具类
│   └── package.json
│
├── miku-script/            # 脚本文件
│   ├── sql/
│   │   └── init.sql        # 数据库初始化脚本
│   ├── FEATURES_GUIDE.md   # 功能使用指南
│   └── IMPLEMENTATION_SUMMARY.md  # 实现总结
│
├── ARCHITECTURE_ASSESSMENT.md     # 架构评估报告 ⭐NEW
├── GENERATOR_FILE_GUIDE.md        # 代码生成器 & 文件存储使用指南 ⭐NEW
├── README.md
└── pom.xml
```

---

## 🚀 快速开始

### 环境要求

- JDK 21
- MySQL 5.7+
- Redis 5.0+
- Maven 3.6+
- Node.js 16+ (前端)

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/miku.git
cd miku
```

### 2. 初始化数据库

执行 `miku-script/sql/init.sql` 创建数据库和初始化数据。

**默认账号**：

- 超级管理员：`admin` / `123456`（拥有所有权限）
- 开发人员：`dev` / `123456`（只有查询权限）
- 测试人员：`test` / `123456`（只有查询权限）

### 3. 修改配置

编辑 `miku-cmd/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/miku_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: # 如有密码请填写

jwt:
  secret: your-secret-key-must-be-at-least-256-bits-long
  expiration: 1800000 # 30分钟
```

### 4. 启动后端

```bash
cd miku-cmd
mvn spring-boot:run
```

启动成功后访问：

- **API 接口文档**: http://localhost:8080/doc.html
- **健康检查**: http://localhost:8080/actuator/health

### 5. 启动前端

```bash
cd miku-ui
npm install
npm run dev
```

访问: http://localhost:5173

---

## 📖 核心功能

### 1. 代码生成器 🎨

**特性:**

- ✅ 可视化界面，无需编写代码
- ✅ 支持指定生成到 `miku-mod` 下的任意模块
- ✅ 一键生成 Entity/Mapper/Service/Controller/Vue/API
- ✅ 代码预览，生成前可查看
- ✅ 批量生成，支持多表

**使用方法:**

1. 访问 http://localhost:8080 → "代码生成器" 菜单
2. 选择数据库表
3. 配置生成参数（类名、模块名、作者等）
4. 点击"生成"

**示例:**

```
配置:
  表名: t_product
  模块名: product
  类名: Product

生成结果:
  miku-mod/product/
    ├── entity/Product.java
    ├── mapper/ProductMapper.java
    ├── service/ProductService.java
    ├── controller/ProductController.java
    └── resources/mapper/ProductMapper.xml

  miku-ui/src/
    ├── views/product/index.vue
    └── api/product.ts
```

📖 详细文档: [GENERATOR_FILE_GUIDE.md](miku-script/GENERATOR_FILE_GUIDE.md)

---

### 2. 文件存储 Starter 📦

**特性:**

- ✅ 策略模式，统一接口
- ✅ 支持本地/阿里云 OSS/MinIO
- ✅ 即插即用，零配置启动
- ✅ 自动管理文件路径和文件名

**引入依赖:**

```xml
<dependency>
    <groupId>com.miku</groupId>
    <artifactId>miku-starter-file</artifactId>
</dependency>
```

**配置文件:**

```yaml
# 本地存储（开发环境）
miku:
  file:
    type: local
    local:
      path: ./uploads
      prefix: /files

# 阿里云 OSS（生产环境）
miku:
  file:
    type: oss
    oss:
      access-key-id: ${OSS_ACCESS_KEY_ID}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET}
      endpoint: oss-cn-hangzhou.aliyuncs.com
      bucket-name: miku-files
```

**使用示例:**

```java
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileStorage fileStorage;

    @PostMapping("/upload")
    public Result<FileInfo> upload(@RequestParam("file") MultipartFile file) throws IOException {
        FileInfo fileInfo = fileStorage.upload(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType()
        );
        return Result.success(fileInfo);
    }
}
```

📖 详细文档: [miku-starter-file/README.md](miku-starter-file/README.md)

---

### 3. 权限管理 🔐

**RBAC 模型:**

```
用户(User) → 角色(Role) → 权限(Permission)
```

**数据权限（5 级）:**

| 级别                     | 说明                         | 应用场景           |
| ------------------------ | ---------------------------- | ------------------ |
| 1 - 全部数据权限         | 可以查看所有数据             | 超级管理员、总经理 |
| 2 - 自定义数据权限       | 可以查看指定部门的数据       | 跨部门管理者       |
| 3 - 本部门数据权限       | 只能查看本部门的数据         | 部门经理           |
| 4 - 本部门及以下数据权限 | 可以查看本部门及子部门的数据 | 大区经理           |
| 5 - 仅本人数据权限       | 只能查看自己创建的数据       | 普通员工           |

**使用示例:**

```java
@GetMapping("/list")
@PreAuthorize("hasAuthority('system:user:list')")
@DataScope(deptAlias = "d", userAlias = "u")  // 自动注入数据权限 SQL
public Result<Page<SysUser>> list() {
    return Result.success(userService.page(...));
}
```

---

### 4. 操作日志 📝

**使用示例:**

```java
@PostMapping
@Log(title = "用户管理", businessType = Log.BusinessType.INSERT)
public Result<Void> add(@RequestBody SysUser user) {
    userService.save(user);
    return Result.success();
}
```

---

### 5. API 限流 🚦

**使用示例:**

```java
@PostMapping("/login")
@RateLimiter(key = "login", time = 60, count = 10, limitType = RateLimiter.LimitType.IP)
public Result<LoginResponse> login(@RequestBody LoginRequest request) {
    // 每个 IP 每分钟最多 10 次
}
```

---

### 6. 防重复提交 🔒

**使用示例:**

```java
@PostMapping("/order")
@RepeatSubmit(interval = 5000)
public Result<Void> createOrder(@RequestBody OrderDTO order) {
    // 5 秒内不允许重复提交
}
```

---

## 📊 架构成熟度评分

| 维度           | 得分   | 说明                                |
| -------------- | ------ | ----------------------------------- |
| **模块化设计** | 85/100 | ⭐⭐⭐⭐ 结构清晰,职责明确          |
| **权限安全**   | 90/100 | ⭐⭐⭐⭐⭐ RBAC+数据权限,非常完善   |
| **基础功能**   | 75/100 | ⭐⭐⭐⭐ 已补充代码生成器和文件存储 |
| **可扩展性**   | 85/100 | ⭐⭐⭐⭐ Starter 设计,模块化        |
| **开发效率**   | 85/100 | ⭐⭐⭐⭐ 代码生成器大幅提升效率     |
| **文档完善**   | 90/100 | ⭐⭐⭐⭐⭐ 文档详细,示例丰富        |

**综合评分: 85/100** ✨

---

## 📚 文档索引

- 📖 [架构评估报告](miku-script/ARCHITECTURE_ASSESSMENT.md) - 详细的架构分析和改进建议
- 📖 [代码生成器 & 文件存储使用指南](miku-script/GENERATOR_FILE_GUIDE.md) - 核心功能使用文档
- 📖 [功能使用指南](miku-script/FEATURES_GUIDE.md) - 操作日志、限流、防重复提交等
- 📖 [实现总结](miku-script/IMPLEMENTATION_SUMMARY.md) - 技术实现详细说明
- 📖 [文件存储 Starter](miku-starter-file/README.md) - 文件存储独立文档

---

## 🎯 后续规划

### 短期（1 个月）

- [ ] Spring Boot Actuator 监控
- [ ] Redisson 分布式锁
- [ ] 定时任务 (XXL-Job)
- [ ] 导入导出 (EasyExcel)
- [ ] 数据字典管理

### 中期（3 个月）

- [ ] 配置中心 (Nacos)
- [ ] 监控体系 (Prometheus + Grafana)
- [ ] 容器化部署 (Docker + K8s)
- [ ] 链路追踪 (SkyWalking)

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

[Apache License 2.0](LICENSE)

---

## 👨‍💻 作者

**lihainuo**

- Website: https://www.lihainuo.com
- Email: contact@lihainuo.com

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！

**Miku Framework** - 让企业级开发变得优雅简单 🚀
