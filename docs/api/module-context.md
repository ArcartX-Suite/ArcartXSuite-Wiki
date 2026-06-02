# ModuleContext 上下文

`ModuleContext` 是宿主提供给每个模块的上下文接口。模块通过此接口获取基础设施能力（桥接、文件、UI 注册、事件、命令等），而无需直接引用宿主插件主类。

在 `onEnable(ModuleContext context)` 时注入，模块应将其保存为成员变量。

## 基础设施

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `plugin()` | `JavaPlugin` | 宿主插件实例（用于注册事件、调度 BukkitTask） |
| `logger()` | `Logger` | 带模块前缀的 Logger |
| `dataFolder()` | `File` | 模块私有数据目录（`plugins/ArcartXSuite/data/<moduleId>/`） |
| `uiFolder()` | `File` | UI 文件输出目录（`plugins/ArcartXSuite/ui/`） |
| `pluginDataFolder()` | `File` | 宿主插件数据目录（`plugins/ArcartXSuite/`） |
| `hasPlugin(String)` | `boolean` | 检查外部 Bukkit 插件是否已安装 |

### 数据迁移工具

用于将 1.0.x 时代散落在根目录的旧数据归位到模块私有数据目录：

```java
// 迁移单个文件（同时处理 SQLite 的 -shm/-wal 后缀）
File dataDir = context.migrateLegacyDataFile("chat.db");

// 迁移整个目录
File channelsDir = context.migrateLegacyDirectory("chat/channels");
```

## 桥接 API

模块通过以下方法获取类型安全的 ArcartX 桥接接口。所有桥接在 ArcartX 未安装时返回 `null`。

| 方法 | 返回类型 | 稳定性 | 说明 |
|------|----------|--------|------|
| `packetBridge()` | `PacketBridgeAPI` | `@Stable` | UI / Packet 桥接 |
| `clientBridge()` | `ClientBridgeAPI` | `@Stable` | 客户端桥接（伤害飘字、变量下发） |
| `itemStackBridge()` | `ItemBridgeAPI` | `@Stable` | ItemStack → JSON 序列化 |

```java
PacketBridgeAPI bridge = context.packetBridge();
if (bridge != null && bridge.isAvailable()) {
    bridge.openUi(player, "my_ui");
    bridge.sendPacket(player, "my_ui", "update", Map.of("key", "value"));
}
```

详见 [桥接 API 参考](./bridge-api)。

## 账号识别服务

宿主统一提供微软正版 / LittleSkin / 离线账号判定，供 LoginView、QQBot、EventPacket 等模块共享，取代各模块自行实现且不一致的判定逻辑。

| 方法 | 返回类型 | 稳定性 | 说明 |
|------|----------|--------|------|
| `accountTypeService()` | `AccountTypeService` | `@Stable` | 宿主统一账号识别服务，**永不为 null** |

```java
AccountTypeService accounts = context.accountTypeService();

// 主线程安全（走缓存，非阻塞）
AccountType type = accounts.resolve(player);
if (type.premium()) {
    // 微软正版或 LittleSkin，可免密直接进服
}

// 异步线程（如 AsyncPlayerPreLoginEvent）允许阻塞查询 Mojang 并写缓存
AccountType resolved = accounts.resolveBlocking(uuid, name);
```

`AccountType` 取值：

| 取值 | id | premium | 判定条件 |
|------|-----|---------|----------|
| `MICROSOFT` | `microsoft` | 是 | 玩家名在 Mojang 正版库存在（无论 UUID 为 v3 离线或 v4 在线） |
| `LITTLESKIN` | `littleskin` | 是 | 玩家名不在 Mojang，且 UUID 为 v4（已通过 yggdrasil 认证） |
| `OFFLINE` | `offline` | 否 | v3 离线 UUID 且不在 Mojang |

::: tip
判定开关位于宿主 `config.yml` 的 `account-type` 节（`enable-mojang-lookup` / `mojang-timeout-ms` / `debug`）。服务在 `AsyncPlayerPreLogin` 阶段异步预热缓存，因此 `resolve()` 在玩家进服后通常命中缓存，可在主线程安全调用。
:::

## 模块间通信

### 查找模块

```java
// 按类型查找
Optional<TitleModule> title = context.getModule(TitleModule.class);

// 按 id 查找
Optional<AXSModule> module = context.getModule("warehouse");
```

### Capability 跨模块通信

Capability 是 ArcartXSuite 推荐的跨模块通信方式。模块通过注册 Capability 暴露自己的能力，其他模块通过查找 Capability 来调用。

```java
// 注册 Capability（通常在 startService 中）
context.registerCapability(MailDispatchable.class, mailService);

// 查找 Capability（通常用 Supplier 延迟查找）
Supplier<MailDispatchable> mailSupplier = () -> context.getCapability(MailDispatchable.class);
```

| 方法 | 稳定性 | 说明 |
|------|--------|------|
| `registerCapability(Class<T>, T)` | `@Stable` | 注册能力接口实例，onDisable 时自动注销 |
| `getCapability(Class<T>)` | `@Stable` | 按类型查找已注册的能力实例，未找到返回 `null` |

详见 [Capability 接口](./capability)。

## 事件与命令注册

### 事件监听器

```java
// 注册（由宿主管理生命周期）
context.registerListener(new MyListener());

// 注销当前模块所有监听器
context.unregisterListeners();
```

### 命令绑定

```java
// 绑定命令（命令名必须在 plugin.yml 中已声明）
context.registerCommand("mycommand", new MyCommand());
```

::: tip
使用 `AbstractAXSModule` 时，通过 `commandBindings()` 返回映射即可自动绑定，无需手动调用。
:::

### PlaceholderAPI

```java
context.registerPlaceholderExpansion(new MyPlaceholderExpansion());
context.unregisterPlaceholderExpansions();
```

## 客户端事件路由 {#clientpackethandler}

### ClientPacketHandler

客户端自定义包处理器。实现后注册到宿主，宿主按优先级顺序分发客户端回包。

```java
@FunctionalInterface
public interface ClientPacketHandler {
    /**
     * @param player   发包玩家
     * @param packetId 数据包 id
     * @param data     数据负载
     * @return true 表示已消费此包，后续处理器不再接收
     */
    boolean handleClientPacket(Player player, String packetId, List<String> data);
}
```

```java
// 注册（默认优先级 0）
context.registerClientPacketHandler((player, packetId, data) -> {
    if ("my_packet".equals(packetId)) {
        // 处理
        return true;
    }
    return false;
});

// 注册（指定优先级，数值越小越优先）
context.registerClientPacketHandler(handler, 100);
```

### ClientInitializedHandler {#clientinitializedhandler}

客户端初始化完成回调。当 ArcartX 客户端初始化完成时通知所有已注册的处理器。

```java
@FunctionalInterface
public interface ClientInitializedHandler {
    void onClientInitialized(Player player);
}
```

```java
context.registerClientInitializedHandler(player -> {
    // 客户端已就绪，可以安全发包
    bridge.sendPacket(player, "my_ui", "init", payload);
});
```

## 资源导出

### 配置资源

```java
// 导出模块 Jar 内的配置文件到宿主数据目录
context.exportResource("defaults/my_config.yml", targetFile, false);
context.exportConfigResource("defaults/extra.yml", "data/mymodule/extra.yml", false, getClass().getClassLoader());
```

### UI 资源

```java
// 导出 UI 文件
File uiFile = context.exportUiResource(
    "arcartx/ui/my_view.yml",   // Jar 内路径
    "ui/my_view.yml",            // 相对输出路径
    false,                        // 是否覆写
    getClass().getClassLoader()
);
```

### UI 注册与注销

```java
// 高层 API（推荐，自动处理注册 + 绑定）
UiBinding binding = context.prepareUiBinding("MyModule", "my_ui", true, uiFile);
if (binding != null) {
    String runtimeUiId = binding.runtimeUiId();
    String registeredUiId = binding.registeredUiId();
}

// 注销
context.unregisterUi(registeredUiId);
```

### 受保护资源

```java
// 读取模块 Jar 内的受保护资源流
InputStream stream = context.openProtectedResource("data/template.dat", getClass().getClassLoader());
```
