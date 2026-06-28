---
title: 客户端包守卫 | ArcartX-Suite Minecraft插件架构文档
description: ClientPacketGuard 速率限制配置，防止客户端高频回包刷取资源。
---

# 客户端包守卫

ArcartX-Suite 通过 `ClientPacketGuard` 对 ArcartX UI 的**客户端→服务端回包**做滑动窗口限流，防止连点器、宏脚本刷取称号装备、邮件领取、地图传送等资源。

每条回包抽象为 `(player, module, action)` 三元组，在 `window-ms` 时间窗内超过 `max-hits` 次即按 `mode` 处理。

## 全局配置

位于宿主 `config.yml` → `client-packet-guard`：

```yaml
client-packet-guard:
  enabled: true
  cleanup-interval-ticks: 200
  defaults:
    window-ms: 1000
    max-hits: 20
    mode: "silent"              # silent | notify | punish
    notify-message: "&c操作过快，请稍后再试。"
    notify-cooldown-ms: 3000
    punish-command: ""          # mode=punish 时执行，支持 {player} {uuid} {action} {module}
```

| 字段 | 说明 |
| --- | --- |
| `enabled` | 总开关 |
| `cleanup-interval-ticks` | 过期计数器清理周期（20 tick = 1 秒） |
| `defaults.*` | 未单独配置的模块/动作继承此默认值 |
| `mode` | `silent` 静默丢弃；`notify` 丢弃并提示；`punish` 丢弃并执行惩罚命令 |

## 模块级与动作级覆写

在 `client-packet-guard.modules.<moduleId>` 下可覆写模块默认值；在 `actions.<actionName>` 下可进一步细化单个 UI 回包动作：

```yaml
client-packet-guard:
  modules:
    title:
      window-ms: 1000
      max-hits: 4
      mode: "silent"
      actions:
        equip:
          window-ms: 1500
          max-hits: 1
          mode: "notify"
    mail:
      actions:
        claimall:
          window-ms: 5000
          max-hits: 1
          mode: "notify"
    market:
      window-ms: 2000
      max-hits: 6
      mode: "silent"
      actions:
        sell:
          window-ms: 3000
          max-hits: 1
          mode: "notify"
```

::: tip 动作名从哪来？
动作名对应 UI 模板中 `Packet.send("AXS_XXX", "<action>", ...)` 的第二个参数，或各模块 `*PacketHandler` 路由的 `action` 字符串。调试时可开启对应模块的 `settings.debug: true` 查看实际回包。
:::

## 内置预设模块

宿主 `config.yml` 已为以下高频模块预置限流（可按服情调整）：

| 模块 | 典型动作 | 默认策略 |
| --- | --- | --- |
| `title` | `equip` / `unequip` / `hide` 等 | 1.5s 内 1 次，`notify` |
| `mail` | `compose-send` / `claimall` / `cdk` | 3–5s 内 1 次，`notify` |
| `questgps` | `accept_quest` / `abandon_quest` | 2s 内 1 次 |
| `map` | `unlock_anchor` / `teleport_anchor` / `create_waypoint` | 2–3s 内 1 次 |
| `announcer` | `click` | 3s 内 2 次 |
| `eventpacket` | `client-packet` | 3s 内 2 次 |
| `market` | `sell` / `buy` / `bid` / `cancel` / `recycle` | 各动作独立窗口 |

完整列表见仓库 `src/main/resources/config.yml` 中 `client-packet-guard.modules` 段。

## 模块开发者接入

模块通过 `ModuleContext.packetGuard()` 注册自定义动作限制，或在 `onEnable` 阶段依赖宿主全局配置。第三方模块建议使用 `PacketGuardAPI`（`@Stable`），详见 [ModuleContext](/api/module-context)。

## 故障排查

| 现象 | 可能原因 |
| --- | --- |
| 玩家点击 UI 无反应 | `mode: silent` 下被限流；临时调高 `max-hits` 或加大 `window-ms` |
| 频繁提示「操作过快」 | `notify` 模式阈值过严；检查是否连点或 UI 重复发包 |
| 某动作始终无法触发 | 动作名与 `actions` 键不一致；开 debug 核对实际 action |
