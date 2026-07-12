---
title: 条件系统（PlaceholderAPI + Aria + JavaScript） | ArcartX-Suite Minecraft插件文档
description: 条件系统（PlaceholderAPI + Aria + JavaScript） - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 条件系统（PlaceholderAPI + Aria + JavaScript）

ArcartX-Suite 在 **Menu、Prop、EventPacket、Mail** 等模块中统一使用同一套条件引擎。你可以用 **PlaceholderAPI 行内表达式** 做简单数值/字符串判断，也可以用 **Aria 脚本** 或 **原生 JavaScript** 编写复杂逻辑。

本页是**通用参考**；各模块的字段名与行为差异见文末「模块对照表」，并链接到对应模块文档。

---

## 前置知识：三种条件类型

| 类型 | 配置识别方式 | 运行时依赖 | 典型用途 |
| --- | --- | --- | --- |
| **PAPI 条件** | `%变量% 运算符 期望值` | [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)（推荐安装） | 等级、余额、权限组、世界名 |
| **Aria 条件** | `aria:` 前缀、`type: aria`、或 `aria-conditions` 键 | **ArcartX 内置**（ArcartX-Suite 硬依赖 ArcartX，Aria 随之提供，无需额外插件） | 调用 `player` 单层方法（等级/权限/血量等）+ PAPI 值算术/逻辑组合；与玩家无关的 Java 工具（`use()`） |
| **JS 条件** | `js:` 前缀、`type: js`、或 `js-conditions` 键 | classpath 上的 **JS `ScriptEngine`**（Nashorn / GraalJS）；Java 15+ 默认已无内置引擎 | 复杂逻辑、Bukkit API 互操作 |

::: info 逻辑关系
同一列表内的**多条条件为 AND（且）关系**——必须全部通过才算满足。暂不支持 OR / NOT 组合；复杂逻辑请写进一条 Aria 或 JS 脚本。
:::

::: warning Aria 由 ArcartX 内置提供，无需额外插件
[Aria](https://github.com/17Artist/Aria) 是 ArcartX 内置的脚本语言运行时（Java 17，无第三方依赖）。**ArcartX-Suite 硬依赖 ArcartX**（`plugin.yml` 的 `depend: [ArcartX]`，ArcartX 必装才能启动），因此 Aria 运行时**随 ArcartX 一起提供、始终可用**——不再需要 Blink 系插件（Symphony / Overture / BlinkAriaHost）。

Suite 通过 `DefaultAriaBridge` 在运行时连接 Aria，发现顺序为：

1. **ArcartX 内置的新版 Aria**（`priv.seventeen.artist.aria.Aria`）——首选；
2. Blink框架的 `AriaScriptManager`——仅作历史兼容回退，正常部署下不会用到。
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

::: tip Aria 会注入活的 player 对象（原生 Bukkit Player）
ArcartX-Suite 按 [Overture](https://github.com/17Artist/Overture) 的官方用法，在求值前把当前玩家以 `JavaObjectMirror` 包装成全局变量注入 Aria（`ObjectValue(JavaObjectMirror(player))` 写入 `globalStorage`）。因此脚本内可以直接用裸名 `player` 调用其 **Bukkit 方法**：

```yaml
conditions:
  - "aria: player.getLevel() >= 10"
  - "aria: player.hasPermission('essentials.fly')"
```

条件为**单个表达式**时，Suite 会自动补 `return` 再交给 Aria 求值。
:::

::: warning 只支持 player 上的【单层】方法调用
经真机（`aria-1.0.1`）验证：`player.getLevel()`、`player.hasPermission('...')`、`player.performCommand('...')`、`player.sendMessage('...')` 这类**直接在 player 上**的调用可用；但**链式调用不可用**——`player.getWorld().getName()` 里 `getWorld()` 返回的对象不会被再次包装，读到 `none`。需要世界名、经济余额等时，请改用 `%...%` PAPI 占位符。
:::

除了 `player`，Suite 还会在求值**前**把脚本文本里的 **PAPI 占位符（`%...%`）和 `{player}`** 展开成字面值——用于取那些不在 Player 对象上、或链式才能取到的值（如 `%player_world%`、`%vault_eco_balance%`）。字符串值记得加引号：

```yaml
conditions:
  # player 对象方法（直接调用）
  - "aria: player.getLevel() >= 10"
  # PAPI 占位符（求值前展开为字面值；字符串加引号）
  - "aria: '%player_world%' == 'world'"
```

::: warning Aria 语法与 JS 不同
Aria 有自己的语法，勿套用 JS 写法：
- 注释用 `//`（无 `/* */`）；
- 局部变量声明带命名空间前缀，如 `var.x = 1`（读取时可写裸名 `x`）；
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
  G[注入活 player 对象<br/>ObjectValue+JavaObjectMirror] --> D[条件评估]
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

`aria:` 后面整段为脚本内容（可多行时用 YAML 块标量，见下文）。脚本里可用裸名 `player` 调用其单层 Bukkit 方法；出现的 `%...%` 与 `{player}` 会在求值前先展开成字面值。

### 2.3 写法二：独立 Aria 列表键

以下键名下的**每一行字符串整段视为 Aria 脚本**（无需写 `aria:` 前缀）：

| 键名 | 说明 |
| --- | --- |
| `aria-conditions` | 推荐，语义清晰 |
| `ariaConditions` | 驼峰别名 |
| `aria-condition` / `ariaCondition` | 单数形式 |
| `aria` | 简短形式 |

```yaml
aria-conditions:
  - |
    // 组合条件：等级 ≥ 10（player 对象方法）且余额 ≥ 100（%...% 求值前已展开成数字）
    return player.getLevel() >= 10 && %vault_eco_balance% >= 100
```

Menu 模块还在按钮级支持 `condition` / `use-conditions` 等键旁挂载 Aria；见 [Menu 文档](/modules/menu#按钮条件)。

### 2.4 写法三：结构化 Map

```yaml
conditions:
  - type: aria                    # 或 kind: aria
    script: |
      var.level = player.getLevel()
      return level >= 10 && level <= 100
  - type: aria
    expression: "'%player_gamemode%' == 'SURVIVAL'"   # script / code / aria 字段等价
```

| 字段 | 说明 |
| --- | --- |
| `type` / `kind` | 填 `aria` 表示 Aria 条件 |
| `script` | 脚本正文（推荐） |
| `expression` / `code` / `aria` | 与 `script` 等价 |

::: tip 与 Overture 物品脚本的关系
[Overture](https://github.com/17Artist/Overture) 物品事件使用同一 Aria 运行时，Suite 的 `player` 注入方式（`ObjectValue(JavaObjectMirror(...))` 写入 `globalStorage`）也与它一致。区别在于 Overture 还注入了 `item`、`event` 等物品/事件上下文对象，而 **ArcartX-Suite 通用条件只注入 `player`**（外加 PAPI/`{player}` 字面值展开）；物品相关逻辑请在 Overture 配置或自定义 Aria 函数中编写。
:::

### 2.5 脚本里能用什么值

Aria 脚本可用两类值：

**1) 活的 `player` 对象**（直接注入，原生 Bukkit Player，仅支持单层方法调用）：

```aria
return player.getLevel() >= 10
return player.hasPermission('vip.access')
return player.getHealth() > 10
```

**2) 求值前展开的字面值记号**（Suite 先把脚本文本中的以下记号替换成字面值，其余部分原样交给 Aria）：

| 记号 | 展开为 |
| --- | --- |
| `%papi_placeholder%` | 对应 PlaceholderAPI 的返回值（字符串 / 数字文本） |
| `{player}` | 当前玩家名 |

对于 Player 对象上有的单层方法（等级、权限、血量…）优先用 `player.xxx()`；而**链式才能取到的值**（如世界名 `player.getWorld().getName()`）或 **PAPI 才有的值**（经济余额、权限组名、在线时长…）则用 `%...%`。`use()` 仍可调用与玩家无关的 Java 工具类（如 `java.util.Calendar`）。

```aria
// player 对象方法（不需展开）
return player.getLevel() >= 10

// player 方法 + PAPI 字面值混用
return player.getLevel() * 10 + %vault_eco_balance% / 100 >= 500
```

::: warning 展开的 PAPI 字符串值务必加引号
`%...%` 展开是纯文本替换。`%player_name% == 'Steve'` 会变成 `Steve == 'Steve'`，左边 `Steve` 被当成变量（读到 `none`）。PAPI 字符串比较请写成 `'%player_name%' == 'Steve'`；数值比较不用引号：`%player_level% >= 30`。（`player.getName()` 等对象方法返回的字符串无需额外加引号。）
:::

::: warning 链式调用不可用
只能调用 `player` 上的单层方法；`player.getWorld().getName()` 中 `getWorld()` 返回的对象不会被再次包装，后续 `.getName()` 读到 `none`。请改用对应的 PAPI 占位符（如 `%player_world%`）。
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
      // 等级/权限用 player 对象方法；世界名链式取不到，改用展开的 PAPI（字符串加引号）
      return player.getLevel() >= 30
        && '%player_world%' == 'world'
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
      var.world = '%player_world%'
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
      var.cal = Calendar.getInstance()
      var.dow = cal.get(Calendar.DAY_OF_WEEK)   // 1=周日 ... 7=周六
      var.isWeekend = dow == 1 || dow == 7
      return isWeekend && %player_level% >= 10
```

#### 案例 6：PAPI + Aria 混用

同一列表可混写（仍 AND）：

```yaml
open-requirements:
  - "%player_level% >= 5"                       # PAPI：快速筛等级
  - "aria: player.hasPermission('vip.access')"  # Aria：player 对象方法
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
| **注入变量** | `player`、`Bukkit`（可反射调用、支持链式） | `player`（原生 Bukkit Player，仅单层方法）；外加 PAPI/`{player}` 字面值展开 |
| **脚本语法** | 标准 JavaScript（Nashorn / GraalJS 方言） | Aria 语言（KISS 语法：`//` 注释、`var.` 命名空间、`use()` 互操作） |
| **性能** | 中等（解释执行） | 中等至优秀（支持 JIT） |
| **适用场景** | 需链式 Bukkit API / 复杂逻辑（需引擎） | player 单层方法 + PAPI 值的算术/逻辑组合、与玩家无关的 Java 工具 |

### 3.2 写法一：行内 `js:` 前缀

```yaml
conditions:
  - "js: player.getLevel() >= 10"
  - "js: player.getWorld().getName() == 'world'"
  - "js: player.getHealth() > player.getMaxHealth() * 0.5"
```

`js:` 后面整段为脚本内容。与 `aria:` 前缀用法完全一致，只是引擎不同。

### 3.3 写法二：独立 JS 列表键

以下键名下的**每一行字符串整段视为 JS 脚本**（无需写 `js:` 前缀）：

| 键名 | 说明 |
| --- | --- |
| `js-conditions` | 推荐，语义清晰 |
| `jsConditions` | 驼峰别名 |
| `js-condition` / `jsCondition` | 单数形式 |
| `js` | 简短形式 |

```yaml
js-conditions:
  - "player.getLevel() >= 10 && player.hasPermission('vip.access')"
  - |
    var world = player.getWorld().getName()
    var isNether = world == 'world_nether'
    var hasPerm = player.hasPermission('nether.travel')
    return isNether && hasPerm
```

### 3.4 写法三：结构化 Map

```yaml
conditions:
  - type: js                    # 或 kind: js
    script: |
      var level = player.getLevel()
      return level >= 10 && level <= 100
  - type: js
    expression: "return !player.isOp()"   # script / code / js 字段等价
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
  "player": <Bukkit Player 对象>,
  "Bukkit": <org.bukkit.Bukkit 类对象>
}
```

这意味着你可以在 JS 中：

```javascript
// 直接调用 Player API
player.getLevel()
player.getWorld().getName()
player.hasPermission('myplugin.admin')

// 通过 Bukkit 类调用静态方法
Bukkit.getOnlinePlayers().size()   // 在线人数
Bukkit.getWorld('world').getTime() // 世界时间

// 构造 Java 对象（Nashorn 写法）
var cal = java.util.Calendar.getInstance()
cal.get(java.util.Calendar.DAY_OF_WEEK)
// 注：直写 java.xxx / org.bukkit.xxx 全限定名是 Nashorn 行为；
// GraalJS 需改用 Java.type('java.util.Calendar') 取类。
```

::: warning 不要在 JS 条件里直接写 %placeholder%
ArcartX-Suite **不会**在 JS 脚本内自动展开 PAPI。若要用 PAPI 值，请改用 PAPI 行内条件，或通过 `Bukkit.dispatchCommand` 等间接方式获取。
:::

### 3.6 返回值与布尔语义

与 Aria 完全一致，脚本最后一行值会被转换为布尔：

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
      var max = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
      return health / max > 0.3   // 血量高于 30%
```

#### 案例 2：在线人数限制

```yaml
conditions:
  - type: js
    script: |
      var online = Bukkit.getOnlinePlayers().size()
      return online >= 10 && online <= 50
```

#### 案例 3：世界时间判断（白天/黑夜）

```yaml
conditions:
  - type: js
    script: |
      var time = player.getWorld().getTime()
      return time >= 0 && time < 13000   // 白天
```

#### 案例 4：玩家名正则匹配

```yaml
conditions:
  - type: js
    script: |
      var name = player.getName()
      var regex = /^[A-Z][a-z]+$/
      return regex.test(name)   // 首字母大写，其余小写
```

#### 案例 5：持有特定物品判断

```yaml
conditions:
  - type: js
    script: |
      var inv = player.getInventory()
      var item = inv.getItemInMainHand()
      if (item == null) return false
      var meta = item.getItemMeta()
      if (meta == null) return false
      return meta.hasDisplayName() && meta.getDisplayName().contains('神剑')
```

#### 案例 6：多世界 OR 判断

```yaml
conditions:
  - type: js
    script: |
      var w = player.getWorld().getName()
      return w == 'world' || w == 'world_nether' || w == 'world_the_end'
```

#### 案例 7：冷却时间计算（基于 System.currentTimeMillis）

```yaml
conditions:
  - type: js
    script: |
      // 假设玩家 metadata 中存储了上次使用时间
      var meta = player.getMetadata('last_use_skill')
      if (meta.isEmpty()) return true
      var last = meta.get(0).asLong()
      var now = java.lang.System.currentTimeMillis()
      return now - last > 30000   // 冷却 30 秒
```

#### 案例 8：PAPI + JS 混用

同一列表可混写三种类型（仍 AND）：

```yaml
conditions:
  - "%player_level% >= 20"                    # PAPI：快速筛等级
  - "js: player.hasPermission('dungeon.entry')"  # JS：Bukkit 权限
  - type: js
    script: |
      var world = player.getWorld().getName()
      return world == 'dungeon_world'
```

---

## 四、模块对照表

各模块**字段名**不同，但**语法引擎相同**。

| 模块 | 配置文件 | 条件字段 | 不满足时行为 | 文档 |
| --- | --- | --- | --- | --- |
| **Menu** | `data/menu/menus/*.yml` | `open-requirements`（打开）<br>`requirements` / `view-conditions`（可见）<br>`condition` / `use-conditions`（可点击） | 打不开 / 隐藏按钮 / 灰色禁用 | [Menu](/modules/menu) |
| **Prop** | `data/prop/props/*.yml` | `conditions` | 禁止使用，提示 `CONDITION_NOT_MET` | [Prop](/modules/prop) |
| **EventPacket** | `data/eventpacket/rules/*.yml` | `conditions` | 跳过该规则动作链 | [EventPacket](/modules/eventpacket) |
| **Mail** | `data/mail/presets/*.yml` 等 | `claim-conditions` | 无法领取附件/命令奖励 | [Mail](/modules/mail) |

### Menu 特有：可见 vs 使用

| 类型 | 字段 | 玩家看到的效果 |
| --- | --- | --- |
| **可见条件** | `requirements`、`view-conditions`、`conditions` | 不满足 → **按钮不渲染** |
| **使用条件** | `condition`、`use-conditions`、`click-conditions` | 不满足 → **按钮灰色显示**，点击无效；可配 `deny-message` |

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

1. ArcartX 是 Suite 的硬依赖，Aria 随 ArcartX 一起提供——服务器能正常启动即代表 Aria 可用，**无需再装 Symphony / Overture 等 Blink 插件**。
2. 如需程序判断，可用开发者 API `context.ariaBridge().available()`。
3. 在 Menu 按钮上添加：

```yaml
condition:
  - "aria: player.getLevel() >= 1"
```

4. 若 Aria 未就绪，按钮应显示为**禁用**（使用条件）或**打不开**（打开条件）。

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
      // 等级/权限用 player 对象方法；世界名链式取不到，改用展开的 PAPI
      return player.getLevel() >= 30
        && '%player_world%' == 'world'
        && player.hasPermission('group.warrior')
```

### 步骤 5 — 使用 JS 条件（需 JS 引擎）

若你更熟悉 JavaScript，且服务器 classpath 上有可用的 JS 引擎，可改用 JS 条件：

```yaml
conditions:
  - type: js
    script: |
      return player.getLevel() >= 30
        && player.getWorld().getName() == 'world'
        && player.hasPermission('group.warrior')
```

注意：JS 条件需服务器 classpath 上存在 JS 引擎（Java 15+ 默认无 Nashorn，需自行提供 GraalJS 等）。本插件运行于 Java 21，若未提供引擎则该条件恒为 false——此时请改用 **Aria**。

### 步骤 6 — 混用三种条件

同一条件列表可以混写 PAPI、Aria、JS（AND 关系）：

```yaml
conditions:
  - "%player_level% >= 30"                       # PAPI
  - "aria: player.hasPermission('vip.access')"   # Aria（player 单层方法）
  - "js: player.getWorld().getName() == 'world'"  # JS（需引擎，支持链式）
```

---

## 六、故障排查

| 现象　　　　　　　　　　　| 可能原因　　　　　　　　　　　　　 | 处理　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　 | 　　　　　　　　　　　　　　　 |
| ---------------------------| ------------------------------------| ----------------------------------------------------------------------------------------------------| --------------------------------|
| PAPI 条件永远不满足　　　 | 未装 PAPI / Expansion 未注册　　　 | 安装 PAPI，`/papi ecloud download <expansion>`　　　　　　　　　　　　　　　　　　　　　　　　　　 | 　　　　　　　　　　　　　　　 |
| 数值比较异常　　　　　　　| 占位符返回带单位字符串　　　　　　 | 检查 PAPI 返回值；必要时改用 JS 做 `parseInt`　　　　　　　　　　　　　　　　　　　　　　　　　　　| 　　　　　　　　　　　　　　　 |
| Aria 条件永远不满足　　　 | 用了链式调用（如 `player.getWorld().getName()`，返回 `none`）| 改用单层 `player.xxx()` 或对应的 `%占位符%`；PAPI 字符串比较记得加引号 | 　　　　　　　　　　　　　　　 |
| Aria 条件恒 false（另一种）| 未发现 Aria 运行时（正常不应发生） | 确认 ArcartX 已正常加载（Suite 硬依赖之）；若仍不可用请反馈，可能是 ArcartX 版本的 Aria 包路径差异 | 　　　　　　　　　　　　　　　 |
| JS 条件永远不满足　　　　 | classpath 上无 JS 引擎　　　　　　 | 本插件运行于 Java 21（无内置 Nashorn），需自行提供 GraalJS / standalone Nashorn；或改用 **Aria**　 | 　　　　　　　　　　　　　　　 |
| 脚本语法错误　　　　　　　| YAML 缩进 / 引号　　　　　　　　　 | 用 `\　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　| ` 块标量；查看服务端 fine 日志 |
| JS 报 `ReferenceError`　　| 变量名拼写 / 大小写　　　　　　　　| `player`、`Bukkit` 首字母大写；Bukkit API 严格区分大小写　　　　　　　　　　　　　　　　　　　　　 | 　　　　　　　　　　　　　　　 |
| Menu 按钮「看得见点不了」 | 仅 **使用条件** 未通过　　　　　　 | 检查 `condition` / `deny-message`　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　| 　　　　　　　　　　　　　　　 |
| Menu 按钮完全消失　　　　 | **可见条件** 未通过　　　　　　　　| 检查 `requirements` / `view-conditions`　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　| 　　　　　　　　　　　　　　　 |
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
- [Blink 开发者手册](https://github.com/17Artist/Blink/blob/main/DEVELOPER.md) — AriaScriptManager API
- [Aria 语言仓库](https://github.com/17Artist/Aria) — 语法与 JIT 特性
- [Nashorn 用户指南](https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/) — JVM 内置 JS 引擎文档
