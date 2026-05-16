# CombatEffect 战斗特效

## 功能定位

战斗视觉反馈一站式方案，包含两大能力：

- **击杀特效** — 监听玩家攻击 / 击杀活体实体，把上下文发包给 ArcartX UI 播放击杀特效或弹出击杀提示。不限 PVP，可监听任意活体
- **伤害飘字** — 把伤害 / 治疗事件桥接到 ArcartX 伤害显示，按来源拆分：原始伤害、玩家伤害、暴击、治疗等

### 核心特性

**击杀特效：**
- **双触发器**：`kill`（击杀时触发）和 `attack`（攻击时触发），可同时启用
- **多接收者**：支持 `killer`（攻击者）、`victim`（受害者）等接收者角色
- **丰富变量**：pack 支持 `{killer_name}`、`{victim_name}`、`{victim_display_name}`、`{victim_entity_type_name}`、`{victim_mythic_mob_id}`、`{victim_is_player}`、坐标、世界、UUID、主手物品等
- **黑名单过滤**：支持按 MythicMob ID 或 Bukkit EntityType 过滤，避免对特定实体触发特效
- **灵活发包格式**：pack 支持字符串、列表、字典三种模式，适配不同 UI 需求

**伤害飘字：**
- **智能来源检测**：自动选择最优伤害来源 — MythicLib → CraneAttribute → AttributePlus → Bukkit 原版
- **来源回退**：指定来源不可用时自动回退到下一个可用来源
- **分类显示**：原始伤害、玩家伤害、MythicLib 属性伤害、CraneAttribute 属性伤害分别使用不同配置 ID
- **治疗飘字**：原版治疗和 MythicMobs 技能治疗均可显示，支持精确模式（实际生效量）
- **最小阈值**：每种伤害/治疗类型可配置最小显示值，避免微量数字刷屏

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 播放击杀特效、伤害飘字和治疗飘字 | 模块无法向客户端展示战斗反馈 |
| 可选 | MythicLib / MMOItems | 读取 MythicLib 结算后的属性伤害和 MMOItems 伤害来源 | 自动回退到下一个来源或 Bukkit 原版伤害 |
| 可选 | CraneAttribute | 读取 CraneAttribute 属性伤害 | 自动回退到下一个来源 |
| 可选 | AttributePlus | 读取 AttributePlus 属性伤害 | 自动回退到 Bukkit 原版伤害 |
| 可选 | MythicMobs / MythicBukkit | MythicMob ID 黑名单、技能治疗识别 | 原版实体照常显示；MythicMob 专属过滤/治疗识别不可用 |

## 启用步骤

```yaml
modules:
  combateffect:
    enabled: true
```

## 配置

CombatEffect 的配置分为击杀特效和伤害飘字两部分。

### 击杀特效（`ArcartXCombatEffect.yml`）

```yaml
settings:
  debug: false
  ui-id: "AXS:kill_effect_hud"
  register-ui-on-enable: true

triggers:
  player-kill:
    enabled: true
    recipient: attacker
    pack: "{killer_name};{victim_name};{weapon}"
```

### 伤害飘字

伤害 / 治疗事件的数字显示配置：

```yaml
digis-display:
  debug: false
  damage:
    enabled: true
    default-config-id: "default_damage"
  heal:
    enabled: true
    default-config-id: "default_heal"
  source-detection:
    exact-mode: true
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs combateffect status` | 查看战斗特效模块是否启用和配置加载状态 |
| `/axs combateffect reload` | 重载战斗特效配置 |
