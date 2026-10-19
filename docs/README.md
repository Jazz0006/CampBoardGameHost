# 文档入口（2026-10-19）

## 现行权威与推荐阅读顺序

1. [**Minimal Host Architecture Rebuild（已批准的唯一总设计与执行合同）**](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)：唯一 Session + 一个 pending、Host 全局确定性流程、整链替换与立即删旧、P0–P5 与风险/验收。
2. [AGENTS](../AGENTS.md)：提交、安全与工程约束；[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md)、[新对话交接](NEXT_DEVELOPMENT_HANDOFF.md)：当前分支、PR、第一批实际任务。
3. [测试策略](TESTING_STRATEGY.md)：业务行为与退出门槛；[Host 事实合同](MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)：核心事件语义。

**冲突优先级**：新总纲 > AGENTS 最新段 > Roadmap/NEXT 最新段 > 事实合同/测试 > 历史文件。旧三模块新 Rules、旧 P1–P5、局部 F12 和 ARCH-CLEANUP-2 分批指令均只作为当时审计/提交证据，不能再次作为下一步执行命令。

## 只作为库存和历史证据，不给出新的开发优先级

- [当前扫描源码索引](MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv)：原扫描 717 个代码类文件的路径、大小与**初步分类**；需以最新 Git 树更新，不能视为完成逐文件行为审计。
- [第一轮全仓扫描与删除台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)：包含曾经误设的“Host 不计算规则”与 F12 过程记录，当前按新两职责 Host + 已有 AI 重构合同重新归属；仅保留历史大小与实际删除证据。
- [2026-10-17 旧最小状态清理](MINIMAL_GAME_STATE_CLEANUP_2026-10-17.md)、[D 阶段拆分审计](APP_HOST_STAGE_D_AUDIT_2026-10-17.md)、[旧 ARCH-CLEANUP-2 / Recovery](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md) 均是历史。
- [历史归档](archive/README.md) 和 Git 提交保留设计追溯。旧 Rules 完整合法性、恢复/历史影子、多路径开发 NEXT 不再是生产必须保持的合同。

新任务只维护顶部现行状态和 GitHub 可验证结果；历史旧路线尽量归档而不是不断叠加“最新指令”，不得与现行总纲竞争权威。
