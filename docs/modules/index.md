# 模块

AXS 共 **17 个功能模块**，涵盖聊天、战斗、播报、追踪、登录等核心玩法。其中 **10 个免费模块** 开箱即用，**7 个付费模块** 需要单独授权。

## 依赖总览

| 模块 | 授权 | 必需依赖 | 按功能选装 | 说明 |
| --- | --- | --- | --- | --- |
| Announcer | 免费 | ArcartX | PlaceholderAPI、EventPacket | PAPI 只影响文本变量解析 |
| EntityTracker | 免费 | ArcartX | MythicMobs/MythicBukkit、PlaceholderAPI、NeigeItems、MythicLib/MMOItems | Boss 追踪需要 Mythic；普通攻击目标 HUD 可独立使用 |
| Chat | 免费 | ArcartX | PlaceholderAPI、Redis、MySQL | Redis/MySQL 只用于跨服或共享数据 |
| EventPacket | 免费 | ArcartX | PlaceholderAPI、MythicMobs/MythicBukkit、其他 AXS 模块 | 只在使用对应触发器或动作时需要 |
| CombatEffect | 免费 | ArcartX | MythicLib/MMOItems、CraneAttribute、AttributePlus、MythicMobs | 属性伤害来源会自动回退 |
| LoginView | 免费 | ArcartX | AuthMe、MySQL、EventPacket | `authme` 模式必须安装 AuthMe；`standalone` 可不用 |
| OnlineRewards | 免费 | ArcartX | PlaceholderAPI、Mail、Vault、Redis、MySQL | 邮件奖励、跨服同步按功能启用 |
| Pickup | 免费 | ArcartX | NeigeItems、MythicMobs/MythicBukkit、MMOItems | 物品库缺失时按普通物品显示 |
| Prop | 免费 | ArcartX | MythicLib/MMOItems、AttributePlus、命令型插件 | 只影响对应道具效果 |
| RGB | 免费 | ArcartX、PlaceholderAPI | Chat、Tab、Title | RGB 本质是 PAPI 输出 |
| Tab | 付费 | ArcartX、PlaceholderAPI、有效模块授权 | Redis、Title | PAPI 是渲染和排序核心依赖 |
| Warehouse | 付费 | ArcartX、有效模块授权 | Vault、PlayerPoints、PlaceholderAPI、MythicMobs、NeigeItems、MMOItems、MySQL | 货币、物品库、跨服存储按功能启用 |
| Map | 付费 | ArcartX、有效模块授权 | Vault、PlayerPoints、QuestGPS、MythicMobs、NeigeItems | 收费和任务导航按功能启用 |
| Mail | 付费 | ArcartX、有效模块授权 | PlaceholderAPI、Vault、PlayerPoints、Redis、MySQL、物品库插件 | 跨服邮件建议 MySQL + Redis |
| Title | 付费 | ArcartX、有效模块授权 | PlaceholderAPI、AttributePlus、CraneAttribute、MythicLib/MMOItems、MySQL | PAPI 只影响对外输出 |
| QuestGPS | 付费 | ArcartX、Chemdah、有效模块授权 | Map、EventPacket | Chemdah 是任务来源 |
| Conversation | 付费 | ArcartX、Chemdah、有效模块授权 | Adyeshach、EventPacket | Adyeshach 只影响 NPC 入口/选择器 |

## 免费模块

<div class="module-grid">
  <a href="announcer" class="module-card">
    <div class="card-icon">📢</div>
    <div class="card-title">Announcer 播报系统</div>
    <div class="card-desc">HUD 公告 + 打字机字幕动画，服务器信息播报一站式解决</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="entitytracker" class="module-card">
    <div class="card-icon">🐉</div>
    <div class="card-title">EntityTracker 实体追踪</div>
    <div class="card-desc">Boss 血条、伤害排行、自动结算 + 攻击目标 HUD 实时显示</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="chat" class="module-card">
    <div class="card-icon">💬</div>
    <div class="card-title">Chat 聊天</div>
    <div class="card-desc">多频道、私聊、@提及、SocialSpy、禁言、Redis 跨服转发</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="eventpacket" class="module-card">
    <div class="card-icon">⚡</div>
    <div class="card-title">EventPacket 事件引擎</div>
    <div class="card-desc">通用触发器 + 动作链，事件驱动玩法编排</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="combateffect" class="module-card">
    <div class="card-icon">🎯</div>
    <div class="card-title">CombatEffect 战斗特效</div>
    <div class="card-desc">击杀特效 + 伤害飘字，战斗视觉反馈一站式解决</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="loginview" class="module-card">
    <div class="card-icon">🔐</div>
    <div class="card-title">LoginView 登录界面</div>
    <div class="card-desc">ArcartX UI 登录/注册面板，独立模式或 AuthMe 桥接</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="onlinerewards" class="module-card">
    <div class="card-icon">🎁</div>
    <div class="card-title">OnlineRewards 在线奖励</div>
    <div class="card-desc">在线时长奖励、每日签到、补签卡、四维排行榜</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="pickup" class="module-card">
    <div class="card-icon">✨</div>
    <div class="card-title">Pickup 拾取提示</div>
    <div class="card-desc">物品拾取时在 HUD 上弹出提示动画</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="prop" class="module-card">
    <div class="card-icon">🗡️</div>
    <div class="card-title">Prop 快捷道具</div>
    <div class="card-desc">道具快捷键绑定、客户端按键效果、临时属性加成</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
  <a href="rgb" class="module-card">
    <div class="card-icon">🌈</div>
    <div class="card-title">RGB 渐变文本</div>
    <div class="card-desc">PAPI 占位符输出渐变/扫光效果文本，支持嵌套</div>
    <span class="card-badge badge-stable">✅ 免费</span>
  </a>
</div>

## 付费模块

::: tip 价格说明
以下价格为开发阶段预估价格（包含内置UI价格），可随时调整，请以正式发售时的实际价格为准。
:::

<div class="module-grid">
  <a href="warehouse" class="module-card">
    <div class="card-icon">🏦</div>
    <div class="card-title">Warehouse 仓库银行</div>
    <div class="card-desc">个人/共享仓库、多货币银行、展示预览、定期存款</div>
    <span class="card-badge badge-premium">💎 ¥98</span>
  </a>
  <a href="map" class="module-card">
    <div class="card-icon">🗺️</div>
    <div class="card-title">Map 地图</div>
    <div class="card-desc">世界地图、锚点传送、玩家路径点、小地图 HUD</div>
    <span class="card-badge badge-premium">💎 ¥88</span>
  </a>
  <a href="mail" class="module-card">
    <div class="card-icon">📬</div>
    <div class="card-title">Mail 邮箱</div>
    <div class="card-desc">玩家写信、预设派发、CDK 兑换、物品附件、跨服广播</div>
    <span class="card-badge badge-premium">💎 ¥88</span>
  </a>
  <a href="title" class="module-card">
    <div class="card-icon">🏅</div>
    <div class="card-title">Title 称号</div>
    <div class="card-desc">分组称号、套装属性、日期区间、头顶显示、聊天/Tab 前缀</div>
    <span class="card-badge badge-premium">💎 ¥78</span>
  </a>
  <a href="questgps" class="module-card">
    <div class="card-icon">🧭</div>
    <div class="card-title">QuestGPS 任务导航</div>
    <div class="card-desc">Chemdah 任务追踪、HUD 路径点导航、目标指引</div>
    <span class="card-badge badge-premium">💎 ¥38</span>
  </a>
  <a href="tab" class="module-card">
    <div class="card-icon">📋</div>
    <div class="card-title">Tab 在线列表</div>
    <div class="card-desc">ArcartX TAB UI 自定义在线列表，PAPI 变量排序分组</div>
    <span class="card-badge badge-premium">💎 ¥38</span>
  </a>
  <a href="conversation" class="module-card">
    <div class="card-icon">🗣️</div>
    <div class="card-title">Conversation 对话桥</div>
    <div class="card-desc">Chemdah 对话 + Adyeshach NPC 联动，ArcartX UI 渲染</div>
    <span class="card-badge badge-premium">💎 ¥28</span>
  </a>
</div>
