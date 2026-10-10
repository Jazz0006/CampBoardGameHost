# CampBoardGameHost

离线优先的 Android 桌游主持应用，包含 Blood on the Clocktower（血染钟楼）、狼人杀和谁是卧底。当前研发重点是 **Trouble Brewing（暗流涌动）的规则正确性、可独立运行的 Game Engine，以及可替换的说书人推荐 Provider**。

## 当前方向（2026-10-10）

- **生产优先级是 PROD-GLOBAL-1 / PROD-GLOBAL-1D：真实全局 LLM 说书人推荐、后台首夜联合预规划、因果事件驱动的未执行裁量重规划，最终推进 AI_AUTOMATIC / 无人类说书人**。完整现状与未验收边界以 [当前路线](docs/CURRENT_DEVELOPMENT_ROADMAP.md) 和 [当前交接](docs/NEXT_DEVELOPMENT_HANDOFF.md) 为准。
- **Game Engine / 规则 / 合法候选 / 状态写入** 均由 Host 掌握；无网络、无 LLM 时仍能运行 Manual 游戏。旧权重/风格/角色特例推荐已经退役，不能复活。
- [PR #292](https://github.com/Jazz0006/CampBoardGameHost/pull/292)（Draft）已包含首夜 pair/numeric/Boolean 全局 AI_ASSISTED 推荐通道，以及 Android Gateway HTTPS 地址持久化、访问令牌 Keystore 加密持久化；离线/Android CI 已验证对应代码，**尚未完成真实 Android→HTTPS→Gateway→LLM 端到端游戏验收，不能作为已发布全自动 Host**。
- Oracle 开发机的 Gateway 和真实 `gpt-6-luna` 合成首夜测试已成功进行 3 次付费调用，记录 22.83/25.92/25.32 秒。该测试并非真实 Android 游戏，且包含不符合正式首夜顺序的测试前缀，不能直接用作整局效果验收。下一步优先拆分模型延迟、推理/输出 tokens 与首次/后续质量；5 秒的互动刷新只是待验证的体验目标。
- 酒鬼晚绑定：**先固定展示身份并逐人展示，可以随后才从合法镇民展示身份中确认实际酒鬼**，只需在相关首夜能力裁定之前完成；初始后台全局战略不能假装未选择的酒鬼或投毒目标已经确定。
- GSP-MEM0、EvidenceLab、训练和进一步 Recovery 扩展目前**不阻塞生产**；Recovery 仍限同当前兼容格式、4 小时内的意外关闭恢复。

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
- `tools/gsp_api0_responses.py`：历史 API0 离线优先 benchmark transport（[API0 说明](docs/GSP_API0_OPTIONAL_RESPONSES_BENCHMARK_TRANSPORT_2026-10-09.md)），不再是 Android 生产入口。
- `tools/storyteller_gateway.py`：开发机服务端 OpenAI Gateway（Bearer、调用配额与固定 loopback）；Android 客户端计划经已配置的 HTTPS Funnel 访问，**真实手机 E2E 尚待验收**。
- `tools/prod_global_live_smoke.py`：显式 `--live` 才产生最多三次付费请求的合成局测试；私有报告可通过 Mini MCP `read_botc_evaluation_report` 读取。
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

历史 API0 本地（**不需 API key，不发网络请求**）：

```bash
python3 tools/gsp_api0_responses.py
python3 -m unittest discover -v -s tools/tests -p 'test_gsp_api0_responses.py'
```
