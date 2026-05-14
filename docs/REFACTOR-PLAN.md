# ArcartXSuite 模块化重构方案

> 目标：将「宿主包含近乎全部功能 + 模块 Jar 仅起调用作用」改为「各模块负责自身功能逻辑 + UI + 配置，宿主仅负责激活和调用各模块」。

---

## 一、现状分析

### 1.1 数据量

| 维度 | 数量 |
|------|------|
| `ArcartXSuitePlugin.java` 行数 | **4167 行** |
| 模块专属字段 (config + service + file + uiId) | ~100 个 |
| `reloadXxxState()` 方法 | 17 个 (每个 30-80 行) |
| `shutdownXxxModule()` 方法 | 17 个 |
| `ensureXxxConfigExists()` 方法 | 18 个 |
| `loadXxxConfiguration()` 方法 | 17 个 |
| `unregisterXxxUi()` 方法 | 10 个 |
| getter / `isXxxReady()` / `getXxxFile()` | 80+ 个 |
| 源码包 `src/main/java/xuanmo/arcartxsuite/` | 325 个 Java 文件 |
| 配置 YAML `src/main/resources/ArcartX*.yml` | 18 个 |
| UI YAML `src/main/resources/arcartx/ui/` | 21 个文件，合计 ~460KB |

### 1.2 当前模块分类

| 模式 | 模块 | 特征 |
|------|------|------|
| **委托** (13个) | announcer, entitytracker, chat, conversation, eventpacket, loginview, mail, map, onlinerewards, prop, questgps, title, warehouse | 模块 Jar 只有 1 个入口类，`onEnable()` 直接调用 `plugin.reloadXxxState()` |
| **半独立** (4个) | rgb, pickup, tab, combateffect | 模块 Jar 有独立入口但 Service 源码仍在 `src/main/java/` 被编进 axs-core |

### 1.3 关键耦合点

1. **`handleClientCustomPacketEvent()`** — 宿主里的大型 switch 路由，将客户端回包分发给各模块 Service
2. **`handleClientInitializedEvent()`** — 客户端初始化事件通知各模块
3. **`registerCommands()`** — 9 个玩家命令 + 1 个管理命令全在宿主注册
4. **`registerPlaceholderExpansionIfAvailable()`** — 7 个模块的 PAPI 占位符集中注册
5. **`dispatchEventPackets()` / `dispatchCombatEffectPackets()`** — 事件分发逻辑引用多个模块 Service
6. **`plugin.yml`** — commands 和 permissions 定义绑定在宿主
7. **资源加密** — axs-core 的 `ProtectYamlResourcesTask` 加密 `src/main/resources/` 下的 YAML

---

## 二、目标架构

```
重构后:
ArcartXSuite/
├── axs-api/                    # 模块 API + 共享接口 (不变，但需扩展)
├── axs-core/                   # 宿主核心 (大幅瘦身)
│   └── src/main/java/          # 仅保留：主类、桥接、安全、模块注册、共享工具
│   └── src/main/resources/     # 仅保留：plugin.yml、config.yml
├── modules/
│   ├── rgb/
│   │   └── src/main/java/      # 完整源码：config/, service/, placeholder/, shimmer/
│   │   └── src/main/resources/ # ArcartXRGB.yml (默认配置)
│   ├── loginview/
│   │   └── src/main/java/      # 完整源码：config/, service/, storage/, auth/, security/
│   │   └── src/main/resources/ # ArcartXLoginView.yml + arcartx/ui/login_view.yml
│   └── ...                     # 其他模块同理
```

### 2.1 宿主 (axs-core) 最终应保留的包

```
xuanmo.arcartxsuite/
├── ArcartXSuitePlugin.java     # 瘦身后 ~500 行：桥接初始化、模块注册、事件路由框架
├── bridge/                     # ArcartXPacketBridge, ClientBridge, ItemStackBridge, PropBridge, VaultBridge
├── combat/                     # EntityCombatMetadata, TaczCombatBridge (共享战斗元数据)
├── command/                    # ArcartXSuiteCommand (管理命令框架，模块命令委托给 ModuleCommandHandler)
├── config/                     # ProtectedResourceStore, YamlConfigSynchronizer, DefaultConfigResourceRegistry
├── currency/                   # CurrencyProvider 接口 (如模块间需要经济交互)
├── item/                       # ItemStack 相关共享工具
├── module/                     # ModuleRegistry, ModuleClassLoader, DefaultModuleContext
├── mythiclib/                  # MythicLib 共享桥接
├── security/                   # ClientPacketGuard, ModulePasswordAuthenticator
└── util/                       # 共享工具类
```

### 2.2 宿主主类瘦身后的 onEnable 流程

```java
@Override
public void onEnable() {
    // 1. 基础设施
    ensureRootConfigExists();
    reloadRootConfiguration();
    serverPlatform = ServerPlatform.detect(getServer());
    printStartupBanner();

    // 2. 桥接初始化
    packetBridge = new ArcartXPacketBridge(this); ...
    clientBridge = new ArcartXClientBridge(this); ...
    itemStackBridge = new ArcartXItemStackBridge(this); ...
    propBridge = new ArcartXPropBridge(this); ...
    vaultEconomyBridge = new VaultEconomyBridge(this); ...

    // 3. 注册 ArcartX 事件监听 (路由框架)
    registerClientCustomPacketRouter();
    registerClientInitializedRouter();

    // 4. 注册管理命令
    registerAdminCommand();

    // 5. 模块加载 (全部由 ModuleRegistry 管理)
    moduleRegistry = new ModuleRegistry(...);
    moduleRegistry.loadAll();
    printModuleStatusSummary();
}
```

**关键变化：不再有 `reloadXxxState()` 内置加载分支和 `externalModuleIds.contains(id) ||` 短路逻辑。所有模块都是外部 Jar。**

---

## 三、API 层扩展 (axs-api)

### 3.1 ModuleContext 需新增的方法

```java
public interface ModuleContext {
    // === 已有 (保留) ===
    JavaPlugin plugin();
    Logger logger();
    File dataFolder();
    File uiFolder();
    Object packetBridge();
    Object clientBridge();
    Object itemStackBridge();
    Object packetGuard();
    <T extends AXSModule> Optional<T> getModule(Class<T> moduleClass);
    Optional<AXSModule> getModule(String moduleId);
    InputStream openProtectedResource(String resourcePath, ClassLoader loader);
    void exportResource(String resourcePath, File target, boolean overwrite);
    UiBinding prepareUiBinding(String moduleName, String configuredUiId, boolean registerOnEnable, File uiFile);
    boolean hasPlugin(String pluginName);

    // === 新增 ===

    /** 宿主插件数据目录 (plugins/ArcartXSuite/) — 模块配置文件仍放在此处以保持用户习惯 */
    File pluginDataFolder();

    /** Vault 经济桥接 (可能为 null) */
    Object vaultEconomyBridge();

    /** ArcartX Prop 桥接 (可能为 null) */
    Object propBridge();

    /** 注册 Bukkit 事件监听器 (宿主管理生命周期) */
    void registerListener(Listener listener);

    /** 注销模块注册的所有事件监听器 */
    void unregisterListeners();

    /** 注册玩家命令 (委托到 plugin.yml 中已声明的命令) */
    void registerCommand(String commandName, TabExecutor executor);

    /** 注册 PlaceholderAPI 占位符 */
    boolean registerPlaceholderExpansion(Object expansion);

    /** 注销模块注册的所有占位符 */
    void unregisterPlaceholderExpansions();

    /** 注册客户端包处理器 — 模块声明自己能处理的 packetId */
    void registerClientPacketHandler(ClientPacketHandler handler);

    /** 注册客户端初始化处理器 — 模块声明需要在客户端初始化时收到通知 */
    void registerClientInitializedHandler(ClientInitializedHandler handler);

    /** 导出 UI 资源到 ui/ 目录 (模块调用) */
    File exportUiResource(String resourcePath, String relativeUiPath, boolean overwrite, ClassLoader loader) throws IOException;

    /** 从模块 Jar 导出配置到宿主数据目录 */
    File exportConfigResource(String resourcePath, String targetRelativePath, boolean overwrite, ClassLoader loader);
}
```

### 3.2 新增接口

```java
/** 客户端自定义包处理器 — 模块实现此接口后通过 context 注册 */
public interface ClientPacketHandler {
    /** 尝试处理包，返回 true 表示已消费 */
    boolean handleClientPacket(Player player, String packetId, List<String> data);
}

/** 客户端初始化回调 */
public interface ClientInitializedHandler {
    void onClientInitialized(Player player);
}
```

### 3.3 ModuleCommandHandler 扩展

当前 `ModuleCommandHandler` 只处理 `/axs <moduleId> ...` 子命令。  
新增方法让模块声明自己需要的独立玩家命令：

```java
public interface ModuleCommandHandler {
    // ... 已有 ...

    /** 模块需要的独立命令名列表 (如 ["title", "warehouse"]) — 必须在 plugin.yml 中已声明 */
    default List<String> standaloneCommands() { return List.of(); }
}
```

---

## 四、事件路由重构

### 4.1 客户端回包路由

**现状：** `handleClientCustomPacketEvent()` 里手写 if-else 链逐个尝试各模块 Service。  
**重构后：** 宿主维护一个 `List<ClientPacketHandler>` 注册表，模块在 `onEnable()` 时通过 `context.registerClientPacketHandler(handler)` 注册，宿主按序遍历。

```java
// 宿主中:
private final List<ClientPacketHandler> packetHandlers = new CopyOnWriteArrayList<>();

private void handleClientCustomPacketEvent(...) {
    for (ClientPacketHandler handler : packetHandlers) {
        if (handler.handleClientPacket(player, packetId, data)) {
            return; // 已消费
        }
    }
}
```

### 4.2 客户端初始化路由

同理，宿主维护 `List<ClientInitializedHandler>`，模块按需注册。

### 4.3 CombatEffect / EventPacket 分发

这两个模块的 `dispatchXxxPackets()` 方法目前在宿主中。重构后：
- `CombatEffectListener` 和 `PlayerEventPacketListener` 由各自模块注册
- 模块间交互通过 `context.getModule("eventpacket")` 获取实例，或定义 API 接口

---

## 五、命令注册重构

### 5.1 问题

`plugin.yml` 中定义了 9 个玩家命令 (title, warehouse, mail, chat, msg, reply, onlinerewards, questgps, map)。  
Bukkit 要求命令在 `plugin.yml` 中声明才能由 `getCommand()` 获取。模块 Jar 无法自行声明。

### 5.2 方案

保留 `plugin.yml` 中的命令声明，但改为**延迟绑定**：
1. `plugin.yml` 声明所有命令 (不变)
2. 宿主 `onEnable()` 不再硬编码注册 executor
3. 模块在 `onEnable()` 中调用 `context.registerCommand("title", titleExecutor)`
4. `DefaultModuleContext.registerCommand()` 实际执行 `plugin.getCommand(name).setExecutor(executor)`
5. 模块 `onDisable()` 时自动解绑 (设为空执行器)

这样命令的 Executor 逻辑完全在模块里，宿主只提供命令名的"槽位"。

### 5.3 Permissions

Permissions 也保留在 `plugin.yml` 中（Bukkit 限制），但可以提取成常量。

---

## 六、资源加密重构

### 6.1 问题

当前 `ProtectYamlResourcesTask` 只加密 `src/main/resources/` 下的 YAML。  
重构后各模块有自己的资源，也需要加密。

### 6.2 方案

1. 在根 `build.gradle.kts` 中添加一个通用的 `protectModuleResources` 任务
2. 或在每个模块的 `build.gradle.kts` 中应用相同的 Gradle 插件/逻辑
3. 推荐：**抽取 `ProtectYamlResourcesTask` 到 `buildSrc/`**，所有子项目复用

```kotlin
// modules/rgb/build.gradle.kts
plugins {
    id("java")
    id("axs-resource-protection") // 从 buildSrc 引入
}
```

---

## 七、每个模块迁移清单

### 7.0 通用迁移步骤 (适用于每个模块)

```
□ 1. 将 src/main/java/xuanmo/arcartxsuite/<module>/ 下所有源文件移到 modules/<module>/src/main/java/
□ 2. 将 src/main/resources/ArcartX<Module>.yml 移到 modules/<module>/src/main/resources/
□ 3. 将 src/main/resources/arcartx/ui/<module>*.yml 移到 modules/<module>/src/main/resources/arcartx/ui/
□ 4. 将模块额外资源移到模块目录 (如 chat/channels/, prop/*, subtitle/groups/, eventpacket/*)
□ 5. 更新 modules/<module>/build.gradle.kts 添加所需 dependencies
□ 6. 重写模块入口类：从委托模式改为独立模式 (自行加载配置/Service/UI/命令/监听器)
□ 7. 从 ArcartXSuitePlugin.java 删除对应的:
      - import 语句
      - 字段 (config, service, configFile, runtimeUiId, registeredUiId)
      - reloadXxxState() 方法
      - shutdownXxxModule() 方法
      - ensureXxxConfigExists() / loadXxxConfiguration() 方法
      - unregisterXxxUi() 方法
      - getter / isXxxReady() 方法
      - handleClientCustomPacketEvent() 中的对应分支
      - handleClientInitializedEvent() 中的对应分支
      - registerCommands() 中的对应命令注册
      - registerPlaceholderExpansionIfAvailable() 中的对应分支
□ 8. 更新 module.yml 描述符
□ 9. 构建验证
```

### 7.1 各模块具体细节

#### RGB (最简单 — 无 UI, 无 DB, 无命令, 无监听器)

| 项 | 值 |
|----|-----|
| 源码包 | `rgb/` (8 文件: config/4 + placeholder/1 + service/2 + shimmer/1) |
| 配置 | `ArcartXRGB.yml` |
| UI | 无 |
| DB | 无 |
| 命令 | 无 |
| PAPI | `%arcartrgb_%` |
| 外部依赖 | PlaceholderAPI |
| 现有模块 Jar | **已半独立**，`RgbModule.java` 自建 Service |
| 迁移难度 | ⭐ |

**迁移要点：**
- 将 `src/main/java/xuanmo/arcartxsuite/rgb/` → `modules/rgb/src/main/java/xuanmo/arcartxsuite/rgb/`
- `RgbModule.java` 已基本完整，只需自行注册 PAPI 占位符

---

#### Tab (无 UI 文件, 无 DB, 无独立命令)

| 项 | 值 |
|----|-----|
| 源码包 | `tab/` (18 文件) |
| 配置 | `ArcartXTab.yml` |
| UI | 无 (发送 TAB 数据包) |
| DB | 无 |
| 命令 | 无独立命令，仅 `/axs tab ...` |
| PAPI | 无 |
| 外部依赖 | PlaceholderAPI |
| 客户端回包 | ✅ `handleTabClientRefreshPacket()` |
| 迁移难度 | ⭐⭐ |

---

#### Pickup (有 HUD UI, 无 DB)

| 项 | 值 |
|----|-----|
| 源码包 | `pickup/` (3 目录: config/1 + service/1 + ui/1) |
| 配置 | `ArcartXPickup.yml` |
| UI | `pickup_hud.yml` (动态生成) |
| DB | 无 |
| 命令 | 无 |
| 迁移难度 | ⭐⭐ |

---

#### Announcer + Subtitle (捆绑, 有 HUD UI, 无 DB)

| 项 | 值 |
|----|-----|
| 源码包 | `announcer/` (3 目录) + `subtitle/` (4 目录) |
| 配置 | `ArcartXAnnouncer.yml` + `ArcartXSubtitle.yml` |
| UI | `announcer_hud.yml` + `subtitle_hud.yml` |
| DB | 无 |
| 命令 | 无独立命令 |
| 客户端回包 | ✅ `handleAnnouncerClientPacket()` |
| 客户端初始化 | ✅ `markClientInitialized()` |
| 额外资源 | `subtitle/groups/default.yml` |
| 迁移难度 | ⭐⭐⭐ |

---

#### CombatEffect + DigisDisplay (捆绑, 无 UI 注册, 有事件监听)

| 项 | 值 |
|----|-----|
| 源码包 | `combateffect/` (15 文件: packet/ + display/) |
| 配置 | `ArcartXCombatEffect.yml` (共享 kill-effect 和 digis-display) |
| UI | 无自有 UI (通过发包到其他 UI) |
| DB | 无 |
| 命令 | 无 |
| 事件监听 | `CombatEffectListener` + `dispatchCombatEffectPackets()` |
| 外部依赖 | MythicLib(可选), AttributePlus(可选), CraneAttribute(可选) |
| 迁移难度 | ⭐⭐⭐ |

---

#### EventPacket (有事件监听, 有客户端回包, 引用多个模块)

| 项 | 值 |
|----|-----|
| 源码包 | `eventpacket/` (8 文件) |
| 配置 | `ArcartXEventPacket.yml` |
| UI | 无 |
| DB | 无 |
| 命令 | 无 |
| 事件监听 | `PlayerEventPacketListener` |
| 客户端回包 | ✅ `handleClientPacketViaEventPacket()` |
| 跨模块引用 | **大量** — questgps, subtitle, chat, title, mail, announcer, combateffect |
| 额外资源 | `eventpacket/packet-command-presets/` |
| 迁移难度 | ⭐⭐⭐⭐⭐ (需要定义模块间通信接口) |

**特殊处理：** EventPacket 的 `executeXxxAction()` 方法引用了 questgps, subtitle, title, mail, announcer 等模块。  
需要定义 `EventPacketActionExecutor` SPI，各模块注册自己能处理的 action type。

---

#### EntityTracker + AttackTarget (有 MythicMobs 依赖, 有 HUD UI, 有 PAPI)

| 项 | 值 |
|----|-----|
| 源码包 | `entitytracker/` (34 文件: boss/ + target/) |
| 配置 | `ArcartXEntityTracker.yml` |
| UI | `boss_tracker.yml` + `attack_target_hud.yml` |
| DB | 无 |
| PAPI | `%AXSentitytracker_%` |
| 外部依赖 | MythicMobs/MythicBukkit, AttributePlus(可选) |
| 迁移难度 | ⭐⭐⭐ |

---

#### LoginView (有 UI, 有 DB, 有客户端回包+初始化)

| 项 | 值 |
|----|-----|
| 源码包 | `loginview/` (9 文件: config/ + service/ + storage/4 + auth/ + security/ + migration/) |
| 配置 | `ArcartXLoginView.yml` |
| UI | `login_view.yml` |
| DB | SQLite/MySQL (JdbcLoginViewRepository) |
| 命令 | 无独立命令 |
| 客户端回包 | ✅ |
| 客户端初始化 | ✅ |
| 外部依赖 | 无 (AuthMe 可选) |
| 迁移难度 | ⭐⭐⭐ |

---

#### Title (有 UI, 有 DB, 有命令, 有 PAPI)

| 项 | 值 |
|----|-----|
| 源码包 | `title/` (33 文件) |
| 配置 | `ArcartXTitle.yml` |
| UI | `title_menu.yml` |
| DB | SQLite/MySQL (JdbcTitleRepository) |
| 命令 | `/title` |
| PAPI | `%AXStitle_%` |
| 客户端回包 | ✅ |
| 外部依赖 | MythicLib(可选) |
| build.gradle.kts 额外依赖 | `at.favre.lib:bcrypt` (密码相关?), MythicLib |
| 迁移难度 | ⭐⭐⭐⭐ |

---

#### Mail (有 UI x4, 有 DB, 有命令, 有 PAPI, Redis 可选)

| 项 | 值 |
|----|-----|
| 源码包 | `mail/` (37 文件) |
| 配置 | `ArcartXMail.yml` |
| UI | `mail_inbox.yml` + `mail_compose.yml` + `mail_admin.yml` + `mail_logs.yml` |
| DB | SQLite/MySQL (JdbcMailRepository) |
| 命令 | `/mail`, `/axmail` |
| PAPI | `%AXSmail_%` |
| 客户端回包 | ✅ |
| Vault | ✅ (邮件邮资) |
| Redis | ✅ (跨服) |
| 额外资源 | `mail/presets/` |
| 迁移难度 | ⭐⭐⭐⭐ |

---

#### Warehouse (有 UI x3, 有 DB, 有命令, 有 PAPI)

| 项 | 值 |
|----|-----|
| 源码包 | `warehouse/` (9 目录) |
| 配置 | `ArcartXWarehouse.yml` |
| UI | `warehouse_menu.yml` + `warehouse_manage.yml` + `warehouse_bank.yml` |
| DB | SQLite/MySQL (JdbcWarehouseRepository) |
| 命令 | `/warehouse`, `/wh` |
| PAPI | `%AXSwarehouse_%` |
| 客户端回包 | ✅ |
| 迁移难度 | ⭐⭐⭐⭐ |

---

#### Chat (有 DB, 有命令 x3, 有 PAPI, Redis 可选)

| 项 | 值 |
|----|-----|
| 源码包 | `chat/` (30 文件) |
| 配置 | `ArcartXChat.yml` |
| UI | 无 (ArcartX 聊天卡片) |
| DB | SQLite/MySQL (JdbcChatRepository) |
| 命令 | `/chat`, `/msg`, `/reply` (`/r`) |
| PAPI | `%AXSchat_%` |
| 客户端回包 | 无 |
| Redis | ✅ (跨服) |
| 额外资源 | `chat/channels/*.yml` |
| ItemStackBridge | ✅ (展示物品) |
| 迁移难度 | ⭐⭐⭐⭐ |

---

#### OnlineRewards (有 UI, 有 DB, 有命令, 有 PAPI, 有客户端回包+初始化)

| 项 | 值 |
|----|-----|
| 源码包 | `onlinerewards/` (33 文件) |
| 配置 | `ArcartXOnlineRewards.yml` |
| UI | `online_rewards_menu.yml` |
| DB | SQLite/MySQL (JdbcOnlineRewardsRepository) |
| 命令 | `/onlinerewards`, `/signin` |
| PAPI | `%AXSonlinerewards_%` |
| 客户端回包 | ✅ |
| 客户端初始化 | ✅ |
| ClientBridge | ✅ (服务端变量) |
| 迁移难度 | ⭐⭐⭐⭐ |

---

#### Conversation (有 UI x2, Chemdah 依赖, 有客户端回包)

| 项 | 值 |
|----|-----|
| 源码包 | `conversation/` (7 文件) |
| 配置 | `ArcartXConversation.yml` |
| UI | `conversation_menu.yml` + `conversation_selector_hud.yml` |
| DB | 无 |
| 命令 | 无 |
| 客户端回包 | ✅ |
| 外部依赖 | **Chemdah**(必需), Adyeshach(可选) |
| libs 依赖 | `Chemdah-1.1.8.jar` |
| 迁移难度 | ⭐⭐⭐ |

---

#### QuestGPS (有 UI x2, Chemdah 依赖, 有命令, 有客户端回包)

| 项 | 值 |
|----|-----|
| 源码包 | `questgps/` (11 文件) |
| 配置 | `ArcartXQuestGPS.yml` |
| UI | `questgps_menu.yml` + `questgps_hud.yml` |
| DB | 无 |
| 命令 | `/questgps` |
| 客户端回包 | ✅ |
| 外部依赖 | **Chemdah**(必需) |
| ItemStackBridge | ✅ |
| 迁移难度 | ⭐⭐⭐ |

---

#### Map (有 UI x2, 有 DB, 有命令, 有客户端回包+初始化)

| 项 | 值 |
|----|-----|
| 源码包 | `map/` (12 文件) |
| 配置 | `ArcartXMap.yml` |
| UI | `map_menu.yml` + `map_hud.yml` |
| DB | SQLite/MySQL (JdbcMapRepository) |
| 命令 | `/map`, `/axmap` |
| 客户端回包 | ✅ |
| 客户端初始化 | ✅ |
| 迁移难度 | ⭐⭐⭐⭐ |

---

#### Prop (有 ArcartX Prop 桥接, 有客户端初始化)

| 项 | 值 |
|----|-----|
| 源码包 | `prop/` (14 文件) |
| 配置 | `ArcartXProp.yml` |
| UI | 无 |
| DB | 无 |
| 命令 | 无 |
| 客户端初始化 | ✅ |
| 外部依赖 | MythicLib(可选) |
| PropBridge | ✅ |
| 额外资源 | `prop/key.yml`, `prop/language.yml`, `prop/props/道具示例.yml` |
| 迁移难度 | ⭐⭐⭐ |

---

## 八、推荐迁移顺序

按依赖复杂度从低到高，建议分 5 批：

### Batch 1 — 基础设施 + 最简模块
1. **扩展 `axs-api`** — 新增 ModuleContext 方法 + ClientPacketHandler/ClientInitializedHandler 接口
2. **扩展 `DefaultModuleContext`** — 实现新方法
3. **宿主改造** — 客户端包路由改为注册表模式，命令改为延迟绑定
4. **提取 `buildSrc`** — 资源加密任务复用
5. **RGB** — 第一个完全独立模块，验证完整流程

### Batch 2 — 无 DB 的简单模块
6. **Tab**
7. **Pickup**
8. **Announcer + Subtitle**
9. **CombatEffect + DigisDisplay**
10. **Prop**

### Batch 3 — 有 DB 的模块
11. **LoginView**
12. **Title**
13. **OnlineRewards**

### Batch 4 — 有外部插件依赖或复杂交互的模块
14. **EntityTracker + AttackTarget**
15. **Conversation**
16. **QuestGPS**
17. **Map**
18. **Warehouse**
19. **Mail**
20. **Chat**

### Batch 5 — EventPacket + 收尾
21. **EventPacket** (最后迁移，因为它引用几乎所有其他模块)
22. **清理宿主** — 删除所有已迁出代码
23. **更新 axs-core `build.gradle.kts`** — sourceSets 不再包含模块源码
24. **全量构建 + 验证**

---

## 九、构建系统变更

### 9.1 `axs-core/build.gradle.kts`

```kotlin
// 重构后:
sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java"))   // 不再引用 ../src/main/java
        resources {
            setSrcDirs(listOf("src/main/resources", protectedResourcesDir))
        }
    }
}
```

axs-core 将有自己的 `src/main/java/` 目录，只包含宿主代码。

### 9.2 模块 `build.gradle.kts` 模板

```kotlin
plugins {
    id("java")
    id("axs-resource-protection") // 从 buildSrc 引入
}

dependencies {
    compileOnly(project(":axs-api"))
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    // 模块特有的依赖:
    // compileOnly("me.clip:placeholderapi:2.11.7")
    // implementation("com.zaxxer:HikariCP:5.1.0") // 有 DB 的模块
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.jar {
    archiveBaseName.set("ArcartXSuite-LoginView")
    archiveClassifier.set("")
}
```

**关键变化：** `compileOnly(project(":axs-core"))` → 改为 `compileOnly(project(":axs-api"))`。  
模块不再编译时依赖 axs-core，只依赖 API 接口层。

### 9.3 有 DB 的模块需要的额外依赖

```kotlin
// 方案 A: 模块 shadow 自己的 DB 驱动
implementation("com.zaxxer:HikariCP:5.1.0")
implementation("org.xerial:sqlite-jdbc:3.46.0.0")
implementation("com.mysql:mysql-connector-j:8.4.0")

// 方案 B: 将 DB 公共层抽到 axs-api 或 axs-common 模块
// 推荐方案 B，避免每个模块重复打包 ~10MB 的 JDBC 驱动
```

**推荐：** 创建 `axs-common` 模块，包含 JDBC 公共层。模块 `compileOnly` 引用它，axs-core shadow 时打包它。

### 9.4 buildSrc

```
buildSrc/
  src/main/kotlin/
    AxsResourceProtectionPlugin.kt   # 复用 ProtectYamlResourcesTask
```

---

## 十、向下兼容与部署

### 10.1 用户影响

| 方面 | 变化 |
|------|------|
| 配置文件路径 | **不变** — 仍在 `plugins/ArcartXSuite/` |
| UI 文件路径 | **不变** — 仍在 `plugins/ArcartXSuite/ui/` |
| 命令 | **不变** |
| 占位符 | **不变** |
| 部署方式 | **变化** — 必须放入对应模块 Jar 才能使用功能 |

### 10.2 部署示例 (重构后)

```
plugins/
  ArcartXSuite.jar                ← axs-core (瘦身后，~2MB → ~500KB)
  ArcartXSuite/
    config.yml
    modules/
      AXS-RGB-1.0.0.jar          ← 包含完整 RGB 功能
      AXS-LoginView-1.0.0.jar    ← 包含完整 LoginView 功能
      AXS-Mail-1.0.0.jar         ← 包含完整 Mail 功能
      ...
```

### 10.3 移除内置加载逻辑

重构完成后，`ArcartXSuitePlugin.onEnable()` 中的 `externalModuleIds.contains(id) || reloadXxxState(true)` 短路逻辑全部移除。  
如果用户没有放入某个模块 Jar，该功能就不可用 — 这正是目标行为。

---

## 十一、风险与注意事项

| 风险 | 缓解措施 |
|------|---------|
| EventPacket 跨模块引用 | 定义 `EventPacketActionExecutor` SPI，各模块注册处理器 |
| DB 驱动重复打包 | 抽取 `axs-common` 或让 axs-core shadow 统一提供 |
| 模块间循环依赖 | 严格通过 `context.getModule()` 软引用，不在 build 依赖中交叉引用 |
| 资源加密密钥 | 各模块共享相同密钥 (从 buildSrc 统一提供) |
| plugin.yml 命令声明 | 保持在 plugin.yml，模块延迟绑定 executor |
| 构建产物变大 | 每个模块 Jar 需要包含自己的 class，总体磁盘增加但单个模块更独立 |
| 测试 | 现有测试在 axs-core，需迁移到对应模块或保留集成测试 |

---

## 十二、工作量估算

| 阶段 | 内容 | 预估工作量 |
|------|------|-----------|
| Batch 1 | API 扩展 + 基础设施 + RGB | 大 (奠基) |
| Batch 2 | 5 个简单模块 | 中 |
| Batch 3 | 3 个 DB 模块 | 中-大 |
| Batch 4 | 7 个复杂模块 | 大 |
| Batch 5 | EventPacket + 清理 + 验证 | 中 |

**总计：这是一个大规模重构项目，建议按 Batch 逐步推进，每完成一个 Batch 做一次全量构建验证。**
