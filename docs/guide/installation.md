# 安装

## 环境要求

| 项 | 要求 |
| --- | --- |
| 服务端 | Spigot / Paper / mohist，**MC 1.20.1及以上** |
| Java | **17** 或更高 |
| ArcartX 服务端插件 | 必装，版本与客户端 MOD 对齐 |
| 客户端 MOD | 玩家必须安装 ArcartX 客户端 MOD |
| 数据库 | 默认 SQLite；如需 MySQL，准备好访问凭证 |

## 依赖说明

AXS 的硬依赖只有 ArcartX。其他插件都按模块或功能降级处理：缺少非必要依赖时，主插件不应整体不可用。

| 依赖 | 类型 | 影响 |
| --- | --- | --- |
| ArcartX | 必需 | 缺少时 AXS 无法启动 |
| PlaceholderAPI | 可选 | 只影响 AXS 的 PAPI 占位符注册和部分文本解析 |
| MythicMobs / MythicBukkit | 可选 | 缺少时 EntityTracker 的 Boss 追踪跳过，普通攻击目标 HUD 可继续使用 |
| Chemdah | 指定模块必需 | 缺少时 Conversation、QuestGPS 跳过加载 |
| Adyeshach | 可选 | 只影响对话 NPC 相关能力 |
| MythicLib / AttributePlus / CraneAttribute / Vault / PlayerPoints / MMOItems / NeigeItems / AuthMe | 可选 | 只影响对应模块的增强功能或桥接能力 |

::: warning 缺依赖时的预期行为
如果日志出现“某模块需要某插件，已跳过加载”或“跳过占位符注册”，这是正常降级。只有 ArcartX 缺失、模块配置错误、jar 损坏或付费模块授权失败，才需要按错误处理。
:::

## 步骤

### 1. 安装 ArcartX

按 [ArcartX 官方文档](https://wiki.arcartx.com/docs) 安装服务端插件，并向玩家分发客户端 MOD。

### 2. 放入 AXS jar

```
plugins/
├── ArcartX-x.x.x.jar
└── ArcartXSuite-1.0.2-beta.jar
```

### 3. 启动一次，生成默认资源

启动 / 重启服务端。AXS 会：

- 把 jar 内 `.axb` 解密、解压成默认 YAML 释放到 `plugins/ArcartXSuite/`
- 生成 `config.yml`（总开关 + 各模块启用状态）、`license.yml`（QQ + 授权码列表）和各模块配置
- 生成 `arcartx/ui/*.yml` UI 模板
- 全部模块**默认 `enabled: false`**

### 4. 验证

```
/axs status
```

所有行都是 `disabled / locked` 是正常的，因为还没填写授权码，也还没开启模块。

::: info 首次启动后不需要继续做什么
建议保持服务端运行，先编辑 `plugins/ArcartXSuite/license.yml` 填写 QQ 和授权码，再编辑 `plugins/ArcartXSuite/config.yml` 开启需要的模块。
:::

## 模块 Jar 部署（可选）

AXS 1.0.2-beta 支持模块 Jar 独立部署。不放入模块 Jar 时，全部功能由宿主内置加载。

### 结构

```
plugins/
  ArcartXSuite.jar
  ArcartXSuite/
    config.yml
    modules/                ← 按需放入模块 Jar
      ArcartXSuite-RGB-1.0.2-beta.jar
      ArcartXSuite-Tab-1.0.2-beta.jar
      ArcartXSuite-Pickup-1.0.2-beta.jar
      ...
```

### 工作原理

1. 宿主启动时先扫描 `modules/` 目录，识别所有外部模块 Jar 的 id
2. 对于有外部 Jar 的模块，宿主跳过内置加载，由模块 Jar 接管
3. 对于没有外部 Jar 的模块，走内置加载（与之前版本行为一致）
4. `config.yml` 中的 `enabled` 配置与 license.yml 授权对两种模式均生效

### 重载

- `/axs reload all` 自动判断每个模块的加载来源，走对应的重载路径
- `/axs reload <模块名>` 同理

::: tip 无需手动 ax reload
ArcartX 现已支持 UI 自动导入，AXS 不再需要在启动或重载时执行 `ax reload` 命令。
:::

## 升级 / 替换 jar

- 直接覆盖 jar 然后重启；**不会丢已有数据库 / 已编辑过的 YAML**。
- 新版本可能新增配置键；升级后建议备份后重启，让 `YamlConfigSynchronizer` 合并缺失字段。

## 卸载

- 停服 → 删除 jar。
- 数据保留在 `plugins/ArcartXSuite/` 不会自动清理。

