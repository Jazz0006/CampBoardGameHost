# CampBoardGameHost 文档入口

> 最后整理：2026-08-19  
> 当前开发分支：`codex/storyteller-algorithm-v4`  
> **任何新的开发或审计任务都应先读本文，再读 `CURRENT_DEVELOPMENT_ROADMAP.md`。**

## 1. 文档权威与使用规则

本目录只保留仍然会影响当前设计、验证或下一步实施的文档。已经完成、被后续方案取代、或验收结论已经失效的文档统一放入 `archive/`。

当文档之间出现冲突时，按以下方式处理：

1. **游戏规则正确性**：官方 Blood on the Clocktower 规则 / Almanac / published rulings 优先；项目 golden expectation 次之；外部 Oracle 只用于交叉验证。
2. **当前开发状态和“下一步做什么”**：`CURRENT_DEVELOPMENT_ROADMAP.md` 是唯一状态权威。
3. **Possible Worlds / 玩家认知一致性总体架构**：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` 是当前主规范。
4. **阶段专项实现**：对应专项 spec 只在主规范边界内生效。
5. **实现状态、测试报告和历史验收**：只能作为证据，不得覆盖当前路线或规则语义。
6. **`archive/`**：只用于历史追溯，不再作为新代码的实施依据。

特别说明：部分较早的活跃文档正文中仍保留“COMPLETE / READY / PASS”等当时状态。**如果与本目录 README 或 `CURRENT_DEVELOPMENT_ROADMAP.md` 冲突，以当前路线为准。**

## 2. 当前必须阅读的文档

### A. 当前开发路线

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)  
  2026-08-19 全量审计后的唯一实施顺序、阶段状态、阻塞项和退出条件。

### B. 主架构规范

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)  
  Possible Worlds、玩家知识边界、registration、Oracle 权威、A/B/C 阶段总体架构。正文中的旧阶段完成状态不再作为当前状态来源。

### C. 当前 Phase A 专项

- [`storyteller_a4_5_observation_cache_rebuild_spec.md`](storyteller_a4_5_observation_cache_rebuild_spec.md)  
  A4.5 cache rebuild 的原始合同。2026-08-19 审计后 A4.5 已重新打开，必须补齐 durability、lifecycle cancellation/invalidation 和 cache invariant。

- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md)  
  A4 ZDD 原型的实现与设备实验日志。它是工程证据日志，不是当前状态权威；生产仍不得因该文档中的旧完成记录直接切到 ZDD。

### D. Phase A 参考与验证

- [`unified_semantic_model.md`](unified_semantic_model.md) — A1/A1.1 统一语义模型。
- [`external_solver_evaluation.md`](external_solver_evaluation.md) — 外部 solver/research 冻结与使用边界。
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md) — Trouble Brewing golden scenario 矩阵。
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md) — A2/A2.1 ASP Oracle 交叉验证基线。

## 3. 下一阶段文档：当前禁止提前实施

- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md)

该计划仍是 Phase A 之后动态决策引擎的主要实施方案，但其正文顶部的 `READY FOR IMPLEMENTATION` 已被 2026-08-19 审计状态覆盖。

**当前状态：BLOCKED BY PHASE A REMEDIATION。**

只有 `CURRENT_DEVELOPMENT_ROADMAP.md` 中定义的 Phase A 修复和重新退出审查全部通过，才能开始该计划的下一批实施。

## 4. 已归档文档

历史文档统一见 [`archive/README.md`](archive/README.md)。主要包含：

- V3/V4 旧算法设计与基线/最终验收；
- 已被新动态决策计划取代的旧实施说明；
- 已失效的 A3 `PASS` 与旧 A3 baseline 记录；
- 较早的 UI 设计与离线审计材料。

归档不是删除。需要追溯设计演进、比较旧算法行为或理解历史决策时仍可查阅，但不得直接作为新开发任务的入口。

## 5. 子目录局部文档

某些工具拥有自己的 README，例如：

- `tools/asp_oracle/README.md`

这类文档描述局部工具的运行方式和依赖，保留在工具目录中，不迁移到 `docs/` 顶层，也不参与总体开发路线排序。

## 6. 文档维护规则

以后每完成一个较大阶段，遵循以下规则：

- **只有一份当前路线**：只更新 `CURRENT_DEVELOPMENT_ROADMAP.md` 的阶段状态。
- 主规范只在架构/语义发生变化时升级版本，不用它记录每日进度。
- 专项 spec 完成后可保留到该阶段彻底退出；被后续方案取代时移入 `archive/`。
- 验收报告一旦被后续审计撤销，立即归档，并在当前路线记录撤销原因。
- 不再创建名称含“最终版”但以后仍可能被新路线覆盖的并列主规范。
- 新文档必须在本 README 中登记其角色：`CURRENT`、`NORMATIVE`、`REFERENCE`、`FUTURE/BLOCKED` 或 `ARCHIVE`。
