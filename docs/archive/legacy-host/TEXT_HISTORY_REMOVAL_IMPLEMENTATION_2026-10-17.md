# 第二阶段：删除独立文字记录与重复归档

基线 `ef076287`（#316），分支 `codex/remove-duplicate-game-history`。

验收：新局、日夜推进、提名投票、死亡/身份变化、实际信息发布与快速再开局继续可用；不再维护 ClocktowerEvent、文字计数器或重复整局归档。不迁移或擦除设备旧归档。允许暂缺复盘、跨局历史与额外夜间摘要，不新增替代存储。

Architecture pre-flight:
- current owner: App 同时写 Session 事实、ClocktowerEvent、淘汰文字及归档；Host 在记录夜间步骤中确认管家，并在玩家揭示时生成文字副本。
- proposed responsibility: 删除文字副本及归档；正式输入序号读取实时 Session 全局游标，管家确认与同夜去重归 Session。
- authoritative state owner(s): Session 动作/观察与共用全局游标；UI 保留未确认选择。Undercover 的淘汰记录仍是其正常界面的现有数据，不在本阶段另造统一引擎。
- narrow typed input/output seam: Session 下一输入序号和管家确认；玩家揭示保持 authorize -> observation commit -> displayed marker -> reveal。
- keep in current owner / extract: 删除 App/Host 的日志职责，不搬到工具类。管家确认使用已有 Session 提交边界，不建立日志适配器。
- reason: 正式动作身份、规则确认和去重不能依赖已取消的文字展示。

扇出：App 所有事件写入与计数引用；Host 夜间确认、提名/投票叙述、信息发布；Dawn 摘要、Host tools、结果页复盘入口；Archive store/codec、AppGameStateJsonCodec 与专属测试。保留开局多样性所需的轮换历史，它不保存整局游戏副本。

序号风险：必须每次读取实时 Session，不能捕获重组前的游标；同回调连续提交不能冲突。Virgin 预检位于提名提交之前，预检序号需包含即将提交的提名，不能再按“间谍文字是否写入”计算。Dawn 已有稳定观察 ID，去掉文字事件偏移。

验证：既有日夜、信息发布、投票、继任与 Session 测试；为真实 Session 序号与管家确认去重补最小 typed 回归；移除叙述/JSON 专属测试并保留混合测试的正式事实断言。执行完整 JVM、assemble、最终引用检查及 diff 审查；新 head 必须通过远端 CI/R2。没有真机或真实模型则不声明其验收。

## 实施结果与缺口

- 删除全部 ClocktowerEvent 写入/读取、独立文字计数器、整局归档 store/codec、无消费者的 JSON primitives、对应测试/资源和 UI 入口。结果角色展示保留，文件改名为 ClocktowerResultsScreen；R2 保留结果页归属检查，只退休已删除文字标签函数的断言。
- 管家同夜重复确认从 Session 已提交事实判断；同夜重访保留首次选择，新夜允许再选。动作序号实时读取共用游标，单次回调多个提交不依赖 Compose 重组或文字记录。Virgin 预检为先行提名预留一个位置，使用 checked addition。
- 夜间无死亡时直接采用已验证 planner 的无死亡结果清除待公布目标，不再通过生成“士兵/僧侣保护”等文字反推清理。未重写规则 planner。
- 保留投票顺序/幽灵票、正式信息先提交后展示、失败不消费展示标记、已确认裁定等既有回归；仅删文字格式、重复归档和专用 JSON 协议测试。新增两个 Session 回归覆盖连续确认序号及管家跨夜/重复确认。
- 本地完整 JVM 1,383 tests、FAST 1,376 tests，均 0 failures/errors/skipped；assembleDebug 成功。最后无用字段/导入由编译复核。macOS 本地 R2 脚本因 BSD grep 不支持 -P 无法完整执行，远端 Linux R2 才是该门禁证据。
- 当前没有退出恢复、文字复盘、完整跨局历史或额外夜间文字摘要；现有事件覆盖不等于完整。已存在结构化事实与玩家观察未删除。Artist 的旧纯文字问答不再留副本，当前两个受支持脚本都不含 Artist；未来覆盖扩展须在正式记录边界补齐。
- 不宣称真机或真实模型通过；不宣称第三阶段状态收敛或大文件治理完成。
