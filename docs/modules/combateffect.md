# CombatEffect 战斗特效

## 功能定位

战斗视觉反馈一站式方案，包含四大能力：

- **击杀/命中特效** — 监听玩家攻击 / 击杀活体实体，把上下文发包给 ArcartX UI 播放击杀特效或弹出击杀提示
- **连击追踪 (Combo Tracker)** — 追踪玩家连续攻击计数，支持阈值触发 UI 包和实时服务器变量同步
- **死亡缓冲 (Death Buffer)** — 拦截致命伤害，延迟真正死亡，期间播放死亡动画和全屏 UI
- **伤害飘字** — 把伤害 / 治疗事件桥接到 ArcartX 伤害显示，按来源拆分：原始伤害、玩家伤害、暴击、治疗等

---

## 核心特性

### 击杀/命中特效

- **四种触发器**：`kill`（击杀）、`attack`（攻击）、`death`（死亡缓冲触发）、`combo`（连击阈值触发）
- **多接收者**：`attacker`（攻击者）、`target`（受害者，仅玩家可接收）
- **丰富变量**：pack 支持 `{killer_name}`、`{victim_name}`、`{combo_count}`、坐标、UUID、主手物品等
- **黑名单过滤**：按 MythicMob ID 或 Bukkit EntityType 过滤
- **冷却系统**：每个包定义可配置 `cooldown` 防止高频触发刷屏
- **灵活发包格式**：pack 支持字符串、列表、字典三种模式

### 连击追踪

- **双数据源**：Chronos 状态事件（推荐）或 Bukkit 攻击事件，支持 `auto` 自动选择
- **服务器变量同步**：实时推送 combo 计数到客户端，UI 可通过 `{server.combo_count}` 引用
- **目标锁定模式**：开启后切换攻击目标自动重置 combo（适合 Boss 战）
- **超时自动重置**：可配置超时时间，超时后 combo 归零
- **阈值触发**：支持最小/最大 combo 数条件，一次性或重复触发

### 死亡缓冲

- **拦截致命伤害**：取消原版死亡，进入可配置时长的缓冲期
- **视觉效果**：支持 ArcartX Shader、第三人称视角切换、预设相机
- **Chronos 集成**：可强制玩家进入死亡状态（如冰冻/倒地动画）
- **阻止自动复活**：缓冲期间阻止其他插件触发的自动复活
- **全屏 UI**：发送 `death` 包触发死亡缓冲界面
- **世界黑名单**：指定世界不启用缓冲

### 伤害飘字

- **智能来源检测**：MythicLib → CraneAttribute → AttributePlus → Bukkit 原版
- **来源回退**：指定来源不可用时自动回退
- **分类显示**：原始伤害、玩家伤害、属性伤害分别使用不同配置 ID
- **治疗飘字**：原版治疗和 MythicMobs 技能治疗，支持精确模式
- **最小阈值**：避免微量数字刷屏

---

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 播放特效、包发送、伤害飘字、服务器变量同步 | 模块无法向客户端展示任何战斗反馈 |
| 可选 | Chronos | 连击追踪的 Chronos 状态源、死亡缓冲强制状态 | 自动回退 Bukkit 事件源；死亡状态功能不可用 |
| 可选 | MythicLib / MMOItems | 读取 MythicLib 属性伤害 | 自动回退到下一个来源 |
| 可选 | CraneAttribute | 读取 CraneAttribute 属性伤害 | 自动回退 |
| 可选 | AttributePlus | 读取 AttributePlus 属性伤害 | 自动回退到 Bukkit 原版 |
| 可选 | MythicMobs / MythicBukkit | MythicMob ID 黑名单、MythicMob 名称解析、技能治疗识别 | 原版实体照常显示 |

---

## 启用步骤

```yaml
# plugins/ArcartXSuite/modules.yml
modules:
  combateffect:
    enabled: true
```

启用后模块会自动：
1. 导出默认配置到 `data/combateffect/config.yml`
2. 导出包定义到 `data/combateffect/packets/default.yml`
3. 导出 3 个 UI 文件到 `plugins/ArcartX/ui/` 目录

---

## 配置详解

配置文件位于 `data/combateffect/config.yml`（首次启动自动生成）。

### 击杀特效主配置

```yaml
kill-effect:
  settings:
    debug: false                         # 开启后输出详细发包日志
    blacklist:
      mythic-mob-ids: []                 # MythicMob ID 黑名单，大小写不敏感
      entity-types: []                   # Bukkit EntityType 黑名单，如 ARMOR_STAND
    entity-combat:
      enabled: true                      # 总开关
      include-players: true              # 是否处理玩家目标（PVP）
      include-non-player-living: true    # 是否处理非玩家活体

  packets-directory: "packets"           # 包定义目录，相对模块数据目录
```

### 连击追踪配置

```yaml
combo-tracker:
  enabled: false                  # 是否启用连击追踪
  source: "auto"                  # combo 来源: auto / chronos / bukkit
  timeout: 2000                   # 超时重置时间（毫秒）
  chronos-groups:                 # Chronos 状态组过滤
    - "攻击"
    - "连击"
  sync-variable: true             # 是否实时同步到客户端服务器变量
  variable-name: "combo_count"    # 服务器变量名
  per-target: false               # 目标锁定模式
  debug: false
```

**来源模式说明：**

| 模式 | 行为 |
| --- | --- |
| `auto` | Chronos 可用时优先 Chronos 状态事件，否则回退 Bukkit 攻击事件 |
| `chronos` | 仅使用 Chronos `PlayerEnterStateEvent`，Chronos 不可用则不追踪 |
| `bukkit` | 仅使用 Bukkit `EntityDamageByEntityEvent` |

**服务器变量同步**：开启 `sync-variable` 后，combo 计数实时推送到客户端。UI 中可直接用 `{server.combo_count}` 显示数字，无需等待包触发。超时重置时自动推送 `0`。

**目标锁定模式**：开启 `per-target` 后，切换攻击目标会重置 combo。适用于 Boss 战场景，确保只统计对同一目标的连续攻击。

### 死亡缓冲配置

```yaml
death-buffer:
  enabled: false                     # 是否启用死亡缓冲
  duration: 3000                     # 缓冲持续时间（毫秒）
  visuals:
    shader: ""                       # ArcartX Shader 名称（灰度/模糊）
    third-person-camera: true        # 是否切换第三人称视角
    camera-preset: ""                # ArcartX 预设相机 ID
    chronos:
      enabled: false                 # 是否强制进入 Chronos 状态
      state-id: "死亡"               # Chronos 状态 ID
  block-auto-respawn: true           # 阻止自动复活
  world-blacklist: []                # 不启用的世界列表
  debug: false
```

**工作流程：**
1. 玩家受到致命伤害 → 取消死亡事件
2. 应用视觉效果（Shader + 视角 + Chronos 状态）
3. 发送 `death` 触发器包到 UI（全屏死亡界面）
4. 缓冲期间：玩家无敌，阻止自动复活
5. 缓冲结束 → 恢复视觉 → 执行真正死亡

### 伤害飘字配置

```yaml
digis-display:
  damage-display:
    source:
      mode: "auto"                   # auto / craneattribute / attributeplus / bukkit
      fallback: true                 # 指定来源不可用时自动回退
      debug: false

    original:                        # 原始伤害
      enabled: true
      config-id: "damage"            # ArcartX digis 配置 ID
      min-amount: 1.0
      ap-compatible: true

    player:                          # 玩家对玩家伤害
      enabled: true
      config-id: "player-damage"
      min-amount: 1.0

    mythiclib:                       # MythicLib 属性伤害
      enabled: false
      config-id: "damage"
      player-config-id: "player-damage"
      min-amount: 1.0
      player-min-amount: 1.0

    craneattribute:                  # CraneAttribute 属性伤害
      enabled: false
      config-id: "damage"
      player-config-id: "player-damage"
      min-amount: 1.0
      player-min-amount: 1.0

  heal-display:
    original:                        # 原版治疗
      enabled: true
      config-id: "heal"
      min-amount: 1.0

    mythic:                          # MythicMobs 技能治疗
      enabled: true
      config-id: "heal"
      min-amount: 1.0
      exact-mode: true               # true = 实际生效量
```

| 来源模式 | 优先级链 |
| --- | --- |
| `auto` | MythicLib/MMOItems → CraneAttribute → AttributePlus → Bukkit |
| `craneattribute` | CraneAttribute → Bukkit（fallback=true 时） |
| `attributeplus` | AttributePlus → Bukkit（fallback=true 时） |
| `bukkit` | 仅 Bukkit 原版，不回退 |

---

## 包定义

包定义文件位于 `data/combateffect/packets/*.yml`，支持多文件，同一文件可包含多条定义。

### 完整字段一览

```yaml
example-packet:
  enabled: true                    # 是否启用
  trigger: kill                    # 触发器: kill / attack / death / combo
  ui-id: "击杀命中特效"             # 目标 ArcartX UI ID
  packet-handler: "kill"           # 目标 packetHandler 名称
  recipients:                      # 接收者列表
    - attacker                     # attacker / target
  cooldown: 500                    # 冷却时间（毫秒），0 = 无冷却
  conditions:                      # combo 触发条件（仅 trigger: combo 时有效）
    combo-min: 5                   # 最低连击数（含）
    combo-max: 2147483647          # 最高连击数（含）
    combo-repeat: false            # true = min~max 内每击触发; false = 仅 min 时触发一次
  pack:                            # 发包内容
    combo_count: "{combo_count}"
```

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | `false` 时跳过 |
| `trigger` | string | — | 触发器类型 |
| `ui-id` | string | — | 目标 ArcartX UI ID |
| `packet-handler` | string | — | UI 的 `packetHandler` 名称 |
| `recipients` | list | — | 接收者列表 |
| `cooldown` | long | `0` | 冷却时间(ms)，同一玩家在冷却内不会重复收到该包 |
| `conditions.combo-min` | int | `0` | 最低 combo 数 |
| `conditions.combo-max` | int | `MAX` | 最高 combo 数 |
| `conditions.combo-repeat` | boolean | `false` | 是否在范围内重复触发 |
| `pack` | string/list/map | `""` | 发包内容，支持变量替换 |

### 触发器类型

| 触发器 | 时机 | 说明 |
| --- | --- | --- |
| `kill` | 玩家击杀活体实体时 | 最常用，适合击杀反馈 |
| `attack` | 玩家攻击活体实体时 | 高频，建议配合 `cooldown` |
| `death` | 玩家进入死亡缓冲时 | 由 DeathBufferService 内部触发 |
| `combo` | 连击计数达到条件时 | 由 ComboTrackerService 内部触发 |

### pack 内置变量

| 变量 | 适用触发器 | 说明 |
| --- | --- | --- |
| `{killer_name}` | kill/attack/death | 攻击者玩家名 |
| `{victim_name}` | kill/attack/death | 受害者名 |
| `{victim_display_name}` | kill/attack | 受害者显示名 |
| `{victim_entity_type_name}` | kill/attack | Bukkit EntityType 名称 |
| `{victim_mythic_mob_id}` | kill/attack | MythicMob ID（非 MythicMob 为空） |
| `{victim_is_player}` | kill/attack | 是否为玩家 (`true`/`false`) |
| `{receiver_role}` | kill/attack | 接收者角色 |
| `{killer_main_hand}` | kill/attack | 攻击者主手物品名 |
| `{timestamp_local}` | 全部 | 本地时间戳 |
| `{victim_x}` / `{victim_y}` / `{victim_z}` | kill/attack | 受害者坐标 |
| `{victim_world}` | kill/attack | 受害者所在世界 |
| `{victim_uuid}` | kill/attack | 受害者 UUID |
| `{combo_count}` | combo | 当前连击计数 |
| `{player}` | combo | 触发连击的玩家名 |
| `{attacker}` | death/combo | 攻击者名 |
| `{target}` | death | 死亡玩家名 |
| `{damage}` | death | 致命伤害数值 |
| `{death_message}` | death | 死亡消息 |

### 冷却系统

每个包定义可配置 `cooldown`（毫秒）。冷却基于 **包ID + 玩家UUID** 粒度，同一玩家在冷却期间不会重复收到同一包。

典型场景：`attack` 触发器每帧高频触发，设置 `cooldown: 500` 限制每秒最多 2 次发包。

### 默认包定义示例

```yaml
# 击杀时向攻击者发包
kill-effect:
  enabled: true
  trigger: kill
  ui-id: "击杀命中特效"
  packet-handler: "kill"
  recipients:
    - killer
  pack: ""

# 攻击时向攻击者发包（带 500ms 冷却）
attack-effect:
  enabled: false
  trigger: attack
  ui-id: "击杀命中特效"
  packet-handler: "attack"
  recipients:
    - attacker
  cooldown: 500
  pack: ""

# 连击达到 5 时触发一次
combo-5:
  enabled: true
  trigger: combo
  ui-id: "连击特效"
  packet-handler: "combo"
  recipients:
    - attacker
  conditions:
    combo-min: 5
    combo-max: 2147483647
    combo-repeat: false
  pack:
    combo_count: "{combo_count}"

# 连击达到 10 时触发里程碑动画
combo-10:
  enabled: true
  trigger: combo
  ui-id: "连击特效"
  packet-handler: "combo_milestone"
  recipients:
    - attacker
  conditions:
    combo-min: 10
    combo-max: 2147483647
    combo-repeat: false
  pack:
    combo_count: "{combo_count}"

# 死亡缓冲 — 发给死亡玩家
death-buffer-victim:
  enabled: true
  trigger: death
  ui-id: "死亡缓冲界面"
  packet-handler: "death"
  recipients:
    - target
  pack:
    killer: "{attacker}"
    victim: "{target}"

# 死亡缓冲 — 发给击杀者（复用击杀特效 UI）
death-buffer-killer:
  enabled: true
  trigger: death
  ui-id: "击杀命中特效"
  packet-handler: "kill"
  recipients:
    - attacker
  pack:
    killer: "{attacker}"
    victim: "{target}"
```

---

## UI 文件

模块内置 3 个 ArcartX UI 文件，首次启动自动导出到 `plugins/ArcartX/ui/`。

### 击杀命中特效 (`击杀命中特效.yml`)

**类型**：HUD（常驻显示）

**packetHandler：**

| 名称 | 功能 |
| --- | --- |
| `attack` | 显示命中贴图 1 秒 |
| `kill` | 击杀计数 +1，显示击杀贴图 1.5 秒，5 秒无击杀自动清零 |

**控件结构：**
- `命中特效贴图` — 命中时短暂显示的十字准星特效
- `击杀特效贴图` — 击杀时短暂显示的击杀标记
- `击杀计数文本` — 显示当前连续击杀数，`var.击杀数 > 0` 时可见

**客户端资源：**
- `命中击杀特效/命中.png` — 命中特效贴图
- `命中击杀特效/击杀.png` — 击杀特效贴图

### 连击特效 (`连击特效.yml`)

**类型**：HUD（常驻显示）

**数据来源（二选一或同时使用）：**
1. `{server.combo_count}` 服务器变量 — 实时同步，推荐
2. `packetHandler` 包触发 — 达到阈值时推送

**packetHandler：**

| 名称 | 功能 |
| --- | --- |
| `combo` | 更新 combo 计数，3 秒无新 combo 后隐藏 |
| `combo_milestone` | 更新 combo 计数 + 显示里程碑特效 2 秒 |

**控件结构：**
- `连击背景` — 半透明圆角矩形背景
- `连击数字` — 大字号显示当前 combo 数
- `连击标签` — "COMBO" 标签文本
- `里程碑特效` — 里程碑动画贴图（仅 `combo_milestone` 时显示）

**客户端资源：**
- `命中击杀特效/连击里程碑.png` — 里程碑闪光特效

**UI 显隐逻辑：**
```
visible: "server.combo_count > 0 || var.combo_show"
```
当服务器变量大于 0 或包触发后 3 秒内，显示 combo 面板。

### 死亡缓冲界面 (`死亡缓冲界面.yml`)

**类型**：全屏界面（非 HUD）

**特性：**
- `escClose: false` — ESC 不可关闭
- `closeDied: true` — 真正死亡时自动关闭
- `level: 100` — 高渲染层级，覆盖其他 UI
- `through: false` — 阻挡输入

**packetHandler：**

| 名称 | 功能 |
| --- | --- |
| `death` | 接收 `killer`/`victim` 数据，启动缓冲倒计时 |

**tick 逻辑：**
- 实时计算倒计时秒数 (`var.countdown`)
- 计算渐变透明度 (`var.fade_alpha`)
- 缓冲时间到后标记 `var.buffer_active = false`

**控件结构：**
- `全屏遮罩` — 黑色半透明全屏覆盖
- `死亡标题` — "&c&l你死了"
- `击杀者信息` — "被 xxx 击杀"（有击杀者时显示）
- `环境死亡信息` — "你倒下了..."（无击杀者时显示）
- `倒计时文本` — 大字号数字倒计时
- `提示文本` — "即将复活..."

---

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs combateffect status` | 查看所有子系统启用状态、已加载包定义数 |
| `/axs combateffect reload` | 重载全部配置和包定义 |

---

## 快速上手教程

### 场景 1：基础击杀/命中特效

1. 启用模块，确认 `击杀命中特效.yml` 已导出到 `plugins/ArcartX/ui/`
2. 准备客户端贴图资源 `命中击杀特效/命中.png` 和 `击杀命中特效/击杀.png`
3. 编辑 `data/combateffect/packets/default.yml`：
   - `kill-effect.enabled: true`（已默认开启）
   - `attack-effect.enabled: true`（按需开启，建议保留 `cooldown: 500`）
4. 进入游戏攻击/击杀任意生物即可看到特效

### 场景 2：连击 HUD

1. 在 `config.yml` 中启用连击追踪：
   ```yaml
   combo-tracker:
     enabled: true
     sync-variable: true
   ```
2. 确认 `连击特效.yml` 已导出
3. 编辑包定义：
   - `combo-5` 和 `combo-10` 已默认启用
   - 可自行添加更多阈值包
4. 进入游戏连续攻击，右侧出现 combo 面板

**进阶：自定义阈值**

```yaml
combo-20:
  enabled: true
  trigger: combo
  ui-id: "连击特效"
  packet-handler: "combo_milestone"
  recipients:
    - attacker
  conditions:
    combo-min: 20
    combo-repeat: false
  pack:
    combo_count: "{combo_count}"
```

### 场景 3：死亡缓冲（慢动作死亡）

1. 启用死亡缓冲：
   ```yaml
   death-buffer:
     enabled: true
     duration: 3000
     visuals:
       shader: "death_grayscale"    # 需客户端有该 shader
       third-person-camera: true
     block-auto-respawn: true
   ```
2. 确认 `死亡缓冲界面.yml` 已导出
3. 进入游戏被击杀 → 全屏死亡界面 + 3 秒倒计时 + 视觉效果

**与 Chronos 联动：**

```yaml
death-buffer:
  enabled: true
  duration: 3000
  visuals:
    chronos:
      enabled: true
      state-id: "死亡"    # 需要在 Chronos 控制器中注册该状态
```

### 场景 4：伤害飘字

1. 在 `config.yml` 中配置 `digis-display` 节
2. 在 ArcartX 客户端的 `digis` 配置中创建对应的 `config-id` 样式（`damage`、`player-damage`、`heal`）
3. 进入游戏即可看到伤害/治疗数字飘出

---

## 性能优化说明

- **MythicMobs 反射缓存**：`resolveMythicMobId` 使用静态 Method 缓存 + 失败标记，避免重复反射
- **冷却系统**：`ConcurrentHashMap` 存储到期时间戳，O(1) 查询无锁竞争
- **Combo 超时**：Bukkit 延迟任务自动清理，无额外 tick 开销
- **按需初始化**：Chronos/MythicMobs 反射仅在首次调用时初始化

---

## UI / Packet 对应关系

| UI 文件 | UI ID | packetHandler | 触发器 |
| --- | --- | --- | --- |
| `击杀命中特效.yml` | 击杀命中特效 | `attack` / `kill` | attack / kill / death(killer) |
| `连击特效.yml` | 连击特效 | `combo` / `combo_milestone` | combo |
| `死亡缓冲界面.yml` | 死亡缓冲界面 | `death` | death(victim) |

> **提示**：`config-id`（伤害飘字样式）在 ArcartX 客户端侧的 `digis` 配置文件中定义，与服务端包定义解耦。

---

## 文件结构

```
data/combateffect/
├── config.yml                    # 主配置
└── packets/
    └── default.yml               # 默认包定义（可添加更多 .yml）

plugins/ArcartX/ui/
├── 击杀命中特效.yml               # HUD — 击杀/命中
├── 连击特效.yml                   # HUD — 连击
└── 死亡缓冲界面.yml               # 全屏 — 死亡缓冲

# 客户端资源包（需自行制作）
assets/
└── 命中击杀特效/
    ├── 命中.png
    ├── 击杀.png
    └── 连击里程碑.png
```
