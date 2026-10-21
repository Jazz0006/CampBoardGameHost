# P4 独立审计：AI 设置职责与孤立旧规则删除（2026-10-21）

最新生产代码 `6a76a57cfa8034363abbf884d1a0326edc1c15a4` 已包括 F1/F3 及接续 F2 修复。Cloud full 640（146 suites / 30 UI）、APK、原 R2 shell 通过；[完整 Android CI #4496](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39553241174) 与 [R2 #4072](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39553241344) 均 SUCCESS。下文 bc263581 / 629 / #4494 是 F1/F3 历史验收，不替代本批。

审计对象：`ce1c314c` → `f852b04f` 的全部生产改动、删除测试及实际消费者；同时审查尚未提交的路线/交接整理。没有把 CI 绿灯当作完整玩法证明。

## 结论与发现

### F1：配置异步恢复覆盖用户输入（中等，已修正）

原 App 和提取后的设置 owner 都先显示空/默认连接配置，后台才恢复 direct/gateway 值。表单当时已可编辑；恢复晚于输入时，恢复值会覆盖用户新输入。此缺陷在提取之前已存在，本批原样继承。

修正只增加一个读取完成门槛：两份连接配置读完前显示读取状态，不开放连接选择及字段编辑。默认操作模式同步读取，仍可设置。没有新持久字段、版本协议或管理框架。

独立验收：direct 与 gateway 各自未读完时不得出现可编辑字段；读取完成后修改模型/地址/token、删除 key 应作用于当前配置；默认模式点击应写入实际偏好存储。新增 `StorytellerAiSettingsUiTest` 3 项真实 Compose/Robolectric 交互。修正前 2 项失败于“输入框不应存在”，另一项偏好保存通过；修正后完整 `testFull + assembleDebug` **629 tests / 145 suites / 30 UI，0 failure/error/skipped**。组件测试不是设备 Keystore/IO 并发的全覆盖证明。

### F2：投毒者死亡未解除其持续中毒（高，基线既有，已在接续批次修复）

不能把删除的孤立 PoisonEffectLifecycle 测试视为 Session 已覆盖全部中毒时序。实际 Session 路径存在缺口：第二夜投毒者（7）毒目标（2）→ 小恶魔确认攻击（7）→ 投毒者真实死亡；目标（2）的 `poisoned` 仍为 true，后续同夜信息可能错误地按中毒处理。

用实际 `confirmNightPoison` → `confirmNightAttack` 临时集成断言已复现：期望来源死亡后目标不再中毒，实际断言失败。诊断测试在 `/workspace/work/poison-audit/PoisonSourceDeathAuditTest.kt`，仅通过临时 Gradle init 加入测试源；它没有被混入正式通过数量，也没有用 Ignore/改断言掩盖。

审查证据：`acceptDeterministicNightAttack` 的实际死亡 roster 更新与 `commitConfirmedPlayerDeath` 都只清死者自己的 poison 标记；没有清除此死亡来源的持续作用。相关实现与 `ce1c314c` 一致，本批删除的旧 helpers 没有生产消费者，故这不是本批引入的规则回归，但它阻止“全部中毒合同已验收”的结论。

接续修复已在唯一 Session 发布入口统一结束已确认 Death/Execution/RoleChange 来源的持续毒。复用现有 Poison(null) 与同一原子存储；夜间死亡、白天死亡/处决、Imp 继任导致的角色退出、重试/过期隔离及真实文件失败回滚/重读均有耐久回归。旧临时反例已迁入 `ClocktowerPoisonSourceExitTest`；旧“处决 Poisoner 后毒仍到 Dusk”的错误测试改为普通玩家处决仍维持到 Dusk、Poisoner 处决立即结束的两个合同。Cloud full **640 tests / 146 suites / 30 UI，0 failure/error/skipped**、APK 与原 R2 shell 成功。精确生产 SHA `6a76a57c` 已通过完整 Android CI #4496 / R2 #4072，未声明真机、所有规则或整项重构全部验收。

### F3：旧 AI 请求因凭据变化退出后保持 busy（中等，本批回归，已修正）

提取前 `aiToken/aiDirectModel` 是 composition 的局部计算值，异步闭包捕获的是旧值；提取后 `aiSettings.token/model` 会读取当前值，因而能真正识别请求期间的凭据变化。但原代码把 `liveAiAdviceBusy = false` 放在 stale 提前返回之后，同一 Host key 的请求因此可能一直显示处理中并阻止人工重试。

修正采用现有协程内的 `try/finally`：包括 450ms 合并等待、实际请求和所有提前退出，都释放仍属于当前 key 的 busy；key 已被后继请求替换时不动后继状态。随后仍重新检查 Session/step/phase/round/memory/mode/model/token，旧响应不进入建议或事实。没有新请求协议、持久字段、取消管理器或额外付费重试。

证据为前后实际闭包语义、两处返回和清理分支的完整 source review，以及现有 stale/retry 交互与完整回归；没有声称做了真实付费 provider 延迟注入。`23590475` 的读取门槛已通过独立 CI #4493/R2 #4069；最终 finally 修正 `bc263581` 已通过完整 CI #4494/R2 #4070 及 Cloud 629 回归/APK。

## 对最小实现合同的核对

- `StorytellerAiSettings` 只持应用连接偏好与加载/保存 UI 状态，不含玩家、对局、角色、时间线、当前游戏模式或 AI 执行状态；不是第二游戏状态。根部调用保证离开设置页不取消读取/延迟保存，凭据仍在原 Keystore store。
- 本局人工接管与新局默认模式仍分离；真实设置点击的持久化由新增交互测试验证。provider、model/token 的异步结果检查读当前设置对象，未把旧响应写入 Session。
- 两套被删旧规则无生产引用，相关 enum/context 一起删除，没有兼容包装或影子新旧实现。真实攻击/保护/士兵/市长/继任及中毒到期的现有 Session 测试保留；F2 已以实际 Session 和文件场景修复验证，不把不相关测试当作该场景证明。
- Session 私有死亡 roster helper 完全无调用，删除不改任何写入路径。
- `f852b04f` 本身已通过独立完整 Android CI #4492 与 R2 #4068；审计修正 `23590475` 已通过 CI #4493/R2 #4069，最终 `bc263581` 已通过 CI #4494/R2 #4070。P0/P5、两个超限文件、强制线索及真机/杀进程仍未全部结束，不能合并为“已完成重构”。

## 后续顺序

F2 的 Session 毒来源失效闭环已完成本地验证；核实精确代码 CI 后继续夜间 UI/机械职责、两个超限文件与全树归属审计。现有 #334/#335 保持 Draft，不合并/改 base/重写历史，不做付费模型调用。
