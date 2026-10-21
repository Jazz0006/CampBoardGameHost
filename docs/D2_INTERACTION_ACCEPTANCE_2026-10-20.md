> **2026-10-21 后续代码检查点**：`f4d78d5c70c55be988c1daf29558fde2e0c82305` 已完成 B/C 和主要 Day/终局 Session 迁移、删除旧控制链并补唯一活动局原子保存；本地 fast 612/full 639（含原 23 D2/行动 UI＋4 Day UI）、APK、R2 原始脚本通过。[完整 Android CI #4490](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956510) **SUCCESS**；[R2 #4066](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956507) **SUCCESS**。当前进展/剩余见 [路线](CURRENT_DEVELOPMENT_ROADMAP.md) 与 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。下方旧“B/C 尚未实现/无存储”的状态是历史；D2 仅 Next 写 Observation 合同不变，未声称整项重构、全部线索、文件大小或真机/杀进程验收完成。

> **后续净删/全夜控制检查点（2026-10-20）**：代码 `311191a30d6b0d83b5cf2d6de2a67905b526e596` 已将全夜步骤切换到 Session 事实，物理删除旧 UI 游标、Dynamic/Fixed 与 Planner/Projector 链，并执行无用途净删；当前说明见 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 和 [路线](CURRENT_DEVELOPMENT_ROADMAP.md)。本地 fast 593/full 616（含 23 UI）及 APK 通过；精确代码 [CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) SUCCESS / [R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) SUCCESS。下方 af35cee9/21 UI 及“随后才替换全夜路由”的文字是此前检查点；当前合同不变，剩余 P2 全轨迹、P3 B/C、持久恢复、大小/P5 尚未完成。用户先前真机反馈不等于本批新 APK 验收。

# D2 交互验收检查点（2026-10-20，本地 Codex）

本报告区分已运行的真实 Compose 自动化与尚未进行的设备/真实网络验收。本批代码 `af35cee951539df689552fb3c3ff97b7b2b5861e` 的 [完整 CI #4484](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558605) / [R2 #4060](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558519) 均 SUCCESS；Android full/APK、ASP、Clingo 和总门禁通过，remote compare 与本地 16 文件变更一致。起点 `d52cd2b6`，上一代码基线 `b5a955e9`。后续仅文档提交不冒充新 Android 编译证据。

## 本批修复

- 玩家全屏展示卡增加系统 Back 处理：与关闭按钮一样，只返回当前角色，不记录或推进。
- 全屏方桌和直接数字组件沿用当前 Host 的 Next 禁用条件；只有当前有效预览及其他必要输入具备时才可确认。使用现有 CompositionLocal 传只读 UI 条件，不新增 Session、业务游标或缓存。
- AI 请求只合并当前进行中的调用；失败/空结果的说明文本不再阻止显式重试。仍无自动付费重试、无用旧结果替代当前结果。
- Fortune Teller/Chambermaid/Ravenkeeper 的真实查询输入先于 Observation。当前已形成 typed scalar query 且输入版本推进时，原战略审查会识别这项玩家输入；无实际查询的计数变化和重绘仍不得启动模型调用。未提交观察不会伪造 ActionFact/Observation。
- 占卜师、侍女推荐额外核对信息指标，避免同接收人/座位却不同问题的 typed 结果混用。

## 已执行矩阵

测试主体为 `app/src/test/java/com/codex/campboardgamehost/ClocktowerD2UiTest.kt`，21 个真实 Compose UI 用例。它使用生产组件的点击、文本输入、硬件 Back、卸载/重建和响应到达后的重组；不以源码字符串判断行为。

| 场景 | 自动化证据范围 |
| --- | --- |
| 厨师 MANUAL / AI_AUTOMATIC、共情者 AI_ASSISTED | 实际 JudgeScreen→实际全屏→返回→再编辑/再看→Next→真实 GLOBAL_V1 Session；预览不入日志，最终值和私密收件人正确 |
| 洗衣妇/图书管理员/调查员 MANUAL；双人信息代表性 ASSISTED/AUTOMATIC | 实际 JudgeScreen 与 Session；改角色/座位失效、晚到建议不覆盖人工编辑、最终一条 typed pair，Next 前不可跳步 |
| 自动失败/重试/过期响应 | 实际 Host UI 结合离线响应 fixture，旧输入版本的结果不能展示或提交；当前重试成功后双击 Next 仍只有一条 Session Observation |
| 送葬者/守鸦人 MANUAL/ASSISTED/AUTOMATIC | 实际角色卡与实际全屏；返回后的身份选择、编辑失效、AI 到达不重置手选、自动模式显式展示/Next。后夜触发/调度不在这些组件测试的证明范围 |
| 占卜师 MANUAL/ASSISTED/AUTOMATIC | 实际角色卡；更换真人查询对象清空旧预览，旧问题的 AI 答案不复用，最终显示结果绑定新查询 |
| NGJ 侍女、贤者 MANUAL | 实际角色卡；数字/双座位预览返回后重选、失效与最终查询绑定 |
| 数字 Next 禁用 | 实际直接数字组件服从当前 Host 预览条件 |

NGJ 的生产 `storytellerModeForNewGame` 目前固定 MANUAL；本批保持此既有边界，不为凑矩阵新增 NGJ AI 产品。首夜邪恶互认/恶魔伪装、Spy 直接方桌交接和新恶魔通知仍沿现有专用合同，不冒称本表覆盖全部特殊信息通知。全夜 nextStep、恢复、一般确定性线索计算仍是后续任务。

新增一个战略检查行为回归：明确的当前玩家查询可以在尚无 Observation 时请求建议，但不会生成任何虚构事实；原“只有输入计数变化不重复付费请求”的测试继续保留。侍女指标不匹配的回归在原测试中补充。

## 运行结果

本地 `:app:testFast :app:testFull :app:assembleDebug` SUCCESS：fast 671 tests；full 163 suites / 692 tests，0 failures/errors/skipped；Debug APK 成功。UI 用例包含于 testFull，从 testFast 排除；两者都由现行 Gradle 明确路由。全量在最后改动后重跑通过。

本机无连接设备、无已有 AVD；未运行真实设备、付费 AI 或现场玩家对局。尝试从无设备测试运行时捕获像素截图未得到可用结果，临时截图代码已移除；未把它计为视觉通过。Robolectric UI 点击/重组回归不能替代现场视觉、输入法、系统旋转/进程中断和真实网络验收。没有购买 API 或建立新的测试服务。

测试依赖仅为 Android resources + Compose UI JUnit4 + Robolectric 4.16.1；调试测试 Activity 由 ui-test-manifest 提供，release 无此依赖。首次运行时下载使用 Maven Central 主地址，避免备用域名等待；见 [Robolectric 官方配置](https://robolectric.org/getting-started/) 和 [版本兼容表](https://robolectric.org/compatibility_table/)。

## 删除/体积/状态风险

未整删生产或独占测试文件。8 个已有生产文件净增 22 行 / 1,480B；3 个测试文件（含新 UI 测试文件）净增 451 行 / 28,572B；Gradle 增加 12 行测试设置。此批属于行为修复与验证，不能报告为净删。被替换路径为展示卡默认系统 Back、全屏角色脱离 Host 的 Next 按钮、AI 文本缓存阻断重试和忽略真实查询的审查分支；没有增加第二权威状态。

剩余超限：App 150,699B（本批 +438B）、HostScreen 94,508B、Session 54,310B；新 UI 测试文件 26,929B。UI index/dynamic handoff/旧自动点目标提交仍按后续收敛任务处理。两个 PR 保持 Draft，不 merge/retarget/rebase/force-push/删分支。没有新的所有者决策阻塞；最终设备验收证据仍缺。
