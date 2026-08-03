---
title: AfkReward 挂机奖励插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite AfkReward 区域挂机 + 原地挂机双模式、周期命令奖励、VIP 权限阶梯、排行榜、HUD 面板，我的世界服务器挂机插件。
---

# AfkReward 挂机奖励

## 功能定位

AfkReward 模块为服务器提供**双模式挂机奖励**系统：
- **区域挂机（REGION）**：玩家进入指定多边形区域后自动累计时长，按周期发放奖励。
- **原地挂机（MANUAL）**：玩家通过命令主动选择区域并传送至挂机点，挂机期间行为被封锁，结束挂机后一次性结算奖励。

支持多区域、区域自包含奖励定义、多级 VIP 权限阶梯、人数/次数上限控制、反滥用保护、服崩恢复和排行榜。

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
| HUD 面板 | ArcartX UI 实时显示当前区域、挂机时长、下次奖励倒计时、区域人数、本次会话奖励次数与会话时长 |
| 数据持久化 | SQLite / MySQL 双存储，支持跨服共享 |

---

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信 + 模块框架 |
| PlaceholderAPI | 可选 | 挂机统计占位符输出 |
| Mail | 可选 | 奖励溢出邮件、结束挂机邮件 |
| EventPacket | 可选 | 信号派发与字幕播放 |
| Essentials | 可选 | AFK 状态互斥检测 |

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
# 区域唯一 ID（内部标识，也用于区域 tier 权限）
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

# 兼容旧字段；奖励定义现在直接写在本区域 reward 中
type: 泡澡池

# 是否支持原地挂机（/afk start 此区域）
manual-enabled: true

# 区域奖励权重，最终倍率会乘以该值
reward-weight: 1.0

# 本区域完整奖励配置
reward:
  # 每多少分钟发放一次奖励
  round: 15
  max:
    enable: true
    limit: 32
  player:
    enable: true
    limit: 30
  overflow-to-mail:
    enable: false
  describe: "泡澡的人越多，获取的铜币和经验越多"
  mail-presets:
    - "afk_reward_mail"
  full-inventory-mail: ""
  vip3:
    - "addbalance %player_name% 铜币 %axsafkreward_players%*35+%player_level%/4+250"
    - "cmi give %player_name% cobblestone 54"
    - "cmi exp %player_name% add %axsafkreward_players%*%player_level%/4+%player_level%"
  vip2:
    - "addbalance %player_name% 铜币 %axsafkreward_players%*30+%player_level%/6+200"
    - "cmi give %player_name% cobblestone 54"
    - "cmi exp %player_name% add %axsafkreward_players%*%player_level%/6+%player_level%/2"
  vip1:
    - "addbalance %player_name% 铜币 %axsafkreward_players%*25+%player_level%/8+150"
    - "cmi exp %player_name% add %axsafkreward_players%*%player_level%/8+%player_level%/4"
  common:
    - "addbalance %player_name% 铜币 %axsafkreward_players%*20+%player_level%/10+100"
    - "cmi exp %player_name% add %axsafkreward_players%*%player_level%/10+%player_level%/4"

# 本区域倍率配置
multiplier:
  enable: false
  base: 1.0
  weekend: 1.0
  combine: MAX
  schedules:
    - days: "SAT,SUN"
      start: "20:00"
      end: "22:00"
      multiplier: 2.0

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
- `teleport` 为原地挂机传送目标，支持跨世界传送；当前实现要求配置有效的 `teleport`，缺失或传送世界未加载时会跳过该区域。
- 区域实际显示名称由 `name` 字段控制，文件名中的 `<id>` 仅作为内部标识。
- `reward` 与 `multiplier` 都属于当前区域；缺失时分别使用安全默认值（周期 15 分钟、每日上限 32、人数上限 30、倍率关闭）。

### 2. 定义区域奖励

```yaml
reward:
  describe: "泡温泉的人越多，奖励越丰厚"
  mail-presets:
    - "afk_reward_mail"
  full-inventory-mail: ""
  vip3:
    - "addbalance %player_name% 金币 100"
  vip2:
    - "addbalance %player_name% 金币 80"
  vip1:
    - "addbalance %player_name% 金币 60"
  common:
    - "addbalance %player_name% 金币 40"
```

- 奖励定义写在每个 `area-*.yml` 的 `reward:` 内，不再写在主配置的全局 `types:` 中。
- `describe` 是奖励说明；`mail-presets` 是每次奖励后发送的邮件预设 ID 列表；`full-inventory-mail` 是背包满时使用的邮件预设。
- `vip3` 为最高档，权限检测时**从高到低**依次匹配，取玩家拥有的最高 tier。
- 没有任何 tier 权限时，默认使用最低档（配置中的最后一个）。
- 服务会直接替换 `%player_name%`、`%axsafk_multiplier%` 和 `%axsafkreward_multiplier%`；其他占位符（如当前模板中的 `%axsafkreward_players%`、`%player_level%`）需由目标命令或外部插件实际支持解析。PAPI 扩展自身的前缀是 `%axsafkreward_<字段>%`。
- 支持简单四则运算：`+` `-` `*` `/`，运算结果会被展开后作为命令的一部分执行。

### 3. 设置权限

- 进入区域挂机需要 `axs.afkreward.area.<区域名>` 权限
- 获得对应 tier 奖励需要 `axs.afkreward.start.<区域id>.<tier>` 权限
  - 例：`axs.afkreward.start.default.vip3`
  - 也支持简写：`axs.afkreward.start.vip3`
- 绕过每日次数上限：`axs.afkreward.not.reward.limit`
- 绕过区域人数上限：`axs.afkreward.not.player.limit`

### 4. 周期、上限与倍率

```yaml
# 写在区域文件的 reward: 下
reward:
  round: 15          # 每 15 分钟发一次奖励
  max:
    enable: true
    limit: 32        # 每日最多 32 次
  player:
    enable: true
    limit: 30        # 区域最多同时 30 人
  overflow-to-mail:
    enable: false

multiplier:
  enable: false
  base: 1.0
  weekend: 1.0
  combine: MAX
  schedules: []
```

周期、每日次数上限、区域人数上限和倍率均按区域独立生效。

### 区域倍率与溢出邮件

`multiplier` 只对当前区域生效：

| 字段 | 说明 |
| --- | --- |
| `enable` | 是否启用区域倍率计算，默认 `false` |
| `base` | 基础倍率，默认 `1.0` |
| `weekend` | 周六/周日附加倍率，默认 `1.0` |
| `schedules` | 按 `days`、`start`、`end` 匹配的时间段倍率 |
| `combine` | 多个倍率同时命中时的合并策略：`MAX` 取最大值，`MULTIPLY` 相乘，`LAST` 使用最后一个命中值 |

最终倍率还会乘以区域的 `reward-weight`，并可能受到主配置 `anti-abuse.decay` 的影响。奖励命令中使用 `%axsafk_multiplier%` 或 `%axsafkreward_multiplier%` 可取得当前实际倍率；倍率不会自动改变任意未使用该占位符的命令参数。

当区域 `reward.overflow-to-mail.enable` 为 `true`、玩家背包已满且配置了 `reward.full-inventory-mail` 时，本轮奖励会改为发送该邮件预设；邮件模块不可用或预设发送失败时不会吞掉奖励命令。

---

## 配置详解

### 主配置（`ArcartXAfkReward.yml`）

```yaml
config-version: 5

debug: false

# 区域配置文件目录（相对于 data/afkreward/）
areas-directory: "areas"

# reward、types、multiplier 已移至各个 area-*.yml
# 每个区域文件自包含周期、上限、奖励定义与倍率配置。

anti-abuse:
  time-limit:
    enable: false
    session-seconds: 0
    daily-seconds: 0
    on-exceed: STOP
    exempt-permission: "axs.afkreward.bypass.timelimit"
  bot-detection:
    enable: false
    window-seconds: 300
    min-view-changes: 3
    action: NO_REWARD
    exempt-permission: "axs.afkreward.bypass.botcheck"
  ip-limit:
    enable: false
    exempt-permission: "axs.afkreward.bypass.iplimit"
  decay:
    enable: false
    start-seconds: 3600
    factor-per-hour: 0.2
    min-multiplier: 0.3

audit:
  enable: true
  file: "audit.log"

manual:
  restrict-actions: true
  return-on-end: false
  broadcast-rewards: true
  combat-cooldown-seconds: 10
  permission-recheck-seconds: 5
  leaderboard-size: 10
  signal-on-reward: "afk_reward"
  signal-on-end: "afk_end"
  subtitle-on-reward: ""
  subtitle-on-end: ""
  end-mail-presets:
    - "afk_end_mail"
  protections:
    movement: true
    teleport: true
    interact: true
    block-break: true
    inventory: true
    receive-damage: true
    deal-damage: true
    entity-target: true
    vehicle-enter: true
    interact-entity: true
    drop-item: true
    swap-hand: true
    pickup-item: true
    experience: true
    collidable: true

performance:
  pause-on-low-tps:
    enable: false
    min-tps: 15.0

storage:
  dialect: sqlite
  sqlite-file: "afkreward.db"
  host: "127.0.0.1"
  port: 3306
  database: "arcartxsuite"
  username: "root"
  password: ""
  table-prefix: "axs_afk_"
  pool-size: 3

ui:
  hud-id: "AXS:afk_reward_hud"
  register-on-enable: true
  overwrite-ui-file: false
```

### 动态配置节

`manual` 为动态配置节（固定结构），不会被智能配置诊断覆盖。奖励档位不再属于主配置动态节，而是位于各个区域文件的 `reward:` 中。

区域配置放在独立文件中，不在主配置中定义。主配置仅保留 `areas-directory` 指向区域配置存放目录。

### 反滥用与性能保护

以下配置位于主配置的 `anti-abuse`：

| 配置路径 | 默认值 | 行为 |
| --- | --- | --- |
| `anti-abuse.time-limit.enable` | `false` | 启用单次/每日挂机时长限制 |
| `anti-abuse.time-limit.session-seconds` | `0` | 单次挂机时长上限，`0` 表示不限制 |
| `anti-abuse.time-limit.daily-seconds` | `0` | 每日累计挂机时长上限，`0` 表示不限制 |
| `anti-abuse.time-limit.on-exceed` | `STOP` | 超限处理：`STOP` 停止计奖，`KICK` 移除挂机状态，`DECAY` 将倍率压到 `decay.min-multiplier` |
| `anti-abuse.time-limit.exempt-permission` | `axs.afkreward.bypass.timelimit` | 时长限制豁免权限 |
| `anti-abuse.bot-detection.enable` | `false` | 仅对 REGION 模式启用视角变化检测 |
| `anti-abuse.bot-detection.window-seconds` | `300` | 视角变化统计滑动窗口（秒） |
| `anti-abuse.bot-detection.min-view-changes` | `3` | 窗口内最少视角变化次数 |
| `anti-abuse.bot-detection.action` | `NO_REWARD` | 检测到疑似机器人时不发奖；配置 `KICK` 时同时移除挂机状态 |
| `anti-abuse.bot-detection.exempt-permission` | `axs.afkreward.bypass.botcheck` | 机器人检测豁免权限 |
| `anti-abuse.ip-limit.enable` | `false` | 同区域同 IP 只允许最早开始挂机的账号计奖 |
| `anti-abuse.ip-limit.exempt-permission` | `axs.afkreward.bypass.iplimit` | 同 IP 限制豁免权限 |
| `anti-abuse.decay.enable` | `false` | 启用挂机时间收益递减 |
| `anti-abuse.decay.start-seconds` | `3600` | 开始递减的单次挂机秒数 |
| `anti-abuse.decay.factor-per-hour` | `0.2` | 每小时递减系数 |
| `anti-abuse.decay.min-multiplier` | `0.3` | 递减倍率下限 |

`performance.pause-on-low-tps.enable` 默认关闭；启用后，当服务器 TPS 低于 `performance.pause-on-low-tps.min-tps`（默认 `15.0`）时暂停计时处理。

### 审计日志

主配置 `audit.enable` 默认 `true`，日志文件名默认为 `audit.log`，写入模块数据目录。每次实际发放奖励会记录时间、玩家、区域、模式、tier、次数和倍率，便于追查奖励异常。

### 双模式对比

| 特性 | 区域挂机（REGION） | 原地挂机（MANUAL） |
| --- | --- | --- |
| 触发方式 | 进入多边形区域自动开始 | `/afk start <区域>` 手动开启 |
| 位置检测 | 实时检测是否在区域内 | 不检测位置，传送到指定点后固定 |
| 行为限制 | 无（可正常移动/交互） | 封锁移动、交互、破坏、开箱、传送、受击；无法对任何实体造成伤害（含近战与投射物）；不可碰撞；怪物不会锁定为目标 |
| 奖励发放 | 每周期自动发放 | 结束挂机时一次性结算 |
| 服崩恢复 | 退出即失效，无需恢复 | 服崩后自动结算离线期间时长 |
| 排行榜 | 计入总时长 | 计入总时长 |
| 跨世界 | 区域绑定的 world | 传送点可配置不同 world，Bukkit API 自动处理 |

#### MANUAL 防滥用保护

以下保护在 MANUAL 挂机期间默认生效；除战斗冷却时长外，均无单独配置开关：

- MANUAL 只在结束挂机时按本次累计总时长一次性结算奖励，不会按周期逐轮发放，避免与结束结算重复。
- 刚参与战斗（受到伤害或造成实体伤害）后，必须等待 `manual.combat-cooldown-seconds` 配置的冷却时间才能开始原地挂机；默认值为 10 秒，冷却期间开始请求会被拒绝并提示 `hints.manual.in-combat`。
- 挂机玩家无法对任何实体造成伤害，近战攻击和由其发射的投射物都会被拦截，避免利用无敌状态刷怪。
- 挂机玩家不可与其他实体碰撞；结束挂机后会恢复可碰撞状态，避免用无敌身体阻挡或堵住怪物。
- 怪物不会将挂机玩家锁定为目标，避免把挂机玩家当作无敌诱饵。
- 挂机期间不能进入或使用载具；开始挂机时会自动离开当前载具。
- 挂机期间不能右键实体、丢弃物品、换手、拾取掉落物或获得经验。

---


#### MANUAL 防滥用保护：逐项开关

MANUAL 原地挂机的每项保护都可独立开关，默认值全部为 `true`，配置位于 `manual.protections`：

| 配置路径 | 默认 | 作用 |
| --- | --- | --- |
| `manual.protections.movement` | `true` | 禁止移动 |
| `manual.protections.teleport` | `true` | 禁止传送 |
| `manual.protections.interact` | `true` | 禁交互方块或空气 |
| `manual.protections.block-break` | `true` | 禁破坏方块 |
| `manual.protections.inventory` | `true` | 禁打开容器或背包 |
| `manual.protections.receive-damage` | `true` | 受到伤害无敌 |
| `manual.protections.deal-damage` | `true` | 禁造成伤害 |
| `manual.protections.entity-target` | `true` | 禁被怪物锁定为目标 |
| `manual.protections.vehicle-enter` | `true` | 禁进入载具 |
| `manual.protections.interact-entity` | `true` | 禁右键实体 |
| `manual.protections.drop-item` | `true` | 禁丢弃物品 |
| `manual.protections.swap-hand` | `true` | 禁副手换手 |
| `manual.protections.pickup-item` | `true` | 禁拾取掉落物 |
| `manual.protections.experience` | `true` | 禁获得经验 |
| `manual.protections.collidable` | `true` | 禁碰撞 |

可逐项关闭保护；`restrict-actions` 和 `combat-cooldown-seconds` 仍保持原有独立设置。`markCombat(...)` 与伤害保护开关无关，始终会执行。

`manual.permission-recheck-seconds` 默认值为 `5`：MANUAL 模式每隔该秒数复查区域是否仍启用及玩家区域权限，但计时仍按每秒累加。

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
| `/axs afkreward help` | 查看管理命令帮助 | `arcartxsuite.admin` |
| `/axs afkreward status` | 查看模块状态（区域数/类型数/当前挂机人数） | `arcartxsuite.admin` |
| `/axs afkreward reload` | 重载模块配置 | `arcartxsuite.admin` |

---

## PlaceholderAPI 占位符

前缀：`%axsafkreward_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsafkreward_type%` | 文本 | 挂机类型：`区域挂机` / `原地挂机` / `未挂机` |
| `%axsafkreward_area%` | 文本 | 当前所在区域名称，未挂机时返回空 |
| `%axsafkreward_status%` | 文本 | 当前状态：`挂机中` / `未挂机` |
| `%axsafkreward_mode%` | 文本 | 当前模式：`REGION` / `MANUAL` |
| `%axsafkreward_time%` | 文本 | 当前挂机时长，格式化（如 `15分32秒`） |
| `%axsafkreward_total_time%` | 文本 | 累计总挂机时长（格式化） |
| `%axsafkreward_today%` | 数字 | 今日已获得奖励次数 |
| `%axsafkreward_total%` | 数字 | 累计获得奖励总次数 |
| `%axsafkreward_players%` | 数字 | 当前区域同时挂机人数 |
| `%axsafkreward_next%` | 数字 | 距离下次奖励的剩余秒数 |
| `%axsafkreward_session_rewards%` | 数字 | 本次挂机会话已获得奖励次数 |
| `%axsafkreward_session_time%` | 文本 | 本次挂机会话时长（格式化） |
| `%axsafkreward_daily_seconds%` | 数字 | 今日累计挂机秒数 |
| `%axsafkreward_remaining_daily%` | 数字 | 距离每日挂机时长上限的剩余秒数；未配置上限时返回空 |
| `%axsafkreward_multiplier%` | 数字 | 当前区域实际倍率，格式为两位小数 |
| `%axsafkreward_top_1_name%` | 文本 | 排行榜第 1 名玩家名称 |
| `%axsafkreward_top_1_time%` | 文本 | 排行榜第 1 名总时长（格式化） |
| `%axsafkreward_top_1_rewards%` | 数字 | 排行榜第 1 名总奖励次数 |
| `%axsafkreward_area_<name>%` | 文本 | 指定区域的累计挂机时长（格式化，`<name>` 替换为区域 `name`，如 `%axsafkreward_area_温泉%`） |
| `%axsafkreward_area_<name>_today%` | 文本 | 指定区域的今日挂机时长（格式化） |
| `%axsafkreward_area_<name>_status%` | 文本 | 指定区域的当前挂机状态：`是` / `否` |
| `%axsafkreward_total_all%` | 文本 | 所有区域的累计总挂机时长（格式化） |

**使用示例**：
```
%axsafkreward_area%                → 返回 "温泉"
%axsafkreward_time%                → 返回 "15分32秒"
%axsafkreward_players%             → 返回 "12"
%axsafkreward_next%                → 返回 "742"
%axsafkreward_session_rewards%     → 返回 "3"
%axsafkreward_multiplier%          → 返回 "1.00"
%axsafkreward_area_温泉%            → 返回 "3600"
%axsafkreward_area_温泉_today%      → 返回 "1800"
%axsafkreward_area_温泉_status%     → 返回 "是"
%axsafkreward_total_all%            → 返回 "2时00分00秒"
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
| 会话奖励 | 本次挂机会话已发放的奖励次数 |
| 会话时长 | 本次挂机会话累计时长 |

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
  database: "arcartxsuite"
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
  - 区域挂机每周期奖励发放时，按当前区域 `reward.mail-presets` 发送邮件。
  - 原地挂机结束时，按 `manual.end-mail-presets` 发送邮件。
- **前置要求**：Mail 模块已启用并配置了对应的邮件预设。
- **配置示例**：见上文区域 `reward.mail-presets` 与主配置 `manual.end-mail-presets` 字段。

### EventPacket 信号与字幕

- **触发信号**：
  - `afk_start` — 原地挂机开始时触发（变量：`area`、`mode`）。
  - `afk_reward` — 奖励发放时触发（变量：`area`、`mode`、`seconds`、`rewards`）。
  - `afk_end` — 原地挂机结束时触发（变量：`area`、`mode`、`seconds`、`rewards`）。
  - `afk_enter_area` — 玩家进入区域时触发（变量：`area`、`mode`）。
  - `afk_leave_area` — 玩家离开区域时触发（变量：`area`、`mode`、`seconds`）。
- **EventBus 事件**：
  - `axs.afkreward.reward_claimed` — 区域挂机奖励发放时通过 EventBus 发布（payload：`area`、`mode`）。
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
| `isAfk(UUID)` | 查询玩家是否处于挂机状态（REGION 或 MANUAL） |
| `getAreaName(UUID)` | 获取玩家当前挂机区域名称，未挂机时返回 `null` |
| `getAfkMode(UUID)` | 获取当前挂机模式：`REGION` / `MANUAL` / `null` |
| `getAfkSeconds(UUID)` | 获取本次挂机已持续秒数，未挂机时返回 `0` |
| `startManualAfk(Player, String)` | 触发玩家原地挂机到指定区域 |

### PlayerDataPurgeable Capability

AfkReward 注册了 `PlayerDataPurgeable` capability，供全局数据清理工具调用：

| 方法 | 说明 |
| --- | --- |
| `purgePlayerData(UUID)` | 删除指定玩家的所有挂机数据 |
| `purgeAllPlayerData()` | 删除所有玩家的挂机数据 |

---

## 权限汇总

| 权限 | 说明 | 默认 |
| --- | --- | --- |
| `axs.afkreward.area.<区域名>` | 允许进入该区域挂机（区域+原地共用） | OP |
| `axs.afkreward.start.<区域id>.<tier>` | 获得该区域的对应 tier 奖励（区域+原地共用） | OP |
| `axs.afkreward.start.<tier>` | 简写形式，全局 tier 匹配 | OP |
| `axs.afkreward.not.reward.limit` | 绕过每日奖励次数上限 | OP |
| `axs.afkreward.not.player.limit` | 绕过区域人数上限 | OP |
| `axs.afkreward.bypass.timelimit` | 时长限制豁免 | OP |
| `axs.afkreward.bypass.botcheck` | 机器人检测豁免 | OP |
| `axs.afkreward.bypass.iplimit` | 同 IP 限制豁免 | OP |
| `arcartxsuite.afkreward.use` | 使用 `/afkreward` 玩家命令 | 所有人 |
| `arcartxsuite.admin` | 管理命令权限 | OP |

::: tip 原地挂机权限复用
原地挂机**复用**区域挂机的权限体系，无需额外配置。进入区域需要 `axs.afkreward.area.<区域名>`，获得 tier 奖励需要 `axs.afkreward.start.<区域id>.<tier>`。
:::

---

## 注意事项

1. **多边形方向无关**：`pos` 点按顺时针或逆时针排列均可，只要首尾闭合即可。
2. **Y 轴不限制**：区域只检测 `X,Z` 平面，不限制玩家所在高度。
3. **移动不中断**：玩家可以在区域内正常移动、跳跃，只要不出多边形边界就不会中断计时。
4. **跨世界重置**：玩家传送到其他世界后自动离开区域，再次进入时重新计时。
5. **日期切换**：服务端日期切换（0 点）后，`today_count` 会自动重置为 0。
6. **奖励命令解析**：命令在执行前会替换 `%player_name%`，但不自动解析其他插件的 PAPI。如需 PAPI 解析，请在命令中直接使用外部插件的占位符（若目标插件支持控制台执行时解析）。
7. **区域文件校验**：非法坐标、坐标点少于 3 个、缺少 `teleport`、传送世界未加载、区域 `name` 重复或文件名无效时，该区域会被跳过并写入告警；修正文件后可使用 reload 重新加载。
8. **强制结束原因**：原地挂机结束原因包括 `MANUAL`、`COMBAT`、`AREA_REMOVED`、`NO_PERMISSION`、`TIME_LIMIT`、`FULL`，对应情况会显示相应提示。
