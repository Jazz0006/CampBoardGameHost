# 当前交接 — 夜间继任与展示职责收敛（2026-10-21）

原分支 `refactor/minimal-night-flow-20261019` / Draft #335，base 仍为 #334 `76be7e17`。本批基线 `9ec8b969`，前一生产 F2 `6a76a57c` 已通过 full 640 / CI #4496 / R2 #4072。接续先核对本批实际 HEAD、远端和工作区，不能用前一 CI 代替新代码。

## 2026-10-21 P4 接续：夜间继任与角色展示职责收敛

基线 `9ec8b969`，原 Draft #335 / base #334 不变。已确认的来源退出 F2 保留，本批不修改毒效终止语义。

- **继任一份判定**：UI 自算存活 Imp、中毒、僧侣保护、存活人数、红唇女郎及可继任爪牙的代码已删除。既有 RoleIdentityBoundaries 的 Session 只读 `impSelfKillSuccession` 被 JudgeScreen 和最终攻击确认共同调用；底层保留现用 DemonSuccessionSemantics。UI 只把返回的座位映射到展示、保留未确认选择、控制 Next；确认时重新根据当前 Session 判定，无持久 pending、第二状态、新 schema 或通用候选机制。
- **角色一份展示**：投毒者、管家、间谍和已有共情者/占卜师/侍女 materializer 在两夜共享；Registry 仍只按 Session 提供的步骤顺序渲染，不从注册列表决定夜序。普通 actor 从 canonical alive/wakingRole 映射座位；死后触发仍用既有明确 override 和事件时点醉毒语义。
- **身份/草稿**：删去 UI 自建 roster/seed/poison 指纹，信息草稿 key 使用 gameId、Session state/input revision、稳定 interactionId。删除已无消费的 JudgeScreen gameSeed/gameOutcome 参数及 App 接线；Session outcome 仍阻止继续已结束游戏。D2 最后预览/唯一 Next Observation、真实玩家目标与 AI stale/receipt/记忆协议保持。
- **消费者与测试**：生产改动仅 App 接线、JudgeScreen、Session attack 与既有 RoleIdentityBoundaries。新增真实 Session 3 项（4 人/中毒红唇、僧侣保护、旧继任选择失效），扩充已有确认断言；新增真实 JudgeScreen 3 项（多爪牙选择不提前提交、强制红唇、不生效自杀无继任）。无生产或专属测试文件删除，无测试忽略。
- **精确净量**：生产 +104/-190，净删 **86 行 / 4651B**；测试 +138/-5，净 +133 行。App **118438B**、Night Host **63094B** 仍超 50000B，Session **48526B** 达标。没有声称本批解决所有超限文件。
- **验证**：Cloud `testFull + assembleDebug` **646 tests / 146 suites / 33 UI，0 failure/error/skipped**，原始 R2 shell 与 diff 检查通过；远端精确提交 CI/R2 待推送。本批无真机、杀进程或付费 API。首轮新测试失败来自测试前置 Spy 尚未确认、夹具已有 Scarlet Woman 的错误预期和弹窗/方桌重名选择器，未把它们记作生产 bug 或修复前反例。

下一步继续 Night 信息发布/AI 与保存恢复入口职责审计、两个超限文件按内聚职责缩减，再完成逐角色/强制线索、全树 P0/P5 和设备证据。

## 云端执行

Codex Cloud `/workspace/CampBoardGameHost` 直接读写/测试，不需要 MiniMCP 或 Windows。每个 shell 先 `source /workspace/.cloud-setup/activate.sh`；完整验证 `./gradlew -I /workspace/work/cloud-tests.init.gradle :app:testFull :app:assembleDebug`。Test JVM user.home 位于工作区，透传已有代理/CA，不改产品构建或 HOME；Gradle 通信需要执行环境网络能力。

本批日志 `/workspace/work/night-ownership/full.log`，原工作流 R2 shell 为 `/workspace/work/r2-current.sh`。仍需区分真机进程中断和 JVM AtomicFile 回归，禁止把后者当作前者验收。

无新增所有者决策阻塞。保留两剧本、实际 AI 全链和双跨局防重复；#334/#335 保持 Draft，不合并/改 base/重写历史/删分支/调用付费 API。
