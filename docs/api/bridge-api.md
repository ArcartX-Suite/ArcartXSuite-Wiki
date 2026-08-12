---
title: 桥接 API | ArcartX-Suite Minecraft插件API文档
description: 桥接 API - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 桥接 API

ArcartX-Suite 1.1.0 起提供三个类型安全的桥接接口，取代原先返回 `Object` 的旧 API。模块通过 `ModuleContext` 获取实例。

所有桥接接口均标记为 `@ApiStability.Stable`。

## PacketBridgeAPI

UI / Packet 桥接，提供 ArcartX UI 注册、打开、关闭、发包、聊天卡片等能力。

**获取方式：** `context.packetBridge()`

### 可用性检查

```java
PacketBridgeAPI bridge = context.packetBridge();
if (bridge == null || !bridge.isAvailable()) {
    logger.warning("ArcartX 桥接不可用");
    return;
}
```

### UI 生命周期

#### registerOrReloadUi

向 ArcartX 注册或热重载一个 UI 文件。幂等安全——先尝试 reload（已注册则刷新），再尝试 register（未注册则注册）。

```java
UiRegistrationResult result = bridge.registerOrReloadUi("my_ui", uiFile);
if (result.success()) {
    String runtimeUiId = result.runtimeUiId();       // 运行时使用的 id
    String registeredUiId = result.registeredUiId();  // 注册到 ArcartX 的 id（注销时使用）
} else {
    logger.warning("UI 注册失败: " + result.message());
}
```

**参数：**
- `configuredUiId` — 配置中指定的 UI id，可为 `null`（自动从文件名推导）
- `uiFile` — UI YAML 文件

**返回值 `UiRegistrationResult`：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | `boolean` | 是否成功 |
| `runtimeUiId` | `String` | 运行时使用的 UI id |
| `registeredUiId` | `String` | 注册到 ArcartX 的 id，失败时为 `null` |
| `action` | `String` | 执行的操作（`register` / `reload` / `fail`） |
| `message` | `String` | 失败原因 |

#### unregisterUi

```java
boolean success = bridge.unregisterUi("my_ui");
```

#### normalizeUiId

静态工具方法，将配置的 UI id 规范化。如果 `configuredUiId` 为空，则从文件名推导。

```java
String uiId = PacketBridgeAPI.normalizeUiId("my_ui", uiFile);
String uiId = PacketBridgeAPI.normalizeUiId(null, new File("my_view.yml")); // → "my_view"
```

### 打开 / 关闭 UI

```java
// 打开
bridge.openUi(player, "my_ui");

// 打开并注册关闭回调
bridge.openUiWithCallback(player, "my_ui", () -> {
    // UI 被关闭时执行
});

// 关闭
bridge.closeUi(player, "my_ui");

// 批量打开 / 关闭
bridge.openUiAll(player, List.of("ui_a", "ui_b"));
bridge.closeUiAll(player, List.of("ui_a", "ui_b"));
```

### Packet 发送

向客户端 UI 的指定 handler 发送数据包。

```java
// 发送到单个 UI
bridge.sendPacket(player, "my_ui", "update", Map.of(
    "health", player.getHealth(),
    "name", player.getName()
));

// 发送到多个 UI
bridge.sendPacketToAll(player, List.of("ui_a", "ui_b"), "refresh", payload);
```

**参数：**
- `player` — 目标玩家
- `uiId` — UI id
- `handlerName` — handler 名称（对应 UI YAML 中的 `packetHandler`）
- `payload` — 数据载荷（`Map` / `List` / 基本类型）

### 聊天卡片

```java
bridge.sendChatCard(player, "my_card", Map.of(
    "title", "系统通知",
    "message", "你有新邮件"
));
```

### 关闭回调

```java
// 注册
bridge.registerUiCloseCallback("my_ui", player -> {
    // 玩家关闭了 UI
});

// 注销
bridge.unregisterUiCloseCallback("my_ui");
```

---

## ClientBridgeAPI

客户端桥接，提供伤害飘字、服务端变量下发、可见玩家遍历等能力。

**获取方式：** `context.clientBridge()`

### sendDamageDisplay

向玩家发送伤害飘字显示。

```java
ClientBridgeAPI client = context.clientBridge();
if (client != null && client.isAvailable()) {
    client.sendDamageDisplay(player, "physical", 150.0, targetEntity);
}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `player` | `Player` | 观察者玩家 |
| `configId` | `String` | 飘字配置 id |
| `amount` | `double` | 伤害数值 |
| `target` | `Entity` | 受击实体 |

### sendServerVariable

向玩家下发服务端变量，客户端 UI 可通过 `{server.变量名}` 引用。

```java
client.sendServerVariable(player, "my_score", 100);
client.sendServerVariable(player, "my_status", "online");
client.sendServerVariable(player, "my_flag", true);
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `player` | `Player` | 目标玩家 |
| `variableName` | `String` | 变量名 |
| `value` | `Object` | 变量值（String / Number / Boolean） |

### forEachSeenPlayer

遍历能看到指定实体的所有玩家。

```java
client.forEachSeenPlayer(entity, seenPlayer -> {
    // 对每个可见玩家执行操作
    client.sendDamageDisplay(seenPlayer, "critical", 300.0, entity);
});
```

---

## ItemBridgeAPI

ItemStack 序列化桥接，将 Bukkit 物品栈转为 ArcartX 客户端可识别的 JSON 格式。

**获取方式：** `context.itemStackBridge()`

### itemToJson

```java
ItemBridgeAPI itemBridge = context.itemStackBridge();
if (itemBridge != null && itemBridge.isAvailable()) {
    Optional<String> json = itemBridge.itemToJson(itemStack);
    json.ifPresent(jsonStr -> {
        // 将 JSON 放入 packet payload 中发送到客户端
        bridge.sendPacket(player, "my_ui", "show_item", Map.of("itemJson", jsonStr));
    });
}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `itemStack` | `ItemStack` | Bukkit 物品栈 |

**返回值：** `Optional<String>` — JSON 字符串，桥接不可用时返回 `empty`。

---

## CurrencyBridgeAPI

统一货币桥接，支持 Vault / PlayerPoints / XConomy / Rondo / Command 等多种经济提供者。所有模块共享同一组货币定义。

**获取方式：** `context.currencyManager()`

> **配置教程：** 各 provider 类型的详细配置方法请参阅 [货币系统配置](/guide/currencies)。

### 查询余额

```java
CurrencyBridgeAPI currencies = context.currencyManager();
CurrencyBridge bridge = currencies.bridge("money");
if (bridge != null && bridge.available()) {
    BigDecimal balance = bridge.balance(player);
}
```

### 扣款 / 入账

```java
CurrencyTransactionResult result = bridge.withdraw(player, BigDecimal.valueOf(100));
if (result.success()) {
    // 扣款成功
} else {
    player.sendMessage("扣款失败: " + result.message());
}

// 入账
CurrencyTransactionResult depositResult = bridge.deposit(player, BigDecimal.valueOf(50));
```

### 可用货币列表

```java
Set<String> ids = currencies.currencyIds(); // 如 ["money", "points", "gems"]
Collection<CurrencyDefinition> defs = currencies.definitions();
```

### 格式化金额

```java
String formatted = currencies.format("money", BigDecimal.valueOf(99.5)); // "99.5"
```

### 货币变动事件分发

`CurrencyBridgeManager` 在每次 `withdraw` / `deposit` 成功后，自动通过 `EventBusCapability` 分发货币变动事件，模块可通过 `event-topic` 订阅。

| 事件主题 | 触发时机 | Payload 字段 |
|---------|---------|-------------|
| `axs.currency.spent` | 扣款成功 | `currency_id`、`amount`、`action="withdraw"`、`balance_after` |
| `axs.currency.earned` | 入账成功 | `currency_id`、`amount`、`action="deposit"`、`balance_after` |

> 该机制对所有货币提供者（Vault / PlayerPoints / XConomy / Rondo / Command）自动生效，无需各提供者单独实现。

---

## ItemSourceRegistry

全局物品来源注册表，统一 MythicMobs / NeigeItems / Overture / MMOItems 等第三方物品库的桥接。

**获取方式：** `context.itemSourceRegistry()`

```java
ItemSourceRegistry registry = context.itemSourceRegistry();
Optional<ItemStack> item = registry.createItem("mythicmobs:Dark_Sword");
item.ifPresent(stack -> player.getInventory().addItem(stack));
```

| 方法 | 说明 |
|------|------|
| `createItem(String id)` | 按 `provider:itemId` 格式创建物品。如 `mythicmobs:Dark_Sword`、`neigeitems:Legendary_Bow` |
| `isAvailable(String provider)` | 判断指定物品来源提供者是否已加载 |
| `overtureItemId(ItemStack)` | 获取 Overture 物品 ID，非 Overture 物品返回空串 |
| `isOvertureItem(ItemStack)` | 判断是否为 Overture 物品 |
| `overtureItemDisplayName(String itemId)` | 获取 Overture 物品模板显示名 |
| `overtureItemDisplayLore(String itemId)` | 获取 Overture 物品模板描述行 |
| `overtureTemplateItem(String itemId)` | 获取 Overture 物品模板副本（仅展示用，不含实例数据） |
| `overtureItemIds()` | 获取所有已注册的 Overture 物品 ID |
| `overtureSerialize(ItemStack)` | 使用 Overture 原生序列化将 ItemStack 序列化为 JSON 字符串 |
| `overtureDeserialize(String json)` | 使用 Overture 原生反序列化从 JSON 字符串恢复 ItemStack |

---

## VanillaItemNameBridge

原版物品中文名称解析桥接，提供 Minecraft 原版物品的中文显示名查询。用于显示原版物品名、入库搜索文本、聊天物品预览等场景，避免各模块直接显示英文材质名。

**获取方式：** `context.vanillaItemNameBridge()`（永不为 null，since 1.3.3）

```java
VanillaItemNameBridge nameBridge = context.vanillaItemNameBridge();
String displayName = nameBridge.displayName(itemStack);
// 返回如 "钻石剑"、"附魔书：锋利 I"、"药水：治疗"
```

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `displayName(ItemStack)` | `String` | 获取物品的中文显示名。优先使用自定义显示名，其次按附魔书/药水子类型解析，最后回退到 Material 静态映射表 |

---

## ItemMatcherAPI

全局物品匹配器，支持跨物品库的等价性比较。

**获取方式：** `context.itemMatcher()`

```java
ItemMatcherAPI matcher = context.itemMatcher();
boolean same = matcher.matches(handItem, "mythicmobs:Dark_Sword");
```

---

## AttributeBridgeRegistry

全局属性桥接注册表，统一 AttributePlus / CraneAttribute / MythicLib / Symphony 等属性系统的桥接。

**获取方式：** `context.attributeBridge()`

```java
AttributeBridgeRegistry attrBridge = context.attributeBridge();
Optional<AttributeBridge> bridge = attrBridge.bridge("attributeplus");
bridge.ifPresent(b -> b.apply(player, "strength", 10.0, 30000L));
```

| 方法 | 说明 |
|------|------|
| `bridge(String provider)` | 按提供者 ID 获取桥接实例 |
| `isAvailable(String provider)` | 判断指定属性系统是否已加载 |

---

## SymphonyBridge

Symphony 属性系统桥接，提供属性设置/查询、战力、等级、护盾、战斗状态、光环、套装等能力。

**获取方式：** `context.attributeBridge().symphony()`

> Symphony 未安装时 `available()` 返回 `false`，所有查询方法返回 `null` / 默认值。

### 属性写操作

```java
SymphonyBridge symphony = context.attributeBridge().symphony();
// 设置属性修饰符（百分比或固定值）
symphony.setAttribute(player, "physical_damage", false, 50.0, "my_module:buff");
// 移除指定来源的属性
symphony.removeAttribute(player, "my_module:buff");
// 重新计算玩家全部属性
symphony.recalculate(player);
```

### 属性查询

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `attributeValue(LivingEntity, String)` | `double` | 查询实体指定属性的当前值 |
| `combatPower(LivingEntity)` | `SymphonyCombatPower` | 查询战力快照 |
| `level(LivingEntity)` | `SymphonyLevelSnapshot` | 查询等级快照 |
| `shield(LivingEntity)` | `double` | 查询护盾值 |
| `setShield(LivingEntity, double)` | `double` | 设置护盾值，返回实际值 |
| `combatState(LivingEntity)` | `SymphonyCombatState` | 查询战斗状态 |
| `statuses(LivingEntity)` | `List<SymphonyStatusEffect>` | 查询状态效果列表 |
| `auras(LivingEntity)` | `List<SymphonyAura>` | 查询光环列表 |
| `activeSets(LivingEntity)` | `Map<String, Integer>` | 查询激活的套装（key → 件数） |

### 包装类型

| 类型 | 字段 | 说明 |
|------|------|------|
| `SymphonyCombatPower` | `value`、`formattedValue` | 战力数值与格式化字符串 |
| `SymphonyLevelSnapshot` | `level`、`exp`、`expToNext`、`characterId` | 等级与经验信息 |
| `SymphonyCombatState` | `active`、`remainingMillis` | 战斗状态与剩余时间 |
| `SymphonyStatusEffect` | `id`、`stacks`、`remainingMillis` | 状态效果 |
| `SymphonyAura` | `channel`、`gauge`、`remainingMillis` | 光环信息 |

---

## RondoBridge

Rondo 经济系统桥接，提供排行榜、转账、经济快照、交易日志等 Rondo 原生高级功能。

**获取方式：** `context.rondoBridge()`

> Rondo 未安装时 `available()` 返回 `false`，所有查询返回空列表 / `null`。

### 排行榜

```java
RondoBridge rondo = context.rondoBridge();
// 获取金币排行榜前 10 名
List<RondoRankingEntry> ranking = rondo.ranking("money", 1, 10);
// 查询玩家排名
Integer rank = rondo.playerRank(player.getUniqueId(), "money");
```

### 转账

```java
RondoTransferResult result = rondo.transfer(fromUuid, toUuid, "money", BigDecimal.valueOf(100));
if (result.success()) {
    // 转账成功，result.taxAmount() 为扣除的税费
}
```

### 经济快照

```java
// 非阻塞探测余额（纯缓存读取，不访问数据库）
BigDecimal cached = rondo.peekBalance(uuid, "money");

// 异步获取完整经济快照（所有货币）
CompletableFuture<RondoCurrencySnapshot.Full> future = rondo.economySnapshot(uuid);
future.thenAccept(snapshot -> {
    // snapshot.balances() → Map<String, BigDecimal>
    // snapshot.totalEarned() → Map<String, BigDecimal>
    // snapshot.totalSpent() → Map<String, BigDecimal>
});
```

### 交易日志

```java
// 查询玩家交易流水（分页）
List<RondoTransactionLog> logs = rondo.log(uuid, "money", 1, 20);
```

### 相关类型

| 类型 | 字段 | 说明 |
|------|------|------|
| `RondoRankingEntry` | `rank`、`playerUuid`、`playerName`、`balance` | 排行榜条目 |
| `RondoTransactionLog` | `action`、`currencyId`、`amount`、`oldBalance`、`newBalance`、`source`、`timestamp` | 交易流水 |
| `RondoTransferResult` | `success`、`taxAmount`、`message` | 转账结果 |
| `RondoCurrencySnapshot` | `currencyId`、`balance`、`totalEarned`、`totalSpent` | 货币快照 |
| `RondoCurrencySnapshot.Full` | `balances`、`totalEarned`、`totalSpent` | 完整经济快照（所有货币） |

