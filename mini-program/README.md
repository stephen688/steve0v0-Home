# steve0v0 小屋 · 原生微信小程序用户端

## 打开方式

1. 推荐在微信开发者工具中导入 `mini-program/`，项目配置会自动把小程序根目录指向 `dist/`；也可以直接导入已经构建好的 `dist/`。
2. 开发期使用 `project.config.json` 中的 `touristappid`，并保持开发者工具的“不校验合法域名”开启。
3. 本地 Spring Boot 默认地址为 `http://127.0.0.1:8080`，可在 `config/env.ts` 修改后重新构建。

## 构建与检查

```powershell
cd mini-program
npm run typecheck
npm run build
```

构建产物位于 `mini-program/dist/`，其中包含微信开发者工具所需的 `app.json`、页面、组件、脚本、样式和 TabBar 图标。

## 发布前配置

- 将 `config/env.ts` 的 API 地址替换为 HTTPS 公网地址。
- 将 `project.config.json` 的 AppID 替换为真实 AppID。
- 在微信公众平台配置 request、image 和 download 合法域名。
- 在 `config/site.ts` 补充真实个人简介、职业、技能、时间线、联系方式，并把正式动态页头图地址填入 `heroImage`。
- 动态页当前使用纯深蓝占位区，收到正式头图后只替换 `heroImage` 配置，不改变页面结构。

## 当前联调状态

本地后端已加载第三阶段关于页接口。`GET /api/about/profile` 和 `GET /api/about/projects` 均已验证返回 `HTTP 200 / code=200`；个人资料姓名为 `steve0v0`，头像暂时为空，项目列表暂时为空。
