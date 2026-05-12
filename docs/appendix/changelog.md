# 变更日志

按版本倒序记录对**外部接口契约**的破坏性变更与重要新增。

---

## 4.1.0（当前）

- **架构** — 引入 **宿主 + 模块 Jar** 架构（`axs-api` + `axs-core` + `modules/*`），详见 [模块化架构](/architecture/modular)
- **架构** — `ModuleRegistry` 预扫描外部模块 Jar，自动跳过内置加载，避免双重初始化
- **架构** — 20 个模块全部迁移为独立 Gradle 子项目（4 个独立实现 + 16 个委托实现）
- **架构** — 动态命令注册：模块实现 `ModuleCommandHandler` 即可自动注册 `/axs <moduleId>` 子命令
- **改进** — BossBar / EntityTracker 启动流程统一：`reloadBossBarState(boolean logSummary)` 与其他模块一致
- **移除** — 不再自动执行 `ax reload true`，ArcartX 已支持 UI 自动导入
- **移除** — Hybrid Bootstrap 延迟重试机制（`scheduleHybridBootstrap`）
- **移除** — Boss 首次检测触发 reload 机制（`armBossTriggeredReload` / `notifyTrackedBossDetected`）
- **改进** — `/AXS reload all` 和 `/AXS reload <模块名>` 自动感知外部模块，走 `moduleRegistry.reloadModule()` 路径
- **改进** — 所有 16 个委托模块的 `onDisable()` 正确调用 `shutdownXxxModule()` 清理资源
- **改进** — UI 注册流程审查通过：所有有 UI 的模块在 reload 时完整执行 shutdown → unregister → register → start

## 4.0.0

- **架构** — 模块整合为 17 个主模块，EntityTracker（实体追踪）、CombatEffect（战斗特效）、Announcer（播报系统）、EventPacket（事件引擎）功能扩展
- **破坏** — 管理命令和 `config.yml` 中的旧模块名已全部移除，不再向下兼容：`bossbar` → `entitytracker`、`killeffect` → `combateffect`、`subtitle` → `announcer`、`attacktarget` → `entitytracker`、`digisdisplay` → `combateffect`、`packetcommand` → `eventpacket`、`onlinereward` → `onlinerewards`
- **新增** — LoginView 登录界面模块（独立/AuthMe 兼容双模式）
- **文档** — 迁移至 VitePress，全新可视化文档站

## 3.3.8

- **架构** — 资源加密协议固定为 `AES/GCM/NoPadding` + GZIP + magic `AXR1` + 12-byte IV
- **架构** — `ClientPacketGuard` 引入 `mode: silent / notify / punish`
- **Mail** — `attachment-tax-rates.<currency>` 替代旧 `vault-tax-rate`
- **EventPacket** — 引入 `rules.<id>` 模型（trigger + actions 链）
- **Title** — 新增 `craneattribute.enabled` 桥
- **OnlineRewards** — ~~管理命令固定为 `onlinereward`（单数）~~ → 4.0.0 起统一为 `onlinerewards`（复数）
- **EntityTracker** — rewards actions 支持四类；`inventory-full` 策略

---

## 升级注意事项

1. **备份** `plugins/ArcartXSuite/`
2. 启动新版本，让 `YamlConfigSynchronizer` 合并新增字段
3. `/AXS status` 检查模块状态
4. `/AXS reload all` 确认无报错
