# 数据包流向 (init / update / close)

所有 UI 类模块都遵循**五段式生命周期**。

## 五段式

```
1. register   服务端把 UI 模板注册到 ArcartX
2. open       服务端通知客户端打开 UI
3. init       推送首帧完整状态
4. update*    按刷新周期推增量
5. close      通知客户端清场
```

## 服务端 → 客户端

### init

```java
Map<String, Object> payload = Map.of(
    "title", "新手村",
    "owned_count", 12,
    "list", listOfTitles
);
bridge.sendPacket(player, uiId, "init", payload);
```

### update

- **周期型**（EntityTracker / Tab）：`refresh-interval-ticks` 定时推
- **事件型**（Mail / Title）：状态变化时推

## 客户端 → 服务端

UI 模板中通过 Shimmer 调 `Packet.send(...)`：

```yaml
controls:
  equip_btn:
    type: button
    onClick: |-
      Packet.send("AXS_TITLE", "equip", "myth_hunter")
```

服务端 `*PacketHandler` 路由：

```java
switch (action) {
    case "equip"   -> service.equip(player, data.get(1));
    case "unequip" -> service.unequipGroup(player, data.get(1));
    // ...
}
```

## 三种 payload 形态

| 形态 | 例子 | 客户端读取 |
| --- | --- | --- |
| 字符串 | `"killer={name};victim={name}"` | 整段文本 |
| 列表 | `["{name}", "{weapon}"]` | `packet[0]` / `packet[1]` |
| 字典 | `{killer: ..., weapon: ...}` | `packet['killer']` |

::: tip 调试 packet
每个模块的 `settings.debug: true` 会打印 init / update / close 的完整 payload。
:::
