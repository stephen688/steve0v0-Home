# steve0v0 小屋 · 后端第一阶段技术实现计划

> 版本：v2.0 ｜ 日期：2026-08-11  
> 技术栈：Spring Boot 3.5.16 + MyBatis-Plus + MySQL 8.0 + Maven  
> 第一阶段范围：项目搭建 + 暗号登录 JWT 鉴权 + 博客管理 + 动态发布管理

---

## 1. 技术选型确认

| 维度 | 选型 | 版本 | 说明 |
|---|---|---|---|
| 语言 | Java | 21 LTS | 已安装 21.0.8 |
| 框架 | Spring Boot | **3.5.16**（锁定） | 3.5.x 最后一个 OSS 版本 |
| 安全框架 | Spring Security | 随 Boot 版本 | SecurityFilterChain 统一处理认证、CORS、401/403 |
| ORM | MyBatis-Plus | **3.5.12**（锁定） | CRUD 开箱即用 |
| 数据库 | MySQL | 8.0 | 本地安装，Navicat 17 管理 |
| 构建 | Maven | 3.9.x | 已安装 3.9.11 |
| 鉴权 | JWT (jjwt) | **0.12.6**（锁定） | HS256，密钥 ≥ 32 字节 |
| 密码哈希 | BCrypt | 随 Spring Security | 暗号不明文存储 |
| API 文档 | SpringDoc OpenAPI | **2.8.17**（锁定） | 兼容 Spring Boot 3.5.x（2.6.x 仅兼容 3.3.x） |
| 工具库 | Lombok / Hutool | **1.18.36 / 5.8.36**（锁定） | 减少样板代码 |
| 数据库管理 | 手动 SQL 脚本 | - | 提供建表 SQL，手动执行 |

> **版本锁定原则**：所有依赖在 `pom.xml` 中固定版本号，不使用 `LATEST` 或范围版本。

---

## 2. 阶段拆分与 PRD 对齐

PRD 的"本期"指完整小程序全部功能，技术方案分阶段实现。以下是阶段拆分对应关系：

| 阶段 | 包含功能 | 对应 PRD 模块 |
|---|---|---|
| **第一阶段（本文档）** | 项目骨架 + 暗号登录 + 博客管理 + 动态发布管理 + 文件上传 | 2.2 博客页、2.3 动态页、3.1 暗号登录、3.2 博客管理、3.3 动态管理 |
| 第二阶段 | 首页日历 + 学习记录 + 手动课程 + 番茄钟 + 学习数据看板 + 个人状态 | 2.1 首页、3.4-3.6 |
| 第三阶段 | 关于页个人资料 + GitHub 项目 | 2.4 关于页、3.7 |
| 第四阶段 | 课程表 AI 识别导入 + Markdown 文件上传解析 | 2.1.3、3.2（.md 上传） |

### 第一阶段与 PRD 的明确偏差

| PRD 项 | 第一阶段处理 | 原因 |
|---|---|---|
| 预计阅读时长 | 后端按字数实时计算（中文按字符数 / 300 字/分钟），不落库 | 无需额外字段，动态计算 |
| 动态音乐/视频卡片 | 表结构保留 `media_type` 和 `media_url` 字段，但第一阶段**仅开放 text 和 image** | 音乐/视频验证逻辑较复杂，后续阶段开放 |
| Markdown 文件上传 | 第四阶段实现 | 优先保证核心 CRUD 闭环 |
| 暗号支持修改 | 第一阶段实现凭据表 + tokenVersion 失效机制 | PRD 已确认"支持修改" |

---

## 3. 第一阶段范围

### 3.1 包含
- 项目骨架搭建（分层架构 + 统一响应 + 全局异常处理）
- 暗号登录 + JWT 签发与 Spring Security 鉴权
- 凭据表：暗号 BCrypt 哈希存储，支持修改暗号，tokenVersion 失效机制
- 登录接口限流（同 IP 5 分钟内最多 5 次失败）
- 博客管理：文章创建、发布、删除（技术博客 / 生活文章两个分类），含草稿管理
- 动态管理：动态创建、发布、删除（纯文字 / 多图），含事务保证
- 文件上传接口（图片上传，文件签名校验，返回 URL）
- 数据库建表 SQL 脚本

### 3.2 不包含（后续阶段）
- 首页日历、学习记录、课程表、番茄钟、学习数据看板、个人状态
- 关于页个人资料 / GitHub 项目管理；技术栈和联系方式由前端静态维护
- 课程表 AI 识别
- Markdown 文件上传解析
- 音乐/视频动态发布

---

## 4. 项目结构

```
steve-home/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/steve0v0/home/
│       │   ├── SteveHomeApplication.java
│       │   │
│       │   ├── config/
│       │   │   ├── SecurityConfig.java              # SecurityFilterChain、CORS、401/403
│       │   │   ├── MybatisPlusConfig.java            # 分页插件
│       │   │   ├── SwaggerConfig.java                # OpenAPI 文档
│       │   │   └── WebMvcConfig.java                 # 静态资源映射
│       │   │
│       │   ├── common/
│       │   │   ├── result/
│       │   │   │   ├── Result.java
│       │   │   │   └── ResultCode.java
│       │   │   ├── exception/
│       │   │   │   ├── BusinessException.java
│       │   │   │   └── GlobalExceptionHandler.java
│       │   │   ├── constant/
│       │   │   │   └── Constants.java
│       │   │   └── pagination/
│       │   │       ├── PageRequest.java              # 统一分页请求（page从1开始，size≤50）
│       │   │       └── PageResult.java               # 统一分页响应（page/size/total/hasMore）
│       │   │
│       │   ├── security/                             # 安全层
│       │   │   ├── JwtUtil.java                      # JWT 生成/校验（HS256，密钥≥32字节）
│       │   │   ├── JwtAuthenticationFilter.java      # JWT 认证过滤器
│       │   │   ├── SecurityUser.java                 # 认证主体
│       │   │   └── RateLimitService.java             # 登录限流（内存计数）
│       │   │
│       │   ├── controller/
│       │   │   ├── AdminAuthController.java          # 暗号登录 + 修改暗号
│       │   │   ├── ArticleController.java            # 公开-文章列表/详情
│       │   │   ├── AdminArticleController.java       # 管理-文章创建/发布/删除 + 列表 + 详情
│       │   │   ├── MomentController.java             # 公开-动态列表
│       │   │   ├── AdminMomentController.java        # 管理-动态创建/发布/删除 + 列表 + 详情
│       │   │   └── AdminUploadController.java        # 管理-文件上传
│       │   │
│       │   ├── service/
│       │   │   ├── AdminAuthService.java
│       │   │   ├── ArticleService.java
│       │   │   ├── MomentService.java
│       │   │   └── UploadService.java
│       │   │
│       │   ├── mapper/
│       │   │   ├── ArticleMapper.java
│       │   │   ├── MomentMapper.java
│       │   │   ├── MomentImageMapper.java
│       │   │   └── CredentialMapper.java
│       │   │
│       │   ├── entity/
│       │   │   ├── Article.java
│       │   │   ├── Moment.java
│       │   │   ├── MomentImage.java
│       │   │   └── Credential.java
│       │   │
│       │   ├── dto/
│       │   │   ├── ArticleCreateDTO.java             # 新建文章（必填字段校验）
│       │   │   ├── ArticleQueryDTO.java              # 管理端列表查询（含草稿）
│       │   │   ├── MomentCreateDTO.java              # 新建动态（必填字段校验）
│       │   │   └── ChangeSecretDTO.java              # 修改暗号
│       │   │
│       │   └── vo/
│       │       ├── ArticleListVO.java
│       │       ├── ArticleDetailVO.java
│       │       ├── MomentListVO.java
│       │       └── LoginVO.java
│       │
│       └── resources/
│           ├── application.yml                       # 主配置（无敏感信息）
│           ├── application-dev.yml                   # 开发环境（含数据源、SQL日志、Swagger）
│           ├── application-prod.yml                  # 生产环境模板（不含密码）
│           ├── mapper/
│           │   ├── ArticleMapper.xml
│           │   └── MomentMapper.xml
│           └── sql/
│               └── init.sql                          # 建表脚本
│
└── upload/                                           # 图片上传目录（本地存储）
```

---

## 5. 实现步骤

### Step 1：数据库建表

**目标**：初始化第一阶段所需的数据库表，确保应用启动前数据库就绪。

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS steve_home DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE steve_home;

-- 凭据表（暗号哈希存储，支持修改）
CREATE TABLE credential (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    secret_hash     VARCHAR(100) NOT NULL COMMENT '暗号 BCrypt 哈希',
    token_version   INT          NOT NULL DEFAULT 0 COMMENT 'Token 版本号，修改暗号时+1使旧Token失效',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员凭据表';

-- 注意：不在 init.sql 中插入任何凭据记录。
-- 暗号通过应用首次启动时从环境变量 ADMIN_INITIAL_SECRET 读取并 BCrypt 哈希后写入。
-- 详见 Step 3.2 暗号初始化机制。

-- 文章表
CREATE TABLE article (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    summary         VARCHAR(500)          DEFAULT NULL COMMENT '摘要',
    content         LONGTEXT              DEFAULT NULL COMMENT '正文（Markdown）',
    cover_image     VARCHAR(500)          DEFAULT NULL COMMENT '封面图 URL',
    category        VARCHAR(20)  NOT NULL DEFAULT 'tech' COMMENT '分类：tech-技术博客 / life-生活文章',
    tags            VARCHAR(200)          DEFAULT NULL COMMENT '标签，逗号分隔',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布',
    view_count      INT          NOT NULL DEFAULT 0 COMMENT '阅读数',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at    DATETIME              DEFAULT NULL COMMENT '发布时间（首次发布时写入，不随编辑更新）',
    PRIMARY KEY (id),
    INDEX idx_category_status (category, status),
    INDEX idx_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 动态表
CREATE TABLE moment (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    content     TEXT                  DEFAULT NULL COMMENT '动态内容',
    media_type  VARCHAR(20)  NOT NULL DEFAULT 'text' COMMENT '媒体类型：text-纯文字 / image-多图 / music-音乐 / video-视频（第一阶段仅开放text和image）',
    media_url   VARCHAR(500)          DEFAULT NULL COMMENT '音乐/视频外链 URL（第一阶段不使用）',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- 动态图片表
CREATE TABLE moment_image (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    moment_id   BIGINT       NOT NULL COMMENT '动态 ID',
    url         VARCHAR(500) NOT NULL COMMENT '图片 URL',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    INDEX idx_moment_id_sort (moment_id, sort),
    CONSTRAINT fk_moment_image FOREIGN KEY (moment_id) REFERENCES moment(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态图片表';
```

**设计说明**：
- 使用**物理删除**，不配置 MyBatis-Plus 逻辑删除。
- `article` 新增 `published_at`：首次发布时写入，编辑不更新，公开列表按 `published_at DESC, id DESC` 排序。
- `moment_image` 添加外键 `ON DELETE CASCADE`，删除动态时自动清理子记录。
- `credential` 表存储暗号 BCrypt 哈希，`token_version` 用于修改暗号后使旧 Token 失效。

- [ ] 执行建表 SQL
- [ ] 在 Navicat 中验证表结构

---

### Step 2：项目初始化与骨架搭建

**目标**：项目能启动并连接数据库，基础架构就绪。

- [ ] 创建 Maven 项目，配置 `pom.xml` 依赖（版本全部锁定）
  - spring-boot-starter-web **3.5.16**
  - spring-boot-starter-security **3.5.16**
  - mybatis-plus-spring-boot3-starter **3.5.12**
  - mysql-connector-j
  - lombok **1.18.36**
  - hutool-all **5.8.36**
  - jjwt-api / jjwt-impl / jjwt-jackson **0.12.6**
  - springdoc-openapi-starter-webmvc-ui **2.8.17**
- [ ] 编写 `application.yml`（主配置，无敏感信息）
- [ ] 编写 `application-dev.yml`（开发环境：数据源、SQL 日志、Swagger 开启）
- [ ] **不硬编码 `spring.profiles.active`**，通过启动参数 `--spring.profiles.active=dev` 指定
- [ ] 编写启动类
- [ ] 编写统一响应 `Result<T>` + `ResultCode` 枚举
- [ ] 编写全局异常处理器 `GlobalExceptionHandler`
  - 业务异常 `BusinessException`
  - 业务异常的 HTTP 状态码与响应体 `code` 保持一致
  - 参数校验异常 `MethodArgumentNotValidException`
  - `AccessDeniedException`（Spring Security 403）
  - `AuthenticationException`（Spring Security 401）
  - 未匹配路由和静态资源统一返回 404 JSON
  - 其他未捕获异常兜底（不泄露堆栈信息）
- [ ] 编写分页契约：
  - `PageRequest`：page 从 **1** 开始，size 默认 10，最大 **50**
  - `PageResult<T>`：包含 `page`、`size`、`total`、`hasMore`、`list`
- [ ] 验证：项目启动成功（数据库已就绪），访问 Swagger 文档页面

---

### Step 3：安全层 - 暗号登录 + JWT 鉴权

**目标**：管理员通过暗号获取 JWT，管理接口受 Spring Security 保护。

#### 3.1 JWT 密钥安全

- [ ] JWT 密钥从环境变量 `JWT_SECRET` 读取，**不提供默认值**。
- [ ] 启动时校验密钥长度：HS256 要求密钥 ≥ 32 字节（256 bit），不满足则**启动失败**。
- [ ] 密钥建议使用安全随机生成的 Base64 值：
  ```bash
  openssl rand -base64 48
  ```

#### 3.2 暗号存储与首次启动初始化

- [ ] 暗号使用 **BCrypt** 哈希存储在 `credential` 表中，不明文持久化。
- [ ] **init.sql 不插入任何凭据记录**，表结构仅建空表。
- [ ] **首次启动初始化机制**：
  - 应用启动时检查 `credential` 表是否有记录。
  - 若无记录（首次启动）：
    - 从环境变量 `ADMIN_INITIAL_SECRET` 读取初始暗号。
    - 缺失 `ADMIN_INITIAL_SECRET` 时，**启动失败**并明确报错。
    - BCrypt 加密后插入 `credential` 表。
  - 若已有记录（非首次启动）：
    - 忽略 `ADMIN_INITIAL_SECRET`，不覆盖现有暗号。
  - 生产环境（`prod` profile）：无凭据且无 `ADMIN_INITIAL_SECRET` 时启动失败。
- [ ] 首次登录后建议立即通过管理接口修改暗号。

#### 3.3 登录限流

- [ ] `RateLimitService`：同 IP 5 分钟内最多 5 次失败尝试。
- [ ] 超过限制后返回 429，提示"尝试过于频繁，请 5 分钟后再试"。
- [ ] **不记录暗号**到任何日志。审计日志仅记录：IP、时间、成功/失败。

#### 3.4 JWT 签发

- [ ] `JwtUtil`：
  - `generateToken(credentialId, tokenVersion)`：生成 JWT，Payload 含 `sub`（credentialId）和 `tv`（tokenVersion），过期时间 7 天。
  - `validateToken(token)`：校验签名 + 过期时间。
  - `parseToken(token)`：解析 Claims。
- [ ] `JwtAuthenticationFilter`（OncePerRequestFilter）：
  - 从 Header 提取 `Authorization: Bearer <token>`。
  - 校验 Token 有效性 + 数据库中 `token_version` 是否匹配（不匹配则旧 Token 失效）。
  - 设置 `SecurityContext` 认证主体。
  - 无效 Token 不抛异常，由 SecurityFilterChain 统一返回 401。

#### 3.5 SecurityFilterChain

- [ ] `SecurityConfig`：
  - 放行：`/api/articles/**`、`/api/moments/**`、`/api/admin/auth`、`/upload/**`、Swagger 资源。
  - 拦截：`/api/admin/**`（除 auth 外）要求认证。
  - CORS：在 SecurityFilterChain 中统一配置。
  - 未认证返回 401 JSON，无权限返回 403 JSON（不重定向）。
  - 禁用 CSRF（无状态 API）。
  - Session 策略：`STATELESS`。

#### 3.6 暗号修改

- [ ] `POST /api/admin/auth/change-secret`（需 JWT 鉴权）
  - 接收：旧暗号、新暗号。
  - 校验旧暗号 BCrypt 匹配。
  - 更新 `credential` 表：`secret_hash` = 新暗号 BCrypt 哈希，`token_version` = `token_version + 1`。
  - 修改后返回新 JWT（含新 tokenVersion），旧 JWT 立即失效。

- [ ] 验证：
  - 错误暗号被拒，正确暗号返回 JWT。
  - 无 Token / 无效 Token 访问管理接口返回 401。
  - 修改暗号后旧 Token 失效。
  - 连续 5 次错误后被限流。

---

### Step 4：文章模块（博客）

**目标**：完成文章的新建、草稿管理、发布、删除、列表、详情全链路，草稿不泄露；已发布文章暂不提供修改接口。

- [ ] 实体 `Article`：对应 `article` 表，**无 `deleted` 字段**（物理删除）。
- [ ] Mapper `ArticleMapper`：继承 BaseMapper。
- [ ] DTO：
  - `ArticleCreateDTO`：新建文章，`title` 必填，`summary`/`content`/`coverImage`/`tags`/`status` 可选，`category` 默认 `tech`。
  - `ArticleQueryDTO`：分类、状态（可选，管理端用）、页码、每页条数。
- [ ] VO：
  - `ArticleListVO`：id、标题、摘要、封面、分类、标签、发布时间、预计阅读时长、阅读数。
  - `ArticleDetailVO`：完整字段 + 预计阅读时长。
- [ ] Service `ArticleService`：
  - `getPublicArticleList(category, page, size)`：分页查询 **`status = 1`** 的文章，按 `published_at DESC, id DESC` 排序。
  - `getPublicArticleById(id)`：查询 **`WHERE id = ? AND status = 1`**，草稿/已删除/不存在统一返回 404。阅读数**原子更新**：`UPDATE article SET view_count = view_count + 1 WHERE id = ?`。
  - `getAdminArticleList(queryDTO)`：管理端列表，支持查询草稿和已发布。
  - `getAdminArticleById(id)`：管理端详情，可查看草稿。
  - `createArticle(dto)`：新建文章。状态从草稿变为已发布时，写入 `published_at = NOW()`。
  - `deleteArticle(id)`：物理删除。
- [ ] 预计阅读时长：后端按字数实时计算，中文按字符数 / 300 字每分钟，不落库。
- [ ] Controller：
  - 公开：`GET /api/articles`（分页 + 分类筛选，仅已发布）、`GET /api/articles/{id}`（详情，仅已发布）
  - 管理：`GET /api/admin/articles`（列表，含草稿）、`GET /api/admin/articles/{id}`（详情，含草稿）、`POST /api/admin/articles`（新建、发布或保存草稿，接收 `ArticleCreateDTO`）、`DELETE /api/admin/articles/{id}`
- [ ] 验证：
  - 发布一篇技术博客和一篇生活文章。
  - 公开接口仅返回已发布文章，草稿不可见。
  - 公开详情查询草稿 ID 返回 404。
  - 管理端列表和详情可查看草稿。
  - 阅读数原子自增。
  - `published_at` 仅在首次发布时写入。
  - 草稿可在管理端列表和详情中查看；已发布文章暂不提供修改接口，删除正常工作。

---

### Step 5：动态模块

**目标**：完成动态的创建、发布、删除、列表、详情全链路，多表操作有事务保证；暂不提供动态修改接口。

- [ ] 实体 `Moment`、`MomentImage`。
- [ ] Mapper `MomentMapper`、`MomentImageMapper`。
- [ ] DTO：
  - `MomentCreateDTO`：新建动态，`content` 必填，`mediaType` 默认 `text`（仅允许 `text` 和 `image`），`mediaUrl` 可选（第一阶段不使用），`images` 可选（`List<String>`，图片 URL 列表）。
- [ ] VO：`MomentListVO`（含文字和图片列表，动态列表直接展示）。
- [ ] Service `MomentService`（**所有写操作加 `@Transactional`**）：
  - `getMomentList(page, size)`：分页倒序查询，**一次批量查询图片**避免 N+1：
    1. 分页查 moment 列表。
    2. 收集所有 moment_id。
    3. `SELECT * FROM moment_image WHERE moment_id IN (...) ORDER BY sort`。
    4. 在 Java 中按 moment_id 分组组装到 VO。
  - `getAdminMomentList(page, size)`：管理端列表（与公开一致，预留后续筛选）。
  - `createMoment(dto)`：`@Transactional` - 保存 moment + 批量保存图片（若 `images` 非空）。
  - `deleteMoment(id)`：`@Transactional` - 删除 moment，外键 `ON DELETE CASCADE` 自动清理图片。
- [ ] Controller：
  - 公开：`GET /api/moments`（分页倒序）
  - 管理：`GET /api/admin/moments`（列表，直接包含文字和图片）、`POST /api/admin/moments`（新建/发布，接收 `MomentCreateDTO`）、`DELETE /api/admin/moments/{id}`
- [ ] 验证：
  - 发布纯文字动态和多图动态。
  - 公开接口按时间倒序展示。
  - 管理端列表直接展示文字和图片。
  - 新建纯文字和多图动态正常。
  - 暂不提供动态修改接口。
  - 删除动态后关联图片自动清除。
  - 事务回滚测试：模拟图片保存失败，moment 不残留。

---

### Step 6：文件上传

**目标**：管理员上传图片，返回可访问 URL，防止恶意文件。

- [ ] Service `UploadService`：
  - `uploadImage(file)`：
    1. **大小校验**：≤ 5MB（与框架 `max-file-size` 统一为 5MB）。
    2. **文件签名校验**：读取文件头 magic bytes，校验是否为 jpg（`FF D8 FF`）/ png（`89 50 4E 47`）/ gif（`47 49 46 38`），不只看扩展名和 MIME。
    3. **尝试解码**：使用 `ImageIO.read()` 尝试解码，失败则拒绝（防止伪装文件）。
    4. **UUID 文件名**：使用 `UUID.randomUUID()` 生成文件名，阻止路径穿越。
    5. 保存到本地 `upload/yyyy/MM/` 目录。
    6. 返回访问 URL（如 `/upload/2026/08/uuid.png`）。
  - **第一阶段仅支持 jpg / png / gif**：Java 21 标准 ImageIO 不包含 WebP 编解码器，引入 WebP 需要第三方插件（如 webp-imageio），后续阶段按需引入。
- [ ] Controller `AdminUploadController`：
  - `POST /api/admin/upload`：接收 MultipartFile，返回 URL。
- [ ] 在 `WebMvcConfig` 中配置静态资源映射：`/upload/**` -> 本地文件目录。
- [ ] 验证：
  - 上传正常图片返回 URL，URL 可公开访问。
  - 伪装扩展名的非图片文件被拒绝（文件签名校验 + 解码校验）。
  - 超过 5MB 的文件被拒绝。
  - 文件名为 UUID，无路径穿越风险。

---

## 6. 配置文件规划

### application.yml（主配置，无敏感信息）

```yaml
server:
  port: 8080

spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 20MB

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      # 不配置逻辑删除，使用物理删除

steve:
  jwt:
    secret: ${JWT_SECRET}  # 必须通过环境变量提供，缺失则启动失败
    expire: 604800         # 7天（秒）
  upload:
    path: ./upload
  rate-limit:
    max-attempts: 5
    window-minutes: 5
```

### application-dev.yml（开发环境）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/steve_home?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

### application-prod.yml（生产环境模板）

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: WARN
```

> **启动方式**：`java -jar steve-home.jar --spring.profiles.active=dev`  
> 不在配置文件中硬编码 `spring.profiles.active`。

### 环境变量要求

| 变量名 | 说明 | 必须 |
|---|---|---|
| `JWT_SECRET` | JWT 签名密钥，≥ 32 字节，Base64 编码 | 是（缺失启动失败） |
| `DB_USERNAME` | 数据库用户名 | 是 |
| `DB_PASSWORD` | 数据库密码 | 是 |
| `ADMIN_INITIAL_SECRET` | 初始暗号，仅首次启动使用，凭据已存在时忽略 | 是（首次启动时缺失则失败） |

---

## 7. 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| code | 说明 |
|---|---|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 / Token 无效 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁（限流） |
| 500 | 服务器内部错误 |

### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 42,
    "hasMore": true,
    "list": []
  }
}
```

- `page` 从 **1** 开始。
- `size` 默认 10，最大 **50**。
- 排序：文章按 `published_at DESC, id DESC`，动态按 `created_at DESC, id DESC`。

---

## 8. API 接口清单（第一阶段）

### 8.1 公开接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/articles` | 文章列表（分页 + 分类筛选，仅已发布） |
| GET | `/api/articles/{id}` | 文章详情（仅已发布，阅读数原子+1） |
| GET | `/api/moments` | 动态列表（分页倒序） |

### 8.2 管理接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/admin/auth` | 无 | 暗号校验，返回 JWT |
| POST | `/api/admin/auth/change-secret` | JWT | 修改暗号（旧Token失效） |
| GET | `/api/admin/articles` | JWT | 管理端文章列表（含草稿） |
| GET | `/api/admin/articles/{id}` | JWT | 管理端文章详情（含草稿） |
| POST | `/api/admin/articles` | JWT | 发布/保存草稿文章 |
| DELETE | `/api/admin/articles/{id}` | JWT | 删除文章 |
| GET | `/api/admin/moments` | JWT | 管理端动态列表 |
| POST | `/api/admin/moments` | JWT | 发布动态 |
| DELETE | `/api/admin/moments/{id}` | JWT | 删除动态 |
| POST | `/api/admin/upload` | JWT | 图片上传 |


---

## 9. 验收标准

### 9.1 手工验收

| 验收项 | 验证方式 |
|---|---|
| 项目启动 | 数据库先建表，`mvn spring-boot:run -Dspring-boot.run.profiles=dev` 启动成功，Swagger 可访问 |
| 暗号登录 | 错误暗号被拒，正确暗号返回 JWT |
| JWT 鉴权 | 无 Token 访问管理接口返回 401，有 Token 正常 |
| 暗号修改 | 修改暗号后旧 Token 失效，新 Token 正常 |
| 登录限流 | 连续 5 次错误暗号后返回 429 |
| 文章发布 | 后台发布技术博客和生活文章 |
| 草稿隔离 | 草稿不出现在公开列表，公开详情查草稿 ID 返回 404 |
| 管理端列表 | 管理端可查看草稿和已发布 |
| 文章查看 | 公开接口分页查询、分类筛选、详情阅读数原子自增 |
| published_at | 首次发布写入，已发布文章暂不提供修改，公开列表按此排序 |
| 文章草稿与删除 | POST 保存草稿，管理端可查看草稿，DELETE 正常；已发布文章暂不提供修改接口 |
| 动态发布 | 发布纯文字和多图动态 |
| 动态查看 | 公开接口按时间倒序展示 |
| 动态发布删除 | 发布纯文字和多图正常，删除后关联图片自动清除；暂不提供修改接口 |
| 图片上传 | 正常图片返回 URL，伪装文件被拒，超 5MB 被拒 |

### 9.2 集成测试验收

| 测试项 | 验证内容 |
|---|---|
| 认证测试 | 正确/错误暗号、Token 过期、Token 篡改、tokenVersion 不匹配 |
| 草稿隔离测试 | 公开接口无法获取草稿（列表 + 详情） |
| 事务回滚测试 | 动态保存时图片插入失败，moment 回滚不残留 |
| 分页测试 | page=0/负数处理、size>50 截断、hasMore 计算正确 |
| 非法上传测试 | 非图片文件、伪装扩展名、超大文件、空文件 |
| 限流测试 | 5 次失败后返回 429，5 分钟后恢复 |
| 暗号初始化测试 | 首次启动无凭据时从 ADMIN_INITIAL_SECRET 初始化；非首次启动忽略该变量；缺失时启动失败 |
