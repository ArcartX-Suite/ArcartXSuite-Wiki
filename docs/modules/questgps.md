---
title: QuestGPS 任务导航插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite QuestGPS Chemdah 任务追踪、路径寻路 + 3D模型标记、任务指引HUD，我的世界服务器任务导航插件。
---

# QuestGPS 任务导航

::: tip 付费模块
本模块为付费模块。从 **1.2.0-beta** 起，授权由 [云端平台](/guide/cloud-modules) 统一管理：在 [cloud.021209.xyz](https://cloud.021209.xyz) 购买/领取授权后，于「装备模块」页面勾选到对应服务器即可，无需填写 `password` 或 `license.yml`。
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

**overlay 根键** = Chemdah `Template.getId()`，使用**裸 ID**（如 `gps_main_newcomer`），不要使用 `main/example_quest` 这类路径式前缀。

## 配置

### 主配置（`ArcartXQuestGPS.yml`）

```yaml
debug:
  enabled: false

client:
  packet-id: "AXS_QUESTGPS"
  # 支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)
  menu-ui-id: "ArcartX-Suite:questgps_menu"       # 任务菜单 UI
  guide-ui-id: "ArcartX-Suite:questgps_guide"     # 任务指引 HUD
  register-ui-on-enable: true
  overwrite-ui-files: false

navigation:
  enabled: true
  waypoint-style-id: "default"
  quest-id-prefix: "AXS-questgps-"      # 路径点 ID 前缀
  remove-on-finish: true
  # 导航坐标来源: chemdah | overlay | hybrid（默认，Chemdah tracker 优先）
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

# UI 展示字段来源（每字段: chemdah | overlay | merge）
presentation:
  name-source: chemdah
  description-source: chemdah
  task-text-source: chemdah
  task-description-source: chemdah
  rewards-source: chemdah

# 分类解析策略（overlay 的 category 仅 overlay/merge 模式生效）
category:
  source: chemdah                    # chemdah | overlay | merge
  id-prefix-rules:                   # 优先于 meta.type，适用于 gps_main_* 命名
    gps_main_: mainline
    gps_side_: side
    gps_encounter_: encounter

# 任务列表发现: overlay（白名单）| auto（扫描全部 Chemdah 模板）
discovery:
  mode: overlay

database:
  enabled: false                     # Chemdah MySQL 扩展（需 patched/付费 JAR）

# 分类 Tab 显示名与排序（可与 Chemdah meta.type 或 id-prefix 目标 ID 对应）
categories:
  # L1:
  #   display-name: "第一章"
  #   sort-order: 0
  # daily:
  #   display-name: "日常"
  #   sort-order: 150

quests-directory: "quests"
```

### 导航标记配置详解

导航标记通过 Adyeshach 插件生成一个仅对追踪玩家可见的客户端假实体（ArmorStand），并由 ArcartX 的 `ArcartXEntityManager` 为其附加自定义 3D 模型。

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | 是否启用 3D 模型标记；禁用时仅使用 Waypoint 罗盘 |
| `model-id` | string | `nav_beacon` | ArcartX 模型配置中定义的模型 ID |
| `scale` | double | `1.0` | 模型缩放比例 |
| `default-state` | string | `idle` | 模型默认状态名称 |
| `animation` | string | `rotate` | 客户端循环播放的动画名称 |
| `y-offset` | double | `2.0` | 模型显示位置相对目标坐标的垂直偏移（方块数） |
| `path-interval` | double | `3.0` | 路径标记间距（沿路径每隔多少格放一个标记） |
| `path-max-markers` | int | `20` | 路径标记最大数量（超出后截断远端标记） |
| `path-update-ticks` | int | `10` | 路径更新间隔（tick，20 = 1秒，玩家移动后重新计算路径） |
| `path-max-distance` | double | `64` | 路径最大渲染距离（超出此距离不显示路径标记） |
| `path-max-iterations` | int | `2000` | 寻路最大迭代次数（越大路径越精确但越耗时） |

**自定义模型步骤**：

1. 在 ArcartX 的模型配置中定义你的导航标记模型（参考 ArcartX 模型文档）
2. 为模型配置动画状态（如 `idle`、`rotate`）
3. 将模型 ID 填入 `marker.model-id`
4. 重载模块：`/axs questgps reload`

### 分类解析

任务最终归入哪个菜单 Tab，由 `category.source` 与下列规则共同决定：

| 优先级 | 规则 | 说明 |
| --- | --- | --- |
| 1 | `category.id-prefix-rules` | 如 `gps_main_` → `mainline`（**优先于** `meta.type`） |
| 2 | Chemdah `meta.type` | 需在 `categories` 段注册对应 ID（如 `L1`、`welcome`） |
| 3 | Chemdah 模板 path | 低优先级回退 |
| 4 | overlay `category` | 仅当 `category.source` 为 `overlay` 或 `merge` 时作为覆盖 |
| 5 | 回退 | 无法解析时归入 `mainline` 并打 warning |

**典型场景**：服内 Chemdah 任务 `meta.type` 均为 `L1`，但希望 UI 仍按主线/支线 Tab 展示 —— 给任务 ID 加 `gps_main_` / `gps_side_` 前缀，并配置 `id-prefix-rules` 即可。

### 自定义任务分类

除了内置三个分类，可在 `categories` 段定义任意 Tab（显示名 + 排序）：

| 内置分类 | ID | 默认显示名 | sortOrder |
| --- | --- | --- | --- |
| 主线 | `mainline` | 主线 | 0 |
| 支线 | `side` | 支线 | 100 |
| 奇遇 | `encounter` | 奇遇 | 200 |

```yaml
categories:
  L1:
    display-name: "第一章"
    sort-order: 0
  welcome:
    display-name: "新手引导"
    sort-order: 50
  daily:
    display-name: "日常"
    sort-order: 150
```

overlay 中的 `category:` **可选**。默认 `category.source: chemdah` 时，不写 `category` 也会按上述规则自动归类；仅在需要强制覆盖时填写：

```yaml
gps_daily_gather_herbs:
  enabled: true
  category: daily          # 仅 overlay/merge 模式下强制归入 daily Tab
```

**跨文件分布**：同一分类的任务可写在 `quests/` 下不同 yml 文件中，分类由解析规则决定，不必与文件名绑定。

### 路点样式配置

Waypoint 罗盘的显示样式由 ArcartX 插件的 `waypoint/*.yml` 文件控制。`waypoint-style-id` 对应其中的样式 key。

### 主线门禁配置

```yaml
gate:
  required-mainline-quest-ids: []       # 必须完成的主线 ID，为空则不启用门禁
  blocked-categories:                   # 门禁启用时屏蔽的任务分类
    - side
    - encounter
  blocked-command-prefixes:             # 门禁启用时拦截的命令前缀
    - "warp"
    - "rtp"
    - "shop"
  blocked-module-entries: []            # 门禁启用时屏蔽的其他模块入口
  blocked-event-rule-ids: []            # 门禁启用时屏蔽的 EventPacket 规则 ID
  deny-message: "&c你需要先完成必要主线任务。"
  deny-chat-card: ""
  deny-subtitle: ""
```

### 任务 overlay 字段详解

任务文件位于 `data/questgps/quests/*.yml`。根键 **必须** 与 Chemdah 模板 ID 完全一致（裸 ID）：

```yaml
# data/questgps/quests/mainline.yml
gps_main_newcomer:                       # Chemdah Template.getId()
  enabled: true
  # category: mainline                   # 可选；默认由 id-prefix / meta.type 解析
  sort-order: 10
  allow-abandon: false
  required-mainline: []

  presentation:                          # 可选，覆盖全局 presentation 策略
    name-source: chemdah
    description-source: chemdah
    task-text-source: chemdah
    task-description-source: merge
    rewards-source: chemdah

  hooks:
    triggered:
      - "questgps_main_triggered"
    accepted:
      - "questgps_main_accepted"
    completed:
      - "questgps_main_completed"
    track-changed:
      - "questgps_track_changed"

  rewards:                               # 奖励预览（仅 UI）；实际发放由 Chemdah 执行
    - type: neigeitems
      neige-item-id: "starter_sword"
      amount: 1
      display-name: "&6新手长剑"
      fallback-material: "IRON_SWORD"
    - type: title
      title-id: "newcomer"
      duration: "permanent"
      display-name: "&e称号: 初来乍到"
    - type: text
      display-name: "&a经验"
      text: "&7由 Chemdah 执行，这里只做预览。"

  navigation:                            # hybrid/overlay 模式下无 Chemdah tracker 时回退
    enabled: true
    point:
      world: "world"
      x: 0
      y: 80
      z: 0
      title: "初入岛屿"
      style-id: "default"
      map-label: "主线目标"

  tasks:                                 # key 与 Chemdah 目标 ID 一致（常为 "0"、"1"）
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

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `enabled` | boolean | `false` 时不进菜单（overlay 白名单模式下等同于未登记） |
| `category` | string | **可选**。强制分类覆盖，见「分类解析」 |
| `display-name-override` | string | 覆盖任务名（受 `presentation.name-source` 控制） |
| `description` | list | overlay 描述行（受 `presentation.description-source` 控制） |
| `sort-order` | int | 菜单内排序（升序） |
| `allow-abandon` | boolean | 是否允许放弃（主线建议 `false`） |
| `required-mainline` | list | 接取前必须完成的主线 Chemdah ID 列表 |
| `presentation` | map | 单任务级展示字段来源覆盖 |
| `hooks` | map | 各阶段 EventPacket 信号名 |
| `rewards` | list | 奖励预览，支持 `neigeitems` / `mythicmobs` / `mmoitems` / `title` / `material` / `text` |
| `navigation` | map | 任务级导航坐标（`navigation.mode: hybrid` 时 Chemdah 优先） |
| `tasks.<id>` | map | 子目标排序与 overlay 导航；`display-text` 等为可选覆盖 |

### 支线与奇遇示例

```yaml
# data/questgps/quests/side.yml
gps_side_silk_trade:
  enabled: false
  sort-order: 100
  allow-abandon: true
  required-mainline:
    - "gps_main_newcomer"
  presentation:
    rewards-source: chemdah

# data/questgps/quests/encounter.yml
gps_encounter_rare_find:
  enabled: false
  sort-order: 200
  allow-abandon: true
```

### discovery.mode

| 模式 | 行为 |
| --- | --- |
| `overlay`（默认） | 仅 `quests/*.yml` 中 `enabled: true` 的任务出现在菜单 |
| `auto` | 扫描全部 Chemdah 模板；未写 overlay 的模板自动生成最小定义 |

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
      quest-id: "gps_main_newcomer"
      open-menu: true
```

## 从旧版迁移

如果你从使用服务端粒子导航、路径式任务 ID 或旧版 flatRow UI 的版本升级，需要注意：

1. **overlay ID** — 根键改为 Chemdah 裸 ID（如 `gps_main_newcomer`），不再使用 `main/example_quest`
2. **分类** — 默认由 `category.id-prefix-rules` + `meta.type` 解析；overlay 的 `category:` 改为可选
3. **UI 发包** — `questRows` / `taskRows` / `rewardRows` 已改为 `quests` / `tasks` / `rewards` Map；分类 Tab 动态渲染
4. **配置新增** — `presentation.*`、`category.*`、`discovery.mode`、`navigation.mode`、`database.*`
5. **粒子导航** — `navigation.particle` 已替换为 `navigation.marker`
6. **指引 HUD** — 旧 `hud-ui-id` 等配置已移除，改为 `guide-ui-id`；追踪时自动开关
7. **命令** — `/questgps hud` 已移除
8. **依赖** — 3D 模型标记需要 Adyeshach；缺少时标记跳过，Waypoint 罗盘仍可用

升级后建议执行 `/axs questgps reload`，若需覆盖导出的 UI 文件可设 `overwrite-ui-files: true`。

