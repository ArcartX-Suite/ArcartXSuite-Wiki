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
- 福利模块：消费额度达标或单独购买后获得授权码，与付费模块共用授权流程。

当前付费模块：`warehouse`、`map`、`mail`、`title`、`questgps`、`conversation`、`market`、`qqbot`。

当前福利模块：`tab`、`entitytracker`。

## license.yml

AXS v1.3 使用 **QQ + 授权码列表**。每个授权码属于一个 QQ，并且只能绑定一个服务器的 `install_id` 与机器指纹。

```yaml
license:
  qq: "1451759359"
  keys:
    - "AXS-TYPE-ABCD1234ABCD1234ABCD"
  install_id: "auto"

  endpoints:
    - name: "official-domain"
      base_url: "https://axs.021209.xyz"
      priority: 10
      timeout_ms: 4500
    - name: "cloudflare-workers"
      base_url: "https://arcartxsuite-license.arcartxsuite-license.workers.dev"
      priority: 20
      timeout_ms: 4500
```

`official-domain` 是当前建议优先使用的正式授权入口；`cloudflare-workers` 是 Cloudflare Workers 原始兜底入口。插件会按 `priority` 从小到大尝试，正式域名不可达时会自动切到兜底入口。

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

## 授权码类型

| 类型 | 示例前缀 | 解锁范围 |
| --- | --- | --- |
| 单模块码 | `AXS-MOD-WAREHOUSE-...` | 只解锁对应模块 |
| 全模块码 | `AXS-SUITE-...` | 解锁全部付费模块 |

同一个 QQ 可以拥有多个授权码。同一个授权码只能有一个 active 服务器绑定；如果第二台服务器也需要同一模块，请为该服务器单独发一个授权码。

## 授权激活完整流程

授权激活分为“后台发码”和“服务器激活”两部分。后台负责把授权码归属到 QQ；服务器负责把授权码绑定到当前服务器身份。

### 后台发码

管理员发放

```txt
code=AXS-SUITE-xxxxxxxxxxxxxxxxxxxx
```

`code=` 是给服务器填写的授权码明文，只显示一次。

如果需要使用云端网页换绑，还需要生成授权账号设置密码链接：

设置 QQ 授权账号密码。服务器激活本身不需要账号登录；账号登录只用于云端网页换绑和后续自助管理。

### 服务器填写 license.yml

打开服务器配置：

```txt
plugins/ArcartXSuite/license.yml
```

填写 QQ 和授权码：

```yaml
license:
  qq: "1451759359"
  keys:
    - "AXS-SUITE-xxxxxxxxxxxxxxxxxxxx"
  install_id: "auto"
```

多个单模块授权码可以写多行。`install_id: "auto"` 建议保留，插件首次启动时会生成 UUID 并写回配置。

### 执行激活

重启服务器，或在控制台执行：

```txt
/axs license activate
/axs license status
```

激活时插件会：

1. 读取 `license.qq`、`license.keys`、`install_id`。
2. 读取或生成 `security/local-salt.dat`。
3. 计算当前机器指纹 `fingerprintHash`。
4. 请求 Worker 的 `/v1/activate`。
5. Worker 校验 QQ、授权码、模块类型、过期时间和当前绑定。
6. 首次激活时，Worker 创建 `license_bindings` 绑定记录。
7. Worker 返回 Ed25519 签名 ticket 和付费资源解密 key。
8. 插件验签 ticket，并写入本地缓存。

绑定成功后，插件会写入：

```txt
plugins/ArcartXSuite/security/license.cache
plugins/ArcartXSuite/security/secure-clock.dat
plugins/ArcartXSuite/security/local-salt.dat
```

`/axs license status` 中看到 `VALID`，并且模块列表包含需要的付费模块，就表示授权已经生效。

### 刷新与重新激活

日常刷新授权使用：

```txt
/axs license refresh
```

`refresh` 只验证当前绑定，不会消耗换绑次数。

如果授权码还没有绑定当前服务器，或你刚清空过本地缓存，可以执行：

```txt
/axs license activate
```

如果授权码已经绑定其他服务器，普通 `activate` 会返回 `BOUND_TO_OTHER_INSTALL`。确认迁移时再使用：

```txt
/axs license rebind
```

或走云端网页换绑流程。

## 离线缓存

如果授权入口因为 Cloudflare、DNS、VPS 网络或代理问题不可达，插件会自动尝试读取：

```txt
plugins/ArcartXSuite/security/license.cache
```

离线缓存能生效需要同时满足：

1. `license.qq` 与缓存票据里的 QQ 一致。
2. `license.yml` 的 `install_id` 与缓存票据一致。
3. `security/local-salt.dat` 与缓存票据一致。
4. ticket 签名有效，且没有超过 `offlineGraceUntil` / `hardExpireAt`。

从 `1.0.2-beta` 后，离线缓存对 VPN、虚拟网卡、网卡顺序变化造成的 `fingerprintHash` 变化会做兼容：只要 `local-salt.dat` 仍然一致，网络不可达时仍可使用本地缓存。在线激活和在线刷新仍然严格校验当前完整机器指纹。

如果日志只显示网络超时，可以执行：

```txt
/axs license status
```

看 `使用缓存` 和 `原因`。如果缓存不可用，原因里会显示类似：

```txt
本地缓存不可用: 本地缓存无效: installId 不匹配
本地缓存不可用: 本地缓存无效: QQ 不匹配
本地缓存不可用: 本地缓存无效: ticket 已硬过期
本地缓存不可用: 无 license.cache
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

迁移服务器时，如果你希望授权仍然视为同一台服务器，请同时迁移 `license.yml` 和 `security/local-salt.dat`。如果你是故意换到新服务器，应保留新的 `local-salt.dat`，然后执行 `/axs license rebind`，或使用云端网页换绑。

## 换绑方式

AXS 支持两种换绑方式，次数相互独立：

| 方式 | 适用场景 | 次数规则 |
| --- | --- | --- |
| 服务器内换绑 `/axs license rebind` | 已经能进入新服务器控制台 | 消耗授权码的服务器内自助换绑次数 |
| 云端网页换绑 | 旧服务器不可用、无法进入控制台或需要先解绑旧机器 | 每个授权码每月免费 4 次，不消耗服务器内换绑次数 |

云端换绑地址：

```txt
https://axs.021209.xyz/rebind
```

兜底地址：

```txt
https://arcartxsuite-license.arcartxsuite-license.workers.dev/rebind
```

云端换绑前，先在新目标服务器执行：

```txt
/axs license cloud-code
```

复制输出里的 `challengeCode` 到网页。网页换绑需要先用 QQ 授权账号登录，选择该 QQ 名下的授权码，再输入挑战码。旧服务器不需要在线，也不需要旧服务器确认；挑战码由新服务器生成，10 分钟内有效且只能使用一次。

网页换绑成功后，回到目标服务器执行：

```txt
/axs license activate
```

或重启服务器。

## 授权命令

| 命令 | 用途 |
| --- | --- |
| `/axs license status` | 查看 QQ、授权状态、已解锁模块、授权入口、代理状态、缓存状态和每个授权码的结果 |
| `/axs license refresh` | 刷新当前服务器绑定，不消耗换绑次数 |
| `/axs license activate` | 首次绑定或重新激活当前服务器 |
| `/axs license rebind` | 显式换绑到当前服务器，会消耗该授权码的自助换绑次数/冷却 |
| `/axs license cloud-code` | 生成云端网页换绑挑战码，用于证明你控制新目标服务器 |
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
| `CLOUD_REBIND_MONTHLY_LIMIT_EXHAUSTED` | 云端网页换绑本月免费次数已用完 | 等到下个月，或联系管理员处理绑定 |
| `NETWORK_ERROR` | 授权入口不可达 | 检查服务器是否能访问 `axs.021209.xyz`，再检查 Cloudflare Workers 兜底入口或临时代理配置 |

## VPS 网络与代理排查

如果日志里出现：

```txt
proxy=system:none 网络错误: UnknownHostException: arcartxsuite-license.arcartxsuite-license.workers.dev
```

含义是：插件尝试读取系统代理，但当前 VPS 没有可用系统代理，且 VPS 的 DNS 解析不到授权域名。这不是授权码问题。

先在 VPS 上测试：

```bash
nslookup axs.021209.xyz
curl -I https://axs.021209.xyz/v1/time
nslookup arcartxsuite-license.arcartxsuite-license.workers.dev
curl -I https://arcartxsuite-license.arcartxsuite-license.workers.dev/v1/time
```

Windows VPS 可用：

```powershell
nslookup axs.021209.xyz
curl.exe -I https://axs.021209.xyz/v1/time
nslookup arcartxsuite-license.arcartxsuite-license.workers.dev
curl.exe -I https://arcartxsuite-license.arcartxsuite-license.workers.dev/v1/time
```

如果 DNS 失败，先修 VPS 的 DNS，例如改为 `1.1.1.1`、`8.8.8.8` 或服务商推荐 DNS。

如果 VPS 必须走代理，`127.0.0.1` 只代表 VPS 自己。代理必须运行在 VPS 本机，或者填写 VPS 能访问到的代理服务器地址：

```yaml
license:
  network:
    proxy:
      enabled: true
      use_system: false
      type: HTTP
      host: 代理服务器IP或域名
      port: 7897
```

如果使用 Linux systemd 启动服务端，也可以给服务进程设置环境变量，但必须在启动 Minecraft 的同一个进程环境里生效：

```ini
Environment="HTTPS_PROXY=http://代理服务器IP:7897"
Environment="HTTP_PROXY=http://代理服务器IP:7897"
```

`/axs license status` 里看到 `proxy=HTTP /代理IP:端口` 才表示插件真正走到了代理；`proxy=system:none` 表示没有解析到可用代理。
