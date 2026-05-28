# QQBot QQ群服互联

::: tip 付费模块
QQBot 为付费模块，需要有效授权码激活。
:::

**QQBot** 模块通过 **OneBot 11 正向 WebSocket** 协议连接 QQ 机器人，打通 QQ 群与 Minecraft 服务器之间的消息流转，并提供 QQ-游戏账号绑定、白名单管理、群指令查询玩家数据等完整闭环能力。

## 功能概览

| 子系统 | 核心功能 |
|--------|----------|
| **消息同步** | QQ 群 ↔ 游戏聊天双向转发、玩家进退服 QQ 通知、消息格式自定义、CQ 码过滤 |
| **账号绑定** | 群发 `#绑定 玩家名` 申请、游戏内 `/qqbot bind <code>` 确认、SQLite/MySQL 持久化 |
| **白名单** | 绑定自动加白、解绑自动删白、群管理员 `#加白`/`#删白` 直接管理 |
| **群指令** | 内置（在线列表/服务器状态）+ PlaceholderAPI 查询 + 服务器命令执行 + 自定义扩展 |
| **跨模块** | `QQBotBroadcastable` capability 供其他模块推送消息到 QQ 群 |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | 模块加载（无 UI 资源） |
| OneBot 11 实现端 | ✅ 必须 | Lagrange / NapCat / LLOneBot / go-cqhttp 等 |
| SQLite (内置) | 默认 | 绑定数据持久化 |
| MySQL | 可选 | 跨服共享绑定数据时使用 |
| PlaceholderAPI | 可选 | 群指令查询玩家数据 |
| Chat 模块 | 可选 | 推荐共同使用获得更完整的聊天体验 |

::: warning OneBot 实现端
本模块**不提供云端服务**，需要你自行部署 OneBot 11 实现端。推荐方案：
- **[Lagrange](https://github.com/LagrangeDev/Lagrange.Core)** — 跨平台、纯净、活跃维护
- **[NapCat](https://github.com/NapNeko/NapCatQQ)** — 基于 QQNT，功能最完整
- **[LLOneBot](https://github.com/LLOneBot/LLOneBot)** — QQNT 插件形态
:::

## 命令

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/qqbot bind <验证码>` | `arcartxsuite.qqbot.use` | 确认绑定群内申请生成的验证码 |
| `/qqbot unbind` | `arcartxsuite.qqbot.use` | 解除当前账号的 QQ 绑定 |
| `/qqbot info` | `arcartxsuite.qqbot.use` | 查看当前绑定的 QQ 号 |

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs qqbot status` | `arcartxsuite.qqbot.admin` | 查看连接/群数/绑定/白名单状态 |
| `/axs qqbot reload` | `arcartxsuite.qqbot.admin` | 重载配置 |
| `/axs qqbot send <消息>` | `arcartxsuite.qqbot.admin` | 向所有已配置群发送消息 |
| `/axs qqbot lookup <玩家名\|QQ号>` | `arcartxsuite.qqbot.admin` | 查询绑定关系（双向） |

### 群内指令（默认前缀 `#`）

| 群指令 | 权限 | 说明 |
|--------|------|------|
| `#绑定 <玩家名>` | 群员 | 申请绑定，机器人返回 6 位验证码 |
| `#解绑` | 群员 | 解除自己 QQ 的绑定 |
| `#查绑 [玩家名]` | 群员 | 查询自己/指定玩家的绑定 |
| `#加白 <玩家名>` | 群管/群主 | 添加白名单 |
| `#删白 <玩家名>` | 群管/群主 | 移除白名单 |
| `#查在线` | 群员 | 内置：返回在线玩家列表 |
| `#查服务器` | 群员 | 内置：返回 TPS / 内存 / 实体 / 区块 |
| `#查玩家 [玩家名]` | 群员 | PAPI 示例：等级 / 金币 / 生命 / 饥饿 |
| `#执行命令 <命令>` | 群管/群主 | 在控制台执行任意命令 |

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `arcartxsuite.qqbot.use` | true | 玩家命令 |
| `arcartxsuite.qqbot.admin` | op | 管理员命令 |

## PAPI 占位符

前缀：`%AXSqqbot_xxx%`

| 占位符 | 说明 |
|--------|------|
| `%AXSqqbot_connected%` | 机器人连接状态（true/false） |
| `%AXSqqbot_connected_display%` | 机器人连接状态（已连接/未连接） |
| `%AXSqqbot_is_bound%` | 当前玩家是否已绑定（true/false） |
| `%AXSqqbot_bound_qq%` | 当前玩家绑定的 QQ 号 |
| `%AXSqqbot_bound_name%` | 当前玩家绑定时的游戏名 |
| `%AXSqqbot_group_count%` | 已配置监听的群数量 |

## 配置文件

### 主配置 `ArcartXQQBot.yml`

```yaml
settings:
  debug: false
  server-id: "survival"          # 服务器标识（多服时区分来源）

# OneBot 11 正向 WebSocket
onebot:
  ws-url: "ws://127.0.0.1:8080"  # OneBot 实现端的 WS 地址
  access-token: ""               # 鉴权 token（可留空）
  reconnect-interval-seconds: 10
  heartbeat-interval-seconds: 30

# 监听的 QQ 群（支持多群）
groups:
  - group-id: 123456789
    sync-mode: "both"            # both / game-to-qq / qq-to-game / none
    game-to-qq: "[MC] {player}: {message}"
    qq-to-game: "&7[QQ] &f{nick}: &7{message}"
    commands-enabled: true
    join-message: "[MC] {player} 加入了服务器"
    quit-message: "[MC] {player} 离开了服务器"

# QQ-游戏账号绑定
binding:
  enabled: true
  method: "code"                  # 验证码模式
  code-expire-seconds: 300
  max-bindings-per-qq: 1
  bind-prefix: "#绑定"
  unbind-prefix: "#解绑"
  query-prefix: "#查绑"

# 白名单联动
whitelist:
  enabled: true
  auto-add-on-bind: true
  auto-remove-on-unbind: true
  add-command: "whitelist add {name}"
  remove-command: "whitelist remove {name}"

# 自定义群指令
command-prefix: "#"
custom-commands:
  查在线:
    permission: 0                 # 0=群员, 1=群管/群主
    type: "builtin"
    builtin-id: "online-list"
  查服务器:
    permission: 0
    type: "builtin"
    builtin-id: "server-status"
  查玩家:
    permission: 0
    type: "papi-query"
    placeholders:
      - "%player_level%"
      - "%vault_eco_balance%"
      - "%player_health%"
      - "%player_food_level%"
    format: "玩家: {name}\n等级: {0}\n金币: {1}\n生命: {2}\n饥饿: {3}"
  执行命令:
    permission: 1
    type: "server-command"
    command: "{args}"

# 存储
storage:
  mode: "sqlite"                  # sqlite / mysql
  sqlite-file: "qqbot.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "arcartxsuite"
    username: "root"
    password: ""
    table-prefix: "axs_qqbot_"
  pool-size: 4
```

## 自定义指令类型

| 类型 | 字段 | 说明 |
|------|------|------|
| `builtin` | `builtin-id` | 内置指令：`online-list` / `server-status` |
| `papi-query` | `placeholders[]` + `format` | 解析 PAPI 占位符填充模板（`{0}` `{1}` …） |
| `server-command` | `command` | 控制台执行（`{args}` = 群内参数） |

## 存储结构

QQBot 数据库表（SQLite 默认在 `plugins/ArcartXSuite/data/qqbot/qqbot.db`，MySQL 使用 `table-prefix`）：

| 表名 | 字段 | 用途 |
|------|------|------|
| `axs_qqbot_bindings` | `id`、`qq_id`、`player_uuid`、`player_name`、`bound_at` | QQ ↔ 游戏账号绑定关系（`UNIQUE(qq_id, player_uuid)`） |

## 配置诊断

QQBot 模块声明了以下配置校验规则：

| 字段 | 类型 | 约束 |
|------|------|------|
| `onebot.ws-url` | STRING | 必填 |
| `storage.mode` | STRING | 必填，枚举 `sqlite` / `mysql` |
| `storage.pool-size` | INT | 范围 1–50 |

动态节（用户可自由增删，不被结构同步覆盖）：
- `groups`
- `custom-commands`

## 跨模块 Capability

QQBot 注册了 `QQBotBroadcastable` capability，其他模块可推送消息到 QQ 群：

```java
QQBotBroadcastable qqBot = context.getCapability(QQBotBroadcastable.class);
if (qqBot != null) {
    qqBot.sendToGroup(123456789L, "玩家击败了 Boss！");
    qqBot.sendToAllGroups("[公告] 服务器即将重启");
}
```

## 工作流程

### 玩家绑定 QQ
```
QQ 群: "#绑定 Steve"
    ↓
机器人: "验证码: 123456，5 分钟内到游戏中输入"
    ↓
游戏: /qqbot bind 123456
    ↓
机器人: "绑定成功！QQ: 12345 ↔ 游戏: Steve"
    ↓ (whitelist.auto-add-on-bind = true)
控制台: whitelist add Steve
```

### 消息同步
```
游戏聊天: Steve > 你好
    ↓ (AsyncPlayerChatEvent MONITOR 监听)
QQ 群: [MC] Steve: 你好

QQ 群: 张三 > 上线了吗
    ↓ (OneBot WS 推送)
游戏广播: [QQ] 张三: 上线了吗
```

## 架构

```
QQBotModule (AbstractAXSModule)
├── QQBotService (主服务，AsyncPlayerChatEvent / Join / Quit 监听)
│   ├── QQBotBindService (绑定逻辑 + 验证码池)
│   └── QQBotCommandRouter (群指令路由器)
├── OneBotClient (Java 17 内置 WebSocket，自动重连 + 心跳)
│   ├── OneBotAction (动作 JSON 构造)
│   └── OneBotEvent (事件解析)
├── JdbcQQBotRepository (HikariCP + SQLite/MySQL)
├── QQBotPlayerCommand (/qqbot)
├── QQBotAdminCommand (/axs qqbot)
├── QQBotPlaceholderExpansion (PAPI)
└── 注册 QQBotBroadcastable capability
```

## 安全建议

- **OneBot 实现端** 建议监听 `127.0.0.1` 仅本机访问，并配置 `access-token`
- **远程连接** 时务必启用 token，并通过 SSH 隧道 / VPN / 反向代理加密链路
- **`#执行命令`** 默认仅群管/群主可用，建议结合 `permission: 1` 严格控制
- **白名单联动** 在不熟悉时可先关闭 `auto-add-on-bind`，待绑定流程稳定后再启用
