---
title: 事件 | ArcartX-Suite Minecraft插件API文档
description: 事件 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 事件

ArcartX-Suite 提供标准 Bukkit 事件，供第三方插件监听模块状态变化。

## ModuleLifecycleEvent

模块生命周期事件——模块加载 / 卸载 / 重载时触发。标记为 `@ApiStability.Stable`。

**第三方插件**（不仅限于 AXS 模块）可通过标准 Bukkit 事件机制监听此事件，在 ArcartX-Suite 模块状态变化时执行相应逻辑。

### 使用示例

```java
public class MyListener implements Listener {

    @EventHandler
    public void onModuleLifecycle(ModuleLifecycleEvent event) {
        if (event.phase() == ModuleLifecycleEvent.Phase.ENABLED
                && "warehouse".equals(event.moduleId())) {
            // warehouse 模块已加载完成
            // 可以安全查找其 Capability
            getServer().getLogger().info("Warehouse 模块已启用！");
        }

        if (event.phase() == ModuleLifecycleEvent.Phase.DISABLING
                && "title".equals(event.moduleId())) {
            // title 模块即将禁用
            // 清理对 title 的引用
        }
    }
}
```

### 生命周期阶段

| Phase | 说明 | 时机 |
|-------|------|------|
| `ENABLING` | 模块即将启用 | `onEnable()` 之前 |
| `ENABLED` | 模块已成功启用 | `onEnable()` 返回 `true` 之后 |
| `ENABLE_FAILED` | 模块启用失败 | `onEnable()` 返回 `false` 或抛出异常 |
| `DISABLING` | 模块即将禁用 | `onDisable()` 之前 |
| `DISABLED` | 模块已禁用 | `onDisable()` 完成之后 |
| `RELOADING` | 模块开始重载 | `onReload()` 之前 |
| `RELOADED` | 模块重载完成 | `onReload()` 完成之后 |

### 事件属性

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `moduleId()` | `String` | 模块 id（如 `"warehouse"`, `"chat"`） |
| `moduleName()` | `String` | 模块显示名称 |
| `phase()` | `Phase` | 当前生命周期阶段 |
| `module()` | `AXSModule` | 模块实例（`ENABLE_FAILED` 时可能处于不完整状态） |

### 注意事项

- 此事件在**主线程**上触发，避免在 handler 中执行耗时操作。
- `ENABLE_FAILED` 阶段获取的模块实例可能处于不完整状态，不应调用其业务方法。
- 事件实现了标准 Bukkit `HandlerList` 协议，可通过 `ModuleLifecycleEvent.getHandlerList()` 获取。

### 典型场景

| 场景 | 推荐监听的 Phase |
|------|------------------|
| 等待目标模块加载后查找 Capability | `ENABLED` |
| 清理对某模块的引用 | `DISABLING` |
| 重载后刷新缓存 | `RELOADED` |
| 记录模块失败日志 | `ENABLE_FAILED` |

