---
title: Regions 区域保护插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Regions 区域保护，类 WorldGuard 区域保护、40+ 标志、世界规则、UI 区域查看，我的世界服务器领地插件。
---

# Regions 区域保护

## 功能定位
Regions 模块提供类 WorldGuard 的完整区域保护方案，支持 40+ 保护标志、按世界规则限制、成员权限管理和子区域继承。


### 核心特效

| 分类 | 功能 |
| --- | --- |
| 区域管理 | 创建/删除/重定义/传送/列表/信息查看 |
| 选区工具 | 木斧左/右键设置两点选区 |
| 标志系统 | 40+ 区域保护标志，三态（allow/deny/none） |
| 成员管理 | 所有者/成员/权限组，按优先级和继承生效 |
| 子区域 | 父区域继承，支持多层嵌套 |
| 世界规则 | 按世界级别的禁飞/禁活塞/禁交互 |

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
权限：`ArcartX-Suite.regions.admin`

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
| `/rg menu` | 打开区域查看菜单 | 无 |
| `/rg admin` | 打开区域管理面板 | `ArcartX-Suite.regions.admin` |

### 区域 CRUD

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg define <名称>` | 用当前选区创建区域 | `ArcartX-Suite.regions.select` |
| `/rg remove <名称>` | 删除区域 | 所有者或 `ArcartX-Suite.regions.admin` |
| `/rg redefine <名称>` | 用新选区重定义区域范围 | 所有者或 admin |
| `/rg list [世界]` | 列出区域 | `ArcartX-Suite.regions.admin` |
| `/rg info <名称>` | 查看区域详细信息 | 所有人 |
| `/rg tp <名称>` | 传送到区域中心 | `ArcartX-Suite.regions.admin` |

### 选区

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg pos1` | 将当前位置设为点1 | `ArcartX-Suite.regions.select` |
| `/rg pos2` | 将当前位置设为点2 | `ArcartX-Suite.regions.select` |
| 木斧左键 | 设置点1（自动识别选区工具） | `ArcartX-Suite.regions.select` |
| 木斧右键 | 设置点2 | `ArcartX-Suite.regions.select` |

### 标志管理

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg flag <区域> <标志> <allow\|deny> [数据]` | 设置区域标志 | 所有者或 admin |
| `/rg removeflag <区域> <标志>` | 移除区域标志 | 所有者或 admin |
| `/rg flags <区域>` | 查看区域所有标志 | 所有人 |

### 成员管理

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg addowner <区域> <玩家\|g:组名>` | 添加所有者 | 所有者或 admin |
| `/rg removeowner <区域> <玩家\|g:组名>` | 移除所有者 | admin |
| `/rg addmember <区域> <玩家\|g:组名>` | 添加成员 | 所有者或 admin |
| `/rg removemember <区域> <玩家\|g:组名>` | 移除成员 | 所有者或 admin |

### 其他

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/rg priority <区域> <数字>` | 设置区域优先级 | 所有者或 admin |
| `/rg parent <区域> <父区域\|none>` | 设置/清除父区域 | admin |

---

## 标志系统

共 40+ 个保护标志，按分类如下：

### 方块类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `block-break` | 方块破坏 | DENY |
| `block-place` | 方块放置 | DENY |
| `use` | 交互使用（门/按钮/拉杆） | ALLOW |
| `chest-access` | 容器访问 | DENY |
| `trample` | 踩踏耕地 | DENY |

### 实体类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `pvp` | 玩家间 PVP | DENY |
| `mob-damage` | 怪物对玩家的伤害 | ALLOW |
| `mob-spawning` | 怪物自然生成 | ALLOW |
| `animal-damage` | 对动物的伤害 | DENY |
| `entity-interact` | 实体交互 | ALLOW |

### 环境类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `creeper-explosion` | 苦力怕爆炸 | DENY |
| `tnt` | TNT 爆炸 | DENY |
| `fire-spread` | 火焰蔓延 | DENY |
| `lava-flow` | 岩浆流动 | DENY |
| `water-flow` | 水流动 | ALLOW |
| `ice-melt` | 冰融化 | DENY |
| `snow-fall` | 雪覆盖 | ALLOW |
| `leaf-decay` | 树叶衰减 | ALLOW |
| `lightning` | 闪电 | DENY |

### 玩家行为类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `entry` | 进入区域 | ALLOW |
| `exit` | 离开区域 | ALLOW |
| `fly` | 飞行 | ALLOW |
| `ender-pearl` | 末影珍珠传送 | ALLOW |
| `chorus-fruit` | 紫颂果传送 | ALLOW |
| `item-drop` | 丢弃物品 | ALLOW |
| `item-pickup` | 拾取物品 | ALLOW |
| `exp-drop` | 经验球掉落 | ALLOW |
| `hunger` | 饥饿消耗 | ALLOW |
| `potion-splash` | 投掷药水 | ALLOW |
| `vehicle-place` | 放置载具 | ALLOW |
| `vehicle-destroy` | 破坏载具 | DENY |
| `sleep` | 睡觉 | ALLOW |

### 特殊类

| 标志 | 说明 | 默认 |
| --- | --- | --- |
| `pistons` | 活塞推拉 | ALLOW |
| `invincible` | 区域内无敌 | NONE |
| `greeting` | 进入区域提示文字 | NONE |
| `farewell` | 离开区域提示文字 | NONE |
| `deny-message` | 自定义拒绝提示 | NONE |
| `enderman-grief` | 末影人搬方块 | DENY |
| `ghast-fireball` | 恶魂火球 | DENY |
| `wither-damage` | 凋灵破坏 | DENY |
| `painting-destroy` | 破坏画 | DENY |
| `item-frame-destroy` | 破坏展示框 | DENY |
| `snow-golem-trail` | 雪傀儡铺雪 | ALLOW |

### 标志状态

每个标志有三种状态：
- **ALLOW** — 允许该行为
- **DENY** — 禁止该行为
- **NONE** — 不设置（继承父区域或全局默认）

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
| 禁飞 | 指定世界禁止飞行，进入时自动取消 | `ArcartX-Suite.essentials.fly.bypass` |
| 禁活塞 | 指定世界所有活塞推拉无效 | 无 |
| 禁交互 | 指定世界中特定方块类型不可交互 | `ArcartX-Suite.essentials.interact.bypass` |

---

## 选区工具

默认选区工具为**木斧**（`WOODEN_AXE`），可在配置中修改。

- **左键方块** → 设置点 1
- **右键方块** → 设置点 2
- 两点设置完成后自动显示选区体积

```yaml
selection:
  wand-item: WOODEN_AXE
  max-volume: 1000000
  max-regions-per-player: 10
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
  database: "ArcartX-Suite"
  username: "root"
  password: ""
  table-prefix: "axs_rg_"
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
| `ArcartX-Suite.regions.select` | 使用选区工具 | false |
| `ArcartX-Suite.regions.admin` | 管理所有区域 | OP |
| `ArcartX-Suite.regions.bypass` | 绕过所有区域保护 | OP |
| `ArcartX-Suite.regions.bypass.limit` | 绕过区域数量限制 | OP |
| `ArcartX-Suite.regions.group.<name>` | 区域权限组成员 | false |

