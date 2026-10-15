# PROD-GLOBAL-3B2-EXPERT-PROBE — 自主专家说书人提示词与测试设计 V1

> 2026-10-12 · **EXPERIMENT DESIGN / NOT RUN / NO PRODUCTION CUTOVER**  
> 唯一规则权威：[`PROD_GLOBAL_3B2_EXPERT_STORYTELLER_PLANNING_PRINCIPLE_2026-10-12.md`](PROD_GLOBAL_3B2_EXPERT_STORYTELLER_PLANNING_PRINCIPLE_2026-10-12.md)。基线 `main@10e5da8f165cbae0288b42d33c50bd0344b116d8`（写入时的核对值，执行时必须复查）。  
> 机器输入固定见 [`PROD_GLOBAL_3B2_EXPERT_PROBE_FIXTURES_V1.json`](PROD_GLOBAL_3B2_EXPERT_PROBE_FIXTURES_V1.json)，包含四个冻结基线配置 E01–E04，及额外可选、全员新手的 E05 同局对照。禁止将 `graderOnly` 送入模型。

## 1. 实验目的与边界

测试 **gpt-6-luna** 能否在不给候选枚举、不给示范答案的前提下，根据完整展示身份、Drunk 是否存在及其尚未绑定状态、经验等级、必要 TB 角色知识，独立输出 **一套** 可行、有推理空间的开局方案。AI 可独立理解规则，Host 仍独立验证并只在正确的时间提交。

只做实验文件；**不改生产、Mini MCP 或 Host 校验规则**，不恢复旧 heuristic。真实付费请求需在独立、已授权、明确限额且可读取原始输出的执行接口下进行。Mini MCP 的旧版 18 个 BoTC 实验任务已经退役（其 `docs/ORACLE_VM_DEPLOYMENT_RUNBOOK.md`）；Mini MCP EXPT-0/1 已于 2026-10-12 部署并通过真实连接的离线 canary；EXPT-2 付费调用接口尚未部署。现有 `run_task` 测试能力不等于付费模型实验能力。不能声称本 V1 已测过 Luna。

## 2. V1 System/Developer 提示词（固定，不带候选菜单）

```text
你是 Blood on the Clocktower《Trouble Brewing》的专家说书人，正在为一个已锁定展示身份、但尚未绑定可能的 Drunk 的真实合法配置规划开局。你熟知 TB 角色能力与时序；同时参考本请求提供的剧本规则事实。你不替任何阵营追求必胜，你要让善良有合理可推理路径、邪恶有可操作空间，游戏信息丰富且有不止一种合理玩家视角世界。

输入包含 Host 知道的座位、展示身份、玩家经验、本局是否包含 Drunk、尚未发生的事件、可选近期局摘要。不要把模型没有收到的信息当成已知：未知经验就是 UNKNOWN，未知投毒者目标、占卜师查询对象、管家主人或守鸦人未来死亡均不能捏造。已展示的镇民角色若选为 Drunk，其实际身份为 Drunk；这是合法的普通候选情形，不要对已知的明显合法性作无意义免责声明。

在脑中比较几种真正不同的完整联合策略，考虑：
(1) Drunk 决定如何同时改变多个首夜信息与善良交叉验证；
(2) Chef、Empath、Investigator、Washerwoman、Librarian 与 Fortune Teller 信息如何互相支撑或施压；
(3) Recluse / Spy 的合法多种登记解释：选择玩家真正看到的结果，而非杜撰唯一登记见证；
(4) 玩家水平、恶魔/爪牙是否过早陷入无解处境，善良方是否仍有纠错及推理路线；
(5) 红鲱鱼、Drunk、可变登记与恶魔掩饰的互补或机会成本，避免浪费所有不确定性机制于同一作用；
(6) 首夜行动先后以及尚未发生的玩家选择；绝不提前发布有未解决依赖的观察。

仅给出一套你最推荐的具体语义方案及简短私有策略记忆。不要展开冗长分析、输出多个替代答案或预枚举候选；不要虚构合法候选 ID。Host 会在采用前独立验证整包选择、时序、最新游戏修订与待定依赖；这不是让你逃避对规则作正确判断的理由。

严格输出一个 JSON 对象，遵循输出字段约定。需要模型裁量的已知信息尽可能给出具体数值/二人座位和角色；Host 唯一规则确定的信息标记为 Host-calculated 而非假装另作决策。对未知 Poisoner 行动引起的下游变化可以给尚未提交的条件性意图，必须显式列出需要等待的事件。已知不存在的能力不得硬塞信息或红鲱鱼。
```

**组装请求：** `system` 采用上段；`user` JSON 仅包含 `schemaVersion`, `roleCatalog`, `roleEffects`, `rules`, `fixture.input`。绝对剔除 `fixture.graderOnly`，不包含 `Host legalCandidates`、`DrunkHypotheses`、每项候选笛卡尔积或历史 B2 答案。实验版本固定角色名，最终生产实现再决定英文 RoleId 映射/JSON Schema 的最小稳定结构。

## 3. 响应的试验性语义契约（非已生效的 Kotlin/API 契约）

```json
{
  "asOfRevision": 1,
  "openingPlan": {
    "drunkSeat": "<合法展示镇民座位或null>",
    "redHerringSeat": "<合法善良座位或null>",
    "demonBluffRoles": ["<场外善良角色1>", "<场外善良角色2>", "<场外善良角色3>"],
    "informationIntents": [
      {
        "sourceSeat": "<实际具备该能力的座位>",
        "ability": "<配对信息角色>",
        "kind": "PAIR_CHARACTER",
        "targetSeats": ["<座位A>", "<座位B>"],
        "character": "<具体被展示角色>",
        "value": null,
        "condition": "CURRENT_UNAFFECTED"
      },
      {
        "sourceSeat": "<需要非确定数值的角色座位>",
        "ability": "<具有数值信息的角色>",
        "kind": "NUMBER",
        "targetSeats": [],
        "character": null,
        "value": "<合法数字>",
        "condition": "DRUNK_MISINFORMATION"
      }
    ],
    "hostDetermined": ["only decisions whose outcomes are uniquely rule-calculated"],
    "deferred": [
      {
        "event": "fortune_teller.targets",
        "sourceSeat": "<适用的角色座位>",
        "reason": "player has not selected queried seats"
      }
    ]
  },
  "strategyMemory": {
    "tensions": ["最多3个短句"],
    "intentions": ["最多3个可修订的条件策略"],
    "uncertainties": ["最多3个真实未知的依赖或假设"]
  },
  "choiceSummary": "不超过120字说明联合策略要点和主要代价"
}
```

上面用字符串占位符表示语义类型，**不是可直接提交的 JSON 实例、不是 E01 标准答案，也不是真实 Luna 输出**。模型应该写该局自己的语义结果而不是复制数值。当前评审为便于人工判断允许 `choiceSummary`；生产时可删除。不要把模型指定的登记 witness 纳入持久事实。

- `drunkSeat`：`hasDrunk=true` 必须是一个合法展示镇民座位；不存在则必须 `null`。
- `redHerringSeat`：仅实际具有 Fortune Teller 能力时选择善良座位；如果该展示角色被选为 Drunk，须为 `null`。
- `demonBluffRoles`：严格三个不同、合法、场外的善良角色；必须用 Host 对当前阵容的校验确认，不能先信模型。
- `informationIntents`：只列正在本次全局开局计划中需要说书人裁量的真实信息（含选择 Drunk 后为其虚构的信息）；`PAIR_CHARACTER` 含两个不同座位与具体所示角色；`NUMBER` 含一个数值。信息的结果合法性与毒/醉状态、合法登记必须一起核验；不可把模棱两可的 witness 断言为真实决定。`CURRENT_UNAFFECTED`、`DRUNK_MISINFORMATION`、`PENDING_POISONER` 是实验注释而非 Host 状态承诺。
- `hostDetermined`：若用户实际应见到的结果只有唯一合法值，应由 Host **重算并发布**；模型不得与之矛盾或人为选择。未来实现可省略此字段，由 Host 拼入。
- `deferred`：只列选择联合方案后仍可能生效的依赖；输入的 `unknownEvents` 是 **潜在**依赖，不代表所有能力必然生效。例如 Drunk 若占据 Fortune Teller 展示位，则该角色并无实际查询能力。
- `strategyMemory`：不超过 3+3+3 个短句，标记为非权威建议；不得充当 Host 已确认事实。

**重要协议待验证点：** 带 Poisoner 的场景中是否用 `PENDING_POISONER` 表示未生效的条件性配置信息，或者严格延后全部可能受影响的结果，应根据实际 Luna 合法性/依赖覆盖表现及真实 Host 发牌顺序决定；不能偷偷把预计信息当已发布观察。

## 4. 固定案例及覆盖

| ID | 展示座位布局 | 机制与观察点 |
|---|---|---|
| E01 | Chef / Fortune Teller / Ravenkeeper / Butler / Investigator / Recluse / Baron / Imp；Drunk 存在 | 初夜双重信息压力、Baron+Drunk、Recluse、低熟练邪恶与 bluff/red herring 机会成本；**不**以 Chef=Drunk 等特定答案作 oracle |
| E02 | Washerwoman / Librarian / Chef / Empath / Fortune Teller / Recluse / Poisoner / Imp；无 Drunk | Poisoner 必须先选目标，之后信息才可能受影响；未中毒的 Empath 0 属规则确定信息，模型不得把毒选人捏造成事实 |
| E03 | Investigator / Chef / Fortune Teller / Recluse / Empath / Spy / Washerwoman / Imp；无 Drunk | Empath 同时夹 Recluse 与 Spy、数值可能有多种登记解释，Investigator/Washerwoman 与 Spy 配对的可能性 |
| E04 | Washerwoman / Librarian / Empath / Fortune Teller / Saint / Butler / Baron / Imp；Drunk 存在 | 两个首夜配对信息与 Drunk 的互相影响；选择醉酒的 FT 会令“实际 FT 红鲱鱼”不适用；可选近期局经验仅温和影响 |
| E05（可选） | 与 E01 座位完全相同，**八人经验全部 BEGINNER** | 对齐用户 ChatGPT 5.5 Low/High 盲测时的局面经验设定，但保持 Luna 的独立专家系统提示词、候选禁发及结构化输出；此前聊天结果永远不发入模型 |

测试案例经过 8 人 TB 基本构成核算：E01/E04/E05 为 4 个展示镇民 + 2 个展示外来者 + Baron + Imp，且指定 Drunk 使实际角色类别变为 3 镇民 + 3 外来者；E02/E03 为 5 镇民 + 1 外来者 + 1 爪牙 + Imp，无 Drunk。E05 不参与默认 `--case all` 四局付费批次，可用 `--case E05` 单独测试。ChatGPT 5.5 Low/High 参考审阅见 [独立参考文档](PROD_GLOBAL_3B2_CHAT_BENCHMARK_REFERENCE_2026-10-12.md)，绝不作为提示词示例、Gold 答案或盲评分标准。

## 5. 实际运行与盲评分离

### 执行协议

1. 首先检查 `main`/实验分支和 Mini MCP **现有**工具。没有授权的付费任务接口不得绕开 MCP 用外部副作用或借旧退役任务；本阶段可以完成全部离线实验设计，但必须如实标为 `NOT RUN`。
2. 通用 EXPT-0/1 已部署；现在使用 `experiment_define/preflight/read` 登记和核验离线输入及元数据，其中 `preflight` 始终报告 `networkRequests:0` / `paidExecutionAuthorized:false`。EXPT-0/1 不会检查真实 API Key 是否存在，也不能发起请求。真正通过 Mini MCP 付费执行仍需未来独立授权的 EXPT-2 `experiment_run/status`；当前可由用户在开发机明确执行受限手动 runner。所有真实 API 请求均需 `store:false`、限额及私有结果目录。不提交 Key、原始隐藏身份/回答至公共仓库。
3. 控制同一 `gpt-6-luna`、同一 API transport、同一输出格式预算/超时与重复次数。首轮建议 4 个 fixture × 2 次重复；若需完整同局直接 A/B，应额外为同四局制作对应 B2 输入后以同预算测，不可把历史不同输入/不同运行条件的 B2 响应当严格统计基线。
4. 每条样本私有保存原请求/响应、是否完整/拒答、结构验证、API 错误、真实延迟、usage tokens、成本（仅账单有可靠数据时）。失败不自动重试；需要重新执行必须单独记为新试次。
5. 评判者看匿名响应与匿名样本 ID，**不得**看到 `graderOnly` 或此前 A/B/B2 答案来推定“标准选择”；可使用单独的规则审查记录做合法校验。

### 独立评分轴（不要合成一个拍脑袋总分）

- **L / 法律与事实（硬门）**：Drunk/角色类别、bluff 数量与场外身份、FT 真实存在性和 RH 善良性、合法信息和登记、时序、未知行动不得捏造、修订标识吻合。独立 Host 验证才可声称 L-PASS；在 Host 尚无语义 adapter 前标记 `MANUAL_AUDITED`，不可冒充机器验证。
- **C / 完整性（硬门）**：所有当前适用的初夜可裁量领域覆盖；该由 Host 决定的值不要重复裁量；待定 `poisoner.target`、`fortune_teller.targets`、`butler.master` 等真实依赖均明确标记，已不适用的依赖不能凭模板套入。
- **S / 联合战略（5 项各 0–4）**：善良推理/可反驳空间；邪恶可玩性与玩家水平；跨角色交互与替代世界；混淆机制机会成本；长期连续性/非重复（无历史的 case 不计最后一项）。按结果评判，不预设最优 Drunk、pair、bluff 或 RH。
- **P / 表现**：实际总耗时、输入/输出/总 tokens、拒答与重试率、按可执行整包计费成本；不能根据生成文字短就假定网络更快。

### B2 基准（历史，非新试验）

3P 文档记录 E01 的旧 B2 类似八人局 **34.34s / 4,286 tokens**，选择 Chef Drunk、Investigator 4/7、RH Recluse 6；旧 B 初版 **28.15s / 4,014 tokens**，但将 Investigator 指向实际两个邪恶座位。一次一局的指标**不能**证明稳定差异。另一个 B2 无 Drunk 测试遗漏了 Poisoner 的显式 deferred 依赖。这些是已知问题待复核，不是结果标签、模型示范或本轮跑过的指标。

## 6. 当前源码观察：玩家水平数据不可误认为已记录

已有 `InitialGlobalOpeningScopeV1` 内部经验域包含 `BEGINNER / REGULAR / VETERAN / UNKNOWN`，但 `StorytellerPlayerContextInputV1` 旧持久化输入的缺省值是 `NORMAL`，并用缺省实例表示「尚未覆盖」。因此不能简单地把未显式记录的生产玩家资料映射为已知的 REGULAR/NORMAL：未来真实全局输入构造器应保留 **是否实际登记过经验** 的来源标识，缺失时传 UNKNOWN。此为本轮只读发现，**尚未修改生产实现**。测试 E01 有一个 UNKNOWN 座位专门验证模型不会代入常规水平。

## 7. 生产设计的条件性决策门

- 若合法整包与依赖覆盖稳定：生产推荐单个 `gameWorld -> openingPlan+strategyMemory` 语义合同，由 3B1/Host 内部候选与版本约束校验，不把候选菜单传给模型；3C/3D 联合后台请求同步身份展示。
- 若规则/角色错误集中：先修 **通用、准确、有限** 的规则/时序提示，再重测同组；不添加 E01 座位或角色组合特例。
- 若 Spy/Recluse、Poisoner 等特定机制仍失败：仅针对经实证需要的机制添加精准 Host 事实/约束；拒绝退回所有候选笛卡尔积。
- 若 JSON/覆盖仍有缺陷：先试简化 schema、较严 JSON Schema、完整包一次有限纠正。不可自动采纳部分结果或按首个候选兜底。
- **未有真实可比样本前，结论固定为 `INCONCLUSIVE`，不准标记 3B2 生产验收。**
