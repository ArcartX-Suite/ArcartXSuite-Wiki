---
title: Lottery 抽奖系统插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Lottery 抽奖系统，CS 开箱横向滚动动画 + 原神祈愿卡池，纯色块 UI、保底机制、磨损度系统，我的世界服务器抽奖开箱插件。
---

# Lottery 抽奖系统

::: tip 付费模块
Lottery 为付费模块，需要有效授权码激活。
:::

**Lottery** 模块为服务器提供完整的抽奖系统，包含 **CS 开箱**（Case Opening）和 **原神祈愿**（Gacha）两种模式。所有 UI 界面均使用纯色块与渐变色渲染，无外部贴图依赖。

## 功能概览

| 模式 | 核心功能 |
|------|----------|
| **Case 开箱** | 横向滚动开箱动画、CS 式品质层级（Consumer → Special）、磨损度系统、暗金计数器 |
| **Gacha 祈愿** | 原神式卡池系统、5星/4星保底机制、软保底递增、跨池共享保底计数、命定值系统 |
| **通用** | 多物品来源（MythicMobs/NeigeItems/原版）、多货币/物品消耗、SQLite/MySQL 持久化、背包满时自动发邮件 |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信（开箱滚动动画 / 祈愿界面） |
| MySQL | 可选 | 多服共享抽奖记录与保底计数 |
| Mail | 可选 | 背包满时自动发放奖品为邮件 |
| MythicMobs / NeigeItems | 可选 | 自定义物品来源（奖池奖品） |

## 启用步骤

```yaml
modules:
  lottery:
    enabled: true
```

## 命令

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/lottery` | `arcartxsuite.lottery.use` | 打开默认抽奖界面（Gacha 模式） |
| `/lottery case <奖池ID>` | `arcartxsuite.lottery.use` | 打开指定 Case 开箱界面 |
| `/lottery info <奖池ID>` | `arcartxsuite.lottery.use` | 查看奖池详细信息与概率 |
| `/lottery history <奖池ID>` | `arcartxsuite.lottery.use` | 查看个人抽奖历史 |

别名：`/lot` 为 `/lottery` 的快捷形式。

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs lottery status` | `arcartxsuite.lottery.admin` | 查看所有奖池状态与统计数据 |
| `/axs lottery reload` | `arcartxsuite.lottery.admin` | 重载奖池配置 |
| `/axs lottery reset <玩家> <奖池ID>` | `arcartxsuite.lottery.admin` | 重置指定玩家在某奖池的保底计数 |
| `/axs lottery give-ticket <玩家> <奖池ID> <数量>` | `arcartxsuite.lottery.admin` | 向玩家发放 Case 开箱钥匙 |
| `/axs lottery simulate <奖池ID> <次数>` | `arcartxsuite.lottery.admin` | 概率模拟，验证奖池实际出率 |

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `arcartxsuite.lottery.use` | true | 使用抽奖功能 |
| `arcartxsuite.lottery.admin` | op | 管理员命令 |

## PAPI 占位符

前缀：`%axslottery_xxx%`

| 占位符 | 说明 |
|--------|------|
| `%axslottery_pools_total%` | 已加载奖池总数 |
| `%axslottery_pools_active%` | 已启用奖池数 |
| `%axslottery_history_<奖池ID>%` | 玩家在该奖池的总抽奖次数 |

## 配置文件

### 主配置 `ArcartXLottery.yml`

```yaml
config-version: 1

# 奖池文件目录（相对于模块数据目录）
pools-directory: "lottery/pools"

storage:
  mode: sqlite          # sqlite 或 mysql
  sqlite-file-name: "lottery.db"
  pool-size: 1
  # mysql-host: "localhost"
  # mysql-port: 3306
  # mysql-database: "axs_lottery"
  # mysql-username: "root"
  # mysql-password: ""
  # table-prefix: "lottery_"

# Gacha 跨池共享保底组
# 同一组内的所有 Gacha 池子共享保底计数
shared-pity-groups:
  character_events: "character_event_*"
  weapon_events: "weapon_event_*"
```

### 奖池配置

奖池文件存放于 `data/lottery/pools/*.yml`，每个文件定义一个独立奖池。

#### Gacha 祈愿奖池

```yaml
id: "character_event_1"
type: GACHA
enabled: true
display-name: "角色活动祈愿"

cost:
  mode: CURRENCY          # 消耗模式：CURRENCY 货币 / ITEM 物品
  currency: "gem"         # 货币类型（依赖 Vault / CurrencyBridgeAPI）
  single: 160             # 单抽消耗
  ten: 1600               # 十连消耗

gacha:
  pool-type: CHARACTER    # 池子类型（仅展示用）
  pity-5star: 90          # 5星保底抽数
  pity-4star: 10          # 4星保底抽数
  soft-pity-start: 73     # 软保底起始抽数
  soft-pity-increment: 0.06   # 软保底后每次递增概率
  base-5star-rate: 0.006      # 基础 5星概率
  base-4star-rate: 0.051      # 基础 4星概率
  up-rate: 0.5            # UP 物品在小保底内被歪时，下一次 5星必为 UP 的概率
  shared-pity-group: "character_events"   # 跨池共享组（与主配置中 shared-pity-groups 对应）

  # UP 5星物品（当期概率提升）
  up-5star-items:
    - id: "character_star"
      name: "星辰"
      plugin-type: MYTHIC
      plugin-id: "StarCharacter"
      delivery: DIRECT
      weight: 1

  # 标准 5星物品（小保底可能歪到的常驻）
  standard-5star-items:
    - id: "character_moon"
      name: "月影"
      plugin-type: MYTHIC
      plugin-id: "MoonCharacter"
      delivery: DIRECT
      weight: 1

  # UP 4星物品
  up-4star-items:
    - id: "weapon_sword"
      name: "星光剑"
      plugin-type: NEIGE
      plugin-id: "StarSword"
      delivery: DIRECT
      weight: 1

  # 标准 4星物品
  standard-4star-items:
    - id: "weapon_bow"
      name: "风之弓"
      plugin-type: NEIGE
      plugin-id: "WindBow"
      delivery: DIRECT
      weight: 1

  # 3星基础物品（填充概率用）
  star3-items:
    - id: "material_common"
      name: "普通材料"
      plugin-type: PLAIN
      plugin-id: "minecraft:stone"
      delivery: DIRECT
      weight: 1
```

#### Case 开箱奖池

```yaml
id: "default_weapon_case"
type: CASE
enabled: true
display-name: "常规武器箱"

cost:
  mode: ITEM              # 消耗模式：ITEM 物品 / CURRENCY 货币
  item-id: "CaseKey"      # 物品标识（通过 ItemMatcherAPI 解析）
  item-amount: 1          # 每次消耗数量

case:
  # 品质层级与基础权重（CS:GO 风格）
  rarity-settings:
    CONSUMER:
      color: "&f"
      base-weight: 15625
    INDUSTRIAL:
      color: "&b"
      base-weight: 3125
    MIL_SPEC:
      color: "&9"
      base-weight: 625
    RESTRICTED:
      color: "&5"
      base-weight: 125
    CLASSIFIED:
      color: "&d"
      base-weight: 25
    COVERT:
      color: "&c"
      base-weight: 5
    SPECIAL:
      color: "&6"
      base-weight: 2

  stattrak-chance: 0.1    # 暗金计数器概率

  items:
    - id: "skin_ak47_redline"
      name: "AK-47 | 红线"
      rarity: CLASSIFIED
      plugin-type: NEIGE
      plugin-id: "AK47_Redline"
      delivery: DIRECT          # DIRECT 直接放入背包 / MAIL 满包时发邮件
      stattrak-enabled: true    # 是否可出暗金版本
      wear-distribution:        # 磨损度分布
        FACTORY_NEW:
          min: 0.00
          max: 0.07
          weight: 0.05
        MINIMAL_WEAR:
          min: 0.07
          max: 0.15
          weight: 0.15
        FIELD_TESTED:
          min: 0.15
          max: 0.38
          weight: 0.50
        WELL_WORN:
          min: 0.38
          max: 0.45
          weight: 0.20
        BATTLE_SCARRED:
          min: 0.45
          max: 1.00
          weight: 0.10
```

#### 奖池字段详解

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | String | ✅ | — | 奖池唯一标识，用于命令和 PAPI |
| `type` | String | ✅ | — | `GACHA` 祈愿 或 `CASE` 开箱 |
| `enabled` | Boolean | ✅ | — | 是否启用 |
| `display-name` | String | ✅ | — | UI 中显示的名称 |
| `cost.mode` | String | ✅ | — | `CURRENCY` 消耗货币 / `ITEM` 消耗物品 |
| `cost.currency` | String | `CURRENCY` 时 | — | 货币类型（依赖 CurrencyBridgeAPI） |
| `cost.single` | Int | `GACHA+CURRENCY` | — | 单抽消耗数量 |
| `cost.ten` | Int | `GACHA+CURRENCY` | — | 十连消耗数量 |
| `cost.item-id` | String | `ITEM` 时 | — | 消耗物品标识 |
| `cost.item-amount` | Int | `ITEM` 时 | — | 每次消耗数量 |
| `plugin-type` | String | ✅ | — | 物品来源：`MYTHIC` / `NEIGE` / `PLAIN` |
| `plugin-id` | String | ✅ | — | 物品在对应插件中的 ID |
| `delivery` | String | ❌ | `DIRECT` | `DIRECT` 直接给 / `MAIL` 邮件发送 |

## UI 设计

Lottery 模块包含两个独立 UI，全部使用**纯色块与渐变色**渲染，不依赖任何外部贴图资源。

### 开箱 UI `lottery_case.yml`

采用 **CS 开箱** 风格的横向滚动动画：

- **全屏背景**：`~0,0,0,140` 半透明黑色遮罩
- **顶部/底部渐变装饰**：`~80,100,140,180` → `~80,100,140,0` 线性渐变
- **滚动动画**：`Scroll` 控件配合 `moveX` + `TwoLerp` 缓动函数，先快后慢
- **奖励预览**：`HGrid` 展示所有可能奖品，按品质色块排列
- **单抽/十连结果**：`~500,400` 径向渐变光环（5星金/4星紫/3星蓝）+ 品质色背景
- **操作按钮**：绿色渐变胶囊（`#50A0A0` → `#70C8C8`）用于开箱，灰色扁平用于关闭

### 祈愿 UI `lottery_gacha.yml`

采用 **原神祈愿** 风格的卡池界面：

- **全屏背景**：蓝 → 紫 → 粉径向渐变（`gradient type:1` 径向）
- **左侧卡池导航**：金色选中指示器（`~255,215,0`）+ 动态背景色
- **中央主展示**：卡池类型标签、UP 物品名、星级、保底说明、剩余时间
- **顶部资源栏**：纠缠之缘/原石/星辉，圆角半透明黑色底（`~0,0,0,180`）
- **祈愿按钮**：金色渐变大圆角（`~255,230,150` → `~255,180,50`）
- **单抽结果**：径向渐变品质光环 + 物品大图标 + 星级展示
- **十连结果**：2行5列 `HGrid` 网格，每个格子带 `round_rect` 品质色背景

## 品质颜色系统

两个 UI 共用同一套**品质颜色字典**，由 `var.rarityColors` 在 `action.load` 阶段初始化：

| 品质等级 | 颜色值 | 说明 |
|----------|--------|------|
| `COMMON` | `~192,195,198` | 普通（灰） |
| `UNCOMMON` | `~176,195,217` | 罕见（浅蓝） |
| `RARE` | `~94,152,217` | 稀有（蓝） |
| `EPIC` | `~136,71,255` | 史诗（紫） |
| `LEGENDARY` | `~235,75,75` | 传说（红） |
| `MYTHIC` | `~255,215,0` | 神话（金） |

UI 中通过物品 lore 中的 `"rarity"` 字段动态绑定颜色：

```yaml
# UI 中读取物品 lore 的 rarity 字段
action:
  create: |-
    var.r = self.parent['slot0'].getItemText('rarity')
    self.rarityColor = var.rarityColors[var.r] != null ? var.rarityColors[var.r] : var.rarityColors['']
```

> **注意**：Java 后端需要在生成奖品物品时，在 lore 中添加 `rarity: <等级>` 行，UI 才能正确识别并染色。

## 数据持久化

### 存储表

| 表名 | 用途 |
|------|------|
| `lottery_player_gacha_state` | 玩家 Gacha 保底计数、命定值 |
| `lottery_player_case_state` | 玩家 Case 开箱状态 |
| `lottery_history` | 抽奖历史记录 |

### 字段说明

**`lottery_player_gacha_state`**：
- `player_uuid` / `pool_id` — 联合主键
- `pity_5` / `pity_4` — 当前保底计数
- `fate_points` — 命定值（UP 池歪了时累计）

**`lottery_player_case_state`**：
- `player_uuid` / `pool_id` — 联合主键
- `total_opens` — 总开箱次数
- `last_open_time` — 上次开箱时间

## 客户端包协议

### Case 开箱

包 ID：`AXS_CASE`

| action | 参数 | 说明 |
|--------|------|------|
| `open` | — | 请求开箱 |
| `open_ten` | — | 请求十连开箱 |
| `close` | — | 关闭 UI |

**服务端 → 客户端 init 包字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `packetId` | String | `AXS_CASE` |
| `poolName` | String | 奖池显示名 |
| `poolItems` | Map | 所有可能奖品（展示用） |
| `itemJsons` | Map | 各槽位物品 JSON（由服务端生成） |

### Gacha 祈愿

包 ID：`AXS_LOTTERY_GACHA`

| action | 参数 | 说明 |
|--------|------|------|
| `pull` | `count` (1/10) | 请求祈愿 |
| `switch_pool` | `index` | 切换卡池 |
| `exchange` | — | 打开尘辉兑换 |
| `details` | — | 查看卡池详情 |
| `history` | — | 查看历史记录 |

**服务端 → 客户端 init 包字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `packetId` | String | `AXS_LOTTERY_GACHA` |
| `poolName` | String | 当前卡池名称 |
| `poolType` | String | 卡池类型标签（如"角色活动"） |
| `upItemName` | String | UP 物品名称 |
| `upItemStar` | Int | UP 物品星级 |
| `upItemDesc` | String | UP 物品描述 |
| `guaranteeDesc` | String | 保底说明文字 |
| `remainingTime` | String | 卡池剩余时间 |
| `fateCount` / `primogemCount` / `stardustCount` | Int | 玩家资源数量 |
| `pity4` / `pity5` | Int | 当前保底计数 |
| `poolList` | Map | 可用卡池列表（导航用） |
| `currentPoolIndex` | Int | 当前选中卡池索引 |

## 配置诊断

Lottery 模块声明了以下配置校验规则：

| 字段 | 类型 | 约束 |
|------|------|------|
| `config-version` | INT | 必填，当前版本为 `1` |
| `storage.mode` | STRING | 必填，枚举 `sqlite` / `mysql` |
| `storage.pool-size` | INT | 范围 1–100 |

动态节（用户可自由增删，不被结构同步覆盖）：
- `shared-pity-groups`
- 奖池文件（`pools/*.yml`）中的所有内容

## 架构

```
LotteryModule (AbstractAXSModule)
├── LotteryService (门面，协调引擎与分发)
│   ├── GachaEngine (祈愿概率计算)
│   ├── CaseOpeningEngine (开箱滚动 + 磨损度计算)
│   └── PrizeDistributor (奖品分发：背包 / 邮件)
├── JdbcLotteryRepository (SQLite / MySQL 持久化)
├── LotteryPlayerCommand (/lottery /lottery case)
├── LotteryAdminCommand (/axs lottery)
├── LotteryPlaceholderExpansion (PAPI)
└── GachaPoolConfig / CasePoolConfig (奖池加载)
```

