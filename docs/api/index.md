# 开放 API 参考

ArcartXSuite 1.1.0 起提供 **`axs-api`** 模块作为第三方开发的稳定接口层。所有公开 API 均位于 `xuanmo.arcartxsuite.api` 包下，第三方模块只需 `compileOnly` 依赖 `axs-api` 即可开发自定义模块。

## API 稳定性标记

所有公开接口均使用 `@ApiStability` 注解标记其稳定性级别：

| 注解 | 含义 |
|------|------|
| `@ApiStability.Stable` | 稳定 API，向后兼容，不会在小版本中破坏 |
| `@ApiStability.Experimental` | 实验性 API，可能在未来版本中修改或移除 |
| `@ApiStability.Internal` | 内部 API，仅供 ArcartXSuite 内部模块使用，第三方不应依赖 |
| `@ApiStability.Deprecated` | 已弃用 API，将在下一个大版本中移除（含 `replacedBy` 和 `removeIn` 属性） |

## API 总览

| 类 / 接口 | 包路径 | 说明 |
|-----------|--------|------|
| [`AXSModule`](./module-lifecycle) | `api` | 模块生命周期接口 |
| [`AbstractAXSModule`](./module-lifecycle#abstractaxsmodule) | `api` | 模块抽象基类，封装声明式生命周期 |
| [`ModuleContext`](./module-context) | `api` | 宿主提供给模块的上下文接口 |
| [`ModuleDescriptor`](./module-lifecycle#moduledescriptor) | `api` | 模块元数据描述符 |
| [`PacketBridgeAPI`](./bridge-api#packetbridgeapi) | `api.bridge` | UI/Packet 桥接 API |
| [`ClientBridgeAPI`](./bridge-api#clientbridgeapi) | `api.bridge` | 客户端桥接 API |
| [`ItemBridgeAPI`](./bridge-api#itembridgeapi) | `api.bridge` | ItemStack 序列化 API |
| [`ModuleLifecycleEvent`](./events#modulelifecycleevent) | `api.event` | 模块生命周期 Bukkit 事件 |
| [`ClientPacketHandler`](./module-context#clientpackethandler) | `api` | 客户端自定义包处理器 |
| [`ClientInitializedHandler`](./module-context#clientinitializedhandler) | `api` | 客户端初始化回调 |
| [Capability 接口](./capability) | `api.capability` | 跨模块能力通信 |
| [`MessageProvider`](./i18n) | `api.message` | 消息外部化 / i18n |

## 快速开始

```groovy
// build.gradle.kts
dependencies {
    compileOnly(project(":axs-api"))
    // 或使用发布的 jar
    // compileOnly(files("libs/axs-api.jar"))
}
```

```java
public class MyModule extends AbstractAXSModule {

    @Override
    public ModuleDescriptor descriptor() {
        return ModuleDescriptor.builder("mymodule")
            .name("MyModule").version("1.0.0")
            .mainClass(getClass().getName()).build();
    }

    @Override
    protected String configFileName() { return "ArcartXMyModule.yml"; }

    @Override
    protected void loadConfiguration(File configFile) {
        // 解析配置
    }

    @Override
    protected void startService() {
        PacketBridgeAPI bridge = context.packetBridge();
        if (bridge != null && bridge.isAvailable()) {
            // 使用桥接 API
        }
    }

    @Override
    protected void stopService() {
        // 释放资源
    }
}
```
