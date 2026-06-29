---
title: AfkReward 挂机奖励插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite AfkReward 区域挂机 + 原地挂机双模式、周期命令奖励、VIP 权限阶梯、排行榜、HUD 面板，我的世界服务器挂机插件。
---

# AfkReward 挂机奖励

## 功能定位

AfkReward 模块为服务器提供**双模式挂机奖励**系统：
- **区域挂机（REGION）**：玩家进入指定多边形区域后自动累计时长，按周期发放奖励。
- **原地挂机（MANUAL）**：玩家通过命令主动选择区域并传送至挂机点，挂机期间行为被封锁，结束挂机后一次性结算奖励。

支持多区域、多奖励类型、多级 VIP 权限阶梯、人数/次数上限控制、服崩恢复和排行榜。

### 核心特性

| 分类 | 功能 |
| --- | --- |
| 区域定义 | 基于 `X,Z` 多边形角坐标定义任意形状挂机区域，支持多世界 |
| 原地挂机 | 玩家可主动选择区域传送挂机，支持多世界传送、行为封锁、结束结算 |
| 周期奖励 | 按配置周期（分钟）自动发放，支持命令奖励 + PAPI 变量 + 简单四则运算 |
| 权限阶梯 | 同一区域内按 `vip3 > vip2 > vip1 > common` 权限匹配不同奖励档次 |
| 人数上限 | 限制区域同时挂机人数，满员后新玩家进入会收到提示 |
| 次数上限 | 每日最大奖励次数限制，防止过度收益 |
| 统计追踪 | 当前挂机时长、总挂机时长、今日/总奖励次数，支持 PAPI 输出 |
| 排行榜 | 按总挂机时长排序的在线排行榜 |
| 服崩恢复 | 服崩后自动检测并结算未正常结束的原地挂机记录 |
| HUD 面板 | ArcartX UI 实时显示当前区域、挂机时长、下次奖励倒计时、区域人数 |
| 数据持久化 | SQLite / MySQL 双存储，支持跨服共享 |

---

## 启用步骤

```yaml
modules:
  afkreward:
    enabled: true
```

模块首次启用时会自动导出默认配置文件到 `data/afkreward/` 目录，并创建数据表。

---

## 快速开始

### 1. 定义区域（双模式）

每个挂机区域使用**独立配置文件**，放置在 `data/afkreward/areas/` 目录下，文件名为 `area-<id>.yml`（例如 `area-default.yml`）。

示例 `area-default.yml`：

```yaml
# 区域唯一 ID（内部标识）
id: default

# 区域显示名称（支持中文）
name: "温泉"

# 是否启用
enabled: true

# 所属世界
world: bskyblock_world

# 多边形坐标点（射线法判断）
pos:
  - "-11,138"
  - "17,114"
  - "40,114"
  - "48,128"
  - "51,138"
  - "42,167"
  - "20,172"
  - "10,171"
  - "-4,159"

# 关联奖励类型（对应主配置 types 中的 key）
type: 泡澡池

# 是否支持原地挂机（/afk start 此区域）
manual-enabled: true

# 原地挂机传送点
teleport:
  world: bskyblock_world
  x: 20.5
  y: 64.0
  z: 140.5
  yaw: 0
  pitch: 0
```

- `pos` 为多边形**按顺序排列的角坐标**（`X,Z`），最少 3 个点围成闭合区域。
- 支持凹凸多边形，自动使用射线法判定玩家是否在区域内。
- `teleport` 为原地挂机传送目标，支持跨世界传送；不配置则该区域仅支持区域挂机。
- 区域实际显示名称由 `name` 字段控制，文件名中的 `<id>` 仅作为内部标识。

### 2. 定义奖励类型

```yaml
types:
  温泉奖励:
    describe: "泡温泉的人越多，奖励越丰厚"
    vip3:
      - "addbalance %player_name% 金币 %axsafk_players%*35+%player_level%/4+250"
      - "cmi exp %player_name% add %axsafk_players%*%player_level%/4+%player_level%"
    vip2:
      - "addbalance %player_name% 金币 %axsafk_players%*30+%player_level%/6+200"
    vip1:
      - "addbalance %player_name% 金币 %axsafk_players%*25+%player_level%/8+150"
    common:
      - "addbalance %player_name% 金币 %axsafk_players%*20+%player_level%/10+100"
```

- `vip3` 为最高档，权限检测时**从高到低**依次匹配，取玩家拥有的最高 tier。
- 没有任何 tier 权限时，默认使用最低档（配置中的最后一个）。
- 指令中支持 PAPI 占位符（如 `%player_name%`、`%player_level%`）和模块自占位符（如 `%axsafk_players%`）。
- 支持简单四则运算：`+` `-` `*` `/`，运算结果会被展开后作为命令的一部分执行。

### 3. 设置权限

- 进入区域挂机需要 `ArcartX-Suite.afkreward.area.<区域名>` 权限
- 获得对应 tier 奖励需要 `ArcartX-Suite.afkreward.start.<类型>.<tier>` 权限
  - 例：`ArcartX-Suite.afkreward.start.温泉奖励.vip3`
  - 也支持简写：`ArcartX-Suite.afkreward.start.vip3`
- 绕过每日次数上限：`ArcartX-Suite.afkreward.not.reward.limit`
- 绕过区域人数上限：`ArcartX-Suite.afkreward.not.player.limit`

### 4. 周期与上限

```yaml
reward:
  round: 15          # 每 15 分钟发一次奖励
  max:
    enable: true
    limit: 32        # 每日最多 32 次
  player:
    enable: true
    limit: 30        # 区域最多同时 30 人
```

---

## 配置详解

### 主配置（`ArcartXAfkReward.yml`）

```yaml
config-version: 3

debug: false

# 区域配置文件目录（相对于 data/afkreward/）
areas-directory: "areas"

reward:
  round: 15
  max:
    enable: true
    limit: 32
  player:
    enable: true
    limit: 30

types:
  <类型名>:
    describe: "描述文本"
    # 奖励发放时触发的邮件预设 ID 列表（可选，需 Mail 模块启用）
    mail-presets:
      - "afk_reward_mail"
    <tier名>:
      - "命令1"
      - "命令2"

manual:
  restrict-actions: true
  return-on-end: false
  broadcast-rewards: true
  leaderboard-size: 10
  # 奖励发放时触发的 EventPacket 信号（可选，需 EventPacket 模块启用）
  signal-on-reward: "afk_reward"
  # 挂机结束时触发的 EventPacket 信号（可选）
  signal-on-end: "afk_end"
  # 奖励发放时播放的字幕组 ID（可选，需 Announcer 模块启用）
  subtitle-on-reward: "afk_reward_subtitle"
  # 挂机结束时播放的字幕组 ID（可选）
  subtitle-on-end: "afk_end_subtitle"
  # 挂机结束时发送的邮件预设 ID 列表（可选，需 Mail 模块启用）
  end-mail-presets:
    - "afk_end_mail"

storage:
  dialect: sqlite      # sqlite / mysql
  sqlite-file: "afkreward.db"
  host: "127.0.0.1"
  port: 3306
  database: "ArcartX-Suite"
  username: "root"
  password: ""
  table-prefix: "axs_afk_"
  pool-size: 3

ui:
  hud-id: "ArcartX-Suite:afk_reward_hud"
  register-on-enable: true
  overwrite-ui-file: false
```

### 动态配置节

`types` 和 `manual` 为**动态节**（`types` 允许自由增删；`manual` 为固定结构），不会被智能配置诊断覆盖。

区域配置放在独立文件中，不在主配置中定义。主配置仅保留 `areas-directory` 指向区域配置存放目录。

### 双模式对比

| 特性 | 区域挂机（REGION） | 原地挂机（MANUAL） |
| --- | --- | --- |
| 触发方式 | 进入多边形区域自动开始 | `/afk start <区域>` 手动开启 |
| 位置检测 | 实时检测是否在区域内 | 不检测位置，传送到指定点后固定 |
| 行为限制 | 无（可正常移动/交互） | 封锁移动、交互、破坏、开箱、传送、受击 |
| 奖励发放 | 每周期自动发放 | 结束挂机时一次性结算 |
| 服崩恢复 | 退出即失效，无需恢复 | 服崩后自动结算离线期间时长 |
| 排行榜 | 计入总时长 | 计入总时长 |
| 跨世界 | 区域绑定的 world | 传送点可配置不同 world，Bukkit API 自动处理 |

---

## 命令

### 玩家命令

| 命令 | 别名 | 说明 | 权限 |
| --- | --- | --- | --- |
| `/afkreward toggle` | `/afk toggle` | 开启/关闭 HUD 显示 | `arcartxsuite.afkreward.use` |
| `/afkreward status` | `/afk status` | 查看当前挂机状态（模式/区域/时长/今日奖励/总人数） | `arcartxsuite.afkreward.use` |
| `/afkreward start <区域>` | `/afk start <区域>` | 原地挂机：传送至区域挂机点并开始计时 | `arcartxsuite.afkreward.use` |
| `/afkreward end` | `/afk end` | 结束原地挂机，一次性结算奖励 | `arcartxsuite.afkreward.use` |
| `/afkreward list` | `/afk list` | 查看当前在线挂机玩家列表 | `arcartxsuite.afkreward.use` |
| `/afkreward top` | `/afk top` | 查看挂机时长排行榜 | `arcartxsuite.afkreward.use` |

### 管理命令

主入口：`/axs afkreward <子命令>`

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/axs afkreward status` | 查看模块状态（区域数/类型数/当前挂机人数） | `arcartxsuite.admin` |
| `/axs afkreward reload` | 重载模块配置 | `arcartxsuite.admin` |

---

## PlaceholderAPI 占位符

前缀：`%axsafk_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsafk_type%` | 文本 | 挂机类型：`区域挂机` / `原地挂机` / `未挂机` |
| `%axsafk_area%` | 文本 | 当前所在区域名称，未挂机时返回空 |
| `%axsafk_time%` | 文本 | 当前区域挂机时长，格式化（如 `15分32秒`） |
| `%axsafk_total_time%` | 文本 | 累计总挂机时长（格式化） |
| `%axsafk_today%` | 数字 | 今日已获得奖励次数 |
| `%axsafk_total%` | 数字 | 累计获得奖励总次数 |
| `%axsafk_players%` | 数字 | 当前区域同时挂机人数 |
| `%axsafk_next%` | 数字 | 距离下次奖励的剩余秒数 |
| `%axsafk_top_1_name%` | 文本 | 排行榜第 1 名玩家名称 |
| `%axsafk_top_1_time%` | 文本 | 排行榜第 1 名总时长（格式化） |
| `%axsafk_top_1_rewards%` | 数字 | 排行榜第 1 名总奖励次数 |
| `%axsafk_area_<name>%` | 数字 | 指定区域的累计总挂机秒数（`<name>` 替换为区域 `name`，如 `%axsafk_area_温泉%`） |
| `%axsafk_area_<name>_today%` | 数字 | 指定区域的今日挂机秒数 |
| `%axsafk_area_<name>_status%` | 文本 | 指定区域的当前挂机状态：`挂机中` / `未挂机` |
| `%axsafk_total_all%` | 数字 | 所有区域的累计总挂机秒数之和 |

**使用示例**：
```
%axsafk_area%                → 返回 "温泉"
%axsafk_time%                → 返回 "15分32秒"
%axsafk_players%             → 返回 "12"
%axsafk_next%                → 返回 "742"
%axsafk_area_温泉%            → 返回 "3600"
%axsafk_area_温泉_today%      → 返回 "1800"
%axsafk_area_温泉_status%     → 返回 "挂机中"
%axsafk_total_all%           → 返回 "7200"
```

---

## HUD UI 面板

AfkReward 提供一个 ArcartX HUD 面板，玩家进入挂机区域后自动显示：

| 字段 | 说明 |
| --- | --- |
| 区域名称 | 当前所在的挂机区域 |
| 当前挂机 | 本次进入区域后的累计时长 |
| 下次奖励 | 距离下一次发放奖励的倒计时 |
| 区域人数 | 当前区域内同时挂机的玩家数 |

::: info 前置要求
UI 面板需要玩家安装 ArcartX 客户端 mod。未安装时模块功能不受影响，只是无法显示 HUD。
:::

服务端通过 `/afkreward toggle` 可控制玩家 HUD 的显示/隐藏。

---

## 存储

支持 SQLite（默认）和 MySQL。

```yaml
storage:
  dialect: sqlite
  sqlite-file: "afkreward.db"
  host: "127.0.0.1"
  port: 3306
  database: "ArcartX-Suite"
  username: "root"
  password: ""
  table-prefix: "axs_afk_"
```

数据表：
- `<prefix>stats` — 玩家挂机统计（player_uuid / player_name / today_date / today_count / total_count / total_seconds）
- `<prefix>sessions` — 原地挂机 session（服崩恢复用：player_uuid / area_name / reward_type / mode / start_seconds / start_time / today_count / total_count / today_date / total_seconds）
- `<prefix>area_stats` — 玩家**各区域**挂机统计（player_uuid / area_name / total_seconds / today_seconds / today_date）

跨服部署时建议共享 MySQL 库，确保玩家在不同子服的挂机数据一致。

---

## 跨模块联动

AfkReward 通过 ArcartX-Suite Capability 系统与以下模块联动：

### Mail 邮件

- **触发时机**：
  - 区域挂机每周期奖励发放时，按 `types.<类型>.mail-presets` 发送邮件。
  - 原地挂机结束时，按 `manual.end-mail-presets` 发送邮件。
- **前置要求**：Mail 模块已启用并配置了对应的邮件预设。
- **配置示例**：见上文 `types` 与 `manual` 中的 `mail-presets` / `end-mail-presets` 字段。

### EventPacket 信号与字幕

- **触发信号**：
  - `afk_start` — 原地挂机开始时触发。
  - `afk_reward` — 奖励发放时触发（含变量 `area`、`mode`、`seconds`、`rewards`）。
  - `afk_end` — 原地挂机结束时触发。
  - `afk_enter_area` — 玩家进入区域时触发。
  - `afk_leave_area` — 玩家离开区域时触发。
- **字幕播放**：
  - 奖励发放时播放 `manual.subtitle-on-reward` 指定的字幕组。
  - 挂机结束时播放 `manual.subtitle-on-end` 指定的字幕组。
- **前置要求**：EventPacket / Announcer 模块已启用。

### Essentials AFK 冲突处理

- **问题**：Essentials 的 AFK 自动检测可能将正在 AfkReward 挂机的玩家标记为 AFK，甚至踢出。
- **解决**：AfkReward 注册 `AfkRewardDispatchable` capability，Essentials 在 `checkAfk()` 中查询该 capability。若玩家正处于 AfkReward 挂机状态，Essentials 会**跳过 AFK 标记和踢人逻辑**。
- **配置**：在 `ArcartXEssentials.yml` 的 `afkreward-integration` 节中启用联动（默认开启）。

### PlaceholderAPI（供外部读取）

- Chemdah 任务系统或其他插件可通过 PAPI 读取 AfkReward 提供的区域挂机时长、状态等数据。
- 详细占位符列表见上表。

### AfkRewardDispatchable Capability

其他模块可通过 Capability 系统与 AfkReward 交互：

| 方法 | 说明 |
| --- | --- |
| `isAfk(UUID)` | 查询玩家是否处于挂机状态 |
| `getCurrentArea(UUID)` | 获取玩家当前挂机区域名称 |
| `getCurrentMode(UUID)` | 获取当前挂机模式：`REGION` / `MANUAL` / `null` |
| `getCurrentSeconds(UUID)` | 获取本次挂机已持续秒数 |
| `startManualAfk(Player, String)` | 触发玩家原地挂机到指定区域 |
| `endManualAfk(Player)` | 强制结束玩家原地挂机 |

---

## 权限汇总

| 权限 | 说明 | 默认 |
| --- | --- | --- |
| `ArcartX-Suite.afkreward.area.<区域名>` | 允许进入该区域挂机（区域+原地共用） | OP |
| `ArcartX-Suite.afkreward.start.<类型>.<tier>` | 获得该类型的对应 tier 奖励（区域+原地共用） | OP |
| `ArcartX-Suite.afkreward.start.<tier>` | 简写形式，全局 tier 匹配 | OP |
| `ArcartX-Suite.afkreward.not.reward.limit` | 绕过每日奖励次数上限 | OP |
| `ArcartX-Suite.afkreward.not.player.limit` | 绕过区域人数上限 | OP |
| `arcartxsuite.afkreward.use` | 使用 `/afkreward` 玩家命令 | 所有人 |
| `arcartxsuite.admin` | 管理命令权限 | OP |

::: tip 原地挂机权限复用
原地挂机**复用**区域挂机的权限体系，无需额外配置。进入区域需要 `ArcartX-Suite.afkreward.area.<区域名>`，获得 tier 奖励需要 `ArcartX-Suite.afkreward.start.<类型>.<tier>`。
:::

---

## 注意事项

1. **多边形方向无关**：`pos` 点按顺时针或逆时针排列均可，只要首尾闭合即可。
2. **Y 轴不限制**：区域只检测 `X,Z` 平面，不限制玩家所在高度。
3. **移动不中断**：玩家可以在区域内正常移动、跳跃，只要不出多边形边界就不会中断计时。
4. **跨世界重置**：玩家传送到其他世界后自动离开区域，再次进入时重新计时。
5. **日期切换**：服务端日期切换（0 点）后，`today_count` 会自动重置为 0。
6. **奖励命令解析**：命令在执行前会替换 `%player_name%`，但不自动解析其他插件的 PAPI。如需 PAPI 解析，请在命令中直接使用外部插件的占位符（若目标插件支持控制台执行时解析）。

