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

### 1.1.0-beta (Build 2026-05-18) — 热加载 / 目录归位 / 控制台美化

- **热加载** — `ModuleRegistry` 新增 `loadModuleById` / `unloadModule` 公开方法，配套 `/axs load|unload <模块名>` 子命令，Tab 补全自动区分已加载/未加载模块。
- **热卸载** — 卸载时会检查反向依赖（其他已启用模块在描述符里 `depends` 该模块），存在 dependents 则拒绝卸载并提示。卸载成功会执行 `onDisable` → 移除命令处理器 → 移除客户端 packet handler → 关闭 `URLClassLoader`，释放 jar 文件句柄。
- **目录归位** — `ModuleContext` 新增 `migrateLegacyDirectory(relativePath)` API。模块在 `onEnable` 时一次性把 1.0.x 时代散落在根目录的目录（如 `chat/channels`、`mail/presets`、`prop/props`、`subtitle/groups`、`combateffect` 配置、`shimmer-rgb-*` 临时目录等）整体搬迁到 `data/<moduleId>/<relativePath>/`，原位为空时不动。
- **目录归位** — `MailService` 新增 `baseDataDir` 字段；`PropModule`、`AnnouncerModule`、`ChatModule`、`CombatEffectModule` 改用 `context.dataFolder()` 拼接资源路径；`RgbModule` 清理 legacy shimmer 目录的日志带颜色高亮。
- **控制台美化** — `ArcartXSuitePlugin.STARTUP_BANNER` 改为 ANSI Shadow 字体绘制的「SUITE」六行块状字符画，主体青→蓝→紫渐变，顶部新增 `✦ A R C A R T X ✦` 副标题，底部居中作者署名。
- **控制台美化** — 迁移类 INFO 日志统一格式 `→ 已归位 X: <来源> ➜ <目标>`，使用金色箭头 + 黄色源 + 灰色 ➜ + 青色目标，便于在密集启动日志中一眼识别。

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

