---
title: 跨服通信（CrossServer SDK） | ArcartX-Suite Minecraft插件架构文档
description: 跨服通信（CrossServer SDK） - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 跨服通信（CrossServer SDK）

ArcartX-Suite 将跨服能力收敛为**宿主侧统一 SDK**，各模块不再各自维护 Redis Pub/Sub 或 BungeeCord 专用频道。

## 设计原则

| 原则 | 说明 |
| --- | --- |
| **子服侧总线** | 跨服逻辑在子服 `ArcartX-Suite-core` 的 `CrossServerService` 中实现，模块只注册通道与业务 payload |
| **统一信封** | 所有模块共用 JSON `CrossServerEnvelope`（HMAC 可选），不再各模块自定义 wire 格式 |
| **双后端** | Redis Pub/Sub 与 BungeeCord/Velocity `Forward` 可同时启用，出站双发、入站去重 |
| **Proxy 非总线** | `ArcartXSuite-Proxy` 负责认证路由，**不是**跨服消息总线；跨服消息走子服 SDK |

## 架构示意

```
┌────────────────────────────────────────────────────────────────────────────┐
│  Chat / Tab / Mail / Market / Announcer / OnlineRewards / EntityTracker …  │
│       openChannel("chat", config, consumer)                                │
└────────────────────────────┬───────────────────────────────────────────────┘
                             │ 模块 payload（字符串/JSON/YAML）
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│  CrossServerService（ArcartX-Suite-core）                                 │
│  · 封装 CrossServerEnvelope（module, nodeId, messageId, sign）   │
│  · 出站：Redis PUBLISH + Proxy Forward（可选）                   │
│  · 入站：验签 → message-id 去重 → 跳过本 node → 按 module 分发    │
└──────────────┬──────────────────────────────┬───────────────────┘
               │                              │
        Redis  │ ArcartX-Suite:CROSS                    │ BungeeCord Forward
               │                              │ AXS_CROSS
               ▼                              ▼
         其他子服 CrossServerService
```

## 配置分层

### 宿主 `config.yml` — 连接与全局策略

所有子服**共用同一套连接参数**（Redis 地址、Proxy 频道、签名密钥等），每台子服 `node-id` **必须不同**。

```yaml
cross-server:
  node-id: "lobby-1"          # 每台子服唯一，用于去重与来源识别
  dedupe-ttl-ms: 60000        # 入站 message-id 去重窗口
  max-payload-chars: 524288   # 单条 payload 上限（字符）
  redis:
    enabled: true
    host: "192.168.1.100"
    port: 6379
    password: ""
    database: 0
    channel: "ArcartX-Suite:CROSS"      # 全模块共用频道
    connect-timeout-ms: 5000
  proxy:
    enabled: false            # 可与 Redis 同时 true
    messenger-channel: "AXS_CROSS"
    forward-target: "ALL"
  signature:
    enabled: true
    secret: "your-shared-secret"
    verify: true
```

| 字段 | 说明 |
| --- | --- |
| `node-id` | 当前子服节点 ID。留空时回退为 Bukkit 服务器名 |
| `dedupe-ttl-ms` | 同一 `messageId` 在此窗口内只处理一次（Redis + Proxy 双通道防重复） |
| `max-payload-chars` | 超过上限的 payload 会被拒绝；Tab 大快照建议依赖 Redis |
| `redis.channel` | 默认 `ArcartX-Suite:CROSS`，**所有子服、所有模块**共用 |
| `proxy.messenger-channel` | 默认 `AXS_CROSS` |
| `signature` | 启用后出站 HMAC-SHA256；`verify: true` 时入站必须验签通过 |

::: warning Proxy 通道限制
BungeeCord Plugin Messaging 单包约 **32KB**。超大 payload（如 Tab 全量快照）在 Proxy 通道会被跳过，仅走 Redis。生产环境多服 Tab 同步**强烈建议启用 Redis**。
:::

::: tip 无在线玩家时 Proxy 出站
Proxy `Forward` 需要至少一名在线玩家作为载体。若子服长期零在线，仅 Proxy 无法发出跨服消息；此时应启用 Redis 或保证有常驻假人/管理员在线。
:::

### 模块 `ArcartX*.yml` — 仅开关与可选覆盖

各模块统一使用 `cross-server` 节，**不再**配置 Redis host、Proxy channel 等连接信息。

```yaml
cross-server:
  enabled: true
  # 可选：仅覆盖本模块的后端开关（继承宿主默认）
  # redis:
  #   enabled: true
  # proxy:
  #   enabled: false
```

模块 `cross-server.enabled: false` 时不会注册通道，也不会收发跨服消息。

## Wire 协议

### 外层信封 `CrossServerEnvelope`（JSON）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `protocol` | int | 固定 `1` |
| `module` | string | 模块通道 ID，如 `chat`、`tab`、`mail` |
| `nodeId` | string | 发送方子服 `node-id` |
| `messageId` | string | UUID，全局去重用 |
| `timestamp` | long | 毫秒时间戳 |
| `payload` | string | 模块业务载荷（UTF-8 文本） |
| `signature` | string | HMAC-SHA256（`signature.enabled` 时） |

- **Redis**：`PUBLISH ArcartX-Suite:CROSS <整段 JSON>`
- **Proxy**：`BungeeCord` → `Forward` → `AXS_CROSS`，body = 整段 JSON

### 内层 payload（模块自定义）

SDK **只负责**信封与路由；业务格式由各模块定义，例如：

| 模块 | channel ID | payload 格式 |
| --- | --- | --- |
| Chat | `chat` | Chat 信封 JSON |
| Tab | `tab` | Tab 快照 JSON |
| Announcer | `announcer` | YAML `AnnouncerEnvelope` |
| Mail | `mail` | `refresh:{uuid}` |
| OnlineRewards | `onlinerewards` | `refresh:{uuid}` |
| Market | `market` | `LISTING_CREATED:{id}` 等短文本 |
| EntityTracker | `entitytracker` | `BEST_DAMAGE\t...` / `KILL_RECORD\t...` Tab 分隔 |
| Warehouse | `warehouse` | `LOCK\t{sharedId}\t...` / `UNLOCK\t{sharedId}\t...` |

## 已接入模块

| 模块 | 配置文件 | 典型用途 |
| --- | --- | --- |
| Chat | `ArcartXChat.yml` | 频道消息跨服；频道文件内 `cross-server: true` |
| Tab | `ArcartXTab.yml` | 在线列表快照同步；Tab 定义 `cross-server: true` |
| Announcer | `ArcartXAnnouncer.yml` | `gbroadcast` / `gbroadcastnow` 手动公告 |
| Mail | `ArcartXMail.yml` | 新邮件到达后刷新其他子服收件箱 UI |
| OnlineRewards | `ArcartXOnlineRewards.yml` | 签到/补签/管理操作后刷新 |
| Market | `ArcartXMarket.yml` | 拍卖事件广播（**Redis 缓存**仍用模块内 `redis` 节） |
| EntityTracker | `ArcartXEntityTracker.yml` | Boss 死亡结算后同步各服玩家最高伤害（`player_boss_best_damage`） |
| Warehouse | `ArcartXWarehouse.yml` | 共享仓库编辑锁 `LOCK` / `UNLOCK`（需 MySQL 共享库 + `shared.enabled`） |

Market 的 `redis` 节仅用于**拍卖列表缓存**（`cache-ttl-seconds`），与跨服 Pub/Sub 无关。

EntityTracker 需同时开启根级 `cross-server.enabled` 与 `new-features.cross-server-ranking.enabled`；Redis/Proxy 连接见宿主 `config.yml`。

Warehouse 跨服编辑锁需：`shared.enabled: true`、模块 `cross-server.enabled: true`、各子服共用同一 MySQL 仓库库。

## 多服部署清单

1. **宿主** `config.yml`：配置 `cross-server`（Redis/Proxy/签名/node-id）
2. **每台子服** `node-id` 唯一（如 `lobby`、`survival`、`creative`）
3. **各模块** `cross-server.enabled: true`
4. **共享数据**：Mail / OnlineRewards / Chat 禁言等需 MySQL 共享库
5. **重载**：`/axs reload all` 或分模块 reload；观察控制台 `[CrossServer] 已启动`
6. **验证**：模块 status 命令（如 `/axs chat status`）查看跨服通道是否 active

## 开发者 API

模块通过 `ModuleContext.crossServer()` 获取 `CrossServerAPI`：

```java
CrossServerChannel channel = context.crossServer().openChannel(
    "mymodule",
    configuration.crossServer(),
    delivery -> handlePayload(delivery.payload())
);

// 出站
channel.publish(myPayloadString);

// 关闭（模块 onDisable）
channel.close();
```

配置解析：

```java
CrossServerChannelConfig cfg = CrossServerChannelConfigs.fromSection(
    yaml.getConfigurationSection("cross-server")
);
```

详见 [ModuleContext — crossServer()](/api/module-context#跨服-api-crossserverapi)。

## 与 ArcartX-Suite-Proxy 的关系

| 组件 | 职责 |
| --- | --- |
| **CrossServerService（子服）** | 跨服游戏数据：聊天、Tab、邮件刷新、拍卖同步等 |
| **ArcartX-Suite-Proxy（代理端）** | Yggdrasil 多源认证、离线拦截、账号类型标记 |

两者独立部署、独立配置。跨服消息**不需要**在 Proxy 插件中额外配置频道（子服 SDK 直接使用 BungeeCord `Forward`）。

## 故障排查

| 现象 | 可能原因 |
| --- | --- |
| 其他子服收不到消息 | 宿主 `cross-server.redis/proxy` 未启用；模块 `cross-server.enabled: false` |
| 消息重复 | 正常：双后端会双发，SDK 按 `messageId` 去重 |
| Tab 跨服不完整 | 仅 Proxy 且 payload 超 32KB → 启用 Redis |
| 验签失败 | 各子服 `signature.secret` 不一致或未配置 |
| Proxy 发不出 | 子服无在线玩家；改用 Redis |

