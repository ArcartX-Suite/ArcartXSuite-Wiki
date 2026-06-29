---
title: 桥接层 (Bridge) | ArcartX-Suite Minecraft插件架构文档
description: 桥接层 (Bridge) - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 桥接层 (Bridge)

ArcartX-Suite 在 `plugin.yml` 里只硬依赖 `ArcartX`，其他全是软依赖。**软依赖意味着插件可能不存在，直接 import 会让 ArcartX-Suite 启动失败**。所以内部使用反射桥接：

- 启动时 `Class.forName(...)` 试探，失败即 `bridge.ready = false`
- 调用时通过 `MethodHandle / Method.invoke` 远程调对方 API

::: tip 开发者 API
模块开发者无需关心反射细节，直接使用 [类型安全的桥接 API](/api/bridge-api) 即可。
:::

## 公开桥接 API

模块通过 `ModuleContext` 获取以下类型安全的桥接接口（详见 [API 参考](/api/bridge-api)）：

| 接口 | 获取方式 | 功能 |
| --- | --- | --- |
| `PacketBridgeAPI` | `context.packetBridge()` | UI 注册/打开/关闭/发包/聊天卡片 |
| `ClientBridgeAPI` | `context.clientBridge()` | 伤害飘字/服务端变量/可见玩家遍历 |
| `ItemBridgeAPI` | `context.itemStackBridge()` | ItemStack → JSON 序列化 |

## 内部反射桥列表

| 类 | 目标插件 | 用途 |
| --- | --- | --- |
| `ArcartXPacketBridge` | ArcartX | UI 注册、`open/close/sendPacket`、聊天卡片 |
| `ArcartXClientBridge` | ArcartX | `sendDamageDisplay`、`sendServerVariable` |
| `ArcartXItemStackBridge` | ArcartX | 物品序列化（Mail / Warehouse） |
| `ArcartXKeyBindBridge` | ArcartX | KeyBind 注册（Map / Conversation / Prop） |
| `ArcartXPropBridge` | ArcartX | 道具效果绑定（Prop 独占，`@Internal`，模块开发者不应直接使用） |
| `ArcartXWaypointBridge` | ArcartX | 路径点（Map / QuestGPS） |
| `AdyeshachNpcBridge` | Adyeshach | 附近 NPC（Conversation） |

### 属性与物品来源桥（`config.yml` → `bridges`）

宿主在启动时按 `bridges.*` 开关探测并注册以下桥，模块通过 `context.attributeBridge()`、`context.itemSourceRegistry()` 使用：

| 配置键 | 目标 | 典型消费者 |
| --- | --- | --- |
| `attributeplus` | AttributePlus | Title、CombatEffect、Prop |
| `craneattribute` | CraneAttribute | Title、CombatEffect |
| `mythiclib` | MythicLib / MMOItems | Title、CombatEffect、Prop |
| `symphony` | Symphony | Menu 条件、部分战斗逻辑 |

物品库反射桥（NeigeItems、MythicMobs、MMOItems、Overture 等）由全局 `ItemSourceRegistry` 统一注册，Mail / Warehouse / Market 发奖与附件时自动识别物品来源。

Aria 脚本条件通过 `DefaultAriaBridge` 对接 BlinkAriaHost；未安装时 Aria 条件求值为 false，可改用 JS 条件，见 [条件系统](/guide/conditions)。

> **货币桥接**：所有经济相关读写统一通过全局 `CurrencyBridgeAPI`（`context.currencyManager()`）完成，支持 Vault / PlayerPoints / Rondo / Command / PlaceholderCommand / Custom 多 provider。详见 [桥接 API → 货币](/api/bridge-api)。

## 使用示例

```java
PacketBridgeAPI bridge = context.packetBridge();
if (bridge != null && bridge.isAvailable()) {
    bridge.registerOrReloadUi("my_ui", uiFile);
    bridge.openUi(player, "my_ui");
    bridge.sendPacket(player, "my_ui", "update", payload);
    bridge.closeUi(player, "my_ui");
}
```

## 桥失败时的降级策略

每个 Service 在 `enable()` 阶段调 `bridge.isAvailable()`，失败则标 `bridge missing`，**不阻止其他模块启动**。

