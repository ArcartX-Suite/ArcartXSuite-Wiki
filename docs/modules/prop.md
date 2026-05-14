# Prop 快捷道具

## 功能定位

把物品标记为"快捷道具"，绑定 ArcartX 客户端按键效果和临时属性加成。

### 核心特性

- **道具定义**：在 `prop/props/*.yml` 中定义快捷道具，每个道具有独立的按键绑定、冷却、消耗和效果
- **按键触发**：绑定 ArcartX 客户端自定义按键，按下即触发道具效果
- **冷却与消耗**：每个道具可配置使用冷却和消耗条件
- **临时属性加成**：道具使用时可附加 MythicLib 临时属性（如攻击力、暴击率），效果结束后自动移除
- **提示文案**：自定义道具使用、冷却中、消耗不足等提示文本（`prop/language.yml`）
- **按键配置**：按键绑定定义在 `prop/key.yml`，可自定义客户端按键映射

## 依赖

- 必需：ArcartX
- 可选：MythicLib / AttributePlus（属性加成）

## 启用步骤

```yaml
modules:
  prop:
    enabled: true
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/AXS prop status` | 查看 Prop 模块状态和已加载道具列表 |
| `/AXS prop reload` | 重载 Prop 配置 |
| `/AXS prop set <道具ID>` | 把指定道具 ID 写入执行者的当前 Prop 状态，用于调试 |
