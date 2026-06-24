---
title: 模块总览 - ArcartX-Suite 26个Minecraft服务器插件 | 我的世界插件套件
description: ArcartX-Suite 提供 26 个 Minecraft 服务器功能模块，涵盖聊天、战斗、播报、经济交易、全球市场、抽奖开箱、钓鱼、称号、战令、仓库等核心玩法，是 Minecraft ArcartX 服务器开发者的首选插件系列。
---

# 模块

AXS 共 **26 个功能模块**，涵盖聊天、战斗、播报、追踪、登录、基础工具、区域保护、通用菜单、经济交易、QQ群联动、钓鱼、抽奖等核心玩法，所有模块均通过 `config.yml` 的 `modules.<module>.enabled` 开关启用。

## 🆕 1.1.0-beta 新特性

- **Essentials 基础工具**：新增完整玩家工具模块，含传送/管控/一键砍树/背包操作
- **Essentials UI 面板**：玩家菜单（首页/家/传送点/TPA/设置）+ 管理员面板（玩家/封禁/传送点/世界）
- **Regions 区域保护**：新增类 WorldGuard 区域保护模块，40+ 标志 + 世界规则
- **Regions UI 面板**：玩家区域查看（当前/我的/详情）+ 管理员区域管理（列表/编辑/标志切换）
- **配置目录拆分**：大型内联配置段已拆分为独立目录，便于管理和维护
- **热加载/卸载**：支持运行时动态加载和卸载模块，无需重启服务端
- **配置智能体检**：自动检测配置问题并提供修复建议
- **Menu 通用菜单**：新增配置驱动 ArcartX 菜单系统，支持 ESC 替换、命令/物品绑定、按钮图标
- **QQBot 集成**：新增 QQBot 付费模块，通过 OneBot 11 连接 QQ 机器人，打通 QQ 群、服务器、玩家数据三者

## 配置目录拆分说明

从 1.1.0-beta 开始，以下模块的大型内联配置段已拆分为独立目录：

| 模块 | 拆分的配置 | 新目录结构 |
| --- | --- | --- |
| Announcer | `entries:`、`subtitle.groups:` | `data/announcer/entries/*.yml`、`data/announcer/subtitle/groups/*.yml` |
| RGB | `entries:` | `data/rgb/entries/*.yml` |
| Tab | `tabs:` | `data/tab/tabs/*.yml` |
| Title | `titles:` | `data/title/titles/*.yml` |
| EntityTracker | `bosses:` | `data/entitytracker/bosses/*.yml` |
| CombatEffect | `packets:` | `data/combateffect/packets/*.yml` |
| EventPacket | `rules:` | `data/eventpacket/rules/*.yml` |
| QuestGPS | `quests:` | `data/questgps/quests/*.yml` |

升级时请手动将对应段落内容复制到新目录的文件中。首次启动会自动导出默认示例文件。

## 依赖总览

| 模块 | 必需依赖 | 按功能选装 | 说明 |
| --- | --- | --- | --- |
| **Essentials** | ArcartX | PlaceholderAPI、Tab、Chat | 基础玩家工具集合 |
| **Regions** | ArcartX | — | 区域保护 + 世界规则 |
| **Menu** | ArcartX | PlaceholderAPI、BlinkAriaHost（Aria 条件）、MythicMobs/NeigeItems/MMOItems | 配置驱动菜单；[条件系统](/guide/conditions) |
| **AfkReward** | ArcartX | PlaceholderAPI | PAPI 用于奖励命令解析和对外输出 |
| Announcer | ArcartX | PlaceholderAPI、EventPacket | PAPI 只影响文本变量解析 |
| Chat | ArcartX | PlaceholderAPI、MySQL | MySQL 用于跨服共享禁言/状态；跨服走宿主 cross-server |
| EventPacket | ArcartX | PlaceholderAPI、MythicMobs/MythicBukkit、其他 AXS 模块 | 只在使用对应触发器或动作时需要 |
| CombatEffect | ArcartX | MythicLib/MMOItems、CraneAttribute、AttributePlus、MythicMobs | 属性伤害来源会自动回退 |
| LoginView | ArcartX | AuthMe、MySQL、EventPacket | `authme` 模式必须安装 AuthMe；`standalone` 可不用 |
| OnlineRewards | ArcartX | PlaceholderAPI、Mail、Vault、MySQL | 邮件奖励；跨服需 MySQL + 宿主 cross-server |
| Pickup | ArcartX | NeigeItems、MythicMobs/MythicBukkit、MMOItems | 物品库缺失时按普通物品显示 |
| Prop | ArcartX | MythicLib/MMOItems、AttributePlus、命令型插件 | 只影响对应道具效果 |
| RGB | ArcartX、PlaceholderAPI | Chat、Tab、Title | RGB 本质是 PAPI 输出 |
| Warehouse | ArcartX | Vault、PlayerPoints、PlaceholderAPI、MythicMobs、NeigeItems、MMOItems、MySQL | 货币、物品库、跨服存储按功能启用 |
| Map | ArcartX | Vault、PlayerPoints、QuestGPS、MythicMobs、NeigeItems | 收费和任务导航按功能启用 |
| Mail | ArcartX | PlaceholderAPI、Vault、PlayerPoints、MySQL、物品库插件 | 跨服邮件建议 MySQL + 宿主 cross-server |
| Title | ArcartX | PlaceholderAPI、AttributePlus、CraneAttribute、MythicLib/MMOItems、MySQL | PAPI 只影响对外输出 |
| QuestGPS | ArcartX、Chemdah | Map、EventPacket | Chemdah 是任务来源 |
| Conversation | ArcartX、Chemdah | Adyeshach、EventPacket | Adyeshach 只影响 NPC 入口/选择器 |
| **Market** | ArcartX、MySQL | Redis（缓存）、Vault、PlayerPoints、PlaceholderAPI、MythicMobs、NeigeItems | Redis 可选列表缓存；跨服走 cross-server |
| **Lottery** | ArcartX | Mail、MythicMobs、NeigeItems、PlaceholderAPI | 背包满时自动发邮件；物品库用于奖品来源；PAPI 用于输出统计 |
| **QQBot** | ArcartX、OneBot 11 实现端 | MySQL、PlaceholderAPI、Chat | OneBot 实现端需自行部署（Lagrange/NapCat/LLOneBot） |
| EntityTracker | ArcartX | MythicMobs/MythicBukkit、PlaceholderAPI、NeigeItems、MythicLib/MMOItems、宿主 CrossServer、Vault | Boss 追踪需要 Mythic；普通攻击目标 HUD 可独立使用；跨服排行需宿主 cross-server；Vault 用于金钱奖励 |
| Tab | ArcartX、PlaceholderAPI | Title | 跨服快照走宿主 cross-server（建议 Redis） |

## 功能模块

<div class="module-grid">
  <a href="announcer" class="module-card">
    <div class="card-icon">📢</div>
    <div class="card-title">Announcer 播报系统</div>
    <div class="card-desc">HUD 公告 + 打字机字幕动画，服务器信息播报一站式解决</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
    <a href="chat" class="module-card">
    <div class="card-icon">💬</div>
    <div class="card-title">Chat 聊天</div>
    <div class="card-desc">多频道系统、私聊回复、@提及通知、物品展示、敏感词过滤、CrossServer 跨服转发</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="eventpacket" class="module-card">
    <div class="card-icon">⚡</div>
    <div class="card-title">EventPacket 事件引擎</div>
    <div class="card-desc">9种触发器×11种动作自由组合，内置实体清理 + 定时命令，支持跨模块联动</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="combateffect" class="module-card">
    <div class="card-icon">🎯</div>
    <div class="card-title">CombatEffect 战斗特效</div>
    <div class="card-desc">击杀特效、连击追踪、死亡缓冲、伤害飘字，支持按键/状态/控制器触发，四插件属性来源自动检测</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="loginview" class="module-card">
    <div class="card-icon">🔐</div>
    <div class="card-title">LoginView 登录界面</div>
    <div class="card-desc">ArcartX UI 登录/注册面板，独立模式或 AuthMe 桥接</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="onlinerewards" class="module-card">
    <div class="card-icon">🎁</div>
    <div class="card-title">OnlineRewards 在线奖励</div>
    <div class="card-desc">在线时长阶段奖励、每日签到、连续签到、补签卡、四维排行榜、跨服同步</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="pickup" class="module-card">
    <div class="card-icon">✨</div>
    <div class="card-title">Pickup 拾取提示</div>
    <div class="card-desc">物品拾取 HUD 弹出提示 + 扫描器模式，双模式可切换</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="prop" class="module-card">
    <div class="card-icon">🗡️</div>
    <div class="card-title">Prop 快捷道具</div>
    <div class="card-desc">道具快捷键绑定、客户端按键效果、临时属性加成</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="rgb" class="module-card">
    <div class="card-icon">🌈</div>
    <div class="card-title">RGB 渐变文本</div>
    <div class="card-desc">PAPI 占位符输出渐变/扫光效果文本，支持嵌套</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="essentials" class="module-card">
    <div class="card-icon">🛠️</div>
    <div class="card-title">Essentials 基础工具</div>
    <div class="card-desc">玩家管理、传送系统、安全管控、一键砍树、背包操作、UI 玩家菜单 + 管理员面板</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="regions" class="module-card">
    <div class="card-icon">🛡️</div>
    <div class="card-title">Regions 区域保护</div>
    <div class="card-desc">类 WorldGuard 区域保护、40+ 标志、世界规则、UI 区域查看 + 管理员编辑面板</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="menu" class="module-card">
    <div class="card-icon">📋</div>
    <div class="card-title">Menu 通用菜单</div>
    <div class="card-desc">配置驱动 ArcartX 菜单、ESC 替换、命令/物品绑定、按钮图标，类 TrMenu 体验</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="afkreward" class="module-card">
    <div class="card-icon">♨️</div>
    <div class="card-title">AfkReward 挂机奖励</div>
    <div class="card-desc">区域挂机 + 原地挂机双模式、周期命令奖励、VIP 权限阶梯、人数/次数上限控制、服崩恢复、排行榜、PAPI 输出、HUD 面板</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
</div>

<div class="module-grid">
  <a href="fishing" class="module-card">
    <div class="card-icon">🎣</div>
    <div class="card-title">Fishing 钓鱼系统</div>
    <div class="card-desc">星露谷风格钓鱼小游戏、多水域生态、饵料加成、宝藏奖励、钓鱼图鉴</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="warehouse" class="module-card">
    <div class="card-icon">🏦</div>
    <div class="card-title">Warehouse 仓库银行</div>
    <div class="card-desc">个人/共享仓库、多货币银行、展示预览、定期存款</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="mail" class="module-card">
    <div class="card-icon">📬</div>
    <div class="card-title">Mail 邮箱</div>
    <div class="card-desc">玩家写信、预设派发、CDK 兑换、物品附件、跨服广播</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="title" class="module-card">
    <div class="card-icon">🏅</div>
    <div class="card-title">Title 称号</div>
    <div class="card-desc">分组称号、套装属性、日期区间、头顶显示、聊天/Tab 前缀</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="questgps" class="module-card">
    <div class="card-icon">🧭</div>
    <div class="card-title">QuestGPS 任务导航</div>
    <div class="card-desc">Chemdah 任务追踪、路径寻路 + 3D模型标记、任务指引HUD、任务菜单</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="map" class="module-card">
    <div class="card-icon">🗺️</div>
    <div class="card-title">Map 地图</div>
    <div class="card-desc">世界地图、锚点传送、玩家路径点、小地图 HUD</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="conversation" class="module-card">
    <div class="card-icon">🗣️</div>
    <div class="card-title">Conversation 对话桥</div>
    <div class="card-desc">Chemdah 对话 ArcartX UI 渲染，调用ArcartX 给 NPC 设置模型动画</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="battlepass" class="module-card">
    <div class="card-icon">🎖️</div>
    <div class="card-title">BattlePass 战令系统</div>
    <div class="card-desc">三层通行证（免费/高级/典藏）+ 每日/每周/赛季任务池 + 条件过滤 + 动态增量 + 加权随机分配 + ArcartX UI 面板</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="lottery" class="module-card">
    <div class="card-icon">🎰</div>
    <div class="card-title">Lottery 抽奖系统</div>
    <div class="card-desc">CS 开箱横向滚动动画 + 原神祈愿卡池系统，纯色块 UI、品质颜色绑定、保底机制、磨损度系统</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="market" class="module-card">
    <div class="card-icon">🏪</div>
    <div class="card-title">Market 全球市场</div>
    <div class="card-desc">系统商店 + 玩家拍卖行 + 回收商店，多货币支持、跨服同步、ArcartX UI 全套交易界面</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="qqbot" class="module-card">
    <div class="card-icon">🤖</div>
    <div class="card-title">QQBot QQ群服互联</div>
    <div class="card-desc">OneBot 11 双向同步、QQ-游戏账号绑定、白名单联动、群指令查玩家数据/PAPI/服务器命令</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
</div>

<div class="module-grid">
  <a href="entitytracker" class="module-card">
    <div class="card-icon">🐉</div>
    <div class="card-title">EntityTracker 实体追踪</div>
    <div class="card-desc">Boss血条HUD、实时伤害排行、自动结算奖励、攻击目标信息显示、多Boss并行追踪、掉落记录、分配系统、跨服排行、排行榜每周每月奖励发放</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
  <a href="tab" class="module-card">
    <div class="card-icon">📋</div>
    <div class="card-title">Tab 在线列表</div>
    <div class="card-desc">ArcartX UI 自定义在线列表，支持ArcartX-Suite多模块联动、排序、分组、PAPI 变量、跨服、可模仿CS与LOL的TAB阵容功能，队友血量显示</div>
    <span class="card-badge badge-stable">✅ 可用</span>
  </a>
</div>
