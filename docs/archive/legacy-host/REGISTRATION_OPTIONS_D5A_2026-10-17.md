# 第四阶段 D5a：登记结果候选准备

> 历史检查点：后续执行方向已由 [最小 Host 产品合同](../../MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md) 覆盖。旧规则保留要求及 NEXT 不再适用；本文件保留实施证据。

基线 D4e #327 `a8c1c479`。D4 已完成源码层面的日间早分流，真机生命周期验收仍缺；本步进入 D5 夜间信息准备边界。

Architecture pre-flight:
- current owner: Host 的三个 resultFirst*RegistrationOptions 局部函数。
- proposed responsibility: 独立只读登记候选准备，结合当前登记投影、查询座位与显示文本，调用既有结果展示模块。
- authoritative state owner(s): Session 仍拥有事实；RegistrationInteractionState 仍拥有临时选择；新准备器不保存可写状态，不写登记，也不发布观察。
- narrow typed input/output seam: cards、RegistrationProjection、文本/角色名称函数；每次查询输入角色/目标/登记 key 和数值求值函数，输出 List<ClocktowerDisplayOption>。
- keep in current owner / extract: 提取到 ClocktowerRegistrationOptionPreparation；不传整个 Host、Session、checkpoint、推荐 coordinator，不新增参数包。角色真实数值、有效夜间时序和信息提交仍留原归属。
- reason: 登记候选准备已有独立语义，合法性继续来自 RegistrationProjection/规则域，结果去重与展示继续来自 RegistrationResultPresentation。避免修改候选时必须理解整个 Host。

消费者：numeric 用于 Chef/Empath；Fortune Teller 用于占卜师隐士登记；role reveal 用于 Undertaker/Ravenkeeper。其余脚本/不可靠能力原有调用门控不变。FirstNight/其他夜晚通过相同准备器，仍由原投影提供对应时序身份/中毒。

审计未找到本段可直接删除的无消费者计算。Washerwoman/Librarian/Investigator 的旧预览和发布回退仍被使用，本步不以名称判死代码。三个函数的查询、顺序、去重、空结果和文字语义保持不变；占卜师重复的选中姓名/座位/次要文字计算改用既有共用值。

验收：既有登记合法性/结果去重/信息发布回归，加准备器的健康、失能和非法查询边界测试；完整 JVM、APK、差异审查、远端 CI/R2。没有新恢复能力、没有设备或付费模型验收。

## 实施与证据

Host 2,713 → 2,616 行，164,567 → 159,659 bytes。新准备器 121 行 / 5,574 bytes，生产净增加 24 行 / 666 bytes；这是收缩 Host 的职责范围，并非净删除成果。现有规则投影、结果展示与信息提交边界均未改写。

三个准备器回归覆盖健康/中毒/有效身份改变后的登记资格、候选顺序、无效/重复查询不求值、候选准备不改写选择；原结果合并及信息发布测试继续保留。五个生产调用已复查，无旧局部函数残留。

本地 `:app:testFull :app:testFast :app:assembleDebug --offline` 通过：完整 1,345 / 快速 1,338，失败、错误和跳过均 0。R2 shell 除 macOS 不支持的 grep -P 检查外通过，该项使用等价 Python 正则核对；远端 CI/R2 以 PR 实时状态为准。完整本地 diff 与新增文件已审查，`git diff --check` 通过。无真机验收；不把编译和 JVM 测试当作 Compose 生命周期证明。
