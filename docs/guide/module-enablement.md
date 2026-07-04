---
title: 模块启用 | ArcartX-Suite Minecraft插件文档
description: 模块启用 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 模块启用

ArcartX-Suite 的 `config.yml` 负责模块启用开关：

```yaml
modules:
  announcer:
    enabled: true
  warehouse:
    enabled: true
```

所有模块统一受 `config.yml` 的 `modules.<module>.enabled` 控制，`true` 后即可加载。

::: tip 授权方式
付费/福利模块的授权由 [云端平台](cloud-modules) 管理：在 [cloud.021209.xyz](https://cloud.021209.xyz) 领取或购买后，于「装备模块」勾选到服务器，再在本机 `config.yml` 将对应模块 `enabled: true`。执行 `/axs sync` 可立即同步装备列表。
:::

## 模块列表

ArcartX-Suite 提供以下模块，按授权方式分为 **免费模块**、**付费模块** 与 **福利模块**：

### 免费模块

服务器绑定云端后自动装备，无需额外购买：

- `announcer` — 播报系统（含 HUD 公告 + 打字机字幕）
- `combateffect` — 战斗特效（含击杀特效 + 伤害飘字）
- `eventpacket` — 事件引擎（含客户端回包预设功能）
- `loginview` — 登录界面
- `onlinerewards` — 在线奖励与签到
- `pickup` — 拾取提示
- `prop` — 快捷道具
- `rgb` — RGB 渐变文本
- `essentials` — 基础工具（玩家管理、传送系统、安全管控、实用工具）
- `regions` — 区域保护（40+ 保护标志、世界规则、成员权限、子区域继承）
- `afkreward` — 挂机奖励

### 付费模块

需在 [云端平台](https://cloud.021209.xyz) 购买授权后，于「装备模块」页面勾选到对应服务器：

- `conversation` — 对话系统（NPC 对话 + 选择器）
- `mail` — 邮箱系统
- `title` — 称号系统
- `map` — 地图系统
- `questgps` — 任务导航
- `warehouse` — 仓库系统
- `market` — 全球市场（系统商店 + 玩家拍卖行 + 回收商店，多货币、跨服同步）
- `qqbot` — QQ群服互联（OneBot 11 协议，消息同步、绑定、白名单、群指令查询）
- `battlepass` — 战令系统
- `fishing` — 钓鱼系统
- `lottery` — 抽奖系统
- `menu` — 通用 ArcartX 菜单系统（配置驱动，支持 ESC 暂停界面替换）

### 福利模块

按累计消费在云端平台领取授权份额：

- `chat` — 聊天系统（**任意消费**可领 5 份额）
- `tab` — Tab 在线列表（**消费满 ¥100** 可领 5 份额）
- `entitytracker` — 实体追踪（含 Boss 追踪 + 攻击目标 HUD；**消费满 ¥200** 可领 5 份额）
