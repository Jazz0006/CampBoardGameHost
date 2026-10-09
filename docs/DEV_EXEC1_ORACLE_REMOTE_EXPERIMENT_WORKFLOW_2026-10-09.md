# DEV-EXEC1 — 开发机远程实验执行与回传（2026-10-09）

> 状态：**离线任务已在 mini-mcp PR #8 实现，本地 Mini MCP 检查通过；当前 tunnel 进程未加载新任务，远程端到端验收阻塞。** 本阶段没有进行付费实验。
>
> Mini MCP implementation: https://github.com/Jazz0006/mini-mcp/pull/8

## 目标与权威边界

`ChatGPT -> Mini MCP -> Oracle VM（开发机）-> Host API0 runner -> bounded task results -> ChatGPT audit`。

Host 继续持有所有规则、当前游戏 canonical state、合法候选及用户最终决策。实验不进入 Android 产品、不修改 Game Engine/Recovery、不增加长期游戏档案。

## 离线 MVP

在部署新 Mini MCP 配置并**实际重启服务**后，ChatGPT 调用：

```text
run_task({ repo: "clocktower", task: "api0-offline" })
```

执行由 Mini MCP 固定配置，禁止传入 shell 参数：

```sh
/usr/bin/python3.12 -B -m unittest discover -v -s tools/tests -p test_gsp_api0_responses.py
/usr/bin/python3.12 -B tools/gsp_api0_responses.py
```

第一个命令输出单元测试数与失败信息；第二个命令仅生成冻结 A/B/C×D0/D1/D2 九组 prompt SHA-256 manifest（`network_calls: 0`），不读取密钥、不发 HTTPS 请求。验收：任务状态 exit 0、`Ran 10 tests`、`count: 9`、`network_calls: 0`。任一条件不成立就不能声称远程执行链路已打通。

检查时确认：
- Host checkout 的两个早先 `__pycache__` 为未跟踪缓存，严禁纳入提交；`-B` 抑制后续缓存。
- Oracle 的互动 Shell 环境变量**不等于** systemd 的 Mini MCP 服务环境；参考 mini-mcp `docs/ORACLE_VM_DEPLOYMENT_RUNBOOK.md` 中 `User=opc`、`EnvironmentFile=/home/opc/.config/mini-mcp/tunnel.env`。
- `/usr/bin/python3.12` 是 Mini MCP EvidenceLab 任务的既有解释器。真正执行成功后才说明服务上下文具备该 Python 版本。

### 当前可复现阻塞

在 mini-mcp feature commit `380bc011cb3bf74fc548811284f7e074258ce3d0` 后执行过 `runtime_restart`，工具返回 `Restart scheduled: true`，但后续 `runtime_status` 的 instance ID/startedAt **不变**，`restartScheduledAt` 仍停留在 2026-10-02，调用新任务继续得到 `Task is not allowed`。代码表明 restartPending 是进程内持久置位，已安排的失败重启不会重新调度；不要认为请求重启成功即为服务已重启。需要开发机管理员从 SSH 会话实际执行一次 `sudo systemctl restart mini-mcp-tunnel.service`，然后 ChatGPT 重新检查实例 ID、再运行离线任务。重启前先确保运行进程从预期的 Mini MCP 工作树启动。

## MEM0 付费预案（**本轮不授权、不执行**）

- 使用已有冻结 fixture 和固定 seed，限定 **最多九次尝试**、每组独立 Responses API 请求、零自动重试；API0 `store:false` 与合法 candidate ID 校验必须保留。
- `mem0-live-pilot` 必须由另一阶段配置固定命令、明确模型与服务侧独立计费开关；即使安装任务，也**不能因默认调用而产生收费**。
- `OPENAI_API_KEY` 仅放在开发机上的服务私有环境文件，不经 MCP 参数、日志、Git、实验文件传输；输出必须位于 repo 外、每次全新目录，模式建议 `0700/0600`。
- 预先设定总调用预算、明确用户再次授权后才执行。用户已有 S001 单次调用成功，可作为传输冒烟验证，但这**不是**完整九组 MEM0 或模型质量结论。

## 私有结果回传与盲评隔离

API0 目前输出 `S001.json`…`S009.json`、`S###.prompt.txt` 和 `private_arm_mapping.json`，位于开发机仓库外。Mini MCP 的通用 `read_file` 仅能读取已配置 Git 仓库，因此**暂时不能直接读取 repo 外的响应文件**。不能把结果复制回 Git 以规避路径限制。

Mini MCP PR #8 已准备受控只读代码 `read_mem0_sample({sample_id:"S001"})`，并通过合成样本、本地类型检查与 MCP 协议测试；**尚未在实际服务进程启用或对真实批次验收**。服务只有配置 `MINI_MCP_MEM0_SAMPLE_DIR` 到一个私有、仓库外、`0700` 的**唯一批次目录**后才允许读取；响应文件必须 `0600`、小于 64 KiB。安全限制如下：

1. 服务配置绑定唯一的非 Git 实验批次输出目录，不接受用户给的任意文件路径；
2. 只允许精确 `S001`—`S009` 的 JSON，检查真实路径、符号链接、普通文件、大小上限与 JSON 结构；
3. 永不通过该接口暴露 `private_arm_mapping.json`、prompt 文件、API Key、HTTP 请求头；
4. 独立评分完成后由未参与盲评的审计者解盲映射；验证 S001–S009 响应内容来自真实执行，不把随机 seed 推知的 arm 身份交给盲评模型；
5. 使用合成内容完成正常读取、越界、路径穿越、超限和映射拒绝测试后再启用实际结果回传。

## 阶段结论与下一步

**已证实：** ChatGPT → Mini MCP 访问开发机仓库、固定任务框架、Mini MCP `check` 本地验收。

**待证实：** 新 task 在实际服务进程生效、Python 3.12 Host API0 离线测试执行，以及只读工具加载/真实响应读取。**未启用：** MEM0 付费任务；盲样只读工具当前未配置真实批次目录。

后续必须先解除 systemd 重启阻塞，再完成离线 GREEN 验收；之后设计并测试受限 JSON 读取，最后才寻求九次付费调用的显式授权。
