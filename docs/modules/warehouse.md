---
title: Warehouse 仓库银行插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Warehouse 仓库银行，个人仓库NBT分类、共享仓库、多货币银行、定期存款、二级密码，我的世界服务器仓库银行插件。
---

# Warehouse 仓库银行

::: tip 付费模块
本模块为付费模块。授权由 [云端平台](/guide/cloud-modules) 统一管理：在 [cloud.021209.xyz](https://cloud.021209.xyz) 购买/领取授权后，于「装备模块」页面勾选到对应服务器即可，无需填写 `password` 或 `license.yml`。
:::
**Warehouse** 模块提供完整的个人仓库、共享仓库与多货币银行系统，所有交互通过 ArcartX 客户端 UI 完成，支持 SQLite / MySQL 双存储后端。

## 功能概览

| 子系统 | 核心功能 |
|--------|----------|
| **个人仓库** | 多仓库、多等级容量体系、NBT 自动分类、搜索排序、黑名单过滤、自动拾取存入 |
| **共享仓库** | 创建/删除/转让、成员权限（所有者/成员/观众）、等级扩充、编辑锁互斥 |
| **银行系统** | 多货币活期存款、定期存款产品、阶梯利率、权限专属产品、原子领取 |
| **安全系统** | PBKDF2 二级密码、会话有效期、管理员密码重置 |
| **展示系统** | 仓库展示卡片/可点击消息、只读预览、冷却控制 |

## 操作指南

以下按 UI 界面分步说明每个功能的操作方式。所有交互均在 **ArcartX 客户端 UI** 中完成，无需输入命令。

---

### 1. 打开仓库

| 方式 | 操作 |
|------|------|
| 命令 | 输入 `/warehouse` 或 `/wh` |
| 命令别名 | `/wh open` |
| 管理员代开 | `/axs warehouse open <玩家名>` |

打开后默认进入**仓库存取主界面**（`warehouse_menu.yml`）。界面右上角有导航按钮可在「仓库 / 管理 / 银行」三个子界面间切换。

---

### 2. 仓库存取主界面

界面分为四个区域：
- **顶部**：仓库名称、管理/银行/刷新按钮、个人/共享仓库切换栏
- **左侧**：搜索框 + 分类筛选列表
- **中右**：仓库物品网格（9×6 = 54 格）
- **底部**：已选槽位信息、数量输入框、操作按钮、背包网格

#### 2.1 存入物品（从背包到仓库）

**方式一：双击快捷存入（推荐）**
1. 在界面右下角的**背包网格**中找到要存入的物品槽位
2. **双击鼠标左键**该槽位 → 该槽位的**全部物品**立即存入当前仓库
3. 若仓库已满或物品命中黑名单，会收到提示且物品保留在背包

**方式二：选择 + 数量存入**
1. 在背包网格中**单击**要存入的物品槽位 → 槽位会高亮（蓝色边框），表示已选中
2. 在底部「数量输入框」中输入要存入的数量（留空默认为 1）
3. 点击底部操作栏的**「存入」**按钮 → 按指定数量存入
4. 若输入数量大于实际堆叠数，按实际数量存入

**方式三：一键存入全部背包物品**
1. 点击背包网格上方的**「全部存入」**按钮
2. 系统会遍历背包主库存（9~44 槽），将可存入物品全部移入仓库
3. 命中黑名单或仓库已满的物品会自动跳过并提示

::: tip 存入规则
- 相同物品（NBT 哈希一致）会自动合并到同一格，单格上限由服务端配置决定
- 黑名单物品、只读仓库、未解锁二级密码时均无法存入
:::

#### 2.2 取出物品（从仓库到背包）

**方式一：双击快捷取出（推荐）**
1. 在仓库物品网格中找到要取出的物品
2. **双击鼠标左键**该物品 → 该物品的**全部数量**立即取出到背包
3. 若背包已满，会按 Minecraft 堆叠上限（64/16/1）分批给予，满后停止

**方式二：选择 + 数量取出**
1. 在仓库网格中**单击**要取出的物品 → 物品格会高亮（蓝色背景），表示已选中
2. 在底部「数量输入框」中输入要取出的数量（留空默认为 1）
3. 点击底部操作栏的**「取出」**按钮 → 按指定数量取出到背包

**方式三：全部取出**
1. 先单击选中仓库中的某物品
2. 点击底部操作栏的**「全部取出」**按钮 → 该格全部数量取出

::: warning 二级密码限制
若界面底部提示「提现和领取需要先解锁二级密码」，需先在密码框输入二级密码并点击「解锁」。解锁后 10 分钟内（可在配置中调整）无需再次输入。
:::

#### 2.3 搜索与分类筛选

**搜索物品**
1. 在左侧「搜索框」中输入关键词（支持物品名称、拼音、首字母）
2. 按 **Enter** 键或点击「搜索」按钮执行搜索
3. 仓库网格会只显示匹配结果；点击「重置」清空搜索恢复全览

**分类筛选**
1. 在左侧「分类列表」中点击目标分类（如「装备」「消耗品」「材料」）
2. 仓库网格会只显示该分类下的物品
3. 再次点击同一分类或「全部」可取消筛选

分类由服务端配置文件的 `categories` 节定义，默认按物品 NBT 标签（`pdc:ArcartX-Suite:item_type`）自动归类，未匹配到的归入「其它」。

#### 2.4 分页切换

仓库物品超过 54 格时会自动分页。
- 点击仓库网格下方的**「上一页」/「下一页」**按钮翻页
- 页码显示在按钮旁（如 `1/3`）

#### 2.5 切换仓库（个人 / 共享）

**个人仓库切换**
1. 在顶部「仓库切换栏」中左右滚动（鼠标滚轮）
2. 点击目标个人仓库按钮（如「个人 Lv.1」）→ 切换到该仓库

**共享仓库切换**
1. 在切换栏中滚动到共享仓库区域
2. 点击目标共享仓库按钮 → 首次进入时默认**只读模式**
3. 若你是该共享仓库的「所有者」或「成员」，底部会出现**「编辑」**按钮；点击后若无人占用编辑锁，即可进入编辑模式进行存取

::: warning 共享仓库编辑锁
同一共享仓库同一时间只允许一人编辑。若他人正在编辑，你打开时界面为只读，并提示「该共享仓库正在被 XXX 编辑」。
:::

#### 2.6 仓库升级

1. 切换到你拥有的个人仓库
2. 若底部提示栏显示升级信息（如「下一级容量 2500，消耗 5000 金币」）
3. 确保背包内有足够货币，点击底部「升级」按钮（若配置有升级消耗）
4. 升级成功后容量立即扩展，已存物品不受影响

---

### 3. 仓库管理界面

点击主界面右上角**「管理」**按钮进入（`warehouse_manage.yml`）。界面分为：自动拾取设置、仓库列表、删除/重命名/转让、成员管理。

#### 3.1 自动拾取设置

在「自动拾取」卡片中有三个开关：
- **自动入库**：拾取地面物品时自动存入个人仓库（默认开启）
- **怪物战利品**：击杀 MythicMobs 等怪物时掉落物品自动存入（默认开启）
- **入库通知**：自动存入后在屏幕左下角显示提示（默认开启）

点击对应按钮即可切换开关状态；绿色 = 开，灰色 = 关。

#### 3.2 创建共享仓库

1. 在左侧「仓库列表」上方输入框填写共享仓库名称
2. 点击**「创建」**按钮
3. 若配置有创建费用（如 10000 金币），需确保背包余额充足
4. 创建成功后自动出现在列表中，你成为「所有者」

::: tip 共享仓库数量上限
每个玩家可创建的共享仓库数量受权限等级限制（默认配置下普通玩家 1 个，VIP 玩家 3 个等）。
:::

#### 3.3 删除共享仓库

1. 在左侧列表中单击要删除的共享仓库 → 该仓库被选中
2. 中间「删除共享仓库」卡片中点击**「删除」**按钮
3. 界面会要求输入**二级密码**进行二次确认
4. 输入密码后再次点击确认 → 仓库删除（内部物品一并清除，不可恢复）

**只有所有者可以删除共享仓库。**

#### 3.4 重命名共享仓库

1. 在左侧列表中选中目标共享仓库
2. 在「重命名」卡片中输入新名称
3. 点击**「重命名」**按钮

#### 3.5 转让共享仓库

1. 在左侧列表中选中目标共享仓库
2. 在「转让」卡片中输入接收玩家的名字（该玩家必须是该仓库的**成员**）
3. 点击**「转让」**按钮 → 你降为成员，对方成为所有者

#### 3.6 成员管理

**邀请成员**
1. 选中左侧的共享仓库
2. 在「成员管理」卡片中输入玩家名
3. 选择角色：**成员**（可存取）或**观众**（仅查看）
4. 点击**「邀请」**按钮 → 对方立即获得权限

**修改角色 / 移除成员**
1. 选中共享仓库后，右侧会显示当前成员列表
2. 点击成员项：
   - 选择「设为成员」或「设为观众」可修改角色
   - 点击「移除」可将对方移出仓库

**成员上限**由权限等级决定（默认普通玩家 6 人，VIP 10 人）。

---

### 4. 银行界面

点击主界面右上角**「银行」**按钮进入（`warehouse_bank.yml`）。界面分为：活期余额、定期产品、我的定期。

#### 4.1 活期存款与提现

**存款**
1. 在「账户余额」卡片中点击要操作的货币（如「金币」「点券」）→ 选中项高亮
2. 在「金额」输入框中填写要存入的数额
3. 点击**「存入」**按钮 → 从玩家背包/账户扣除对应金额存入银行

**提现**
1. 选中目标货币，输入金额
2. 点击**「提现」**按钮 → 从银行余额转出到玩家账户

::: warning
提现需要先解锁二级密码。若未解锁，界面顶部会提示「提现和领取需要先解锁二级密码」。
:::

#### 4.2 购买定期存款

1. 在「定期产品」卡片中浏览可购买的产品列表（如「七日定期」）
2. 点击选中目标产品 → 项高亮
3. 在「金额」输入框中填写存入金额
4. 点击**「购买」**按钮
5. 系统扣除活期余额（若余额不足会提示），生成一笔定期记录

定期产品参数（利率、期限、最低金额等）由服务端配置文件的 `bank.deposit-products` 定义。

#### 4.3 领取到期定期

1. 在「我的定期」卡片中查看当前持有的所有定期记录
2. 已到期的记录显示为可领取状态（绿色或带提示）
3. 点击目标定期记录 → 自动计算本息并转入活期余额
4. 领取后该记录消失

::: tip 定期本息计算
本息 = 本金 × (1 + 利率)。利率按金额阶梯匹配（如 100~9999 为 1%，10000 以上为 2%），在配置文件的 `interest-tiers` 中定义。
:::

---

### 5. 二级密码（安全设置）

二级密码用于保护仓库取出、银行提现、共享仓库删除等敏感操作。

#### 5.1 首次设置

1. 首次打开仓库时，底部会显示密码输入框，提示「点击输入二级密码」
2. 输入 4~32 位密码
3. 点击**「设置」**按钮 → 密码设置成功

#### 5.2 解锁会话

1. 在底部密码框输入已设置的密码
2. 点击**「解锁」**按钮 → 会话有效期默认 10 分钟
3. 在有效期内进行取出、提现等操作无需再次输入

#### 5.3 手动锁定

点击底部**「锁定」**按钮可立即结束当前会话，再次操作敏感功能需重新输入密码。

#### 5.4 清除密码

在管理界面的「清除密码」卡片中输入当前密码并确认，即可移除二级密码（之后无需解锁即可操作）。

::: danger 密码存储安全
二级密码使用 PBKDF2WithHmacSHA256 + 120000 次迭代 + 随机 salt 存储，服务端不保存明文。管理员可通过 `/axs warehouse password <玩家> clear` 重置玩家密码。
:::

---

### 6. 仓库展示

向服务器其他玩家展示自己的仓库物品列表。

1. 确保仓库内有物品
2. 输入命令 `/wh showcase`
3. 系统会向公屏发送一条可点击消息（或聊天卡片，取决于配置）
4. 其他玩家点击该消息即可预览你的仓库（只读模式，无法操作）

展示有冷却时间（默认 60 秒），防止刷屏。

---

### 7. 预览他人仓库

1. 输入 `/wh preview <玩家UUID> [仓库ID]`
2. 或以只读模式打开对方的仓库展示链接
3. 界面与正常仓库相同，但所有操作按钮不可用
4. 可自由翻页、搜索、查看分类，但无法存取或修改

---

### 8. 跨服共享仓库（多子服场景）

若服务器采用 BungeeCord / Velocity 群组架构且配置了 MySQL + Redis：

- 各子服共享同一套仓库数据，在任何子服存入的物品，切服后可见
- 共享仓库编辑锁跨服同步：A 服玩家编辑时，B 服玩家打开同一仓库为只读
- 详见 [Warehouse 多服 MySQL 部署](/guide/warehouse-cross-server)

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信 |
| Vault / PlayerPoints | 可选 | 金币/点券货币支持 |
| PlaceholderAPI | 可选 | 自定义货币桥接 + PAPI 占位符 |
| MythicMobs / MythicBukkit | 可选 | 自动存入 Mythic 掉落、物品识别、黑名单 |
| NeigeItems / MMOItems | 可选 | 物品分类、黑名单、展示识别 |
| MySQL | 可选 | 多服共享仓库/银行数据（默认 SQLite） |

## 命令

### 玩家命令（别名 `/wh`）

| 命令 | 权限 | 说明 |
|------|------|------|
| `/warehouse` | `arcartxsuite.warehouse.use` | 打开仓库主界面 |
| `/wh open` | `arcartxsuite.warehouse.use` | 同上 |
| `/wh showcase` | `arcartxsuite.warehouse.showcase` | 向公屏展示自己的仓库 |
| `/wh preview <UUID> [仓库ID]` | `arcartxsuite.warehouse.use` | 以只读模式预览指定玩家的仓库 |

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs warehouse status` | `arcartxsuite.admin` | 查看模块状态（缓存玩家数、货币列表等） |
| `/axs warehouse reload` | `arcartxsuite.admin` | 提示重载（实际通过 `/axs config apply warehouse` 生效） |
| `/axs warehouse open <玩家>` | `arcartxsuite.admin` | 为在线玩家打开仓库界面 |
| `/axs warehouse info <玩家>` | `arcartxsuite.admin` | 查看玩家仓库概览（使用量、共享数、定期数） |
| `/axs warehouse password <玩家> clear` | `arcartxsuite.admin` | 清除玩家的二级密码 |
| `/axs warehouse bank <玩家> <货币ID> <set\|add\|take> <金额>` | `arcartxsuite.admin` | 管理玩家银行余额 |

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `arcartxsuite.warehouse.use` | true | 使用仓库基本功能 |
| `arcartxsuite.warehouse.showcase` | true | 使用 `/wh showcase` 展示仓库 |
| `arcartxsuite.admin` | op | 管理员命令 |

## PAPI 占位符

前缀：`%axswarehouse_*%`

| 占位符 | 说明 |
|--------|------|
| `%axswarehouse_total_items%` | 仓库物品总数（所有个人仓库合计） |
| `%axswarehouse_personal_used%` | 个人仓库已使用格子数 |
| `%axswarehouse_personal_capacity%` | 个人仓库总容量 |
| `%axswarehouse_shared_owned_count%` | 拥有的共享仓库数量 |
| `%axswarehouse_shared_joined_count%` | 加入的他人共享仓库数量（含拥有） |
| `%axswarehouse_category_<分类ID>_amount%` | 指定分类的物品数量 |
| `%axswarehouse_bank_balance_<货币ID>%` | 指定货币的银行活期余额 |
| `%axswarehouse_bank_fixed_active_<货币ID>%` | 活跃（未领取）定期存款笔数 |
| `%axswarehouse_bank_fixed_matured_<货币ID>%` | 已到期且未领取的定期存款笔数 |

## 配置文件

### 主配置 `ArcartXWarehouse.yml`

```yaml
# 通用设置
settings:
  debug: false                      # 调试日志开关
  flush-interval-ticks: 100         # 数据刷新间隔（tick）

# 跨服共享仓库编辑锁（多子服共用 MySQL 时建议开启；连接参数见宿主 config.yml cross-server 节）
cross-server:
  enabled: false

# UI 设置
ui:
  id: "ArcartX-Suite:warehouse_storage"         # 仓库 UI ID
  file: "arcartx/ui/warehouse_menu.yml"
  manage-id: "ArcartX-Suite:warehouse_manage" # 共享管理 UI ID
  manage-file: "arcartx/ui/warehouse_manage.yml"
  bank-id: "ArcartX-Suite:warehouse_bank"     # 银行 UI ID
  bank-file: "arcartx/ui/warehouse_bank.yml"
  packet-id: "AXS_WAREHOUSE"        # 客户端包 ID
  register-ui-on-enable: true         # 启动时自动注册 UI
  overwrite-ui-files: false         # 是否覆盖已存在的 UI 文件
  page-size: 54                     # UI 分页大小（默认 54 格）

# 存储设置
storage:
  mode: "sqlite"                    # sqlite / mysql
  sqlite:
    file: "warehouse.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "ArcartX-Suite"
    username: "root"
    password: ""
  pool-size: 4                      # 连接池大小（SQLite 建议 1，MySQL 建议 4+）

# 安全设置
security:
  min-length: 4                     # 二级密码最小长度
  max-length: 32                    # 二级密码最大长度
  unlock-session-ms: 600000         # 解锁会话有效期（毫秒，默认 10 分钟）
  allow-admin-password-reveal: false # 是否允许管理员通过 secret 解密密码
  admin-reveal-secret: ""           # AES-GCM 解密密钥（留空则禁止解密）

# 自动拾取设置
pickup:
  auto-store-on-pickup: true        # 拾取时自动存入仓库
  auto-store-mythic-loot: true      # 自动存入 MythicMobs 掉落
  notify-on-auto-store: true        # 自动存入后发送通知

# 搜索设置
search:
  page-size: 54                     # 搜索结果显示分页大小
  default-sort: "time"              # 默认排序方案 ID

# 仓库定义
warehouses:
  personal:
    display-name: "&6个人仓库"
    default-owned: true             # 新玩家是否默认拥有
    permission: ""                 # 查看/使用该仓库所需权限（空=无限制）
    default-level: 1
    levels:
      1:
        capacity: 1000              # 格子容量
        upgrade:                    # 升级消耗（null 或空=免费）
          currency: "money"
          amount: 0
      2:
        capacity: 2500
        upgrade:
          currency: "money"
          amount: 5000
      3:
        capacity: 6000
        upgrade:
          currency: "money"
          amount: 15000

# 物品分类定义
categories:
  equipment:
    display-name: "装备"
    priority: 10                    # 优先级越小越靠前
    nbt:
      path: "pdc:ArcartX-Suite:item_type"    # NBT 路径：material / display-name / custom-model-data / pdc:namespace:key
      values: ["weapon", "armor", "accessory"]
  consumable:
    display-name: "消耗品"
    priority: 20
    nbt:
      path: "pdc:ArcartX-Suite:item_type"
      values: ["consumable", "potion"]
  material:
    display-name: "材料"
    priority: 30
    nbt:
      path: "pdc:ArcartX-Suite:item_type"
      values: ["material"]
  other:
    display-name: "其它"
    priority: 9999
    default: true                   #  fallback 分类（未匹配到的物品归入此类）

# 银行货币定义
bank:
  currencies:
    money:
      enabled: true
      provider: "vault"             # vault / playerpoints / placeholder / command
      display-name: "金币"
      scale: 2                      # 小数精度
      balance-placeholder: ""       # provider=placeholder 时读取的 PAPI 变量
      withdraw-command: ""          # provider=command 时执行的扣款命令
      deposit-command: ""           # provider=command 时执行的放款命令
    points:
      enabled: true
      provider: "playerpoints"
      display-name: "点券"
      scale: 0

  # 定期存款产品
  deposit-products:
    seven_day:
      enabled: true
      display-name: "七日定期"
      description: "低风险稳健收益"
      currency: "money"
      duration-seconds: 604800      # 7 天
      min-amount: 100
      max-amount: 0                  # 0 = 无上限
      permission: ""                # 空 = 无权限限制
      interest-tiers:
        low:
          min: 100
          max: 9999
          rate: 0.01                # 1%
        high:
          min: 10000
          max: 0
          rate: 0.02                # 2%

# 共享仓库设置
shared:
  enabled: true
  create-cost:                      # 创建消耗（null = 免费）
    currency: "money"
    amount: 10000
  default-level: 1
  role-names:                       # 角色显示名称
    owner: "所有者"
    member: "成员"
    viewer: "观众"
  levels:                           # 共享仓库等级（语法与个人仓库相同）
    1:
      capacity: 1000
    2:
      capacity: 3000
      upgrade:
        currency: "money"
        amount: 20000
  permission-tiers:                 # 按权限节点分层限制
    default:
      permission: ""
      priority: 0
      max-owned: 1                  # 可创建的共享仓库数量上限
      max-members: 6                # 每个共享仓库的成员上限
    vip:
      permission: "arcartxsuite.vip"
      priority: 10
      max-owned: 3
      max-members: 10

### 跨服共享仓库与编辑锁

Warehouse **支持多子服共用同一 MySQL 库**实现共享仓库数据同步（`storage.mode: mysql` + 各子服指向同一 `database`）。SQLite 仅适合单服。

共享仓库进入**编辑模式**时采用互斥锁：同一时刻只允许一名成员编辑，其余成员以只读打开。单服时锁保存在本进程内存；多子服时可通过 CrossServer SDK 广播锁状态：

| 条件 | 说明 |
| --- | --- |
| `shared.enabled: true` | 启用共享仓库功能 |
| `storage.mode: mysql` | 各子服共用同一仓库库 |
| 宿主 `config.yml` → `cross-server` 已启用 | Redis 或 Proxy 通道可用 |
| `cross-server.enabled: true` | 模块级开关（`ArcartXWarehouse.yml`） |

**频道**：`warehouse`  
**Payload**：`LOCK\t{sharedId}\t{playerUuid}\t{playerName}\t{nodeId}` / `UNLOCK\t{sharedId}\t{playerUuid}\t{nodeId}`

行为摘要：

- 玩家在本服抢到编辑锁后，向其他子服广播 `LOCK`
- 他服若已有玩家正在编辑同一共享仓库，会被降级为只读并刷新 UI，提示「玩家名（nodeId）正在编辑」
- 关闭 UI、切换只读、退出服务器时在本服释放锁并广播 `UNLOCK`
- `/axs warehouse status` 可查看跨服通道与当前活跃锁数量

详见 [跨服架构](/architecture/cross-server) 与 [跨服部署速查](/guide/cross-server-setup)。**完整多子服 MySQL + 跨服锁部署清单**见 [Warehouse 多服 MySQL 部署](/guide/warehouse-cross-server)。

# 排序方案定义
sort-profiles:
  time:
    fields:
      - "updated:desc"             # 按更新时间降序
      - "name"
  name:
    fields:
      - "name"                     # 按名称升序
  amount:
    fields:
      - "amount:desc"              # 按数量降序
      - "updated:desc"

# 物品黑名单（禁止存入仓库的物品匹配规则）
blacklist:
  materials: []                     # 材质列表，如 ["BEDROCK", "COMMAND_BLOCK"]
  mythic-items: []                  # MythicMobs Internal Name 列表
  neige-items: []                   # NeigeItems ID 列表
  name-patterns: []                 # 物品显示名正则列表
  lore-patterns: []                 # Lore 正则列表

# 展示设置
showcase:
  enabled: true
  cooldown-seconds: 60              # 展示冷却（秒）
  max-items: 9                      # 展示列表最大物品数
  card-id: ""                      # 留空使用可点击聊天消息；填写则使用 ArcartX 聊天卡片 ID
  permission: "arcartxsuite.warehouse.showcase"
```

### 配置字段详解

#### 仓库等级字段

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `capacity` | Long | ✅ | — | 该等级的格子容量 |
| `upgrade.currency` | String | ❌ | — | 升级消耗的货币 ID |
| `upgrade.amount` | Decimal | ❌ | `0` | 升级消耗金额，`0` 或留空表示免费升级 |

#### 分类字段

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `display-name` | String | ✅ | — | UI 中显示的分类名称 |
| `priority` | Int | ❌ | `9999` | 排序优先级，数字越小越靠前 |
| `nbt.path` | String | ❌ | `""` | NBT 读取路径 |
| `nbt.values` | List | ❌ | `[]` | 匹配值列表 |
| `default` | Boolean | ❌ | `false` | 是否为 fallback 分类（未匹配物品归于此） |

**NBT 路径语法**：
- `material` — 匹配 Bukkit Material 名称
- `display-name` / `name` — 匹配物品显示名
- `custom-model-data` — 匹配 CustomModelData 整数值
- `pdc:namespace:key` — 读取 PersistentDataContainer 中的 STRING/INTEGER/LONG 值

#### 货币字段

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `provider` | String | ✅ | — | `vault` / `playerpoints` / `placeholder` / `command` |
| `display-name` | String | ❌ | 货币 ID | UI 显示名称 |
| `scale` | Int | ❌ | `0` | 小数位数 |
| `balance-placeholder` | String | ❌ | `""` | `provider=placeholder` 时使用的 `%xxx%` 变量（不含百分号） |
| `withdraw-command` | String | ❌ | `""` | `provider=command` 时执行的扣款命令，`{player}` `{amount}` 占位 |
| `deposit-command` | String | ❌ | `""` | `provider=command` 时执行的放款命令 |

#### 定期产品字段

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `currency` | String | ✅ | — | 关联的货币 ID |
| `duration-seconds` | Long | ❌ | `86400` | 定期时长（秒） |
| `min-amount` | Decimal | ❌ | `1` | 最低存入金额 |
| `max-amount` | Decimal | ❌ | `0` | 最高存入金额，`0` = 无上限 |
| `permission` | String | ❌ | `""` | 购买该产品所需权限 |
| `interest-tiers` | Map | ✅ | — | 利率阶梯，按 `min` 升序匹配，金额落入区间内适用对应 `rate` |

#### 利率阶梯字段

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `min` | Decimal | ❌ | `0` | 区间下限（含） |
| `max` | Decimal | ❌ | `0` | 区间上限（含），`0` = 无上限 |
| `rate` | Decimal | ✅ | — | 利率（如 `0.05` = 5%），到期本息 = 本金 × (1 + rate) |

#### 排序字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `fields` | `` `List<String>` `` | ✅ | 排序键列表，如 `name:desc` 表示按名称降序。可用键：`name` / `amount` / `updated` / `created` / `material` / `category` / `warehouse` |

#### 黑名单字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `materials` | `` `List<String>` `` | Bukkit Material 枚举名，匹配即禁止存入 |
| `mythic-items` | `` `List<String>` `` | MythicMobs Internal Name |
| `neige-items` | `` `List<String>` `` | NeigeItems ID |
| `name-patterns` | `` `List<String>` `` | 物品显示名正则表达式 |
| `lore-patterns` | `` `List<String>` `` | 物品 Lore 正则表达式 |

::: tip 黑名单优先级
自动存入时若物品命中任一黑名单规则，则跳过存入并向玩家提示。存入单件和存入全部均会检查黑名单。
:::

## UI 文件

模块自带三套 AXUI 定义文件，会在启用时自动导出到 `plugins/ArcartX-Suite/ui/`：

| 资源文件 | 导出路径 | 用途 |
|----------|----------|------|
| `arcartx/ui/warehouse_menu.yml` | `ui/warehouse_menu.yml` | 仓库存取主界面 |
| `arcartx/ui/warehouse_manage.yml` | `ui/warehouse_manage.yml` | 共享管理 + 设置界面 |
| `arcartx/ui/warehouse_bank.yml` | `ui/warehouse_bank.yml` | 银行活期/定期界面 |

可通过 `ui.file` / `ui.manage-file` / `ui.bank-file` 配置项指向自定义文件。

## 存储结构

自动创建以下数据表：

| 表名 | 用途 |
|------|------|
| `warehouse_personal` | 个人仓库列表（等级、自定义名称） |
| `warehouse_slots` | 物品槽位（个人/共享共用，owner_type + owner_id 区分） |
| `warehouse_bank_balances` | 玩家各货币活期余额 |
| `warehouse_fixed_deposits` | 定期存款记录 |
| `warehouse_shared` | 共享仓库基本信息 |
| `warehouse_shared_members` | 共享仓库成员关系与角色 |
| `warehouse_security` | 玩家二级密码（salt + PBKDF2 hash） |

## 客户端包协议

包 ID：`AXS_WAREHOUSE`

所有操作由客户端 UI 控件通过 `Packet.send("AXS_WAREHOUSE", action, ...)` 发起，服务端处理后回发更新包。

### 界面导航

| action | 参数 | 说明 |
|--------|------|------|
| `open` / `storage` | — | 打开仓库存取界面 |
| `manage` | — | 打开共享管理界面 |
| `bank` | — | 打开银行界面 |
| `refresh` | — | 刷新当前全部界面 |

### 仓库操作

| action | 参数 | 说明 |
|--------|------|------|
| `page` | `页码` | 切换分页 |
| `warehouse` | `仓库ID` | 切换个人仓库 |
| `shared` | `共享仓库ID` | 切换到指定共享仓库 |
| `shared_mode` | `readonly` / `edit` | 切换共享仓库只读/编辑模式 |
| `category` | `分类ID` | 切换物品分类筛选 |
| `search` | `关键词` | 设置搜索关键词（支持名称/拼音/首字母） |
| `select` | `显示槽位` | 选择仓库中的物品 |
| `deposit_slot` | `背包原始槽位`, `数量` | 存入指定背包槽位的物品；数量 `0` 表示全部存入 |
| `deposit_all_backpack` | — | 一键存入背包全部物品（跳过黑名单和已满格） |
| `withdraw` | `显示槽位`, `数量` | 取出指定数量的物品 |
| `withdraw_all` | `显示槽位` | 取出该槽位全部物品 |
| `warehouse_upgrade` | — | 扩充当前仓库到下一等级 |

### 银行操作

| action | 参数 | 说明 |
|--------|------|------|
| `bank_deposit` | `货币ID`, `金额` | 从玩家背包货币存入银行 |
| `bank_withdraw` | `货币ID`, `金额` | 从银行提现到玩家 |
| `fixed_create` | `产品ID`, `金额` | 购买定期存款产品 |
| `fixed_claim` | `存款ID` | 领取到期定期本息 |

### 共享仓库管理

| action | 参数 | 说明 |
|--------|------|------|
| `shared_create` | `名称` | 创建共享仓库 |
| `shared_rename` | `共享ID`, `新名称` | 重命名共享仓库（仅所有者） |
| `shared_delete` | `共享ID`, `二级密码` | 删除共享仓库（需密码确认） |
| `shared_invite` | `共享ID`, `玩家名`, `角色` | 邀请/修改成员角色（viewer/member） |
| `shared_remove` | `共享ID`, `玩家名` | 移除共享成员 |
| `shared_transfer` | `共享ID`, `目标玩家名` | 将共享仓库转让给成员（目标必须是 member 角色） |

### 安全与设置

| action | 参数 | 说明 |
|--------|------|------|
| `password_set` | `密码` | 设置二级密码 |
| `password_unlock` | `密码` | 解锁二级密码会话 |
| `password_lock` | — | 立即锁定会话 |
| `password_clear` | `密码` | 清除二级密码 |
| `toggle_auto_pickup` | — | 切换自动拾取存入 |
| `toggle_auto_mythic` | — | 切换 Mythic 掉落自动存入 |
| `toggle_auto_notify` | — | 切换自动存入通知 |

### 预览模式限制

预览模式下仅支持 `page`、`category`、`search`、`select`、`refresh`，其余操作会提示"预览模式下无法执行此操作"。

## 配置诊断

Warehouse 模块声明了以下配置校验规则：

| 字段 | 类型 | 约束 |
|------|------|------|
| `storage.mode` | STRING | 必填，枚举 `sqlite` / `mysql` |
| `storage.pool-size` | INT | 范围 1–100 |
| `settings.flush-interval-ticks` | INT | ≥ 1 |
| `security.min-length` | INT | 范围 1–32 |
| `security.max-length` | INT | 范围 4–64 |

动态节（用户可自由增删，不被结构同步覆盖）：
- `warehouses`
- `categories`
- `bank.currencies`
- `bank.deposit-products`
- `shared`
- `sort-profiles`

## 架构

```
WarehouseModule (AbstractAXSModule)
├── WarehouseService (门面，协调所有业务逻辑)
│   ├── 个人仓库：存取、升级、分类、搜索、排序
│   ├── 共享仓库：创建、成员、权限、编辑锁
│   ├── 银行：活期、定期、阶梯利率、原子领取
│   ├── 二级密码：PBKDF2 hash、会话管理
│   ├── 自动拾取：EntityPickupItemEvent 监听
│   └── 展示/预览：聊天卡片 / 可点击消息
├── JdbcWarehouseRepository (SQLite / MySQL 持久化)
├── WarehouseModuleConfiguration (配置聚合)
├── WarehouseAdminCommand (/axs warehouse)
├── WarehousePlayerCommand (/warehouse, /wh)
└── WarehousePlaceholderExpansion (PAPI)
```

## 安全说明

- 二级密码使用 **PBKDF2WithHmacSHA256**，120,000 次迭代，256 位输出，随机 16 字节 salt
- 密码验证通过 `MessageDigest.isEqual()` 进行恒定时间比较，防止时序攻击
- 若开启 `allow-admin-password-reveal`，管理员可使用 `admin-reveal-secret` 通过 AES/GCM 解密密码（用于密码找回场景）
- 共享仓库编辑采用**互斥锁**：同一时间只有一个成员可进入编辑模式，其他人以只读方式打开；多子服场景下通过 CrossServer SDK 同步锁状态（见上文「跨服共享仓库与编辑锁」）
- 所有银行扣款/存款操作使用**原子数据库操作**，失败自动回滚
- 定期存款领取为**原子操作**：`claimFixedDepositAtomic` 在数据库层面标记 claimed 并计算本息入账，避免并发重复领取

