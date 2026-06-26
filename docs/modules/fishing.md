---
title: Fishing 钓鱼系统插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Fishing 钓鱼系统，星露谷风格钓鱼小游戏、多水域生态、季节/天气/时间分布、钓鱼图鉴收集，我的世界服务器钓鱼小游戏插件。
---

# Fishing 钓鱼系统

::: tip 付费模块

Fishing 为付费模块，需要有效授权码激活。
:::

**Fishing** 模块为服务器提供一套完整的 **星露谷风格钓鱼小游戏**，通过 ArcartX UI HUD 实时渲染钓鱼过程。玩家抛竿后进入钓鱼小游戏，操控绿条追逐游动的鱼，填满进度条即可捕获。支持多水域生态、鱼种季节/天气/时间分布、宝藏奖励、饵料加成和钓鱼图鉴收集。

## 功能概览

| 功能 | 说明 |
|------|------|
| **星露谷钓鱼小游戏** | 服务端物理引擎驱动，实时同步到客户端 HUD |
| **多水域系统** | 圆形/矩形指定地点 + 默认兜底，不同水域独立鱼池/难度/宝藏 |
| **鱼种生态** | 季节、天气、水域类型、时间段四维过滤 |
| **宝藏系统** | 不同水域拥有独立宝藏池，捕获时概率获得 |
| **饵料加成** | 鱼竿 lore 识别饵料，提升特定鱼种吸引率和宝藏概率 |
| **钓鱼图鉴** | `/fishing` 打开图鉴 UI，记录捕获数量和最大尺寸 |
| **等级系统** | 捕获获取经验，升级提升绿条高度 |
| **售卖系统** | `/fishing sell` 直接出售手中鱼，`/fishing sell all` 批量出售背包中所有鱼 |
| **跨模块联动** | 捕获成功自动发射 EventBus 事件与 EventPacket Signal，联动 BattlePass / Title / Mail / Market |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信 |
| PlaceholderAPI | 可选 | 钓鱼统计占位符输出 |

## 命令

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/fishing` | `ArcartX-Suite.fishing.use` | 打开钓鱼图鉴界面 |
| `/fishing sell` | `ArcartX-Suite.fishing.use` | 出售主手中的钓鱼产物 |
| `/fishing sell all` | `ArcartX-Suite.fishing.use` | 出售背包中所有钓鱼产物 |

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs fishing stats [玩家]` | `ArcartX-Suite.fishing.admin` | 查看钓鱼统计 |
| `/axs fishing givexp <玩家> <经验>` | `ArcartX-Suite.fishing.admin` | 给予钓鱼经验 |
| `/axs fishing reset <玩家>` | `ArcartX-Suite.fishing.admin` | 重置钓鱼数据 |

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `ArcartX-Suite.fishing.use` | true | 使用钓鱼功能 |
| `ArcartX-Suite.fishing.admin` | op | 管理员命令 |

## PlaceholderAPI 占位符

| 占位符 | 说明 |
|--------|------|
| `%axs_fishing_level%` | 钓鱼等级 |
| `%axs_fishing_xp%` | 当前经验值 |
| `%axs_fishing_total_caught%` | 总捕获数 |
| `%axs_fishing_perfect_catches%` | 完美捕获数 |
| `%axs_fishing_collection%` | 图鉴收集进度 |
| `%axs_fishing_favorite_fish%` | 捕获最多的鱼种 |

## 配置结构

### ArcartXFishing.yml

```yaml
config-version: 2

storage:
  mode: sqlite          # sqlite / mysql
  sqlite-file-name: fishing.db

fishing:
  replace-vanilla: true
  minigame-tick-interval: 1
  bar-gravity: 0.3
  bar-bounce-damping: 0.7
  bar-click-force: -0.6
  catch-duration-ticks: 600
  progress-drain-rate: 0.5
  progress-gain-rate: 1.0
  base-green-bar-height: 96
  height-per-level: 4
  perfect-bonus-multiplier: 1.5
  base-xp-per-level: 100

# ─── 水域定义 ────────────────────────────────────────
waters:
  specified:
    - name: "dragon_lake"
      display-name: "&b龙泉湖"
      type: circle              # circle / rectangle
      world: "world"
      center: [100, 64, 200]    # 圆心 [x, y, z]
      radius: 50                # 半径
      fish-pool: "lake"         # 引用 fish-pools
      treasure-pool: "common"   # 引用 treasure-pools
      difficulty-modifier: 1.2  # 难度倍率
      bait-multipliers:         # 饵料在此水域加成
        worm: 1.5
        corn: 2.0
      require-permission: ""    # 可选进入权限

    - name: "jade_river"
      display-name: "&a翡翠河"
      type: rectangle
      world: "world"
      min: [500, 60, 300]       # 矩形最小角 [x, y, z]
      max: [700, 80, 400]       # 矩形最大角 [x, y, z]
      fish-pool: "river"
      treasure-pool: "common"
      difficulty-modifier: 0.9
      bait-multipliers:
        worm: 1.0
        bread: 1.3

  default:
    name: "default"
    display-name: "&7普通水域"
    fish-pool: "default"
    treasure-pool: "default"
    difficulty-modifier: 1.0
    bait-multipliers:
      worm: 1.0
      bread: 1.0
      corn: 1.0

# ─── 鱼类池 ────────────────────────────────────────
# 引用 fishes/ 目录下的鱼种 ID
fish-pools:
  default: ["sea_bass", "salmon"]
  lake: ["sea_bass", "catfish", "legendary_carp"]
  river: ["salmon", "catfish"]
  ocean: ["tuna", "pufferfish", "sea_bass"]

# ─── 宝藏池 ────────────────────────────────────────
# 引用 treasures/ 目录下的宝藏 ID
treasure-pools:
  default: ["old_boot", "lost_ring"]
  common: ["old_boot", "lost_ring", "ancient_coin"]
  rare: ["ancient_coin", "lost_ring"]

# ─── 饵料设置 ──────────────────────────────────────
baits:
  enabled: true
  default-bait: "worm"
  lore-pattern: "&7饵料: {bait_name}"
  nbt-tag: "axs_bait"
```

### fishes/ 鱼种文件

每鱼种一个独立 YAML 文件，存放在 `fishes/` 目录下：

```yaml
id: "sea_bass"
display-name: "海鲈鱼"
rarity: common              # common / uncommon / rare / legendary
min-size: 10
max-size: 50
base-price: 80
base-xp: 15
seasons: [spring, summer, fall, winter]
weathers: [clear, rain]
water-types: [ocean, river]  # ocean / river / lake
time-ranges:
  - start: "06:00"
    end: "20:00"
item: "minecraft:cod"
difficulty: 30              # 1~100，影响鱼 AI 游动速度
behaviors:
  - type: smooth
    weight: 0.6
  - type: dart
    weight: 0.4

# 捕获后自动发放的货币奖励（可选）
currency-reward:
  currency-id: "coin"       # 货币 ID
  amount: 15                  # 基础数量（完美捕获会自动乘以 perfect-bonus-multiplier）
```

**行为类型**：`smooth`（平滑游动）、`dart`（快速冲刺）、`sinker`（下沉型）、`floater`（上浮型）

### treasures/ 宝藏文件

每宝藏一个独立 YAML 文件，存放在 `treasures/` 目录下：

```yaml
id: "old_boot"
display-name: "旧靴子"
item: "minecraft:leather_boots"
chance: 0.3        # 在宝藏池中的出现概率
min-amount: 1
max-amount: 1
```

### baits/ 饵料文件

每饵料一个独立 YAML 文件，存放在 `baits/` 目录下：

```yaml
id: "worm"
display-name: "蚯蚓"
item: "minecraft:string"
default: true               # 是否为默认饵料
fish-attract-modifiers:     # 针对特定鱼种的吸引倍率
  catfish: 2.0
  sea_bass: 1.5
treasure-chance-boost: 0.02  # 宝藏概率加成
max-durability-bonus: 0
```

## 使用饵料

在鱼竿的 lore 中添加对应饵料名称即可生效。例如：

```
&7饵料: 蚯蚓
```

系统会根据 `baits.lore-pattern` 配置解析 lore 内容。未检测到饵料时使用 `default-bait` 指定的默认饵料。

## 快速开始教程

以下是一个从零搭建完整钓鱼系统的步骤，按顺序操作即可让钓鱼功能正常运行。

### 第一步：安装与激活

1. 将 `ArcartXFishing-*.jar` 放入服务器的 `plugins/` 目录
2. 确保 `ArcartX` 本体已安装并正常启动
3. 首次启动后，在 `plugins/ArcartXFishing/` 目录下会生成默认配置

### 第二步：创建鱼种（fishes/）

在 `plugins/ArcartXFishing/fishes/` 目录下创建鱼种文件：

```yaml
# sea_bass.yml
id: "sea_bass"
display-name: "海鲈鱼"
rarity: common
min-size: 20
max-size: 60
base-price: 50
base-xp: 10
seasons: [spring, summer, fall]
weathers: [clear, rain]
water-types: [ocean, river]
time-ranges:
  - start: "06:00"
    end: "22:00"
item: "minecraft:cod"
difficulty: 25
behaviors:
  - type: smooth
    weight: 0.8
  - type: dart
    weight: 0.2

currency-reward:
  currency-id: "coin"
  amount: 8
```

```yaml
# legendary_carp.yml
id: "legendary_carp"
display-name: "传说锦鲤"
rarity: legendary
min-size: 80
max-size: 150
base-price: 5000
base-xp: 500
seasons: [spring]
weathers: [clear]
water-types: [lake]
time-ranges:
  - start: "05:00"
    end: "07:00"
item: "minecraft:tropical_fish"
difficulty: 85
behaviors:
  - type: dart
    weight: 0.5
  - type: sinker
    weight: 0.5
```

**稀有度说明**：`common`（普通）→ `uncommon`（稀有）→ `rare`（罕见）→ `legendary`（传说）。稀有度越高，基础被选中的权重越低，但可通过饵料倍率提升。

### 第三步：创建宝藏（treasures/）

在 `plugins/ArcartXFishing/treasures/` 目录下创建：

```yaml
# old_boot.yml
id: "old_boot"
display-name: "旧靴子"
item: "minecraft:leather_boots"
chance: 0.5
min-amount: 1
max-amount: 1
```

### 第四步：创建饵料（baits/）

在 `plugins/ArcartXFishing/baits/` 目录下创建：

```yaml
# worm.yml
id: "worm"
display-name: "蚯蚓"
item: "minecraft:string"
default: true
fish-attract-modifiers:
  catfish: 2.0
  sea_bass: 1.5
treasure-chance-boost: 0.05
max-durability-bonus: 0
```

### 第五步：配置水域与池（ArcartXFishing.yml）

编辑主配置，将鱼种和宝藏绑定到水域：

```yaml
waters:
  specified:
    - name: "dragon_lake"
      display-name: "&b龙泉湖"
      type: circle
      world: "world"
      center: [100, 64, 200]
      radius: 50
      fish-pool: "lake"
      treasure-pool: "common"
      difficulty-modifier: 1.2
      bait-multipliers:
        worm: 1.5

  default:
    name: "default"
    display-name: "&7普通水域"
    fish-pool: "default"
    treasure-pool: "default"
    difficulty-modifier: 1.0
    bait-multipliers:
      worm: 1.0

fish-pools:
  default: ["sea_bass"]
  lake: ["sea_bass", "legendary_carp"]

treasure-pools:
  default: ["old_boot"]
  common: ["old_boot"]
```

### 第六步：配置 Market 回收（与 Market 模块联动）

如果使用 Market 模块，在 `plugins/ArcartXMarket/recycle/` 目录下添加：

```yaml
fishing_recycle:
  display-name: "&a鱼市回收"
  entries:
    sea_bass:
      source: "fishing"
      item-id: "sea_bass"
      price: 50
      currency: "coin"
    legendary_carp:
      source: "fishing"
      item-id: "legendary_carp"
      price: 5000
      currency: "coin"
```

### 第七步：使用

1. 玩家手持钓鱼竿，抛竿到指定水域或任意水面
2. 鱼上钩后进入 HUD 小游戏，按住 **空格** 控制绿条
3. 捕获成功后鱼物品自动进入背包
4. 使用 `/fishing sell` 出售手中鱼，或 `/fishing sell all` 批量出售
5. 使用 `/fishing` 打开图鉴查看收集进度

---

## 水域匹配规则

1. 玩家抛竿时，按 `waters.specified` 列表顺序匹配
2. 圆形水域检测玩家是否在圆心 `radius` 范围内
3. 矩形水域检测玩家是否在 `min` ~ `max` 坐标范围内
4. 未匹配任何指定地点时回退到 `waters.default`
5. 匹配成功后使用该水域的 `fish-pool` 和 `treasure-pool`

## 小游戏玩法

1. 玩家使用钓鱼竿抛竿（触发 `PlayerFishEvent`）
2. 模块拦截事件，取消原版钓鱼，打开钓鱼 HUD
3. 玩家按住 **空格** 或 **鼠标左键** 控制绿条上升
4. 松开按键绿条受重力下落
5. 鱼在轨道内游动，绿条覆盖鱼时进度条上涨
6. 进度条满则捕获成功，超时或进度归零则失败
7. 全程鱼未离开绿条可获得 **完美捕获** 加成

## 售卖系统

钓鱼产物在生成时会通过 **PersistentDataContainer** 标记鱼种 ID、尺寸和完美捕获状态，物品 lore 也会自动显示售价信息。

- **`/fishing sell`** — 出售主手中的鱼，自动根据尺寸计算价格并发放货币
- **`/fishing sell all`** — 扫描背包中所有钓鱼产物，按鱼种尺寸统一出售并发放货币

### Market 回收联动

Market 模块的回收系统已兼容 Fishing 产物。在 `recycle/*.yml` 中配置 `source: "fishing"` 即可：

```yaml
sea_bass:
  source: "fishing"
  item-id: "sea_bass"        # 对应 fishes/*.yml 中的 id
  price: 80
  currency: "coin"
```

Market 回收界面会自动识别带 PDC 标记的鱼物品，按回收表价格结算。

## 跨模块联动

### EventBus 事件主题

Fishing 在捕获成功时自动向 EventBus 发布以下事件，其他模块可订阅：

| 事件主题 | 触发条件 | Payload 字段 |
|----------|----------|--------------|
| `ArcartX-Suite.fishing.success` | 每次捕获成功 | `fish_id`, `fish_rarity`, `fish_size`, `water_area`, `bait_id`, `is_perfect`, `player_level` |
| `ArcartX-Suite.fishing.perfect` | 完美捕获 | 同上 |
| `ArcartX-Suite.fishing.collection_unlock` | 首次捕获某鱼种 | 同上 + `total_collection_count` |
| `ArcartX-Suite.fishing.treasure` | 获得宝藏 | `water_area` |

### EventPacket Signal

EventPacket 模块可通过以下 Signal ID 配置规则：

| Signal ID | 变量 |
|-----------|------|
| `fishing_success` | `{fish_id}`, `{fish_name}`, `{fish_rarity}`, `{fish_size}`, `{water_area}`, `{is_perfect}` |
| `fishing_legendary_catch` | 同上 |
| `fishing_first_catch` | 同上 |
| `fishing_treasure` | `{water_area}` |

### Title / Mail 里程碑

Fishing 内置里程碑检测，达成条件时自动调用 Title 和 Mail 模块（需已安装并启用）：

| 里程碑 | 条件 | 奖励 |
|--------|------|------|
| 首次传说鱼 | 首次捕获 `rarity: legendary` 的鱼 | 称号 `legendary_hunter`（7天）+ 邮件 `legendary_first_catch` |
| 学徒渔夫 | 累计捕获 10 条 | 称号 `fisher_apprentice`（永久） |
| 大师渔夫 | 累计捕获 100 条 | 称号 `fisher_master`（永久） |
| 传说渔夫 | 累计捕获 500 条 | 称号 `fisher_legend`（永久） |
| 完美大师 | 完美捕获 50 次 | 称号 `perfect_master`（永久） |

## 进阶联动配置示例

### EventPacket 联动（发送奖励）

在 `plugins/ArcartXEventPacket/rules/` 下创建规则，当玩家钓起传说鱼时给予额外奖励：

```yaml
legendary_catch_reward:
  enabled: true
  trigger: "COMMAND_SIGNAL"
  signal-id: "fishing_legendary_catch"
  actions:
    - type: GIVE_CURRENCY
      currency: "coin"
      amount: 1000
    - type: SEND_MESSAGE
      message: "&6🎉 传说捕获！额外奖励 1000 coin！"
```

### BattlePass 任务联动

在 `plugins/ArcartXBattlePass/tasks.yml` 中配置钓鱼相关任务：

```yaml
weekly_fish_master:
  name: "周常：钓鱼大师"
  description: "本周捕获 20 条鱼"
  event-topic: "ArcartX-Suite.fishing.success"
  conditions:
    - key: "fish_rarity"
      operator: "ANY"
  increment: "+1"
  target: 20
  reward-xp: 200
```

```yaml
legendary_hunter:
  name: "传说猎人"
  description: "钓起一条传说鱼"
  event-topic: "ArcartX-Suite.fishing.success"
  conditions:
    - key: "fish_rarity"
      operator: "EQ"
      value: "LEGENDARY"
  increment: "+1"
  target: 1
  reward-xp: 500
```

### 称号与邮件预设配置

Fishing 里程碑调用的称号和邮件需要在对应模块中预先配置：

**Title 模块**：在 `plugins/ArcartXTitle/titles.yml` 中添加
```yaml
fisher_apprentice:
  display-name: "&7[学徒渔夫]"
  # ... 其他称号属性

fisher_master:
  display-name: "&a[大师渔夫]"

fisher_legend:
  display-name: "&6[传说渔夫]"

perfect_master:
  display-name: "&b[完美大师]"

legendary_hunter:
  display-name: "&c[传说猎人]"
```

**Mail 模块**：在 `plugins/ArcartXMail/presets.yml` 中添加
```yaml
legendary_first_catch:
  subject: "传说捕获嘉奖"
  content: "恭喜你首次捕获传说级鱼类！这是你的奖励。"
  attachments:
    - item: "minecraft:diamond"
      amount: 5
```

## 常见问题排查

| 问题 | 原因 | 解决方法 |
|------|------|----------|
| 抛竿后没有进入小游戏 | `replace-vanilla: false` 或 ArcartX UI 未正常加载 | 检查主配置 `replace-vanilla` 是否为 `true`，确认 ArcartX 客户端正常连接 |
| 鱼种不刷新 | 季节/天气/时间/水域类型不匹配 | 检查当前服务器季节、天气和时间段是否符合鱼种配置条件 |
| 饵料不生效 | 鱼竿 lore 格式与 `lore-pattern` 不匹配 | 确认 lore 严格匹配，例如 `&7饵料: 蚯蚓`（注意空格和颜色码） |
| Market 无法回收鱼 | recycle 表中未配置 `source: "fishing"` 条目 | 参考上方 Market 回收联动配置 |
| `/fishing sell` 提示"不是钓鱼产物" | 鱼物品缺少 PDC 标记 | 这些鱼是旧版本生成的，重新钓鱼即可获得带标记的新物品 |
| EventPacket 收不到 Signal | EventPacket 模块未安装或未启用 fishing 相关规则 | 确认模块已安装，并在规则中配置正确的 `signal-id` |

## 数据库表

| 表名 | 说明 |
|------|------|
| `axs_fishing_player` | 玩家等级、经验、总捕获数、完美捕获数 |
| `axs_fishing_collection` | 图鉴条目：鱼种 ID、捕获次数、最大尺寸、首次捕获时间 |

