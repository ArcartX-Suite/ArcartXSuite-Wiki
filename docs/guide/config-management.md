---
title: 配置智能体检 | ArcartX-Suite Minecraft插件文档
description: 配置智能体检 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 配置智能体检

ArcartX-Suite 内置**智能配置自动修正系统**，可在不中断服务的情况下，自动检测并修复 26 个模块的配置文件问题。

## 功能概览

智能体检系统提供四层诊断能力：

| 层级 | 功能 | 说明 |
|------|------|------|
| **1. 结构同步** | 键对齐 | 对比 jar 内默认配置，自动补全缺失键、标记废弃键 |
| **2. 类型修复** | 值校验 | 检查字段类型（STRING/INT/BOOLEAN 等），自动类型转换 |
| **3. 字段迁移** | 版本升级 | 根据 `migrations/<from>-<to>.yml` 执行重命名、删除、移动 |
| **4. 值验证** | 范围/枚举 | 验证数值范围（如 `pool-size` ∈ [1,100]）、枚举值合法性 |

## 核心配置节说明

### `auth` — 多方认证（单端）

仅在单端服务器有效。群组服请使用 ArcartXSuite-Proxy 插件，见 [Proxy 使用文档](proxy-usage.md)。

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `auth.auto-check-version` | BOOLEAN | `true` | 启动时自动检测 authlib-injector 版本并提示更新 |
| `auth.yggdrasil-source` | STRING | `https://littleskin.cn/api/yggdrasil?mixed` | 启动脚本中使用的 Yggdrasil API 地址。包含 `?mixed` 时生成 `start-mixed-auth` 脚本 |
| `auth.mixed-proxy-port` | INT | `25599` | 本地混合代理监听端口（`?mixed` 模式下使用） |

### `account-type` — 统一账号识别

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `account-type.enable-mojang-lookup` | BOOLEAN | `true` | 是否查询 Mojang API 识别微软正版。关闭后仅按 UUID 版本判定 |
| `account-type.mojang-timeout-ms` | INT | `5000` | Mojang API 请求超时（毫秒） |
| `account-type.debug` | BOOLEAN | `false` | 是否输出账号判定调试日志 |

### `tacz-compat` — 永恒枪械工坊兼容

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `tacz-compat.enabled` | BOOLEAN | `true` | 是否启用 TaCZ（永恒枪械工坊）兼容性修复 |
| `tacz-compat.debug` | BOOLEAN | `false` | TaCZ 兼容调试日志 |

### `keybinds` — 全局按键绑定

由宿主统一注册到 ArcartX 客户端，各模块共享。

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `keybinds.interact.name` | STRING | `AXS_INTERACT` | 交互按键注册名 |
| `keybinds.interact.default-key` | STRING | `F` | 默认键位（GLFW 键名） |
| `keybinds.interact.category` | STRING | `ArcartX-Suite` | 按键设置界面分类名 |
| `keybinds.navigate-prev.name` | STRING | `AXS_NAVIGATE_PREV` | 导航上一项按键注册名 |
| `keybinds.navigate-prev.default-key` | STRING | `NUMPAD_8` | 默认键位 |
| `keybinds.navigate-next.name` | STRING | `AXS_NAVIGATE_NEXT` | 导航下一项按键注册名 |
| `keybinds.navigate-next.default-key` | STRING | `NUMPAD_2` | 默认键位 |

模块通过 `context.registerKeybindHandler("AXS_INTERACT", ...)` 订阅按键回调。详见 [ModuleContext 全局按键订阅](/api/module-context#全局按键订阅)。

### `cross-server` — 全模块共用跨服传输

各模块在 `ArcartX*.yml` 中只需 `cross-server.enabled`；连接与签名在宿主配置。

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `cross-server.node-id` | STRING | Bukkit 服务器名 | 当前子服节点 ID，**多服必须唯一** |
| `cross-server.dedupe-ttl-ms` | LONG | `60000` | 入站 message-id 去重 TTL（毫秒） |
| `cross-server.max-payload-chars` | INT | `524288` | 单条 payload 最大字符数 |
| `cross-server.redis.enabled` | BOOLEAN | `false` | 是否启用 Redis Pub/Sub |
| `cross-server.redis.host` | STRING | `127.0.0.1` | Redis 地址 |
| `cross-server.redis.port` | INT | `6379` | Redis 端口 |
| `cross-server.redis.channel` | STRING | `ArcartX-Suite:CROSS` | 共用频道名 |
| `cross-server.proxy.enabled` | BOOLEAN | `false` | 是否启用 BungeeCord Forward |
| `cross-server.proxy.messenger-channel` | STRING | `AXS_CROSS` | Forward 子频道 |
| `cross-server.signature.enabled` | BOOLEAN | `false` | 是否 HMAC 签名 |
| `cross-server.signature.secret` | STRING | `""` | 签名密钥（多服一致） |

完整说明见 [跨服功能配置指南](cross-server-setup.md) 与 [跨服通信架构](/architecture/cross-server)。

### `client-packet-guard` — 客户端包频率限制

防止伪造/高频回包 DoS，按模块和动作粒度独立配置。

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `client-packet-guard.enabled` | BOOLEAN | `true` | 总开关 |
| `client-packet-guard.cleanup-interval-ticks` | INT | `200` | 过期窗口清理间隔（tick） |
| `client-packet-guard.defaults.window-ms` | INT | `1000` | 默认时间窗口（毫秒） |
| `client-packet-guard.defaults.max-hits` | INT | `20` | 默认窗口内最大命中数 |
| `client-packet-guard.defaults.mode` | STRING | `silent` | 默认超限处理模式：`silent`（静默丢弃）/ `notify`（通知玩家）/ `kick`（踢出） |
| `client-packet-guard.defaults.notify-message` | STRING | `&c操作过快，请稍后再试。` | 通知模式下的提示消息 |
| `client-packet-guard.defaults.notify-cooldown-ms` | INT | `3000` | 通知冷却（毫秒，防止刷屏） |
| `client-packet-guard.defaults.punish-command` | STRING | `（空）` | 超限后执行的惩罚命令（留空则不执行） |

各模块可独立覆写 `window-ms`、`max-hits`、`mode`，并为每个动作（action）设置更严格的限制：

```yaml
client-packet-guard:
  modules:
    title:
      window-ms: 1000
      max-hits: 4
      actions:
        equip:
          window-ms: 1500
          max-hits: 1
          mode: "notify"
```

当前已细分的模块：`title`、`mail`、`questgps`、`map`、`announcer`、`eventpacket`、`loginview`、`tab`、`essentials`、`regions`、`market`。

## 命令使用

### `/axs config` 子命令

| 子命令 | 权限 | 说明 |
|--------|------|------|
| `diagnose [owner]` | `arcartxsuite.admin` | 重新运行诊断（`owner` 可以是 `core`、模块ID或留空查全部） |
| `preview <owner>` | `arcartxsuite.admin` | 查看某模块的诊断报告 Markdown |
| `apply <owner>` | `arcartxsuite.admin` | 应用自动修复提案（会备份原文件） |
| `rollback <owner>` | `arcartxsuite.admin` | 回滚到最近一次 apply 之前的备份 |
| `status [owner]` | `arcartxsuite.admin` | 查看诊断状态统计 |

### 典型工作流

#### 场景 1：首次升级后检查

升级 ArcartX-Suite 或模块 jar 后，控制台会显示：

```
[ArcartX-Suite] 配置诊断: 26 个目标, 0 ERROR / 0 WARN / 26 INFO
[ArcartX-Suite] 报告: plugins/ArcartX-Suite/diagnosis/2026-05-18_15-30-22
```

若看到 `ERROR` 或 `WARN`，执行：

```
/axs config preview core          # 查看宿主配置问题
/axs config preview warehouse     # 查看仓库模块问题
```

#### 场景 2：安全应用修复

```
/axs config apply warehouse       # 应用仓库模块修复（会自动备份）
/axs config status warehouse      # 确认修复成功
```

如需回滚：

```
/axs config rollback warehouse     # 恢复到修复前状态
```

#### 场景 3：配置文件刚从旧位置迁移

当模块检测到配置文件从 `plugins/ArcartX-Suite/xxx.yml` 迁移到 `plugins/ArcartX-Suite/data/<module>/config.yml` 时，控制台会提示：

```
[模块名] 配置文件已迁移至新位置，建议运行 '/axs config preview <module>' 检查配置兼容性
```

此时建议执行 preview 检查是否有结构差异。

## 诊断报告解读

诊断报告存储在 `plugins/ArcartX-Suite/diagnosis/YYYY-MM-DD_HH-mm-ss/summary.md`，包含：

### 问题分级

| 级别 | 图标 | 含义 |
|------|------|------|
| ERROR | 🔴 | 必须修复，可能导致功能异常 |
| WARN | 🟡 | 建议修复，可能影响性能或体验 |
| INFO | 🟢 | 信息提示，无实质影响 |

### 常见问题类型

```markdown
## 结构差异 (Sync)
- `storage.pool-size` — INFO: 值类型不匹配 (预期: int, 实际: string)
- `settings.new-feature` — INFO: 缺失键，将从默认值合并

## 版本迁移 (Migration)
- `v1→v2`: 执行 Rename `old-key` → `new-key`
- `v1→v2`: 执行 Remove `deprecated-section`

## 值验证 (Validation)
- `storage.mode` — ERROR: 值 "sql" 不在允许集合 {sqlite, mysql} 中
- `pool-size` — WARN: 值 500 超出范围 [1, 100]
```

## 配置版本管理

每个配置文件独立维护版本号：

```yaml
# data/warehouse/config.yml
config-version: 1
```

当模块需要破坏性变更时：
1. 新版本 jar 包含 `migrations/1-2.yml`
2. 诊断引擎检测到 `config-version: 1` 低于当前版本 `2`
3. 自动应用迁移规则（重命名、删除、设置默认值）

## 与物理文件迁移的整合

智能配置体检与物理文件迁移协同工作：

```
启动流程:
1. 旧配置文件检测 → 从 plugins/ArcartX-Suite/xxx.yml 迁移到 data/<module>/config.yml
2. 触发智能诊断 → 对比 jar 内默认配置，检测结构差异
3. 提示管理员 → 建议运行 config preview 检查兼容性
4. 服务正常启动 → 即使存在配置问题也不中断
```

## 配置目录拆分

自 Build 2026-05-19 起，以下模块的大型数据段从主配置文件拆分到独立目录：

| 模块 | 原内联段 | 新配置键 | 目录内容 |
|------|---------|---------|---------|
| announcer | `entries:` | `entries-directory: "announcer"` | 公告条目（`data/announcer/announcer/*.yml`） |
| combateffect | `packets:` | `packets-directory: "packets"` | 战斗特效包定义 |
| title | `titles:` | `titles-directory: "titles"` | 称号定义（按组分文件） |
| rgb | `entries:` | `entries-directory: "entries"` | 渐变色条目 |
| map | `anchors:` | `anchors-directory: "anchors"` | 锚点定义 |
| questgps | `quests:` | `quests-directory: "quests"` | 任务定义（按分类分文件） |
| onlinerewards | `sign-in:` / `rewards:` | `sign-in-file` / `rewards-file` | 签到与奖励 |
| tab | `tabs:` | `tabs-directory: "tabs"` | Tab 面板定义 |
| entitytracker | bosses (旧根级) | `bosses-directory: "bosses"` | Boss 追踪定义 |
| eventpacket | `rules:` | `rules-directory: "rules"` | 事件包规则 |

### 目录文件规范

- 目录下每个 `*.yml` 文件可包含**多个**定义，根键即为该定义的 ID。
- 不需要每条定义独立一个文件，可按业务逻辑分组管理。
- 文件按文件名字母序加载，同名 ID 后加载的覆盖先加载的。
- 首次启动时模块会自动导出默认示例文件（如 `default.yml`）。

### 示例

```yaml
# data/rgb/entries/my-custom.yml
# 同一文件中放多个 RGB 定义
welcome:
  enabled: true
  text: "欢迎来到服务器"
  gradient-colors: ["#FF7A18", "#FFD64D"]
  shine: true

server_name:
  enabled: true
  text: "我的服务器"
  gradient-colors: ["#6A5CFF", "#FF6B9D"]
  shine: false
```

::: warning 破坏性变更
主配置中的旧内联段（如 `entries:`、`tabs:` 等）**不再被读取**。升级前请将旧数据手动迁移到对应目录文件中。
:::

## 最佳实践

1. **定期检查**：每周执行一次 `/axs config diagnose` 检查累积问题
2. **先 preview 后 apply**：重要生产环境务必先查看报告再应用修复
3. **利用备份**：apply 会自动创建 `.bak` 备份，rollback 可快速恢复
4. **关注日志**：启动时若看到 `configFileJustMigrated` 提示，及时检查兼容性

## 故障排除

### 诊断报告路径找不到

检查目录权限：
```bash
ls -la plugins/ArcartX-Suite/diagnosis/
```

### apply 后配置未生效

某些字段需要重启才能生效，apply 后观察控制台是否提示：
```
[ArcartX-Suite] 配置已修复，部分变更需重启后生效
```

### 迁移文件加载失败

确认 `migrations/<from>-<to>.yml` 格式正确：
```yaml
from-version: 1
to-version: 2
operations:
  - type: rename
    from: "old-key"
    to: "new-key"
```

## 何时需要更新诊断声明

智能诊断引擎对大多数 yml 改动可自动识别，但**部分情况必须显式更新声明**，否则会丢值或漏告警。

### 自动覆盖（无需更新声明）

| 改动 | 自动行为 |
|------|---------|
| 新增 jar 默认字段 | 报告 `JAR_NEW`，用户配置自动合并 |
| 删除 jar 默认字段 | 报告 `USER_DEPRECATED` |
| 修改 jar 默认值 | 标记用户旧值为 `USER_MODIFIED` |
| 修改注释/排版 | 不影响 |

### 必须更新声明

| 改动 | 必须做的事 |
|------|-----------|
| **重命名字段**（`a.b` → `c.d`） | 写 `migrations/<from>-<to>.yml` 添加 `rename` 操作 + 递增 `currentConfigVersion()` |
| **移动字段**（嵌套层级变化） | 写 migration + 升版本号 |
| **新增字段类型/范围/枚举约束** | 在模块的 `mainConfigValidations()` 或 `additionalConfigSpecs()` 加 `ValidationRule` |
| **新增动态节**（用户可自由扩展的子节） | 在 `defaultSyncPolicy()` 用 `SyncPolicy.builder().dynamicSection("path").build()` 声明 |

### 字段约束速查

模块入口（继承 `AbstractAXSModule`）需要覆写：

```java
@Override
protected List<ValidationRule> mainConfigValidations() {
    return List.of(
        ValidationRule.of("storage.mode", ValueType.STRING)
            .withEnum(Set.of("sqlite", "mysql")),
        ValidationRule.of("storage.pool-size", ValueType.INT)
            .withRange(1, 100),
        ValidationRule.required("storage.url", ValueType.STRING)
    );
}

@Override
protected SyncPolicy defaultSyncPolicy() {
    return SyncPolicy.builder()
        .dynamicSection("warehouses")  // 用户自由添加子节
        .build();
}

@Override
protected int currentConfigVersion() {
    return 2; // 字段重命名时递增
}
```

### 强制流程

每次改动 yml 默认配置时按此清单执行：

1. ✅ 修改 jar 默认 yml
2. ✅ 如重命名/移动字段 → 写 migration + 升版本号
3. ✅ 如有新增值约束 → 加 `ValidationRule`
4. ✅ 如有新增动态节 → 更新 `SyncPolicy`
5. ✅ 同步更新 `docs/guide/config-management.md` 和 `docs/architecture/config-autofix.md` 相关描述
6. ✅ 跑 `.\gradlew.bat build` 验证

### 反模式（禁止）

- ❌ 重命名字段但不写 migration → 用户值丢失
- ❌ 修改字段含义但保持名字不变 → 用户值会被错误保留
- ❌ 删除已发布的 migration 文件 → 老版本升级路径断裂


