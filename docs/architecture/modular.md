# 模块化架构

ArcartXSuite 1.0.2-beta 使用 **宿主 + 模块 Jar** 架构。宿主（`axs-core`）提供核心基础设施，各功能模块可以打包为独立 Jar 放入 `modules/` 目录按需加载。

## 项目结构

```
ArcartXSuite/
├── axs-api/              # 模块 API 接口层（AXSModule, ModuleContext 等）
├── axs-core/             # 宿主核心（ShadowJar 输出）
├── modules/
│   ├── announcer/        # Announcer 播报 + Subtitle 字幕
│   ├── entitytracker/    # EntityTracker 实体追踪 + 目标 HUD
│   ├── chat/             # Chat 频道聊天
│   ├── conversation/     # Conversation 对话桥
│   ├── eventpacket/      # EventPacket 事件引擎
│   ├── combateffect/     # CombatEffect 战斗特效 + 伤害飘字
│   ├── loginview/        # LoginView 登录界面
│   ├── mail/             # Mail 邮箱
│   ├── map/              # Map 世界地图
│   ├── onlinerewards/    # OnlineRewards 在线奖励
│   ├── pickup/           # Pickup 拾取提示
│   ├── prop/             # Prop 快捷道具
│   ├── questgps/         # QuestGPS 任务导航
│   ├── rgb/              # RGB 渐变色文本
│   ├── tab/              # Tab 在线列表
│   ├── title/            # Title 称号
│   └── warehouse/        # Warehouse 仓库银行
```

## 核心组件

| 组件 | 包路径 | 说明 |
|------|--------|------|
| `AXSModule` | `axs-api` | 模块生命周期接口：`onEnable` / `onDisable` / `onReload` / `isReady` |
| `ModuleContext` | `axs-api` | 宿主暴露给模块的上下文：plugin 实例、Logger、各种 Bridge |
| `ModuleDescriptor` | `axs-api` | 模块元数据：id / name / version / depends |
| `ModuleCommandHandler` | `axs-api` | 可选命令处理接口，实现后自动注册 `/axs <moduleId>` 子命令 |
| `ModuleRegistry` | `axs-core` | 模块扫描 / 加载 / 启用 / 禁用 / 重载 |
| `ModuleClassLoader` | `axs-core` | 模块隔离 ClassLoader，每个模块 Jar 独立加载 |
| `DefaultModuleContext` | `axs-core` | `ModuleContext` 的默认实现 |

## 启动流程

```
onEnable()
  ├── 初始化反射桥 (packetBridge, clientBridge, itemStackBridge …)
  ├── 创建 ModuleRegistry
  ├── scanAvailableModuleIds()
  │     └── 预扫描 modules/ 目录，收集所有外部模块 Jar 的 id
  ├── 对每个内置模块:
  │     externalModuleIds.contains(id) → 跳过（交给 ModuleRegistry）
  │     否则 → reloadXxxState(true) 执行内置加载
  ├── printModuleStatusSummary()
  ├── moduleRegistry.loadAll()
  │     └── 按拓扑排序加载所有外部模块 Jar
  └── 加载完成
```

**关键设计**：短路求值 `externalModuleIds.contains(id) || reloadXxxState(true)` 防止双重初始化。外部模块 Jar 的 `onEnable()` 内部会调用宿主 `reloadXxxState()` 或使用自建 Service。

## 重载流程

### `/axs reload all`

对每个模块判断加载来源：

- **外部 Jar 已加载** → `moduleRegistry.reloadModule(id)` → 触发模块 `onReload()`
- **内置加载** → `plugin.reloadXxxState(true)`

### `/axs reload <模块名>`

单模块重载遵循同样逻辑，通过 `isExternalModule()` 判断走外部还是内置路径。

### UI 注册与更新

每个有 UI 的模块在 reload 时严格执行四步：

| 步骤 | 操作 |
|------|------|
| 1 | `shutdownXxxModule()` — 停止 Service + `unregisterXxxUi()` 注销旧 UI |
| 2 | 加载新配置 → 导出 UI YAML 文件到 ArcartX 目录 |
| 3 | `prepareUiBinding()` → `packetBridge.registerOrReloadUi()` 注册/更新 UI |
| 4 | 创建并启动新 Service |

`registerOrReloadUi()` 先尝试 reload（已注册则刷新），再尝试 register（未注册则注册），是**幂等安全**的。ArcartX 现已支持 UI 自动导入，不再需要手动执行 `ax reload`。

## 模块实现模式

### 独立模式

模块自建 Service，完全不依赖宿主业务逻辑。适合逻辑简单或已完全解耦的模块。

```java
public final class RgbModule implements AXSModule {
    private ModuleContext context;
    private RgbService service;

    @Override
    public ModuleDescriptor descriptor() {
        return ModuleDescriptor.builder("rgb")
            .name("RGB").version("1.0.2-beta")
            .mainClass(getClass().getName()).build();
    }

    @Override
    public boolean onEnable(ModuleContext context) throws Exception {
        this.context = context;
        service = new RgbService(context);
        service.start();
        return true;
    }

    @Override
    public void onDisable() {
        if (service != null) { service.shutdown(); service = null; }
    }

    @Override
    public void onReload() throws Exception {
        onDisable();
        if (context != null) onEnable(context);
    }
}
```

### 委托模式

模块的 `onEnable`/`onDisable` 委托给宿主 `reloadXxxState()` / `shutdownXxxModule()`。适合 Service 与宿主紧密耦合的模块。

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
            ((ArcartXSuitePlugin) context.plugin()).shutdownAnnouncerModule();
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

## 模块 Jar 描述文件

每个模块 Jar 在 `resources/` 中必须包含 `module.yml`：

```yaml
id: mymodule           # 唯一标识，与 config.yml 中的键对应
name: MyModule         # 显示名称
version: 1.0.2-beta
main: com.example.MyModule   # AXSModule 实现类全限定名
api-version: 1.0
depends: []            # 强依赖的其他模块 id
softdepends: []        # 软依赖的其他模块 id
external-depends: []   # 强依赖的外部 Bukkit 插件名
external-softdepends: []
```

## `ModuleContext` API

| 方法 | 说明 |
|------|------|
| `plugin()` | 宿主 `JavaPlugin` 实例 |
| `logger()` | 模块专用 `Logger` |
| `packetBridge()` | ArcartX 发包桥接 |
| `clientBridge()` | ArcartX 客户端桥接 |
| `itemStackBridge()` | ItemStack 桥接 |
| `packetGuard()` | 客户端包守卫 |
| `hasPlugin(String)` | 检查外部 Bukkit 插件是否可用 |

## 迁移状态

| 模块 Jar | 对应功能模块 | 模式 | UI | 说明 |
|----------|-------------|------|-----|------|
| rgb | RGB | ✅ 独立 | — | 自建 ArcartRgbService |
| pickup | Pickup | ✅ 独立 | HUD | 自建 PickupService |
| tab | Tab | ✅ 独立 | — | 自建 TabSyncService |
| combateffect | CombatEffect + 伤害飘字 | ✅ 独立 | — | 自建 CombatEffectService，伤害飘字随 CombatEffect 加载 |
| announcer | Announcer + Subtitle | 🔗 委托 | HUD | reloadAnnouncerState，Subtitle 随 Announcer 加载 |
| entitytracker | EntityTracker + 目标 HUD | 🔗 委托 | HUD | reloadEntityTrackerState，目标 HUD 随 EntityTracker 加载 |
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

> **委托模式**下模块 Jar 只控制「是否加载」，业务逻辑仍在宿主中执行。后续可逐步将 Service 源码搬入模块子项目实现完全解耦。

