# 运行时安全检测详解

ArcartXSuite 在启动时会执行**四步保护流水线**的安全检测（Step 4），目的是帮助服主和服务商尽早发现 Jar 篡改、调试器连接等潜在风险。本文详细解释控制台警告的含义、授权后台的异常分级，以及如何正确处理各类安全提示。

---

## 检测维度一览

`JarIntegrityVerifier` 启动时通过 `verify()` 检查五个维度，结果以 `integrityFlags`（位掩码）上报给授权服务器：

| 位掩码 | 名称 | 检测内容 | 严重程度 | 是否需要处理 |
|---|---|---|---|---|
| `0x01` (1) | `TAMPERED` | Jar 完整性校验失败（SHA-256 摘要不符） | **critical** | **必须处理** |
| `0x02` (2) | `AGENT_DETECTED` | 检测到非预期的 Java Agent（`-javaagent`） | **critical** | **必须处理** |
| `0x04` (4) | `DEBUG_ATTACHED` | 检测到调试器参数（`-Xdebug`/jdwp） | warning | 生产服建议处理 |
| `0x08` (8) | `ATTACH_OPEN` | JVM Attach API 未关闭 | info | 按需选择 |
| `0x10` (16) | `NATIVE_ALERT` | Native（C++ JNI）侧环境告警 | **critical** | **必须处理** |

> 控制台日志格式：`[AXS-Security] 安全警告: [Agent注入] [Attach未封锁]`  
> 授权后台格式：`INTEGRITY_AGENT`、`INTEGRITY_TAMPERED` 等独立异常项。

---

## 各维度详解

### 1. Jar 篡改检测（TAMPERED）

构建流水线（`build.gradle.kts → EmbedIntegrityTask`）在每次编译后会计算 Jar 中所有 `.class` 文件的 SHA-256 摘要，写入 `META-INF/axs-integrity.bin`。启动时 `JarIntegrityVerifier` 重新计算摘要并与期望值对比。

**出现此警告意味着：**
- Jar 被人为修改（反编译后重打包）
- 传输过程中文件损坏
- 构建产物被篡改（建议使用官方渠道下载）

**处理建议：** 立即停止使用该 Jar，从官方源重新下载并核对 MD5/SHA。

### 2. Agent 注入检测（AGENT_DETECTED）

遍历 JVM 启动参数中的 `-javaagent`、`-agentlib`、`-agentpath`。大部分 Agent 都是调试/监控工具，但在生产环境中意味着**任何人都可以通过 Attach 注入任意字节码**，对付费模块构成破解风险。

**合法白名单：**

| Agent 名称 | 用途 | 是否已豁免 |
|---|---|---|
| `classfinal-agent.jar` | ClassFinal VMP 解密（AXS 构建流水线 Step 3） | ✅ 已豁免 |
| `authlib-injector.jar` | authlib-injector 外置登录代理 | ✅ 已豁免 |

**出现此警告意味着：** 存在**不在白名单内**的 Agent，需要排查是否为自己添加的监控/调试工具，或是否存在恶意注入。

> ⚠️ **重要**：AXS 的 `AuthlibInjectorManager` 会自动生成 `start-mixed-auth.bat/sh` 脚本，其中包含 `-javaagent:plugins/ArcartXSuite/authlib-injector.jar=...`。authlib-injector 是**合法的、必需的**外置登录代理，已经加入白名单，不应被误判。

### 3. 调试器检测（DEBUG_ATTACHED）

检查 `-Xdebug`、`-Xrunjdwp`、`-agentlib:jdwp` 等参数。启动时带有调试器意味着**远程断点可以暂停服务器主线程**。

**处理建议：**
- 本地开发测试：可忽略
- 生产服务器：从启动脚本中移除调试参数

### 4. Attach API 封锁检测（ATTACH_OPEN）

检查是否启用了 `-XX:+DisableAttachMechanism`。Attach API 是 JVM 自带的**热注入接口**，任何拥有服务器 OS 权限的用户都可以通过 `jmap`/`jstack`/VisualVM 等工具抓取内存、注入 Agent、修改字节码。关闭后彻底阻断此类攻击路径。

**处理建议：**
- 在意安全的服务器：启动参数加 `-XX:+DisableAttachMechanism`
- 需要随时 `jstack`/`jmap` 诊断的运维环境：可不加（属于 info 级别，不触发告警）
- 不会影响 authlib-injector 等**启动时加载**的 Agent

```bash
# 示例：混合登录安全启动脚本
java -XX:+DisableAttachMechanism -javaagent:plugins/ArcartXSuite/authlib-injector.jar=https://littleskin.cn/api/yggdrasil?mixed -jar server.jar nogui
```

### 5. Native 告警（NATIVE_ALERT）

`NativeBridge`（C++ JNI 层）在检测到可疑环境时通过 `environmentCheck()` 返回标志位。目前覆盖的场景较少，但一旦触发通常是严重的底层篡改。

---

## 授权后台异常分级说明

心跳服务（`HeartbeatService`）每 3 分钟向授权服务器发送一次快照，其中包含 `integrityFlags`。服务端不再将"任何非零 flags"统一判定为 `INTEGRITY_VIOLATION`，而是**按位拆分、独立评级**：

| 服务端异常类型 | 触发条件 | 严重程度 | 是否进告警队列 |
|---|---|---|---|
| `INTEGRITY_TAMPERED` | `flags & 1 != 0` | critical | ✅ 是 |
| `INTEGRITY_AGENT` | `flags & 2 != 0` | critical | ✅ 是 |
| `INTEGRITY_NATIVE` | `flags & 16 != 0` | critical | ✅ 是 |
| `INTEGRITY_DEBUG` | `flags & 4 != 0` | warning | ❌ 否 |
| `INTEGRITY_ATTACH` | `flags & 8 != 0` | info | ❌ 否 |

这意味着：
- **纯 Attach 未封锁**（`flags = 8`）不再显示红色的 `INTEGRITY_VIOLATION`，后台只会记录一条 info 级别的 `INTEGRITY_ATTACH`
- **调试器连接**（`flags = 4`）降级为 warning，不会触发机器人告警推送
- 真正的篡改和非法 Agent 仍保持 critical，继续进告警队列

---

## 常见 Q&A

**Q：我服务器用了某注入型插件，会被判定为非法吗？**  
A：如果该插件以 `-javaagent` 方式注入 JVM，且**不在白名单内**（目前只有 `classfinal` 和 `authlib-injector`），`AGENT_DETECTED` 会触发 critical 告警。建议将其名称反馈给 AXS 开发团队评估是否加入白名单，或者改用普通 Bukkit 插件形式加载。

**Q：控制台显示 `[AXS-Security] 检测到非预期的 Java Agent: -javaagent:...`，但我确认这个 Agent 是安全的。**  
A：请核对 Agent 文件名是否包含 `classfinal` 或 `authlib-injector` 关键字（白名单匹配规则）。如果确认是合法工具，可以在 `JarIntegrityVerifier.checkAgentInjection()` 的白名单条件中添加对应关键字，然后重新构建。

**Q：`[Attach未封锁]` 会导致功能异常吗？**  
A：不会。这只是安全建议，不影响任何游戏功能或授权验证。从 `1.2.0-beta` 起已降级为 info，不会在授权后台产生红色异常。

**Q：启动脚本里加了 `-XX:+DisableAttachMechanism` 后，authlib-injector 还能用吗？**  
A：可以。`DisableAttachMechanism` 关闭的是**运行时动态 attach**，authlib-injector 作为**启动时通过 `-javaagent` 加载**的 Agent 完全不受影响。

**Q：我是服主，如何查看当前服务器的 `integrityFlags`？**  
A：启动日志中 `JarIntegrityVerifier.summary()` 会打印完整的警告摘要。精确 flags 值不会直接打印到控制台，但会随心跳上报到授权后台。联系授权管理员可以查询历史心跳记录。

---

## 快速检查清单

启动前自查：

- [ ] Jar 来源可信（从官方渠道下载，核对 SHA）
- [ ] 没有多余的 `-javaagent`（除 authlib-injector / classfinal 外）
- [ ] 生产环境已移除 `-Xdebug`/`-agentlib:jdwp`
- [ ] 在意安全的服务器已加 `-XX:+DisableAttachMechanism`
- [ ] `authlib-injector` 通过官方脚本启动（`start-mixed-auth.bat` 或 `start-secure.bat`）
