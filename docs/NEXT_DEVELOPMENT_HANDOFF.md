## 2026-10-21 Codex Cloud：P4 AI 配置职责收敛与无调用规则退休

在 `ce1c314c` 基线上实施，沿用 Draft #335，不改 base、不合并。当前执行环境为 Codex Cloud；每个 shell 先 `source /workspace/.cloud-setup/activate.sh`。SDK/Gradle 已预装，不依赖 MiniMCP 或 Windows 开发机。

- App 的 AI 默认模式、direct/gateway 连接字段、异步加载、700ms 延迟安全保存及表单绑定统一由 `StorytellerAiSettings` 和原设置面板负责；App 根部保留该生命周期，离开设置页不取消保存。本局模式与持久默认仍分开，开局/后夜请求、旧响应校验、Session 及私密记录未迁入设置对象。原表单 16 参数接口与 App 内重复接线已替换，不留双轨。凭据仍使用原 Keystore 存储。
- 全部生产 Kotlin 符号库存筛查后，核对 `DemonNightAttackSemantics` 与 `PoisonEffectLifecycle` 的生产/测试/CI 消费者：两者均已无生产调用，旧攻击 Context/Outcome 与两套实现及专属测试共 4 文件删除。Session 仍承担真实攻击/死亡/保护/市长/继任、中毒生效与到期；ConfirmedNightAttack、ConfirmedNightOperation、ClocktowerPoisonSettlement、NightFlow 等真实行为测试保留。额外删除 Session 无调用的私有死亡 roster helper。
- 计入新增设置文件，本批生产 Kotlin **净 -84 行 / -3381B**；专属旧测试 **-146 行（13 项）**。App **118275B**、Night HostScreen **68496B** 仍超限，Session **49379B**，设置新文件 **3507B**。这是一个完整的设置职责与孤立旧链清理批，不是全树零遗留结论。
- Codex Cloud 实跑 `:app:testFull :app:assembleDebug` 成功：**626 tests / 144 suites / 27 UI，0 failure/error/skipped**；工作流原始 R2 shell 与 `git diff --check` 通过。测试首次启动遇到只读 home 的 Robolectric 锁文件，使用工作区临时 Gradle init 将 Test JVM `user.home` 定向至 `/workspace/work/java-user`，并传递现有代理与 CA 配置后通过；未改产品构建来绕过测试。未运行新 APK 真机、杀进程或付费 API。
- 远端精确 SHA CI/R2 尚待本批提交推送后核对；此前 #4490/#4066 只证明旧代码。下一批继续夜间 Host 展示/机械职责、所有提交与恢复入口及角色合同审计，先净删再内聚整理两个超限文件，最后补 P0/P5 全树及设备证据。

# 当前交接 — P3/单一 Session 保存检查点（2026-10-21）

**代码 HEAD `f4d78d5c70c55be988c1daf29558fde2e0c82305` 已提交并推送 Draft #335。** 生产迁移提交 `fedde3e7`，显示轮次/跨阶段幂等修正 `f4d78d5c`。[完整 Android CI #4490](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956510) **SUCCESS**；[R2 #4066](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956507) **SUCCESS**。本地 `:app:testFast :app:testFull :app:assembleDebug` 和原始 R2 脚本通过：fast 612/full 639（145/146 suites，27 UI，0 failure/error/skipped）。前一 `25c0aca0` 在途文档里的 Artist 测试编译失败已修复，不能再当作当前阻塞。

## 当前事实与必须保留的边界

当前执行环境为 Codex Cloud：`/workspace/CampBoardGameHost`。代码和大文件直接读写，测试/构建在配置所需工具链后直接执行，Git 用于分支同步及授权提交/推送，GitHub 工具用于 PR 和精确 SHA 验收；不依赖 MiniMCP 或 Windows 开发机。历史 Windows JDK 路径及原未跟踪 package 文件描述不代表当前云环境。2026-10-21 实测文件直接读写与远端分支拉取/切换成功。云环境已预装 Android SDK 与 Gradle 9.5.0；每个执行 shell 先运行 `source /workspace/.cloud-setup/activate.sh`，复用已准备的 SDK、Gradle 缓存和工具路径。默认 shell 未加载该脚本不代表工具缺失。Gradle 使用本地进程通信，受限沙箱若报 wildcard IP 错误，需使用带网络能力的执行权限。此前 Windows/CI 结果不等于本次云端测试结果。

B 同页日结、C Death/Execution 派生 Klutz 真选择，以及主要白天/终局逻辑已切换至 Session DayFlow。旧 EndConfirm 页、Learned 事件/producer、App 可写 gameOutcome/pending/resume、DayMode Vote/EndConfirm/Klutz 控制退出。确认提名未投票由事实派生；重启不丢该步骤，不允许越过投票结束 Day。确认接口携带所显示轮次防双击；私密信息 D2 仍只有 Next 正式记录。

活动局仅有一份 Session JSON / AtomicFile，放在应用私有 noBackupFilesDir，未增加存档/恢复引擎或持久草稿。保存失败保持旧状态并提供重试；实际信息/角色/死亡/投毒/保护/全局事件顺序、AI 已接受建议记忆/会话、开局双防重复身份元数据经文件重读测试。新进程/设备杀进程仍需真机验收，不能把 Robolectric 新 Store/Session 当作设备已验证。

精确删除、全部旧→新消费者、测试范围及净量见 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。生产字节本批 **+1775B**、行 **-108**；测试 **+412 行/+27595B**。必要存储/行为证明计入新增，不能只算已删代码。Session **49577B** 达标；App **122572B**、Night HostScreen **68496B** 仍超限。

## 接续顺序

1. 本代码精确 CI #4490 已核实 Android full/APK、ASP、真实 Clingo、总门禁及 R2 #4066 均 SUCCESS。后续每批仍核对精确 SHA；不以 docs-only 或 `311191a3` 旧绿灯替代。
2. 继续最终收敛：从真实全树/两剧本角色合同核验余留 UI/AI 机械判断与职责、持久化所有入口/错误路径和稳定步骤身份。当前 P0 全树语义归属、P5 零遗留证明仍未完成。
3. App 和夜间 HostScreen 先净删，再按确有需要的内聚职责整理到 <=50000B；不要引入通用 Context/Manager 或新游戏状态。白天草稿现在仅在 DayHostScreen，可随界面丢弃，正式进度从 Session 读。
4. P2 两夜人数轨迹已加（显式角色 fixture，不冒充所有合法阵容/完整线索生成）；仍需完整角色/实际组合核对、必要真机操作与杀进程续局。更广泛唯一信息输出仍需单独证明。
5. 保持 Draft，未获 merge/retarget/rebase/force-push/删分支或付费 API 授权。AI 开局/后夜/transport/parser/memory/modes、TB/NGJ、双跨局防重复、实际展示事实/收件人和 stale 安全继续保护。

历史 `311191a3` / CI #4486/R2 #4062 的全夜切换及净删证据保留于台账；本次没有声明整项重构、P2/P3/P5 或真机验收全部结束。
