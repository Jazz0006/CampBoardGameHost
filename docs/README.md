# 文档入口（2026-10-18）

## 现行权威：只从这些文件读取下一步

1. [AGENTS](../AGENTS.md)：产品边界、开发与验收规则。
2. [Host / Rules / AI 三模块及 P1–P5 删除路线](HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md)：唯一执行设计权威。
3. [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) 与 [当前 NEXT](NEXT_DEVELOPMENT_HANDOFF.md)：阶段状态、远端准确起点、下一批实际代码工作。
4. [Host 事实合同](MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)。
5. [测试策略](TESTING_STRATEGY.md)：evidence-first、CI/R2 及废弃测试退休。

## 只作为库存和历史证据，不给出新的开发优先级

- [当前扫描源码索引](MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv)：原扫描 717 个代码类文件的路径、大小与**初步分类**；需以最新 Git 树更新，不能视为完成逐文件行为审计。
- [第一轮全仓扫描与删除台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)：曾用最小 Host 无 Rules 的定义，现在被现行三模块路线覆盖。保留其历史尺寸与实际删除证据。
- [2026-10-17 旧最小状态清理](MINIMAL_GAME_STATE_CLEANUP_2026-10-17.md)、[D 阶段拆分审计](APP_HOST_STAGE_D_AUDIT_2026-10-17.md)、[旧 ARCH-CLEANUP-2 / Recovery](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md) 均是历史。
- [历史归档](archive/README.md) 和 Git 提交保留设计追溯。旧 Rules 完整合法性、恢复/历史影子、多路径开发 NEXT 不再是生产必须保持的合同。

不要在 CURRENT_DEVELOPMENT_ROADMAP 或 NEXT_DEVELOPMENT_HANDOFF 再叠加旧日期、旧下一步列表；新任务只更新唯一当前状态与 GitHub 可验证结果。
