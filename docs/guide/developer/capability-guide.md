---
title: Capability 详解 | ArcartX-Suite 开发者指南
description: ArcartX-Suite 跨模块通信机制 Capability——原理、内置能力表、提供方与使用方开发教程、多实例与 EventBus。
---

# Capability 详解

**Capability** 是 ArcartX-Suite 各模块之间协作的 **标准方式**。宿主在 `ModuleRegistry` 中维护一张「能力注册表」：模块启动时注册自己提供的接口，其他模块按 **Java 接口类型** 查找并调用——全程无需 `import` 对方实现类。

::: info 为什么用 Capability？
| 方式 | 问题 |
|------|------|
| 直接 `import` 其他模块的 Service | ClassLoader 隔离、循环依赖、版本耦合 |
| Bukkit 自定义事件 | 适合广播，不适合「调用对方业务方法」 |
| **Capability** | 只依赖 `axs-api` 中的接口，松耦合、可 softdepends |
:::

接口方法签名速查见 [Capability API 参考](/api/capability)。本文侧重 **如何开发与使用**。

## 架构一览

```mermaid
sequenceDiagram
    participant Title as Title 模块
    participant Registry as ModuleRegistry
    participant Event as EventPacket 模块

    Title->>Registry: registerCapability(TitleGrantable, impl)
    Note over Registry: capabilities 表: TitleGrantable → impl
    Event->>Registry: getCapability(TitleGrantable.class)
    Registry-->>Event: TitleGrantable 实例
    Event->>Title: giveTitle(playerId, titleId, ...)
```

数据流：

```
提供方 startService()
    └── context.registerCapability(接口.class, 实现)

使用方 startService()
    └── capability = context.getCapability(接口.class)  // 可能为 null

宿主 onDisable(提供方)
    └── removeCapabilities(moduleId)  // 自动清理
```

## 三种 Capability 形态

### 1. 单例 Capability（最常见）

一个接口类型全局 **只有一个** 实现。后注册会覆盖先注册（因此每个接口通常只由一个模块提供）。

```java
context.registerCapability(TitleGrantable.class, titleService);
```

适用于：`TitleGrantable`、`MailDispatchable`、`MapNavigable` 等。

### 2. 多实例 Capability

`PlayerDataPurgeable` 与 `DatabaseMigratable` **允许多个模块各注册一个实例**。宿主把它们放入独立列表，供 `/axs purge`、`/axs migrate` 统一调度。

```java
context.registerCapability(PlayerDataPurgeable.class, new PlayerDataPurgeable() {
    @Override public String moduleId() { return "mymodule"; }
    @Override public int purgePlayerData(UUID uuid) { return repo.deletePlayer(uuid); }
});
```

### 3. 全局 EventBus（内置）

宿主在初始化时注册 **唯一的** `EventBusCapability`（`SimpleEventBus`），任何模块可发布/订阅主题，适合 1 对多广播。

```java
EventBusCapability bus = context.getCapability(EventBusCapability.class);
bus.publish("market.purchase", player, Map.of("item", "diamond"));
```

与 `SignalDispatchable`（面向 EventPacket 规则引擎的 1 对 1 触发）互补。

## 加载顺序与 softdepends

`module.yml` 中：

- **`depends`**：硬依赖，保证提供方 **先于** 使用方 `onEnable`
- **`softdepends`**：软依赖，提供方可能不存在——使用方 **必须判空**

```yaml
# 使用方 module.yml
id: mymodule
depends: []
softdepends: [title, mail, eventpacket]
```

即使写了 `depends: [title]`，仍建议用 **Supplier 延迟查找**（见下文），以应对 reload 顺序边缘情况。

## 提供方：如何暴露能力

### 步骤 1 — 定义接口

接口放在 **`axs-api`**（官方能力）或你自己的 **`api` 子包**（第三方能力），供他人 `compileOnly`：

```java
package com.example.mymodule.api;

import org.bukkit.entity.Player;

public interface ShopDiscountable {
    double getDiscount(Player player);
}
```

### 步骤 2 — 实现并在 startService 注册

```java
@Override
protected void startService() {
    shopService = new ShopService(context, config);
    shopService.start();

    context.registerCapability(ShopDiscountable.class, player ->
        shopService.resolveDiscount(player));
}
```

### 步骤 3 — 使用官方模块的写法（Title 示例）

Title 模块注册称号发放能力（简化）：

```java
context.registerCapability(TitleGrantable.class,
    (playerId, titleId, duration, source) -> {
        var spec = TitleDurationParser.parse(duration);
        if (spec.isEmpty()) return false;
        return service.giveTitle(playerId, titleId, spec.get(), source).success();
    });

context.registerCapability(TitleConfigQueryable.class, titleId -> {
    TitleDefinition def = config.title(titleId);
    return def == null ? null
        : new TitleConfigQueryable.TitleInfo(def.displayName(), def.qualityName(), def.description());
});
```

## 使用方：如何调用其他模块

### 基本用法

```java
TitleGrantable title = context.getCapability(TitleGrantable.class);
if (title != null) {
    title.giveTitle(player.getUniqueId(), "legend", "7d", "MyModule");
}
```

### 推荐：Supplier 延迟查找

避免在 `onEnable` 瞬间因加载顺序得到 `null`：

```java
private Supplier<TitleGrantable> titleGrantable;
private Supplier<MailDispatchable> mailDispatchable;

@Override
protected void startService() {
    titleGrantable = () -> context.getCapability(TitleGrantable.class);
    mailDispatchable = () -> context.getCapability(MailDispatchable.class);
    dispatchService = new DispatchService(titleGrantable, mailDispatchable);
    dispatchService.start();
}

// 业务代码中
public void onQuestComplete(Player player) {
    TitleGrantable title = titleGrantable.get();
    if (title != null) {
        title.giveTitle(player.getUniqueId(), "hero", "permanent", "Quest");
    }
    MailDispatchable mail = mailDispatchable.get();
    if (mail != null) {
        mail.dispatchPreset("quest_reward", player.getName(), "Quest");
    }
}
```

### 通过 getModule 获取模块实例（慎用）

`context.getModule(SomeModule.class)` 可以拿到模块 **入口类实例**，但会耦合具体模块 Jar，**不如 Capability 稳定**。仅在你控制双方源码且同版本部署时考虑。

## 内置 Capability 能力图

下表列出 AXS 官方模块 **提供** 的能力及典型 **使用方**。第三方模块也可 `getCapability` 调用这些接口。

| Capability 接口 | 提供模块 | 典型使用方 | 用途 |
|-----------------|----------|------------|------|
| `TitleGrantable` | title | eventpacket, battlepass | 发放称号 |
| `TitleConfigQueryable` | title | tab, chat | 查询称号元数据 |
| `MailDispatchable` | mail | eventpacket, onlinerewards | 按预设发邮件 |
| `SubtitlePlayable` | announcer | eventpacket | 播放字幕组 |
| `ChatCardSendable` | chat | eventpacket | 发送聊天卡片 |
| `ChatMutable` | chat | essentials | 禁言/解禁 |
| `QuestGpsNavigable` | questgps | eventpacket | 任务导航 |
| `MapNavigable` | map | questgps | 地图外部导航点 |
| `TabRefreshable` | tab | title, chat | 刷新 Tab 列表 |
| `CombatEffectTriggerable` | combateffect | eventpacket, prop | 触发战斗特效 |
| `SignalDispatchable` | eventpacket | onlinerewards, afkreward | 触发规则引擎信号 |
| `QQBotBroadcastable` | qqbot | eventpacket, mail | 推送到 QQ 群 |
| `QQBotNotifiable` | qqbot | 第三方 | 监听群消息/进退群 |
| `QqBindCapable` | qqbot | loginview | QQ 绑定查询 |
| `WarehouseAutoDepositable` | warehouse | pickup | 拾取自动入库 |
| `PickupNotifiable` | pickup | warehouse | 查询拾取 HUD 状态 |
| `EssentialsQueryable` | essentials | tab, chat | AFK/隐身/禁言/昵称 |
| `MenuOpenable` | menu | essentials, 第三方 | 打开配置菜单 |
| `AfkRewardDispatchable` | afkreward | essentials | 挂机状态/原地挂机 |
| `InteractionState` | conversation | pickup, prop | 对话中让出共享按键 |
| `EventBusCapability` | **宿主** | market, qqbot, 第三方 | 主题 pub/sub |
| `PlayerDataPurgeable` | 多模块 | **宿主** `/axs purge` | 清除玩家数据 |
| `DatabaseMigratable` | 多模块 | **宿主** `/axs migrate` | 跨库迁移 |

::: details 调用示例：EventPacket 发称号 + 邮件
EventPacket 在规则动作中通过 Capability 调用，而不是链接 Title/Mail 的类：

```java
TitleGrantable title = titleGrantable.get();
if (title != null) {
    title.giveTitle(player.getUniqueId(), titleId, duration, "EventPacket");
}
MailDispatchable mail = mailDispatchable.get();
if (mail != null) {
    mail.dispatchPreset(presetId, player.getName(), "EventPacket");
}
```
:::

::: details 调用示例：OnlineRewards 触发信号
```java
SignalDispatchable signals = context.getCapability(SignalDispatchable.class);
if (signals != null) {
    signals.dispatchSignal("signin_success", player, Map.of("day", "7"));
}
```
:::

## 宿主命令与 Capability

部分 Capability 由 **宿主命令** 统一调用，模块只需注册即可接入：

| 命令 | 使用的 Capability |
|------|-------------------|
| `/axs purge <玩家> <模块\|all>` | `PlayerDataPurgeable` |
| `/axs migrate <模块> <方向>` | `DatabaseMigratable` |

实现 `PlayerDataPurgeable` 时 `moduleId()` 必须与本模块 `id` 一致。

## 第三方模块：完整示例

**模块 A（提供方）** — `reputation` 声望系统：

```java
// api/ReputationQuery.class
public interface ReputationQuery {
    int getReputation(UUID playerId);
}

// ReputationModule.java
@Override
protected void startService() {
    service = new ReputationService(context);
    service.start();
    context.registerCapability(ReputationQuery.class, service);
}
```

**模块 B（使用方）** — `shop` 商店折扣：

```yaml
# shop/module.yml
softdepends: [reputation]
```

```java
@Override
protected void startService() {
    Supplier<ReputationQuery> rep = () -> context.getCapability(ReputationQuery.class);
    shopService = new ShopService(context, rep);
}
```

```java
// ShopService 内
double discount = 1.0;
ReputationQuery rep = reputationSupplier.get();
if (rep != null && rep.getReputation(player.getUniqueId()) >= 1000) {
    discount = 0.9;
}
```

B 的 Jar **不需要** 依赖 A 的实现类，只需 `compileOnly` A 发布的 `api` 接口 Jar（或把接口放进共享的 api 包）。

## 生命周期与清理

| 事件 | 行为 |
|------|------|
| 模块 `onEnable` | 在 `startService()` 中 `registerCapability` |
| 模块 `onDisable` | 宿主自动 `removeCapabilities(moduleId)` |
| `/axs unload` | 同上，并关闭 ClassLoader |

::: warning 不要在 onDisable 里留悬挂引用
使用方应把 Capability 引用置空；提供方无需手动 unregister（宿主会处理）。若你在静态字段缓存了 Capability，unload 后可能持有已失效对象。
:::

## 与 EventPacket / 配置联动

许多官方模块的联动 **不需要写 Java**——EventPacket 规则动作（grant_title、send_mail、dispatch_signal 等）内部已使用 Capability。服主只需：

1. 启用相关模块（title、mail、eventpacket…）
2. 在 EventPacket 配置中编写规则

开发者写代码时，Capability 与配置驱动是 **同一套底层机制**。

## 最佳实践 checklist

- [ ] 接口放在 `api` 包，仅含业务方法，不暴露内部 Service
- [ ] 使用方对 `getCapability` 结果 **始终判 null**
- [ ] 可选依赖写 `softdepends`，必选依赖写 `depends`
- [ ] 使用 `Supplier` 延迟查找
- [ ] 单接口单一职责；不要做一个「万能 Capability」
- [ ] 为对外 Capability 编写 Javadoc 与 wiki 说明
- [ ] 持久化模块实现 `PlayerDataPurgeable` / `DatabaseMigratable` 以接入宿主运维命令

## 相关文档

- [开发第三方模块](./module-development) — 模块入门
- [Capability API 速查](/api/capability) — 各接口方法签名
- [ModuleContext](/api/module-context#capability-跨模块通信) — API 层说明
- [EventPacket 模块](/modules/eventpacket) — 规则引擎与信号
