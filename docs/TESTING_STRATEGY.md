# CampBoardGameHost 测试与验收策略（2026-10-23）

## 2026-10-25 用户再次纠正：限制测试投入（当前最高优先级）

此前已有“不机械 test-first”的规定，但实际执行仍为箭头、虚线、长按时长等低风险修改新增了像素/几何测试，并扩大人数、模式组合。以下规则将取舍落实到执行，覆盖下文及历史文档中冲突的“一律 UI 全量验证”等要求：

- **不默认 test-first，不是每次修改都新增测试。** 纯样式和低风险调整（箭头、虚线、颜色、间距、文案、长按时长）不新增专用回归、截图像素或几何矩阵，按需人工检查或构建。
- **只有具体重要风险且现有覆盖缺失时才新增少量行为测试。** 包括错误规则/死亡/传位、私密信息泄露、存档丢失/恢复错误、重复提交/过期回调、游戏无法继续。已有覆盖优先复用；组件拆分、新函数和文件数量不构成新增理由。
- **新增前说明价值。** 在正常进度说明中用一句话交代实际故障及现有覆盖缺口；没有明确独立价值就不加，不另造审批/检查流程。
- **保留代表案例，避免重复组合。** 不机械遍历人数、工作模式和布局；只有存在不同故障机制时才增加案例。安全/流程混合测试可删显示细节断言，保留独立行为保障。
- **验证与风险相称。** 小型 UI 调整不默认运行完整套件，也不因每次外观微调反复全量测试。任务明确要求的验收/CI 仍须完成。测试数量不是质量目标，不为维持数量保留低价值或过期测试。

2026-10-25 已按用户授权完成本批清理，合并状态以关联 PR 为准：移除 **10 个测试方法**（含今天尚未提交的 2 项），删除提名手势/箭头像素、唤醒箭头像素、候选虚线像素三个专用测试文件，以及箭头几何矩阵、简单唤醒标记、重复继任模式案例和已不符合当前厨师候选范围的 0～15 滚动案例。厨师按钮可操作性缩为 15 座位最大候选的一个代表场景；继任通知缩为一个代表对局，保留隐私、状态变化/恢复后预览失效保障；删除重复预览横幅消失的纯显示断言，保留正式信息只在 Next 写入的断言。规则、存档、隐私和真实完整对局测试继续保留。本次仅运行受影响的保留用例和测试编译，不重复全量测试。

## 当前五批 Host UI/Resolver 验收证据（优先于历史检查点）

**所有五批已合并 `main` `b551d7f2efb074e45f2722c7bcd712d5f8bd56e8`**。Stage 1 视觉 [#343](https://github.com/Jazz0006/CampBoardGameHost/pull/343) CI #4543/R2 #4110、Stage 2 只读求值 [#344](https://github.com/Jazz0006/CampBoardGameHost/pull/344) CI #4544/R2 #4111、Stage 3 Chef/Empath [#348](https://github.com/Jazz0006/CampBoardGameHost/pull/348) CI #4562/R2 #4124、Stage 4 Pair [#349](https://github.com/Jazz0006/CampBoardGameHost/pull/349) CI #4569/R2 #4130、Stage 5 NGJ 数字 [#350](https://github.com/Jazz0006/CampBoardGameHost/pull/350) 完整 [Android CI #4572](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39768833600) / R2 #4132，**全部 SUCCESS**。第五批最后文档提交另有 CI #4573 / R2 #4133 SUCCESS，但该次 Android 测试因 docs-only 变更被 **SKIPPED**，以 `7c2950aa` 的生产代码 CI 为代码证据。

**已有高价值自动化**：TB 5–15 与 NGJ 5–6 代表局、360×520 共享方桌/数值/Pair、醉酒中毒/Spy/Recluse 登记（适用于包含相应角色的剧本）、侍女排除酒鬼假唤醒、钟表匠环形座位距离、当前 Session AI 建议、D2 全屏预览→改选→仅 Next 写最终 Observation、全局阶段/死亡/鬼票、Session 序列化恢复以及完整 App 闭环。不要把此描述扩大成穷举全部角色组合或真实来电/物理强退证明。

**尚未验收：实际 Android 手机。** 在代表性 5/15 人 TB 与 5/6 人 NGJ 局检查昼夜/阵营配色、姓名/角色文字、死亡/鬼票/醉酒/中毒叠加、虚线/选中/中央按钮和滚动、三种工作模式、全屏手机交接与 Next、真实来电/进程被杀及从唯一 Session 继续游戏。CI/Robolectric 和本地脚本成功 ≠ 真机视觉/设备恢复；付费 LLM 调用需要用户明确授权。

**测试取舍：** 遵循高价值行为风险测试，不机械 test-first，不为被删旧 Rules/评分/长候选接口恢复僵化测试；新读写路径迁移成功后同批删除旧接口、状态与调用。仍待独立 P1 行为验收：活人提名死人；正常占卜师固定布尔信息。完整合同见 [五批方案](HOST_TABLE_AND_INFORMATION_REBUILD_2026-10-22.md)、[最新路线图](CURRENT_DEVELOPMENT_ROADMAP.md)、[第五批验收](HOST_INFORMATION_STAGE5_ACCEPTANCE_2026-10-22.md)。

## 2026-10-21 既有测试政策 / 历史基线（无冲突部分继续适用）

> **最终高价值测试清理**：PR #338 + #339 合并，精确代码 `4b9852cf5828c445b532550115aafbf2ddc8c4bf`，其中补回两项独立安全/可玩性覆盖的 #339 [Android CI #4528](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39604089426) 与 R2 #4098 SUCCESS。最终静态 124 测试类/566 项（原 144 类/639 项），不把旧版 639 或中途 564 视为现在的应过数量。文件下方保留执行过程的分批计数供追踪，但以本段为最新基线；真机强退和付费 AI 未验收。

> **当前唯一验收策略。** 先以 [开发路线](CURRENT_DEVELOPMENT_ROADMAP.md)、[当前交接](NEXT_DEVELOPMENT_HANDOFF.md) 和 [合并后独立审计](archive/legacy-host/MINIMAL_HOST_POST_MERGE_CODE_AUDIT_2026-10-21.md) 判断应测什么；不照搬 F12/P1–P5 历史进度或机械 test-first。之前的详细时间线已存档至 [旧版测试策略](archive/checkpoints/TESTING_STRATEGY_PRE_POST_MERGE_AUDIT_2026-10-21.md)，仅用于溯源而非现行命令。

## 事实和验证基线

- #334/#335 已合并，阶段一最小整局已验收；#337 已合并，阶段二全仓清理已验收。不是仍需开始的工作。最新独立审计时 `main` 为 `6230b5959cf4b64e1c41f08bb48aa66460e13667`。
- 最后一次**已验收代码** `e5ffc4e5e3ccdac5a5d4d3d7845f9754b998dbba`： [Android CI #4520](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977318) / [R2 #4095](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977316) SUCCESS；Cloud full 639 tests / 144 suites / 38 Compose UI、Debug APK 通过。后续 docs-only 提交不能冒充对新代码的编译结果。静态 ASP/Clingo 旧候选/合成门禁已退休，不能继续要求其 jobs 出现在当前 CI。
- 真实 App 完整对局的 TB/NGJ 代表性流程与文件重读由 `ClocktowerCompleteAppUiTest` 证明，范围见 [最小整局验收](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)。该结果不等于穷举所有阵容、实际 Android 进程杀死或付费 API 通过。

## 优先级与行为测试

1. **P1 Host 唯一结果**：健康 Chef/Empath 及固定布尔查询，在完成合法登记裁量后应由 Host 提供唯一结果，不依赖手动任意输入或 AI 猜测；Drunk/poisoned/Spy/Recluse 等有裁量边界的情况允许真正选择，按玩家实际收到的内容只写一条最终 Observation。对同一 Session 的输入、查询目标、能力状态、已确认事实分别测试，明确人工误播记录边界。
2. **P1 Day 提名被提名者**：存活玩家可以提名死者，死者不能发起提名。验证活人→死人手势选择、确认 `Nomination`、投票/处决（死者执行不产生第二次死亡）、中断重载；同时保护每天一人提名一次、每人被提名一次和真人实际输入。当前 UI 过窄问题详见 [独立审计](archive/legacy-host/MINIMAL_HOST_POST_MERGE_CODE_AUDIT_2026-10-21.md)。
3. **夜间步骤与安全事务**：无 StartNight/独立 Dawn/跨夜 Previous；既有保护/毒/攻击/死/市长/继任/唤醒触发由唯一 Session 决定，正式 Next 立即原子提交、稳定 stepId 幂等，不能从 UI 第二游标提交。真实 Session 应能在磁盘写入失败回滚，并基于已确认记录安全重载。
4. **D2 确认门禁**：选择→全屏预览→返回可修改→再预览→当前角色 Next 只写**最后一条私密 Observation**；上一轮预览、过期 AI、改变查询目标后旧结果不能被接受。三模式下自动只替代说书人裁量，不替代真人玩家的点击/目标输入。
5. **保留产品边界**：TB 5–15、NGJ 5–6 实际支持人数与代表角色，已有 AI 联合开局/续规划 V3、receipt/记忆/凭据隔离、两项跨局角色阵容/同玩家身份防重复、游戏身份和隐私。不要为退休旧合法候选/评分/世界枚举恢复专属断言。

## 执行与退出标准

- Evidence-first：每次改动先证明生产消费者、预期真实行为、风险和旧实现删除范围；机械重构不强制 RED/GREEN，必要时新增稳健的真实 Session / Compose 回归。测试不要求复制冗余算法来证明旧实现。
- 不默认运行测试命令；按具体风险选择现有定向测试、`./gradlew :app:testFast` 或构建。涉及 Session 事实、持久化、AI 请求、结构化事件、身份/隐私或规模重构时，按影响运行 `./gradlew :app:testFull :app:assembleDebug` 并检查适用 CI/R2；纯 UI 外观调整不自动触发全量。若开发机不具备编译资源，可由有 Android SDK 的工作环境执行，并把精确 SHA 的 GitHub 全量 Android CI 与真实构建日志作为验收，不以纯文档 CI 成功代替。
- 本次及未来验收须注明 **代码 SHA + 具体测试结果 + CI/R2/设备/付费 API 是否真实执行**；CI 成功不等于现场 UI 成功。已运行与未运行分开写，失败先定位原因再修复而不是忽略测试。
- 生产修改不能引入第二份 GameState、Pending/夜间游标、独立恢复器、旧规则候选/评分系统或永续双轨。替换生产路径后追踪完整消费者并同批移除旧代码和独占测试；所有生产 Kotlin 源文件维持 ≤50,000B。
- 修改 GitHub workflow 时验其真实脚本、路由、失败门禁和权限。禁止无授权 merge/rebase/force-push、删除分支及付费模型请求。未验收真机或现场展示之前保持明确未验证状态。

## 2026-10-21 高价值测试保留与清理

完成低中价值测试删减：以 `1cfacda8` 的 144 个测试类、639 个 `@Test` 为静态基线，删除 22 个仅覆盖文案、展示布局、内部结构/注册表、架构来源及重复的测试类，另删除无消费者的 `A4RuntimeFixtures.kt`。同时从两个混合价值测试中删除健康厨师任意数字 `17` 和“只能提名活人”的错误断言，其余真实查询和手势约束保留。静态预期为 122 个测试类、564 个 `@Test`、125 个 Kotlin 测试文件（包含 3 个保留 helper）；测试数量变化 **不是质量证明**。精确代码 `b050033b12b66f21fd24e8ecb9534c4f33397f7a` 在 [CI #4524](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39602538249)、[CI #4525](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39602587292) 及 R2 #4097 均 SUCCESS，PR #338 已合并 main `2dcf8a97`；实际生产 Bug 未修，不能用断言删除宣称修复。必须保留唯一 Session 夜间/白天机械、TB/NGJ 完整 App、存档原子回滚/恢复、D2 Next、AI 请求/解析/stale/receipt/记忆、隐私/真实显示和两项跨局防重复。后续修正固定线索/死者被提名时，重建直接对应的高价值行为测试，而非恢复旧结构测试。

**高价值边界复审修正：** 代码复审指出 `CurrentResidualScopeV2` 的未确认操作必须阻断 AI 跨步推荐，以及紧凑竖屏 5–15 人席位布局的原单例回归属于独立高价值，不能跟随内部结构/几何细节测试一并删除。已单独提取成两项窄范围真实行为测试；不恢复废弃的格式、注册表、几何常量和来源标签断言。两批合计静态 124 类/566 项（相对旧 144 类/639 项仍净减 20 类/73 项）；新精确代码须经过 Android CI/R2 才能宣称通过。
