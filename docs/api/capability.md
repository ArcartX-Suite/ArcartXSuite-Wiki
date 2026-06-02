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
