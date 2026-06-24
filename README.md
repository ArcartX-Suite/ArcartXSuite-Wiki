# ArcartXSuite

> **Suite — 组曲，亦是套装。**
>
> 在音乐中，**Suite（组曲）** 是由多首独立小曲串联而成的成套器乐作品。
> 一部组曲有四段——序曲启程，变奏展开，华彩交织，终章回响。
> 每一段风格、节奏各不相同，**分开能单独演奏，合起来是一部统一于同一调性的完整作品**。
>
> **ArcartX-Suite** 正是这样的理念：26+ 个模块如同 26+ 个乐章，每一个独立运作、各具音色、按需启用，
> 共享 ArcartX 的统一调性与 UI 主题，组合成一部属于你自己的 **服务器组曲**。
>
> 「 从登录到击杀，每一帧都是一个音符 」——不只是插件，是服务器玩法的交响组曲。

---

## 💡 项目定位

**ArcartXSuite** 是专为 **ArcartX** 生态构建的下一代全场景 Minecraft 服务器核心套件与玩法基础设施。一个插件覆盖 **26 个功能领域**，涵盖经济交易、全球市场、抽奖开箱、钓鱼小游戏、称号系统、战令系统、仓库银行等核心玩法，统一 ArcartX UI 体验，模块间深度联动，消除适配烦恼。

---

## 📦 模块一览（26 个）

### 免费模块（13 个）

| 模块 | 说明 |
|------|------|
| **RGB** | 渐变文本渲染，PAPI 输出扫光效果 |
| **Pickup** | 物品拾取 HUD 弹出提示 + 扫描器模式 |
| **Announcer** | 公告 / 字幕轮播系统，可点击执行命令 |
| **LoginView** | ArcartX UI 登录/注册面板，支持 AuthMe 桥接 |
| **OnlineRewards** | 在线奖励 / 每日签到 / 连续签到 / 补签卡 |
| **CombatEffect** | 战斗特效（击杀/连击/伤害飘字），四插件属性来源自动检测 |
| **EventPacket** | 事件引擎，9种触发器 × 11种动作自由组合 |
| **Prop** | 快捷道具栏，客户端按键绑定临时属性加成 |
| **Chat** | 全频道聊天系统，私聊/@提及/物品展示/敏感词过滤/跨服转发 |
| **Essentials** | 基础工具（传送/家/Warp/一键砍树/背包整理/UI 菜单） |
| **Regions** | 区域保护（40+ 标志/优先级继承/世界规则） |
| **Menu** | 配置驱动 ArcartX 菜单系统，ESC 替换、命令/物品绑定 |
| **AfkReward** | 区域挂机 + 原地挂机双模式、周期命令奖励、排行榜 |

### 付费模块（11 个）

| 模块 | 说明 |
|------|------|
| **Title** | 分组称号系统，有效期/品质/套装属性/头顶显示/聊天Tab前缀 |
| **Conversation** | NPC 对话引擎，Chemdah + Adyeshach 联动，ArcartX UI 渲染 |
| **Mail** | 邮件系统，玩家写信/预设派发/CDK兑换/物品附件/跨服广播 |
| **Warehouse** | 仓库银行，个人/共享仓库、多货币银行、定期存款、二级密码 |
| **QuestGPS** | 任务导航，Chemdah 任务追踪、路径寻路 + 3D模型标记 |
| **Map** | 大地图 / 锚点传送 / 玩家路径点 / 小地图 HUD |
| **BattlePass** | 战令系统，三层通行证 + 日/周/赛季任务池 + ArcartX UI 面板 |
| **Market** | 全球市场插件，系统商店 + 玩家拍卖行 + 回收商店，多货币跨服同步 |
| **Lottery** | 抽奖系统插件，CS 开箱滚动动画 + 原神祈愿卡池，纯色块 UI、保底机制 |
| **Fishing** | 钓鱼系统插件，星露谷风格钓鱼小游戏、多水域生态、钓鱼图鉴 |
| **QQBot** | QQ 群服互联插件，OneBot 11 双向消息同步、QQ-游戏账号绑定 |

### 福利模块（2 个）

| 模块 | 说明 |
|------|------|
| **Tab** | 在线列表插件，ArcartX TAB UI 渲染，排序/分组/PAPI/跨服快照 |
| **EntityTracker** | 实体追踪插件，Boss 血条 HUD、实时伤害排行、自动结算奖励、跨服排行 |

---

## 🛠️ 核心设计

### ❖ 模块化
全部 26 个模块独立 jar，支持热加载 / 热卸载，`config.yml` 一键开关，按需启用。

### ❖ 跨模块联动
原生 Capability 桥接，模块间零配置联动。事件包、称号授予、邮件派发、聊天卡片、QQ 推送、QuestGPS 导航等能力可自由组合。

### ❖ ArcartX UI 原生
所有模块统一使用 ArcartX 客户端 UI 渲染，实现游戏级沉浸式交互体验。无外部贴图依赖，全部支持纯色块与渐变色自定义。

### ❖ 智能配置体检
内置 `ConfigDiagnosticEngine`，自动检测配置结构差异、类型错误、字段迁移，一键修复。

### ❖ 跨服同步
支持 CrossServer SDK（Redis + Proxy 双后端），Market 拍卖行、OnlineRewards 签到、Mail 邮件、Tab 列表等模块可跨服务器无缝同步。

---

## 🔧 环境要求

- **Java** 17+
- **Minecraft** 1.20.1+（Spigot / Paper / Mohist / Arclight）
- **ArcartX** 客户端 MOD

---

## 🌐 相关链接

- 📖 **[官方文档](https://xuanmomo233.github.io/ArcartXSuite-Wiki/)** — Minecraft 服务器插件配置教程与 API 参考
- 💻 **[GitHub 仓库](https://github.com/xuanmomo233/ArcartXSuite)** — 我的世界插件源码与 Issue 反馈
- 🏰 **[ArcartX 社区](https://arcartx.com)** — 连接 Minecraft 服主与开发者
- 🔗 **[ArcartX 官方文档](https://wiki.arcartx.com/docs)** — ArcartX 客户端框架使用指南
