---
title: Map 世界地图插件 | ArcartXSuite Minecraft服务器
description: ArcartXSuite Map 世界地图，锚点传送、玩家路径点、小地图 HUD、世界解锁，我的世界服务器地图插件。
---

# Map 地图

::: tip 付费模块

Map 为付费模块，需要有效授权码激活。
:::
## 功能定位

世界地图系统：锚点传送、玩家自定义路径点、小地图 HUD、世界解锁。

### 核心特性

- **世界地图 UI**：ArcartX UI 驱动的全屏世界地图界面
- **多世界支持**：每个世界独立配置地图，可列出所有已配置的世界
- **锚点传送**：管理员定义锚点，玩家在地图上点击锚点进行传送（可配合 Vault 收费）
- **玩家路径点**：玩家可在地图上自定义标记点
- **小地图 HUD**：常驻 HUD 显示小地图，可通过命令开关
- **追踪系统**：玩家可在地图上追踪目标点，HUD 显示导航方向

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 世界地图 UI、小地图 HUD、路径点和点击回包 | 模块无法显示地图界面 |
| 可选 | Vault | 锚点传送、地图解锁等金币费用 | 收费项不可用或需要改为免费/命令扣费 |
| 可选 | PlayerPoints | 点券费用 | 点券收费不可用 |
| 可选 | MythicMobs / NeigeItems | 地图奖励、条件或展示物品来自对应物品库时识别 | 只影响对应配置项 |
| 可选 | QuestGPS 模块 | 将任务目标推到地图临时目标/导航 | 地图自身可用，任务导航联动不可用 |

## 启用步骤

```yaml
modules:
  map:
    enabled: true
```

## 配置

### 主配置（`ArcartXMap.yml`）

```yaml
debug: false

client:
  packet-id: "AXS_MAP"           # 客户端 Packet.send / 服务端 packetHandler 通信标识
  menu-ui-id: "AXS:map_menu"     # 全屏地图 UI
  hud-ui-id: "AXS:map_hud"       # 小地图 HUD
  register-ui-on-enable: true
  overwrite-ui-files: false

keybinds:
  category: "AXS Map"
  open-menu:
    enabled: true
    display-name: "打开地图"
    default-key: "M"
  toggle-hud:
    enabled: true
    display-name: "切换小地图"
    default-key: "H"

join:
  show-hud-on-join: true          # 玩家进服时自动显示小地图 HUD
  show-hud-delay-ticks: 20        # 延迟多少 tick 后显示（20 tick = 1 秒）

storage:
  mode: "sqlite"                  # sqlite 或 mysql
  sqlite:
    file: "map.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "arcartxsuite"
    username: "root"
    password: ""
  pool-size: 4
```

### 货币配置

```yaml
currencies:
  money:
    enabled: true
    provider: "vault"             # vault 或 playerpoints
    display-name: "金币"
    scale: 2                      # 显示小数位数
  points:
    enabled: true
    provider: "playerpoints"
    display-name: "点券"
    scale: 0
```

### 世界配置

每个世界需要在 `worlds` 节单独配置，对应一张地图图片资源：

```yaml
worlds:
  world:
    enabled: true
    display-name: "主世界"
    texture: "Map/world.png"      # ArcartX 资源包中的图片路径
    image-width: 2048             # 地图图片像素宽度
    image-height: 2048
    pixel-offset-x: 0            # 图片在坐标系中的像素偏移
    pixel-offset-z: 0
    default-zoom: 1.6            # 全屏地图默认缩放比
    hud-zoom: 0.18               # 小地图缩放比
    hud-size: 180                # 小地图 HUD 尺寸（像素）

  world_nether:
    enabled: true
    display-name: "下界"
    texture: "Map/world_nether.png"
    image-width: 1024
    image-height: 1024
    pixel-offset-x: 0
    pixel-offset-z: 0
    default-zoom: 1.4
    hud-zoom: 0.16
    hud-size: 180
```

| 字段 | 说明 |
| --- | --- |
| `texture` | ArcartX 资源包中的图片路径，对应 `textures/` 目录下的文件 |
| `image-width/height` | 地图图片分辨率（像素），用于坐标 → 像素换算 |
| `pixel-offset-x/z` | 图片中心偏移量，用于对齐游戏坐标与图片 |
| `default-zoom` | 全屏地图默认缩放倍率 |
| `hud-zoom` | 小地图 HUD 缩放倍率 |
| `hud-size` | 小地图显示尺寸（像素） |

### 默认解锁配置

```yaml
default-unlocks:
  - permission: "ArcartXSuite.map.use"
    anchors:              # 持有该权限时默认可见/可用的锚点 ID 列表
      - "spawn"
```

### 锚点字段详解

锚点文件位于 `data/map/anchors/*.yml`，同一文件可包含多个锚点，根键即为锚点 ID：

```yaml
# data/map/anchors/default.yml
spawn:
  enabled: true
  display-name: "新手村"
  world: "world"
  x: 0
  y: 80
  z: 0
  description: "所有玩家默认可见的主城锚点。"
  permission: ""                # 可见权限，留空则所有人可见
  sort-order: 0                 # 在地图 UI 中的排序
  unlock-currencies: []         # 解锁所需货币，留空则默认解锁
  teleport-currencies:          # 每次传送所需货币
    - currency: "money"
      amount: 10
  unlock-items: []              # 解锁所需物品

mine:
  enabled: true
  display-name: "矿洞入口"
  world: "world"
  x: 128
  y: 64
  z: -42
  description: "演示货币 + 物品解锁。"
  permission: ""
  sort-order: 10
  unlock-currencies:
    - currency: "money"
      amount: 200
    - currency: "points"
      amount: 5
  teleport-currencies:
    - currency: "money"
      amount: 30
  unlock-items:
    - amount: 2
      matcher:
        material-ids:
          - "ender_pearl"
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `enabled` | boolean | `false` 时该锚点不加载 |
| `display-name` | string | 在 UI 中显示的名称，支持颜色代码 |
| `world` | string | 所在世界名 |
| `x/y/z` | int | 传送坐标 |
| `description` | string | 锚点描述文本 |
| `permission` | string | 可见权限，留空则无限制 |
| `sort-order` | int | UI 排序（升序） |
| `unlock-currencies` | list | 解锁所需货币，`currency` 对应 `currencies` 节键名，`amount` 为数量 |
| `teleport-currencies` | list | 每次传送消耗的货币 |
| `unlock-items` | list | 解锁所需物品（`amount` + `matcher.material-ids`） |

### 路径点配置

```yaml
waypoints:
  enabled: true
  default-style-id: "default"   # 路径点导航样式
  id-prefix: "AXS-map-wp-"     # 路径点 ID 前缀
  auto-name-prefix: "标记点"    # 自动命名前缀
  default-max-count: 5          # 玩家默认最多可创建的路径点数
  limits:                       # 按权限设置更高上限
    - permission: "ArcartXSuite.map.admin"
      max-count: 20
```

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs map status` | 查看地图模块、世界、锚点和路径点状态 |
| `/axs map reload` | 重载地图配置和 UI |
| `/axs map open <玩家> [世界名]` | 为在线玩家打开地图界面，可选指定世界 |
| `/axs map list` | 列出所有已配置的地图世界 |
| `/axs map anchors [世界名]` | 列出全部或指定世界的锚点 |

### 玩家命令（权限：`arcartxsuite.map.use`，别名 `/axmap`）

| 命令 | 说明 |
| --- | --- |
| `/map` 或 `/map open [世界名]` | 打开地图界面，不指定世界时显示当前所在世界 |
| `/map hud [on\|off\|toggle]` | 控制小地图 HUD 显示，默认 `toggle` 切换 |
| `/map cleartrack` | 清除地图上正在追踪的目标点 |

## UI / Packet

| 功能 | UI ID | 说明 |
| --- | --- | --- |
| 全屏地图 | `AXS:map_menu` | 服务端推送世界数据、锚点列表、玩家路径点和当前位置；客户端点击锚点/路径点回包 |
| 小地图 HUD | `AXS:map_hud` | 服务端按玩家移动/传送事件推送坐标和追踪目标；`packet-id: "AXS_MAP"` |

### 地图 Packet 主要字段

| 字段 | 来源 | 说明 |
| --- | --- | --- |
| `world` | 玩家当前世界 | 当前显示的世界 ID |
| `anchors` | 锚点配置 | 该世界所有对玩家可见的锚点列表 |
| `waypoints` | 数据库 | 玩家自己创建的路径点列表 |
| `player.x/y/z` | 玩家坐标 | 玩家当前位置，用于地图光标定位 |
| `tracking` | 追踪状态 | 当前追踪目标的坐标（来自 QuestGPS 或手动标记） |
