---
title: index | ArcartX-Suite Minecraft插件文档
description: index - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 快速开始

新接触 ArcartX-Suite 的服主请按下面顺序读完本章 — 整个过程大约 **15 分钟**就能让一个模块跑起来。

## 路线图

1. [安装](installation) — 把 jar 丢进 `plugins/`，确认依赖、Java、MC 版本无误。
2. [模块启用](module-enablement) — 理解 `modules.<module>.enabled` 模块开关。
3. [第一次启用流程](first-run) — 推荐的"先开 1 个最小模块 → 全开"流程。
4. [命令速查](commands) — 管理命令（`/AXS`）与玩家命令一表打尽。
5. [PlaceholderAPI 速查](placeholders) — 各模块的 PAPI 前缀与典型字段。
6. [条件系统（PAPI + Aria + JS）](conditions) — Menu / Prop / EventPacket / Mail 统一条件语法，支持 PAPI 行内、Aria 脚本、原生 JavaScript 三种模式。
7. [跨服功能配置](cross-server-setup) — 多服 Chat / Tab / Mail 等（`1.2.0-beta` 统一 CrossServer SDK）。
8. [Warehouse 多服 MySQL 部署](warehouse-cross-server) — 仓库跨服数据共享。
9. [多 UI 同时发包](multi-ui) — 多界面并存时的包路由。
10. [Proxy 代理端插件](proxy-usage) — 群组服代理端装哪个 jar、BC/VC 目录示例、与跨服 SDK 的区别。

## 开发者

若要 **自己开发第三方模块** 或 **通过 Capability 调用官方模块**，请阅读 [开发者指南](./developer/)：

- [开发第三方模块](./developer/module-development) — 从零搭建 Gradle 工程到部署
- [使用第三方模块](./developer/using-third-party-modules) — 服主安装他人模块 Jar
- [Capability 详解](./developer/capability-guide) — 跨模块通信的开发与使用（AXS 核心联动机制）

## 一句话总览

```
ArcartX 客户端 MOD  ──────  网络包 ──────  ArcartX-Suite 服务端 jar
     ↑ 渲染 UI / HUD                           ↑ 业务逻辑 / 数据库 / 桥接
     │                                          │
     └────── plugins/ArcartX-Suite/ ─────────────┘
              ├── config.yml          总开关 + cross-server 跨服
              ├── ArcartX*.yml        各模块配置
              ├── ui/                 ArcartX UI 模板
              ├── chat/, mail/, ...   模块子资源
              └── *.db                持久化数据
```

::: tip 顺序很重要
**先安装 ArcartX 客户端 MOD，再装 ArcartX-Suite 服务端**。ArcartX-Suite 在 `plugin.yml` 中 `depend: ArcartX`，服务端缺少 ArcartX 时 ArcartX-Suite **不会启动**。
:::

