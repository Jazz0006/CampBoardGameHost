# Stage 4 验收记录：Pair 信息统一方桌（2026-10-22）

> **2026-10-23 合并后状态：** 本批 [PR #349](https://github.com/Jazz0006/CampBoardGameHost/pull/349) 最终代码 `18a64bc8da052b41b28067918cca7a719bbd5cfb` 完整 Android CI **#4569** / R2 **#4130 SUCCESS**，已 squash 合并 `main` `97a4179d8461a96e1d38f0fc169b1f5386a4c730`。随后第五批 [#350](https://github.com/Jazz0006/CampBoardGameHost/pull/350) 也已合并，第五批主线合并点 `b551d7f2efb074e45f2722c7bcd712d5f8bd56e8`。下文 Draft/未合并描述是当时快照，非当前状态。真机尚未验收。

## 合并与基线（合并前开发记录）

- Stage 3 [PR #348](https://github.com/Jazz0006/CampBoardGameHost/pull/348) 已经 squash 合并 main `e241d03b9a7e344f422937cdb6c8419ad091981b`。
- 从上述干净 main 新建 `feature/host-pair-information-stage4-20261022`；本批 [Draft PR #349](https://github.com/Jazz0006/CampBoardGameHost/pull/349)，未合并。
- 第一次生产代码提交 `ff96496a783e5e7ab9635081deeb4e6a9e073afb` 完整 Android [CI #4565](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39758856042) / R2 #4126 **SUCCESS**。后续新增 360×520 真实 Compose 测试及文档时，**必须以更新后最终 SHA 重新验收 CI/R2**。

## 实现

1. `ClocktowerJudgeScreen` 唯一读取 `ClocktowerGameSession/ClocktowerSessionState`，向当前步骤传递只读 `PairInformationResolver.resolve(sessionState, nightRuleset, role, sourceSeat, firstSeat, secondSeat)`；无第二游戏对象、无预生成全部角色×座位组合。
2. 统一 `ClocktowerPairInformationSquareTableDialog`：用户先点击两名不同物理座位；同一方桌再按当前 pair 求出可展示角色。正常 singleton 直接显示带完整信息的展示按钮；多个选项才用单列 Dropdown，按「在场角色」「恶魔伪装」来源分区、不重复来源列。可变更座位和角色，变更 pair 后旧角色不在当前集合则清空。
3. 卡片继续使用唯一共享 `ClocktowerHostSquareTableScaffold`，角色/生死/毒酒/鬼票为 Session 只读投影；普通候选使用已有长虚线，Spy/Recluse 特殊候选用已有短虚线，其他座位仍可点击，候选只是提示。
4. 健康 Librarian 无真实外来者时可展示「没有外来者」；醉毒信息源也可以显示短名单内允许的零结果。Investigator 不创造「没有爪牙」；Washerwoman 的恶魔伪装角色必须来自 Session 已确认列表，空短名单等语义沿用第二批 Resolver。
5. MANUAL 直接手动选择；AI_ASSISTED 当前合法建议+人工编辑；AI_AUTOMATIC 仅当前可信推荐，缺失则等待/重试。推荐必须对应当前 pair、角色与 Session 局部短名单；Host 建议接纳与 AI 重新请求入口共享同一 Session 校验；迟到建议不覆盖手动草稿。
6. 点击展示进入既有玩家私密全屏，不写事件；返回可修改，修改失效上次预览；只有 Next 提交最终一次 `Observation`。角色间无 Previous。未更动 Stage 5 钟表匠/侍女及付费 AI 调用路径。

## 实测范围 / 历史验收记录

- `ClocktowerD2UiTest` 覆盖 Washerwoman/Librarian/Investigator 手动选 pair 后的单选/多选、三模式的当前建议、改选后的 Next 门及唯一 Observation。
- `ClocktowerDirectPairInformationTest` 验证先座位后角色，错误/过期当前 query AI 建议不可复用。
- `ClocktowerPairCompactUiTest`：360×520 / 15 玩家共享方桌，先点两物理座位后才出现展示信息按钮、Next 在预览前不可提交。回归发现受限页面原严格座位布局可能抛出异常；复用已有密度候选、`hostTableLayoutOrNull` 和 `hostTableLayoutBestEffort` 兜底，不重建另一个方桌排列算法。
- 第一版 `ff96496a` CI #4565 / R2 #4126 SUCCESS；增加小屏与当前 AI 建议验收、复用受限布局回退的 `4f418656` 完整 CI #4568 / R2 #4129 SUCCESS。去除旧重复预览提示的最终清理提交仍须按新 SHA 验收。
- 不等价于真实设备渲染、人眼可读性、Android 物理强退测试或付费 API 实测。

**下一步**：记录最后 CI/R2，审核 Draft #349 后依约合并；第五批实施 Clockmaker/Chambermaid 及全局设备验收；独立 P1 白天可提名死者和 Fortune Teller 确定性结果仍未在本批改动。
