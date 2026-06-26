---
title: 客户端包守卫 | ArcartX-Suite Minecraft插件架构文档
description: 客户端包守卫 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 客户端包守卫

ArcartX-Suite 的安全由 `ClientPacketGuard` 负责，防止伪造 / 高频回包 DoS。

## ClientPacketGuard

每条客户端→服务端的包被抽象成 `(player, module, action)` 三元组。

```yaml
client-packet-guard:
  enabled: true
  cleanup-interval-ticks: 200
  defaults:
    window-ms: 1000
    max-hits: 20
    mode: "silent"
```

| 字段 | 说明 |
| --- | --- |
| `window-ms` | 时间窗（毫秒） |
| `max-hits` | 时间窗内最多允许的回包数 |
| `mode` | `silent` 静默丢弃 / `notify` 丢弃并提示 / `punish` 执行命令 |

