# CampBoardGameHost

Blood on the Clocktower Android 主持项目。正在进行 **Host Core 优先的整仓架构清理**；当前旧 Rules/AI 和旧世界推演仍有残留，不能把历史代码误认为最终模块实现。

## 三个职责

- **Host Core：当前唯一优先级。** 唯一真实状态与结构化关键事件，基本手动操作和安全提交。以后与独立 WebHost、EvidenceLab 共用状态/事件语义合同。
- **Rules：将来重新建。** 只读 Host，返回指定角色、时刻和状态的合法信息；旧规则检查实现现阶段全部删除。
- **AI Clues：Host 稳定后再规划。** 只读 Host + 实际事件，生成一条或多条接近专家说书人的线索；建议确认后才形成事实。

**严格执行 P1–P5：**删除旧规则 → 删除旧 AI/候选/世界推演 → 收敛 Host 事实核心 → 精简 UI/所有源文件 <=50,000 B → 全仓验收/跨项目合同。清理期间不允许旧规则计算阻止手动信息输入。

## 唯一权威文档

1. [开发规范](AGENTS.md)
2. [三模块重建与全仓清理路线](docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md)
3. [当前路线](docs/CURRENT_DEVELOPMENT_ROADMAP.md) 与 [当前交接](docs/NEXT_DEVELOPMENT_HANDOFF.md)
4. [Host 事实合同](docs/MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)
5. [源码库存](docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv)、[测试策略](docs/TESTING_STRATEGY.md)、[文档索引](docs/README.md)

## 项目结构和运行

app/ 为 Android Gradle 模块。tools/ 包含旧实验/测试及 CI 工具，须逐项按新合同审查。web/ 是另一套独立旧网页主持程序，仍有 Undercover/狼人杀/localStorage，是否全盘退役待用户审核；**不能误当成独立 WebHost 项目的未来统一 Host**。

Android Studio 可直接打开仓库；本地入口为 ./gradlew :app:testFast 和 ./gradlew :app:testFull :app:assembleDebug。GitHub CI/R2 是当前代码提交的独立验收。真实付费模型测试须另行获得预算授权。
