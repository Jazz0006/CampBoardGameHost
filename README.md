# CampBoardGameHost

离线优先的 Android 桌游主持应用，包含 Blood on the Clocktower（血染钟楼）、狼人杀和谁是卧底。当前研发重点是 **Trouble Brewing（暗流涌动）的规则正确性、可独立运行的 Game Engine，以及可替换的说书人推荐 Provider**。

## 当前方向（2026-10-09）

- **Game Engine / 规则 / 合法候选 / 状态写入** 均由 Host 掌握；无网络、无 LLM 时仍可完整进行 Manual 游戏。
- 旧版权重式、风格式、角色特例式自动推荐引擎已按 RES-0～5 / GSP-1 退役。多个合法候选若没有合格 Provider，应保持 `MANUAL_REQUIRED`；不得恢复旧 heuristic。
- **GSP-MEM0 是当前研究优先级**：用冻结的多决策测试比较当前状态、事实历史及战术/跨局战略记忆的实际推荐质量。尚无独立模型实验结论。
- **GSP-API0 已合入**：仅提供开发者端可选 Responses API benchmark runner，默认零网络。它不是 Android 正式接入或质量验收；付费调用必须由开发者显式启动。
- **Recovery 只用于同版本、同兼容令牌、4 小时内的当前游戏意外中断恢复**。不把完整历史、任意时点重建或长久存档作为产品目标。

## 权威文档与测试

1. [开发规范](AGENTS.md)
2. [文档入口](docs/README.md)
3. [当前路线（唯一优先级权威）](docs/CURRENT_DEVELOPMENT_ROADMAP.md)
4. [当前交接（唯一活动 handoff）](docs/NEXT_DEVELOPMENT_HANDOFF.md)
5. [测试策略](docs/TESTING_STRATEGY.md)

历史路线、已完成阶段和验收快照收录于 [文档归档](docs/archive/README.md)，**不**决定下一阶段任务。

## 工程结构与本地运行

- `app/`：Android 客户端与游戏主持逻辑。
- `tools/asp_oracle/`：ASP Oracle / golden fixture。
- `tools/gsp_api0_responses.py`：离线默认的 MEM0 API benchmark transport（参阅 [API0 说明](docs/GSP_API0_OPTIONAL_RESPONSES_BENCHMARK_TRANSPORT_2026-10-09.md)）。
- `player/`、`ui/`、`web/`：其他客户端与界面资产；`docs/`：规范与记录。

用 Android Studio 打开仓库根目录。常用 Android JVM 检查：

```bash
./gradlew :app:testFast
./gradlew :app:testFull
```

ASP Oracle：

```bash
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
```

API0 本地（**不需 API key，不发网络请求**）：

```bash
python3 tools/gsp_api0_responses.py
python3 -m unittest discover -v -s tools/tests -p 'test_gsp_api0_responses.py'
```
