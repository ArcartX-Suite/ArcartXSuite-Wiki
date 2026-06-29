---
title: Native 安全与模块加密 | ArcartX-Suite Minecraft插件架构文档
description: ArcartX-Suite 本体与云端模块的 native 解密、类字节码 .axb 与 YAML .axb 的区别。
---

# Native 安全与模块加密

ArcartX-Suite 存在**两种 `.axb` 加密产物**，用途不同，勿混淆：

| 类型 | 加密对象 | 打包时机 | 运行时行为 |
| --- | --- | --- | --- |
| **YAML 资源 `.axb`** | 模块/宿主 `resources/*.yml` | Gradle `protectYamlResources` | 解密后合并到 `data/<module>/` 可编辑配置 |
| **模块类 `.axb`** | 模块 Java 类字节码 | CI `-Paxs.protectModules` / 云端构建 | 内存解密，经 `ByteArrayModuleClassLoader` 加载，**不落地磁盘** |

YAML 资源加密详见 [资源加密 (.axb)](protected-resources)。本节说明**本体核心与云端模块**的安全链路。

## 云端模块加载流程

```
服主在云端「装备模块」
        │
        ▼
服务器启动 / /axs sync
        │
        ▼
CloudModuleService 用 qq + apiKey 换取模块令牌（24h）
        │
        ▼
下载加密 .axb（模块类字节码）
        │
        ▼
NativeBridge 解密（AES-GCM / ChaCha20，密钥来自 root_seed）
        │
        ▼
ByteArrayModuleClassLoader 在内存中 defineClass
        │
        ▼
AXSModule.onEnable(context)
```

配置步骤见 [云端模块](/guide/cloud-modules)。立即拉取更新可用 `/axs update <模块ID|all>`。

## Bootstrap 瘦壳

服主首次安装的是 **bootstrap jar**（瘦壳），启动后从云端下载加密**核心本体** `.axb` 并注入运行。这样核心逻辑可随云端更新，无需手动替换整包 jar。

## Native 库依赖

解密依赖平台 native 库（随插件分发）：

- Windows：`ArcartX-Suite-native.dll`
- Linux x86_64：`libaxs-native.so`

若日志出现 `解密模块 xxx 失败`：

1. 确认操作系统为 Windows 或 Linux x86_64
2. 确认 jar 内 `native/` 目录完整
3. 重新安装插件；避免与其他占用相同 JNI 符号的插件冲突

## 模块签名验证（可选）

对 `modules/` 下的模块 Jar（含云端内存加载），可在 `config.yml` 配置 Ed25519 公钥列表，校验 `module.yml` 中的 `signature` 字段：

```yaml
module-signature-public-keys:
  - "Base64编码的Ed25519公钥"
```

::: warning 默认应留空
官方模块通常无 `signature`。列表非空时，**未签名模块一律拒绝加载**。开发者签名流程与服主配置见 **[模块 Ed25519 签名](/guide/developer/module-signature)**。
:::

云端 `.axb` 的令牌与平台校验链路与此独立，但内存还原后仍会执行 `module.yml` 验签（若已配置公钥）。

## 与服主的关系

- **纯云端路径**：配置 `cloud.qq` + `apiKey`，在云端装备模块即可，无需手动放 jar
- **本地 jar 路径**：将 `ArcartXSuite-<Module>-*.jar` 放入 `modules/`，宿主检测到外部 jar 时跳过内置加载
- **两种路径可混用**：未装备到云端的模块仍可按传统方式放 jar
