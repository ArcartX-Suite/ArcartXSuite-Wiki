# Warehouse 仓库银行

## 功能定位

个人仓库（物品存取）+ 多货币银行（活期/定期存款）。

### 核心特性

**个人仓库：**
- **大容量存储**：多等级容量体系（如 1000 → 2500 → 6000 格），升级消耗自定义货币
- **NBT 分类**：按物品 PDC/NBT 标签自动分类（装备、材料、消耗品等），每个分类有独立显示名和优先级
- **搜索与排序**：支持按时间、名称、数量排序，内置搜索功能
- **自动存入**：拾取物品时自动存入仓库（可选），支持 MythicMobs 掉落物品
- **物品黑名单**：按材质、MythicMobs ID、NeigeItems ID、物品名/Lore 关键词或正则过滤

**共享仓库：**
- **创建与管理**：玩家可花费货币创建共享仓库，设置成员权限（所有者/成员/观众）
- **等级升级**：共享仓库也有多等级容量体系
- **权限分层**：按权限节点限制玩家可拥有和可加入的共享仓库数量

**银行系统：**
- **多货币支持**：Vault 金币、PlayerPoints 点券、自定义货币（通过 PAPI + 命令桥接）
- **定期存款产品**：配置多种定期产品，每种有独立的期限、门槛、权限和阶梯利率
- **阶梯利率**：存款金额越高利率越高（如 100~9999 → 1%、10000+ → 2%）
- **权限专属产品**：部分定期产品可要求特定权限才能购买

**展示与预览：**
- **仓库展示**：玩家可通过 `/wh showcase` 向公屏展示自己的仓库，支持 ArcartX 聊天卡片或可点击消息
- **只读预览**：其他玩家点击展示消息后以只读模式打开仓库，不能切换仓库或存取物品
- **展示冷却**：防止刷屏，可配置展示冷却时间

**安全与 UI：**
- **二级密码**：仓库可设置独立密码，会话有效期可配置
- **AXUI 全流程**：存取、共享管理、银行操作全部在 ArcartX UI 内完成
- **数据持久化**：SQLite 或 MySQL

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 仓库、共享管理、银行 UI 和虚拟物品槽 | 模块无法提供可视化仓库 |
| 可选 | Vault | 仓库升级、共享仓库、银行等金币货币 | Vault 货币不可用，可改用其他货币/命令桥接 |
| 可选 | PlayerPoints | 点券货币、升级或银行产品 | PlayerPoints 货币不可用 |
| 可选 | PlaceholderAPI | 自定义货币余额读取、条件文本变量 | PAPI 货币/变量不可用 |
| 可选 | MythicMobs / MythicBukkit | 自动存入 MythicMob 掉落、MythicItems 识别和黑名单 | 原版物品正常；Mythic 专属识别不可用 |
| 可选 | NeigeItems / MMOItems | 物品分类、黑名单和展示时识别专属物品 | 对应物品库信息可能按普通 ItemStack 处理 |
| 可选 | MySQL 服务 | 多服共享仓库/银行数据 | 默认 SQLite 可用；多服共享建议改 MySQL |

## 启用步骤

```yaml
modules:
  warehouse:
    enabled: true
```

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs warehouse status` | 查看仓库模块、数据库和配置状态 |
| `/axs warehouse reload` | 重载仓库配置 |
| `/axs warehouse open <玩家>` | 为在线玩家打开仓库界面 |
| `/axs warehouse info <玩家>` | 查看玩家仓库概览（使用量、共享仓库数等） |
| `/axs warehouse password <玩家> clear` | 清除玩家的二级密码（玩家忘记密码时使用） |
| `/axs warehouse bank <玩家> <货币ID> <set\|add\|take> <金额>` | 管理玩家银行余额。`set` 设定、`add` 增加、`take` 扣除 |

### 玩家命令（权限：`arcartxsuite.warehouse.use`，别名 `/wh`）

| 命令 | 说明 |
| --- | --- |
| `/warehouse` 或 `/wh` | 打开仓库界面，所有操作（存/取/共享/银行）在 AXUI 中完成 |
| `/wh showcase` | 向公屏展示自己的仓库（权限：`arcartxsuite.warehouse.showcase`） |
| `/wh preview <UUID>` | 以只读模式预览指定玩家的仓库 |

## PAPI

前缀：`%AXSwarehouse_*%`

| 占位符 | 说明 |
| --- | --- |
| `%AXSwarehouse_total_items%` | 仓库物品总数 |
| `%AXSwarehouse_personal_used%` | 个人仓库已使用格子数 |
| `%AXSwarehouse_personal_capacity%` | 个人仓库总容量 |
| `%AXSwarehouse_shared_owned_count%` | 拥有的共享仓库数量 |
| `%AXSwarehouse_shared_joined_count%` | 加入的他人共享仓库数量 |
| `%AXSwarehouse_category_<分类ID>_amount%` | 指定分类的物品数量 |
| `%AXSwarehouse_bank_balance_<货币ID>%` | 指定货币的银行余额 |
| `%AXSwarehouse_bank_fixed_active_<货币ID>%` | 活跃定期存款笔数 |
| `%AXSwarehouse_bank_fixed_matured_<货币ID>%` | 已到期定期存款笔数 |
