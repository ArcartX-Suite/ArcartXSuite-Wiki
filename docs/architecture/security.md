# 客户端包守卫 + 授权门控

AXS 的安全由两部分组成：

- **`ClientPacketGuard`**：防止伪造 / 高频回包 DoS。
- **`LicenseService`**：对付费模块执行 Worker 票据验签、离线缓存、机器指纹和资源密钥门控。

## ClientPacketGuard

每条客户端→服务端的包被抽象成 `(player, module, action)` 三元组。

```yaml
client-packet-guard:
  enabled: true
  cleanup-interval-ticks: 200
  defaults:
    window-ms: 1000
    max-hits: 20
    mode: "silent"
```

| 字段 | 说明 |
| --- | --- |
| `window-ms` | 时间窗（毫秒） |
| `max-hits` | 时间窗内最多允许的回包数 |
| `mode` | `silent` 静默丢弃 / `notify` 丢弃并提示 / `punish` 执行命令 |

## LicenseService

付费模块加载前，`ModuleRegistry` 会检查当前授权决策：

- `VALID` / `GRACE` / `EMERGENCY_GRACE` 且 ticket 包含该模块：允许加载。
- `AUTH_DENIED` / `DISABLED` / `NETWORK_ERROR` / `NOT_CONFIGURED`：跳过付费模块。

授权身份由 `license.yml` 中的 `license.qq` 与 `license.keys` 组成。插件端不会再读取旧的 `license.key` 字段。

```yaml
license:
  qq: "1451759359"
  keys:
    - "AXS-SUITE-D58D6FAECBE18E94F15A"
  install_id: "auto"
```

Worker 票据会绑定：

| 字段 | 作用 |
| --- | --- |
| `ownerQq` | 授权码所属 QQ，必须与 `license.qq` 一致 |
| `subjectId` | `qq:<QQ>:install:<install_id>`，用于绑定当前服务器主体 |
| `installId` | 当前服务器安装 ID |
| `fingerprintHash` | 当前服务器机器指纹 |
| `modules` | 多个授权码合并后的最终模块 entitlement |
| `keyResults` | 每个授权码的成功/失败原因，用于 `/axs license status` 诊断 |
| `resourceKeys` | 已授权模块的资源密钥包装结果 |

授权缓存位于：

```txt
plugins/ArcartXSuite/security/license.cache
plugins/ArcartXSuite/security/secure-clock.dat
plugins/ArcartXSuite/security/local-salt.dat
```

`/axs license refresh` 只刷新当前绑定，不消耗换绑次数。`/axs license rebind` 是显式换绑操作，会请求 Worker 的 `/v1/rebind`，成功后旧绑定失效，新服务器成为 active binding。

付费模块资源走 `arcartx/internal/license/<module>/*.axl`，需要 ticket 中的 `resourceKeys` 解包后才能在内存中解密。资源密钥包装使用 `subjectId + installId + fingerprintHash + moduleId + localSaltHash` 派生本地解包密钥，因此复制 `license.cache` 到另一台服务器不会解锁资源。

`local-salt.dat` 是绑定身份的一部分，不能把它看成普通临时缓存。删除它会让插件生成新的 local salt，继而改变 `localSaltHash` 和最终 `fingerprintHash`。即使 `license.yml` 里的 `install_id` 没变，Worker 也会把新的指纹视为另一个安装环境，并返回“授权码已绑定到其他服务器或旧机器指纹”。

网络不可达时，插件会进入本地 `license.cache` 校验。在线票据验签仍严格要求完整 `fingerprintHash` 匹配；离线缓存额外允许 VPN、虚拟网卡或网卡顺序变化导致的 `fingerprintHash` 变化，只要 `QQ + install_id + local-salt.dat + ticket 签名 + 有效期` 仍然匹配，就可以继续使用缓存中的付费模块和资源密钥。

排查这类问题时按顺序确认：

1. `license.yml` 的 `qq` 和 `keys` 是否属于同一个 QQ。
2. `license.yml` 的 `install_id` 是否与后台 binding 一致。
3. `/axs license fingerprint` 输出的 `localSaltHash` 是否与后台 binding 中的旧记录一致。
4. 如果只有 `localSaltHash` 不一致，优先恢复旧 `security/local-salt.dat`；确实要迁移时再执行 `/axs license rebind`。
