# 模块生命周期

## AXSModule

模块生命周期核心接口。每个独立模块 Jar 需提供一个实现此接口的主类，并在 `module.yml` 的 `main` 字段中声明。

```java
public interface AXSModule {
    ModuleDescriptor descriptor();
    boolean onEnable(ModuleContext context) throws Exception;
    void onDisable();
    void onReload() throws Exception;
    boolean isReady();
    default List<ModuleConfigSpec> configSpecs() { return List.of(); }
}
```

### 生命周期方法

| 方法 | 调用时机 | 说明 |
|------|----------|------|
| `descriptor()` | 模块加载前 | 返回模块元数据 |
| `onEnable(context)` | 密码验证和依赖检查通过后 | 启动模块，返回 `true` 表示成功 |
| `onDisable()` | 模块被卸载/禁用时 | 释放所有资源 |
| `onReload()` | 执行 `/axs reload` 时 | 热重载配置，不卸载 ClassLoader |
| `isReady()` | 任意时刻 | 查询模块是否正常运行 |
| `configSpecs()` | `onEnable` 之前 | 返回配置诊断规约（参与智能配置体检） |

## AbstractAXSModule

推荐使用的模块抽象基类，封装了声明式生命周期管理。子类通过覆写声明式钩子方法和实现三个抽象方法即可完成模块开发。

### 声明式钩子（按需覆写）

| 方法 | 默认值 | 说明 |
|------|--------|------|
| `configFileName()` | `null` | 配置文件名（如 `"ArcartXMyModule.yml"`），`null` 表示无配置 |
| `defaultSyncPolicy()` | `SyncPolicy.strict()` | 配置同步策略，有动态节点时需覆写 |
| `currentConfigVersion()` | `1` | 配置版本号，破坏性变更时递增 |
| `configVersionPath()` | `"config-version"` | 版本号字段路径 |
| `migrationFolder()` | `"migrations"` | 迁移文件目录 |
| `mainConfigValidations()` | `List.of()` | 主配置校验规则列表 |
| `additionalConfigSpecs()` | `List.of()` | 附属配置规约列表 |
| `uiResourceMappings()` | `Map.of()` | UI 资源映射（Jar 内路径 → 输出路径） |
| `overwriteUiFiles()` | `false` | 是否覆写已有 UI 文件 |
| `createListeners()` | `List.of()` | Bukkit 事件监听器列表（自动注册/注销） |
| `commandBindings()` | `Map.of()` | 命令绑定（命令名 → TabExecutor） |
| `createPlaceholderExpansion()` | `null` | PlaceholderAPI 扩展实例 |
| `createPacketHandler()` | `null` | 客户端包处理器 |
| `packetHandlerPriority()` | `0` | 包处理器优先级（越小越优先） |
| `createInitializedHandler()` | `null` | 客户端初始化回调 |

### 必须实现的抽象方法

```java
// 加载并解析配置
protected abstract void loadConfiguration(@Nullable File configFile) throws Exception;

// 创建并启动服务
protected abstract void startService() throws Exception;

// 关闭服务并释放资源
protected abstract void stopService();
```

### 自动处理的生命周期流程

`onEnable` 时基类按以下顺序自动执行：

1. **导出配置** — 从模块 Jar 导出默认配置文件到宿主数据目录
2. **加载配置** — 调用 `loadConfiguration(configFile)`
3. **导出 UI** — 根据 `uiResourceMappings()` 导出并注册 UI 文件
4. **绑定命令** — 根据 `commandBindings()` 注册命令
5. **启动服务** — 调用 `startService()`
6. **注册监听器** — 注册 `createListeners()` 返回的监听器
7. **注册 PAPI** — 注册 `createPlaceholderExpansion()` 返回的占位符
8. **注册包处理器** — 注册 `createPacketHandler()` 和 `createInitializedHandler()`

`onDisable` 时反向清理所有已注册的资源。

### 完整示例

```java
public class MyModule extends AbstractAXSModule {

    private MyConfig config;
    private MyService service;

    @Override
    public ModuleDescriptor descriptor() {
        return ModuleDescriptor.builder("mymodule")
            .name("MyModule").version("1.0.0")
            .mainClass(getClass().getName())
            .depends(List.of())              // 依赖的其他 AXS 模块
            .externalDepends(List.of())      // 依赖的外部 Bukkit 插件
            .build();
    }

    @Override protected String configFileName() { return "ArcartXMyModule.yml"; }
    @Override protected boolean overwriteUiFiles() { return config.overwriteUi(); }

    @Override
    protected Map<String, String> uiResourceMappings() {
        return Map.of("arcartx/ui/my_view.yml", "ui/my_view.yml");
    }

    @Override
    protected Map<String, TabExecutor> commandBindings() {
        return Map.of("mycommand", new MyCommand(this));
    }

    @Override
    protected List<Listener> createListeners() {
        return List.of(new MyListener(service));
    }

    @Override
    protected void loadConfiguration(File configFile) {
        config = MyConfig.load(configFile);
    }

    @Override
    protected void startService() {
        service = new MyService(context, config);
        service.start();
    }

    @Override
    protected void stopService() {
        if (service != null) { service.shutdown(); service = null; }
    }
}
```

## ModuleDescriptor

模块元数据描述符，使用 Builder 模式构造。

```java
ModuleDescriptor desc = ModuleDescriptor.builder("mymodule")
    .name("MyModule")                        // 显示名称
    .version("1.0.0")                        // 版本号
    .mainClass("com.example.MyModule")       // 主类全限定名
    .depends(List.of("title"))               // 必须已加载的 AXS 模块
    .softDepends(List.of("warehouse"))       // 可选增强的 AXS 模块
    .externalDepends(List.of("MythicMobs"))  // 必须已安装的外部插件
    .externalSoftDepends(List.of("Vault"))   // 可选的外部插件
    .build();
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | **必填**，模块唯一标识（如 `"mymodule"`） |
| `name` | `String` | 显示名称，默认同 `id` |
| `version` | `String` | 版本号，默认 `"1.0.0"` |
| `mainClass` | `String` | AXSModule 实现类全限定名 |
| `depends` | `List<String>` | 强依赖的其他 AXS 模块 id |
| `softDepends` | `List<String>` | 软依赖的其他 AXS 模块 id |
| `externalDepends` | `List<String>` | 强依赖的外部 Bukkit 插件名 |
| `externalSoftDepends` | `List<String>` | 软依赖的外部 Bukkit 插件名 |

## module.yml

每个模块 Jar 在 `resources/` 中必须包含 `module.yml`：

```yaml
id: mymodule
name: MyModule
version: 1.0.0
main: com.example.MyModule
api-version: 1.0
depends: []
softdepends: []
external-depends: []
external-softdepends: []
```

宿主在加载模块 Jar 时解析此文件，并将其与 `descriptor()` 返回值合并。
