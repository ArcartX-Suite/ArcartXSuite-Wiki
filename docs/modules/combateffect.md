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

- **八种触发器**：`kill`、`attack`、`death`、`combo`、`manual`、`keybind`（AX 按键）、`state`（AC 状态）、`controller`（AC 控制器切换）
- **多接收者**：`attacker`（攻击者/按键玩家）、`target`（受害者，仅玩家可接收）
- **丰富变量**：pack 支持 `{killer_name}`、`{victim_name}`、`{combo_count}`、`{player}`、`{key_name}`、`{state_id}`、`{controller_id}` 等
- **通配符匹配**：`key-name`、`state-id`、`controller-id` 支持 `*` 通配符（如 `attack_*`）
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
| 可选 | Chronos | 连击追踪的 Chronos 状态源、死亡缓冲强制状态、`state`/`controller` 触发器 | 自动回退 Bukkit 事件源；死亡状态功能不可用；state/controller 触发器不可用 |
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
3. 导出 3 个 UI 文件到 `plugins/ArcartXSuite/ui/` 目录（`combat_kill_effect.yml`、`combo_effect.yml`、`death_buffer.yml`）

---

## 配置详解

配置文件位于 `data/combateffect/config.yml`（首次启动自动生成）。

### 击杀特效主配置

```yaml
kill-effect:
  settings:
    # 是否开启调试日志（输出详细发包信息）
    debug: false
    blacklist:
      # MythicMob ID 黑名单，大小写不敏感。命中后不发送 CombatEffect 包。
      mythic-mob-ids: []
      # Bukkit EntityType 黑名单，例如 ZOMBIE、PLAYER、ARMOR_STAND。
      entity-types: []
    # 实体战斗监听设置
    entity-combat:
      # 是否启用 CombatEffect 的实体攻击 / 击杀监听（总开关）
      enabled: true
      # 是否处理玩家目标（PVP）
      include-players: true
      # 是否处理非玩家活体实体，例如原版怪物和 MythicMob
      include-non-player-living: true

  # 包定义目录，路径相对模块数据目录
  # 目录下每个 *.yml 文件可包含多个包定义，根键即为包 ID
  packets-directory: "packets"
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `debug` | boolean | 输出详细发包日志，排查问题时开启 |
| `blacklist.mythic-mob-ids` | list | MythicMob ID 黑名单，匹配的实体不触发任何包 |
| `blacklist.entity-types` | list | Bukkit EntityType 黑名单（如 `ARMOR_STAND`、`VILLAGER`） |
| `entity-combat.enabled` | boolean | 实体战斗监听总开关，关闭后不监听任何攻击/击杀事件 |
| `entity-combat.include-players` | boolean | 是否处理玩家目标（PVP 场景） |
| `entity-combat.include-non-player-living` | boolean | 是否处理非玩家活体（原版怪物、MythicMob） |
| `packets-directory` | string | 包定义文件所在目录，相对模块数据目录 |

### 连击追踪配置

```yaml
combo-tracker:
  # 是否启用连击追踪
  enabled: false
  # combo 来源模式:
  #   auto    — Chronos 可用时优先 Chronos 状态事件，否则回退 Bukkit 攻击事件
  #   chronos — 仅使用 Chronos PlayerEnterStateEvent
  #   bukkit  — 仅使用 Bukkit EntityDamageByEntityEvent
  source: "auto"
  # combo 超时重置时间（毫秒），超过该时间无攻击则重置计数
  timeout: 2000
  # Chronos 状态组过滤（仅这些组的状态进入才计为 combo 一击）
  chronos-groups:
    - "攻击"
    - "连击"
  # 实时同步 combo 计数到 ArcartX 客户端服务器变量
  # 客户端 UI 可通过 {server.combo_count} 直接引用当前连击数
  sync-variable: true
  # 服务器变量名称（客户端通过 {server.<此名称>} 引用）
  variable-name: "combo_count"
  # 目标锁定模式: true 时切换攻击目标会重置 combo
  # 适用于 Boss 战等场景，确保 combo 仅计入对同一目标的连续攻击
  per-target: false
  debug: false
```

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `false` | 是否启用连击追踪 |
| `source` | string | `auto` | combo 来源模式 |
| `timeout` | int | `2000` | 超时重置时间（毫秒） |
| `chronos-groups` | list | `["攻击", "连击"]` | Chronos 状态组过滤，仅这些组计为一击 |
| `sync-variable` | boolean | `true` | 是否实时同步到客户端服务器变量 |
| `variable-name` | string | `combo_count` | 服务器变量名称 |
| `per-target` | boolean | `false` | 目标锁定模式 |
| `debug` | boolean | `false` | 调试日志 |

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
  # 是否启用死亡缓冲
  enabled: false
  # 缓冲持续时间（毫秒），玩家在此期间不会真正死亡
  duration: 3000
  visuals:
    # ArcartX Shader 名称，用于死亡画面效果（灰度/模糊等）
    # 需要在客户端资源 shaders/ 目录下存在对应配置
    shader: ""
    # 是否切换到第三人称视角
    third-person-camera: true
    # ArcartX 预设相机 ID（如需电影级俭拍镜头）
    camera-preset: ""
    # Chronos 集成（需安装 Chronos 插件）
    chronos:
      # 是否强制玩家进入 Chronos 状态（如冰冻/倒地动画）
      enabled: false
      # 强制进入的 Chronos 状态 ID，需在控制器中注册该状态
      state-id: "死亡"
  # 缓冲期间阻止其他插件触发的自动复活
  block-auto-respawn: true
  # 不启用死亡缓冲的世界列表
  world-blacklist: []
  debug: false
```

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `false` | 是否启用死亡缓冲 |
| `duration` | int | `3000` | 缓冲持续时间（毫秒） |
| `visuals.shader` | string | `""` | ArcartX Shader 名称，空 = 不应用 |
| `visuals.third-person-camera` | boolean | `true` | 是否切换第三人称 |
| `visuals.camera-preset` | string | `""` | ArcartX 预设相机 ID，空 = 不使用 |
| `visuals.chronos.enabled` | boolean | `false` | 是否强制进入 Chronos 状态 |
| `visuals.chronos.state-id` | string | `"死亡"` | Chronos 状态 ID |
| `block-auto-respawn` | boolean | `true` | 阻止自动复活 |
| `world-blacklist` | list | `[]` | 不启用缓冲的世界名称列表 |
| `debug` | boolean | `false` | 调试日志 |

**工作流程：**
1. 玩家受到致命伤害 → 取消死亡事件
2. 应用视觉效果（Shader + 视角 + Chronos 状态）
3. 发送 `death` 触发器包到 UI（全屏死亡界面）
4. 缓冲期间：玩家无敌，阻止自动复活
5. 缓冲结束 → 恢复视觉 → 执行真正死亡

### 按键触发配置

```yaml
keybind-trigger:
  enabled: false                   # 是否启用 ArcartX 按键触发
```

启用后，`KeybindTriggerService` 会通过反射注册以下 5 个 ArcartX 事件监听：

| ArcartX 事件类 | 对应 `key-type` | 方向 |
| --- | --- | --- |
| `ClientKeyPressEvent` | `client` | press |
| `ClientKeyReleaseEvent` | `client` | release |
| `ClientSimpleKeyPressEvent` | `simple` | press |
| `ClientSimpleKeyReleaseEvent` | `simple` | release |
| `ClientKeyGroupPressEvent` | `group` | press |

**key-type 含义：**

| 类型 | 含义 | 典型场景 |
| --- | --- | --- |
| `client` | 玩家可在 ArcartX 设置界面自定义绑定的按键 | 技能快捷键、闪避键 |
| `simple` | 固定组合键（如 Shift+Q），不可由玩家自定义 | 系统操作 |
| `group` | 按键组事件，按组 ID 匹配（如 "combat_keys"） | 同类技能一组 |

> **注意：** ArcartX 未安装时 `start()` 会静默跳过并在日志打印提示。

**工作流程：**
1. 玩家在客户端按下/释放 ArcartX 注册按键
2. ArcartX 服务端触发对应事件
3. `KeybindTriggerService` 收到事件，提取 `player`、`keyName`、`isPress`、`keyType`
4. 遍历所有 `trigger: keybind` 的 `PacketDefinition`，依次检查：
   - `conditions.key-name` 是否与 `keyName` 匹配（支持通配符）
   - `conditions.key-type` 是否与事件类型匹配
   - `conditions.key-action` 是否与按下/释放方向匹配
5. 通过冷却检查后，用 `{player}`、`{key_name}` 渲染 `pack` 模板，发包到客户端

### 状态/控制器触发配置

```yaml
state-trigger:
  enabled: false                   # 是否启用 Chronos 状态/控制器触发
```

启用后，`StateTriggerService` 会通过反射注册以下 3 个 Chronos 事件监听：

| Chronos 事件类 | 匹配的触发器 | 方向 |
| --- | --- | --- |
| `PlayerEnterStateEvent` | `state` | enter |
| `PlayerLeaveStateEvent` | `state` | leave |
| `PlayerControllerChangeEvent` | `controller` | — |

> **注意：** Chronos 插件（`ArcartX_Chronos_Plugin` 或 `Chronos`）未安装时会静默跳过。

**state 触发工作流程：**
1. 玩家进入或离开 Chronos 状态（如释放技能、切换姿态）
2. Chronos 触发 `PlayerEnterStateEvent` / `PlayerLeaveStateEvent`
3. `StateTriggerService` 提取 `player`、`stateId`、`isEnter`，并通过 `ChronosAPI.getPlayerControllerId()` 获取当前控制器 ID
4. 遍历所有 `trigger: state` 的包定义，依次检查：
   - `conditions.state-id` 是否与 `stateId` 匹配（支持通配符）
   - `conditions.state-action` 是否与进入/离开方向匹配
   - `conditions.controller-id`（可选）是否与当前控制器匹配
5. 通过冷却检查后，用 `{player}`、`{state_id}`、`{controller_id}` 渲染 pack 模板

**controller 触发工作流程：**
1. 玩家的 Chronos 控制器发生切换（如从"战士"切换到"法师"）
2. Chronos 触发 `PlayerControllerChangeEvent`
3. `StateTriggerService` 提取 `player`、`controllerId`
4. 遍历所有 `trigger: controller` 的包定义，检查 `conditions.controller-id` 匹配
5. 通过冷却检查后渲染 pack 模板并发包

### 伤害飘字配置

```yaml
digis-display:
  # 伤害显示配置
  damage-display:
    # 伤害来源检测配置
    source:
      # 伤害来源模式:
      #   auto            — 按优先级自动选择: MythicLib → CraneAttribute → AttributePlus → Bukkit
      #   craneattribute  — 仅优先使用 CraneAttribute 属性结算后的伤害
      #   attributeplus   — 仅优先使用 AttributePlus 伤害事件
      #   bukkit          — 仅使用 Bukkit 原版伤害事件
      mode: "auto"
      fallback: true                 # 指定来源不可用时是否自动回退到下一个可用来源
      debug: false                   # 是否输出伤害来源选择与回退日志

    # 原始伤害（非玩家对玩家）
    original:
      enabled: true                  # 是否启用原始伤害飘字
      config-id: "damage"            # ArcartX 客户端 digis 配置 ID，决定飘字颜色/字体/动画等样式
      min-amount: 1.0                # 最小显示阈值，低于此值的伤害不显示飘字（避免微量数字刷屏）
      ap-compatible: true            # 当伤害来源为 AttributePlus 时，是否允许显示

    # 玩家对玩家伤害（PVP）
    player:
      enabled: true                  # 是否启用 PVP 伤害飘字
      config-id: "player-damage"     # ArcartX 客户端 digis 配置 ID（可与原始伤害使用不同样式）
      min-amount: 1.0                # 最小显示阈值

    # MythicLib / MMOItems 属性伤害（需安装 MythicLib 插件）
    mythiclib:
      enabled: false                 # 是否启用 MythicLib 属性伤害飘字
      config-id: "damage"            # 普通目标伤害的 ArcartX digis 配置 ID
      player-config-id: "player-damage" # 目标为玩家时使用的 digis 配置 ID（PVP 伤害样式）
      min-amount: 1.0                # 最小显示阈值
      player-min-amount: 1.0         # PVP 伤害最小显示阈值

    # CraneAttribute 属性伤害（需安装 CraneAttribute 插件）
    craneattribute:
      enabled: false                 # 是否启用 CraneAttribute 属性伤害飘字
      config-id: "damage"            # 普通目标伤害的 ArcartX digis 配置 ID
      player-config-id: "player-damage" # 目标为玩家时使用的 digis 配置 ID
      min-amount: 1.0                # 最小显示阈值
      player-min-amount: 1.0         # PVP 伤害最小显示阈值

  # 治疗显示配置
  heal-display:
    # 原版治疗事件（药水、生命恢复等）
    original:
      enabled: true                  # 是否启用原版治疗飘字
      config-id: "heal"             # ArcartX 客户端 digis 配置 ID（治疗样式）
      min-amount: 1.0                # 最小显示阈值

    # MythicMobs 技能治疗（需安装 MythicMobs / MythicBukkit 插件）
    mythic:
      enabled: true                  # 是否启用 MythicMobs 技能治疗飘字
      config-id: "heal"             # ArcartX 客户端 digis 配置 ID（治疗样式）
      min-amount: 1.0                # 最小显示阈值
      exact-mode: true               # true: 显示实际生效的治疗量；false: 显示技能理论治疗量
```

**字段速查表：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `mode` | string | 伤害来源选择模式，决定读取哪个插件的伤害数据 |
| `fallback` | boolean | 指定来源不可用时是否自动回退到下一个来源 |
| `enabled` | boolean | 是否启用该类别的飘字 |
| `config-id` | string | ArcartX 客户端 `digis` 配置中的样式 ID，控制飘字外观 |
| `player-config-id` | string | PVP 场景单独使用的 digis 样式 ID |
| `min-amount` | double | 最小显示阈值，低于此值不显示飘字 |
| `player-min-amount` | double | PVP 场景的最小显示阈值 |
| `ap-compatible` | boolean | 是否允许 AttributePlus 结算的伤害触发此飘字 |
| `exact-mode` | boolean | MythicMobs 治疗：true 显示实际生效量，false 显示技能原始值 |

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
  trigger: kill                    # 触发器: kill / attack / death / combo / manual / keybind / state / controller
  ui-id: "combat_kill_effect"      # 目标 ArcartX UI ID
  packet-handler: "kill"           # 目标 packetHandler 名称
  recipients:                      # 接收者列表
    - attacker                     # attacker / target
  cooldown: 500                    # 冷却时间（毫秒），0 = 无冷却
  conditions:                      # 触发条件
    combo-min: 5                   # [combo] 最低连击数（含）
    combo-max: 2147483647          # [combo] 最高连击数（含）
    combo-repeat: false            # [combo] true = min~max 内每击触发; false = 仅 min 时触发一次
    key-name: "skill_dodge"        # [keybind] 按键名，支持 * 通配符
    key-action: press              # [keybind] press / release / both
    key-type: client               # [keybind] client / simple / group
    state-id: "attack_*"           # [state] Chronos 状态 ID，支持 * 通配符
    state-action: enter            # [state] enter / leave / both
    controller-id: "warrior"       # [state/controller] Chronos 控制器 ID，支持 * 通配符
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
| `conditions.key-name` | string | — | [keybind] ArcartX 按键名，支持 `*` 通配符 |
| `conditions.key-action` | string | `press` | [keybind] `press` / `release` / `both` |
| `conditions.key-type` | string | `client` | [keybind] `client` / `simple` / `group` |
| `conditions.state-id` | string | — | [state] Chronos 状态 ID，支持 `*` 通配符 |
| `conditions.state-action` | string | `enter` | [state] `enter` / `leave` / `both` |
| `conditions.controller-id` | string | — | [state/controller] Chronos 控制器 ID，支持 `*` 通配符 |
| `pack` | string/list/map | `""` | 发包内容，支持变量替换 |

### 触发器类型

| 触发器 | 时机 | 说明 |
| --- | --- | --- |
| `kill` | 玩家击杀活体实体时 | 最常用，适合击杀反馈 |
| `attack` | 玩家攻击活体实体时 | 高频，建议配合 `cooldown` |
| `death` | 玩家进入死亡缓冲时 | 由 DeathBufferService 内部触发 |
| `combo` | 连击计数达到条件时 | 由 ComboTrackerService 内部触发 |
| `manual` | 仅命令或 API 触发 | 不会被任何事件自动触发，适合外部系统集成 |
| `keybind` | ArcartX 按键按下/释放时 | 需启用 `keybind-trigger.enabled`，通过 `conditions.key-name` 匹配 |
| `state` | Chronos 状态进入/离开时 | 需启用 `state-trigger.enabled`，通过 `conditions.state-id` 匹配 |
| `controller` | Chronos 控制器切换时 | 需启用 `state-trigger.enabled`，通过 `conditions.controller-id` 匹配 |

### keybind 触发条件

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `conditions.key-name` | string | — | 匹配的 ArcartX 按键名，支持 `*` 通配符 |
| `conditions.key-action` | string | `press` | `press` / `release` / `both` |
| `conditions.key-type` | string | `client` | `client`（玩家可自定义）/ `simple`（固定组合键）/ `group`（按键组） |

配置示例：

```yaml
dodge-effect:
  enabled: true
  trigger: keybind
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  cooldown: 800
  conditions:
    key-name: "skill_dodge"
    key-action: release
    key-type: client
  pack:
    skill: "闪避翻滚"
    player: "{player}"
```

### state 触发条件

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `conditions.state-id` | string | — | 匹配的 Chronos 状态 ID，支持 `*` 通配符（如 `attack_*`） |
| `conditions.state-action` | string | `enter` | `enter` / `leave` / `both` |
| `conditions.controller-id` | string | — | 可选，限定只在特定控制器下触发，支持 `*` 通配符 |

配置示例：

```yaml
fire-blast-effect:
  enabled: true
  trigger: state
  ui-id: "combat_kill_effect"
  packet-handler: "kill"
  cooldown: 1500
  conditions:
    state-id: "skill_fire"
    state-action: enter
    controller-id: "warrior"
  pack:
    skill_name: "烈焰爆发"
    player: "{player}"
```

### controller 触发条件

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `conditions.controller-id` | string | — | 匹配的 Chronos 控制器 ID，支持 `*` 通配符，留空则匹配所有控制器切换 |

配置示例：

```yaml
warrior-stance:
  enabled: true
  trigger: controller
  ui-id: "combat_kill_effect"
  packet-handler: "kill"
  conditions:
    controller-id: "warrior"
  pack:
    stance: "战士姿态"
    player: "{player}"
```

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
| `{player}` | keybind/state/controller | 触发的玩家名 |
| `{key_name}` | keybind | ArcartX 按键名 |
| `{state_id}` | state | Chronos 状态 ID |
| `{controller_id}` | state/controller | Chronos 控制器 ID |

### 通配符匹配规则

`conditions.key-name`、`conditions.state-id`、`conditions.controller-id` 均支持 `*` 通配符。

**匹配规则：**
- 不含 `*` 时为精确匹配（大小写不敏感）
- 含 `*` 时，`*` 匹配任意长度的任意字符序列（大小写不敏感）
- `.` 字符被视为字面量，不是正则通配符

**示例：**

| 模式 | 匹配 | 不匹配 |
| --- | --- | --- |
| `skill_dodge` | `skill_dodge`、`Skill_Dodge` | `skill_dodge_2` |
| `skill_*` | `skill_dodge`、`skill_fire`、`skill_` | `skills_dodge` |
| `attack_*_combo` | `attack_1_combo`、`attack_heavy_combo` | `attack_combo` |
| `*` | 任何字符串 | — |
| `warrior` | `warrior`、`Warrior` | `warrior_2` |

### 冷却系统

每个包定义可配置 `cooldown`（毫秒）。冷却基于 **包ID + 玩家UUID** 粒度，同一玩家在冷却期间不会重复收到同一包。

典型场景：`attack` 触发器每帧高频触发，设置 `cooldown: 500` 限制每秒最多 2 次发包。keybind 触发器也建议配合冷却使用，避免按键连发刷屏。

### 默认包定义示例

```yaml
# 击杀时向攻击者发包
kill-effect:
  enabled: true
  trigger: kill
  ui-id: "combat_kill_effect"
  packet-handler: "kill"
  recipients:
    - killer
  pack: ""

# 攻击时向攻击者发包（带 500ms 冷却）
attack-effect:
  enabled: false
  trigger: attack
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  recipients:
    - attacker
  cooldown: 500
  pack: ""

# 连击达到 5 时触发一次
combo-5:
  enabled: true
  trigger: combo
  ui-id: "combo_effect"
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
  ui-id: "combo_effect"
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
  ui-id: "death_buffer"
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
  ui-id: "combat_kill_effect"
  packet-handler: "kill"
  recipients:
    - attacker
  pack:
    killer: "{attacker}"
    victim: "{target}"
```

### keybind / state / controller 包定义示例

```yaml
# ─── 按键触发示例 ───────────────────────────

# 玩家释放闪避键时播放闪避特效
dodge-release:
  enabled: true
  trigger: keybind
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  cooldown: 800
  conditions:
    key-name: "skill_dodge"
    key-action: release
    key-type: client
  pack:
    skill: "闪避翻滚"
    player: "{player}"

# 任意 skill_ 开头的按键按下时触发（通配符）
skill-press-any:
  enabled: false
  trigger: keybind
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  cooldown: 300
  conditions:
    key-name: "skill_*"
    key-action: press
    key-type: client
  pack:
    key: "{key_name}"
    player: "{player}"

# ─── 状态触发示例 ───────────────────────────

# 玩家进入 "烈焰爆发" 状态时（限 warrior 控制器）
fire-blast-enter:
  enabled: true
  trigger: state
  ui-id: "combat_kill_effect"
  packet-handler: "kill"
  cooldown: 1500
  conditions:
    state-id: "skill_fire"
    state-action: enter
    controller-id: "warrior"
  pack:
    skill_name: "烈焰爆发"
    player: "{player}"
    state: "{state_id}"

# 所有 attack_ 开头的状态进入时触发（通配符，不限控制器）
attack-state-any:
  enabled: false
  trigger: state
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  cooldown: 500
  conditions:
    state-id: "attack_*"
    state-action: enter
  pack:
    state: "{state_id}"
    controller: "{controller_id}"
    player: "{player}"

# 玩家离开任意状态时（both = 进入+离开都触发）
state-leave-all:
  enabled: false
  trigger: state
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  conditions:
    state-id: "*"
    state-action: leave
  pack:
    event: "状态离开"
    state: "{state_id}"
    player: "{player}"

# ─── 控制器触发示例 ──────────────────────────

# 切换到 warrior 控制器时触发
warrior-stance:
  enabled: true
  trigger: controller
  ui-id: "combat_kill_effect"
  packet-handler: "kill"
  conditions:
    controller-id: "warrior"
  pack:
    stance: "战士姿态"
    controller: "{controller_id}"
    player: "{player}"

# 任意控制器切换都触发（controller-id 留空 = 匹配所有）
controller-change-any:
  enabled: false
  trigger: controller
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  cooldown: 1000
  pack:
    event: "控制器切换"
    controller: "{controller_id}"
    player: "{player}"
```

---

## UI 文件

模块内置 3 个 ArcartX UI 文件，首次启动自动导出到 `plugins/ArcartXSuite/ui/`。

### 击杀命中特效 (`combat_kill_effect.yml`)

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
- `combat_effect/hit.png` — 命中特效贴图
- `combat_effect/kill.png` — 击杀特效贴图

### 连击特效 (`combo_effect.yml`)

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
- `combat_effect/combo_milestone.png` — 里程碑闪光特效

**UI 显隐逻辑：**
```
visible: "server.combo_count > 0 || var.combo_show"
```
当服务器变量大于 0 或包触发后 3 秒内，显示 combo 面板。

### 死亡缓冲界面 (`death_buffer.yml`)

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
| `/axs combateffect help` | 查看可用子命令 |
| `/axs combateffect status` | 查看所有子系统启用状态（含按键触发、状态触发）、已加载包定义数 |
| `/axs combateffect reload` | 重载全部配置和包定义 |
| `/axs combateffect send <包ID> <玩家> [k=v ...]` | 按包定义 ID 手动触发特效（绕过事件匹配） |
| `/axs combateffect direct <uiId> <handler> <玩家> [k=v ...]` | 直接向客户端发包（绕过包定义系统） |

### status 命令输出示例

```
[AXS] CombatEffect v1.0.2-beta 运行中
[AXS] 包定义: 7/8
[AXS] 战斗显示: 已启用
[AXS] 死亡缓冲: 未启用
[AXS] 连击追踪: 已启用
[AXS] 按键触发: 已启用
[AXS] 状态触发: 已启用
```

- **包定义**：`已启用数/总数`，`enabled: false` 的包不计入已启用
- **按键触发**：显示 `已启用` 表示 `keybind-trigger.enabled: true` 且 ArcartX 事件注册成功
- **状态触发**：显示 `已启用` 表示 `state-trigger.enabled: true` 且 Chronos 事件注册成功

### send 命令详解

```
/axs combateffect send kill-effect Steve
/axs combateffect send combo-5 Steve combo_count=15
```

- **包ID**：`packets/*.yml` 中定义的根键名（如 `kill-effect`、`combo-5`）
- **k=v 参数**：以键值对形式传入额外变量，会覆盖包定义 `pack` 模板中的同名占位符
- 支持 Tab 补全：包 ID 和在线玩家名

### direct 命令详解

```
/axs combateffect direct combat_kill_effect kill Steve killer=Steve victim=Zombie
```

- **uiId**：ArcartX UI ID（即 UI 文件名去掉 `.yml`）
- **handler**：目标 `packetHandler` 名称
- 完全绕过包定义，直接构造发包内容

---

## 跨模块 API（Capability）

CombatEffect 注册了 `CombatEffectTriggerable` capability，其他模块可通过以下方式调用：

```java
// 在其他模块中获取 capability
Supplier<CombatEffectTriggerable> combatEffect =
    () -> context.getCapability(CombatEffectTriggerable.class).orElse(null);

// 按包 ID 触发
CombatEffectTriggerable ce = combatEffect.get();
if (ce != null) {
    ce.triggerPacket("kill-effect", player, Map.of("killer", player.getName()));
}

// 直接发包（绕过包定义）
ce.triggerDirect("combat_kill_effect", "kill", player, Map.of("killer", "Steve"));
```

**接口方法：**

| 方法 | 说明 |
| --- | --- |
| `triggerPacket(packetId, recipient, variables)` | 按已注册的包定义 ID 发送，变量合并到 pack 模板渲染 |
| `triggerDirect(uiId, handler, recipient, payload)` | 直接发包，完全自定义 UI ID、handler 和内容 |

### 与 EventPacket 联动示例

在 EventPacket 的规则中，可通过 `CombatEffectTriggerable` capability 触发战斗特效：

```yaml
# EventPacket 规则示例
boss-kill-special:
  event: mythicmobs_mob_death
  condition: "{mythic_mob_id} == 'WorldBoss'"
  actions:
    - type: capability
      capability: CombatEffectTriggerable
      method: triggerPacket
      args: ["kill-effect", "{player}", {"killer": "{player}", "victim": "WorldBoss"}]
```

### trigger: manual 包定义

新增 `manual` 触发类型，表示该包**仅**通过命令或 API 触发，不会被任何事件自动触发：

```yaml
special-effect:
  enabled: true
  trigger: manual
  ui-id: "combat_kill_effect"
  packet-handler: "kill"
  pack:
    killer: "{player}"
    victim: "特殊效果"
```

```
/axs combateffect send special-effect Steve
```

---

## 快速上手教程

### 场景 1：基础击杀/命中特效

1. 启用模块，确认 `combat_kill_effect.yml` 已导出到 `plugins/ArcartXSuite/ui/`
2. 准备客户端贴图资源 `combat_effect/hit.png` 和 `combat_effect/kill.png`
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
2. 确认 `combo_effect.yml` 已导出
3. 编辑包定义：
   - `combo-5` 和 `combo-10` 已默认启用
   - 可自行添加更多阈值包
4. 进入游戏连续攻击，右侧出现 combo 面板

**进阶：自定义阈值**

```yaml
combo-20:
  enabled: true
  trigger: combo
  ui-id: "combo_effect"
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
2. 确认 `death_buffer.yml` 已导出
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

### 场景 4：按键触发特效（ArcartX 联动）

1. 在 `config.yml` 中启用按键触发：
   ```yaml
   keybind-trigger:
     enabled: true
   ```
2. 确保 ArcartX 插件已安装且已在 ArcartX 客户端注册了自定义按键（如 `skill_dodge`）
3. 在 `data/combateffect/packets/` 目录下新建 `keybind.yml`（或添加到已有文件）：
   ```yaml
   dodge-release:
     enabled: true
     trigger: keybind
     ui-id: "combat_kill_effect"
     packet-handler: "attack"
     cooldown: 800
     conditions:
       key-name: "skill_dodge"
       key-action: release
       key-type: client
     pack:
       skill: "闪避翻滚"
       player: "{player}"
   ```
4. 重载配置 `/axs combateffect reload`
5. 进入游戏按下并释放闪避键 → 屏幕显示命中特效

### 场景 5：Chronos 状态触发特效

1. 在 `config.yml` 中启用状态触发：
   ```yaml
   state-trigger:
     enabled: true
   ```
2. 确保 Chronos 插件已安装，且已在控制器中注册了目标状态（如 `skill_fire`）
3. 新建包定义：
   ```yaml
   fire-blast:
     enabled: true
     trigger: state
     ui-id: "combat_kill_effect"
     packet-handler: "kill"
     cooldown: 1500
     conditions:
       state-id: "skill_fire"
       state-action: enter
       controller-id: "warrior"     # 可选，不填则不限控制器
     pack:
       skill_name: "烈焰爆发"
       player: "{player}"
       state: "{state_id}"
   ```
4. 重载配置 `/axs combateffect reload`
5. 进入游戏切换到 warrior 控制器并进入 `skill_fire` 状态 → 屏幕显示击杀特效

**进阶：使用通配符批量匹配**

```yaml
# 所有 skill_ 开头的状态进入都触发同一特效
skill-any:
  enabled: true
  trigger: state
  ui-id: "combat_kill_effect"
  packet-handler: "attack"
  cooldown: 500
  conditions:
    state-id: "skill_*"
    state-action: enter
  pack:
    state: "{state_id}"
    player: "{player}"
```

### 场景 6：伤害飘字

1. 在 `config.yml` 中配置 `digis-display` 节
2. 在 ArcartX 客户端的 `digis` 配置中创建对应的 `config-id` 样式（`damage`、`player-damage`、`heal`）
3. 进入游戏即可看到伤害/治疗数字飘出

---

## 性能优化说明

- **MythicMobs 反射缓存**：`resolveMythicMobId` 使用静态 Method 缓存 + 失败标记，避免重复反射
- **冷却系统**：所有服务（PacketService、KeybindTriggerService、StateTriggerService）均使用独立 `ConcurrentHashMap` 存储到期时间戳，O(1) 查询无锁竞争
- **Combo 超时**：Bukkit 延迟任务自动清理，无额外 tick 开销
- **按需初始化**：Chronos/MythicMobs/ArcartX 反射仅在模块启动时初始化一次；插件不存在时静默跳过，不抛异常
- **事件反射注册**：`KeybindTriggerService` 和 `StateTriggerService` 在 `start()` 时一次性反射获取事件类和 Method，后续事件调用无反射开销
- **ChronosAPI 查询缓存**：`StateTriggerService` 在初始化时缓存 `ChronosAPI.getInstanceAPI()` 和 `getPlayerControllerId` Method 引用，状态事件中查询控制器 ID 为纯反射调用（无类加载）

---

## UI / Packet 对应关系

| UI 文件 | UI ID | packetHandler | 触发器 |
| --- | --- | --- | --- |
| `combat_kill_effect.yml` | combat_kill_effect | `attack` / `kill` | attack / kill / death(killer) / keybind / state / controller |
| `combo_effect.yml` | combo_effect | `combo` / `combo_milestone` | combo |
| `death_buffer.yml` | death_buffer | `death` | death(victim) |

> **提示**：`config-id`（伤害飘字样式）在 ArcartX 客户端侧的 `digis` 配置文件中定义，与服务端包定义解耦。

---

## 文件结构

```
data/combateffect/
├── config.yml                    # 主配置
└── packets/
    └── default.yml               # 默认包定义（可添加更多 .yml）

plugins/ArcartXSuite/ui/
├── combat_kill_effect.yml      # HUD — 击杀/命中
├── combo_effect.yml            # HUD — 连击
└── death_buffer.yml            # 全屏 — 死亡缓冲

# 客户端资源包（需自行制作）
assets/
└── combat_effect/
    ├── hit.png
    ├── kill.png
    └── combo_milestone.png
```
