---
title: 事件 | ArcartX-Suite Minecraft插件API文档
description: 事件 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# 事件

ArcartX-Suite 提供标准 Bukkit 事件，供第三方插件监听模块状态变化。

## ModuleLifecycleEvent

模块生命周期事件——模块加载 / 卸载 / 重载时触发。标记为 `@ApiStability.Stable`。

**第三方插件**（不仅限于 ArcartX-Suite 模块）可通过标准 Bukkit 事件机制监听此事件，在 ArcartX-Suite 模块状态变化时执行相应逻辑。

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

---

## AttributeDamageEvent

统一属性伤害事件，由宿主从各属性插件（AttributePlus / CraneAttribute / MythicLib / Symphony）归一化后分发给注册了 `AttributeDamageListener` 的模块。标记为 `@ApiStability.Stable`。

**注册方式：** `context.attributeBridge().registerDamageListener(listener)`

### 事件字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `attacker` | `@Nullable Player` | 攻击者（非玩家攻击时为 null） |
| `target` | `@NotNull Entity` | 受击实体 |
| `damage` | `double` | 伤害数值 |
| `source` | `Source` | 伤害来源 |
| `critical` | `boolean` | 是否暴击 |
| `dodged` | `boolean` | 是否被闪避（dodged=true 时 damage=0） |
| `relation` | `@Nullable DamageRelation` | 元素克制关系（无克制系统时为 null） |

### Source 枚举

| 值 | 说明 |
|----|------|
| `ATTRIBUTE_PLUS` | AttributePlus 属性伤害 |
| `CRANE_ATTRIBUTE` | CraneAttribute 属性伤害 |
| `MYTHIC_LIB` | MythicLib / MMOItems 属性伤害 |
| `SYMPHONY` | Symphony 属性伤害 |
| `BUKKIT` | Bukkit 原版伤害 |
| `OTHER` | 其他来源 |

### DamageRelation 枚举

| 值 | 说明 |
|----|------|
| `ADVANTAGED` | 优势（克制对方） |
| `NEUTRAL` | 中性 |
| `DISADVANTAGED` | 劣势（被克制） |

### 暴击/闪避/克制的来源支持

| 功能 | Symphony | AttributePlus | CraneAttribute | MythicLib |
|------|----------|--------------|---------------|-----------|
| 暴击标记 | ✅ 伤害事件内置 | ❌ 独立事件，伤害事件不暴露 | ❌ | ❌ |
| 闪避标记 | ✅ HitCheckEvent | ❌ 无事件暴露 | ❌ | ❌ |
| 元素克制 | ✅ 优势/中性/劣势伤害占比 | ❌ 无此系统 | ❌ | ❌ |

> AttributePlus 的暴击判定在内部 `Crit.runAttack()` 中完成，不通过事件 API 暴露。闪避同理。未来版本可能支持。

### 使用示例

```java
context.attributeBridge().registerDamageListener(event -> {
    if (event.dodged()) {
        // 闪避飘字
        showDodgeText(event.attacker(), event.target());
        return;
    }
    if (event.critical()) {
        // 暴击飘字
        showCriticalText(event.attacker(), event.target(), event.damage());
        return;
    }
    if (event.relation() == AttributeDamageEvent.DamageRelation.ADVANTAGED) {
        // 克制飘字
        showAdvantagedText(event.attacker(), event.target(), event.damage());
    }
});
```

---

## AttributeHealEvent

统一属性治疗事件，由宿主从各属性插件归一化后分发。标记为 `@ApiStability.Stable`。

**注册方式：** `context.attributeBridge().registerHealListener(listener)`

### 事件字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `healer` | `@Nullable Player` | 治疗者 |
| `target` | `@NotNull LivingEntity` | 治疗目标 |
| `amount` | `double` | 治疗量 |
| `source` | `Source` | 治疗来源 |

---

## TaczGunDamageEvent

TACZ（创世战术武器）枪械伤害事件。当 TACZ Mod 的 `EntityHurtByGunEvent.Pre` 触发时，由 `TaczCombatBridge` 转换为标准 Bukkit 事件并广播。标记为 `@ApiStability.Stable`，since 1.1.0。

AXS 各模块可通过标准 Bukkit 事件机制监听此事件，以获取 TACZ 枪械伤害信息，而无需关心 Forge/NeoForge 事件总线的反射细节。

此事件与 `EntityDamageByEntityEvent` 完全解耦：TACZ 伤害不再被伪装为 Bukkit 原版事件，模块可明确区分枪械伤害与近战/弓箭等原版伤害。

**注册方式：** 标准 Bukkit `@EventHandler` 监听

### 事件字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `attacker` | `@NotNull Player` | 攻击者（开枪的玩家） |
| `target` | `@NotNull LivingEntity` | 被击中的实体 |
| `damage` | `double` | 伤害值（对应 TACZ `getBaseAmount()`，尚未经过护甲/抗性等减免） |
| `headShot` | `boolean` | 是否为爆头 |
| `gunId` | `@NotNull String` | 枪械 ID（如 `tacz:modern_kinetic_gun`） |

### 使用示例

```java
@EventHandler
public void onTaczDamage(TaczGunDamageEvent event) {
    Player attacker = event.getAttacker();
    LivingEntity target = event.getTarget();
    double damage = event.getDamage();
    boolean headshot = event.isHeadShot();
    String gunId = event.getGunId();
    // CombatEffect 模块可据此显示枪械伤害飘字
    // EntityTracker 模块可据此统计 Boss 枪械伤害排行
}
```

