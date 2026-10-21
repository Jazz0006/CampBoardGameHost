> **独立审计跟进**：已修正继承的配置加载覆盖输入竞态，以及本批 live getter 导致 stale AI 请求退出后 busy 不清除的回归；新增 3 项设置 UI 测试；读取门槛版本 `23590475` 的完整回归 629（30 UI）/APK、CI #4493/R2 #4069 均通过，finally 修正 `bc263581` 也已通过完整 CI #4494/R2 #4070 与云端 629 回归/APK。另通过真实 Session 反例确认基线既有“投毒者死亡未解除目标中毒”缺陷，需作为下一独立 Session 批次优先修复。不得据此前绿灯宣称无 BUG。见 [审计报告](MINIMAL_HOST_P4_REVIEW_2026-10-21.md)。

# 当前交接 — Cloud P4 配置职责收敛（2026-10-21）

## 已提交生产基线

`refactor/minimal-night-flow-20261019` / Draft #335，最新代码 **`bc2635817060184e9ab6bb396e991d1a1d392b62`**，最后父提交 `23590475`，本轮基线 `ce1c314c`。GitHub 已独立核对 head、各提交 ancestry、文件范围及语义 diff；base 仍为 #334 `76be7e17`。接续前重新核对远端/本地 HEAD 与干净状态，保留无关文件。

本批将 AI 连接设置的完整加载/延迟安全保存/表单职责从 App 迁出，替换原 16 参数面板接口；根部生命周期、本局模式独立、Keystore 与 AI 实际请求/stale 边界保持。已删除无生产调用的 DemonNightAttackSemantics/PoisonEffectLifecycle 两套实现与 13 项专属测试，以及 Session 无调用私有 helper。当前 Session 的真实确定性规则没有删除。具体消费者、净量与保留测试见 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。

## 云端工具链及证据

直接使用 `/workspace/CampBoardGameHost`，无需 MiniMCP 或 Windows 开发机。每次 shell 先：

```bash
source /workspace/.cloud-setup/activate.sh
```

SDK、Gradle 9.5.0 缓存和 JDK25 daemon 已准备。当前 sandbox 的 home 只读，Robolectric 会尝试在 user.home 写下载锁；本批使用 `/workspace/work/cloud-tests.init.gradle` 给 Test JVM 设置 `user.home=/workspace/work/java-user` 并传递现有代理/CA 后运行：

```bash
./gradlew -I /workspace/work/cloud-tests.init.gradle :app:testFull :app:assembleDebug
```

该 init 为当前环境中间文件，不是产品构建或可依赖的跨会话工件；若重建环境，按相同最小配置重建。Gradle 本地进程通信需要带网络能力的执行权限；不要改 HOME 或读写用户 home。正常云端命令直接使用已有 SDK/Gradle 缓存，不因默认 shell 未激活就误报未安装。

最终文件快照实跑 **629 tests / 145 suites / 30 UI，0 failure/error/skipped**，Debug APK、原 R2 shell 和 diff 检查成功。精确代码 [CI #4494](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39550998373) **SUCCESS**、[R2 #4070](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39550998375) **SUCCESS**。未运行真机、杀进程或付费 API。历史 Windows 测试及旧 CI 不能替代本批证据。

## 立即接续

1. 优先修复审计 F2：投毒者死亡/身份退出应让持续毒失效，覆盖真实 Session 确认与恢复；再从最新源码核验 Night Host 的角色 actor/能力展示、确认与 AI 绑定职责，以及所有保存/恢复入口的实际消费者；同批切换并删除完整旧链，保留 narrow forced rules 和实际发布信息。
2. App **118544B**、Night HostScreen **68496B** 仍超限；Session **49379B** 达标。先净删，再内聚整理，不用泛用 Context/Manager、机械拆文件或第二游戏状态凑 <=50000B。
3. 完成逐角色合同、更多唯一线索输出、P0/P5 全树语义归属与零遗留证据。TB/NGJ 已有支持人数的显式角色两夜 fixture；仍需代表性组合和新 APK 现场/进程中断验收。
4. 保持 D2 仅 Next 正式记录最终一条 Observation；保留 Session 夜间进度、同页日结、Death 派生 Klutz、原子保存回滚、生产 AI 全链与双跨局防重复。

没有新的所有者决策阻塞。#334/#335 保持 Draft；不合并/改 base/重写历史/删分支，不调用付费 API。
