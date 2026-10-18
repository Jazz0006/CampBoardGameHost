> **历史库存保留，现行审计结论见下文最新补记。** 2026-10-18 所有者纠正了早期方案：Host Core 保留唯一事实及安全提交；旧 Host 规则合法性门禁要删除，未来 Rules 独立只读重建；**现有 AI 说书人推荐系统必须完整保留**，不再执行“AI 清空后重建”。旧库存列表含先前已删除文件，不能视为现行 Git tree。

# 最小 Host 全仓源码遍历 / 删除边界台账（2026-10-18）

## F12：当前 Git 树逐文件语义审计（进行中，第一批）

- **证据 HEAD**：`cfe258b17ed1c8abb248f0ada4ee1c871593d961`。2026-10-18 重新核实本地/远端 Draft #334 一致，base `64be30905b88f639a464204ed1db89b7b62bc910`，main `61cd6796569ed8dfdea360caefeb5b9328745090`。Mini MCP 独立旧 Linux HEAD `a2cbcac8` 不用作编辑工作副本。原未跟踪文件保持不动。
- **准确范围**：完整 `git -c core.quotepath=false ls-tree -r --name-only HEAD` 共 **905** 跟踪文件；审计分母 **497**：246 生产 Kotlin、195 测试 Kotlin + 1 测试 fixture、14 Android XML/JSON资源配置、20 tools 源码/数据、21 构建/CI/仓库配置及 player/ui 网页源码。纳入非 docs/artifacts 目录的 `.kt/.kts/.java/.py/.sh/.ps1/.bat/.xml/.yml/.yaml/.html/.css/.js/.ts/.tsx/.jsx/.json/.properties` 及 `gradlew/.gitignore/.gitattributes`。`docs/`、`artifacts/` 的历史文档/报告/数据，以及 README/AGENTS 说明、两个二进制 PNG/JAR 排除语义源码分母；AGENTS 是已读治理权威而非产品源码。构建输出与未跟踪 node_modules/package 文件不在 Git tree，不计入。完整树由程序读取并核对数量，终端大段输出曾被截断，不用该输出冒充完整清单。
- **逐项状态**：本节每条只有在全文读取并核查真实生产消费者后才给最终分类；本批已审计 **71/497（14.29%）**，审计中 **2**，其余 **424** 待审。扫描、符号匹配、旧台账曾提及和代码净删率不计语义完成率。测试/工具未读全文的维持待审，不能随生产文件结论自动标成已审。
- **验证口径**：下表最终结论已做源码静态审计、声明/实际引用交叉检索（app/src/main、app/src/test、tools、.github）；这不是新运行的行为测试。现有完整代码验收仍是 `d9fb47f2` 的 Android CI #4369 / R2 #3949 SUCCESS；文档 HEAD 的 CI #4370 / R2 #3950 SUCCESS，Android skipped。本轮尚未改产品/测试/CI；本地已运行 `:app:testFast --tests *GatewayTokenEnvelopeV1Test --tests *MurmurHash3Test --no-daemon`，BUILD SUCCESSFUL，3用例0失败；其余表项仅静态审计，未运行真机或付费 API。
- **首批整组建议**：① `PairInformationOutcome.kt` 为独立孤立叶子，列必须删除；此审计批先记录、不靠删一个叶子替代全树审计。② 攻击/继承/毒/Klutz 规则计算及 flow eligibility 为混合生产链，必须先保留实际结果输入和 Session 状态应用再退休。③ Provider/日间 causal DTO 仍有合法候选/真值标签和重复决策历史，需联合实际消费者审计；不能删活跃 AI。④ 目录、文本、时序、密钥、canonical state 与只读 AI 快照不是旧规则真值引擎，保留必要责任。
- **风险与待办**：所有手工事实应能绕过规则生成步骤/合法菜单，事实 seat/权限/时序/去重/原子性仍保留。App **178170B**、HostScreen **101971B** 仍超限；其整文件审计尚在进行。人工审核项 U1：是否退出无生产用户入口的 custom/homebrew 导入分支；两内置脚本均只有 official references。推荐删除未上线分支，保留两剧本解析/目录；未决前不删。详见 parser/normalizer 两行。本批生产删除 0、专属测试删除 0、移除链 0、净代码缩减 0；不宣称 P1/P4 或全仓审计完成。

表内路径以仓库根为基准。所有条目的证据 HEAD 均为上列完整 SHA；“待审”的 `—` 表示没有最终结论，“审计中”不计完成；“静态”验证同上，后续运行结果另补，不能从历史 CI 推断新改动已编译。

| 文件（当前完整范围，每路径恰好一次） | 字节 | 状态 | 最终分类 | 真实消费者 / 证据与处理边界 |
|---|---:|---|---|---|
| `.gitattributes` | 296 | 待审 | — | 尚未形成有证据结论 |
| `.github/workflows/api0-offline.yml` | 835 | 待审 | — | 尚未形成有证据结论 |
| `.github/workflows/ci.yml` | 11704 | 已审计 | 需要解耦 | GitHub push/PR/dispatch→Android JVM/APK、ASP、Clingo、gate；保留 exact-head 分类、full-ci、Android和独立gate；ASP/Clingo 仍被广泛路由但旧引擎已删，须读oracle工具实际合同再决定退役哪些job/触发路径，不能依据绿灯维持旧Rules。（静态，运行结论仅明确列出者） |
| `.github/workflows/field-test-apk.yml` | 13182 | 待审 | — | 尚未形成有证据结论 |
| `.github/workflows/p1-wire-fixture.yml` | 3164 | 待审 | — | 尚未形成有证据结论 |
| `.github/workflows/prod1-gateway-offline.yml` | 990 | 待审 | — | 尚未形成有证据结论 |
| `.github/workflows/r2-write-probe.yml` | 14336 | 已审计 | 需要解耦 | GitHub PR/push/dispatch→verify-boundary；现行守卫多为旧文件/函数存在、命名policy禁入和UI位置，不是实际privacy/Session行为证明；随职责删除退休过时字符串守卫，保留实际架构退化检测，不能为其重建旧控件。（静态，运行结论仅明确列出者） |
| `.gitignore` | 233 | 待审 | — | 尚未形成有证据结论 |
| `app/build.gradle.kts` | 2215 | 已审计 | 必须保留 | settings.gradle.kts include(:app)、CI Android/FieldTest jobs；实际唯一 Android 应用构建、依赖、版本、17字节码及 JVM测试入口。testFast 当前无 exclude，沿用 debugUnitTest 的 classes/classpath；testFull dependsOn testDebugUnitTest，不用历史排除清单推执行范围。（静态，运行结论仅明确列出者） |
| `app/src/main/AndroidManifest.xml` | 1369 | 已审计 | 必须保留 | Android打包/启动器→Application/MainActivity/FileProvider；真实平台入口、INTERNET与私有debug FileProvider，反射声明消费者不能靠Kotlin零引用判死；密钥另在noBackup/Keystore，allowBackup 的游戏隐私范围待对应存储消费者核查。（静态，运行结论仅明确列出者） |
| `app/src/main/assets/rules/trouble_brewing.json` | 3031 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/assets/scripts/no_greater_joy.json` | 548 | 已审计 | 必须保留 | BuiltInCatalog.loadScript→Parser/Normalizer→Flow/Setup/AI；12条：1 _meta+11 official role引用，包含首/后夜顺序；无custom definition，NGJ活跃产品不因清理门禁退出。（静态，运行结论仅明确列出者） |
| `app/src/main/assets/scripts/trouble_brewing.json` | 894 | 已审计 | 必须保留 | BuiltInCatalog.loadScript→Parser/Normalizer→Flow/Setup/AI；23条：1 _meta+22 official role引用，包含首/后夜顺序；无custom definition，不能随旧Host Rules引擎删当前剧本内容。（静态，运行结论仅明确列出者） |
| `app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json` | 331689 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/assets/setup/trouble_brewing_setup_presets_v3.schema.json` | 1650 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppClocktowerLandingScreen.kt` | 6026 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppDealScreens.kt` | 13556 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppGameModels.kt` | 464 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppGameReviewScreens.kt` | 12093 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppSettingsScreen.kt` | 10085 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/BotcAiTrace.kt` | 3982 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt` | 178170 | 审计中 | — | 已读取部分职责/调用，待全文和所有消费边界核对；不计完成 |
| `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApplication.kt` | 367 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerAppModels.kt` | 1007 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerArtistTableState.kt` | 2237 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerAutomaticRulings.kt` | 605 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerBeginnerCompactNightUi.kt` | 7186 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerBeginnerNightGuidance.kt` | 4873 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidPresentationSemantics.kt` | 882 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidSquareTableUi.kt` | 8699 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidStepMaterializer.kt` | 1647 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerContrastPolicy.kt` | 1565 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerCurrentDemonAuthority.kt` | 2751 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerCurrentResidualScopeV2.kt` | 7465 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDayNominationGesture.kt` | 2043 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDayOverviewTableState.kt` | 1280 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDemonAttackProductionAdapter.kt` | 6684 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDemonBluffPresentation.kt` | 2251 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDirectBooleanResult.kt` | 1495 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDirectNumericResultUi.kt` | 5398 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDynamicNightAdvance.kt` | 2118 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerEvilInfoSquareTableUi.kt` | 8274 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerFirstNightEvilInformationPresentation.kt` | 8559 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerFirstNightInformationRequest.kt` | 3696 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt` | 8796 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGhostVoteAuthority.kt` | 913 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGhostVotePresentation.kt` | 304 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGlobalFirstNightDecision.kt` | 2158 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGlobalFirstNightScalarDecision.kt` | 3678 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostBottomInsetPolicy.kt` | 1233 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostCoreSemantics.kt` | 6943 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostFullScreenScaffold.kt` | 2726 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostPresentationModels.kt` | 3747 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSeatContentPresentation.kt` | 2600 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSelectionSemantics.kt` | 10583 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSquareTableScaffold.kt` | 2788 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableContract.kt` | 5651 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableGameStateProjection.kt` | 1916 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableLayout.kt` | 12909 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableReorder.kt` | 2043 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableSurfaceLayout.kt` | 3596 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt` | 7643 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerInformationStepBuilder.kt` | 5970 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerJointOpeningDisplayMatcherV2.kt` | 7042 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerJointOpeningReviewScreen.kt` | 4438 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerKlutzTableState.kt` | 2629 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNeutralInformationPreparation.kt` | 7236 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt` | 10146 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightFullScreenOwnership.kt` | 3756 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightHostProjection.kt` | 18178 | 已审计 | 需要解耦 | HostScreen→NightHostProjectionFactory；effective state 供 materializers/夜间步骤；只读外壳仍自行推 death、Sage/Ravenkeeper eligibility、succession candidate、醉毒效果；保留实际已确认状态/事件的时序读投影，退出预测结果冒充 confirmedEvents，不能拆第二 writable GameState。（静态，运行结论仅明确列出者） |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightPresentationRole.kt` | 710 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepMaterializerRegistry.kt` | 3241 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt` | 44505 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightTargetLegality.kt` | 305 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNumericInformationOptionPreparation.kt` | 1317 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt` | 14493 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPairManualAuthority.kt` | 3870 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPairManualSelectionModel.kt` | 5862 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPairManualSelectionUi.kt` | 1683 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPairPlayerRevealPresentation.kt` | 3074 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPhaseStorytellerAdapter.kt` | 407 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlainInformationSquareTableUi.kt` | 6219 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayResolution.kt` | 3001 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayUi.kt` | 15450 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerRevealHandoff.kt` | 2149 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPresentationRoleCatalog.kt` | 7722 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPresentationTheme.kt` | 3168 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRavenkeeperSquareTableUi.kt` | 11251 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRecurringInformationStepMaterializers.kt` | 1897 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationDecisionControls.kt` | 7221 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationInteractionState.kt` | 4023 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerResidualDisplayMatcherV2.kt` | 12977 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerResidualRequestIdentityV2.kt` | 947 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRoleLocalization.kt` | 583 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRoleRevealChoiceProjection.kt` | 2263 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRuleOnlyCompactChoice.kt` | 352 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSageSquareTableUi.kt` | 11900 | 已审计 | 需要解耦 | NightStepUi.clocktowerSageResultChoices→ClocktowerSageSquareTableDialog；保留稳定 seat 高亮和明确确认回调；当前 manual 是 choices 下拉且 table enabled=false，不能录菜单外 pair；去 LegacyUnreliable/规则候选与默认 first fallback，改任意结构合法 pair。（静态，运行结论仅明确列出者） |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSageStepMaterializer.kt` | 5333 | 已审计 | 需要解耦 | HostScreen.sagePair/storytellerPairHint→clocktowerSageStepMaterializer；NightStepUi 消费 step；枚举排除 actor 的全 pair、计算 isTruthful 并以真实 Demon 构建默认展示；改为结构化实际选定两 seat，不从真值默认值或菜单生成；保留 AnyOf 公布命题与 actor/recipient/revision。（静态，运行结论仅明确列出者） |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSeatNumberBadge.kt` | 2796 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSeatNumberLabel.kt` | 390 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSessionPlayerCards.kt` | 2544 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleNumericChoiceUi.kt` | 4197 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleRecommendationChoiceUi.kt` | 2362 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionPresentation.kt` | 3915 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionUi.kt` | 6760 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSlayerTableState.kt` | 3543 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableLifeMarkers.kt` | 434 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatDensityPolicy.kt` | 3360 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt` | 35812 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerStorytellerRecommendationUi.kt` | 7356 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerStructuredInformationPreparation.kt` | 756 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerTableVoteState.kt` | 3141 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt` | 11451 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerVoteHistoryRecord.kt` | 1024 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/CommonPlayersDefaultPolicy.kt` | 415 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/HostBottomActionBar.kt` | 3802 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/HostInteractionUi.kt` | 844 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/HostSeatingRoster.kt` | 2101 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/HostSeatingSetupFlow.kt` | 742 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/MainActivity.kt` | 1103 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/PlayerExperiencePicker.kt` | 1921 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/SeatingFirstSetupUi.kt` | 10527 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/StorytellerAiSettingsPanel.kt` | 8104 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/StorytellerOperationMode.kt` | 2026 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/TroubleBrewingGameSnapshotJsonCodec.kt` | 7985 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/BuiltInClocktowerRulesetCatalog.kt` | 3528 | 已审计 | 必须保留 | App、HostScreen；生产调用 fromContext.ruleset；现用两内置剧本资产/元数据入口，延迟缓存是静态内容；parse/adapter 均真实消费，不能按 legacy 历史注释退休。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerCatalogModels.kt` | 6756 | 已审计 | 需要解耦 | Flow、Setup、InitialOpening、GrimoireReminderToken、Catalog；保留角色文本/token/ValidatedRuleset；ClocktowerScriptCatalog 仅 catalog 专属测试调用，homebrew/extra team 扩展须与生产内置数据模型分清再减，不能整文件删除。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptJsonParser.kt` | 21423 | 已审计 | 需要人工审核 | RulesetJsonLoader.parseScript→BuiltInCatalog，仅两内置 assets；直接解析消费者仅测试；保留 official ID/_meta/顺序解析；custom角色、homebrew、jinx/special/URI 导入当前没有用户入口，两资产均无 custom definition。建议退出未上线自定义导入分支；风险是删去未来导入能力及专属测试，须所有者决定是否仍属产品范围；不能整删现用 parser。（静态，运行结论仅明确列出者） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptNormalizer.kt` | 13636 | 已审计 | 需要人工审核 | RulesetJsonLoader→BuiltInCatalog；没有外部脚本导入入口；保留两内置脚本规范化、内容身份及顺序；customDefinition/homebrew/extra teams 是上一行同一产品决策，推荐连同 parser 未上线分支整体缩减。确认范围前保留，不能靠现有测试默认永久保留或直接整删。（静态，运行结论仅明确列出者） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptValidation.kt` | 1161 | 已审计 | 必须保留 | ScriptJsonParser、ScriptNormalizer→BuiltInCatalog；JSON/字段/ID/重复等格式错误类型，非已确认信息规则门禁；随 parser 未来瘦身缩减枚举即可。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/LegacyRulesetCatalogAdapter.kt` | 2594 | 已审计 | 必须保留 | BuiltInClocktowerRulesetCatalog.load；将真实 rules asset 文本和顺序组装 registry；无候选/事实否决，独占生产消费者尚在，需等目录来源替换才可退役。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/NoGreaterJoyOfficialCharacterMetadata.kt` | 3502 | 已审计 | 必须保留 | BuiltInClocktowerRulesetCatalog.load；现用 NGJ 角色文本/顺序/提醒元数据来源，无 truth 计算；不能借 Host Rules 清理删除支持剧本。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/OfficialReminderTokenMetadata.kt` | 3220 | 已审计 | 必须保留 | BuiltInClocktowerRulesetCatalog→GrimoireReminderToken；真实魔典 reminder token 字典及来源版本；结构完整性检查，已确认标记/私密展示消费者须保留。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ActionFact.kt` | 11069 | 已审计 | 需要解耦 | Session、ActionFactDraft/Timeline、SemanticJson、GlobalEventProjection、ProviderHistoryPrefix；ActionFact 和有序 reducer 必须保留；no-self nomination/Butler/Klutz 与 hit⇒consumed 仍是规则约束，不能把已确认事实拒绝为不合法；保留 seat/ID/sequence/原子性。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerGameStateAdapter.kt` | 1817 | 已审计 | 需要解耦 | App、TroubleBrewingSetupCommitter；是旧 PlayerCard→canonical immutable 输入适配，非第二写入者；以 name 匹配毒和死亡自动清毒需改为稳定 seat/明确事实，不能直接删除开局适配。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerSemanticHistoryMode.kt` | 2302 | 已审计 | 必须保留 | GameSnapshot、Session、ProviderHistoryPrefixMaterializer、StrategicContinuation；显式 legacy/global 模式、防未来或冲突序列，避免从缺失历史捏造事实；迁移策略不等于独立恢复产品。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt` | 2462 | 已审计 | 必须保留 | SetupCommitter/Provider/ShownIdentity/Template、GameSnapshotProjector、RotationRecordFactory；确切已确认初始身份不可由 provenance 重算；只作不可变输入/事实，结构检查为座位身份和非空，不算线索合法性。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/DecisionOutcomeSnapshot.kt` | 597 | 已审计 | 需要解耦 | DayAbilityRegistrationRulingProducer、ProviderPriorDecision、Session；actual registration 仍使用，非孤立；与 causal journal candidate/ability/truth 元数据一同收敛到唯一实际事件后再删除多余包装，不能丢明确登记。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/DecisionTypes.kt` | 1115 | 已审计 | 需要解耦 | DayAbilityRegistrationRulingProducer、ProviderContract、SemanticModel/Json、CausalJournal；保留明确 RegistrationFact/Question；truthRelation 与 AbilityState 标签须退出 Host 规则裁决，现用类型/codec/AI prior 有真实消费者，不能整删。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/DynamicDecisionModels.kt` | 134 | 已审计 | 必须保留 | Session、ActionFact、Snapshot、Timeline、StrategicContinuation；实际仅 StorytellerPhase 枚举；不是旧动态候选引擎，保留阶段语义，暂不机械一文件合并。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/GameSnapshot.kt` | 2000 | 已审计 | 必须保留 | Session、DynamicActionReducer、GameSnapshotProjector、SemanticModel；唯一 canonical state 的不可变包络+两类全局绑定事实视图及 revision，不能按 Snapshot 名称当第二写入状态；仍须后续核对唯一日志物理收敛。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/GameState.kt` | 1305 | 已审计 | 必须保留 | Session、SetupCommitter、Snapshot、AI/RoleCatalog、UI projections；唯一玩家/seat/角色/阵营/存活/毒事实类型，seat 唯一与正数为结构安全；无线索真值计算。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/InformationModels.kt` | 1003 | 已审计 | 需要解耦 | FirstNightInformationRequest/Migration、GlobalFirstNightScalar/Pair、ProviderContract、StepBuilder；Reliability/YesNo/AbilityObservation 仍供现用 AI；请求已用 NOT_APPLICABLE，清旧 truth/registration 候选元数据要先迁消费者，不能删配对推荐。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/MurmurHash3.kt` | 3775 | 已审计 | 必须保留 | SetupDiversitySelector、SetupShownIdentityCommitment、SetupDealPlanner、NoGreaterJoySetupPreparer；实际种子/稳定身份哈希仍被生产发牌使用；非 orphan candidate ID，保留 hash vectors 验证。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/PairInformationOutcome.kt` | 869 | 已审计 | 必须删除 | 无生产/测试/tools/.github 外部引用；rg 唯一命中自身声明；完整类只有 shownRole/target/decoy 及旧候选形状；与现用 PendingPairInformationDecision/OpeningRecommendationV2 不同；可整删 1 生产文件，无专属测试，不影响活跃 pair producer。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/RoleCatalogAdapter.kt` | 1337 | 已审计 | 必须保留 | App、BuiltInCatalog、SetupCommitter/Rotation、StrategicContinuation；内置角色与 script ID 的平台适配，服务实际身份与 AI；不计算候选或信息真值。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/RulesetRef.kt` | 756 | 已审计 | 必须保留 | GameSnapshot、Session、Catalog、Persistence、SemanticJson、Oracle fixtures；内容版本/来源/覆盖标签身份；只校验格式不产生规则 verdict。coverage 后续用途单独审，不以未验证标签否决事实。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerPlayerContext.kt` | 1439 | 已审计 | 必须保留 | AppSettings、Seating、AppPreferences、Session、ProviderGameContextBuilder、StrategicContinuation；现用 AI 经验/claims/压力输入；独立于 actual role 的声明，默认和去重为结构，保护战略产品能力。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerProviderContractV1.kt` | 17196 | 已审计 | 需要解耦 | RequestFactory、Initial/Scalar/Pair/Mayor consumers、CausalJournal、StrategicContinuation；身份/revision/seat/history cutoff 保留；legalCandidates、candidate payload 与 prior truth/ability 是剩余旧耦合，先验证各真实 AI context 不依赖菜单再删；不得停用请求/解析。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerProviderHistoryPrefixV1.kt` | 9442 | 已审计 | 必须保留 | ProviderHistoryPrefixMaterializer、GlobalEventProjection、StrategicContinuation、ProviderContract；不可变冻结事实前缀、明确 UNKNOWN/PARTIAL、recipient 和 cutoff；供 AI 连续策略，非独立文本复盘。Action.copy 对 list 字段浅拷贝是后续不可变性核查项，未断言 bug。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/TroubleBrewingGameSnapshot.kt` | 5165 | 已审计 | 必须保留 | RuntimeGameProjector、GameSnapshotProjector、InitialOpeningRequest/Joint、MayorBoundary、JsonCodec；只读真实座位/状态投影及 known/uncommitted/unknown，不自算信息真值；保留现用 AI 请求；不是另一份可写 GameState。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/ActionFactDraft.kt` | 12179 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/ActionFactTimeline.kt` | 5094 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicContracts.kt` | 848 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicObservationDraft.kt` | 3378 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicObservationLog.kt` | 9783 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicSemanticJson.kt` | 29221 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicSemanticModel.kt` | 22154 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/GrimoireReminderToken.kt` | 3201 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/PlayerHistoricalTimeline.kt` | 7026 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerCharacterInteractionRegistry.kt` | 8465 | 已审计 | 需要解耦 | ClocktowerHostInteractionProjector→ProductionFirst/OtherNightFlow；保留私密角色变更通知及真实步骤身份；Ravenkeeper/Sage/Undertaker 等 eligibility 决定步骤是否存在，需改为实际交互可录入而非规则唯一通道。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerFlowPlanner.kt` | 4731 | 已审计 | 需要解耦 | ProductionFirstNightFlow、ProductionOtherNightFlow；保留规范夜间顺序/系统边界；off-script require、>=7 邪恶信息和 inPlay 过滤是规则筛选，不能成为实际信息/动作的唯一可表示范围。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerHostInteraction.kt` | 1576 | 已审计 | 必须保留 | HostPresentationModels、NightHostProjection、NightStepMaterializerRegistry、Flow/EffectiveNightState；稳定 interaction ID、phase、kind 与完成类型，只有形状约束，无候选/真值推理；不新增可写 Context。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerHostInteractionProjector.kt` | 5245 | 已审计 | 需要解耦 | ProductionFirstNightFlow、ProductionOtherNightFlow；保留稳定 token→interaction 身份；actualRole setup 过滤和 registry eligibility 属于规则耦合，手工操作不能只允许这些生成步骤。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionFirstNightFlow.kt` | 2855 | 已审计 | 需要解耦 | ClocktowerHostScreen、NightStepMaterializerRegistryTest；当前真实首夜顺序消费者，不能按 shadow 注释判死；保留顺序/身份，去掉 functioningRole→依赖筛选和 exact-set 对手工入口的限制。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionInteractionOrderer.kt` | 6257 | 已审计 | 需要解耦 | ProductionFirst/OtherNightFlow、HostScreen、NightHostProjection、StepMaterializers；保留重复 ID/phase 不混合；projectedSet==productionSet 将规则生成集合升为硬边界，需与手工实际交互登记解耦。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionOtherNightFlow.kt` | 2198 | 已审计 | 必须保留 | ClocktowerNightHostProjection；当前纯组合已给出的 resolvedFacts/顺序，不自行判死亡或候选；保留窄职责，依赖的 planner/registry 门禁在其所有者处拆除。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerResolvedFlowFacts.kt` | 877 | 已审计 | 需要解耦 | NightHostProjection→CharacterInteractionRegistry；混合实际 EXECUTION/角色变化与 MAYOR_REDIRECT_ELIGIBLE/DEMON_SUCCESSION_REQUIRED 等推导标签；改从实际事件/未完成操作读取，不能存第二历史。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyPlanner.kt` | 3658 | 已审计 | 必须保留 | FirstNightSetupDependencyProjection；仅按 interaction 顺序找已声明事实首次消费点；防止展示未确认 setup，保留 identity/顺序检查，角色规则在 projector 端清理。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyProjection.kt` | 11867 | 已审计 | 需要解耦 | ProductionFirstNightFlow；保留实际 bluff/红鲱鱼公布前确认；FortuneTeller RESULT_SEMANTICS_REQUIRED、functioning Spy 观察依赖属于旧真值/醉毒规则耦合，删除后不能影响真实魔典记录。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/persistence/ClocktowerRulesetPersistence.kt` | 1848 | 已审计 | 需要解耦 | App.troubleBrewingRulesetRefFor、开局 rulesetBasis；现用 setup role-set 哈希与 catalog script hash 两种身份并行；先核对 Snapshot/历史消费者选一种内容身份，不能直接废弃现用 RulesetRef 或引入兼容恢复壳。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/recommendation/StorytellerDecisionAuthority.kt` | 1711 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/AbilityFunctioningSemantics.kt` | 2689 | 已审计 | 需要解耦 | DemonAttackProductionAdapter、NightHostProjection、Sage/InformationStepBuilder、NominationInteraction；保留实际角色/所示角色读取；functionsAs/oneShotDecision 的醉毒、存活与效果裁决退出提交/结果选择，先给现用夜间和日间操作保留明确结果入口。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerEffectiveNightState.kt` | 3936 | 已审计 | 必须保留 | NightHostProjection、NightDawnResolutionPlanner、NightTransactionRestoreComposition；按已确认死亡/角色变化和 interaction BEFORE/AFTER 投影，不枚举合法信息；保留唯一时间线输入与稳定顺序，不能成为第二个可写状态。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerOptionalNightSourceChronology.kt` | 1387 | 已审计 | 必须保留 | ClocktowerNightHostProjection.effectivePoisonTarget；仅检查 source/cursor 在当夜计划中并比较时序；保留未知 source 不伪造已行动，不承担毒效果合法性。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/CurrentDemonAuthority.kt` | 655 | 已审计 | 需要解耦 | ClocktowerCurrentDemonAuthority、TroubleBrewingDemonSuccessionResolver；singleOrNull(living Demon) 仍决定行动资格；保留身份不歧义边界，取消以角色/存活规则替代用户确认来源的依赖后再缩减。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/DemonNightAttackSemantics.kt` | 1752 | 已审计 | 需要解耦 | ClocktowerDemonAttackProductionAdapter、NightDawnResolutionPlanner、TroubleBrewingDemonSuccessionResolver；当前自行算 Soldier/Monk/醉毒/Mayor/自杀后果；先把实际死亡、转移和继承结果改为确认输入，保留 Session 原子应用后再整删计算器及规则专属测试。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/DemonSuccessionSemantics.kt` | 1186 | 已审计 | 需要解耦 | App、CurrentDemonAuthority、NightHostProjection、NightDawnResolutionPlanner、SuccessionResolver；5人阈值/红唇强制与爪牙候选属于旧规则计算；保留已确认 RoleChange/私密身份通知，退出强制/候选生产链后删实现。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/NoGreaterJoyKlutzChoiceRuleV1.kt` | 2569 | 已审计 | 需要解耦 | App、NoGreaterJoyKlutzHistoryProducerV1；按存活/真实角色/阵营校验并算 evilWins；保留学死通知、玩家实际选择与公开事件，独立确认实际终局，不能整删 Klutz 操作。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/PoisonEffectLifecycle.kt` | 480 | 已审计 | 需要解耦 | NightHostProjection、NightDawnResolutionPlanner；effectiveTarget 受 functioning、afterNight 受 poisonerAlive 控制；保留明确毒状态变化和阶段事件，退出规则推导；先核对 Dawn 清毒的原子应用。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/RulesetContentHasher.kt` | 2875 | 已审计 | 必须保留 | TroubleBrewingRulesetPersistence.refFor；现用开局 RulesetRef 内容身份哈希，非信息真值或行动许可；有第二 catalog 哈希但用途/输入不同，不能按算法相似直接删除。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/RulesetJsonLoader.kt` | 3108 | 已审计 | 必须保留 | App、BuiltInClocktowerRulesetCatalog；parse 的旧 rules asset 仍是文本/顺序 registry 来源，parseScript 仍加载两内置脚本；没有据此判信息正确，命名 legacy 不等于孤立。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/RulesetKnowledge.kt` | 995 | 已审计 | 必须保留 | RulesetJsonLoader、LegacyRulesetCatalogAdapter、ContentHasher、RulesetPersistence；角色文本/顺序/jinx 数据及结构检查；服务现用目录和内容身份，不能随旧 Rules 门禁整包删。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/BotcAiWireTrace.kt` | 6748 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt` | 39575 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionAliveBoundaries.kt` | 2512 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionPoisonBoundaries.kt` | 4846 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionRoleIdentityBoundaries.kt` | 1629 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerNightCheckpoint.kt` | 1320 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerNightTargets.kt` | 1844 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerVoteStanding.kt` | 633 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/DawnCommitIdentity.kt` | 992 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/DayAbilityRegistrationRulingProducerV1.kt` | 17986 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationLifecycle.kt` | 911 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationMigration.kt` | 3609 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/HostCurrentOpenDecisionInventoryV2.kt` | 4553 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/InitialJointOpeningDirectV2.kt` | 21911 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/InitialOpeningRequestV2.kt` | 4144 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/MayorRedirectDecisionBoundary.kt` | 1561 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlanner.kt` | 11857 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/NightTransactionRestoreComposition.kt` | 5660 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/NoGreaterJoyKlutzHistoryProducerV1.kt` | 3449 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/OpeningRecommendationV2.kt` | 3975 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/PendingPairInformationDecision.kt` | 530 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/PersonalDirectOpenAiV1.kt` | 10171 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerCausalDecisionJournalV1.kt` | 17613 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerDecisionFoundation.kt` | 975 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerGlobalEventProjectionV1.kt` | 9133 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderGameContextBuilderV1.kt` | 3306 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1.kt` | 3345 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderRequestFactoryV1.kt` | 5331 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerResidualPackageGateV2.kt` | 2795 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicChangeCheckpointV2.kt` | 8667 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicContinuationV2.kt` | 30293 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/TroubleBrewingDemonSuccessionResolver.kt` | 6161 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/TroubleBrewingRuntimeGameProjector.kt` | 3512 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupDistribution.kt` | 1018 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProvider.kt` | 4186 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSource.kt` | 5567 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/NoGreaterJoyProductionSetupPreparer.kt` | 3562 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversityHistory.kt` | 1550 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelector.kt` | 11312 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitment.kt` | 4807 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicy.kt` | 8649 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepository.kt` | 2996 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDealRoleResolver.kt` | 2756 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDrunkSelectionRouter.kt` | 2488 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingGameSnapshotProjector.kt` | 7788 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingIntermediateSetup.kt` | 5166 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingProductionSetupPreparer.kt` | 4782 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupCommitter.kt` | 5650 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupDealPlanner.kt` | 13250 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetJson.kt` | 5548 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetModels.kt` | 1083 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetValidator.kt` | 8574 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupRotationHistory.kt` | 2896 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupRotationRecordFactory.kt` | 11970 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerArtistInteraction.kt` | 6603 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerArtistTableUi.kt` | 4130 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayOverviewTableUi.kt` | 6871 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayScreen.kt` | 13540 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayTableScaffoldUi.kt` | 1002 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDrunkSelectionScreen.kt` | 8884 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt` | 101971 | 审计中 | — | 已读取部分职责/调用，待全文和所有消费边界核对；不计完成 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerKlutzTableUi.kt` | 5712 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerNightScreen.kt` | 15696 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerNominationInteraction.kt` | 5956 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerPendingNominationTableUi.kt` | 7382 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerResultsScreen.kt` | 17211 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerSetupScreen.kt` | 20901 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerSlayerInteraction.kt` | 3693 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerSlayerTableUi.kt` | 8133 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerVoteTableUi.kt` | 7854 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/StorytellerAutoPauseScreen.kt` | 2721 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/debug/DebugFlightRecorder.kt` | 8403 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/debug/DebugFlightRecorderCore.kt` | 2706 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/persistence/AppPreferencesStore.kt` | 4026 | 已审计 | 必须保留 | CampBoardGameHostApp；语言/常用玩家/AI操作模式/经验偏好；非 canonical 游戏状态或重复历史。保留 MANUAL 默认与 AI 模式开关。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/GatewayTokenEnvelopeV1.kt` | 1294 | 已审计 | 必须保留 | StorytellerGatewayConnectionStore；AES-GCM envelope 长度/IV/version parser，保护设备密钥传输；无 token 输出或规则计算。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/PersonalOpenAiSettingsStore.kt` | 1273 | 已审计 | 必须保留 | App→PersonalDirectOpenAiV1；真实 direct 模型/启用/密钥配置，分离 gateway scope；无授权不发付费请求，不能当实验删现用 AI。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/StorytellerGatewayConnectionStore.kt` | 4649 | 已审计 | 必须保留 | App、PersonalOpenAiSettingsStore；AndroidKeyStore AES-GCM、scope/AAD 隔离、AtomicFile/noBackup 原子密钥持久化，必要安全支撑；不能与普通偏好或游戏历史合并。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/TroubleBrewingSetupRotationHistoryStore.kt` | 12917 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/drawable/ic_launcher.xml` | 768 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values-en/debug_strings.xml` | 472 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values-en/strings.xml` | 6858 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values-v27/themes.xml` | 479 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values/debug_strings.xml` | 426 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values/strings.xml` | 6616 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values/themes.xml` | 412 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/xml/debug_file_paths.xml` | 208 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerAiPairRecommendationRecoveryTest.kt` | 4932 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerArtistDraftTest.kt` | 2172 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerAutomaticRulingsTest.kt` | 1777 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerBeginnerCompactNightPresentationTest.kt` | 1155 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerBeginnerNightGuidanceTest.kt` | 4298 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidPresentationSemanticsTest.kt` | 992 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidSelectionAuthorityTest.kt` | 853 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidStepMaterializerTest.kt` | 1566 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChefAlternativePublicationTest.kt` | 2457 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerContrastPolicyTest.kt` | 2991 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerCurrentDemonAuthorityTest.kt` | 4137 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerCurrentResidualScopeV2Test.kt` | 19070 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDayNominationGestureTest.kt` | 2643 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDayOverviewTableStateTest.kt` | 2519 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDemonAttackDawnFactsTest.kt` | 4312 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDemonAttackProductionAdapterTest.kt` | 3325 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDemonBluffPresentationTest.kt` | 3571 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDirectBooleanResultTest.kt` | 1687 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDirectNumericResultTest.kt` | 1960 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDisplayedInformationReliabilityTest.kt` | 2112 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDynamicNightAdvanceTest.kt` | 1930 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerEvilInfoSquareTablePresentationTest.kt` | 4197 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFirstNightEvilInformationPresentationTest.kt` | 4882 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFirstNightInformationRequestTest.kt` | 3674 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFirstNightPlayerRevealHandoffTest.kt` | 6649 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFortuneTellerPhaseAuthorityTest.kt` | 2658 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFortuneTellerRuleOnlyAuthorityTest.kt` | 1107 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTablePresentationTest.kt` | 3277 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerGhostVoteAuthorityTest.kt` | 2641 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerGhostVotePresentationTest.kt` | 1930 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerGlobalFirstNightScalarDecisionTest.kt` | 10699 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerGrimoireRevealGateTest.kt` | 1708 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostSeatContentPresentationTest.kt` | 4526 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableContractTest.kt` | 5504 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableGameStateProjectionTest.kt` | 6009 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableLayoutTest.kt` | 6317 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableReorderTest.kt` | 6461 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableSurfaceLayoutTest.kt` | 4245 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerJointOpeningDisplayMatcherV2Test.kt` | 7413 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerMonkRetargetRavenkeeperDeathTest.kt` | 2701 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTablePresentationTest.kt` | 3116 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightFullScreenOwnershipTest.kt` | 6754 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightHostProjectionTest.kt` | 5831 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightStepMaterializerRegistryTest.kt` | 7629 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightTargetLegalityTest.kt` | 1273 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNominationDraftTest.kt` | 1419 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNumericInformationOptionPreparationTest.kt` | 3306 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerOtherNightWakingRoleAuthorityTest.kt` | 2639 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTablePresentationTest.kt` | 6549 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPairManualAuthorityTest.kt` | 1844 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPairManualSeatPresentationTest.kt` | 1313 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPairManualSelectionModelTest.kt` | 8082 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPendingSuccessionFlowAuthorityTest.kt` | 3023 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayPresentationTest.kt` | 6681 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayResolutionTest.kt` | 5458 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRavenkeeperSquareTablePresentationTest.kt` | 2585 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRecurringInformationStepMaterializersTest.kt` | 1457 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRedHerringAutoAdvancePolicyTest.kt` | 17783 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRegistrationInteractionStateTest.kt` | 4372 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerResidualDisplayMatcherV2Test.kt` | 3455 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerResidualRequestIdentityV2Test.kt` | 1594 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRoleLocalizationTest.kt` | 620 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRoundedRectanglePerimeterLayoutTest.kt` | 6857 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRuleOnlyCompactChoiceTest.kt` | 628 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSageSquareTablePresentationTest.kt` | 7879 | 已审计 | 需要解耦 | Gradle testFast/testFull；SageSquareTableUi/PlayerDisplayResolution；保留 typed seat、不从显示文本解析、actor 与 pair 独立高亮及未知 seat 拒绝；候选来源/LegacyUnreliable/菜单预选断言随新直接 pair 合同缩减，不整删空间展示验证。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSageStepMaterializerTest.kt` | 4129 | 已审计 | 需要解耦 | Gradle testFast/testFull；SageStepMaterializer；typed 实际 pair/命题需保留；truthful-only 全合法pair、默认真实恶魔+decoy 是旧 Host 真值断言，可随职责退休；不是保护正常 AI 配对推荐的理由。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSeatNumberLabelTest.kt` | 601 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSessionPlayerCardsTest.kt` | 3910 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerShownUnreliablePropositionTest.kt` | 4641 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionPresentationTest.kt` | 7521 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSlayerDraftTest.kt` | 2344 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSlayerTableStateTest.kt` | 3471 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableLifeMarkersTest.kt` | 1151 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatDensityPolicyTest.kt` | 3003 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatLayoutTest.kt` | 4397 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerTableVoteStateTest.kt` | 3843 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTablePresentationTest.kt` | 2339 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerVoteHistoryRecordTest.kt` | 2045 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/CommonPlayersDefaultPolicyTest.kt` | 1062 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/GatewayTokenEnvelopeV1Test.kt` | 1557 | 已审计 | 必须保留 | Gradle testFast/testFull→JVM JUnit；GatewayTokenEnvelopeV1；round-trip、版本/IV/长度 fail closed 为实际密钥 envelope 合同；本轮定向运行2用例通过，无明文/网络/付费调用。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/HostRoleLocalizationTest.kt` | 1497 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/HostSeatingRosterTest.kt` | 4070 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/LiveKlutzScriptReachabilityTest.kt` | 9913 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/NoGreaterJoySetupRegressionTest.kt` | 1100 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/StorytellerNewGameConfigurationTest.kt` | 3716 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/StorytellerOperationModeContractTest.kt` | 1313 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/TwoPlayerSelectionActionTest.kt` | 2645 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/TwoPlayerSelectionRevalidationTest.kt` | 2397 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/BuiltInClocktowerRulesetCatalogTest.kt` | 2247 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptCatalogTest.kt` | 10823 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/OfficialReminderTokenMetadataTest.kt` | 5307 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/TroubleBrewingCanonicalScriptAssetTest.kt` | 3369 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerGameStateAdapterTest.kt` | 2151 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerSemanticHistoryModeTest.kt` | 5985 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetupTest.kt` | 2547 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/GameSnapshotTest.kt` | 1545 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/GameStateTest.kt` | 635 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/MurmurHash3Test.kt` | 872 | 已审计 | 必须保留 | Gradle testFast/testFull→JVM JUnit；生产 setup hash 消费者；保留 reference vectors，已不含退休 candidate ID 测试；本轮定向运行1用例通过。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/StorytellerProviderContractV1Test.kt` | 5042 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/A4RuntimeFixtures.kt` | 1520 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ActionFactTimelineContractTest.kt` | 5514 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ActiveRulesetGrimoireReminderTokenTest.kt` | 4098 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ClocktowerPrivateObservationRecordIdTest.kt` | 2045 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/CrossTypeTimelineCompatibilityTest.kt` | 5263 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicSemanticModelTest.kt` | 6887 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/FormalActionTimelinePersistenceTest.kt` | 4956 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/GrimoireReminderTokenBoundaryTest.kt` | 6148 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/GrimoireTruthBindingPersistenceTest.kt` | 3250 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ObservationTimelineBindingTest.kt` | 7764 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ShownRoleAtSemanticContractTest.kt` | 899 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/fixtures/TroubleBrewingFixtures.kt` | 3370 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/fixtures/g10-game2-precommit-tbgs-v1.json` | 2116 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerDrunkFortuneTellerFlowTest.kt` | 4367 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerFlowPlannerTest.kt` | 6150 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerHostInteractionProjectorTest.kt` | 10341 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerNewDemonIdentityContractTest.kt` | 1979 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionFirstNightFlowTest.kt` | 4489 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionInteractionOrdererTest.kt` | 9550 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionOtherNightFlowTest.kt` | 4667 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyPlannerTest.kt` | 7875 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyProjectionTest.kt` | 8894 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/NoGreaterJoyStructuralProofTest.kt` | 7788 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/recommendation/StorytellerDecisionAuthorityTest.kt` | 2203 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/AbilityFunctioningSemanticsTest.kt` | 7212 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerEffectiveNightStateTest.kt` | 9553 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerOptionalNightSourceChronologyTest.kt` | 2131 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/DemonNightAttackSemanticsTest.kt` | 3221 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/DemonSuccessionSemanticsTest.kt` | 4187 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/PoisonEffectLifecycleTest.kt` | 1271 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/RulesetContentHasherTest.kt` | 2515 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/AcceptedSessionActionTestFixture.kt` | 520 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/BotcAiWireTraceCodecTest.kt` | 1579 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionAliveBoundaryTest.kt` | 6135 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionConfirmedActionBoundaryTest.kt` | 11333 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionPhaseBoundaryTest.kt` | 5343 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionPoisonBoundaryTest.kt` | 8150 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionRoleIdentityBoundaryTest.kt` | 3371 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionTest.kt` | 16466 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGlobalObservationCommitTest.kt` | 9807 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerHistoricalActionObservationCaptureTest.kt` | 6397 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerNightVoteOwnershipTest.kt` | 7461 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerPoisonSettlementTest.kt` | 5511 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/DawnCommitBoundaryTest.kt` | 4486 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/DayAbilityRegistrationRulingProducerV1Test.kt` | 8345 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/DayVirginSpyRegistrationProducerV1Test.kt` | 6984 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationLifecycleTest.kt` | 613 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationMigrationTest.kt` | 4195 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/InitialJointOpeningDirectV2Test.kt` | 24971 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/MayorRedirectDependencyInvalidationTest.kt` | 4928 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnOrdinaryPoisonOwnershipTest.kt` | 3286 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnPoisonClearIntentTest.kt` | 4176 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlannerAttackOutcomeContractTest.kt` | 4429 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlannerContractTest.kt` | 8913 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlannerMayorContractTest.kt` | 5955 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlannerPoisonContractTest.kt` | 3798 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlannerSuccessionResolutionTest.kt` | 4555 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightTransactionHostIntegrationSmokeTest.kt` | 6872 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightTransactionReconstructionContractTest.kt` | 13649 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightTransactionReconstructorSuccessionLegalityTest.kt` | 7014 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightTransactionRestoreCompositionTest.kt` | 6838 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/PersonalDirectOpenAiV1Test.kt` | 6575 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/PoisonExecutionDuskTimelineRegressionTest.kt` | 3627 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/SNE7NightTransactionBehaviorMatrixTest.kt` | 12779 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerCausalDecisionJournalV1Test.kt` | 14313 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1Test.kt` | 13013 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderRequestFactoryV1Test.kt` | 9645 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicChangeCheckpointV2Test.kt` | 23299 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicContinuationV2Test.kt` | 24035 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/TroubleBrewingCurrentDemonRegressionTest.kt` | 5377 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProviderRegistryTest.kt` | 6378 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/Dlb7TroubleBrewingAcceptanceTest.kt` | 6939 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSourceTest.kt` | 9487 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/NoGreaterJoyGenericArchitectureAcceptanceTest.kt` | 6503 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/NoGreaterJoyProductionSetupPreparerTest.kt` | 4826 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelectorTest.kt` | 9107 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitterTest.kt` | 9218 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicyResolverTest.kt` | 10934 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepositoryTest.kt` | 6538 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDealRoleResolverTest.kt` | 2710 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDrunkSelectionRouterTest.kt` | 5076 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingGameSnapshotProjectorTest.kt` | 11556 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingGenericProductionAuthorityTest.kt` | 5097 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingIntermediateSetupTest.kt` | 4130 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingProductionSetupPreparerTest.kt` | 6322 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingProductionVisibleRosterCutoverTest.kt` | 2603 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupCommitterTest.kt` | 9018 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetJsonTest.kt` | 5031 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetValidatorTest.kt` | 10158 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupRotationRecordFactoryTest.kt` | 10100 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingVisibleRosterSeatPlannerTest.kt` | 7656 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/debug/DebugFlightRecorderCoreTest.kt` | 2500 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerRulesetPersistenceBasisTest.kt` | 2123 | 已审计 | 需要解耦 | Gradle testFast/testFull；RulesetPersistence/ContentHasher；保留角色变化不改写已冻结内容身份的真实不变量；若统一 catalog 内容哈希则迁移该断言并退休 role-set basis 形状，不能当新恢复系统依据。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerTypedDayActionHistoryTest.kt` | 3875 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/TroubleBrewingRecentPlayerRotationHistoryStoreTest.kt` | 5634 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/TroubleBrewingSetupRotationHistoryStoreTest.kt` | 12710 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/TroubleBrewingSetupRotationRecordStoreTest.kt` | 2165 | 待审 | — | 尚未形成有证据结论 |
| `build.gradle.kts` | 217 | 已审计 | 必须保留 | Gradle wrapper/settings→:app；实际 AGP/Kotlin/Compose 插件版本定义；非旧规则模块，不为审计更新版本。（静态，运行结论仅明确列出者） |
| `gradle.properties` | 553 | 已审计 | 必须保留 | Gradle/AGP→:app；现用 JVM/AndroidX/Kotlin/AGP开关；部分deprecated不等于删除许可，需独立构建验证后才能改，审计不混入工具链升级。（静态，运行结论仅明确列出者） |
| `gradle/gradle-daemon-jvm.properties` | 1138 | 已审计 | 必须保留 | Gradle wrapper Daemon JVM criteria；toolchainVersion=25，故 CI setup-java17 不代表 daemon一定17；本地25 Gradle启动及本轮定向JVM测试成功，保留真实构建所需配置。（静态，运行结论仅明确列出者） |
| `gradle/wrapper/gradle-wrapper.properties` | 252 | 已审计 | 必须保留 | gradlew/gradlew.bat→wrapper JAR；固定Gradle9.5.0下载/缓存和超时配置；现用本地/CI构建入口，不是规则计算或重复状态。（静态，运行结论仅明确列出者） |
| `gradlew` | 9014 | 待审 | — | 尚未形成有证据结论 |
| `gradlew.bat` | 2966 | 待审 | — | 尚未形成有证据结论 |
| `player/handbook.html` | 48372 | 待审 | — | 尚未形成有证据结论 |
| `settings.gradle.kts` | 336 | 已审计 | 必须保留 | Gradle构建入口；仅 include(:app)，Google/MavenCentral/插件仓库和项目身份；无被删 web/独立 Rules module，后续不能据历史路径虚构消费者。（静态，运行结论仅明确列出者） |
| `tools/ai_conversation_probe.py` | 8245 | 待审 | — | 尚未形成有证据结论 |
| `tools/asp_oracle/oracle_harness.py` | 22933 | 待审 | — | 尚未形成有证据结论 |
| `tools/asp_oracle/scenarios/trouble_brewing_a2.json` | 44315 | 待审 | — | 尚未形成有证据结论 |
| `tools/asp_oracle/test_oracle_harness.py` | 6979 | 待审 | — | 尚未形成有证据结论 |
| `tools/gsp_api0_responses.py` | 12988 | 待审 | — | 尚未形成有证据结论 |
| `tools/prod_global_3b2_manual_probe.py` | 14826 | 待审 | — | 尚未形成有证据结论 |
| `tools/prod_global_3b3_semantic_v2.py` | 15136 | 待审 | — | 尚未形成有证据结论 |
| `tools/prod_global_latency_profile.py` | 14080 | 待审 | — | 尚未形成有证据结论 |
| `tools/prod_global_live_smoke.py` | 13173 | 待审 | — | 尚未形成有证据结论 |
| `tools/setup/migrate_tb_drunk_templates.py` | 4626 | 待审 | — | 尚未形成有证据结论 |
| `tools/setup_field_test_keystore.sh` | 1965 | 待审 | — | 尚未形成有证据结论 |
| `tools/storyteller_gateway.py` | 40604 | 待审 | — | 尚未形成有证据结论 |
| `tools/test_ai_conversation_probe.py` | 2268 | 待审 | — | 尚未形成有证据结论 |
| `tools/testing/summarize_junit.py` | 2979 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_gsp_api0_responses.py` | 5431 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_prod_global_3b2_manual_probe.py` | 6616 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_prod_global_3b3_semantic_v2.py` | 12186 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_prod_global_latency_profile.py` | 4194 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_storyteller_gateway.py` | 22183 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_storyteller_gateway_compact.py` | 4932 | 待审 | — | 尚未形成有证据结论 |
| `ui/components.css` | 22652 | 待审 | — | 尚未形成有证据结论 |
| `ui/design-tokens.css` | 4351 | 待审 | — | 尚未形成有证据结论 |
| `ui/ui-showcase.html` | 21113 | 待审 | — | 尚未形成有证据结论 |
| `ui/ui-showcase.js` | 1810 | 待审 | — | 尚未形成有证据结论 |

## F11：信息真值/登记投影与 Mayor 候选链已解耦（完整 CI/R2 已通过）

- **核对基线**：本地/远端 `f2c09e175e8e70e2394594806ee7e969d285a996`、PR #334 Draft/base `64be30905b88f639a464204ed1db89b7b62bc910`，main `61cd6796569ed8dfdea360caefeb5b9328745090`；其 docs-only CI #4367 / R2 #3947 SUCCESS，代码基线仍为 `800f1d97` 的完整 CI #4366 / R2 #3946。Mini MCP 是独立旧 checkout `a2cbcac8`、clean，不在那里修改。本批按用户明确指示先完成 B4/B5 解耦，再交接新对话做逐文件分类审计。
- **B4 信息链**：现有 presentation model 内的窄 `ClocktowerInformationQuestion` 仅携带 Number/Boolean 指标和实际查询 subjects 或 Role 查询座位；source 仍由当前 actor 与 canonical Session roster 定位，没有新增可写游戏状态。Builder → current scalar request、joint opening numeric matcher、direct numeric input 不再从真值/候选菜单抽取问题。Chef/Empath/Clockmaker/Chambermaid 输入任意非负 Int；FT/Chambermaid/Ravenkeeper 等须先有实际目标；Role 揭示可选择已知角色。保留原私密展示、手工/AI_ASSISTED 预选、AI_AUTOMATIC 发布回调、实际 API/提示词/解析/记忆及 revision/recipient 边界。直接输入绑定真实 actor 和 targets；改变数字查询目标会重置输入。
- **B4 删除链**：HostScreen 的 WW/Librarian/Investigator 默认真角色/自动 decoy、Chef/Empath/FT/Clockmaker/Chambermaid 真值计算、角色结果默认真值/登记假设及发布时真值回退已退出。RegistrationOptionPreparation → ResultPresentation/ResultDomain → RegistrationProjection → TroubleBrewingRegistrationDomain 整链删除；日间 Nomination/Slayer 直接保留明确选择，退出该规则投影的 poison/role 合法域门禁，未删除真实登记事实生产者。FixedInformationEvaluator 整删，livingNeighbors 只保留现有查询座位定位，不算阵营数字。旧 Chef/Empath/Clockmaker 方桌候选 renderer、Chambermaid wake-count/0..2 旧 helper 及无人消费的夜间登记 metadata 同时删除。
- **B5 Mayor 链**：pending 只保留 identity/revision/source，AI Provider 不再发 legalCandidates，返回的 SEAT_TARGET 按当前座位/版本直接预选。App/Session 删除 candidate ID 回匹配、living-actual-Mayor 事实门禁和排除 Demon 座位的合法域；Session 保留 request game/source/revision、已知座位、去重与单 writer 原子 ActionFact。一次确认只有实际 MayorRedirectChoice，不再为同一动作追加候选 causal capture/decision/correction；AI 历史仍从实际 ActionFact 读取。Dawn planner 使用已确认目标，不因目标后来成为 Demon 改杀 Mayor；保留原死亡、角色改变、继承和基本续局应用。
- **整删生产 10 文件**：
  - `ClocktowerChefSquareTableUi.kt`
  - `ClocktowerClockmakerSquareTableUi.kt`
  - `ClocktowerEmpathSquareTableUi.kt`
  - `ClocktowerRegistrationOptionPreparation.kt`
  - `ClocktowerRegistrationProjection.kt`
  - `ClocktowerRegistrationResultDomain.kt`
  - `ClocktowerRegistrationResultPresentation.kt`
  - `clocktower/rules/FixedInformationEvaluator.kt`
  - `clocktower/rules/MayorRedirectLegality.kt`
  - `clocktower/rules/TroubleBrewingRegistrationDomain.kt`
- **整删专属旧测试 11 文件**：
  - `ClocktowerChefSquareTablePresentationTest.kt`
  - `ClocktowerClockmakerSquareTablePresentationTest.kt`
  - `ClocktowerEmpathSquareTablePresentationTest.kt`
  - `ClocktowerRegistrationOptionPreparationTest.kt`
  - `ClocktowerRegistrationProjectionTest.kt`
  - `ClocktowerRegistrationResultDomainTest.kt`
  - `ClocktowerRegistrationResultPresentationTest.kt`
  - `FirstNightPairInformationSelectionDomainTest.kt`
  - `clocktower/rules/FixedInformationEvaluatorTest.kt`
  - `clocktower/session/MayorRedirectDecisionBoundaryTest.kt`
  - `clocktower/session/StorytellerDecisionFoundationTest.kt`
- **保留/迁移的验证**：当前无答案/无候选菜单时的首夜/后夜请求与已发表不可重开、首夜 joint 数字直接建议、查询 targets 与 actor 身份、任意 99 数字及阴性结构检查、Role 查询无真值锚仍接受手工/AI、actual information 不反推 Spy/Recluse 登记、Mayor Demon 目标接受/未知座位拒绝/源不匹配/过期版本/失败无半写/重复 no-op/改选事件顺序、Dawn/角色变化重建不改写已确认目标。保持实际 AI transport/模型响应/战略与隐私测试；退役旧 truth/候选/witness-only 断言，不恢复旧规则为测试兜底。
- **实际规模/本地验收**：生产 +258/-3085（净删 **2827**），测试 +169/-2020（净删 **1851**），合计净删 **4678 行代码**。当前存量 **905 跟踪文件、246 生产 + 195 测试 Kotlin**。`testFull + assembleDebug` SUCCESS，**819 tests，0 failure/error/skipped**；diff whitespace check 通过。超限源码：App **178170B**、HostScreen **101971B**；NightStepUi **44505B** 已低于 50000B。代码 **`d9fb47f2e805339736f6a70cd3b347090810bea9`** 的完整 Android **[CI #4369 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39116071137)**（完整单测+Debug APK）、**[R2 #3949 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39116071147)**，ASP/真实 Clingo/CI gate SUCCESS；本地 JVM/APK 和 CI 不等于实际真机或付费模型验收。
- **明确保留与剩余**：本批完成上述两条指定链，不宣称 P1 或全仓语义审计完成。Sage 仍有默认真值/pair 与 truth 标签、Spy 魔典/poison 分支、日间 Virgin/Slayer 后果、其它行动/继承/发牌门禁、剩余 candidate presentation/Provider DTO、因果日志及重复状态/历史仍须逐文件分类审计；必要死亡/角色变化/继承应用不能因含规则就整模块删除。跨项目 codec、安全/平台、tools/CI 必须读真实消费者。无新增必须由用户决策的产品阻塞项；潜在范围问题在下轮用证据列人工审核，不能假称已经裁定。
- **远端初次检查与修正**：代码 `56a927979fad03402d9a3de6e611db5ce2eb7840` 的 GitHub diff 独立核对为68文件+506/-5301，生产净删2827、测试1851，无未授权目录变更；R2 #3948 因要求已退休 ClocktowerRegistrationDetail 存在的旧源码守卫失败。仅移除该存在性断言，保留其余边界检查；从当前 workflow 提取的实际 R2 Bash 脚本在本地执行 SUCCESS。修正提交 `d9fb47f2e805339736f6a70cd3b347090810bea9` 带 [full-ci] 重新验收，完整 CI #4369 / R2 #3949 均 SUCCESS，旧失败已解决；本批没有通过复活旧 enum 满足源码字符串守卫。
- **交接与边界**：NEXT_DEVELOPMENT_HANDOFF 和 CURRENT_DEVELOPMENT_ROADMAP 已改为单一当前入口，旧批次历史只留本台账；新对话先核实最新远端/head/CI，再按“必须保留（Host 控制/记录/AI 推荐/平台安全支撑）、必须删除、需要解耦、需要人工审核”逐文件审计。待审状态不是已审核类，混合文件需分职责；完成比例按有证据的最终结论数/明确范围统计，不以扫描或净删除率冒充审计进度。Draft #334 不合并/retarget，不动既有未跟踪文件，无付费 API/真机操作。

## F10-B：孤立候选与旧世界合同整组删除（完整 CI/R2 已通过）

- **起点与范围**：远端/本地 `968261005f0003a137745eedbdaafbe9d4c40040`，Draft #334；沿 F10-A 的全树分类落实 A1–A3。重新核实无 App、Session、现用 AI、tools/CI 消费者后删除封闭旧类型岛，不把混合文件整体判死。
- **整删生产 7 文件**：`ClocktowerBooleanDisplayOptionSelection.kt`、`clocktower/domain/DecisionCandidate.kt`、`DynamicInformationOutcome.kt`、`EffectDraft.kt`、`RecommendationModels.kt`、`SetupClueOutcome.kt`、`StableCandidateIdFactory.kt`（后六个均在 domain/）。移除链为旧 candidate/effect/plan/ID → 孤立辅助/专属测试；实际事实仍用 ActionFactDraft/EpistemicObservationDraft。
- **混合职责删除**：StorytellerProviderContractV1 删除旧 response/outcome/recommendation/confidence/validator 尾部，保留现用 request/context/prior-decision；DecisionTypes 删除无人使用的 DetectionSemantics/AbilityType。EpistemicContracts/SemanticModel/Json 删除 F10-A 列出的 13 个旧世界、合法选择、登记推演类型及专属 codec/identity helper，并移除无人使用的 GOOD 红鲱鱼 eligible helper；保留 TimelinePoint、真实 ActionFact/Observation、Grimoire、PlayerKnowledge、安全稳定 ID 和现用 JSON。
- **测试**：整删 `ClocktowerBooleanDisplayOptionSelectionTest.kt`、`clocktower/domain/UnifiedDecisionModelsTest.kt`、`clocktower/epistemic/TimelinePointJsonCompatibilityTest.kt`（最后一项仅调用退休 RegistrationQuery codec）；混合 MurmurHash3/Provider/SemanticModel/ObservationTimelineBinding 测试只移除旧候选/世界段。现用观察日志仍验证缺失 globalSequence 拒绝、旧 local 显式兼容、全局序列一致性、隐私和 round-trip；保留实际 hash vectors、AI request/stale/隐私测试。
- **实际验证/规模**：本地 `:app:testFull :app:assembleDebug` SUCCESS，**883 tests，0 failure/error/skipped**。生产 +2/-816（净删 814），测试 +0/-432，合计净删 **1246 行代码**。Git 跟踪存量预计 **926 文件，256 生产 + 206 测试 Kotlin**。仍有两个 >50000B 源码：App **180424B**、HostScreen **144485B**；NightStepUi **49650B**。`git diff --check` 通过。代码 **`800f1d9797ecb0154c970a8a7cd8c58f5352d588`** 已经 GitHub Connector 独立比较远端 24 文件 +25/-1248 diff；完整 Android **[CI #4366 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39113685085)**（完整单测+Debug APK）、**[R2 #3946 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39113685103)**，ASP/真实 Clingo/CI gate SUCCESS。退役符号在现用 app/src、tools、CI 无残留；跟踪工作树干净，未跟踪用户文件保留。
- **事实/AI 风险及剩余**：本批没有改 Session 写入、事件身份/顺序、实际信息、recipient/revision、AI transport/parser/策略记忆/预选/模式；没有 API 调用或真机验收。B4 真实问题/真值/登记投影链、B5 Mayor 候选与实际死亡/继承链仍待先解耦后删除，不能称 Host Rules 已清空；无新增所有者决策，跨项目核心 codec 和现用工具保留。PR #334 保持 Draft，不 merge/retarget；用户未跟踪文件不动。

## F10-A 审计：F9 后剩余高置信清理对象（仅审计，未删除源码）

- **核对基线**：GitHub PR #334 仍为 Draft，远端/本地 HEAD `7552918161508522c2a0e677f38df6a49bf1a9ca`、base `64be30905b88f639a464204ed1db89b7b62bc910`，main `61cd6796569ed8dfdea360caefeb5b9328745090`；Mini MCP 独立旧 checkout `a2cbcac8`，不能当成本地工作副本。代码基线 `4113974d` 完整 Android CI #4363 / R2 #3943 SUCCESS；当前 docs-only HEAD CI #4364 / R2 #3944 SUCCESS，不能作为新 Android 编译证明。
- **覆盖口径**：读取未截断的全部 936 文件 Git tree，按目录/体积横向比较 263 个 Android 生产 Kotlin、209 个测试 Kotlin，以及 tools/CI；对下述候选检查声明、真实调用、manifest、工具/工作流和混合测试。它是全树库存加候选语义审计，**不等于 263 个生产文件均已逐函数完整审计**。初筛发现“零 Kotlin 引用”的 Application 实际由 manifest 加载，已排除；没有仅按名称、正则扫描或测试数量判死代码。

### A：可以直接清除的高置信对象

1. **F9 删除后孤立的候选模型/辅助岛：7 个整文件，274 生产行 / 9277B。**
   - `clocktower/domain/DecisionCandidate.kt`：CandidateMetadata/DecisionCandidate/DecisionEvaluation；只有旧模型专属测试，已无生产生成器或消费者。
   - `clocktower/domain/DynamicInformationOutcome.kt`：旧 Number/Category 候选包装，生产/工具/工作流无外部消费者。
   - `clocktower/domain/EffectDraft.kt`：InformationValue/PlayerStatus/EffectDraft 只互相引用或被上述孤立 DecisionCandidate 引用；真正事实仍是 ActionFactDraft/EpistemicObservationDraft，不是这个候选 effect。
   - `clocktower/domain/RecommendationModels.kt`：CandidatePlan、StorytellerDecision、QualityTier 等只由本岛的 SetupClueOutcome/DecisionCandidate 引用，不是现用 OpeningRecommendationV2 或 StrategicContinuation。
   - `clocktower/domain/SetupClueOutcome.kt`：只剩自身声明；旧开局生成器已退役。
   - `clocktower/domain/StableCandidateIdFactory.kt`：无生产消费者，仅 MurmurHash3Test 的旧候选 ID 用例。保留实际 setup/seed 使用的 MurmurHash3 和 hash golden vectors，删除候选 ID 专属用例即可。
   - `ClocktowerBooleanDisplayOptionSelection.kt`：findBooleanDisplayOption 仅独占测试调用；F9 已删除其真实调用方，现用真假直接结果不需要反查菜单。
   - 测试：可整删 UnifiedDecisionModelsTest、ClocktowerBooleanDisplayOptionSelectionTest；MurmurHash3Test 只删候选部分，不连同活跃 hash 测试一起删。DecisionTypes.kt 的 DetectionSemantics/AbilityType 也没有生产/工具消费者，可以连带精简；保留仍服务 Session/观察/登记事实的 AbilityState、TruthRelation、RegistrationFact 等。
2. **旧 V1 Provider 响应/候选验证器：144 生产行 / 5706B，混合文件中的完整尾部。**
   - `clocktower/domain/StorytellerProviderContractV1.kt:420` 起的 Response/Outcome/Recommendation/Confidence/Validation/ResponseValidator 七个类型只在本段和 StorytellerProviderContractV1Test 的旧 response 用例中引用。其职责是 primary/alternative candidate ID 回匹配和 deferred response，不在真实 OpenAI 网络/解析/记忆路径。
   - 可直接删除该尾部和专属旧断言；**不能整删文件**：Request、DecisionContext、Revision、GameContext、PriorDecision 等仍由 App、Session、AI 续规划与实际因果日志消费。也不能据此整删剩余 Candidate DTO：Mayor 和日间登记事实生产者仍在使用它们。
3. **旧世界/合法选择合同残留：三个混合文件中的封闭分支。**
   - `epistemic/EpistemicContracts.kt`：EpistemicHypothesis、RegistrationQuery/Basis/Profile/Semantics、TroubleBrewingRegistrationSemantics、WorldCardinality、CandidateFamilyId、WorldExplanationClusterId、PlayerWorldSetIdentity。
   - `epistemic/EpistemicSemanticModel.kt`：StorytellerDecisionPoint、LegalEpistemicChoice、LegalChoiceSet；与上一项合计 13 个顶层旧类型。
   - 上述对象的外部“生产”引用仅为 EpistemicSemanticJson 的对应 overload/codec 和对象内部 ID 构造，没有 App/实际 AI/Session 入口；tools/CI 无这些类型或旧 schema kind 消费者。旧 RegistrationSemantics 算法只有 EpistemicSemanticModelTest 调用。可连同 Json 中相应 encode/decode/private helper 和混合测试的旧世界/合法候选段一起删除。
   - 三个文件总库存为 1281 行 / 72296B，**这不是可删除量**，不把整文件体积当收益。必须保留 TimelinePoint、InformationProposition、Recorded/EpistemicObservation、可见范围/Grimoire、安全稳定 ID、实际事件 JSON，以及仍与观察日志/历史兼容有关的部分。保留 World identity 测试中真正的隐私/顺序性质时，应在现用日志合同验证，不为保旧 wrapper 继续供养世界合同。

A1+A2 确定整段生产体量为 **418 行 / 14983B**，另有 A3 数百行混合职责及专属测试可退役；精确净减少量应在真实删除后由 Git diff 统计，不能把“关联文件体量”算成净删除。建议把 A1–A3 合为一个封闭删除批：优先退出旧世界/合法选择合同，再一次收走孤立类型与旧响应验证器。

### B：删除方向置信高，但先断开当前事实/交互消费者

4. **Host 信息真值与登记见证投影（下一大批优先）**：RegistrationOptionPreparation、RegistrationResultPresentation、RegistrationResultDomain、RegistrationProjection、rules/FixedInformationEvaluator 五个关联文件合计 463 行 / 21878B，另有 HostScreen 中大段 Chef/Empath/FT/角色结果真值/枚举和 InformationStepBuilder 的旧候选字段。真实链是 HostScreen → RegistrationOptionPreparation → ResultPresentation/Domain，并经 RegistrationProjection → TroubleBrewingRegistrationDomain；FixedInformationEvaluator 由 HostCoreSemantics → HostScreen 使用。
   - Host 不应继续算线索真假或枚举 Spy/Recluse 见证，因此方向明确。先让当前问题显式持有 source/metric/实际查询 subjects，再移除旧数值/角色菜单及回退真值；保留实际输入、AI advice、Session 发布、recipient/revision 和必要座位展示。livingNeighbors 与其它必要查询/座位投影须单独判断，不能以删数字计数为由抹掉实际目标语义。
   - Builder 已把 night step 的 spy/recluseRegistrationKey 强制为 null，部分旧分支实际不会进入；但相同登记 State/Controls 仍被白天 Nomination/Virgin/Slayer 的明确登记事实操作消费，**不整包删除全部登记 UI**。日间实际确认不得被夜间“见证推导”连带删除。
5. **Mayor 合法候选/确认及通用 pending 候选底座**：MayorRedirectLegality、MayorRedirectDecisionBoundary、StorytellerDecisionFoundation 三个文件总库存 258 行 / 9935B。Mayor 边界是 PendingStorytellerDecision 的唯一生产消费者；规则域又被 Session.commitConfirmedMayorRedirect、NightDawnResolutionPlanner、NightHostProjection 使用。当前排除恶魔座位的产品限制与最新 Host 事实职责不符，应删除规则限制和候选 ID 中转。
   - 先保留已有 request/revision、实际重定向 seat 与后续死亡/继承事实应用，再退出域与确认包装。Foundation 前段 RequestIdentity/Revision 仍被活跃 AI/其他事实大量引用，不能连根整删。相关旧合法性测试可退役，去重/过期/原子事件/基本续局测试保留。

### 本轮不列为“可直接整删”的对象

- **旧 Chef/Empath/Clockmaker 方桌 UI**：3 文件库存 816 行 / 32332B。Chef 的真实 typed numeric 路径会在 NightStepUi 前段转直接数字输入；部分旧 UI 可以跟随 B4 化简，但 **Clockmaker materializer 仍未提供 typed NumericResult，旧数值展示兜底仍是真实路径**。不能因看到 F9 的 early return 就宣布三文件全无消费者。先补齐当前问题/输入合同再删除旧重复 renderer；不把源码 shape 测试当实际产品证据。
- **Epistemic 时间线/隐私、PlayerHistoricalTimeline 与 TB snapshot Json codec**：部分无 Android live 调用，但属于明确保留的观察/公共历史安全及跨 EvidenceLab 合同，不能仅凭零调用列为死实验；当前保留。后续合同收敛另议，避免误删 core serialization/security。
- **Python gateway / API0 / prod_global 工具**：不是已经证明无用的孤岛。prod1-gateway-offline、api0-offline、ci.yml 仍实际执行对应脚本/测试；这只证明有开发验收消费者，不自动证明长期产品必需，但本轮证据不足以批准整包删除。需要先判断现用连接/验收职责，不能为腾空间删掉所有 AI 工具。
- **Dawn、死亡、角色变化、继承、投毒等**：含规则和实际状态应用的混合模块；应去规则否决，保留必要事实提交和安全继续。此次不认定为整模块垃圾，也不扩大成删除现有 AI/基本 Host 操作。

- **本轮变更/验收**：仅形成审计结论与更新交接，生产删除 0、测试删除 0、净代码减少 0；未改状态/事件/AI 代码，因此不重复运行 Android 测试。最近完整代码验收仍为 `4113974d` 的 CI #4363/R2 #3943（898 tests + Debug APK）；未运行付费 API/真机。现仍有 App 180482B、HostScreen 144543B 两个超限源码，NightStepUi 49650B。未新增必须由所有者决策的阻塞项，工具/跨项目范围保守保留。PR #334 保持 Draft，不 merge/retarget，原有未跟踪文件不动。

## F9：四模块整组解耦与删除（完整 Android CI/R2 已通过）

- 起点：GitHub/本地 `a3008b33ff9fa4d8f8fc11389ef1f972fb529323`，main `61cd6796569ed8dfdea360caefeb5b9328745090`，Draft #334/base `64be30905b88f639a464204ed1db89b7b62bc910`；fetch 后一致。Mini MCP 仍为独立旧 checkout `a2cbcac8`，本批直接改 Windows 本地文件。完整未截断 Git tree 974 文件；本批工作树预计 936，生产 263 + 测试 209 Kotlin。历史版本库存不代表逐文件语义审计完成。
- **Pair**：App/current residual scope/HostScreen 的真实 AI 请求保留，只发送当前 recipient/ability/revision/problem。删除 PairInformationDecisionBoundary → PairInformationLegalDomain → NaturalPair/DisplaySemantics 真值、见证与合法候选枚举链；保留窄 PendingPairInformationDecision 身份 DTO。直接建议生成 witness-free typed display，不匹配合法候选 ID；手工 pair 允许任意已有座位（含 actor）及已知剧本角色，零结果仍表达本角色类别的缺席陈述，不从显示反推登记。
- **Numeric/Boolean**：删除 Coordinator → NightRecommendationModule → DynamicCandidateGenerator → InformationDecisionFoundation → StructuredNumber/Boolean UI model/adapter 的生成、值域/真假分类、候选包装与确认链；删除 FirstNightNumericInformationSemantics 真值枚举、UI truth projection 与 UnifiedSelectionPool。统一使用已有 ReliabilityState，退出重复枚举/版本 aliases。Chef/Empath 等直接数字输入保留非负 Int 的结构限制，AI_ASSISTED 预填、AI_AUTOMATIC 通过同一实际发布回调；FT 等只要求真实已选查询座位，直接接收真假结果，不以旧真值域二次否决。
- **Setup**：删除 Drunk 候选域/假设 GameState projector 和第二候选确认边界；原 pending object/screen/game identity 仍隔离旧点击，最终 commit 一次构造实际状态。任意已发牌座位都能被明确确认为 Drunk，保留 shown role 和实际 Drunk 元数据；未知/改名/换牌引用仍拒绝。红鲱鱼选座和 Session/SessionState 均退出善良阵营门禁；恶魔伪装使用所有已知剧本角色，Session/SessionState 不再拒绝已在场角色，去重、事件时序与单 writer 保留。
- **后夜接收**：StorytellerStrategicContinuationV2 的真实 transport、结构化 JSON、提示词、原模型响应 anchor、历史增量和多夜战略记忆保留；直接返回 SemanticResult，删除“语义 → 当前 legalCandidates 匹配 → 内部候选 ID → UI 再匹配”。保留严格字段/整数、当前问题/实际目标、已知角色与座位、game/revisions/global-prefix/memory-version、完整响应和已发布不可重开校验。记忆只在同一私密 recipient 实际收到相同结果后晋升；不凭建议写事实。Session 发布端增加当前 revision 校验，替代退役候选 snapshot 的过期安全职责。
- **整删生产 19 文件**：
  - `ClocktowerRecommendationRules.kt`
  - `StructuredBooleanInformationAdapter.kt`
  - `StructuredNumericInformationAdapter.kt`
  - `clocktower/recommendation/UnifiedSelectionPool.kt`
  - `clocktower/recommendation/dynamic/DynamicCandidateGenerator.kt`
  - `clocktower/recommendation/dynamic/UnreliableInformationModels.kt`
  - `clocktower/rules/FirstNightNumericInformationSemantics.kt`
  - `clocktower/rules/NaturalPairInformationCandidateGenerator.kt`
  - `clocktower/rules/PairInformationDisplaySemantics.kt`
  - `clocktower/rules/PairInformationLegalDomain.kt`
  - `clocktower/session/ClocktowerRecommendationCoordinator.kt`
  - `clocktower/session/DrunkAssignmentDecisionBoundary.kt`
  - `clocktower/session/InformationDecisionFoundation.kt`
  - `clocktower/session/PairInformationDecisionBoundary.kt`
  - `clocktower/session/RecommendationModules.kt`
  - `clocktower/session/StructuredBooleanInformationUiModel.kt`
  - `clocktower/session/StructuredNumberInformationUiModel.kt`
  - `clocktower/setup/TroubleBrewingDrunkCandidateProjection.kt`
  - `clocktower/ui/StructuredStorytellerInformationUi.kt`
- **整删专属测试 20 文件**（旧真值/合法候选、包装/假设投影/来源形状断言；真实 AI 与事实安全测试保留并改接直接语义）：
  - `ClocktowerRecommendationRulesTest.kt`
  - `ClocktowerStructuredInformationPreparationTest.kt`
  - `FirstNightPairInformationPoolLegalityTest.kt`
  - `StructuredChefInformationAdapterTest.kt`
  - `StructuredEmpathInformationAdapterTest.kt`
  - `StructuredFortuneTellerInformationAdapterTest.kt`
  - `clocktower/recommendation/NaturalPairInformationCandidateGeneratorPerceivedAbilityTest.kt`
  - `clocktower/recommendation/NaturalPairInformationCandidateGeneratorTest.kt`
  - `clocktower/recommendation/PairInformationLegalDomainTest.kt`
  - `clocktower/recommendation/UnifiedSelectionPoolTest.kt`
  - `clocktower/recommendation/dynamic/DynamicCandidateGeneratorTest.kt`
  - `clocktower/rules/FirstNightNumericInformationSemanticsTest.kt`
  - `clocktower/rules/PairInformationDisplaySemanticsTest.kt`
  - `clocktower/session/ClocktowerRecommendationCoordinatorTest.kt`
  - `clocktower/session/InformationDecisionCoordinatorIntegrationTest.kt`
  - `clocktower/session/InformationDecisionFoundationTest.kt`
  - `clocktower/session/PairInformationDecisionArchitectureTest.kt`
  - `clocktower/session/PairInformationDecisionBoundaryTest.kt`
  - `clocktower/session/StructuredNumberInformationUiModelTest.kt`
  - `clocktower/setup/TroubleBrewingDrunkCandidateProjectionTest.kt`
- **验证**：本地 `:app:testFull :app:assembleDebug` SUCCESS，898 项、0 失败/错误/跳过。旧候选测试/fixture 编译及旧断言曾失败，已按新职责退休/修改；过程中删 import 误去掉 Compose 委托运算符，编译发现后已恢复。重点回归：直接 99 数字与不在旧菜单的 pair/role、未知/重复/小数/越界座位拒绝、真实已存首夜模型响应、后夜记忆/历史/收件人、非镇民 Drunk、evil 红鲱鱼/in-play bluff 的单 writer 事件及重放、过期回复不改 Session。CI routing 只移除 R2 对已退役 unifiedFirstNightInformationPool 的存在性守卫；其他安全/构建/R2 验收保留。没有付费 API 或真机验收。
- **规模**：生产 +410/-3215，测试 +165/-2649，源码净删 **5289 行**（以本地 Git diff 加新 DTO 实测；另 R2 -1 行）。NightStepUi **49650B** 已低于 50,000；仍超限 App **180482B**、HostScreen **144543B**，源码按 canonical UTF-8/LF，修改的生产文件本地亦保存 LF。
- **明确剩余/风险**：这不是全部 Host Rules 清空。HostScreen/InformationStepBuilder 的角色信息真值/登记预览、ResultFirstRegistrationDomain 与 FixedInformationEvaluator、Mayor/NewDemon/其他行动候选及旧 Provider 通用候选 DTO 仍需横向审计；其中部分服务当前 Host flow，要先断开必要事实/交互。Pair 手工 UI 当前仍把结构化组合物化为展示列表，未来可继续化简。App/HostScreen 两个大混合文件须继续删职责并收敛到 <=50,000B。跨项目状态合同/真机/实际收费模型品质未验收；无新所有者决策。保留唯一 Session、既有结构化事实与观察日志、私密交付/事件顺序；PR #334 保持 Draft，不 merge/retarget。

- **Exact code HEAD 验收**：`4113974d840f1c201d6b9aa7686193435c710f9b`，GitHub 完整 Android **[CI #4363](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39111596271) SUCCESS**（Android tests/Debug APK）、**[R2 #3943](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39111596284) SUCCESS**，ASP、真实 Clingo、CI gate 均成功。GitHub 独立 compare 为 96 文件 +641/-5865（含文档），生产 +410/-3215、测试 +165/-2649，**源码净删 5289 行**；另 R2 净删 1 行。实际提交 tree 936 文件、263 生产 + 209 测试 Kotlin，两个超限文件及 NightStepUi 49650B 已以完整 Git tree 复核。后续 docs-only HEAD 不替代此代码的 Android 验收。未改变用户原有未跟踪文件，未合并/retarget Draft #334。

## F8：首夜无效候选预计算、等待门禁与重复上下文整链删除（本地完整构建与 exact-SHA CI/R2 已通过）

- 起点：远端/本地 `0e7e4a4e6c1c51c3522456393349d51240e109b3`，main `61cd6796`，PR #334 Draft/base `64be3090`；Mini MCP 仍为 `a2cbcac8`，本批继续本地直接读写。读取完整 979 文件树，横向比较大文件/消费者后选中整个首夜预计算链，而非沿某个规则小函数逐段清理。
- **消费证据**：App 在 setup commit 后预热整局 natural pair 候选；HostScreen 通过 ready/result provider 拉取并保存在 `firstNightNaturalPairCandidates`。该结果没有送入任何信息 Builder、AI 请求或确认回调，唯一读取用途是 `firstNightNaturalPairPrecomputeReady`，进而阻塞手动/自动开始首夜并保留失败重试 UI 状态。实际 AI 配对由另一个 current-step producer 独立生成，信息展示已有结构化手工选项。
- **整删 3 个生产文件**：`clocktower/session/TroubleBrewingFirstNightPrecomputeCoordinator.kt`（缓存/后台任务/BUSY/READY/fallback）、`clocktower/recommendation/TroubleBrewingFirstNightPairDecisionContext.kt`（snapshot 重建 poison-neutral GameState、角色定义拷贝及忽略版本/投毒的缓存相等性）、`clocktower/recommendation/NaturalPairInformationCandidateGenerator.kt`（兼容转发层）。删除 App 的 prewarm scope/调度/context builder/双 provider，HostScreen 的候选缓存/异步加载/失败重试/待开始状态以及纯预计算等待门禁；确认当前必要 setup 信息后直接进入实际首夜步骤。
- `ClocktowerRecommendationCoordinator` 删除只为预计算存在的 naturalPairCandidates 和 SetupClueOutcome/EffectDraft 包装；实际 rules/NaturalPairInformationCandidateGenerator 删去已经没有生产消费者的整局 perceived 汇总、其角色识别 helper 和旧 functioning 入口。**保留**仍被 PairInformationLegalDomain 消费的 generateHealthyInformationSpace，不重建或扩展规则算法。
- **AI 等价接续**：`pendingGlobalFirstNightPairDecision` 直接接受当前 canonical Session 的 GameState/gameId/revision，调用现存 AI-only pair boundary；App、HostScreen 和残余 scope 全部改用同一事实/版本输入。保留座位顺序/来源身份引用检查及真实请求/响应 key、战略记忆、已确认事件历史和过期异步隔离；不再为配对建议复制另一份 roster 或把毒状态归零。角色定义使用已有 TB 元数据函数（旧 context 测试已证明它与 registry 投影相同），没有引入新 Context/Manager 或第二份 writer。
- **测试处理**：整删 `TroubleBrewingFirstNightPrecomputeCoordinatorTest.kt`、`TroubleBrewingFirstNightPairDecisionContextTest.kt` 两个专属旧测试；去掉只验证已退役 functioning 入口拒绝 poison 的一项旧测试。保留并改为直测实际 rules owner 的自然配对、Drunk perceived Investigator、NoGreaterJoy 通用配置测试；保留首夜真实已存模型响应、配对 Provider/预选和战略续规划测试。补充真实 Session 投毒事件前后配对请求版本更新/残余 Provider 接续、来源身份/座位漂移拒绝、非首夜/非实际步骤退出及事实不被生产建议改写的回归。
- **本地验收**：首次 testFast 在测试编译发现遗漏的旧包名消费者，已迁移到实际 owner；补充测试中的 Provider revision 字段名修正后，最终 `:app:testFull :app:assembleDebug` SUCCESS，**973 项、0 失败/错误/跳过**。没有为旧测试恢复预计算或规则入口，没有执行付费 API/真机。
- **规模/风险**：生产 +31/-554，测试 +76/-313，源码净删 **760 行**；整删 3 个生产、2 个测试，无新增文件。树 979→974，Android 生产 284→281、测试 231→229。三个超限源码按 UTF-8/LF 实测：App **184919B**、HostScreen **145615B**、NightStepUi **60237B**。唯一 Session、事实/观察日志、收件人和原子提交未修改；变化只退出无用缓存与等待，并将活跃 AI producer 绑定当前 canonical facts。代码 `119de9496e52088a0fde164aeafd24973743bfda` 的 GitHub 完整 Android **CI #4361 SUCCESS**（单测/Debug APK）、**R2 #3941 SUCCESS**，ASP、真实 Clingo、CI gate 均成功。独立 GitHub diff：18 文件 +130/-867（含文档），源码净删 760 行；完整非截断 Git tree 实测 974 文件、281 生产 + 229 测试 Kotlin及上述三个超限源码。此 SHA 为本批代码验收基线，后续 docs-only HEAD 不是新的 Android 验收。
- **明确剩余**：当前 pair AI-only 旧候选边界、Numeric/UI/setup/目标合法域及后夜 response candidate matching 仍需解耦；不能把本批删除说成所有 Host 规则门禁已清空。其余大块按实际职责继续审计，没有新增所有者决策，PR 保持堆叠 Draft。

## F7：优先集中删除已证实无生产消费者的旧实现（本地完整构建及 exact-SHA CI/R2 已通过）

- **执行顺序按所有者最新指示调整**：先横向比较全树的体量、职责和消费者证据，集中整删规模较大且有把握的旧实现；清完明确可删部分后，再逐文件审计剩余混合模块。不再默认沿酒鬼确认链逐函数推进，也不先建立另一套文件台账。
- 起点：GitHub/local `13228a7e7dbe994d1eeebe6fa108389e27401bdd`，PR #334 Draft、base `64be3090`；Mini MCP 仍停在 `a2cbcac8`（本批不通过它改代码或提交）。已读取完整 998 文件树，横向比较全部 Kotlin 模块；词法引用扫描只作候选筛选，最终删除逐项以完整受控源码/XML/工具/CI 的符号和路径引用复核为依据。普通本地未跟踪文件不动。
- **整删 11 个生产文件（444 行）**：
  - `clocktower/rules/FortuneTellerInformationSemantics.kt`（占卜师目标枚举/真值）和 `FirstNightNumericInformationDisplayDomain.kt`（厨师/共情者旧规则范围）：仅自身和各自测试引用，当前 App/AI/UI 均未调用。
  - `clocktower/config/TroubleBrewingRecommendationMetadata.kt`：旧角色评分表与 metadata 类型没有任何消费者；不是模型提示词或战略记忆。
  - `clocktower/session/FirstNightPoisonLifecycle.kt`：仅专属测试使用的另一份投毒状态/版本生命周期；保留真正 App/Session 的 poison draft/确认、canonical 事件和安全回归。`AuthenticatedModelTransportV1.kt`：没有任何消费者的重复 HTTPS/旧网关传输；真正 `PersonalDirectOpenAiV1`、首夜/后夜 API、认证和对应测试不动。
  - `clocktower/history/InformationReferenceExtractor.kt`：未使用的展示文本座位提取器；实际结构化历史/收件人投影不动。`ClocktowerSetupRecommendationSemantics.kt`：仅专属测试使用的旧 mutable setup lock 合并函数。
  - `clocktower/domain/RegistrationOutcome.kt`、`clocktower/recommendation/dynamic/PairInformationPresentationModels.kt`：只剩定义，无生产或测试消费者的旧登记 DTO/enum。
  - `GameSettingsUi.kt`：只含未调用的 StepperRow，不是当前设置页面。`clocktower/recommendation/PairInformationLegalDomain.kt`：无生产调用的兼容转发/typealias；实际 `clocktower/rules/PairInformationLegalDomain` 仍支撑后续 AI，保留原实现和有意义的测试，测试改为直接导入实际 owner。
- **整删 5 个专属测试（281 行）**：`clocktower/rules/FortuneTellerInformationSemanticsTest.kt`、`FirstNightNumericInformationDisplayDomainTest.kt`、`clocktower/session/FirstNightPoisonLifecycleTest.kt`、`clocktower/history/InformationReferenceExtractorTest.kt`、`ClocktowerCommittedSetupRecommendationLockTest.kt`。这些均只验证被退休实现；没有删除实际 Session 去重、poison commit、AI Provider/请求时效或隐私测试。
- **整删 3 条失效实验工作流（193 行）**：`.github/workflows/fn-bundle-3-calibration.yml`、`sde-2d5f-expert-observed-calibration.yml`、`sde-2d5f-b4f-silver-generalization.yml`。各自仅调用 F5 已退休的 Gradle 实验任务/测试；当前 app/build.gradle.kts 无这些 task，工作流不是复用 workflow，无正式 CI/生产依赖。保留完整 Android CI、R2、离线 API/网关检查和 field-test 构建。
- **明确保留/剩余**：厨师/共情者仍调用 ClocktowerSingleNumericChoiceRows，其他现用信息 UI 仍调用 SingleRecommendationButtons 和 compact helper，因此不因粗筛结果删除。Snapshot JSON codec、PlayerHistoricalTimeline 和其余 epistemic 核心继续保留。当前有真实消费者的大块（Session、NightTransaction、Setup/Flow、规则生成器、UI/后夜候选）不是本批整删对象，留待剩余审计和可验证解耦；不宣称 P1 完成。
- 代码/配置总净删 **917 行**（生产 -444、测试 +1/-281、实验 CI -193），没有新算法/新兼容层/新状态。实际 Git tree 998→979，Android 生产 295→284、测试 236→231。三个超限源码仍为 App 188197B、HostScreen 149006B、NightStepUi 60237B。唯一事实/事件/安全和活跃 AI 未修改；无付费 API/真机测试，无新增所有者决策；本地 `:app:testFull :app:assembleDebug` SUCCESS（980 项、0 失败/错误/跳过）；代码 `6320a735a9ee13a7c57dcd6a259361d40de591c3` 的完整 Android **CI #4359 SUCCESS**（单测/Debug APK）、**R2 #3939 SUCCESS**，ASP/真实 Clingo/CI gate 均成功。GitHub 独立 diff 核对：24 文件 +33/-920（含文档）；源码净减 724 行、另删 CI 193 行，合计代码/配置净删 917 行。该 SHA 是本批代码验收基线；后续 docs-only HEAD 不是新的编译证明。PR 保持堆叠 Draft、不合并。

## F6：首夜候选合法域整链删除（本地完整构建及独立 CI/R2 已通过）

- 本批按所有者最新指示在 Windows 本地直接读写；起点为远端/本地 `a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`，main `61cd6796`，PR #334 仍堆叠 Draft、base `64be3090`。读取完整 1,005 文件 Git tree 并复核当前模块分类；未声称全部文件已完成语义审计。旧 web/world 引擎不重建。
- 删除生产 `HostOpeningRulesDomainV2.kt`、`HostOpeningSemanticGateV2.kt`、`InitialGlobalOpeningScopeV1.kt`：退出「每个酒鬼假设构造 GameState → Pair/Numeric/Setup 合法候选 → 语义匹配候选 ID → V1 选择复核 → App 反解推荐」整链。真实 AI 网络调用不再生成这些合法域；首夜提示词去掉 Host 后验规则校验承诺，要求模型提供具体观察值而非依赖 Host 计算唯一答案。
- 断开最后生产消费者后，删除 `recommendation/FirstNightNumericLegalDomain.kt` 和 `recommendation/setup/SetupCandidateGenerator.kt`。删除四个专属测试文件：`HostOpeningSemanticGateV2Test.kt`、`InitialGlobalOpeningScopeV1Test.kt`、`FirstNightNumericLegalDomainTest.kt`、`CandidateGeneratorTest.kt`（该文件只含 SetupCandidateGeneratorTest）。删除前已检索所有源码、工具和 CI 消费者；PairInformationLegalDomain 仍有真实后续 AI 消费者，未误删。
- `InitialOpeningRequestV2` 只承载已有 precommit snapshot、请求身份/版本、经验和信息/待输入事件标识；`OpeningRecommendationV2` 保留实际 AI 结构化建议、待决意图和战略记忆 DTO。没有候选枚举、第二份 GameState、规则 verdict、登记见证或 Session writer。App 直接保存模型的 seat/value/role 信息、Red Herring 座位和 bluffs；移除候选 ID 中转。保留 serial、pending object、模式/页面边界、response receipt、记忆续规划和逐步事实确认。
- 保留/补充有价值的原生产测试：不发送候选、真实已存 P15 模型响应解析（离线）、战略续规划；验证具体数值/角色不再按旧合法域拒绝，并验证跨游戏/版本/roster、信息清单遗漏/重复、收件来源身份、未知角色/座位、待决行动身份、整数/DTO 结构及战略记忆上限。第一轮本地 testFast、testFull + assembleDebug 均通过（993 项、0 失败、0 跳过）；最终代码变动后的复验与 GitHub CI/R2 见下方验收补记。
- **明确剩余绑定**：`TroubleBrewingDrunkSelectionRouter` / setup commit、HostScreen/NightStepUi 的既有菜单匹配/数值/目标/掩饰身份确认，以及后夜 `StorytellerStrategicContinuationV2` 对候选的复核仍需继续解耦。本批只删除首夜响应规则域；尚不能声称所有 AI/UI 和 Host 事实提交均已无规则门禁。新解析器接受、旧酒鬼选择器尚不能应用的建议会显示重试/手动接管，避免 UI 抛异常；后续应从结构化 setup 确认端清除该限制。
- Host 状态/事件风险：未修改唯一 Session、ActionFact、ObservationLog、收件权限、事件顺序和原子提交；建议、条件意图、战略记忆都不是已发生事实。未执行真机或新付费 API 调用，无新增所有者决策，PR 不合并/不 retarget。

- **本批代码规模与最终本地复验**：含两个保留 AI 合同新文件，生产 +356/-1268，测试 +100/-784，合计净删 **1596 行代码**。删除 5 个生产 + 4 个测试、增加 2 个窄职责 AI 合同文件，实际受控树 1,005→998、Android 生产 298→295、测试 240→236。最终本地 `:app:testFull :app:assembleDebug` SUCCESS（993 项，0 失败/错误/跳过）；APK 为 `app/build/outputs/apk/debug/app-debug.apk`。当前超 50,000B 源码：App 188197B、HostScreen 149006B、NightStepUi 60237B（按 UTF-8/LF 源码实测），其余受控源码无新增超限。代码提交 `f3fae3f2e5bfde9083c919f9a5ae34f4e10fb90c` 的 GitHub 完整 Android **CI #4357 SUCCESS**、**R2 #3937 SUCCESS**，ASP、真实 Clingo 和 CI gate 全部成功。该 SHA 是本批代码验收基线；后续 docs-only HEAD 不等于新的 Android 构建证据。

## F5：大型整块删除已完成（Android CI/R2 全通过）

**顺序更新（所有者 2026-10-18）**：先整删旧 `web/` 客户端，再横向删除 `epistemic` 旧世界推演系统及专属实验，**最后**处理仍依赖 Host 候选 ID 的旧首夜合法域引擎；不要把小函数手术当主进度。

- `web/` 旧独立网页客户端 8 个受控文件整体移除（其中 PNG 由 GitHub 内容 API 单独提交 `5be8d37bad28e6055b89611de7be22388a7fffd1`，7 个文本文件由 Mini MCP 提交 `bb96b0c525b1550875d5e0ae22e9c68e62b0e308`）。Android、CI、工具源码无任何引用；这个网页不是未来新设计的独立 WebHost。
- `epistemic/` 不能整目录删：原 35 个生产 Kotlin 中，**26 个**旧 A3/A4/B4 枚举、历史世界重放、Exact、Topology、玩家知识计算、登记见证推演、Spy 旧投影等已整文件删除；保留 9 个核心文件：`ActionFactDraft`、`ActionFactTimeline`、`EpistemicObservationDraft`、`EpistemicObservationLog`、`EpistemicSemanticJson`、`EpistemicSemanticModel`、`EpistemicContracts`、`GrimoireReminderToken`、`PlayerHistoricalTimeline`。原因：App、唯一 Session、历史事实、可见/私密信息、结构化 JSON、两个公共历史隐私测试仍引用其类型；之后可按统一合同重命名/迁目录，但不需为删目录制造新工程。
- `epistemic` 原 60 测试 Kotlin 删除 **48 个**世界推演专属测试；保留事件时间线、私密观察、JSON 兼容、魔典令牌和历史投影相关 12 个测试及测试 fixture。整删独立 `clocktower/review/` 的 17 个旧专家世界后验实验 Kotlin 测试、其 3 份离线 manifest，及仅这些实验/测试使用的两份 `recommendation/` 源文件与三份专属测试。
- `app/build.gradle.kts` 移除已退役 SDE-2D4/SDE-2D5F 规模/费用实验的 Gradle 路由及排除规则，保留原 `:app:testFast`、`:app:testFull` 正式验证任务。
- **AI 保证**：`InitialJointOpeningDirectV2`、Provider/OpenAI API、模型提示词、战略记忆、后夜增量规划、AI_ASSISTED/AI_AUTOMATIC、结构化结果与自动/辅助选择均未触及。Host 保存实际 ActionFact/ObservationLog 的路径和安全/身份/时间戳校验保留。原 `epistemic` 中保留的 `EpistemicContracts` 仍含旧登记语义，下一步 Host candidate cleanup 才核查删除。
- 当前仍有未审计的旧 `HostOpeningRulesDomainV2` → `HostOpeningSemanticGateV2` → `InitialGlobalOpeningScopeV1` 候选 ID 采纳链，列为**下一大块 REMOVE_AFTER_DISCONNECT**。不可把“当前生产调用”写成最终 AI 策略需 Host 候选；保留 AI 请求/返回及 Session 安全，改为直接结构化字段后删除。
- **F5 实际验收**：旧 Web PNG 删除提交 `5be8d37bad28e6055b89611de7be22388a7fffd1`，其余 7 文件删除 `bb96b0c525b1550875d5e0ae22e9c68e62b0e308`；世界引擎/测试整删及 Gradle 提交 `3be99bbc40a099a18c8f1bfc13b1a5d7ed978bc7`，该代码提交 **103 文件 +27/-17731，净删 17,704 行**。完整 Android **CI #4355 SUCCESS**（单测、Debug APK），**R2 #3935 SUCCESS**，ASP/Clingo/CI gate SUCCESS。最新完整非截断 Git tree **1005 个文件**（此前 1112），Android 生产 Kotlin **298 个**、测试 Kotlin **240 个**；epistemic 生产 9/test 12，web 文件 0。超 50,000 字节源码 3 件：App 188473B、HostScreen 149006B、NightStepUi 60237B；旧 web/app.js 已删。无真实收费 API/真机测试，PR #334 仍 Draft。



## 2026-10-18 全树重启审计（替代沿调用路径逐段清理；最新当前方向）

- 全量 Git tree 快照 SHA `8c1a4dc114060806c33d5f78102b6b3c6457cf71`：未截断、**1135 个文件**（树条目除目录）、按扩展名初筛 **723 个代码/配置类文件**（含 Kotlin/DSL、Java、JS/TS、HTML/CSS、Python、Shell、XML、YAML、JSON 等）；Android Kotlin **337 生产 + 320 测试**。不同扩展名口径下的源代码数量不能与上方历史的 717 或窄口径 703 直接比作增删。
- 审计必须**横向遍历全部真实代码目录及构建/工具资产**：Host UI 与根组件、Session、domain、epistemic、rules、recommendation、setup、flow、catalog、persistence、debug、test、Web、工具/CI；每个文件先按职责分类，而不是沿某个旧规则调用栈寻找下一步。
- **决策标签**：`KEEP_CORE` 唯一事实、事件、安全、隐私、必要输入/展示；`KEEP_AI` 实际 AI 规划、建议、提示词、Provider、响应/战略与必须的规则候选依赖；`REMOVE_CONFIRMED` 仅自身或专属测试使用的旧实验/适配和过度 Host 规则门禁；`REMOVE_AFTER_DISCONNECT` 已判断应删但存在实际消费者，整条拆除后删；`REVIEW_SCOPE` 需要产品边界决策；`AUDIT_PENDING` 只有路径/体积证据，尚不能声称消费者审计完成。不得以目录名推定去留，也不得保留原代码仅因为旧测试要求。
- 全仓源码索引 `MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv` 是**旧基线库存**，不是另一套可写状态；新增审计结果统一记入本文件。任何新 HEAD 对照均重新拉完整 Git 树，不以静态清单判定实际删除完成。

| 目录/链路 | 当前判定 | 可核实依据与操作 |
|---|---|---|
| domain / ClocktowerGameSession / canonical ActionFact / UI 事实提交 | KEEP_CORE + AUDIT_PENDING（混合文件） | 保留唯一状态、已确认事实、安全、私密/顺序/去重/过期防护；不按旧 Rules 正确答案驳回已发生信息 |
| 首夜 InitialJointOpeningDirectV2、Provider、战略续规划与 UI 预选 | KEEP_AI | `encode` 已不把候选列表交给模型。保留真实模型请求、响应合同、上下文/记忆和 UI | 
| 首夜旧合法域 / 语义匹配 / V1 scope | F6 REMOVED | 已断开首夜 Provider/App 消费并整删；改为结构化 AI 建议和请求身份，UI/setup 与后夜残留仍另行审计 |
| FixedInformationEvaluator、FirstNightNumericInformationSemantics、TroubleBrewingRegistrationDomain | REMOVE_AFTER_DISCONNECT（混合） | 当前仍被 Host/UI 候选与 AI 辅助旧路径引用，**不代表 AI 需要 Host 给出候选**；优先断开合法域复核及候选消费，不牺牲真实 AI 功能。结构/必要手动操作可独立保留 |
| A4 identity reveal prewarm / A4 runtime/ZDD rollout | F3 + F4 REMOVE_CONFIRMED | Debug-only 缓存已在 F3 删除；F4 确认 A4 world runtime、rollout policy、ZDD 原型无生产消费者，仅测试自持，连专属测试和双轨契约整删 |
| B4DynamicPlayerWorldSetShadow / ExactHistorical 和 Topology bundle 旧实验 | F2 + F5 REMOVE_CONFIRMED | B4 查询接口在 F2 已删；独立 Exact/Topology/枚举世界实验及专属测试在 F5 一并删除；事件/观察核心类型未删 |
| FN-BUNDLE PublicGoodInfoProjection 等压力实验 | REMOVE_AFTER_DISCONNECT 待核 | 旧实验/测试消费者存在；与正式联合规划 API 不是同一合同；先完整检查引用，再批量删除 |
| ClocktowerHostSelectionSemantics / HostScreen / NightStepUi | AUDIT_PENDING 混合 | 夜间人工候选过滤和规则否决需要删，但显示、事实与 AI 自动/辅助流程要保留 |
| legacy web/ 独立网页客户端及多剧本 | F5 REMOVE_CONFIRMED | 产品所有者批准整删：Git tree 中 web/ 已零文件；不是新 WebHost |
| tools、CI、assets、测试 | AUDIT_PENDING | 以当前 CI/R2/运行时消费者实际使用为依据，专属废弃实验测试可与生产实现一起删除 |

### F4：A4/ZDD 原型整链退役；AI 无 Host 合法候选原则（CI/R2 已完成）

- **最终设计**：AI 不从 Host 获取候选表，只接收真实 GameState 和已确认事件，自行规划。源码核实：`InitialJointOpeningDirectV2.encode` 没有输出候选列表，但 `decode` 仍走 `HostOpeningSemanticGateV2.accept(raw, domain.forDrunkSeat(drunk))`，由 `HostOpeningRulesDomainV2` 的规则候选域重验模型方案。这部分标记 `REMOVE_AFTER_DISCONNECT`，不是长期 `KEEP_AI`；安全合同的 identity/版本/座位/隐私与 JSON shape、strategic memory 必须保留。
- **真实删除**：`A4PlayerWorldSetRuntime.kt`、`A4WorldEngineRuntimePolicy.kt`、`ZddPlayerWorldSet.kt` 及三份同名专属测试整删；`KnowledgeSafeWorldInputTest` 保留结构输入/隐藏真相验证，退役仅对 A4 Runtime 的测试；`KnowledgeTimelineSemanticsTest` 保留共享时间线检查，退役孤立 ZDD 双轨；`A3GoldenContractCatalogTest` 金标准改用已存在的枚举引擎，不再生成第二份 ZDD 结果；Gradle 移除过期 `testFast` 的 ZDD 排除。
- 实测删除后 `app/src` 不再含 `A4PlayerWorldSetRuntime` / `A4WorldEngineRuntimePolicy` / `ZddPlayerWorldSet` / `ZddFilterStrategy` / `useZdd` 调用。本批不动 Session、ActionFact、模型 API、AI 请求、提示词、战略记忆或 UI。**代码 SHA `52ef98ee4c406fc49af21e1592a954da7e4246ff`，15 文件 +27/-1309，净删 1282 行；代码净删 1296 行（生产 740 行、旧测试及混合测试 555 行、Gradle 1 行）。完整 Android CI #4351 SUCCESS，R2 #3931 SUCCESS，ASP/Clingo/CI gate 成功。** 当前未截断树 1112 文件、宽口径代码/配置 700、Android 326 生产 Kotlin/308 测试 Kotlin。超 50000B 四源码仍是 App 188473B、HostScreen 149006B、NightStepUi 60237B、web/app.js 143503B。未进行真实付费 API/真机验收。
### 全树横向删除批次 F3（已通过完整 GitHub CI/R2）

- 跨目录整体删除 `CampBoardGameHostApp.kt` 中**仅适用于 Debug 5 人局**的 A4 identity-reveal prewarm、观察缓存重建、worker 和框架耗时诊断，以及伴随每个 Session 写入/修订触发的旧 A4 cache-invalidations 和临时记录 ID，不改动原 Session 状态写入、公开/私密 EpistemicObservationLog、结构化事件或真实首夜 AI 预计算。
- 完整删除 5 个已无生产消费者的 A4 影子缓存源码：`A4IdentityRevealPrewarmCoordinator.kt`、`A4ObservationCacheRebuildExecutor.kt`、`A4ObservationCacheUpdateCoordinator.kt`、`A4ShadowWorldSetCache.kt`、`A4ShadowLifecycleInvalidator.kt`，及它们 5 个独占单测/实验验证类（含 `A4ShadowProductionIsolationTest.kt`，已无对象可测）。
- **保留** `A4PlayerKnowledgeFactory`、`A4PlayerWorldSetRuntime`、`A4WorldEngineRuntimePolicy`、`ZddPlayerWorldSet` 等被其他精确推演测试使用的类型，本批没有整包误删；下一次仍需判定这些类有无真实 AI 消费者，不能以旧测试为长期保留理由。
- 对 App 的所有 A4 调用和源文件互引用进行实际符号检索，删除后无 A4 预热/缓存/失效器残余；生产 AI Provider、整局首夜与后夜推荐、战略记忆、预选和模式路径保持原样。**代码 SHA `6ea36666ceb0b2d3d7a919a25c2e0b04841f7dce`，14 文件、生产源码 -977 行（App -187，5 服务 -790）、专属测试 -735 行，净删 1712 行代码**；文档 +22/-6，全提交 +22/-1718，净删 1696 行。完整 Android **CI #4349 SUCCESS**，R2 **#3929 SUCCESS**，ASP/Clingo/CI gate 全部成功。最新 Git tree 1118 文件、宽口径代码/配置 706、Android 329 主 Kotlin + 311 测试 Kotlin；源码超 50000B 仍为 App 188473B、HostScreen 149006B、NightStepUi 60237B、web/app.js 143503B。无付费 API 或真机验收。

### 全树横向删除批次 F2（已通过 CI/R2）

- 生产 `epistemic/B4DynamicPlayerWorldSetShadow.kt`（B4ShadowRequest、B4ShadowCandidate、B4ShadowReport、`b4FormalGameState`、`b4ReplayObservationsInTimelineOrder`）在 `app/src/main` 无任何外部使用：B4 旧影子查询适配只给专属实验提供接口，**不是 App 首夜/后夜 AI 推荐、Host 状态或隐私写入依赖**。
- 整删该生产文件以及独占的 `B4DynamicPlayerWorldSetShadowTest.kt`、`B4HistoricalExactShadowBridgeTest.kt`；`CrossTypeTimelineCompatibilityTest` 改为直接测试原始 `ActionFactTimeline.requireCompatibleWith(observationLog)` 和防外部列表篡改，`ExactHistoricalHypotheticalObservationBundleEvaluatorTest` 保留真实 Exact 评估稳定性/不变更记录断言，删 B4 对照包装要求。
- `A4IdentityRevealPrewarmCoordinator` / `A4ObservationCacheRebuildExecutor` **仍由 App 生产引用**，故仍标记 `REMOVE_AFTER_DISCONNECT` 而不误删；`ExactHistoricalHypotheticalObservationBundleEvaluator` 和 Topology 分支当前仅旧测试消费者，但仍在关联实验系统中，待整组独立审计，未动。
- F2 代码提交 `bdf5dbcdccdb9ad7024580a2283ad4d704f496a6`；完整 Android CI **#4348 SUCCESS**、R2 **#3928 SUCCESS**（ASP/Clingo/CI gate 均成功）。整文件删除生产 180 行与专属测试 391 行，混合测试另净删 26 行；整个 F2 无 Canonical GameState、ActionFact、AI Provider、提示词、战略记忆、OpenAI 传输或 Android UI 生产改动。

### 全树横向删除批次 F1（源码修改与 CI 以提交为准）

- **整删 `clocktower/recommendation/DecisionSeedFactory.kt` 及 `DecisionSeedFactoryTest.kt`**：旧 `DecisionSeedFactory`/`DecisionSeedMaterial` 在全部生产 Kotlin 中只有自身文件，无实际 AI 推荐调用，专属测试只测旧决定种子和旧回放假设。
- **整删 `clocktower/recommendation/TroubleBrewingSetupRecommendationDecisionContext.kt` 及 `TroubleBrewingSetupRecommendationDecisionContextTest.kt`**：旧 Builder/上下文在完整仓库的源码检索中只有自身和专属测试引用，当前真正首夜联合规划依赖的是 `HostOpeningRulesDomainV2` 等，而不是这个旧快照转换器。
- 两条完整孤立生产/测试链均与 Host 核心事实、AI Provider、生产提示词和战略记忆无生产调用关系。此批是**按全树角色筛选而非沿 B4 规则调用栈的删除**。已提交 `24995d37041bdf24a5bd6ad8da50a51ad693c8b4`；4 个文件全删 478 行（生产 210 + 专属测试 268），包含文档共 8 文件 +44/-481，净删 437 行。GitHub **完整 Android CI #4346 SUCCESS**、**R2 #3926 SUCCESS**，ASP/Clingo/CI gate 成功。新 Git 树文件 1131、宽口径代码/配置 719，Android 335 主 Kotlin + 318 测试 Kotlin；4 个超 50,000B 实际源文件仍为 App/HostScreen/NightStepUi/web.app.js。未完成的遍历项目保留 `AUDIT_PENDING`，不宣称 337 个起始生产 Kotlin 已逐文件完成语义审计。

**审计基线：** 64be30905b88f639a464204ed1db89b7b62bc910（PR #333 当前 HEAD）。本文件属于当前开发路线的源码库存与分批清理台账，不把“列出文件”混同“已完成逐项语义审计”。

## 两条硬约束

1. 产品只保留继续真实游戏的**最小当前状态 + 一条按实际发生顺序的权威关键事件记录 + 必要的人机操作**。废弃 Host 的规则求值、合法候选枚举、登记推断和历史世界搜索；保留身份/隐私、单次原子确认、防重复、过期 AI 隔离等数据安全防护。
2. **每个源文件严格小于或等于 50,000 字节**（十进制 KB；同时满足 50 KiB）。优先净删无效职责，再拆分仍需要的组件；不可为达到行数而创建委托壳或重复事实对象。

**分阶段交互：** 当前清理期人工信息输入先不加任何游戏规则合法性限制，可选择数值、真假、角色或两座位等结构上表达得出的内容；清理完成后才增加 pair 的座位/角色动态选项、厨师开局状态上限、共情者每次状态相关候选按钮。第二阶段是 UI 建议，不可复活多份事实对象或旧规则引擎。

## 全树元数据检查

- GitHub tree 为完整遍历：文件 1150；匹配源代码扩展名 698；Android 生产 Kotlin 348；Android 测试 Kotlin 327。其余源代码位于 web、tools、ui、artifacts 等目录。
- 基线 >50,000 字节源文件 4 个：
  - `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt` — **198,706 B**
  - `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt` — **150,032 B**
  - `web/app.js` — **143,503 B**
  - `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt` — **60,272 B**

## 模块级决定

| 模块 | 生产 Kotlin 数量 | 判断 | 处理说明 |
|---|---:|---|---|
| Android 根组件、方桌和夜间交互 | 122 | 逐文件审计，生产消费者迁移后判定 | 分离最小导航/确认/展示与规则候选；删除多余的夜间手动规则助手，保留最小操作 |
| Session / AI | 46 | 逐文件审计，生产消费者迁移后判定 | 保留单一状态和提交边界、过期请求防护、结构化事实；消除候选语义协议及冗余策略控制 |
| 状态 / 实际角色 / 事件 | 26 | 逐文件审计，生产消费者迁移后判定 | 保留 GameState 与 ActionFact；其他类型按真实消费者删减 |
| 观察记录 / 历史推理 | 45 | 逐文件审计，生产消费者迁移后判定 | 保留观察日志、时间线、序列化；A4/B4/ZDD/世界枚举推理链为重点删除候选，必须先核查生产使用 |
| 规则 / 线索计算 | 23 | 逐文件审计，生产消费者迁移后判定 | 拟删除游戏规则判断、合法候选、自动真值推导；先隔离残留生产消费者 |
| 推荐 / 合法域 | 15 | 逐文件审计，生产消费者迁移后判定 | 迁移最后生产消费者后删除 Host 候选域与旧策略评分；保留 AI 传输必须的最小字段 |
| 发牌与初始配置 | 22 | 逐文件审计，生产消费者迁移后判定 | 保留身份发放与实际初始事实记录，删除额外合法性/评分/候选附属路径 |
| 流程顺序 | 10 | 逐文件审计，生产消费者迁移后判定 | 保留阶段/行动操作所需最小路由，拆除规则推导前置步骤 |
| 方桌 UI | 18 | 逐文件审计，生产消费者迁移后判定 | 确认与展示职责保留，其余按迁移后净删，ClocktowerHostScreen 为超限关键 |
| 脚本与官方素材 | 8 | 逐文件审计，生产消费者迁移后判定 | 角色名字及展示元数据按 UI 实际需要保留；规则校验型 Catalog 删除 |
| 其他 Clocktower 支持 | 0 | 逐文件审计，生产消费者迁移后判定 | 核对实际 Session 使用，避免重复快照 |
| 偏好/凭据持久化 | 5 | 逐文件审计，生产消费者迁移后判定 | 仅保留必要配置及安全凭据，不复活独立游戏存档 |
| 调试记录 | 2 | 逐文件审计，生产消费者迁移后判定 | 仅保留用户明确需要的诊断能力，否则列入后续删除 |
| 旧模拟模块 | 1 | 确认删除 | 无生产使用的模拟报表整模块删除 |

### 本轮可立即删除（已验证源代码消费者）

- `app/src/main/java/com/codex/campboardgamehost/clocktower/simulation/DistributionReport.kt` — 无生产调用/仅独立旧基准或影子对照职责
- `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/A4DeviceBenchmarkHarness.kt` — 无生产调用/仅独立旧基准或影子对照职责
- `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerB4HistoricalShadowCoordinator.kt` — 无生产调用/仅独立旧基准或影子对照职责
- `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/A4DeviceBenchmarkHarnessTest.kt` — 独立旧测试，仅测试被删除功能
- `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerB4HistoricalShadowCoordinatorTest.kt` — 独立旧测试，仅测试被删除功能

### 需所有者审核的范围（不可先假设删除）

- **`web/` 独立离线网页产品**：`web/app.js` 已达 143,503 B，无法仅通过 Android 运行确认它可直接删。需决定整套 web 是否仍属于本仓产品；若保留则须模块拆分到 <=50,000 B/源文件。
- **A4/B4 活跃影子推理链**：`CampBoardGameHostApp.kt` 存在 A4 identity-reveal prewarm 调用，存在与历史推理联动的消费者；按当前最小合同高度疑似不必要，但必须完成状态、隐私、并发消耗读写审计后整链删除。
- **两个剧本及旧推荐路径**：实际角色/展示元数据仍在调用规则/候选组件；必须先迁移操作路径再删，不能只按目录名删除。
- **调试回放、旧实验/脚本及工具目录**：需要识别 CI/R2 的真实调用再决定删除，不删除仍用于独立验收的工具。

## 源文件逐项库存（当前阶段是体积及路径扫描，非全文件行为审计）


### Android 根组件、方桌和夜间交互 (122)

- `AppClocktowerLandingScreen.kt` — 6026 B
- `AppDealScreens.kt` — 13556 B
- `AppGameModels.kt` — 464 B
- `AppGameReviewScreens.kt` — 12093 B
- `AppSettingsScreen.kt` — 10085 B
- `BotcAiTrace.kt` — 3982 B
- `CampBoardGameHostApp.kt` — 198706 B；超过 50 KB：必须净删/拆分
- `CampBoardGameHostApplication.kt` — 367 B
- `ClocktowerAppModels.kt` — 1007 B
- `ClocktowerArtistTableState.kt` — 2237 B
- `ClocktowerAutomaticRulings.kt` — 605 B
- `ClocktowerBeginnerCompactNightUi.kt` — 7186 B
- `ClocktowerBeginnerNightGuidance.kt` — 4873 B
- `ClocktowerBooleanDisplayOptionSelection.kt` — 934 B
- `ClocktowerChambermaidPresentationSemantics.kt` — 2273 B
- `ClocktowerChambermaidSquareTableUi.kt` — 8577 B
- `ClocktowerChambermaidStepMaterializer.kt` — 1981 B
- `ClocktowerChefSquareTableUi.kt` — 13911 B
- `ClocktowerClockmakerSquareTableUi.kt` — 10904 B
- `ClocktowerContrastPolicy.kt` — 1565 B
- `ClocktowerCurrentDemonAuthority.kt` — 2751 B
- `ClocktowerCurrentResidualScopeV2.kt` — 7658 B
- `ClocktowerDayNominationGesture.kt` — 2043 B
- `ClocktowerDayOverviewTableState.kt` — 1280 B
- `ClocktowerDemonAttackProductionAdapter.kt` — 6684 B
- `ClocktowerDemonBluffPresentation.kt` — 2251 B
- `ClocktowerDirectBooleanResult.kt` — 1495 B
- `ClocktowerDirectNumericResultUi.kt` — 5204 B
- `ClocktowerDynamicNightAdvance.kt` — 2118 B
- `ClocktowerEmpathSquareTableUi.kt` — 14281 B
- `ClocktowerEvilInfoSquareTableUi.kt` — 8274 B
- `ClocktowerFirstNightEvilInformationPresentation.kt` — 8559 B
- `ClocktowerFirstNightInformationRequest.kt` — 8620 B
- `ClocktowerFortuneTellerSquareTableUi.kt` — 8796 B
- `ClocktowerGhostVoteAuthority.kt` — 913 B
- `ClocktowerGhostVotePresentation.kt` — 304 B
- `ClocktowerGlobalFirstNightDecision.kt` — 2783 B
- `ClocktowerGlobalFirstNightScalarDecision.kt` — 11002 B
- `ClocktowerHostBottomInsetPolicy.kt` — 1233 B
- `ClocktowerHostCoreSemantics.kt` — 8233 B
- `ClocktowerHostFullScreenScaffold.kt` — 2726 B
- `ClocktowerHostPresentationModels.kt` — 3677 B
- `ClocktowerHostSeatContentPresentation.kt` — 2600 B
- `ClocktowerHostSelectionSemantics.kt` — 19123 B
- `ClocktowerHostSquareTableScaffold.kt` — 2788 B
- `ClocktowerHostTableContract.kt` — 5651 B
- `ClocktowerHostTableGameStateProjection.kt` — 1916 B
- `ClocktowerHostTableLayout.kt` — 12909 B
- `ClocktowerHostTableReorder.kt` — 2043 B
- `ClocktowerHostTableSurfaceLayout.kt` — 3596 B
- `ClocktowerHostTableUi.kt` — 7643 B
- `ClocktowerInformationStepBuilder.kt` — 9871 B
- `ClocktowerJointOpeningDisplayMatcherV2.kt` — 5261 B
- `ClocktowerJointOpeningReviewScreen.kt` — 4369 B
- `ClocktowerKlutzTableState.kt` — 2629 B
- `ClocktowerNeutralInformationPreparation.kt` — 12286 B
- `ClocktowerNightActionSquareTableUi.kt` — 10146 B
- `ClocktowerNightFullScreenOwnership.kt` — 3756 B
- `ClocktowerNightHostProjection.kt` — 18412 B
- `ClocktowerNightPresentationRole.kt` — 710 B
- `ClocktowerNightStepMaterializerRegistry.kt` — 3241 B
- `ClocktowerNightStepUi.kt` — 60272 B；超过 50 KB：必须净删/拆分
- `ClocktowerNightTargetLegality.kt` — 305 B
- `ClocktowerNumericInformationOptionPreparation.kt` — 1317 B
- `ClocktowerPairInformationSquareTableUi.kt` — 15082 B
- `ClocktowerPairManualAuthority.kt` — 11611 B
- `ClocktowerPairManualSelectionModel.kt` — 5862 B
- `ClocktowerPairManualSelectionUi.kt` — 1683 B
- `ClocktowerPairPlayerRevealPresentation.kt` — 3074 B
- `ClocktowerPhaseStorytellerAdapter.kt` — 407 B
- `ClocktowerPlainInformationSquareTableUi.kt` — 6219 B
- `ClocktowerPlayerDisplayResolution.kt` — 5007 B
- `ClocktowerPlayerDisplayUi.kt` — 15450 B
- `ClocktowerPlayerRevealHandoff.kt` — 2463 B
- `ClocktowerPresentationRoleCatalog.kt` — 7722 B
- `ClocktowerPresentationTheme.kt` — 3168 B
- `ClocktowerRavenkeeperSquareTableUi.kt` — 13933 B
- `ClocktowerRecommendationRules.kt` — 875 B
- `ClocktowerRecurringInformationStepMaterializers.kt` — 5233 B
- `ClocktowerRegistrationDecisionControls.kt` — 7221 B
- `ClocktowerRegistrationInteractionState.kt` — 4023 B
- `ClocktowerRegistrationOptionPreparation.kt` — 5574 B
- `ClocktowerRegistrationProjection.kt` — 5990 B
- `ClocktowerRegistrationResultDomain.kt` — 3809 B
- `ClocktowerRegistrationResultPresentation.kt` — 4927 B
- `ClocktowerResidualDisplayMatcherV2.kt` — 14862 B
- `ClocktowerResidualRequestIdentityV2.kt` — 1054 B
- `ClocktowerRoleLocalization.kt` — 583 B
- `ClocktowerRoleRevealChoiceProjection.kt` — 2263 B
- `ClocktowerRuleOnlyCompactChoice.kt` — 352 B
- `ClocktowerSageSquareTableUi.kt` — 11900 B
- `ClocktowerSageStepMaterializer.kt` — 5652 B
- `ClocktowerSeatNumberBadge.kt` — 2796 B
- `ClocktowerSeatNumberLabel.kt` — 390 B
- `ClocktowerSessionPlayerCards.kt` — 2544 B
- `ClocktowerSetupRecommendationSemantics.kt` — 705 B
- `ClocktowerSingleNumericChoiceUi.kt` — 4197 B
- `ClocktowerSingleRecommendationChoiceUi.kt` — 2362 B
- `ClocktowerSingleTargetInteractionPresentation.kt` — 3915 B
- `ClocktowerSingleTargetInteractionUi.kt` — 6760 B
- `ClocktowerSlayerTableState.kt` — 3543 B
- `ClocktowerSquareTableLifeMarkers.kt` — 434 B
- `ClocktowerSquareTableSeatDensityPolicy.kt` — 3360 B
- `ClocktowerSquareTableUi.kt` — 35812 B
- `ClocktowerStorytellerRecommendationUi.kt` — 7356 B
- `ClocktowerStructuredInformationPreparation.kt` — 9138 B
- `ClocktowerTableVoteState.kt` — 3141 B
- `ClocktowerUndertakerSquareTableUi.kt` — 13044 B
- `ClocktowerVoteHistoryRecord.kt` — 1024 B
- `CommonPlayersDefaultPolicy.kt` — 415 B
- `GameSettingsUi.kt` — 2269 B
- `HostBottomActionBar.kt` — 3802 B
- `HostInteractionUi.kt` — 844 B
- `HostSeatingRoster.kt` — 2101 B
- `HostSeatingSetupFlow.kt` — 742 B
- `MainActivity.kt` — 1103 B
- `PlayerExperiencePicker.kt` — 1921 B
- `SeatingFirstSetupUi.kt` — 10527 B
- `StorytellerAiSettingsPanel.kt` — 8104 B
- `StorytellerOperationMode.kt` — 2026 B
- `StructuredBooleanInformationAdapter.kt` — 5523 B
- `StructuredNumericInformationAdapter.kt` — 5336 B

### 其他源代码 (5)

- `clocktower/config/TroubleBrewingRecommendationMetadata.kt` — 2178 B
- `clocktower/history/HistoricalClueSignature.kt` — 915 B
- `clocktower/history/InformationReferenceExtractor.kt` — 645 B
- `clocktower/persistence/ClocktowerRulesetPersistence.kt` — 1848 B
- `clocktower/TroubleBrewingGameSnapshotJsonCodec.kt` — 7985 B

### 脚本与官方素材 (8)

- `clocktower/catalog/BuiltInClocktowerRulesetCatalog.kt` — 3528 B
- `clocktower/catalog/ClocktowerCatalogModels.kt` — 6756 B
- `clocktower/catalog/ClocktowerScriptJsonParser.kt` — 21423 B
- `clocktower/catalog/ClocktowerScriptNormalizer.kt` — 13636 B
- `clocktower/catalog/ClocktowerScriptValidation.kt` — 1161 B
- `clocktower/catalog/LegacyRulesetCatalogAdapter.kt` — 2594 B
- `clocktower/catalog/NoGreaterJoyOfficialCharacterMetadata.kt` — 3502 B
- `clocktower/catalog/OfficialReminderTokenMetadata.kt` — 3220 B

### 状态 / 实际角色 / 事件 (26)

- `clocktower/domain/ActionFact.kt` — 11069 B
- `clocktower/domain/ClocktowerGameStateAdapter.kt` — 1817 B
- `clocktower/domain/ClocktowerSemanticHistoryMode.kt` — 2302 B
- `clocktower/domain/CommittedClocktowerSetup.kt` — 2462 B
- `clocktower/domain/ConstraintAuthority.kt` — 212 B
- `clocktower/domain/DecisionCandidate.kt` — 1653 B
- `clocktower/domain/DecisionOutcomeSnapshot.kt` — 597 B
- `clocktower/domain/DecisionTypes.kt` — 1489 B
- `clocktower/domain/DynamicDecisionModels.kt` — 134 B
- `clocktower/domain/DynamicInformationOutcome.kt` — 349 B
- `clocktower/domain/EffectDraft.kt` — 3038 B
- `clocktower/domain/GameSnapshot.kt` — 2000 B
- `clocktower/domain/GameState.kt` — 1305 B
- `clocktower/domain/InformationModels.kt` — 1003 B
- `clocktower/domain/MurmurHash3.kt` — 3775 B
- `clocktower/domain/PairInformationOutcome.kt` — 869 B
- `clocktower/domain/RecommendationModels.kt` — 1105 B
- `clocktower/domain/RegistrationOutcome.kt` — 353 B
- `clocktower/domain/RoleCatalogAdapter.kt` — 1337 B
- `clocktower/domain/RulesetRef.kt` — 756 B
- `clocktower/domain/SetupClueOutcome.kt` — 451 B
- `clocktower/domain/StableCandidateIdFactory.kt` — 1747 B
- `clocktower/domain/StorytellerPlayerContext.kt` — 1439 B
- `clocktower/domain/StorytellerProviderContractV1.kt` — 23003 B
- `clocktower/domain/StorytellerProviderHistoryPrefixV1.kt` — 9442 B
- `clocktower/domain/TroubleBrewingGameSnapshot.kt` — 5165 B

### 观察记录 / 历史推理 (45)

- `clocktower/epistemic/A4DeviceBenchmarkHarness.kt` — 8954 B；此批确认删除
- `clocktower/epistemic/A4IdentityRevealPrewarmCoordinator.kt` — 19443 B
- `clocktower/epistemic/A4ObservationCacheRebuildExecutor.kt` — 10141 B
- `clocktower/epistemic/A4ObservationCacheUpdateCoordinator.kt` — 1888 B
- `clocktower/epistemic/A4PlayerKnowledgeFactory.kt` — 4467 B
- `clocktower/epistemic/A4PlayerWorldSetRuntime.kt` — 6031 B
- `clocktower/epistemic/A4ShadowLifecycleInvalidator.kt` — 970 B
- `clocktower/epistemic/A4ShadowWorldSetCache.kt` — 2432 B
- `clocktower/epistemic/A4WorldEngineRuntimePolicy.kt` — 3208 B
- `clocktower/epistemic/ActionFactDraft.kt` — 12179 B
- `clocktower/epistemic/ActionFactTimeline.kt` — 5094 B
- `clocktower/epistemic/B4DynamicPlayerWorldSetShadow.kt` — 8920 B
- `clocktower/epistemic/EnumeratedHistoricalExactBaseline.kt` — 6030 B
- `clocktower/epistemic/EnumeratedHistoricalWorldReplay.kt` — 19075 B
- `clocktower/epistemic/EnumeratedWorldImpSelfKillSuccessionBranching.kt` — 3437 B
- `clocktower/epistemic/EnumeratedWorldMayorNightDeathBranching.kt` — 4707 B
- `clocktower/epistemic/EnumeratedWorldNightObservationAnchor.kt` — 3639 B
- `clocktower/epistemic/EnumeratedWorldNightReplayPlan.kt` — 2668 B
- `clocktower/epistemic/EnumeratedWorldNightSchedule.kt` — 1432 B
- `clocktower/epistemic/EnumeratedWorldOtherNightAttackBranching.kt` — 3055 B
- `clocktower/epistemic/EnumeratedWorldOtherNightMechanicsMaterializer.kt` — 3734 B
- `clocktower/epistemic/EnumeratedWorldOtherNightProtectionBranching.kt` — 2068 B
- `clocktower/epistemic/EnumeratedWorldSet.kt` — 36760 B
- `clocktower/epistemic/EpistemicContracts.kt` — 8761 B
- `clocktower/epistemic/EpistemicEvaluationCapability.kt` — 2975 B
- `clocktower/epistemic/EpistemicObservationDraft.kt` — 3378 B
- `clocktower/epistemic/EpistemicObservationLog.kt` — 9783 B
- `clocktower/epistemic/EpistemicSemanticJson.kt` — 38851 B
- `clocktower/epistemic/EpistemicSemanticModel.kt` — 24684 B
- `clocktower/epistemic/ExactHistoricalHypotheticalObservationBundleEvaluator.kt` — 28136 B
- `clocktower/epistemic/GrimoireReminderToken.kt` — 3201 B
- `clocktower/epistemic/KnowledgeConstructionInput.kt` — 2734 B
- `clocktower/epistemic/KnowledgeSafeWorldInput.kt` — 1706 B
- `clocktower/epistemic/PlayerHistoricalTimeline.kt` — 7026 B
- `clocktower/epistemic/PlayerWorldSet.kt` — 2890 B
- `clocktower/epistemic/RegistrationWitnessSemanticMatcher.kt` — 2313 B
- `clocktower/epistemic/SpyGrimoireTruthProjection.kt` — 5574 B
- `clocktower/epistemic/StrategicWorldKey.kt` — 2182 B
- `clocktower/epistemic/TroubleBrewingGrimoireReminderSemantics.kt` — 2352 B
- `clocktower/epistemic/TroubleBrewingStrategicTopologyDomain.kt` — 2404 B
- `clocktower/epistemic/TroubleBrewingTopologyHypotheticalBundleEvaluator.kt` — 10056 B
- `clocktower/epistemic/TroubleBrewingTopologyObservationWitnessEvaluator.kt` — 28837 B
- `clocktower/epistemic/TroubleBrewingTopologySetupWitnessEvaluator.kt` — 24302 B
- `clocktower/epistemic/TroubleBrewingWorldEnumerator.kt` — 11627 B
- `clocktower/epistemic/ZddPlayerWorldSet.kt` — 24866 B

### 流程顺序 (10)

- `clocktower/flow/ClocktowerCharacterInteractionRegistry.kt` — 8465 B
- `clocktower/flow/ClocktowerFlowPlanner.kt` — 4731 B
- `clocktower/flow/ClocktowerHostInteraction.kt` — 1576 B
- `clocktower/flow/ClocktowerHostInteractionProjector.kt` — 5245 B
- `clocktower/flow/ClocktowerProductionFirstNightFlow.kt` — 2855 B
- `clocktower/flow/ClocktowerProductionInteractionOrderer.kt` — 6257 B
- `clocktower/flow/ClocktowerProductionOtherNightFlow.kt` — 2198 B
- `clocktower/flow/ClocktowerResolvedFlowFacts.kt` — 877 B
- `clocktower/flow/FirstNightSetupDependencyPlanner.kt` — 3658 B
- `clocktower/flow/FirstNightSetupDependencyProjection.kt` — 11867 B

### 推荐 / 合法域 (15)

- `clocktower/recommendation/DecisionSeedFactory.kt` — 1866 B
- `clocktower/recommendation/dynamic/DynamicCandidateGenerator.kt` — 5350 B
- `clocktower/recommendation/dynamic/PairInformationPresentationModels.kt` — 380 B
- `clocktower/recommendation/dynamic/UnreliableInformationModels.kt` — 1577 B
- `clocktower/recommendation/FirstNightBundleCandidateSpaceAudit.kt` — 16640 B
- `clocktower/recommendation/FirstNightBundleExperimentContract.kt` — 8207 B
- `clocktower/recommendation/FirstNightInformationPropositionMaterializer.kt` — 6870 B
- `clocktower/recommendation/FirstNightNumericLegalDomain.kt` — 2143 B
- `clocktower/recommendation/NaturalPairInformationCandidateGenerator.kt` — 1999 B
- `clocktower/recommendation/PairInformationLegalDomain.kt` — 1188 B
- `clocktower/recommendation/setup/SetupCandidateGenerator.kt` — 10175 B
- `clocktower/recommendation/StorytellerDecisionAuthority.kt` — 1711 B
- `clocktower/recommendation/TroubleBrewingFirstNightPairDecisionContext.kt` — 6666 B
- `clocktower/recommendation/TroubleBrewingSetupRecommendationDecisionContext.kt` — 7410 B
- `clocktower/recommendation/UnifiedSelectionPool.kt` — 2465 B

### 规则 / 线索计算 (23)

- `clocktower/rules/AbilityFunctioningSemantics.kt` — 2689 B
- `clocktower/rules/ClocktowerEffectiveNightState.kt` — 3936 B
- `clocktower/rules/ClocktowerOptionalNightSourceChronology.kt` — 1387 B
- `clocktower/rules/CurrentDemonAuthority.kt` — 655 B
- `clocktower/rules/DemonNightAttackSemantics.kt` — 1752 B
- `clocktower/rules/DemonSuccessionSemantics.kt` — 1186 B
- `clocktower/rules/FirstNightNumericInformationDisplayDomain.kt` — 2885 B
- `clocktower/rules/FirstNightNumericInformationSemantics.kt` — 4964 B
- `clocktower/rules/FixedInformationEvaluator.kt` — 1578 B
- `clocktower/rules/FortuneTellerInformationSemantics.kt` — 3137 B
- `clocktower/rules/MayorRedirectLegality.kt` — 2068 B
- `clocktower/rules/NaturalPairInformationCandidateGenerator.kt` — 16610 B
- `clocktower/rules/NoGreaterJoyKlutzChoiceRuleV1.kt` — 2569 B
- `clocktower/rules/PairInformationDisplaySemantics.kt` — 2901 B
- `clocktower/rules/PairInformationLegalDomain.kt` — 4078 B
- `clocktower/rules/PlanLegalityValidator.kt` — 8310 B
- `clocktower/rules/PoisonEffectLifecycle.kt` — 480 B
- `clocktower/rules/RegistrationInteractionRules.kt` — 347 B
- `clocktower/rules/RulesetContentHasher.kt` — 2875 B
- `clocktower/rules/RulesetJsonLoader.kt` — 3108 B
- `clocktower/rules/RulesetKnowledge.kt` — 995 B
- `clocktower/rules/TroubleBrewingRecurringPlayerInformationRouteDomain.kt` — 1394 B
- `clocktower/rules/TroubleBrewingRegistrationDomain.kt` — 5578 B

### Session / AI (46)

- `clocktower/session/AuthenticatedModelTransportV1.kt` — 2770 B
- `clocktower/session/BotcAiWireTrace.kt` — 6748 B
- `clocktower/session/ClocktowerB4HistoricalShadowCoordinator.kt` — 3468 B；此批确认删除
- `clocktower/session/ClocktowerGameSession.kt` — 41763 B
- `clocktower/session/ClocktowerGameSessionAliveBoundaries.kt` — 2512 B
- `clocktower/session/ClocktowerGameSessionPoisonBoundaries.kt` — 4846 B
- `clocktower/session/ClocktowerGameSessionRoleIdentityBoundaries.kt` — 1629 B
- `clocktower/session/ClocktowerNightCheckpoint.kt` — 1320 B
- `clocktower/session/ClocktowerNightTargets.kt` — 1844 B
- `clocktower/session/ClocktowerRecommendationCoordinator.kt` — 3497 B
- `clocktower/session/ClocktowerVoteStanding.kt` — 633 B
- `clocktower/session/DawnCommitIdentity.kt` — 992 B
- `clocktower/session/DayAbilityRegistrationRulingProducerV1.kt` — 20375 B
- `clocktower/session/DrunkAssignmentDecisionBoundary.kt` — 2472 B
- `clocktower/session/FirstNightInformationLifecycle.kt` — 911 B
- `clocktower/session/FirstNightInformationMigration.kt` — 3609 B
- `clocktower/session/FirstNightPoisonLifecycle.kt` — 1257 B
- `clocktower/session/HostCurrentOpenDecisionInventoryV2.kt` — 4612 B
- `clocktower/session/HostOpeningResidualEventGateV2.kt` — 19089 B
- `clocktower/session/HostOpeningRulesDomainV2.kt` — 14527 B
- `clocktower/session/HostOpeningSemanticGateV2.kt` — 18263 B
- `clocktower/session/InformationDecisionFoundation.kt` — 11251 B
- `clocktower/session/InitialGlobalOpeningScopeV1.kt` — 14446 B
- `clocktower/session/InitialJointOpeningDirectV2.kt` — 14756 B
- `clocktower/session/MayorRedirectDecisionBoundary.kt` — 4496 B
- `clocktower/session/NightDawnResolutionPlanner.kt` — 12201 B
- `clocktower/session/NightTransactionRestoreComposition.kt` — 5660 B
- `clocktower/session/NoGreaterJoyKlutzHistoryProducerV1.kt` — 3449 B
- `clocktower/session/PairInformationDecisionBoundary.kt` — 5263 B
- `clocktower/session/PersonalDirectOpenAiV1.kt` — 10171 B
- `clocktower/session/RecommendationModules.kt` — 1741 B
- `clocktower/session/SmallDomainPresentation.kt` — 1608 B
- `clocktower/session/StorytellerCausalDecisionJournalV1.kt` — 17613 B
- `clocktower/session/StorytellerDecisionFoundation.kt` — 3371 B
- `clocktower/session/StorytellerGlobalEventProjectionV1.kt` — 9133 B
- `clocktower/session/StorytellerProviderGameContextBuilderV1.kt` — 3306 B
- `clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1.kt` — 3345 B
- `clocktower/session/StorytellerProviderRequestFactoryV1.kt` — 6606 B
- `clocktower/session/StorytellerResidualPackageGateV2.kt` — 2858 B
- `clocktower/session/StorytellerStrategicChangeCheckpointV2.kt` — 10135 B
- `clocktower/session/StorytellerStrategicContinuationV2.kt` — 28761 B
- `clocktower/session/StructuredBooleanInformationUiModel.kt` — 3290 B
- `clocktower/session/StructuredNumberInformationUiModel.kt` — 3272 B
- `clocktower/session/TroubleBrewingDemonSuccessionResolver.kt` — 6161 B
- `clocktower/session/TroubleBrewingFirstNightPrecomputeCoordinator.kt` — 4153 B
- `clocktower/session/TroubleBrewingRuntimeGameProjector.kt` — 3512 B

### 发牌与初始配置 (22)

- `clocktower/setup/ClocktowerSetupDistribution.kt` — 1018 B
- `clocktower/setup/ClocktowerSetupProvider.kt` — 4186 B
- `clocktower/setup/GeneratedSetupCandidateSource.kt` — 5567 B
- `clocktower/setup/NoGreaterJoyProductionSetupPreparer.kt` — 3562 B
- `clocktower/setup/SetupDiversityHistory.kt` — 1550 B
- `clocktower/setup/SetupDiversitySelector.kt` — 11312 B
- `clocktower/setup/SetupShownIdentityCommitment.kt` — 4807 B
- `clocktower/setup/SetupShownIdentityPolicy.kt` — 8649 B
- `clocktower/setup/TemplateRepository.kt` — 2996 B
- `clocktower/setup/TroubleBrewingDealRoleResolver.kt` — 2756 B
- `clocktower/setup/TroubleBrewingDrunkCandidateProjection.kt` — 9104 B
- `clocktower/setup/TroubleBrewingDrunkSelectionRouter.kt` — 2505 B
- `clocktower/setup/TroubleBrewingGameSnapshotProjector.kt` — 7788 B
- `clocktower/setup/TroubleBrewingIntermediateSetup.kt` — 5166 B
- `clocktower/setup/TroubleBrewingProductionSetupPreparer.kt` — 4782 B
- `clocktower/setup/TroubleBrewingSetupCommitter.kt` — 7659 B
- `clocktower/setup/TroubleBrewingSetupDealPlanner.kt` — 13250 B
- `clocktower/setup/TroubleBrewingSetupPresetJson.kt` — 5548 B
- `clocktower/setup/TroubleBrewingSetupPresetModels.kt` — 1083 B
- `clocktower/setup/TroubleBrewingSetupPresetValidator.kt` — 8574 B
- `clocktower/setup/TroubleBrewingSetupRotationHistory.kt` — 2896 B
- `clocktower/setup/TroubleBrewingSetupRotationRecordFactory.kt` — 11970 B

### 旧模拟模块 (1)

- `clocktower/simulation/DistributionReport.kt` — 4605 B；此批确认删除

### 方桌 UI (18)

- `clocktower/ui/ClocktowerArtistInteraction.kt` — 6603 B
- `clocktower/ui/ClocktowerArtistTableUi.kt` — 4130 B
- `clocktower/ui/ClocktowerDayOverviewTableUi.kt` — 6871 B
- `clocktower/ui/ClocktowerDayScreen.kt` — 13540 B
- `clocktower/ui/ClocktowerDayTableScaffoldUi.kt` — 1002 B
- `clocktower/ui/ClocktowerDrunkSelectionScreen.kt` — 8884 B
- `clocktower/ui/ClocktowerHostScreen.kt` — 150032 B；超过 50 KB：必须净删/拆分
- `clocktower/ui/ClocktowerKlutzTableUi.kt` — 5712 B
- `clocktower/ui/ClocktowerNightScreen.kt` — 15696 B
- `clocktower/ui/ClocktowerNominationInteraction.kt` — 6127 B
- `clocktower/ui/ClocktowerPendingNominationTableUi.kt` — 7382 B
- `clocktower/ui/ClocktowerResultsScreen.kt` — 17211 B
- `clocktower/ui/ClocktowerSetupScreen.kt` — 20901 B
- `clocktower/ui/ClocktowerSlayerInteraction.kt` — 4217 B
- `clocktower/ui/ClocktowerSlayerTableUi.kt` — 8133 B
- `clocktower/ui/ClocktowerVoteTableUi.kt` — 7854 B
- `clocktower/ui/StorytellerAutoPauseScreen.kt` — 2721 B
- `clocktower/ui/StructuredStorytellerInformationUi.kt` — 1753 B

### 调试记录 (2)

- `debug/DebugFlightRecorder.kt` — 8403 B
- `debug/DebugFlightRecorderCore.kt` — 2706 B

### 偏好/凭据持久化 (5)

- `persistence/AppPreferencesStore.kt` — 4026 B
- `persistence/GatewayTokenEnvelopeV1.kt` — 1294 B
- `persistence/PersonalOpenAiSettingsStore.kt` — 1273 B
- `persistence/StorytellerGatewayConnectionStore.kt` — 4649 B
- `persistence/TroubleBrewingSetupRotationHistoryStore.kt` — 12917 B

## 变更验收方法

每批从当前正确 HEAD 起步，定位生产读写者（包括 UI、Session、AI、CI），标记“删除文件、同步删除旧测试、保留的数据完整性”，实际修改后跑 GitHub CI/R2；不要让旧规则校验回归测试阻止明确批准的行为退休。每批给出净减少、超限文件数和留下的待确认风险。PR 在未授权合并前保持 Draft。

## 实施检查点 S0b（2026-10-18）

经过独立生产引用检索，下列原孤立规则/候选模型没有 Android 生产消费者；已随测试删除，而不是为过时测试保留规则引擎：
- `app/src/main/java/com/codex/campboardgamehost/clocktower/session/SmallDomainPresentation.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/history/HistoricalClueSignature.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/PlanLegalityValidator.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ConstraintAuthority.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/session/SmallDomainPresentationTest.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/PlanLegalityValidatorTest.kt`

`CandidateGeneratorTest.kt` 中只依赖 `PlanLegalityValidator` 的旧规则断言已移除，保留该文件其余断言暂待整个旧 setup candidate 生成器迁移审计。**本台账上方库存为基线目录树**，包含清理前的文件名；实际最新余量以当前 PR 的 Git 树为准。

## 全类型源码清单和网页残留复核（2026-10-18）

使用最新 HEAD 重新扫描 Kotlin、Kotlin DSL、Java、JS/TS、HTML/CSS、Python、Shell、XML、YAML 及 Gradle 代码文件：**717 个**。完整路径/字节数/逐文件初步审计队列见 [全源码文件索引](MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv)，所有条目来源于未截断的 Git 树。该索引中的待处理状态只是分流，不宣称所有文件已完成源代码行为审计。

### 新发现：旧 web 产品边界

- `web/app.js` 143,503 B，是独立的离线网页主持台，不依赖 Android Host 当前 Session；包含 `freshUndercover`、`renderUndercover`、`freshWerewolf`、`renderWerewolf`、网页独立 `localStorage` / 事件逻辑。
- 所以“Undercover 已完全清除”严格只对 Android 源代码成立；**整个仓库 web 文件仍包含 Undercover**。是否整个废弃该嵌入式 PWA、是否同时废弃狼人杀，**需用户明确审核**。如果保留 web，就必须先剔除已废弃玩法和独立重复事实状态，再把 `web/app.js` 拆分至 <=50,000 B；不可通过单纯分文件假装业务已经精简。
- 历史 `app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json` 为 331,689 B，属于配置数据而非本轮“源码 <=50 KB”硬限制，但其必要性需要单独审计消费者与模板内容。

