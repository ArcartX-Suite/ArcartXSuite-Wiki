---
title: QQBot QQ群服互联插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite QQBot OneBot 11 双向消息同步、QQ-游戏账号绑定、白名单联动、群指令查玩家数据，我的世界服务器 QQ 群联动插件。
---

# QQBot QQ群服互联

::: tip 付费模块
本模块为付费模块。从 **1.2.0-beta** 起，授权由 [云端平台](/guide/cloud-modules) 统一管理：在 [cloud.021209.xyz](https://cloud.021209.xyz) 购买/领取授权后，于「装备模块」页面勾选到对应服务器即可，无需填写 `password` 或 `license.yml`。
:::

**QQBot** 模块通过 **OneBot 11 正向 WebSocket** 协议连接 QQ 机器人，打通 QQ 群与 Minecraft 服务器之间的消息流转，并提供 QQ-游戏账号绑定、白名单管理、群指令查询玩家数据等完整闭环能力。

## 功能概览

| 子系统 | 核心功能 |
|--------|----------|
| **消息同步** | QQ 群 ↔ 游戏聊天双向转发、玩家进退服 QQ 通知、消息格式自定义、CQ 码过滤 |
| **账号绑定** | 群发 `#绑定 玩家名` 申请、游戏内 `/qqbot bind <code>` 确认、SQLite/MySQL 持久化 |
| **白名单** | 绑定自动加白、解绑自动删白、群管理员 `#加白`/`#删白` 直接管理 |
| **登录门控** | 结合 LoginView 面板实现分级准入：未绑定 QQ 的 LittleSkin/正版玩家进服后通过 UI 面板输入验证码完成绑定，不再在 PreLogin 阶段踢人 |
| **签到积分** | `#签到`/`#打卡` 每日签到 + 连续加成、`#积分` 查询、`#积分榜` 排行、`#商店`/`#兑换` 积分换邮件奖励 |
| **积分转账** | `#转账 <QQ号> <数量>` 群内积分互转，检查绑定 + 原子扣增 |
| **积分商城限时折扣** | `prizes[]` 支持 `discount-rate` + `discount-until`，兑换时自动计算折扣价 |
| **拼手气红包** | `#红包 <总积分> <份数>` 发拼手气红包，`#抢红包` 领取，24h 过期自动退款 |
| **群活跃度** | 每条群消息自动统计发言次数，`#活跃排行 [week|month]` 查本周/本月 Top10 |
| **群指令** | 内置（在线列表/服务器状态/积分榜）+ PlaceholderAPI 查询 + 服务器命令执行 + 自定义扩展 |
| **自动化** | 服务器监控告警、定时消息、击杀播报、**死亡广播**、入群欢迎、关键词自动回复（FAQ） |
| **群管理** | `#公告` 同步游戏内、`#踢`/`#封禁` 远程管理、QQ 禁言同步封禁绑定玩家、**关键词自动撤回+禁言** |
| **黑名单** | 配置化禁止指定 QQ 号使用机器人（指令/消息同步/欢迎/签到等全部拦截） |
| **双向@联动** | 群内 @QQ → 游戏内 Title 提示；游戏内 `/qqbot at <QQ> <消息>` → 广播到 QQ 群 |
| **跨模块** | `QQBotBroadcastable` 推送消息、`QQBotNotifiable` 反向监听群事件、`MailDispatchable` 发放兑换奖励 |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | 模块加载（无 UI 资源） |
| OneBot 11 实现端 | ✅ 必须 | SnowLuma（推荐）/ NapCat / LLBot 等 |
| SQLite (内置) | 默认 | 绑定数据持久化 |
| MySQL | 可选 | 跨服共享绑定数据时使用 |
| PlaceholderAPI | 可选 | 群指令查询玩家数据 |
| Chat 模块 | 可选 | 推荐共同使用获得更完整的聊天体验 |

::: warning OneBot 实现端
本模块**不提供云端服务**，需要你自行部署 OneBot 11 实现端。推荐方案：
- **[SnowLuma](https://github.com/SnowLuma/SnowLuma)** ⭐ 推荐 — TypeScript 全链路，WebUI 管理面板，多账号并行，NapCat 团队推荐的下一代框架
- **[NapCat](https://github.com/NapNeko/NapCatQQ)** — 基于 NTQQ，9k+ Stars，社区最大
- **[LLBot](https://github.com/LLOneBot/LuckyLilliaBot)** — 支持 OneBot 11 / Satori / Milky 多协议
:::

## 命令

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/qqbot bind <验证码>` | `arcartxsuite.qqbot.use` | 确认绑定群内申请生成的验证码 |
| `/qqbot unbind` | `arcartxsuite.qqbot.use` | 解除当前账号的 QQ 绑定 |
| `/qqbot info` | `arcartxsuite.qqbot.use` | 查看当前绑定的 QQ 号 |
| `/qqbot at <QQ号> <消息>` | `arcartxsuite.qqbot.use` | 向 QQ 群发送 @ 指定 QQ 的消息 |

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs qqbot status` | `arcartxsuite.qqbot.admin` | 查看连接/群数/绑定/白名单状态 |
| `/axs qqbot reload` | `arcartxsuite.qqbot.admin` | 重载配置 |
| `/axs qqbot send all <消息>` | `arcartxsuite.qqbot.admin` | 向所有已配置群发送消息 |
| `/axs qqbot send <群号> <消息>` | `arcartxsuite.qqbot.admin` | 向指定群发送消息 |
| `/axs qqbot lookup <玩家名\|QQ号>` | `arcartxsuite.qqbot.admin` | 查询绑定关系（双向） |
| `/axs qqbot snowluma install` | `arcartxsuite.qqbot.admin` | 从 GitHub 下载安装/更新（原生）或拉取镜像/运行容器（Docker） |
| `/axs qqbot snowluma start` | `arcartxsuite.qqbot.admin` | 启动 SnowLuma（子进程 / Docker 容器） |
| `/axs qqbot snowluma stop` | `arcartxsuite.qqbot.admin` | 停止 SnowLuma |
| `/axs qqbot snowluma status` | `arcartxsuite.qqbot.admin` | 查看 SnowLuma 安装/运行状态及版本号 |
| `/axs qqbot snowluma logs` | `arcartxsuite.qqbot.admin` | 查看 Docker 容器最近日志（仅 Docker 模式） |
| `/axs qqbot snowluma check-update` | `arcartxsuite.qqbot.admin` | 检查 GitHub Release 是否有新版本 |
| `/axs qqbot blacklist add <QQ号>` | `arcartxsuite.qqbot.admin` | 将指定 QQ 加入黑名单 |
| `/axs qqbot blacklist remove <QQ号>` | `arcartxsuite.qqbot.admin` | 将指定 QQ 移出黑名单 |
| `/axs qqbot blacklist list` | `arcartxsuite.qqbot.admin` | 查看当前数据库黑名单列表 |

### 群内指令（默认前缀 `#`）

| 群指令 | 权限 | 说明 |
|--------|------|------|
| `#帮助` | 群员 | 显示当前可用指令列表（根据启用功能动态生成） |
| `#绑定 <玩家名>` | 群员 | 申请绑定，机器人返回 6 位验证码 |
| `#解绑` | 群员 | 解除自己 QQ 的绑定 |
| `#查绑 [玩家名]` | 群员 | 查询自己/指定玩家的绑定 |
| `#加白 <玩家名>` | 群管/群主 | 添加白名单（兼容旧指令） |
| `#删白 <玩家名>` | 群管/群主 | 移除白名单（兼容旧指令） |
| `#白名单添加 <玩家名>` | 群管/群主 | 添加白名单 |
| `#白名单移除 <玩家名>` | 群管/群主 | 移除白名单 |
| `#白名单列表` | 群管/群主 | 查看服务器当前白名单 |
| `#查在线` | 群员 | 内置：返回在线玩家列表 |
| `#查服务器` | 群员 | 内置：返回 TPS / 内存 / 实体 / 区块 |
| `#查玩家 [玩家名]` | 群员 | PAPI 示例：等级 / 金币 / 生命 / 饥饿 |
| `#执行命令 <命令>` | 群管/群主 | 在控制台执行任意命令 |
| `#签到` / `#打卡` | 群员 | 每日签到领积分，连续签到有加成 |
| `#积分` | 群员 | 查询积分余额、累计获得/消费 |
| `#积分榜` | 群员 | 内置：积分排行榜 TOP10 |
| `#商店` | 群员 | 查看积分兑换商店奖品列表（含折扣标识） |
| `#兑换 <编号>` | 群员 | 消费积分兑换奖品（邮件发放给绑定玩家，自动折扣） |
| `#转账 <QQ号> <数量>` | 群员 | 积分转账给指定 QQ（需双方绑定） |
| `#红包 <总积分> <份数>` | 群员 | 发拼手气积分红包（24h 过期退款） |
| `#抢红包` | 群员 | 抢当前群的拼手气红包 |
| `#活跃排行 [week/month]` | 群员 | 查看本周/本月群活跃度 TOP10 |
| `#公告 <内容>` | 群管/群主 | 发布公告，同步到游戏内聊天栏 + 标题 |
| `#踢 <玩家名> [原因]` | 群管/群主 | 远程踢出玩家 |
| `#封禁 <玩家名> [原因]` | 群管/群主 | 远程封禁玩家 |
| `#黑名单添加 <QQ号>` | 群管/群主 | 将指定 QQ 加入黑名单（禁止其使用机器人） |
| `#黑名单移除 <QQ号>` | 群管/群主 | 将指定 QQ 移出黑名单 |
| `#黑名单列表` | 群管/群主 | 查看当前数据库黑名单列表 |

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `arcartxsuite.qqbot.use` | true | 玩家命令 |
| `arcartxsuite.qqbot.admin` | op | 管理员命令 |

## PAPI 占位符

前缀：`%axsqqbot_xxx%`

| 占位符 | 说明 |
|--------|------|
| `%axsqqbot_connected%` | 机器人连接状态（true/false） |
| `%axsqqbot_connected_display%` | 机器人连接状态（已连接/未连接） |
| `%axsqqbot_is_bound%` | 当前玩家是否已绑定（true/false） |
| `%axsqqbot_bound_qq%` | 当前玩家绑定的 QQ 号 |
| `%axsqqbot_bound_name%` | 当前玩家绑定时的游戏名 |
| `%axsqqbot_group_count%` | 已配置监听的群数量 |

## 配置文件

### 主配置 `ArcartXQQBot.yml`

```yaml
settings:
  debug: false
  server-id: "survival"          # 服务器标识（多服时区分来源）

# OneBot 11 正向 WebSocket
onebot:
  ws-url: "ws://127.0.0.1:3001"  # OneBot 实现端的 WS 地址（SnowLuma 默认 3001）
  access-token: ""               # 鉴权 token（需与 SnowLuma WS 节点一致，留空则两边都不设）
  reconnect-interval-seconds: 10
  heartbeat-interval-seconds: 30
  snowluma:
    # 运行模式：native（本地子进程）/ docker（Docker 容器，推荐 Linux VPS）
    mode: "native"
    dir: "snowluma"              # 安装目录（相对服务端根目录，仅 native 模式有效）
    auto-start: false            # 模块启动时自动拉起 SnowLuma
    docker:
      container-name: "snowluma"
      image: "motricseven7/snowluma:latest"
      webui-port: 5099
      ws-port: 3001
      http-port: 3000
      auto-install: false        # 模块启动时若容器不存在则自动拉取并运行容器

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
  add-prefix: "#白名单添加"
  remove-prefix: "#白名单移除"
  list-prefix: "#白名单列表"

# 白名单登录门控（v3.0 架构）
# 【重要】QQ 绑定验证已从 PreLogin 拦截完全迁移到 LoginView 登录面板中完成。
# 本配置保留向后兼容和日志审计，实际的绑定控制由 LoginView 的 qq-binding 节接管。
whitelist-login:
  enabled: false
  # 以下字段仅供日志参考，不再用于实际拦截：
  microsoft-pass: true
  littleskin-require-bind: true
  deny-offline: true
  kick-not-bound: "&c你还未在QQ群完成绑定认证\n&7请在QQ群发送: #绑定 {name}\n&7完成验证后方可进入游戏"
  kick-offline: "&c本服务器仅允许正版/LittleSkin 账号登录"
  kick-denied: "&c你没有权限进入本服务器"

# 自定义群指令
command-prefix: "#"
help-prefix: "#帮助"              # 显示可用指令列表
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
  积分榜:
    permission: 0
    type: "builtin"
    builtin-id: "points-leaderboard"

# 签到打卡 + 积分系统
signin:
  enabled: true
  base-points: 10                 # 每日基础积分
  streak-bonus: 2                 # 每连续 1 天额外加分
  max-streak-bonus: 50            # 连续加成上限
  sign-prefix: "#签到"
  aliases: ["#打卡"]
  shop-prefix: "#商店"
  redeem-prefix: "#兑换"
  points-query-prefix: "#积分"
  transfer-prefix: "#转账"         # 积分转账
  red-packet-prefix: "#红包"      # 发拼手气红包
  grab-red-packet-prefix: "#抢红包" # 抢红包
  activity-prefix: "#活跃排行"    # 群活跃度排行

# 积分兑换奖品（通过 Mail 模块邮件发放，mail-preset-id 对应邮件预设）
prizes:
  - id: "diamond"
    name: "钻石礼包"
    cost: 100
    mail-preset-id: "qqbot_diamond"
    description: "10 颗钻石"
    limit-per-day: 1              # 每日兑换上限（0=不限）
    require-bind: true            # 是否需要先绑定游戏账号
    discount-rate: 1.0            # 折扣比例（0.8=八折），不折扣填 1.0 或省略
    discount-until: 0             # 折扣截止 unix 时间戳（毫秒），0=无折扣

# 服务器监控告警
monitor:
  enabled: false
  tps-threshold: 15.0
  memory-threshold-percent: 90
  check-interval-seconds: 60
  cooldown-seconds: 300          # 同类告警冷却，防刷屏
  alarm-groups: []               # 接收告警的群（留空=所有群）
  tps-alarm-format: "⚠ 服务器告警\nTPS 过低: {tps}（阈值 {threshold}）"
  memory-alarm-format: "⚠ 服务器告警\n内存占用过高: {used}MB/{max}MB ({percent}%)"

# 定时消息（interval=固定间隔 / daily=每日定时；占位符 {online}/{max}/{tps}）
scheduled-messages:
  - id: "online_report"
    mode: "interval"
    interval-seconds: 21600
    message: "📊 当前在线人数: {online}/{max}"
    target-groups: []
  - id: "daily_signin_reminder"
    mode: "daily"
    daily-time: "12:00"
    message: "🎁 别忘了发送 #签到 领取今日积分哦~"
    target-groups: []

# 击杀播报
broadcast:
  kill:
    enabled: false
    format: "🗡 {killer} 击杀了 {victim}"
    boss-only: true              # 仅播报 Boss（按 boss-keywords 匹配）
    player-kill-only: false      # 仅 PvP（优先级高于 boss-only）
    boss-keywords: ["Boss", "首领", "魔王"]
  death:
    enabled: false
    format: "☠ {player} 死亡了"   # 玩家死亡广播（独立于击杀播报）

# 入群欢迎
welcome:
  enabled: false
  message: "欢迎新成员加入！\n发送 #绑定 <游戏名> 关联你的游戏账号"

# 关键词自动回复（FAQ）
auto-reply:
  enabled: false
  cooldown-seconds: 30           # 每群两次回复最小间隔
  rules:
    - keywords: ["怎么进服", "服务器地址", "ip"]
      response: "服务器地址: play.example.com"
      exact-match: false         # true=完全匹配, false=包含即触发

# 群公告广播
announce:
  enabled: false
  prefix: "#公告"
  game-format: "&e&l[群公告] &f{content}"
  qq-receipt: "✓ 公告已发布到游戏内"
  title-enabled: true
  title-text: "&e群公告"
  subtitle-format: "&f{content}"

# 群管理 moderation（踢人/封禁 + QQ 禁言同步）
moderation:
  enabled: false
  kick-prefix: "#踢"
  ban-prefix: "#封禁"
  kick-command: "kick {name} {reason}"
  ban-command: "ban {name} {reason}"
  sync-ban:
    enabled: false               # QQ 群禁言 → 游戏内封禁绑定玩家
    command: "tempban {name} {duration} {reason}"
    use-duration: true           # 按 QQ 禁言时长设置封禁时长
    reason: "QQ群禁言同步"
  # 关键词自动撤回 + 禁言（广告/敏感词拦截）
  auto-moderation:
    enabled: false
    keywords: ["http://", "https://", "www.", "点击", "加群"]
    ban-duration-seconds: 600     # 命中后禁言时长
    cooldown-seconds: 300         # 同一群两次自动 moderation 最小间隔

# 群内 @ → 游戏内提示
at-to-game:
  enabled: true                   # 群内 @ 绑定的 QQ 时，在线玩家收到 Title 提示

# 黑名单（列入的 QQ 号禁止使用机器人的所有功能，支持群指令动态管理）
blacklist:
  enabled: false
  qq-list:
    - 987654321
  add-prefix: "#黑名单添加"
  remove-prefix: "#黑名单移除"
  list-prefix: "#黑名单列表"

# 存储
storage:
  mode: "sqlite"                  # sqlite / mysql
  sqlite-file: "qqbot.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "ArcartX-Suite"
    username: "root"
    password: ""
    table-prefix: "axs_qqbot_"
  pool-size: 4
```

## 自定义指令类型

| 类型 | 字段 | 说明 |
|------|------|------|
| `builtin` | `builtin-id` | 内置指令：`online-list` / `server-status` / `points-leaderboard` |
| `papi-query` | `placeholders[]` + `format` | 解析 PAPI 占位符填充模板（`{0}` `{1}` …） |
| `server-command` | `command` | 控制台执行（`{args}` = 群内参数） |

## 存储结构

QQBot 数据库表（SQLite 默认在 `plugins/ArcartX-Suite/data/qqbot/qqbot.db`，MySQL 使用 `table-prefix`）：

| 表名 | 字段 | 用途 |
|------|------|------|
| `axs_qqbot_bindings` | `id`、`qq_id`、`player_uuid`、`player_name`、`bound_at` | QQ ↔ 游戏账号绑定关系（`UNIQUE(qq_id, player_uuid)`） |
| `axs_qqbot_points` | `qq_id`、`balance`、`total_earned`、`total_spent`、`updated_at` | 积分账户（`qq_id` 主键） |
| `axs_qqbot_signin` | `qq_id`、`sign_date`、`streak`、`signed_at` | 每日签到记录（`PRIMARY KEY(qq_id, sign_date)` 防重复） |
| `axs_qqbot_redeem_log` | `id`、`qq_id`、`prize_id`、`cost`、`redeem_date`、`created_at` | 兑换流水（每日限购统计） |
| `axs_qqbot_blacklist` | `qq_id`、`added_by`、`added_at` | 动态黑名单（`qq_id` 主键，群指令添加/移除） |
| `axs_qqbot_red_packets` | `id`、`sender_qq`、`group_id`、`total_amount`、`remaining_amount`、`count`、`claimed_count`、`expire_at`、`created_at` | 拼手气红包主表 |
| `axs_qqbot_red_packet_claims` | `id`、`red_packet_id`、`claimer_qq`、`amount`、`claimed_at` | 红包领取记录 |
| `axs_qqbot_activity` | `qq_id`、`group_id`、`activity_date`、`message_count` | 群活跃度统计（`PRIMARY KEY(qq_id, group_id, activity_date)`） |

## 配置诊断

QQBot 模块声明了以下配置校验规则：

| 字段 | 类型 | 约束 |
|------|------|------|
| `onebot.ws-url` | STRING | 必填 |
| `storage.mode` | STRING | 必填，枚举 `sqlite` / `mysql` |
| `storage.pool-size` | INT | 范围 1–50 |
| `signin.base-points` | INT | 范围 0–100000 |
| `signin.streak-bonus` | INT | 范围 0–100000 |
| `signin.max-streak-bonus` | INT | 范围 0–1000000 |
| `monitor.tps-threshold` | DOUBLE | 范围 0.0–20.0 |
| `monitor.memory-threshold-percent` | INT | 范围 1–100 |
| `monitor.check-interval-seconds` | INT | 范围 5–86400 |

动态节（用户可自由增删，不被结构同步覆盖）：
- `groups`
- `custom-commands`
- `prizes`
- `scheduled-messages`
- `auto-reply.rules`

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

### 白名单登录门控（v2.0 架构）

> **架构变更**：QQ 绑定验证已从 `AsyncPlayerPreLoginEvent` 拦截迁移到 **LoginView 登录面板**中完成。

```
LittleSkin/正版玩家 进服
    ↓
LoginView 面板弹出
    ↓
已绑定 QQ ? ──是──→ 显示「进入服务器」按钮 ──→ 点击进服
    │
    否
    ↓
显示绑定提示 + 验证码输入框
    ↓
玩家在 QQ 群发送「#绑定 <玩家名>」
    ↓
机器人返回 6 位验证码
    ↓
玩家在 UI 中输入验证码
    ↓
验证通过 → 自动完成 QQ 绑定 + 加白名单
    ↓
显示「进入服务器」按钮 → 点击进服
```

**与旧版区别**：
- **旧版**（v1.x）：未绑定玩家在 `AsyncPlayerPreLoginEvent` 阶段直接被踢出，需先绑定才能进服
- **新版**（v2.0）：未绑定玩家可以进服，通过 LoginView UI 面板完成绑定验证后才允许进入服务器

**必要条件**：
- LoginView 模块启用且 `premium-bypass.enabled: true`
- LoginView 配置 `qq-binding.enabled: true`
- QQBot 模块启用

## 架构

```
QQBotModule (AbstractAXSModule)
├── QQBotService (主服务，Chat / Join / Quit / EntityDeath / PlayerDeath 监听 + notice 事件处理)
│   ├── QQBotBindService (绑定逻辑 + 验证码池)
│   └── QQBotCommandRouter (群指令路由器：绑定/白名单/签到/商店/兑换/转账/红包/活跃度/公告/踢封)
├── QQBotSignInService (签到 + 积分 + 兑换商店 + 转账 + 拼手气红包 + 活跃度统计，邮件发放奖励)
├── QQBotWeeklyRankService (每周日 23:59 自动推送积分 Top10 到群)
├── QQBotMonitorService (TPS/内存监控告警，周期推送)
├── QQBotScheduledMessageService (定时消息：interval / daily)
├── OneBotClient (Java-WebSocket 库，自动重连 + 心跳 + 指数退避)
│   ├── OneBotAction (动作 JSON 构造：群消息 / @ / 禁言 / 撤回消息)
│   └── OneBotEvent (事件解析：message / notice 入群·禁言·群消息含 @ 段)
├── JdbcQQBotRepository (HikariCP + SQLite/MySQL，8 张表)
├── QQBotPlayerCommand (/qqbot)
├── QQBotAdminCommand (/axs qqbot)
├── QQBotPlaceholderExpansion (PAPI)
├── QQBotUiService (绑定中心 / 通知 HUD / 管理后台 UI)
└── 注册 QQBotBroadcastable / QQBotNotifiable capability
```

## SnowLuma 快速接入

推荐使用 [SnowLuma](https://github.com/SnowLuma/SnowLuma) 作为 OneBot 11 实现端。以下按运行模式分别说明。

### 模式选择：native vs docker

| 模式 | 适用场景 | 特点 |
|------|---------|------|
| **native**（默认） | Windows / Linux 有图形环境 | 本地子进程，解压即用，支持 WebUI 扫码登录 |
| **docker** | Linux VPS（无图形环境） | 容器运行，内置 Xvfb + noVNC，**需 Docker 环境** |

::: warning Linux native 的局限
SnowLuma 官方说明：**"Linux 版本仅提供 OneBot 桥接能力，NTQQ 进程注入仅在 Windows 下生效。"**
- **Linux native 模式**：无 NTQQ 客户端 hook，功能和 NapCat 类似
- **Linux Docker 模式**：容器内完整环境（Linux QQ + Xvfb + SnowLuma），**有完整客户端能力**，和 Windows 体验一致
:::

---

### Native 模式（Windows / Linux）

#### 1. 安装 SnowLuma

ArcartX-Suite 附带了一键脚本，**自动下载最新版 SnowLuma 并启动**（需要 Node.js 18+）：

```bash
# Windows — 双击或命令行执行
start-snowluma.bat

# Linux
chmod +x start-snowluma.sh && ./start-snowluma.sh
```

脚本会自动从 GitHub Release 下载、解压到 `snowluma/` 目录，后续再次运行会直接启动（不重复下载）。

> **架构自动匹配**：安装脚本会根据系统架构自动下载正确的构建产物：
> - `SnowLuma-vX.Y.Z-win-x64.zip`
> - `SnowLuma-vX.Y.Z-linux-x64.tar.gz`
> - `SnowLuma-vX.Y.Z-linux-arm64.tar.gz`

首次启动后 WebUI 默认监听 `5099` 端口，控制台会打印初始密码：
```
initial credentials: user=admin password=<随机密码>
```

#### 2. 登录 QQ 账号

浏览器打开 `http://127.0.0.1:5099`，用初始密码登录 WebUI，然后扫码或密码登录你的 QQ 机器人账号。

#### 3. 配置 WebSocket 适配器

在 SnowLuma WebUI 中操作：

1. 左侧菜单点击 **节点配置**
2. 选择你刚登录的 QQ 账号（显示 UIN 号码）
3. 点击顶部 **WS 服务端** 选项卡
4. 你会看到一条默认节点 **ws-default**，监听 `0.0.0.0:3001/`
5. 点击右侧 **编辑** ✏️ 按钮，查看或设置 **Access Token**
6. 记住这个 Token 值（如果不需要鉴权可以清空，然后点保存）

::: warning Access Token 必须一致
如果 SnowLuma 的 WS 节点设置了 Token，QQBot 配置也**必须填写完全相同的 Token**，否则连接会被服务端以 `code=1008 invalid access token` 拒绝。
:::

#### 4. 配置 ArcartX-Suite QQBot

编辑 `plugins/ArcartX-Suite/data/qqbot/ArcartXQQBot.yml`：

```yaml
onebot:
  ws-url: "ws://127.0.0.1:3001"     # 端口与 SnowLuma WS 节点一致
  access-token: "你在 SnowLuma 设置的 token"  # 留空则两边都不设
  snowluma:
    mode: "native"
    dir: "snowluma"
    auto-start: false
```

#### 5. 验证连接

重载模块或重启服务器：
```
/axs qqbot reload
```

---

### Docker 模式（Linux VPS 推荐）

#### 1. 前提条件

确保服务器已安装 Docker，且当前用户有 docker 权限（或在 `root` 下运行）。

#### 2. 配置 ArcartX-Suite QQBot

编辑 `ArcartXQQBot.yml`，切换到 `docker` 模式：

```yaml
onebot:
  ws-url: "ws://127.0.0.1:3001"
  access-token: ""
  snowluma:
    mode: "docker"
    auto-start: true
    docker:
      container-name: "snowluma"
      image: "motricseven7/snowluma:latest"
      webui-port: 5099
      ws-port: 3001
      http-port: 3000
      auto-install: true
```

#### 3. 启动

执行：
```
/axs qqbot snowluma install
/axs qqbot snowluma start
```

或直接重启服务器（`auto-install: true` 时模块启动会自动拉取镜像并运行容器）。

#### 4. 登录 QQ

容器启动后，WebUI 映射到宿主机 `5099` 端口。在浏览器访问：
```
http://<VPS_IP>:5099
```

> 注意：VPS 需开放 `5099` 端口，或配置反向代理 / SSH 隧道。

#### 5. Docker 管理命令

```
/axs qqbot snowluma status      # 查看容器运行状态 + 版本号
/axs qqbot snowluma logs       # 查看容器最近 50 行日志
/axs qqbot snowluma stop       # docker stop
/axs qqbot snowluma start      # docker start
/axs qqbot snowluma install    # docker pull + docker run（更新镜像时重新执行）
```

::: tip 版本检测
执行 `/axs qqbot snowluma check-update` 可异步检查 GitHub 最新版本，与本地版本比对后提示是否需要更新。
:::

::: tip 多账号
SnowLuma 支持同一进程托管多个 QQ 账号，每个账号独立 WebSocket 端口。如果你有多个群需要不同机器人，可以在 SnowLuma 中配置多账号，ArcartX-Suite 侧只需连接主账号即可。
:::

## Mixed Auth Mode（混合认证）

若你的服务器需要同时支持 **LittleSkin + 微软正版 + 离线** 三种账号，必须按以下步骤配置：

### 1. 生成混合认证启动脚本

**`?mixed` 并非 authlib-injector 的有效参数，不能直接加到 JVM Agent 参数中。** 它是 ArcartX-Suite 的「启用混合代理」开关，只有当 `/axs auth setup` 检测到 `config.yml` 的 `auth.yggdrasil-source` 包含 `?mixed` 时，才会生成独立的本地混合代理进程。

执行以下命令生成正确的启动脚本：

```
/axs auth setup https://littleskin.cn/api/yggdrasil?mixed
```

或在 `config.yml` 中预先设置好 `auth.yggdrasil-source` 后再执行：

```
/axs auth setup
```

该命令会生成 `start-mixed-auth.bat`（Windows）或 `start-mixed-auth.sh`（Linux），其工作流程为：

1. **先独立启动**本地 Yggdrasil 混合代理（监听 `auth.mixed-proxy-port`，默认 25599）
2. 等待代理端口就绪
3. 再启动服务器，authlib-injector 指向 `http://127.0.0.1:25599`

代理行为：`hasJoined` 先查 LittleSkin，未命中再 fallback 到 Mojang 官方，让 LittleSkin 玩家与微软正版玩家都能进服。

### 2. 开启正版验证

编辑 `server.properties`：

```properties
online-mode=true
```

**必须开启**，否则 LittleSkin 玩家会被分配 v3 离线 UUID，无法与真正的离线玩家区分。

### 3. 配置 QQ 绑定（LoginView）

编辑 `ArcartXLoginView.yml`：

```yaml
qq-binding:
  enabled: true
  microsoft-require-bind: false   # 微软正版是否需要绑定 QQ
  littleskin-require-bind: true   # LittleSkin 是否需要绑定 QQ
```

::: tip 代理端离线拦截
Velocity/BungeeCord 群组服在**代理服** `plugins/` 部署对应平台的 Proxy jar（`ArcartXSuite-Proxy-Velocity` 或 `ArcartXSuite-Proxy-Bungee`），配合 `online-mode=true` 与 MultiLogin / authlib-injector 实现入口认证与离线拦截；**子服不要装 Proxy jar**。完整目录结构与 BC/VC 示例见 [Proxy 代理端插件](/guide/proxy-usage)。
:::

### 4. 账号判定流程

```
玩家连接
  ├─ LittleSkin 账号（通过 LS yggdrasil 认证）→ v4 UUID，查 Mojang UUID 不同 → LITTLESKIN
  ├─ 微软正版（通过 Mojang 验证）→ v4 UUID，查 Mojang UUID 一致 → MICROSOFT
  └─ 离线玩家（未通过任何验证）→ 无法连接（online-mode=true 已被服务端拒绝）
```

- **MICROSOFT**：由 LoginView `microsoft-require-bind` 控制；false 直接放行，true 需 QQ 绑定
- **LITTLESKIN**：由 LoginView `littleskin-require-bind` 控制；false 直接放行，true 需 QQ 绑定
- **OFFLINE**：`online-mode=true` 下离线玩家已被服务端拒绝，不会到达本插件门控

## 安全建议

- **OneBot 实现端** 建议监听 `127.0.0.1` 仅本机访问，并配置 `access-token`
- **远程连接** 时务必启用 token，并通过 SSH 隧道 / VPN / 反向代理加密链路
- **`#执行命令`** 默认仅群管/群主可用，建议结合 `permission: 1` 严格控制
- **白名单联动** 在不熟悉时可先关闭 `auto-add-on-bind`，待绑定流程稳定后再启用

