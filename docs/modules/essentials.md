---
title: Essentials 基础工具插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Essentials 基础工具，传送/家/Warp、飞行/无敌/治疗、一键砍树、背包整理、UI 玩家菜单，我的世界服务器基础工具插件。
---

# Essentials 基础工具

## 功能定位
Essentials 模块集合了服务器最常用的玩家工具、传送系统、安全管控和实用功能，开箱即用。


### 核心特性

| 分类 | 功能 |
| --- | --- |
| 玩家管理 | 飞行 / 无敌 / 回血 / 回饱食 / 速度 / 隐身 / AFK / 修复 / 帽子 |
| 容器 | 末影箱 / 工作台 / 铁砧 / 垃圾桶 |
| 传送系统 | Home / Warp / Spawn / TPA / Back / TP / Top / TpPos |
| 世界管理 | 时间 / 天气 |
| 安全管控 | 封禁 / 禁言（委托 Chat 模块） / 踢出 / 警告 / Sudo / 背包查看 |
| 交互 | 坐下 / 躺下 |
| 一键砍树 | 连锁砍伐原木 + 树叶，可配置斧头/潜行/连锁数 |
| 背包操作 | 自动补种作物 / 背包整理 / 自动工具切换 |
| 玩家信息 | 昵称 / 上次在线查询 |

---

## UI 面板

Essentials 模块提供两个 ArcartX UI 面板，需要服务端安装 ArcartX 客户端 mod 才可使用。

### 玩家菜单

命令：`/axs essentials menu`

单文件多视图架构，通过左侧导航栏切换 5 个子页面：

| 页面 | 功能 |
| --- | --- |
| 首页 | 玩家状态总览（飞行/无敌/AFK/速度/位置/家数量/传送点数） |
| 家 | Home 列表 + 传送/删除/新建 |
| 传送点 | Warp 列表 + 一键传送 |
| TPA | 在线玩家列表 + 点击发送传送请求 + 接受/拒绝 |
| 设置 | 飞行/自动补种/自动工具开关 + 背包整理按钮 |

### 管理员面板

命令：`/axs essentials admin`  
权限：`ArcartX-Suite.essentials.admin`

| 页面 | 功能 |
| --- | --- |
| 玩家 | 在线玩家列表 + 治疗/喂食/飞行/踢出/封禁 |
| 封禁 | 封禁记录列表 + 解封 |
| 传送点 | Warp 管理 + 新建/删除 |
| 世界 | 时间设置（白天/夜晚/正午）+ 天气（晴天/雨天/雷暴）+ 设置出生点 |

::: info 前置要求
UI 面板需要玩家安装 ArcartX 客户端 mod。未安装时命令会提示"UI 功能不可用"。
:::

---

## 命令

主入口：`/axs essentials <子命令>`，或使用缩写 `/ess <子命令>`

> 两种写法完全等价，例如 `/ess fly` = `/axs essentials fly`

### UI

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/ess menu` | 打开玩家功能菜单 | `ArcartX-Suite.essentials.menu` |
| `/ess admin` | 打开管理员面板 | `ArcartX-Suite.essentials.admin` |

### 玩家管理

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/ess fly [玩家]` | 切换飞行模式 | `ArcartX-Suite.essentials.fly` |
| `/ess god [玩家]` | 切换无敌模式 | `ArcartX-Suite.essentials.god` |
| `/ess heal [玩家]` | 恢复生命值 | `ArcartX-Suite.essentials.heal` |
| `/ess feed [玩家]` | 恢复饥饿值 | `ArcartX-Suite.essentials.feed` |
| `/ess gamemode <模式> [玩家]` | 设置游戏模式 | `ArcartX-Suite.essentials.gamemode` |
| `/ess speed <数值> [玩家]` | 设置移动速度 | `ArcartX-Suite.essentials.speed` |
| `/ess vanish` | 切换隐身 | `ArcartX-Suite.essentials.vanish` |
| `/ess afk` | 切换 AFK 状态 | `ArcartX-Suite.essentials.afk` |
| `/ess repair` | 修复手持物品 | `ArcartX-Suite.essentials.repair` |
| `/ess hat` | 将手持物品戴在头上 | `ArcartX-Suite.essentials.hat` |
| `/ess nick <昵称\|off>` | 设置/重置昵称 | `ArcartX-Suite.essentials.nick` |
| `/ess seen <玩家>` | 查看玩家最后在线信息 | `ArcartX-Suite.essentials.seen` |

### 容器

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/ess enderchest [玩家]` | 打开末影箱 | `ArcartX-Suite.essentials.enderchest` |
| `/ess workbench` | 打开工作台 | `ArcartX-Suite.essentials.workbench` |
| `/ess anvil` | 打开铁砧 | `ArcartX-Suite.essentials.anvil` |
| `/ess trash` | 打开垃圾桶 | `ArcartX-Suite.essentials.trash` |

### 传送系统

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/ess home [名称]` | 传送到家 | `ArcartX-Suite.essentials.home` |
| `/ess sethome [名称]` | 设置家 | `ArcartX-Suite.essentials.sethome` |
| `/ess delhome [名称]` | 删除家 | `ArcartX-Suite.essentials.delhome` |
| `/ess warp <名称>` | 传送到传送点 | `ArcartX-Suite.essentials.warp` |
| `/ess setwarp <名称>` | 设置传送点 | `ArcartX-Suite.essentials.setwarp` |
| `/ess delwarp <名称>` | 删除传送点 | `ArcartX-Suite.essentials.delwarp` |
| `/ess spawn` | 传送到出生点 | `ArcartX-Suite.essentials.spawn` |
| `/ess setspawn` | 设置出生点 | `ArcartX-Suite.essentials.setspawn` |
| `/ess tpa <玩家>` | 发送传送请求 | `ArcartX-Suite.essentials.tpa` |
| `/ess tpahere <玩家>` | 请求对方传送到自己 | `ArcartX-Suite.essentials.tpahere` |
| `/ess tpaccept` | 接受传送请求 | `ArcartX-Suite.essentials.tpa` |
| `/ess tpdeny` | 拒绝传送请求 | `ArcartX-Suite.essentials.tpa` |
| `/ess back` | 返回上次位置 | `ArcartX-Suite.essentials.back` |
| `/ess tp <玩家>` | 管理员直接传送 | `ArcartX-Suite.essentials.tp` |
| `/ess top` | 传送到头顶最高方块 | `ArcartX-Suite.essentials.top` |
| `/ess tppos <x> <y> <z> [世界]` | 传送到坐标 | `ArcartX-Suite.essentials.tppos` |

### 安全管控

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/ess ban <玩家> [原因]` | 永久封禁 | `ArcartX-Suite.essentials.ban` |
| `/ess tempban <玩家> <时长> [原因]` | 临时封禁 | `ArcartX-Suite.essentials.ban` |
| `/ess unban <玩家>` | 解封 | `ArcartX-Suite.essentials.unban` |
| `/ess mute <玩家> [原因]` | 永久禁言（委托 Chat 模块） | `ArcartX-Suite.essentials.mute` |
| `/ess tempmute <玩家> <时长> [原因]` | 临时禁言（委托 Chat 模块） | `ArcartX-Suite.essentials.mute` |
| `/ess unmute <玩家>` | 解除禁言（委托 Chat 模块） | `ArcartX-Suite.essentials.unmute` |
| `/ess kick <玩家> [原因]` | 踢出服务器 | `ArcartX-Suite.essentials.kick` |
| `/ess warn <玩家> <原因>` | 警告玩家 | `ArcartX-Suite.essentials.warn` |
| `/ess sudo <玩家> <命令>` | 强制玩家执行命令 | `ArcartX-Suite.essentials.sudo` |
| `/ess inv <玩家>` | 查看玩家背包 | `ArcartX-Suite.essentials.inv` |

### 交互 & 工具

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/ess sit` | 坐下 | `ArcartX-Suite.essentials.sit` |
| `/ess lay` | 躺下 | `ArcartX-Suite.essentials.lay` |
| `/ess sort` | 整理背包 | `ArcartX-Suite.essentials.sort` |
| `/ess replant` | 开关自动补种 | `ArcartX-Suite.essentials.replant` |
| `/ess autotool` | 开关自动工具切换 | `ArcartX-Suite.essentials.autotool` |

---

## 配置文件

配置文件位于 `plugins/ArcartX-Suite/data/essentials/ArcartXEssentials.yml`

### 一键砍树 (TreeCapitator)

```yaml
tree-capitator:
  enabled: true
  # 需要的权限节点 (留空 = 无需权限)
  permission: "ArcartX-Suite.essentials.treecap"
  # 最大连锁方块数
  max-blocks: 128
  # 需要手持斧头
  require-axe: true
  # 需要潜行触发
  require-sneak: false
  # 是否消耗工具耐久
  consume-durability: true
  # 支持的原木类型 (留空 = 所有原木)
  log-types: []
  # 是否同时破坏树叶
  break-leaves: true
  # 树叶搜索范围
  leaf-radius: 4
```

**工作原理**：玩家用斧头破坏原木时，自动向上搜索所有连接的原木方块并一次性破坏，模拟"砍倒整棵树"的效果。

### 背包操作 (InvActions)

```yaml
inv-actions:
  # 自动补种作物
  auto-replant:
    enabled: true
    permission: "ArcartX-Suite.essentials.replant"
    crops:
      - WHEAT
      - CARROTS
      - POTATOES
      - BEETROOTS
      - NETHER_WART
  # 背包整理
  inventory-sort:
    enabled: true
    permission: "ArcartX-Suite.essentials.sort"
    sort-mode: type  # type / name / amount
  # 自动工具切换
  auto-tool:
    enabled: true
    permission: "ArcartX-Suite.essentials.autotool"
    switch-on-break: true
```

**自动补种**：收获成熟作物时自动从背包消耗种子补种。仅成熟作物触发。

**背包整理**：整理 9-35 槽位（非快捷栏），合并同类物品后按配置的模式排序。

**自动工具切换**：左键点击方块时自动切换快捷栏中的最佳工具（镐/斧/锹/锄）。

---

## 存储

支持 SQLite（默认）和 MySQL，存储传送点、Home、封禁/警告记录等数据。禁言数据由 Chat 模块统一管理。

```yaml
storage:
  dialect: sqlite
  sqlite-file: essentials.db
  mysql:
    host: localhost
    port: 3306
    database: ArcartX-Suite
    username: root
    password: ''
    table-prefix: axs_ess_
```

---

## 跨模块联动

| 联动模块 | 说明 |
| --- | --- |
| Tab | AFK/Vanish 状态变化时刷新 Tab 列表 |
| Chat | 禁言命令委托 Chat 模块执行（通过 `ChatMutable` capability）；昵称联动 |
| EventPacket | 通过 `EssentialsQueryable` capability 供其他模块查询玩家状态 |

---

## 权限汇总

| 权限 | 说明 | 默认 |
| --- | --- | --- |
| `ArcartX-Suite.essentials.admin` | 管理员面板 UI | OP |
| `ArcartX-Suite.essentials.treecap` | 一键砍树 | false |
| `ArcartX-Suite.essentials.replant` | 自动补种 | false |
| `ArcartX-Suite.essentials.sort` | 背包整理 | false |
| `ArcartX-Suite.essentials.autotool` | 自动工具切换 | false |
| `ArcartX-Suite.essentials.fly.bypass` | 绕过世界禁飞 | OP |
| `ArcartX-Suite.essentials.interact.bypass` | 绕过交互限制 | OP |

