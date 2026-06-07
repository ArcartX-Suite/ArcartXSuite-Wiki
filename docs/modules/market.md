# Market 全球市场

::: tip 付费模块
Market 为付费模块，需要有效授权码激活。
:::

**Market** 模块为服务器提供完整的经济交易系统，包含 **系统商店**、**玩家拍卖行** 和 **回收商店** 三大子系统，通过 ArcartX 客户端 UI 呈现，支持多货币、跨服同步。

## 功能概览

| 子系统 | 核心功能 |
|--------|----------|
| **拍卖行** | 一口价/竞价双模式、分类筛选、关键词搜索、收藏夹、交易税、到期退回、跨服 Redis 同步 |
| **系统商店** | YAML 配置商品、多物品来源（MythicMobs/NeigeItems/Overture/原版）、限购/折扣、权限分层 |
| **回收商店** | 回收表配置、批量一键回收、自动拾取回收、权限倍率 |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信 |
| MySQL | ✅ 必须 | 拍卖/交易/限购数据持久化 |
| Redis | 可选 | 拍卖列表缓存 + 跨服同步广播 |
| Vault / PlayerPoints | 可选 | 多货币支持（通过 CurrencyBridgeAPI） |
| PlaceholderAPI | 可选 | PAPI 占位符输出 |
| MythicMobs / NeigeItems | 可选 | 系统商店自定义物品来源 |

## 命令

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/market` | `arcartxsuite.market.use` | 打开拍卖行主界面 |
| `/market shop [商店ID]` | `arcartxsuite.market.use` | 打开系统商店 |
| `/market sell <一口价> [起拍价] [时长秒] [货币]` | `arcartxsuite.market.use` | 上架手持物品 |
| `/market recycle [all]` | `arcartxsuite.market.use` | 打开回收界面 / 一键回收 |
| `/market history` | `arcartxsuite.market.use` | 查看交易历史 |
| `/market my` | `arcartxsuite.market.use` | 查看我的上架 |
| `/market search <关键词>` | `arcartxsuite.market.use` | 搜索拍卖物品 |
| `/market cancel <ID>` | `arcartxsuite.market.use` | 取消上架 |

别名：`/mk`、`/ah`

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs market status` | `arcartxsuite.market.admin` | 查看模块状态 |
| `/axs market reload` | `arcartxsuite.market.admin` | 重载商店/回收配置 |
| `/axs market clear-expired` | `arcartxsuite.market.admin` | 手动处理到期物品 |
| `/axs market remove <ID>` | `arcartxsuite.market.admin` | 强制移除上架 |

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `arcartxsuite.market.use` | true | 使用市场基本功能 |
| `arcartxsuite.market.admin` | op | 管理员命令 |
| `axsmarket.autorecycle` | false | 自动回收拾取物品 |
| `axsmarket.tax.vip` | false | 税率折扣（配置定义） |

## PAPI 占位符

前缀：`%AXSmarket_xxx%`

| 占位符 | 说明 |
|--------|------|
| `%AXSmarket_auction_count%` | 当前活跃拍卖数量 |
| `%AXSmarket_shop_count%` | 已加载系统商店数量 |
| `%AXSmarket_recycle_count%` | 回收表条目数量 |
| `%AXSmarket_redis_status%` | Redis 连接状态 |
| `%AXSmarket_my_listings%` | 我的上架数量 |

## 配置文件

### 主配置 `ArcartXMarket.yml`

```yaml
# 核心配置段
settings:
  scheduler-interval-ticks: 1200    # 到期检查间隔

ui:
  packet-id: "AXS_MARKET"          # 客户端包 ID
  auction-id: "market_auction"      # 拍卖行 UI ID
  shop-id: "market_shop"            # 商店 UI ID
  recycle-id: "market_recycle"      # 回收 UI ID
  history-id: "market_history"      # 历史 UI ID
  overwrite-ui-files: false

storage:
  mode: "mysql"
  host: "localhost"
  port: 3306
  database: "arcartxsuite"
  username: "root"
  password: ""
  table-prefix: "axs_market_"
  pool-size: 10

redis:
  enabled: false
  host: "localhost"
  port: 6379
  password: ""
  database: 0
  channel: "axs:market:sync"
  cache-ttl-seconds: 300

auction:
  enabled: true
  max-listings-per-player: 20
  default-duration-seconds: 86400
  min-duration-seconds: 3600
  max-duration-seconds: 604800
  listing-fee: 0
  listing-fee-currency: "money"
  transaction-tax-rate: 0.05
  min-bid-increment-ratio: 0.05
  min-bid-increment-absolute: 1.0
  expired-return-method: "inventory"  # inventory / mail

shop:
  enabled: true
  shops-directory: "shops"
  default-currency: "money"
  refresh-interval-ticks: 72000

recycle:
  enabled: true
  recycle-directory: "recycle"
  default-currency: "money"
  allow-auto-recycle: true
```

### 商店配置 `shops/example_shop.yml`

```yaml
display-name: "&6示例商店"
icon: "CHEST"
permission: ""
tags: ["全部", "武器", "材料"]
items:
  diamond_sword:
    source: "minecraft"
    item-id: "DIAMOND_SWORD"
    display-name: "&b钻石剑"
    buy-price: 500
    sell-price: 100
    currency: "money"
    stock-mode: "unlimited"
    stock-amount: 0
    limit-per-player: 5
    limit-reset: "daily"
    discount:
      "vip.gold": 0.8
      "vip.diamond": 0.6
    conditions:
      permission: ""
      min-level: 0
```

#### 商店字段详解

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `display-name` | String | ✅ | — | 商店在 UI 列表中显示的名称，支持 `&` 颜色码 |
| `icon` | String | ❌ | `CHEST` | 商店列表图标材质（Bukkit Material 枚举名） |
| `permission` | String | ❌ | `''` | 查看此商店所需的权限节点，空字符串表示无限制 |
| `tags` | List<String> | ❌ | `[]` | 分类标签，用于 UI 筛选和搜索 |
| `items` | Map | ✅ | — | 商品定义映射，键为商品内部 ID |

#### 商品字段详解

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `source` | String | ✅ | — | 物品来源：`minecraft` / `mythic` / `neige` / `overture` / `mmoitems` |
| `item-id` | String | ✅ | — | 物品标识，格式取决于 `source` |
| `item-nbt` | String | ❌ | — | 原版物品 NBT 数据（JSON），仅 `minecraft` 有效 |
| `display-name` | String | ❌ | 物品原名 | 在商店 UI 中覆盖显示的自定义名称 |
| `buy-price` | Double | ❌ | `0` | 购买价格，`0` 表示不可购买 |
| `sell-price` | Double | ❌ | `0` | 出售价格，`0` 表示不可出售 |
| `currency` | String | ❌ | 商店默认货币 | 货币类型，如 `money`、`points` |
| `stock-mode` | String | ❌ | `unlimited` | 库存模式，见下表 |
| `stock-amount` | Int | ❌ | `0` | 库存数量，`unlimited` 模式下无效 |
| `limit-per-player` | Int | ❌ | `0` | 每人限购数量，`0` = 无限制 |
| `limit-reset` | String | ❌ | `never` | 限购重置周期：`daily` / `weekly` / `monthly` / `never` |
| `discount` | Map<String, Double> | ❌ | `{}` | 权限节点 → 折扣比例（见折扣计算） |
| `conditions.permission` | String | ❌ | `''` | 购买此商品所需权限 |
| `conditions.min-level` | Int | ❌ | `0` | 购买此商品所需最低等级 |

#### 库存模式说明

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| `unlimited` | 无限库存，可无限次购买 | 基础物资、常规商品 |
| `global` | 全服共享库存，所有玩家共用同一数量 | 限量稀有物品、活动商品 |
| `per-player` | 每个玩家独立库存，互不影响 | 个人专属商品、新手礼包 |

库存刷新逻辑：当 `refresh-interval-ticks` 到达时，系统会检查所有 `global` 和 `per-player` 模式的商品库存，按 `limit-reset` 规则重置已到期玩家的限购记录，并按配置补充 `stock-amount` 数量。

#### 限购系统

限购控制玩家在特定周期内最多可购买某商品的数量。`limit-reset` 重置时机：

- `daily` — 服务器时间每日 00:00 重置
- `weekly` — 服务器时间每周一 00:00 重置
- `monthly` — 服务器时间每月 1 日 00:00 重置
- `never` — 终身限购，不重置

::: tip 库存与限购的关系
`stock-amount` 控制商品**当前可购买数量**，`limit-per-player` 控制**每个玩家的购买上限**。两者独立工作：
- 即使库存充足，若玩家已达限购上限，仍不可购买
- 即使玩家未达限购上限，若库存为 0，仍显示"已售罄"
:::

#### 折扣计算

折扣配置为 `权限节点 → 折扣比例` 的映射。拥有对应权限的玩家购买时享受折扣，**取最低折扣值**（最优惠）。

计算公式：
```
实际支付 = buy-price * min(所有匹配权限的折扣比例)
```

**示例**：
```yaml
buy-price: 1000
discount:
  "axsmarket.vip": 0.9    # VIP 9 折
  "axsmarket.mvp": 0.8    # MVP 8 折
```
- 普通玩家：支付 `1000`
- VIP 玩家：支付 `1000 * 0.9 = 900`
- MVP 玩家（同时拥有 VIP 和 MVP）：支付 `1000 * 0.8 = 800`（取最低折扣）

#### 条件系统

`conditions` 定义商品上架条件，**所有条件必须同时满足**才显示在商店中：

- `permission` — 玩家必须拥有指定权限才能看到/购买此商品
- `min-level` — 玩家等级必须达到要求（依赖等级插件支持）

不满足条件的商品在 UI 中自动隐藏，不会显示为"灰色不可点击"状态。

### 回收配置 `recycle/default_recycle.yml`

```yaml
display-name: "&a默认回收表"
entries:
  cobblestone:
    source: "minecraft"
    item-id: "COBBLESTONE"
    price: 1
    currency: "money"
  iron_ingot:
    source: "minecraft"
    item-id: "IRON_INGOT"
    price: 10
    currency: "money"
```

#### 回收表字段详解

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `display-name` | String | ❌ | `''` | 回收表在 UI 中显示的名称 |
| `entries` | Map | ✅ | — | 回收条目映射，键为条目内部 ID |

#### 回收条目字段详解

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `source` | String | ✅ | — | 物品来源，与商店配置相同 |
| `item-id` | String | ✅ | — | 物品标识，格式取决于 `source` |
| `price` | Double | ✅ | — | 回收单价（每个物品获得的货币数量） |
| `currency` | String | ❌ | 默认货币 | 回收货币类型 |

#### 回收价格倍率

回收的最终价格受主配置中 `recycle.price-multiplier` 影响：

```
最终回收价 = price * max(匹配的 price-multiplier)
```

**示例**：
```yaml
# 回收表配置
entries:
  diamond:
    price: 100

# 主配置
recycle:
  price-multiplier:
    "axsmarket.recycle.vip": 1.2    # VIP +20%
    "axsmarket.recycle.mvp": 1.5      # MVP +50%
```
- 普通玩家回收钻石：`100 * 1.0 = 100`
- VIP 玩家回收钻石：`100 * 1.2 = 120`
- MVP 玩家回收钻石：`100 * 1.5 = 150`

#### 自动回收

开启 `allow-auto-recycle: true` 后，玩家拾取物品时自动检测背包中的可回收物品并出售：

- 需要玩家拥有 `axsmarket.autorecycle` 权限
- 仅在拾取事件触发时检查，不会持续扫描
- 出售数量等于拾取数量，不影响背包中已有的同类型物品
- 自动回收收益通过聊天消息提示玩家

#### 批量回收

在回收界面中，系统会扫描玩家整个背包，列出所有匹配的回收条目：

- **单件回收** — 点击单个条目，出售该物品栏位的全部匹配物品
- **一键回收** — 点击"全部回收"按钮，出售背包中所有可回收物品
- 若开启 `batch-confirm: true`，一键回收前会弹出确认窗口
- 不匹配的入侵物品不会出现在回收列表中

## 第三方物品库配置

系统商店支持多种物品来源，除原版 Minecraft 物品外，还支持通过第三方插件定义自定义物品。每种来源对应不同的插件与配置方式。

### 物品来源总览

| `source` 值 | 对应插件 | `item-id` 格式 | 说明 |
|-------------|---------|---------------|------|
| `minecraft` | 原版 | `DIAMOND_SWORD`、`POTION` 等 | 直接使用 Bukkit Material 名称 |
| `neige` | NeigeItems | 在 NeigeItems 中注册的 Item ID | 需在 NeigeItems 中预先配置物品 |
| `mythic` | MythicMobs | 在 MythicMobs 中注册的 Internal Name | 需在 MythicMobs Items 目录中配置 |
| `overture` | Overture | 在 Overture 中注册的物品 ID | 需在 Overture items 目录中配置 |
| `mmoitems` | MMOItems | `TYPE;ID` 格式，如 `ARMOR;STEEL_HELMET` | 需在 MMOItems 对应类型目录中配置 |

### NeigeItems 配置

**依赖插件**: [NeigeItems](https://github.com/ankhorg/NeigeItems-Kotlin)

**配置路径**: `plugins/NeigeItems/Items/NeigeFishRod.yml`

```yaml
NeigeFishRod:
  material: FISHING_ROD
  name: '&e神奇钓鱼竿'
  lore:
    - '&7一根被施了魔法的钓鱼竿，据说能钓到珍稀鱼类。'
    - ''
    - '&e✦ &7稀有度: &b罕见'
    - '&e✦ &7类别: &f工具'
    - '&e✦ &7等级要求: &f10'
    - ''
    - '&8&m------------&7 属性 &8&m------------'
    - '&7▸ 海之眷顾 &bIII'
    - '&7▸ 饵钓 &bIII'
    - '&7▸ 耐久 &bIII'
  nbt:
    Enchantments:
      - id: "minecraft:luck_of_the_sea"
        lvl: (Short) 3
      - id: "minecraft:lure"
        lvl: (Short) 3
      - id: "minecraft:unbreaking"
        lvl: (Short) 3
    HideFlags: (Int) 1
```

**商店引用**:

```yaml
neige_fishing_rod:
  source: "neige"
  item-id: "NeigeFishRod"
  display-name: "&e神奇钓鱼竿"
  buy-price: 3000
```

### MythicMobs 配置

**依赖插件**: [MythicMobs](https://www.mythicmobs.net/)

**配置路径**: `plugins/MythicMobs/Items/ExampleMythicSword.yml`

```yaml
ExampleMythicSword:
  Id: DIAMOND_SWORD
  Display: '&6传说之剑'
  Lore:
    - '&7一把散发着神秘光芒的古老长剑。'
    - ''
    - '&e✦ &7稀有度: &6传说'
    - '&e✦ &7类别: &f武器'
    - '&e✦ &7等级要求: &f30'
    - ''
    - '&8&m------------&7 属性 &8&m------------'
    - '&7▸ 伤害: &c+25'
    - '&7▸ 暴击率: &c+15%'
    - '&7▸ 攻击速度: &c+10%'
  Enchantments:
    - DAMAGE_ALL:5
    - FIRE_ASPECT:2
    - LOOT_BONUS_MOBS:3
    - DURABILITY:3
  Options:
    Unbreakable: true
    HideEnchants: false
```

::: warning 附魔名称
MythicMobs 5.x 使用 Bukkit `Enchantment` 枚举名，而非 Minecraft 原版英文名称。常见映射：`SHARPNESS` → `DAMAGE_ALL`、`LOOTING` → `LOOT_BONUS_MOBS`、`UNBREAKING` → `DURABILITY`。完整列表请参考 Bukkit API 文档。
:::

**商店引用**:

```yaml
mythic_weapon_example:
  source: "mythic"
  item-id: "ExampleMythicSword"
  display-name: "&6传说之剑"
  buy-price: 10000
```

### MMOItems 配置

**依赖插件**: [MMOItems](https://www.spigotmc.org/resources/mmoitems.39267/)

**配置路径**: `plugins/MMOItems/item/armor/STEEL_HELMET.yml`

```yaml
STEEL_HELMET:
  base:
    material: IRON_HELMET
  name: '&7钢盔'
  lore:
    - '&7由坚固的钢材锻造而成的头盔。'
    - ''
    - '&e✦ &7稀有度: &f普通'
    - '&e✦ &7类别: &f护甲'
    - '&e✦ &7等级要求: &f20'
    - ''
    - '&8&m------------&7 属性 &8&m------------'
    - '&7▸ 护甲值: &a+3'
    - '&7▸ 韧性: &a+1'
    - '&7▸ 耐久: &a150'
  enchants:
    PROTECTION_ENVIRONMENTAL: 2
    DURABILITY: 2
  item-type: ARMOR
  max-durability: 150
  bound: false
```

**商店引用**:

```yaml
mmoitems_steel_helmet:
  source: "mmoitems"
  item-id: "ARMOR;STEEL_HELMET"
  display-name: "&7钢盔"
  buy-price: 1200
```

### Overture 配置

**依赖插件**: [Overture](https://17artist.github.io/Overture/)（ArcartX 官方生态插件）

Overture 使用「物品定义 + 展示方案」分离的架构。物品文件定义数据和 Meta，展示方案模板定义最终渲染的名称与 Lore。

**物品配置路径**: `plugins/Overture/items/OvertureTreasure.yml`

```yaml
OvertureTreasure:
  display: treasure_display
  icon: CHEST
  name:
    item_name: "&d神秘宝藏"
  lore:
    item_type: "&5消耗品"
    item_desc:
      - "&7一个散发着紫光的神秘宝箱。"
      - "&7右键打开它，或许能获得意想不到的惊喜。"
      - ""
      - "&e✦ &7稀有度: &d史诗"
      - "&e✦ &7类别: &f消耗品"
      - "&e✦ &7来源: &fOverture"
  meta:
    shiny: true
  data:
    treasure_type: "legendary"
    open_limit: 1
```

**展示方案配置路径**: `plugins/Overture/displays/treasure_display.yml`

```yaml
treasure_display:
  name: "<item_name>"
  lore:
    - "<item_type>"
    - ""
    - "<item_desc...>"
```

::: tip 展示方案
`name` 与 `lore` 中可使用 `<key>` 占位符引用物品定义中 `name.*` 和 `lore.*` 字段。`<item_desc...>` 会展开列表中的所有行。
:::

**商店引用**:

```yaml
overture_treasure_box:
  source: "overture"
  item-id: "OvertureTreasure"
  display-name: "&d神秘宝藏"
  buy-price: 5000
```

## 存储结构

Market 使用 MySQL 存储，自动创建以下表：

| 表名 | 用途 |
|------|------|
| `axs_market_listings` | 拍卖上架物品 |
| `axs_market_bids` | 竞价记录 |
| `axs_market_history` | 交易历史 |
| `axs_market_favorites` | 玩家收藏 |
| `axs_market_shop_limits` | 商店限购记录 |
| `axs_market_recycle_stats` | 回收统计 |

## 客户端包协议

包 ID：`AXS_MARKET`

| action | 参数 | 说明 |
|--------|------|------|
| `auction_list` | page, category?, keyword? | 请求拍卖列表 |
| `auction_buy` | listingId | 一口价购买 |
| `auction_bid` | listingId, amount | 竞价 |
| `auction_cancel` | listingId | 取消上架 |
| `auction_favorite` | listingId | 切换收藏 |
| `shop_buy` | shopId, itemId, amount | 系统商店购买 |
| `recycle_all` | — | 一键回收 |
| `recycle_single` | slot | 回收指定槽位物品 |

## 配置诊断

Market 模块声明了以下配置校验规则：

| 字段 | 类型 | 约束 |
|------|------|------|
| `storage.mode` | STRING | 必填，枚举 `mysql` |
| `storage.pool-size` | INT | 范围 1–100 |
| `auction.max-listings-per-player` | INT | 范围 1–1000 |
| `auction.min-bid-increment-ratio` | DOUBLE | 范围 0.01–1.0 |
| `auction.transaction-tax-rate` | DOUBLE | 范围 0.0–0.99 |
| `auction.min-duration-seconds` | INT | ≥ 60 |
| `auction.max-duration-seconds` | INT | ≥ 3600 |
| `redis.cache-ttl-seconds` | INT | 范围 10–86400 |

动态节（用户可自由增删，不被结构同步覆盖）：
- `auction.categories`
- `auction.blacklist`
- `auction.tax-discount`
- `recycle.price-multiplier`
- `messages`

## 跨服同步

启用 Redis 后，Market 通过 Pub/Sub 频道 `axs:market:sync` 广播以下事件：

| 消息格式 | 触发时机 |
|----------|----------|
| `LISTING_CREATED:<id>` | 新物品上架 |
| `LISTING_SOLD:<id>` | 一口价成交 |
| `LISTING_CANCELLED:<id>` | 上架取消 |
| `BID_PLACED:<id>:<amount>` | 新竞价 |

所有服务器接收到消息后自动清除本地拍卖列表缓存，保证数据一致性。

## 架构

```
MarketModule (AbstractAXSModule)
├── MarketService (门面，协调三个子服务)
│   ├── AuctionService (拍卖行核心逻辑)
│   ├── ShopService (系统商店)
│   └── RecycleService (回收商店)
├── JdbcMarketRepository (MySQL 持久化)
├── RedisMarketCache (缓存 + Pub/Sub)
├── MarketPlayerCommand (/market)
├── MarketAdminCommand (/axs market)
├── MarketPlaceholderExpansion (PAPI)
└── MarketEventListener (自动回收监听)
```
