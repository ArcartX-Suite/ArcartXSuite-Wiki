# QuestGPS 任务导航

## 功能定位

将 Chemdah 任务追踪展示为 ArcartX HUD 路径点导航。

### 核心特性

- **HUD 导航箭头**：在 ArcartX HUD 上显示指向任务目标的导航箭头和距离信息
- **Chemdah 联动**：自动读取 Chemdah 任务追踪数据，无需手动配置坐标
- **目标追踪**：玩家可选择追踪/取消追踪特定任务目标
- **HUD 开关**：玩家可随时开关导航 HUD 显示
- **EventPacket 联动**：可通过 EventPacket 的 `questgps.offer` / `questgps.accept` 动作自动推送任务导航

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 任务菜单、HUD 路径点、导航箭头和客户端交互 | 模块无法显示任务导航 UI |
| 必需 | Chemdah | 读取任务、目标、追踪状态和奖励预览 | 模块不会加载 |
| 可选 | Map 模块 | 将任务目标同步为地图临时目标 | QuestGPS HUD 仍可用，地图联动关闭 |
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
  menu-ui-id: "AXS:questgps_menu"     # 任务菜单 UI
  hud-ui-id: "AXS:questgps_hud"       # 导航 HUD
  register-ui-on-enable: true
  overwrite-ui-files: false
  auto-open-hud-on-track: true         # 追踪任务时自动打开 HUD
  hud-enabled-by-default: true         # 玩家首次进服时 HUD 是否默认开启

navigation:
  enabled: true
  waypoint-style-id: "default"         # 导航路径点样式
  quest-id-prefix: "AXS-questgps-"    # 路径点 ID 前缀
  remove-on-finish: true               # 任务完成后自动移除路径点

quests-directory: "quests"             # 任务定义目录，相对模块数据目录
```

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
"main/example_quest":
  enabled: true
  category: mainline                     # mainline / side / encounter
  display-name-override: "初入岛屿"      # 覆盖 Chemdah 任务名，留空则用 Chemdah 原名
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
| `/questgps cleartrack` | 清除当前追踪的任务目标，HUD 导航箭头消失 |
| `/questgps hud [on\|off\|toggle]` | 控制 HUD 导航显示，默认 `toggle` 切换 |

## UI / Packet

| 功能 | UI ID | 说明 |
| --- | --- | --- |
| 任务菜单 | `AXS:questgps_menu` | 服务端推送任务列表（分类、描述、状态、奖励预览、可接/可放弃）；客户端点击接取/放弃/追踪回包 |
| 导航 HUD | `AXS:questgps_hud` | 服务端按追踪状态推送目标坐标、距离、标题；玩家切换追踪时实时刷新 |

### QuestGPS Packet 主要字段

| 字段 | 说明 |
| --- | --- |
| `quests` | 任务列表（`id`、`category`、`name`、`status`、`description`、`rewards`、`allow-abandon`） |
| `tracking` | 当前追踪的任务 ID 及导航目标坐标 |
| `hud.distance` | 玩家到导航目标的距离（由服务端计算） |
| `hud.title` | 导航目标标题（`navigation.point.title`） |

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
