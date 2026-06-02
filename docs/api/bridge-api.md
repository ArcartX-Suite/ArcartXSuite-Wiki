# 桥接 API

ArcartXSuite 1.1.0 起提供三个类型安全的桥接接口，取代原先返回 `Object` 的旧 API。模块通过 `ModuleContext` 获取实例。

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

统一货币桥接，支持 Vault / PlayerPoints / Rondo / Command 等多种经济提供者。所有模块共享同一组货币定义。

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
