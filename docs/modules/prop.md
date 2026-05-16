# Prop 快捷道具

## 功能定位

把物品标记为"快捷道具"，绑定 ArcartX 客户端按键效果和临时属性加成。

### 核心特性

- **道具定义**：在 `prop/props/*.yml` 中定义快捷道具，每个道具有独立的按键绑定、冷却、消耗和效果
- **按键触发**：绑定 ArcartX 客户端自定义按键，按下即触发道具效果
- **冷却与消耗**：每个道具可配置使用冷却和消耗条件
- **使用条件**：通过 PlaceholderAPI 变量配置使用条件（等级、余额、职业等），支持数值比较、字符串匹配、包含判断和正则匹配
- **临时属性加成**：道具使用时可附加 MythicLib 临时属性（如攻击力、暴击率），效果结束后自动移除
- **提示文案**：自定义道具使用、冷却中、消耗不足等提示文本（`prop/language.yml`）
- **按键配置**：按键绑定定义在 `prop/key.yml`，可自定义客户端按键映射

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 客户端按键、道具触发包和 UI 提示 | 模块无法接收客户端快捷键 |
| 可选 | PlaceholderAPI | 解析道具使用条件中的 `%...%` 变量 | 条件原样保留无法解析，等同未配置条件 |
| 可选 | MythicLib / MMOItems | `ml\|...` / `mythiclib\|...` 临时属性效果 | 对应属性效果跳过，命令/消息类效果仍可用 |
| 可选 | AttributePlus | `ap\|...` AttributePlus 属性加成效果 | 对应属性效果跳过 |
| 可选 | 其他命令型插件 | 道具效果中执行该插件命令 | AXS 不强依赖，命令不存在时由服务端返回未知命令 |

## 启用步骤

```yaml
modules:
  prop:
    enabled: true
```

## 配置

### 主配置（`ArcartXProp.yml`）

```yaml
settings:
  debug: false

mythiclib:
  enabled: false
  source-prefix: "AXS_PROP"
```

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `settings.debug` | boolean | `false` | 是否输出调试日志 |
| `mythiclib.enabled` | boolean | `false` | 是否启用 MythicLib 临时属性效果 |
| `mythiclib.source-prefix` | string | `AXS_PROP` | MythicLib modifier 前缀，最终生成 `<前缀>_<道具ID>_<属性>` |

### 按键映射（`prop/key.yml`）

```yaml
category: "ArcartX 快捷道具按键"

keys:
  快捷道具1:
    defaultKey: "Z"
    slot: Slot1
  快捷道具2:
    defaultKey: "X"
    slot: Slot2
  快捷道具3:
    defaultKey: "C"
    slot: Slot3
```

| 字段 | 说明 |
| --- | --- |
| `category` | 客户端按键分类名，显示在按键设置界面 |
| `keys.<名称>.defaultKey` | 默认绑定的按键 |
| `keys.<名称>.slot` | 对应的 ArcartX 额外槽位 ID |

### 提示文案（`prop/language.yml`）

```yaml
COOL_DOWN: "&7[&dArcartXProp&7]&c{NAME}&f还在冷却,你还需要等待&a{TIME}秒&f才能使用道具"
NO_PERMISSION: "&7[&dArcartXProp&7]&f你没有权限使用 &c{NAME}"
NO_KEY: "&7[&dArcartXProp&7]&c{NAME} &f无法按键使用"
NO_HAND: "&7[&dArcartXProp&7]&c{NAME} &f该道具无法手持使用"
CONDITION_NOT_MET: "&7[&dArcartXProp&7]&c{NAME} &f使用条件不满足: &e{CONDITION}"
```

| 变量 | 说明 |
| --- | --- |
| `{NAME}` | 道具显示名 |
| `{TIME}` | 剩余冷却秒数（保留两位小数） |
| `{CONDITION}` | 未满足的条件原文（仅 `CONDITION_NOT_MET` 可用） |

## 道具定义

道具定义文件位于 `prop/props/*.yml`，支持子目录。每个 `.yml` 文件定义一个道具。

### 字段说明

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `name` | string | 文件名 | 道具显示名 |
| `coolDownGroup` | string | `默认冷却组` | 冷却组；同组道具共享冷却 |
| `coolDownTime` | int | `10` | 冷却时间（秒） |
| `Duration` | int | `10` | 持续时间（秒）；药水、属性效果到期后自动移除 |
| `remove` | boolean | `true` | 使用后是否消耗物品 |
| `hand` | boolean | `true` | 是否允许手持右键使用 |
| `key` | boolean | `true` | 是否允许通过客户端按键触发 |
| `permission` | string | `""` | 使用权限；留空表示不检查 |
| `conditions` | list | `[]` | 使用条件列表；详见下方"使用条件" |
| `effects` | list | `[]` | 效果列表；详见下方"效果类型" |

### 使用条件（conditions）

通过 PlaceholderAPI 变量实现条件判断，所有条件必须同时满足（AND 逻辑）。

**格式**：`<PlaceholderAPI变量> <运算符> <期望值>`

**支持运算符**：

| 运算符 | 说明 | 示例 |
| --- | --- | --- |
| `==` | 等于（不区分大小写） | `%player_world% == world` |
| `!=` | 不等于 | `%craneattribute_job% != 无职业` |
| `>=` | 大于等于（数值比较） | `%player_level% >= 10` |
| `<=` | 小于等于 | `%player_max_health% <= 40` |
| `>` | 大于 | `%player_health% > 5` |
| `<` | 小于 | `%player_food_level% < 20` |
| `contains` | 包含子串（不区分大小写） | `%luckperms_groups% contains VIP` |
| `regex` | 正则匹配（不区分大小写） | `%player_name% regex ^[A-Z].*` |

**数值比较**：当两端都可解析为数字时按数值比较，否则按字符串字典序比较。

**完整示例**：

```yaml
conditions:
  # 数值比较
  - "%player_level% >= 10"
  - "%vault_eco_balance% >= 100"

  # 字符串精确匹配（支持中文）
  - "%player_world% == world"
  - "%player_biome% == 森林"
  - "%luckperms_primary_group% == 战士"
  - "%craneattribute_job% != 无职业"

  # 包含判断
  - "%luckperms_groups% contains VIP"
  - "%player_biome% contains 沙漠"

  # 正则匹配
  - "%player_name% regex ^[A-Z].*"
  - "%luckperms_primary_group% regex (战士|法师|弓手)"
```

> 条件依赖 PlaceholderAPI 插件；未安装时条件跳过，等同未配置。

### 效果类型（effects）

每行格式：`<类型>|<参数>`

| 类型 | 参数格式 | 说明 |
| --- | --- | --- |
| `cmd` | 命令文本 | 以控制台身份执行命令，`{player}` 替换为玩家名 |
| `msg` | 消息文本 | 给玩家发送彩色消息，支持 `&` 颜色码 |
| `food` | 数字 | 恢复饱食度 |
| `health` | 数字 | 恢复生命值 |
| `healthPercent` | 数字 | 按最大生命值百分比恢复 |
| `exp` | 数字 | 增加经验值 |
| `potion` | `效果:等级` | 附加药水效果，持续时间取 `Duration`，如 `SPEED:2` |
| `ap` | `属性名:数值` | 附加 AttributePlus 属性，持续时间取 `Duration`，如 `物理伤害:10` |
| `ml` / `mythiclib` | `属性:数值` | 附加 MythicLib 临时属性，需在主配置启用，如 `ATTACK_DAMAGE:5` |

## 物品绑定

Prop 模块通过 ArcartX 的 NBT 系统将道具 ID 写入物品：

| NBT 标签 | 说明 |
| --- | --- |
| `prop_id` | 道具定义 ID（对应 `prop/props/` 下文件名，不含扩展名） |
| `cooldown` | 冷却组标签，用于 ArcartX 冷却系统 |

使用 `/axs prop set <道具ID>` 可将手持物品绑定为指定道具（调试用）。

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs prop status` | 查看 Prop 模块状态和已加载道具列表 |
| `/axs prop reload` | 重载 Prop 配置 |
| `/axs prop set <道具ID>` | 把指定道具 ID 写入执行者手持物品的 NBT，用于调试 |

## 使用流程

1. 玩家手持右键或按下绑定按键
2. 读取物品 `prop_id` NBT → 查找道具定义
3. 依次检查：手持/按键权限 → 使用权限 → **使用条件（conditions）** → 冷却
4. 设置冷却 → 消耗物品（如配置） → 执行效果列表

## 注意事项

- 道具文件名（不含 `.yml`）即为道具 ID，匹配时转为小写
- 同一冷却组内的多个道具共享冷却时间
- MythicLib 效果需要在 `ArcartXProp.yml` 中启用 `mythiclib.enabled: true`
- 条件中使用的 PlaceholderAPI 变量取决于服务端已安装的 PAPI 扩展
- 条件值支持中文（如 `%player_biome% == 森林`、`%luckperms_primary_group% == 战士`）
