# RGB 渐变文本

## 功能定位

通过 PlaceholderAPI 输出渐变 / 扫光效果文本，供聊天前缀、Tab 列表、称号显示、计分板或任何支持 PAPI 的场景调用。`text` 字段自身可嵌套其他 PAPI 变量，先解析再叠加渐变。

### 核心特性

- **逐字渐变**：文本按字符分配颜色梯度，支持两色到任意多色渐变（如红 → 黄 → 蓝）
- **扫光效果**：在渐变基础上叠加动态扫光高亮，可配置扫光颜色、宽度和强度
- **动画帧切换**：每隔 `switch-interval-ticks` tick 切换到下一帧，形成动态流光效果
- **PAPI 嵌套**：`text` 会先按目标玩家解析 PlaceholderAPI（如 `%player_name%`），再叠加 RGB 渐变
- **多条目管理**：支持任意数量条目，每个条目独立配置颜色、扫光和速度
- **Shimmer 函数**：提供 `ArcartRGB.rgb(text, shine, color1, color2, ...)` 工具函数，可在 UI YAML Shimmer 脚本中直接调用

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 注册 Shimmer 工具函数 `ArcartRGB.rgb(...)` | Shimmer 函数不可用，UI 侧无法调用 |
| 必需 | PlaceholderAPI | 注册 `%arcartrgb_*%` 占位符，并解析 `text` 中的嵌套 PAPI | 模块不会加载，占位符输出不可用 |
| 可选 | Chat、Tab、Title 等 | 调用 `%arcartrgb_*%` 展示渐变文本 | RGB 本身可用，只是没有对应展示入口 |

## 启用步骤

```yaml
modules:
  rgb:
    enabled: true
```

## 配置

### 主配置（`ArcartXRGB.yml`）

```yaml
settings:
  debug: false

  # Shimmer 函数 ArcartRGB.rgb(...) 的默认动画参数，条目级可覆盖。
  # 格式: ArcartRGB.rgb('文本', true, '#FF7A18', '#FFD64D', '#7FE7FF')
  shimmer-switch-interval-ticks: 2
  shimmer-shine-width: 2
  shimmer-shine-color: "#FFFFFF"
  shimmer-shine-strength: 0.55

# 条目目录，相对模块数据目录。
# 目录下每个 *.yml 文件可包含多个条目，根键即为条目 ID。
entries-directory: "entries"
```

### 条目字段详解

条目文件位于 `data/rgb/entries/*.yml`，同一文件可包含多个定义：

```yaml
# data/rgb/entries/default.yml
welcome:
  enabled: true
  text: "欢迎来到 ArcartX，%player_name%"   # 支持 PAPI 变量，先解析后渐变
  gradient-colors:                            # 至少两色，按字符分配颜色梯度
    - "#FF7A18"
    - "#FFD64D"
    - "#7FE7FF"
  shine: true                                 # 是否叠加扫光动画
  switch-interval-ticks: 2                    # 每 N tick 切换一帧（留空则用 settings 全局值）
  shine-width: 2                              # 扫光宽度（字符数）
  shine-color: "#FFFFFF"                      # 扫光颜色
  shine-strength: 0.55                        # 扫光强度 0.0~1.0，越大越亮

momo:
  enabled: true
  text: "多请墨大师喝奶茶，谢谢"
  gradient-colors:
    - "#6A5CFF"
    - "#FF6B9D"
  shine: false
  switch-interval-ticks: 3
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | ✅ | `false` 时不注册该条目的 PAPI |
| `text` | string | ✅ | 要渐变的文本，支持颜色代码和 PAPI 变量 |
| `gradient-colors` | list | ✅ | 至少两个十六进制颜色，按字符分配 |
| `shine` | boolean | ❌ | 是否启用扫光，默认 `false` |
| `switch-interval-ticks` | int | ❌ | 帧切换速度，不填则用 `settings.shimmer-switch-interval-ticks` |
| `shine-width` | int | ❌ | 扫光宽度（字符数），不填则用全局值 |
| `shine-color` | string | ❌ | 扫光颜色，不填则用全局值 |
| `shine-strength` | double | ❌ | 扫光强度 0.0~1.0，不填则用全局值 |

## PAPI

前缀：`%arcartrgb_*%`（**必须安装 PlaceholderAPI**）

| 占位符 | 说明 |
| --- | --- |
| `%arcartrgb_<条目ID>%` | 输出对应条目的当前渐变帧文本 |

示例：`%arcartrgb_welcome%` → 渲染 `welcome` 条目 `text` 的渐变动画当前帧

### 典型使用场景

| 场景 | 写法 |
| --- | --- |
| 聊天前缀 | Chat 模块 `prefix: "%arcartrgb_vip_prefix%"` |
| Tab 列表名称 | Tab 定义 `pack.name: "%arcartrgb_server_name%"` |
| 称号显示名 | Title 定义 `display-name: "%arcartrgb_title_hero%"` |
| UI Shimmer 脚本 | `ArcartRGB.rgb(var.someText, true, '#FF7A18', '#FFD64D')` |

## Shimmer 函数

在 UI YAML 的 Shimmer 脚本（`|-` 块）中使用：

```
ArcartRGB.rgb('文本', shine, color1, color2, ...)
```

| 参数 | 说明 |
| --- | --- |
| `'文本'` | 要渲染的字符串，可以是变量 |
| `shine` | `true` / `false`，是否启用扫光 |
| `color1, color2, ...` | 十六进制颜色，至少两个 |

示例：
```
ArcartRGB.rgb(var.playerName, true, '#FF7A18', '#FFD64D', '#7FE7FF')
```

颜色参数会覆盖 `settings` 中的全局颜色，但扫光宽度/强度/速度仍使用 `settings` 的全局值。

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs rgb status` | 查看 RGB 模块状态和已加载条目数 |
| `/axs rgb reload` | 重载全部渐变条目和 Shimmer 全局参数 |

## UI / Packet

RGB 模块本身不直接发包，输出通过以下两个渠道调用：

| 渠道 | 说明 |
| --- | --- |
| PAPI `%arcartrgb_<id>%` | 服务端任意支持 PAPI 的字段均可使用 |
| Shimmer 函数 `ArcartRGB.rgb(...)` | 仅限 ArcartX UI YAML 的 Shimmer 脚本块内 |

动画帧由服务端每隔 `switch-interval-ticks` tick 自动推进，PAPI 每次被解析时返回当前帧的渐变文本。
