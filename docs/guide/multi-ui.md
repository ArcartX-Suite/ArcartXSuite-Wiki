---
title: 多 UI 同时发包 | ArcartX-Suite Minecraft插件文档
description: 多 UI 同时发包 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 多 UI 同时发包

ArcartX-Suite 大多数模块的 `ui-id` 字段都支持**字符串**和**列表**两种格式。当配置为列表时，服务端会把同一个 payload 同时发送给列表中的每一个 UI，适用于一份数据同时驱动多套 UI 展现的场景。

## 适用场景

- 同一个功能需要在**全屏菜单**和**小地图 HUD** 上同时展示数据
- 你为某个模块制作了多套风格的 UI 文件，希望一次性推送数据到所有 UI
- Tab 定义需要同时推送到在线列表和竞技场积分板

## 配置格式

所有支持多 UI 的字段都使用统一的解析器（`UiIdParser`），兼容两种写法：

### 单 UI（默认写法）

```yaml
ui-id: "AXS:boss_tracker"
```

### 多 UI（列表写法）

```yaml
ui-id:
  - "AXS:boss_tracker"
  - "AXS:boss_tracker_alt"
```

::: tip 向后兼容
从单 UI 切换到多 UI **不需要修改代码或 migration**，只需把配置值改为列表即可。服务端内部统一处理为 `List<String>`。
:::

## 支持的模块与字段

| 模块 | 配置文件 | 支持多 UI 的字段 |
|------|---------|----------------|
| **Tab** | `tabs/*.yml` | `ui-targets[].ui-id`（或旧格式 `ui-id`） |
| **EntityTracker** | `ArcartXEntityTracker.yml` | `boss.settings.ui-id`、`attack-target.settings.ui-id` |
| **Announcer** | `ArcartXAnnouncer.yml` | `settings.ui-id`、`subtitle.settings.ui-id` |
| **Conversation** | `ArcartXConversation.yml` | `client.dialog-ui-id`、`client.selector-ui-id` |
| **QuestGPS** | `ArcartXQuestGPS.yml` | `client.menu-ui-id`、`client.guide-ui-id` |
| **OnlineRewards** | `ArcartXOnlineRewards.yml` | `ui.menu-ui-id` |
| **Subtitle 字幕组** | `subtitle/groups/*.yml` | 每个字幕组的 `ui-id`（覆盖全局默认） |

::: info Tab 的特殊格式
Tab 模块除了通用的 `ui-id` 字段外，还支持更灵活的 `ui-targets` 列表格式，每个 target 可独立指定 `packet-handler`：

```yaml
ui-targets:
  - ui-id: "tab"
    packet-handler: "tab"
  - ui-id: "tab-arena"
    packet-handler: "arena"
```

`ui-targets` 优先级高于 `ui-id` + `packet-handler`，两者不要同时写。
:::

## 工作原理

1. 模块启动时，为列表中每个 `ui-id` 分别向 ArcartX 注册 UI 文件
2. 发包时调用 `ArcartXPacketBridge.sendPacketToAll(player, uiIds, handler, payload)` 遍历所有 UI 发送
3. 打开 / 关闭 UI 同样通过 `openUiAll` / `closeUiAll` 批量操作
4. 客户端回包（`Packet.send`）仍然只发给配置中的 `packet-id`，服务端统一处理

## 注意事项

- 列表中的每个 `ui-id` 必须对应一个实际存在的 UI 文件，否则注册会失败
- 客户端回包的 `packet-id` 不受多 UI 影响，仍然使用配置中的单一 `packet-id` 字段
- 多 UI 发包只是服务端广播同一份 payload，不会为不同 UI 生成不同的数据
- 首个 UI ID 会作为向后兼容的 "主 UI ID"，用于日志输出和 PAPI 等需要单值的场景

