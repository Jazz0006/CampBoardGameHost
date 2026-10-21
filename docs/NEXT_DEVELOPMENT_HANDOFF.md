# 当前开发交接（2026-10-21，已审计 main）

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
