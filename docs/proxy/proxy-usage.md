# ArcartXSuite-Proxy 使用文档

## 概述

ArcartXSuite-Proxy 是 ArcartXSuite 的代理端伴侣插件，用于 Velocity / BungeeCord 代理服务器环境。它提供以下功能：

- **多 Yggdrasil 源认证路由**：同时支持微软正版 + LittleSkin + 其他自定义 Yggdrasil 源
- **离线玩家拦截**：在代理层拒绝离线账号（不进入后端服务器）
- **账号类型标记**：将玩家账号类型（微软正版 / LittleSkin / 离线）通过 GameProfile Property 传递给后端，后端可直接读取

> **注意**：Proxy 插件**不能替代 authlib-injector**。后端子服仍需要 authlib-injector 作为 JVM Agent 运行，以正确处理 Mixed Mode 下的 UUID 和 Skin 回调。

---

## 构建产物

| 产物 | 用途 |
|---|---|
| `ArcartXSuite-Proxy-Velocity.jar` | 部署到 Velocity 代理端 `plugins/` |
| `ArcartXSuite-Proxy-Bungee.jar` | 部署到 BungeeCord 代理端 `plugins/` |

---

## 部署场景对照

| 服务器架构 | 代理端部署 | 后端子服部署 |
|---|---|---|
| **单端**（无代理） | — | `ArcartXSuite.jar` + authlib-injector（`/axs auth setup`） |
| **Velocity 群组服** | `ArcartXSuite-Proxy-Velocity.jar` | `ArcartXSuite.jar` + authlib-injector |
| **BungeeCord 群组服** | `ArcartXSuite-Proxy-Bungee.jar` | `ArcartXSuite.jar` + authlib-injector |

---

## 配置

Proxy 插件首次启动会在 `plugins/ArcartXSuite-Proxy/` 下生成 `config.yml`：

```yaml
# Yggdrasil 认证源（按优先级排列）
yggdrasil-sources:
  - name: "LittleSkin"
    api-url: "https://littleskin.cn/api/yggdrasil"
    enabled: true
  - name: "Custom"
    api-url: "https://your-yggdrasil.com/api/yggdrasil"
    enabled: false

# 离线玩家处理
deny-offline: true
kick-offline-message: "&c本服务器仅支持正版/LittleSkin 账号登录"

# 调试日志
debug: false
```

### Yggdrasil 源说明

- **微软正版**：不需要手动配置源。玩家使用微软正版登录时，Minecraft 服务端本身已通过 Mojang 会话验证，UUID 为 v4，Proxy 插件会识别为 `MICROSOFT`。
- **LittleSkin**：配置 `https://littleskin.cn/api/yggdrasil`（Mixed Mode 用 `?mixed`）。UUID 通常为 v4（由 LittleSkin 分配），Proxy 插件会识别为 `LITTLESKIN`。
- **自定义源**：如 Blessing Skin 等自建 Yggdrasil，添加 `api-url` 即可。

---

## 命令

代理端控制台或拥有权限的管理员可执行：

| 命令 | 权限 | 说明 |
|---|---|---|
| `/axsproxy` | — | 查看状态 |
| `/axsproxy reload` | `arcartxsuite.proxy.admin` | 重载配置 |
| `/axsproxy status` | — | 查看认证源状态 |
| `/axsproxy help` | — | 查看帮助 |

---

## 后端接收账号类型

后端子服的 ArcartXSuite 会自动读取 Proxy 传递的账号类型，无需额外配置。通过以下方式访问：

```java
AccountTypeService service = /* 从 ModuleContext 获取 */;
AccountType type = service.resolve(player);
// type 可能为：MICROSOFT, LITTLESKIN, OFFLINE
```

Proxy 通过 GameProfile Property `axs_account_type` 传递类型标识。

---

## 常见问题

### Q: 代理端已部署 Proxy 插件，后端还需要 authlib-injector 吗？
**A: 需要。** Proxy 只负责拦截和分类。authlib-injector 仍必须在后端作为 JVM Agent 运行，否则服务端无法识别非 Mojang 的 Yggdrasil 会话，玩家会被视为未认证而被踢出。

### Q: 单端服务器需要用 Proxy 插件吗？
**A: 不需要。** 单端直接使用本体的 `/axs auth setup` 管理 authlib-injector 即可。

### Q: 为什么 LittleSkin 玩家 UUID 是 v4？
**A:** LittleSkin 使用 v4 UUID 分配策略，与 Mojang 一致。旧版 authlib-injector（<1.2.0）可能使用 v3，建议更新到最新版。
