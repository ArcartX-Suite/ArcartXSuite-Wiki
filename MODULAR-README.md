# ArcartXSuite 模块化架构

## 概述

ArcartXSuite 已从单体插件重构为 **宿主 + 模块 Jar** 架构。  
用户可按需放入模块 Jar，实现功能的自由组合。宿主会自动检测外部模块并跳过内置加载，  
由 `ModuleRegistry` 统一管理生命周期。

## 项目结构

```
ArcartXSuite/
├── axs-api/              # 模块 API 接口层（AXSModule, ModuleContext 等）
├── axs-core/             # 宿主核心（ShadowJar 输出）
├── modules/
│   ├── announcer/        # Announcer 播报 + Subtitle 字幕
│   ├── entitytracker/    # EntityTracker 实体追踪 + AttackTarget 攻击目标
│   ├── chat/             # Chat 频道聊天
│   ├── conversation/     # Conversation 对话桥（需要 Chemdah）
│   ├── eventpacket/      # EventPacket 事件引擎
│   ├── combateffect/     # CombatEffect 战斗特效 + DigisDisplay 伤害飘字
│   ├── loginview/        # LoginView 登录界面
│   ├── mail/             # Mail 邮箱
│   ├── map/              # Map 世界地图
│   ├── onlinerewards/    # OnlineRewards 在线奖励
│   ├── pickup/           # Pickup 拾取提示
│   ├── prop/             # Prop 快捷道具
│   ├── questgps/         # QuestGPS 任务导航（需要 Chemdah）
│   ├── rgb/              # RGB 渐变色文本
│   ├── tab/              # Tab 在线列表
│   ├── title/            # Title 称号
│   └── warehouse/        # Warehouse 仓库银行
```

## 构建

```bash
# 构建宿主
./gradlew :axs-core:shadowJar

# 构建全部模块 Jar
./gradlew buildModules

# 构建单个模块
./gradlew :modules:rgb:jar

# 全量构建（宿主 + 所有模块）
./gradlew :axs-core:shadowJar buildModules
```

## 部署

```
plugins/
  ArcartXSuite.jar              ← axs-core/build/libs/ArcartXSuite-x.x.x.jar
  ArcartXSuite/
    config.yml                  ← 模块启用/密码配置
    modules/                    ← 按需放入模块 Jar
      AXS-RGB-x.x.x.jar
      AXS-Tab-x.x.x.jar
      AXS-Pickup-x.x.x.jar
      ...
```

### 说明

1. 将 `axs-core` 产出的 ShadowJar 重命名为 `ArcartXSuite.jar` 放入 `plugins/`
2. 在 `plugins/ArcartXSuite/modules/` 目录下放入需要的模块 Jar
3. 在 `config.yml` 中配置模块的 `enabled` 和 `password`
4. 启动服务器，宿主自动扫描 `modules/` 目录
5. 有外部 Jar 的模块由模块 Jar 接管，无外部 Jar 的模块走内置加载

## 启动流程

```
onEnable()
  ├── 初始化桥接 (packetBridge, clientBridge, itemStackBridge, propBridge, vaultEconomyBridge)
  ├── 创建 ModuleRegistry
  ├── scanAvailableModuleIds() → 预扫描 modules/ 目录
  ├── 对每个模块:
  │     externalModuleIds.contains(id) → 跳过内置加载（交给 ModuleRegistry）
  │     否则 → 走内置 reloadXxxState() 加载
  ├── printModuleStatusSummary()
  ├── moduleRegistry.loadAll() → 按拓扑排序加载外部模块 Jar
  └── 完成
```

> **关键设计**：短路求值 `externalModuleIds.contains(id) || reloadXxxState(true)` 避免双重初始化。  
> 外部模块的 `onEnable()` 负责调用宿主 `reloadXxxState()` 或使用自建 Service。

## 重载流程

### `axs reload all`

对每个模块：
- 如果外部 Jar 已加载 → 调用 `moduleRegistry.reloadModule(id)` → 触发模块 `onReload()`
- 如果走内置 → 调用 `plugin.reloadXxxState(true)`

### `axs reload <module>`

单模块重载遵循同样逻辑，通过 `isExternalModule()` 判断走外部还是内置路径。

### UI 注册/更新

每个有 UI 的模块在 reload 时完整执行：

| 步骤 | 操作 |
|------|------|
| 1 | `shutdownXxxModule()` → 停止 Service + `unregisterXxxUi()` |
| 2 | 加载配置 → 导出 UI 文件 |
| 3 | `prepareUiBinding()` → `packetBridge.registerOrReloadUi()` |
| 4 | 创建并启动新 Service |

`registerOrReloadUi()` 先尝试 reload（已注册则更新），再尝试 register（未注册则注册），是**幂等安全**的。  
ArcartX 现已支持 UI 自动导入，不再需要手动执行 `ax reload`。

## 模块实现模式

### 独立模式

模块自建 Service，完全不依赖宿主业务逻辑。适合逻辑简单或已完全解耦的模块。

```java
public final class RgbModule implements AXSModule {
    private RgbService service;

    @Override
    public boolean onEnable(ModuleContext context) throws Exception {
        // 自建 Service，从 context 获取 plugin/logger/bridges
        service = new RgbService(context);
        service.start();
        return true;
    }

    @Override
    public void onDisable() {
        if (service != null) service.shutdown();
    }

    @Override
    public void onReload() throws Exception {
        onDisable();
        // re-enable with saved context
    }
}
```

### 委托模式

模块的 `onEnable`/`onDisable` 委托给宿主的 `reloadXxxState()`/`shutdownXxxModule()`。  
适合 Service 与宿主紧密耦合、短期内不宜全面解耦的模块。

```java
public final class AnnouncerModule implements AXSModule {
    private ModuleContext context;
    private boolean ready;

    @Override
    public boolean onEnable(ModuleContext context) throws Exception {
        this.context = context;
        ArcartXSuitePlugin plugin = (ArcartXSuitePlugin) context.plugin();
        ready = plugin.reloadAnnouncerState(true);
        return ready;
    }

    @Override
    public void onDisable() {
        if (ready) {
            ArcartXSuitePlugin plugin = (ArcartXSuitePlugin) context.plugin();
            plugin.shutdownAnnouncerModule();
        }
        ready = false;
    }

    @Override
    public void onReload() throws Exception {
        onDisable();
        if (context != null) onEnable(context);
    }
}
```

## 模块开发指南

### 1. 实现 `AXSModule` 接口

```java
public final class MyModule implements AXSModule {
    @Override
    public ModuleDescriptor descriptor() {
        return ModuleDescriptor.builder("mymodule")
            .name("MyModule")
            .version("1.0.0")
            .mainClass(getClass().getName())
            .build();
    }

    @Override
    public boolean onEnable(ModuleContext context) throws Exception {
        // 初始化逻辑
        return true;
    }

    @Override
    public void onDisable() {
        // 清理逻辑
    }

    @Override
    public void onReload() throws Exception {
        // 重载逻辑（通常 disable + enable）
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
```

### 2. 创建 `module.yml`

```yaml
id: mymodule
name: MyModule
version: 1.0.0
main: com.example.MyModule
api-version: 1.0
depends: []
softdepends: []
external-depends: []
external-softdepends: []
```

### 3. 注册命令（可选）

实现 `ModuleCommandHandler` 接口即可自动注册 `/axs mymodule ...` 子命令：

```java
public final class MyModule implements AXSModule, ModuleCommandHandler {
    @Override
    public String commandId() { return "mymodule"; }

    @Override
    public List<String> actions() { return List.of("help", "status", "reload"); }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        // 命令处理
        return true;
    }
}
```

### 4. `ModuleContext` 可用能力

| 方法 | 说明 |
|------|------|
| `plugin()` | 宿主 JavaPlugin 实例 |
| `logger()` | 模块专用 Logger |
| `packetBridge()` | ArcartX 发包桥接 |
| `clientBridge()` | ArcartX 客户端桥接 |
| `itemStackBridge()` | ItemStack 桥接 |
| `packetGuard()` | 客户端包守卫 |
| `hasPlugin(String)` | 检查外部插件是否可用 |

## 核心组件

| 组件 | 说明 |
|------|------|
| `AXSModule` | 模块生命周期接口（onEnable / onDisable / onReload / isReady） |
| `ModuleContext` | 宿主暴露给模块的上下文（plugin / bridges / logger） |
| `ModuleDescriptor` | 模块元数据（id / name / version / depends） |
| `ModuleCommandHandler` | 可选命令处理接口（commandId / actions / onCommand） |
| `ModuleRegistry` | 模块扫描 / 加载 / 启用 / 禁用 / 重载 |
| `ModuleClassLoader` | 模块隔离 ClassLoader |
| `DefaultModuleContext` | ModuleContext 默认实现 |

## 迁移状态

| 模块 Jar | 对应功能模块 | 模式 | UI | 说明 |
|----------|-------------|------|-----|------|
| rgb | RGB | ✅ 独立 | — | 自建 ArcartRgbService |
| pickup | Pickup | ✅ 独立 | HUD | 自建 PickupService |
| tab | Tab | ✅ 独立 | — | 自建 TabSyncService |
| combateffect | CombatEffect + DigisDisplay | ✅ 独立 | — | 自建 CombatEffectService，DigisDisplay 随 CombatEffect 加载 |
| announcer | Announcer + Subtitle | 🔗 委托 | HUD | reloadAnnouncerState，Subtitle 随 Announcer 加载 |
| entitytracker | EntityTracker + AttackTarget | 🔗 委托 | HUD | reloadEntityTrackerState，AttackTarget 随 EntityTracker 加载 |
| chat | Chat | 🔗 委托 | — | reloadChatState |
| conversation | Conversation | 🔗 委托 | UI+Selector | reloadConversationState |
| eventpacket | EventPacket | 🔗 委托 | — | reloadEventPacketState |
| loginview | LoginView | 🔗 委托 | UI | reloadLoginViewState |
| mail | Mail | 🔗 委托 | — | reloadMailState |
| map | Map | 🔗 委托 | Menu+HUD | reloadMapState |
| onlinerewards | OnlineRewards | 🔗 委托 | — | reloadOnlineRewardsState |
| prop | Prop | 🔗 委托 | — | reloadPropState |
| questgps | QuestGPS | 🔗 委托 | Menu+HUD | reloadQuestGpsState |
| title | Title | 🔗 委托 | — | reloadTitleState |
| warehouse | Warehouse | 🔗 委托 | — | reloadWarehouseState |

> **委托模式**：模块 Jar 控制「是否加载」，业务逻辑仍在宿主中执行。  
> 后续可逐步将 Service 源码搬到模块子项目实现完全解耦。

## 近期变更记录

### v2 — 统一启动流程 & 移除 ax reload

- **EntityTracker 流程统一**：`reloadEntityTrackerState` 签名简化为 `(boolean logSummary)`，与其他模块一致
- **移除 ArcartX 自动 reload**：不再执行 `ax reload true`，ArcartX 已支持 UI 自动导入
- **移除 Hybrid Bootstrap**：不再有延迟重试机制（`scheduleHybridBootstrap`）
- **预扫描避免双重初始化**：`ModuleRegistry.scanAvailableModuleIds()` 在内置加载前执行
- **命令感知外部模块**：`axs reload all / axs reload <module>` 自动判断走外部模块还是内置路径
- **完善模块 onDisable**：所有 16 个委托模块正确调用 `shutdownXxxModule()` 清理资源

### v1 — 模块化基础架构

- Gradle 多模块结构 + axs-api 接口定义
- ModuleRegistry + ModuleClassLoader + ModuleContext
- 4 个独立实现模块 (RGB, Pickup, Tab, CombatEffect)
- 13 个委托实现模块
- 动态命令注册 (ModuleCommandHandler)
