# 当前交接 — F2 投毒来源退出修复（2026-10-21）

本批从 `2dea23bc` 接续 `refactor/minimal-night-flow-20261019` / Draft #335，base 仍为 #334 `76be7e17`。当前 F2 修复已通过 Cloud full/APK，精确提交/远端 CI 待推送核对；前一 `bc263581` 的 CI #4494/R2 #4070 只证明前一代码。接续先重新核对 HEAD、分支、工作区及远端状态。

## 本批实现

F2 已改为唯一 Session 发布入口的最小确定性后处理：确认的 Death/Execution/RoleChange 让原实际 Poisoner 死亡或退出身份时，活动目标毒标记与既有 Poison(null) 事实一并结束，再允许保存/发布。来源仍在、没有活动毒、其他玩家死亡或保护挡住攻击都不结束毒；单纯未配套确认事实的 raw state 编辑不制造新事实。

全部写者已核对：夜间 `confirmNightAttack → acceptDeterministicNightAttack → updateState`；白天 `confirmEndDay/killInDay → commitConfirmedPlayerDeath → commitConfirmedGameAction → updateState`；身份 `commitConfirmedActualRoleChange → commitConfirmedGameAction → updateState`。单一原子保存前完成，不新建框架、源状态字段或独立历史，不改旧 Observation。失败回滚/重复确认/过期隔离沿用 Session。

新增 9 项来源退出行为测试、1 项真实 AtomicFile 中断/重读测试；旧时间线测试保留并拆分普通玩家处决与 Poisoner 处决两种语义。原 8 项来源定向测试有 5 个先失败，修复后全部通过。最终 Cloud **640 tests / 146 suites / 30 UI，0 failure/error/skipped**，Debug APK、原 R2 shell 与 diff 检查通过。没有本批真机、杀进程或付费 API 验收。

本批生产 +33 行/+1904B，测试净 +187 行；删除文件 0。App 118544B、Night HostScreen 68496B 超限，Session 49453B 合规。完整证据见 [源台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 与 [审计 F2](MINIMAL_HOST_P4_REVIEW_2026-10-21.md)。

## 云端执行

```bash
source /workspace/.cloud-setup/activate.sh
./gradlew -I /workspace/work/cloud-tests.init.gradle :app:testFull :app:assembleDebug
```

当前 Cloud `/workspace/CampBoardGameHost` 直接读写/测试，无需 MiniMCP/Windows。SDK/Gradle 缓存和 JDK25 daemon 已准备；临时 init 把 Test JVM user.home 定向至 `/workspace/work/java-user` 并传递已有代理/CA，解决只读 home 的 Robolectric 锁文件。init 属当前环境中间文件，换环境按同样最小配置重建，不是产品构建依赖。Gradle 进程通信需带网络能力的执行权限；不要改 HOME 或写用户 home。

## 立即接续

先核实本批精确代码 CI/R2，然后继续 Night Host 的角色 actor/能力展示、实际确认与 AI 绑定职责及所有保存/恢复入口审计。两个超限文件先净删、再内聚整理，不引入 Context/Manager、机械拆文件或第二状态。随后完成逐角色/强制线索、P0/P5 全树与必要设备证据。

保持 D2 仅 Next 最终 Observation、Session 夜间进度/同页日结/Death 派生 Klutz、生产 AI 全链及双跨局防重复。#334/#335 继续 Draft；不合并/改 base/重写历史/删分支或调用付费 API。无新的所有者决策阻塞。
