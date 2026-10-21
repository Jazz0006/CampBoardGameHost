# 当前交接 — 先最小完整游戏，再全仓清理（2026-10-21）

## 最新指令与立即任务

所有者已调整路线：**完整游戏流程的最小实现 → 全部代码遍历和大规模删除 → 界面与流程细节优化**。旧 P0-first、P4 局部细化/文件大小优先顺序不再执行。完整定义和退出标准见 [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md)。

接续先核对 HEAD/远端/工作区，再从真实创建入口追踪首夜、白天、后夜、终局及安全重载。列出短的“已可用 / 阻断 / 延后优化”清单，直接修最高影响的阻断并证明整局轨迹。不要再优先做局部高亮、按钮统一、普遍职责抽取或两个大文件缩减；没有阻断且最小闭环证据达标时即进入全仓清理。

保留唯一 Session、确定机械、A/B/C/D2、隐私与有序事实、幂等/原子确认及安全续局；不能用人工裁量替代强制唤醒/死亡/胜负。保留现有两剧本、实际 AI 及其模式支持范围、双跨局防重复。不新建通用引擎、持久 pending 或第二状态。必要替换仍同批删除旧消费者；全面删除和 <=50000B 的最终达标归第二阶段。当前界面允许粗糙，不把未确认草稿的精确恢复或穷举组合列作闭环前置条件。

## 代码基线与已完成证据

分支 `refactor/minimal-night-flow-20261019` / Draft #335，base 为 #334 `76be7e17`。路线调整前文档 HEAD `52cd7931`；最新生产代码 `9b3cee4c5ba8fa405080cd0929726e66ffd6c324`，Cloud full 646（146 suites / 33 UI）、APK、原 R2 shell、精确完整 Android CI #4498 / R2 #4074 全通过。此前投毒来源退出及夜间继任/展示职责收敛已验收。生产逻辑未随路线调整改变；docs-only CI 不替代新代码测试。

已有夜间 Session 路由、白天/终局、AtomicFile 活动局和真实 UI 回归，但不等于完整游戏、所有角色或真机验收已经完成。App 118438B、Night Host 63094B 超限，Session 48526B；前批细节保留在 [台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。

## 云端执行与边界

Cloud `/workspace/CampBoardGameHost` 直接读写，不需要 MiniMCP/Windows。每个 shell 先 `source /workspace/.cloud-setup/activate.sh`；完整验证 `./gradlew -I /workspace/work/cloud-tests.init.gradle :app:testFull :app:assembleDebug`。临时 init 使用工作区 Test JVM user.home 并透传已有代理/CA，不改产品构建或 HOME；Gradle 通信需要执行环境网络能力。前批日志 `/workspace/work/night-ownership/full.log`，原 R2 shell `/workspace/work/r2-current.sh`。

保留 Draft，不合并/改 base/重写历史/删分支/调用付费 API。真机/实际杀进程证据须单列，JVM 文件回归不能冒称设备验收。没有新增所有者决策阻塞。
