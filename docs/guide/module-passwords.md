# 模块启用与授权

AXS 的 `config.yml` 只负责模块启用意图：

```yaml
modules:
  announcer:
    enabled: true
  warehouse:
    enabled: true
```

- 免费模块：`enabled: true` 后即可加载。
- 付费模块：`enabled: true` 后，还必须通过 `license.yml` 云端授权。

当前付费模块：`warehouse`、`map`、`mail`、`title`、`questgps`、`conversation`。

## license.yml

AXS v1.3 使用 **QQ + 授权码列表**。每个授权码属于一个 QQ，并且只能绑定一个服务器的 `install_id` 与机器指纹。

```yaml
license:
  qq: "1451759359"
  keys:
    - "AXS-SUITE-D58D6FAECBE18E94F15A"
  install_id: "auto"

  endpoints:
    - name: "fallback-workers"
      base_url: "https://arcartxsuite-license.arcartxsuite-license.workers.dev"
      priority: 10
      timeout_ms: 4500
    - name: "primary-cloudflare"
      base_url: "https://license.arcartxsuite.com"
      priority: 30
      timeout_ms: 3500
```

多个单模块授权码可以叠加：

```yaml
license:
  qq: "1451759359"
  keys:
    - "AXS-MOD-WAREHOUSE-XXXXXXXXXXXX"
    - "AXS-MOD-MAP-YYYYYYYYYYYY"
    - "AXS-MOD-MAIL-ZZZZZZZZZZZZ"
  install_id: "auto"
```

::: warning 不再使用旧字段
`license.key` 已废弃，不会被当作单个授权码读取。请始终使用 `license.keys` 列表。
:::

## 授权码类型

| 类型 | 示例前缀 | 解锁范围 |
| --- | --- | --- |
| 单模块码 | `AXS-MOD-WAREHOUSE-...` | 只解锁对应模块 |
| 全模块码 | `AXS-SUITE-...` | 解锁全部付费模块 |

同一个 QQ 可以拥有多个授权码。同一个授权码只能有一个 active 服务器绑定；如果第二台服务器也需要同一模块，请为该服务器单独发一个授权码。

## 首次绑定流程

1. 在后台为 QQ 发放单模块码或 suite 码。
2. 把 QQ 和授权码写入 `plugins/ArcartXSuite/license.yml`。
3. 保持 `install_id: "auto"`，首次启动时插件会生成 UUID 并写回配置。
4. 重启服务器，或在控制台执行：

```txt
/axs license activate
/axs license status
```

绑定成功后，Worker 会返回签名票据，插件会写入：

```txt
plugins/ArcartXSuite/security/license.cache
plugins/ArcartXSuite/security/secure-clock.dat
plugins/ArcartXSuite/security/local-salt.dat
```

::: danger 不要随意删除 security 目录
`local-salt.dat` 是服务器机器指纹的一部分。授权中心绑定的不只是 `install_id`，还包括 `local-salt.dat` 参与计算后的机器指纹。

如果删除 `plugins/ArcartXSuite/security/local-salt.dat`，插件会生成新的 salt，即使 `install_id` 没变，授权中心也会认为这是另一台服务器或旧机器指纹不匹配。
:::

## 绑定身份注意事项

一个服务器的授权身份由这些信息共同组成：

| 文件/字段 | 作用 | 是否建议备份 |
| --- | --- | --- |
| `license.yml` 的 `license.qq` | 授权码所属 QQ | 是 |
| `license.yml` 的 `license.keys` | 当前服务器使用的授权码列表 | 是 |
| `license.yml` 的 `install_id` | 当前服务器安装 ID | 是 |
| `security/local-salt.dat` | 本地随机 salt，参与机器指纹计算 | 是，尤其重要 |
| `security/license.cache` | 已签名的离线缓存票据 | 可备份，但不能单独跨服复用 |
| `security/secure-clock.dat` | 安全时间记录，防止系统时间回退 | 可备份 |

推荐备份整个目录：

```txt
plugins/ArcartXSuite/license.yml
plugins/ArcartXSuite/security/
```

迁移服务器时，如果你希望授权仍然视为同一台服务器，请同时迁移 `license.yml` 和 `security/local-salt.dat`。如果你是故意换到新服务器，应保留新的 `local-salt.dat`，然后执行 `/axs license rebind`。

## 授权命令

| 命令 | 用途 |
| --- | --- |
| `/axs license status` | 查看 QQ、授权状态、已解锁模块、授权入口、代理状态、缓存状态和每个授权码的结果 |
| `/axs license refresh` | 刷新当前服务器绑定，不消耗换绑次数 |
| `/axs license activate` | 首次绑定或重新激活当前服务器 |
| `/axs license rebind` | 显式换绑到当前服务器，会消耗该授权码的自助换绑次数/冷却 |
| `/axs license fingerprint` | 输出当前服务器机器指纹和 localSaltHash，用于后台核对 |

## 校验流程

1. 插件读取 `license.qq`、`license.keys`、`install_id`、`local-salt.dat` 与机器指纹。
2. `/v1/activate` 或 `/v1/verify` 发送 `qq + licenseKeys[] + installId + fingerprintHash`。
3. Worker 逐个校验授权码，合并有效授权码的模块 entitlement，并返回 Ed25519 签名票据。
4. 插件验签并校验 QQ、`subjectId`、`install_id`、机器指纹和有效期。
5. `ModuleRegistry` 在加载付费模块前检查最终 ticket 中是否包含该模块。
6. 付费模块 YAML/UI 资源需要合法票据里的 `resourceKeys` 才能在内存中解密。

## 常见状态

| 状态 | 含义 |
| --- | --- |
| `VALID` | 在线票据有效 |
| `GRACE` | 网络不可用，但缓存处于离线宽限期 |
| `EMERGENCY_GRACE` | 应急宽限期，只允许已解锁模块 |
| `AUTH_DENIED` | Worker 明确拒绝授权 |
| `NETWORK_ERROR` | Worker 不可达，已尝试使用本地缓存 |
| `NOT_CONFIGURED` | 未填写 `license.qq` 或 `license.keys` |

## 常见错误

| 错误 | 含义 | 处理方式 |
| --- | --- | --- |
| `MISSING_QQ` | `license.qq` 未填写 | 填写授权所属 QQ |
| `MISSING_LICENSE_KEYS` | `license.keys` 为空 | 写入至少一个授权码 |
| `QQ_MISMATCH` | 授权码不属于当前 QQ | 检查 QQ 或换成该 QQ 名下的码 |
| `LICENSE_CODE_NOT_FOUND` | 授权中心不存在该授权码 | 检查是否填错，或确认是否发到远程 D1 |
| `LICENSE_CODE_NOT_ACTIVE` | 授权码已停用 | 后台启用或重新发码 |
| `LICENSE_CODE_EXPIRED` | 授权码已过期 | 后台延长有效期或重新发码 |
| `BINDING_NOT_FOUND` | 当前授权码还没有绑定本服务器 | 执行 `/axs license activate` |
| `BOUND_TO_OTHER_INSTALL` | 授权码已绑定其他服务器或旧机器指纹。常见原因是删除/重建了 `security/local-salt.dat`，或把同一个授权码拿到另一台服务器使用 | 如果是迁移服务器，执行 `/axs license rebind`；如果是误删 salt，恢复旧 `local-salt.dat` 备份；管理员也可以删除旧 binding 后重新 `/axs license activate` |
| `REBIND_QUOTA_EXHAUSTED` | 自助换绑次数不足 | 后台补换绑次数或管理员删除绑定 |
| `REBIND_COOLDOWN_ACTIVE` | 换绑冷却中 | 等待冷却结束或后台重置冷却 |
| `NETWORK_ERROR` | 授权入口不可达 | 检查服务器网络、Cloudflare 可达性或临时代理配置 |
