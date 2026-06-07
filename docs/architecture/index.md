# 架构

AXS 共享**同一组反射桥、同一套客户端包守卫、同一种资源加密协议、同一份数据包流向约定**。

## 一图概览

```
┌────────────────────────────────────────────────────────────────┐
│                       ArcartXSuite                             │
│                                                                │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌────────────┐  │
│  │ Bridge   │   │ Security │   │ Config   │   │ Combat /   │  │
│  │ (反射桥) │   │ (Guard,  │   │ (.axb +  │   │ Util       │  │
│  │          │   │  Pwd)    │   │  Sync)   │   │            │  │
│  └────┬─────┘   └────┬─────┘   └────┬─────┘   └─────┬──────┘  │
│       │              │              │               │          │
│  ┌────▼──────────────▼──────────────▼───────────────▼─────┐    │
│  │            21 个 Module (config / service /             │    │
│  │            listener / placeholder / command)            │    │
│  └────┬───────────────────────────────────────────────┬───┘    │
│       │  sendPacket(player, uiId, handler, payload)   │        │
└───────┼───────────────────────────────────────────────┼────────┘
        │                                               │
   ┌────▼─────┐                                    ┌────▼─────┐
   │ ArcartX  │ ──────── WebSocket ─────────────── │ 客户端   │
   │ 服务端   │  ◄ Packet.send(packetId, action) ─ │ MOD      │
   └──────────┘                                    └──────────┘
```

## 模块化

- [Modular — 宿主 + 模块 Jar 架构](modular)：模块加载、重载、UI 注册、开发指南

## 四个共享层

- [Bridge — 反射桥](bridges)：全部通过反射 + 类名探测访问第三方 API
- [CrossServer — 统一跨服 SDK](cross-server)：`1.2.0-beta` 起全模块共用 Redis/Proxy 双后端与 JSON 信封
- [Security — ClientPacketGuard + 模块授权](security)：速率限制 + 授权门控
- [Protected Resources — `.axb` 加密资源](protected-resources)：YAML 加密打包协议
- [Packet Flow — init/update/close 协议](packet-flow)：UI 数据包五段式生命周期

## 开发者 API

1.1.0 起提供独立的 `axs-api` 模块作为第三方开发的稳定接口层，详见 [API 参考](/api/)。

- [模块生命周期 — AXSModule / AbstractAXSModule / ModuleDescriptor](/api/module-lifecycle)
- [ModuleContext 上下文 — 桥接、事件、命令、资源导出](/api/module-context)
- [桥接 API — PacketBridgeAPI / ClientBridgeAPI / ItemBridgeAPI](/api/bridge-api)
- [事件 — ModuleLifecycleEvent](/api/events)
- [Capability 跨模块通信](/api/capability)

## 数据库

AXS 用 **HikariCP + SQLite/MySQL 共存**：

- 默认 `mode: sqlite`，文件位于 `plugins/ArcartXSuite/<module>.db`
- 改 `mode: mysql` 后填连接信息即可切换
- 所有模块用各自独立的连接池

涉及持久化的模块：`title` / `mail` / `chat` / `onlinerewards` / `loginview` / `map` / `warehouse`
