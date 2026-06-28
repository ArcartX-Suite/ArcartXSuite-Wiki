---
title: 云端模块 | ArcartX-Suite Minecraft插件文档
description: 通过 ArcartX-Suite 云端平台自动下载并加载加密模块。
---

# 云端模块

::: info 文档导航
本文档对应顶栏 **「云端授权」** 专区。服主日常配置见下文；模块开关见 [模块启用](/guide/module-enablement)；本地 jar 安装见 [安装](/guide/installation)。
:::

ArcartX-Suite 云端平台提供**加密模块分发**功能。

## 前提条件

1. **已注册 ArcartX-Suite 云端账号** — 在 [ArcartX-Suite 云端平台](https://cloud.021209.xyz) 用你的 QQ 注册服主账号。
2. **已获得模块授权** — 免费模块自动发放；付费模块需购买授权（联系服主群或管理员）。
3. **服务器已绑定云端** — 首次使用需要把服务器和云端账号绑定。

## 服务器端配置

编辑 `plugins/ArcartX-Suite/config.yml`，在最外层添加 `cloud` 节：

```yaml
cloud:
  qq: "你的QQ号"
  apiKey: "验证密匙"
  server-code: ""   # 留空即可，首次绑定成功后由云端自动回填，请勿手动修改
  debug: false
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `qq` | ✅ | 注册云端平台时用的 QQ 号 |
| `apiKey` | ✅ | 从云端后台「设置 → 验证密匙」复制的 API 密钥 |
| `server-code` | ❌ | 服务器码，绑定成功后自动写回；昵称在云端「我的服务器」页面编辑 |
| `debug` | ❌ | 调试日志，生产环境保持 `false` |

::: warning 配置安全
`apiKey` 仅用于服务器与云端通信，无法登录管理后台，与登录密码完全分离。但 `config.yml` 仍以明文存储，建议：
1. 设置 `plugins/ArcartX-Suite/` 目录权限为仅服务器进程可读。
2. 不要在公共仓库或聊天群中分享此配置文件。
3. 如怀疑泄露，登录云端后台重新生成密匙。
:::

## 云端平台服主使用流程

登录 [cloud.021209.xyz](https://cloud.021209.xyz) 后，按下面顺序操作：

### 1. 仪表盘

首页仪表盘展示：
- 当前账号下绑定的服务器数量
- 已装备的模块总数
- 各服务器的在线状态（通过心跳判断）
- 最近的操作日志

### 2. 我的服务器

左侧菜单「我的服务器」可查看所有绑定到此 QQ 的服务器：
- 服务器码（`serverCode`）
- 服务器昵称（可编辑）
- 已装备的模块列表
- 最近心跳时间

::: tip 多服管理
一个 QQ 可以绑定**多个服务器**，每个服务器有独立的 `serverCode`。装备模块时需要分别给每个服务器勾选。
:::

### 3. 装备模块

「装备模块」页面列出你已获得授权的全部模块：
1. 左侧选择要操作的服务器
2. 右侧勾选要装备（启用）或取消装备的模块
3. 点击「保存」

保存后执行 **`/axs sync`** 立即同步装备列表，或**重启服务器**（或等待自动令牌刷新），服务器会重新拉取模块列表并下载新装备模块。

::: info 免费模块自动装备
免费模块在服务器首次绑定时已自动装备。若后续取消装备，可回到此页面重新勾选。
:::

### 4. 下载 ArcartX-Suite

「下载 ArcartX-Suite」页面提供：
- 最新 bootstrap jar 下载（用于首次安装）
- 核心本体 `.axb` 的 manifest 信息
- 各版本 checksum 校验值

## 首次绑定流程

配置好 `cloud.qq` 和 `cloud.apiKey` 后，**重启服务器**即可：

```
[ArcartX-Suite-Cloud] 正在向云端绑定服务器...
[ArcartX-Suite-Cloud] 服务器绑定成功，服务器码: XXXXXXXX
[ArcartX-Suite-Cloud] 模块令牌已刷新，授权模块: N
[ArcartX-Suite-Cloud] 模块 xxx 已加载
```

绑定成功后，服务器会：
1. 向云端申请唯一服务器码（如 `XXXXXXXX`）
2. 获取当前已授权的模块列表
3. 逐个下载 `.axb` 加密模块并自动解密加载

::: tip 自动续期
模块令牌 24 小时过期。服务器会**自动刷新**，无需手动操作。令牌过期期间不影响已加载的模块运行，但新模块无法下载。
:::

## 模块更新

云端管理员上传新版本后，服务器在**重启**或**令牌刷新周期**（约 24 小时）内会自动下载最新版本。无需手动替换 jar。

如果你想立即更新，可以：
- **`/axs sync`** — 从云端同步授权/装备列表（新装备或取消装备的模块会立即加载/卸载）
- **`/axs update <模块ID>`** — 拉取指定云端模块的最新 `.axb` 并热重载
- **`/axs update all`** — 更新所有已加载的云端模块
- 或直接重启服务器

详见 [命令速查 → 云端模块管理](/guide/commands#云端模块管理)。

## 常见问题

### 绑定失败：USER_NOT_FOUND / INVALID_CREDENTIALS

- 检查 `cloud.qq` 和 `cloud.apiKey` 是否与云端平台「设置 → 验证密匙」中复制的一致
- `apiKey` 区分大小写，且与登录密码不同

### 未装备任何云端模块

- 授权不等于装备。登录云端平台确认模块已勾选到服务器上
- 免费模块在首次绑定时自动装备，但后续取消后不会自动恢复

### 下载成功但加载失败

- 检查服务器 Java 版本（推荐 Java 17+）
- 检查 `cloud.021209.xyz` 能否正常访问（海外服务器可能需要代理）
- 查看日志中 `[ArcartX-Suite-Cloud] 解密模块 xxx 失败` 提示：可能是 native 库未正确加载，重新安装 ArcartX-Suite 插件

### 服务器码丢失

绑定成功后，服务器码会自动写回 `config.yml` 的 `cloud.server-code` 字段。若因配置重置丢失，只需保留 `qq` 和 `apiKey` 重新启动即可，云端会返回同一个服务器码。

### 多服共用同一个 QQ

一个 QQ 可以绑定**多个服务器**，每个服务器有独立的 `serverCode`。装备模块时需要分别给每个服务器勾选。

## 模块签名验证（可选安全加固）

如果你使用了第三方/私人定制模块，且对方提供了 Ed25519 公钥，可在 `config.yml` 中配置：

```yaml
module-signature-public-keys:
  - "Base64编码的Ed25519公钥1"
  - "Base64编码的Ed25519公钥2"
```

启用后，`modules/` 目录下所有 `.jar` 模块必须经过有效数字签名才能加载。签名无效或缺失的模块将被拒绝，并在控制台输出警告。

::: tip 向后兼容
旧版单字符串配置 `module-signature-public-key` 仍可正常读取，无需手动迁移。
:::

## .axb 加密模块加载流程

云端下载的模块以 `.axb` 格式存储（AES 加密），加载时自动解密：

1. 服务器向云端申请模块令牌（24 小时有效）
2. 云端返回已装备模块列表及加密 `.axb` 数据
3. ArcartX-Suite 使用 native 安全库解密 `.axb`，在内存中还原为 jar 字节码
4. 通过 `ByteArrayModuleClassLoader` 在内存中直接加载，不落地磁盘
5. 模块启用时执行正常的 `AXSModule.onEnable()` 生命周期

::: warning native 库依赖
解密 `.axb` 需要 ArcartX-Suite 内置的 native 库（`ArcartX-Suite-native.dll` / `libaxs-native.so`）。若出现解密失败，请确认：
- 服务器操作系统为 Windows 或 Linux（x86_64）
- ArcartX-Suite jar 内 `native/` 目录包含对应平台的库文件
- 无其他插件冲突占用 native 符号

技术细节见 [Native 安全与模块加密](/architecture/native-security)。
:::
