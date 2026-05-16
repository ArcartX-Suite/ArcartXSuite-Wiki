# 第一次启用流程

为避免一次开全部模块导致排查困难，<strong>强烈推荐“先单模块 → 再全量”</strong>的渐进流程。

## 推荐顺序

1. **`Announcer`** — 几乎零依赖，验证 ArcartX 桥接是否正常
2. **`EntityTracker`** — 验证 MythicMobs 联动和 HUD 注册
3. **`RGB`**（可选）— 验证 PlaceholderAPI 流程
4. **`Title`** — 引入数据库（SQLite），验证持久化
5. **`Chat`** / **`Mail`** — 业务量更大的模块，放最后

## 单模块启用步骤

以 `Announcer` 为例：

### 0. 先确认授权

免费模块可以直接启用；如果要测试 `warehouse`、`map`、`mail`、`title`、`questgps`、`conversation`、`tab` 这些付费模块，需要先完成授权激活。

1. 管理员在授权后台为 QQ 发放授权码。
2. 把 QQ 和授权码写入：

```txt
plugins/ArcartXSuite/license.yml
```

3. 执行：

```txt
/axs license activate
/axs license status
```

4. `license status` 显示 `VALID`，并且模块列表包含目标付费模块后，再启用对应模块。

云端 Worker 会把授权码绑定到当前服务器的 `install_id + 机器指纹 + local-salt.dat`。迁服或重装前请备份 `plugins/ArcartXSuite/license.yml` 和 `plugins/ArcartXSuite/security/`。

### 1. 改 config.yml

```yaml
modules:
  announcer:
    enabled: true
```

### 2. 改模块配置（可选）

```yaml
# plugins/ArcartXSuite/ArcartXAnnouncer.yml
entries:
  welcome:
    enabled: true
    text: "欢迎来到 ArcartX 测试服 — 你好，%player_name%。"
    click-command: "say <player> 点了公告"
```

### 3. reload 并验证

```
/axs announcer reload
/axs announcer status
```

### 4. 进游戏看 HUD

带 ArcartX 客户端的玩家进入服务器，几秒后 HUD 应滚出公告文本。

| 现象 | 排查 |
| --- | --- |
| 控制台 `bridge missing` | ArcartX 服务端插件没装 |
| UI 注册成功但 HUD 不出现 | 客户端 MOD 没装 |
| HUD 出现但文字不对 | YAML 缩进/引号问题 |

## 全开

确认 1-2 个模块跑通后，逐个打开其他模块：

```
/axs reload all
/axs status
```
