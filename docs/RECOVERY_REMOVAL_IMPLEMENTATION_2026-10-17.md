# 独立 Recovery 删除实施

基线：`704f7b29`；分支：`codex/remove-independent-recovery`。

## 验收与范围

删除继续上次游戏入口、预览、active-game 快照读写、恢复应用及其专属编解码与测试。Clocktower 与 Undercover 正常新局、发牌、进行和胜负流程保留。允许应用退出后无法继续，不创建替代快照，不读取、迁移或擦除磁盘上旧存档。

文字记录、已结束归档及状态收敛属于后续检查点。名称带 Recovery 的正常昼夜中毒事实读取仍保留。只读投影与已确认裁定不能按名称删除。

Architecture pre-flight:
- current owner: App 管理独立恢复的持久化、应用和生命周期；persistence 管理快照协议；Landing/Seating 显示恢复入口。
- proposed responsibility: 整条移除独立恢复职责及失去生产消费者的专属支持代码。
- authoritative state owner(s): 现有 Session 与尚未收敛的 App 正常流程状态；本阶段不新增所有者。
- narrow typed input/output seam: 保留 Session 的动作/信息确认边界；Debug A4 缓存只读已提交的 Session 观察。
- keep in current owner / extract: 删除，不抽取恢复控制器。A4 仅保留一个待处理观察 ID，在 Compose 提交后合并触发缓存刷新，取消“写盘成功”门控。
- reason: 保持同一回调中的多项状态提交及缓存失效顺序，不让诊断缓存要求另一套存档。

## 扇出与风险

- App 快照、预览、恢复、显式写入与生命周期写入全部移除；两个界面入口同步移除。
- Recovery 专属 JSON 编解码随最后生产调用者删除。混合测试只删除往返序列化断言，保留规则、Session、裁定验证。
- A4 观察待处理 ID 跨 revision 保留、跨 session 清除；既有缓存取消/失效保护保留，禁止伪造持久化成功。
- 游戏规则与正在运行的夜间事务不是退出恢复；保留其已有证明。

## 验证计划

基线完整 CI/R2 已通过。本阶段先编译与相关既有测试，再执行完整 JVM 回归及 assemble；复查旧入口、存储键、恢复 DTO 的生产引用与完整 diff。移除的测试只保护已取消产品或其序列化协议。新 head 的远端 CI/R2 是合并条件，真机与真实模型未执行则明确报告。

## 第一阶段结果

- 独立恢复及其生产专属 JSON 编解码已删除；`restoreProduction` 已无调用并移除。已结束归档、文字记录留给第二阶段。
- 混合裁定测试改为直接验证 typed journal；死亡/身份/投票/公开信息等现有证明保留。旧 JSON 对伪造字段的校验测试随 decoder 退休；试图直接套到无校验的 ActionFact DTO 构造器不成立，未为保留该测试扩展生产模型，正常动作生产边界仍由既有测试保护。
- `ClocktowerGameSession.restore(GameSnapshot)` 目前只由测试使用，且与既有测试构造及历史上下文契约耦合，列入第三阶段逐项审计；它没有磁盘入口。journal 的 typed restore 仍用于运行中的原子裁定验证，不能随 Recovery codec 删除。
- 本地 `:app:testFull :app:assembleDebug --offline` 成功：1,397 tests，0 failures/errors/skipped。最终注释与格式调整后的 `:app:testFast --offline` 成功：1,390 tests，0 failures/errors/skipped。
- App 从 4,846 行 / 280,654 bytes 降至 4,510 行 / 264,939 bytes。Host 本阶段未改，大文件问题尚未解决。
- 当前不提供退出恢复；旧存档留在原设备但不再读写。未执行真机或真实模型验证；远端门禁另行实时核验，不声明已合并。
