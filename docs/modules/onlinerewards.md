---
title: OnlineRewards 在线奖励插件 | ArcartX-Suite
description: ArcartX-Suite OnlineRewards 在线时长阶段奖励、每日签到、连续签到、补签卡、四维排行榜、跨服同步，Minecraft 服务器签到插件。
---

# OnlineRewards 在线奖励

## 功能定位

在线时长奖励、每日签到、补签卡、四维排行榜（日/周/月/总）。

### 核心特性

**在线时长：**
- **四维统计**：今日、本周、本月、总在线时长自动统计
- **阶段奖励**：按今日累计在线分钟设置多个阶段，达标自动发放命令奖励和邮件预设
- **时长倍率**：权限组加速（如 VIP 2x 在线计时），匹配多个权限时取最高优先级
- **客户端进度变量**：实时推送 `arcartx_online_time`（0.0~1.0）和阶段标题到 ArcartX 客户端

**签到系统：**
- **每日签到**：`/signin` 一键签到，支持登录提醒
- **连续签到奖励**：连续签到恰好 N 天时触发额外奖励（如连续 3 天、7 天）
- **累计签到奖励**：累计签到恰好 N 天时触发额外奖励（如累计 10 天、30 天）
- **每月日期奖励**：每月指定日期签到可触发专属奖励（如 1 号、15 号）
- **节日签到奖励**：指定月日签到触发节日奖励（如元旦、中秋）
- **权限额外奖励**：拥有特定权限时签到额外发放一组奖励
- **补签卡**：管理员可发放补签卡，玩家通过 UI 消耗补签卡补签本月未签到日期
- **Mail 联动**：签到和阶段奖励均支持通过 Mail 模块发送预设邮件

**排行榜与跨服：**
- **四维排行榜**：日榜、周榜、月榜、总榜，Top 1~10 通过 PAPI 输出
- **CrossServer 跨服同步**：签到、补签、管理操作后通知其他子服刷新（需 MySQL 共享库 + 宿主 cross-server）

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 在线进度、签到 UI、排行榜 UI 和客户端提示 | 模块无法展示可视化奖励界面 |
| 可选 | PlaceholderAPI | 输出在线/签到排行榜 PAPI，解析奖励条件 | PAPI 输出和条件解析不可用 |
| 可选 | Mail 模块 | 签到、在线阶段奖励通过预设邮件发放 | 邮件奖励跳过或需要改为命令奖励 |
| 可选 | Vault | 奖励/补签卡等配置中使用金币时扣发款 | Vault 货币动作不可用 |
| 可选 | 宿主 cross-server | 多服刷新签到/补签/管理操作后的客户端 UI | 单服正常；跨服同步关闭 |
| 可选 | MySQL 服务 | 多服共享在线与签到数据 | 默认 SQLite 可用；跨服必须使用共享 MySQL |

## 启用步骤

```yaml
modules:
  onlinerewards:
    enabled: true
```

## 配置

::: tip 多 UI 发包
`ui.menu-ui-id` 支持列表格式，可同时向多个 UI 推送同一份 payload，详见 [多 UI 同时发包](/guide/multi-ui)。
:::

配置分为主配置和两个独立外部文件：

```yaml
# ArcartXOnlineRewards.yml（主配置）
cross-server:
  enabled: false

# 签到配置文件路径，相对模块数据目录。
sign-in-file: "sign-in.yml"

# 在线阶段奖励文件路径，相对模块数据目录。
rewards-file: "rewards.yml"
```

跨服需 **MySQL 共享库** + 宿主 `config.yml` → `cross-server`。详见 [跨服功能配置指南](/guide/cross-server-setup)。

### 在线阶段奖励（`data/onlinerewards/rewards.yml`）

```yaml
rewards:
  - minutes: 30
    name: "第一阶段"
    rewardText: "在线 30 分钟"
    commands:
      - "give {player} diamond 1"

  - minutes: 60
    name: "第二阶段"
    rewardText: "在线 1 小时"
    mail-presets:
      - "online_reward_1h"
```

### 签到配置（`data/onlinerewards/sign-in.yml`）

```yaml
reminder-on-join: true

messages:
  sign-in-success: "§a今日签到成功，连续 {streak} 天。"
  sign-in-repeat: "§e今天已经签到过了。"

# 每次成功签到执行的命令
base-commands:
  - "give {player} emerald 1"

# 连续签到里程碑奖励
streak-rewards:
  - days: 7
    commands:
      - "give {player} diamond 3"

# 累计签到里程碑（恰好等于 days 时触发）
total-rewards:
  - days: 30
    mail-presets: ["signin_30d"]

# 每月指定日期奖励（day-of-month-rewards）
day-of-month-rewards:
  - day: 1
    commands: ["give {player} gold_ingot 1"]

# 节日奖励（holiday-rewards，按 month + day）
holiday-rewards:
  - month: 1
    day: 1
    name: "元旦签到"
    commands: []

# 权限额外奖励（permission-bonus-groups，取 priority 最高匹配）
permission-bonus-groups:
  - permission: "ArcartXSuite.onlinerewards.signin.bonus"
    priority: 10
    commands: []

# 补签卡（makeup）
makeup:
  enabled: true
  card-name: "补签卡"
```

### 主配置扩展（`ArcartXOnlineRewards.yml`）

除今日阶段奖励外，主配置还支持：

```yaml
time-bonus:
  permission-groups:
    - permission: "ArcartXSuite.onlinerewards.time.2"
      multiplier: 2.0
      priority: 20

weekly-rewards:      # 本周累计在线时长里程碑
  - id: "weekly_5h"
    minutes: 300
    commands: []

monthly-rewards:     # 本月累计在线时长里程碑
  - id: "monthly_10h"
    minutes: 600
    mail-presets: []

offline-savings:     # 离线时长储蓄（次日登录加成）
  enabled: true
  max-minutes: 120
  storage-rate: 1.0

server-sign-in-goal: # 全服签到人数目标，达成后给在线玩家发奖
  enabled: true
  targets:
    - id: "goal_50"
      required: 50
      broadcast: "§e今日 50 人签到已达成！"
```

### UI 菜单

```yaml
ui:
  menu-ui-id: AXS:online_rewards_menu
  packet-id: AXS_ONLINE_REWARDS
  register-ui-on-enable: true
```

玩家通过 `/onlinerewards open` 打开菜单，可查看在线进度、签到日历、补签与排行榜。`menu-ui-id` 支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)。

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs onlinerewards status` | 查看在线奖励、签到和排行榜状态 |
| `/axs onlinerewards reload` | 重载在线奖励配置和 UI |
| `/axs onlinerewards add\|remove\|set <时长> <玩家>` | 修改玩家在线时长。`add` 增加、`remove` 减少、`set` 设为指定值。时长如 `30m`、`2h`、`1d` |
| `/axs onlinerewards card add\|remove\|set <数量> <玩家>` | 修改玩家的补签卡数量 |

### 玩家命令（权限：`arcartxsuite.onlinerewards.use`，别名 `/signin`）

| 命令 | 说明 |
| --- | --- |
| `/onlinerewards` 或 `/onlinerewards open` | 打开在线奖励菜单界面 |
| `/onlinerewards status` | 查看自己的在线时长统计和签到状态 |
| `/onlinerewards signin` 或 `/signin` | 进行今日签到 |
| `/onlinerewards top <范围> [页码]` | 查看排行榜。范围：`daily`（日）、`weekly`（周）、`monthly`（月）、`total`（总） |

## PAPI

前缀：`%axsonlinerewards_*%`

### 个人数据

| 占位符 | 说明 |
| --- | --- |
| `%axsonlinerewards_daily_minutes%` | 今日在线分钟数 |
| `%axsonlinerewards_weekly_minutes%` | 本周在线分钟数 |
| `%axsonlinerewards_monthly_minutes%` | 本月在线分钟数 |
| `%axsonlinerewards_total_minutes%` | 总在线分钟数 |
| `%axsonlinerewards_daily_time%` | 今日在线时间（格式化显示） |
| `%axsonlinerewards_weekly_time%` | 本周在线时间（格式化） |
| `%axsonlinerewards_monthly_time%` | 本月在线时间（格式化） |
| `%axsonlinerewards_total_time%` | 总在线时间（格式化） |
| `%axsonlinerewards_signin_signed_today%` | 今日是否已签到（`true`/`false`） |
| `%axsonlinerewards_signin_streak%` | 连续签到天数 |
| `%axsonlinerewards_signin_total%` | 累计签到天数 |
| `%axsonlinerewards_offline_savings_minutes%` | 离线储蓄分钟数 |
| `%axsonlinerewards_server_signin_count%` | 今日全服已签到人数 |
| `%axsonlinerewards_signin_history_yyyy-MM_count%` | 指定月份签到天数（替换日期） |

### 排行榜

格式：`%axsonlinerewards_top_<范围>_<名次>_<字段>%`

| 参数 | 可选值 |
| --- | --- |
| `<范围>` | `daily`、`weekly`、`monthly`、`total` |
| `<名次>` | 1\~10 |
| `<字段>` | `name`（玩家名）、`minutes`（分钟数）、`time`（格式化时间） |

## EventPacket 联动

OnlineRewards 在签到成功时自动向 EventPacket 发射信号：

| 信号名 | 触发时机 | 携带变量 |
| --- | --- | --- |
| `signin_success` | 玩家签到成功 | `streak`, `total`, `date`, `day_of_month` |

可在 `ArcartXEventPacket.yml` 中配置对应规则实现连续签到里程碑奖励、邮件派发等联动效果。

## 故障排查

| 现象 | 排查 |
| --- | --- |
| 跨服签到不同步 | `storage.mode` 必须为 `mysql` 且各子服连接同一库 |
| 阶段奖励未发放 | 检查 `rewards.yml` 的 `minutes` 阈值；VIP 倍率见 `time-bonus` |
| 补签失败 | `makeup.enabled` 是否为 true；玩家是否有补签卡 |
| UI 不显示 | 客户端需安装 ArcartX MOD；检查 `ui.menu-ui-id` 是否已注册 |

