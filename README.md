# CampBoardGameHost

Blood on the Clocktower Android 主持项目。**2026-10-19 已批准 Minimal Host Architecture Rebuild**：按完整业务链重建；每条新路径验收后立即删除对应旧控制与全部消费者，最后全仓复查。不先全删，也不长期维护新旧双轨。

## 两项现行业务职责

- **Minimal Host**：唯一 Session/GameState、顺序已确认事件及实际公布信息、最多一个可持久化 pending、剧本驱动的昼夜确定性步骤/机械状态/胜负，以及原子提交和安全恢复。Host 独立判断下一步和唯一规则结果。未来 WebHost、EvidenceLab 共享状态与事件语义合同，而非 Android UI。
- **现有 AI 推荐**：保留首夜联合规划、后夜增量、提示词、API/解析、战略记忆以及人工/辅助/自动模式；玩家输入仍由玩家提供，只有非唯一结果由 AI/人工裁量，实际事实都由 Host 提交。
内置 Trouble Brewing（5–15 人）、No Greater Joy（5–6 人）及阵容和玩家身份跨局双多样性必须保留。旧评分、候选合法域、世界枚举、影子 GameState 和 UI 自有机械控制属于整链退出对象；**确定性规则不得误删**。未来单独 Rules 顾问或自定义剧本不是当前目标。

**现行 P0–P5：** 全仓职责归属与替代/删除清单 → P1 单步 pending 和提交/恢复联动代表性夜间组合 → P2 完整夜间 → P3 白天/胜负 → P4 UI 与现有 AI 收敛 → P5 全树零旧控制/文件 <=50,000 B /完整对局验收。Next 仅暂存最近操作，下一步骤真正执行前原子提交；之前允许退一步纠错，已公布信息不能普通回退。

## 唯一权威文档

1. [**Minimal Host Architecture Rebuild：唯一现行设计及实施合同**](docs/MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)
2. [开发规范](AGENTS.md)、[当前路线](docs/CURRENT_DEVELOPMENT_ROADMAP.md)、[新对话交接](docs/NEXT_DEVELOPMENT_HANDOFF.md)
3. [测试策略](docs/TESTING_STRATEGY.md)、[Host 事实合同](docs/MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)
4. [源码库存（历史扫描）](docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv)、[清理证据（历史）](docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)、[文档索引](docs/README.md)

## 项目结构和运行

app/ 为 Android Gradle 模块。tools/ 包含旧实验/测试及 CI 工具，须逐项按新合同审查。历史独立 web/ 主持实现已在清理批次退出；不得恢复其旧控制为未来 WebHost 或第二游戏事实权威。

Android Studio 可直接打开仓库；本地入口为 ./gradlew :app:testFast 和 ./gradlew :app:testFull :app:assembleDebug。GitHub CI/R2 是当前代码提交的独立验收。真实付费模型测试须另行获得预算授权。
