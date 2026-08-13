# steve0v0 小屋 · 后端第三阶段技术实现计划

> 版本：v1.0 ｜ 日期：2026-08-11 ｜ 状态：代码已实现，待真实接口验收  
> 技术栈：Spring Boot 3.5.16 + MyBatis-Plus 3.5.12 + MySQL 8.0 + Maven  
> 第三阶段范围：关于页个人资料和 GitHub 项目管理

---

## 1. 阶段定位

第三阶段只实现关于页中需要后端动态维护的内容：

- 个人姓名
- 个人头像外部链接
- GitHub 项目列表及管理

技术栈和联系方式由前端静态维护，不新增后端表和接口。课程手动录入已在第二阶段完成，课程表 AI 识别和 Markdown 文件解析安排到第四阶段，本阶段不重复设计。

本文件是第三阶段的后端实现依据，和当前调整后的 [PRD.md](C:/Users/dwc12/Desktop/steve0v0的小屋/PRD.md) 保持一致。

## 2. 技术和兼容性约定

- 沿用 Java 21、Spring Boot 3.5.16、MyBatis-Plus 3.5.12、MySQL 8.0。
- 沿用 `Result<T>` 统一响应格式、Spring Security + JWT 鉴权和现有异常处理机制。
- 公开读取接口无需登录，管理端接口统一要求有效 JWT。
- 第三阶段数据库脚本为 `src/main/resources/sql/phase3.sql`，执行顺序为 `init.sql → phase2.sql → phase3.sql`。
- 不修改第二阶段已有接口、表结构和统计逻辑。
- 本阶段不插入示例姓名、头像或项目数据，空数据库应返回空资料和空项目列表。

## 3. 功能范围

### 3.1 包含功能

- 公开查询个人姓名和头像。
- 管理端查询、覆盖和清空个人姓名和头像。
- 公开查询 GitHub 项目列表。
- 管理端对 GitHub 项目执行增删改查。
- 项目按自定义排序值展示。
- 项目技术标签以 JSON 数组形式传输和存储。
- 统一 JWT、参数校验、404 和 400 异常响应。

### 3.2 不包含功能

- 技术栈的后端动态管理。
- 联系方式的后端表、接口、跳转、复制和二维码处理。
- 项目封面、项目分类、显示/隐藏状态和多用户数据隔离。
- 头像文件上传、图片裁剪和图片本地存储。
- 课程表 AI 识别、Markdown 文件解析和前端页面实现。

## 4. 数据库设计

### 4.1 单例个人资料表

表名：`about_profile`。系统固定维护 `id = 1` 的一条记录。

```sql
CREATE TABLE IF NOT EXISTS about_profile (
    id         BIGINT       NOT NULL COMMENT '固定为1',
    name       VARCHAR(100) NOT NULL DEFAULT '' COMMENT '个人姓名',
    avatar_url VARCHAR(500) NOT NULL DEFAULT '' COMMENT '外部HTTPS头像地址',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关于页单例个人资料';
```

资料没有记录或字段为空时，公开接口返回空字符串。头像不保存文件，只保存外部 HTTPS URL。

### 4.2 GitHub 项目表

表名：`about_project`。所有保存成功的项目默认公开。

```sql
CREATE TABLE IF NOT EXISTS about_project (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100) NOT NULL COMMENT '项目名称',
    description VARCHAR(500)          DEFAULT NULL COMMENT '项目简介',
    github_url  VARCHAR(500) NOT NULL COMMENT 'GitHub仓库HTTPS地址',
    tech_tags   JSON         NOT NULL COMMENT '技术标签JSON数组',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '展示排序，数值越小越靠前',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_about_project_sort (sort, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关于页GitHub项目';
```

项目数量较少，公开列表和管理端列表均不分页，按 `sort ASC, id DESC` 排序。`tech_tags` 缺省时使用空数组，不插入 NULL。

## 5. API 清单

### 5.1 公开接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/about/profile` | 无 | 查询姓名和头像 |
| GET | `/api/about/projects` | 无 | 查询全部公开 GitHub 项目 |

### 5.2 个人资料管理接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/admin/about/profile` | JWT | 查询管理端当前资料 |
| PUT | `/api/admin/about/profile` | JWT | 覆盖或清空单例资料 |

资料不提供 DELETE。PUT 请求必须包含 `name` 和 `avatarUrl` 两个字段，空字符串表示清空对应字段；服务端使用固定 ID 1 执行原子 upsert。

请求示例：

```json
{
  "name": "steve0v0",
  "avatarUrl": "https://example.com/avatar.png"
}
```

### 5.3 GitHub 项目管理接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/admin/about/projects` | JWT | 查询项目列表 |
| GET | `/api/admin/about/projects/{id}` | JWT | 查询项目详情 |
| POST | `/api/admin/about/projects` | JWT | 新增项目 |
| PUT | `/api/admin/about/projects/{id}` | JWT | 全量修改项目 |
| DELETE | `/api/admin/about/projects/{id}` | JWT | 物理删除项目 |

新增和修改请求示例：

```json
{
  "name": "steve-home",
  "description": "个人小程序后端项目",
  "githubUrl": "https://github.com/example/steve-home",
  "techTags": ["Java", "Spring Boot", "MySQL"],
  "sort": 0
}
```

## 6. 请求校验和业务规则

### 6.1 个人资料

- `name` 必须存在，最大长度 100；允许使用空字符串清空。
- `avatarUrl` 必须存在，最大长度 500；允许使用空字符串清空。
- 非空头像地址必须使用 HTTPS，且必须是合法绝对 URL。
- PUT 是完整覆盖语义，前端必须同时提交 `name` 和 `avatarUrl`。
- 没有资料记录时 GET 返回 `name = ""`、`avatarUrl = ""`。

### 6.2 GitHub 项目

- `name` 必填，最大长度 100。
- `description` 可选，最大长度 500。
- `githubUrl` 必填，必须是 `https://github.com/{owner}/{repository}` 形式；不接受 HTTP、其他域名、脚本协议或相对路径。
- `techTags` 缺省时按空数组处理，最多 10 个标签，每个标签非空且最大长度 50。
- `sort` 缺省为 0，必须大于或等于 0。
- 修改和删除目标不存在时返回 404。
- 所有参数错误返回 400，所有成功和错误响应继续使用 `Result<T>`。

## 7. JSON 标签映射

`AboutProject.techTags` 使用 `List<String>`，实体配置以下 MyBatis-Plus 映射约定：

- `@TableName(value = "about_project", autoResultMap = true)`。
- `techTags` 使用 `@TableField(typeHandler = JacksonTypeHandler.class)`。
- DTO 和 VO 对外均使用 JSON 数组，不暴露数据库原始 JSON 字符串。
- 写入前保证标签列表非空、无 null 元素，缺省值统一为 `[]`。

## 8. 推荐项目结构

```text
src/main/java/com/steve0v0/home/
├── controller/
│   ├── AboutProfileController.java
│   ├── AboutProjectController.java
│   ├── AdminAboutProfileController.java
│   └── AdminAboutProjectController.java
├── service/
│   ├── AboutProfileService.java
│   └── AboutProjectService.java
├── mapper/
│   ├── AboutProfileMapper.java
│   └── AboutProjectMapper.java
├── entity/
│   ├── AboutProfile.java
│   └── AboutProject.java
├── dto/
│   ├── AboutProfileUpdateDTO.java
│   ├── AboutProjectCreateDTO.java
│   └── AboutProjectUpdateDTO.java
└── vo/
    ├── AboutProfileVO.java
    └── AboutProjectVO.java
```

资料 upsert 应使用数据库原子 upsert，避免并发首次写入时出现固定主键冲突。项目删除继续沿用当前物理删除约定。

## 9. 权限和兼容性

`SecurityConfig` 应明确配置：

```text
GET /api/about/profile          公开
GET /api/about/projects         公开
/api/admin/about/**             JWT
```

既有 `/api/admin/**` 规则继续保留，第三阶段新增管理接口不得被 `anyRequest().permitAll()` 放行。第二阶段首页、学习记录、课程、番茄钟、统计和个人状态接口不做修改。

## 10. 测试和验收标准

### 10.1 资料接口

- 没有 `about_profile` 记录时公开查询返回两个空字符串。
- JWT 管理端 PUT 可以首次创建、修改和清空姓名/头像。
- 非 HTTPS 或非法头像地址返回 400。
- 无 JWT 访问管理端资料接口返回 401。

### 10.2 项目接口

- 项目新增、列表、详情、修改、物理删除全部成功。
- 项目列表按 `sort ASC, id DESC` 返回。
- 非 GitHub HTTPS 地址、空名称、超长字段和非法标签数组返回 400。
- 删除后的项目不再出现在公开列表中。
- `techTags` JSON 数组完成数据库写入和读取往返验证。
- 无 JWT 无法访问任何项目管理接口，公开列表无需 JWT。

### 10.3 回归验证

- 执行 `mvn clean test`。
- 保证第一、二阶段已有接口和鉴权规则不受影响。
- 检查 `phase3.sql` 可以在 `init.sql` 和 `phase2.sql` 执行后正常执行。
- 代码和数据库脚本已实现，未插入示例姓名、头像或项目数据；真实 HTTP 接口冒烟需在加载最新代码的应用实例上执行。

## 11. 阶段边界

- 本阶段新增动态后端数据只有 `about_profile` 和 `about_project`。
- 技术栈和联系方式继续由前端写死，后续如需后台配置再单独设计数据模型和接口。
- 联系方式暂不设计跳转、复制和二维码行为，交由后续前端阶段决定。
- 课程表 AI 识别和 Markdown 文件解析属于第四阶段。
