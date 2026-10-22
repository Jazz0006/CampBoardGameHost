# 第四阶段 D4b：共享登记查询投影

基线 D4a #323 `16f32d8e`。本检查点解除白天提名/Slayer 对 Host 内部登记闭包的依赖，尚不宣称白天早分流完成。

Architecture pre-flight:
- current owner: ClocktowerJudgeScreen 内九个登记转换/查询函数，由日间 Virgin/Slayer 和夜间信息角色共同调用。
- proposed responsibility: ClocktowerRegistrationProjection 只读转换卡片/有效身份/中毒及 UI 选择，调用已有 TroubleBrewingRegistrationDomain，返回合法角色和当前显示解释。
- authoritative state owner(s): Session/已有 NightHostProjection 拥有有效事实；RegistrationInteractionState 仍拥有手动选择，remember 生命周期不变；规则域拥有合法性。
- narrow typed input/output seam: cards、script、手动选择、按角色查询有效身份/中毒的两个函数 -> registration resolution/角色列表/显示解释。没有整个 Host、Session、checkpoint 或全局 Context。
- keep in current owner / extract: 共享只读登记投影独立；所有日夜调用共同迁移。以 NightHostProjection 构造完毕后的不可变函数引用替代两个先赋默认值再重赋值的局部 var。
- reason: 白天与夜间都必须继承同一登记语义，不能将夜间闭包或规则副本带进白天界面。

完整扇出：Virgin 提名、Slayer Recluse 选择；Washerwoman/Librarian/Investigator、Fortune Teller、Chef/Empath、Undertaker、Ravenkeeper 等信息准备；结果优先选择和正式发布前的 registration resolution。所有调用必须继承；无例外。registrationKey、手动选择写入与发布回调不移动。spyCard/recluseCard 在 Host 仍供其他信息步骤使用。

验收：有效身份与中毒继续按查询角色读取；手动选择不越过规则合法域；无显式选择时不创造裁定；原 UI 选择生命周期不变。现有规则域/投影/登记回归与完整编译；新增窄投影测试保护跨查询角色状态及显示角色合法筛选。无真机/付费模型验收。

本次重构持续授权：用户已授权本地提交、向 Jazz0006/CampBoardGameHost 的 codex/* 分支推送、创建/更新 Draft PR 和 CI/R2，无需逐次确认；强制推送、删除远端分支和合并须另行确认。

## 实施结果

Host 减少 91 行（2,925 → 2,834），174,272 → 169,852 bytes。共享投影 109 行 / 5,990 bytes，生产总行数增加 18 行。原查询函数体逐段核对未改变；所有日夜调用接入相同投影，没有重新实现合法域，也没有迁移 remember 或正式发布顺序。投影在现有有效夜间投影之后构造，不新增可写事实状态。

这一步是白天拆分的共享前置边界；尚未移走提名/投票 UI，也尚未避开白天执行夜间准备。下一步继续按日间交互收敛，不能以尺寸下降代替完整 D4 验收。

验证：本地 `:app:testFull :app:testFast :app:assembleDebug --offline` 通过，完整 1,337、快速 1,330，失败/错误/跳过均为 0；`git diff --check` 通过。原有规则、登记选择、夜间投影和正式发布回归保留。没有真机或付费模型验收；远端 CI/R2 以 PR 当前 head 为准。
