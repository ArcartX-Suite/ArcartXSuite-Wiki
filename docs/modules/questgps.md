# QuestGPS 任务导航

## 功能定位

将 Chemdah 任务追踪展示为 ArcartX HUD 路径点导航。

### 核心特性

- **HUD 导航箭头**：在 ArcartX HUD 上显示指向任务目标的导航箭头和距离信息
- **Chemdah 联动**：自动读取 Chemdah 任务追踪数据，无需手动配置坐标
- **目标追踪**：玩家可选择追踪/取消追踪特定任务目标
- **HUD 开关**：玩家可随时开关导航 HUD 显示
- **EventPacket 联动**：可通过 EventPacket 的 `questgps.offer` / `questgps.accept` 动作自动推送任务导航

## 依赖

- 必需：ArcartX、Chemdah

## 启用步骤

```yaml
modules:
  questgps:
    enabled: true
```

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/AXS questgps status` | 查看任务导航模块状态 |
| `/AXS questgps reload` | 重载任务导航配置和 UI |
| `/AXS questgps open <玩家>` | 为在线玩家打开任务导航界面 |

### 玩家命令（权限：`arcartxsuite.questgps.use`）

| 命令 | 说明 |
| --- | --- |
| `/questgps` 或 `/questgps open` | 打开任务导航菜单 |
| `/questgps cleartrack` | 清除当前追踪的任务目标，HUD 导航箭头消失 |
| `/questgps hud [on\|off\|toggle]` | 控制 HUD 导航显示。默认 `toggle` 切换 |
