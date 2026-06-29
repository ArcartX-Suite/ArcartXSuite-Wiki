---
title: 模块 Ed25519 签名 | ArcartX-Suite 开发者指南
description: 第三方模块 module.yml 签名的生成、验证与服主配置——与云端通信、跨服 HMAC 的区别说明。
---

# 模块 Ed25519 签名

AXS 支持对 **`modules/` 目录下的模块 Jar**（含云端下发的内存加载）做 **Ed25519 数字签名校验**。开发者用私钥对 `module.yml` 元数据签名，服主在宿主 `config.yml` 配置作者公钥后，只有签名合法且公钥匹配的模块才会被加载。

::: warning 与「云端绑定密钥」不是一回事
| 机制 | 配置位置 | 用途 |
|------|----------|------|
| **模块签名**（本文） | `module-signature-public-keys` + `module.yml` 的 `signature` | 验证**模块作者身份**，防 Jar 被篡改 |
| **云端服务器绑定** | `cloud.qq` / `apiKey` + 本地 `data/` 下 Ed25519 密钥对 | 服务器与 **cloud.021209.xyz** 通信、拉取 `.axb` |
| **跨服 HMAC** | `cross-server.signature.secret` | 子服之间 Redis/Proxy 消息的 **HMAC-SHA256**，不是 Ed25519 |
| **版本检查公钥** | `version-check-public-key` | 校验云端返回的**更新公告**是否被篡改 |

请勿把跨服 `signature.secret` 或云端绑定公钥填进 `module-signature-public-keys`。
:::

## 何时需要签名

| 角色 | 建议 |
|------|------|
| **模块开发者** | 发布给服主的定制模块时，可选签名并附带公钥；服主开启校验后只信任你的 Jar |
| **服主** | 仅在使用**第三方/私人定制** Jar 且希望防篡改时，向作者索取公钥并写入 `config.yml` |
| **仅用官方云端模块** | 默认 **不要** 配置公钥列表（留空 `[]`）。官方模块 `module.yml` 通常无 `signature` 字段，开启校验会导致**全部模块无法加载** |

::: danger 开启校验后的行为
`module-signature-public-keys` **非空** 时，宿主对**每一个**待加载模块执行验签：

- `module.yml` 缺少 `signature` → **拒绝加载**
- 签名与任一已配置公钥不匹配 → **拒绝加载**
- 验签通过 → 正常加载

因此这不是「只校验第三方」的开关，而是「**只允许已签名且公钥在白名单内的模块**」。
:::

## 签名规则（与代码一致）

宿主 `ModuleSignatureVerifier` 的验签逻辑如下。

### 待签名字符串（payload）

```
{id}:{version}:{mainClass}
```

示例：`mymodule:1.0.0:com.example.mymodule.MyModule`

- 三段均来自 **`module.yml`** 的 `id`、`version`、`main`
- 字符编码：**UTF-8**
- **不含** Jar 文件内容、不含 `depends` 等其他字段

### module.yml

```yaml
id: mymodule
name: MyModule
version: 1.0.0
main: com.example.mymodule.MyModule
api-version: 1.0
# Ed25519 签名（Base64），由作者私钥对 payload 签名得到
signature: "xxxxxxxx..."
```

修改 `id` / `version` / `main` 任一项后，**必须重新签名**。

### 公钥格式（给服主）

- 算法：**Ed25519**
- 编码：**X.509 SubjectPublicKeyInfo**（Java `PublicKey.getEncoded()` 的格式）
- 传输：**Base64 单行字符串**
- **不是** 裸 32 字节公钥，也**不是** PEM 文件原文

服主配置示例：

```yaml
# plugins/ArcartX-Suite/config.yml
module-signature-public-keys:
  - "MCowBQYDK2VwAyEA..."   # 作者 A 的公钥
  - "MCowBQYDK2VwAyEA..."   # 作者 B 的公钥（可多个）
```

留空 `[]` 表示**关闭**模块签名校验（默认）。

---

## 开发者：生成密钥并签名

### 方式一：OpenSource 仓库脚本（推荐）

[ArcartXSuite-Core](https://github.com/ArcartX-Suite/ArcartXSuite-Core) 提供 `scripts/sign-module.py`：

```bash
# 1. 首次：生成密钥对（私钥仅保存在本机，勿提交 Git）
python scripts/sign-module.py keygen --out-dir ./module-signing-keys

# 2. 对 module.yml 签名并写回 signature 字段
python scripts/sign-module.py sign \
  --module-yml src/main/resources/module.yml \
  --private-key module-signing-keys/ed25519-private.pem

# 3. 查看应发给服主的公钥（Base64 一行）
python scripts/sign-module.py pubkey --public-key module-signing-keys/ed25519-public.pem
```

打包前执行 `sign`，确保 Jar 内 `module.yml` 已含最新 `signature`。

### 方式二：Java 17+ 一行工具类

```java
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.Base64;

public final class ModuleSignTool {
    public static void main(String[] args) throws Exception {
        String id = "mymodule";
        String version = "1.0.0";
        String mainClass = "com.example.mymodule.MyModule";
        String payload = id + ":" + version + ":" + mainClass;

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();

        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign(kp.getPrivate());
        sig.update(payload.getBytes(StandardCharsets.UTF_8));
        String signatureB64 = Base64.getEncoder().encodeToString(sig.sign());
        String publicKeyB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

        System.out.println("signature (写入 module.yml): " + signatureB64);
        System.out.println("public key (给服主): " + publicKeyB64);
    }
}
```

生产环境应**固定使用同一私钥**多次签名，不要每次 `keygen` 换新钥。

### 方式三：OpenSSL（仅生成密钥）

```bash
openssl genpkey -algorithm Ed25519 -out ed25519-private.pem
openssl pkey -in ed25519-private.pem -pubout -out ed25519-public.pem
# 公钥转 Base64（X509 DER，与 Java 验签格式一致）
openssl pkey -in ed25519-public.pem -pubin -outform DER | base64 -w0
```

签名 payload 建议仍用 `sign-module.py` 或 Java，避免与宿主算法不一致。

---

## Gradle 构建集成（可选）

在 `build.gradle.kts` 中于 `jar` 任务前签名：

```kotlin
tasks.register<Exec>("signModuleYml") {
    val moduleYml = file("src/main/resources/module.yml")
    val key = file("module-signing-keys/ed25519-private.pem")
    commandLine(
        "python", "scripts/sign-module.py", "sign",
        "--module-yml", moduleYml.absolutePath,
        "--private-key", key.absolutePath
    )
    onlyIf { key.exists() }
}

tasks.jar {
    dependsOn("signModuleYml")
    from("src/main/resources") { include("module.yml") }
}
```

未配置私钥时跳过签名，便于本地调试；发布 CI 再注入私钥。

---

## 服主：开启校验

1. 向模块作者索取 **Base64 公钥**（一行）
2. 编辑 `plugins/ArcartX-Suite/config.yml`：

```yaml
module-signature-public-keys:
  - "作者的公钥Base64"
```

3. 重启或 `/axs reload all`
4. 控制台若出现 `[ModuleSignature] 模块 xxx 缺少 signature` 或 `签名验证失败`，说明 Jar 未签名、公钥不对，或 `module.yml` 与签名时的 `id/version/main` 不一致

::: tip 与云端 `.axb` 的关系
云端下发的加密模块在内存中还原为 Jar 后，**同样会走** `module.yml` 签名校验（若你已配置公钥）。官方模块若未带 `signature`，请勿在生产环境开启此列表；定制模块若需云端下发，也须在 `.axb` 内的 `module.yml` 写好签名。
:::

---

## 发布清单（给服主的信息）

发布定制模块时，建议 README 中写明：

```markdown
## 模块签名校验（可选）

- 模块 ID：`mymodule`
- Ed25519 公钥（Base64，X509）：`MCowBQYDK2VwAyEA...`
- 配置方式：在 `plugins/ArcartX-Suite/config.yml` 的 `module-signature-public-keys` 中加入上述公钥
- 注意：开启后仅加载带有效签名的模块；请确认未与未签名的官方模块混用
```

---

## 常见问题

### 公钥填了仍加载失败

- 确认公钥是 **X509 Base64**，不是 PEM 头尾或裸 32 字节
- 确认 `module.yml` 中 `id` / `version` / `main` 与签名时完全一致
- 确认 Jar 根目录的 `module.yml` 已更新（重新打包）

### 开启校验后官方模块也挂了

预期行为：官方模块默认无 `signature`。仅在使用**全部来自已签名第三方**的场景下开启；或保持 `module-signature-public-keys: []`。

### 和 Jar 内 PROTECTION.MF / native 校验的关系

宿主 **core** 本体有 Merkle + native 完整性校验；**模块 Jar** 的 `module.yml` Ed25519 是独立机制，由服主按需开启。详见 [Native 安全](/architecture/native-security)。

---

## 相关文档

- [开发第三方模块](./module-development) — `module.yml` 字段说明
- [使用第三方模块](./using-third-party-modules) — 服主安装与排错
- [云端模块](/guide/cloud-modules) — 云端授权与 `.axb` 加载
- [Native 安全与模块加密](/architecture/native-security)
