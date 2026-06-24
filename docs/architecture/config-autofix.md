---
title: 配置智能诊断 (Config Autofix) | ArcartX-Suite Minecraft插件架构文档
description: 配置智能诊断 (Config Autofix) - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 配置智能诊断 (Config Autofix)

ArcartX-Suite 提供**四层配置诊断体系**，在模块加载阶段自动完成配置校验与修复，无需中断服务。

## 架构概览

```
┌─────────────────────────────────────────────────────────────────────┐
│                     ConfigDiagnosticEngine                          │
├─────────────────────────────────────────────────────────────────────┤
│  1. Structure Sync  →  YamlConfigSynchronizer + SyncPolicy          │
│  2. Type Coercion   →  TypeCoercer (STRING↔INT↔BOOLEAN)             │
│  3. Version Migrate →  MigrationLoader + MigrationOperationExecutor │
│  4. Value Validate  →  ValidationRule (range/enum/required)         │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   ConfigDiagnosisStore (内存缓存)                    │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│              MarkdownReportWriter → diagnosis/YYYY-MM-DD/           │
│                         + .proposal.yml (修复提案)                   │
└─────────────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. SyncPolicy — 结构同步策略

模块通过覆写 `defaultSyncPolicy()` 声明动态配置节：

```java
@Override
protected @NotNull SyncPolicy defaultSyncPolicy() {
    return SyncPolicy.builder()
        .dynamicSection("warehouses")      // 用户自定义键，不强制同步
        .dynamicSection("categories")
        .opaqueSection("presets")          // 不透明节，不递归比较
        .obsoletePath("legacy.backup-mode") // 废弃路径，建议删除
        .build();
}
```

| 方法 | 用途 |
|------|------|
| `dynamicSection(path)` | 标记用户可自由增删的节（如仓库列表、称号定义） |
| `opaqueSection(path)` | 标记不透明节，内容不递归对比 |
| `obsoletePath(path)` | 标记已废弃的键路径，诊断报告提示删除 |

### 2. ValidationRule — 值校验规则

模块在 `mainConfigValidations()` 中声明字段约束：

```java
@Override
protected @NotNull List<ValidationRule> mainConfigValidations() {
    return List.of(
        ValidationRule.required("storage.mode", ValueType.STRING)
            .withEnum(Set.of("sqlite", "mysql")),
        ValidationRule.required("storage.pool-size", ValueType.INT)
            .withRange(1, 100),
        ValidationRule.of("settings.timeout", ValueType.INT)
            .withRange(100, 30000)
    );
}
```

支持校验类型：
- **类型检查**：STRING, INT, LONG, DOUBLE, BOOLEAN, STRING_LIST, SECTION
- **范围校验**：`.withRange(min, max)`，支持 null 表示无界
- **枚举校验**：`.withEnum(Set.of("a", "b"))`
- **必填校验**：`.asRequired()` 或 `ValidationRule.required(path, type)`

### 3. MigrationOperation — 版本迁移操作

模块在 `migrations/<from>-<to>.yml` 定义升级规则：

```yaml
# modules/warehouse/src/main/resources/migrations/0-1.yml
from-version: 0
to-version: 1
description: "仓库配置 1.0 升级"

operations:
  - type: rename
    from: "pickup.auto-store-on-pickup"
    to: "pickup.auto-store"
    
  - type: remove
    path: "legacy.backup-mode"
    
  - type: set-if-missing
    path: "pickup.notify-on-auto-store"
    value: true
    
  - type: value-map
    path: "storage.mode"
    mapping:
      "file": "sqlite"
      "remote": "mysql"
```

支持操作类型：

| 类型 | 说明 |
|------|------|
| `rename` | 字段重命名，保留原值 |
| `remove` | 删除废弃字段 |
| `move` | 移动路径（复制值后删除原键） |
| `set-if-missing` | 若键不存在则设置默认值 |
| `value-map` | 值映射转换（如旧枚举值映射到新值） |

### 4. ModuleConfigSpec — 配置声明规范

模块通过 `configSpecs()` 返回完整配置声明：

```java
@Override
public List<ModuleConfigSpec> configSpecs() {
    List<ModuleConfigSpec> specs = new ArrayList<>();
    
    // 主配置
    specs.add(new ModuleConfigSpec(
        "warehouse",                                    // ownerId
        new ConfigSyncSpec(
            "ArcartXWarehouse.yml",                      // jar 内资源名
            "data/warehouse/config.yml",                 // 目标路径
            defaultSyncPolicy()                          // SyncPolicy
        ),
        1,                                              // currentVersion
        "config-version",                               // versionPath
        "migrations",                                   // migrationFolder
        mainConfigValidations()                         // List<ValidationRule>
    ));
    
    // 附加配置（如多语言文件）
    specs.addAll(additionalConfigSpecs());
    
    return List.copyOf(specs);
}
```

## 模块集成

### AbstractAXSModule 自动集成

继承 `AbstractAXSModule` 的模块默认获得以下集成：

1. **物理迁移检测**：`ensureConfigExists()` 检测旧文件迁移并设置 `configFileJustMigrated` 标志
2. **配置声明注册**：`configSpecs()` 自动构建基于 `configFileName()` 的规范
3. **诊断启动提示**：若刚发生迁移，`onEnable()` 提示使用 `/axs config preview`

### 非继承模块手动集成

直接实现 `AXSModule` 的模块需手动声明：

```java
public class CombatEffectModule implements AXSModule {
    @Override
    public List<ModuleConfigSpec> configSpecs() {
        return List.of(new ModuleConfigSpec(
            "combateffect",
            new ConfigSyncSpec(CONFIG_FILE_NAME, "data/combateffect/config.yml", 
                SyncPolicy.builder()
                    .dynamicSection("kill-effect")
                    .dynamicSection("digis-display")
                    .build()),
            1, "config-version", "migrations", List.of()
        ));
    }
}
```

## 引擎工作流程

```
模块加载阶段:
    │
    ├─ 1. ModuleRegistry.loadAndEnable()
    │      └─ 调用 plugin.registerModuleConfigSpecs(specs, classLoader)
    │
    ├─ 2. ConfigDiagnosticEngine.diagnose()
    │      ├─ 加载 jar 内默认配置 (ProtectedResourceOpener)
    │      ├─ 加载磁盘当前配置
    │      ├─ 检测版本差异 → 加载并应用 migrations
    │      ├─ 结构同步 (SyncPolicy 指导合并)
    │      ├─ 值校验 (ValidationRule 检查)
    │      └─ 生成报告 + 修复提案
    │
    └─ 3. instance.onEnable()
           ├─ ensureConfigExists() (物理迁移)
           ├─ 若 configFileJustMigrated, 提示 config preview
           └─ 服务启动 (即使存在配置问题也不阻塞)
```

## API 扩展

### axs-api 新增类型

| 类型 | 说明 |
|------|------|
| `SyncPolicy` | 结构同步策略（动态节/废弃路径） |
| `SyncResult` | 单次同步结果（新增/删除/修改键） |
| `ConfigSyncSpec` | 单文件同步规范（资源路径→目标路径） |
| `ValueType` | 配置值类型枚举 |
| `ValidationRule` | 字段校验规则（类型/范围/枚举/必填） |
| `MigrationOperation` | 迁移操作（Rename/Remove/Move/SetIfMissing/ValueMap） |
| `ConfigMigrationDescriptor` | 版本迁移描述符（from→to + operations） |
| `ModuleConfigSpec` | 模块完整配置声明（owner + sync + validations） |
| `ConfigIssue` / `ConfigDiagnosisReport` | 诊断问题与报告 |

### 宿主插件集成

`ArcartX-SuitePlugin` 提供：

```java
// 注册模块配置规范（ModuleRegistry 调用）
void registerModuleConfigSpecs(String ownerId, 
                               List<ModuleConfigSpec> specs, 
                               ClassLoader classLoader);

// 获取诊断引擎与存储
ConfigDiagnosticEngine getConfigDiagnosticEngine();
ConfigDiagnosisStore getConfigDiagnosisStore();
```

## 文件结构

```
plugins/ArcartX-Suite/
├── diagnosis/
│   └── 2026-05-18_15-30-22/
│       ├── summary.md           # 可读诊断报告
│       ├── warehouse.proposal.yml  # 修复提案
│       └── core.proposal.yml
├── data/
│   └── <module>/
│       ├── config.yml           # 当前配置
│       └── config.yml.bak      # 最近一次 apply 前的备份
└── ...
```

## 模块诊断声明一览

| 模块 | configVersion | 动态节 | ValidationRule 数 | 备注 |
|------|:---:|---|:---:|---|
| Warehouse | 1 | warehouses, categories | 6 | 首个实现诊断的模块 |
| Market | 0 (初始) | auction.categories, auction.blacklist, auction.tax-discount, recycle.price-multiplier, messages | 8 | MySQL 必需；Redis 可选 |
| QQBot | 0 (初始) | groups, custom-commands | 3 | OneBot WS 必需；SQLite/MySQL 可切换 |

## 最佳实践

### 模块开发者

1. **准确声明 dynamicSection**：所有用户可自由增删的键都应声明，避免被结构同步误删
2. **合理设置 version**：破坏性变更时递增 `currentConfigVersion()`，提供 migrations
3. **全面校验关键字段**：`storage.mode`、`pool-size` 等影响启动的字段必须加校验
4. **测试迁移路径**：使用 `/axs config apply <module>` 验证 migrations 正确性

### 服务端管理员

1. **定期诊断**：每周执行 `diagnose` 检查累积问题
2. **关注迁移提示**：看到 "配置文件已迁移至新位置" 时及时 `preview`
3. **利用 backup**：`apply` 自动备份，`rollback` 可快速恢复
4. **查看 Markdown 报告**：`diagnosis/` 下的报告可直接阅读并分享

## 故障排查

### 诊断引擎未运行

检查 `ArcartX-SuitePlugin` 初始化日志：
```
[AXS] 配置诊断引擎初始化完成，资源加载器: ModuleRegistry
```

### 迁移文件加载失败

确认 jar 内路径：`migrations/0-1.yml` 必须位于 `src/main/resources/` 根目录，非 `arcartx/internal/`

### 模块 spec 未注册

检查 `ModuleRegistry.loadAndEnable()` 是否捕获异常：
```
模块 xxx 配置诊断异常: <message>
```

## 与其他系统的整合

| 系统 | 整合点 |
|------|--------|
| **物理文件迁移** | `AbstractAXSModule.configFileJustMigrated` 标志联动 |
| **加密资源加载** | `ProtectedResourceOpener` 支持 ownerId 路由 |
| **UI 配置导出** | `ModuleContext.exportConfigResource()` 独立处理 |
| **备份清理** | `RetentionCleaner` 管理旧诊断文件与备份 |

