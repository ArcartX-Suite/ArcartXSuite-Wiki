# Tab 在线列表

::: tip 福利模块
Tab 属于福利模块，消费额度满 ¥300 或 找我购买高级会员 即可获得授权码。启用后仍需在 `license.yml` 中配置对应授权码。
:::

## 功能定位

通过 ArcartX TAB UI 渲染自定义在线列表，支持排序、分组、PAPI 变量、跨服。

### 核心特性

- **多视图 / 命令切换**：每个 tab 可绑定 `view` 标签，玩家通过 `/axstab view <name>` 在多套布局间切换
- **分组**：按 PAPI 表达式分桶，可在每组之前插入 `header-pack`
- **分页**：客户端 `Packet.send("TAB_PAGE", "next/prev/set", N)` 翻页，或 `/axstab page <id> <next|prev|N>`
- **跨服聚合**：`aggregate.enabled: true` 时每个服务器在 Tab 中只占一行（常用于网络总览视图）
- **退服宽限**：`settings.leave-grace-ms` 让玩家退服后短时间内仍出现在跨服快照中，避免跨服跳传时的"闪烁"
- **多键复合排序**：`sort-keys` 列表按优先级递减组合多个排序键，单键支持 `name` / `prem` / `papi` 三种模式
- **三种单键排序兼容写法**：
  - `name` — 按玩家名字母排序
  - `prem` — 按权限组排序，优先级列表自定义（如 admin > vip3 > vip2 > default）
  - `papi` — 按 PlaceholderAPI 变量排序（如 `%player_level%`），支持数字/文本排序
- **过滤器**：`filters.include` / `filters.exclude` 按 PAPI 表达式或权限节点过滤；`hide-vanished: true` 一键隐藏隐身玩家
- **置顶 / 置底**：`pinned.top` / `pinned.bottom` 把管理员、好友等强制锁在头部/尾部
- **灵活 pack 格式**：字符串、列表、字典三种模式，按每个玩家渲染 PAPI 后发给 UI
- **周期同步**：服务端按配置间隔（默认 20 tick）自动 diff 推送，客户端无需主动刷新
- **客户端刷新入口**：保留客户端 `Packet.send("TAB", "update")` 兼容入口，带限流保护
- **跨服玩家列表**：Redis Pub/Sub 或代理通道同步远程服务端玩家列表，超时自动移除过期快照
- **条目过滤**：支持 `max-entries` 限制条目数、`omit-blank-values` 跳过空值
- **称号前后缀集成**：pack 中直接引用 `%AXStitle_tab_<组ID>_prefix%` 等 Title 模块占位符

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 自定义 TAB UI、玩家条目包和客户端刷新 | 模块无法替换 TAB 界面 |
| 必需 | PlaceholderAPI | 渲染 pack、按 PAPI 排序和分组 | 模块不会加载或无法正确渲染动态变量 |
| 可选 | Redis 服务 | 多服在线列表同步 | 单服 TAB 正常，跨服玩家列表关闭 |
| 可选 | Title 模块 | 使用称号前后缀 PAPI 展示称号 | TAB 正常，称号前后缀变量按 PAPI 可用性决定 |

## 启用步骤

Tab 是福利模块，启用前需要先完成 `license.yml` 授权激活。

```yaml
modules:
  tab:
    enabled: true
```

## 关键配置（`ArcartXTab.yml`）

```yaml
settings:
  debug: false
  register-ui-on-enable: true
  overwrite-ui-file: false
  refresh-interval-ticks: 20

# Tab 定义目录，相对模块数据目录。
# 目录下每个 *.yml 文件为一个 Tab 定义，文件名（去掉 .yml）即为定义 ID。
tabs-directory: "tabs"
```

Tab 定义文件位于 `data/tab/tabs/*.yml`，文件名即定义 ID：

```yaml
# data/tab/tabs/online-tab.yml
enabled: true
ui-id: "tab"
packet-handler: "tab"
```

## UI / Packet

| 功能 | UI ID | 说明 |
| --- | --- | --- |
| 在线列表 | `tab` | 服务端按 `refresh-interval-ticks` 周期 diff 推送；客户端 `Packet.send("TAB", "update")` 触发一次强制重发（受 `client-refresh-guard` 限流） |

> UI 注册 ID 由 `ArcartXTab.yml` 中 `tabs.<id>.ui-id` 决定，默认 `tab`。模块内置 UI 文件为 `arcartx/ui/tab.yml`，启动时复制到 `plugins/ArcartXSuite/ui/tab.yml`。

## 排序、过滤与置顶

> 以下示例均为 `data/tab/tabs/<定义ID>.yml` 文件内的根级字段，不再写 `tabs:` 前缀。

### 多键复合排序 `sort-keys`

按优先级递减组合多个排序键，命中第一个键相同的项再按下一个键比较。填写 `sort-keys` 后，旧字段 `sort-mode` / `sort-papi-key` / `sort-papi-numeric` / `sort-prem-group` / `sort-descending` 全部失效。

```yaml
# data/tab/tabs/online-tab.yml
sort-keys:
  - { mode: prem, prem-group: [admin, vip3, vip2, vip1, default] }
  - { mode: papi, key: "%player_level%", numeric: true, order: desc }
  - { mode: name }
```

| 字段 | 适用模式 | 说明 |
| --- | --- | --- |
| `mode` | 全部 | `name` / `prem` / `papi` |
| `key` | papi | PlaceholderAPI 表达式，如 `%player_money%` |
| `numeric` | papi | 是否按数字解析后排序 |
| `prem-group` | prem | 优先级列表，未列出的玩家归 `default` |
| `order` | 全部 | `asc`（默认）/ `desc`；也可写 `descending: true` |

> 跨服模式（`cross-server: true`）下，远程节点只接收**首个 sortKey** 作为代表排序值，本地多键排序仅在本服内完整生效。

### 过滤器 `filters`

```yaml
# data/tab/tabs/online-tab.yml
filters:
  hide-vanished: false        # 一键隐藏隐身玩家
  include:                    # 任一命中保留；为空表示不过滤
    - { papi: "%player_world%", equals: "world" }
  exclude:                    # 任一命中剔除
    - { permission: "axs.tab.hide" }
    - { papi: "%player_gamemode%", equals: "SPECTATOR" }
```

- 规则字段：`papi` + `equals`（PAPI 模式）或 `permission`（权限模式），二选一。
- `equals` 留空时，PAPI 规则只判断"PAPI 渲染结果非空且不等于原表达式"，可用于探测变量是否存在。
- `invert: true` 反转单条规则判定（"不持有 / 不匹配"）。
- **隐身集成**：`hide-vanished: true` 同时检测以下来源，命中任一即隐藏：
  - Bukkit `player.metadata("vanished")`（绝大部分隐身插件都会写入）
  - `%essentials_vanished%` / `%supervanish_vanished%` / `%vanish_vanished%` / `%premiumvanish_vanished%`

### 置顶 / 置底 `pinned`

```yaml
# data/tab/tabs/online-tab.yml
pinned:
  top:
    - { permission: "axs.tab.pin-top" }   # 管理员置顶
  bottom:
    - { permission: "axs.tab.pin-bottom" }
```

命中 `top` 的玩家排在所有"中间层"之前；命中 `bottom` 的排在最后。三个分桶各自再用 `sort-keys` 内部排序，因此置顶后仍保持权限组/PAPI 排序结果。

## 视图、分组、分页

### 多视图 `view`

每个 tab 定义可以挂在一个 `view` 标签下（默认 `"default"`）。玩家通过 `/axstab view <name>` 切换当前视图，此时：

- 与玩家新 view 相同的 definitions 立刻重发；
- 其他 view 的 definitions 各发一个空 payload 用于清空 UI。

```yaml
# data/tab/tabs/list-default.yml
view: "default"
# ...

# data/tab/tabs/list-team.yml
view: "team"
# ...
```

玩家命令：

- `/axstab view`：查看当前 view
- `/axstab view <name>`：切换 view
- `/axstab refresh`：强制重发当前 view 的所有 tab

### 分组 `grouping`

按 PAPI 把已排序玩家分桶并在每组之前可选输出 `header-pack`。仅 string / list 形态的 pack 支持分组。

```yaml
# data/tab/tabs/online-tab.yml
grouping:
  enabled: true
  group-by-papi: "%vault_primary_group%"
  group-order: ["admin", "vip3", "vip2", "vip1", "default"]
  include-unordered: true
  header-pack: "&6=== {group} ==="
```

`{group}` 在 `header-pack`（无论 string / list / map / key 名）中被替换为当前组键。header 内的 PAPI 按该组第一个玩家上下文渲染。

### 分页 `pagination`

```yaml
# data/tab/tabs/online-tab.yml
pagination:
  enabled: true
  page-size: 80
  packet-id: "TAB_PAGE"
  next-action: "next"
  prev-action: "prev"
  set-action: "set"
```

客户端翻页：

```yaml
# 在 UI YAML 中给翻页按钮：
events:
  onClick:
    actions:
      - "[packet] TAB_PAGE,next"
# 或绝对页：
      - "[packet] TAB_PAGE,set,2"
```

服务端命令：`/axstab page <definitionId> <next|prev|N>`。页码越界会自动收敛到最后一页。

### 跨服聚合 `aggregate`

仅当 `cross-server: true` 时生效。启用后 Tab 不展开玩家，每个服务器只占一行。常用于跨大区 BungeeCord / Velocity 网络中的服务器总览视图。

```yaml
# data/tab/tabs/network-overview.yml
cross-server: true
aggregate:
  enabled: true
  line-pack: "&b{server-display} &7- &f{server-online} 人在线"
```

`line-pack` 支持以下花括号占位符：

| 占位符 | 含义 |
| --- | --- |
| `{server-id}` | 服务器节点 ID |
| `{server-display}` | 同 `server-id`（保留作为后续自定义渲染入口） |
| `{server-online}` | 当前节点在线人数 |

PAPI 仍按本服第一个在线玩家上下文渲染。

### 跨服增强：批节流与退服宽限

`settings.batch.window-ticks` 控制跨服快照广播的最小间隔（ticks，50ms/tick）。0 = 禁用节流；推荐 = `refresh-interval-ticks`，可避免玩家进退服触发的短时间快照风暴。

`settings.leave-grace-ms` 控制玩家退服后在跨服快照中保留虚拟条目的毫秒数。典型场景：玩家跨服跳传时旧服立刻把他从 Tab 移除、新服稍后才出现，造成短暂"消失"；启用宽限后旧服在该时间窗内仍保留该条目，等新服快照接管。

## 玩家头像（PlayerSkin 渲染）

Tab pack 中已内置 `{player_uuid}` 渲染变量，服务端**不抓 Mojang、不缓存皮肤**，玩家头像由 ArcartX 客户端 UI 端的 `PlayerSkin:<UUID>` 纹理表达式直接渲染，跨服同样适用。

服务端 pack 把 UUID 输出给 UI：

```yaml
# data/tab/tabs/online-tab.yml
pack:
  name: "%AXStitle_tab_adventure_prefix%%player_name%"
  uuid: "{player_uuid}"
  health: "%player_health%"
```

> **内置完整示例**：模块自带两套示例 UI（启用时自动复制到 `plugins/ArcartXSuite/ui/`），都在 `ArcartXTab.yml` 中以 `enabled: false` 形式提供，把对应 definition 切到 `true` 即可体验（建议同时关掉 `tabs.online-tab.enabled` 避免双 HUD）：
>
> - **`tab-rich`** + `tabs.demo`：头像 + PVP / vanish / ping 视觉风格的完整版"在线列表"形态。
> - **`tab-arena`** + `tabs.arena`：CS / LOL 风格的**双队 PVP 计分板**——红蓝左右分栏 / 常驻显示 / 按住 TAB 切换详细模式 / 队友战斗状态图标。队伍来源默认 `%player_scoreboardteam%`，可替换为自己 PVP 插件的 PAPI（`%bedwars_team%` / `%matrixduels_team%` 等）。UI 端按 `packet.get(i).team` 在客户端拆桶（不依赖服务端 grouping，因 map pack 在服务端 grouping 下会退化）。

UI YAML 端（`arcartx/ui/tab.yml`）在 `packetHandler` 中把 `packet.uuid` 赋给行控件的变量，Texture 控件再用 `PlayerSkin:` 渲染：

```yaml
controls:
  player_row:
    type: Canvas
    children:
      head:
        type: texture
        attribute:
          width: 24
          height: 24
          normal: "PlayerSkin:{var.uuid}"
      name_text:
        type: text
        attribute:
          texts: "{var.name}"
```

> `{player_uuid}` 是 Tab 模块的内置花括号占位符，**与 PlaceholderAPI 的 `%player_uuid%` 不同**。前者由服务端在调用 PAPI 之前替换，离线 / 无 PAPI 也可用；后者依赖 PAPI 注册。两者都可用，**推荐用 `{player_uuid}`** 减少一次 PAPI 调用开销。

### 内置示例：`tab-arena`（竞技场 / 枪战 PVP 计分板）

启用方式：将 `data/tab/tabs/arena.yml`（首次启动自动导出）中的 `enabled` 改为 `true`（建议同时把 `data/tab/tabs/online-tab.yml` 中的 `enabled` 改 `false`）。

**定义文件（`data/tab/tabs/arena.yml`）**：

```yaml
enabled: true
ui-id: "tab-arena"
packet-handler: "tab"
client-refresh-packet-id: "TAB"
client-refresh-action: "update"
sort-keys:
  - { mode: papi, key: "%player_scoreboardteam%" }
  - { mode: name }
pack:
  team: "%player_scoreboardteam%"   # 可替换为 %bedwars_team% / %matrixduels_team%
  uuid: "{player_uuid}"
  name: "%AXStab_pvp_color%%AXStab_vanish_color%%player_name%"
  status: "%AXStab_pvp%"            # 1=战斗中, 0=默认
  health: "%player_health%/%player_max_health%"
```

**UI 端要点（`arcartx/ui/tab-arena.yml`）**：

- **常驻显示**：`defaultOpen: true`，计分板始终在屏幕顶部；按住 TAB 切换"详细模式"（显示血量）。
- **双队分栏**：左红右蓝，`packetHandler` 按 `packet.get(i).team` 值决定 `copy` 到哪侧 VGrid：

```yaml
packetHandler:
  tab: |-
    // ...（清理旧条目）
    while(i <= packet.size()){
      if(val.red_player_row.width == 320){
        if(packet.get(i).team == "red"){
          ROW = val.red_player_row.copy('red_' + var.ArenaTAB刷新轮次 + '_' + i)
        } else if(packet.get(i).team == "blue"){
          ROW = val.blue_player_row.copy('blue_' + var.ArenaTAB刷新轮次 + '_' + i)
        }
        ROW['head'].normal = 'PlayerSkin:' + packet.get(i).uuid
        ROW['name_text'].texts = packet.get(i).name
        // status: "1" → 交战图标, "0" → 默认
        if(packet.get(i).status == "1"){
          ROW['status_text'].texts = "&c⚔"
        } else {
          ROW['status_text'].texts = "&7•"
        }
        ROW['health_text'].texts = "&c❤ " + packet.get(i).health
        ROW.visible = true
        i++
      }
    }
```

- **每行控件**：`head`（`PlayerSkin:` 头像）+ `name_text`（PVP / vanish 颜色前缀）+ `status_text`（战斗图标）+ `health_text`（仅详细模式 visible）。
- **队伍来源替换**：修改 `pack.team` 的 PAPI 表达式 + UI 端 `== "red"` / `== "blue"` 判定值即可适配任何 PVP 插件。

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs tab status` | 查看 Tab 模块状态 |
| `/axs tab reload` | 重载 Tab 配置并刷新在线玩家显示 |

玩家命令（无需 `arcartxsuite.admin`）：

| 命令 | 说明 |
| --- | --- |
| `/axstab view [name]` | 查看 / 切换当前 view |
| `/axstab page <id> <next\|prev\|N>` | 翻页 |
| `/axstab refresh` | 强制重发当前 view |
| `/axstab debug <player> [id]` | 排序 / 分组 / 状态调试快照（权限：`axstab.debug` 或 OP，控制台亦可） |
| `/axstab snapshot save <name>` | 把当前在线玩家 + 跨服快照落盘到 `data/tab/snapshots/<name>.json` |
| `/axstab snapshot load <name>` | 把存档注入为 `snapshot:<name>:<nodeId>` 虚拟节点，本服 viewer 立即可见（dev 用） |
| `/axstab snapshot unload <name\|all>` | 卸载该 name 对应的全部虚拟节点 |
| `/axstab snapshot list` | 列出已保存的快照 + 已注入的虚拟节点 |
| `/axstab snapshot delete <name>` | 删除存档文件（不影响已注入的虚拟节点） |

## PAPI 输出

在 `settings.papi.enabled: true` 后注册 `PlaceholderExpansion`，identifier 由 `settings.papi.expansion-id` 决定（默认 `AXStab`）。

按 definition 维度：

| 占位符 | 含义 |
| --- | --- |
| `%AXStab_<defId>_count%` | 本服该 definition 当前可见玩家数（已应用 filters / pinned / maxEntries） |
| `%AXStab_<defId>_total%` | 本服 + 所有跨服节点合计 |
| `%AXStab_<defId>_rank%` | 当前玩家在排序中的位次（1 起；不可见返回 0） |
| `%AXStab_<defId>_view%` | 当前玩家所在 view |
| `%AXStab_<defId>_page%` | 当前玩家在该 definition 的页码（0 起） |

按玩家维度（无 definition 前缀，受 `settings.style` / `settings.privacy` 控制）：

| 占位符 | 含义 |
| --- | --- |
| `%AXStab_ping%` | 当前玩家 ping（ms） |
| `%AXStab_ping_icon%` | 根据 `style.ping-icon.tiers` 选取的图标 |
| `%AXStab_pvp%` | 处于 PVP 高亮窗口返回 `1`，否则 `0` |
| `%AXStab_pvp_color%` | PVP 期间返回 `style.pvp-highlight.color`，否则空 |
| `%AXStab_vanished%` | 玩家隐身返回 `1`，否则 `0` |
| `%AXStab_vanish_color%` | 隐身且 `style.vanish-grey.enabled` 时返回灰色颜色码 |
| `%AXStab_uuid%` | `privacy.hide-uuid` 时返回空，否则返回 UUID |
| `%AXStab_ip%` | `privacy.hide-ip` 时返回空，否则返回 IP |

## 视觉风格 `settings.style`

不修改 pack 渲染流程，全部通过 PAPI 占位符执行：

- **pvp-highlight**：玩家造成或受到伤害后，`window-ms` 内 `%AXStab_pvp%` 返回 `1`，`%AXStab_pvp_color%` 输出 `color`，可用作前缀色覆盖。
- **vanish-grey**：识别 essentials / supervanish / vanish / premiumvanish 等隐身插件，命中则 `%AXStab_vanish_color%` 输出 `color`。
- **ping-icon**：按 `tiers`（升序 `max-ms`）匹配第一档作为 `%AXStab_ping_icon%` 输出；超出最大档时使用最末一档 icon。

## 隐私脱敏 `settings.privacy`

- `hide-uuid: true`：`%AXStab_uuid%` 返回空。
- `hide-ip: true`：`%AXStab_ip%` 返回空。

建议在 pack 中用 `%AXStab_uuid%` / `%AXStab_ip%` 替代 `%player_uuid%` / `%player_ip%`，以便集中开关脱敏。`{player_uuid}` 花括号占位符仍按原逻辑解析，不受 `hide-uuid` 影响（其用于 `PlayerSkin:` 头像渲染，需保留真实 UUID）。

## 调试

`/axstab debug <player> [definitionId]` 打印玩家当前 definition 的：

- 多键排序 `sort-keys` 的 numeric / string 值
- 分组键 `group-key`
- 是否命中 `pinned-top` / `pinned-bottom`
- `view` / `page` / `rank`
- 是否隐身、是否 PVP、ping
- `local-visible-count` / `total-visible-count`

未指定 `definitionId` 时输出全部 definition。控制台与玩家均可调用（需 `axstab.debug` 权限或 OP）。

### dry-run 模式

```yaml
settings:
  debug-tools:
    dry-run: false
```

开启后服务端**完整跑一遍渲染流程**（filters / sortKeys / pinned / grouping / pagination / PAPI 解析），但**不调用 `bridge.sendPacket(...)`**，客户端 Tab 保持上一次状态。配合 `settings.debug: true` 会逐 viewer 输出 `ArcartXTab[dry-run] skip send def=... viewer=...` 日志，用于：

- **生产排查**：玩家投诉显示异常时，开 dry-run + debug 观察服务端实际算出的 payload，不影响其他在线玩家。
- **配置预演**：改完排序键/grouping 后先 dry-run 看日志，确认正确再关闭重发。
- **性能基线**：单独测量"渲染计算耗时"vs"网络发包耗时"。

### snapshot 调试快照

存档目录：`plugins/ArcartXSuite/data/tab/snapshots/<name>.json`，格式 `version: 2`，包含：

- `localEntries`：本服每个 definition 的 `TabRemoteEntry` 列表（含 `sortValues` / `sortStringValues` / `groupKey` / `renderedPack`）。
- `remoteSnapshots`：当前已知的真实跨服节点快照（不包含 `snapshot:*` 虚拟节点，避免循环嵌套）。

`load <name>` 把存档注入为 `snapshot:<name>:local` + `snapshot:<name>:<原 nodeId>` 形式的**虚拟跨服节点**，与真实跨服节点一同进入 `buildCrossServerPayload` 渲染。`snapshot:*` 节点在 `cleanupStaleSnapshots` 中**豁免清理**，必须由 `unload <name>` / `unload all` 显式移除。

> **限制**：load 路径**不重新解析 PAPI**，使用存档落盘时的 `renderedPack`。这意味着存档中玩家的 `%player_health%` 等占位符值是落盘时刻的快照，不会跟随真实玩家变化。该行为用于"忠实复现"调试场景，不适合作为长期跨服数据源。

> **安全提示**：load 会让本服所有 viewer 看到存档玩家。请仅在测试服或受控环境使用，并在排查完成后及时 `unload`。
