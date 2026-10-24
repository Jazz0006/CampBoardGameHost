# 第四阶段 D4c：提名交互与投票草稿

基线 #324 `fda503ef`。

Architecture pre-flight:
- current owner: Host 保存提名双方草稿，准备圣女提示、登记开关、提名确认与投票返回路径。
- proposed responsibility: NominationInteraction 拥有提名展示与手动登记交互；两字段 NominationDraft 跨提名/投票共享并保持原 remember(gameId, round) 生存期。Host 继续按顺序转发正式确认到 App。
- authoritative state owner(s): Session 拥有提名、投票、圣女使用与幽灵票事实；既有规则域/共享登记投影决定登记可用性；草稿与手动选择仍为 UI 状态。
- narrow typed input/output seam: 卡片、共享桌面投影、登记投影/选择、草稿、轮数/中毒/圣女使用/可操作性 -> 一次 NominationConfirmation 请求；导航单独回调。
- keep in current owner / extract: 不传整个 Host/Session/checkpoint；登记查询不复制。输入较多但都属于提名展示，使用具名参数，不制造大 Context。正式多步提交留在既有 Host→App 边界，本次不改变其原子性。
- reason: 一次提名交互修改无需进入夜间 materializer；一份提名草稿支持取消投票后返回，不能条件记忆导致重置。

扇出：Overview 合法提名手势写草稿；Nomination 读取并取消清空；Vote 读取，提交成功后清空，取消保持；唯一生产调用位于 Host。确认顺序保持 preflight → nomination → Virgin，失败不导航。无真机生命周期验收，不宣称 D4 或整个白天分流已完成。

## 实施结果与边界

提名文案、圣女呈现与登记开关移入 ClocktowerNominationInteraction（135 行 / 6,127 bytes），通过一个具名确认请求交回 Host；Host 的预检、提名事实、圣女事实顺序保持不变。显式登记选择在请求构造时读取，不成为事实。缺少提名双方时不发确认请求（既有界面禁用该入口）。

Host 减少 62 行，2,834 → 2,772 行，169,852 → 166,290 bytes。生产总行数增加 73 行；是职责拆分而非净删除。没有新增持久状态或扩大 Session。投票 UI/幽灵票算法仍在现有所有者；投票取消仍保留双方，成功确认后才清空并导航。

两个 NominationDraft typed 回归验证成功时原样转发投票与双方、失败保留草稿/缺失双方拒绝；既有 Virgin、投票、幽灵票、登记规则回归保留。没有真机导航验证，不能据 JVM 宣称取消/来回切换已完成设备验收。D4 剩余 Slayer 交互与白天/黎明早分流仍待实施。

验证：`:app:testFull :app:testFast :app:assembleDebug --offline` 通过，完整 1,339 / 快速 1,332，失败、错误、跳过均为 0；`git diff --check` 通过。远端 CI/R2 以当前 PR head 为准，保持 Draft，合并须另行确认。

R2 后续修复：远端旧断言要求 SpyTownsfolk 控件直接位于 Host，提取后失败。改为检查 Host→NominationInteraction→控件的实际归属，并在两处防止不可达的 Klutz/Spy 控件回归。未删除能力覆盖。完整 R2 shell 本地其余断言通过；macOS grep 不支持的既有多行 -P 检查用等价 Python 正则确认，Ubuntu 远端仍执行原检查。无需重复 JVM 测试（仅 CI 守卫/文档变化）。
