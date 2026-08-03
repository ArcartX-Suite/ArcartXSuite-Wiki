---
title: ExtraBackpack 额外背包插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite ExtraBackpack 额外槽位背包，多分类页自动分类、付费扩容、二级密码销毁、拾取自动存入，与原版背包共存的我的世界服务器背包扩展插件。
---

# ExtraBackpack 额外背包

**ExtraBackpack** 模块为服务器提供一套 **多分类额外槽位背包系统**，与玩家原版背包共存。启用后，玩家打开原版物品栏界面时，ArcartX UI 会叠加显示额外背包面板，包含多个分类页（装备/材料/杂物等），每页独立配置初始槽位数、上限和付费扩容价格。物品可根据 NBT/PDC 标签或 lore 自动分类，支持拾取自动存入、快捷栏一键存入、付费购买槽位和二级密码销毁物品。

## 功能概览

| 功能 | 说明 |
|------|------|
| **多分类页** | 自定义分类（装备/材料/杂物…），每页独立容量与扩容价格 |
| **自动分类** | 根据 NBT/PDC 标签、lore、材质、显示名等规则自动将物品分到对应分类页 |
| **原版背包共存** | UI 叠加在原版物品栏界面上，额外背包与原版 36 格背包同时可用 |
| **拾取自动存入** | 玩家拾取物品时自动分类存入额外背包，原版背包满时优先存入额外页 |
| **付费扩容** | 每个分类页可配置初始槽位数、上限和每槽价格，玩家可在 UI 中付费购买 |
| **快捷栏存入** | UI 底部显示快捷栏 9 格，点击即可将快捷栏物品存入额外背包 |
| **原版背包操作** | UI 内可查看原版背包物品信息、存入额外背包或通过二级密码销毁 |
| **二级密码销毁** | 销毁原版背包物品需验证二级密码，支持设置/解锁/清除二级密码 |
| **跨模块联动** | 注册 `ExtraBackpackAccess` 能力，Warehouse 等模块可自动存入物品 |
| **数据持久化** | SQLite 或 MySQL 存储，玩家上线加载、下线保存，支持数据迁移与清除 |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信 |
| Warehouse | 可选 | Warehouse 模块通过 `ExtraBackpackAccess` 能力自动存入物品到额外背包 |

## 启用步骤

```yaml
# config.yml
modules:
  extrabackpack:
    enabled: true
```

模块默认功能关闭，需在 `ArcartXExtraBackpack.yml` 中设置 `extra-backpack.enabled: true` 才会启用额外背包功能。

## 命令

本模块**无独立命令**。额外背包界面通过打开原版物品栏（E 键）自动叠加显示，所有操作均通过 ArcartX UI 客户端发包完成。

## 权限

本模块**无独立权限节点**。所有操作通过 UI 内交互完成，敏感操作（销毁物品、购买槽位）通过操作令牌（action token）和二级密码保护。

## PlaceholderAPI 占位符

本模块**无 PlaceholderAPI 占位符输出**。

## 配置结构

### ArcartXExtraBackpack.yml

```yaml
settings:
  debug: false

ui:
  register-ui-on-enable: true   # 启用时自动注册 UI
  overwrite-ui-files: false     # 是否覆盖用户已修改的 UI 文件

storage:
  mode: "sqlite"                # sqlite / mysql
  sqlite:
    file: "extrabackpack.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "arcartxsuite"
    username: "root"
    password: ""
  pool-size: 4                  # HikariCP 连接池最大连接数

extra-backpack:
  enabled: false                # 是否启用额外背包功能
  categories:
    equipment:
      display-name: "装备"
      priority: 10              # 分类排序优先级（数字越小越靠前）
      slots:
        initial: 9              # 初始槽位数
        max: 27                 # 最大槽位数
      price:
        currency: "points"      # 扩容使用的货币 ID
        per-slot: 100           # 每个槽位的价格
      match:
        nbt:
          path: "pdc:arcartx:item_category"   # 匹配路径
          values: ["equipment", "weapon", "armor"]  # 匹配值列表
    material:
      display-name: "材料"
      priority: 20
      slots:
        initial: 9
        max: 27
      price:
        currency: "points"
        per-slot: 80
      match:
        nbt:
          path: "pdc:arcartx:item_category"
          values: ["material"]
    other:
      display-name: "杂物"
      priority: 9999
      default: true             # 兜底分类：不匹配其他分类的物品存入此页
      slots:
        initial: 9
        max: 27
      price:
        currency: "points"
        per-slot: 60
```

### 分类页字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| 键名 | string | 分类 ID（自动转为小写），用于内部标识 |
| `display-name` | string | UI 中显示的分类名称 |
| `priority` | int | 排序优先级，数字越小越靠前（默认按配置顺序） |
| `default` | boolean | 是否为兜底分类；不匹配其他分类的物品会存入此页 |
| `slots.initial` | int | 初始可用槽位数 |
| `slots.max` | int | 最大槽位上限（扩容不可超过此值） |
| `price.currency` | string | 购买槽位使用的货币 ID |
| `price.per-slot` | number | 每个槽位的价格；未配置则该分类不可购买 |
| `match.nbt.path` | string | 物品匹配路径（见下方说明） |
| `match.nbt.values` | list | 匹配值列表，物品路径值在此列表中即匹配该分类 |

### 物品匹配路径（`match.nbt.path`）

分类匹配支持以下路径类型：

| 路径 | 说明 | 示例 |
|------|------|------|
| `pdc:<namespace>:<key>` | 读取物品 PDC（PersistentDataContainer）字符串值 | `pdc:arcartx:item_category` |
| `pdc:<key>` | 读取 PDC 值，namespace 默认为插件名 | `pdc:item_category` |
| `material` | 读取物品材质名（通过 VanillaItemNameBridge 翻译） | 返回 `diamond_sword` 等 |
| `display-name` / `name` | 读取物品显示名 | 含自定义名称的物品 |
| `custom-model-data` | 读取 CustomModelData 值 | 返回数字字符串 |
| `lore` | 匹配 lore 行（包含判断，忽略大小写） | 逐行检查是否包含 values 中的值 |

::: tip 匹配逻辑
分类按 `priority` 排序后依次匹配。物品首先尝试匹配非兜底分类（`default: true` 或无 match 规则的分类跳过）；如果都不匹配，则存入兜底分类。如果兜底分类也满了，剩余物品退回原版背包。
:::

## UI 界面说明

额外背包 UI 叠加在原版物品栏界面上（`match: 物品栏界面`），包含以下区域：

### 分类标签栏

顶部显示所有分类页标签，点击切换当前查看的分类。第一个标签固定为「背包」（原版 36 格背包视图），后续为自定义分类页。

### 背包网格

显示当前分类页的槽位物品。点击物品可取出（左键取出 1 个，右键取出全部）。在原版背包标签页下，左键查看物品信息，右键打开物品操作菜单。

### 快捷栏区域

底部显示玩家快捷栏 0-8 格内容，点击即可将对应快捷栏物品存入额外背包。

### 购买槽位卡片

显示当前分类页的扩容信息（每槽价格、剩余可购买数）。输入数量后点击「购买槽位」按钮即可付费扩容。

### 物品操作菜单（原版背包页）

在原版背包标签页中右键物品，弹出操作菜单：
- **存入** — 将该物品存入额外背包（自动分类）
- **销毁** — 销毁该物品（需二级密码验证）

### 二级密码面板

销毁物品时需要二级密码验证。面板支持三种模式：
- **设置** — 首次使用需设置二级密码（需输入当前密码（如有）和新密码）
- **解锁** — 输入当前密码解锁销毁权限
- **清除** — 输入当前密码清除二级密码

## 客户端包协议

模块监听 packet ID 为 `AXS_BACKPACK` 的客户端包，通过 `Packet.send(...)` 触发操作：

| 动作 | 参数 | 说明 |
|------|------|------|
| `open` / `refresh` | 无 | 打开/刷新额外背包界面 |
| `category` | `分类ID` | 切换到指定分类页 |
| `withdraw` | `槽位号` | 从当前分类页取出 1 个物品 |
| `withdraw_all` | `槽位号` | 从当前分类页取出全部物品 |
| `store` | `快捷栏槽位号` | 将快捷栏物品存入额外背包 |
| `buy_slots` | `数量`, `actionToken` | 购买指定数量的槽位 |
| `vanilla_info` | `原版槽位号` | 查看原版背包物品信息 |
| `vanilla_options` | `原版槽位号` | 打开原版背包物品操作菜单 |
| `vanilla_store` | `原版槽位号` | 将原版背包物品存入额外背包 |
| `vanilla_destroy` | `原版槽位号`, `true`, `actionToken` | 销毁原版背包物品（需二级密码） |
| `password_set` | `当前密码`, `新密码`, `actionToken` | 设置二级密码 |
| `password_unlock` | `当前密码` | 解锁二级密码 |
| `password_clear` | `当前密码`, `actionToken` | 清除二级密码 |
| `password_panel_close` | 无 | 关闭二级密码面板 |
| `close` | 无 | 关闭额外背包界面 |

::: tip 操作令牌（action token）
`buy_slots`、`vanilla_destroy`、`password_set`、`password_clear` 等敏感操作需要携带 action token。token 在打开 UI 时生成，每次敏感操作完成后自动刷新，防止重放攻击。
:::

## 拾取自动存入

启用后，玩家拾取地面物品时会自动分类存入额外背包：

1. 根据物品的 NBT/PDC/lore 匹配对应分类页
2. 匹配成功且分类页有空间 → 物品存入额外背包，取消原版拾取
3. 分类页空间不足 → 部分存入，剩余物品以更新后的数量留在地面
4. 不匹配任何分类 → 正常拾取到原版背包

## 跨模块联动

### ExtraBackpackAccess 能力

本模块注册 `ExtraBackpackAccess` 能力，其他模块可通过 `getCapability(ExtraBackpackAccess.class)` 获取实例，无需打开 UI 即可操作额外背包：

| 方法 | 说明 |
|------|------|
| `isAvailable(Player)` | 额外背包是否对该玩家可用 |
| `snapshot(Player)` | 获取所有分类页的容量、已用数和物品快照 |
| `deposit(Player, ItemStack)` | 将物品存入匹配的分类页，返回存入数量和剩余数量 |
| `withdraw(Player, categoryId, slot, amount)` | 从指定分类页槽位取出物品 |

### Warehouse 模块联动

Warehouse 模块通过 `ExtraBackpackAccess` 能力实现自动存入：当仓库自动整理时，可将物品优先存入额外背包的对应分类页。

### SecondaryPasswordAccess 能力

本模块在销毁物品时使用宿主提供的 `SecondaryPasswordAccess` 能力进行二级密码验证。二级密码由 ArcartX-Suite 本体统一管理（PBKDF2 哈希，SQLite/MySQL 存储）。

### PlayerDataPurgeable 能力

注册 `PlayerDataPurgeable` 能力，支持宿主统一清除指定玩家的额外背包数据。

### DatabaseMigratable 能力

注册 `DatabaseMigratable` 能力，支持宿主统一迁移额外背包数据库（SQLite ↔ MySQL）。

## 数据存储

| 存储项 | 说明 |
|--------|------|
| `extra_backpack_slots` | 每个玩家各分类页的物品数据（Base64 序列化的 ItemStack） |
| `extra_backpack_capacity` | 每个玩家各分类页的当前容量（扩容后更新） |

- **SQLite**：默认模式，数据文件位于 `data/extrabackpack/extrabackpack.db`
- **MySQL**：配置 `storage.mode: mysql` 后使用远程数据库
- 玩家上线时从数据库加载物品到内存，下线时持久化保存
- UI 关闭时也会触发保存

## 消息配置

消息文件位于 `data/extrabackpack/messages.yml`，支持 `&` 颜色码和 `{0}` `{1}` 占位符：

| 消息键 | 说明 |
|--------|------|
| `extra-backpack.disabled` | 功能未启用提示 |
| `extra-backpack.page-full` | 分类页已满 |
| `extra-backpack.buy-success` | 购买槽位成功（{0}=数量, {1}=当前容量） |
| `extra-backpack.buy-hint` | 购买提示（{0}=单价, {1}=剩余可购买数） |
| `extra-backpack.buy-maxed` | 槽位已达上限 |
| `extra-backpack.destroy-confirm` | 销毁确认提示 |
| `extra-backpack.destroy-success` | 销毁成功 |
| `extra-backpack.password-set-success` | 二级密码设置成功 |
| `extra-backpack.password-unlocked` | 二级密码解锁成功 |
| `extra-backpack.password-wrong` | 二级密码错误 |
| `extra-backpack.password-cleared` | 二级密码已清除 |
| `extra-backpack.action-token-invalid` | 操作令牌无效 |
| `extra-backpack.item-changed` | 物品已变化 |
| `extra-backpack.no-available-slot` | 无可用额外槽位 |
