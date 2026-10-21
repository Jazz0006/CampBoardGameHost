# 全仓遍历与清理收尾验收（2026-10-21）

## 当前结论

最后两批清理和精确远端验收全部完成，**第二阶段已关闭**。当前台账 **430/430 个非 docs 跟踪路径均有结论，0 待审/待删除/待解耦**；215 个生产 Kotlin 文件全部 **≤50000B**。历史 F12 中要退出固定机械/双轮换的建议已被后续所有者合同覆盖，当前逐路径结论替代旧消费者名，不以删除必要规则凑数量。

main仍为`40050941efc3`；同一Draft [#337](https://github.com/Jazz0006/CampBoardGameHost/pull/337)，不合并、不改历史。#334/#335已合并，第一阶段最小整局已关闭。当前工作仅清理收尾；后续转第三阶段界面/流程细节。

## 所有者要求的最后两批

第六批代码`1b8502008cd65332bff99a0e4786db7eac47dbdd`：63个原待审现存非测试路径形成结论，另物理删除10文件。独立Python旧Gateway→synthetic smoke/latency→21项专属测试→专用CI整链退休；旧SDE probe/init与孤立player单页退休，历史审计证据保留。Host通用/首夜显示回既有无状态materializer，后夜回既有Session只读投影；Host63787B→46706B。净删2855行/162007B。Cloud full641/144 suites/38 UI、APK通过；精确[CI #4519](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39588708480)和[R2 #4094](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39588708434)均SUCCESS，full tests/APK构建上传与gate逐步核实。

第七批从同一代码继续：剩余60个测试/fixture全文及被测边界核查，台账待审60→0；复核所有沿用旧架构的分类。App按实际责任收敛：

- App仅保留导航/设置、唯一Session创建/原子保存/恢复、完成局跨局记录。115421B→48947B。
- ClocktowerOpeningDraft（19761B）只持有pre-Session准备/显示发牌/未确认联合开局建议；显式commit回调创建原Session，两个跨局历史查询仍独立。
- ClocktowerAiConversation（25741B）只持有advisory记忆/receipt/model/未确认建议及运输；不保存Session字段、不拥有可写游戏事实/进度；Baseline只保留同一Session不可变比较快照。
- ClocktowerSessionHost（30907B）挂载实际Session与其只读state，局部目标为UI草稿；所有Next/白天/夜序确认调用同一Session。state参数保证同一Session更新后Compose刷新。

无通用Context/Manager，无第二游戏对象/恢复器/进度日志，不是按行数机械拆片。新建3个明确职责源码文件，Session仍46770B，Host46706B，最终最大文件为App。

继续删除：只有测试消费的TemplateShownIdentityPolicySource/Key/resolveTemplate分支及专属用例；旧P1/P15两个opt-in实验wire导出入口；重复生产八段几何算法的测试oracle及专属用例；latest单局轮换查询包装、interactsAs/functionsAs包装、无调用DebugFlightRecorder.updateState镜像；不可能在已有needsReview通过后进入的Host-determined分支及布尔包装/专属测试。当前直连联合开局/续规划V3、规范fixture黄金编码、真实几何分离及两轮换断言保留。修正安全暂停中“只支持Drunk/独立旧引擎”过时文案。

第七批非文档17文件+1485/-1695，**净删210行/3236B**；整删文件0，新生产文件3。删除5项只保护退休实现/导出入口的测试，新增3项实际Conversation回归：匹配最终已提交观察才晋升receipt/memory；人工改答/未确认预览不得晋升。原仅requireNotNull的scalar用例补回当前query/metric/reliability及候选无关断言，未忽略任何现用测试。

两批合计净删**3065行/165243B**、整删10文件。整个#337相对main净删**7635行/376286B**、整删30文件（统计均排除维护docs与AUDIT.md说明，包含实际源码/工具/独占测试/退休示例；不把文档净删冒充产品代码）。

## 验证与边界

最终Cloud full **639 tests/144 suites/38 UI，0 failure/error/skipped**，Debug APK成功。3项实际App两剧本整局/发牌恢复、32项D2真实Compose、3项设置生命周期仍执行；两项跨局防重复/存档AtomicFile失败回滚/隐私与AI协议/记忆/stale全部保留。原R2 shell、10类CI路由/失败门禁/workflow shell、退休符号零残余与diff检查通过。

精确生产代码`e5ffc4e5e3ccdac5a5d4d3d7845f9754b998dbba`的[完整Android CI #4520](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977318)和[R2 #4095](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977316)均SUCCESS，已独立核对21路径/全部diff（大台账校验前后Git blob）、full tests/APK构建上传与CI gate。后续仅文档提交记录本验收，不替代代码构建。Cloud日志`/tmp/campboard-final-gradle5.log`，JUnit摘要`/tmp/campboard-final-tests.json`。第六批CI不能替代第七批代码验收；后续文档提交也不能代替代码全量Android验证。

不声称已做真机/实际进程杀死或付费API验收。后续第三阶段可核对Day提名/Slayer目标范围和UI说明等细节；这些是当前实际交互限制，不是未审源码、死链或本批已确认的整局阻断。没有新增所有者决策阻塞。
