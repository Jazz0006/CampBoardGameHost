# 当前路线 — 最小整局已验收，进入全仓清理（2026-10-21）

唯一游戏对象为 `ClocktowerGameSession`。UI 草稿、静态剧本、AI 传输/只读投影和存储是适配层，不是其他事实/进度来源。所有者顺序：**最小完整流程 → 全部代码遍历和大规模删除 → 界面与流程细节优化**。

## 第一阶段：最小完整流程已完成并验收（已关闭）

真实 App 入口已验证：首页/玩家/座位/剧本 → 生产发牌 → 首夜 → 白天真实提名手势/投票/同页日结 → 后夜实际死亡 → 文件恢复/继续 → 正式终局/明确揭晓 → 恢复后原班再开一局。两剧本真实准备器生成的合法 5 人代表阵容均跑通；已有 5–15/NGJ 5–6 人轨迹和必要角色分支继续保留。

修复了最后两个实际阻断：Session 创建后即保存，不再等到 Judge；恢复后的 quick restart 从当前 Session 取确认名单，不依赖已经丢失的准备页对象。中断发牌从同一 Session 重新展示，不保存额外发牌游标。

[最小完整验收报告](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md) 给出逐环节证据、边界和最终 CI。完整 full/APK 与精确 CI/R2 已通过，第一阶段已关闭；不再以局部 UI、普遍职责抽取或穷举组合阻止进入清理。真机/实际杀进程/付费模型未运行，不影响这里的代码/自动化最小闭环结论，也不能宣称那些证据已完成。

## 第二阶段：下一主任务——全仓遍历、大规模删除

以已验收完整可玩闭环为保留基线，重新从未截断当前 Git 树逐文件/责任检查生产、资源、测试、CI 与真实消费者。删除旧引擎、冗余功能、重复状态/控制流、孤立实验和兼容包装及其专属测试；混合文件只留必要 Host、现有 AI、隐私/存储/平台职责。必要迁移和旧消费者删除同批完成，不留第二 Session 或新旧双轨。

每批给出完整删除链、实际文件/行/字节净量和保留游戏行为的验证。先删再内聚整理，全部源码 <=50000B 和全树零遗留属于本阶段验收，不能机械切块或新增泛用 Context/Manager。

当前 App **116000B**、Night Host **63787B** 超限，Session **47578B**。本批核心修复生产 +3 行/+316B、生产文件新增/删除 0；新增 1 测试文件 / 160 行。前批 SessionView、NightCheckpoint、setupCards 活跃依赖的删除已验收，详见 [台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。

## 第三阶段：清理后统一优化界面与细节

在精简后的完整流程上集中调整布局、按钮、提示、高亮、错误反馈和体验一致性，再做相应完整视觉/设备验收。当前仅记录非阻断问题，不预设现有页面结构必须保留。

## 验证与执行边界

本批 Cloud full 654 / 147 suites / 38 UI、APK、原 R2 shell 通过，生产代码 `e73e4807691ab974dddfa86532a30efe89973753` 的[完整 Android CI #4503](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618033) 与 [R2 #4079](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618124) 均 SUCCESS，已独立核对全量 Android/APK 上传、ASP、Clingo 和 gate；前一 `3268440f` / Cloud 651 / CI #4501 / R2 #4077 为历史基线。保持两剧本、现有 AI 全链/已有模式范围、双跨局防重复、确定机械、D2 最终 Next、顺序/隐私/幂等/原子保存。Cloud 直接读写测试；原 #335、#334 已合并，main `40050941efc3` 的完整 CI #4506 与 Field Test APK #157 SUCCESS；旧 Draft 禁令已过时。新清理 PR 独立验收，保持 Draft，不强推/删分支/付费 API。交接见 [下一步](NEXT_DEVELOPMENT_HANDOFF.md)。

## 第二阶段首批：孤立工具/静态样例整链退休

从 main `40050941efc3` 建立 `codex/whole-tree-cleanup-20261021`。删除 `tools/asp_oracle/`（静态 FormalGameState corpus→外部 botc-asp、专属 Python 测试/说明）、孤立 `ui/` HTML/CSS/JS 展示样例和已完成的模板迁移脚本。CI 同批移除 ASP/Clingo 路由/jobs/dependencies，保留 Android full/APK、失败门禁和独立 R2。真实 Host/AI 没有调用该旧 corpus；Android 生产/测试 Kotlin 零改动。

可执行/样例/工具及其说明共删除 9 文件，11 文件 +4/-2766，净删 2762 行 / 134078B（不含本批路线/规范/台账说明变动）。Cloud full 654 / 147 suites / 38 UI，0 failure/error/skipped，APK 和原 R2 shell 通过；10 类 CI 路由及门禁失败/取消检查通过。代码 `280992ca271d7edffcd190834340cd1768fd7ad3` 的[完整 Android CI #4508](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39580230705) 和 [R2 #4083](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39580230512) 均 SUCCESS；已独立核对 full tests、APK 构建/上传及 CI gate。Draft PR [#337](https://github.com/Jazz0006/CampBoardGameHost/pull/337) 保持未合并；本次后续文档提交不替代上述代码验收。

唯一台账按当前 Git 树对齐：清除前阶段已删除但仍挂表的 54 条路径和本批 8 条已删源路径；漏登的新 Session/UI/测试及历史 artifacts 补为待审。库存校正不算新语义审计，全树尚未完成。后续继续待审代码及现有 App/Night Host 混合职责，优先整链净删；两个超限文件仍为 116000B / 63787B，Session 47578B。

## 第二阶段第二批：生产孤立回放/观察副本与旧候选展示退出

在同一Draft #337从`2a8ad070`继续，整删只供测试调用的旧动态reducer、旧snapshot-bound observation/ID/JSON链、无入口数字候选UI、旧角色揭示投影和唤醒集合辅助，连同专属旧测试。保留Session确定机械和当前Recorded观察；schema、AI协议、AtomicFile与两类轮换未改。另清未调用日志/UI辅助、12组无引用字符串，以及两处不可达的多推荐分支。

不含维护文档：31文件+47/-748，净删701行/30956B；生产Kotlin净删574行/24267B，整删生产2/测试1文件。原654测试减少3个退休实现专属用例，全部现用UI/AI/隐私/原子性回归继续执行。全文审计进度与逐路径证据见唯一台账；全树仍未完成，App115420B/Night Host63787B仍超限。代码 `212e53f4b16519d1f5a6fcacad0a8f5fb38096ab` 的[完整 Android CI #4511](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39582576627) 和 [R2 #4086](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39582576631) 均 SUCCESS；已独立核对 full tests、APK 构建/上传和 CI gate。后续仅文档提交不替代此代码验收，PR #337 保持 Draft。 最终本地及远端验收见交接，不能使用上批CI代替本批代码。

## 2026-10-21 第二阶段第三批：过时AI实验与Legacy直接记录入口退休

从同一Draft #337头`5eb48fa0`继续，main仍`40050941efc3`。本批删除旧API-0候选ID协议实验→专属fixture/Python测试/CI工作流，以及只验证continuation-v2的独立探针→专属测试。现用Android V3请求、previousResponseId/retainForContinuation、receipt/记忆/三模式及连接配置均保留；没有调用付费API。

Session仅退休无生产消费者的LegacyLocal直接append方法recordEpistemicObservation，旧测试迁到真实Global私密信息提交与后续Death确认，保留隐私/顺序/revision断言；删除1项只拒绝旧方法的专属测试。其余LegacyLocal存档解码/模式隔离、Global序列/重复冲突/耗尽原子性、Session唯一写者与原子存储继续保留。

不含维护文档共**9文件+26/-1009，净删983行/39695B**，6文件整删（4工具/专属测试、1工作流、1fixture）。Android专属旧入口测试-1，Python退休专属测试-15；现有业务断言迁移，未忽略测试。首次完整审计原待审现存**11路径**；当前**440路径、220有结论、220待审**。共享UI/平台资源有真实消费者则保留；发现contentMode及底部单项策略包装待后续收敛，不把部分修改当全仓完成。

验证：Cloud full **650 tests/146 suites/38 UI，0 failure/error/skipped**及Debug APK通过；精确代码`8ac04443aa8e1578e8e8252550f8650e400d0554`的[完整Android CI #4513](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39584194981)与[R2 #4088](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39584194745)均SUCCESS，独立核对14路径/逐行diff、full tests、APK构建/上传及CI gate。本次后续提交只记录验收，不替代代码构建；R2原脚本、CI 10路由/5门禁和shell语法、零退休符号产品引用、diff检查通过。App115420B、Night Host63787B仍超50000B，Session46770B。两剧本、现有AI、两条跨局防重复、D2、隐私及原子保存合同不变。PR保持Draft，不合并/强推；无新增所有者决策阻塞。

## 2026-10-21 第二阶段第四批：无作用展示合同与旧快照入口删除

从Draft #337 `73299a8c`继续，main仍`40050941efc3`。全局引用核实后移除HostSeatContentMode/contentMode字段及所有构造/专属断言；底部单项策略文件整删，必要navigationBars bottom padding回接既有FullScreenScaffold。对比度算法原样迁回既有测试，全部实际配色和可读性断言保留。

旧FortuneTeller首/后夜投影角色包装、storytellerAiResponseMayApply及专属2测试文件退休；真实FT读Session，App当前异步请求前后模式/凭据/旧响应检查不变。旧fromCommitted快照入口及专属1用例/helper删除；现行开局中间态/运行时AI快照、V3协议、会话延续/记忆、存档schema及兼容枚举均不变。

不含维护文档15文件+29/-323，净删**294行/11391B**，整删1生产策略文件和2专属测试文件，无新文件/框架。Android测试9项只保护已退役入口；身份/隐私/配色实际断言保留。首次完整核查原待审现存**13路径**；台账现为**437路径、232有结论、205待审**。其余修改文件只局部复核则不重计全文审计，未把扫描清单当结论。

验证：Cloud full **641 tests/144 suites/38 UI，0 failure/error/skipped**和Debug APK通过；代码`21aed457c6282e985ac7a99b4de1af42dd0f563f`的[完整Android CI #4515](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39585170348)与[R2 #4090](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39585170361)均SUCCESS，独立核对18路径/逐行diff、全量测试/APK构建与上传及CI gate；后续仅文档提交只记录验收，不替代代码验证；原R2、10类CI路由/失败门禁及shell语法、退休符号零残余、diff检查通过。两剧本、唯一Session、现有AI、D2、隐私/原子保存、两项跨局防重复保持。App115420B、Night Host63787B仍超50000B，Session46770B；本批不声称解决大文件。下一步继续App/Host真实混合职责和剩余待审源文件，UI细节后置。Draft不合并/强推，无新增所有者决策阻塞。
