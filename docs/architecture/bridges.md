# 桥接层 (Bridge)

源码：`xuanmo.arcartxsuite.bridge.*`，共 8 个类。

AXS 在 `plugin.yml` 里只硬依赖 `ArcartX`，其他全是软依赖。**软依赖意味着插件可能不存在，直接 import 会让 AXS 启动失败**。所以用反射：

- 启动时 `Class.forName(...)` 试探，失败即 `bridge.ready = false`
- 调用时通过 `MethodHandle / Method.invoke` 远程调对方 API

## 反射桥列表

| 类 | 目标插件 | 用途 |
| --- | --- | --- |
| `ArcartXPacketBridge` | ArcartX | UI 注册、`open/close/sendPacket`、聊天卡片 |
| `ArcartXClientBridge` | ArcartX | `sendDamageDisplay`、`sendServerVariable` |
| `ArcartXItemStackBridge` | ArcartX | 物品序列化（Mail / Warehouse） |
| `ArcartXKeyBindBridge` | ArcartX | KeyBind 注册（Map / Conversation / Prop） |
| `ArcartXPropBridge` | ArcartX | 道具效果绑定（Prop 独占） |
| `ArcartXWaypointBridge` | ArcartX | 路径点（Map / QuestGPS） |
| `AdyeshachNpcBridge` | Adyeshach | 附近 NPC（Conversation） |
| `VaultEconomyBridge` | Vault | 货币读写 |

## ArcartXPacketBridge — 核心桥

```java
bridge.registerOrReloadUi(uiId, yamlContent);
bridge.openUi(player, uiId);
bridge.sendPacket(player, uiId, "init",  payload);
bridge.sendPacket(player, uiId, "update", payload);
bridge.sendPacket(player, uiId, "close", payload);
bridge.closeUi(player, uiId);
```

## 桥失败时的降级策略

每个 Service 在 `enable()` 阶段调 `bridge.isReady()`，失败则标 `bridge missing`，**不阻止其他模块启动**。
