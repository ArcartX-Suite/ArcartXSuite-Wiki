---
title: 开发第三方模块 | ArcartX-Suite 开发者指南
description: 从零编写 ArcartX-Suite 第三方模块——Gradle 工程、module.yml、AbstractAXSModule、UI、配置、命令与客户端包。
---

# 开发第三方模块

本文是 **完整动手教程**：读完即可创建一个可加载的 AXS 模块 Jar。更底层的 API 说明见 [API 参考](/api/)。

## 前置条件

| 项目 | 要求 |
|------|------|
| JDK | 17+ |
| 构建 | Gradle（推荐）或 Maven |
| 服务端 | 已安装 **ArcartX** 插件 + **ArcartXSuite** 宿主 |
| 客户端 | 玩家需安装 **ArcartX 模组**（UI/HUD/自定义包依赖客户端） |
| SDK | `axs-api.jar` — [ArcartXSuite-Core Releases](https://github.com/xuanmomo233/ArcartXSuite-Core/releases)（推荐）；或克隆该仓库后 `./gradlew :axs-api:jar` 构建 |

::: warning 不要引用宿主实现
模块代码中 **只能** `import xuanmo.arcartxsuite.api.*`。不要 `import xuanmo.arcartxsuite.bridge.*` 或宿主 `module` 包——会导致 ClassLoader 隔离失败，且在不同版本间不兼容。
:::

## 第一步：工程结构

```
MyAXSModule/
├── build.gradle.kts
├── settings.gradle.kts
├── libs/
│   └── axs-api-x.x.x.jar
└── src/main/
    ├── java/com/example/mymodule/
    │   ├── MyModule.java          # 入口
    │   ├── MyService.java         # 业务
    │   ├── MyListener.java        # 事件（可选）
    │   └── MyPacketHandler.java   # 客户端包（可选）
    └── resources/
        ├── module.yml             # 必须：模块元数据
        ├── ArcartXMyModule.yml    # 默认配置（首次导出用）
        └── arcartx/ui/my_ui.yml   # ArcartX UI 模板（可选）
```

### build.gradle.kts

```kotlin
plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly(files("libs/axs-api-x.x.x.jar"))
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    // 可选
    compileOnly("me.clip:placeholderapi:2.11.7")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.jar {
    archiveBaseName.set("MyAXSModule")
    // module.yml 必须在 Jar 根目录
    from("src/main/resources") {
        include("module.yml")
    }
}
```

执行 `./gradlew jar`，产物在 `build/libs/MyAXSModule.jar`。

## 第二步：module.yml

放在 `src/main/resources/module.yml`，打包后位于 Jar **根目录**：

```yaml
id: mymodule
name: MyModule
version: 1.0.0
main: com.example.mymodule.MyModule
api-version: 1.0
depends: []
softdepends: []
external-depends: []
external-softdepends: []
```

| 字段 | 说明 |
|------|------|
| `id` | 全局唯一，与 `config.yml` 中 `modules.mymodule` 对应 |
| `main` | 实现 `AXSModule` 的类全限定名 |
| `depends` | **硬依赖**的其他 AXS 模块 id；缺失则本模块拒绝启动 |
| `softdepends` | **软依赖**；缺失时跳过，不报错（跨模块 Capability 常用） |
| `external-depends` | 硬依赖的外部 Bukkit 插件名（如 `PlaceholderAPI`） |
| `signature` | 可选；Ed25519 签名（Base64）。服主开启公钥校验时必填，见 [模块签名](./module-signature) |

::: tip 加载顺序
宿主按 `depends` 做拓扑排序。若模块 A `depends: [title]`，则 Title 一定先于 A 启用——这对 Capability 使用方很重要。详见 [Capability 详解](./capability-guide#加载顺序与-softdepends)。
:::

## 第三步：实现模块入口

### 推荐：继承 AbstractAXSModule

基类自动处理：配置导出、UI 资源复制、监听器/命令/PAPI 注册与卸载、reload 时保留 UI。

```java
package com.example.mymodule;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.bukkit.event.Listener;
import xuanmo.arcartxsuite.api.AbstractAXSModule;
import xuanmo.arcartxsuite.api.ModuleDescriptor;

public final class MyModule extends AbstractAXSModule {

    private MyService service;

    @Override
    public ModuleDescriptor descriptor() {
        return ModuleDescriptor.builder("mymodule")
            .name("MyModule")
            .version("1.0.0")
            .mainClass(getClass().getName())
            .build();
    }

    @Override
    protected String configFileName() {
        return "ArcartXMyModule.yml";
    }

    @Override
    protected Map<String, String> uiResourceMappings() {
        return Map.of("arcartx/ui/my_ui.yml", "ui/my_ui.yml");
    }

    @Override
    protected List<Listener> createListeners() {
        return List.of(new MyListener(context));
    }

    @Override
    protected void loadConfiguration(File configFile) {
        // 读取 configFile（已导出到 data/mymodule/config.yml）
    }

    @Override
    protected void startService() {
        bindUi("my_ui", "ui/my_ui.yml");
        service = new MyService(context);
        service.start();

        // 若对外暴露能力，在此注册 Capability（见 capability-guide）
        // context.registerCapability(MyApi.class, service);
    }

    @Override
    protected void stopService() {
        if (service != null) {
            service.shutdown();
            service = null;
        }
    }
}
```

### 极简：直接实现 AXSModule

无配置、无 UI 时可用，需自行管理 `onEnable` / `onDisable` / `onReload`。

## 第四步：使用 ModuleContext

`context` 是模块访问宿主能力的唯一方式：

```java
// 调度与日志
context.plugin().getServer().getScheduler().runTask(...);
context.logger().info("模块已启动");

// 数据目录
File data = context.dataFolder();           // plugins/ArcartXSuite/data/mymodule/
File uiDir = context.uiFolder();            // plugins/ArcartXSuite/ui/

// ArcartX 桥接
var packet = context.packetBridge();        // UI 注册、发包
var client = context.clientBridge();        // 检测客户端在线
var items  = context.itemStackBridge();     // 物品序列化

// 全局桥接
var itemsrc = context.itemSourceRegistry(); // MythicMobs / MMOItems 等
var currency = context.currencyManager();   // Vault / PlayerPoints
var attr     = context.attributeBridge();   // AttributePlus / MythicLib 等

// 跨模块
var mail = context.getCapability(MailDispatchable.class);

// 跨服
context.crossServer().publish("my_channel", payload);
```

完整列表见 [ModuleContext](/api/module-context)。

## 第五步：UI 与客户端包

### UI 绑定

1. 资源放在 Jar 内 `arcartx/ui/xxx.yml`
2. `uiResourceMappings()` 声明导出路径
3. `startService()` 中 `bindUi(uiId, relativePath)`

reload 时 `AbstractAXSModule` 会 **保留已打开的 UI**，避免玩家 HUD 闪烁消失。

### 客户端自定义包

实现 `ClientPacketHandler`，在模块中注册：

```java
@Override
protected ClientPacketHandler createPacketHandler() {
    return new MyPacketHandler();
}
```

客户端通过 ArcartX 模组发送 `action` 字段匹配的数据包，服务端在 `handle(Player, Map)` 中处理。

## 第六步：注册 /axs 子命令

实现 `ModuleCommandHandler`：

```java
public final class MyModule extends AbstractAXSModule implements ModuleCommandHandler {

    @Override
    public String commandId() { return "mymodule"; }

    @Override
    public List<String> actions() { return List.of("help", "reload"); }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        // /axs mymodule help
        return true;
    }
}
```

## 第七步：与其他模块联动（Capability）

AXS **不推荐** 在模块 A 里 `import` 模块 B 的实现类。标准做法是：

1. 在 `axs-api` 或你的公共包中定义 **接口**（Capability）
2. 提供方：`context.registerCapability(接口.class, 实现)`
3. 使用方：`context.getCapability(接口.class)`，务必判空

完整教程见 **[Capability 详解](./capability-guide)**。

## 第八步：打包与部署

可选：发布前对 `module.yml` 做 [Ed25519 签名](./module-signature)，并把公钥交给服主配置 `module-signature-public-keys`。

```bash
./gradlew jar
cp build/libs/MyAXSModule.jar /path/to/server/plugins/ArcartXSuite/modules/
```

`config.yml`：

```yaml
modules:
  mymodule:
    enabled: true
```

热加载（无需重启）：

```
/axs load mymodule
/axs reload mymodule
/axs unload mymodule
```

服主侧安装说明见 [使用第三方模块](./using-third-party-modules)。

## 配置与迁移

若模块有 YAML 配置，推荐：

- `configFileName()` 返回 Jar 内默认文件名
- 覆写 `currentConfigVersion()`、`migrationFolder()` 做破坏性升级
- 使用 `SyncPolicy` 声明动态节点（避免智能同步覆盖玩家数据）

详见 [配置智能体检](/guide/config-management)。

## 常见问题

### ClassNotFoundException

- 检查 `module.yml` 的 `main` 是否与类名一致
- 确认 Jar 内含编译后的 `.class`

### 模块加载了但 UI 不显示

- 玩家是否安装 ArcartX 客户端模组
- `bindUi` 的路径是否与 `uiResourceMappings` 一致

### PlaceholderAPI 扩展未注册

- 服务器需安装 PAPI
- `createPlaceholderExpansion()` 返回非 null 对象

### 依赖模块未加载

- `depends` 写错 id → 本模块拒绝启动
- 需要可选联动时用 `softdepends` + Capability 判空

## 下一步

- [Capability 详解](./capability-guide) — 跨模块调用的核心机制
- [使用第三方模块](./using-third-party-modules) — 给服主的部署说明
- [模块化架构](/architecture/modular) — 宿主加载流程源码级说明
