---
title: Warehouse 多子服 MySQL + 跨服编辑锁部署清单 | ArcartX-Suite Minecraft插件文档
description: Warehouse 多子服 MySQL + 跨服编辑锁部署清单 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# Warehouse 多子服 MySQL + 跨服编辑锁部署清单

本页面向**运维**，说明如何在 BungeeCord / Velocity 群组下，让多台子服共用同一套仓库 MySQL 数据，并启用**共享仓库跨服编辑锁**（避免 A 服与 B 服同时编辑同一共享仓）。

架构细节见 [跨服通信（CrossServer SDK）](/architecture/cross-server)；模块功能见 [Warehouse 仓库银行](/modules/warehouse)。

## 目标架构

```
                    ┌─────────────────────────────────┐
                    │  MySQL（ArcartX-Suite_warehouse） │
                    │  个人仓 / 共享仓 / 银行 / 槽位    │
                    └───────────────┬─────────────────┘
                                    │ JDBC（各子服相同连接参数）
        ┌───────────────────────────┼───────────────────────────┐
        ▼                           ▼                           ▼
  子服 survival-1              子服 survival-2              子服 lobby
  node-id: survival-1          node-id: survival-2          node-id: lobby
        │                           │                           │
        └───────────────────────────┴───────────────────────────┘
                                    │
                    Redis Pub/Sub  channel: ArcartX-Suite:CROSS
                    （或 BungeeCord Forward，生产推荐 Redis）
                                    │
              warehouse 频道：LOCK / UNLOCK 编辑锁同步
```

| 能力 | 依赖 |
| --- | --- |
| **仓库数据跨服一致** | 各子服 `storage.mode: mysql` 且指向**同一 database** |
| **共享仓编辑互斥（跨服）** | 上表 + 宿主 `cross-server` + `ArcartXWarehouse.yml` → `cross-server.enabled: true` + `shared.enabled: true` |

::: warning SQLite 不能多服共享
`storage.mode: sqlite` 时每个子服各自一份 `warehouse.db`，**不存在**跨服共享仓库。多子服场景必须使用 MySQL。
:::

---

## 部署前检查清单

- [ ] 已安装 **ArcartX** 客户端与 **ArcartX-Suite**（含 Warehouse 授权）
- [ ] 群组代理（BungeeCord / Velocity）与子服网络互通
- [ ] 独立 **MySQL 8.0+**（或 5.7+）实例，子服均可访问
- [ ] 独立 **Redis**（推荐；跨服锁消息量小，1 核 512MB 足够）
- [ ] 各子服 `plugins/ArcartX-Suite/config.yml` 中 **`node-id` 互不相同**
- [ ] 各子服 **`signature.secret` 完全一致**（若启用验签）
- [ ] Warehouse 模块 `modules.warehouse.enabled: true`

---

## 第一步：MySQL 建库与账号

在 MySQL 上执行（可按实际环境修改库名/密码）：

```sql
CREATE DATABASE ArcartX-Suite_warehouse
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'axs_wh'@'%' IDENTIFIED BY '请替换为强密码';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP
  ON ArcartX-Suite_warehouse.* TO 'axs_wh'@'%';
FLUSH PRIVILEGES;
```

::: tip 表结构
首次启动 Warehouse 模块时会自动建表，无需手工导入 SQL。
:::

**从单服 SQLite 迁移**（仅一次，在**已有数据的那台**子服控制台执行）：

```bash
# 1. 先把 ArcartXWarehouse.yml 改为 mysql 并填好连接
# 2. 重启或 reload 后执行：
/axs migrate warehouse sqlite-to-mysql
```

---

## 第二步：Redis（推荐）

所有子服指向同一 Redis，`channel` 使用默认值 `ArcartX-Suite:CROSS` 即可。

```yaml
# 仅需保证各子服 host/port/password/database/channel 一致
redis:
  enabled: true
  host: "192.168.1.100"
  port: 6379
  password: ""
  database: 0
  channel: "ArcartX-Suite:CROSS"
```

无 Redis 时可改用 Proxy Forward（见下文「仅 Proxy 方案」），但 Warehouse 锁消息虽很小，仍建议与其他跨服模块一并使用 Redis。

---

## 第三步：宿主 config.yml（每台子服）

路径：`plugins/ArcartX-Suite/config.yml`

**三台子服示例**：除 `node-id` 外，其余字段保持一致。

### 子服 `survival-1`

```yaml
config-version: 1

cross-server:
  node-id: "survival-1"              # ← 每台子服必须唯一
  dedupe-ttl-ms: 60000
  max-payload-chars: 524288
  redis:
    enabled: true
    host: "192.168.1.100"
    port: 6379
    password: ""
    database: 0
    channel: "ArcartX-Suite:CROSS"
    connect-timeout-ms: 5000
  proxy:
    enabled: false                   # 与 Redis 二选一或同时 true（双发+去重）
    messenger-channel: "AXS_CROSS"
    forward-target: "ALL"
  signature:
    enabled: true
    secret: "同一网络内所有子服填写相同密钥"
    verify: true

modules:
  warehouse:
    enabled: true
  # … 其他模块 …
```

### 子服 `survival-2`

```yaml
cross-server:
  node-id: "survival-2"              # ← 仅改这里
  # redis / signature 等与 survival-1 完全相同
```

### 子服 `lobby`

```yaml
cross-server:
  node-id: "lobby"
  # redis / signature 等与 survival-1 完全相同
```

::: danger node-id 冲突
若两台子服 `node-id` 相同，CrossServer SDK 会把其中一台发出的消息当作「本机回声」丢弃，导致**锁状态不同步**。
:::

---

## 第四步：Warehouse 模块配置（各子服相同）

路径：`plugins/ArcartX-Suite/data/warehouse/ArcartXWarehouse.yml`

```yaml
config-version: 1

settings:
  debug: false
  flush-interval-ticks: 100

# 跨服共享仓库编辑锁（多子服 MySQL 时开启）
cross-server:
  enabled: true

storage:
  mode: "mysql"                      # ← 多子服必须为 mysql
  mysql:
    host: "192.168.1.100"            # MySQL 地址（各子服相同）
    port: 3306
    database: "ArcartX-Suite_warehouse"
    username: "axs_wh"
    password: "请替换为强密码"
  pool-size: 4                       # 每子服连接池；子服多时适当增大

shared:
  enabled: true                      # ← 必须开启共享仓库功能
  create-cost:
    currency: "points"
    amount: 1000
  default-level: 1
  # levels / permission-tiers 等按服内经济自行调整

# bank / warehouses / categories … 建议各子服配置保持一致，
# 避免不同子服容量、权限规则不一致造成体验混乱。
```

**跨服锁生效的四个条件（缺一不可）**：

1. 宿主 `cross-server.redis.enabled` 或 `proxy.enabled` 至少一个为 `true`
2. `ArcartXWarehouse.yml` → `cross-server.enabled: true`
3. `shared.enabled: true`
4. 各子服 MySQL 指向同一 `database`

---

## 第五步：仅 Proxy 方案（无 Redis 时的备选）

```yaml
# plugins/ArcartX-Suite/config.yml（各子服 node-id 仍须不同）
cross-server:
  node-id: "survival-1"
  redis:
    enabled: false
  proxy:
    enabled: true
    messenger-channel: "AXS_CROSS"
    forward-target: "ALL"
  signature:
    enabled: true
    secret: "shared-secret"
    verify: true
```

限制：

- 子服**零在线玩家**时无法通过 Proxy 发出跨服消息
- 单包约 32KB 上限（Warehouse 锁 payload 很小，一般不受影响）

---

## 第六步：启动与重载顺序

1. 启动 MySQL、Redis
2. 启动 BungeeCord / Velocity
3. 依次启动各子服（或并行）
4. 确认控制台输出：

```
[CrossServer] 已启动 | node=survival-1 | redis=true | proxy=false | sign=true | verify=true
[Warehouse] 跨服共享仓库编辑锁已启用
```

5. 修改配置后重载：

```bash
/axs reload all
# 或
/axs warehouse reload
```

---

## 第七步：验证清单

### 7.1 状态命令

每台子服执行：

```bash
/axs warehouse status
```

期望输出包含：

| 项 | 期望 |
| --- | --- |
| 存储 | `mysql` |
| 跨服编辑锁 | **已启用**（绿色） |
| 活跃共享锁 | 数字（无人在编辑时为 `0`） |

### 7.2 数据共享验证

1. 玩家在 **survival-1** 打开 `/warehouse`，向个人仓或共享仓存入物品
2. 同一账号切到 **survival-2**，打开仓库 → 应看到相同物品与余额

### 7.3 跨服编辑锁验证

1. 玩家甲在 **survival-1** 打开某共享仓库，切换到**编辑模式**
2. 玩家乙在 **survival-2** 打开**同一共享仓库**并尝试编辑
3. 期望：乙收到提示「该共享仓库正在被 甲（survival-1）编辑…」，界面为**只读**
4. 甲关闭 UI 或切回只读 → 乙可再次抢锁进入编辑

### 7.4 释放锁场景

以下操作应在本服释放锁并向其他子服广播 `UNLOCK`：

- 关闭仓库 / 管理 / 银行 UI
- 从编辑模式切回只读
- 玩家退出服务器

---

## 故障排查

| 现象 | 可能原因 | 处理 |
| --- | --- | --- |
| 跨服编辑锁显示「未启用」 | 模块 `cross-server.enabled: false` | 改为 `true` 并重载 |
| 同上 | 宿主未启 Redis/Proxy | 至少启用一种传输后端 |
| 同上 | `shared.enabled: false` | 开启共享仓库 |
| 两服数据不一致 | 仍用 SQLite 或 database 名不同 | 统一 `storage.mode: mysql` 与库名 |
| 锁只在单服生效 | `node-id` 重复或跨服通道未连通 | 检查各服 `node-id`、Redis 连通性 |
| 收不到远程锁 | `signature.secret` 不一致 | 各子服密钥对齐 |
| 控制台验签失败 | secret 错误或某服未配置 | 统一 `signature` 或临时 `verify: false` 排查 |
| MySQL 连接失败 | 防火墙 / 账号权限 | 子服 IP 授权、`GRANT` 检查 |

---

## 配置对照速查

| 文件 | 路径 | 各子服是否相同 |
| --- | --- | --- |
| 宿主跨服 | `plugins/ArcartX-Suite/config.yml` | **除 `node-id` 外相同** |
| 仓库模块 | `data/warehouse/ArcartXWarehouse.yml` | **MySQL 连接相同**；业务配置建议相同 |
| 授权 | `config.yml` → `cloud` + 云端「装备模块」 | 各子服均需 Warehouse 云端授权并装备 |

---

## 相关文档

- [跨服功能配置指南](/guide/cross-server-setup) — 通用 CrossServer 两步启用
- [跨服通信架构](/architecture/cross-server) — SDK、`warehouse` 频道 payload 格式
- [Warehouse 模块说明](/modules/warehouse) — 共享仓、银行、UI 包协议

