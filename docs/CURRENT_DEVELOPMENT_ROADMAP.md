# 当前开发路线（2026-10-18）

唯一详细权威：docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md。现行目标：Host Core 唯一状态+已确认结构化事件+必要操作；全部旧 Rules 实现退休后零开始重建独立只读规则查询；旧 AI 推荐/候选协议退出，Host 稳定后才重建 AI Clues。Rules 和 AI 均不得写入 Host 事实，人工结果在清理期间不受游戏规则候选限制。

## 严格阶段顺序

| 阶段 | 任务 | 状态 |
|---|---|---|
| P1 | 全仓删除旧规则引擎、合法候选、真值/登记见证、关联 UI/Session/Setup/AI 调用及专属旧测试 | **实施中**：B1 登记及展示见证消费者已清理，规则整链尚未完成 |
| P2 | 删除旧 AI 候选协议与评分、A4/B4/ZDD/世界枚举和缓存，保留事件/隐私/请求安全 | 待 P1 |
| P3 | 收敛唯一 GameState/Session 事实及结构化事件记录，最小继续游戏、通用合同 | 待 P2 |
| P4 | 净删后的 UI 和源码职责拆分；全部源文件 <=50,000 bytes | 待 P3 |
| P5 | 再遍历全树验收并对接 EvidenceLab、独立 WebHost 的数据合同，然后单独规划新版 Rules / AI | 待 P4 |

以真实生产消费者审计为依据，有把握的整条删除；不确定的给用户一份简短的保留/删除风险报告，倾向先删、以后按正确边界重建。允许现有自动规则/旧 AI 临时不可用，**必须保留 MANUAL 最小游戏操作、真实已确认事实、安全和隐私**。每批报告净删除、源文件超限、唯一事实风险和真实 CI/R2。

## P1 实施记录（只记录真实提交）

- **B1 代码提交 `892e0252eb89d9c4bf99f69754f1984fe5bbba98`**，PR #334 Draft 堆叠关系不变。删除旧 `RegistrationInteractionRules` 及专属测试、无消费者的 `TroubleBrewingRecurringPlayerInformationRouteDomain`，以及厨师方桌由数值推导登记见证/有效邪恶座位/相邻计数高亮、配对方桌对 Spy/Recluse 登记见证推断勾选。另清除旧步骤构建器中依可靠性过滤登记键的调用。初始净减少 **379 行（+31/-410）**，修复后 B1 合计净减少 **378 行（+32/-410）**，生产源码删除 2 文件、旧测试删除 1 文件，其余按职责精简。
- B1 首个代码 HEAD 的完整 Android CI **#4330**：编译通过，1301 项测试中 1 项旧规则键断言失败；R2 **#3910 SUCCESS**。已根据批准的退役方向修复：夜间信息 Builder 不再开启旧登记见证交互（无自动见证、不可凭信息展示补记登记），修复提交 **`4b1267e8e7d3277577e44c01dd7004cb04da6fb5`**；修复后的完整 Android CI **#4331 SUCCESS**（`testFull` 和 Debug APK 构建），R2 **#3911 SUCCESS**；ASP/Clingo 和 CI gate 均成功。保留原 Session 事实提交、直接数字选择和方桌实际身份提示；未进行真机手动通局验收。
- P1 **未完成**：`clocktower/rules` 仍有约 20 个旧源码文件；Pair/Numeric legal domain、Registration、AbilityFunctioning、Night/Session、Opening residual/event gate、Recommendation 与 Setup/Provider 旧消费者须继续断开并删除，不得提前进入 P2。后续批次复核 CI/R2 旧源码守卫。

## 当前历史基线（不是 P1 完成证明）

GitHub 堆叠 Draft PR #330→#334 未合并。开发机已从旧 `codex/minimal-host-reset` 显式切换到 #334 正确分支 `codex/minimal-host-full-scan-20261018`。P1 B1 原始代码提交 `892e0252...`，后续修复代码提交 `4b1267e8...`；当前 HEAD 后续可能被文档提交推进，始终以最新 GitHub 和本地 Git 实测为准。更早 #4325/#3905 是 P1 之前的历史基线，不得当作新 P1 验收。

原全树扫描约 717 个代码类文件，4 个源文件超过 50,000 bytes：CampBoardGameHostApp.kt、ClocktowerHostScreen.kt、ClocktowerNightStepUi.kt、web/app.js。web/ 为旧独立网页客户端，含 Undercover/狼人杀与独立 localStorage；是否整个退休属待用户审核，不应误当成未来独立 WebHost。其余旧 Rules/A4/B4/ZDD 整链已获准退休，不需重复征求方向批准。

当前开发实施入口：docs/NEXT_DEVELOPMENT_HANDOFF.md；全部源码原始索引：docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv。旧 M1b/M1c、D5/D6/Recovery/规则维护 NEXT 均为历史，不得继续执行。
