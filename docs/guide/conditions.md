---
title: 条件系统（PlaceholderAPI + Aria + JavaScript） | ArcartX-Suite Minecraft插件文档
description: 条件系统（PlaceholderAPI + Aria + JavaScript） - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 条件系统（PlaceholderAPI + Aria + JavaScript）

ArcartX-Suite 在 **Menu、Prop、EventPacket、Mail** 等模块中统一使用同一套条件引擎。你可以用 **PlaceholderAPI 行内表达式** 做简单数值/字符串判断，也可以用 **Aria 脚本** 或 **原生 JavaScript** 编写复杂逻辑。

本页是**通用参考**；各模块的字段名与行为差异见文末「模块对照表」，并链接到对应模块文档。

---

## 前置知识：四种条件类型

| 类型　　　　　| 配置识别方式　　　　　　　　　　　　　　　　　　　　| 运行时依赖　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　| 典型用途　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　 |
| ---------------| -----------------------------------------------------| ---------------------------------------------------------------------------------------| --------------------------------------------------------------------------------------------------------------|
| **PAPI 条件** | `%变量% 运算符 期望值`　　　　　　　　　　　　　　　| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)（推荐安装） | 等级、余额、权限组、世界名　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　 |
| **Aria 条件** | `aria:` 前缀、`type: aria`、或 `conditions` 键 | **ArcartX 内置**（ArcartX-Suite 硬依赖 ArcartX，Aria 随之提供，无需额外插件）　　　　 | 调用 `AriaPlayer` 门面（等级/权限/血量/世界名等）+ PAPI 值算术/逻辑组合；需要原生 API 时用 `player.bukkit()` |
| **JS 条件**　 | `js:` 前缀、`type: js`、或 `conditions` 键　　　 | classpath 上的 **JS `ScriptEngine`**（Nashorn / GraalJS）；Java 15+ 默认已无内置引擎　| 复杂逻辑、通过 `player.bukkit()` 或 `Bukkit` 进行 Bukkit API 互操作　　　　　　　　　　　　　　　　　　　　　|
| **CONTEXT 条件** | `type: context` 或 `kind: context` | 无（纯上下文变量替换） | EventPacket 事件载荷变量判断（`{variable}` 语法） |

::: info 逻辑关系
同一列表内的**多条条件为 AND（且）关系**——必须全部通过才算满足。暂不支持 OR / NOT 组合；复杂逻辑请写进一条 Aria 或 JS 脚本。
:::

::: warning Aria 由 ArcartX 内置提供，无需额外插件
[Aria](https://github.com/17Artist/Aria) 是 ArcartX 内置的脚本语言运行时。**ArcartX-Suite 硬依赖 ArcartX**（`plugin.yml` 的 `depend: [ArcartX]`），因此正常启动后 Aria 会随 ArcartX 一起提供并可用。

Aria 完全由 ArcartX 内置提供，正常启动 ArcartX 即可使用。
:::

::: tip JS 条件与 Aria 条件的取舍
- **Aria**：ArcartX 内置、开箱即用；与 ArcartX 脚本运行时共享同一引擎与标准库（`use()` Java 互操作、`regex`、`console` 等）。本插件运行环境**推荐优先使用 Aria**。
- **JS**：使用 JVM 的 `javax.script` 引擎，**并非零依赖**——Java 15 起 JDK 已移除 Nashorn，需在服务器 classpath 上自行提供 JS 引擎（GraalJS 或 standalone Nashorn `org.openjdk.nashorn`）。无引擎时 JS 条件恒为 false。因此除非确有 JS 引擎，**否则请用 Aria**。
:::

---

## 一、PlaceholderAPI 条件（PAPI）

### 1.1 行内写法（最常用）

```yaml
# 格式：%占位符% <运算符> <期望值>
conditions:
  - "%player_level% >= 10"           # 等级 ≥ 10
  - "%player_world% == world"        # 在主世界
  - "%luckperms_groups% contains VIP" # 权限组包含 VIP
  - "%player_name% regex ^[A-Z].*"   # 名字以大写字母开头
```

**解析规则：**

- 占位符必须以 `%` 开头和结尾。
- 占位符与运算符、期望值之间用**空格**分隔。
- 期望值可含空格（从第三个 token 起全部算作期望值），例如：`%player_biome% == 深暗之域`。

### 1.2 结构化写法

适合在 UI 或管理工具中逐项编辑：

```yaml
conditions:
  - placeholder: "%player_level%"   # 可写 player_level，会自动补 %
    operator: ">="                  # 别名 op
    value: "10"
  - expr: "%vault_eco_balance% >= 100"   # 等价于行内写法
  - expression: "%player_gamemode% == SURVIVAL"
```

| 字段 | 别名 | 说明 |
| --- | --- | --- |
| `placeholder` | `placeholders` | PAPI 变量 |
| `operator` | `op` | 运算符，见下表 |
| `value` | — | 期望比较值 |
| `expr` / `expression` | — | 整行 PAPI 表达式，与行内写法等价 |

### 1.3 运算符

| 运算符 | 含义 | 示例 |
| --- | --- | --- |
| `==` | 等于（**不区分大小写**） | `%player_world% == world` |
| `!=` | 不等于 | `%craneattribute_job% != 无职业` |
| `>=` `<=` `>` `<` | 数值比较；无法解析为数字时**回退为字符串比较** | `%player_level% >= 30` |
| `contains` | 子串包含（不区分大小写） | `%luckperms_groups% contains admin` |
| `regex` | 正则匹配（不区分大小写） | `%player_name% regex ^Steve.*` |

### 1.4 未安装 PlaceholderAPI 时

- PAPI 占位符**不会被解析**，按字面字符串参与比较。
- 例如 `%player_level% >= 10` 会把 `%player_level%` 当作普通文本，几乎永远不等于 `10`，条件**不通过**。
- 生产环境请安装 PAPI，并确认相关 Expansion 已注册。

### 1.5 教学示例：限制 VIP 且等级 ≥ 20

```yaml
conditions:
  - "%luckperms_primary_group% == vip"
  - "%player_level% >= 20"
```

玩家必须**同时**是 VIP 组且等级 ≥ 20。任一不满足则整体失败。

---

## 二、Aria 脚本条件

[Aria](https://github.com/17Artist/Aria) 是 ArcartX 内置、运行在 JVM 上的轻量脚本语言（与 Shimmer 同系，独立 KISS 语法——**不是 JavaScript**）。由于 ArcartX-Suite 硬依赖 ArcartX，Aria 始终可用。

::: tip Aria 会注入 AXS 的 `AriaPlayer` 门面
ArcartX-Suite 会把当前玩家包装成 AXS 自有的 `AriaPlayer` 门面，并以全局变量 `player` 注入 Aria。因此脚本内可以直接调用门面提供的读方法、权限判断、PAPI 方法和动作方法：

```yaml
conditions:
  - "aria: player.getLevel() >= 10"
  - "aria: player.hasPermission('essentials.fly')"
```

条件为**单个表达式**时，Suite 会自动补 `return` 再交给 Aria 求值。
:::

::: info `player` 是 AXS 自有门面
`player` 的类型是 `AriaPlayer`，不是原生 Bukkit `Player`。常用玩家信息、权限、PAPI 和动作均可通过门面单层调用；`player.getWorld()` 直接返回字符串世界名。需要门面未覆盖的 Bukkit API 或链式调用时，使用 `player.bukkit()` 取回原生对象。
:::

除了 `player`，Aria 路径还会在求值**前**把脚本文本里的 **PAPI 占位符（`%...%`）和 `{player}`** 展开成字面值。常用玩家信息优先使用 `AriaPlayer` 的单层方法；需要门面未覆盖的 Bukkit API 或链式调用时，可通过 `player.bukkit()` 取回原生 Bukkit `Player`。字符串值记得加引号：

```yaml
conditions:
  # AriaPlayer 门面方法（直接调用）
  - "aria: player.getLevel() >= 10"
  # Aria 路径会在求值前展开 PAPI 和 {player}
  - "aria: '%player_world%' == 'world'"
```

::: warning Aria 语法与 JS 不同
Aria 有自己的语法，勿套用 JS 写法：
- 注释用 `//`（无 `/* */`）；
- 命名空间**完全隔离、读写不回退**：裸标识符（无前缀）、`var.`、`val.` 是三个互不相通的命名空间——`var.x = 10` 之后读裸名 `x` 得到的是 `none`（是另一个变量），反之亦然。写和读必须用**同一前缀**（局部变量最简单的写法是全程用裸名：`x = 1` 后读 `x`）；
- 无 `new`；用 `use('全限定类名')` 取 Java 类，再 `类()` 构造 / `类.静态方法()` / `类.枚举字段`；
- 空值是 `none`（不是 `null`）；
- 区间 `a..b` 左闭右开；遍历用 `for (i in 0..n) { }` 或 `for (x in list) { }`；
- 条件分支 `if (...) { } elif (...) { } else { }`。

字符串比较要给展开的 PAPI 值加引号，否则裸标识符会被当变量：`"aria: '%player_name%' == 'Steve'"`。（`player.getName()` 这类对象方法返回的字符串则无需额外加引号。）
:::

### 2.1 运行时架构（简要）

```mermaid
flowchart LR
  A[ArcartX 内置 Aria 运行时] --> B[AriaBridge]
  C[ArcartX-Suite 硬依赖 ArcartX] --> A
  G[注入 AriaPlayer 门面<br/>player] --> D[条件评估]
  F[展开 PAPI %...% / {player}] --> D
  D -->|eval + 自动补 return| B
  E[Aria 条件 YAML] --> F
```

### 2.2 写法一：行内 `aria:` 前缀

```yaml
conditions:
  - "aria: player.getLevel() >= 10"
  - "aria: '%player_world%' == 'world'"
```

`aria:` 后面整段为脚本内容（可多行时用 YAML 块标量，见下文）。脚本里可用裸名 `player` 调用 `AriaPlayer` 门面方法；出现的 `%...%` 与 `{player}` 会在 Aria 求值前先展开成字面值。门面未覆盖的 Bukkit API 可通过 `player.bukkit()` 访问原生对象。

### 2.3 写法二：在规范条件列表中使用 Aria

不再使用独立的 Aria 条件键。各模块只保留自己的规范条件键，在列表项前使用 `aria:` 前缀即可；例如 Prop 和 EventPacket 使用 `conditions`：

```yaml
conditions:
  - |
    aria: |
      // 组合条件：等级 ≥ 10（AriaPlayer 门面）且余额 ≥ 100（%...% 求值前已展开成数字）
      player.getLevel() >= 10 && %vault_eco_balance% >= 100
```

Menu 按钮分别使用规范的 `requirements`（可见条件）或 `use-conditions`（使用条件）；见 [Menu 文档](/modules/menu#按钮条件)。

### 2.4 写法三：结构化 Map

```yaml
conditions:
  - type: aria                    # 或 kind: aria
    script: |
      level = player.getLevel()
      level >= 10 && level <= 100
  - type: aria
    expression: "'%player_gamemode%' == 'SURVIVAL'"   # script / code / aria 字段等价
```

| 字段 | 说明 |
| --- | --- |
| `type` / `kind` | 填 `aria` 表示 Aria 条件 |
| `script` | 脚本正文（推荐） |
| `expression` / `code` / `aria` | 与 `script` 等价 |

::: tip 
ArcartX-Suite 通用条件只注入 `player` 这一 `AriaPlayer` 门面（外加 Aria 路径的 PAPI/`{player}` 字面值展开）。
:::

### 2.5 `AriaPlayer` 门面与脚本值

| 分类 | 方法 | 返回类型 | 说明 |
| --- | --- | --- | --- |
| 读取 | `getName()` / `getUuid()` / `getDisplayName()` | `String` | 玩家名称、UUID 字符串、显示名称 |
| 读取 | `getLevel()` / `getFood()` / `getBlockX()` / `getBlockY()` / `getBlockZ()` | `int` | 等级、饱食度、方块坐标 |
| 读取 | `getExp()` | `float` | 当前经验进度 |
| 读取 | `getHealth()` / `getX()` / `getY()` / `getZ()` | `double` | 生命值、精确坐标 |
| 读取 | `getGameMode()` / `getWorld()` | `String` | 游戏模式名；世界名称（`getWorld()` 直接返回字符串） |
| 读取 | `isSneaking()` / `isSprinting()` / `isFlying()` / `isOp()` | `boolean` | 玩家状态 |
| 权限 | `hasPermission(String permission)` / `hasPerm(String permission)` | `boolean` | 权限判断；`hasPerm` 是简写 |
| PAPI | `papi(String input)` | `String` | 以当前玩家解析 PlaceholderAPI 占位符 |
| PAPI | `papiNumber(String input)` | `double` | 解析 PAPI 结果；无法解析时返回 `0.0` |

`papiNumber` 解析失败或占位符不可用时按 `0.0` 处理。
| 动作 | `command(String command)` / `console(String command)` / `op(String command)` | `boolean` | 玩家、控制台或临时 OP 身份执行命令 |
| 动作 | `msg(String message)` / `sendMessage(String message)` | `void` | 发送支持 `&` 颜色代码的消息 |
| 动作 | `title(String title, String subtitle)` | `void` | 发送标题，默认时间 `10, 60, 10` |
| 动作 | `title(String title, String subtitle, int fadeIn, int stay, int fadeOut)` | `void` | 发送标题并指定显示时间 |
| 动作 | `sound(String name)` | `void` | 播放声音，默认音量和音调 `1.0, 1.0` |
| 动作 | `sound(String name, double volume, double pitch)` | `void` | 播放声音并指定音量、音调 |
| 动作 | `close()` | `void` | 关闭玩家当前背包界面 |
| 原生对象 | `bukkit()` | `Player` | 返回原生 Bukkit `Player`，用于门面未覆盖的 API 或链式调用 |

命令、消息、标题和声音输入都会先经过 `papi()` 解析；命令开头的 `/` 会自动移除。`op()` 执行完成后会恢复玩家原本的 OP 状态。

Aria 脚本中的 `player` 是 AXS 自有的 `AriaPlayer` 门面，不是原生 Bukkit `Player`。常用玩家信息和动作均通过单层门面方法提供：

**1) `AriaPlayer` 门面方法**：

```aria
return player.getLevel() >= 10
return player.hasPermission('vip.access')
return player.getHealth() > 10
return player.getWorld() == 'world'
return player.isSneaking()
```

**2) 求值前展开的字面值记号**（Suite 先把脚本文本中的以下记号替换成字面值，其余部分原样交给 Aria）：

| 记号 | 展开为 |
| --- | --- |
| `%papi_placeholder%` | 对应 PlaceholderAPI 的返回值（字符串 / 数字文本） |
| `{player}` | 当前玩家名 |

门面没有覆盖的值或需要链式调用时，使用 `player.bukkit()` 访问原生 Bukkit `Player`，例如 `player.bukkit().getInventory()`。Aria 路径仍会在求值前展开 `%...%` PAPI 和 `{player}`；`use()` 仍可调用与玩家无关的 Java 工具类（如 `java.util.Calendar`）。

```aria
// AriaPlayer 门面方法（不需展开）
return player.getLevel() >= 10

// player 方法 + PAPI 字面值混用
return player.getLevel() * 10 + %vault_eco_balance% / 100 >= 500
```

::: warning 展开的 PAPI 字符串值务必加引号
`%...%` 展开是纯文本替换。`%player_name% == 'Steve'` 会变成 `Steve == 'Steve'`，左边 `Steve` 被当成变量（读到 `none`）。PAPI 字符串比较请写成 `'%player_name%' == 'Steve'`；数值比较不用引号：`%player_level% >= 30`。（`player.getName()` 等对象方法返回的字符串无需额外加引号。）
:::

::: tip 需要原生 Bukkit API 时使用 `bukkit()`
常用值优先使用门面单层方法，例如 `player.getWorld()` 直接返回字符串世界名。门面没有覆盖的 API、需要链式调用或需要世界对象时，使用 `player.bukkit()`，例如 `player.bukkit().getWorld().getTime()`。
:::

### 2.6 返回值与布尔语义

脚本最后一行的值会被转换为布尔：

| 返回值 | 视为 |
| --- | --- |
| `true` / 非零数字 | 通过 |
| `false` / `none` / `0` / 空字符串 | 不通过 |
| 非空字符串 | 通过（除 `"false"`、`"0"`） |

条件为单个表达式时 Suite 会自动补 `return`；多行脚本请自行写 `return`。**建议**复杂条件显式 `return true` 或 `return false`，避免歧义。

### 2.7 Aria 详细案例集

#### 案例 1：等级 + 世界 + 权限组三合一（player 方法 + PAPI 值混用）

```yaml
conditions:
  - type: aria
    script: |
      // 等级/权限/世界名直接使用 AriaPlayer 门面方法
      return player.getLevel() >= 30
        && player.getWorld() == 'world'
        && player.hasPermission('group.warrior')
```

#### 案例 2：在线时间判断（分钟）

```yaml
conditions:
  - type: aria
    # 具体占位符名视统计类 Expansion 而定；这里示意用分钟数比较
    script: |
      return %statistic_time_played% >= 120
```

#### 案例 3：余额与等级组合（算术）

```yaml
conditions:
  - type: aria
    script: |
      // 等级用 player 方法，余额用展开的 PAPI：等级*10 + 余额/100 达到阈值
      return player.getLevel() * 10 + %vault_eco_balance% / 100 >= 500
```

#### 案例 4：字符串 / 多世界判断

```yaml
conditions:
  - type: aria
    script: |
      world = '%player_world%'
      return world == 'world' || world == 'world_nether'
```

#### 案例 5：周末双倍活动入口（use() 取日期，与玩家无关）

```yaml
# EventPacket 规则的 conditions 片段
conditions:
  - type: aria
    script: |
      // Aria 无 new；用 use() 取 java.util.Calendar 判断星期几
      Calendar = use('java.util.Calendar')
      cal = Calendar.getInstance()
      dow = cal.get(Calendar.DAY_OF_WEEK)   // 1=周日 ... 7=周六
      isWeekend = dow == 1 || dow == 7
      return isWeekend && %player_level% >= 10
```

#### 案例 6：PAPI + Aria 混用

同一列表可混写（仍 AND）：

```yaml
open-requirements:
  - "%player_level% >= 5"                       # PAPI：快速筛等级
  - "aria: player.hasPermission('vip.access')"  # AriaPlayer 门面
```

---

## 三、JavaScript 脚本条件（JS）

JS 条件通过 JVM 的 `javax.script.ScriptEngine` 执行。ArcartX-Suite 在 `DefaultScriptConditionEvaluator` 中按 `getEngineByName("JavaScript")` → `getEngineByName("nashorn")` 顺序探测引擎，并将 **`player`** 与 **`Bukkit`** 注入为脚本变量。

::: warning JS 引擎并非内置，需 classpath 提供
该 JS 引擎不由 ArcartX-Suite 打包，也不一定由 JVM 提供：
- **Java 8~14**：JDK 自带 Nashorn，开箱即用。
- **Java 15+**：Nashorn 已从 JDK 移除，需在服务器 classpath 上自行提供 JS 引擎（GraalJS，或 standalone Nashorn `org.openjdk.nashorn:nashorn-core`）。

**本插件以 Java 21 构建与运行，默认 JDK 不含 JS 引擎**。探测不到引擎时：JS 条件恒为 false，JS 动作静默跳过。若不确定环境是否有引擎，优先用 **Aria**（本服已内置）。
:::

### 3.1 与 Aria 的核心差异

| 维度 | JS 条件 | Aria 条件 |
| --- | --- | --- |
| **外部依赖** | 需 classpath 上的 JS 引擎（Java 15+ 默认无） | 无（ArcartX 内置，随硬依赖提供） |
| **注入变量** | `player`（`AriaPlayer` 门面）、`Bukkit`（Bukkit 类）；可用 `player.bukkit()` 访问原生对象 | `player`（`AriaPlayer` 门面）；Aria 路径额外进行 PAPI/`{player}` 字面值展开 |
| **脚本语法** | 标准 JavaScript（Nashorn / GraalJS 方言） | Aria 语言（KISS 语法：`//` 注释、`var.` 命名空间、`use()` 互操作） |
| **性能** | 中等（解释执行） | 中等至优秀（支持 JIT） |
| **适用场景** | 复杂逻辑、JS 语法或需要通过 `player.bukkit()` / `Bukkit` 访问 Bukkit API（需引擎） | 门面方法 + Aria 路径预展开的 PAPI 值、与玩家无关的 Java 工具 |

### 3.2 写法一：行内 `js:` 前缀

```yaml
conditions:
  - "js: player.getLevel() >= 10"
  - "js: player.getWorld() == 'world'"
  - "js: player.getHealth() > player.bukkit().getMaxHealth() * 0.5"
```

`js:` 后面整段为脚本内容。与 `aria:` 前缀用法完全一致，只是引擎不同。

### 3.3 写法二：在规范条件列表中使用 JS

不再使用独立的 JS 条件键。各模块只保留自己的规范条件键，在列表项前使用 `js:` 前缀即可；例如 Prop 和 EventPacket 使用 `conditions`：

```yaml
conditions:
  - "js: player.getLevel() >= 10 && player.hasPermission('vip.access')"
  - |
    js: |
      var world = player.getWorld()
      var isNether = world == 'world_nether'
      var hasPermission = player.hasPermission('nether.travel')
      isNether && hasPermission
```

### 3.4 写法三：结构化 Map

```yaml
conditions:
  - type: js                    # 或 kind: js
    script: |
      var level = player.getLevel()
      level >= 10 && level <= 100
  - type: js
    expression: "!player.isOp()"   # script / code / js 字段等价
```

| 字段 | 别名 | 说明 |
| --- | --- | --- |
| `type` / `kind` | — | 填 `js` 表示 JavaScript 条件 |
| `script` | — | 脚本正文（推荐） |
| `expression` / `code` / `js` | — | 与 `script` 等价 |

### 3.5 脚本中可用的变量

评估时 ArcartX-Suite 注入：

```text
bindings = {
  "player": <AriaPlayer 门面>,
  "Bukkit": <org.bukkit.Bukkit 类对象>
}
```

这意味着你可以在 JS 中：

```javascript
// 调用 AriaPlayer 门面 API
player.getLevel()
player.getWorld()
player.hasPermission('myplugin.admin')
player.papi('%vault_eco_balance%')
player.bukkit().getInventory() // 门面未覆盖的原生 API

// 通过 Bukkit 类调用静态方法
Bukkit.getOnlinePlayers().size()   // 在线人数
Bukkit.getWorld('world').getTime() // 世界时间

// 构造 Java 对象（Nashorn 写法）
var cal = java.util.Calendar.getInstance()
cal.get(java.util.Calendar.DAY_OF_WEEK)
// 注：直写 java.xxx / org.bukkit.xxx 全限定名是 Nashorn 行为；
// GraalJS 需改用 Java.type('java.util.Calendar') 取类。
```

::: warning JS 不会做脚本级 PAPI 预展开
JS 路径不会在脚本执行前自动展开 `{player}` 或 `%...%` PAPI 文本。需要 PAPI 值时，请在 JS 中使用 `player.papi('%placeholder%')` 或 `player.papiNumber('%placeholder%')`；不要把 `%placeholder%` 直接当作 JS 表达式的一部分。
:::

### 3.6 返回值与布尔语义

JS 路径不会自动补 `return`；脚本执行结果取 JavaScript 引擎返回的末表达式值，再按布尔规则判断：

| 返回值 | 视为 |
| --- | --- |
| `true` / 非零数字 | 通过 |
| `false` / `null` / `0` / 空字符串 | 不通过 |
| 非空字符串 | 通过（除 `"false"`、`"0"`） |

### 3.7 JS 详细案例集

#### 案例 1：血量百分比判断

```yaml
conditions:
  - type: js
    script: |
      var health = player.getHealth()
      var max = player.bukkit().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
      health / max > 0.3   // 血量高于 30%
```

#### 案例 2：在线人数限制

```yaml
conditions:
  - type: js
    script: |
      var online = Bukkit.getOnlinePlayers().size()
      online >= 10 && online <= 50
```

#### 案例 3：世界时间判断（白天/黑夜）

```yaml
conditions:
  - type: js
    script: |
      var time = player.bukkit().getWorld().getTime()
      time >= 0 && time < 13000   // 白天
```

#### 案例 4：玩家名正则匹配

```yaml
conditions:
  - type: js
    script: |
      var name = player.getName()
      var regex = /^[A-Z][a-z]+$/
      regex.test(name)   // 首字母大写，其余小写
```

#### 案例 5：持有特定物品判断

```yaml
conditions:
  - type: js
    script: |
      var inv = player.bukkit().getInventory()
      var item = inv.getItemInMainHand()
      var meta = item == null ? null : item.getItemMeta()
      meta != null && meta.hasDisplayName() && meta.getDisplayName().contains('神剑')
```

#### 案例 6：多世界 OR 判断

```yaml
conditions:
  - type: js
    script: |
      var w = player.getWorld()
      w == 'world' || w == 'world_nether' || w == 'world_the_end'
```

#### 案例 7：冷却时间计算（基于 System.currentTimeMillis）

```yaml
conditions:
  - type: js
    script: |
      // 假设玩家 metadata 中存储了上次使用时间
      var meta = player.bukkit().getMetadata('last_use_skill')
      var last = meta.isEmpty() ? 0 : meta.get(0).asLong()
      var now = java.lang.System.currentTimeMillis()
      meta.isEmpty() || now - last > 30000   // 冷却 30 秒
```

#### 案例 8：PAPI + JS 混用

同一列表可混写三种类型（仍 AND）：

```yaml
conditions:
  - "%player_level% >= 20"                    # PAPI：快速筛等级
  - "js: player.hasPermission('dungeon.entry')"  # JS：Bukkit 权限
  - type: js
    script: |
      var world = player.getWorld()
      world == 'dungeon_world'
```

---

## 四、CONTEXT 条件（上下文变量）

CONTEXT 条件用于 EventPacket 等需要根据**事件载荷变量**判断是否执行动作的场景。左右操作数均支持 `{variable}` 语法从上下文变量映射中取值，也支持字面量。

### 4.1 写法

```yaml
conditions:
  - type: context                    # 或 kind: context
    placeholder: "{event_type}"
    operator: "=="
    value: "kill"
  - type: context
    placeholder: "{damage}"
    operator: ">="
    value: "100"
```

### 4.2 变量解析规则

- 操作数以 `{` 开头、`}` 结尾时，从上下文变量映射（`Map<String, String>`）中查找对应键的值
- 变量不存在时返回空字符串 `""`
- 不符合 `{...}` 格式的操作数按字面量处理
- 支持所有 PAPI 条件的运算符（`==`、`!=`、`>=`、`<=`、`>`、`<`、`contains`、`regex`）

### 4.3 使用场景

EventPacket 规则引擎在触发动作链时，会将事件载荷（如击杀实体类型、伤害值、任务 ID 等）作为上下文变量传入条件评估器。CONTEXT 条件允许规则根据这些变量值决定是否执行：

```yaml
# 仅当击杀的是 Boss 类型实体时触发奖励规则
conditions:
  - type: context
    placeholder: "{entity_type}"
    operator: "contains"
    value: "boss"
  - type: context
    placeholder: "{damage}"
    operator: ">="
    value: "1000"
```

::: tip CONTEXT 条件不需要玩家上下文
与 PAPI/Aria/JS 条件不同，CONTEXT 条件纯基于变量映射求值，不依赖玩家对象，适合在事件触发时无玩家上下文的场景使用。
:::

---

## 五、模块对照表

各模块**字段名**不同，但**语法引擎相同**。

| 模块 | 配置文件 | 条件字段 | 不满足时行为 | 文档 |
| --- | --- | --- | --- | --- |
| **Menu** | `data/menu/menus/*.yml` | `open-requirements`（打开）<br>`requirements`（可见）<br>`use-conditions`（可点击） | 打不开 / 隐藏按钮 / 灰色禁用 | [Menu](/modules/menu) |
| **Prop** | `data/prop/props/*.yml` | `conditions` | 禁止使用，提示 `CONDITION_NOT_MET` | [Prop](/modules/prop) |
| **EventPacket** | `data/eventpacket/rules/*.yml` | `conditions` | 跳过该规则动作链 | [EventPacket](/modules/eventpacket) |
| **Mail** | `data/mail/presets/*.yml` 等 | `claim-conditions` | 无法领取附件/命令奖励 | [Mail](/modules/mail) |

### Menu 特有：可见 vs 使用

| 类型 | 字段 | 玩家看到的效果 |
| --- | --- | --- |
| **可见条件** | `requirements` | 不满足 → **按钮不渲染** |
| **使用条件** | `use-conditions` | 不满足 → **按钮灰色显示**，点击无效；可配 `deny-message` |

详见 [Menu 按钮条件](/modules/menu#按钮条件)。

### Mail 特有：序列化格式

邮件领取条件持久化到数据库时支持：

- 行内：`%player_level% >= 10`
- 预设 YAML 旧格式：`%player_level%::GTE::10`（`::` 分隔，兼容）
- Aria：`aria:` 前缀或 `type: aria` 块（存储为 Base64 编码的 `aria\t...`）
- JS：`js:` 前缀或 `type: js` 块（存储为 Base64 编码的 `js\t...`）

---

## 五、从零配置：分步教程

### 步骤 1 — 确认 PAPI 可用（PAPI 条件）

```bash
/papi parse me %player_level%
```

有正常数值输出即可。ArcartX-Suite 占位符见 [PlaceholderAPI 速查](/guide/placeholders)。

### 步骤 2 — 写一条最简单的 PAPI 条件

在 Prop 道具定义中：

```yaml
# data/prop/props/healing_potion.yml
conditions:
  - "%player_health% < 10"    # 血量低于 10 才可用
```

重载 Prop 模块后测试。

### 步骤 3 — 使用 Aria 条件（ArcartX 内置，无需额外安装）

1. ArcartX 是 Suite 的硬依赖，Aria 随 ArcartX 一起提供——服务器能正常启动即代表 Aria 可用，**无需额外安装任何 Aria 宿主组件**。
2. 如需程序判断，可用开发者 API `context.ariaBridge().available()`。
3. 在 Menu 按钮上添加：

```yaml
use-conditions:
  - "aria: player.getLevel() >= 1"
```

4. 若 Aria 条件异常，先确认 ArcartX 已正常加载；Aria 由 ArcartX 内置提供，正常启动后无需额外安装宿主插件。

### 步骤 4 — 用 Aria 替代多条 PAPI

**之前（3 条 PAPI）：**

```yaml
conditions:
  - "%player_level% >= 30"
  - "%player_world% == world"
  - "%luckperms_primary_group% == warrior"
```

**之后（1 条 Aria）：**

```yaml
conditions:
  - type: aria
    script: |
      // 等级/权限/世界名直接使用 AriaPlayer 门面方法
      return player.getLevel() >= 30
        && player.getWorld() == 'world'
        && player.hasPermission('group.warrior')
```

### 步骤 5 — 使用 JS 条件（需 JS 引擎）

若你更熟悉 JavaScript，且服务器 classpath 上有可用的 JS 引擎，可改用 JS 条件：

```yaml
conditions:
  - type: js
    script: |
      return player.getLevel() >= 30
        && player.getWorld() == 'world'
        && player.hasPermission('group.warrior')
```

注意：JS 条件需服务器 classpath 上存在 JS 引擎（Java 15+ 默认无 Nashorn，需自行提供 GraalJS 等）。本插件运行于 Java 21，若未提供引擎则该条件恒为 false——此时请改用 **Aria**。

### 步骤 6 — 混用三种条件

同一条件列表可以混写 PAPI、Aria、JS（AND 关系）：

```yaml
conditions:
  - "%player_level% >= 30"                       # PAPI
  - "aria: player.hasPermission('vip.access')"   # AriaPlayer 门面
  - "js: player.getWorld() == 'world'"            # JS（需引擎）
```

---

## 六、故障排查

| 现象　　　　　　　　　　　| 可能原因　　　　　　　　　　　　　 | 处理　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　 | 　　　　　　　　　　　　　　　 |
| ---------------------------| ------------------------------------| ----------------------------------------------------------------------------------------------------| --------------------------------|
| PAPI 条件永远不满足　　　 | 未装 PAPI / Expansion 未注册　　　 | 安装 PAPI，`/papi ecloud download <expansion>`　　　　　　　　　　　　　　　　　　　　　　　　　　 | 　　　　　　　　　　　　　　　 |
| 数值比较异常　　　　　　　| 占位符返回带单位字符串　　　　　　 | 检查 PAPI 返回值；必要时改用 JS 做 `parseInt`　　　　　　　　　　　　　　　　　　　　　　　　　　　| 　　　　　　　　　　　　　　　 |
| Aria 条件永远不满足 | 门面方法名、返回类型或脚本语法写错 | 先核对 `AriaPlayer` 方法表；世界名直接使用 `player.getWorld()`，门面未覆盖的 API 使用 `player.bukkit()`；Aria 中的 PAPI 字符串比较记得加引号 |　　　　　　　　　　　　　　　　|
| Aria 条件恒 false（另一种）| 未发现 Aria 运行时（正常不应发生） | 确认 ArcartX 已正常加载（Suite 硬依赖之）；若仍不可用请反馈，可能是 ArcartX 版本的 Aria 包路径差异 | 　　　　　　　　　　　　　　　 |
| JS 条件永远不满足　　　　 | classpath 上无 JS 引擎　　　　　　 | 本插件运行于 Java 21（无内置 Nashorn），需自行提供 GraalJS / standalone Nashorn；或改用 **Aria**　 | 　　　　　　　　　　　　　　　 |
| 脚本语法错误　　　　　　　| YAML 缩进 / 引号　　　　　　　　　 | 用 `\　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　| ` 块标量；查看服务端 fine 日志 |
| JS 报 `ReferenceError`　　| 变量名拼写 / 大小写　　　　　　　　| `player`、`Bukkit` 首字母大写；Bukkit API 严格区分大小写　　　　　　　　　　　　　　　　　　　　　 | 　　　　　　　　　　　　　　　 |
| Menu 按钮「看得见点不了」 | 仅 **使用条件** 未通过　　　　　　 | 检查 `condition` / `deny-message`　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　| 　　　　　　　　　　　　　　　 |
| Menu 按钮完全消失　　　　 | **可见条件** 未通过　　　　　　　　| 检查 `requirements`　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　| 　　　　　　　　　　　　　　　 |
| Mail 领取失败无提示　　　 | `claim-conditions` 未满足　　　　　| 检查预设 YAML 与 PAPI/Aria/JS 条件　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　 | 　　　　　　　　　　　　　　　 |

---

## 七、开发者 API

模块可通过 `ModuleContext` 获取统一评估器：

```java
// 判断是否通过（PAPI、Aria、JS 三种统一走此接口）
boolean ok = context.scriptConditionEvaluator().passes(player, conditions);

// 获取第一条失败条件（用于提示）
ScriptCondition failed = context.scriptConditionEvaluator().firstFailed(player, conditions);

// Aria 桥接状态
boolean ariaReady = context.ariaBridge().available();

// JS 引擎状态（内部自动维护，无需手动检查）
```

---

## 相关链接

- [PlaceholderAPI 速查](/guide/placeholders) — ArcartX-Suite 输出的 `%ArcartX-Suite...%` 变量
- [Menu 通用菜单](/modules/menu) — 可见/使用/打开条件详解
- [Aria 语言仓库](https://github.com/17Artist/Aria) — 语法与 JIT 特性
- [Nashorn 用户指南](https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/) — JVM 内置 JS 引擎文档
