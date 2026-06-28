---
title: Proxy 代理端插件 | ArcartX-Suite Minecraft插件文档
description: ArcartX-Suite Proxy 代理端插件部署与配置 — BungeeCord / Velocity 群组服认证辅助、离线拦截、与子服 AXS 本体配合说明。
---

# Proxy 代理端插件

## 它是什么

**ArcartXSuite Proxy** 是 ArcartX-Suite 在 **BungeeCord / Waterfall / Velocity 代理服** 上使用的**伴侣插件**，与游戏子服上的 `ArcartX-Suite.jar` **不是同一个 jar**。

| 能力 | 说明 |
| --- | --- |
| **离线玩家拦截** | 在代理层根据 UUID 版本识别离线账号，可配置直接踢出，不让其进入任何子服 |
| **账号类型预判** | 登录代理时按 UUID 版本初步判定微软正版 / LittleSkin / 离线（规则与子服 `account-type` 一致） |
| **认证环境检测** | 启动时检测是否配置了 authlib-injector / MultiLogin，并输出混合登录建议 |
| **Yggdrasil 源清单** | `proxy-config.yml` 中维护 Mojang、LittleSkin 等源列表，供状态查看与未来扩展 |

::: info 与「跨服 SDK」不是一回事
Proxy 插件**不负责** Chat / Tab / Mail / Warehouse 等**游戏数据**的跨服同步。

跨服同步由**各子服**宿主 `plugins/ArcartX-Suite/config.yml` → `cross-server` 配置，经 **Redis**（`ArcartX-Suite:CROSS`）和/或 **BungeeCord Forward**（`AXS_CROSS`）完成。详见 [跨服功能配置](/guide/cross-server-setup) 与 [跨服通信架构](/architecture/cross-server)。

**Proxy 管「谁能进网络」；CrossServer 管「进网后各子服怎么互通」。**
:::

::: warning Proxy 不能替代 authlib-injector
Proxy 只做**入口拦截与分类**。LittleSkin / 混合登录仍需要在**代理端 JVM** 配置 **MultiLogin** 或 **authlib-injector**，子服也需要 **online-mode=true** + **IP 转发**，否则外置登录会话无法正确传递到后端。

单端服务器（无群组）**不需要**装 Proxy，请用子服 `/axs auth setup` 生成混合登录启动脚本。详见下文「何时需要 / 不需要」。
:::

---

## 何时需要 / 不需要

| 场景 | 代理端 Proxy | 子服 ArcartX-Suite |
| --- | --- | --- |
| **单端 Paper 服**（玩家直连一台游戏服） | ❌ 不需要 | ✅ 需要；混合登录用 `config.yml` → `auth` + `/axs auth setup` |
| **Velocity / BungeeCord 群组**（1 代理 + 多子服） | ✅ **需要**（与代理类型匹配的 jar） | ✅ **每个子服都要装** |
| **仅正版、无 LittleSkin** | 可选（主要用 `deny-offline`） | ✅ 各子服照常 |
| **LittleSkin + 微软正版混合** | ✅ **强烈推荐** | ✅ 各子服 + 代理/子服 `online-mode=true` |
| **需要 Chat/Tab/Mail 等跨服** | Proxy **无关** | ✅ 各子服开启 `cross-server`（Redis 等） |

---

## 构建产物与下载

| 文件 | 安装位置 |
| --- | --- |
| `ArcartXSuite-Proxy-Velocity-*.jar` | **仅** Velocity 代理服 `plugins/` |
| `ArcartXSuite-Proxy-Bungee-*.jar` | **仅** BungeeCord / Waterfall 代理服 `plugins/` |
| `ArcartX-Suite-*.jar` | **各游戏子服** `plugins/`（**不要**放在代理服） |

Common 共享库已内联进上述两个 Proxy jar，**代理端只需放一个对应平台的 jar**。

可从 [云端下载中心](https://cloud.021209.xyz)（登录后「下载中心」→ 代理端配套）或 CI 构建产物获取。

---

## 装在哪台服、哪个目录

### 原则（一张图）

```
玩家 ──► 【代理服 BC/VC】装 Proxy jar
              │
              ├──► 【子服 lobby】装 ArcartX-Suite.jar
              ├──► 【子服 survival】装 ArcartX-Suite.jar
              └──► 【子服 resource】装 ArcartX-Suite.jar

❌ 子服不要装 Proxy jar
❌ 代理服不要装 ArcartX-Suite 游戏服本体（除非该机器同时跑子服进程）
```

### Velocity 示例

假设目录结构：

```
/home/mc/
├── velocity/                    ← 代理进程（玩家连 25565）
│   └── plugins/
│       ├── ArcartXSuite-Proxy-Velocity-1.2.0-beta.jar
│       └── arcartxsuite-proxy/  ← 首次启动自动生成（插件 id）
│           └── proxy-config.yml
├── lobby/                       ← 子服
│   └── plugins/
│       ├── ArcartX-x.x.x.jar
│       └── ArcartX-Suite-1.2.0-beta.jar
└── survival/                    ← 子服
    └── plugins/
        ├── ArcartX-x.x.x.jar
        └── ArcartX-Suite-1.2.0-beta.jar
```

### BungeeCord / Waterfall 示例

```
/home/mc/
├── bungee/                      ← 代理进程
│   └── plugins/
│       ├── ArcartXSuite-Proxy-Bungee-1.2.0-beta.jar
│       └── ArcartXSuite-Proxy/  ← 首次启动自动生成（plugin.yml name）
│           └── proxy-config.yml
├── lobby/
│   └── plugins/
│       └── ArcartX-Suite-1.2.0-beta.jar
└── survival/
    └── plugins/
        └── ArcartX-Suite-1.2.0-beta.jar
```

::: tip 配置目录名差异
- **Velocity**：`plugins/arcartxsuite-proxy/proxy-config.yml`
- **BungeeCord**：`plugins/ArcartXSuite-Proxy/proxy-config.yml`

两个平台配置文件**内容格式相同**，只是父目录名不同。
:::

---

## 完整部署示例

### 示例 A：Velocity + 2 个子服（LittleSkin + 正版混合）

**目标**：玩家从 Velocity 入口进服；LittleSkin 与微软正版都能玩；纯离线进不来；子服 Chat 跨服。

#### 1. 代理服（Velocity）

1. 将 `ArcartXSuite-Proxy-Velocity-*.jar` 放入 `velocity/plugins/`
2. 配置 **混合登录**（二选一，推荐方案 A）：
   - **方案 A（推荐）**：安装 [MultiLogin](https://github.com/InventivetalentDev/MultiLogin)，配置 `OFFICIAL` + `BLESSING_SKIN`（LittleSkin）多源
   - **方案 B（仅外置）**：Velocity 启动参数加 authlib-injector 指向 `littleskin.cn`（**不支持**正版+外置同时登录）
3. `velocity.toml` 中确保子服已注册，`online-mode = true`
4. 启动 Velocity，确认控制台出现 `ArcartXSuite Proxy (Velocity) 加载完成`
5. 编辑 `plugins/arcartxsuite-proxy/proxy-config.yml`（见下文「配置文件」）

#### 2. 每个子服（lobby / survival）

1. 安装 ArcartX + `ArcartX-Suite-*.jar` 到 `plugins/`
2. `server.properties`：

```properties
online-mode=true
```

3. Paper / Spigot 开启 IP 转发（**必须**，否则代理转发 UUID 失效）：

```yaml
# paper-global.yml 或 spigot.yml
settings:
  bungeecord: true   # Paper 部分版本在 paper-global.yml → proxies.velocity.support
```

Velocity 官方也需在 `velocity.toml` 开启 `player-info-forwarding-mode = "modern"`（或 legacy，与后端一致）。

4. 子服 **不要** 启用 `config.yml` → `auth.enabled`（群组服用代理端认证；单端混合登录才用 `auth` 节）：

```yaml
# plugins/ArcartX-Suite/config.yml（子服）
auth:
  enabled: false   # 群组服：混合登录在代理端处理，勿用 /axs auth setup
```

5. 若需跨服 Chat，在各子服配置 `cross-server`（与 Proxy **独立**）：

```yaml
cross-server:
  node-id: "lobby"      # 每台子服不同：lobby / survival
  redis:
    enabled: true
    host: "127.0.0.1"
    port: 6379
  proxy:
    enabled: false      # 有 Redis 时建议 false
```

#### 3. 验证

| 检查项 | 期望结果 |
| --- | --- |
| 正版玩家经 Velocity 进入 lobby | 成功 |
| LittleSkin 玩家经 Velocity 进入 | 成功 |
| 离线客户端直连子服 IP | 失败（应连代理地址） |
| `deny-offline: true` 时 v3 离线 UUID | 代理层踢出 |
| `/axsproxy status`（代理控制台） | 显示源列表与拒绝离线开关 |
| 子服 `/axs chat status` | 跨服 ON（若已配置 Redis） |

---

### 示例 B：BungeeCord + 3 个子服（仅拦截离线，暂不做 LittleSkin）

**目标**：全网 `online-mode=true`，Proxy 仅作离线兜底拦截；子服暂不配跨服。

#### 代理服

1. `bungee/plugins/ArcartXSuite-Proxy-Bungee-*.jar`
2. `config.yml`（Bungee 主配置）`online_mode: true`
3. `proxy-config.yml`：

```yaml
deny-offline: true
kick-offline-message: "&c请使用正版账号登录"
debug: false
```

#### 各子服

1. `ArcartX-Suite.jar` + ArcartX
2. `server.properties` → `online-mode=true`
3. `spigot.yml` → `settings.bungeecord: true`
4. **不装** Proxy jar

---

### 示例 C：单端服务器 — **不要装 Proxy**

```
plugins/
├── ArcartX-x.x.x.jar
└── ArcartX-Suite-1.2.0-beta.jar
```

混合登录流程：

1. `config.yml` → `auth.enabled: true`
2. 游戏内 `/axs auth setup` 生成 `start-mixed-auth.sh`
3. **用该脚本启动**子服（会先起本地混合 Yggdrasil 代理，再起服务端）

详见 [QQBot 混合登录说明](/modules/qqbot#混合登录-mixed-auth-mode) 与宿主 `config.yml` 中 `account-type` / `auth` 注释。

---

## 配置文件

首次启动后，Proxy 会在数据目录释放 **`proxy-config.yml`**（不是 `config.yml`）。

### 完整示例

```yaml
# plugins/.../proxy-config.yml

debug: false

# 是否拒绝离线模式玩家（UUID v3）
deny-offline: true
kick-offline-message: "&c本服务器仅支持正版/LittleSkin 账号登录"

# 是否为离线玩家自动分配 UUID（通常保持 true）
auto-assign-uuid: true

# Yggdrasil 认证源列表（按顺序；用于 status 展示与后续扩展）
sources:
  - name: Mojang
    api-url: "https://sessionserver.mojang.com/"
    enabled: true
    allow-offline-fallback: false
  - name: LittleSkin
    api-url: "https://littleskin.cn/api/yggdrasil"
    enabled: true
    allow-offline-fallback: false
```

### 字段说明

| 字段 | 说明 |
| --- | --- |
| `debug` | 为 `true` 时在代理控制台输出每位玩家的账号类型判定日志 |
| `deny-offline` | `true` 时拒绝 UUID v3 的离线玩家 |
| `kick-offline-message` | 踢出提示（支持 `&` 颜色码；Velocity 端会转为 MiniMessage） |
| `sources` | Yggdrasil 源清单；**实际握手认证**仍由 MultiLogin / authlib-injector / 代理内核完成 |

### 账号类型判定规则（代理层）

与子服 `account-type` 服务保持一致（代理端**不做 Mojang API 网络查询**）：

| UUID 版本 | 代理层判定 | 说明 |
| --- | --- | --- |
| v4 | LittleSkin | 微软正版在部分场景也是 v4，子服可开 `enable-mojang-lookup` 二次确认 |
| v3 | 离线 | 若 `deny-offline: true` 则在代理踢出 |
| v1 | 微软正版 | 部分微软账号场景 |

子服精确判定（含 Mojang API）见 `plugins/ArcartX-Suite/config.yml` → `account-type`。

---

## 命令

在**代理服**控制台或具备权限的管理员执行：

| 命令 | 权限 | 说明 |
| --- | --- | --- |
| `/axsproxy help` | `arcartxsuite.proxy.admin` | 帮助 |
| `/axsproxy status` | 同上 | 拒绝离线开关、调试模式、Yggdrasil 源列表 |
| `/axsproxy reload` | 同上 | 重载配置 |

::: tip
修改 `proxy-config.yml` 后建议 **`/axsproxy reload`**；若未生效，重启代理进程。
:::

---

## 与子服 config.yml 的分工

| 配置节 | 单端子服 | 群组子服 | 代理服 proxy-config.yml |
| --- | --- | --- | --- |
| `auth` + `/axs auth setup` | ✅ 使用 | ❌ 不用（`auth.enabled: false`） | — |
| `account-type` | ✅ 精确判定 | ✅ 精确判定 | —（代理只做入口预判） |
| `cross-server` | 单服可关 | ✅ 各子服必配（若需跨服） | ❌ 无关 |
| `deny-offline` | 模块内（LoginView 等） | 模块内 | ✅ **代理入口**拦截 |

---

## 常见问题

### Q: 代理装了 Proxy，子服还要 authlib-injector 吗？

**看认证架构。** 混合登录通常在**代理 JVM** 配 MultiLogin / authlib-injector；子服只需 `online-mode=true` + Bungee/Velocity IP 转发。子服上的 `/axs auth setup` **是给单端用的**，群组服请关闭 `auth.enabled`。

### Q: 能把 Proxy jar 和 ArcartX-Suite jar 都塞子服吗？

**不要。** Proxy 仅面向 BungeeCord / Velocity API，在 Paper 子服上无法加载。子服只放 `ArcartX-Suite-*.jar`。

### Q: 跨服 Chat 不工作，是 Proxy 没装好吗？

**多半不是。** 先查各子服 `cross-server.redis` / `cross-server.proxy`、模块 `cross-server.enabled`、Redis 是否互通。Proxy 与 CrossServer 无依赖关系。

### Q: LittleSkin 官方说不推荐 BungeeCord 了怎么办？

Wiki 与插件启动日志均建议：**新网络优先 Velocity**；Bungee 仍可使用 `ArcartXSuite-Proxy-Bungee.jar`，但混合登录更推荐 Velocity + MultiLogin。

### Q: `?mixed` 能写在 authlib-injector URL 里吗？

**不能。** `?mixed` 是 AXS **单端** `/axs auth setup` 用来启用本地混合代理的开关，不是 authlib-injector 或 LittleSkin 的标准参数。群组服请用 MultiLogin 或代理端方案。

---

## 相关文档

- [安装](/guide/installation) — 子服 ArcartX-Suite 安装
- [跨服功能配置](/guide/cross-server-setup) — Redis / Proxy Forward 通道
- [Warehouse 多服部署](/guide/warehouse-cross-server) — MySQL + 跨服锁完整清单
- [QQBot 混合登录](/modules/qqbot#混合登录-mixed-auth-mode) — 账号类型与 LoginView 绑定
- [跨服通信架构](/architecture/cross-server) — SDK 与 payload 格式
