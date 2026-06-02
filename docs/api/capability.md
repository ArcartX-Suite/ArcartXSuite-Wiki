# Capability 跨模块通信

Capability 是 ArcartXSuite 推荐的跨模块通信机制。模块通过 `ModuleContext` 注册自己提供的能力接口，其他模块通过类型查找来调用，实现松耦合的模块间协作。

## 工作原理

```
┌──────────┐  registerCapability   ┌──────────────┐  getCapability   ┌──────────┐
│ Title    │ ─────────────────────→│  Capability  │←──────────────── │ EventPkt │
│ Module   │  TitleGrantable.class │  Registry    │  TitleGrantable  │ Module   │
└──────────┘                       └──────────────┘                  └──────────┘
```

1. **提供方**在 `startService()` 中注册 Capability
2. **消费方**通过 `Supplier` 延迟查找，避免加载顺序问题
3. 模块 `onDisable` 时宿主自动注销其注册的所有 Capability

## 使用示例

### 提供方（注册 Capability）

```java
public class TitleModule extends AbstractAXSModule {

    @Override
    protected void startService() {
        TitleService service = new TitleService(context, config);
        service.start();
        // 注册能力接口
        context.registerCapability(TitleGrantable.class, service);
    }
}
```

### 消费方（查找 Capability）

```java
public class EventPacketModule extends AbstractAXSModule {

    @Override
    protected void startService() {
        // 使用 Supplier 延迟查找，容忍目标模块尚未加载
        Supplier<TitleGrantable> titleSupplier =
            () -> context.getCapability(TitleGrantable.class);

        dispatchService = new EventPacketDispatchService(titleSupplier);
        dispatchService.start();
    }
}

// 在服务中使用
public class EventPacketDispatchService {
    private final Supplier<TitleGrantable> titleSupplier;

    public void grantTitle(Player player, String titleId) {
        TitleGrantable title = titleSupplier.get();
        if (title != null) {
            title.grantTitle(player, titleId);
        }
    }
}
```

## 内置 Capability 接口

以下是 ArcartXSuite 内置模块注册的 Capability 接口，第三方模块可通过 `getCapability()` 查找并调用。

### MailDispatchable

由 Mail 模块注册，按预设模板发送邮件。

```java
public interface MailDispatchable {
    void dispatchTemplateMail(Player player, String templateId);
}
```

**使用场景：** EventPacket 等模块在触发特定事件时自动发送邮件。

### TitleGrantable

由 Title 模块注册，给予/移除玩家称号。

```java
public interface TitleGrantable {
    void grantTitle(Player player, String titleId);
}
```

### SubtitlePlayable

由 Announcer/Subtitle 模块注册，播放字幕组。

```java
public interface SubtitlePlayable {
    void playSubtitle(Player player, String groupId);
}
```

### ChatCardSendable

由 Chat 模块注册，发送聊天卡片。

```java
public interface ChatCardSendable {
    void sendChatCard(Player player, String cardId, Map<String, String> data);
}
```

### QuestGpsNavigable

由 QuestGPS 模块注册，任务导航控制。

```java
public interface QuestGpsNavigable {
    void offerQuest(Player player, String questId, boolean openMenu);
    void startTracking(Player player, String questId);
    void stopTracking(Player player);
}
```

### MapNavigable

由 Map 模块注册，地图外部导航 + 菜单打开。

```java
public interface MapNavigable {
    void setExternalNavigation(Player player, String label, Location target);
    void clearExternalNavigation(Player player);
    void openMenuFor(Player player);
}
```

### TabRefreshable

由 Tab 模块注册，触发 Tab 列表刷新。

```java
public interface TabRefreshable {
    void refreshTab(Player player);
}
```

**使用场景：** Title、Chat 等模块在数据变更时通知 Tab 刷新。

### TitleConfigQueryable

由 Title 模块注册，查询称号配置信息。

```java
public interface TitleConfigQueryable {
    // 根据称号 ID 查询称号信息
}
```

### CombatEffectTriggerable

由 CombatEffect 模块注册，跨模块触发战斗特效。

```java
public interface CombatEffectTriggerable {
    void triggerPacket(Player player, String packetId, Map<String, Object> extraVars);
    void triggerDirect(Player player, String uiId, String handler, Map<String, Object> payload);
}
```

### QQBotBroadcastable

由 QQBot 模块注册，供其他模块推送消息到 QQ 群。

```java
public interface QQBotBroadcastable {
    void broadcastToGroups(String message);
    void sendToGroup(long groupId, String message);
}
```

**使用场景：** EventPacket、Mail 等模块在特定事件时向 QQ 群推送通知。

### SignalDispatchable

由 EventPacket 模块注册，供其他模块触发信号。

```java
public interface SignalDispatchable {
    void dispatchSignal(Player player, String signalId);
}
```

**使用场景：** OnlineRewards 等模块在特定条件下触发 EventPacket 规则引擎。

### PlayerDataPurgeable

由各持久化存储模块注册（多实例 Capability），供 `/axs purge` 命令统一调度玩家数据删除。

```java
public interface PlayerDataPurgeable {
    @NotNull String moduleId();
    int purgePlayerData(@NotNull UUID playerUuid);
    default int purgeAllPlayerData() { return -1; }
}
```

**已注册模块：** qqbot、warehouse、eventpacket、map、essentials、title、chat、mail、onlinerewards

**特殊说明：**
- 这是唯一一个支持多实例注册的 Capability（每个模块各注册一个实例）
- `purgePlayerData` 删除指定玩家数据，`purgeAllPlayerData` 清空模块全部玩家数据表
- 底层由 `AbstractModuleRepository.deletePlayerData(UUID)` / `deleteAllPlayerData()` 实现
- 未注册此 Capability 的模块（如 market、loginview、regions）在 purge 时会被跳过

### ChatMutable

由 Chat 模块注册，供 Essentials 等模块执行禁言/解禁操作。

```java
public interface ChatMutable {
    @NotNull String mutePlayer(@NotNull String playerName, @Nullable Instant expiresAt,
                                @Nullable String reason, @Nullable String mutedBy);
    @NotNull String unmutePlayer(@NotNull String playerName);
    boolean isMuted(@NotNull UUID playerUuid);
}
```

**使用场景：** Essentials 的管理命令调用 Chat 模块执行禁言/解禁。

### DatabaseMigratable

由各持久化存储模块注册，供宿主 `/axs migrate` 命令统一调度跨源数据库迁移。

```java
public interface DatabaseMigratable {
    @NotNull String moduleId();
    @NotNull MigrationResult migrateDatabase(@NotNull StorageDescriptor targetDescriptor, boolean overwriteTarget);
    @NotNull StorageDescriptor currentDescriptor();
}
```

**已注册模块：** chat、essentials、eventpacket、loginview、mail、map、market、onlinerewards、qqbot、regions、title、warehouse

### PickupNotifiable

由 Pickup 模块注册，供 Warehouse 等模块查询玩家拾取通知状态。

```java
public interface PickupNotifiable {
    boolean isNotificationActive(UUID playerId);
}
```

**使用场景：** Warehouse 自动入库前判断是否需要额外发送聊天栏提示。

### QQBotNotifiable

由 QQBot 模块注册，供其他模块监听 QQ 群事件。

```java
public interface QQBotNotifiable {
    void registerListener(@NotNull QQGroupEventListener listener);
    void unregisterListener(@NotNull QQGroupEventListener listener);

    interface QQGroupEventListener {
        default void onGroupMessage(long groupId, long senderId, @NotNull String nickname, @NotNull String message) {}
        default void onMemberJoin(long groupId, long userId) {}
        default void onMemberLeave(long groupId, long userId) {}
    }
}
```

**使用场景：** 其他模块需要在 QQ 群成员加入/离开时触发游戏内动作。

### QqBindCapable

由 QQBot 模块注册，供 LoginView 等模块查询玩家 QQ 绑定状态。

```java
public interface QqBindCapable {
    boolean isBound(@NotNull UUID playerUuid);
    @Nullable Long getBoundQqId(@NotNull UUID playerUuid);
    @NotNull BindResult confirmBind(@NotNull Player player, @NotNull String code);

    record BindResult(boolean success, @Nullable Long qqId, @NotNull String message) {}
}
```

**使用场景：** LoginView 登录面板判断玩家是否需要完成 QQ 绑定才能进服。

### WarehouseAutoDepositable

由 Warehouse 模块注册，供 Pickup 等模块在不打开 UI 的情况下直接存入物品。

```java
public interface WarehouseAutoDepositable {
    @NotNull DepositResult depositToPersonalWarehouse(@NotNull Player player, @NotNull ItemStack itemStack);

    record DepositResult(boolean success, long storedAmount, int remainingAmount, @NotNull String message) {}
}
```

**使用场景：** Pickup 模块在玩家拾取物品时自动尝试存入仓库。

### `EssentialsQueryable`

由 Essentials 模块注册，供 Tab、Chat 等模块查询玩家 AFK/Vanish/Mute/Nick/Flying/GodMode 状态。

```java
public interface EssentialsQueryable {
    boolean isAfk(@NotNull UUID playerUuid);
    boolean isVanished(@NotNull UUID playerUuid);
    boolean isMuted(@NotNull UUID playerUuid);
    @Nullable String getNickname(@NotNull UUID playerUuid);
    boolean isFlying(@NotNull UUID playerUuid);
    boolean isGodMode(@NotNull UUID playerUuid);
}
```

**使用场景：** Tab 模块在渲染玩家列表时查询隐身/AFK 状态；Chat 模块查询昵称和禁言状态。

### `EventBusCapability`

模块间解耦事件总线（pub/sub 模式）。任何模块都可以发布事件，其他模块订阅感兴趣的主题。

```java
public interface EventBusCapability {
    void publish(@NotNull String topic, @Nullable Player player, @NotNull Map<String, String> payload);
    String subscribe(@NotNull String topic, @NotNull EventHandler handler);
    void unsubscribe(@NotNull String subscriptionId);

    record BusEvent(@NotNull String topic, @Nullable Player player, @NotNull Map<String, String> payload, long timestamp) {}
    interface EventHandler { void handle(@NotNull BusEvent event); }
}
```

**使用场景：** Market 模块发布 `market.purchase` 事件，QQBot 模块订阅后推送到 QQ 群。与 `SignalDispatchable`（面向 EventPacket 规则引擎的 1 对 1）不同，EventBusCapability 面向所有模块的 1 对多 pub/sub。

### `InteractionState`

由拥有交互式 HUD/Menu 的模块注册（如 Conversation），供其他模块在共享按键时判断是否应让步。

```java
public interface InteractionState {
    boolean isPlayerInteracting(@NotNull Player player);
}
```

**使用场景：** Conversation 模块注册后，Pickup 模块在处理 `AXS_INTERACT`（默认 F 键）时先查询 `isPlayerInteracting`，若玩家正在对话则不抢占按键。

## 自定义 Capability

第三方模块可以定义自己的 Capability 接口并注册：

### 1. 定义接口

```java
// 放在你的模块 API 包中
public interface MyCustomAbility {
    void doSomething(Player player, String param);
    boolean isSupported(String feature);
}
```

### 2. 实现并注册

```java
public class MyModule extends AbstractAXSModule {
    @Override
    protected void startService() {
        MyService service = new MyService(context, config);
        service.start();
        context.registerCapability(MyCustomAbility.class, service);
    }
}
```

### 3. 其他模块消费

```java
MyCustomAbility ability = context.getCapability(MyCustomAbility.class);
if (ability != null && ability.isSupported("feature_x")) {
    ability.doSomething(player, "hello");
}
```

## 最佳实践

- **使用 `Supplier` 延迟查找**：避免因模块加载顺序导致 `getCapability` 返回 `null`
- **始终做 `null` 检查**：目标模块可能未安装或未启用
- **注册时机**：在 `startService()` 中注册，确保服务已完全初始化
- **接口粒度**：Capability 接口应聚焦单一职责，避免暴露过多内部细节
- **文档化**：如果你的 Capability 供第三方使用，务必提供清晰的 Javadoc

## 消息外部化

`api.message.MessageProvider` 提供模块消息的外部化加载，支持 `&` 颜色码和 `{0}` 占位符：

```java
// 在模块 startService() 中初始化
MessageProvider msg = new MessageProvider(context.dataFolder(), "messages.yml", getClass().getClassLoader(), context.logger());
msg.load();

// 使用
player.sendMessage(msg.get("purge.confirm", "10"));
```

模块首次加载时自动从 JAR 导出默认 `messages.yml`；用户可自定义文本而无需修改代码。
