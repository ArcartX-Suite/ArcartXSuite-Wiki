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
  - **展示属性**：仅在装备该称号时生效
  - **收藏属性**：只要拥有且未过期即累计（收集图鉴式）
  - **原生属性行**：直接传给 AttributePlus / CraneAttribute，支持百分比和中文属性名
- **三大属性插件**：同时支持 AttributePlus、MythicLib/MMOItems、CraneAttribute，各有独立开关和前缀
- **UI 菜单**：ArcartX UI 驱动的称号管理界面，玩家可浏览、装备、卸下、隐藏称号
- **PAPI 全量输出**：拥有数量、装备信息、剩余时间、属性加成等全部通过 PlaceholderAPI 输出
- **数据持久化**：SQLite 或 MySQL，带连接池

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 称号管理 UI、头顶显示和客户端图标文本 | 模块无法提供可视化称号界面 |
| 可选 | PlaceholderAPI | 输出 `%AXStitle_*%`，供聊天、TAB、计分板读取 | 称号 UI 仍可用，PAPI 输出不可用 |
| 可选 | AttributePlus | 原生属性行和属性插件加成 | AttributePlus 属性不生效 |
| 可选 | CraneAttribute | CraneAttribute 属性加成 | CraneAttribute 属性不生效 |
| 可选 | MythicLib / MMOItems | MythicLib stat modifier 和 MMOItems 生态属性 | MythicLib/MMOItems 属性不生效 |
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
