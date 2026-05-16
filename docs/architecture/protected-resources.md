# 资源加密 (.axb)

AXS 在 Gradle 阶段把**除 `plugin.yml` 之外的所有 YAML** 加密成 `.axb` 文件打进 jar，运行时才解密释放。

## 加密时机

`build.gradle.kts` 注册了 `protectYamlResources` 任务：

```kotlin
val protectYamlResources by tasks.registering(ProtectYamlResourcesTask::class) {
    sourceDir.set(layout.projectDirectory.dir("src/main/resources"))
    outputDir.set(protectedResourcesDir)
}
```

## 协议（per-file）

```
+----+----+----+----+--------+--------------------------+
| 'A'| 'X'| 'R'| '1'|  IV    |   AES/GCM ciphertext     |
| 04 byte magic      | 12 byte|         (variable)        |
+----+----+----+----+--------+--------------------------+
```

明文 = `GZIP(原 YAML 字节)`

- 算法：`AES/GCM/NoPadding`，GCM tag 128 bit
- IV：每个文件随机 12 byte
- Key：SHA-256 派生

## 运行时解密

1. `ProtectedResourceStore.load("ArcartXEntityTracker.yml")` → 解密 `.axb` → 原 YAML
2. `YamlConfigSynchronizer` 合并到玩家已有配置
3. 最终 YAML 交给模块 Configuration POJO 解析

::: danger 不要去掉 protectYamlResources 任务
去掉后默认 YAML 会直接打进 jar，违反 AXS 发行约定。
:::
