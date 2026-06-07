# 跨服功能配置指南

本页是**运维向**快速上手。架构细节见 [跨服通信（CrossServer SDK）](/architecture/cross-server)。

## 两步启用

### 1. 宿主 `plugins/ArcartXSuite/config.yml`

```yaml
cross-server:
  node-id: "survival"    # ← 每台子服改成不同值
  redis:
    enabled: true
    host: "192.168.1.100"
    port: 6379
    password: ""
    channel: "AXS:CROSS"
  proxy:
    enabled: false       # 无 Redis 时可改为 true（有 32KB 与在线玩家限制）
  signature:
    enabled: true
    secret: "同一网络内所有子服填写相同密钥"
    verify: true
```

### 2. 模块 `ArcartX*.yml`

```yaml
cross-server:
  enabled: true
```

需要跨服的具体功能（Chat 频道、Tab 定义等）仍按模块文档设置 `cross-server: true`。

## 按模块速查

| 模块 | 模块开关 | 业务开关 | 共享存储 |
| --- | --- | --- | --- |
| Chat | `ArcartXChat.yml` → `cross-server.enabled` | `chat/channels/*.yml` → `cross-server: true` | MySQL 推荐 |
| Tab | `ArcartXTab.yml` → `cross-server.enabled` | `data/tab/tabs/*.yml` → `cross-server: true` | 无（快照同步） |
| Announcer | `ArcartXAnnouncer.yml` → `cross-server.enabled` | `gbroadcast` / `gbroadcastnow` 命令 | 无 |
| Mail | `ArcartXMail.yml` → `cross-server.enabled` | 自动（发信后广播 refresh） | **MySQL 必需** |
| OnlineRewards | `ArcartXOnlineRewards.yml` → `cross-server.enabled` | 自动（签到/补签后 refresh） | **MySQL 必需** |
| Market | `ArcartXMarket.yml` → `cross-server.enabled` | 拍卖事件自动广播 | **MySQL 必需**；`redis` 节仅缓存 |

## Chat 跨服示例

**宿主**（三台子服仅 `node-id` 不同）+ **Chat 模块**：

```yaml
# ArcartXChat.yml（每台子服相同，除 settings.server-id 可不同）
cross-server:
  enabled: true

settings:
  server-id: "lobby"   # 展示用，与 node-id 无关
```

```yaml
# data/chat/channels/Global.yml
cross-server: true
```

重载后 `/axs chat status` 应显示跨服通道已激活。

## 从旧版迁移

`1.2.0-beta` 之前各模块独立的配置**已失效**，请删除并改用新结构：

| 旧配置（已移除） | 新配置 |
| --- | --- |
| `ArcartXChat.yml` → `transport.redis` / `transport.proxy` | 宿主 `cross-server` + 模块 `cross-server.enabled` |
| `ArcartXAnnouncer.yml` → `transport.proxy` | 同上 |
| `ArcartXMail.yml` → `redis`（Pub/Sub） | 同上 |
| `ArcartXOnlineRewards.yml` → `redis` | 同上 |
| `ArcartXTab.yml` → 模块内 Redis/Proxy 节 | 同上 |
| Market `redis.channel`（Pub/Sub） | 跨服用宿主 SDK；`redis` 仅保留缓存字段 |

旧频道名（`AXS:chat`、`AXS_ANNOUNCER` 等）不再使用；统一为 Redis `AXS:CROSS`、Proxy `AXS_CROSS`。

## 验证

1. 控制台出现：`[CrossServer] 已启动 | node=... | redis=true | proxy=...`
2. 模块 status 命令显示跨服 ON
3. 子服 A 触发业务（聊天/公告/上架等），子服 B 有对应反应
