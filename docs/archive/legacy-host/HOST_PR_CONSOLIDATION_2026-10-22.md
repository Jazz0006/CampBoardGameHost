# 2026-10-22 GitHub PR 收敛与第三批开工基线

> 本文件是本轮实际 GitHub PR 审计／合并／替代证据，不是对未运行的真机或付费模型测试的承诺。最新未合并 PR 搜索结果：**0 个开放 PR**。本轮未删除历史分支，也没有强推、rebase 或覆盖旧工作。

## PR 处置结果

| PR | 结论 | 已核实证据 |
| --- | --- | --- |
| [#343](https://github.com/Jazz0006/CampBoardGameHost/pull/343) Stage 1 共享卡片视觉 | **已 squash 合并 main** | 源 `1ea2c918`，合并 `78d2b1156e0eea9d0f43fea93698f378a822db40`；Android CI #4543 / R2 #4110 SUCCESS |
| [#344](https://github.com/Jazz0006/CampBoardGameHost/pull/344) Stage 2 只读信息求值 | **已 squash 合并 main** | 源生产代码 `bb024f42`、文档头 `0ab2305a`，合并 `d3a75c9961103ea0e5f11d53ef1a7a4025c92e0a`；Android CI #4544 / R2 #4111 SUCCESS；Docs CI #4545 / R2 #4112 SUCCESS |
| [#342](https://github.com/Jazz0006/CampBoardGameHost/pull/342) 原设计文档 | **关闭，已由 #345 替代** | 原完整规划文件已完整迁移到 main；原 Draft 五处旧进度文档不覆盖 #344 的更新内容 |
| [#345](https://github.com/Jazz0006/CampBoardGameHost/pull/345) 合同集成与当前文档 | **已 squash 合并 main** | `0ee82171739300d170203e59c0446c38e408c0c5`；文档 CI #4549 / R2 #4114 SUCCESS，Android job 按 docs-only 规则跳过 |
| [#341](https://github.com/Jazz0006/CampBoardGameHost/pull/341) 酒鬼镇民方桌 | **关闭，已由 #346 适配替代** | 原功能完整转到 Stage 1 后的共享 `isCandidate/isSelected` 模型，删除独立列表，不把旧 SeatState/虚线旗标搬回；原分支历史保留 |
| [#346](https://github.com/Jazz0006/CampBoardGameHost/pull/346) 酒鬼方桌新架构兼容集成 | **已 squash 合并 main** | 源 `421e8ad3`，合并 `b66f8fe34c7b92268321ccbf9491df83154d21dc`；完整 Android CI #4551 / R2 #4115 SUCCESS |
| [#336](https://github.com/Jazz0006/CampBoardGameHost/pull/336) 旧 Host UI 拆分 | **关闭、未合并** | 旧 head `d58b2816` 与主线冲突，早于已合并全仓清理 #337；其额外模块拆分并未直接移植以免重引旧调用。如以后证明某项仍需要，应另行按当前架构评估小批实现 |

## 新对话实际执行入口

- **当前代码主线**：`main` 至少 `b66f8fe34c7b92268321ccbf9491df83154d21dc`（本记录的后续文档提交会推进 main，请实时核对）。
- Oracle Mini MCP 开发机仓库别名 `clocktower`。本次代码合并后已经切换并将开发机 `main` 快进到 `b66f8fe3`，确认 Clean 且与远端同步（记录完成后还需对最新文档 HEAD 再同步）。
- 下一步：**Stage 3 Chef/Empath Numeric UI 实际代码实施**。从最新 main 派生新的开发分支；无需再依赖或解决之前的 Stage 1/Stage 2 堆叠 PR。先读 `AGENTS.md`、`docs/HOST_TABLE_AND_INFORMATION_REBUILD_2026-10-22.md`、Roadmap、Handoff、Testing Strategy 和 Stage 2 acceptance。
- Stage 2 只读 Resolver 已在生产源树，但厨师/共情者数字界面仍使用旧任意非负输入，必须完成正式 Session 求值器接入、唯一结果按钮／有裁量时数字单选、AI 当前建议高亮、D2 预览重选与 Next 唯一 Observation、旧路径删除和 CI/R2。Stage 4 Pair UI、Stage 5 Clockmaker/Chambermaid 与真机验收排在后面。
- **仍未修复的独立 P1**：活人应可提名已死亡玩家、Fortune Teller 固定布尔结果；不得把这两个问题默认为 PR 收敛已修复。
- **未验收**：真实 Android 设备 360×520 上视觉实测、实际强退/恢复、付费 API 调用；支付类调用须单独授权。
