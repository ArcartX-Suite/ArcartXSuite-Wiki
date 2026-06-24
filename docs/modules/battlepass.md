---
title: BattlePass 战令系统插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite BattlePass 战令系统，三层通行证、日/周/赛季任务池、条件过滤、ArcartX UI 面板，我的世界服务器战令插件。
---

# BattlePass 战令系统

::: tip 付费模块

BattlePass 为付费模块，需要有效授权码激活。
:::

**BattlePass** 模块为服务器提供一套完整的赛季制战令（Battle Pass）系统，支持 **免费 / 高级 / 典藏** 三层通行证体系，以及 **每日任务（短期）/ 每周挑战（中期）/ 赛季目标（长期）** 三类任务周期。任务系统内置条件过滤、动态增量计算、加权随机分配、难度 XP 加成等机制，通过 ArcartX 客户端 UI 呈现主界面与任务列表。

---

## 功能概览

| 功能 | 说明 |
|------|------|
| **三层通行证** | 免费（FREE）、高级（PREMIUM）、典藏（DELUXE），典藏额外提供 1.5 倍 XP 加成 |
| **每日任务** | 每天 0 点自动重置，从任务池加权随机抽取分配给玩家 |
| **每周挑战** | 每 7 天自动重置，递增周数，从周任务池加权随机抽取 |
| **赛季目标** | 贯穿整个赛季，不会重置，所有玩家共享同一批赛季任务 |
| **难度体系** | 简单（EASY，1.0 倍）、普通（NORMAL，1.2 倍）、困难（HARD，1.5 倍） |
| **条件系统** | 任务可绑定事件 Payload 条件（如限定击杀僵尸、Boss）或玩家状态条件（Chronos / Region / World） |
| **增量策略** | 支持固定增量（+1）和 Payload 动态值增量（如伤害数值），支持 max-per-event 限制和 scale 缩放 |
| **UI 面板** | ArcartX 客户端主界面 + 任务列表界面，实时展示任务进度、难度、描述、完成状态 |
| **跨服同步** | 支持通过宿主 CrossServer 同步玩家进度（可选） |

---

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信 |
| MySQL / SQLite | ✅ 必须 | 玩家进度与任务实例持久化 |
| CrossServer | 可选 | 进度/任务/已领取奖励跨服同步 |
| PlaceholderAPI | 可选 | PAPI 占位符对外输出 |

---

## 命令

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/bp` | 无 | 打开战令主界面（等同于 `/bp open`） |
| `/bp tasks` | 无 | 打开任务列表界面 |
| `/bp help` | 无 | 显示帮助 |

> 也可以通过 Menu 模块按钮调用 `Packet.send('AXS_BATTLEPASS', 'open_main')` 打开。

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs battlepass help` | `arcartxsuite.battlepass.admin` | 查看帮助 |
| `/axs battlepass status` | `arcartxsuite.battlepass.admin` | 查看当前赛季状态与活跃玩家数 |
| `/axs battlepass reload` | `arcartxsuite.battlepass.admin` | 重载提示（实际走宿主热重载机制） |
| `/axs battlepass reset <玩家>` | `arcartxsuite.battlepass.admin` | 重置指定玩家的战令全部进度（含任务、已领取奖励） |
| `/axs battlepass unlock <玩家> <premium\|deluxe>` | `arcartxsuite.battlepass.admin` | 为玩家解锁高级或典藏通行证 |

---

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `arcartxsuite.battlepass.admin` | op | 管理员命令权限 |

---

## PAPI 占位符

前缀：`%axsbattlepass_xxx%`

| 占位符 | 说明 |
|--------|------|
| `%axsbattlepass_season%` | 当前赛季 ID |
| `%axsbattlepass_season_display%` | 当前赛季显示名称 |
| `%axsbattlepass_level%` | 当前等级 |
| `%axsbattlepass_max_level%` | 最大等级 |
| `%axsbattlepass_xp%` | 当前 XP |
| `%axsbattlepass_xp_per_level%` | 每级所需 XP |
| `%axsbattlepass_xp_needed%` | 升级还需 XP |
| `%axsbattlepass_premium%` | 高级版是否已激活（"已激活" / "未激活"） |
| `%axsbattlepass_deluxe%` | 典藏版是否已激活（"已激活" / "未激活"） |
| `%axsbattlepass_tier%` | 当前通行证层级（免费 / 高级 / 典藏） |
| `%axsbattlepass_active_tasks%` | 当前未完成的任务数量 |

---

## 配置文件

### 主配置 `ArcartXBattlePass.yml`

```yaml
# =============================================================================
# 战令系统主配置文件
# 版本: 2 (1.1.0-beta+)
# =============================================================================

config-version: 2

# ---------------------------------------------------------------------------
# 数据存储配置
# ---------------------------------------------------------------------------
storage:
  mode: sqlite          # 可选: sqlite / mysql
  sqlite-file-name: battlepass.db
  pool-size: 1
  mysql-host: localhost
  mysql-port: 3306
  mysql-database: axs_battlepass
  mysql-username: root
  mysql-password: ""

# ---------------------------------------------------------------------------
# 赛季定义
# ---------------------------------------------------------------------------
season:
  season-id: "season-1"          # 赛季唯一标识，切换赛季时玩家旧进度自动隔离
  display-name: "第一赛季"      # 赛季显示名称
  max-level: 100               # 最大等级
  xp-per-level: 1000            # 每级所需 XP
  start-date: "2026-06-01"      # 赛季开始日期（仅展示用）
  end-date: "2026-08-01"        # 赛季结束日期（仅展示用）

# ---------------------------------------------------------------------------
# 任务配置
# ---------------------------------------------------------------------------
# 任务模板已拆分为外部文件，位于 <模块数据目录>/<tasks-directory>/ 下：
#   - daily.yml  : 每日任务池
#   - weekly.yml : 每周任务池
#   - season.yml : 赛季任务池
#
# 每日/每周任务从池中按 weight 加权随机抽取，抽取数量由 daily-count / weekly-count 控制
# 兼容旧版：也可在主配置 tasks.daily / tasks.weekly / tasks.season 中直接内联定义
# ---------------------------------------------------------------------------
tasks:
  daily-count: 3                  # 每天分配给玩家的每日任务数量
  weekly-count: 2                 # 每周分配给玩家的每周任务数量
  tasks-directory: "tasks"        # 任务文件所在目录（相对模块数据目录）

# ---------------------------------------------------------------------------
# 奖励配置
# ---------------------------------------------------------------------------
# 每个等级可配置 free / premium / deluxe 三层奖励
# 玩家领取时，系统会自动检查其 PassTier，未解锁的层级无法领取
# ---------------------------------------------------------------------------
rewards:
  - level: 1
    free:
      type: command
      data: "give {player} diamond 1"
    premium:
      type: command
      data: "give {player} diamond 5"
    deluxe:
      type: command
      data: "give {player} diamond 8"

  - level: 2
    free:
      type: command
      data: "give {player} iron_ingot 8"
    premium:
      type: command
      data: "give {player} iron_ingot 32"
    deluxe:
      type: command
      data: "give {player} iron_ingot 48"

  - level: 5
    free:
      type: command
      data: "give {player} experience_bottle 4"
    premium:
      type: command
      data: "give {player} experience_bottle 16"
    deluxe:
      type: command
      data: "give {player} experience_bottle 24"

  - level: 10
    free:
      type: command
      data: "give {player} emerald 2"
    premium:
      type: command
      data: "give {player} emerald 8"
    deluxe:
      type: command
      data: "give {player} emerald 12"

# ---------------------------------------------------------------------------
# UI 面板配置
# ---------------------------------------------------------------------------
ui:
  register-on-enable: true      # 是否在模块启用时自动注册 UI 文件
  main-id: "battlepass_main"    # 主界面 UI ID
  tasks-id: "battlepass_tasks"  # 任务列表 UI ID

# ---------------------------------------------------------------------------
# 跨服同步配置
# ---------------------------------------------------------------------------
cross-server:
  enabled: false
  sync-fields:
    - "progress"
    - "task-progress"
    - "claimed-rewards"
```

---

## 配置详解

### 1. 任务模板字段

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `task-id` | String | 是 | — | 任务唯一标识，也是事件匹配时的模板 ID |
| `display-name` | String | 否 | `task-id` | 显示在 UI 上的任务名称 |
| `description` | String | 否 | `""` | 任务描述，展示在 UI 上 |
| `difficulty` | String | 否 | `easy` | 难度：`easy` / `normal` / `hard` |
| `event-topic` | String | 是 | — | 监听的事件主题（如 `axs.combateffect.kill_entity`） |
| `required-count` | int | 否 | `1` | 完成任务所需累计值 |
| `base-xp-reward` | int | 否 | `0` | 基础 XP 奖励（最终 = baseXpReward × difficultyMultiplier） |
| `difficulty-multiplier` | float | 否 | 按难度自动 | 可覆盖难度默认倍率 |
| `conditions` | List | 否 | `[]` | 任务触发条件列表（见下方条件系统） |
| `increment-strategy` | Map | 否 | `fixed:1` | 增量策略（见下方增量策略） |
| `weight` | int | 否 | `1` | 随机分配权重，值越大越容易被抽中 |

### 2. 条件系统

任务可以附加条件，只有当条件全部满足时，事件才会触发进度增加。

#### EventPayloadCondition（事件 Payload 条件）

```yaml
conditions:
  - type: event_payload
    key: entity_type          # 事件 Payload 中的字段名
    operator: equals          # 操作符
    value: zombie             # 对比值
```

| 操作符 | 说明 | 示例 |
|--------|------|------|
| `equals` | 精确相等 | `entity_type = zombie` |
| `contains` | 包含子串 | `damage_source contains "arrow"` |
| `gt` | 大于 | `damage_amount > 100` |
| `lt` | 小于 | `player_count < 5` |
| `regex` | 正则匹配 | `world matches "world_.*"` |

#### PlayerStateCondition（玩家状态条件）

```yaml
conditions:
  - type: player_state
    state-id: "attack1"       # 检查玩家当前 Chronos 状态
  - type: player_state
    region-id: "spawn"       # 检查玩家是否在指定 Region 内
  - type: player_state
    world: "world"            # 检查玩家所在世界
```

### 3. 增量策略

#### FixedIncrementStrategy（固定增量）

```yaml
increment-strategy:
  type: fixed
  value: 1         # 每次事件触发 +1 进度
```

#### PayloadValueStrategy（Payload 动态值）

```yaml
increment-strategy:
  type: payload_value
  payload-key: damage_amount    # 从事件 Payload 取该字段的数值
  max-per-event: 1000          # 单次事件上限
  scale: 1.0                   # 缩放倍率（如 0.5 则只计一半）
```

> 适用场景：对 Boss 造成伤害累计任务。每次造成伤害事件，取 `damage_amount` 作为增量，但单次不超过 1000。

### 4. 难度与 XP 加成

| 难度 | 默认倍率 | 说明 |
|------|----------|------|
| `EASY` | 1.0x | 基础 XP 不变 |
| `NORMAL` | 1.2x | 基础 XP × 1.2 |
| `HARD` | 1.5x | 基础 XP × 1.5 |

最终 XP 计算公式：

```
实际获得 XP = baseXpReward × difficultyMultiplier × xpMultiplier
```

其中 `xpMultiplier` 由通行证层级决定：

| 层级 | XP 倍率 |
|------|---------|
| FREE | 1.0x |
| PREMIUM | 1.0x |
| DELUXE | 1.5x |

### 5. 任务分配规则

- **每日任务**：每天 0 点（服务器时间）自动从 `tasks.daily` 池中按 `weight` 加权随机抽取 `daily-count` 个分配给玩家，旧每日任务被清除。
- **每周任务**：每隔 >= 7 天自动从 `tasks.weekly` 池中按 `weight` 加权随机抽取 `weekly-count` 个分配给玩家，周数自动递增（`currentWeekNumber`），旧每周任务被清除。
- **赛季任务**：不会重置，所有玩家共享同一批 `tasks.season` 中的任务，直接通过旧进度表追踪。

---

## 外部任务文件

任务模板已拆分到模块数据目录下的 `tasks/` 文件夹中，与主配置文件分离，便于管理和热更新。

**文件位置**：`plugins/ArcartX-Suite/modules/battlepass/tasks/`

> **向后兼容**：如果主配置 `ArcartXBattlePass.yml` 中仍保留了 `tasks.daily`、`tasks.weekly`、`tasks.season` 内联定义，系统会优先使用内联配置，忽略外部文件。

### `tasks/daily.yml`（每日任务池）

```yaml
# 每日任务池 — 每天 0 点自动重置并按 weight 加权随机抽取 daily-count 个分配
daily-login:
  display-name: "每日登录"
  description: "每天登录游戏即可获得奖励"
  difficulty: easy
  event-topic: "axs.onlinerewards.signin_success"
  required-count: 1
  base-xp-reward: 100
  conditions: []
  increment-strategy:
    type: fixed
    value: 1
  weight: 5

daily-kill-zombie:
  display-name: "击杀僵尸"
  description: "在主世界击杀20个僵尸"
  difficulty: normal
  event-topic: "axs.combateffect.kill_entity"
  required-count: 20
  base-xp-reward: 150
  conditions:
    - type: event_payload
      key: entity_type
      operator: equals
      value: zombie
  increment-strategy:
    type: fixed
    value: 1
  weight: 3
```

### `tasks/weekly.yml`（每周任务池）

```yaml
# 每周任务池 — 每 7 天自动重置并按 weight 加权随机抽取 weekly-count 个分配
weekly-quest-5:
  display-name: "完成悬赏任务"
  description: "本周累计完成5个悬赏任务"
  difficulty: normal
  event-topic: "axs.questgps.quest_completed"
  required-count: 5
  base-xp-reward: 500
  conditions: []
  increment-strategy:
    type: fixed
    value: 1
  weight: 3
```

### `tasks/season.yml`（赛季任务池）

```yaml
# 赛季任务池 — 不会重置，所有玩家共享同一批赛季目标
season-kill-1000:
  display-name: "千人斩"
  description: "本赛季累计击杀1000个生物"
  difficulty: hard
  event-topic: "axs.combateffect.kill_entity"
  required-count: 1000
  base-xp-reward: 2000
  conditions: []
  increment-strategy:
    type: fixed
    value: 1
```

---

## UI 面板

### 主界面 `battlepass_main`

通过 `Packet.send('AXS_BATTLEPASS', 'open_main')` 触发打开。

展示内容：
- 赛季名称
- 当前等级 / 最大等级
- 当前 XP / 每级所需 XP
- 升级还需 XP
- 进度条（百分比可视化）
- 通行证层级标签（免费=灰色、高级=金色、典藏=紫色）
- **任务列表** 按钮 → 打开任务界面
- **领取奖励** 按钮 → 打开奖励界面（由宿主或其他模块实现）

### 任务列表 `battlepass_tasks`

通过 `Packet.send('AXS_BATTLEPASS', 'open_tasks')` 触发打开。

展示内容：
- 每日任务区（蓝色标题）
- 每周任务区（橙色标题）
- 赛季任务区（紫色标题）

每个任务卡片展示：
- **难度标签**（简单=绿色、普通=黄色、困难=红色）
- 任务名称
- 任务描述
- 当前进度 / 目标值（含百分比）
- XP 奖励
- 完成状态（已完成 / 进行中）

---

## 使用步骤

### 步骤 1：配置赛季与存储

编辑 `plugins/ArcartX-Suite/modules/battlepass/ArcartXBattlePass.yml`，设定：

```yaml
season:
  season-id: "season-summer-2026"
  display-name: "夏日赛季"
  max-level: 50
  xp-per-level: 800
```

若使用 MySQL，修改 `storage` 节。

### 步骤 2：设计任务池

在 `tasks.daily`、`tasks.weekly`、`tasks.season` 下定义任务模板。关键点：

- **事件主题** 必须与实际发出的 EventBus 事件主题一致。例如 CombatEffect 击杀事件主题为 `axs.combateffect.kill_entity`。
- **条件** 用于过滤任务匹配范围。比如限定只统计僵尸击杀、只统计 Boss 伤害。
- **增量策略** 决定每次事件增加多少进度。普通计数用 `fixed:1`，伤害累计用 `payload_value`。
- **weight** 控制随机分配概率。如果想让某任务出现频率更高，调高 weight。

### 步骤 3：配置奖励

在 `rewards` 下按等级定义三层奖励：

```yaml
rewards:
  - level: 1
    free:
      type: command
      data: "give {player} stone 16"
    premium:
      type: command
      data: "give {player} stone 64"
    deluxe:
      type: command
      data: "give {player} stone 128"
```

目前 `type` 支持 `command`，`{player}` 会被替换为玩家名。

### 步骤 4：打开 UI

玩家直接输入 `/bp` 打开战令主界面，或 `/bp tasks` 打开任务列表。

也可在 Menu 模块的按钮中配置：

```yaml
action:
  clickLeft: |-
    Packet.send('AXS_BATTLEPASS', 'open_main')
```

### 步骤 5：管理玩家通行证

管理员可通过命令为玩家解锁：

```
/axs battlepass unlock Steve premium
/axs battlepass unlock Steve deluxe
```

### 步骤 6：监控与重置

查看赛季状态：

```
/axs battlepass status
```

重置玩家（清空全部进度、任务、已领取奖励，谨慎使用）：

```
/axs battlepass reset Steve
```

---

## 数据库表结构

模块自动创建以下表：

| 表名 | 说明 |
|------|------|
| `bp_player_progress` | 玩家赛季进度（等级、XP、PassTier、日/周重置日期、周数） |
| `bp_task_progress` | 赛季任务进度（兼容旧表，用于无实例的赛季任务） |
| `bp_claimed_rewards` | 已领取奖励记录 |
| `bp_player_tasks` | 玩家任务实例（每日/每周分配的任务，含进度和完成状态） |

---

## 注意事项

1. **切换赛季**：修改 `season.season-id` 后，旧进度不会自动迁移到新赛季。不同 `season-id` 的数据在数据库中完全隔离。
2. **日/周重置**：由后台异步线程每 5 分钟检查一次。玩家下线期间的重置会在其下次上线时自动补执行。
3. **首次登录**：新玩家首次触发任务数据加载时，系统会自动分配当日/当周任务。
4. **向后兼容**：
   - 旧配置中的 `xp-reward` 字段会自动映射为 `base-xp-reward`（通过 migration `1-2.yml`）。
   - 旧数据库中 `unlocked_premium = 1` 但无 `pass_tier` 字段的数据，加载时会自动推断为 `PREMIUM`。
5. **增量策略的 `max-per-event`**：用于防止单次事件（如一次极高伤害）直接完成任务，建议困难任务设置合理上限。

---

## 更新日志

### 1.1.0-beta

- 重构任务系统：引入 TaskTemplate + PlayerTaskInstance 双层模型
- 新增三层通行证体系：FREE / PREMIUM / DELUXE（典藏额外 1.5x XP）
- 新增任务难度体系：EASY / NORMAL / HARD，分别提供 1.0x / 1.2x / 1.5x XP 加成
- 新增条件系统：EventPayloadCondition（事件字段过滤）和 PlayerStateCondition（玩家状态过滤）
- 新增增量策略系统：FixedIncrementStrategy（固定增量）和 PayloadValueStrategy（Payload 动态值增量）
- 新增加权随机任务分配器：每日/每周任务从池中按 weight 抽取
- 新增自动日/周重置引擎：异步定时检查并重新分配任务
- 新增管理员命令 `unlock`，支持解锁 premium / deluxe
- 新增 PAPI 占位符：`deluxe`、`tier`、`active_tasks`
- 新增 BattlePassPacketHandler，支持 `open_main` / `open_tasks` 客户端包
- UI 任务列表新增难度标签、任务描述、进度百分比
- UI 主界面层级标签改为动态（免费/高级/典藏，颜色区分）

