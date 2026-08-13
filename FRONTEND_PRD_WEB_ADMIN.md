# steve0v0 小屋 · 网页管理端 PRD

> 版本：v0.1 ｜ 日期：2026-08-11 ｜ 状态：初稿待确认
> 技术栈：Vue 3 + Vite ｜ 对接后端：steve-home（Spring Boot 3.5.16）
> 说明：本 PRD 只定义**功能、页面逻辑、交互行为与后端 API 对接**，不涉及任何布局与 UI 视觉，UI 由后续补充。

---

## 1. 项目定位

网页管理端是 steve0v0 小屋的**全量管理后台**，管理员通过暗号登录后，对线上小程序展示的所有内容进行增删改查。它与小程序共享同一套后端（`steve-home`）和同一份数据，因此本机运行的网页管理端即可有效管理线上小程序。

- **运行形态**：本地开发运行（本机浏览器），对接线上生产后端。后续如需随时随地管理，再决定是否部署。
- **小程序管理后台**：本期不做（保留，后续阶段在原生小程序内实现隐藏入口管理）。

---

## 2. 技术栈与运行环境

| 维度 | 选型 | 说明 |
|---|---|---|
| 前端框架 | Vue 3 | 组合式 API |
| 构建 | Vite | 本地开发服务器 |
| 路由 | Vue Router | 管理端各功能页路由 |
| 状态管理 | Pinia | 全局登录态、token |
| HTTP | Axios | 封装统一请求/响应/鉴权 |
| API 地址 | 环境变量 `VITE_API_BASE` | 指向线上生产后端地址 |

**本地对接说明**：网页管理端在本机浏览器运行，调用线上生产后端。后端 CORS 已放行所有来源（`allowedOriginPatterns("*")` + `allowCredentials(true)`），本机跨域调用无需额外配置。`VITE_API_BASE` 配置为生产后端公网地址即可。

---

## 3. 全局约定

### 3.1 统一响应格式

所有接口返回 `{ code, message, data }`，`data` 可为对象/数组/`null`。

| code | 含义 | 前端处理 |
|---|---|---|
| 200 | 成功 | 正常取 `data` |
| 400 | 请求参数错误 | 提示后端 message |
| 401 | 未授权 / Token 无效 / 暗号错误 | 见 3.4 会话处理 |
| 403 | 禁止访问 | 提示无权限 |
| 404 | 资源不存在 | 提示资源不存在 |
| 429 | 请求过于频繁（登录限流） | 提示稍后再试 |
| 500 | 服务器内部错误 | 提示系统错误 |

### 3.2 分页格式

`page` 从 **1** 开始，`size` 默认 **10**、最大 **50**。

```json
{
  "code": 200, "message": "success",
  "data": { "page": 1, "size": 10, "total": 42, "hasMore": true, "list": [] }
}
```

### 3.3 鉴权方式

- 登录成功后获取 JWT，前端存储；所有管理接口（除登录）请求头携带 `Authorization: Bearer <token>`。
- JWT 有效期 **7 天**；登录返回 `expireAt`（毫秒时间戳），前端可据此做本地过期判断。
- 登录失败 **401**；同 IP **5 分钟内连续失败 5 次** 返回 **429** 限流。
- 修改暗号后旧 Token 立即失效（后端 `tokenVersion + 1`），需用返回的新 Token 继续操作。

### 3.4 会话处理（网页端）

- 登录态持久化保存（localStorage + Pinia），页面刷新后自动恢复并校验有效期。
- Axios 统一拦截：请求自动附加 Token；响应收到 **401** 时，清除本地登录态并跳转登录页。
- 可依据 `expireAt` 在本地判断是否已过期，提前提示重新登录。
- 登录页与登录态管理：管理员输入暗号 → `POST /api/admin/auth` → 成功后进入管理功能。

---

## 4. 功能模块清单

网页管理端包含以下功能模块（仅功能划分，不含 UI 布局）：

| 模块 | 功能 |
|---|---|
| 登录 | 暗号登录、修改暗号 |
| 文章管理 | 文章列表（含草稿）、新建（发布/存草稿）、详情、删除 |
| 动态管理 | 动态列表、发布、删除 |
| 学习记录管理 | 列表（日期筛选）、新增、编辑、删除 |
| 课程表管理 | 课程列表、新增、编辑、删除 |
| 个人状态管理 | 修改当前状态、当前任务、心情签名 |
| 关于页·个人资料 | 查询、覆盖/清空姓名与头像 |
| 关于页·GitHub 项目 | 项目列表、详情、新增、修改、删除 |
| 图片上传 | 上传图片获取 URL（供文章/动态/项目引用） |

> 番茄钟不需要后台管理接口（PRD 已确认：仅前端展示，不提供后台管理）。技术栈与联系方式由小程序前端静态维护，不在本管理端范围。

---

## 5. 各模块功能与 API 对接明细

> 字段标注：`必填` / `可选`；`P` 表示分页参数（`page`/`size`）。

### 5.1 登录

**功能**：管理员输入暗号登录；支持修改暗号。

#### 5.1.1 暗号登录

`POST /api/admin/auth`（无鉴权）

请求体：

```json
{ "secret": "暗号" }
```

响应 `LoginVO`：

```json
{ "token": "...", "expireAt": 1790000000000 }
```

- `expireAt`：Token 到期时间（毫秒时间戳）。
- 业务交互：
  - 暗号为空 → 前端校验提示"请输入暗号"。
  - 暗号错误 → 后端返回 401 `"暗号错误"`，前端提示（**不暴露其他信息**）。
  - 限流 429 → 提示"尝试过于频繁，请 5 分钟后再试"。
  - 成功 → 保存 Token，进入管理功能。

#### 5.1.2 修改暗号

`POST /api/admin/auth/change-secret`（JWT 鉴权）

请求体：

```json
{ "oldSecret": "旧暗号", "newSecret": "新暗号" }
```

- 校验：旧暗号必填；新暗号 **6-50 个字符**、必填。
- 响应：新 `LoginVO`（含新 Token），旧 Token 立即失效。
- 业务交互：
  - 旧暗号错误 → 401 `"旧暗号错误"`。
  - 成功后前端保存返回的新 Token，替换本地旧 Token。

---

### 5.2 文章管理

**功能**：管理技术博客与生活文章，支持草稿；查看（含草稿）、新建（发布或存草稿）、删除。

> 后端本期**未提供文章编辑接口**（无 `PUT /api/admin/articles/{id}`）。因此本期仅支持新建、发布、存草稿、删除、查看。草稿继续编辑能力见 §7 待确认问题。

#### 5.2.1 文章列表（含草稿）

`GET /api/admin/articles`（JWT）

查询参数：`category`（tech/life，可选）、`status`（0-草稿 / 1-已发布，可选）、`P`。

响应 `PageResult<ArticleListVO>`：

```json
{
  "id": 1, "title": "标题", "summary": "摘要", "coverImage": null,
  "category": "tech", "tags": "java,spring", "viewCount": 0,
  "publishedAt": "2026-08-11T12:00:00", "readTimeMinutes": 5
}
```

- `tags` 为逗号分隔字符串；`publishedAt` 已发布才有值，草稿为 `null`。
- 业务交互：支持按分类、状态筛选；支持分页加载；列表不含正文。

#### 5.2.2 文章详情（含草稿）

`GET /api/admin/articles/{id}`（JWT）

响应 `ArticleDetailVO`（在列表字段基础上追加）：

```json
{ "...列表字段...", "content": "Markdown正文", "status": 0,
  "createdAt": "...", "updatedAt": "...", "readTimeMinutes": 5 }
```

- 业务交互：查看完整 Markdown 正文与元信息；草稿也可查看。

#### 5.2.3 新建文章（发布或存草稿）

`POST /api/admin/articles`（JWT）

请求体 `ArticleCreateDTO`：

```json
{
  "title": "标题",            // 必填，≤200
  "summary": "摘要",          // 可选，≤500
  "content": "Markdown正文",  // 可选
  "coverImage": "图片URL",    // 可选
  "category": "tech",         // tech/life，默认 tech
  "tags": "java,spring",      // 可选，逗号分隔字符串
  "status": 0                 // 0-草稿 / 1-已发布，默认草稿
}
```

响应：`{ "data": 文章ID }`。

- 业务交互：
  - 提供"保存草稿"与"直接发布"两种提交，二者仅 `status` 不同（草稿 0 / 发布 1）。
  - 发布后写入 `publishedAt`；存草稿则 `publishedAt` 为空。
  - 封面图与正文中图片：写作时先经图片上传（§5.10）获得 URL 后填入。

#### 5.2.4 删除文章

`DELETE /api/admin/articles/{id}`（JWT）

- 物理删除，不可恢复。
- 业务交互：删除前需二次确认。

---

### 5.3 动态管理

**功能**：发布、删除、查看生活动态；**不支持修改**。本期仅支持纯文字（text）与多图（image）两种类型。

#### 5.3.1 动态列表

`GET /api/admin/moments?page=&size=`（JWT）

响应 `PageResult<MomentListVO>`：

```json
{
  "id": 1, "content": "动态内容", "mediaType": "image",
  "mediaUrl": null, "createdAt": "...", "images": ["url1", "url2"]
}
```

- `mediaType`：`text` / `image`；`images` 按顺序返回图片 URL 列表；`mediaUrl` 本期不使用（音乐/视频预留）。

#### 5.3.2 发布动态

`POST /api/admin/moments`（JWT）

请求体 `MomentCreateDTO`：

```json
{ "content": "动态内容",   // 必填
  "mediaType": "image",    // text/image，默认 text
  "images": ["url1"] }     // 可选，仅 mediaType=image 时使用
```

响应：`{ "data": 动态ID }`。

- 业务交互：
  - 纯文字动态：仅填 `content`。
  - 多图动态：先经图片上传（§5.10）获取 URL 列表，再随内容一起提交。

#### 5.3.3 删除动态

`DELETE /api/admin/moments/{id}`（JWT）

- 物理删除，关联图片自动清除。
- 业务交互：删除前需二次确认。

---

### 5.4 学习记录管理

**功能**：学习记录的增、删、改、查，支持按日期范围筛选。

#### 5.4.1 学习记录列表

`GET /api/admin/study-records`（JWT）

查询参数：`startDate`、`endDate`（可选，`yyyy-MM-dd`）、`P`。

响应 `PageResult<StudyRecordListVO>`：

```json
{ "id": 1, "recordDate": "2026-08-11", "subject": "主题", "duration": 60, "createdAt": "..." }
```

- 业务交互：支持按日期范围筛选；分页展示。

#### 5.4.2 学习记录详情

`GET /api/admin/study-records/{id}`（JWT）

响应 `StudyRecordDetailVO`（追加字段）：

```json
{ "...列表字段...", "content": "学习内容", "updatedAt": "..." }
```

#### 5.4.3 新增学习记录

`POST /api/admin/study-records`（JWT）

请求体 `StudyRecordCreateDTO`：

```json
{ "recordDate": "2026-08-11",   // 必填，日期
  "subject": "主题",             // 必填，≤100
  "content": "学习内容",         // 必填
  "duration": 60 }               // 必填，分钟，≥1
```

响应：`{ "data": 记录ID }`。

#### 5.4.4 编辑学习记录

`PUT /api/admin/study-records/{id}`（JWT）

请求体 `StudyRecordUpdateDTO`（**全量更新**，所有字段必填，同新增字段）：

```json
{ "recordDate": "2026-08-11", "subject": "主题", "content": "学习内容", "duration": 90 }
```

#### 5.4.5 删除学习记录

`DELETE /api/admin/study-records/{id}`（JWT）

- 业务交互：删除前二次确认。

---

### 5.5 课程表管理

**功能**：手动录入/编辑/删除课程，支持两种模式：**仅当天** 与 **每周重复**。

#### 5.5.1 课程列表

`GET /api/admin/courses?page=&size=`（JWT）

响应 `PageResult<CourseVO>`：

```json
{
  "id": 1, "name": "高等数学", "startDate": "2026-09-01", "endDate": "2027-01-15",
  "startTime": "08:00", "endTime": "09:40", "location": "A201",
  "dayOfWeek": 1, "isRepeated": 1, "createdAt": "...", "updatedAt": "..."
}
```

- `dayOfWeek`：1-7 表示周一至周日；`isRepeated`：0-仅当天 / 1-每周重复。

#### 5.5.2 课程详情

`GET /api/admin/courses/{id}`（JWT）

响应 `CourseVO`（同列表字段）。

#### 5.5.3 新增课程

`POST /api/admin/courses`（JWT）

请求体 `CourseCreateDTO`：

```json
{
  "name": "课程名称",        // 必填，≤200
  "startDate": "2026-09-01", // 必填
  "endDate": "2027-01-15",   // 必填
  "startTime": "08:00",      // 必填
  "endTime": "09:40",        // 必填
  "location": "A201",        // 可选，≤200
  "dayOfWeek": 1,            // 仅每周重复模式必填（1-7）
  "isRepeated": 1            // 0-仅当天 / 1-每周重复，默认0
}
```

响应：`{ "data": 课程ID }`。

- 业务交互（模式联动，前端校验）：
  - **仅当天**：`startDate = endDate`，不填 `dayOfWeek`。
  - **每周重复**：必填 `dayOfWeek`（1-7），课程在 `startDate` 至 `endDate` 范围内按该星期重复出现。

#### 5.5.4 编辑课程

`PUT /api/admin/courses/{id}`（JWT）

请求体 `CourseUpdateDTO`（**全量更新**，字段同新增，`isRepeated` 必填）：

```json
{ "name": "...", "startDate": "...", "endDate": "...", "startTime": "...",
  "endTime": "...", "location": "...", "dayOfWeek": 1, "isRepeated": 1 }
```

#### 5.5.5 删除课程

`DELETE /api/admin/courses/{id}`（JWT）

- 业务交互：删除前二次确认。

---

### 5.6 个人状态管理

**功能**：修改当前个人状态、当前正在做的事、心情签名。

`PUT /api/admin/status`（JWT）

请求体 `StatusUpdateDTO`（**所有字段可选**，只更新传入字段）：

```json
{ "state": "studying", "currentTask": "写前端PRD", "mood": "状态不错" }
```

- `state` 取值：`online`（在线）/ `studying`（学习中）/ `busy`（忙碌）/ `rest`（休息）。
- 业务交互：可单独更新某一项，未传字段保持不变。

---

### 5.7 关于页 · 个人资料管理

**功能**：查询与覆盖/清空关于页的个人姓名与头像。

#### 5.7.1 查询个人资料

`GET /api/admin/about/profile`（JWT）

响应 `AboutProfileVO`：

```json
{ "name": "steve0v0", "avatarUrl": "https://..." }
```

#### 5.7.2 覆盖/清空个人资料

`PUT /api/admin/about/profile`（JWT）

请求体 `AboutProfileUpdateDTO`（`name`、`avatarUrl` 均**必填**；**覆盖语义**，传空字符串表示清空）：

```json
{ "name": "steve0v0", "avatarUrl": "https://..." }
```

- 头像只保存外部 HTTPS 图片地址（不接收图片文件上传）。
- 业务交互：进入页面先 `GET` 回显当前值；保存时整体覆盖；清空则传空字符串。

---

### 5.8 关于页 · GitHub 项目管理

**功能**：GitHub 项目的增、删、改、查，所有项目默认公开。

#### 5.8.1 项目列表

`GET /api/admin/about/projects`（JWT）

响应：`List<AboutProjectVO>`（**不分页**）：

```json
{
  "id": 1, "name": "项目名", "description": "简介", "githubUrl": "https://github.com/...",
  "techTags": ["java", "vue"], "sort": 0
}
```

#### 5.8.2 项目详情

`GET /api/admin/about/projects/{id}`（JWT）

响应 `AboutProjectVO`（同列表字段）。

#### 5.8.3 新增项目

`POST /api/admin/about/projects`（JWT）

请求体 `AboutProjectCreateDTO`：

```json
{
  "name": "项目名",           // 必填，≤100
  "description": "简介",      // 可选，≤500
  "githubUrl": "https://github.com/...",  // 必填，≤500，仅 GitHub HTTPS 地址
  "techTags": ["java", "vue"],// 可选，最多10个，每个≤50
  "sort": 0                   // 可选，≥0，默认0
}
```

响应：`{ "data": 项目ID }`。

- 业务交互：`githubUrl` 需为 GitHub HTTPS 地址；`techTags` 以数组形式提交。

#### 5.8.4 修改项目

`PUT /api/admin/about/projects/{id}`（JWT）

请求体 `AboutProjectUpdateDTO`（**全量更新**，字段同新增）。

#### 5.8.5 删除项目

`DELETE /api/admin/about/projects/{id}`（JWT）

- 业务交互：删除前二次确认。

---

### 5.9 图片上传（工具）

**功能**：上传图片获得可公开访问的 URL，供文章封面/正文、动态多图、GitHub 项目等处引用。

`POST /api/admin/upload`（JWT，`multipart/form-data`）

- 参数：字段名 `file`。
- 限制：仅 **JPG / PNG / GIF**，**≤ 5MB**；后端做文件签名 + 解码双重校验。
- 响应：`{ "url": "/upload/2026/08/xxxx.png" }`（返回相对路径，可拼接后端地址访问）。

- 业务交互：上传成功后返回 URL，前端将其填入对应字段（文章封面/正文、动态图片列表等）。

### 5.10 Markdown 文件导入（工具）

**功能**：将本地 Markdown 博客正文导入文章编辑器，并立即刷新右侧渲染预览。

`POST /api/admin/upload/markdown`（JWT，`multipart/form-data`）

- 参数：字段名 `file`。
- 限制：仅 `.md` / `.markdown`，UTF-8 编码，≤ 5MB；文件只解析不落盘。
- 响应：`{ "fileName": "article.md", "content": "# 标题\n\n正文" }`。
- 业务交互：导入会替换当前正文；已有正文时先确认，成功后由 `markdown-it` 自动更新预览。

---

## 6. 鉴权与接口对照表

| 模块 | 接口 | 鉴权 | 说明 |
|---|---|---|---|
| 登录 | POST `/api/admin/auth` | 无 | 暗号登录 |
| 修改暗号 | POST `/api/admin/auth/change-secret` | JWT | 旧 Token 失效 |
| 文章 | GET `/api/admin/articles` | JWT | 列表（含草稿，分类/状态筛选） |
| 文章 | GET `/api/admin/articles/{id}` | JWT | 详情（含草稿） |
| 文章 | POST `/api/admin/articles` | JWT | 新建（发布/存草稿） |
| 文章 | DELETE `/api/admin/articles/{id}` | JWT | 删除 |
| 动态 | GET `/api/admin/moments` | JWT | 列表 |
| 动态 | POST `/api/admin/moments` | JWT | 发布 |
| 动态 | DELETE `/api/admin/moments/{id}` | JWT | 删除 |
| 学习记录 | GET `/api/admin/study-records` | JWT | 列表（日期筛选） |
| 学习记录 | GET `/api/admin/study-records/{id}` | JWT | 详情 |
| 学习记录 | POST `/api/admin/study-records` | JWT | 新增 |
| 学习记录 | PUT `/api/admin/study-records/{id}` | JWT | 编辑（全量） |
| 学习记录 | DELETE `/api/admin/study-records/{id}` | JWT | 删除 |
| 课程 | GET `/api/admin/courses` | JWT | 列表 |
| 课程 | GET `/api/admin/courses/{id}` | JWT | 详情 |
| 课程 | POST `/api/admin/courses` | JWT | 新增 |
| 课程 | PUT `/api/admin/courses/{id}` | JWT | 编辑（全量） |
| 课程 | DELETE `/api/admin/courses/{id}` | JWT | 删除 |
| 状态 | PUT `/api/admin/status` | JWT | 部分更新 |
| 关于资料 | GET `/api/admin/about/profile` | JWT | 查询 |
| 关于资料 | PUT `/api/admin/about/profile` | JWT | 覆盖/清空 |
| GitHub 项目 | GET `/api/admin/about/projects` | JWT | 列表（不分页） |
| GitHub 项目 | GET `/api/admin/about/projects/{id}` | JWT | 详情 |
| GitHub 项目 | POST `/api/admin/about/projects` | JWT | 新增 |
| GitHub 项目 | PUT `/api/admin/about/projects/{id}` | JWT | 修改（全量） |
| GitHub 项目 | DELETE `/api/admin/about/projects/{id}` | JWT | 删除 |
| 上传 | POST `/api/admin/upload` | JWT | 图片上传 |
| 上传 | POST `/api/admin/upload/markdown` | JWT | 导入 Markdown 正文 |

---

## 7. 待确认问题

| # | 问题 | 影响 |
|---|---|---|
| 1 | **文章本期无编辑接口**（后端无 `PUT /api/admin/articles/{id}`）。存为草稿后如需继续编辑、或已发布文章需修订，是否新增后端编辑接口？ | 影响文章管理是否提供"编辑"能力 |
| 2 | 动态仅支持 `text` / `image`，音乐/视频是否本期开放？ | 影响动态发布表单字段 |
| 3 | 技术栈与联系方式由小程序前端静态维护，本管理端是否需要任何提示位？ | 仅说明性，不影响功能 |
| 4 | 修改暗号是否需要"再次输入确认新暗号"的前端校验？ | 前端交互细节 |
| 5 | `VITE_API_BASE` 默认指向本地还是线上后端？开发期切换方式？ | 影响运行配置 |

---

## 8. 已确认决策

| 问题 | 决策 |
|---|---|
| 管理端技术栈 | Vue 3 + Vite + Vue Router + Pinia + Axios |
| 运行形态 | 本机本地运行，对接线上生产后端 |
| 管理端是否部署 | 本期不部署，本地开发用 |
| 功能范围 | 全量管理（文章/动态/学习记录/课程/状态/关于资料/GitHub项目/上传） |
| 小程序管理后台 | 保留，本期不做 |
| 番茄钟 | 无需后台管理接口（仅前端展示） |
| 技术栈/联系方式 | 小程序前端静态维护，不进管理端 |
