# CombatEffect 战斗特效

## 功能定位

战斗视觉反馈一站式方案，包含两大能力：

- **击杀特效** — 监听玩家攻击 / 击杀活体实体，把上下文发包给 ArcartX UI 播放击杀特效或弹出击杀提示。不限 PVP，可监听任意活体
- **伤害飘字** — 把伤害 / 治疗事件桥接到 ArcartX 伤害显示，按来源拆分：原始伤害、玩家伤害、暴击、治疗等

### 核心特性

**击杀特效：**
- **双触发器**：`kill`（击杀时触发）和 `attack`（攻击时触发），可同时启用
- **多接收者**：支持 `killer`（攻击者）、`victim`（受害者）等接收者角色
- **丰富变量**：pack 支持 `{killer_name}`、`{victim_name}`、`{victim_display_name}`、`{victim_entity_type_name}`、`{victim_mythic_mob_id}`、`{victim_is_player}`、坐标、世界、UUID、主手物品等
- **黑名单过滤**：支持按 MythicMob ID 或 Bukkit EntityType 过滤，避免对特定实体触发特效
- **灵活发包格式**：pack 支持字符串、列表、字典三种模式，适配不同 UI 需求

**伤害飘字：**
- **智能来源检测**：自动选择最优伤害来源 — MythicLib → CraneAttribute → AttributePlus → Bukkit 原版
- **来源回退**：指定来源不可用时自动回退到下一个可用来源
- **分类显示**：原始伤害、玩家伤害、MythicLib 属性伤害、CraneAttribute 属性伤害分别使用不同配置 ID
- **治疗飘字**：原版治疗和 MythicMobs 技能治疗均可显示，支持精确模式（实际生效量）
- **最小阈值**：每种伤害/治疗类型可配置最小显示值，避免微量数字刷屏

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 播放击杀特效、伤害飘字和治疗飘字 | 模块无法向客户端展示战斗反馈 |
| 可选 | MythicLib / MMOItems | 读取 MythicLib 结算后的属性伤害和 MMOItems 伤害来源 | 自动回退到下一个来源或 Bukkit 原版伤害 |
| 可选 | CraneAttribute | 读取 CraneAttribute 属性伤害 | 自动回退到下一个来源 |
| 可选 | AttributePlus | 读取 AttributePlus 属性伤害 | 自动回退到 Bukkit 原版伤害 |
| 可选 | MythicMobs / MythicBukkit | MythicMob ID 黑名单、技能治疗识别 | 原版实体照常显示；MythicMob 专属过滤/治疗识别不可用 |

## 启用步骤

```yaml
modules:
  combateffect:
    enabled: true
```

## 配置

### 击杀特效主配置（`ArcartXCombatEffect.yml`）

```yaml
kill-effect:
  settings:
    debug: false
    blacklist:
      mythic-mob-ids: []       # MythicMob ID 黑名单，大小写不敏感，命中则不发包
      entity-types: []         # Bukkit EntityType 黑名单，如 ZOMBIE、ARMOR_STAND
    entity-combat:
      enabled: true
      include-players: true              # 是否处理玩家目标（PVP）
      include-non-player-living: true    # 是否处理非玩家活体（原版怪物/MythicMob）

  packets-directory: "packets"           # 包定义目录，相对模块数据目录
```

### 包定义字段详解

包定义文件位于 `data/combateffect/packets/*.yml`，同一文件可包含多条定义，根键即为包 ID：

```yaml
# data/combateffect/packets/default.yml
kill-effect:
  enabled: true
  trigger: kill                  # kill = 击杀时触发；attack = 攻击时触发
  ui-id: "击杀命中特效"           # 目标 UI ID
  packet-handler: "kill"         # 目标 packetHandler 名称
  recipients:                    # 接收者列表
    - killer                     # killer = 攻击者；target = 被攻击者（仅玩家可收）
  pack: ""                       # 发包内容，支持字符串/列表/字典，可使用内置变量

attack-effect:
  enabled: false
  trigger: attack
  ui-id: "击杀命中特效"
  packet-handler: "attack"
  recipients:
    - attacker
  pack: ""
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `enabled` | boolean | `false` 时跳过该包定义 |
| `trigger` | string | `kill`（实体被击杀）/ `attack`（实体被攻击） |
| `ui-id` | string | 接收该包的 ArcartX UI ID |
| `packet-handler` | string | 目标 UI 的 `packetHandler` 名称 |
| `recipients` | list | `killer` / `attacker` / `target`（仅玩家目标） |
| `pack` | string/list/map | 发包内容，支持内置变量（见下表） |

### pack 内置变量

| 变量 | 说明 |
| --- | --- |
| `{killer_name}` | 攻击者玩家名 |
| `{victim_name}` | 受害者名（可能为实体名） |
| `{victim_display_name}` | 受害者显示名 |
| `{victim_entity_type_name}` | 受害者 Bukkit EntityType 名称 |
| `{victim_mythic_mob_id}` | 受害者 MythicMob ID（非 MythicMob 时为空） |
| `{victim_is_player}` | 受害者是否为玩家（`true`/`false`） |
| `{receiver_role}` | 接收者角色（`killer` / `attacker` / `target`） |
| `{killer_main_hand}` | 攻击者主手物品名 |
| `{timestamp_local}` | 本地时间戳字符串 |
| `{victim_x}` / `{victim_y}` / `{victim_z}` | 受害者坐标 |
| `{victim_world}` | 受害者所在世界名 |
| `{victim_uuid}` | 受害者 UUID |

### 伤害飘字（`digis-display` 节）

```yaml
digis-display:
  damage-display:
    source:
      # auto: MythicLib/MMOItems → CraneAttribute → AttributePlus → Bukkit 原版
      # craneattribute: 仅使用 CraneAttribute 结算伤害
      # attributeplus: 仅使用 AttributePlus 伤害事件
      # bukkit: 仅使用 Bukkit 原版伤害
      mode: "auto"
      fallback: true      # 指定来源不可用时自动回退
      debug: false

    original:             # 原始伤害飘字
      enabled: true
      config-id: "damage"          # 对应 ArcartX digis 配置 ID
      min-amount: 1.0              # 低于此值不显示
      ap-compatible: true          # 是否允许 AttributePlus 作为来源

    player:               # 玩家对玩家伤害
      enabled: true
      config-id: "player-damage"
      min-amount: 1.0

    mythiclib:            # MythicLib 属性伤害（需 MMOItems/MythicLib）
      enabled: false
      config-id: "damage"
      player-config-id: "player-damage"
      min-amount: 1.0
      player-min-amount: 1.0

    craneattribute:       # CraneAttribute 属性伤害（需 CraneAttribute）
      enabled: false
      config-id: "damage"
      player-config-id: "player-damage"
      min-amount: 1.0
      player-min-amount: 1.0

  heal-display:
    original:             # 原版治疗
      enabled: true
      config-id: "heal"
      min-amount: 1.0

    mythic:               # MythicMobs 技能治疗
      enabled: true
      config-id: "heal"
      min-amount: 1.0
      exact-mode: true    # true = 实际生效量；false = 技能理论量
```

| 来源模式 | 优先级链 |
| --- | --- |
| `auto` | MythicLib/MMOItems → CraneAttribute → AttributePlus → Bukkit |
| `craneattribute` | CraneAttribute → Bukkit（fallback=true 时） |
| `attributeplus` | AttributePlus → Bukkit（fallback=true 时） |
| `bukkit` | 仅 Bukkit 原版，不回退 |

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs combateffect status` | 查看击杀特效和伤害飘字的启用状态、已加载包定义数 |
| `/axs combateffect reload` | 重载全部包定义和伤害飘字配置 |

## UI / Packet

| 功能 | 说明 |
| --- | --- |
| 击杀特效 | 包定义的 `ui-id` + `packet-handler` 决定目标，`pack` 内容随触发上下文动态填充 |
| 伤害飘字 | 通过 ArcartX `digis` 系统发送，`config-id` 对应 ArcartX 客户端的飘字样式配置 |

> `config-id` 的样式（颜色、字体、动画等）在 ArcartX 客户端侧的 `digis` 配置文件中定义，与服务端包定义解耦。
