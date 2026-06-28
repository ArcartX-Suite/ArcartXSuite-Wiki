---
title: 使用第三方模块 | ArcartX-Suite 开发者指南
description: 服主如何安装、启用、热加载第三方 ArcartX-Suite 模块 Jar，以及依赖、签名与排错。
---

# 使用第三方模块

本文面向 **服主与运维**：如何安装他人开发的 AXS 模块、如何启用与排错。若你要 **自己开发** 模块，请读 [开发第三方模块](./module-development)。

## 安装位置

第三方模块与官方模块使用 **相同的目录和开关**：

```
plugins/
  ArcartXSuite.jar
  ArcartX/
    config.yml
    modules/
      MyCustomModule.jar    ← 第三方模块放这里
      AnotherModule.jar
    data/
      mycustommodule/       ← 运行时自动创建
    ui/                     ← UI 从模块 Jar 自动导出
```

::: warning 不要放进 plugins 根目录
模块 Jar 必须放在 `plugins/ArcartXSuite/modules/`，**不要**像普通 Bukkit 插件那样丢进 `plugins/`。AXS 宿主会扫描 `modules/` 并按 `module.yml` 加载。
:::

## 启用模块

编辑 `plugins/ArcartXSuite/config.yml`：

```yaml
modules:
  mycustommodule:    # 必须与 module.yml 里的 id 一致
    enabled: true
```

保存后：

- **重启服务器**，或
- 执行 `/axs load mycustommodule` 热加载（模块此前未加载时）

查看状态：

```
/axs status
```

## 热加载 / 重载 / 卸载

| 命令 | 作用 |
|------|------|
| `/axs load <id>` | 从 `modules/` 扫描并首次加载模块 |
| `/axs reload <id>` | 对模块执行 `onDisable` → `onEnable`（同一 ClassLoader） |
| `/axs reload all` | 重载所有已加载模块 |
| `/axs unload <id>` | 卸载模块并释放 ClassLoader |

::: tip unload 与依赖
若模块 B `depends` 模块 A，卸载 A 前需先卸载 B。宿主会提示仍依赖 A 的模块列表。
:::

## 检查 module.yml

用压缩软件打开 Jar，根目录应有 `module.yml`：

```yaml
id: mycustommodule
name: My Custom Module
version: 1.0.0
main: com.example.mymodule.MyModule
api-version: 1.0
depends: [title]              # 需要先启用 title 模块
softdepends: [eventpacket]
external-depends: [PlaceholderAPI]
```

| 依赖类型 | 含义 | 服主需做什么 |
|----------|------|----------------|
| `depends` | 缺少则 **拒绝启动** | 在 `config.yml` 启用对应模块 |
| `softdepends` | 缺少则 **跳过联动** | 可选启用，不影响本模块基本功能 |
| `external-depends` | 需要 Bukkit 插件 | 安装对应插件（如 PAPI） |
| `external-softdepends` | 可选 Bukkit 插件 | 不装也能跑，部分功能降级 |

## 与官方模块混用

第三方模块与 Title、Mail 等官方模块 **平级**：

- 共用 `config.yml` 的 `modules.<id>.enabled`
- 共用跨服、货币、属性桥接等宿主配置
- 通过 **Capability** 调用官方模块能力（如发邮件、发称号）

若第三方模块文档写「需要 Mail 模块」，你应在 `config.yml` 中同时启用 `mail: enabled: true`。

## 模块签名（可选）

宿主支持对 `module.yml` 做 **Ed25519** 校验：开发者用私钥签名，服主在 `config.yml` 配置作者公钥后，无有效签名的 Jar 会被拒绝加载。

```yaml
module-signature-public-keys:
  - "BASE64_ENCODED_ED25519_PUBLIC_KEY"
```

留空 `[]` 表示关闭（默认）。

::: warning 开启前请读完整说明
配置**任意**公钥后，**所有**模块（含官方云端模块）都必须带合法 `signature`，否则无法加载。公钥格式、签名 payload、Gradle 集成与脚本用法见 **[模块 Ed25519 签名](./module-signature)**。
:::

## 云端模块 vs 本地 Jar

| 来源 | 位置 | 说明 |
|------|------|------|
| 官方/云端授权 | 内存加载，无本地文件 | 见 [云端授权](/guide/cloud-modules) |
| 第三方 / 自研 | `modules/*.jar` | 本文所述方式 |

第三方自研模块 **通常** 以本地 Jar 分发；不要将未授权的加密 `.axb` 与云端体系混用。

## 配置与数据目录

模块首次启动后：

| 路径 | 内容 |
|------|------|
| `data/<moduleId>/config.yml` | 从 Jar 导出的默认配置，可修改 |
| `data/<moduleId>/` | 数据库、缓存等 |
| `ui/` | 从 Jar 导出的 ArcartX UI YAML |

修改配置后执行 `/axs reload <moduleId>`。若模块支持配置迁移，可用 `/axs config diagnose <moduleId>` 检查兼容性。

## 常见问题

### 控制台报「模块依赖 xxx 未就绪」

在 `config.yml` 启用 `depends` 中列出的模块，并确认对应 Jar 存在于 `modules/`（官方模块可能由云端下发）。

### 模块 enabled 为 true 但未加载

- 检查 Jar 是否在 `plugins/ArcartXSuite/modules/`
- 检查 `module.yml` 的 `id` 与 `config.yml` 键名是否一致（大小写敏感）
- 查看控制台是否有 `ClassNotFoundException` 或签名失败

### UI / HUD 不显示

- 确认玩家客户端已安装 **ArcartX 模组**
- 确认服务端已安装 **ArcartX** 插件（AXS 的前置依赖）

### 与其他插件冲突

第三方模块仍是独立 ClassLoader，一般不会与 `plugins/` 下其他 Jar 的类冲突；但若模块 `external-depends` 某插件，需保证版本兼容。

### 如何卸载干净

```
/axs unload mycustommodule
```

删除 `modules/MyCustomModule.jar`。若需清除玩家数据，且模块注册了 `PlayerDataPurgeable`：

```
/axs purge <玩家名> mycustommodule
```

## 给模块作者的发布清单

发布 Jar 时建议附带：

1. 兼容的 AXS / `api-version` 版本
2. `module.yml` 中的 `depends` / `external-depends` 列表
3. `config.yml` 中需添加的 `modules.<id>.enabled` 片段
4. 若使用 Capability，说明需要启用哪些官方模块
5. （可选）[Ed25519 公钥与签名说明](./module-signature) — 供服主开启 `module-signature-public-keys`

## 相关文档

- [开发第三方模块](./module-development) — 自己写模块
- [Capability 详解](./capability-guide) — 模块之间如何联动
- [模块启用](/guide/module-enablement) — 官方模块开关说明
- [命令速查](/guide/commands) — `/axs` 完整命令
