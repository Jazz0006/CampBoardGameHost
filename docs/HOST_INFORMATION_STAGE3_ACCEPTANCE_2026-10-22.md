# Stage 3 验收：Chef / Empath 数字信息统一方桌（2026-10-22）

> **2026-10-23 合并后更新：** 第三批 [PR #348](https://github.com/Jazz0006/CampBoardGameHost/pull/348) 已 squash 合并 `main` `e241d03b9a7e344f422937cdb6c8419ad091981b`；Stage 4 [#349](https://github.com/Jazz0006/CampBoardGameHost/pull/349) 和 Stage 5 [#350](https://github.com/Jazz0006/CampBoardGameHost/pull/350) 也已合并。第五批主线合并点 `b551d7f2efb074e45f2722c7bcd712d5f8bd56e8`。此文保留 Stage 3 具体代码/回归证据，以下分支及 Draft 状态是**当时的验收快照**，不是当前待办。真实设备仍未验收。

## 交付与精确验收（合并前验收快照）

- 仓库：`Jazz0006/CampBoardGameHost`；分支：`feature/host-numeric-chef-empath-stage3-20261022`；[Draft PR #348](https://github.com/Jazz0006/CampBoardGameHost/pull/348)。从干净 main `d1327648a8520a498b3cfc6adfb33f1be3807970` 开工。
- **最后一次修改生产代码的验收 SHA：`44fb05c769735826187a66008749432e25368ea4`**。
- [完整 Android CI #4562](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39756358897) SUCCESS；[R2 #4124](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39756358937) SUCCESS。PR 4/4 检查成功、无未解决审查，仍为 Draft、未合并。
- 开发机工作树与远端分支在上述验收 SHA 同步、Clean；本验收说明的后续文档提交不修改已验收的生产代码。

## 完成的行为边界

1. 生产 `clocktower/ui/ClocktowerHostScreen.kt` 从当前唯一 `ClocktowerGameSession/ClocktowerSessionState` 调用只读 `NumericInformationResolver`；不使用旧 Host displayProposition 或 AI 叙述伪装为数字真值。
2. Chef、Empath 经 `ClocktowerNightFullScreenSurface.Chef/Empath` 的单一分发路径进入 `ClocktowerNumericInformationSquareTableDialog`，复用 `ClocktowerHostSquareTableScaffold`、共享红蓝卡片与醉毒/死亡/鬼票座位状态，以及统一底部 Host / Next。已删除这两个角色旧独立数字页面与重复导航。
3. Resolver 唯一结果只展示一个「展示给玩家：N」按钮；Spy/Recluse 登记或信息源醉毒形成多个展示数字时才提供单选；在 360×520 竖屏可滚动长选项，无任意非负整数输入。
4. MANUAL 保留人工选择，AI_ASSISTED 标记匹配当前 metric / 来源座位 / subjects / value 的有效建议，AI_AUTOMATIC 无有效建议时不得自动选一个结果。确定性单值不依赖 AI 请求。
5. 点击展示进入既有私密全屏预览；关闭返回同一步并保留预览值；改选使原预览失效，必须再次展示。仅本步显式 Next 写唯一最终 `Observation`，不记录选择来源、不允许跨角色上一步。

## 测试与曾发现的回归

- 保留 `ClocktowerD2UiTest` 中真实 Session/Compose 的人工、辅助、自动数字信息流程和「改选 → 重新展示 → 单次 Observation」回归。
- `ClocktowerNumericCompactUiTest` 验证单值无冗余输入、360×520 共享方桌底部导航禁用门、0–15 多值滚动与自动模式的有效建议门。
- `ClocktowerCompleteAppUiTest` 覆盖 TB/NGJ 实际 App 开局、夜间、白天、结束、恢复及快速重开；TB 数字步骤使用统一方桌 `Next →`，未迁移的 NGJ Clockmaker 保持原 `Next`。
- `99ad85fb` 的 CI #4561 有一次完整对局测试失败：测试仍点击旧数字页面 `Next`；已在 `44fb05c` 修订回归并用完整 CI #4562 / R2 #4124 验证。

## 明确未交付

- Stage 4 Washerwoman / Librarian / Investigator：先选两个物理座位、Session 局部求角色候选、单列来源分区与三模式 UI。
- Stage 5 Clockmaker / Chambermaid：精确数字语义及旧输入迁移。此阶段保留其现有路径，不能误报为统一数字 UI 已全部覆盖。
- 真机 Android 视觉、物理杀进程恢复、付费真实 AI API 未执行；CI 通过不等于以上验收。
- 独立遗留 P1：白天可提名已死玩家，以及健康 Fortune Teller 固定布尔真值消费审计，不属于 Stage 3。

**下一步：** 按既定 PR 合并流程先处理 Draft #348，然后从合并后的最新 main 进入 Stage 4；不得从旧 Stage 2 开发分支重新叠加或重建已完成的 Chef/Empath 逻辑。
