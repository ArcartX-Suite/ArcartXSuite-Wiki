---
title: 安装 | ArcartX-Suite Minecraft插件文档
description: 安装 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 安装

## 环境要求

| 项 | 要求 |
| --- | --- |
| 服务端 | Spigot / Paper / mohist，**MC 1.20.1及以上** |
| Java | **17** 或更高（推荐 21） |
| ArcartX 服务端插件 | 必装，版本与客户端 MOD 对齐 |
| 客户端 MOD | 玩家必须安装 ArcartX 客户端 MOD |
| 数据库 | 默认 SQLite；如需 MySQL，准备好访问凭证 |

## 依赖说明

ArcartX-Suite 的硬依赖只有 ArcartX。其他插件都按模块或功能降级处理：缺少非必要依赖时，主插件不应整体不可用。

| 依赖 | 类型 | 影响 |
| --- | --- | --- |
| ArcartX | 必需 | 缺少时 ArcartX-Suite 无法启动 |
| PlaceholderAPI | 可选 | 只影响 ArcartX-Suite 的 PAPI 占位符注册和部分文本解析 |
| MythicMobs / MythicBukkit | 可选 | 缺少时 EntityTracker 的 Boss 追踪跳过，普通攻击目标 HUD 可继续使用 |
| Chemdah | 指定模块必需 | 缺少时 Conversation、QuestGPS 跳过加载 |
| Adyeshach | 可选 | 只影响对话 NPC 相关能力 |
| MythicLib / AttributePlus / CraneAttribute / Vault / PlayerPoints / MMOItems / NeigeItems / AuthMe | 可选 | 只影响对应模块的增强功能或桥接能力 |
| OneBot 11 实现端（[SnowLuma](https://github.com/SnowLuma/SnowLuma) 推荐） | QQBot 模块必需 | 缺少时 QQBot 模块无法连接 QQ 群 |
| MySQL | 部分模块可选 | Market 必需；Mail / Chat / OnlineRewards 等跨服或持久化功能可选；跨服连接见宿主 `cross-server` |

::: warning 缺依赖时的预期行为
如果日志出现“某模块需要某插件，已跳过加载”或“跳过占位符注册”，这是正常降级。只有 ArcartX 缺失、模块配置错误、jar 损坏，才需要按错误处理。
:::

## 步骤

### 1. 安装 ArcartX

按 [ArcartX 官方文档](https://wiki.arcartx.com/docs) 安装服务端插件，并向玩家分发客户端 MOD。

### 2. 放入 ArcartX-Suite jar

```
plugins/
├── ArcartX-x.x.x.jar
└── ArcartX-Suite.jar
```

### 3. 启动一次，生成默认资源

启动 / 重启服务端。ArcartX-Suite 会：

- 把 jar 内 `.axb` 解密、解压成默认 YAML 释放到 `plugins/ArcartX-Suite/`
- 生成 `config.yml`（总开关 + 各模块启用状态）和各模块配置
- 生成 `arcartx/ui/*.yml` UI 模板
- 全部模块**默认 `enabled: false`**

### 4. 验证

```
/axs status
```

所有行都是 `disabled` 是正常的，因为还没开启模块。

::: info 首次启动后不需要继续做什么
建议保持服务端运行，先编辑 `plugins/ArcartX-Suite/config.yml` 开启需要的模块。
:::

## 模块 Jar 部署（可选）

ArcartX-Suite 支持模块 Jar 独立部署。不放入模块 Jar 时，全部功能由宿主内置加载。

### 结构

```
plugins/
  ArcartX-Suite.jar
  ArcartX-Suite/
    config.yml
    modules/                ← 按需放入模块 Jar
      ArcartXSuite-RGB.jar
      ArcartXSuite-Tab.jar
      ArcartXSuite-Pickup.jar
      ...
```

### 工作原理

1. 宿主启动时先扫描 `modules/` 目录，识别所有外部模块 Jar 的 id
2. 对于有外部 Jar 的模块，宿主跳过内置加载，由模块 Jar 接管
3. 对于没有外部 Jar 的模块，走内置加载
4. `config.yml` 中的 `enabled` 配置对两种模式均生效

### 何时使用外部 Jar

| 场景 | 推荐方式 |
|---|---|
| 服主只使用免费/默认模块 | **内置加载**即可，无需任何额外操作 |
| 服主购买付费模块 | 放入对应模块的 `.jar`，由外部 Jar 接管 |
| 云端授权 | 云端自动下载 `.axb` 并加载，**无需手动放 jar** |
| 开发/调试自定义模块 | 放入外部 Jar，方便独立替换和重载 |

### 模块 Jar 命名规范

`modules/` 目录下的 jar 文件名没有强制格式要求，但推荐遵循以下命名以便识别：

```
ArcartXSuite-<ModuleName>-<version>.jar
# 例如
ArcartXSuite-RGB.jar
ArcartXSuite-Tab.jar
```

宿主通过读取 jar 内 `module.yml` 的 `id` 字段来匹配模块身份，**与文件名无关**。

### 混淆与依赖关系

- `ArcartX-Suite-core` 产出物会先经过 ProGuard 混淆，模块编译时依赖的是**混淆后的 core jar**
- 模块 jar 内部包含 `module.yml` 描述文件，声明模块 id、版本、主类、依赖
- 模块 jar 以 `compileOnly` 方式依赖 `ArcartX-Suite-api`，打包时不包含 API 类
- 运行时，模块类加载器由宿主统一管理，模块间通过 `Capability` 和 `ModuleContext` 通信

### 重载

- `/axs reload all` 自动判断每个模块的加载来源，走对应的重载路径
- `/axs reload <模块名>` 同理

::: tip 无需手动 ax reload
ArcartX 现已支持 UI 自动导入，ArcartX-Suite 不再需要在启动或重载时执行 `ax reload` 命令。
:::

## 代理端部署（Velocity / BungeeCord 群组服）

若使用 Velocity / BungeeCord / Waterfall 群组架构，**代理服与子服安装不同的 jar**：

| 机器 | 安装 |
| --- | --- |
| **代理服** | `ArcartXSuite-Proxy-Velocity-*.jar` 或 `ArcartXSuite-Proxy-Bungee-*.jar` → 代理 `plugins/` |
| **各子服** | `ArcartX-Suite-*.jar` + ArcartX → 子服 `plugins/`（**不要**装 Proxy jar） |

```
velocity/plugins/
  ArcartXSuite-Proxy-Velocity.jar
  arcartxsuite-proxy/proxy-config.yml

lobby/plugins/
  ArcartX-Suite.jar
```

::: tip 认证与跨服是两件独立的事
- **Proxy**：代理入口认证辅助、离线拦截 → 见 [Proxy 代理端插件](proxy-usage)
- **CrossServer**：子服间 Chat / Tab / Mail 等数据同步 → 见 [跨服功能配置](cross-server-setup)

群组服混合登录在**代理端**配置 MultiLogin / authlib-injector；子服 `auth.enabled` 保持 `false`，**不要**在各子服用 `/axs auth setup`。
:::

完整 BC/VC 目录示例、配置项与验证清单见 **[Proxy 代理端插件](proxy-usage)**。

## 升级 / 替换 jar

- 直接覆盖 jar 然后重启；**不会丢已有数据库 / 已编辑过的 YAML**。
- 新版本可能新增配置键；升级后建议备份后重启，让 `YamlConfigSynchronizer` 合并缺失字段。

## 卸载

- 停服 → 删除 jar。
- 数据保留在 `plugins/ArcartX-Suite/` 不会自动清理。


