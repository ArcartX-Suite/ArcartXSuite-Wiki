---
title: EntityTracker 实体追踪插件 | ArcartX-Suite
description: ArcartX-Suite EntityTracker Boss血条HUD、实时伤害排行、自动结算奖励、攻击目标信息显示、跨服排行，我的世界服务器 Boss 追踪插件。
---

# EntityTracker 实体追踪

::: tip 福利模块
本模块为福利模块（消费满 ¥150 可领取授权）。从 **1.2.0-beta** 起，在 [云端平台](https://cloud.021209.xyz) 领取授权后于「装备模块」勾选即可，无需 `license.yml`。
:::

## 功能定位

全方位的实体追踪方案，涵盖 **Boss 追踪**、**攻击目标 HUD**、**掉落分配**、**跨服排行** 和 **排行榜定时奖励** 五大系统。

- **Boss 追踪** — 跟踪 MythicMob Boss，把血量、距离、存活时间、实时伤害排行推给 HUD/聊天卡片。Boss 死亡时按伤害排行自动结算并发奖。
- **攻击目标 HUD** — 实时显示玩家最近命中的活体目标：名称、生命、距离、坐标、实体类型、MythicMob ID。
- **掉落记录 & 分配** — 记录 Boss 掉落物，支持 DKP 竞价 / ROLL 随机 / 职业优先级 / 手动 四种分配模式。
- **跨服排行** — Boss 死亡结算后，经统一 CrossServer SDK 同步各服玩家对 Boss 的最高伤害记录。
- **排行榜定时奖励** — 按周/月自动发放排行榜奖励，支持 6 种奖励动作。

---

## 核心特性

### Boss 追踪

- **MythicMobs 联动**：自动检测 MythicMob 生成事件，按 `mob-id`（Boss 定义文件名）匹配
- **实时血条与排行**：HUD 周期推送 Boss 名称、血量百分比、自己的伤害排名和 Top N 伤害排行
- **多 Boss 并行**：支持同时追踪多个 Boss 实体，每个 Boss 独立会话（`BossSession`）
- **优先级与排序**：多个 Boss 存活时按 `priority` 和配置的排序模式（5 种）决定显示顺序
- **观察范围**：仅在 `viewer-range` 距离内的玩家才收到 Boss HUD，超出范围自动隐藏
- **伤害排行结算**：Boss 死亡时按伤害排名自动发放奖励（即时结算），支持 6 种奖励动作
- **最低伤害门槛**：可配置 `min-damage` 为固定值或百分比（如 `"1%"`），低于门槛不参与排名
- **聊天卡片**：Boss 生成/死亡/消失时可触发 ArcartX Chat Card 推送
- **背包满处理**：物品奖励支持 `drop`（掉落）、`fail`（跳过）、`silent-drop`（静默掉落）三种策略
- **补发机制**：管理员可通过 `reissue` 命令按历史结算记录补发奖励
- **EventPacket 联动**：结算时向每位参与玩家发射 `boss_settlement` 信号

### 攻击目标 HUD

- **实时目标信息**：命中任意活体后显示名称、生命、距离、坐标、实体类型
- **超时自动关闭**：`target-timeout-ms` 时间内未再次命中则 HUD 自动关闭
- **黑名单过滤**：支持按 MythicMob ID 和 Bukkit EntityType 两种维度屏蔽
- **Boss 排除**：已配置追踪的 Boss 默认不触发攻击目标 HUD（`ignore-configured-bosses: true`），避免重复显示
- **自定义格式**：`title-format` 和 `subtitle-format` 支持颜色代码和内置变量

---

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | Boss HUD、攻击目标 HUD、聊天卡片和客户端数据包 | 模块 UI 不可用 |
| 按功能必需 | MythicMobs / MythicBukkit | Boss 追踪依赖 MythicMob ID、生成/死亡事件和 MythicItems 奖励 | Boss 追踪会跳过；攻击目标 HUD 仍可使用 |
| 可选 | PlaceholderAPI | 输出 EntityTracker PAPI，奖励模板渲染时解析外部变量 | PAPI 输出和变量解析不可用 |
| 可选 | NeigeItems | `neigeitems` 奖励动作发放 NeigeItems 物品 | 只影响对应奖励动作 |
| 可选 | MythicLib / MMOItems | `mythicitems` 奖励动作发放 MythicMobs 物品 | 只影响对应奖励动作 |
| 可选 | 宿主 CrossServer SDK | 跨服 Boss 最高伤害同步（需宿主 `config.yml` 启用 Redis/Proxy） | 跨服排行不可用，仅限本服 SQLite 数据 |
| 可选 | Vault | 金钱类型奖励发放 | 金钱类型奖励不可用 |
| 可选 | Mail 模块 | `mail` 奖励动作通过 Mail 模块发送预设邮件 | 邮件类型奖励不可用 |
| 内置 | SQLite JDBC | 本地数据库存储（击杀记录、掉落统计、排行数据、奖励发放记录） | 不可缺少 |

---

## 启用步骤

在宿主 `config.yml` 中启用模块：

```yaml
modules:
  entitytracker:
    enabled: true
```

首次启用后，模块会在 `plugins/ArcartX-Suite/data/entitytracker/` 下生成：

```
data/entitytracker/
├── ArcartXEntityTracker.yml    # 主配置
├── bosses/
│   └── ExampleBoss.yml         # 默认 Boss 定义示例
├── entitytracker.db            # SQLite 数据库（自动建表）
└── ui/
    ├── boss_tracker.yml        # Boss 追踪 HUD
    ├── attack_target_hud.yml   # 攻击目标 HUD
    ├── ranking_rewards.yml     # 排行奖励管理面板
    ├── reward_editor.yml       # 奖励配置编辑面板
    └── reward_history.yml      # 奖励发放历史面板
```

---

## 配置详解

### 主配置 `ArcartXEntityTracker.yml`

主配置分为四大段：`boss`（Boss 追踪）、`attack-target`（攻击目标）、`new-features`（1.1.0 新功能）和 `database`（数据库）。

#### Boss 追踪全局设置

```yaml
boss:
  settings:
    # HUD 数据推送间隔（tick），越低越实时，但越消耗性能
    refresh-interval-ticks: 5
    # 默认观察范围（格），超过此距离的玩家不收到 HUD
    default-viewer-range: 48.0
    # 同时显示的最大 Boss 血条数量
    max-visible-bars: 5
    # 排序模式（见下方说明）
    sort-mode: "spawn-order"
    # ArcartX UI 注册 ID（支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)）
    ui-id: "ArcartX-Suite:boss_tracker"
    # 模块启用时是否自动向 ArcartX 注册 UI
    register-ui-on-enable: true
    # 是否每次启动覆盖 UI yml 文件
    overwrite-ui-file: false

  # Boss 定义目录，相对模块数据目录
  bosses-directory: "bosses"
```

**排序模式** `sort-mode` 可选值：

| 值 | 行为 |
| --- | --- |
| `spawn-order` | 按 Boss 生成时间排序（先出现的排在前面） |
| `distance` | 距离玩家最近的排在前面 |
| `health-percent` | 血量百分比最低的排在前面 |
| `priority` | 按 Boss 定义中的 `priority` 值排序（数值越大越靠前） |
| `hybrid` | 混合排序，综合距离、优先级和血量 |

根级跨服开关（与 `new-features.cross-server-ranking` 配合使用）：

```yaml
# Redis/Proxy 连接与鉴权见宿主 plugins/ArcartX-Suite/config.yml 的 cross-server 节
cross-server:
  enabled: false
```

#### 攻击目标 HUD 设置

```yaml
attack-target:
  settings:
    debug: false
    # HUD 刷新间隔
    refresh-interval-ticks: 5
    # 目标超时（毫秒）：超过此时间未再次命中，自动关闭 HUD
    target-timeout-ms: 3000
    # 最大视距
    max-view-distance: 48.0
    # 支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)
    ui-id: "ArcartX-Suite:attack_target_hud"
    register-ui-on-enable: true
    overwrite-ui-file: false

    # 黑名单：命中后不显示 HUD
    blacklist:
      # MythicMob ID 黑名单（大小写不敏感）
      mythic-mob-ids: []
      # Bukkit EntityType 黑名单，如 ZOMBIE、PLAYER、ARMOR_STAND
      entity-types: []

    # 与 Boss 追踪的联动
    entitytracker-link:
      # true 时，ArcartXEntityTracker.yml 中 enabled=true 的 Boss 不触发目标 HUD
      ignore-configured-bosses: true

    # 格式化模板
    title-format: "&c{display_name}"
    subtitle-format: "&7{entity_type_name} &8| &c{health}/{max_health} &8| &f{distance_text}"
```

**攻击目标 HUD 内置变量**：`{display_name}`、`{entity_type_name}`、`{health}`、`{max_health}`、`{health_percent}`、`{distance}`、`{distance_text}`、`{world}`、`{x}`、`{y}`、`{z}`。

---

### Boss 定义文件

Boss 定义文件位于 `data/entitytracker/bosses/` 目录下，**文件名（去掉 `.yml`）即为 MythicMob ID**（大小写不敏感）。例如 `SkeletalKnight.yml` 对应 MythicMob ID `SkeletalKnight`。

```yaml
# data/entitytracker/bosses/ExampleBoss.yml

# 是否启用追踪
enabled: true

# 优先级（数值越大越优先显示）
priority: 100

# 覆盖全局的观察范围（格）
viewer-range: 48.0

# HUD 标题格式（支持颜色代码 + 内置变量）
title-format: "&c{display_name}"

# HUD 副标题格式
subtitle-format: "&7MythicMob: &f{mob_id}"

# 伤害排行系统
damage-ranking:
  enabled: true
  # 排行榜最大显示人数
  max-entries: 10
  # 最低伤害门槛（可写固定值如 "100" 或百分比如 "1%"）
  min-damage: "1%"

  # 即时结算奖励（Boss 死亡时触发）
  rewards:
    # 只在 Boss 死亡结算时发奖；重载/消失/停机不触发
    enabled: false
    # 背包满策略: drop / fail / silent-drop
    inventory-full: "drop"
    ranks:
      1:
        actions:
          - type: neigeitems
            item-id: "legendary_sword"
            amount: 1
          - type: mythicitems
            item-id: "BossGem"
            amount: 1
          - type: command
            command: "points give {player} 100"
          - type: message
            target: "player"
            text: "&6你在 {boss_display_name} 伤害排名第 {rank} 名，获得奖励。"
      2:
        actions:
          - type: command
            command: "points give {player} 50"
          - type: message
            target: "player"
            text: "&e你在 {boss_display_name} 伤害排名第 {rank} 名。"

  # 排行榜定时奖励（周/月）— 独立于 rewards（即时结算）
  ranking-rewards:
    weekly:
      enabled: false
      inventory-full: "drop"
      ranks:
        1:
          actions:
            - type: command
              command: "points give {player} 100"
            - type: message
              target: "broadcast"
              text: "&6&l恭喜 {player} 获得本周 {boss_display_name} 伤害冠军！"
        2:
          actions:
            - type: command
              command: "points give {player} 50"
    monthly:
      enabled: false
      inventory-full: "drop"
      ranks:
        1:
          actions:
            - type: command
              command: "points give {player} 500"
            - type: message
              target: "broadcast"
              text: "&6&l&n恭喜 {player} 获得本月 {boss_display_name} 伤害冠军！"

# 聊天卡片（填 ArcartX chat card 模板 ID，留空不触发）
spawn-chat-card: ""
death-chat-card: ""
despawn-chat-card: ""
```

#### Boss 定义内置变量

`title-format`、`subtitle-format` 和聊天卡片模板均支持以下变量：

| 变量 | 说明 |
| --- | --- |
| `{display_name}` | MythicMob 显示名称 |
| `{mob_id}` / `{mob_id_lower}` | MythicMob ID / 小写 ID |
| `{entity_uuid}` | 实体 UUID |
| `{health}` / `{max_health}` / `{health_percent}` | 当前血量 / 最大血量 / 血量百分比 |
| `{progress}` | 血量进度条字符 |
| `{distance}` / `{distance_text}` | 距离（数值） / 距离（格式化文本） |
| `{spawn_order}` / `{priority}` | 生成顺序 / 配置优先级 |
| `{world}` / `{x}` / `{y}` / `{z}` | 世界名 / 坐标 |
| `{alive_seconds}` / `{alive_time}` | 存活秒数 / 格式化存活时间 |
| `{has_target}` / `{target_name}` / `{target_display_name}` | 是否有目标 / 目标名 / 目标显示名 |
| `{target_uuid}` / `{target_type}` | 目标 UUID / 目标类型 |
| `{ranking_enabled}` | 是否开启伤害排行 |
| `{damage_participant_count}` / `{damage_tracked_player_count}` | 参与人数 / 被追踪人数 |
| `{total_damage}` | 全体累计伤害 |
| `{viewer_rank}` / `{viewer_rank_text}` | 当前玩家排名 / 排名文本 |
| `{viewer_damage}` / `{viewer_damage_percent}` / `{viewer_taken_damage}` | 当前玩家伤害 / 伤害占比 / 受到伤害 |
| `{top_N_name}` / `{top_N_damage}` / `{top_N_damage_percent}` / `{top_N_taken_damage}` | 第 N 名玩家名 / 伤害值 / 伤害占比 / 受伤（N 从 1 到 `max-entries`） |

---

### 奖励动作类型

Boss 即时结算（`rewards`）和排行榜定时奖励（`ranking-rewards`）共用同一套奖励动作格式：

| `type` | 必需参数 | 说明 |
| --- | --- | --- |
| `command` | `command` | 以控制台身份执行命令 |
| `message` | `text`, `target` | 发送消息。`target` 可选 `player`（发给获奖玩家）/ `broadcast`（全服广播）/ `console`（控制台输出） |
| `neigeitems` | `item-id`, `amount` | 发放 NeigeItems 物品（需安装 NeigeItems） |
| `mythicitems` | `item-id`, `amount` | 发放 MythicMobs 物品（需安装 MythicMobs） |
| `mail` | `preset-id` | 通过 Mail 模块发送预设邮件（需启用 Mail 模块） |
| `signal` | `signal` | 向 EventPacket 发射自定义信号，可串联更多动作 |

#### 奖励动作占位符

所有奖励动作的文本字段均支持以下变量。模板渲染后会再走一次 PlaceholderAPI 解析。

| 变量 | 说明 |
| --- | --- |
| `{player}` / `{player_name}` | 获奖玩家名 |
| `{player_display_name}` | 获奖玩家显示名 |
| `{player_uuid}` | 获奖玩家 UUID |
| `{boss_id}` / `{boss_display_name}` | Boss MythicMob ID / 显示名 |
| `{entity_uuid}` | Boss 实体 UUID |
| `{rank}` / `{rank_text}` | 排名（数字）/ 排名文本 |
| `{damage}` / `{damage_percent}` / `{taken_damage}` | 伤害值 / 伤害占比 / 受伤 |
| `{total_damage}` / `{participant_count}` / `{tracked_player_count}` | 总伤害 / 参与人数 / 追踪人数 |
| `{top_N_name}` | 排行第 N 名玩家名（排行榜字段同理） |
| `{period_type}` / `{period_type_display}` | 周期类型 / 周期中文名（仅定时奖励） |

#### 背包满策略 `inventory-full`

| 值 | 行为 |
| --- | --- |
| `drop` | 放不下的物品自然掉落在玩家脚下，并发送提示消息 |
| `fail` | 发奖前检测空间不足时直接跳过，标记奖励失败 |
| `silent-drop` | 同 `drop`，但不发送提示消息 |

---

## 1.1.0 新功能

### Boss 掉落记录系统

记录每次 Boss 击杀的掉落信息，支持掉落率统计与历史查询。数据存储在 SQLite 的 `boss_kill_records` 和 `boss_drop_statistics` 表中。

```yaml
new-features:
  drop-recording:
    enabled: true
    # 数据保留天数，超过此天数的记录将被自动清理
    retention-days: 30
    # 是否启用掉落统计（汇总各 Boss 各物品的掉落率）
    enable-statistics: true
    # 统计更新间隔（秒）
    statistics-update-interval: 300
```

**统计内容**：每个 Boss + 物品组合记录掉落次数、击杀次数和实际掉落率（`drop_count / kill_count`）。统计表会在指定间隔后自动刷新。

### 掉落分配系统

支持 4 种分配模式，适用于工会/团本场景。每次 Boss 掉落时弹出分配界面。

```yaml
new-features:
  drop-allocation:
    enabled: true
    # 默认分配模式: dkp / roll / priority / manual
    default-mode: "roll"
    # 分配超时时间（秒），超时未操作则自动分配
    allocation-timeout: 60

    # ── DKP 积分系统 ──
    dkp:
      enabled: true
      # Boss 击杀基础积分（参与即得）
      base-earn-points: 10
      # 伤害排名额外积分奖励
      rank-bonus-points:
        1: 20   # 第一名额外 +20
        2: 15   # 第二名额外 +15
        3: 10   # 第三名额外 +10
        4: 5    # 第四名额外 +5
        5: 3    # 第五名额外 +3

    # ── ROLL 随机系统 ──
    roll:
      enabled: true
      # ROLL 超时时间（秒）
      timeout-seconds: 30
      # ROLL 范围
      roll-range: "1-100"
      # 是否允许放弃（pass）
      allow-pass: true

    # ── 优先级分配系统 ──
    priority:
      enabled: true
      # 职业优先级（数值越小越优先）
      class-priority:
        tank: 1
        healer: 2
        dps: 3
      # 装备品质优先级
      quality-priority:
        legendary: 1
        epic: 2
        rare: 3
        common: 4
```

**DKP 模式**：玩家消耗积分竞价，出价最高者获得物品。击杀 Boss 时根据参与和排名获得积分。

**ROLL 模式**：参与者在指定范围内随机 ROLL，点数最高者获得物品。支持 Pass 放弃。

**Priority 模式**：根据玩家职业和装备品质双维优先级自动分配。

**Manual 模式**：由管理员/团长手动指定分配对象。

### 跨服 Boss 最高伤害同步

Boss 死亡结算后，若玩家本次伤害创本地新高，则写入 `player_boss_best_damage` 并通过统一 CrossServer SDK（channel `entitytracker`）广播至其他子服。各子服共享同一 SQLite 文件路径时可直接查询全服排行；即使数据库文件独立，入站消息也会合并远程记录。

**启用条件**（两项均需为 `true`）：

1. 根级 `cross-server.enabled`
2. `new-features.cross-server-ranking.enabled`
3. 宿主 `config.yml` 已配置 `cross-server`（Redis 和/或 Proxy）

服务器标识使用宿主 `cross-server.node-id`，不再在模块配置中单独填写 `server-name`。

```yaml
cross-server:
  enabled: true

new-features:
  cross-server-ranking:
    enabled: true
    update-interval: 60    # 预留
    ranking-types:
      - "best_damage"      # 当前已实现：最高单次伤害
    max-entries: 50
```

**当前实现范围**：

| 类型 | 数据来源 | 缓存表 |
| --- | --- | --- |
| `best_damage` | `player_boss_best_damage` 全 Boss 聚合 | `cross_server_boss_rankings` |
| `boss_damage` | 按 Boss ID 过滤最高伤害 | 同上（`boss_id` 字段） |
| `kills` | `boss_kill_records.participants` 击杀次数 | 同上 |
| `participate` | `boss_kill_records` 参与不同 Boss 数 | 同上 |
| `server` | 按 `server_name` 过滤最高伤害 | 同上 |

Boss 死亡后写入 `boss_kill_records`，跨服经 `KILL_RECORD` payload 同步；`update-interval` 秒定时刷新 `cross_server_boss_rankings` JSON 缓存。

### Boss 掉落记录

Boss 死亡时捕获 `EntityDeathEvent` 掉落列表，写入 `boss_kill_records.drops` 并更新 `boss_drop_statistics` 掉落率；`retention-days` 到期自动清理历史击杀。

### 掉落分配

Boss 死亡后按 `default-mode` 分配掉落：

| 模式 | 行为 |
| --- | --- |
| `roll` | 参与玩家随机 ROLL，最高点数者获得物品 |
| `dkp` | 击杀时发放 DKP（基础分 + 排名奖励），物品给当前 DKP 最高者 |
| `priority` | 按职业优先级配置选取获得者（默认 dps 最低优先） |
| `manual` | 仅写入 `drop_allocation_records`，不自动发物品 |

分配结果写入 `drop_allocation_records` / `roll_participation_records`；在线玩家直接收到物品，背包满则掉落脚下。

### 排行榜定时奖励系统

调度器按周/月自动发放排行榜奖励。全局调度设置在主配置中，**具体奖励定义在各 Boss yml 文件** 的 `damage-ranking.ranking-rewards` 节点。

```yaml
new-features:
  ranking-rewards:
    # ── 调度计划 ──
    schedule:
      weekly:
        enabled: true
        day-of-week: "MONDAY"     # MONDAY, TUESDAY, ..., SUNDAY
        hour: 0
        minute: 0
      monthly:
        enabled: true
        day-of-month: 1           # 1-28（建议不超过 28 避免短月问题）
        hour: 0
        minute: 0
    # 时区
    timezone: "Asia/Shanghai"

    # ── 发放策略 ──
    reward-settings:
      # 调度器到期时是否自动发放
      auto-distribute: true
      # 发放失败重试次数
      max-retry-count: 3
      # 重试间隔（分钟）
      retry-interval-minutes: 30
      # 是否全服广播奖励发放结果
      broadcast-rewards: true
      # 是否记录详细日志
      detailed-logging: true
```

**工作流程**：

1. 调度器在配置的时间点（如每周一 00:00）触发
2. 遍历所有 Boss 定义文件，查找 `ranking-rewards.weekly.enabled: true` 的 Boss
3. 从数据库查询该 Boss 的伤害排行（`player_boss_best_damage` 表）
4. 按名次匹配奖励定义，在主线程执行奖励动作
5. 记录发放结果到 `ranking_reward_records` 表
6. 动态重新调度下一次执行

**手动触发**：管理员可通过 `/axs entitytracker rewards distribute weekly` 手动触发发放，无需等待调度器。

---

## 数据库配置

```yaml
database:
  # 数据库类型: sqlite（默认）或 mysql
  type: "sqlite"

  # SQLite 配置
  sqlite:
    file: "entitytracker.db"    # 相对模块数据目录

  # MySQL 配置（type 为 mysql 时生效）
  mysql:
    host: "localhost"
    port: 3306
    database: "ArcartX-Suite"
    username: "root"
    password: ""
    pool-size: 5                # HikariCP 连接池大小
    connection-timeout: 30000   # 连接超时（毫秒）
```

### 数据库表结构

模块启动时自动建表，无需手动操作。

| 表名 | 用途 | 关键字段 |
| --- | --- | --- |
| `boss_kill_records` | Boss 击杀记录 | boss_id, kill_time, participants(JSON), drops(JSON), total_damage, duration_seconds, 坐标 |
| `boss_drop_statistics` | 掉落率统计（Boss + 物品聚合） | boss_id, item_id, drop_count, kill_count, drop_rate |
| `player_dkp` | 玩家 DKP 积分 | player_uuid, total_points, earned_points, spent_points |
| `dkp_transaction_records` | DKP 变动流水 | player_uuid, transaction_type, points, reason, boss_kill_id |
| `drop_allocation_records` | 掉落分配记录 | boss_kill_id, item_id, allocation_type, winner_uuid, points_cost, roll_value |
| `roll_participation_records` | ROLL 参与明细 | allocation_record_id, player_uuid, roll_type, roll_value |
| `player_boss_best_damage` | 玩家 Boss 最佳伤害（排行依据；跨服经 SDK 合并） | player_uuid, boss_id, best_damage, server_name（UNIQUE 约束） |
| `cross_server_boss_rankings` | 跨服排行 JSON 缓存（定时刷新） | ranking_type, boss_id, ranking_data, expire_time |
| `ranking_reward_configs` | 排行奖励配置（预留表结构） | reward_type, ranking_type, rank_start, rank_end, 奖励内容 |
| `ranking_reward_records` | 奖励发放记录 | reward_config_id, player_uuid, rank, score, status, failure_reason |
| `ranking_periods` | 排行统计周期 | period_type, ranking_type, period_start, period_end, status |
| `offline_reward_storage` | 离线奖励暂存 | player_uuid, reward_type, reward_data(JSON), claimed |
| `entitytracker_version` | 数据库版本号 | version |

---

## 命令

### Boss 追踪命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs entitytracker help` | 查看 EntityTracker 管理命令帮助 |
| `/axs entitytracker status` | 查看模块状态：活跃会话数、查看者人数、历史结算数 |
| `/axs entitytracker reload` | 重载 EntityTracker 配置、UI 和追踪服务 |
| `/axs entitytracker sessions [mobId]` | 列出当前活跃 Boss 会话，显示 HP、参与人数、实体 UUID。可选 `mobId` 过滤 |
| `/axs entitytracker rank <entityUuid> [page]` | 查看指定 Boss 实体的实时伤害排行（UUID 从 `sessions` 获取） |
| `/axs entitytracker settlements [page]` | 分页查看历史结算记录：Boss 名称、击杀时间、参与人数 |
| `/axs entitytracker settlement <结算ID> [page]` | 查看指定结算详情：每位参与者的伤害值、排名和奖励状态（✓/✗） |
| `/axs entitytracker reissue <结算ID> <名次> [玩家]` | 补发指定结算中某名次的奖励。不指定玩家则发给原排名玩家 |

### 排行榜奖励命令

> 权限：`entitytracker.rewards.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs entitytracker rewards help` | 查看奖励子命令帮助 |
| `/axs entitytracker rewards manage` | 打开奖励管理 UI 界面（仅玩家） |
| `/axs entitytracker rewards history` | 打开奖励发放历史 UI 界面（仅玩家） |
| `/axs entitytracker rewards distribute <weekly\|monthly>` | 手动触发指定周期的奖励发放 |
| `/axs entitytracker rewards status` | 查看调度器运行状态：周/月任务是否运行、下次执行时间 |
| `/axs entitytracker rewards reload` | 重新加载调度器配置并重新调度任务 |

---

## PAPI 占位符

前缀：`%axsentitytracker_*%`

### 全局 / 模块信息

| 占位符 | 说明 |
| --- | --- |
| `%axsentitytracker_sort_mode%` | 当前 Boss 排序模式（如 `spawn-order`） |
| `%axsentitytracker_max_visible_bars%` | 配置中最大同时显示的 Boss 血条数 |
| `%axsentitytracker_configured_boss_count%` | 配置文件中定义的 Boss 总数 |
| `%axsentitytracker_damage_ranking_boss_count%` | 开启伤害排行的 Boss 数量 |
| `%axsentitytracker_max_damage_ranking_entries%` | 所有 Boss 中最大的 `max-entries` 值 |
| `%axsentitytracker_active_session_count%` | 当前活跃的 Boss 战斗会话数 |
| `%axsentitytracker_active_viewer_count%` | 当前正在观察 Boss 的玩家总数 |
| `%axsentitytracker_boss_count%` | 该玩家视野中的 Boss 数量 |
| `%axsentitytracker_total_boss_count%` | 全服正在追踪的 Boss 总数 |
| `%axsentitytracker_ui_id%` | 配置的 UI ID |
| `%axsentitytracker_runtime_ui_id%` | 运行时实际使用的 UI ID |
| `%axsentitytracker_bridge_ready%` | ArcartX 桥接是否就绪（`true`/`false`） |

### 当前 Boss / 槽位

| 占位符 | 说明 |
| --- | --- |
| `%axsentitytracker_current_<字段>%` | 视野中第 1 个 Boss 的信息（等同 `slot_1_<字段>`） |
| `%axsentitytracker_slot_<N>_<字段>%` | 视野中第 N 个 Boss 的信息 |

**常用 `<字段>`**：

| 字段 | 说明 |
| --- | --- |
| `display_name` | Boss 显示名称 |
| `mob_id` | MythicMobs ID |
| `health` / `max_health` / `health_percent` | 当前血量 / 最大血量 / 百分比 |
| `distance` / `distance_text` | 距离（数值/格式化） |
| `viewer_rank` / `viewer_damage` / `viewer_damage_percent` | 玩家自身排名/伤害/占比 |
| `top_<N>_name` / `top_<N>_damage` / `top_<N>_damage_percent` | 第 N 名玩家名/伤害值/占比 |
| `alive_seconds` / `alive_time` | 存活秒数 / 格式化存活时间 |
| `priority` / `spawn_order` | 优先级 / 生成顺序 |
| `world` / `x` / `y` / `z` | 坐标信息 |

### 最近结算

| 占位符 | 说明 |
| --- | --- |
| `%axsentitytracker_last_<字段>%` | 玩家参与的最近一次 Boss 结算信息 |

**常用 `<字段>`**：`rank`（排名）、`damage`（伤害值）、`boss_name`（Boss 名称）、`total_participants`（参与人数）、`total_damage`（总伤害）。

---

## EventPacket 联动

EntityTracker 在 Boss 击杀结算时自动向 EventPacket 发射信号（每位参与玩家各一次）：

| 信号名 | 触发时机 | 携带变量 |
| --- | --- | --- |
| `boss_settlement` | Boss 死亡结算 | `boss_id`, `boss_name`, `settlement_id`, `rank`, `damage`, `total_damage`, `participant_count` |

可在 `ArcartXEventPacket.yml` 的规则中配置对应信号，实现：

- 击杀庆祝字幕（通过 `SubtitlePlayable`）
- 额外邮件奖励（通过 `MailDispatchable`）
- 称号授予（通过 `TitleGrantable`）
- 导航至下一个 Boss（通过 `QuestGpsNavigable`）
- 聊天卡片广播（通过 `ChatCardSendable`）

---

## UI / Packet

### UI 文件

| 功能 | UI 文件 | 类型 | 说明 |
| --- | --- | --- | --- |
| Boss 追踪 HUD | `boss_tracker.yml` | HUD | 服务端周期推 `init` / `update`，Boss 死亡推 `close` |
| 攻击目标 HUD | `attack_target_hud.yml` | HUD | 命中时推 `init` / `update`，超时推 `close` |
| 排行奖励管理 | `ranking_rewards.yml` | 管理面板 | `config-list` / `scheduler-status` / `result` 推送 |
| 奖励配置编辑 | `reward_editor.yml` | 管理面板 | `config-detail` / `boss-list` / `result` 推送 |
| 奖励发放历史 | `reward_history.yml` | 管理面板 | `history-list` / `statistics` / `result` 推送 |

### 客户端指令通道

`Packet.send('AXS_ENTITY_TRACKER_REWARD', action, ...args)`

| action | 参数 | 说明 |
| --- | --- | --- |
| `list-configs` | `rewardType` | 请求奖励配置列表 |
| `scheduler-status` | — | 请求调度器状态 |
| `toggle` | `configId` | 切换奖励配置启用/禁用 |
| `manual-distribute` | `rewardType` | 手动发放奖励 |
| `save-config` | 配置 JSON | 保存奖励配置 |
| `open-history` | — | 打开发放历史界面 |
| `history-list` | `status`, `periodType`, `page` | 请求历史记录列表 |
| `retry` | `recordId` | 重试失败记录 |
| `open-manage` | — | 返回管理主界面 |

---

## 架构说明

### 模块入口

`EntityTrackerModule` 继承 `AbstractAXSModule`，同时实现 `ModuleCommandHandler`。模块启用时执行以下流程：

1. **加载配置** — 读取 `ArcartXEntityTracker.yml`，解析 Boss 追踪设置和攻击目标设置
2. **导出默认文件** — `bosses/ExampleBoss.yml`（首次）
3. **检测 MythicMobs** — 如不存在则跳过 Boss 追踪，仅保留攻击目标 HUD
4. **注册 UI** — 向 ArcartX 注册 Boss HUD 和 Target HUD
5. **启动服务** — `BossTrackerService`、`EntityTargetHudService`；若启用跨服排行则启动 `EntityTrackerCrossServerService`
6. **初始化奖励系统** — 创建 SQLite 数据库、建表、启动 `RankingRewardScheduler` 调度器
7. **注册 PAPI** — 注册 `AXSentitytracker` PlaceholderExpansion

### 跨模块能力

| 消费的 Capability | 来源模块 | 用途 |
| --- | --- | --- |
| `MailDispatchable` | Mail | `mail` 奖励动作发送预设邮件 |

### 关键类

| 类 | 职责 |
| --- | --- |
| `BossTrackerService` | Boss 追踪核心：会话管理、伤害监听、HUD 推送、结算触发 |
| `BossDamageSettlementService` | 死亡结算：排名计算、奖励分发、记录存储 |
| `EntityTargetHudService` | 攻击目标 HUD：命中检测、超时管理、HUD 推送 |
| `RankingRewardService` | 排行榜奖励：查询排行、遍历 Boss 配置、执行奖励 |
| `RankingRewardScheduler` | 定时调度：周/月任务的计算、调度和重新调度 |
| `EntityTrackerCrossServerService` | 跨服 Boss 最高伤害：结算后写库 + 广播，入站合并 |
| `RewardActionExecutor` | 奖励执行器：6 种动作统一入口，模板渲染 + PAPI |
| `EntityTrackerPlaceholderExpansion` | PAPI 输出：全局/槽位/结算三大类占位符 |

