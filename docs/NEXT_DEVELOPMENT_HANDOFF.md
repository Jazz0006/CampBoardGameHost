# 当前交接 — 全仓清理第三批（2026-10-21）

## 2026-10-21 第二阶段第三批：过时AI实验与Legacy直接记录入口退休

从同一Draft #337头`5eb48fa0`继续，main仍`40050941efc3`。本批删除旧API-0候选ID协议实验→专属fixture/Python测试/CI工作流，以及只验证continuation-v2的独立探针→专属测试。现用Android V3请求、previousResponseId/retainForContinuation、receipt/记忆/三模式及连接配置均保留；没有调用付费API。

Session仅退休无生产消费者的LegacyLocal直接append方法recordEpistemicObservation，旧测试迁到真实Global私密信息提交与后续Death确认，保留隐私/顺序/revision断言；删除1项只拒绝旧方法的专属测试。其余LegacyLocal存档解码/模式隔离、Global序列/重复冲突/耗尽原子性、Session唯一写者与原子存储继续保留。

不含维护文档共**9文件+26/-1009，净删983行/39695B**，6文件整删（4工具/专属测试、1工作流、1fixture）。Android专属旧入口测试-1，Python退休专属测试-15；现有业务断言迁移，未忽略测试。首次完整审计原待审现存**11路径**；当前**440路径、220有结论、220待审**。共享UI/平台资源有真实消费者则保留；发现contentMode及底部单项策略包装待后续收敛，不把部分修改当全仓完成。

验证：Cloud full **650 tests/146 suites/38 UI，0 failure/error/skipped**及Debug APK通过，精确远端CI待提交后独立核对；R2原脚本、CI 10路由/5门禁和shell语法、零退休符号产品引用、diff检查通过。App115420B、Night Host63787B仍超50000B，Session46770B。两剧本、现有AI、两条跨局防重复、D2、隐私及原子保存合同不变。PR保持Draft，不合并/强推；无新增所有者决策阻塞。

下一步继续剩余待审源文件，优先审计App/Night Host混合职责及已确认无作用的contentMode/底部策略包装，再与真实消费者同批删除；不要回到界面精修，也不要把测试专用Session方法及其安全测试一次盲删。直接使用Cloud，工具链和保留合同见下方历史交接；当前完整日志`/tmp/campboard-batch3-gradle.log`，测试摘要`/tmp/campboard-batch3-tests.json`。

以下第二批交接保留为历史证据，当前批以上文为准。

# 当前交接 — 全仓删除阶段（2026-10-21）

#335、#334 均已合并；main 基线 `40050941efc3a9bcfd061d3c91d5cc72c5c1d2e5`。GitHub 独立核实完整 CI #4506（39577688281）和 Field Test APK #157（39577688218）均 SUCCESS。旧“两 PR 仍 Draft、不得合并”记录过时。最小完整游戏第一阶段关闭，具体闭环/限制见 [验收报告](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)。现在全仓遍历/大规模删除，之后才优化界面和流程细节。

## 当前批：旧回放/观察副本/候选呈现删除

同一工作分支 `codex/whole-tree-cleanup-20261021` / Draft [#337](https://github.com/Jazz0006/CampBoardGameHost/pull/337)，从上批文档头`2a8ad070`继续；main未变。上批孤立ASP/静态样例证据留在路线和台账，不重复计本批删除。

- 删除DynamicActionReducer/ReducedDynamicGameState，及只供它的reducerFacts桥；测试直接读canonical entries，Session实际规则和当前保存不依赖旧replay。
- 删除旧snapshot-bound EpistemicObservation、bindTo副本、SemanticStableId与独有JSON入口；实际RecordedObservation、InformationProposition、顺序/recipient及现有存档字节合同保留，未改schema。
- 整删无外部消费者的ClocktowerSingleNumericChoiceUi及ClocktowerRoleRevealChoiceProjection；移除仅旧测试消费的TypedResult/唤醒集合、无调用的App能力/语言辅助、FullScreenColumn及日志回调。
- 删除12个零引用字符串key的中英文条目；2个仍在使用的单推荐控件只删take(1)后不可达分支，当前推荐/手选/自动/Next行为保持。
- 3个退休实现专属测试退出（1个旧唤醒测试文件、旧reducer等价用例、旧bindTo用例）。实际JSON/schema/私密收件人断言迁到Recorded记录；其他UI/Session事实/幂等测试保留。

不含维护文档共31文件+47/-748，净删**701行/30956B**；生产Kotlin净删574行/24267B，资源24行/1726B，测试净删102行/4905B，R2去除1条死符号存在性守卫。生产整删2文件、专属测试整删1文件，无新生产文件/框架。

## 验证与当前库存

最终Cloud `:app:testFull :app:assembleDebug` SUCCESS，651 tests /146 suites /38 UI，0 failures/errors/skips；原始R2 shell、CI的10类路由/失败门禁、零残余符号、资源引用及diff检查通过。较原654减少的3项只保护退休实现，不减少现有两剧本、AI、隐私或原子保存行为覆盖。代码 `212e53f4b16519d1f5a6fcacad0a8f5fb38096ab` 的[完整 Android CI #4511](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39582576627) 和 [R2 #4086](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39582576631) 均 SUCCESS；已独立核对 full tests、APK 构建/上传和 CI gate。后续仅文档提交不替代此代码验收，PR #337 保持 Draft。 上批#4508/#4083及docs-only头#4510/#4085仅为历史。

本批真正首次全文审计原待审的18个现存文件，更新唯一[源码台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)；当前445路径，213带审计结论、232待审。改了一行测试但未阅读全文仍保留待审，不以扫描数替代审计。App115420B、Night Host63787B超限，Session47578B。

下一步继续待审源文件和App/Night Host混合职责，按真实消费者/必要固定规则/既有AI审计后整链删除；LegacyLocal兼容、Session仅测试使用的改写入口等是后续候选，不能一次盲删其安全测试。UI视觉/提示细节在清理后集中做。新PR保持Draft，不自动合并/强推/删分支；无新增所有者决策阻塞。

## 环境和保留合同

Codex Cloud `/workspace/CampBoardGameHost` 直接读写、执行；不依赖 MiniMCP/Windows。每个 shell `source /workspace/.cloud-setup/activate.sh`；JDK25 位于 `/workspace/.cloud-setup/jdk25`。本批临时测试 init `/tmp/campboard-tests.init.gradle`、用户目录 `/tmp/campboard-java-user`，复用环境代理/CA；本批完整日志 `/tmp/campboard-stage2-final.log`，摘要 `/tmp/campboard-stage2-tests.json`，CI/R2 本地检查 `/tmp/campboard-ci-check.py`，均不提交。工具缓存可能需要网络补齐，不修改产品构建来绕过环境问题。

保留唯一 ClocktowerGameSession、两内置剧本、现有 AI 全链和模式范围、A/B/C/D2、实际展示信息/顺序/隐私、旧响应隔离、原子/幂等存储，以及开局阵容和同一玩家身份两项跨局防重复。
