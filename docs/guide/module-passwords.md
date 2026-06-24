---
title: 模块启用 | ArcartX-Suite Minecraft插件文档
description: 模块启用 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 模块启用

AXS 的 `config.yml` 负责模块启用开关：

```yaml
modules:
  announcer:
    enabled: true
  warehouse:
    enabled: true
```

所有模块统一受 `config.yml` 的 `modules.<module>.enabled` 控制，`true` 后即可加载。

## 模块列表

AXS 提供以下模块：

- `announcer` — 播报系统（含 HUD 公告 + 打字机字幕）
- `entitytracker` — 实体追踪（含 Boss 追踪 + 攻击目标 HUD）
- `combateffect` — 战斗特效（含击杀特效 + 伤害飘字）
- `eventpacket` — 事件引擎（含客户端回包预设功能）
- `chat` — 聊天系统
- `conversation` — 对话系统（NPC 对话 + 选择器）
- `loginview` — 登录界面
- `mail` — 邮箱系统
- `onlinerewards` — 在线奖励与签到
- `pickup` — 拾取提示
- `prop` — 快捷道具
- `rgb` — RGB 渐变文本
- `tab` — Tab 在线列表
- `title` — 称号系统
- `map` — 地图系统
- `questgps` — 任务导航
- `warehouse` — 仓库系统
- `essentials` — 基础工具（玩家管理、传送系统、安全管控、实用工具）
- `regions` — 区域保护（40+ 保护标志、世界规则、成员权限、子区域继承）
- `market` — 全球市场（系统商店 + 玩家拍卖行 + 回收商店，多货币、跨服同步）
- `qqbot` — QQ群服互联（OneBot 11 协议，消息同步、绑定、白名单、群指令查询）
- `battlepass` — 战令系统
- `fishing` — 钓鱼系统
- `lottery` — 抽奖系统
- `menu` — 通用 ArcartX 菜单系统（配置驱动，支持 ESC 暂停界面替换）
- `afkreward` — 挂机奖励

