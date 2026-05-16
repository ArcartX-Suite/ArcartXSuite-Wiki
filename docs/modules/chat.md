# Chat 聊天

## 功能定位

多频道聊天系统：频道切换、私聊、@提及、SocialSpy、禁言、聊天卡片推送。支持 Redis 跨服转发。

### 核心特性

- **多频道体系**：自定义频道（全服、本地、员工等），每个频道可独立配置格式、范围、权限，频道定义文件在 `chat/channels/*.yml`
- **私聊与回复**：`/msg` 私聊、`/reply` 快速回复，支持开关私聊接收
- **@提及**：聊天中输入 `@玩家名` 触发提及通知，可选 ArcartX 聊天卡片提醒
- **SocialSpy 社交监听**：管理员或有权限的玩家可查看全服私聊内容
- **忽略系统**：玩家可屏蔽不想看到的玩家消息
- **禁言管理**：支持定时禁言（如 `30m`、`7d`）和永久禁言，带原因记录
- **物品展示**：聊天中输入 `[item]` 自动替换为手持物品名称和数量，其他玩家可通过聊天卡片预览
- **自定义组件**：通过正则/字符串匹配替换聊天内容中的特殊标记（如 `:star:` → `★`）
- **敏感词过滤**：本地词库 + 远程云词库，支持正则匹配，可选取消发言或替换敏感内容
- **聊天卡片**：提及、私聊、系统提示、物品展示均可绑定 ArcartX 聊天卡片 ID
- **跨服转发**：Redis Pub/Sub 或 BungeeCord/Velocity 代理通道，多服消息互通
- **发言冷却与重复检测**：防刷屏，可配置冷却时间和重复消息窗口
- **数据持久化**：SQLite 或 MySQL 存储聊天记录和玩家状态
- **TrChat 迁移**：内置旧版 TrChat 配置迁移工具

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 聊天卡片、物品展示卡片、客户端提示包 | 基础 Bukkit 聊天可拦截，但 AXS 的可视化卡片能力不可用 |
| 可选 | PlaceholderAPI | 解析频道格式、卡片文本、展示文本里的 `%...%` | 对应变量不会动态替换 |
| 可选 | Redis 服务 | 多服聊天转发、去重和远程节点同步 | 单服聊天正常，跨服互通关闭 |
| 可选 | MySQL 服务 | 多服共享聊天记录和玩家状态 | 默认 SQLite 可用；多服共享建议改 MySQL |

## 启用步骤

```yaml
modules:
  chat:
    enabled: true
```

## 关键配置（`ArcartXChat.yml`）

```yaml
settings:
  debug: false
  default-channel: "global"

channels:
  global:
    enabled: true
    format: "&7[全服] &f{player}: {message}"
    range: -1
  local:
    enabled: true
    format: "&a[本地] &f{player}: {message}"
    range: 100
```

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs chat status` | 查看聊天模块状态、存储、Redis 和 Proxy 连接信息 |
| `/axs chat reload` | 重载聊天频道、过滤和跨服配置 |
| `/axs chat mute <玩家> <时长> [原因]` | 禁言玩家。时长如 `30m`、`12h`、`7d`、`permanent`（永久） |
| `/axs chat unmute <玩家>` | 解除玩家禁言 |
| `/axs chat spy <玩家> <on\|off>` | 开启/关闭对指定玩家的私聊监听 |

### 玩家命令（权限：`arcartxsuite.chat.use`）

| 命令 | 说明 |
| --- | --- |
| `/chat` | 查看当前聊天状态（频道、私聊开关、忽略列表等） |
| `/chat channel <频道ID>` | 切换聊天频道，之后发送的消息进入该频道 |
| `/chat toggle private [on\|off]` | 开启/关闭私聊接收 |
| `/chat toggle mentions [on\|off]` | 开启/关闭 @提及通知 |
| `/chat ignore <玩家>` | 屏蔽指定玩家，不再看到对方消息 |
| `/chat unignore <玩家>` | 取消屏蔽 |
| `/chat socialspy [on\|off]` | 开启/关闭社交监听（可查看他人私聊） |
| `/msg <玩家> <消息>` | 向指定在线玩家发送私聊（权限：`arcartxsuite.chat.msg`） |
| `/reply <消息>` | 快速回复最近一次私聊你的玩家 |

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
