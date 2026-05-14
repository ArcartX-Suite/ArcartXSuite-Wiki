# RGB 渐变文本

## 功能定位

通过 PlaceholderAPI 输出渐变 / 扫光效果文本。`entries.<id>.text` 自身可以再嵌套其他 PAPI。

### 核心特性

- **逐字渐变**：文本按字符分配颜色梯度，支持多色渐变（如红→黄→蓝）
- **扫光效果**：在渐变基础上叠加动态扫光高亮，可配置扫光颜色、宽度和强度
- **动画帧切换**：每隔 N tick 切换到下一帧，形成动态流光效果
- **PAPI 嵌套**：`entries.<id>.text` 会先按目标玩家解析 PlaceholderAPI（如 `%player_name%`），再叠加 RGB 渐变
- **多条目管理**：支持定义多个渐变条目，每个条目独立配置颜色、扫光和速度
- **Shimmer 函数**：提供 `ArcartRGB.rgb(...)` Shimmer 工具函数，可在 UI YAML 中直接调用
- **广泛适用**：输出可用于聊天前缀、Tab 列表、称号显示、计分板或任何支持 PAPI 的场景

## 依赖

- 必需：ArcartX、**PlaceholderAPI**

## 启用步骤

```yaml
modules:
  rgb:
    enabled: true
```

## 关键配置（`ArcartXRGB.yml`）

```yaml
entries:
  welcome:
    text: "欢迎来到服务器"
    colors:
      - "#FF0000"
      - "#00FF00"
      - "#0000FF"
    mode: gradient
```

## PAPI

前缀：`%arcartrgb_*%`（**必须安装 PlaceholderAPI**）

| 占位符 | 说明 |
| --- | --- |
| `%arcartrgb_<条目ID>%` | 渲染配置中定义的渐变文本条目。条目 ID 对应 `entries.<id>` |

示例：`%arcartrgb_welcome%` → 渲染 `entries.welcome.text` 的渐变文本

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/AXS rgb status` | 查看 RGB 模块状态 |
| `/AXS rgb reload` | 重载动态渐变配置 |
