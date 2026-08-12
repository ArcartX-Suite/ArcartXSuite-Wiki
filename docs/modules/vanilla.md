---
title: VanillaUI 原版界面替换插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite VanillaUI 原版界面替换，工作台/熔炉 ArcartX UI 替换、配方书、中文搜索、一键填充材料、熔炉状态实时推送，我的世界服务器界面增强插件。
---

# VanillaUI 原版界面替换

**VanillaUI** 模块基于 ArcartX UI 系统替换原版工作台与熔炉界面，提供配方书、中文搜索、一键填充材料、熔炉状态实时推送等功能。所有交互通过 ArcartX 客户端 UI 完成，无需输入命令。

## 功能概览

| 功能 | 说明 |
|------|------|
| **工作台 UI 替换** | 通过 UI `match` 自动拦截原版工作台界面，用 ArcartX UI 替换渲染 |
| **熔炉 UI 替换** | 同上，自动拦截原版熔炉界面 |
| **配方书** | 工作台/熔炉均内置配方书，按玩家已解锁配方过滤，支持分类浏览 |
| **中文搜索** | 配方书支持按物品中文名、英文名关键词搜索 |
| **一键填充材料** | 选中配方后点击按钮，自动从背包中取出材料填入合成格/熔炉输入槽 |
| **材料可合成检测** | 每个配方标记 `canCraft` 状态，背包材料不足的配方会标灰 |
| **熔炉状态实时推送** | 服务端按配置间隔轮询熔炉方块状态，实时推送燃烧进度和烹饪进度到 UI |
| **分类浏览** | 工作台配方按工具/建筑/食品/红石分类，熔炉配方按食品/建筑/杂项分类 |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | UI 渲染 + 数据包通信 + 物品中文名翻译 |

## 命令

本模块无独立命令。工作台和熔炉界面在玩家打开对应原版界面时自动替换，所有操作在 UI 内完成。

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/axs vanilla help` | `arcartxsuite.admin` | 查看可用子命令 |
| `/axs vanilla status` | `arcartxsuite.admin` | 查看模块状态 |

## 权限

本模块无独立权限节点。工作台和熔炉界面替换对所有玩家生效，由配置中的 `enabled` 开关控制。

## PlaceholderAPI 占位符

本模块不注册 PlaceholderAPI 占位符扩展。

## 配置结构

### ArcartXVanilla.yml

```yaml
config-version: 1

# 工作台界面替换
crafting:
  # 是否启用工作台 UI 替换
  enabled: true
  # UI id（与 vanilla_crafting.yml 文件名对应）
  ui-id: vanilla_crafting
  # 是否覆盖用户已修改的 UI 文件
  overwrite-ui-file: false

# 熔炉界面替换
furnace:
  # 是否启用熔炉 UI 替换
  enabled: true
  # UI id（与 vanilla_furnace.yml 文件名对应）
  ui-id: vanilla_furnace
  # 是否覆盖用户已修改的 UI 文件
  overwrite-ui-file: false
  # 熔炉状态推送间隔（tick），每秒 20 tick，默认 2 tick（每秒 10 次）
  push-interval-ticks: 2
```

### 配置字段说明

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `crafting.enabled` | Boolean | `true` | 是否启用工作台 UI 替换 |
| `crafting.ui-id` | String | `vanilla_crafting` | 工作台 UI 注册 ID |
| `crafting.overwrite-ui-file` | Boolean | `false` | 启动时是否覆盖用户已修改的 UI 文件 |
| `furnace.enabled` | Boolean | `true` | 是否启用熔炉 UI 替换 |
| `furnace.ui-id` | String | `vanilla_furnace` | 熔炉 UI 注册 ID |
| `furnace.overwrite-ui-file` | Boolean | `false` | 启动时是否覆盖用户已修改的 UI 文件 |
| `furnace.push-interval-ticks` | Int | `2` | 熔炉状态推送间隔（tick），范围 1–100 |

## UI 文件

模块自带两套 UI 定义文件，会在启用时自动导出到 `plugins/ArcartX-Suite/ui/`：

| 资源文件 | 导出路径 | 用途 |
|----------|----------|------|
| `arcartx/ui/vanilla_crafting.yml` | `ui/vanilla_crafting.yml` | 工作台界面（含配方书、搜索、一键填充） |
| `arcartx/ui/vanilla_furnace.yml` | `ui/vanilla_furnace.yml` | 熔炉界面（含配方书、搜索、一键填充、状态推送） |

UI 文件通过 `match` 自动拦截原版界面：
- 工作台：`match: [合成界面]`
- 熔炉：`match: [熔炉界面]`

> 如果 `crafting.overwrite-ui-file` 或 `furnace.overwrite-ui-file` 设为 `true`，每次启动都会用 jar 内默认 UI 覆盖用户修改。默认 `false`，用户自定义的 UI 文件不会被覆盖。

## 客户端包协议

### 工作台（packet ID：`vanilla_crafting`）

| action | 参数 | 说明 |
|--------|------|------|
| `toggle_recipe_book` | — | 切换配方书显示/隐藏 |
| `request_recipe_book` | — | 请求当前配方书状态和配方列表 |
| `place_ingredients` | `配方索引` | 一键填充选中配方的材料到合成格 |

### 熔炉（packet ID：`vanilla_furnace`）

| action | 参数 | 说明 |
|--------|------|------|
| `toggle_recipe_book` | — | 切换配方书显示/隐藏 |
| `request_recipe_book` | — | 请求当前配方书状态和配方列表 |
| `place_ingredients` | `配方索引` | 一键填充选中配方的输入材料到熔炉输入槽 |

### 服务端推送

| packet ID | action | 说明 |
|-----------|--------|------|
| `vanilla_furnace` | `furnace_state` | 熔炉状态推送，包含 `burnTime`、`burnTimeTotal`、`cookTime`、`cookTimeTotal`、`burning` 字段 |
| `vanilla_crafting` | `recipe_book_toggle` | 配方书切换响应，包含 `open`、`recipes`、`recipeCount`、`catIcons` 字段 |
| `vanilla_furnace` | `recipe_book_toggle` | 同上，熔炉配方书切换响应 |

## 功能详解

### 工作台界面

打开原版工作台时自动替换为 ArcartX UI。界面包含：

- **合成网格**：3×3 合成区 + 结果槽，与原版操作方式一致
- **配方书面板**：可切换显示/隐藏，展示玩家已解锁的所有合成配方
- **分类筛选**：全部 / 工具 / 建筑 / 食品 / 红石
- **搜索框**：支持物品中文名和英文名搜索
- **一键填充**：选中配方后点击按钮，自动从背包取出材料填入合成格；已正确放置的材料会保留，不匹配的槽位物品会退回背包

配方数据由 `RecipeCollector` 从 Bukkit 注册的 `ShapedRecipe` 和 `ShapelessRecipe` 中收集，仅展示玩家已解锁的配方（`player.getDiscoveredRecipes()`）。每个配方标记 `canCraft` 字段，表示玩家背包中是否有足够材料。

### 熔炉界面

打开原版熔炉时自动替换为 ArcartX UI。界面包含：

- **输入槽 / 燃料槽 / 结果槽**：与原版操作方式一致
- **配方书面板**：可切换显示/隐藏，展示玩家已解锁的所有熔炉/烟熏炉/高炉配方
- **分类筛选**：全部 / 食品 / 建筑 / 杂项
- **搜索框**：支持物品中文名和英文名搜索
- **一键填充**：选中配方后点击按钮，自动从背包取出输入材料填入熔炉输入槽；已有正确材料时尝试追加而非替换，避免中断燃烧
- **状态实时推送**：服务端按 `furnace.push-interval-ticks` 间隔轮询熔炉方块状态，推送燃烧进度（`burnTime` / `burnTimeTotal`）和烹饪进度（`cookTime` / `cookTimeTotal`）到 UI，驱动火焰图标和进度条动画

配方数据由 `RecipeCollector` 从 `FurnaceRecipe`、`SmokingRecipe`、`BlastingRecipe` 中收集。

### 熔炉状态推送

`FurnaceStateTask` 负责追踪当前打开熔炉界面的玩家，按配置间隔轮询熔炉 BlockEntity 状态：

1. `InventoryOpenListener` 监听 `InventoryOpenEvent` / `InventoryCloseEvent`，玩家打开熔炉时注册追踪，关闭时取消
2. 每个推送周期，对每个追踪中的玩家解析其熔炉方块，读取 `burnTime`、`cookTime` 等状态
3. 通过 `PacketBridgeAPI` 发送 `furnace_state` 包到 UI，UI 端根据数据更新火焰图标可见性和进度条宽度
4. 玩家离线或关闭界面时自动停止追踪

### 物品中文名翻译

配方书中物品的中文名通过 `VanillaItemNameBridge` 获取。如果宿主插件提供了中文名翻译桥接（如连接到 i18n 模块），则使用翻译后的名称；否则回退为 Material 名称的小写形式（下划线替换为空格）。

## 配置诊断

VanillaUI 模块声明了以下配置校验规则：

| 字段 | 类型 | 约束 |
|------|------|------|
| `crafting.enabled` | BOOLEAN | 非必填 |
| `crafting.ui-id` | STRING | 非必填 |
| `furnace.enabled` | BOOLEAN | 非必填 |
| `furnace.ui-id` | STRING | 非必填 |
| `furnace.push-interval-ticks` | INT | 范围 1–100 |

同步策略为 `SyncPolicy.strict()`（严格模式），无动态节。

## 架构

```
VanillaModule (AbstractAXSModule)
├── VanillaConfiguration (配置聚合)
│   ├── Crafting (工作台配置)
│   └── Furnace (熔炉配置)
├── VanillaService (业务服务)
│   ├── 工作台配方书推送
│   ├── 熔炉配方书推送
│   ├── 一键填充材料（工作台 + 熔炉）
│   └── RecipeCollector (配方收集器)
│       ├── ShapedRecipe / ShapelessRecipe 收集
│       ├── FurnaceRecipe / SmokingRecipe / BlastingRecipe 收集
│       ├── 物品分类（tools / building / food / redstone / misc / equipment）
│       └── 物品中文名翻译
├── FurnaceStateTask (熔炉状态轮询推送)
├── InventoryOpenListener (熔炉打开/关闭追踪)
└── UI 文件
    ├── vanilla_crafting.yml (工作台界面)
    └── vanilla_furnace.yml (熔炉界面)
```
