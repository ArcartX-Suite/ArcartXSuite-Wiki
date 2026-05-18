# 变更日志

当前 wiki 只保留正在发布的 beta 版本记录，旧版历史不再展示。

---

## 1.0.2-beta（当前）

- **模块管理** — 新增 `/axs load <模块名>` 与 `/axs unload <模块名>` 子命令，支持运行时热加载新模块与热卸载已加载模块（释放 ClassLoader），不再需要重启服务端。卸载时会检查反向依赖，被其他模块依赖的模块会被拒绝卸载。
- **目录归位** — 模块产物统一收纳到 `plugins/ArcartXSuite/data/<moduleId>/`：配置文件 `config.yml`、SQLite 数据库、子目录（如 `chat/channels`、`mail/presets`、`prop/props`、`subtitle/groups`）等首次启动时一次性自动迁移，原 1.0.x 散落在根目录的旧路径全部归位。迁移日志带高亮色标。
- **控制台美化** — 启动 banner 改为 ANSI Shadow 字体的「SUITE」主标题 + 顶部 `✦ A R C A R T X ✦` 副标题，垂直青蓝紫渐变；迁移类日志统一格式 `→ 已归位 X: <来源> ➜ <目标>`，金/黄/灰/青多色标记。
- **授权** — 使用 QQ + 多授权码体系，支持单模块码和全模块 suite 码。
- **授权** — 付费模块绑定 `QQ + install_id + 机器指纹`，`security/local-salt.dat` 作为绑定身份的一部分。
- **授权** — `/axs license status|refresh|activate|rebind|cloud-code|fingerprint` 提供授权状态、刷新、激活、换绑、云端换绑挑战码和机器指纹诊断。
- **授权** — 云端网页换绑改为 QQ 授权账号登录 + 新服务器挑战码，不再接受仅凭 `QQ + 授权码` 直接换绑。
- **架构** — 保留宿主 + 模块 Jar 架构，`axs-core` 提供核心能力，`modules/*` 可按需外置加载。
- **资源保护** — 付费模块资源通过 ticket 中的 `resourceKeys` 解包后在内存中解密。
- **文档** — 安装、授权、命令速查和安全架构文档已同步到 `1.0.2-beta`。

### 1.0.2-beta (Build 2026-05-18) — 热加载 / 目录归位 / 控制台美化

- **热加载** — `ModuleRegistry` 新增 `loadModuleById` / `unloadModule` 公开方法，配套 `/axs load|unload <模块名>` 子命令，Tab 补全自动区分已加载/未加载模块。
- **热卸载** — 卸载时会检查反向依赖（其他已启用模块在描述符里 `depends` 该模块），存在 dependents 则拒绝卸载并提示。卸载成功会执行 `onDisable` → 移除命令处理器 → 移除客户端 packet handler → 关闭 `URLClassLoader`，释放 jar 文件句柄。
- **目录归位** — `ModuleContext` 新增 `migrateLegacyDirectory(relativePath)` API。模块在 `onEnable` 时一次性把 1.0.x 时代散落在根目录的目录（如 `chat/channels`、`mail/presets`、`prop/props`、`subtitle/groups`、`combateffect` 配置、`shimmer-rgb-*` 临时目录等）整体搬迁到 `data/<moduleId>/<relativePath>/`，原位为空时不动。
- **目录归位** — `MailService` 新增 `baseDataDir` 字段；`PropModule`、`AnnouncerModule`、`ChatModule`、`CombatEffectModule` 改用 `context.dataFolder()` 拼接资源路径；`RgbModule` 清理 legacy shimmer 目录的日志带颜色高亮。
- **控制台美化** — `ArcartXSuitePlugin.STARTUP_BANNER` 改为 ANSI Shadow 字体绘制的「SUITE」六行块状字符画，主体青→蓝→紫渐变，顶部新增 `✦ A R C A R T X ✦` 副标题，底部居中作者署名。
- **控制台美化** — 迁移类 INFO 日志统一格式 `→ 已归位 X: <来源> ➜ <目标>`，使用金色箭头 + 黄色源 + 灰色 ➜ + 青色目标，便于在密集启动日志中一眼识别。

### 1.0.2-beta (Build 2026-05-19) — 配置目录拆分

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

### 1.0.2-beta (Build 2026-05-18) — 配置智能体检

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
6. **配置目录拆分（1.0.2-beta Build 2026-05-19）**：旧主配置中的内联数据段（如 `entries:`、`tabs:`、`bosses:`、`rules:`、`anchors:`、`quests:`、`titles:`、`sign-in:`、`rewards:`）不再被读取。升级前请手动将对应段落内容复制到 `data/<module>/<目录>/` 下的 `*.yml` 文件中。首次启动会自动导出默认示例文件供参考。

