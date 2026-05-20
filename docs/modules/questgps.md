# QuestGPS 任务导航

## 功能定位

将 Chemdah 任务系统的追踪和导航功能，通过 ArcartX 客户端实现**零服务端开销**的沉浸式 RPG 导航体验。

### 核心特性

- **Waypoint 罗盘导航**：ArcartX 客户端内置罗盘/距离指示器，自动指向追踪目标（纯客户端渲染）
- **3D 模型标记**：在导航目标位置生成客户端 3D 模型标记（Adyeshach 发包实体 + ArcartX 模型），支持自定义模型和动画
- **任务指引 HUD**：RPG 风格左侧竖排浮窗，显示当前任务名、目标清单（最多 3 条）、完成进度和导航坐标
- **任务菜单**：分类浏览任务、查看描述/奖励预览、接取/放弃/追踪操作
- **Chemdah 联动**：自动读取 Chemdah 任务追踪数据，无需手动配置坐标
- **EventPacket 联动**：可通过 EventPacket 的 `questgps.offer` / `questgps.accept` / `questgps.track` 动作自动推送任务
- **主线门禁**：未完成必要主线时自动屏蔽支线/奇遇入口和部分命令

### 性能架构

| 组件 | 渲染位置 | 服务端开销 |
| --- | --- | --- |
| Waypoint 罗盘 | 客户端 | 追踪变更时 1 次发包 |
| 3D 模型标记 | 客户端（Adyeshach 包级别实体） | 追踪变更时 1 次发包 |
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

## 配置

### 主配置（`ArcartXQuestGPS.yml`）

```yaml
debug:
  enabled: false

client:
  packet-id: "AXS_QUESTGPS"
  menu-ui-id: "AXS:questgps_menu"       # 任务菜单 UI
  guide-ui-id: "AXS:questgps_guide"     # 任务指引 HUD
  register-ui-on-enable: true
  overwrite-ui-files: false

navigation:
  enabled: true
  waypoint-style-id: "default"           # ArcartX 路点样式 ID（在 ArcartX/waypoint/*.yml 中定义）
  quest-id-prefix: "AXS-questgps-"      # 路径点 ID 前缀
  remove-on-finish: true                 # 任务完成后自动移除路径点
  # 导航标记 — Adyeshach 私有实体 + ArcartX 模型
  marker:
    enabled: true
    model-id: "nav_beacon"               # ArcartX 模型 ID
    scale: 1.0                           # 模型缩放
    default-state: "idle"                # 模型默认状态
    animation: "rotate"                  # 循环动画名称（客户端播放）
    y-offset: 2.0                        # 相对导航目标的 Y 轴偏移

quests-directory: "quests"               # 任务定义目录
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

**自定义模型步骤**：

1. 在 ArcartX 的模型配置中定义你的导航标记模型（参考 ArcartX 模型文档）
2. 为模型配置动画状态（如 `idle`、`rotate`）
3. 将模型 ID 填入 `marker.model-id`
4. 重载模块：`/axs questgps reload`

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

### 任务定义字段详解

任务文件位于 `data/questgps/quests/*.yml`，建议按任务分类分文件。任务 ID 使用 Chemdah 的原始任务 ID（**必须与 Chemdah 配置一致**）：

```yaml
# data/questgps/quests/mainline.yml
"main/example_quest":                    # Chemdah 的任务 ID
  enabled: true                          # 是否启用本任务
  category: mainline                     # mainline / side / encounter
  display-name-override: "初入岛屿"       # 覆盖 Chemdah 任务名，留空则用 Chemdah 原名
  description:                           # 任务描述，完全由 QuestGPS 控制
    - "&7跟随引导完成第一条主线。"
    - "&8这里的描述不读取 Chemdah 配置。"
  sort-order: 10                         # 在菜单中的排序（升序）
  allow-abandon: false                   # 是否允许玩家放弃该任务
  required-mainline: []                  # 接取本任务前必须完成的主线任务 ID 列表

  hooks:                                 # 对应 EventPacket command-signal 触发器的信号名
    triggered:
      - "questgps_main_triggered"        # 任务被触发（出现）时发出的信号
    accepted:
      - "questgps_main_accepted"         # 任务被接取时
    completed:
      - "questgps_main_completed"        # 任务完成时
    track-changed:
      - "questgps_track_changed"         # 追踪状态改变时

  rewards:                               # 奖励预览列表（仅展示，实际发放由 Chemdah 执行）
    - type: neigeitems
      neige-item-id: "starter_sword"
      amount: 1
      display-name: "&6新手长剑"
      text: "&7NeigeItems 物品"
      fallback-material: "IRON_SWORD"    # 缺失插件时的备用材质
    - type: mythicmobs
      mythic-item-id: "starter_relic"
      amount: 1
      display-name: "&d遗物"
      fallback-material: "AMETHYST_SHARD"
    - type: title
      title-id: "newcomer"
      duration: "permanent"
      display-name: "&e称号: 初来乍到"
    - type: material                     # 原版 Bukkit 物品
      material: "GOLD_INGOT"
      amount: 16
      display-name: "&e金锭"
    - type: text                         # 纯文本预览（无物品图标）
      display-name: "&a经验"
      text: "&7由 Chemdah 执行，这里只做预览。"

  navigation:
    enabled: true
    point:
      world: "world"
      x: 0
      y: 80
      z: 0
      title: "初入岛屿"        # 导航路径点标题
      style-id: "default"
      map-label: "主线目标"    # 在地图上显示的标签

  tasks:                                 # 子任务列表（对应 Chemdah 任务目标）
    "talk_to_elder":
      display-text: "与长老对话"
      description:
        - "&7前往村口与长老完成对话。"
      sort-order: 10
      navigation:                        # 子任务独立导航目标（覆盖任务级 navigation）
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
| `category` | string | `mainline` / `side` / `encounter`，影响门禁和菜单分类 |
| `display-name-override` | string | 覆盖 Chemdah 任务名，留空则使用 Chemdah 原名 |
| `allow-abandon` | boolean | 是否允许玩家主动放弃（主线建议 `false`） |
| `required-mainline` | list | 接取前必须已完成的主线任务 ID 列表 |
| `hooks` | map | 各阶段触发的 EventPacket 信号名，对应 `command-signal` 触发器 |
| `rewards` | list | 奖励预览（仅展示），支持 `neigeitems` / `mythicmobs` / `mmoitems` / `title` / `material` / `text` |
| `navigation.point` | map | 任务级导航目标，`tasks.<id>.navigation` 会覆盖它 |
| `tasks` | map | 子任务（Chemdah 任务目标）的导航和显示配置 |

### 支线与奇遇示例

```yaml
# data/questgps/quests/side.yml
"side/example_side":
  enabled: false
  category: side
  display-name-override: "示例支线"
  description:
    - "&7把 enabled 改为 true 后显示。"
  sort-order: 100
  allow-abandon: true
  required-mainline:
    - "main/example_quest"           # 必须完成指定主线才可接取
  rewards:
    - type: mythicmobs
      mythic-item-id: "side_token"
      amount: 1
      fallback-material: "EMERALD"
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
  ├─② 3D 模型标记 — AdyeshachNpcBridge.spawnPrivateMarker()
  │   在目标坐标生成仅该玩家可见的包级别实体
  │   + ArcartXEntityManager.setModel() 附加自定义模型
  │   + playAnimation() 播放循环动画
  │
  ├─③ 任务指引 HUD — bridge.openUi() + sendPacket()
  │   左侧浮窗显示任务名、目标列表、进度条、坐标
  │
  └─④ 地图目标同步 — MapNavigable.upsertExternalTarget()
      （如果 Map 模块可用）

玩家点击"取消追踪"或 HUD 上的 ✕
  │
  ├─① removeWaypoint()
  ├─② removePrivateMarker()
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

### 3D 模型标记

在导航目标位置渲染一个自定义 3D 模型（如光柱、箭头、水晶等）。技术实现：

1. **Adyeshach 私有临时实体** — 生成一个不可见的 ArmorStand，仅对追踪玩家可见，通过数据包传输，不进入服务端实体 tick
2. **ArcartX 模型附加** — 通过 `ArcartXEntityManager.getOrCreateEntity()` + `setModel()` 为该实体附加自定义模型
3. **客户端动画** — 通过 `playAnimation()` 设置循环动画（如旋转、浮动），完全由客户端执行

**特点**：
- 追踪期间零服务端开销（无 tick 循环、无粒子发送）
- 支持任意 ArcartX 模型（blockbench 导出等）
- 支持自定义动画和状态
- 单人可见（不影响其他玩家）

### 任务指引 HUD

追踪任务时自动打开的左侧竖排浮窗（`AXS:questgps_guide`），内容包括：

- **任务名** — 当前追踪的任务名称
- **进度条** — 绿色点阵进度条 `■■■□□` 显示完成比例
- **目标列表** — 最多 3 条任务目标，`✓` 已完成 / `◈` 进行中
- **导航坐标** — 目标位置的世界名和 XYZ 坐标
- **关闭按钮** — 点击 ✕ 取消追踪

视觉风格：深色半透明背景 + 左侧金色强调竖条 + 首次打开滑入动画。

## UI / Packet

| 功能 | UI ID | 说明 |
| --- | --- | --- |
| 任务菜单 | `AXS:questgps_menu` | 分类任务列表、描述、奖励预览、接取/放弃/追踪 |
| 任务指引 HUD | `AXS:questgps_guide` | 追踪中显示的任务目标清单和导航坐标 |

### Menu Packet 主要字段

| 字段 | 说明 |
| --- | --- |
| `packetId` | 包标识（`AXS_QUESTGPS`） |
| `categoryId` / `categoryName` | 当前分类 ID 和显示名 |
| `pageId` / `pageName` | 当前页签（可用/进行中/已完成） |
| `questCount` | 当前页任务数量 |
| `quests[i].*` | 任务行：`id`、`name`、`status`、`description`、`rewards`、`canTrack`、`canAbandon` |
| `navigationReady` | 导航功能是否就绪 |

### Guide Packet 主要字段

| 字段 | 说明 |
| --- | --- |
| `active` | 是否有活跃追踪 |
| `questName` | 追踪的任务名称 |
| `completedCount` / `totalCount` | 已完成/总目标数 |
| `progressText` | 进度文本（如 `2/5`） |
| `tasks[i].id` | 目标 ID |
| `tasks[i].text` | 目标描述文本 |
| `tasks[i].completed` | 是否已完成 |
| `tasks[i].status` | 状态文本 |
| `taskCount` | 当前显示的目标数（≤3） |
| `hasNav` | 是否有导航坐标 |
| `navWorld` / `navX` / `navY` / `navZ` | 导航目标世界坐标 |

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
      quest-id: "main/tutorial"
      open-menu: true
```

## 从旧版迁移

如果你从使用服务端粒子导航或旧版 HUD 的版本升级，需要注意：

1. **配置变更** — `navigation.particle` 节已替换为 `navigation.marker`，旧粒子配置将被忽略
2. **HUD 变更** — 旧 `hud-ui-id` / `auto-open-hud-on-track` / `hud-enabled-by-default` 配置已移除，替换为 `guide-ui-id` 任务指引 HUD
3. **命令变更** — `/questgps hud` 子命令已移除；指引 HUD 随追踪自动开关
4. **依赖变更** — 3D 模型标记功能需要 Adyeshach 插件；如果缺少 Adyeshach，标记功能会静默跳过，Waypoint 罗盘仍正常工作
