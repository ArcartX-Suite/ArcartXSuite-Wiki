# Pickup 拾取提示

## 功能定位

物品拾取时在 ArcartX HUD 上弹出提示动画，可自定义外观和持续时间。

### 核心特性

- **实时拾取提示**：玩家拾取地面掉落物时，自动在 HUD 上弹出物品名称、数量和图标
- **多条堆叠**：同时最多显示 N 条最近拾取记录（默认 4 条），超出后旧提示向上挤出
- **自动消失**：每条提示在 HUD 上存活指定时间后自动消失（默认 3 秒）
- **物品序列化**：支持原版物品、NeigeItems、MythicMobs 物品等，自动读取 ItemStack 信息
- **HUD 自动注册**：启动时自动注册拾取提示 HUD，无需手动配置

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 拾取提示 HUD、物品图标和客户端动画包 | 模块无法显示拾取提示 |
| 可选 | NeigeItems | 识别 NeigeItems 物品显示名/数据 | 原版物品正常，NeigeItems 专属信息可能按普通物品显示 |
| 可选 | MythicMobs / MythicBukkit | 识别 MythicItems / MythicMob 掉落物信息 | 原版物品正常，Mythic 物品专属信息可能按普通物品显示 |
| 可选 | MMOItems | 识别 MMOItems 物品信息 | 原版物品正常，MMOItems 专属信息可能按普通物品显示 |

## 启用步骤

```yaml
modules:
  pickup:
    enabled: true
```

## 关键配置（`ArcartXPickup.yml`）

```yaml
settings:
  debug: false
  ui-id: "AXS:pickup_hud"
  register-ui-on-enable: true
  overwrite-ui-file: false
  display-duration-ticks: 60
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs pickup status` | 查看拾取提示模块状态 |
| `/axs pickup reload` | 重载拾取提示配置和 HUD |
