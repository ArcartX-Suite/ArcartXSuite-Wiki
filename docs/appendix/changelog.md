# 变更日志

当前 wiki 只保留正在发布的 beta 版本记录，旧版历史不再展示。

---

## 1.0.0-beta（当前）

- **授权** — 使用 QQ + 多授权码体系，支持单模块码和全模块 suite 码。
- **授权** — 付费模块绑定 `QQ + install_id + 机器指纹`，`security/local-salt.dat` 作为绑定身份的一部分。
- **授权** — `/AXS license status|refresh|activate|rebind|fingerprint` 提供授权状态、刷新、激活、换绑和机器指纹诊断。
- **架构** — 保留宿主 + 模块 Jar 架构，`axs-core` 提供核心能力，`modules/*` 可按需外置加载。
- **资源保护** — 付费模块资源通过 ticket 中的 `resourceKeys` 解包后在内存中解密。
- **文档** — 安装、授权、命令速查和安全架构文档已同步到 `1.0.0-beta`。

---

## 升级注意事项

1. 备份 `plugins/ArcartXSuite/`，尤其是 `license.yml` 和 `security/local-salt.dat`。
2. 覆盖 jar 后重启服务端。
3. 执行 `/AXS license status` 检查授权状态。
4. 执行 `/AXS status` 检查模块状态。
5. 需要迁移授权到新服务器时，确认后再执行 `/AXS license rebind`。
