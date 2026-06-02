# 模块化架构

ArcartXSuite 1.1.0-beta 使用 **宿主 + 模块 Jar** 架构。宿主（`axs-core`）提供核心基础设施，各功能模块可以打包为独立 Jar 放入 `modules/` 目录按需加载。

## 项目结构

```
ArcartXSuite/
├── axs-api/              # 模块 API 接口层（AXSModule, ModuleContext 等）
├── axs-core/             # 宿主核心（ShadowJar 输出）
├── modules/
│   ├── announcer/        # Announcer 播报 + Subtitle 字幕
│   ├── entitytracker/    # EntityTracker 实体追踪 + 目标 HUD
│   ├── essentials/       # Essentials 基础工具 (传送/管控/砍树/InvActions)
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
│   ├── regions/          # Regions 区域保护 + 世界规则
│   ├── rgb/              # RGB 渐变色文本
│   ├── tab/              # Tab 在线列表
│   ├── title/            # Title 称号
│   ├── warehouse/        # Warehouse 仓库银行
│   ├── market/           # Market 全球市场
│   └── qqbot/            # QQBot QQ群服互联
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

**关键设计**：短路求值 `externalModuleIds.contains(id) || reloadXxxState(true)` 防止双重初始化。外部模块 Jar 通过自建 Service 完全独立运行；无外部 Jar 的模块走宿主内置加载。

## 重载流程

### `/axs reload all`

对每个模块判断加载来源：

- **外部 Jar 已加载** → `moduleRegistry.reloadModule(id)` → 触发模块 `onReload()`
- **内置加载** → `plugin.reloadXxxState(true)`

### `/axs reload <模块名>`

单模块重载遵循同样逻辑，通过 `isExternalModule()` 判断走外部还是内置路径。

## 热加载 / 热卸载

不同于 `reload`（onDisable + onEnable 在同一个 ClassLoader 内复位），`/axs load|unload` 提供真正的运行时插拔：

### `/axs load <模块名>`

1. 检查模块未加载（已加载则拒绝，提示走 reload）。
2. 扫描 `modules/` 目录寻找 id 匹配的 jar。
3. license 校验（`LicenseService.isModuleAllowed(id)`）。
4. 进入与启动期相同的 `loadAndEnable(DiscoveredModule)` 流程：
   - 检查外部插件依赖 / AXS 模块依赖（depends）
   - 创建独立 `ModuleClassLoader`（URLClassLoader 子类）
   - 实例化 `AXSModule` 主类
   - 构建 `DefaultModuleContext`
   - 注册模块的 `ModuleConfigSpec`（用于配置诊断）
   - 调用 `instance.onEnable(context)`
5. 失败时调用 `cleanupFailedModule(id)` 回滚（onDisable + 关闭 ClassLoader + 从 modules 表移除）。

### `/axs unload <模块名>`

1. **反向依赖检查**：遍历所有已启用模块的 `descriptor.depends()`，若存在依赖该模块的 dependent，则拒绝卸载并提示 dependents 列表。
2. 执行 `disableModule(loaded)`：
   - `commandHandlers.remove(id)` — 取消 `/axs <id>` 子命令
   - `instance.onDisable()` — 模块自清理（停止 Service、注销事件、关闭数据库）
   - 标记 `loaded.setEnabled(false)`
3. `removePacketHandlers(id)` — 移除该模块注册的 `ClientPacketHandler`。
4. `modules.remove(id)` — 从注册表移除。
5. `closeClassLoader(loaded)` — 调用 `URLClassLoader.close()` 释放 jar 文件句柄。

### 已知约束

- **UI 残留**：ArcartX 的 UI 不支持显式 unregister，卸载后旧 UI 仍由 ArcartX 持有，但 packetHandler 已断开。重新 `load` 会重新 export + register 覆盖旧 UI。
- **Capability 清理**：当前 capability 表不跟踪 owner，模块需在 `onDisable` 中自行清理 capabilities（否则旧引用会持有死对象）。`ModuleRegistry.removeCapabilities(id)` 保留作为接口契约位。
- **依赖图变化**：`unload` 不会自动 disable dependents，要求管理员按依赖顺序手动 unload。

### UI 注册与更新

每个有 UI 的模块在 reload 时严格执行四步：

| 步骤 | 操作 |
|------|------|
| 1 | 停止旧 Service 并注销旧 UI |
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
            .name("RGB").version("1.1.0-beta")
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

### 委托模式（历史说明）

1.1.0-beta 早期有部分模块采用「委托模式」——模块 Jar 的 `onEnable`/`onDisable` 委托给宿主的 `reloadXxxState()` / `shutdownXxxModule()`，业务逻辑仍在宿主中执行。截至 2026-05-14，**全部 17 个基础模块已完成独立化迁移**，委托模式已退出使用。

当前所有模块均使用「独立模式」实现，业务逻辑封装在模块自身的 `XxxService` 中，宿主只负责提供 `ModuleContext` 基础设施。

## 模块 Jar 描述文件

每个模块 Jar 在 `resources/` 中必须包含 `module.yml`：

```yaml
id: mymodule           # 唯一标识，与 config.yml 中的键对应
name: MyModule         # 显示名称
version: 1.1.0-beta
main: com.example.MyModule   # AXSModule 实现类全限定名
api-version: 1.0
depends: []            # 强依赖的其他模块 id
softdepends: []        # 软依赖的其他模块 id
external-depends: []   # 强依赖的外部 Bukkit 插件名
external-softdepends: []
```

## `ModuleContext` API

完整参考见 [ModuleContext 上下文](/api/module-context)。

| 方法 | 返回类型 | 稳定性 | 说明 |
|------|----------|--------|------|
| `plugin()` | `JavaPlugin` | — | 宿主插件实例 |
| `logger()` | `Logger` | — | 模块专用 Logger |
| `dataFolder()` | `File` | — | 模块私有数据目录（`plugins/ArcartXSuite/data/<moduleId>/`） |
| `pluginDataFolder()` | `File` | — | 宿主插件数据目录（`plugins/ArcartXSuite/`） |
| `packetBridge()` | `PacketBridgeAPI` | `@Stable` | ArcartX UI/Packet 桥接 |
| `clientBridge()` | `ClientBridgeAPI` | `@Stable` | ArcartX 客户端桥接 |
| `itemStackBridge()` | `ItemBridgeAPI` | `@Stable` | ItemStack → JSON |
| `packetGuard()` | `PacketGuardAPI` | `@Stable` | 客户端包守卫（频率限制） |
| `accountTypeService()` | `AccountTypeService` | `@Stable` | 统一账号识别服务（微软正版 / LittleSkin / 离线） |
| `itemSourceRegistry()` | `ItemSourceRegistry` | `@Stable` | 全局物品来源注册表 |
| `itemMatcher()` | `ItemMatcherAPI` | `@Stable` | 全局物品匹配器 |
| `currencyManager()` | `CurrencyBridgeAPI` | `@Stable` | 全局货币管理器 |
| `attributeBridge()` | `AttributeBridgeRegistry` | `@Stable` | 全局属性桥接注册表 |
| `registerCapability()` | `void` | `@Stable` | 注册跨模块能力 |
| `getCapability()` | `T` | `@Stable` | 查找跨模块能力 |
| `hasPlugin(String)` | `boolean` | — | 检查外部 Bukkit 插件 |

## 迁移状态

| 模块 Jar | 对应功能模块 | 模式 | UI | 说明 |
|----------|-------------|------|-----|------|
| rgb | RGB | ✅ 独立 | — | 自建 ArcartRgbService |
| pickup | Pickup | ✅ 独立 | HUD | 自建 PickupService |
| tab | Tab | ✅ 独立 | — | 自建 TabSyncService |
| combateffect | CombatEffect + 伤害飘字 | ✅ 独立 | — | 自建 CombatEffectService，伤害飘字随 CombatEffect 加载 |
| announcer | Announcer + Subtitle | ✅ 独立 | HUD | 自建 AnnouncerService，Subtitle 随 Announcer 加载 |
| entitytracker | EntityTracker + 目标 HUD | ✅ 独立 | HUD | 自建 EntityTrackerService，目标 HUD 随 EntityTracker 加载 |
| chat | Chat | ✅ 独立 | — | 自建 ChatService |
| conversation | Conversation | ✅ 独立 | UI+Selector | 自建 ConversationService |
| eventpacket | EventPacket | ✅ 独立 | — | 自建 EventPacketDispatchService |
| loginview | LoginView | ✅ 独立 | UI | 自建 LoginViewService |
| mail | Mail | ✅ 独立 | — | 自建 MailService |
| map | Map | ✅ 独立 | Menu+HUD | 自建 MapService |
| onlinerewards | OnlineRewards | ✅ 独立 | — | 自建 OnlineRewardsService |
| prop | Prop | ✅ 独立 | — | 自建 PropService |
| questgps | QuestGPS | ✅ 独立 | Menu+HUD | 自建 QuestGpsService |
| title | Title | ✅ 独立 | — | 自建 TitleService |
| warehouse | Warehouse | ✅ 独立 | — | 自建 WarehouseService |
| essentials | Essentials | ✅ 独立 | UI | 自建 EssentialsService，玩家菜单 + 管理员面板 |
| regions | Regions | ✅ 独立 | UI | 自建 RegionsService，区域查看 + 管理编辑面板 |
| market | Market | ✅ 独立 | UI | 自建 MarketService，商店 + 拍卖行 + 回收 |
| qqbot | QQBot | ✅ 独立 | — | 自建 QQBotService，OneBot 11 WebSocket 连接 |

> 所有 17 个基础模块均已完成独立化迁移，业务逻辑封装在各自的 `XxxService` 中，由模块 Jar 自行管理生命周期。宿主仅提供基础设施（`ModuleContext`、反射桥、配置诊断等），不再包含模块业务源码。

