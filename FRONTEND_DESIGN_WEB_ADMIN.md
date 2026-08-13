# steve0v0 小屋 · 网页管理端前端设计文档

> 版本：v0.2 ｜ 日期：2026-08-11 ｜ 状态：待确认
> 技术栈：Vue 3 + Vite + shadcn-vue + Pinia + Vue Router + Axios
> 设计主题：航海日志 / 探险家手账
> 说明：本文档为前端实现的设计依据，包含视觉系统、组件规范、页面结构与交互行为定义。

## v0.2 变更摘要

1. **装饰收敛**：全站签名元素收敛为「胶带徽章 + 纸张阴影」两项；移除图钉、随机旋转、印章 Toast、登录页"翻开"动画。
2. **对比度修正**：浅色模式 `primary`、`muted-foreground` 加深，实测对比度由约 4.2-4.3:1 提升至 ≥ 5:1，达到文档自定的 4.5:1 底线。
3. **语义色补全**：`accent` 与 `destructive` 解耦；新增 success / warning / info 三色。
4. **字体分工重构**：正文回归无衬线；霞鹜文楷仅用于标题与品牌；Mono 限定于数字、时间、代码、标签。
5. **布局分级**：新增宽页（1400px）/ 窄页（680px）两级内容宽度。
6. **规范补全**：新增动效 token、z-index 分层、focus-visible、prefers-reduced-motion、loading/空态/错误态规范、图标库约定。
7. **噪点修正**：纹理不再全局覆盖内容层。
8. **文案去装饰化**：界面文案回归工具语言。

---

## 1. 设计定位

网页管理端是 steve0v0 小屋的"内容航海日志"——管理员像船长记录航行一样，管理文章、动态、学习记录、课程与项目。它不是冷冰冰的表格后台，而是一本摊开的、可翻阅的、有触感的数字手账。

**核心气质关键词**：真实质感、个人品牌、可触摸的纸张、克制、秩序感。

**质感原则**：手账气质由**材质**（纸张色、阴影、纹理）承载，而不是由装饰元素的数量承载。全站只允许两个签名装饰元素：

1. **胶带式状态徽章**（已发布 / 草稿等状态标签）
2. **纸张阴影系统**（多层柔和阴影 + 顶部内高光）

其他一切装饰（图钉、印章、旋转、手绘涂鸦）默认不使用。例外只允许出现在登录页这一处"门面"场景。

**借鉴对象**：
- 真实工具：Notion（编辑秩序）、Linear（信息层级）、Sanity Studio（内容卡片）、Things 3（拟物按钮）。
- 视觉灵感：复古航海日志、胶片手账、皮革封面笔记本、便签纸与胶带。

---

## 2. 已确认设计决策

| 决策项 | 结论 |
|---|---|
| 组件库 | shadcn-vue（shadcn/ui 的 Vue 官方移植） |
| 状态管理 | Pinia |
| HTTP 库 | Axios |
| 路由 | Vue Router |
| 图标库 | Lucide（线性、圆角端点，全站统一，禁止混用填充图标） |
| 布局 | 左侧固定侧边栏 |
| 主题 | 浅色默认，支持深色切换 |
| 视觉方向 | 航海日志 / 探险家手账 |
| 配色基调 | 焦糖棕 + 象牙白 + 锈红 |
| 字体组合 | 衬线标题 + 无衬线正文 + 等宽数字 |
| 装饰程度 | 低度：仅胶带徽章 + 纸张阴影两个签名元素 |
| Markdown 编辑 | 左侧编辑 + 右侧实时预览 |
| 列表形态 | 混合：文章/学习记录/课程用表格，动态/项目用卡片流 |
| 页面宽度 | 分级：宽页 1400px / 窄页 680px |

---

## 3. 设计原则

1. **纸张优先**：所有容器都应像某种纸张（笔记本页、便签、卡片纸），而不是漂浮的色块。
2. **质感真实**：使用细微的纸张噪点、多层阴影、顶部内高光，拒绝扁平渐变与塑料质感。
3. **层级靠材质区分**：不依赖大面积色块区分层级，而是靠边框粗细、阴影深浅、纸张白度、字体层级。
4. **装饰极简**：签名装饰元素只有两个（胶带徽章、纸张阴影），不加第三种。主题气质靠材质而非贴纸。
5. **反应即承诺**：只有可交互元素才有 hover 位移/阴影变化；纯展示容器保持静止。用户能用 hover 预判什么可以点击。
6. **可访问性底线**：正文与背景对比度 ≥ 4.5:1（浅色、深色同标准）；交互元素有清晰 focus-visible 状态；尊重 `prefers-reduced-motion`。

---

## 4. 色彩系统

### 4.1 色彩意象

| 意象 | 色名 | 用途 |
|---|---|---|
| 皮革封面 | 焦糖棕 | 主色、导航激活、重要按钮 |
| 旧纸张 | 象牙白 | 页面背景、卡片底色 |
| 火漆/印章 | 锈红 | 强调、选中态、高亮标签 |
| 墨水 | 深褐 | 正文、图标 |
| 铅笔灰 | 暖灰 | 次要文字、边框、禁用 |
| 黄铜 | 金褐 | 成功状态、高光 |
| 鲜血红 | 深绯红 | 危险/删除操作（与锈红强调色区分） |
| 钢笔墨水 | 墨蓝 | 信息提示 |

### 4.2 浅色模式 CSS 变量（映射 shadcn-vue）

```css
:root {
  --background: 40 33% 96%;          /* #F9F6F0 象牙白页面底 */
  --foreground: 24 30% 18%;          /* #3D2B1F 深褐墨水 */

  --card: 40 40% 98%;                /* #FDFBF7 更白的纸张 */
  --card-foreground: 24 30% 18%;

  --popover: 40 40% 98%;
  --popover-foreground: 24 30% 18%;

  --primary: 24 40% 39%;             /* #8A5A3B 焦糖棕（加深版，对比度 5.4:1） */
  --primary-foreground: 40 33% 96%;

  --secondary: 30 30% 88%;           /* #E9DED0 浅焦糖 */
  --secondary-foreground: 24 30% 18%;

  --muted: 35 25% 91%;               /* #EDE6DB 米灰 */
  --muted-foreground: 31 15% 40%;    /* #756656 铅笔灰（加深版，对比度 5.1:1） */

  --accent: 8 55% 45%;               /* #B0483A 锈红·强调（选中、高亮） */
  --accent-foreground: 40 33% 96%;

  --destructive: 0 60% 45%;          /* #B82E2E 深绯红·危险操作专用 */
  --destructive-foreground: 40 33% 96%;

  --success: 42 70% 35%;             /* #97721B 黄铜·成功 */
  --success-foreground: 40 33% 96%;

  --warning: 35 85% 40%;             /* #BE7B10 琥珀·警告 */
  --warning-foreground: 40 33% 96%;

  --info: 215 35% 42%;               /* #466591 墨蓝·信息 */
  --info-foreground: 40 33% 96%;

  --border: 30 20% 78%;              /* #D9CFC0 旧纸边缘 */
  --input: 30 20% 78%;
  --ring: 24 40% 39%;

  --radius: 0.5rem;
}
```

### 4.3 深色模式 CSS 变量

```css
.dark {
  --background: 24 20% 14%;          /* #2A211B 深夜木桌 */
  --foreground: 35 30% 92%;          /* #F2E9DE 奶油墨水 */

  --card: 24 18% 18%;                /* #332A23 深色纸张 */
  --card-foreground: 35 30% 92%;

  --popover: 24 18% 18%;
  --popover-foreground: 35 30% 92%;

  --primary: 25 40% 58%;             /* #C99B7A 浅焦糖 */
  --primary-foreground: 24 20% 14%;

  --secondary: 24 15% 28%;           /* #4A3E35 深棕 */
  --secondary-foreground: 35 30% 92%;

  --muted: 24 15% 24%;
  --muted-foreground: 30 12% 62%;    /* 次要文字提亮，保证对比度 */

  --accent: 8 50% 62%;               /* #DC8172 亮锈红·强调 */
  --accent-foreground: 24 20% 14%;

  --destructive: 0 55% 58%;          /* #D65A5A 亮绯红·危险 */
  --destructive-foreground: 24 20% 14%;

  --success: 42 60% 55%;             /* 亮黄铜 */
  --success-foreground: 24 20% 14%;

  --warning: 35 85% 60%;             /* 亮琥珀 */
  --warning-foreground: 24 20% 14%;

  --info: 215 40% 68%;               /* 亮墨蓝 */
  --info-foreground: 24 20% 14%;

  --border: 24 15% 30%;
  --input: 24 15% 30%;
  --ring: 25 40% 58%;
}
```

### 4.4 色彩使用规则

- **主按钮**：`bg-primary text-primary-foreground`，hover 时略微加深并移除上浮阴影，像印章压下。
- **次要/幽灵按钮**：`bg-secondary` 或透明 + `border`，hover 时背景变为纸张高光。
- **强调（accent）**：选中态、Tab 激活、高亮标签，锈红色。
- **危险（destructive）**：删除、不可逆操作专用，深绯红。**与 accent 严格区分**，同一界面中两种色不得表达相近含义。
- **状态色**：成功 `success`、警告 `warning`、信息 `info`，仅用于状态提示（Toast 侧条、校验信息、状态点），不用于装饰。
- **链接**：正文链接使用 `primary` 并带下划线；hover 时下划线变虚线。
- **禁用状态**：`muted-foreground` + `muted` 背景，无阴影、无 hover 反馈。
- **对比度验收**：所有文字/背景组合在实现后需实测对比度（推荐 WebAIM Contrast Checker），不得低于 4.5:1；大字号（≥18.66px 粗体或 ≥24px）不得低于 3:1。

---

## 5. 字体系统

### 5.1 字体家族

| 用途 | 字体 | 备注 |
|---|---|---|
| 大标题 / 品牌 | Source Serif 4 + 霞鹜文楷（LXGW WenKai） | 衬线气质，仅用于标题与登录页品牌 |
| 正文 / 表单 / 描述 | system-ui + Noto Sans SC（思源黑体） | 保证长文本可读性，不使用文楷或 Mono |
| 数字 / 日期 / 时间 / 代码 / 标签 | IBM Plex Mono | 等宽对齐，工具感 |

**原则**：手账感来自标题的衬线字体，正文必须让位于可读性。霞鹜文楷笔画细、字重单一，15px 正文下渲染发虚，严禁用于正文。

```css
:root {
  --font-serif: "Source Serif 4", "LXGW WenKai", "Noto Serif SC", serif;
  --font-sans: system-ui, -apple-system, "Segoe UI", "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
  --font-mono: "IBM Plex Mono", ui-monospace, "SF Mono", Consolas, monospace;
}
```

### 5.2 字号层级

| 级别 | 大小 | 字重 | 字体 | 用途 |
|---|---|---|---|---|
| 品牌标题 | 1.75rem / 28px | 700 | serif | 登录页标题、侧边栏品牌 |
| 页面标题 | 1.5rem / 24px | 700 | serif | 各模块标题 |
| 卡片标题 | 1.125rem / 18px | 600 | serif | 卡片/表格条目标题 |
| 正文 | 0.9375rem / 15px | 400 | sans | 段落、表单、描述 |
| 小字/标签 | 0.8125rem / 13px | 500 | mono | 标签、时间、辅助信息 |
| 微字 | 0.75rem / 12px | 500 | mono | 脚标、徽章 |

### 5.3 排版规则

- 标题字母间距略微收紧（`tracking-tight`），营造杂志感。
- 正文行高 1.65，段间距 1em。
- 数字、日期、时间一律使用 Mono，保证表格中纵向对齐。
- 中文段落使用左对齐，禁止两端对齐（避免字间距不均）。
- 中英文混排时，Mono 仅包裹数字/英文片段，中文部分仍用 sans。

---

## 6. 间距、圆角与阴影

### 6.1 间距系统

基础单位为 **4px**，采用 4 的倍数：

| Token | 值 |
|---|---|
| xs | 4px |
| sm | 8px |
| md | 16px |
| lg | 24px |
| xl | 32px |
| 2xl | 48px |

页面内边距：
- 内容区左右 `px-6 lg:px-10`（24px / 40px）
- 内容区上下 `py-6 lg:py-8`（24px / 32px）
- 卡片内部 `p-5`（20px）
- 表单字段间距 `gap-4`（16px）

### 6.2 圆角系统

| Token | 值 | 用途 |
|---|---|---|
| sm | 4px | 输入框、小标签、胶带徽章 |
| DEFAULT | 8px | 按钮、卡片 |
| lg | 12px | 大卡片、模态框 |
| xl | 16px | 登录面板、图片 |
| full | 9999px | 头像、状态点 |

手账纸张不使用大圆角；卡片圆角保持 8-12px，像裁切的纸页。

### 6.3 阴影系统

纸张质感的关键：**多层柔和外阴影 + 顶部内高光**。真实纸张的边缘是亮的，内高光比噪点更能出质感。

```css
--shadow-sm: 0 1px 2px hsl(24 20% 18% / 0.06),
             inset 0 1px 0 hsl(40 40% 100% / 0.6);   /* 卡片默认：外阴影 + 顶部纸边高光 */
--shadow-md: 0 3px 6px hsl(24 20% 18% / 0.08),
             0 1px 2px hsl(24 20% 18% / 0.04),
             inset 0 1px 0 hsl(40 40% 100% / 0.6);
--shadow-lg: 0 8px 20px hsl(24 20% 18% / 0.10),
             0 3px 6px hsl(24 20% 18% / 0.05),
             inset 0 1px 0 hsl(40 40% 100% / 0.5);
```

深色模式下内高光减弱为 `hsl(35 30% 92% / 0.06)`，外阴影不透明度提高至 0.3-0.4。

使用规则：
- 默认卡片使用 `--shadow-sm`。
- **仅可交互卡片**在 hover 时升级到 `--shadow-md` 并上浮 `translateY(-2px)`；纯展示卡片 hover 无位移。
- 弹窗/抽屉使用 `--shadow-lg`。
- 主按钮 hover 时使用 `inset 0 2px 4px hsl(24 30% 15% / 0.25)`，模拟按下。

---

## 7. 纹理与材质

### 7.1 纸张噪点

噪点只铺在**页面背景层**，不覆盖任何内容：

```css
body {
  background-color: hsl(var(--background));
  background-image: url("data:image/svg+xml,..."); /* 噪点 SVG，128px 平铺 */
  background-size: 128px 128px;
}
```

- 噪点位于背景层，**不使用伪元素全局覆盖**（避免压在文字、弹窗上导致小字号发虚）。
- 浅色模式：米白底上的灰褐噪点；深色模式：深棕底上的浅褐噪点。
- 噪点图本身透明度控制在视觉等效 opacity ≤ 0.04。
- 固定 128px 平铺尺寸，避免高分屏缩放出摩尔纹；上线前需在 1x/2x/3x 屏各验证一次。

### 7.2 横线/网格纸（可选，按页面启用）

- **表单页/编辑器**：输入区域背景使用淡横线，像笔记本内页。
- **表格页**：表头下方使用 2px 加粗下边框，像表格纸首行。
- **动态卡片流**：不使用横线，保持卡片干净。

### 7.3 胶带徽章（全站唯一装饰元素）

- 仅用于状态徽章（已发布/草稿/分类标签）。
- 实现：圆角 4px + 半透明白色高光条（`inset 0 1px 0 rgba(255,255,255,0.35)`）+ 边缘轻微不规则（SVG mask，提供 2-3 种固定形状轮换，禁止随机生成）。
- **不旋转**。信息载体一律保持水平，保证可读与对齐。
- 除胶带徽章外，不使用图钉、印章、手绘涂鸦等其他装饰。登录页除外（见 10.1）。

---

## 8. 布局架构

### 8.1 整体骨架

侧边栏全高固定，顶部栏从内容区上方开始，不覆盖侧边栏：

```
┌──────────────┬───────────────────────────────────┐
│              │  顶部栏 (64px, sticky top)        │
│  侧边栏       ├───────────────────────────────────┤
│  (fixed,     │                                   │
│   240px,     │         页面内容区                 │
│   100vh)     │      (padding 24-40px)            │
│              │                                   │
│  · 品牌       │                                   │
│  · 导航       │                                   │
│  · 主题切换   │                                   │
│  · 退出登录   │                                   │
└──────────────┴───────────────────────────────────┘
```

### 8.2 侧边栏

- 宽度：240px，`fixed` 定位，高度 100vh，z-index 40。
- 背景：比页面背景深一度的纸张色（`secondary`），右侧 1px `border` 细边框。
- 顶部：品牌区（Lucide `compass` 图标 + "小屋日志"衬线标题）。
- 导航：一级菜单项，图标 + 文字；hover 时背景 `muted`；当前激活项背景 `muted` + 文字 `primary` + 左侧 3px `primary` 竖线。
- 底部：主题切换按钮 + 退出登录。

### 8.3 顶部栏

- 高度：64px，`sticky top-0`，位于侧边栏右侧（`left: 240px` 起），z-index 30。
- 背景：`background` + 底部 1px `border`，滚动时下边缘出现 `shadow-sm`。
- 左侧：页面标题（衬线字体）+ 面包屑（可选）。
- 右侧：快速操作入口（新建文章、发布动态）+ 管理员头像/首字母。

### 8.4 内容区（宽度分级）

| 级别 | 最大宽度 | 适用页面 |
|---|---|---|
| 宽页 | 1400px | 仪表盘、文章列表、动态卡片流、学习记录、课程表、项目卡片流 |
| 窄页 | 680px | 所有单栏表单页（发布动态、新增/编辑记录、课程、状态、个人资料、项目表单）、文章详情 |

- 窄页内容左对齐（不居中），与宽页左缘对齐，保持视觉锚点一致。
- 页面标题与内容间距 24px；内容卡片之间间距 16-24px。

### 8.5 路由结构

| 路径 | 页面 | 宽度 | 说明 |
|---|---|---|---|
| `/login` | 登录页 | 全屏 | 暗号登录 |
| `/` | 仪表盘 | 宽 | 数据概览与快捷入口 |
| `/articles` | 文章列表 | 宽 | 表格 + 筛选 |
| `/articles/new` | 新建文章 | 宽 | Markdown 双栏编辑器 |
| `/articles/:id` | 文章详情 | 窄 | 只读查看 |
| `/moments` | 动态管理 | 宽 | 卡片流 |
| `/moments/new` | 发布动态 | 窄 | 表单 |
| `/study-records` | 学习记录 | 宽 | 表格 + 日期筛选 |
| `/study-records/new` | 新增学习记录 | 窄 | 表单 |
| `/study-records/:id/edit` | 编辑学习记录 | 窄 | 表单 |
| `/courses` | 课程表 | 宽 | 表格 |
| `/courses/new` | 新增课程 | 窄 | 表单 |
| `/courses/:id/edit` | 编辑课程 | 窄 | 表单 |
| `/status` | 个人状态 | 窄 | 表单 |
| `/about/profile` | 个人资料 | 窄 | 表单 |
| `/about/projects` | GitHub 项目 | 宽 | 卡片流 |
| `/about/projects/new` | 新增项目 | 窄 | 表单 |
| `/about/projects/:id/edit` | 编辑项目 | 窄 | 表单 |

---

## 9. shadcn-vue 组件改造规范

所有 shadcn-vue 组件安装后，都需要在样式层做主题覆盖，以匹配手账气质。

### 9.1 Button

- 默认按钮：圆角 8px，字体 sans，字重 500，带 `shadow-sm`。
- 主按钮：hover 时阴影向内收缩（`inset 0 2px 4px`），模拟按下。
- 次要按钮：透明底 + 边框，hover 时填充 `secondary`。
- 危险按钮：`destructive` 色，hover 加深。
- 幽灵按钮：无边框，hover 背景 `muted`。
- 禁用：`muted` 背景 + `muted-foreground`，无阴影，`cursor: not-allowed`。

### 9.2 Card

- 默认圆角 12px，背景 `card`，`shadow-sm`（含顶部内高光）。
- **可交互卡片**（点击跳转）：hover 上浮 2px + `shadow-md`，`cursor: pointer`。
- **展示卡片**：hover 无位移、无阴影变化。
- 不使用旋转、胶带条等装饰。

### 9.3 Input / Textarea

- 背景 `card`，边框 `border`，圆角 8px。
- focus-visible：边框 `primary` + 外圈 `ring` 2px `ring/30`。
- 校验错误：边框 `destructive` + 下方 13px 错误文案（`destructive`，sans）。
- Textarea 最小高度 120px，行高 1.65。
- 编辑器内页变体：背景加淡横线纹理。

### 9.4 Table

- 表头：背景 `secondary`，文字 `foreground`，下边框 2px `border`，字号 13px Mono 大写感。
- 行 hover：背景 `muted/50`（仅当行可点击时同时显示 `cursor: pointer`）。
- 单元格 padding：12px 16px。
- 数字/时间列使用 Mono，右对齐或左对齐全列统一。
- loading：骨架屏（3-5 行 `muted` 色呼吸色块，形状与真实行高一致）。
- 空状态：Lucide 线性图标（如 `file-text`）+ 一行说明文字 + 主操作按钮，不使用插画。

### 9.5 Badge（胶带徽章）

- 圆角 4px，带顶部半透明高光条，边缘轻微不规则（2-3 种固定 SVG mask 轮换）。
- 已发布：`primary` 底色。
- 草稿：`muted` 底色 + 虚线边框。
- 分类：`secondary` 底色。
- 不旋转。

### 9.6 Dialog / Sheet

- 圆角 16px，`shadow-lg`。
- 标题用衬线字体。
- 关闭按钮使用 Lucide `x` 图标，hover 背景 `muted`。
- 危险操作确认 Dialog：主操作按钮用 `destructive`，文案明确说明后果（如"删除后不可恢复"）。

### 9.7 Tabs

- 底部对齐，激活项有 2px `primary` 下划线 + 文字 `primary`。
- 用于文章分类筛选（技术 / 生活）。

### 9.8 Calendar / DatePicker

- 使用 shadcn-vue Calendar，覆盖颜色为纸张色系。
- 选中日期用 `primary` 圆形背景。
- 时间输入单独使用 `<input type="time">` 并统一样式。

### 9.9 Select / Combobox

- 触发器样式与 Input 一致。
- 下拉列表背景 `popover`，带 `shadow-md`。

### 9.10 Avatar

- 圆形，边框 2px `border`。
- 无头像时显示姓名首字母，背景 `primary`，文字 `primary-foreground`。

### 9.11 Toast

- 位置：右上角，垂直堆叠，最多同时 3 条，超出排队。
- 样式：`card` 背景 + `shadow-md` + 左侧 3px 状态色条（成功 `success` / 错误 `destructive` / 信息 `info`）。
- 时长：成功 3s，错误 5s，带操作按钮的 Toast 不自动消失。
- 不使用印章、旋转等装饰。

### 9.12 骨架屏 / Loading

- 表格：3-5 行骨架，行高与真实行一致。
- 卡片流：2-3 个骨架卡片，结构与真实卡片一致。
- 表单：字段轮廓骨架。
- 动画：`muted` 色块呼吸（opacity 0.6 ↔ 1，1.5s 循环）；`prefers-reduced-motion` 下改为静态。

---

## 10. 各页面详细设计

### 10.1 登录页 `/login`

**布局**：全屏居中，背景为深色皮革质感桌面（CSS 渐变 + 噪点，不用图片）。

**主体**：
- 中央一张"笔记本封面"卡片，圆角 16px，`shadow-lg`，宽 400px。
- 顶部：Lucide `compass` 图标 + 标题"小屋日志"（衬线）+ 副标题"管理员登录"。
- 表单：暗号输入框（password 类型）+ "登录"主按钮。
- 底部：小字版权信息。
- 登录页是全站唯一允许额外装饰的场景：卡片封面可压印一枚罗盘线稿（SVG，opacity 0.1），不使用其他装饰。

**交互**：
- 输入暗号后按回车或点击按钮提交。
- 提交中：按钮进入 loading 态（spinner + "登录中…"）。
- 错误：输入框边框变 `destructive`，下方提示"暗号错误"；卡片不晃动。
- 限流 429：按钮禁用并显示倒计时。
- 成功：直接跳转 `/`，页面级 fade-in，不做"翻开"动画。

**API**：`POST /api/admin/auth`

---

### 10.2 仪表盘 `/`

**布局**：数据统计卡片 + 快捷操作 + 最近内容概览。

**内容**：
- 统计卡片区（4 张）：文章总数、动态总数、学习记录总数、本周学习时长。数字使用 Mono 大字号，标签使用 13px `muted-foreground`。
- 快捷操作区（4 个可交互卡片入口）：写新文章、发布动态、添加学习记录、更新个人状态。
- 最近文章：5 条文章列表（表格）。
- 最近动态：3 条动态卡片。

**交互**：
- 快捷操作卡片 hover 上浮 + `shadow-md`（可交互卡片规则）。
- 统计卡片为展示卡片，hover 无位移。
- 点击跳转对应页面。

---

### 10.3 文章列表 `/articles`

**布局**：页面标题 + 筛选栏 + 数据表格 + 分页。

**筛选栏**：
- 分类 Tab：`全部` / `技术博客` / `生活文章`。
- 状态 Tab：`全部` / `已发布` / `草稿`。
- 新建文章按钮（右上角）。

**表格列**（精简为 5 列）：
- 标题（衬线字体，点击进详情）
- 分类（Badge）
- 状态（Badge：已发布/草稿）
- 发布时间（Mono）
- 操作（查看、删除）

**交互**：
- 点击标题进入文章详情。
- 删除需要二次确认 Dialog（`destructive` 按钮 + "删除后不可恢复"提示）。
- 分页使用 shadcn-vue Pagination。
- loading 显示表格骨架屏；空状态见 9.4。

**API**：`GET /api/admin/articles`

---

### 10.4 新建文章 `/articles/new`

**布局**：双栏编辑器（左编辑 / 右预览），顶部为标题输入与元信息。

**顶部表单**：
- 标题输入（大字号衬线，无边框显示，像纸页上的标题）。
- 分类 Select：`技术博客` / `生活文章`。
- 标签输入（逗号分隔，显示为 Badge 列表）。
- 摘要 Textarea。
- 封面图上传（拖拽/点击上传，预览缩略图，带上传进度条与失败重试按钮）。
- 状态选择：`保存草稿` / `直接发布`。

**编辑器区**：
- 左侧：Markdown Textarea，Mono 字体 15px，淡横线背景。
- 右侧：实时 Markdown 渲染预览（markdown-it），正文 sans。
- 中间可拖动分隔条，分栏比例持久化到 localStorage。
- 底部状态条：实时字数统计（Mono 13px）。

**底部**：
- 取消按钮、保存草稿按钮、发布按钮。

**交互**：
- `Ctrl/Cmd + S` 触发保存草稿。
- Markdown 导入：选择 `.md` / `.markdown` 文件后，后端返回 UTF-8 正文并替换编辑区内容，右侧预览同步自动刷新；已有正文时先确认。
- 图片上传插入：在 textarea 当前光标处插入 `![描述](url)`。
- 发布前校验：标题必填，未填时标题输入框显示错误态。
- 成功提示：Toast（成功色条，文案"文章已发布" / "草稿已保存"）。
- 离开页面前有未保存内容时，弹出确认 Dialog。

**API**：`POST /api/admin/articles`、`POST /api/admin/upload/markdown`

---

### 10.5 文章详情 `/articles/:id`

**布局**：窄页，只读展示卡片。

**内容**：
- 顶部：标题 + 分类/状态 Badge + 操作（删除）。
- 元信息：发布时间、阅读数、预计阅读时长、标签（Mono 小字）。
- 封面图（如有）。
- 正文 Markdown 渲染。

**交互**：
- 删除二次确认。
- 返回列表。

**API**：`GET /api/admin/articles/{id}` / `DELETE /api/admin/articles/{id}`

---

### 10.6 动态管理 `/moments`

**布局**：页面标题 + 新建按钮 + 卡片流网格。

**卡片设计**（便签纸风格）：
- 卡片结构：内容文字 → 图片区（1-9 张图网格，最多 3 列）→ 底部发布时间（Mono）+ 删除按钮。
- 无图钉、无旋转；卡片以纸张阴影区分层级。

**交互**：
- 卡片为展示卡片，hover 无位移；删除按钮 hover 有自身反馈。
- 删除二次确认。
- 上拉/点击加载更多分页。

**API**：`GET /api/admin/moments`

---

### 10.7 发布动态 `/moments/new`

**布局**：窄页单栏表单。

**表单**：
- 内容 Textarea。
- 媒体类型 Switch：`文字` / `图片`。
- 图片上传区（多图拖拽上传，可排序、删除；每张图带上传进度条，失败显示重试按钮）。

**交互**：
- 切换为文字时隐藏图片区。
- 上传图片后立即显示缩略图。
- 提交成功后跳转 `/moments`。

**API**：`POST /api/admin/moments`

---

### 10.8 学习记录管理 `/study-records`

**布局**：页面标题 + 日期范围筛选 + 表格 + 分页。

**筛选栏**：
- 开始日期 DatePicker。
- 结束日期 DatePicker。
- 新增按钮。

**表格列**：
- 日期（Mono）
- 主题
- 学习时长（Mono）
- 创建时间（Mono）
- 操作（编辑、删除）

**API**：`GET /api/admin/study-records`

---

### 10.9 新增/编辑学习记录

**布局**：窄页单栏表单。

**表单字段**：
- 学习日期 DatePicker
- 主题 Input
- 学习内容 Textarea
- 学习时长 Input（分钟，number）

**交互**：
- 编辑页进入时回填数据（`GET /api/admin/study-records/{id}`），回填期间显示表单骨架屏。
- 提交成功后返回列表。

**API**：`POST /api/admin/study-records` / `PUT /api/admin/study-records/{id}`

---

### 10.10 课程表管理 `/courses`

**布局**：页面标题 + 表格 + 分页。

**表格列**：
- 课程名称
- 日期范围（startDate ~ endDate，Mono）
- 时间（startTime ~ endTime，Mono）
- 地点
- 重复模式（仅当天 / 每周 X）
- 操作（编辑、删除）

**API**：`GET /api/admin/courses`

---

### 10.11 新增/编辑课程

**布局**：窄页单栏表单。

**表单字段**：
- 课程名称 Input
- 开始日期 DatePicker
- 结束日期 DatePicker
- 上课时间 `<input type="time">`
- 下课时间 `<input type="time">`
- 地点 Input
- 重复模式 Select：`仅当天` / `每周重复`
- 星期 Select（1-7），仅每周重复时显示

**交互**：
- 切换"仅当天"时，自动将 endDate 设为 startDate，隐藏星期选择。
- 切换"每周重复"时，显示星期选择并校验必填。

**API**：`POST /api/admin/courses` / `PUT /api/admin/courses/{id}`

---

### 10.12 个人状态 `/status`

**布局**：窄页单栏表单卡片。

**表单字段**：
- 当前状态 Select：`在线` / `学习中` / `忙碌` / `休息`
- 当前正在做的事 Input
- 心情签名 Input

**交互**：
- 所有字段可选，未修改不提交。
- 保存成功后显示 Toast。

**API**：`PUT /api/admin/status`

---

### 10.13 关于页 · 个人资料 `/about/profile`

**布局**：窄页单栏表单卡片。

**表单字段**：
- 姓名 Input
- 头像 URL Input（仅 HTTPS 外部地址）
- 头像预览（右侧小图，加载失败显示占位图标）

**交互**：
- 进入页面时自动 GET 当前资料回填。
- 保存为覆盖语义；空字符串表示清空。

**API**：`GET /api/admin/about/profile` / `PUT /api/admin/about/profile`

---

### 10.14 GitHub 项目管理 `/about/projects`

**布局**：页面标题 + 新建按钮 + 卡片流网格。

**卡片内容**：
- 项目名称（衬线标题）
- 简介
- 技术标签（Badge 列表）
- GitHub 链接（Lucide `external-link` 图标）
- 排序值（Mono）
- 操作（编辑、删除）

**交互**：
- 点击 GitHub 链接新标签页打开。
- 删除二次确认。

**API**：`GET /api/admin/about/projects`

---

### 10.15 新增/编辑 GitHub 项目

**布局**：窄页单栏表单。

**表单字段**：
- 项目名称 Input
- 简介 Textarea
- GitHub 地址 Input（校验 HTTPS + github.com 域名，错误时显示校验错误态）
- 技术标签 Tag 输入（可增删，最多 10 个）
- 排序值 Input（number，≥0）

**API**：`POST /api/admin/about/projects` / `PUT /api/admin/about/projects/{id}`

---

## 11. 交互与动效

### 11.1 动效 Token

```css
:root {
  --duration-fast: 150ms;    /* hover、focus 反馈 */
  --duration-base: 250ms;    /* 卡片位移、展开收起 */
  --duration-slow: 400ms;    /* 页面进入、主题切换 */
  --ease-out: cubic-bezier(0.22, 1, 0.36, 1);      /* 默认出场 */
  --ease-in-out: cubic-bezier(0.65, 0, 0.35, 1);   /* 状态切换 */
}
```

所有过渡必须引用 token，禁止散写时长。

### 11.2 全局动效

- **主题切换**：颜色过渡 `duration-slow`。
- **页面进入**：内容区 staggered fade-in + translateY(8px → 0)，`duration-base`，`ease-out`，逐项延迟 40ms。
- **减少动态**：`@media (prefers-reduced-motion: reduce)` 下，关闭所有位移、缩放、呼吸动画，仅保留透明度渐变（≤150ms）。

### 11.3 微交互

- 按钮 hover：背景加深 + 阴影变化（`duration-fast`）；主按钮 hover 用 inset 阴影模拟按下。
- 可交互卡片 hover：`translateY(-2px)` + `shadow-md`。
- 展示卡片 hover：无反馈。
- 输入框 focus-visible：边框 `primary` + ring 扩散。
- 图片上传：拖拽进入区域时，边框变为 `primary` 虚线并高亮。
- 删除按钮 hover：`destructive` 色加深；不使用 shake 动画。

### 11.4 焦点与键盘

- 所有可交互元素必须有可见的 focus-visible 样式：2px `ring` + 2px 偏移。
- 不用 `:focus` 替代 `:focus-visible`，避免鼠标点击出现焦点环。
- Dialog 打开时焦点进入第一个输入框，关闭时焦点归还触发元素；Esc 关闭。

### 11.5 Toast / 通知

见 9.11。

---

## 12. z-index 分层

| 层级 | 值 | 内容 |
|---|---|---|
| 背景纹理 | 0 | 噪点（在 body 背景层，不占 z-index 语义层） |
| 内容 | 1 | 常规页面内容 |
| 顶部栏 | 30 | sticky top bar |
| 侧边栏 | 40 | fixed sidebar |
| 抽屉/Sheet | 50 | 移动端导航抽屉 |
| Dialog | 60 | 模态框（含遮罩 59） |
| Toast | 70 | 通知 |

新增浮层必须先查本表，禁止出现 `z-index: 9999`。

---

## 13. 主题切换

- 切换按钮位于侧边栏底部，使用 Lucide `sun` / `moon` 图标。
- 主题状态持久化到 localStorage。
- 首次进入时跟随系统偏好；默认浅色。
- 切换时同步切换 shadcn-vue 的 `.dark` class 与自定义 CSS 变量，过渡 `duration-slow`。

---

## 14. 响应式策略

网页管理端主要面向桌面使用，但需保证基础可用性：

| 断点 | 策略 |
|---|---|
| ≥1280px | 完整布局，侧边栏 240px，宽页最大 1400px |
| 1024-1279px | 侧边栏保持 220px，内容区 padding 缩小 |
| 768-1023px | 侧边栏折叠为图标栏（hover/点击展开），内容区全宽 |
| <768px | 侧边栏变为抽屉导航；表格横向滚动；编辑器改为上下分栏 |

**最小支持宽度**：360px，以手机浏览器应急查看。窄页在小屏下退化为全宽 - 32px padding。

---

## 15. 依赖清单

| 依赖 | 用途 |
|---|---|
| `shadcn-vue` | 基础 UI 组件 |
| `lucide-vue-next` | 图标库（全站统一） |
| `@vueuse/core` | 工具 hook（深色模式、本地存储等） |
| `vue-router` | 路由 |
| `pinia` | 状态管理 |
| `axios` | HTTP 请求 |
| `markdown-it` | Markdown 渲染 |
| `@fontsource/source-serif-4` | 标题衬线字体 |
| `@fontsource/ibm-plex-mono` | 数字/标签等宽字体 |
| `lxgw-wenkai-webfont` 或 CDN | 中文标题字体（霞鹜文楷，仅标题使用，按需子集化） |

---

## 16. 待确认与待补充

| # | 问题 | 说明 |
|---|---|---|
| 1 | 霞鹜文楷子集化方案 | 仅标题使用，建议按实际标题用字子集化，或退化为 Noto Serif SC |
| 2 | 登录页背景 | 当前定为 CSS 渐变 + 噪点 + 罗盘线稿压印，不使用图片素材 |
| 3 | 文章编辑（修改已发布/草稿继续编辑）是否在本期实现？ | 后端当前无 PUT 接口，需先确认后端是否扩展 |
| 4 | 仪表盘统计数据接口 | 统计卡片需要后端提供聚合接口，需确认 |
| 5 | 封面图/动态图片上传接口与存储方案 | 需确认后端上传接口、大小限制、返回格式 |

---

## 17. 文件输出

本文档生成后，下一步建议产出：

1. `web-admin/` 项目骨架（Vite + Vue 3 + shadcn-vue 初始化）。
2. 全局样式文件（CSS 变量、字体、纹理、动效 token）。
3. 路由与布局组件（`AppLayout`、`Sidebar`、`TopBar`）。
4. 登录页实现。
5. 文章管理模块（列表 + 新建 + 详情）。
6. 其他模块按优先级逐个实现。
