---
title: 货币系统配置 | ArcartX-Suite Minecraft插件文档
description: 货币系统配置 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 货币系统配置

ArcartX-Suite 内置统一货币桥接层，所有涉及经济的模块（Warehouse、Mail、Map、Market、OnlineRewards 等）共享同一组货币定义。你只需在 `config.yml` 顶部的 `currencies` 节配置一次，全局生效。

## 基本结构

```yaml
currencies:
  <货币ID>:
    enabled: true
    provider: "<提供者类型>"
    display-name: "显示名称"
    precision: 2
    rounding: "DOWN"
    # 以下三项仅 command / placeholder-command / custom 类型需要
    balance-placeholder: ""
    withdraw-command: ""
    deposit-command: ""
```

### 通用字段说明

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | boolean | `true` | 设为 `false` 则跳过该货币 |
| `provider` | string | `"vault"` | 提供者类型，见下方详细说明 |
| `display-name` | string | 货币 ID | 显示给玩家的名称（如 "金币"、"点券"） |
| `precision` | int | `2` | 小数精度位数。`0` = 整数，`2` = 保留两位小数 |
| `rounding` | string | `DOWN` | 舍入策略，支持 `HALF_UP` 等标准 `RoundingMode` 名称；缺失或非法值回退为 `DOWN` |

### 支持的 Provider 类型

| Provider | 前置插件 | 适用场景 |
|----------|----------|----------|
| `vault` | [Vault](https://www.spigotmc.org/resources/vault.34315/) + 经济插件 | 最通用，适配 CMI / EssentialsX / 其他 Vault 经济 |
| `playerpoints` | [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/) | 整数点券系统 |
| `xconomy` | [XConomy](https://github.com/YiC200333/XConomy) | 直连 XConomy 原生 API，适用于 XConomy 货币 |
| `rondo` | [Rondo](https://wiki.arcartx.com) | ArcartX 生态多货币插件 |
| `command` | [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | 通过控制台命令 + PAPI 占位符桥接任意经济 |
| `placeholder-command` | PlaceholderAPI | 同 `command`，别名 |
| `custom` | PlaceholderAPI | 同 `command`，别名 |

::: tip 三合一
`command`、`placeholder-command`、`custom` 三个名称完全等价，内部使用相同的桥接实现。选择哪个名称取决于你的阅读习惯。
:::

---

## Provider 详解

### vault — Vault 经济

最常用的经济类型。需要服务端安装 **Vault** 插件以及至少一个注册了 Vault Economy 服务的经济插件（如 CMI、EssentialsX、Economy 等）。

```yaml
currencies:
  money:
    enabled: true
    provider: "vault"
    display-name: "金币"
    precision: 2
    rounding: "DOWN"
```

**前置条件：**
1. 安装 [Vault](https://www.spigotmc.org/resources/vault.34315/)
2. 安装任一经济插件（如 CMI、EssentialsX、Economy）
3. 确认 `/money` 或类似命令可正常使用

**工作原理：**
- 余额查询：调用 `Economy.getBalance(player)`
- 扣款：调用 `Economy.withdrawPlayer(player, amount)`
- 入账：调用 `Economy.depositPlayer(player, amount)`

::: warning 注意
一个服务端只能注册一个 Vault Economy 服务。如果你需要**多种货币**，请使用 `rondo` 或 `command` 类型而非注册多个 `vault` 货币。多个 `vault` 货币实际上都会指向同一个 Economy 实例。
:::

---

### playerpoints — PlayerPoints 点券

适用于基于整数点数的经济系统。

```yaml
currencies:
  points:
    enabled: true
    provider: "playerpoints"
    display-name: "点券"
    precision: 0
    rounding: "DOWN"
```

**前置条件：**
1. 安装 [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/)
2. 确认 `/points` 或类似命令可正常使用

**工作原理：**
- 余额查询：调用 `PlayerPointsAPI.look(uuid)`
- 扣款：调用 `PlayerPointsAPI.take(uuid, amount)`
- 入账：调用 `PlayerPointsAPI.give(uuid, amount)`

::: tip 建议
PlayerPoints 是整数点数系统，建议设置 `precision: 0`。
:::

---

### xconomy \u2014 XConomy \u539f\u751f\u7ecf\u6d4e

`xconomy` \u76f4\u63a5\u8c03\u7528 XConomy \u539f\u751f `XConomyAPI`\uff0c\u4e0d\u7ecf\u8fc7 Vault\u3002\u4f59\u989d\u67e5\u8be2\u3001\u5145\u503c\u548c\u6263\u6b3e\u5168\u7a0b\u4f7f\u7528 `BigDecimal`\uff0c\u4e0d\u4f1a\u7ecf\u8fc7 `double` \u8f6c\u6362\u3002

```yaml
currencies:
  money:
    enabled: true
    provider: "xconomy"
    display-name: "\u91d1\u5e01"
    precision: 2
    rounding: "DOWN"
```

\u5c06 `money.provider` \u8bbe\u4e3a `"xconomy"` \u5373\u53ef\u4f7f\u7528\u3002\u670d\u52a1\u5668\u9700\u8981\u5b89\u88c5\u5e76\u542f\u7528 XConomy\uff1b\u672a\u5b89\u88c5\u3001API \u4e0d\u53ef\u7528\u6216\u64cd\u4f5c\u5931\u8d25\u65f6\uff0c\u6865\u63a5\u4f1a\u8fd4\u56de\u5931\u8d25\u7ed3\u679c\u3002

\u64cd\u4f5c\u7ed3\u679c\u6cbf\u7528\u73b0\u6709\u4e2d\u6587\u63d0\u793a\uff1a\u4f59\u989d\u4e0d\u8db3\u8fd4\u56de\u201c\u4f59\u989d\u4e0d\u8db3\u201d\uff0c\u8d85\u8fc7 XConomy \u4f59\u989d\u4e0a\u9650\u8fd4\u56de\u201c\u91d1\u989d\u8d85\u51fa\u8be5\u8d27\u5e01\u53ef\u5904\u7406\u8303\u56f4\u201d\uff0c\u5176\u4ed6\u7981\u6b62\u6216\u5931\u8d25\u60c5\u51b5\u8fd4\u56de\u5bf9\u5e94\u5931\u8d25\u63d0\u793a\u3002

---

### rondo — Rondo 多货币

[Rondo](https://wiki.arcartx.com) 是 ArcartX 生态的多货币插件，原生支持多种货币 ID。ArcartX-Suite 会将配置中的货币 ID 直接传递给 Rondo API。

```yaml
currencies:
  money:
    enabled: true
    provider: "rondo"
    display-name: "金币"
    precision: 2
    rounding: "DOWN"
  points:
    enabled: true
    provider: "rondo"
    display-name: "点券"
    precision: 0
    rounding: "DOWN"
```

**前置条件：**
1. 安装 Rondo
2. 在 Rondo 配置中定义对应的货币 ID（如 `money`、`points`）

**工作原理：**
- 余额查询：调用 `RondoAPI.getBalance(uuid, currencyId)`
- 扣款：调用 `RondoAPI.withdraw(uuid, currencyId, amount, "ArcartX-Suite")`
- 入账：调用 `RondoAPI.deposit(uuid, currencyId, amount, "ArcartX-Suite")`

::: info 货币 ID 映射
ArcartX-Suite 中的货币 ID（即 `currencies:` 下的键名）会直接作为 Rondo 的 `currencyId` 参数传递。因此请确保两边的 ID **完全一致**。
:::

---

### command / placeholder-command / custom — 命令桥接

最灵活的类型。通过 **PlaceholderAPI 占位符**读取余额，通过**控制台命令**执行扣款和入账。可以桥接任何经济插件，甚至非标准的自定义点数系统。

```yaml
currencies:
  money:
    enabled: true
    provider: "command"
    display-name: "宝石"
    precision: 0
    rounding: "DOWN"
    balance-placeholder: "%economy_balance_gems%"
    withdraw-command: "esc take gems %player% %amount%"
    deposit-command: "esc give gems %player% %amount%"
```

**前置条件：**
1. 安装 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
2. 安装对应的 PAPI 扩展（`/papi ecloud download <expansion>`）
3. 确保余额占位符和管理命令可用

#### 必填字段

| 字段 | 说明 | 示例 |
|------|------|------|
| `balance-placeholder` | PAPI 占位符，返回**纯数字**余额 | `%economy_balance_gems%` |
| `withdraw-command` | 扣款时执行的控制台命令 | `money take %player% %amount%` |
| `deposit-command` | 入账时执行的控制台命令 | `money give %player% %amount%` |

::: danger 三项缺一不可
`balance-placeholder`、`withdraw-command`、`deposit-command` 三项必须全部填写，否则该货币桥接将标记为不可用。
:::

#### 命令模板变量

命令模板中可使用以下变量，运行时自动替换：

| 变量 | 替换为 | 示例 |
|------|--------|------|
| `%player%` | 玩家名 | `Steve` |
| `%uuid%` | 玩家 UUID | `069a79f4-...` |
| `%amount%` | 操作金额（经过精度处理） | `100`、`99.5` |

::: tip 命令前缀
命令**不需要**加 `/` 前缀（加了也会自动去除）。命令以控制台身份执行。
:::

#### 常见经济插件的命令桥接示例

##### Economy

```yaml
currencies:
  money:
    enabled: true
    provider: "command"
    display-name: "宝石"
    precision: 0
    rounding: "DOWN"
    balance-placeholder: "%economy_balance_gems%"
    withdraw-command: "eco take gems %player% %amount% gems"
    deposit-command: "eco give gems %player% %amount% gems"
```

##### CoinsEngine

```yaml
currencies:
  coins:
    enabled: true
    provider: "command"
    display-name: "硬币"
    precision: 0
    rounding: "DOWN"
    balance-placeholder: "%coinsengine_balance_coins%"
    withdraw-command: "coinsengine take %player% coins %amount%"
    deposit-command: "coinsengine give %player% coins %amount%"
```

##### 通用模板

如果你的经济插件提供了 PAPI 扩展和管理命令，可以按以下模板配置：

```yaml
currencies:
  my_currency:
    enabled: true
    provider: "command"
    display-name: "自定义货币"
    precision: 0
    rounding: "DOWN"
    balance-placeholder: "%<PAPI扩展>_<余额占位符>%"
    withdraw-command: "<扣款命令> %player% %amount%"
    deposit-command: "<入账命令> %player% %amount%"
```

::: warning 余额占位符要求
`balance-placeholder` 返回的文本必须是**纯数字**（允许小数点和千位逗号）。如果返回的字符串包含货币符号（如 `$100`）或其他非数字字符，系统会尝试自动提取数字部分，但建议使用返回纯数字的占位符以确保准确性。
:::

---

## 完整配置示例

以下示例展示了同时使用多种 provider 的典型配置：

```yaml
currencies:
  # 1. 主货币 — 使用 Vault
  money:
    enabled: true
    provider: "vault"
    display-name: "金币"
    precision: 2
    rounding: "DOWN"

  # 2. 点券 — 使用 PlayerPoints
  points:
    enabled: true
    provider: "playerpoints"
    display-name: "点券"
    precision: 0
    rounding: "DOWN"

  # 3. 宝石 — 使用命令桥接（Economy）
  money:
    enabled: true
    provider: "command"
    display-name: "宝石"
    precision: 0
    rounding: "DOWN"
    balance-placeholder: "%Economy_balance_gems%"
    withdraw-command: "eco take gems %player% %amount% gems"
    deposit-command: "eco give gems %player% %amount% gems"

  # 4. Rondo 多货币示例
  # rondo_gold:
  #   enabled: true
  #   provider: "rondo"
  #   display-name: "R金"
  #   precision: 2
```

---

## 模块局部货币

部分模块（如 Warehouse、Map、Market）在自身配置中也支持 `currencies` 节。模块中定义的货币会**合并到全局池**中，但**不覆盖**已有的同名货币。

```yaml
# data/warehouse/config.yml
currencies:
  warehouse_token:
    enabled: true
    provider: "command"
    display-name: "仓库代币"
    precision: 0
    rounding: "DOWN"
    balance-placeholder: "%some_placeholder%"
    withdraw-command: "token take %player% %amount%"
    deposit-command: "token give %player% %amount%"
```

::: tip 优先级规则
全局 `config.yml` 中的货币定义优先。如果全局已定义 `money`，模块中再定义 `money` 会被忽略。
:::

---

## 货币变动事件

`CurrencyBridgeManager` 在每次 `withdraw` / `deposit` 成功后，自动通过 `EventBusCapability` 分发货币变动事件。模块（如 BattlePass）可通过 `event-topic` 订阅这些事件来追踪消费/收入。

### 事件主题

| 事件主题 | 触发时机 |
|---------|---------|
| `axs.currency.spent` | 扣款（withdraw）成功时 |
| `axs.currency.earned` | 入账（deposit）成功时 |

### Payload 字段

| 字段 | 说明 |
|------|------|
| `currency_id` | 货币 ID（如 `money`、`points`） |
| `amount` | 变动金额（字符串形式） |
| `action` | `"withdraw"` 或 `"deposit"` |
| `balance_after` | 变动后余额（字符串形式） |

### 通用性

该机制对所有货币提供者（Vault / PlayerPoints / XConomy / Rondo / Command）自动生效，无需各提供者单独实现。模块开发者只需订阅事件主题即可追踪任意货币的消费/收入。

### BattlePass 累计消费任务示例

```yaml
weekly-spend-1000:
  display-name: "消费达人"
  description: "本周累计消费1000金币"
  event-topic: "axs.currency.spent"
  required-count: 1000
  conditions:
    - type: event_payload
      key: currency_id
      operator: equals
      value: money          # 可改为其他货币ID，或删除条件追踪所有货币
  increment-strategy:
    type: payload_value
    payload-key: amount     # 从 payload 提取消费金额作为进度增量
    max-per-event: 500
    scale: 1.0
```

---

## 状态检查

使用 `/axs status` 命令可以查看当前已注册的货币数量：

```
ArcartX-Suite
 - ArcartX 桥接: 已连接
 - 货币桥接: 已注册 3 种货币
```

如果某个货币的 provider 不可用（如 Vault 未安装），该货币的所有操作将返回失败结果，不会导致崩溃。模块会在日志中输出警告信息。

---

## 故障排除

### 货币不可用

**现象：** 模块扣款/入账失败，提示 "桥接不可用"。

**排查步骤：**
1. 确认前置插件已安装并启用
2. 检查 `provider` 拼写是否正确
3. 对 `command` 类型，确认 `balance-placeholder`、`withdraw-command`、`deposit-command` 三项均已填写
4. 使用 `/papi parse me <占位符>` 验证占位符是否返回数字

### Vault 提示未注册

**现象：** 日志显示 "Vault Economy 服务未注册"。

**原因：** Vault 已安装但没有经济插件注册 Economy 服务。

**解决：** 安装一个 Vault 经济插件（如 CMI、EssentialsX、Economy）。

### PlayerPoints 余额始终为 0

**现象：** `precision` 设为 2 导致整数点数被截断。

**解决：** 将 `precision` 设为 `0`。

### command 类型扣款失败但余额正常

**现象：** `balance-placeholder` 返回正确余额，但 `withdraw-command` 执行无效。

**排查：**
1. 在控制台手动执行扣款命令（替换变量），确认命令本身可用
2. 检查命令是否需要特定权限（控制台通常拥有所有权限）
3. 确认命令格式正确（不带 `/` 前缀）

