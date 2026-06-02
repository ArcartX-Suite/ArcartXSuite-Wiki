# Chat 聊天

## 功能定位

多频道聊天系统：频道切换、私聊、@提及、SocialSpy、禁言、聊天卡片推送。支持 Redis / BungeeCord 跨服转发。

### 核心特性

- **多频道体系**：自定义频道（普通、全服、私聊、管理），每个频道是 `chat/channels/` 目录下一个独立 `.yml` 文件，可独立配置模式、格式、范围和权限
- **私聊与回复**：`/msg` 私聊、`/reply` 快速回复；跨服模式下目标玩家可在其他子服
- **@提及**：聊天中输入 `@玩家名` 或 `@all` 触发提及通知，可选 ArcartX 聊天卡片提醒；支持 `@` 名称自动补全，双通道实现：① 原版聊天栏通过 Paper API / NMS 发包（1.19.1+），② ArcartX 自定义聊天栏通过 overlay UI 实时匹配（需 ArcartX 客户端，打开聊天栏后输入 `@` 即显示候选列表，点击插入）
- **SocialSpy 社交监听**：拥有权限的玩家可查看全服私聊内容
- **忽略系统**：玩家可屏蔽不想看到的玩家消息
- **禁言管理**：支持定时禁言（如 `30m`、`7d`）和永久禁言，带原因记录
- **物品展示**：聊天中输入 `[item]`（可配置）自动替换为手持物品名称和数量。当配置了 `item-preview-card-id` 时，通过 ArcartX 聊天卡片展示物品详情（可触发 Tooltip），此时原始聊天消息行会被抑制以避免重复；未配置卡片时，物品文本带有原版 `SHOW_ITEM` 悬浮提示
- **自定义组件**：通过正则匹配替换聊天内容中的特殊标记（如 `:star:` → `★`）
- **敏感词过滤**：本地词库 + 远程云词库，支持正则匹配，可选取消发言或替换敏感内容
- **聊天卡片**：提及、私聊、系统提示、物品展示均可绑定 ArcartX 聊天卡片 ID
- **跨服转发**：Redis Pub/Sub 或 BungeeCord/Velocity 代理通道，多服消息互通
- **发言冷却与重复检测**：防刷屏，可配置冷却时间和重复消息窗口
- **Paper 兼容**：自动检测 Paper 的 `AsyncChatEvent`，优先使用；不可用时回退 Bukkit `AsyncPlayerChatEvent`
- **数据持久化**：SQLite 或 MySQL 存储玩家聊天状态和禁言记录

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 聊天卡片、物品展示卡片、客户端提示包 | 基础 Bukkit 聊天可拦截，但 AXS 的可视化卡片能力不可用 |
| 可选 | PlaceholderAPI | 解析频道格式中的 `%...%` 占位符 | 对应变量不会动态替换 |
| 可选 | Redis 服务 | 多服聊天转发、去重和远程节点同步 | 单服聊天正常，跨服互通关闭 |
| 可选 | MySQL 服务 | 多服共享玩家状态和禁言记录 | 默认 SQLite 可用；多服共享建议改 MySQL |

## 启用步骤

```yaml
modules:
  chat:
    enabled: true
```

## 配置说明

### 主配置（`ArcartXChat.yml`）

模块首次加载后在插件数据目录生成。完整结构如下：

```yaml
settings:
  debug: false
  # 当前服务端节点标识；跨服转发和 Redis 去重时使用，多服必须不同。
  server-id: "default"
  # 默认频道 ID（对应 chat/channels/ 下的文件名，不含扩展名，全小写）。
  default-channel: "normal"
  # 单条消息最大字符长度。
  max-length: 256
  # 连续发言冷却（毫秒）。
  cooldown-millis: 750
  # 重复消息判定窗口（毫秒），窗口内发送相同内容会被拒绝。
  duplicate-window-millis: 4000

compatibility:
  other-chat-plugins:
    # 强制接管聊天事件。仅在存在其他聊天插件导致消息冲突时开启。
    force-takeover: false

storage:
  # sqlite 或 mysql。
  mode: "sqlite"
  sqlite:
    file: "chat.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "arcartxsuite"
    username: "root"
    password: ""
  pool-size: 4

transport:
  redis:
    enabled: false
    host: "127.0.0.1"
    port: 6379
    password: ""
    database: 0
    channel: "AXS:chat"
    # 本节点标识，用于消息去重；留空时继承 settings.server-id。
    node-id: ""
    connect-timeout-ms: 5000
  proxy:
    enabled: false
    # BungeeCord/Velocity 插件信道名称，所有子服必须一致。
    messenger-channel: "AXS_CHAT"
    # 转发目标：ALL 表示广播到所有子服。
    forward-target: "ALL"
    node-id: ""

cards:
  # ArcartX 聊天卡片 ID；留空表示不发送卡片。
  mention-card-id: ""
  private-card-id: ""
  system-card-id: ""
  item-preview-card-id: "axs_item_preview"
  # 卡片固定宽度（自适应坐标单位）。
  card-width: 500
  # 卡片单行基础高度；多行消息时自动增长。
  card-height: 100

function:
  mention:
    enabled: true
    # 是否允许 @all 提及全部在线玩家。
    allow-all: true
  item:
    enabled: true
    # 聊天中触发物品展示的标记文本。
    token: "[item]"
    # 展示格式；支持变量 {item_name}、{item_amount}。
    format: "&b[&f{item_name}&b x{item_amount}]"
    # 手持空气或无法序列化时的回退文本。
    failed-format: "&c[物品不可用]"
  # 自定义组件匹配表；pattern 使用 Java 正则。
  custom-components:
    # star:
    #   enabled: true
    #   pattern: ":star:"
    #   replacement: "★"

filter:
  enabled: false
  # true = 命中后取消发言；false = 替换后继续发送。
  cancel-on-match: false
  replacement: "*"
  blocked-words: []
  # Java 正则列表，大小写不敏感。
  blocked-patterns: []
  cloud:
    enabled: false
    url: ""
    refresh-minutes: 60

# 频道定义文件目录，相对于插件数据目录。
channels-directory: "chat/channels"
```

### 频道定义（`chat/channels/*.yml`）

每个频道是一个独立的 `.yml` 文件，文件名（去掉 `.yml` 后缀并转小写）就是频道 ID。模块首次加载时自动生成四个示例频道：

| 文件 | 频道 ID | 模式 | 说明 |
| --- | --- | --- | --- |
| `Normal.yml` | `normal` | normal | 默认普通频道，仅本服广播 |
| `Global.yml` | `global` | global | 全服频道，默认开启跨服同步 |
| `Private.yml` | `private` | private | 私聊频道，`/msg` 和 `/reply` 复用此格式 |
| `Staff.yml` | `staff` | staff | 管理频道，需要权限才能收发 |

#### 频道模式

| 模式 | 说明 |
| --- | --- |
| `normal` | 普通频道。可选 `range` 限制接收范围（方块距离）；`range: 0` 表示不限距离，所有同服在线玩家都能看到 |
| `global` | 全服频道。默认 `cross-server: true`，消息通过 Redis/Proxy 转发到其他子服 |
| `private` | 私聊频道。通过 `/msg` 和 `/reply` 使用；设有该频道的 `send-permission` 和 `receive-permission` 才能私聊 |
| `staff` | 管理频道。默认 `cross-server: true`；仅拥有 `receive-permission` 的玩家可收到消息 |

#### 频道配置完整字段

```yaml
enabled: true
display-name: "普通"
mode: "normal"

# 发送权限；留空表示所有玩家可发送。
send-permission: ""
# 接收权限；留空表示所有玩家可接收。默认继承 send-permission。
receive-permission: ""

# 是否通过 transport 跨服转发。global 和 staff 模式默认 true。
cross-server: false
# 接收范围（仅 normal 模式生效）；0 = 不限距离。
range: 0

# 公屏消息格式。
format: "&7[{channel}] &f{player_name}&7: &r{message}"
# 控制台日志格式。
console-format: "[{channel}] {player_name}: {message}"
# 私聊 — 发送方看到的格式。
sender-format: "&d[私聊 -> {target_name}] &f{player_name}&7: &r{message}"
# 私聊 — 接收方看到的格式。
recipient-format: "&d[私聊 <- {player_name}] &f{player_name}&7: &r{message}"
# SocialSpy 监听者看到的格式。
spy-format: "&5[监听 {player_name} -> {target_name}] &r{message}"
```

#### 格式可用变量

| 变量 | 说明 |
| --- | --- |
| `{channel}` | 频道显示名称 |
| `{player_name}` | 发送者游戏名 |
| `{player_display_name}` | 发送者显示名 |
| `{target_name}` | 目标玩家名（私聊/监听时有值） |
| `{message}` | 消息内容 |
| `%...%` | PlaceholderAPI 占位符（需安装 PAPI） |

## 跨服聊天设置教程

跨服聊天允许多个子服间互通消息。Chat 模块支持两种传输方式：**Redis** 和 **Proxy**（BungeeCord/Velocity）。

### 前置条件

1. 所有子服均安装了 ArcartXSuite，且 Chat 模块已启用
2. 选择一种传输方式（推荐 Redis，延迟低、稳定性好）
3. 多服共享数据建议将 `storage.mode` 改为 `mysql`，否则各服的玩家状态和禁言记录不同步

### 方式一：Redis 跨服（推荐）

**第 1 步 — 准备 Redis 服务**

确保有一台所有子服均可访问的 Redis 实例（如 `192.168.1.100:6379`）。

**第 2 步 — 修改每个子服的 `ArcartXChat.yml`**

```yaml
settings:
  # 每个子服必须填写不同的 server-id
  server-id: "lobby"          # 另一个子服填 "survival"、"creative" 等

transport:
  redis:
    enabled: true
    host: "192.168.1.100"     # Redis 服务器地址
    port: 6379
    password: "your_password" # 无密码留空
    database: 0
    channel: "AXS:chat"      # 所有子服必须一致
    node-id: ""               # 留空则自动使用 server-id
    connect-timeout-ms: 5000
```

**第 3 步 — 标记需要跨服的频道**

在 `chat/channels/` 目录下，将需要跨服同步的频道文件中 `cross-server` 设为 `true`：

```yaml
# chat/channels/Global.yml
cross-server: true
```

`global` 和 `staff` 模式的频道默认 `cross-server: true`；`normal` 模式默认 `false`。

**第 4 步 — 多服共享存储（推荐）**

将所有子服的存储改为同一个 MySQL 数据库，使玩家状态、禁言、忽略列表在各子服同步：

```yaml
storage:
  mode: "mysql"
  mysql:
    host: "192.168.1.100"
    port: 3306
    database: "arcartxsuite"
    username: "axs_user"
    password: "your_db_password"
  pool-size: 4
```

**第 5 步 — 重载**

每个子服执行 `/axs reload chat`，检查控制台日志确认 `Transport=redis (true)` 字样。

### 方式二：BungeeCord / Velocity 代理跨服

适用于没有独立 Redis 的环境，依赖代理端的插件消息通道转发。

**第 1 步 — 修改每个子服的 `ArcartXChat.yml`**

```yaml
settings:
  server-id: "lobby"

transport:
  proxy:
    enabled: true
    # 信道名称，所有子服必须一致
    messenger-channel: "AXS_CHAT"
    # ALL = 广播到所有子服
    forward-target: "ALL"
    node-id: ""
```

> **注意**：代理跨服需要至少有一名在线玩家作为消息载体。如果子服没有在线玩家，消息将无法发出。

**第 2 步** — 同方式一的第 3–5 步。

### 验证跨服是否生效

1. 在子服 A 发送一条消息，检查子服 B 是否收到
2. 使用 `/axs chat status` 查看 `传输层` 状态，应显示 `redis (true)` 或 `proxy (true)`
3. 如果使用 Redis，检查控制台是否有 `Chat Redis 订阅中断` 等错误信息

### 跨服私聊

私聊频道（`Private.yml`）设置 `cross-server: true` 后，`/msg` 可向其他子服的玩家发送私聊。目标玩家无需在线于同一子服，只要其 profile 在数据库中存在即可定位。

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs chat status` | 查看聊天模块状态（频道数、缓存、传输层信息） |
| `/axs reload chat` | 重载聊天模块配置（频道、过滤、跨服等） |
| `/axs chat mute <玩家> <时长> [原因]` | 禁言玩家。时长如 `30m`、`12h`、`7d`、`permanent`（永久） |
| `/axs chat unmute <玩家>` | 解除玩家禁言 |
| `/axs chat spy <玩家> <on\|off>` | 开启/关闭指定玩家的社交监听（无需目标玩家在线） |

### 玩家命令（权限：`arcartxsuite.chat.use`）

| 命令 | 说明 |
| --- | --- |
| `/chat` | 查看当前聊天状态（频道、私聊开关、忽略列表等） |
| `/chat channel <频道ID>` | 切换聊天频道，之后发送的消息进入该频道 |
| `/chat toggle private [on\|off]` | 开启/关闭私聊接收 |
| `/chat toggle mentions [on\|off]` | 开启/关闭 @提及通知 |
| `/chat ignore <玩家>` | 屏蔽指定玩家，不再看到对方消息 |
| `/chat unignore <玩家>` | 取消屏蔽 |
| `/chat socialspy [on\|off]` | 开启/关闭社交监听（权限：`arcartxsuite.chat.socialspy`） |
| `/msg <玩家> <消息>` | 向指定玩家发送私聊（权限：`arcartxsuite.chat.msg`，跨服可用） |
| `/reply <消息>` | 快速回复最近一次私聊你的玩家（权限：`arcartxsuite.chat.msg`） |

## 权限

| 权限节点 | 说明 |
| --- | --- |
| `arcartxsuite.admin` | 管理命令（mute、unmute、spy、status） |
| `arcartxsuite.chat.use` | 玩家聊天命令（channel、toggle、ignore 等） |
| `arcartxsuite.chat.msg` | 私聊和回复（`/msg`、`/reply`） |
| `arcartxsuite.chat.socialspy` | 社交监听（`/chat socialspy`） |
| 频道的 `send-permission` | 在该频道发送消息（配置在频道 yml 中） |
| 频道的 `receive-permission` | 接收该频道消息（配置在频道 yml 中） |

## 聊天卡片

聊天卡片是 ArcartX 客户端提供的可交互消息控件，在聊天栏中显示为可点击的卡片。Chat 模块在特定场景自动发送卡片，卡片模板配置在 ArcartX 的 `chat_card` 目录下。

通过 `ArcartXChat.yml` 的 `cards` 节配置每种场景绑定的卡片 ID：

| 配置项 | 触发场景 | 卡片数据（`self.parent.data['key']`） |
| --- | --- | --- |
| `mention-card-id` | 有人 @提及你时 | `senderName`、`senderDisplayName`、`channel`、`channelId`、`message`、`cardWidth`、`cardHeight` |
| `private-card-id` | 收到/发送私聊时 | `senderName`、`senderDisplayName`、`targetName`、`direction`（`sender`/`recipient`）、`message`、`cardWidth`、`cardHeight` |
| `item-preview-card-id` | 聊天中展示物品时 | `cardWidth`、`cardHeight`、`senderName`、`senderDisplayName`、`itemName`、`itemAmount`、`itemMaterial`、`channel`、`itemJson` |
| `system-card-id` | 系统通知（禁言提示等） | `type`、`cardWidth`、`cardHeight` + 各场景额外字段 |

### 内置卡片模板

模块首次启动时自动导出以下四种内置卡片到 `plugins/ArcartX/chat_card/`（文件已存在则不覆盖）：

| 文件 | 默认配置键 | 说明 |
| --- | --- | --- |
| `axs_chat_mention.yml` | `mention-card-id` | @提及通知：蓝色边框，显示谁提到了你及消息内容 |
| `axs_chat_private.yml` | `private-card-id` | 私聊通知：紫色边框，区分发送/接收方向 |
| `axs_chat_system.yml` | `system-card-id` | 系统提示：红色边框，禁言/过滤等警告 |
| `axs_item_preview.yml` | `item-preview-card-id` | 物品预览：含物品图标 Slot，悬浮触发 Tooltip |

卡片宽度由 `cards.card-width` 配置（默认 500），字体大小由每个卡片模板 YAML 自行硬编码（默认 49）。服务端根据卡片可用文字区域自动对超长消息进行换行，并根据行数动态计算 `cardHeight`（单行时等于 `cards.card-height`，多行时自动增长）。将配置值设为空字符串可禁用对应卡片。

> **冗余消息抑制**：当卡片成功发送到客户端时，对应的文字消息不再重复输出。例如禁言提示卡片发送后不再发送红色文字提示；@提及卡片发送后不再发送原始聊天消息行到被提及玩家。

#### 物品预览卡片

- 卡片内包含一个 **Slot**（`slotType: ~Icon`），通过 `setItemIcon(itemJson)` 渲染物品图标，悬浮时可触发 ArcartX Tooltip 显示完整物品信息
- 卡片宽度由配置 `card-width` 决定，高度固定为 `card-height`
- **当卡片成功发送时，原始聊天消息行会被完全抑制**，避免物品信息重复出现
- 禁用后回退到原版 `SHOW_ITEM` 悬浮预览

#### 私聊卡片

- 接收方视角：点击卡片背景调用 `Chat.setMessage('/msg 玩家名 ')`，将回复命令预填到聊天框，玩家直接输入消息内容即可发送
- 卡片图标为铅笔符号（`§d✎`）
- 宽度由配置 `card-width` 决定，超长消息自动换行，高度随行数动态增长

#### 系统提示卡片

系统卡片 `data['type']` 可能的值：

| type | 触发 | 额外字段 |
| --- | --- | --- |
| `mute` | 玩家被禁言时发言 | `remaining`（剩余时间文本）、`reason`（原因） |
| `filter` | 消息被敏感词拦截 | `message`（提示文本） |

## PAPI

前缀：`%AXSchat_*%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSchat_current_channel%` | 文本 | 当前所在频道的 ID |
| `%AXSchat_current_channel_display%` | 文本 | 当前频道的显示名称 |
| `%AXSchat_reply_target%` | 文本 | 最近私聊对象的玩家名，没有时返回空 |
| `%AXSchat_spy_enabled%` | `true`/`false` | 社交监听是否开启 |
| `%AXSchat_ignore_count%` | 数字 | 已屏蔽的玩家数量 |
| `%AXSchat_muted%` | `true`/`false` | 是否处于被禁言状态 |
