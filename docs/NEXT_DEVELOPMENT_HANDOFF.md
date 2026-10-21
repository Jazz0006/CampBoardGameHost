# 当前开发交接（2026-10-21，已审计 main）

> **2026-10-22 UI 长期设计合同**：[HOST_TABLE_VISUAL_CONTRACT_2026-10-22.md](HOST_TABLE_VISUAL_CONTRACT_2026-10-22.md)。第一/二批 Day Light / Night Dark、善蓝恶红已通过 PR #340 合并 main `991d6344`，CI #4533/R2 #4101 SUCCESS。第三批酒鬼选择采用相同方桌、仅显示镇民虚线可选、单选后“选定”；AI 模式继续展示身份期间一次后台全局规划、展示后才处理酒鬼，自动模式不进入人工选择。现已进入实施和验收，最终 SHA/CI 待本轮真实完成后补充。数字手选回方桌尚未实施。共享 renderer/Theme 唯一，替换即删旧页面；不建第二个 GameState。

> **最终测试清理合并验收**：#338/#339 均已合并，最新已验证测试代码 main `4b9852cf5828c445b532550115aafbf2ddc8c4bf`，后续 #339 [Android CI #4528](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39604089426) / R2 #4098 成功，保留两项独有 AI 阻断和 5–15 人界面布局真实风险回归。最终静态 124 类/566 个 `@Test`，从 144 类/639 项净减 20 类/73 项。下一步仍应修复确定性信息和死者被提名的实际生产 Bug。下文各分支/旧 HEAD/待 CI 的信息视为审计过程记录。

> **已合并测试清理更新**：PR [#338](https://github.com/Jazz0006/CampBoardGameHost/pull/338) 经 CI #4524/#4525 和 R2 #4097 成功后已 squash merge 到 main `2dcf8a9745f487c3b4a0e30188fc67e3d58c5dfd`。高价值回归优先保留，低中价值的第一轮 22 类/75 项及 1 无用 helper 已删除；静态余 122 类/564 项。下文 `1cfacda8` 是原审计/工作起点、测试结果是历史基线，不是当前 main HEAD。生产 Host 固定信息和死者被提名两个缺陷仍待修正。

## 立即使用的真实基线

- 主线：`main` `6230b5959cf4b64e1c41f08bb48aa66460e13667`（独立审计起点；执行前重新检查远端）。
- #334/#335：已合并，最小整局阶段关闭；#337：已合并，全仓清理阶段关闭；#336：**仍 open Draft，内容与已合并 #337 重叠，不直接合并**。
- 最新经过全量代码验收的检查点：`e5ffc4e5e3ccdac5a5d4d3d7845f9754b998dbba`， [Android CI #4520](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977318) / [R2 #4095](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977316) SUCCESS；Cloud full 639 / 144 suites / 38 UI、Debug APK。main 上的后续文档提交不等于新 Android 代码测试。
- 开发机：Oracle VM 上 Mini MCP 仓库别名 `clocktower`；以实时 `git_state` 和远端为准，目标工作树是干净、与远端一致的 `main`。旧 Cloud / Windows 路径与临时日志都不是当前交接的环境要求。

## 独立审计结果与下一步

1. **先补最小 Host 确定性信息**：`ClocktowerDirectNumericResultUi.kt:26-56` 允许在缺少 Host typed 结果时直接选择任意数值；`clocktower/ui/ClocktowerHostScreen.kt:450-474` 只在已经有匹配 numeric 结果时绕过 AI。对 Chef/Empath 及有固定真值的其他角色，以 Session 唯一事实、能力是否正常、真实查询目标和登记裁量求结果，禁止把“常规唯一结果”留给 AI。Drunk/poisoned 与登记存在裁量时保留选择；人工误播可明确留痕，不能抹掉实际展示。
2. **修复提名目标 UI**：`ClocktowerDayNominationGesture.kt:19-31` 和 `clocktower/ui/ClocktowerDayHostScreen.kt:143-153` 将死者从被提名目标排除；规则允许存活玩家提名死者，只有发起人必须活着。Session `clocktower/session/ClocktowerDayFlow.kt:144-159` 未限制被提名者存活。修正 UI 和真实交互测试，切勿重建独立 Day 状态。
3. **之后有证据地验收**：D2 各角色/三模式、Slayer 目标、强制退出后的真机恢复、AI 提交/重试/过期，实测后才声明通过；所有付费 API 调用需单独明确授权。重点验证新旧调用链替代后同批清掉旧实现。

## 单一对象与不变量

`ClocktowerGameSession` 拥有一个 `ClocktowerSessionState`；全部阶段、轮次、角色、死亡、中毒、当前夜间步骤、白天票数/执行、已确认 ActionFact/Observation、终局由此读取、提交、原子落盘。UI draft、开局前 `ClocktowerOpeningDraft`、静态剧本、`GameSnapshot`（只读快照）、展示卡片和 `ClocktowerAiConversation`（建议/会话记忆）不能独立推进或持久化另一场游戏。D2 全屏预览可以返回修改，只有本步 Next 写唯一最终 Observation；已确认动作立即生效、不得回到上一角色。保留 TB/NGJ、AI 三模式/当前 V3/receipt/战略记忆、两项跨局轮换、隐私/stale/幂等/AtomicFile 的现行代码和行为测试。

## 文档与历史入口

- [路线图](CURRENT_DEVELOPMENT_ROADMAP.md) 是进度和下一步唯一入口；[完整设计](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md) 是产品合同。
- [独立审计](MINIMAL_HOST_POST_MERGE_CODE_AUDIT_2026-10-21.md) 列证据/缺口；[清理验收](MINIMAL_HOST_WHOLE_TREE_CLEANUP_ACCEPTANCE_2026-10-21.md) 和 [原路径台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 保留可复核历史，不把旧 Draft/CI/阶段状态当现行命令。
- 只读审计不等于重新跑 CI。禁止任意合并旧 Draft、强推或让文档修改假装业务测试。

## 本轮测试清理（2026-10-21）

批准标准：只留保护可感知产品行为与真实状态安全的高价值测试，中低价值及以前保护错误行为的测试均可删除。本轮从 `1cfacda8` 派生 `cleanup/high-value-test-suite-20261021`，静态删去 22 个测试类、75 个 `@Test` 和无用 helper `A4RuntimeFixtures.kt`，另按测试级清理厨师任意值 `17`、只允许活人作为被提名者的错误断言。静态余 122 类、564 个 `@Test`（正式以精确 SHA CI/R2 实测为准）。保留 TB/NGJ 整局、Session 夜/日规则、原子保存、隐私、D2 唯一 Next、AI 请求/stale/回执/记忆及两项跨局防重复。没有修改生产业务代码，因此 Host 固定线索求值和死者被提名 UI Bug 仍需修复。Oracle VM 上原生 Gradle 的锁与 aarch64 运行库受限，不能冒充为本地测试通过；必须用 GitHub 全量 Android CI/R2 验收新提交。

**合并后复审补充（仅恢复两项高价值）：** #338 的自动代码复审发现两条独有缺失边界：① 真实 Session 当前玩家行动未确认，不得让残余 AI Scope 发明其后续目标与请求；② 360×520 手机竖屏 5–15 人方桌必须全部能完成容量布局。只恢复这两项小型行为测试（`ClocktowerResidualUnconfirmedActionBarrierTest`、`ClocktowerSupportedPlayerLayoutTest`），没有恢复以前 22 个低中价值类。静态总数修正为 124 类/566 项；准确验收待修复提交的 CI/R2，第一轮历史删减仍为 22 类/75 项。
