---
title: Regions 区域保护插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Regions 区域保护，类 WorldGuard 区域保护、40+ 标志、世界规则、UI 区域查看，我的世界服务器领地插件。
---

# Regions 区域保护

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 面板渲染 + 数据包通信 |
| Essentials 模块 | 可选 | 世界规则禁飞/禁交互的 bypass 权限联动 |


### 核心特性

| 分类 | 功能 |
|------|------|
| **区域管理** | 创建/删除/重定义/传送/列表/信息查看 |
| **选区工具** | 木斧左/右键设置两点选区 |
| **标志系统** | 40+ 区域保护标志，三态（allow/deny/none） |
| **成员管理** | 所有者/成员，按优先级和继承生效 |
| **子区域** | 父区域继承，支持多层嵌套 |
| **世界规则** | 按世界级别的禁飞/禁活塞/禁交互 |
| **UI 面板** | 玩家区域菜单 + 管理员面板（需 ArcartX 客户端） |
| **数据迁移** | 支持 SQLite ↔ MySQL 迁移（`/axs migrate`） |
| **数据清理** | 支持 `/axs purge` 清理玩家区域数据 |

---

## UI 面板

Regions 模块提供两个 ArcartX UI 面板，需要玩家安装 ArcartX 客户端 mod。

### 玩家区域菜单

命令：`/rg menu`

| 页面 | 功能 |
| --- | --- |
| 当前区域 | 显示玩家所在区域的名称/世界/优先级/成员数/标志列表 |
| 我的区域 | 玩家拥有或参与的区域列表，点击查看详情 |
| 区域详情 | 范围坐标/体积/父区域/所有者/成员/标志详情 |

### 管理员面板

命令：`/rg admin`  
权限：`axs.regions.admin`

| 页面 | 功能 |
| --- | --- |
| 区域列表 | 所有区域（ID/世界/优先级/体积）+ 编辑/删除按钮 |
| 区域编辑 | 成员管理（移除）+ 40+ 标志快速切换（允许/拒绝/清除三态） |

::: info 前置要求
UI 面板需要玩家安装 ArcartX 客户端 mod。未安装时命令会提示"UI 功能不可用"。
:::

---

## 命令

主入口：`/axs regions <子命令>`，或使用缩写 `/rg <子命令>`

> 两种写法完全等价，例如 `/rg define myregion` = `/axs regions define myregion`

### UI

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/axs regions help` | 查看可用子命令 | `axs.regions.admin` |
| `/rg menu` | 打开区域查看菜单 | 无 |
| `/rg admin` | 打开区域管理面板 | `axs.regions.admin` |

### 区域 CRUD

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg define <名称>` | 用当前选区创建区域 | `axs.regions.select` |
| `/rg remove <名称>` | 删除区域 | 所有者或 `axs.regions.admin` |
| `/rg redefine <名称>` | 用新选区重定义区域范围 | 所有者或 admin |
| `/rg list [世界]` | 列出区域 | `axs.regions.admin` |
| `/rg info <名称>` | 查看区域详细信息 | 所有人 |
| `/rg tp <名称>` | 传送到区域中心 | `axs.regions.admin` |

### 选区

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg pos1` | 将当前位置设为点1 | `axs.regions.select` |
| `/rg pos2` | 将当前位置设为点2 | `axs.regions.select` |
| 木斧左键 | 设置点1（自动识别选区工具） | `axs.regions.select` |
| 木斧右键 | 设置点2 | `axs.regions.select` |

### 标志管理

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg flag <区域> <标志> <allow\|deny> [数据]` | 设置区域标志 | 所有者或 admin |
| `/rg removeflag <区域> <标志>` | 移除区域标志 | 所有者或 admin |
| `/rg flags <区域>` | 查看区域所有标志 | 所有人 |

### 成员管理

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg addowner <区域> <玩家>` | 添加所有者 | 所有者或 admin |
| `/rg removeowner <区域> <玩家>` | 移除所有者 | admin |
| `/rg addmember <区域> <玩家>` | 添加成员 | 所有者或 admin |
| `/rg removemember <区域> <玩家>` | 移除成员 | 所有者或 admin |

### 其他

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg priority <区域> <数字>` | 设置区域优先级 | 所有者或 admin |
| `/rg parent <区域> <父区域\|none>` | 设置/清除父区域 | admin |

---

## 标志系统

共 50+ 个保护标志，按分类如下（代码来源 `RegionFlag` 枚举）：

### 战斗类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `pvp` | 玩家间伤害 | DENY |
| `mob-damage` | 怪物对玩家伤害 | ALLOW |
| `damage-animals` | 玩家伤害动物 | DENY |
| `invincibility` | 玩家无敌 | NONE |
| `fall-damage` | 摔落伤害 | ALLOW |

### 方块类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `block-break` | 方块破坏 | DENY |
| `block-place` | 方块放置 | DENY |
| `use` | 右键交互（门/按钮/拉杆等） | ALLOW |
| `chest-access` | 容器访问 | DENY |
| `trample` | 踩踏耕地 | DENY |
| `vehicle-destroy` | 破坏矿车/船 | DENY |
| `vehicle-place` | 放置矿车/船 | ALLOW |

### 环境类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `tnt` | TNT 爆炸 | DENY |
| `creeper-explosion` | 苦力怕爆炸 | DENY |
| `other-explosion` | 其他爆炸（末影水晶等） | DENY |
| `fire-spread` | 火焰蔓延 | DENY |
| `lava-fire` | 岩浆点火 | DENY |
| `lightning` | 雷击起火 | DENY |
| `snow-fall` | 雪覆盖 | ALLOW |
| `snow-melt` | 雪融化 | ALLOW |
| `ice-form` | 冰生成 | ALLOW |
| `ice-melt` | 冰融化 | DENY |
| `leaf-decay` | 树叶腐烂 | ALLOW |
| `grass-spread` | 草方块蔓延 | ALLOW |
| `mushroom-spread` | 蘑菇蔓延 | ALLOW |
| `vine-growth` | 藤蔓生长 | ALLOW |
| `crop-growth` | 作物生长 | ALLOW |
| `water-flow` | 水流动 | ALLOW |
| `lava-flow` | 岩浆流动 | DENY |
| `pistons` | 活塞移动 | ALLOW |
| `soil-dry` | 耕地干燥 | ALLOW |

### 生物类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `mob-spawning` | 怪物生成 | ALLOW |
| `animal-spawning` | 动物生成 | ALLOW |
| `enderman-grief` | 末影人搬方块 | DENY |
| `ghast-fireball` | 恶魂火球 | DENY |
| `wither-damage` | 凋灵破坏 | DENY |

### 玩家行为类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `entry` | 玩家进入 | ALLOW |
| `exit` | 玩家离开 | ALLOW |
| `enderpearl` | 末影珍珠传送 | ALLOW |
| `chorus-fruit` | 紫颂果传送 | ALLOW |
| `item-drop` | 丢弃物品 | ALLOW |
| `item-pickup` | 拾取物品 | ALLOW |
| `exp-drop` | 经验球掉落 | ALLOW |
| `hunger` | 饥饿消耗 | ALLOW |
| `heal` | 自然恢复 | ALLOW |
| `fly` | 允许飞行 | ALLOW |
| `ride` | 骑乘实体 | ALLOW |
| `sleep` | 使用床 | ALLOW |
| `respawn-anchors` | 重生锚 | ALLOW |

### 杂项类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `notify-enter` | 进入时通知区域成员 | NONE |
| `notify-exit` | 离开时通知区域成员 | NONE |
| `greeting` | 进入消息（自定义文本） | NONE |
| `farewell` | 离开消息（自定义文本） | NONE |
| `potion-splash` | 药水瓶投掷 | ALLOW |
| `send-chat` | 发送聊天 | ALLOW |
| `receive-chat` | 接收聊天 | ALLOW |

### 标志状态

每个标志有三种状态：
- **ALLOW** — 允许该行为
- **DENY** — 禁止该行为
- **NONE** — 不设置（继承父区域或全局默认）

---

## 配置结构

### ArcartXRegions.yml

```yaml
config-version: 1

debug: false

# 选区工具
selection:
  wand-item: WOODEN_AXE
  max-volume: 500000
  max-regions-per-player: 20

# 区域默认设置
defaults:
  priority: 0
  global-region-id: "__global__"

# 进入/离开提示
notifications:
  show-actionbar: true
  display-mode: actionbar    # actionbar / chat / title

# 世界规则（按世界级别的限制，不依赖区域选区）
world-rules:
  no-fly-worlds:
    - "world_pvp"
  no-fly-action: cancel
  no-fly-message: "&c此世界禁止飞行。"
  no-piston-worlds: []
  disabled-interactions:
    world_spawn:
      - ENDER_CHEST
      - ANVIL

# 存储
storage:
  dialect: sqlite            # sqlite / mysql
  sqlite-file: "regions.db"
  host: "127.0.0.1"
  port: 3306
  database: "arcartxsuite"
  username: "root"
  password: ""
  table-prefix: "axs_rg_"
  pool-size: 3
```

### 子目录文件

| 文件 | 说明 |
|------|------|
| `arcartx/ui/regions_menu.yml` | 玩家区域查看菜单 UI |
| `arcartx/ui/regions_admin.yml` | 管理员区域编辑面板 UI |
| `messages.yml` | 模块消息文件（前缀、帮助、错误提示等） |

---

## 世界规则

不依赖区域选区的全局世界级别限制：

```yaml
world-rules:
  # 禁止飞行的世界列表
  no-fly-worlds:
    - "world_pvp"
  no-fly-action: cancel
  no-fly-message: "&c此世界禁止飞行。"
  # 禁止活塞推动的世界列表
  no-piston-worlds: []
  # 禁止交互的物品/方块类型 (按世界配置)
  disabled-interactions:
    world_spawn:
      - ENDER_CHEST
      - ANVIL
```

| 功能 | 说明 | 绕过权限 |
| --- | --- | --- |
| 禁飞 | 指定世界禁止飞行，进入时自动取消 | `axs.essentials.fly.bypass` |
| 禁活塞 | 指定世界所有活塞推拉无效 | 无 |
| 禁交互 | 指定世界中特定方块类型不可交互 | `axs.essentials.interact.bypass` |

---

## 选区工具

默认选区工具为**木斧**（`WOODEN_AXE`），可在配置中修改。

- **左键方块** → 设置点 1
- **右键方块** → 设置点 2
- 两点设置完成后自动显示选区体积

```yaml
selection:
  wand-item: WOODEN_AXE
  max-volume: 500000
  max-regions-per-player: 20
```

---

## 存储

支持 SQLite（默认）和 MySQL。

```yaml
storage:
  dialect: sqlite
  sqlite-file: "regions.db"
  host: "127.0.0.1"
  port: 3306
  database: "arcartxsuite"
  username: "root"
  password: ""
  table-prefix: "axs_rg_"
  pool-size: 3
```

数据表：
- `regions` — 区域元数据（id/world/bounds/priority/parent）
- `region_flags` — 区域标志（region_id/flag/state/data）
- `region_members` — 成员关系（region_id/uuid/role）

---

## 优先级与继承

- **优先级**：数值越大越优先。玩家处于多个重叠区域时，优先级最高的区域的标志生效。
- **父区域继承**：当某标志状态为 `NONE` 时，向父区域递归查询直到找到明确设置。
- **成员豁免**：区域所有者和成员默认豁免方块破坏/放置/容器访问等保护标志。
- **全局区域**：`__global__` 作为所有世界的保底规则，无任何区域覆盖时生效。

---

## 权限汇总

| 权限 | 说明 | 默认 |
| --- | --- | --- |
| `axs.regions.select` | 使用选区工具 | true |
| `axs.regions.admin` | 管理所有区域 | op |
| `axs.regions.bypass` | 绕过所有区域保护 | op |
| `axs.regions.bypass.limit` | 绕过区域数量限制 | op |

---

## 跨模块联动

| 能力接口 | 方向 | 说明 |
|----------|------|------|
| `EventBusCapability` | 订阅 | 区域保护事件可通过 EventBus 发布给 EventPacket 等模块 |
| `DatabaseMigratable` | 提供 | 支持 `/axs migrate regions` 进行 SQLite ↔ MySQL 数据迁移 |
| `PlayerDataPurgeable` | 提供 | 支持 `/axs purge` 清理指定玩家的区域成员数据 |
| Essentials 模块 | 联动 | 世界规则的禁飞/禁交互 bypass 权限由 Essentials 模块提供 |

