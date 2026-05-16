# 命令速查

本页列出 ArcartXSuite 全部可用命令及其用法，分为**管理命令**（服务器管理员/OP 使用）和**玩家命令**（普通玩家使用）两部分。

> **约定**：`<参数>` 表示必填，`[参数]` 表示选填，`A|B` 表示二选一。

---

## 管理命令

主入口：`/axs`（别名 `/arcartxsuite`），需要权限 `arcartxsuite.admin`（默认 OP）。

### 全局管理

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs` | 查看全部模块运行状态，等同 `/axs status` | `/axs` |
| `/axs status` | 以列表形式展示所有模块的启用/禁用状态和运行信息 | `/axs status` |
| `/axs help [模块名]` | 查看帮助。不指定模块时列出所有模块概览；指定模块时显示该模块的详细命令用法 | `/axs help title` |
| `/axs reload all` | 按依赖顺序重载全部已启用模块的配置、UI 和服务，适用于修改配置后刷新 | `/axs reload all` |
| `/axs reload <模块名>` | 仅重载指定模块的配置。常用于只改了某个模块的 YAML 后快速生效 | `/axs reload mail` |
| `/axs <模块名> status` | 查看单个模块的状态详情，包括加载的配置数量、数据库连接状态等 | `/axs entitytracker status` |
| `/axs license status` | 查看授权状态、QQ、已解锁模块、授权入口、代理状态、缓存状态和每个授权码的诊断结果 | `/axs license status` |
| `/axs license refresh` | 刷新当前服务器绑定的授权票据。不会消耗换绑次数，适合改完 `license.yml` 后手动同步 | `/axs license refresh` |
| `/axs license activate` | 主动激活当前服务器，把 `qq + keys + install_id + 机器指纹` 绑定到授权中心 | `/axs license activate` |
| `/axs license rebind` | 显式把授权码换绑到当前服务器，会消耗该授权码的自助换绑次数/冷却 | `/axs license rebind` |
| `/axs license cloud-code` | 生成云端网页换绑挑战码，用于证明你控制新目标服务器 | `/axs license cloud-code` |
| `/axs license fingerprint` | 输出当前服务器机器指纹、localSaltHash 和参与指纹计算的组件，用于授权诊断 | `/axs license fingerprint` |

合法的 `<模块名>` 共 17 个：

```
announcer, entitytracker, combateffect, eventpacket,
chat, conversation, loginview, mail, onlinerewards,
pickup, prop, rgb, tab, title,
map, questgps, warehouse
```

---

### 授权命令说明

授权命令用于排查和管理 `plugins/ArcartXSuite/license.yml` 中的 QQ + 授权码配置。当前付费模块为 `warehouse`、`map`、`mail`、`title`、`questgps`、`conversation`、`tab`，免费模块只受 `config.yml` 的 `modules.<module>.enabled` 控制。

| 命令 | 什么时候使用 | 关键输出 |
| --- | --- | --- |
| `/axs license status` | 日常检查授权是否生效、哪些模块已解锁、网络是否走代理 | `状态`、`原因`、`QQ`、`Subject`、`模块`、`使用缓存`、`授权入口`、`代理`、`预检`、`最后操作`、`成功入口`、`授权码结果` |
| `/axs license refresh` | 授权码已绑定本服务器，需要重新向 Worker 验证并刷新缓存 | 本次请求的入口、是否成功、失败原因 |
| `/axs license activate` | 首次绑定、缓存过旧、或出现 `BINDING_NOT_FOUND` 时手动激活 | 成功后会写入新的 `security/license.cache` |
| `/axs license rebind` | 授权码已经绑定到另一台服务器或旧机器指纹，且你确认要迁移到当前服务器 | 成功后旧绑定失效；失败时会显示换绑次数或冷却原因 |
| `/axs license cloud-code` | 需要使用云端网页换绑，且旧服务器不可用或不想消耗服务器内换绑次数 | 输出 10 分钟有效的一次性 `challengeCode` |
| `/axs license fingerprint` | 对比后台记录、排查机器指纹不匹配、确认本地 salt 是否变化 | `hash`、`localSaltHash` 和各指纹组件 |

常见授权错误：

| 错误 | 含义 | 处理方式 |
| --- | --- | --- |
| `MISSING_QQ` | `license.qq` 未填写 | 填写授权所属 QQ |
| `MISSING_LICENSE_KEYS` | `license.keys` 为空 | 写入至少一个授权码 |
| `QQ_MISMATCH` | 授权码不属于当前 QQ | 检查 QQ 或换成该 QQ 名下的码 |
| `LICENSE_CODE_NOT_FOUND` | 授权中心不存在该授权码 | 检查是否填错，或确认是否发到远程 D1 |
| `LICENSE_CODE_NOT_ACTIVE` | 授权码已停用 | 后台启用或重新发码 |
| `LICENSE_CODE_EXPIRED` | 授权码已过期 | 后台延长有效期或重新发码 |
| `BINDING_NOT_FOUND` | 当前授权码还没有绑定本服务器 | 执行 `/axs license activate` |
| `BOUND_TO_OTHER_INSTALL` | 授权码已绑定其他服务器或旧机器指纹，常见于删除/重建 `security/local-salt.dat` 后 | 确认迁移后执行 `/axs license rebind`；如果是误删 salt，优先恢复旧 `local-salt.dat` |
| `REBIND_QUOTA_EXHAUSTED` | 自助换绑次数不足 | 后台补换绑次数或管理员删除绑定 |
| `REBIND_COOLDOWN_ACTIVE` | 换绑冷却中 | 等待冷却结束或后台重置冷却 |
| `NETWORK_ERROR` | 授权入口不可达 | 检查服务器是否能访问 `axs.021209.xyz`，再检查 Cloudflare Workers 兜底入口或临时代理配置 |

---

### 模块管理命令

以下命令均以 `/axs <模块名>` 为前缀，仅管理员可用。每个模块都自带 `status` 和 `reload` 子命令，下面只列出各模块的**特有动作**。

#### EntityTracker（Boss 追踪 / 伤害排行）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs entitytracker sessions [mobId]` | 列出当前正在追踪的全部 Boss 会话。可选传入 `mobId` 来过滤只看某种 Boss | `/axs entitytracker sessions`<br>`/axs entitytracker sessions SkeletonKing` |
| `/axs entitytracker rank <实体UUID> [页码]` | 查看指定 Boss 实体的实时伤害排行榜，按伤害量降序。UUID 可从 `sessions` 命令输出中复制 | `/axs entitytracker rank 550e8400-e29b-41d4-a716 1` |
| `/axs entitytracker settlements [页码]` | 分页查看历史结算记录列表，每条记录包含 Boss 名称、击杀时间和参与人数 | `/axs entitytracker settlements 2` |
| `/axs entitytracker settlement <结算ID> [页码]` | 查看某次结算的详细排名信息，包括每位参与者的伤害值和获得的奖励 | `/axs entitytracker settlement abc123` |
| `/axs entitytracker reissue <结算ID> <名次> [玩家]` | 补发奖励。如果结算时某位玩家不在线导致奖励未送达，可用此命令补发。不指定玩家则发给原排名玩家 | `/axs entitytracker reissue abc123 1`<br>`/axs entitytracker reissue abc123 1 Steve` |

#### EventPacket（事件引擎 / 触发器）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs eventpacket fire <信号名> <玩家> [key=value...]` | 手动向指定玩家触发一个信号，可附带额外参数。常用于调试自定义触发器规则是否正确配置 | `/axs eventpacket fire quest_complete Steve quest-id=main_1`<br>`/axs eventpacket fire level_up Alex value=10` |

#### Announcer（公告 / 字幕播报）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs announcer status` | 查看模块状态，包括活跃公告数、字幕组数和待播队列 | `/axs announcer status` |
| `/axs announcer broadcast <文本>` | 将一条自定义广播加入队列，当前广播展示结束后立即播报，不受广播冷却限制 | `/axs announcer broadcast 服务器将于10分钟后维护` |
| `/axs announcer broadcastnow <文本>` | 立即广播，强制打断当前正在展示的公告 | `/axs announcer broadcastnow 紧急通知：服务器重启` |
| `/axs announcer subtitle list` | 列出所有已加载的字幕组 ID，用于确认字幕配置是否正确加载 | `/axs announcer subtitle list` |
| `/axs announcer subtitle play <玩家> <字幕组ID>` | 向指定玩家播放字幕动画，字幕组 ID 需在配置中预先定义 | `/axs announcer subtitle play Steve welcome_intro` |
| `/axs announcer subtitle stop <玩家>` | 立即停止指定玩家正在播放的字幕动画 | `/axs announcer subtitle stop Steve` |

#### Title（称号系统）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs title give <玩家> <称号ID> <时长>` | 向玩家发放一个称号。时长支持 `permanent`（永久）、`7d`（7天）、`12h`（12小时）、`30m`（30分钟）等格式 | `/axs title give Steve 勇者之证 permanent`<br>`/axs title give Alex 活动限定 7d` |
| `/axs title revoke <玩家> <称号ID>` | 收回玩家的指定称号，该称号将从玩家的拥有列表中移除 | `/axs title revoke Steve 勇者之证` |
| `/axs title open <玩家>` | 为指定在线玩家打开称号管理界面（AXUI），可用于管理员代替玩家操作 | `/axs title open Steve` |

#### LoginView（登录视图）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs loginview open <玩家>` | 为指定在线玩家打开登录视图界面，一般用于调试 | `/axs loginview open Steve` |
| `/axs loginview migrate-authme [dry-run]` | 从 AuthMe 数据库迁移密码哈希到 AXS 独立账户库。加 `dry-run` 参数只预览不执行，用于事先确认迁移数量 | `/axs loginview migrate-authme dry-run`<br>`/axs loginview migrate-authme` |
| `/axs loginview migration-commands` | 显示停用 AuthMe 后的安全操作步骤说明 | `/axs loginview migration-commands` |

#### Mail（邮箱系统）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs mail open <玩家>` | 为指定在线玩家打开邮箱收件箱界面 | `/axs mail open Steve` |
| `/axs mail admin` | 打开邮箱管理 UI，支持可视化新建/编辑/删除/发布邮件预设 | `/axs mail admin` |
| `/axs mail preset list` | 列出所有已加载的邮件预设，显示 ID、显示名、启用状态和附件数量 | `/axs mail preset list` |
| `/axs mail preset send <预设ID> <目标>` | 按预设向目标派发邮件。目标可以是玩家名、`all-online`（全部在线）或 `all-registered`（全部注册玩家） | `/axs mail preset send welcome Steve`<br>`/axs mail preset send update_notice all-online` |
| `/axs mail preset info <预设ID>` | 查看预设详细信息，包括标题、正文、附件、命令和启用状态 | `/axs mail preset info starter` |
| `/axs mail preset delete <预设ID>` | 删除预设（从内存和 YAML 文件） | `/axs mail preset delete old_event` |
| `/axs mail preset reload` | 重新从 YAML 加载所有预设 | `/axs mail preset reload` |
| `/axs mail cdk create <预设ID> <兑换码\|auto> <最大领取数> <有效期>` | 基于邮件预设创建 CDK 兑换码。`auto` 自动生成随机码，有效期如 `1d`、`7d`、`permanent` | `/axs mail cdk create gift_pack auto 100 7d`<br>`/axs mail cdk create vip_reward ABCD1234 1 permanent` |
| `/axs mail cdk info <兑换码>` | 查看 CDK 的绑定预设、已领取次数、过期时间和启用状态 | `/axs mail cdk info ABCD1234` |
| `/axs mail cdk list [页码]` | 分页查看当前所有已创建的 CDK 列表 | `/axs mail cdk list`<br>`/axs mail cdk list 2` |
| `/axs mail cdk delete <兑换码>` | 禁用并删除指定 CDK，已领取的不受影响 | `/axs mail cdk delete ABCD1234` |

#### Chat（聊天系统）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs chat mute <玩家> <时长> [原因]` | 禁言指定玩家。时长格式如 `30m`、`12h`、`7d`、`permanent`（永久） | `/axs chat mute Steve 7d 发送广告`<br>`/axs chat mute Griefer permanent` |
| `/axs chat unmute <玩家>` | 解除指定玩家的禁言状态 | `/axs chat unmute Steve` |
| `/axs chat spy <玩家> <on\|off>` | 开启或关闭对指定玩家的私聊监听，管理员可查看该玩家的私聊内容 | `/axs chat spy Steve on` |

#### OnlineRewards（在线奖励）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs onlinerewards add\|remove\|set <时长> <玩家>` | 修改玩家的在线时长。`add` 增加、`remove` 减少、`set` 设置为指定值。时长如 `30m`、`2h`、`1d` | `/axs onlinerewards add 2h Steve`<br>`/axs onlinerewards set 0m Steve` |
| `/axs onlinerewards card add\|remove\|set <数量> <玩家>` | 修改玩家的补签卡数量 | `/axs onlinerewards card add 3 Steve` |

#### Warehouse（仓库系统）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs warehouse open <玩家>` | 为指定在线玩家打开仓库 AXUI 界面 | `/axs warehouse open Steve` |
| `/axs warehouse info <玩家>` | 查看玩家的仓库概览信息，包括个人仓库使用量、共享仓库数等 | `/axs warehouse info Steve` |
| `/axs warehouse password <玩家> clear` | 清除玩家的二级密码。适用于玩家忘记密码的情况 | `/axs warehouse password Steve clear` |
| `/axs warehouse bank <玩家> <货币ID> <set\|add\|take> <金额>` | 管理玩家银行余额。`set` 设定、`add` 增加、`take` 扣除 | `/axs warehouse bank Steve gold add 1000`<br>`/axs warehouse bank Steve diamond set 50` |

#### Prop（道具脚本）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs prop set <道具ID>` | 将指定道具 ID 绑定到执行者的主手物品上，用于调试道具脚本或测试道具效果 | `/axs prop set magic_sword` |

#### QuestGPS（任务导航）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs questgps open <玩家>` | 为指定在线玩家打开任务导航界面 | `/axs questgps open Steve` |

#### Map（地图系统）

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/axs map open <玩家> [世界名]` | 为指定在线玩家打开地图界面，可选指定世界 | `/axs map open Steve`<br>`/axs map open Steve world_nether` |
| `/axs map list` | 列出所有已在配置中定义的地图世界 | `/axs map list` |
| `/axs map anchors [世界名]` | 列出全部锚点，或只列出指定世界的锚点。锚点是地图上的标记点 | `/axs map anchors`<br>`/axs map anchors world` |

---

## 玩家命令

以下命令面向普通玩家，无需管理员权限（但需要对应的 `arcartxsuite.<模块>.use` 权限节点，默认全部玩家可用）。

### Title — 称号（`/title`）

权限：`arcartxsuite.title.use`

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/title` 或 `/title open` | 打开称号管理菜单，可在界面中查看、装备、卸下和隐藏称号 | `/title` |
| `/title equip <称号ID>` | 装备指定称号，称号效果（前缀/后缀/属性）将立即生效 | `/title equip 勇者之证` |
| `/title unequip <组ID\|all>` | 卸下某个称号组中已装备的称号。使用 `all` 卸下全部已装备的称号 | `/title unequip 战斗`<br>`/title unequip all` |
| `/title hide <称号ID>` | 隐藏指定称号，隐藏后该称号在菜单中不显示但仍然拥有 | `/title hide 新手之证` |
| `/title unhide <称号ID>` | 取消隐藏，让该称号重新在菜单中显示 | `/title unhide 新手之证` |

### Warehouse — 仓库（`/warehouse`，别名 `/wh`）

权限：`arcartxsuite.warehouse.use`

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/warehouse` 或 `/wh` | 打开个人仓库界面。所有仓库操作（存/取/共享/银行）均在 AXUI 界面中完成 | `/wh` |

### Mail — 邮箱（`/mail`，别名 `/axmail`）

权限：`arcartxsuite.mail.use`

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/mail` 或 `/mail open` | 打开邮箱收件箱，查看收到的邮件 | `/mail` |
| `/mail compose` | 打开写信界面，可给其他玩家写信并附带物品附件 | `/mail compose` |
| `/mail claimall` | 一键领取所有未领取邮件中的附件和奖励 | `/mail claimall` |
| `/mail deleteall` | 删除所有已读邮件，清理收件箱 | `/mail deleteall` |
| `/mail cdk <兑换码>` | 使用兑换码领取对应的邮件奖励。兑换码由管理员创建 | `/mail cdk ABCD1234` |

### Chat — 聊天（`/chat`）

权限：`arcartxsuite.chat.use`

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/chat` | 查看当前聊天状态，包括所在频道、私聊开关、忽略列表等 | `/chat` |
| `/chat channel <频道ID>` | 切换到指定聊天频道，之后发送的消息会进入该频道 | `/chat channel Global`<br>`/chat channel Staff` |
| `/chat toggle private [on\|off]` | 开启或关闭私聊接收。关闭后其他玩家无法向你发送私聊消息 | `/chat toggle private off` |
| `/chat toggle mentions [on\|off]` | 开启或关闭 @提及通知。关闭后不会收到被 @ 的提醒 | `/chat toggle mentions off` |
| `/chat ignore <玩家>` | 屏蔽指定玩家，不再看到对方的聊天消息和私聊 | `/chat ignore Spammer` |
| `/chat unignore <玩家>` | 取消屏蔽，恢复接收对方的消息 | `/chat unignore Spammer` |
| `/chat socialspy [on\|off]` | 开启或关闭社交监听（需要权限），可查看其他玩家之间的私聊 | `/chat socialspy on` |

### 私聊与回复

| 命令 | 权限 | 说明 | 使用示例 |
| --- | --- | --- | --- |
| `/msg <玩家> <消息>` | `arcartxsuite.chat.msg` | 向指定在线玩家发送一条私聊消息 | `/msg Steve 你好，需要帮忙吗？` |
| `/reply <消息>` | `arcartxsuite.chat.msg` | 快速回复最近一次私聊你的玩家，无需再输入对方名字 | `/reply 好的，马上来` |

### OnlineRewards — 在线奖励（`/onlinerewards`，别名 `/signin`）

权限：`arcartxsuite.onlinerewards.use`

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/onlinerewards` 或 `/onlinerewards open` | 打开在线奖励菜单界面，可查看奖励进度和领取奖励 | `/onlinerewards` |
| `/onlinerewards status` | 在聊天中查看自己的在线时长统计（今日/本周/本月/总计）和签到状态 | `/onlinerewards status` |
| `/onlinerewards signin` 或 `/signin` | 进行今日签到。使用别名 `/signin` 可直接签到不打开菜单 | `/signin` |
| `/onlinerewards top <范围> [页码]` | 查看在线时长排行榜。范围可选：`daily`（日）、`weekly`（周）、`monthly`（月）、`total`（总计） | `/onlinerewards top daily`<br>`/onlinerewards top total 2` |

### QuestGPS — 任务导航（`/questgps`）

权限：`arcartxsuite.questgps.use`

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/questgps` 或 `/questgps open` | 打开任务导航菜单，可查看可用任务和追踪目标位置 | `/questgps` |
| `/questgps cleartrack` | 清除当前追踪的任务目标，HUD 导航箭头将消失 | `/questgps cleartrack` |
| `/questgps hud [on\|off\|toggle]` | 控制 HUD 导航显示。`on` 开启、`off` 关闭、`toggle` 切换状态（默认） | `/questgps hud off`<br>`/questgps hud` |

### Map — 地图（`/map`，别名 `/axmap`）

权限：`arcartxsuite.map.use`

| 命令 | 说明 | 使用示例 |
| --- | --- | --- |
| `/map` 或 `/map open [世界名]` | 打开地图界面。不指定世界时显示当前所在世界的地图 | `/map`<br>`/map open world_nether` |
| `/map hud [on\|off\|toggle]` | 控制小地图 HUD 的显示开关 | `/map hud off`<br>`/map hud` |
| `/map cleartrack` | 清除地图上正在追踪的目标点 | `/map cleartrack` |
