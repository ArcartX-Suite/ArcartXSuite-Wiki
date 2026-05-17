# Title 称号

## 功能定位

分组称号系统：有效期/永久、属性加成、聊天/TAB 前缀、ArcartX UI 菜单、PAPI 全量输出。

### 核心特性

- **分组管理**：称号按组归类（如冒险类、探索类、活动类），每组有独立显示名和排序
- **品质系统**：普通、传说、神话等多品质等级，每个品质有独立排序
- **双类型称号**：`text`（文字称号）和 `icon`（图标称号），图标称号支持 ArcartX 自定义文字图标
- **有效期 / 永久**：通过 `/axs title give` 命令指定时长（如 `7d`、`30m`、`permanent`），后台自动过期清理
- **日期区间**：支持 `yyyy-MM-dd~yyyy-MM-dd` 格式指定称号的激活日期和过期日期，区间外的称号不生效
- **套装系统**：多个称号组成一套，达到阈值后激发额外套装属性加成，UI 内实时展示进度
- **头顶显示**：称号可配置 `overhead-mode`（`texture` 贴图 / `text` 文本），装备后自动显示在玩家头顶
- **聊天 / Tab 前后缀**：每个称号可独立配置 `chat-prefix`、`chat-suffix`、`tab-prefix`、`tab-suffix`，通过 PAPI 接入任意聊天/Tab 系统
- **属性加成**：
  - **展示属性 (display)**：仅在装备该称号时生效
  - **收藏属性 (collection)**：只要拥有且未过期即累计（收集图鉴式）
  - **两种字段形式**：`*-attributes` (Map<键, 数值>) 与 `*-attribute-lines` (List<String>)，详见下文《属性字段详解》
- **三大属性插件集成**：同时对接 AttributePlus、MythicLib（含基于它的 MMOItems 生态）、CraneAttribute，各有独立开关和 source/modifier 前缀
- **UI 菜单**：ArcartX UI 驱动的称号管理界面，玩家可浏览、装备、卸下、隐藏称号
- **PAPI 全量输出**：拥有数量、装备信息、剩余时间、属性加成等全部通过 PlaceholderAPI 输出
- **数据持久化**：SQLite 或 MySQL，带连接池

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 称号管理 UI、头顶显示和客户端图标文本 | 模块无法提供可视化称号界面 |
| 可选 | PlaceholderAPI | 输出 `%AXStitle_*%`，供聊天、TAB、计分板读取 | 称号 UI 仍可用，PAPI 输出不可用 |
| 可选 | AttributePlus | 接收 `*-attributes` 转出的文本行与 `*-attribute-lines`，调用 `addSourceAttribute` | AttributePlus 属性不生效 |
| 可选 | CraneAttribute | 同 AttributePlus。使用 `addStaticAttributeSource` / `addAttributeSource` | CraneAttribute 属性不生效 |
| 可选 | MythicLib | 只读 `*-attributes`（Map），键当 stat-id 注册 FLAT StatModifier；MMOItems 可通过其注册的 stat-id 间接使用本通道 | MythicLib stat 加成不生效 |
| 可选 | MySQL 服务 | 跨服共享称号数据 | 默认 SQLite 可用；多服共享建议改 MySQL |

## 启用步骤

```yaml
modules:
  title:
    enabled: true
```

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs title status` | 查看称号模块、数据库和缓存状态 |
| `/axs title reload` | 重载称号配置、UI 和玩家状态 |
| `/axs title give <玩家> <称号ID> <时长>` | 向玩家发放称号。时长如 `permanent`（永久）、`7d`、`12h`、`30m`、`2025-01-01~2025-12-31` |
| `/axs title revoke <玩家> <称号ID>` | 收回玩家的指定称号 |
| `/axs title open <玩家>` | 为在线玩家打开称号管理界面 |

### 玩家命令（权限：`arcartxsuite.title.use`）

| 命令 | 说明 |
| --- | --- |
| `/title` 或 `/title open` | 打开称号管理菜单 |
| `/title equip <称号ID>` | 装备指定称号，前缀/后缀/属性立即生效 |
| `/title unequip <组ID\|all>` | 卸下某个组的已装备称号，`all` 卸下全部 |
| `/title hide <称号ID>` | 隐藏指定称号（不在菜单显示但仍拥有） |
| `/title unhide <称号ID>` | 取消隐藏 |

## PAPI

前缀：`%AXStitle_*%`

### 统计

| 占位符 | 说明 |
| --- | --- |
| `%AXStitle_owned_count%` | 拥有的称号总数 |
| `%AXStitle_hidden_count%` | 已隐藏的称号数 |

### 聊天 / Tab 前后缀

| 占位符 | 说明 |
| --- | --- |
| `%AXStitle_chat_<组ID>_prefix%` | 该组已装备称号的聊天前缀 |
| `%AXStitle_chat_<组ID>_suffix%` | 该组已装备称号的聊天后缀 |
| `%AXStitle_tab_<组ID>_prefix%` | 该组已装备称号的 Tab 前缀 |
| `%AXStitle_tab_<组ID>_suffix%` | 该组已装备称号的 Tab 后缀 |

### 装备状态

| 占位符 | 说明 |
| --- | --- |
| `%AXStitle_equipped_<组ID>_id%` | 该组已装备称号的 ID |
| `%AXStitle_equipped_<组ID>_name%` | 该组已装备称号的显示名称 |
| `%AXStitle_equipped_<组ID>_group%` | 该组已装备称号所属组的显示名 |
| `%AXStitle_equipped_<组ID>_quality%` | 该组已装备称号的品质名 |

### 称号查询

| 占位符 | 说明 |
| --- | --- |
| `%AXStitle_owned_<称号ID>%` | 是否拥有指定称号（`true`/`false`） |
| `%AXStitle_hidden_<称号ID>%` | 是否隐藏了指定称号 |
| `%AXStitle_remaining_<称号ID>%` | 剩余有效时间（毫秒），永久返回 `永久` |

### 属性加成

| 占位符 | 说明 |
| --- | --- |
| `%AXStitle_display_attr_<属性键>%` | 当前展示中称号的属性加成值 |
| `%AXStitle_collection_attr_<属性键>%` | 收藏图鉴属性加成 |
| `%AXStitle_total_attr_<属性键>%` | 展示 + 收藏 + 套装的总属性加成 |
| `%AXStitle_set_bonus_attr_<属性键>%` | 套装加成属性值 |

### 日期区间

| 占位符 | 说明 |
| --- | --- |
| `%AXStitle_activates_<称号ID>%` | 称号的激活时间戳 |
| `%AXStitle_effective_<称号ID>%` | 称号当前是否在有效区间内（`true`/`false`） |

### 套装

| 占位符 | 说明 |
| --- | --- |
| `%AXStitle_set_<套装ID>_completion%` | 该套装已拥有的称号数量 |
| `%AXStitle_set_<套装ID>_active%` | 该套装是否已激活（`true`/`false`） |

## 属性字段详解

每个称号可以同时使用两类字段，两类可共存、互不覆盖：

```yaml
titles:
  example_title:
    # 佩戴生效
    display-attributes:           # Map<键, 数值>
      attack: 1
      max_health: 10
    display-attribute-lines:      # List<String>
      - "暴击率:5(%)"
      - "生命力:100"

    # 拥有即累计（未装备也算）
    collection-attributes:
      knowledge: 1
    collection-attribute-lines:
      - "魔力上限:50"
```

### 下发矩阵

| 字段 | 类型 | AttributePlus | CraneAttribute | MythicLib | MMOItems | 原版 Bukkit Attribute |
| --- | --- | --- | --- | --- | --- | --- |
| `*-attributes` | `Map<键, 数值>` | ✅ 转成 `键:数值` 行下发 | ✅ 同 AP | ✅ 键作为 stat-id 注册 FLAT modifier | ⚠️ 仅当键是 MMOItems 在 MythicLib 中注册的 stat-id 时生效 | ❌ 不接入 |
| `*-attribute-lines` | `List<String>` | ✅ 原样下发给 `addSourceAttribute` | ✅ 同 AP | ❌ 完全忽略 | ❌ | ❌ |

### 选型准则

| 场景 | 选择 | 示例 |
| --- | --- | --- |
| 纯数值增量，且要让 MythicLib 生效 | `*-attributes` | `attack: 1`、`max_health: 10` |
| 需要百分比 / 中文属性名 / AP 自定义语法 | `*-attribute-lines` | `"暴击率:5(%)"`、`"生命力:100"` |
| 同一属性要同时走 MythicLib 和 AP | 两者并写 | Map 里写英文 stat-id，lines 里写中文原生行 |

### 各插件的实际行为

#### AttributePlus / CraneAttribute

以 source 名称下发一批字符串行，由插件自己解析语法：

- `display-attributes` 的每个项 → 转成“键:数值”字符串
- `display-attribute-lines` 的每个项 → 原样使用
- 两者合并后调用 `addSourceAttribute(attrData, "AXS_TITLE_DISPLAY", lines, false)`（AP）或 `addStaticAttributeSource(attrData, "AXS_TITLE_DISPLAY", lines)`（CA）
- collection 同理，但使用 source 名 `AXS_TITLE_COLLECTION`
- source 名前缀可在 `ArcartXTitle.yml` 的 `attributeplus.source-prefix` / `craneattribute.source-prefix` 中调整

因此任何 AttributePlus / CraneAttribute 支持的属性名（含中文、百分比、区间、自定义属性）都可以直接在 lines 里写。

#### MythicLib

严格只读 `*-attributes` (Map)，**`*-attribute-lines` 被完全忽略**：

- 每个键经 `MythicLibStatKeyNormalizer.normalize(key)` 规范化后查 `statManager.isRegistered(statId)`，未注册的 stat-id 会记志警告并跳过
- 合格项构造为 `StatModifier(name, statId, value, FLAT, OTHER, OTHER)` 以 modifier 名称注册到玩家 `StatMap`
- modifier 名称默认 `AXS_TITLE_DISPLAY_<statId>` / `AXS_TITLE_COLLECTION_<statId>`，可在 `mythiclib.modifier-prefix` 中调整
- 装备 / 卸下 / 拥有变动时老的 modifier 会被先 `remove` 再重新 `register`，避免叠加

#### MMOItems

本模块 **没有独立的 MMOItems 集成代码**。MMOItems 本身基于 MythicLib stat 系统，只要你在 `*-attributes` 里写的键是 MMOItems 注册的 stat-id（如 `MAX_HEALTH`、`ATTACK_DAMAGE`、`MOVEMENT_SPEED`），就会随 MythicLib 通道生效。`*-attribute-lines` 对 MMOItems 不生效。

#### 原版 Bukkit Attribute

**本模块不直接接入 `org.bukkit.attribute.Attribute`**。如需原版 `GENERIC_MAX_HEALTH` 等生效，请通过 AttributePlus / CraneAttribute / MythicLib 的映射能力（这些插件内部都可以把自己的属性映射到原版 Attribute）间接实现。

### 常见问题

- **在菜单里看到属性显示 `-`**：检查字段名是不是拼错了。有效的只有这 4 个：`display-attributes`、`collection-attributes`、`display-attribute-lines`、`collection-attribute-lines`。其他如 `display-attributeplus` 之类不被识别。
- **中文属性名不生效**：确保写在 `*-attribute-lines` 里（不是 Map），且 AttributePlus / CraneAttribute 的属性表中存在该中文属性。
- **MythicLib 警告 `stat 未注册`**：`*-attributes` 里的键不是 MythicLib/MMOItems 仓库里已有的 stat-id。请查 `MythicLib/stats/*.yml` 或 MMOItems 属性配置。
- **菜单里同名属性堆在一行 / 分散显示**：本版本起，后端会把同名同类（`名:数值` 形式）的行**累加合并**为一项，并把 `*_attributes_text` 字段以 `List<String>` 形式发送，UI Text 控件的 `texts` 字段拿到 List 会自动按行渲染。若仍异常，请确认 `plugins/ArcartXSuite/ui/title_menu.yml` 已被新版本覆盖。
- **属性行没有颜色 / 颜色不对**：在 `ArcartXTitle.yml` 的 `ui.attribute-line-color` 配置统一改（默认 `&0`），不要在每个称号定义里加颜色码。

## 服务端 → UI 数据契约（属性相关）

打开称号菜单时，packet 携带的属性字段统一为 `List<String>`（每行一个属性）。ArcartX `Text` 控件的 `texts` 字段拿到 List 时会自动按多行渲染，所以 UI 端只要把字段直接绑给 `texts` 即可：

| 字段（`List<String>`） | 含义 |
| --- | --- |
| `selected_display_attributes_text` | 当前选中称号的佩戴属性 |
| `selected_collection_attributes_text` | 当前选中称号的收集属性 |
| `display_attributes_text` | 玩家当前装备所有称号汇总后的佩戴属性 |
| `collection_attributes_text` | 玩家所有已拥有称号汇总后的收集属性 |
| `total_attributes_text` | 装备 + 收集 + 套装加成的总属性 |
| `set_bonus_attributes_text` | 已激活套装提供的额外属性 |

所有字段在生成时都会做**同名同类合并**（解析为 `名:数值` 或 `名:数值%` 的行会累加，其他形如 `名:1~5` / `名:5(%)` 的复杂行原样保留并去重），并在每一项前自动加上颜色前缀。空列表时会发出单元素列表 `[<前缀><占位符>]`。

**单行自带颜色码的处理**：如果你在某条 `*-attribute-lines` 里写了 `"&4暴击率:5(%)"` 这种自带颜色码的行，模块会：

1. 解析时剥离行首颜色码（`&` 或 `§` + 一字符，可多组），用剥离后的属性名作为合并键；
2. 合并后输出时把**首次出现的颜色码**重新拼回去，所以 `&4暴击率:5` 和 `暴击率:3` 会合并成 `&4暴击率: 8`；
3. 最后 `colorize` 阶段会检测行首是否已有颜色码：有就跳过默认 `attribute-line-color` 前缀，没有才拼上。

也就是说：单行优先级 > 全局默认。需要某条属性高亮就在那一行写颜色码即可。

### 颜色与占位符配置

颜色前缀和空占位符不在称号定义里写，而是在 `ArcartXTitle.yml` 顶部 `ui:` 节统一配置：

```yaml
ui:
  ui-id: "AXS:title_menu"
  register-ui-on-enable: true
  # 属性列表每行的统一颜色前缀，支持 &0~&f、&l 等
  attribute-line-color: "&0"
  # 属性列表为空时的占位字符串（不需要自己加颜色前缀，模块会自动拼）
  empty-attribute-placeholder: "-"
```

### UI 推荐用法

`title_menu.yml` 默认采用最简洁的"标题 Text + 列表 Text"两件套：

```yaml
selected_display_label:
  type: Text
  attribute:
    width: 660
    fontSize: 49
    texts: "'&0佩戴属性'"
selected_display_value:
  type: Text
  attribute:
    width: 660
    fontSize: 49
    lineSpace: 4
    texts: var.selectedDisplayAttributesText   # 直接绑 List<String>，自动多行
```

如果想自己控制每行渲染（例如不同奇偶行染色、加图标等），也可以用 VStack + Observer + `self.entry` 迭代，写法见 `set_stack` / `set_observer` 的官方示例。
