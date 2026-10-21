# 当前交接 — 最小完整流程验收，下一阶段全仓清理（2026-10-21）

## 接续结论与立即任务

最小实现的真实整局流程已跑通：两剧本均从首页、安排玩家/座位、选剧本和生产发牌，到首夜、真实提名手势/投票/日结、后夜死亡、私有文件恢复、正式终局/揭晓，以及恢复后原班再开一局。新增发牌中断回归。最后 Cloud full 654（147 suites / 38 UI，零失败/跳过）、APK 与原 R2 shell 通过；精确远端 CI/R2 待推送核对；通过后第一阶段关闭。

后续主任务是**全仓未截断源码遍历和大规模删除**，不再返回“真实开局还没检查”或局部 UI 精修。保留闭环作为删除的行为基线；每批按完整责任删除 producer/consumer/独占测试，报告净量和必要验证。两个大文件与全源码 <=50000B 在此阶段收敛。清理完成后才优化界面/流程细节。

完整证据与限制见 [验收报告](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md) 和 [路线](CURRENT_DEVELOPMENT_ROADMAP.md)。代表性 App 轨迹不等于全部阵容穷举或真机/实际杀进程/付费模型验收。

## 本批修复与唯一对象

- 删除首次保存的 screen 门槛、effect screen key 和 activeGameSaveAttached；Session 创建后即绑定原 AtomicFile。
- 首夜尚无任何确认动作/信息时，恢复同一 Session，从第一位安全重新发牌；无额外持久游标/恢复引擎。
- quick restart 先从当前 Session 读取规范名单，再确认下一局座位并新建下一局；修复恢复后 `Production start requires confirmed seating`。
- 只把原 UUID seed 函数移动成内部 App 可传参数，默认随机行为不变；测试固定真实生产准备器的 seed 34 / 26，不替换 GameState、路由、Session、回调或存储。

新增 `ClocktowerCompleteAppUiTest` 3 项，修复前 3 失败，修复后全通过；其他 651 项历史回归与最终全量验收见报告。生产净 +3 行/+316B（App 116000B），测试新增 160 行，无生产/专属测试文件删除，Night Host 63787B、Session 47578B。

## 环境与授权边界

分支 `refactor/minimal-night-flow-20261019` / Draft #335，base #334 `76be7e17` 不变；本批基线 `1e7fbc6d`（前一生产 `3268440f`）。接续先核对最新真实 HEAD/远端/工作区与精确代码 CI，docs-only 不能冒充新代码验收。

Cloud `/workspace/CampBoardGameHost` 直接读写，不需要 MiniMCP/Windows。每个 shell 先 `source /workspace/.cloud-setup/activate.sh`；完整 `./gradlew -I /workspace/work/cloud-tests.init.gradle :app:testFull :app:assembleDebug`，原 R2 shell `/workspace/work/r2-current.sh`。本批日志 `/workspace/work/app-complete/{red-final,focused,full}.log`；临时 seed 探针已删除，不计正式测试。

保留唯一 Session、A/B/C/D2、现有 AI 与双跨局轮换、顺序/隐私/幂等/原子性。继续 Draft，不合并/改 base/重写历史/删分支/付费 API。无新增所有者决策阻塞。
