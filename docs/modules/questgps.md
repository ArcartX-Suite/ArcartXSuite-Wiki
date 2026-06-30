---
title: QuestGPS 任务导航插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite QuestGPS Chemdah 任务追踪、路径寻路 + 3D模型标记、任务指引HUD，我的世界服务器任务导航插件。
---

# QuestGPS 任务导航

::: tip 付费模块
本模块为付费模块。授权由 [云端平台](/guide/cloud-modules) 统一管理：在 [cloud.021209.xyz](https://cloud.021209.xyz) 购买/领取授权后，于「装备模块」页面勾选到对应服务器即可，无需填写 `password` 或 `license.yml`。
:::
## 功能定位

将 Chemdah 任务系统的追踪和导航功能，通过 ArcartX 客户端实现**零服务端开销**的沉浸式 RPG 导航体验。

### 核心特性

- **Waypoint 罗盘导航**：ArcartX 客户端内置罗盘/距离指示器，自动指向追踪目标（纯客户端渲染）
- **3D 模型标记**：在导航目标位置生成客户端 3D 模型标记（Adyeshach 发包实体 + ArcartX 模型），支持自定义模型和动画
- **路径寻路**：从玩家到目标沿地面生成多个导航标记实体，智能绕开障碍物、液体和危险方块，支持 ±1 格台阶处理，玩家移动时动态更新路径
- **任务指引 HUD**：RPG 风格左侧竖排浮窗，显示当前任务名、目标清单（最多 3 条）、完成进度和导航坐标
- **任务菜单**：分类浏览任务、查看描述/奖励预览、接取/放弃/追踪操作
- **Chemdah 联动**：Chemdah 为任务 SSOT（名称、描述、目标进度、奖励执行）；`quests/*.yml` 仅作 overlay 白名单与可选覆盖
- **EventPacket 联动**：可通过 EventPacket 的 `questgps.offer` / `questgps.accept` / `questgps.track` 动作自动推送任务
- **主线门禁**：未完成必要主线时自动屏蔽支线/奇遇入口和部分命令

### 性能架构

| 组件 | 渲染位置 | 服务端开销 |
| --- | --- | --- |
| Waypoint 罗盘 | 客户端 | 追踪变更时 1 次发包 |
| 3D 模型标记 | 客户端（Adyeshach 包级别实体） | 路径更新时发包（可配置间隔） |
| 任务指引 HUD | 客户端 UI | 追踪变更时 1 次发包 |
| 地图目标同步 | 服务端 → Map 模块 | 追踪变更时 1 次内部调用 |

**所有可视化均为客户端渲染，追踪期间服务端零 tick 开销。**

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 任务菜单 UI、路点罗盘、模型渲染、指引 HUD | 模块无法启动 |
| 必需 | Chemdah | 读取任务、目标、追踪状态和奖励预览 | 模块不会加载 |
| 可选 | Adyeshach | 在导航目标生成客户端假实体作为模型载体 | 3D 标记功能跳过，罗盘仍可用 |
| 可选 | Map 模块 | 将任务目标同步为地图临时目标 | 地图联动关闭，其他功能正常 |
| 可选 | EventPacket 模块 | 通过事件动作推送任务接取/追踪 | 不影响 Chemdah 原生任务读取 |

## 启用步骤

```yaml
modules:
  questgps:
    enabled: true
```

## Chemdah 整合

QuestGPS 以 **Chemdah 为单一事实来源**，overlay 只负责「哪些任务进菜单」以及可选覆盖项。

| 层级 | 职责 |
| --- | --- |
| Chemdah | 任务名称、描述、目标 key/进度、tracker 导航、奖励实际发放 |
| QuestGPS overlay | 白名单登记、排序、门禁、hooks、可选导航坐标与展示字段覆盖 |
| QuestGPS 菜单 | 分类 Tab、接取/放弃、追踪、奖励预览、指引 HUD |

**overlay 根键** = Chemdah `Template.getId()`，使用**裸 ID**（如 `example_mainline_quest`），不要使用 `main/example_quest` 这类路径式前缀。

## 配置

配置文件路径：`plugins/ArcartXSuite/data/questgps/ArcartXQuestGPS.yml`  
任务 overlay 目录：`data/questgps/quests/*.yml`（由 `quests-directory` 指定）

### ArcartXQuestGPS.yml 配置项一览

#### `debug`

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `false` | 开发者调试日志 |

#### `client`

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `packet-id` | string | `AXS_QUESTGPS` | 发包协议 ID，须与 UI 脚本一致 |
| `menu-ui-id` | string / list | `AXS:questgps_menu` | 任务菜单 UI；支持列表多 UI 同时发包，见 [多 UI 发包](/guide/multi-ui) |
| `guide-ui-id` | string / list | `AXS:questgps_guide` | 追踪时左侧指引 HUD UI |
| `register-ui-on-enable` | boolean | `true` | 模块启用时向 ArcartX 注册 UI 资源 |
| `overwrite-ui-files` | boolean | `false` | 是否覆盖服内已有 UI 文件 |

#### `presentation`

| 字段 | 类型 | 默认值 | 可选值 | 说明 |
| --- | --- | --- | --- | --- |
| `source` | string | `chemdah` | `chemdah` / `overlay` | UI 展示**全局唯一来源**：名称、描述、目标文案、目标说明、奖励预览均读同一侧 |

单任务可在 overlay 写 `presentation.source` 覆盖。已废弃 `presentation.*-source` 与 `merge`。

#### `category`

| 字段 | 类型 | 默认值 | 可选值 | 说明 |
| --- | --- | --- | --- | --- |
| `source` | string | `chemdah` | `chemdah` / `overlay` | 分类 Tab **全局唯一来源** |

| `source` | 分类由谁决定 |
| --- | --- |
| `chemdah` | Chemdah `meta.type`，须在 `categories` 注册 |
| `overlay` | 每条 overlay 的 `category` 字段 |

未注册分类的任务**不进菜单**。删除 `categories` 段可关闭分类 Tab。

#### `discovery`

| 字段 | 类型 | 默认值 | 可选值 | 说明 |
| --- | --- | --- | --- | --- |
| `mode` | string | `auto` | `overlay` / `auto` | 任务列表来源 |

| 模式 | 行为 |
| --- | --- |
| `overlay` | 仅 `quests/*.yml` 中 `enabled: true` 的任务进菜单 |
| `auto` | 扫描全部 Chemdah 模板；无 overlay 的模板自动生成最小定义 |

#### `categories`

定义任务菜单顶部的分类 Tab。**无内置分类**，须在此注册；Chemdah `meta.type` 或 overlay `category` 必须与下列 **根键 ID** 一致。

每个分类条目：

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `display-name` | string | 分类 ID | Tab 显示名 |
| `sort-order` | int | `300` | Tab 排序，越小越靠前 |

默认示例 ID：`mainline`（主线）、`side`（支线）、`encounter`（奇遇）。可新增如 `daily`、`L1`。

```yaml
categories:
  mainline:
    display-name: "主线"
    sort-order: 0
  daily:
    display-name: "日常"
    sort-order: 300
```

#### `navigation`

| 字段 | 类型 | 默认值 | 可选值 | 说明 |
| --- | --- | --- | --- | --- |
| `enabled` | boolean | `true` | — | 是否启用导航（waypoint + marker） |
| `waypoint-style-id` | string | `default` | — | ArcartX `waypoint/*.yml` 中的样式 ID（全局默认）；overlay `navigation.point.style-id` 可覆盖单点 |
| `quest-id-prefix` | string | `AXS-questgps-` | — | 内部 waypoint ID 前缀 |
| `remove-on-finish` | boolean | `true` | — | 任务/目标完成后自动清除追踪 |
| `mode` | string | `hybrid` | `chemdah` / `overlay` / `hybrid` | 导航坐标来源 |

| `mode` | 坐标来源 |
| --- | --- |
| `chemdah` | 仅 Chemdah tracker |
| `overlay` | 仅 overlay `navigation` / `tasks.*.navigation` |
| `hybrid` | Chemdah tracker 优先，无 tracker 时用 overlay |

##### `navigation.marker`（3D 路径标记，依赖 Adyeshach + ArcartX 客户端）

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | 是否启用 3D 模型路径标记；关闭后仅 waypoint 罗盘 |
| `model-id` | string | `nav_beacon` | ArcartX 模型 ID |
| `scale` | double | `1.0` | 模型缩放 |
| `default-state` | string | `idle` | 模型默认状态 |
| `animation` | string | `rotate` | 循环动画名 |
| `y-offset` | double | `2.0` | 模型相对目标 Y 偏移（格） |
| `path-interval` | double | `3.0` | 路径标记间距（格） |
| `path-max-markers` | int | `20` | 路径标记最大数量 |
| `path-update-ticks` | int | `10` | 路径更新间隔（tick，20=1秒） |
| `path-max-distance` | double | `64` | 路径最大渲染距离（格） |
| `path-max-iterations` | int | `2000` | A* 寻路最大迭代（100–50000） |

Waypoint 罗盘样式在 ArcartX 的 `waypoint/` 目录定义，与 `waypoint-style-id` / overlay `style-id` 对应。

#### `database`

与 QuestGPS 菜单无关，仅影响 **Chemdah 玩家任务进度** 的存储后端。

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `false` | `true` = QuestGPS 启动时为 Chemdah 注册 MySQL 数据库 |
| `load-in-join-event` | boolean | `true` | 玩家进服加载 Chemdah 档案 |
| `release-in-quit-event` | boolean | `true` | 玩家退服释放档案 |
| `disable-auto-save` | boolean | `false` | `true` = 禁止 Chemdah 自动保存 |
| `disable-auto-create-table` | boolean | `false` | `true` = 禁止自动建表 |

::: info 何时需要开启
Chemdah `config.yml` 使用 `database.use=LOCAL`（本地）时保持 `enabled: false`。  
使用 `database.use=SQL`（MySQL）时设 `enabled: true`。QuestGPS 会优先使用内置 `DatabaseMySQL`（基于 Chemdah 免费版 `Relational`），免费版通常无需 patched JAR。
:::

#### `gate`（主线门禁）

**`required-mainline-quest-ids` 为空时整段门禁关闭**，下列 `blocked-*` 均不生效。

| 字段 | 类型 | 默认值 | 填写说明 | 填写示例 |
| --- | --- | --- | --- | --- |
| `required-mainline-quest-ids` | list | `[]` | 须**全部完成**的 Chemdah 任务裸 ID；过关后解锁下方限制 | `example_mainline_quest` |
| `blocked-categories` | list | — | 未过关时锁定的分类 Tab ID，须与 `categories` 根键一致 | `side`、`encounter` |
| `blocked-command-prefixes` | list | — | 未过关时拦截的命令前缀（**不含** `/`） | `warp`、`rtp`、`shop` |
| `blocked-module-entries` | list | `[]` | 未过关时锁定的模块入口 ID；其他模块打开 UI 前调用 `QuestGpsNavigable.moduleEntryLocked(player, id)` | `market`、`regions_warp` |
| `blocked-event-rule-ids` | list | `[]` | 未过关时不触发的 EventPacket 规则 ID，与 `data/eventpacket/rules/*.yml` **根键**一致 | `first_join_offer_mainline`、`kill_zombie_trigger_encounter` |
| `deny-message` | string | 见配置 | 拦截时聊天提示（支持 `&` 颜色码） | `&c请先完成主线` |
| `deny-chat-card` | string | `""` | 可选，ArcartX 聊天卡片 ID | `quest_gate_deny` |
| `deny-subtitle` | string | `""` | 可选，ArcartX 字幕组 ID | `mainline_gate` |

```yaml
gate:
  required-mainline-quest-ids:
    - "example_mainline_quest"
  blocked-categories:
    - side
    - encounter
  blocked-command-prefixes:
    - "warp"
    - "rtp"
  blocked-module-entries:
    - "market"
  blocked-event-rule-ids:
    - "first_join_offer_mainline"
    - "kill_zombie_trigger_encounter"
  deny-message: "&c你需要先完成必要主线任务。"
  deny-chat-card: ""
  deny-subtitle: ""
```

| 拦截项 | 触发时机 |
| --- | --- |
| `blocked-categories` | 任务菜单中无法查看/接取该分类 |
| `blocked-command-prefixes` | 玩家聊天命令被 `PlayerCommandPreprocessEvent` 取消 |
| `blocked-module-entries` | 其他模块查询 `moduleEntryLocked` 为 true 时自行拒绝打开 |
| `blocked-event-rule-ids` | EventPacket 执行规则前查询 `eventRuleLocked` 为 true 则跳过 |

#### 其他根级字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `quests-directory` | string | `quests` | overlay 任务目录名，相对 `data/questgps/` |
| `config-version` | int | `1` | 配置版本号（预留） |

### 配置示例（精简）

```yaml
debug:
  enabled: false

client:
  packet-id: "AXS_QUESTGPS"
  menu-ui-id: "AXS:questgps_menu"
  guide-ui-id: "AXS:questgps_guide"
  register-ui-on-enable: true
  overwrite-ui-files: false

presentation:
  source: chemdah

category:
  source: chemdah

discovery:
  mode: overlay

categories:
  mainline:
    display-name: "主线"
    sort-order: 0
  side:
    display-name: "支线"
    sort-order: 100
  encounter:
    display-name: "奇遇"
    sort-order: 200

navigation:
  enabled: true
  waypoint-style-id: "default"
  quest-id-prefix: "AXS-questgps-"
  remove-on-finish: true
  mode: hybrid
  marker:
    enabled: true
    model-id: "nav_beacon"
    scale: 1.0
    default-state: "idle"
    animation: "rotate"
    y-offset: 2.0
    path-interval: 3.0
    path-max-markers: 20
    path-update-ticks: 10
    path-max-distance: 64
    path-max-iterations: 2000

database:
  enabled: false

gate:
  required-mainline-quest-ids: []
  blocked-categories:
    - side
    - encounter
  blocked-command-prefixes:
    - "warp"
  blocked-module-entries: []
  blocked-event-rule-ids: []
  deny-message: "&c你需要先完成必要主线任务。"

quests-directory: "quests"
```

### `quests/*.yml` overlay 配置项一览

任务文件位于 `data/questgps/quests/*.yml`。**根键** = Chemdah `Template.getId()`（裸 ID，如 `example_mainline_quest`）。

#### 任务级字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | `false` 不进菜单（`discovery.mode: overlay` 时等同于未登记） |
| `sort-order` | int | `0` | 菜单内排序，越小越靠前 |
| `allow-abandon` | boolean | `false` | 是否显示 UI「放弃」按钮 |
| `category` | string | — | 仅 `category.source=overlay` 时**必填**；值须为 `categories` 已注册 ID |
| `required-mainline` | list | `[]` | 接取前须完成的主线 Chemdah ID 列表 |
| `display-name-override` | string | `""` | `presentation.source=overlay` 时覆盖任务名 |
| `description` | list | `[]` | `presentation.source=overlay` 时 overlay 描述行 |
| `presentation.source` | string | 继承全局 | 单任务覆盖 `presentation.source` |
| `rewards` | list | `[]` | 奖励预览（仅 UI），见下方「奖励预览」 |
| `navigation` | map | 见下 | 任务级导航坐标 |
| `tasks` | map | — | 子目标 overlay，key = Chemdah 目标 ID |
| `hooks` | map | — | EventPacket `command-signal` 信号名，见下 |

#### `hooks` 各阶段信号

| 键 | 说明 | 示例信号名 |
| --- | --- | --- |
| `triggered` | 任务出现在菜单 / 被 offer | `questgps_main_triggered` |
| `accepted` | Chemdah 接取成功 | `questgps_main_accepted` |
| `abandoned` | 玩家放弃任务 | `questgps_main_abandoned` |
| `completed` | 任务完成 | `questgps_main_completed` |
| `track-changed` | 开始/取消导航追踪 | `questgps_track_changed` |

#### `navigation` / `tasks.<id>.navigation`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `enabled` | boolean | 是否启用该级导航坐标（任务级 `navigation`） |
| `point.world` | string | 世界名 |
| `point.x` / `y` / `z` | double | 坐标 |
| `point.title` | string | waypoint 罗盘显示标题 |
| `point.style-id` | string | waypoint 样式 ID，对应 ArcartX `waypoint/*.yml`；空则用全局 `waypoint-style-id` |
| `point.map-label` | string | 同步到 Map 模块时的标签 |

`navigation.mode: hybrid` 时 Chemdah tracker 优先；无 tracker 时使用上述 overlay 坐标。子目标 `tasks."0".navigation` 覆盖任务级坐标。

#### `tasks.<id>` 子目标

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `sort-order` | int | 子目标在 UI 中的排序 |
| `display-text` | string | overlay 目标文案（`presentation.source=overlay` 时） |
| `description` | list | overlay 目标说明行 |
| `navigation` | map | 该子目标独立导航点（字段同 `navigation.point`） |

### 任务 overlay 示例

```yaml
# data/questgps/quests/mainline.yml
example_mainline_quest:
  enabled: true
  sort-order: 10
  allow-abandon: false
  required-mainline: []
  hooks:
    triggered:
      - "questgps_main_triggered"
    accepted:
      - "questgps_main_accepted"
    completed:
      - "questgps_main_completed"
    track-changed:
      - "questgps_track_changed"
  navigation:
    enabled: true
    point:
      world: "world"
      x: 0
      y: 80
      z: 0
      title: "初入岛屿"
      style-id: "default"
      map-label: "主线目标"
  tasks:
    "0":
      sort-order: 10
      navigation:
        world: "world"
        x: 12
        y: 80
        z: -8
        title: "与长老对话"
        style-id: "default"
        map-label: "任务目标"
```

### 奖励预览配置详解

`rewards` 段只影响任务菜单中的**奖励预览图标与文案**，不会在接取/完成时自动发放物品。实际奖励仍由 Chemdah 任务模板执行。

展示数据来源由 `presentation.source`（或单任务 `presentation.source`）控制：

| 值 | 行为 |
| --- | --- |
| `chemdah`（默认） | 奖励预览读 Chemdah `QUEST_COMPLETED` agent / `reward` 列表 |
| `overlay` | 仅使用 overlay `rewards` 列表 |

#### 支持的 `type`

| `type` | 依赖插件 / 说明 | 主要 ID 字段 |
| --- | --- | --- |
| `neigeitems` | NeigeItems | `neige-item-id` |
| `mythicmobs` / `mythicitems` | MythicMobs | `mythic-item-id`（别名：`mythicmobs-item-id`、`item-id`） |
| `overture` | Overture | `overture-item-id`（别名：`overture-id`） |
| `mmoitems` | MMOItems | `type-id` + `id`（别名：`mmo-type`/`mmoitems-type`、`mmo-id`/`mmoitems-id`） |
| `material` / `itemstack` | 原版 Bukkit 材质 | `material`（如 `DIAMOND`） |
| `title` | Title 模块（可选） | `title-id` |
| `text` | 无 | 纯文本行，无物品图标 |

#### 通用可选字段

| 字段 | 说明 |
| --- | --- |
| `amount` | 数量，默认 `1` |
| `display-name` | 预览标题（覆盖物品/称号默认名） |
| `text` | 预览描述副文案 |
| `lore` | 物品 lore 行（`material` / 物品库类型可用） |
| `fallback-material` | 物品库未安装或 ID 无效时，用该原版材质占位显示 |

#### 各物品库完整示例

```yaml
rewards:
  # NeigeItems
  - type: neigeitems
    neige-item-id: "starter_sword"
    amount: 1
    display-name: "&6新手长剑"
    text: "&7NeigeItems 示例武器"
    lore:
      - "&7完成任务后可从 Chemdah 获得"
    fallback-material: "IRON_SWORD"

  # MythicMobs（mythicitems 与 mythicmobs 等价）
  - type: mythicmobs
    mythic-item-id: "starter_relic"
    amount: 1
    display-name: "&d远古遗物"
    fallback-material: "AMETHYST_SHARD"
  # 等价写法：
  # - type: mythicitems
  #   item-id: "starter_relic"

  # Overture
  - type: overture
    overture-item-id: "treasure_chest"
    amount: 2
    display-name: "&6宝箱"
    text: "&7Overture 自定义物品"
    fallback-material: "CHEST"

  # MMOItems（type + id 双字段）
  - type: mmoitems
    type-id: "SWORD"
    id: "STEEL_LONGSWORD"
    amount: 1
    display-name: "&b钢之长剑"
    fallback-material: "IRON_SWORD"
  # 等价字段名：
  #   mmo-type / mmoitems-type  → type-id
  #   mmo-id / mmoitems-id      → id

  # 原版材质（material 与 itemstack 等价）
  - type: material
    material: "GOLD_INGOT"
    amount: 16
    display-name: "&e金锭"
    lore:
      - "&7基础货币奖励"

  # Title 称号预览（需 Title 模块；通过 TitleConfigQueryable 读取显示名/品质）
  - type: title
    title-id: "newcomer"
    duration: "permanent"          # permanent 显示「永久」；其他值显示「限时 xxx」
    display-name: "&e称号: 初来乍到"
    text: "&7完成主线后解锁"

  # 纯文本（无物品 JSON，适合经验/货币等 Chemdah 脚本奖励说明）
  - type: text
    display-name: "&a经验 +500"
    text: "&7实际数值由 Chemdah agent 发放"
    amount: 1
```

::: tip 物品库未安装时
对应 `type` 会尝试用 `fallback-material` 生成占位图标；若仍失败，则退化为 `text` 样式并打服务端 fine 日志。
:::

::: warning 与 Chemdah 奖励的关系
overlay `rewards` **不会**替代 Chemdah 发奖逻辑。请保证 Chemdah 任务模板内已配置真实 `give` / `item` agent；此处仅用于让玩家在菜单里提前看到奖励外观。
:::

### 支线与奇遇示例

```yaml
# data/questgps/quests/side.yml
example_side_quest:
  enabled: false
  sort-order: 100
  allow-abandon: true
  required-mainline:
    - "example_mainline_quest"

# data/questgps/quests/encounter.yml
example_encounter_quest:
  enabled: false
  sort-order: 200
  allow-abandon: true
```

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs questgps status` | 查看任务导航模块状态和已加载任务数 |
| `/axs questgps reload` | 重载任务导航配置和 UI |
| `/axs questgps open <玩家>` | 为在线玩家打开任务导航界面 |

### 玩家命令（权限：`arcartxsuite.questgps.use`）

| 命令 | 说明 |
| --- | --- |
| `/questgps` 或 `/questgps open` | 打开任务导航菜单 |
| `/questgps cleartrack` | 清除当前追踪，关闭所有导航可视化 |

## 导航系统

### 导航触发流程

```
玩家在菜单中点击"追踪任务"或"追踪目标"
  │
  ├─① Waypoint 罗盘 — ArcartXWaypointBridge.addWaypoint()
  │   客户端显示罗盘方向指示 + 距离数字
  │
  ├─② 路径寻路 + 3D 模型标记
  │   异步计算路径 → 沿路径采样点生成多个 ArmorStand 私有实体
  │   + ArcartXNetworkSender 点对点发包附加模型 + 动画
  │   + 玩家移动时动态更新路径，实体复用池避免重复创建
  │
  ├─③ 任务指引 HUD — bridge.openUi() + sendPacket()
  │   左侧浮窗显示任务名、目标列表、进度条、坐标
  │
  └─④ 地图目标同步 — MapNavigable.upsertExternalTarget()
      （如果 Map 模块可用）

玩家点击"取消追踪"或 HUD 上的 ✕
  │
  ├─① removeWaypoint()
  ├─② hideMarker() → 清除所有路径标记实体
  ├─③ closeGuide()
  └─④ clearExternalTargets()
```

### Waypoint 罗盘

ArcartX 客户端内置的路点系统，在玩家屏幕上方/指定位置显示方向箭头和距离。样式由 ArcartX 插件的 `waypoint/*.yml` 配置控制：

- 指南针指针方向
- 距离数字格式
- 指示器图标和颜色
- 到达范围和淡出动画

配置 `waypoint-style-id` 即可切换不同的路点显示风格。

### 3D 模型标记 + 路径寻路

从玩家到导航目标沿地面生成多个自定义 3D 模型标记（如箭头、光柱、水晶等），形成一条可视化导航路径。技术实现：

1. **路径寻路** — 异步计算从玩家到目标的地面路径，智能绕开固体障碍物、液体、危险方块（仙人掌、岩浆块、营火等），支持 ±1 格台阶
2. **路径采样** — 简化原始路径后按配置间距采样，生成最终标记点序列
3. **Adyeshach 私有临时实体** — 每个标记点生成一个不可见的 ArmorStand（Marker 模式，无碰撞体积），仅对追踪玩家可见
4. **ArcartX 模型附加** — 通过 `ArcartXNetworkSender` 点对点发包为每个实体附加自定义模型 + 动画
5. **实体朝向** — 每个标记实体面向下一个路径点，最后一个面向目标终点
6. **动态更新** — 玩家移动时异步重新计算路径，现有实体通过传送复用，多余实体移除、不足时创建新实体

**特点**：
- 真实路径寻路，智能绕开障碍物和危险方块
- 异步计算 + 主线程实体操作，不卡服
- 实体复用池，减少创建/销毁开销
- 支持任意 ArcartX 模型（blockbench 导出等）
- 支持自定义动画和状态
- 单人可见（不影响其他玩家）

### 任务指引 HUD

追踪任务时自动打开的左侧竖排浮窗（`ArcartX-Suite:questgps_guide`），内容包括：

- **任务名** — 当前追踪的任务名称
- **进度条** — 绿色点阵进度条 `■■■□□` 显示完成比例
- **目标列表** — 最多 3 条任务目标，`✓` 已完成 / `◈` 进行中
- **导航坐标** — 目标位置的世界名和 XYZ 坐标
- **关闭按钮** — 点击 ✕ 取消追踪

视觉风格：深色半透明背景 + 左侧金色强调竖条 + 首次打开滑入动画。

## UI / Packet

菜单 UI 发包结构对齐 Title 模块：**Map + entryKey 模板列表**，分类 Tab 由服务端 `categories` Map 动态驱动（不再硬编码主线/支线/奇遇）。

| 功能 | UI ID | 说明 |
| --- | --- | --- |
| 任务菜单 | `ArcartX-Suite:questgps_menu` | 动态分类 Tab、任务列表、详情、奖励预览、接取/放弃/追踪 |
| 任务指引 HUD | `ArcartX-Suite:questgps_guide` | 追踪中显示任务目标（最多 3 条）与导航坐标 |

### Menu Packet 主要字段

| 字段 | 说明 |
| --- | --- |
| `packetId` | 包标识（`AXS_QUESTGPS`） |
| `categories` | `Map<categoryId, {id, name, sort_order, selected}>`，驱动分类 Tab |
| `pages` | `Map<pageId, {id, name, count, selected}>`（available / active / completed） |
| `quests` | `Map<questId, {id, name, summary, state, trackable, selected}>`，当前页任务列表 |
| `tasks` | `Map<taskId, {id, text, status, completed, trackable, tracked}>`，选中任务的目标 |
| `rewards` | `Map<rewardId, {id, title, description, type, amount, ...}>`，选中任务的奖励预览 |
| `categoryId` / `categoryName` | 当前分类 |
| `pageId` / `pageName` | 当前页签 |
| `availableCount` / `activeCount` / `completedCount` | 各页签任务数量 |
| `selectedQuestId` / `selectedQuestName` / … | 选中任务详情摘要 |
| `canAccept` / `canAbandon` / `canTrackQuest` / `canTrackTask` / `canClearTrack` | 操作按钮可见性 |
| `navigationReady` | 导航子系统是否就绪 |

客户端 `packetHandler` 收到包后，为模板控件绑定 `entryKey`（与 Title 菜单相同模式），再渲染列表。

### Guide Packet 主要字段

| 字段 | 说明 |
| --- | --- |
| `active` | 是否有活跃追踪 |
| `questName` | 追踪的任务名称 |
| `completedCount` / `totalCount` / `progressText` | 目标完成进度 |
| `tasks` | `Map<taskId, {id, text, completed, status}>`，最多展示 3 条 |
| `taskCount` | 当前 `tasks` Map 条目数 |
| `hasNav` / `navWorld` / `navX` / `navY` / `navZ` | 导航目标坐标 |

## EventPacket 联动

QuestGPS 的 `hooks` 字段向 EventPacket 发出 command-signal，并通过 `QuestGpsNavigable` capability 接收来自 EventPacket 的任务推送动作：

| 动作类型 | 参数 | 说明 |
| --- | --- | --- |
| `questgps.offer` | `quest-id`、`open-menu` | 向玩家推送任务并可选打开菜单 |
| `questgps.accept` | `quest-id` | 直接为玩家接取任务 |
| `questgps.track` | `quest-id` | 为玩家开始追踪指定任务 |

EventPacket 配置示例（`data/eventpacket/rules/onboarding.yml`）：

```yaml
first_join_questgps:
  enabled: true
  trigger: command-signal
  signal: "first_register"
  repeatable: false
  actions:
    - type: questgps.offer
      quest-id: "example_mainline_quest"
      open-menu: true
```


