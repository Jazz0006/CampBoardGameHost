# 当前开发路线（2026-10-18）

唯一详细权威：docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md。现行目标：Host Core 唯一状态+已确认结构化事件+必要操作；全部旧 Rules 实现退休后零开始重建独立只读规则查询；旧 AI 推荐/候选协议退出，Host 稳定后才重建 AI Clues。Rules 和 AI 均不得写入 Host 事实，人工结果在清理期间不受游戏规则候选限制。

## 严格阶段顺序

| 阶段 | 任务 | 状态 |
|---|---|---|
| P1 | 全仓删除旧规则引擎、合法候选、真值/登记见证、关联 UI/Session/Setup/AI 调用及专属旧测试 | 下一轮**立即实施** |
| P2 | 删除旧 AI 候选协议与评分、A4/B4/ZDD/世界枚举和缓存，保留事件/隐私/请求安全 | 待 P1 |
| P3 | 收敛唯一 GameState/Session 事实及结构化事件记录，最小继续游戏、通用合同 | 待 P2 |
| P4 | 净删后的 UI 和源码职责拆分；全部源文件 <=50,000 bytes | 待 P3 |
| P5 | 再遍历全树验收并对接 EvidenceLab、独立 WebHost 的数据合同，然后单独规划新版 Rules / AI | 待 P4 |

以真实生产消费者审计为依据，有把握的整条删除；不确定的给用户一份简短的保留/删除风险报告，倾向先删、以后按正确边界重建。允许现有自动规则/旧 AI 临时不可用，**必须保留 MANUAL 最小游戏操作、真实已确认事实、安全和隐私**。每批报告净删除、源文件超限、唯一事实风险和真实 CI/R2。

## 当前历史基线（不是 P1 完成证明）

GitHub 堆叠 Draft PR #330→#334 未合并。#334 目前确认的代码检查点 HEAD 为 55508645a11dc9277d0af7260bca4968c211e31c；Android 全量代码 CI #4325、R2 #3905 成功；docs-only CI #4326/R2 #3906 成功。开发机本地与此远端分支并不同步，新会话需先检查并从最新远端实际 HEAD 起步。

原全树扫描约 717 个代码类文件，4 个源文件超过 50,000 bytes：CampBoardGameHostApp.kt、ClocktowerHostScreen.kt、ClocktowerNightStepUi.kt、web/app.js。web/ 为旧独立网页客户端，含 Undercover/狼人杀与独立 localStorage；是否整个退休属待用户审核，不应误当成未来独立 WebHost。其余旧 Rules/A4/B4/ZDD 整链已获准退休，不需重复征求方向批准。

当前开发实施入口：docs/NEXT_DEVELOPMENT_HANDOFF.md；全部源码原始索引：docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv。旧 M1b/M1c、D5/D6/Recovery/规则维护 NEXT 均为历史，不得继续执行。
