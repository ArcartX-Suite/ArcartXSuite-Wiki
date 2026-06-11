---
title: Pickup 拾取提示插件 | ArcartXSuite Minecraft
description: ArcartXSuite Pickup 物品拾取 HUD 弹出提示，支持扫描器模式，双模式可切换，我的世界服务器拾取增强插件。
---

﻿# Pickup 拾取模块

## 功能定位

提供两种可切换的拾取体验模式：

- **通知模式（notification）**：物品拾取时在 ArcartX HUD 上弹出提示动画（原有功能）
- **扫描模式（scanner）**：禁用自动拾取，以面板形式展示附近掉落物，玩家通过按键逐个交互拾取，类似 RPG 风格的战利品系统

## 模式对比

| 特性 | 通知模式 | 扫描模式 |
| --- | --- | --- |
| 拾取行为 | 正常自动拾取 | **禁用自动拾取** |
| HUD 功能 | 显示拾取通知（物品名、数量、图标） | 实时展示附近掉落物列表 |
| 交互方式 | 无交互 | F 键拾取选中 / 鼠标点击指定条目 / 滚轮切换 |
| 过滤系统 | 无 | 材质黑/白名单 + 名称正则 + 最小数量 |
| 仓库联动 | 无 | 可选自动存入仓库 |
| 合并显示 | 不适用 | 同类物品合并（可关闭） |

### 核心特性

#### 通知模式

- **实时拾取提示**：玩家拾取地面掉落物时，自动在 HUD 上弹出物品名称、数量和图标
- **多条堆叠**：同时最多显示 N 条最近拾取记录（默认 4 条），超出后旧提示向上挤出
- **自动消失**：每条提示在 HUD 上存活指定时间后自动消失（默认 3 秒）
- **物品序列化**：支持原版物品、NeigeItems、MythicMobs 物品等，自动读取 ItemStack 信息

#### 扫描模式

- **掉落物扫描**：周期性扫描玩家周围指定半径内的掉落物实体（默认 5 格）
- **实时面板**：半透明暗色面板竖向展示附近掉落物，每项包含物品图标 + 名称 + 数量
- **选中高亮**：当前选中项以蓝色高亮，可通过滚轮循环切换
- **鼠标点击拾取**：直接点击面板上的物品条目拾取对应物品（无需先滚轮选中）
- **交互菜单**：按 F 键打开透明交互菜单（`loot_interact`），捕获鼠标光标用于精确点击操作，ESC 关闭
- **跨模块联动**：对话选择器面板悬停时自动禁用拾取点击，避免误触
- **过滤系统**：根据材质黑/白名单、物品名称正则和最小堆叠数量过滤显示
- **合并显示**：同名同类物品合并为一条，显示总数量
- **拾取延迟**：掉落物落地一定时间后才开始显示，防止玩家刚扔出的物品立即出现
- **仓库联动**：可选将拾取的物品直接存入 Warehouse 仓库模块

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | HUD 渲染、物品图标、客户端包和按键事件 | 模块无法工作 |
| 可选 | Warehouse 模块 | 扫描模式拾取后自动存入仓库 | 拾取物品直接进入背包 |
| 可选 | NeigeItems | 识别 NeigeItems 物品显示名/数据 | 按普通物品显示 |
| 可选 | MythicMobs / MythicBukkit | 识别 MythicItems / MythicMob 掉落物 | 按普通物品显示 |
| 可选 | MMOItems | 识别 MMOItems 物品信息 | 按普通物品显示 |

## 启用步骤

```yaml
modules:
  pickup:
    enabled: true
```

## 配置说明（`ArcartXPickup.yml`）

### 全局设置

```yaml
settings:
  # 调试日志
  debug: false
  # 工作模式：notification（通知模式） 或 scanner（扫描模式）
  mode: notification
```

### 通知模式配置

```yaml
notification:
  # 目标 HUD 的 UI ID
  ui-id: "AXS:pickup_hud"
  # 启动时自动注册 HUD
  register-ui-on-enable: true
  # 是否强制覆盖 UI 文件
  overwrite-ui-file: false
  # 同时最多显示的提示条数
  max-visible: 4
  # 每条提示存活时间（毫秒）
  entry-ttl-ms: 3000
```

### 扫描模式配置

```yaml
scanner:
  # 目标 HUD 的 UI ID
  ui-id: "AXS:loot_panel"
  # 交互菜单的 UI ID（按 F 键打开的透明菜单，捕获鼠标光标）
  interact-ui-id: "AXS:loot_interact"
  # 启动时自动注册 HUD
  register-ui-on-enable: true
  # 是否强制覆盖 UI 文件
  overwrite-ui-file: false
  # 扫描半径（格）
  scan-radius: 5.0
  # 扫描间隔（ticks，5 = 250ms）
  scan-interval-ticks: 5
  # 面板最多显示物品数
  max-display: 8
  # 禁用自动拾取（靠近掉落物不会自动吸入背包）
  disable-auto-pickup: true
  # 拾取后自动存入仓库（需 warehouse 模块启用）
  warehouse-auto-deposit: false
  # 掉落物落地后多久开始显示（ticks）
  pickup-delay-ticks: 40
  # 同类物品合并显示
  merge-same-items: true
  # 自定义拾取按键
  keybind:
    # ArcartX 客户端按键注册名
    name: AXS_INTERACT
    # 默认键位（GLFW 键名）
    default-key: F
```

### 过滤系统配置

过滤系统仅在扫描模式下生效，支持**五维过滤**：材质、名称、Lore、NBT、数量。

```yaml
filter:
  # 过滤模式：blacklist（黑名单） 或 whitelist（白名单）
  mode: blacklist
  # 材质黑名单（黑名单模式下这些材质不显示）
  blacklist:
    - DIRT
    - COBBLESTONE
    - GRAVEL
    - SAND
    - NETHERRACK
    - COBBLED_DEEPSLATE
  # 材质白名单（白名单模式下只显示这些材质）
  whitelist: []
  # 物品名称黑名单正则（去色后匹配）
  name-blacklist-regex: []
  # Lore 黑名单正则（物品 Lore 中任意一行去色后匹配到则不显示）
  lore-blacklist-regex: []
  # Lore 白名单正则（设置后，Lore 中必须至少一行匹配才显示；留空不启用）
  lore-whitelist-regex: []
  # NBT 键黑名单（物品 NBT 包含指定键则不显示，支持嵌套路径如 "custom.trash"）
  nbt-blacklist-keys: []
  # NBT 键白名单（设置后，NBT 中必须包含至少一个指定键才显示；留空不启用）
  nbt-whitelist-keys: []
  # 最小堆叠数量（低于此数不显示）
  min-amount: 1
```

#### Lore 过滤示例

```yaml
filter:
  # 隐藏所有 Lore 中包含"不可交易"或"已绑定"字样的物品
  lore-blacklist-regex:
    - "不可交易"
    - "已绑定"
  # 只显示 Lore 中包含"史诗"或"传说"品质标记的物品
  lore-whitelist-regex:
    - "(史诗|传说)"
```

#### NBT 键过滤示例

```yaml
filter:
  # 隐藏带有 MythicMobs 垃圾标记的物品
  nbt-blacklist-keys:
    - "MYTHIC_TYPE"
    - "custom.trash"
  # 只显示带有自定义附魔或品质标签的物品
  nbt-whitelist-keys:
    - "custom.quality"
    - "PublicBukkitValues"
```

::: tip 过滤优先级
五维过滤按以下顺序依次检查，任一步骤不通过则立即过滤掉：
1. **最小数量** → 2. **材质黑/白名单** → 3. **名称正则** → 4. **Lore 正则** → 5. **NBT 键**

白名单规则（Lore 白名单、NBT 白名单）仅在**配置非空时**生效，留空等于不启用该维度的白名单检查。
:::

## 按键绑定（扫描模式）

| 按键 | 场景 | 功能 |
| --- | --- | --- |
| **F**（默认，可改绑） | HUD 显示时 | 打开交互菜单（捕获鼠标光标） |
| **鼠标左键** | 点击 HUD 条目 | 拾取对应物品（`pick_N`） |
| **鼠标左键** | 点击 HUD 空白区域 | 拾取当前选中物品（`pick`） |
| **滚轮上** | HUD / 交互菜单 | 切换选中到上一项 |
| **滚轮下** | HUD / 交互菜单 | 切换选中到下一项 |
| **ESC** | 交互菜单已打开时 | 关闭交互菜单 |

::: tip 按键说明
扫描模式使用 ArcartX 客户端的自定义按键系统（`AXS_INTERACT`），默认绑定为 F 键。玩家可在客户端按键设置中修改绑定。UI 脚本通过 `Keyboard.getKeyBindingKeyName("AXS_INTERACT")` 动态解析实际键位。
:::

## UI 面板（扫描模式）

### loot_panel（常驻 HUD）

面板位于屏幕中心偏右，视觉设计：

- **物品列表**：竖向排列，每项 42px 高，可点击
- **选中高亮**：蓝色背景（`60,140,220,160`），未选中为深灰
- **物品图标**：左侧 32×32 图标（通过 `setItemIcon` 渲染真实物品）
- **物品名称**：图标右侧显示名称和数量（数量 > 1 时追加 `×N`）
- **自动隐藏**：附近无掉落物时面板自动隐藏（`visible: var.count > 0`）
- **输入穿透**：`through: false`，HUD 接管鼠标点击和滚轮；`loot_panel` Canvas 及其子控件设置 `through: true` 让点击事件冒泡到顶层 `adaptive`
- **条件点击**：通过 `client.pickup` 客户端变量控制点击是否生效，Conversation 选择器悬停时自动置为 `false`

### loot_interact（透明交互菜单）

按 F 键打开的全屏透明菜单，用于捕获鼠标光标：

- **透明背景**：`background: false`，`through: true`
- **ESC 关闭**：关闭时自动发送 `close_menu` 包
- **滚轮切换**：`wheel` → 发 `scroll_up` / `scroll_down` 包

## 与 Warehouse 模块联动

启用 `scanner.warehouse-auto-deposit: true` 后，扫描模式下拾取的物品将优先尝试存入玩家的个人仓库：

1. 玩家点击物品条目拾取
2. 系统检查 Warehouse 模块是否可用
3. 调用 `WarehouseService.depositStack()` 尝试存入
4. 仓库满或物品在黑名单中 → 回退到背包
5. 背包也满 → 提示空间不足

::: warning 前置条件
需要 Warehouse 模块同时启用且玩家拥有个人仓库，否则 `warehouse-auto-deposit` 配置无效，物品直接进入背包。
:::

## 命令

### 玩家命令

> 权限：`arcartxsuite.pickup.use`（默认全部玩家拥有）

| 命令 | 说明 |
| --- | --- |
| `/pickup` | 切换拾取功能开关（toggle） |
| `/pickup on` | 开启拾取功能 |
| `/pickup off` | 关闭拾取功能 |
| `/pickup status` | 查看当前拾取状态（模式、开关） |

::: tip 关闭后的效果
- **通知模式**：关闭后不再显示拾取通知 HUD
- **扫描模式**：关闭后不再扫描掉落物，同时恢复原版自动拾取行为
:::

### 管理命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs pickup status` | 查看拾取模块状态（当前模式、配置信息） |
| `/axs pickup reload` | 重载拾取配置和 HUD |

## 技术架构

```
PickupModule
├── mode: notification
│   ├── PickupService（监听 EntityPickupItemEvent → 发包到 HUD）
│   └── pickup_hud.yml（动态生成的通知 HUD）
└── mode: scanner
    ├── LootScannerService（周期扫描 + 阻止自动拾取 + 处理客户端包）
    ├── LootFilterEngine（过滤引擎：材质/名称/Lore/NBT/数量）
    ├── loot_panel.yml（掉落物面板 HUD，常驻）
    └── loot_interact.yml（透明交互菜单，按 F 打开）
```

### 客户端↔服务端数据流（扫描模式）

```
服务端 → 客户端:
  1. 打开常驻 HUD (openUi → loot_panel)
  2. 每 scanIntervalTicks 发送 update 包（物品列表 + 选中索引）
  3. 滚轮切换时发送 select 包（仅 selectedIndex）

客户端 → 服务端（loot_panel HUD）:
  1. F 键 → Screen.open('AXS:loot_interact') + Packet.send('pickup', 'open_menu')
  2. 滚轮 → 本地更新 var.selectedIndex + Packet.send('pickup', 'scroll_up/scroll_down')
  3. 点击条目 → Packet.send('pickup', 'pick_N')  （N = 0~7，指定索引拾取）
  4. 点击空白 → Packet.send('pickup', 'pick')     （拾取当前选中）

客户端 → 服务端（loot_interact 菜单）:
  1. 滚轮 → Packet.send('pickup', 'scroll_up/scroll_down')
  2. ESC → Packet.send('pickup', 'close_menu')
```

### Payload 字段说明

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | int | 当前可见物品总数 |
| `selectedIndex` | int | 当前选中项索引（0-based） |
| `item{N}Visible` | boolean | 第 N 项是否可见 |
| `item{N}Name` | string | 第 N 项显示名称（含颜色代码） |
| `item{N}Amount` | int | 第 N 项数量 |
| `item{N}Material` | string | 第 N 项材质名 |
| `item{N}ItemJson` | string | 第 N 项物品 JSON（用于 setItemIcon 渲染） |
| `item{N}Uuid` | string | 第 N 项掉落物实体 UUID |

> `{N}` 范围为 0 ~ `max-display - 1`（默认 0~7）

## FAQ

### 扫描模式下其他玩家还能拾取物品吗？

`disable-auto-pickup` 会取消开启了拾取功能的玩家的自动拾取事件。玩家可以通过 `/pickup off` 关闭扫描模式恢复原版自动拾取行为。

### 过滤系统和 Warehouse 黑名单的关系？

- **Pickup 过滤系统**：控制面板上**显示**哪些物品（不显示 = 无法通过面板拾取）
- **Warehouse 黑名单**：控制哪些物品**不能存入仓库**（仍可拾取到背包）

两者独立运作，互不影响。

### 如何从通知模式切换到扫描模式？

修改 `ArcartXPickup.yml` 中 `settings.mode` 为 `scanner`，然后执行 `/axs pickup reload`。
