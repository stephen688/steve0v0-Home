# steve0v0 小屋 · 后端第二阶段技术实现计划

> 版本：v1.0 ｜ 日期：2026-08-11  
> 技术栈：Spring Boot 3.5.16 + MyBatis-Plus + MySQL 8.0 + Maven  
> 第二阶段范围：首页日历、学习记录、手动课程、番茄钟、学习数据统计、个人状态

---

## 1. 技术选型确认

第二阶段沿用第一阶段技术栈，不新增第三方依赖。

| 维度 | 选型 | 说明 |
|---|---|---|
| 语言 | Java 21 LTS | 沿用第一阶段 |
| 框架 | Spring Boot 3.5.16 | 沿用第一阶段 |
| 安全 | Spring Security + JWT | 所有数据写入和管理接口需要 JWT |
| ORM | MyBatis-Plus 3.5.12 | 沿用第一阶段 |
| 数据库 | MySQL 8.0 | 使用手动 SQL 脚本建表 |
| 时间类型 | `LocalDate` / `LocalDateTime` | 使用本地时间，时区为 Asia/Shanghai |
| 分页 | `PageRequest` | page 从 1 开始，size 最大 50 |
| 响应格式 | `Result<T>` | 沿用第一阶段统一响应结构 |

---

## 2. 阶段拆分与 PRD 对齐

| 阶段 | 包含功能 | 对应 PRD 模块 |
|---|---|---|
| 第一阶段 | 项目骨架、JWT 登录、博客、动态、图片上传 | 2.2、2.3、3.1、3.2、3.3 |
| **第二阶段** | 首页日历、学习记录、手动课程、番茄钟、统计、个人状态 | 2.1、3.4、3.5、3.6 |
| 第三阶段 | 关于页个人资料、GitHub 项目 | 2.4、3.7 |
| 第四阶段 | 课程表 AI 识别、Markdown 文件解析 | 2.1.3、文件管理扩展 |

> **与第一阶段 TECH_PLAN 的差异**：第一阶段 `TECH_PLAN.md` 将"课程表手动录入"放在第三阶段。第二阶段计划将其提前到第二阶段实现（仅手动录入部分，AI 识别仍在第四阶段）。PRD 已同步更新此调整。

### 第二阶段明确偏差

| PRD 项 | 第二阶段处理 | 原因 |
|---|---|---|
| 课程 AI 识别 | 暂不实现 | 本阶段只实现手动录入 |
| 手动课程 | 本阶段实现 | 支持单日课程和每周重复课程 |
| 课程日期范围 | 新增 `start_date`/`end_date` 字段 | 每周重复课程需要日期范围控制生效区间，PRD 已同步更新 |
| 番茄钟公开提交 | 未登录只运行本地倒计时，登录后才允许保存 | 防止任何人刷统计数据，PRD 已同步更新 |
| 番茄钟统计查询 | 公开只读 | 小程序面向所有人展示 |
| 番茄钟后台历史管理 | 不实现 | 沿用当前产品决策 |
| 番茄钟补录 | 不实现 | 只保存倒计时完成记录 |
| 番茄钟时间字段 | 删除 `start_time`、`end_time`，新增 `completed_at` | `duration` 是倒计时设定时长的唯一真源，PRD 已同步更新 |
| 个人状态表名 | 使用 `personal_status`（PRD 原 `status`） | `status` 是 MySQL 保留字，PRD 已同步更新 |

---

## 3. 第二阶段范围

### 3.1 包含功能

- 首页日历月视图和周视图
- 学习记录的管理端增删改查
- 手动课程的管理端增删改查
- 课程选择“仅当天”或“每周重复”
- 公开个人状态展示
- 管理端修改个人状态
- 未登录用户使用本地番茄钟倒计时
- 登录用户完成番茄钟后持久化记录
- 学习时长、番茄钟数量、连续学习天数和热力图统计
- 统一参数校验、权限校验和异常响应

### 3.2 不包含功能

- 课程 AI 识别
- Markdown 文件解析
- 番茄钟后台历史修改、删除和补录
- 多用户数据隔离
- 番茄钟公开匿名写入
- 番茄钟 `start_time`、`end_time` 字段
- 关于页管理功能

---

## 4. 访问权限设计

### 4.1 公开访问

以下接口无需登录：

- 首页日历查询
- 个人状态查询
- 学习数据统计查询
- 其他面向访客展示的内容接口

### 4.2 JWT 保护

以下操作需要有效 JWT：

- 新增、修改、删除学习记录
- 查询管理端学习记录列表和详情
- 新增、修改、删除课程
- 查询管理端课程列表和详情
- 修改个人状态
- 保存番茄钟完成记录

### 4.3 番茄钟特殊规则

- 未登录用户可以正常使用前端倒计时。
- 未登录倒计时只保存在前端，不调用持久化接口。
- 登录用户倒计时完成后，调用 `POST /api/home/pomodoro`。
- 服务端强制校验 JWT。
- 无 JWT 或 JWT 无效时返回 401，不写入数据库。
- 不增加公开 IP 防刷规则，因为接口本身不允许匿名写入。
- 仍限制单次番茄钟时长为 1–480 分钟。

---

## 5. 项目结构

```text
src/main/java/com/steve0v0/home/
├── controller/
│   ├── HomeCalendarController.java
│   ├── HomeStatusController.java
│   ├── HomePomodoroController.java
│   ├── HomeStatsController.java
│   ├── AdminStudyRecordController.java
│   ├── AdminCourseController.java
│   └── AdminStatusController.java
│
├── service/
│   ├── CalendarService.java
│   ├── StudyRecordService.java
│   ├── CourseService.java
│   ├── PomodoroService.java
│   ├── StatsService.java
│   └── StatusService.java
│
├── mapper/
│   ├── StudyRecordMapper.java
│   ├── CourseMapper.java
│   ├── PomodoroMapper.java
│   └── StatusMapper.java
│
├── entity/
│   ├── StudyRecord.java
│   ├── Course.java
│   ├── Pomodoro.java
│   └── Status.java
│
├── dto/
│   ├── StudyRecordCreateDTO.java
│   ├── StudyRecordUpdateDTO.java
│   ├── StudyRecordQueryDTO.java
│   ├── CourseCreateDTO.java
│   ├── CourseUpdateDTO.java
│   ├── CourseQueryDTO.java
│   ├── PomodoroCompleteDTO.java
│   └── StatusUpdateDTO.java
│
└── vo/
    ├── CalendarVO.java
    ├── StudyRecordListVO.java
    ├── StudyRecordDetailVO.java
    ├── CourseVO.java
    ├── StatusVO.java
    ├── StatsVO.java
    └── HeatmapItemVO.java
```

数据库脚本：

```text
src/main/resources/sql/phase2.sql
```

第二阶段脚本在第一阶段建表脚本执行完成后执行。

---

## 6. 数据库设计

### 6.1 学习记录表

```sql
CREATE TABLE study_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    record_date  DATE         NOT NULL COMMENT '学习日期',
    subject      VARCHAR(100) NOT NULL COMMENT '主题或科目',
    content      TEXT         NOT NULL COMMENT '学习内容',
    duration     INT          NOT NULL COMMENT '学习时长，单位分钟',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';
```

### 6.2 课程表

```sql
CREATE TABLE course (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(200) NOT NULL COMMENT '课程名称',
    start_date   DATE         NOT NULL COMMENT '开始日期',
    end_date     DATE         NOT NULL COMMENT '结束日期',
    start_time   TIME         NOT NULL COMMENT '开始时间',
    end_time     TIME         NOT NULL COMMENT '结束时间',
    location     VARCHAR(200)          DEFAULT NULL COMMENT '上课地点',
    day_of_week  TINYINT               DEFAULT NULL COMMENT '星期，1-7表示周一至周日',
    is_repeated  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0-仅当天，1-每周重复',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_course_date_range (start_date, end_date),
    INDEX idx_course_repeat (is_repeated, day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';
```

课程规则：

- `is_repeated = 0`：仅当天课程，必须满足 `start_date = end_date`，且 `day_of_week` 必须为 null。
- `is_repeated = 1`：每周重复课程，必须提供 `day_of_week`（1-7），且 `start_date <= end_date`。
- 每周课程只在日期范围内、且星期匹配时生成。
- 日期范围为闭区间，包含开始日期和结束日期。
- 服务端新增和修改课程时强制校验上述规则，违反时返回 400。

### 6.3 个人状态表

```sql
CREATE TABLE personal_status (
    id            BIGINT       NOT NULL,
    state         VARCHAR(20)  NOT NULL DEFAULT 'online',
    current_task  VARCHAR(200)          DEFAULT NULL,
    mood          VARCHAR(200)          DEFAULT NULL,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人状态表';
```

设计规则：

- 当前系统为单站点模式，固定使用一条状态记录。
- 建议使用 `id = 1`。
- 没有记录时，接口返回默认状态：
  - `state = online`
  - `currentTask = ""`
  - `mood = ""`
- 表名为 `personal_status`（非 `status`，因 `status` 是 MySQL 保留字）。
- Entity 类名 `Status`，需标注 `@TableName("personal_status")` 与数据库表名映射。
- 使用 `INSERT ... ON DUPLICATE KEY UPDATE` 实现 upsert，固定操作 `id = 1` 的记录。

### 6.4 番茄钟表

```sql
CREATE TABLE pomodoro (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    task_name    VARCHAR(200)          DEFAULT NULL COMMENT '关联任务',
    duration     INT          NOT NULL COMMENT '倒计时设定时长，单位分钟',
    completed_at DATETIME     NOT NULL COMMENT '完成时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_completed_at (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番茄钟完成记录表';
```

设计规则：

- `duration` 是唯一的专注时长真源。
- `completed_at` 只用于确定记录日期和统计范围。
- 删除 `start_time`、`end_time`。
- 不允许保存未完成的番茄钟。
- 不支持本阶段手动补录。
- `task_name` 为自由文本字段，不外键关联 `study_record`。PRD 中"可关联当前学习任务"的定位为显示用途，本阶段不支持按科目聚合统计。如后续需要科目级统计，可改为外键关联。

---

## 7. 实现步骤

### Step 1：数据库和实体层

- [ ] 编写 `phase2.sql`
- [ ] 创建学习记录、课程、状态、番茄钟表
- [ ] 编写对应 Entity、Mapper
- [ ] 为日期和完成时间建立索引
- [ ] 验证数据库结构和字段类型

### Step 2：首页日历和状态查询

- [ ] 实现 `GET /api/home/calendar`
- [ ] 接收（query string）：
  - `view`：`month` 或 `week`，默认 `month`
  - `date`：当前查询日期，格式 `yyyy-MM-dd`，默认今天
- [ ] 月视图查询当前日期所在月份的完整日期范围（当月 1 日到最后一天）。
- [ ] 周视图查询当前日期所在周，按周一至周日返回。
- [ ] 返回指定范围内的学习记录、单日课程和每周重复课程。
- [ ] 响应结构见 9.2，前端根据 `recordDate`/`date` 字段分组到对应日期格子。
- [ ] 实现 `GET /api/home/status`。
- [ ] 无状态记录时返回默认状态。

### Step 3：学习记录管理

- [ ] 实现学习记录列表查询
- [ ] 实现学习记录详情查询
- [ ] 实现新增学习记录
- [ ] 实现修改学习记录
- [ ] 实现删除学习记录
- [ ] 所有管理端接口要求 JWT
- [ ] 列表复用 `PageRequest`
- [ ] 支持按日期范围查询（`startDate`、`endDate`，均为 `yyyy-MM-dd`）
- [ ] 列表按 `record_date DESC, id DESC` 排序
- [ ] 校验日期、主题、内容和学习时长
- [ ] 学习时长 `duration` 必须大于 0

### Step 4：手动课程管理

- [ ] 实现课程列表查询
- [ ] 实现课程详情查询
- [ ] 实现新增课程
- [ ] 实现修改课程
- [ ] 实现删除课程
- [ ] 所有管理端接口要求 JWT
- [ ] 列表复用 `PageRequest`
- [ ] 列表按 `start_date DESC, id DESC` 排序
- [ ] 前端录入时提供两个明确选项：
  - 仅当天
  - 每周重复
- [ ] 服务端校验两种课程模式的数据完整性（见 6.2 课程规则）
- [ ] 日历查询时按日期范围生成课程实例
- [ ] 不实现 AI 识别接口

### Step 5：番茄钟完成记录

- [ ] 实现 `PomodoroCompleteDTO`
- [ ] 请求字段：
  - `taskName`：可选
  - `duration`：必填，1–480 分钟
- [ ] 服务端收到请求时生成当前本地时间 `completed_at`
- [ ] 不接收客户端提交的 `start_time`、`end_time`
- [ ] `POST /api/home/pomodoro` 强制校验 JWT
- [ ] 未登录请求返回 401，不写入数据库
- [ ] 登录用户只能在完成倒计时后提交
- [ ] 不新增番茄钟后台管理接口

### Step 6：统计数据

- [ ] 实现 `GET /api/home/stats`
- [ ] 统计接口公开访问
- [ ] 统计最近 30 天热力图（30 天范围 = 今天往前推 29 天，含今天共 30 天）
- [ ] 统计今日学习时长
- [ ] 统计今日番茄钟数量
- [ ] 统计番茄钟专注总时长
- [ ] 统计本周学习时长（本周 = 周一至周日，与日历周视图一致）
- [ ] 统计本周每日番茄钟数量（固定 7 个元素，周一到周日）
- [ ] 统计连续学习天数
- [ ] 学习记录和已完成番茄钟都算作活跃来源
- [ ] 热力图数量计算：

```text
每日活跃数量 = 当日学习记录数量 + 当日完成番茄钟数量
```

- [ ] 学习时长和番茄钟时长分别统计，不合并。
- [ ] 连续学习天数从今天向前计算。
- [ ] 如果今天没有任何活动，连续天数为 0。
- [ ] "今日"和"本周"的日期边界以 `Asia/Shanghai` 时区为准。

### Step 7：个人状态修改

- [ ] 实现 `PUT /api/admin/status`
- [ ] 服务端要求 JWT
- [ ] 支持修改：
  - `state`
  - `currentTask`
  - `mood`
- [ ] 状态值限制为：
  - `online`
  - `studying`
  - `busy`
  - `rest`
- [ ] 使用单条记录 upsert 逻辑。

### Step 8：权限和异常处理

修改 `SecurityConfig`：

- [ ] 放行：
  - `GET /api/home/calendar`
  - `GET /api/home/status`
  - `GET /api/home/stats`
- [ ] 保护：
  - `POST /api/home/pomodoro`
  - `/api/admin/**`
- [ ] 未认证访问写入接口返回 401。
- [ ] 参数错误返回 400。
- [ ] 资源不存在返回 404。
- [ ] 所有响应继续使用 `Result<T>`。
- [ ] 第二阶段分页接口统一使用 page≥1、size≤50。

---

## 8. 接口清单

### 8.1 首页公开接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/home/calendar` | 无 | 查询月视图或周视图 |
| GET | `/api/home/status` | 无 | 查询个人状态 |
| GET | `/api/home/stats` | 无 | 查询公开统计数据 |

### 8.2 番茄钟接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/home/pomodoro` | JWT | 保存已完成番茄钟 |
| GET | `/api/home/stats` | 无 | 查询番茄钟统计 |

未登录用户使用倒计时不会调用保存接口。

### 8.3 学习记录管理接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/admin/study-records` | JWT | 学习记录列表 |
| GET | `/api/admin/study-records/{id}` | JWT | 学习记录详情 |
| POST | `/api/admin/study-records` | JWT | 新增学习记录 |
| PUT | `/api/admin/study-records/{id}` | JWT | 修改学习记录 |
| DELETE | `/api/admin/study-records/{id}` | JWT | 删除学习记录 |

### 8.4 课程管理接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/admin/courses` | JWT | 课程列表 |
| GET | `/api/admin/courses/{id}` | JWT | 课程详情 |
| POST | `/api/admin/courses` | JWT | 手动新增课程 |
| PUT | `/api/admin/courses/{id}` | JWT | 修改课程 |
| DELETE | `/api/admin/courses/{id}` | JWT | 删除课程 |

### 8.5 状态管理接口

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| PUT | `/api/admin/status` | JWT | 修改个人状态 |

---

## 9. 主要请求和响应结构

### 9.1 番茄钟完成请求

```json
{
  "taskName": "阅读技术文档",
  "duration": 25
}
```

服务端自动生成：

```text
completed_at = 当前本地时间
```

### 9.2 日历响应

```json
{
  "view": "month",
  "startDate": "2026-08-01",
  "endDate": "2026-08-31",
  "records": [
    {
      "id": 1,
      "recordDate": "2026-08-11",
      "subject": "Spring Security",
      "duration": 120
    }
  ],
  "courses": [
    {
      "id": 1,
      "date": "2026-08-11",
      "name": "高等数学",
      "startTime": "08:00",
      "endTime": "09:40",
      "location": "教学楼A301"
    }
  ]
}
```

字段说明：

- `records`：指定日期范围内的学习记录列表，每项包含：
  - `id`：学习记录 ID
  - `recordDate`：学习日期（`yyyy-MM-dd`）
  - `subject`：学习主题
  - `duration`：学习时长（分钟）
- `courses`：指定日期范围内生成的课程实例列表，每项包含：
  - `id`：课程 ID
  - `date`：该课程实例对应的日期（`yyyy-MM-dd`）
  - `name`：课程名称
  - `startTime`：开始时间（`HH:mm`）
  - `endTime`：结束时间（`HH:mm`）
  - `location`：上课地点（可为 null）
- 前端根据 `recordDate` / `date` 字段将数据分组到对应日期格子上。

### 9.3 状态响应

```json
{
  "state": "online",
  "currentTask": "",
  "mood": ""
}
```

### 9.4 统计响应

```json
{
  "todayStudyMinutes": 120,
  "todayPomodoroCount": 3,
  "todayPomodoroMinutes": 75,
  "weeklyStudyMinutes": 480,
  "weeklyPomodoroCounts": [
    { "date": "2026-08-05", "count": 2 },
    { "date": "2026-08-06", "count": 0 },
    { "date": "2026-08-07", "count": 3 },
    { "date": "2026-08-08", "count": 1 },
    { "date": "2026-08-09", "count": 0 },
    { "date": "2026-08-10", "count": 4 },
    { "date": "2026-08-11", "count": 3 }
  ],
  "streakDays": 5,
  "heatmap": []
}
```

字段说明：

- `todayStudyMinutes`：今日学习时长（分钟），来源于 `study_record.duration`。
- `todayPomodoroCount`：今日完成番茄钟数量。
- `todayPomodoroMinutes`：今日番茄钟专注总时长（分钟），来源于 `pomodoro.duration`。
- `weeklyStudyMinutes`：本周（周一至周日）学习时长合计。
- `weeklyPomodoroCounts`：本周每日番茄钟数量，固定 7 个元素，按周一到周日排列：
  - `date`：日期（`yyyy-MM-dd`）
  - `count`：当日番茄钟完成数量（无记录为 0）
- `streakDays`：连续学习天数，从今天向前计算。
- `heatmap`：最近 30 天热力图数据，结构见 9.5。
- 学习时长和番茄钟时长分别统计，不合并。

### 9.5 热力图数据

```json
{
  "date": "2026-08-11",
  "activityCount": 4
}
```

其中：

```text
activityCount = 学习记录数量 + 完成番茄钟数量
```

---

## 10. 配置规划

沿用第一阶段的数据库、JWT、上传和分页配置。

新增或确认：

```yaml
spring:
  jackson:
    time-zone: Asia/Shanghai

steve:
  time-zone: Asia/Shanghai
```

> **注意**：`spring.jackson.time-zone` 只影响 JSON 序列化/反序列化，不影响 `LocalDateTime.now()` 的取值。服务端生成 `completed_at` 等时间字段时，必须使用 `LocalDateTime.now(ZoneId.of("Asia/Shanghai"))`，或在 JVM 启动参数中设置 `-Duser.timezone=Asia/Shanghai`，避免服务器时区不一致导致时间错误。

安全规则必须明确区分（使用具体路径，不使用 `GET /api/home/**` 通配符）：

```text
GET  /api/home/calendar         公开读取
GET  /api/home/status           公开读取
GET  /api/home/stats            公开读取
POST /api/home/pomodoro         JWT 鉴权
/api/admin/**                   JWT 鉴权
```

> **注意**：现有 `SecurityConfig` 使用 `anyRequest().permitAll()` 作为兜底规则。必须在 `anyRequest()` 之前显式声明 `POST /api/home/pomodoro` 需要 `authenticated()`，否则该接口会被兜底规则放行，导致 JWT 鉴权失效。

分页配置保持：

```text
page >= 1
size 默认 10
size 最大 50
```

MyBatis-Plus 分页插件的最大限制应与接口分页上限保持一致，避免出现接口声明 50、底层允许 500 的不一致。

---

## 11. 统一响应格式

沿用第一阶段：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

常用状态码：

| code | 说明 |
|---|---|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或 JWT 无效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 12. 测试计划

### 12.1 权限测试

- [ ] 未登录查询日历成功。
- [ ] 未登录查询状态成功。
- [ ] 未登录查询统计成功。
- [ ] 未登录直接提交番茄钟返回 401。
- [ ] 未登录提交番茄钟后数据库没有新增记录。
- [ ] 携带有效 JWT 可以保存番茄钟。
- [ ] 管理端学习记录和课程接口无 JWT 时返回 401。

### 12.2 番茄钟测试

- [ ] `duration=1` 成功。
- [ ] `duration=480` 成功。
- [ ] `duration=0` 被拒绝。
- [ ] `duration=481` 被拒绝。
- [ ] 缺少 `duration` 被拒绝。
- [ ] 请求中不再接收 `start_time`、`end_time`。
- [ ] 服务端自动生成 `completed_at`。
- [ ] 只有完成后的记录进入统计。

### 12.3 统计测试

- [ ] 学习记录可以增加活跃日。
- [ ] 完成番茄钟可以增加活跃日。
- [ ] 热力图数量等于两类记录数量之和。
- [ ] 学习时长和番茄钟时长分别统计。
- [ ] 最近 30 天范围正确。
- [ ] 今天没有活动时连续天数为 0。
- [ ] 今天有活动时可以正确向前计算连续天数。

### 12.4 课程测试

- [ ] 单日课程必须满足 `start_date=end_date`。
- [ ] 单日课程只在指定日期出现。
- [ ] 每周课程必须填写星期。
- [ ] 每周课程只在日期范围内、指定星期出现。
- [ ] 日期范围边界包含开始和结束日期。
- [ ] 日历月视图和周视图返回结果正确。

### 12.5 管理功能测试

- [ ] 学习记录新增、查询、修改、删除成功。
- [ ] 课程新增、查询、修改、删除成功。
- [ ] 状态修改后公开接口可以读取最新状态。
- [ ] 没有状态记录时返回默认状态。

### 12.6 回归测试

- [ ] 第一阶段文章接口正常。
- [ ] 第一阶段动态接口正常。
- [ ] JWT 登录和暗号修改正常。
- [ ] 文件上传接口正常。
- [ ] 执行 `mvn clean test`。
- [ ] 执行项目编译和接口冒烟测试。

---

## 13. 验收标准

| 验收项 | 验收结果 |
|---|---|
| 首页日历 | 可查看月视图和周视图 |
| 学习记录 | 管理端可以完成增删改查 |
| 手动课程 | 可选择仅当天或每周重复 |
| 单日课程 | 只在指定日期显示 |
| 每周课程 | 在日期范围内按星期显示 |
| 番茄钟使用 | 未登录用户可以正常倒计时 |
| 番茄钟保存 | 只有 JWT 用户完成后才能保存 |
| 番茄钟防刷 | 匿名请求无法写入数据 |
| 番茄钟字段 | 只使用 duration 和 completed_at |
| 公开统计 | 无需登录即可查询 |
| 数据统计 | 学习记录和番茄钟分别统计 |
| 连续学习 | 按最近活动日期正确计算 |
| 热力图 | 正确展示最近 30 天活动数量 |
| 状态展示 | 空数据返回 online 默认状态 |
| 权限控制 | 所有管理端增删改查均要求 JWT |
| 第一阶段回归 | 原有博客、动态、登录、上传功能不受影响 |

---

## 14. 默认前提

- 当前系统仍是单个站点/个人主页模型，JWT 代表管理员或站点维护者，不设计多用户数据隔离。
- “公开”只针对展示和查询接口；涉及数据写入、修改、删除的接口仍需 JWT。
- 本阶段不支持番茄钟手动补录。
- 第一阶段的实现内容不修改；阶段拆分表与 PRD、后续阶段计划保持同步。
