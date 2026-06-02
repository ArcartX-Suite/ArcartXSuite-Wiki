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
    limit-per-player: 5
    limit-reset: "daily"
    discount:
      "vip.gold": 0.8
      "vip.diamond": 0.6
```

### 回收配置 `recycle/default_recycle.yml`

```yaml
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
