# 快速开始

新接触 AXS 的服主请按下面顺序读完本章 — 整个过程大约 **15 分钟**就能让一个模块跑起来。

## 路线图

1. [安装](installation) — 把 jar 丢进 `plugins/`，确认依赖、Java、MC 版本无误。
2. [模块授权门控](module-passwords) — 理解 `modules.<module>.enabled` 与 license.yml 授权。
3. [第一次启用流程](first-run) — 推荐的"先开 1 个最小模块 → 全开"流程。
4. [命令速查](commands) — 管理命令（`/AXS`）与玩家命令一表打尽。
5. [PlaceholderAPI 速查](placeholders) — 各模块的 PAPI 前缀与典型字段。
6. [跨服功能配置](cross-server-setup) — 多服 Chat / Tab / Mail 等（`1.2.0-beta` 统一 CrossServer SDK）。

## 一句话总览

```
ArcartX 客户端 MOD  ──────  网络包 ──────  AXS 服务端 jar
     ↑ 渲染 UI / HUD                           ↑ 业务逻辑 / 数据库 / 桥接
     │                                          │
     └────── plugins/ArcartXSuite/ ─────────────┘
              ├── config.yml          总开关 + 模块授权 + cross-server 跨服
              ├── ArcartX*.yml        各模块配置
              ├── ui/                 ArcartX UI 模板
              ├── chat/, mail/, ...   模块子资源
              └── *.db                持久化数据
```

::: tip 顺序很重要
**先安装 ArcartX 客户端 MOD，再装 AXS 服务端**。AXS 在 `plugin.yml` 中 `depend: ArcartX`，服务端缺少 ArcartX 时 AXS **不会启动**。
:::
