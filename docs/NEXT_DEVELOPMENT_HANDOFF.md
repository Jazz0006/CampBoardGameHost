# 当前交接 — Codex 本地净删与全夜切换（2026-10-20）

**代码基线 `311191a30d6b0d83b5cf2d6de2a67905b526e596`，已推送原 Draft #335。** [完整 CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) SUCCESS；[R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) SUCCESS。本地 fast 593/full 616 tests（145/146 suites，0 failure/error/skipped，含 23 个真实 UI 用例）及 APK 通过。docs-only 后续提交不改变此代码验收归属。

## 已完成的责任与删除边界

直接使用本地 Windows `C:/Users/Jazz_/source/repos/CampBoardGameHost`，不使用 MiniMCP。JDK `C:/Program Files/Android/Android Studio/jbr`。原未跟踪 node_modules/package 文件保留，不属于本批。

一个 Session/GameState + 有序事实 + 稳定 stepId；信息预览不记录，当前角色显式 Next 才写最后唯一 Observation。夜间不再依赖 App/HostScreen 的 index/start/pendingAdvance，所有目标动作显式 Next。旧 Planner/Projector/First/OtherFlow、Fixed/Dynamic resolver、毒游标 chronology 和攻击预测链已实删。NightHostProjection/Materializer 只保留读取当前事实并渲染的职责，不再决定流程。

无用途首夜发布生命周期、旧 live-query/role projection、完整登记 UI/映射和自定义/Homebrew 分支已删除；保留处女/Spy 真实裁量、实际 AI、TB/NGJ 和两类跨局多样性。测试专用 snapshot codec 已移入 test，交换 fixture 保留。Spy 以实际 NightWake 事实完成真实方桌交接，不建 Grimoire 镜像；Imp 待通知从 RoleChange 与已确认私密信息推导，不再有两个可写 UI 通知标记。

生产目录 16 旧路径退出（15 删除+1 codec 移到测试）；新增 Session NightFlow 与 AI 仍需的 Family 分类。18 个专属旧测试文件退休；混合真实行为保留/迁移。生产净删 **2278 行/101108B**，测试净删 **1961 行/77387B**。当前超限 App **148697B**、HostScreen **80726B**、Session **53414B**。具体旧→新消费者、完整删除名单及风险见 [当前台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 和代码 diff。

## 下一批

1. 核实此代码精确 Android CI/R2 完整成功，并做 P2 新切换的跨夜/全角色/人数与必要新 APK 真机检查。用户已报告前一版本流程可运行；不冒称本批新 APK 真机/全角色/中断恢复已通过。无付费 API 实验。
2. 然后推进 P3 B/C：白天共享方桌一次日结，删除 EndConfirm；Death/Execution 派生 Klutz 真实选择，删除 Learned 事件/producer/编码/消费者；必要白天/胜负规则归 Host，同批退役旧 UI 控制。
3. 原子持久化/真实进程恢复、AI/展示剩余收敛、源码 <=50000B 与 P5 全树证明仍未完成。不要把同一已确认状态的纯读取重建测试当作落盘/进程恢复验证。

每批列删除生产/专属测试、消费者迁移、净量、超限文件、状态/隐私风险与精确代码 CI/R2。保持 #334/#335 Draft，不 merge/retarget/rebase/force-push/删分支；没有新增所有者决策阻塞。

历史原记录保留于 [交接快照](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_LOCAL_D2_2026-10-20.md)、[D2 交互矩阵](D2_INTERACTION_ACCEPTANCE_2026-10-20.md) 和台账。旧游标/Pending/Previous/Dawn/展示即提交/MiniMCP NEXT 不恢复。
