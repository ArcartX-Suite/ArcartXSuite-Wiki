# Tab 在线列表

## 功能定位

通过 ArcartX TAB UI 渲染自定义在线列表，支持排序、分组、PAPI 变量。

### 核心特性

- **三种排序模式**：
  - `name` — 按玩家名字母排序
  - `prem` — 按权限组排序，优先级列表自定义（如 admin > vip3 > vip2 > default）
  - `papi` — 按 PlaceholderAPI 变量排序（如 `%player_level%`），支持数字/文本排序
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

Tab 是付费模块，启用前需要先完成 `license.yml` 授权激活。

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

tabs:
  online-tab:
    ui-id: "ArcartXTab"
    packet-handler: "tab"
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs tab status` | 查看 Tab 模块状态 |
| `/axs tab reload` | 重载 Tab 配置并刷新在线玩家显示 |
