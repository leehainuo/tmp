# Miku Redis Starter

统一的 Redis 功能模块，整合了限流、防重复提交和 Redisson 集成功能。

## ✨ 功能特性

- ✅ **API 限流**：支持 RedisTemplate + Lua 和 Redisson 两种实现方式
- ✅ **防重复提交**：支持 RedisTemplate 和 Redisson 分布式锁两种实现方式
- ✅ **Redisson 集成**：提供分布式锁、信号量、限流器等工具类
- ✅ **灵活配置**：通过配置开关控制功能启用和实现方式
- ✅ **完全解耦**：只依赖 `miku-pkg`，不依赖 `miku-core`
- ✅ **注解与实现一体化**：注解和切面在同一个模块中

## 📦 模块说明

本模块整合了以下三个功能：

1. **限流功能**（原 `miku-starter-rate-limit`）
2. **防重复提交功能**（原 `miku-starter-repeat-submit`）
3. **Redisson 集成**（新增）

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.miku</groupId>
    <artifactId>miku-starter-redis</artifactId>
</dependency>
```

### 2. 配置 Redis

确保已配置 Redis 连接（通过 Spring Boot 自动配置）。

### 3. 配置文件（可选）

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
      # Miku Redis 功能配置
      # 限流功能配置
      rate-limit:
        enabled: true                    # 是否启用限流功能（默认：true）
        implementation: redis-template   # 实现方式：redis-template 或 redisson（默认：redis-template）
      # 防重复提交功能配置
      repeat-submit:
        enabled: true                    # 是否启用防重复提交功能（默认：true）
        implementation: redis-template   # 实现方式：redis-template 或 redisson（默认：redis-template）
      # Redisson 配置（可选）
      redisson:
        enabled: false                  # 是否启用 Redisson（默认：false）
```

## 📖 使用指南

### 限流功能

#### 基本使用

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    // 限制每个IP每分钟最多10次请求
    @PostMapping("/login")
    @RateLimiter(key = "login", time = 60, count = 10, limitType = RateLimiter.LimitType.IP)
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 业务逻辑
    }

    // 限制每秒最多5次请求
    @PostMapping("/sms")
    @RateLimiter(key = "sms", time = 1, count = 5)
    public Result<Void> sendSms(@RequestParam String phone) {
        // 业务逻辑
    }

    // 限制每个用户每分钟最多3次请求
    @PostMapping("/resetPassword")
    @RateLimiter(key = "resetPassword", time = 60, count = 3, limitType = RateLimiter.LimitType.USER)
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        // 业务逻辑
    }
}
```

#### 注解参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `key` | 限流 key（支持 SpEL 表达式） | "" |
| `time` | 时间窗口（秒） | 60 |
| `count` | 时间窗口内最大请求次数 | 100 |
| `limitType` | 限流类型 | `LimitType.DEFAULT` |

#### 限流类型

- `DEFAULT` - 全局限流（根据方法签名）
- `IP` - 根据 IP 地址限流
- `USER` - 根据登录用户限流

### 防重复提交功能

#### 基本使用

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    // 5秒内不允许重复提交
    @PostMapping
    @RepeatSubmit(interval = 5000)
    public Result<Void> createOrder(@RequestBody OrderDTO order) {
        // 业务逻辑
    }

    // 自定义提示消息
    @PostMapping("/pay")
    @RepeatSubmit(interval = 3000, message = "请勿重复支付")
    public Result<Void> payOrder(@RequestBody PayDTO pay) {
        // 业务逻辑
    }

    // 不包含请求参数，只要同一用户/IP就拦截
    @PostMapping("/submit")
    @RepeatSubmit(interval = 10000, includeParams = false)
    public Result<Void> submit(@RequestBody FormDTO form) {
        // 业务逻辑
    }
}
```

#### 注解参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `interval` | 间隔时间（毫秒） | 5000 |
| `message` | 提示消息 | "请勿重复提交" |
| `includeParams` | 是否包含请求参数 | `true` |

#### 防重复机制

1. **包含请求参数**（`includeParams = true`）：
   - 计算请求参数的 MD5 值
   - 格式：`repeat_submit:{username}:{method}:{paramsHash}`
   - 只有相同用户、相同方法、相同参数才会被拦截

2. **不包含请求参数**（`includeParams = false`）：
   - 格式：`repeat_submit:{username}:{method}`
   - 只要同一用户调用同一方法就会被拦截

### Redisson 工具类

#### 分布式锁

```java
@Autowired
private RedissonLockUtil redissonLockUtil;

public void doSomething() {
    // 尝试获取锁并执行
    redissonLockUtil.tryLock("lock:key", 1000, 5000, () -> {
        // 业务逻辑
        return result;
    });
}
```

#### 信号量

```java
@Autowired
private RedissonSemaphoreUtil redissonSemaphoreUtil;

public void doSomething() {
    // 尝试获取许可
    if (redissonSemaphoreUtil.tryAcquire("semaphore:key", 1, 1000)) {
        try {
            // 业务逻辑
        } finally {
            // 释放许可
            redissonSemaphoreUtil.release("semaphore:key", 1);
        }
    }
}
```

#### 限流器

```java
@Autowired
private RedissonRateLimiterUtil redissonRateLimiterUtil;

public void doSomething() {
    // 尝试获取许可
    if (redissonRateLimiterUtil.tryAcquire("rateLimiter:key", 
            RateType.OVERALL, 10, 1, RateIntervalUnit.SECONDS)) {
        // 业务逻辑
    }
}
```

## 🔧 实现方式切换

### 使用 Redisson 实现

1. **在应用层引入 Redisson 依赖**（在 `miku-cmd/pom.xml` 中）：

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
</dependency>
```

2. **配置实现方式**：

```yaml
spring:
  data:
    redis:
      rate-limit:
        implementation: redisson    # 使用 Redisson 限流器
      
      repeat-submit:
        implementation: redisson    # 使用 Redisson 分布式锁
```

**注意**：Redisson 会自动使用 Spring Boot 的 Redis 配置（`spring.data.redis.host`、`spring.data.redis.port` 等），无需额外配置。

### 使用 RedisTemplate 实现（默认）

```yaml
spring:
  data:
    redis:
      rate-limit:
        implementation: redis-template    # 使用 RedisTemplate + Lua
      
      repeat-submit:
        implementation: redis-template    # 使用 RedisTemplate setIfAbsent
```

## 📝 注意事项

1. **Redis 连接**：需要配置 Redis 连接（通过 Spring Boot 自动配置）
2. **用户信息**：USER 类型限流和防重复提交依赖 `miku-core` 模块获取用户信息
3. **Redisson 依赖**：
   - Redisson 在 `miku-starter-redis` 中是可选依赖（optional）
   - 如果要在应用中使用 Redisson 功能，需要在应用层（如 `miku-cmd`）显式引入 `redisson-spring-boot-starter` 依赖
   - 如果未引入 Redisson 依赖，会自动使用 RedisTemplate 实现，不会报错
4. **向后兼容**：原有的 `@RateLimiter` 和 `@RepeatSubmit` 注解用法保持不变，只需更新包名

## 🔄 迁移指南

### 从旧模块迁移

如果你之前使用的是 `miku-starter-rate-limit` 或 `miku-starter-repeat-submit`：

1. **更新依赖**：

```xml
<!-- 移除旧依赖 -->
<!--
<dependency>
    <groupId>com.miku</groupId>
    <artifactId>miku-starter-rate-limit</artifactId>
</dependency>
<dependency>
    <groupId>com.miku</groupId>
    <artifactId>miku-starter-repeat-submit</artifactId>
</dependency>
-->

<!-- 添加新依赖 -->
<dependency>
    <groupId>com.miku</groupId>
    <artifactId>miku-starter-redis</artifactId>
</dependency>
```

2. **更新导入语句**：

```java
// 旧导入
import com.miku.ratelimit.annotation.RateLimiter;
import com.miku.repeatsubmit.annotation.RepeatSubmit;

// 新导入
import com.miku.redis.annotation.RateLimiter;
import com.miku.redis.annotation.RepeatSubmit;
```

3. **注解用法保持不变**：所有注解参数和用法完全相同

## 🎯 架构设计

本模块遵循项目的"注解与实现一体化，但保持完全解耦"原则：

- ✅ **注解与实现一体化**：注解和切面在同一个模块中
- ✅ **完全解耦**：只依赖 `miku-pkg`，不依赖 `miku-core`
- ✅ **按需引入**：通过配置开关控制功能启用
- ✅ **灵活实现**：支持多种实现方式，可配置切换

## 📚 相关文档

- [架构设计文档](../../ARCHITECTURE_ELEGANT.md)
- [Starter 使用指南](../../STARTER_USAGE_GUIDE.md)

