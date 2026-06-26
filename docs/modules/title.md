---
title: Title 称号插件 | ArcartX-Suite Minecraft服务器
description: ArcartX-Suite Title 分组称号系统，支持有效期/永久、品质等级、属性加成、套装加成、头顶显示、聊天Tab前缀，我的世界服务器称号插件。
---

# Title 称号

::: tip 付费模块

Title 为付费模块，需要有效授权码激活。
:::
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
  - **两种字段形式**：`*-attributes`（`Map<键, 数值>`）与 `*-attribute-lines`（`List<String>`），详见下文《属性字段详解》
- **四大属性插件集成**：同时对接 AttributePlus、MythicLib（含基于它的 MMOItems 生态）、CraneAttribute、Symphony，各有独立开关和 source/modifier 前缀
- **UI 菜单**：ArcartX UI 驱动的称号管理界面，玩家可浏览、装备、卸下、隐藏称号
- **PAPI 全量输出**：拥有数量、装备信息、剩余时间、属性加成等全部通过 PlaceholderAPI 输出
- **数据持久化**：SQLite 或 MySQL，带连接池

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 称号管理 UI、头顶显示和客户端图标文本 | 模块无法提供可视化称号界面 |
| 可选 | PlaceholderAPI | 输出 `%axstitle_*%`，供聊天、TAB、计分板读取 | 称号 UI 仍可用，PAPI 输出不可用 |
| 可选 | AttributePlus | 接收 `*-attributes` 转出的文本行与 `*-attribute-lines`，调用 `addSourceAttribute` | AttributePlus 属性不生效 |
| 可选 | CraneAttribute | 同 AttributePlus。使用 `addStaticAttributeSource` / `addAttributeSource` | CraneAttribute 属性不生效 |
| 可选 | MythicLib | 只读 `*-attributes`（Map），键当 stat-id 注册 FLAT StatModifier；MMOItems 可通过其注册的 stat-id 间接使用本通道 | MythicLib stat 加成不生效 |
| 可选 | Symphony | 接收 `*-attributes` 与 `*-attribute-lines`，按 Symphony 属性系统格式下发 | Symphony 属性不生效 |
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

### 时长格式详解

`/axs title give` 的 `<时长>` 参数支持以下格式：

| 格式 | 示例 | 说明 |
| --- | --- | --- |
| `permanent` | `permanent` | 永久称号，无过期时间 |
| `<数字>s` | `30s` | 30 秒后过期 |
| `<数字>m` | `30m` | 30 分钟后过期 |
| `<数字>h` | `12h` | 12 小时后过期 |
| `<数字>d` | `7d` | 7 天后过期 |
| `yyyy-MM-dd~yyyy-MM-dd` | `2025-01-01~2025-12-31` | 日期区间，每天 0 点（系统时区）生效/失效 |

> **注意**：日期区间的结束日期采用"当日结束"语义，即 `2025-12-31` 会持续到该日 23:59:59，而非 0 点整立即失效。

## 跨模块能力（Capability）

Title 模块在启动时向 `ModuleContext` 注册以下能力接口，供 EventPacket 等模块调用：

### TitleGrantable

由 Title 模块注册，供 EventPacket 等模块授予称号。

```java
boolean giveTitle(UUID playerId, String titleId, String duration, String source);
```

- `duration` 支持 `permanent`、`7d`、`30m`、`2025-01-01~2025-12-31` 等格式
- `source` 用于日志追踪（如 `"EventPacket"`）
- 返回 `true` 表示授予成功

**使用场景**：EventPacket 规则引擎在触发特定事件时自动发放称号奖励。

### TitleConfigQueryable

由 Title 模块注册，供外部模块查询称号配置元数据（避免直接依赖 Title 内部配置类）。

```java
TitleInfo queryTitle(String titleId);
record TitleInfo(String displayName, String qualityName, String description) {}
```

**使用场景**：QuestGPS 等模块在任务奖励提示中查询称号的显示名称和品质。

### PlayerDataPurgeable

由 Title 模块注册，支持 `/axs purge` 统一清理玩家数据。

```java
String moduleId();           // 返回 "title"
int purgePlayerData(UUID);   // 删除指定玩家数据，返回影响行数
int purgeAllPlayerData();    // 清空全部玩家数据，返回影响行数
```

### DatabaseMigratable

由 Title 模块注册，支持 `/axs migrate title sqlite-to-mysql [overwrite]` 跨源数据库迁移。

```java
String moduleId();
MigrationResult migrateDatabase(StorageDescriptor target, boolean overwrite);
StorageDescriptor currentDescriptor();
```

## PAPI

前缀：`%axstitle_*%`

### 统计

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_owned_count%` | 拥有的称号总数 |
| `%axstitle_hidden_count%` | 已隐藏的称号数 |

### 聊天 / Tab 前后缀

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_chat_<组ID>_prefix%` | 该组已装备称号的聊天前缀 |
| `%axstitle_chat_<组ID>_suffix%` | 该组已装备称号的聊天后缀 |
| `%axstitle_tab_<组ID>_prefix%` | 该组已装备称号的 Tab 前缀 |
| `%axstitle_tab_<组ID>_suffix%` | 该组已装备称号的 Tab 后缀 |

### 装备状态

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_equipped_<组ID>_id%` | 该组已装备称号的 ID |
| `%axstitle_equipped_<组ID>_name%` | 该组已装备称号的显示名称 |
| `%axstitle_equipped_<组ID>_group%` | 该组已装备称号所属组的显示名 |
| `%axstitle_equipped_<组ID>_quality%` | 该组已装备称号的品质名 |

### 总展示称号

玩家可在称号菜单中手动指定一个已装备的称号作为主展示称号。未指定时，按 `display-title.groups` 配置的分组顺序回退到第一个已装备的称号。无装备时返回 `empty-text`。

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_display%` | 玩家指定的主展示称号名称；未指定时按 `display-title.groups` 顺序回退到第一个已装备的称号 |
| `%axstitle_display_name%` | 同 `%axstitle_display%` |
| `%axstitle_display_chat_prefix%` | 主展示称号的聊天前缀 |
| `%axstitle_display_chat_suffix%` | 主展示称号的聊天后缀 |
| `%axstitle_display_tab_prefix%` | 主展示称号的 Tab 前缀 |
| `%axstitle_display_tab_suffix%` | 主展示称号的 Tab 后缀 |

> 只想展示单个组的称号时，`display-title.groups` 只填一个组 ID；想展示多组则填多个，留空则按定义顺序展示所有组。

### 称号查询

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_owned_<称号ID>%` | 是否拥有指定称号（`true`/`false`） |
| `%axstitle_hidden_<称号ID>%` | 是否隐藏了指定称号 |
| `%axstitle_remaining_<称号ID>%` | 剩余有效时间（毫秒），永久返回 `永久` |

### 属性加成

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_display_attr_<属性键>%` | 当前展示中称号的属性加成值 |
| `%axstitle_collection_attr_<属性键>%` | 收藏图鉴属性加成 |
| `%axstitle_total_attr_<属性键>%` | 展示 + 收藏 + 套装的总属性加成 |
| `%axstitle_set_bonus_attr_<属性键>%` | 套装加成属性值 |

### 日期区间

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_activates_<称号ID>%` | 称号的激活时间戳 |
| `%axstitle_effective_<称号ID>%` | 称号当前是否在有效区间内（`true`/`false`） |

### 套装

| 占位符 | 说明 |
| --- | --- |
| `%axstitle_set_<套装ID>_completion%` | 该套装已拥有的称号数量 |
| `%axstitle_set_<套装ID>_active%` | 该套装是否已激活（`true`/`false`） |

## 头顶显示

称号可配置装备后在玩家头顶显示称号名或贴图。有两种模式可选：

### 配置字段

在称号定义 YAML 中（`data/title/titles/*.yml`）添加以下字段：

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `overhead-mode` | String | `none` | `texture`（ArcartX 贴图） / `text`（Scoreboard Team） / `none`（不显示） |
| `overhead-texture` | String | 空 | TEXTURE 模式的渲染内容，使用 ArcartX 文字贴图格式（如 `§k!%000001&lt;icon&gt;`） |
| `overhead-width` | int | `64` | TEXTURE 模式贴图宽度 |
| `overhead-height` | int | `64` | TEXTURE 模式贴图高度 |
| `overhead-offset-y` | double | `2.3` | TEXTURE 模式 Y 轴偏移（越大越高） |
| `overhead-prefix` | String | 空 | TEXT 模式的玩家名前缀 |
| `overhead-suffix` | String | 空 | TEXT 模式的玩家名后缀 |

### 模式一：TEXTURE（推荐）

使用 ArcartX 客户端渲染引擎（WorldTextureEffect），文字以自定义字体贴图形式显示在玩家头顶，效果同 UI 中的 Text 控件。需要服务器安装 ArcartX 插件。

```yaml
starwalker:
  enabled: true
  group: exploration
  display-name: "&6星轨行者"
  overhead-mode: texture
  overhead-texture: "§k!%000001<icon>"
  overhead-width: 80
  overhead-height: 20
  overhead-offset-y: 2.3
```

- `overhead-texture` 使用 ArcartX 文字贴图格式 `§k!%000001&lt;icon&gt;`，与 UI 和聊天中使用的格式一致
- 贴图始终面向观察者（billboard 模式）
- ArcartX 未安装时自动降级为 TEXT 模式

### 模式二：TEXT

纯服务端实现，通过 Scoreboard Team 的 prefix/suffix 在玩家名称旁显示文字。无需任何额外插件。

```yaml
adventurer:
  enabled: true
  group: adventure
  display-name: "&0冒险者"
  overhead-mode: text
  overhead-prefix: "&7[冒险者] "
  overhead-suffix: ""
```

- 受 Minecraft 原版限制，前缀/后缀长度有限
- 其他 Scoreboard 相关插件可能冲突（如已有 Team 管理）

### 降级行为

| 条件 | 实际行为 |
| --- | --- |
| `overhead-mode: texture` 且 ArcartX 正常 | 使用 ArcartX WorldTextureEffect 渲染 |
| `overhead-mode: texture` 但 ArcartX 不可用 | 自动降级到 TEXT 模式（需配好 prefix/suffix） |
| `overhead-mode: text` | 使用 Scoreboard Team |
| `overhead-mode: none` 或未配置 | 不显示头顶称号 |

### 触发时机

- 玩家登录后加载称号状态时
- 装备 / 卸下称号时
- 称号过期被清理时

系统遍历玩家已装备的所有分组称号，选取第一个 `overhead-mode != none` 的称号进行头顶显示。同一时间只能显示一个头顶称号。

## 属性字段详解

每个称号可以同时使用两类字段，两类可共存、互不覆盖：

称号目录位于 `data/title/titles/*.yml`，建议按称号组分文件：

```yaml
# data/title/titles/adventure.yml
newcomer:
  enabled: true
  group: adventure
  quality: common
  display-name: "&e初来乍到"
  # 佩戴生效
  display-attributes:           # Map<键, 数值>
    attack: 1
    max_health: 10
  display-attribute-lines:      # List<String>
    - "暴击率:5(%)"
    - "生命力:100"

explorer:
  enabled: true
  group: adventure
  quality: legend
  display-name: "&6探险者"
  # 拥有即累计（未装备也算）
  collection-attributes:
    knowledge: 1
  collection-attribute-lines:
    - "魔力上限:50"
```

主配置 `ArcartXTitle.yml` 中的称号目录键：

```yaml
# 称号定义目录，相对模块数据目录。
# 建议按称号组分文件，例如 titles/adventure.yml、titles/exploration.yml。
titles-directory: "titles"
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

#### Symphony

接收 `*-attributes`（Map）与 `*-attribute-lines`（List&lt;String&gt;），按 Symphony 属性系统格式下发：

- `display-attributes` 的每个项 → 转成 Symphony 属性键值对
- `display-attribute-lines` 的每个项 → 原样下发
- 两者合并后调用 Symphony API 注册到玩家
- collection 同理，source 名可在 `ArcartXTitle.yml` 的 `symphony.source-prefix` 中调整（默认 `AXS_TITLE`）

#### 原版 Bukkit Attribute

**本模块不直接接入 `org.bukkit.attribute.Attribute`**。如需原版 `GENERIC_MAX_HEALTH` 等生效，请通过 AttributePlus / CraneAttribute / MythicLib / Symphony 的映射能力（这些插件内部都可以把自己的属性映射到原版 Attribute）间接实现。

### 常见问题

- **在菜单里看到属性显示 `-`**：检查字段名是不是拼错了。有效的只有这 4 个：`display-attributes`、`collection-attributes`、`display-attribute-lines`、`collection-attribute-lines`。其他如 `display-attributeplus` 之类不被识别。
- **中文属性名不生效**：确保写在 `*-attribute-lines` 里（不是 Map），且 AttributePlus / CraneAttribute 的属性表中存在该中文属性。
- **MythicLib 警告 `stat 未注册`**：`*-attributes` 里的键不是 MythicLib/MMOItems 仓库里已有的 stat-id。请查 `MythicLib/stats/*.yml` 或 MMOItems 属性配置。
- **菜单里同名属性堆在一行 / 分散显示**：本版本起，后端会把同名同类（`名:数值` 形式）的行**累加合并**为一项，并把 `*_attributes_text` 字段以 `List&lt;String&gt;` 形式发送，UI Text 控件的 `texts` 字段拿到 List 会自动按行渲染。若仍异常，请确认 `plugins/ArcartX-Suite/ui/title_menu.yml` 已被新版本覆盖。
- **属性行没有颜色 / 颜色不对**：在 `ArcartXTitle.yml` 的 `ui.attribute-line-color` 配置统一改（默认 `&0`），不要在每个称号定义里加颜色码。

## 服务端 → UI 数据契约（属性相关）

打开称号菜单时，packet 携带的属性字段统一为 `List<String>`（每行一个属性）。ArcartX `Text` 控件的 `texts` 字段拿到 List 时会自动按多行渲染，所以 UI 端只要把字段直接绑给 `texts` 即可：

| 字段 | 含义 |
| --- | --- |
| `titles` | 全部称号字典，key 为称号 ID，value 包含 `id`、`display_name`、`group_id`、`quality_id`、`owned`、`hidden`、`equipped`、`selected`、`remaining_text` 等 |
| `groups` | 分组筛选字典，包含 `all`（全部）与配置的分组 |
| `qualities` | 品质筛选字典，包含 `all`（全部）与配置的品质 |
| `filter_modes` | 模式筛选字典，固定为 `all` / `owned` / `hidden` |
| `selected_id` | 当前选中的称号 ID |
| `selected_display_name` | 当前选中称号的展示名 |
| `selected_kind` | 当前选中称号类型：`text` / `icon` |
| `selected_group_id` | 当前选中称号的分组 ID |
| `selected_group_name` | 当前选中称号的分组名 |
| `selected_quality_name` | 当前选中称号的品质名 |
| `selected_chat_prefix` / `selected_chat_suffix` | 当前选中称号的聊天前缀 / 后缀 |
| `selected_tab_prefix` / `selected_tab_suffix` | 当前选中称号的 Tab 前缀 / 后缀 |
| `selected_description` | 当前选中称号的介绍 |
| `selected_source` | 当前选中称号的获取渠道 |
| `selected_owned` | 当前选中称号是否已拥有 |
| `selected_hidden` | 当前选中称号是否已隐藏 |
| `selected_remaining_text` | 当前选中称号的剩余时间文本 |
| `selected_can_equip` | 当前选中称号是否可装备（已拥有且未装备） |
| `selected_is_equipped` | 当前选中称号是否已装备在所属分组 |
| `selected_group_equipped_name` | 当前选中称号所属分组中已装备的称号名（兼容字段） |
| `selected_display_attributes_text` | 当前选中称号的佩戴属性（`List<String>`） |
| `selected_collection_attributes_text` | 当前选中称号的收集属性（`List<String>`） |
| `display_attributes_text` | 玩家当前装备所有称号汇总后的佩戴属性（`List<String>`） |
| `collection_attributes_text` | 玩家所有已拥有称号汇总后的收集属性（`List<String>`） |
| `total_attributes_text` | 装备 + 收集 + 套装加成的总属性（`List<String>`） |
| `set_bonus_attributes_text` | 已激活套装提供的额外属性（`List<String>`） |
| `display_title_id` | 当前主展示称号 ID（无则空） |
| `display_title_name` | 当前主展示称号名称（无则返回 `empty-text`） |
| `display_title_chat_prefix` | 主展示称号聊天前缀 |
| `display_title_chat_suffix` | 主展示称号聊天后缀 |
| `display_title_tab_prefix` | 主展示称号 Tab 前缀 |
| `display_title_tab_suffix` | 主展示称号 Tab 后缀 |
| `selected_display_title_id` | 玩家已选的主展示称号 ID（可能为空） |
| `selected_is_display_title` | 当前选中称号是否就是主展示称号 |
| `equipped_summary` | 已装备称号汇总文本（兼容字段） |
| `equipped_count` | 已装备的分组数量 |
| `equipped_by_group` | 按组展示的装备状态字典，每个条目含 `group_id`、`group_name`、`title_id`、`display_name`、`quality_name` |
| `owned_count` | 已拥有的称号数量 |
| `hidden_count` | 已隐藏的称号数量 |
| `sets` | 套装进度字典，每个条目含 `id`、`display_name`、`owned_count`、`total_count`、`active`、`progress`、`bonus_text`、`required_titles_text` |

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
  ui-id: "ArcartX-Suite:title_menu"
  register-ui-on-enable: true
  # 属性列表每行的统一颜色前缀，支持 &0~&f、&l 等
  attribute-line-color: "&0"
  # 属性列表为空时的占位字符串（不需要自己加颜色前缀，模块会自动拼）
  empty-attribute-placeholder: "-"
```

### 称号菜单界面

`title_menu.yml` 采用 **左 - 中 - 右三栏布局**，宽度分别约为 420、540、440，整体面板 1520×900。

- **左栏**
  - 顶部三个模式按钮：`全部` / `已拥有` / `已隐藏`（当前选中的按钮会高亮）
  - 分组筛选条、品质筛选条（横向滚动，均支持 `all` 选项）
  - 称号列表（纵向滚动，带 `装备 ✔` 标记与选中高亮）
  - 点击任意称号项即可发送 `select` 包并刷新中间详情

- **中栏**
  - 当前选中称号的展示名、类型（文本/图标）、分组、品质
  - 聊天前缀 / 后缀、Tab 前缀 / 后缀
  - 介绍、获取渠道、拥有状态、剩余时间
  - 佩戴属性、收集属性（仅针对选中称号）
  - 当前装备属性、当前收集属性、总属性、套装加成属性
  - 套装详情卡片（进度、所需称号、加成说明）

- **右栏**
  - 当前已按组装备的称号列表（未装备的分组显示为空）
  - 总属性汇总
  - 当前主展示称号（玩家手动指定或自动回退）
  - 动作按钮：`设为主展示` / `装备本组` / `卸下本组` / `全部卸下` / `隐藏` / `取消隐藏` / `刷新`
    - `设为主展示` 仅在选中称号已装备且不是当前主展示称号时显示

> 称号列表不再使用 `Observer`，改为 `HStack` / `VStack` + `action.create` 手动 `copy` 模板项，每个项通过 `entryKey` / `entry` 自定义属性绑定到 `var.titles`。过滤条件由 `var.filterMode`、`var.filterGroup`、`var.filterQuality` 控制。

### UI 推荐用法

属性文本字段统一为 `List<String>`，直接绑给 `Text` 控件的 `texts` 字段即可多行渲染：

```yaml
selected_display_value:
  type: Text
  attribute:
    width: 660
    fontSize: 36
    lineSpace: 4
    texts: var.selectedDisplayAttributesText
```

列表项模板示例：

```yaml
称号模板:
  type: Texture
  attribute:
    entryKey: ''
    entry: var.titles[self.entryKey]
    width: 372
    height: 64
    normal: "self.entry != null && self.entry['selected'] ? '120,190,255,230' : '145,145,145,255'"
    visible: "self.entryKey != '' && self.entry != null && ..."
  action:
    clickLeft: |-
      Packet.send('AXS_TITLE_MENU', 'select', self.entryKey)
```

## 总展示称号配置

`display-title` 配置节控制「总展示称号」的行为——玩家可从**所有已装备的称号中选择一个**作为主展示称号，供 PAPI 和 UI 使用。未手动指定时，按 `groups` 顺序自动回退到第一个已装备的称号。

```yaml
display-title:
  # 可选主展示称号的分组列表，按顺序优先回退。
  # 留空 [] 表示按分组定义顺序回退。
  # 玩家未指定时，取第一个已装备的分组称号。
  groups: []

  # 多组拼接时的分隔符。
  separator: " "

  # 没有装备任何称号时返回的文本。留空则返回空字符串。
  empty-text: ""
```

### 典型场景

| 场景 | 配置 | `%axstitle_display%` 输出示例 |
| --- | --- | --- |
| 只展示冒险组称号 | `groups: [adventure]` | `勇者` |
| 展示冒险+探索两组 | `groups: [adventure, exploration]` | `勇者 探险家` |
| 展示所有组（默认） | `groups: []` | `勇者 探险家 节日特赠` |
| 没装备任何称号 | `empty-text: "无称号"` | `无称号` |

## 完整配置示例

以下是一份可直接使用的 `ArcartXTitle.yml` 骨架，覆盖存储、UI、分组、品质、套装和属性插件前缀：

```yaml
# 称号定义目录，相对模块数据目录（plugins/ArcartX-Suite/modules/title/）
titles-directory: "titles"

settings:
  debug: false
  # 过期清理周期（ticks），默认 1200 = 60 秒
  expiration-cleanup-interval-ticks: 1200

storage:
  mode: sqlite          # sqlite 或 mysql
  pool-size: 4
  sqlite:
    file: "titles.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "ArcartX-Suite"
    username: "root"
    password: ""

ui:
  ui-id: "ArcartX-Suite:title_menu"
  register-ui-on-enable: true
  attribute-line-color: "&0"
  empty-attribute-placeholder: "-"

display-title:
  groups: []
  separator: " "
  empty-text: ""

# 分组定义（用户可自由增删）
groups:
  adventure:
    name: "冒险"
    sort-order: 0
  exploration:
    name: "探索"
    sort-order: 1

# 品质定义（用户可自由增删）
qualities:
  common:
    name: "普通"
    sort-order: 0
  legend:
    name: "传说"
    sort-order: 1

# 套装定义（用户可自由增删）
sets:
  starwalker_set:
    display-name: "星轨套装"
    required-titles:
      - starwalker
      - stargazer
    completion-threshold: 2
    bonus-attributes:
      luck: 5
    bonus-attribute-lines:
      - "星轨之力:10"

# 属性插件 source/modifier 前缀
attributeplus:
  source-prefix: "AXS_TITLE"
mythiclib:
  source-prefix: "AXS_TITLE"
craneattribute:
  source-prefix: "AXS_TITLE"
symphony:
  source-prefix: "AXS_TITLE"
```

> **提示**：`groups`、`qualities`、`titles`、`sets` 均为动态节点，用户增删不会被 `ConfigDiagnosticEngine` 判定为废弃。

