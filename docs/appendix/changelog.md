# 变更日志

当前 wiki 只保留正在发布的 beta 版本记录，旧版历史不再展示。

---

## 1.1.0-beta（当前）

- **模块管理** — 新增 `/axs load <模块名>` 与 `/axs unload <模块名>` 子命令，支持运行时热加载新模块与热卸载已加载模块（释放 ClassLoader），不再需要重启服务端。卸载时会检查反向依赖，被其他模块依赖的模块会被拒绝卸载。
- **目录归位** — 模块产物统一收纳到 `plugins/ArcartXSuite/data/<moduleId>/`：配置文件 `config.yml`、SQLite 数据库、子目录（如 `chat/channels`、`mail/presets`、`prop/props`、`subtitle/groups`）等首次启动时一次性自动迁移，原 1.0.x 散落在根目录的旧路径全部归位。迁移日志带高亮色标。
- **控制台美化** — 启动 banner 改为 ANSI Shadow 字体的「SUITE」主标题 + 顶部 `✦ A R C A R T X ✦` 副标题，垂直青蓝紫渐变；迁移类日志统一格式 `→ 已归位 X: <来源> ➜ <目标>`，金/黄/灰/青多色标记。
- **授权** — 使用 QQ + 多授权码体系，支持单模块码和全模块 suite 码。
- **授权** — 付费模块绑定 `QQ + install_id + 机器指纹`，`security/local-salt.dat` 作为绑定身份的一部分。
- **授权** — `/axs license status|refresh|activate|rebind|cloud-code|fingerprint` 提供授权状态、刷新、激活、换绑、云端换绑挑战码和机器指纹诊断。
- **授权** — 云端网页换绑改为 QQ 授权账号登录 + 新服务器挑战码，不再接受仅凭 `QQ + 授权码` 直接换绑。
- **架构** — 保留宿主 + 模块 Jar 架构，`axs-core` 提供核心能力，`modules/*` 可按需外置加载。
- **资源保护** — 付费模块资源通过 ticket 中的 `resourceKeys` 解包后在内存中解密。
- **文档** — 安装、授权、命令速查和安全架构文档已同步到 `1.1.0-beta`。

### 1.1.0-beta (Build 2026-05-27) — Essentials 基础工具 + Regions 区域保护

- **Essentials** — 新增 `essentials` 模块，集合了服务器最常用的基础管理功能：
  - **玩家管理**：fly / god / heal / feed / gamemode / speed / vanish / afk / repair / hat / nick / seen
  - **容器**：enderchest / workbench / anvil / trash
  - **传送系统**：home / sethome / delhome / warp / setwarp / delwarp / spawn / setspawn / tpa / tpahere / tpaccept / tpdeny / back / tp / top / tppos
  - **世界管理**：time / weather
  - **安全管控**：ban / tempban / unban / mute / tempmute / unmute / kick / warn / sudo / inv
  - **交互**：sit / lay
  - **一键砍树 (TreeCapitator)**：连锁砍伐原木 + 树叶，可配置最大连锁数/斧头/潜行/耐久消耗
  - **背包操作 (InvActions)**：自动补种成熟作物（消耗种子）/ 背包整理（`/axs ess sort`）/ 自动工具切换
- **Essentials** — 配置文件 `ArcartXEssentials.yml`，支持 SQLite/MySQL 存储
- **Essentials** — 注册 `EssentialsQueryable` capability，供 Tab/Chat 等模块查询 AFK/Vanish 状态
- **Essentials** — 新增权限：`axs.essentials.treecap`、`axs.essentials.replant`、`axs.essentials.sort`、`axs.essentials.autotool`

- **Regions** — 新增 `regions` 模块，提供类 WorldGuard 的完整区域保护方案：
  - **选区工具**：木斧左/右键设置两点选区，自动显示体积
  - **区域 CRUD**：define / remove / redefine / list / info / tp
  - **标志系统**：40+ 保护标志（方块/实体/环境/玩家行为/特殊），三态 allow/deny/none
  - **成员管理**：所有者 / 成员 / 权限组（`g:组名`），成员豁免方块保护
  - **优先级 + 继承**：数值优先级，父区域继承未设置的标志
  - **世界规则**：按世界级别的禁飞/禁活塞/禁交互（从 Essentials 移入）
- **Regions** — 配置文件 `ArcartXRegions.yml`，支持 SQLite/MySQL 存储
- **Regions** — 新增权限：`axs.regions.select`、`axs.regions.admin`、`axs.regions.bypass`、`axs.regions.bypass.limit`

- **文档** — 新增 `docs/modules/essentials.md` 和 `docs/modules/regions.md`
- **文档** — 命令速查、模块总览、sidebar 导航全面更新
- **构建** — 模块总数从 17 → 19，全量 107 任务 BUILD SUCCESSFUL

### 1.1.0-beta (Build 2026-05-26) — LoginView 正版/LittleSkin 免登录

- **LoginView** — 新增正版/LittleSkin 白名单免登录功能（`auth.premium-bypass`）：启用后，通过 authlib-injector（LittleSkin）或 Mojang 正版认证的玩家无需输入密码，直接显示「进入服务器」按钮一键登录。
- **LoginView** — 检测原理：正版/LittleSkin 认证后玩家 UUID version=4（随机 UUID），离线模式玩家 UUID version=3（name-based UUID）。
- **LoginView** — 配置新增 `auth.premium-bypass.enabled`（默认 false）和 `auth.premium-bypass.message`（免登录成功提示）。
- **LoginView** — 服务端新增 `bypass_enter` packet action，UI 新增 `bypass` 类型视图（含免登录提示文字 + 进入服务器按钮）。
- **LoginView** — 两套 UI（`login_view.yml` 和 `login_view_menu.yml`）均已适配 bypass 视图。
- **LoginView** — `LoginViewModuleConfiguration` 新增 `PremiumBypassConfiguration` record。
- **LoginView** — `buildInitPayload` 新增 `premiumBypass` 字段，客户端通过 `var.premiumBypass` 和 `var.type == 'bypass'` 控制视图切换。
- **LoginView** — 新增 `AuthlibInjectorHelper` 工具类：运行时检测 authlib-injector 是否已加载、从 LittleSkin 自动下载最新版、生成含 `-javaagent` 参数的启动脚本。
- **LoginView** — 新增 `/axs loginview setup-authlib` 命令：一键下载 authlib-injector 并生成 `start-littleskin.bat` / `start-littleskin.sh`。
- **LoginView** — 启动时若 `premium-bypass.enabled: true` 但未检测到 authlib-injector Agent，控制台自动输出配置指南。

### 1.1.0-beta (Build 2026-05-26) — Announcer 跨服广播 + 公告目录重命名

- **Announcer** — 公告条目目录从 `entries/` 重命名为 `announcer/`，旧目录自动迁移。配置 `entries-directory` 默认值同步更新。
- **Announcer** — 新增跨服广播功能（BungeeCord Forward）：配置 `transport.proxy` 段启用后，`broadcast` / `broadcastnow` 命令发出的手动公告会转发到其他子服，同时接收来自其他子服的公告。自动轮播条目不跨服。
- **Announcer** — 新增 `AnnouncerProxyTransport`、`AnnouncerEnvelope`、`AnnouncerEnvelopeCodec`、`AnnouncerProxyConfiguration` 类；`AnnouncerService` 构造函数新增 `transport`/`nodeId` 参数。
- **Announcer** — `/axs announcer status` 新增跨服传输状态显示。
- **Announcer** — 版本号从 `1.0.2-beta` 升至 `1.1.0-beta`。

### 1.1.0-beta (Build 2026-05-26) — Pickup 交互重构 + Conversation UI 优化

- **Pickup** — 扫描模式交互重构为双 UI 架构：
  - **loot_panel**（常驻 HUD）：`through: false`，直接接管鼠标点击和滚轮；内部 Canvas 及子控件 `through: true` 让事件冒泡到顶层 `adaptive`。
  - **loot_interact**（透明交互菜单）：按 F 键打开，`through: true`，ESC 关闭时发送 `close_menu` 包；仅处理滚轮切换，不处理按键拾取。
- **Pickup** — F 键功能精简为仅打开交互菜单（`loot_interact`），不再用于拾取。拾取完全由鼠标点击实现：点击物品条目（`bg_N`）发送 `pick_N` 包直接拾取指定索引物品；点击空白区域发送 `pick` 包拾取当前选中物品。
- **Pickup** — 服务端 `handleClientPacket` 新增 `pick_N`（N=0~7）action 解析，`handlePick` 支持指定索引或使用当前 `selectedIndex`。
- **Pickup** — `Packet.send` 仅支持 `(channel, action)` 两个参数，不支持第三个 data 参数。索引通过编码到 action 字符串中传递（`pick_0` ~ `pick_7`）。
- **Pickup** — 移除 `loot_panel` 中的 `panel_bg` 控件、`shadow` 属性和 `level`/`closeDied`/`background` 配置项。
- **Pickup** — 新增 `client.pickup` 客户端变量联动：`adaptive` 控件的 `click` 仅在 `client.pickup == true` 时发送拾取包。
- **Conversation** — NPC 选择器 HUD（`conversation_selector`）UI 优化：
  - `through` 改为 `true`，移除 `background`/`closeDied`，新增 `level: 0`。
  - `selector_root` 新增 `enter`/`leave` 事件，悬停时设置 `client.pickup = false`，离开时恢复 `client.pickup = true`，避免点击 NPC 选项时误触 Pickup 拾取。
- **文档** — `pickup.md` 更新：交互方式表、按键绑定表、UI 面板说明、技术架构图和数据流全部重写以反映双 UI 交互模型。
- **文档** — `conversation.md` 新增 NPC 选择器 HUD 特性段落，说明 Pickup 联动机制。

### 1.1.0-beta (Build 2026-05-25) — Conversation NPC 模型应用 Bug 修复

- **Conversation** — 修复 `npc-appearances` 配置对 Adyeshach NPC 应用模型失败的问题（日志：`AdyeshachNpcBridge: ArcartXEntityManager 未提供 getOrCreateEntity(int) 方法，无法为私有实体应用模型`、`NPC "xxx" 应用失败`）。
  - **根因**：`AdyeshachNpcBridge.applyModel/applyAnimation/applyDefaultState` 在 Bukkit Entity 路径失败时回退到 `ArcartXEntityManager.getOrCreateEntity(int)`，但 ArcartX 实际 API 只提供 `getOrCreateEntity(Entity)` 和 `getEntity(UUID)` 两个重载，从无 `int` 版本，回退路径必然失败。同时 Adyeshach 公共/私有 NPC 都是基于协议包的虚拟实体，并不附带真实 `org.bukkit.entity.Entity`，Bukkit Entity 路径同样无效。
  - **修复**：删除不存在的 `getOrCreateEntity(int)` 反射查找；新增 NetworkSender 广播回退路径——当 Bukkit Entity 路径失败时，自动遍历能看见该实体的在线玩家（通过 `isVisibleViewer(Player)` 判定），对每个玩家调用 `ArcartXNetworkSender.sendSetEntityModel/sendSetEntityAnimation` 点对点发包。此路径已在 QuestGPS 模块 (`applyModelForPlayer`) 验证可行。
  - **影响范围**：`applyModel`、`applyAnimation`、`applyDefaultState` 三个 API，以及调用它们的 `/axs conversation adyeshach setModel|setAnimation|playAnimation` 命令与 Conversation 模块 reload 时的 `npc-appearances` 自动应用流程。
  - **已知限制**：NetworkSender 路径是 per-player 视觉应用，对当前不在视野内的玩家以及后续新进入视野的玩家无效——需要等待后续实现"实体进入视野时自动重发模型包"的增强（监听 Adyeshach 实体可见事件）。当前修复保证 reload/手动命令时立即对所有已看见实体的在线玩家生效。

- **Conversation** — 修复服务器**启动时** `npc-appearances` 报"未找到 NPC"的问题（日志：`ArcartXConversation npc-appearances: 未找到 NPC "xxx"，跳过`）。
  - **根因**：Adyeshach 的 NPC json 加载与 ArcartXSuite 模块启动之间存在时序竞态。`ConversationService.initializeInteractionEnhancement` 末尾立即同步调用 `applyNpcAppearances`，而此时 Adyeshach 的 `PublicEntityManager.getEntities()` 可能尚未完成对 `plugins/Adyeshach/npc/*.json` 的扫描加载，导致 `findByName` 返回 empty。reload 场景没有该问题（实体早已加载完）。
  - **修复**：将 `applyNpcAppearances` 改为延迟重试机制——第一次尝试遍历所有 NPC 条目，未找到的实体加入"待重试队列"，调度 `runTaskLater` 10 tick 后重试，最多重试 30 次（共 ~15 秒）。reload 场景第一次就成功，无延迟无副作用；启动场景静默等待 Adyeshach 加载完成，最终一致。仍找不到的 NPC 在 30 次重试后输出最终警告。
  - **新增字段/常量**：`npcAppearanceRetryTask`、`NPC_APPEARANCE_MAX_ATTEMPTS=30`、`NPC_APPEARANCE_RETRY_INTERVAL_TICKS=10L`；`shutdownInteractionEnhancement` 中通过 `cancelNpcAppearanceRetry()` 取消待重试任务，避免模块卸载/重载时遗留计时器。

- **Conversation** — 架构改进：彻底解决 NPC 模型在客户端不显示的问题，改用 **"玩家看见 NPC 时立即发送模型包"** 的事件驱动模型。
  - **背景**：前两条修复后服务端日志已无报错，但客户端仍看不到 NPC 模型。根因是 NetworkSender 的 `sendSetEntityModel` 是 per-player 即时发送的，需要在玩家客户端**已经持有该实体**的时刻才有效——若发送早于"实体生成给客户端"，包会被客户端丢弃；若玩家暂时离开 NPC 视野再回来，模型也会丢失。
  - **新架构**：在 `AdyeshachNpcBridge` 中通过反射注册 `ink.ptms.adyeshach.core.event.AdyeshachEntityVisibleEvent`（继承自 TabooLib `BukkitProxyEvent`，等效 Bukkit Event），监听玩家进入 NPC 视野范围的瞬间。在该事件触发时，`ConversationService` 反查配置映射表（`npc 名称 -> NpcAppearanceEntry`），若匹配则立即对该玩家调用 `applyModelForPlayer / applyAnimationForPlayer / applyDefaultStateForPlayer`，保证模型/动画/状态包与客户端的实体生成时序对齐。
  - **新增 API**（`AdyeshachNpcBridge`）：
    - `registerVisibleHandler(BiConsumer<Player, Object>)` — 注册可见事件回调，通过 `PluginManager.registerEvent` + `EventExecutor` + 反射加载事件类（避免编译期依赖 Adyeshach），仅处理 `visible == true` 事件
    - `unregisterVisibleHandler()` — 取消监听，shutdown 时自动调用
    - `getEntityNames(Object)` — 反向匹配辅助，返回实体的 displayName/customName/id 列表，供上层用同一识别逻辑反查配置
  - **ConversationService 集成**：
    - 新增 `volatile Map<String, NpcAppearanceEntry> appearanceMapByLowerName` 映射表
    - `applyNpcAppearances` 现在同时做三件事：构建映射表 → 注册 visible handler → 立即广播一次（兜底，对已在线且已看见 NPC 的玩家）
    - `onAdyeshachEntityVisible` 回调：玩家进入 NPC 视野时按候选名（displayName/customName/id）匹配配置，匹配则发送模型/动画/默认状态包
    - `shutdownInteractionEnhancement` 中通过 `npcBridge.unregisterVisibleHandler()` 清理监听器并重置映射表
  - **效果**：
    - 服务器启动后第一个玩家上线、走近 NPC 时即刻看到模型；后续每次玩家进入视野（NPC 离开/重新出现）都会自动重发模型包
    - 不再依赖 ArcartX 的 Bukkit Entity 路径或 ArcartXEntityManager API，纯走 ArcartXNetworkSender 点对点发包，与 QuestGPS 的标记实体模型机制完全一致
    - reload 时立即对所有当前已看见 NPC 的玩家应用，并自动接管后续视野变化

- **Conversation** — 修复 visible 事件触发早于 ArcartX 客户端 mod 握手完成导致模型包丢失的问题（症状：玩家登录后必须 `/axs reload conversation` 才能看到 NPC 模型）。
  - **根因**：玩家登录后 Adyeshach 几乎立刻（~2 秒）触发 `AdyeshachEntityVisibleEvent`，此时服务端 `ArcartXNetworkSender.sendSetEntityModel` 立即发出，但**ArcartX 客户端 mod 与服务端的 ArcartX 通信通道握手尚未完成**，客户端无法处理该包并丢弃。reload 时手动重新应用因玩家已稳定，客户端早就准备好，所以有效。
  - **修复**：在 `ConversationModule` 中通过 `createInitializedHandler()` 注册 `ClientInitializedHandler`，监听 ArcartX 的 `ClientInitializedEvent$End`（宿主 `ArcartXSuitePlugin` 已通过反射路由到模块）。该事件在客户端 mod 完成与服务端 ArcartX 通道握手后才触发，此时调用新增的 `ConversationService.applyNpcAppearancesForPlayer(player)` 对该玩家**补发**一次所有配置 NPC 的模型/动画/默认状态包，保证客户端 mod 已就绪并能正确应用。
  - **新增 API**：`ConversationService.applyNpcAppearancesForPlayer(Player)` — per-player 全量补发，遍历 `appearanceMapByLowerName` 并通过 `applyModelForPlayer / applyAnimationForPlayer / applyDefaultStateForPlayer` 点对点发包，debug 模式下记录"已补发 N 个 NPC 模型包"日志。
  - **双保险机制**：现在 NPC 模型应用有三条独立路径互相兜底：(1) reload/启动后立即广播（对已在线已看见的玩家）；(2) `AdyeshachEntityVisibleEvent` 监听器（玩家移动进入视野时）；(3) `ClientInitializedHandler`（玩家登录且客户端握手完成时）。任一路径成功即可使客户端看到模型，无单点失效风险。

### 1.1.0-beta (Build 2026-05-24e) — Pickup 玩家指令

- **Pickup** — 新增 `/pickup` 玩家指令（权限 `arcartxsuite.pickup.use`，默认全员拥有），支持 `on` / `off` / `status` 子命令，玩家可自行切换拾取功能开关。
- **Pickup** — 关闭后效果：通知模式下不再显示 HUD 通知；扫描模式下停止扫描并恢复原版自动拾取行为。
- **Pickup** — `plugin.yml` 新增 `pickup` 命令注册和 `arcartxsuite.pickup.use` 权限声明。

### 1.1.0-beta (Build 2026-05-24d) — 开放 API 升级

- **API** — 新增三个类型安全的桥接接口，取代原先返回 `Object` 的旧 API：
  - `PacketBridgeAPI` — UI 注册/打开/关闭/发包/聊天卡片/关闭回调
  - `ClientBridgeAPI` — 伤害飘字/服务端变量下发/可见玩家遍历
  - `ItemBridgeAPI` — ItemStack → JSON 序列化
- **API** — `ModuleContext` 的 `packetBridge()` / `clientBridge()` / `itemStackBridge()` 返回类型从 `Object` 改为对应的类型安全接口，模块无需再强制转型。
- **API** — 新增 `@ApiStability` 注解体系（`@Stable` / `@Experimental` / `@Internal` / `@Deprecated`），标记每个公开 API 的稳定性级别，指导第三方开发者安全依赖。
- **API** — 新增 `ModuleLifecycleEvent` Bukkit 事件，模块加载/卸载/重载时触发，支持 7 种生命周期阶段（`ENABLING` / `ENABLED` / `ENABLE_FAILED` / `DISABLING` / `DISABLED` / `RELOADING` / `RELOADED`），第三方插件可通过标准事件机制监听。
- **API** — `UiRegistrationResult` record 迁移到 `PacketBridgeAPI.UiRegistrationResult`，`normalizeUiId` 迁移到 `PacketBridgeAPI.normalizeUiId`。
- **API** — `ModuleContext.vaultEconomyBridge()` 和 `propBridge()` 标记为 `@Internal`，第三方不应依赖。
- **API** — `ModuleContext.registerCapability()` 和 `getCapability()` 标记为 `@Stable`。
- **文档** — 新增完整 API 参考文档（`/api/`），涵盖模块生命周期、ModuleContext、桥接 API、事件和 Capability 跨模块通信。

### 1.1.0-beta (Build 2026-05-24c) — Title 总展示称号 PAPI

- **Title** — 新增 `display-title` 配置节，支持按指定分组列表（或全部分组）拼接已装备称号的总展示文本，可自定义分隔符和空文本。
- **Title** — 新增 6 个 PAPI 占位符：`%AXStitle_display%` / `%AXStitle_display_name%`（总称号名称）、`%AXStitle_display_chat_prefix%` / `%AXStitle_display_chat_suffix%`（聊天前/后缀）、`%AXStitle_display_tab_prefix%` / `%AXStitle_display_tab_suffix%`（TAB 前/后缀）。
- **Title** — 只想展示单个组称号时，`display-title.groups` 只填一个组 ID 即可；想展示多组则填多个，留空则按定义顺序展示所有组。
- **Title** — 称号菜单 UI 新增「总展示称号」预览行，实时显示拼接后的总展示称号名称；packet 新增 `display_title_name`、`display_title_chat_prefix`、`display_title_chat_suffix`、`display_title_tab_prefix`、`display_title_tab_suffix` 字段。
- **Title** — wiki `docs/modules/title.md` 同步更新：新增「总展示称号」PAPI 段落、数据契约字段、`display-title` 配置说明及典型场景表。

### 1.1.0-beta (Build 2026-05-24b) — Pickup 双模式升级

- **Pickup** — 模块升级为双模式架构（版本号 `1.0.2-beta` → `1.1.0-beta`）：
  - **通知模式（notification）**：原有功能，拾取时弹出 HUD 提示（物品名、数量、图标）。
  - **扫描模式（scanner）**：禁用自动拾取，周期扫描附近掉落物实体并以面板形式在 HUD 展示，玩家通过 F 键逐个交互拾取、滚轮/方向键切换选中项。
- **Pickup** — 新增掉落物过滤系统（仅扫描模式生效）：支持材质黑/白名单、物品名称正则、最小堆叠数量过滤。
- **Pickup** — 新增 `LootScannerService`：周期扫描任务、`EntityPickupItemEvent` 拦截、客户端交互包处理（`pick`/`scroll_up`/`scroll_down`）。
- **Pickup** — 新增 `LootFilterEngine`：评估物品是否应在面板中显示。
- **Pickup** — 新增 `loot_panel.yml` HUD UI 文件：暗色圆角面板 + 蓝色选中高亮 + 物品图标 + 名称数量 + F 键提示。
- **Pickup** — 配置文件 `ArcartXPickup.yml` 重构为三段式：`settings`（全局）+ `notification`（通知模式）+ `scanner`（扫描模式）+ `filter`（过滤规则）。
- **Pickup** — 扫描模式预留 Warehouse 仓库联动配置（`warehouse-auto-deposit`），拾取后可选直接存入仓库。
- **Pickup** — `PickupModule` 新增 `ClientPacketHandler`，处理客户端发来的拾取交互包。
- **Pickup** — `ValidationRule` 更新：移除旧的 `settings.auto-pickup-delay-ticks` / `settings.display-duration-ticks`，新增 `notification.max-visible`、`notification.entry-ttl-ms`、`scanner.scan-radius`、`scanner.scan-interval-ticks`、`scanner.max-display` 约束。
- **Pickup** — 过滤系统升级为五维过滤：在原有材质黑/白名单 + 名称正则 + 最小数量基础上，新增 **Lore 正则黑/白名单**（`lore-blacklist-regex` / `lore-whitelist-regex`）和 **NBT 键黑/白名单**（`nbt-blacklist-keys` / `nbt-whitelist-keys`，支持嵌套路径如 `custom.trash`）。

### 1.1.0-beta (Build 2026-05-24b) — Pickup 双模式升级

- **Pickup** — 模块升级为双模式架构（版本号 `1.0.2-beta` → `1.1.0-beta`）：
  - **通知模式（notification）**：原有功能，拾取时弹出 HUD 提示（物品名、数量、图标）。
  - **扫描模式（scanner）**：禁用自动拾取，周期扫描附近掉落物实体并以面板形式在 HUD 展示，玩家通过 F 键逐个交互拾取、滚轮/方向键切换选中项。
- **Pickup** — 新增掉落物过滤系统（仅扫描模式生效）：支持材质黑/白名单、物品名称正则、最小堆叠数量过滤。
- **Pickup** — 新增 `LootScannerService`：周期扫描任务、`EntityPickupItemEvent` 拦截、客户端交互包处理（`pick`/`scroll_up`/`scroll_down`）。
- **Pickup** — 新增 `LootFilterEngine`：评估物品是否应在面板中显示。
- **Pickup** — 新增 `loot_panel.yml` HUD UI 文件：暗色圆角面板 + 蓝色选中高亮 + 物品图标 + 名称数量 + F 键提示。
- **Pickup** — 配置文件 `ArcartXPickup.yml` 重构为三段式：`settings`（全局）+ `notification`（通知模式）+ `scanner`（扫描模式）+ `filter`（过滤规则）。
- **Pickup** — 扫描模式预留 Warehouse 仓库联动配置（`warehouse-auto-deposit`），拾取后可选直接存入仓库。
- **Pickup** — `PickupModule` 新增 `ClientPacketHandler`，处理客户端发来的拾取交互包。
- **Pickup** — `ValidationRule` 更新：移除旧的 `settings.auto-pickup-delay-ticks` / `settings.display-duration-ticks`，新增 `notification.max-visible`、`notification.entry-ttl-ms`、`scanner.scan-radius`、`scanner.scan-interval-ticks`、`scanner.max-display` 约束。
- **Pickup** — 过滤系统升级为五维过滤：在原有材质黑/白名单 + 名称正则 + 最小数量基础上，新增 **Lore 正则黑/白名单**（`lore-blacklist-regex` / `lore-whitelist-regex`）和 **NBT 键黑/白名单**（`nbt-blacklist-keys` / `nbt-whitelist-keys`，支持嵌套路径如 `custom.trash`）。

### 1.1.0-beta (Build 2026-05-24) — CombatEffect 连击追踪 + 死亡缓冲 + 冷却系统

- **CombatEffect** — 新增 `combo-tracker` 配置节和 `ComboTrackerService`：追踪玩家连续攻击计数，支持 Chronos 状态事件或 Bukkit 攻击事件双源，可配置超时重置、目标锁定模式（`per-target`）、服务器变量实时同步（`sync-variable`）。
- **CombatEffect** — 新增 `death-buffer` 配置节和 `DeathBufferService`：拦截致命伤害延迟真正死亡，期间应用 ArcartX Shader、第三人称视角、Chronos 强制状态，阻止其他插件自动复活。
- **CombatEffect** — `PacketDefinition` 新增 `cooldown` 字段（毫秒），基于包ID+玩家UUID 粒度的冷却系统，防止高频 `attack` 触发刷屏。
- **CombatEffect** — `PacketTrigger` 新增 `DEATH` 和 `COMBO` 枚举值；`PacketDefinition` 新增 `conditions.combo-min`、`conditions.combo-max`、`conditions.combo-repeat` 字段。
- **CombatEffect** — `CombatPacketContext.resolveMythicMobId` 重写为静态反射缓存 + 失败标记，避免高频事件中重复反射开销。
- **CombatEffect** — 新增 3 个内置 UI 文件：`击杀命中特效.yml`（HUD）、`连击特效.yml`（HUD）、`死亡缓冲界面.yml`（全屏界面），首次启动自动导出到 `plugins/ArcartX/ui/`。
- **CombatEffect** — 新增 `CombatEffectTriggerable` capability 接口（`triggerPacket` + `triggerDirect`），供 EventPacket 等模块跨模块触发战斗特效。
- **CombatEffect** — 实现 `ModuleCommandHandler`，新增 `/axs combateffect send <包ID> <玩家> [k=v]` 和 `/axs combateffect direct <uiId> <handler> <玩家> [k=v]` 命令，支持 Tab 补全。
- **CombatEffect** — `PacketTrigger` 新增 `MANUAL` 类型，表示仅通过命令/API 触发，不被任何事件自动触发。
- **CombatEffect** — `PacketTrigger` 新增 `KEYBIND`、`STATE`、`CONTROLLER` 三种触发类型，联动 ArcartX 按键事件和 Chronos 状态/控制器事件。
- **CombatEffect** — 新增 `KeybindTriggerService`：反射监听 ArcartX 5 种按键事件（ClientKey Press/Release、SimpleKey Press/Release、KeyGroup），匹配 `trigger: keybind` 包定义。
- **CombatEffect** — 新增 `StateTriggerService`：反射监听 Chronos `PlayerEnterStateEvent`/`PlayerLeaveStateEvent`/`PlayerControllerChangeEvent`，匹配 `trigger: state` 和 `trigger: controller` 包定义。
- **CombatEffect** — `PacketDefinition` 新增 `conditions.key-name`/`key-action`/`key-type`（keybind 条件）和 `conditions.state-id`/`state-action`/`controller-id`（state/controller 条件），全部支持 `*` 通配符。
- **CombatEffect** — 配置新增 `keybind-trigger.enabled` 和 `state-trigger.enabled` 开关。
- **CombatEffect** — wiki 文档全面更新，覆盖八种触发器的配置详解、包定义字段、UI 对应关系、命令、Capability API 和性能优化说明。

### 1.1.0-beta (Build 2026-05-23c) — 卡片固定宽度 + 文本换行 + 字号由模板决定

- **Chat** — 卡片尺寸体系重构：宽度改为配置固定值（`cards.card-width`，默认 500），高度单行等于 `cards.card-height`（默认 100），多行消息时动态增长。移除 `cards.font-size` 配置，字号由各卡片模板 YAML 自行硬编码。
- **Chat** — 新增服务端文本换行：超出卡片可用文字区域（`cardWidth - 160 - 20`）的消息自动按字符宽度断行，保留颜色代码连续性，以换行列表形式发送到客户端。
- **Chat** — Payload 变更：`fontSize` 移除，新增 `cardHeight`（所有卡片）；`message` 字段可能包含 `\n` 换行符。
- **Mail** — 邮件通知卡片同步改造：`ui.notify-card-font-size` 移除，新增 `ui.notify-card-width`（默认 500）和 `ui.notify-card-height`（默认 100）；卡片模板 fontSize 硬编码为 49。
- **Mail** — 移除 `MailService` 中不再使用的 `estimateCardWidth`/`estimateTextWidth`/`isFullWidth` 方法。

### 1.1.0-beta (Build 2026-05-23b) — 聊天卡片动态尺寸 + 冗余消息抑制

- **Chat** — 所有聊天卡片（提及、私聊、系统、物品预览）宽度改为服务端动态估算，根据实际文本内容计算 `cardWidth`，字体大小通过 `fontSize` 传入客户端，新增 `cards.font-size` 配置项（默认 49）。
- **Chat** — 冗余消息抑制：禁言/过滤系统卡片成功发送后，不再重复输出红色文字提示；@提及卡片发送后，被提及玩家不再收到原始聊天消息行；物品预览卡片发送后抑制原始消息行。新增 `ChatOperationResult.cardNotified` 标志。
- **Chat** — 私聊卡片新增点击回复：接收方视角点击卡片背景自动执行 `/reply`；图标从信封改为铅笔符号（`§d✎`）。
- **Chat** — 系统卡片宽度修复：禁言卡片统一基于实际显示的详情行（`剩余时间: xxx`）估算宽度，解决禁言指令触发与玩家发言触发时卡片长短不一致的问题。
- **Chat** — 物品预览卡片背景改为 `Texture` 类型（与其他卡片一致），保留全卡片区域透明 Slot 叠加层用于物品 Tooltip 悬浮；payload 中 `cardWidth`/`fontSize` 优先排列，确保大体积 `itemJson` 不影响关键变量传输。
- **Mail** — 邮件通知卡片同步支持动态尺寸，新增 `ui.notify-card-font-size` 配置项（默认 49）。

### 1.1.0-beta (Build 2026-05-23) — Chat @补全 + 聊天卡片模板 + Mail 通知卡片

- **Chat** — 新增 `@` 名称聊天补全（双通道）：① 原版聊天栏：Paper/Purpur API 优先，不可用时回退 NMS `ClientboundCustomChatCompletionsPacket`（Spigot/Mohist 1.19.1+）；② ArcartX 自定义聊天栏：注册 `axs_chat_completion` overlay UI，匹配 `ChatScreen`，服务端 join/quit 时推送在线玩家列表，客户端 100ms 轮询聊天输入并显示 `@` 候选下拉菜单（最多 8 项），点击即插入。
- **Chat** — 新增三套内置聊天卡片模板（提及、私聊、系统提示），统一 500×100 尺寸，首次启动自动导出到 `plugins/ArcartX/chat_card/`。
- **Chat** — 默认配置 `mention-card-id`、`private-card-id`、`system-card-id` 改为非空，默认启用卡片通知。
- **Mail** — 新增 `ui.notify-card-id` 配置，当玩家在线时收到新邮件，自动发送 ArcartX 聊天卡片通知（含邮件主题和发件人）。
- **Mail** — 内置 `axs_mail_notify.yml` 卡片模板，首次启动自动导出。

### 1.1.0-beta (Build 2026-05-22) — Chat 模块 Bug 修复 + 文档重写

- **Chat Bug 修复** — `ChatAdminCommand.handleSpy` 改用管理员专用重载 `setSocialSpy(String, boolean, String)`，不再检查目标玩家权限，并支持离线玩家。
- **Chat Bug 修复** — `ProxyChatTransport.onPluginMessageReceived` 修正 BungeeCord Forward 接收协议解析：第一个 `readUTF()` 读取 `messengerChannel` 而非 `"Forward"`，修复 Proxy 跨服完全无法接收的问题。
- **Chat Bug 修复** — `ChatService.onPlayerQuit` 新增 `states`/`replyTargets`/`lastMessageTimes`/`lastDuplicateStamps` 缓存清理，`runCleanup` 新增离线 `profiles` 清理，修复长期运行的内存泄漏。
- **Chat Bug 修复** — `Private.yml` 权限统一为小写 `arcartxsuite.chat.msg`，与代码常量一致。
- **Chat Bug 修复** — `RedisChatTransport` 使用完整 `JedisPool` 构造函数传入密码、database 和超时，修复密码保护 Redis 连接池验证失败的问题。
- **Chat 文档重写** — 修正配置示例中频道结构错误（频道为独立文件非嵌套配置）、变量名错误（`{player}` → `{player_name}`）；补充完整配置说明、四种频道模式、权限列表、聊天卡片数据载荷；新增跨服聊天设置详细教程（Redis + Proxy 两种方式）。

### 1.1.0-beta (Build 2026-05-22) — QuestGPS 移除自动行走

- **QuestGPS** — 移除自动行走功能（`auto-walk` 配置段、`QuestGpsAutoWalkService`、`/questgps autowalk|stopwalk` 命令、菜单"开始导航"按钮、HUD tick 按键模拟）。仅保留路径标记导航。
- **QuestGPS** — 配置 `NavigationDefaults` record 不再包含 `AutoWalkDefaults autoWalk` 字段。
- **QuestGPS** — 玩家命令精简为 `/questgps open|cleartrack`。

### 1.1.0-beta (Build 2026-05-22) — 多 UI 文档补齐 + QuestGPS 路径寻路

- **文档** — 新增「多 UI 同时发包」专题文档（`/guide/multi-ui`），统一说明 `ui-id` 字段的列表格式配置方式。
- **文档** — Announcer、Conversation、EntityTracker、QuestGPS、OnlineRewards 模块 wiki 配置段补充多 UI 交叉引用。
- **配置注释** — EntityTracker、Announcer、Conversation、QuestGPS、OnlineRewards 的默认 yml 配置文件补充多 UI 列表格式示例注释。

### 1.1.0-beta (Build 2026-05-22) — QuestGPS 路径寻路 + 标记修复

- **QuestGPS** — 新增 A* 路径寻路系统：从玩家到目标沿地面生成多个导航标记实体，智能绕开障碍物、液体和危险方块（仙人掌、岩浆块、营火等），支持 ±1 格台阶。
- **QuestGPS** — 路径标记动态更新：玩家移动时异步重新计算路径，现有实体通过传送复用，减少创建/销毁开销。
- **QuestGPS** — 路径标记实体朝向：每个标记面向下一个路径点方向，最后一个标记面向目标终点。
- **QuestGPS** — 新增 `marker` 配置字段：`path-interval`（标记间距）、`path-max-markers`（最大数量）、`path-update-ticks`（更新频率）、`path-max-distance`（最大渲染距离）、`path-max-iterations`（A* 最大迭代次数）。
- **AdyeshachNpcBridge** — 修复实体类型：`findCreateMethod` 中枚举重建从 `PLAYER` 改为 `ARMOR_STAND`，解决实体显示名称标签和碰撞体积的问题。
- **AdyeshachNpcBridge** — 新增实体初始化：隐藏名称标签（`setCustomName("")` + `setNameTagVisible(false)`）、关闭碰撞（`isMarker=true` + `setCollidable(false)`）、禁用重力（`isNoGravity=true`）。
- **AdyeshachNpcBridge** — 新增 `teleportMarker` 方法，支持传送已有私有标记实体到新位置。
- **日志** — QuestGPS 导航标记和 AdyeshachNpcBridge 的调试日志全部改为受 `debug.enabled` 配置控制，默认关闭。

### 1.1.0-beta (Build 2026-05-20) — QuestGPS 导航系统重构

- **QuestGPS** — 移除服务端粒子导航（`QuestGpsParticleService`），替换为零开销的客户端渲染方案。
- **QuestGPS** — 新增 3D 模型导航标记：通过 Adyeshach 私有临时实体 + ArcartX `setModel()` 在导航目标位置渲染自定义模型（如光柱/箭头/水晶），仅追踪玩家可见，追踪期间零服务端 tick。
- **QuestGPS** — 新增任务指引 HUD（`AXS:questgps_guide`）：RPG 风格左侧竖排浮窗，显示任务名、进度条、目标清单（≤3 条）和导航坐标，追踪时自动打开。
- **QuestGPS** — 移除旧 `hud-ui-id` / `auto-open-hud-on-track` / `hud-enabled-by-default` 配置项。
- **QuestGPS** — 配置变更：`navigation.particle` → `navigation.marker`（`model-id`、`scale`、`default-state`、`animation`、`y-offset`）。
- **QuestGPS** — 新增 `client.guide-ui-id` 配置项。
- **QuestGPS** — 移除 `/questgps hud` 子命令，指引 HUD 随追踪自动开关。
- **AdyeshachNpcBridge** — 扩展：新增 `spawnPrivateMarker` / `removePrivateMarker` / `clearAllPrivateMarkers` / `getPrivateMarker` 方法，支持按玩家生成私有临时实体。

### 1.1.0-beta (Build 2026-05-19) — 配置目录拆分

- **配置拆分** — 以下模块的大型内联配置段已拆分为独立目录，每个 `*.yml` 文件可包含多条定义：
  - `announcer`: `entries:` → `entries-directory: "entries"`（`entries/*.yml`）
  - `combateffect`: `packets:` → `packets-directory: "packets"`（`packets/*.yml`）
  - `title`: `titles:` → `titles-directory: "titles"`（`titles/*.yml`，按组分文件）
  - `rgb`: `entries:` → `entries-directory: "entries"`（`entries/*.yml`）
  - `map`: `anchors:` → `anchors-directory: "anchors"`（`anchors/*.yml`）
  - `questgps`: `quests:` → `quests-directory: "quests"`（`quests/*.yml`，按分类分文件）
  - `onlinerewards`: `sign-in:`/`rewards:` → `sign-in-file`/`rewards-file`（外部独立文件）
  - `tab`: `tabs:` → `tabs-directory: "tabs"`（`tabs/*.yml`）
  - `entitytracker`: `bosses:` → `bosses-directory: "bosses"`（`bosses/*.yml`）
  - `eventpacket`: `rules:` → `rules-directory: "rules"`（`rules/*.yml`）
- **破坏性变更** — 主配置中的旧内联段不再被读取。用户必须将已有数据迁移到对应目录文件中。首次启动时模块会自动导出默认示例文件。
- **文件组织** — 同一目录下每个 `*.yml` 文件可包含多个定义（根键即为 ID），不强制每条定义独立一个文件，方便按业务逻辑分组管理。

### 1.1.0-beta (Build 2026-05-18) — 热加载 / 目录归位 / 控制台美化

- **热加载** — `ModuleRegistry` 新增 `loadModuleById` / `unloadModule` 公开方法，配套 `/axs load|unload <模块名>` 子命令，Tab 补全自动区分已加载/未加载模块。
- **热卸载** — 卸载时会检查反向依赖（其他已启用模块在描述符里 `depends` 该模块），存在 dependents 则拒绝卸载并提示。卸载成功会执行 `onDisable` → 移除命令处理器 → 移除客户端 packet handler → 关闭 `URLClassLoader`，释放 jar 文件句柄。
- **目录归位** — `ModuleContext` 新增 `migrateLegacyDirectory(relativePath)` API。模块在 `onEnable` 时一次性把 1.0.x 时代散落在根目录的目录（如 `chat/channels`、`mail/presets`、`prop/props`、`subtitle/groups`、`combateffect` 配置、`shimmer-rgb-*` 临时目录等）整体搬迁到 `data/<moduleId>/<relativePath>/`，原位为空时不动。
- **目录归位** — `MailService` 新增 `baseDataDir` 字段；`PropModule`、`AnnouncerModule`、`ChatModule`、`CombatEffectModule` 改用 `context.dataFolder()` 拼接资源路径；`RgbModule` 清理 legacy shimmer 目录的日志带颜色高亮。
- **控制台美化** — `ArcartXSuitePlugin.STARTUP_BANNER` 改为 ANSI Shadow 字体绘制的「SUITE」六行块状字符画，主体青→蓝→紫渐变，顶部新增 `✦ A R C A R T X ✦` 副标题，底部居中作者署名。
- **控制台美化** — 迁移类 INFO 日志统一格式 `→ 已归位 X: <来源> ➜ <目标>`，使用金色箭头 + 黄色源 + 灰色 ➜ + 青色目标，便于在密集启动日志中一眼识别。

### 1.1.0-beta (Build 2026-05-18) — 配置智能体检

- **配置诊断** — 新增四层智能诊断：结构同步、类型修复、版本迁移、值验证。
- **配置诊断** — 17 个模块全部配置 `SyncPolicy` + `ValidationRule`，支持自动字段校验。
- **配置诊断** — 新增 `/arcartxsuite config diagnose|preview|apply|rollback|status` 命令。
- **配置诊断** — 模块配置文件物理迁移（旧位置 → `data/<module>/`）与内容诊断联动提示。
- **配置诊断** — 支持 `migrations/<from>-<to>.yml` 版本升级规则（rename/remove/move/set-if-missing/value-map）。
- **配置诊断** — 诊断报告生成到 `diagnosis/YYYY-MM-DD_HH-mm-ss/summary.md`。
- **API** — `axs-api` 新增 `SyncPolicy`、`ValidationRule`、`MigrationOperation`、`ModuleConfigSpec` 等类型。
- **文档** — 新增「配置智能体检」用户指南与架构文档。

---

## 升级注意事项

1. 备份 `plugins/ArcartXSuite/`，尤其是 `license.yml` 和 `security/local-salt.dat`。
2. 覆盖 jar 后重启服务端。
3. 执行 `/axs license status` 检查授权状态。
4. 执行 `/axs status` 检查模块状态。
5. 需要迁移授权到新服务器时，可执行 `/axs license rebind`；旧服务器不可用时，在新服务器执行 `/axs license cloud-code` 后使用云端网页换绑。
6. **配置目录拆分（1.1.0-beta Build 2026-05-19）**：旧主配置中的内联数据段（如 `entries:`、`tabs:`、`bosses:`、`rules:`、`anchors:`、`quests:`、`titles:`、`sign-in:`、`rewards:`）不再被读取。升级前请手动将对应段落内容复制到 `data/<module>/<目录>/` 下的 `*.yml` 文件中。首次启动会自动导出默认示例文件供参考。

