# R5.5 多剧本 / 动态流程实施进度与下个会话交接

> 日期：2026-08-20  
> 分支：`codex/storyteller-algorithm-v4`  
> PR：#2，继续保持 **open + Draft + base=main + do not merge**  
> 路线权威：`CURRENT_DEVELOPMENT_ROADMAP.md`  
> 架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`

## 1. 本次重新审计后的架构结论

### 1.1 不再新增“多剧本框架”抽象层

当前已经形成足够的多剧本基础：

```text
ClocktowerCharacterDefinition
ClocktowerCharacterRegistry
ClocktowerScriptDefinition
ClocktowerScriptCatalog
ValidatedClocktowerRuleset
NightOrderToken
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteraction
```

因此后续**不要**再创建 `MultiScriptManager`、`GenericScriptEngine` 或另一套 script identity / JSON loader。

判断一个新剧本是否真正被框架支持，应该看它能否按下面的路径加入：

```text
script asset / imported JSON
        ↓
existing parser + normalizer
        ↓
existing character registry + script catalog
        ↓
existing FlowPlanner
        ↓
role-specific handler / interaction hook where needed
```

如果加入新剧本必须在 `ClocktowerFlowPlanner` 主干增加：

```kotlin
if (script == NoGreaterJoy) { ... }
```

或新的 script-name `when`，则说明抽象仍有问题，应修 seam，而不是继续堆剧本分支。

### 1.2 当前 App 已经存在真实第二剧本，应该立即利用

现有 legacy product 已经支持：

- `ClocktowerScript.TroubleBrewing`
- `ClocktowerScript.NoGreaterJoy`
- 5–6 人 setup UI 可选择两个剧本；
- 5–6 人默认 No Greater Joy；
- legacy 发牌函数已经按 script 选择角色池；
- No Greater Joy 已有实际 gameplay，包括 `Clockmaker / Chambermaid / Artist / Sage / Klutz` 等 TB 外角色。

因此 S2 不再使用人工 toy script 证明“理论上多剧本”。

**S2 必须直接使用现有 No Greater Joy 作为第二个真实 structural proof。**

No Greater Joy 当前角色池：

```text
Clockmaker
Investigator
Empath
Chambermaid
Artist
Sage
Drunk
Klutz
Baron
Scarlet Woman
Imp
```

它同时覆盖：

- first-night information；
- recurring night information；
- day action；
- night-death trigger；
- death follow-up；
- setup mutation；
- Demon succession。

这使它比虚构 fixture 更适合发现“表面通用、实际 TB-only”的流程设计。

## 2. 当前已完成进度

### S0 — Schema / Catalog / Import / Validation — PASS

#### S0.1 catalog core

Source：`beb23512b4133712025e534152d268fd9315a5bf`

完成：

- 强类型 character/script catalog；
- official/custom script parser + normalizer；
- `RulesetJsonLoader.parseScript(...)` 作为现有 loader 的新强类型入口；
- Trouble Brewing legacy composition/night-order parity；
- deterministic content hash；
- unknown/duplicate/night-token fail closed；
- homebrew/bootlegger 自动降级为 `UNVERIFIED`。

验证：CI #120 success；R2 boundary #112 success。

#### S0.2 official schema compatibility + typed validation

Source：`b02153f821f2403aa39d733d3fe30ace3e6abebd`

完成：

- typed validation error code；
- 当前 TPI custom-script schema 的主要约束；
- `remindersGlobal / jinxes / special` metadata；
- decimal night priority；
- custom-character `additionalProperties=false` 类安全边界；
- JSON metadata 仍不被解释为通用规则 DSL。

验证：CI #121 success；R2 boundary #113 success。

#### S0.3 canonical Trouble Brewing script asset

Source：`df0509da...`

新增 canonical asset：

```text
app/src/main/assets/scripts/trouble_brewing.json
```

锁定后续 FlowPlanner 的稳定输入，而不是依赖测试内联 JSON。

验证：CI #122 success；R2 boundary #114 success。

### S1 — Trouble Brewing FlowPlanner golden migration — ACTIVE

#### S1.1 pure shadow base night planner — PASS

Source：`e76614ffc4382e247cf37d6c994172642b922a8c`

建立 pure `ClocktowerFlowPlanner`：

```text
ValidatedClocktowerRuleset
+ playerCount
+ inPlayRoleIds
        ↓
base night token plan
```

已验证：

- explicit script night override 优先；
- 无 override 时从 character night-order metadata 派生；
- dusk/dawn/system token；
- 5–6 人首夜不发 minion/demon info；
- 7+ 且对应阵营在场时保留邪恶信息；
- in-play filtering；
- off-script role fail closed；
- TB first/other-night canonical parity。

Production Host **尚未接入**。

验证：CI #123 success；R2 boundary #115 success。

#### S1.2 stable HostInteraction projection — PASS

Source：`5a628675e3f047d919908fbd7f14eaa31b788ac6`

建立：

- stable `ClocktowerHostInteraction`；
- character interaction registry；
- pure interaction projector；
- Fortune Teller red herring 作为 Fortune Teller handler 产生的 storyteller-setup interaction，而不是 script-specific FlowPlanner hardcode；
- `DynamicDecisionRequest` 与流程 interaction 继续保持职责分离。

Golden 已证明 TB legacy 的关键插入顺序：

```text
Empath
→ Fortune Teller red-herring setup
→ Fortune Teller action
```

Production Host **仍未切换**。

验证：CI #124 success；R2 boundary #116 success。

## 3. 当前开发断点：S1.3

### 3.1 为什么 S1.3 必须先做

重新审计 legacy night flow 后确认，official night-order metadata 中有些角色 token 只是**排序锚点**，不能解释为“角色在场就一定生成 interaction”。

典型例子：

- `Undertaker`：只有当天发生处决才需要 interaction；
- `Ravenkeeper`：只有实际夜死且死亡角色为 Ravenkeeper 才触发；
- `Mayor`：只有 Demon attack 将导致 Mayor 死亡、且保护/中毒等结算没有先改变结果时才进入 redirect resolution；
- `Scarlet Woman`：night token 不能简单投影为普通 wake/action；继任依赖 Demon death / alive-count 等已解析事实；
- Imp self-kill 后的 Demon successor 是 Imp interaction 后产生的临时 interaction，而不是普通角色夜序。

因此 FlowPlanner 不应自己重新计算这些完整规则。正确输入是**规则层已经解析好的 flow facts / pending events**。

### 3.2 S1.3 最小实现目标

下个会话从已经全绿的 S1.2 source baseline `5a628675...` 继续。

新增/完善概念：

```text
ClocktowerResolvedFlowFacts
CharacterInteractionHandler eligibility
before-role / after-role conditional interactions
pending-event interactions
```

目标行为至少覆盖：

```text
Imp normal action
→ optional Demon successor after Imp
→ optional Mayor death resolution
→ optional Ravenkeeper trigger
→ conditional Undertaker
```

要求：

1. 没有对应 fact 时，不得因为 role 在场而误生成 interaction；
2. fact 存在但对应角色不在场时，不得凭空生成；
3. condition ordering 与 legacy TB 行为一致；
4. FlowPlanner 消费 resolved facts，不重复实现 Demon attack/death/poison/protection correctness；
5. 仍保持 shadow-only，不修改 Compose Host 主流程。

### 3.3 当前仓库状态

截至本交接文档创建前：

- 最新已验证 source head：`5a628675e3f047d919908fbd7f14eaa31b788ac6`；
- S1.3 **尚无 source commit**；
- 之前准备过的 S1.3 思路/临时内容没有移动 feature branch；
- 下个会话应重新从当前 GitHub head 审计后实施，不假设任何未提交本地状态存在。

## 4. S1 后续与 S2 调整

### S1.4 — legacy ↔ planner shadow differential

S1.3 通过后，先建立真实 legacy/planner differential，而不是直接 production cutover。

至少比较：

- first night；
- other night；
- day transition；
- conditional/event-triggered interaction；
- 已提交 action/observation 的顺序与 stable identity。

只有 shadow/golden parity 通过后，才能考虑关闭 legacy `officialNightOrder()` / fixed flow source。

### S2 — No Greater Joy real second-script proof

S2 调整后的实施目标：

1. 增加 canonical `no_greater_joy.json` script asset；
2. 把 NGJ 独有角色 metadata / behavior binding 接入现有 central CharacterRegistry；
3. 使用同一 `ClocktowerScriptCatalog` / parser / normalizer；
4. 用**同一个** `ClocktowerFlowPlanner` 生成 NGJ first/other-night plan；
5. 为 `Clockmaker / Chambermaid / Sage / Klutz / Artist` 等补必要 handler，而不是修改 FlowPlanner script core；
6. 建立 TB + NGJ dual-script structural regression；
7. 证明 NGJ 接入不需要新的 script-specific Host UI 主流程分支。

S2 的强判据：

> 如果加入 No Greater Joy 只需要 script asset、角色 metadata/handler 和测试，而 FlowPlanner 主干不出现 `NoGreaterJoy` 分支，则 multi-script foundation 成立。

## 5. 哪些内容继续明确延后

### A3/A4 Possible Worlds

不要因为 NGJ 接入而顺手泛化 A3/A4。

当前 A3/A4 correctness/device boundary 仍可保持 Trouble Brewing-specific。允许出现：

```text
Trouble Brewing:
  Catalog ✅
  FlowPlanner ✅
  Host ✅/migration
  Possible Worlds ✅/shadow

No Greater Joy:
  Catalog ✅
  FlowPlanner ✅
  Host ✅/migration
  Possible Worlds deferred
```

### Persistence / Ruleset identity

当前 save 已经同时保存 legacy `currentClocktowerScript` 和新 `clocktowerRulesetRef`。不要在 S1/S2 提前拆这两套身份。

统一到 `ScriptId / RulesetRef / contentHash / handler compatibility identity` 仍放在 S4。

### Legacy `troubleBrewingRulesetRefFor(...)`

它仍服务当前 TB A3/A4 correctness boundary。不要为了“看起来通用”在 S1.3 期间提前改掉；等 dual-script catalog 已证明后再在 S4 收口。

## 6. GitHub 操作边界

继续执行现有 playbook：

```text
read-only audit
→ minimal atomic source commit
→ exact diff audit
→ normal PR CI
```

超大文件且 connector 无法安全构造完整内容时，才使用 temporary trusted writer。

本次重新审计**没有发现新的 GitHub Actions / connector 机制**，因此：

- `docs/github_connector_large_file_editing_playbook.md` 不需要新增规则；
- 不重复记录已经确认的 `pull_request` base-workflow 语义；
- 不制造新的 workflow；
- 不为 S1.3 增加新的 CI 类型，现有 Android + ASP + real Clingo + R2 structural verifier 继续作为正常 gate。

## 7. 下个会话启动清单

新会话中按以下顺序继续，不需要重新讨论总体路线：

```text
1. 读取 CURRENT_DEVELOPMENT_ROADMAP.md
2. 读取本交接文档
3. 确认 PR #2：open + Draft + base=main + unmerged
4. 确认 feature head，检查是否只有 docs-only handoff commit 位于 5a628675... 之后
5. 审计 S1.2 flow files + legacy conditional night-step eligibility
6. 实施 S1.3 tests first / contract first
7. 原子 source commit
8. exact diff audit
9. normal CI：Android + ASP + real Clingo + R2 verifier
10. S1.3 PASS 后开始 S1.4 legacy↔planner shadow differential
11. S1 完成后进入 S2，并直接使用真实 No Greater Joy
```

## 8. 不变量

- PR #2 不合并；
- R6 继续 BLOCKED；
- Production Host 尚未切换到新 FlowPlanner；
- A4 ZDD 继续 shadow/prototype，`ZDD_DEVICE_VALIDATED` 未授权；
- 不把 metadata JSON 扩展成规则 DSL；
- 不从 ability text 推断 handler；
- 不为了 NGJ 新建第二套 catalog/flow framework；
- 新剧本主要增加 asset + metadata + handler + tests，不修改 FlowPlanner script core。
