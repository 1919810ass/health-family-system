## 问题定位

* 根路径 `'/'` 的子路由空路径被重定向到 `'/home'`，导致首次访问 `http://localhost:5173/` 自动跳 `'/home'`（src/router/index.js:17–18）。

* 全局路由守卫仅在“未登录访问受保护路由”时跳到 `'/login'`，未处理“已登录访问 `'/login'`”的情况（src/router/index.js:86–94）。

* `Home` 视图本身有内容并非空组件，Layout 内存在 `<router-view />`（src/components/Layout/AppLayout.vue:62；src/views/Home/index.vue:3,5,23）。

* Home 空白更可能是运行时错误阻断渲染，例如 `src/stores/app.js` 顶部使用了 `ref` 却未导入（src/stores/app.js:4）。

* 入口与挂载正常（src/main.js:10–12；src/App.vue:2）；Token 读取统一为 `access_token`（src/utils/auth.js:1）。

## 修改方案

1. 将根子路由的空路径重定向目标从 `'/home'` 改为 `'/login'`（src/router/index.js:17–18）。
2. 在 `beforeEach` 增加逻辑：若已登录且访问 `'/login'`，则 `next('/home')`。同时统一用 `getToken()` 读取 Token（src/router/index.js:86–94；src/utils/auth.js:1）。
3. 修复可能导致空白的运行时错误：在 `src/stores/app.js` 顶部补充 `import { ref } from 'vue'`，确保使用的响应式 API 已导入（src/stores/app.js:1–4）。
4. 为关键路由补充 `name` 字段（`login`、`home`），便于后续基于路由名的导航与权限控制（src/router/index.js:5–11, 21–26）。

## 验证步骤

* 清理浏览器 `localStorage` 中的 `access_token`，访问 `http://localhost:5173/` 应直接进入 `'/login'`。

* 设置一个模拟 Token（例如通过登录或手动设置），访问 `'/login'` 应自动跳到 `'/home'`。

* 进入 `'/home'` 应正确渲染卡片网格；如果仍为空白，打开控制台查看是否还有未导入的 API 或其他报错，逐项修复。

## 影响评估

* 更改仅影响初始导航与登录页访问逻辑，不改变业务路由结构；修复导入后可避免页面运行时崩溃导致的空白。

## 回滚说明

* 如需回滚默认入口，可将空路径重定向改回 `'/home'`；删除新增的登录页跳转逻辑即可恢复原守卫行为。

