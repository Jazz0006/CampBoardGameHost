# PROD-GLOBAL-3B2 — 开发机手动 Luna 测试包（2026-10-12）

状态：READY FOR USER MANUAL EXECUTION；ChatGPT 尚未发起任何付费请求。本包是开发机终端执行工具，不是 Mini MCP EXPT 服务，也不是生产 Android 接口。

## 包内文件

- tools/prod_global_3b2_manual_probe.py — Python 3 标准库、默认零网络、显式授权的真实 Luna 请求。
- tools/tests/test_prod_global_3b2_manual_probe.py — 零网络单元测试。
- docs/PROD_GLOBAL_3B2_EXPERT_PROBE_FIXTURES_V1.json — E01–E04 四个 TB 输入和 22 种角色知识；评分目标 graderOnly 不发给模型。
- docs/PROD_GLOBAL_3B2_EXPERT_PROBE_DESIGN_V1_2026-10-12.md — 专家 system 指令、实验协议和评估标准。

脚本直接从上述设计文档第 2 节提取提示词；模型 JSON 仅包含 roleCatalog、roleEffects、rules 和 fixture.input，不发送 graderOnly、B2 历史答案或 Host 候选枚举。

## 第一步：离线预检，零 API 费用

在 Oracle 开发机的 SSH 终端执行：

    cd /home/opc/repos/CampBoardGameHost
    git status --short --branch
    python3 -m unittest -v tools.tests.test_prod_global_3b2_manual_probe
    python3 tools/prod_global_3b2_manual_probe.py --case all

预期八个单元测试通过，并打印 mode=OFFLINE_PREFLIGHT、networkCalls=0、四个测试编号及 request SHA-256。如果失败，不要执行 --live。

## 第二步：先真实测试 E01 一次

将 OpenAI Key 安全输入当前 shell，不在命令历史中写明文：

    read -rsp 'OpenAI API key: ' OPENAI_API_KEY; echo
    export OPENAI_API_KEY
    umask 077

执行一笔真实计费请求（固定 gpt-5.6-luna）：

    OUT="$HOME/botc-3b2-e01-$(date +%Y%m%d-%H%M%S)"
    python3 tools/prod_global_3b2_manual_probe.py \
      --live --case E01 --max-calls 1 --timeout 180 \
      --output-dir "$OUT"
    python3 -m json.tool "$OUT/summary.json"

命令显式规定了最多 1 次调用。真实 API 用量另行收费；模型固定为 gpt-5.6-luna，store=false，strict JSON Schema，max_output_tokens=8192，超时 180 秒，无重试。API HTTP 错误只保留状态，不记录密钥、请求头或上游错误正文。

结果仅能写入 Git 仓库外的绝对路径，且要求路径全新、私有（0700）；JSON 文件为 0600。

## 第三步：如果 E01 正常，再测试全部案例

以下会新增四笔计费请求，包括 E01 的另一次独立测试：

    OUT="$HOME/botc-3b2-all-$(date +%Y%m%d-%H%M%S)"
    python3 tools/prod_global_3b2_manual_probe.py \
      --live --case all --repeat 1 --max-calls 4 \
      --output-dir "$OUT"

要对全部四局各重复两次，换一个新的私有目录，使用 --case all --repeat 2 --max-calls 8（八笔额外付费请求）。失败不自动重试；发生 HTTP、模型拒答或解析错误等技术/协议失败时立即停止本批，不继续发生额外付费调用，已完成结果仍保留。收到完整计划但基础检查存在问题时会保留样本继续实验，由后续人工审查。任何再次执行均是新的样本。

最后可从 shell 清理环境变量：

    unset OPENAI_API_KEY

## 私有文件与回传

每个样本例如 E01_R1，会写入：

- E01_R1.request.json：精确发送的模型请求，不含凭据；
- E01_R1.raw.json：收到的完整 API 响应；
- E01_R1.plan.json：解析出的结构化配置；
- E01_R1.metrics.json：耗时、token 用量、粗筛结果；
- summary.json：整批汇总。

这些输出仅是**测试证据**。basic_checks_ok 不代表 Host 合法性校验通过，也不意味着推理策略优秀；本阶段仅有普通字段检查、Drunk 座位类别、bluff 集合、真实 Fortune Teller 与红鲱鱼关系、典型行动依赖等快速检查。完整规则合法性、信息依赖与战略质量仍需要单独审查。

手动测试完成后，可以将 summary.json 与各个 plan.json 文件上传到本对话；最好同时提供 raw.json，以便复查 tokens、响应状态和格式。不要上传密钥、.env、认证头或 shell 历史。不要将这些结果复制进 Git 仓库以便 Mini MCP 读取。

## 历史对比和失败处理

旧 B2 在类似 E01 八人局的历史单次运行耗时 34.34 秒、总 token 4,286；当时输入和格式并不相同，这只是参考，不是可直接推断的新基准或标准答案。本轮仍需区分规则正确性、全局战略质量、信息/待定依赖完整性、实际延迟和用量。

若遇到 HTTP 400（参数或 strict schema 不支持），记下状态并停止；不要无声切换成非结构化回答。HTTP 401/429 则单独检查认证或配额，不要将 API 错误原文及密钥发送给 ChatGPT。

**本手动测试是用户明确要求的终端执行路径。** 它不会恢复 Mini MCP 退役的实验任务，不绕过 Host 规则权威，也不改变原定 EXPT 框架的并行开发计划。
