# EntityTracker 实体追踪

## 功能定位

全方位的实体追踪方案，包含两大能力：

- **Boss 追踪** — 跟踪 MythicMob，把血量、距离、存活时间、实时伤害排行推给 HUD/聊天卡片。Boss 死亡时按伤害排行自动结算并发奖
- **攻击目标 HUD** — 实时显示玩家最近命中的活体目标：名称、生命、距离、坐标、实体类型、MythicMob ID

### 核心特性

**Boss 追踪：**
- **MythicMobs 联动**：自动检测配置中的 Boss 生成，按 `mob-id` 匹配
- **实时血条与排行**：HUD 显示 Boss 名称、血量、自己的伤害排名和 Top N 伤害排行
- **多 Boss 并行**：支持同时追踪多个 Boss 实体，每个 Boss 独立会话
- **优先级与排序**：多个 Boss 存活时按 `priority` 和配置的排序模式决定显示顺序
- **观察范围**：只有在指定距离内的玩家才会收到 Boss HUD
- **伤害排行结算**：Boss 死亡时按伤害排名自动发放奖励，支持 6 种奖励动作
- **补发机制**：管理员可通过 `reissue` 命令按结算记录补发奖励
- **EventPacket 联动**：结算时向每位参与玩家发射 `boss_settlement` 信号

**攻击目标 HUD：**
- **实时目标信息**：命中任意活体后显示名称、生命、距离、坐标、实体类型
- **超时自动关闭**：指定时间内未再次命中则 HUD 自动关闭
- **Boss 排除**：已配置追踪的 Boss 默认不触发攻击目标 HUD，避免重复

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | Boss HUD、攻击目标 HUD、聊天卡片和客户端数据包 | 模块 UI 不可用 |
| 按功能必需 | MythicMobs / MythicBukkit | Boss 追踪依赖 MythicMob ID、生成/死亡事件和 MythicItems 奖励 | Boss 追踪会跳过；普通攻击目标 HUD 仍可使用 |
| 可选 | PlaceholderAPI | 输出 EntityTracker PAPI，并解析部分显示文本 | PAPI 输出和变量解析不可用 |
| 可选 | NeigeItems | 奖励命令或物品库联动时发放/识别 NeigeItems 物品 | 只影响对应奖励动作 |
| 可选 | MythicLib / MMOItems | 配合奖励、物品展示或其他战斗生态识别 | 只影响对应联动 |

## 启用步骤

```yaml
modules:
  entitytracker:
    enabled: true
```

## 配置

EntityTracker 的配置分为 Boss 追踪和攻击目标两部分。

### Boss 追踪（`ArcartXEntityTracker.yml`）

```yaml
settings:
  debug: false
  refresh-interval-ticks: 5
  viewer-range: 48.0

bosses:
  ExampleBoss:
    enabled: true
    priority: 100
    mob-id: "ExampleBoss"
    title-format: "&c{display_name}"
    subtitle-format: "&7HP &c{health}/{max_health}"
    damage-ranking:
      enabled: true
      max-entries: 10
      rewards:
        enabled: true
        ranks:
          1:
            actions:
              - type: command
                command: "points give {player} 100"
              - type: mail
                preset-id: "boss_first_reward"
              - type: signal
                signal: "boss_first_cleared"
```

### 奖励动作类型

| `type` | 参数 | 说明 |
| --- | --- | --- |
| `command` | `command` | 以控制台身份执行命令，支持 `{player}`、`{rank}` 等占位符 |
| `message` | `text`, `target` | 发送消息给玩家或全服 |
| `neigeitems` | `item-id`, `amount` | 发放 NeigeItems 物品 |
| `mythicitems` | `item-id`, `amount` | 发放 MythicMobs 物品 |
| `mail` | `preset-id` | 通过 Mail 模块发送预设邮件（需 Mail 模块） |
| `signal` | `signal` | 向 EventPacket 发射自定义信号，可串联更多动作 |

### 攻击目标 HUD

玩家命中任意活体后实时展示目标信息 HUD。已配置追踪的 Boss 默认不触发此 HUD，避免重复显示。

```yaml
attack-target:
  debug: false
  refresh-interval-ticks: 5
  target-timeout-ms: 3000
  max-view-distance: 48.0
  ui-id: "AXS:attack_target_hud"
```

## 命令

> 权限：`arcartxsuite.admin`（管理命令）

| 命令 | 说明 |
| --- | --- |
| `/axs entitytracker status` | 查看模块状态、活跃会话数和奖励配置信息 |
| `/axs entitytracker reload` | 重载 EntityTracker 配置、UI 和追踪服务 |
| `/axs entitytracker sessions [mobId]` | 列出当前正在追踪的 Boss 会话。可选传入 `mobId` 过滤指定 Boss |
| `/axs entitytracker rank <entityUuid> [page]` | 查看指定 Boss 实体的实时伤害排行榜（UUID 可从 sessions 命令获取） |
| `/axs entitytracker settlements [page]` | 分页查看历史结算记录，包含 Boss 名称、击杀时间和参与人数 |
| `/axs entitytracker settlement <结算ID> [page]` | 查看指定结算的详细排名：每位参与者的伤害值和奖励 |
| `/axs entitytracker reissue <结算ID> <名次> [玩家]` | 按结算名次补发奖励，不指定玩家则发给原排名玩家 |

## PAPI

前缀：`%AXSentitytracker_*%`

### 全局信息

| 占位符 | 说明 |
| --- | --- |
| `%AXSentitytracker_sort_mode%` | 当前 Boss 排序模式 |
| `%AXSentitytracker_max_visible_bars%` | 配置中允许同时显示的最大 Boss 血条数 |
| `%AXSentitytracker_configured_boss_count%` | 配置文件中定义的 Boss 总数 |
| `%AXSentitytracker_damage_ranking_boss_count%` | 开启伤害排行的 Boss 数量 |
| `%AXSentitytracker_active_session_count%` | 当前活跃的 Boss 战斗会话数 |
| `%AXSentitytracker_active_viewer_count%` | 当前正在观察 Boss 的玩家总数 |
| `%AXSentitytracker_boss_count%` | 该玩家视野中的 Boss 数量 |
| `%AXSentitytracker_total_boss_count%` | 全服正在追踪的 Boss 总数 |

### 当前 Boss / 槽位

| 占位符 | 说明 |
| --- | --- |
| `%AXSentitytracker_current_<字段>%` | 视野中第 1 个 Boss 的信息（等同 `slot_1_<字段>`） |
| `%AXSentitytracker_slot_<N>_<字段>%` | 视野中第 N 个 Boss 的信息 |

常用 `<字段>`：`display_name`（名称）、`health_percent`（血量百分比）、`mob_id`（MythicMobs ID）、`viewer_rank`（玩家伤害排名）、`viewer_damage`（玩家累计伤害）、`top_<排名>_name`（排行第 N 名玩家名）、`top_<排名>_damage`（排行第 N 名伤害值）

### 最近结算

| 占位符 | 说明 |
| --- | --- |
| `%AXSentitytracker_last_<字段>%` | 玩家参与的最近一次 Boss 结算信息 |

常用 `<字段>`：`rank`（排名）、`damage`（伤害值）、`boss_name`（Boss 名称）、`total_participants`（参与人数）

## EventPacket 联动

EntityTracker 在 Boss 击杀结算时自动向 EventPacket 发射信号（每位参与玩家各一次）：

| 信号名 | 触发时机 | 携带变量 |
| --- | --- | --- |
| `boss_settlement` | Boss 死亡结算 | `boss_id`, `boss_name`, `settlement_id`, `rank`, `damage`, `total_damage`, `participant_count` |

可在 `ArcartXEventPacket.yml` 中配置对应规则实现击杀庆祝字幕、额外邮件奖励、称号授予等联动效果。

## UI / Packet

| 功能 | UI ID | 说明 |
| --- | --- | --- |
| Boss 追踪 | 配置中指定 | 服务端周期推 `init` / `update`，Boss 死亡推 `close` |
| 攻击目标 | `AXS:attack_target_hud` | 命中时推 `init` / `update`，超时推 `close` |
