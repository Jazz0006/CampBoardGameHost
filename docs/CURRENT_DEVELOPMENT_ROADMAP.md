# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-23  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 最近已验证 live source baseline：`205473868b50e159977a8ad34e2cf239a711a79d`（PR #40 merge commit）  
> 当前开发重点：**Historical Action + Observation Capture 准备**  
> 当前交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`

> 新会话实施前仍必须重新查询 live `main`，不得把本文 SHA 当作永久 HEAD。

## 1. 当前状态

```text
Phase A correctness foundation                         PASS
R5.5 Script & Dynamic Flow Foundation                  CLOSED / MERGED
R6 P1 semantic prerequisites                           CLOSED
PR #28 Drunk/Poison mechanical correctness             CLOSED / MERGED
PR #24 Production Semantic-History Foundation          CLOSED / MERGED
PR #29 Impaired Information Semantics                  CLOSED / MERGED
PR #27 New-game Global Observation Ownership           CLOSED / MERGED
PR #39 Storyteller Information Decision Foundation     CLOSED / MERGED
PR #40 Structured Manual Storyteller UI — Empath       CLOSED / MERGED
PR #41 developer workflow + LF policy                  DRAFT / DOCS-INFRA ONLY
Next product source slice                              HISTORICAL ACTION + OBSERVATION CAPTURE
```

当前 live `main`：

```text
205473868b50e159977a8ad34e2cf239a711a79d
```

该 commit 是 PR #40 merge commit。

## 2. PR #39 — Storyteller Information Decision Foundation

PR #39 已合并，Foundation 现在提供统一 decision authority seam：

```text
Actual / registered state
  ↓
role-specific legal information builder
  ↓
impairment policy
  ↓
InformationDecisionContext
  ├ recommended candidate
  └ manual legal candidates
  ↓
Storyteller confirm
  ↓
shared validator
  ↓
EpistemicObservationDraft
  ↓
ClocktowerGameSession
```

关键 contract：

- recommendation 与 manual 是 peer inputs；
- recommendation 是 advice，不是 durable authority；
- manual 不是 free-text bypass；
- functioning information 只能确认 legal truthful/registered result；
- Drunk/Poisoned 可在 legal unreliable candidate space 中选择；
- hard block 与 soft warning 分离；
- Foundation 只产生 unbound draft；Global identity 仍由 session 分配；
- provenance 维持 `MANUAL` / `RECOMMENDATION_ACCEPTED`。

Foundation merge 后 live main 曾为：

```text
faad0e52dbbe55e1a7cc09c642318d0f6ef99342
```

## 3. PR #40 — Structured Manual Storyteller Information UI

PR #40 已于 2026-08-23 前完成全部 gate 并合并。

最终 feature head：

```text
4a083b45e1f0525ca49ff7d6968da7e6d373ca1e
```

merge commit：

```text
205473868b50e159977a8ad34e2cf239a711a79d
```

当前 production rollout 是**第一切片：Empath numeric information**，不是所有信息角色的完整 manual UI rollout。

已完成：

- structured number model / panel / adapter；
- Empath manual legal values；
- recommendation/manual 共用 Foundation validation；
- healthy no-recommendation fallback；
- prior shown value handling；
- later-night previousShownNumber；
- assisted recommendation 与 `step.displayOptions` 对齐；
- telemetry 只在 selector 实际产生 preview 时 commit；
- fallback display 不依赖 telemetry preview。

最终 gate：

```text
CI #439                          GREEN
  Android tests + debug APK      GREEN
  ASP contract tests             GREEN
  Real Clingo                    GREEN
R2 #382                          GREEN
fresh Codex review               CLEAN / 👍
all review threads               RESOLVED
```

## 4. 当前开发工作流决策

PR #40 的大文件最终修复，以及 PR #41 的后续验证，形成新的开发运行结论。

### 4.1 Remote writer 探索结果

曾实现 permanent `issue_comment` trusted patch writer，并通过静态 CI/安全 contract，但 pre-merge canary 无法端到端触发：该类 workflow 必须先存在于 default branch。

项目决定不先把尚未端到端验证的 write-enabled workflow 部署到 `main`。

状态：

```text
Permanent remote patch writer
  EXPLORED
  STATICALLY VALIDATED
  NOT END-TO-END VALIDATED
  NOT ADOPTED
```

### 4.2 正式采用的大文件流程

当 connector 无法可靠取得大文件完整内容时：

```text
ChatGPT
  -> 生成最小 patch + tests + Luna prompt
Codex Luna local worktree
  -> apply/check/test
  -> commit
  -> push feature branch
ChatGPT
  -> GitHub exact diff / CI / review / merge gate
```

规范：

- `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`
- `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`

Luna 只负责机械执行；不自行扩大修改范围，不 merge。

## 5. PR #41 当前定位

PR #41 最初用于 permanent remote writer 探索。经过端到端验证后已收缩为 **developer workflow / portability docs-infra PR**。

最终目标只保留：

- `.gitattributes` 跨平台 LF policy；
- ChatGPT ↔ Codex Luna 本地 patch 工作流规范；
- connector 工作流规范更新；
- roadmap / handoff / README 状态更新；
- remote writer 探索结论作为文档历史。

明确不进入最终 tree：

- permanent writer workflow；
- remote writer parser；
- remote writer runtime tests；
- production source behavior changes。

PR #41 不改变 R6 产品语义。

## 6. NEXT — Historical Action + Observation Capture

PR #39 和 #40 已建立“信息 decision → observation draft → session authority”的生产路径。下一产品 source slice 转向历史动作/观察捕获，为后续 multi-night exact baseline 提供完整输入。

目标方向：

```text
physical/game action
  ↓
semantic action capture
  ↓
information decision / observation capture
  ↓
Global semantic timeline
  ↓
historical reconstruction inputs
```

下一 slice 开始前必须重新审计当前生产事件类型、history persistence、Global sequence ownership 和已有 observation coverage，再决定最小 tests-first 边界。

### 明确 non-goals

不要在第一 Historical Capture PR 同时扩展：

- history UI redesign；
- misinformation tuning；
- Investigator 小人数平衡；
- broad evil-side win-rate tuning；
- Spy/Recluse registration rewrite；
- A3 multi-night solver implementation；
- B4 / ZDD production promotion；
- 全角色 structured manual UI rollout；
- ML / personalized tuning。

## 7. 更新后的 rollout 顺序

```text
1. Production Semantic-History Foundation                    DONE / #24
2. Impaired Information Semantics                            DONE / #29
3. New-game Global Observation Ownership                     DONE / #27
4. Storyteller Information Decision Foundation               DONE / #39
5. Structured Manual UI first production slice (Empath)      DONE / #40
6. Historical Action + Observation Capture                   NEXT
7. A3 historical multi-night exact baseline
8. Authoritative physical Grimoire ledger / Spy VerifiedExact
9. B4 historical expansion
10. Revision-driven recommendation/history unification
11. Broader structured manual role rollout as prioritized
12. Reconsider ZDD production promotion
```

## 8. 当前长期架构边界

### Registration 与 impairment 分层

```text
actual world
  ↓
registration projection
  ↓
truthful result / legal information space
  ↓
impairment policy
  ↓
storyteller decision
```

### Session authority

UI / recommendation / manual selector / history adapter 都不得自行分配 Global timeline identity。Global identity / sequence 仍由 `ClocktowerGameSession` authority 负责。

### Recommendation status

Recommendation 是候选建议；只有经过 Storyteller confirmation + shared validation 的结果才能进入 observation pipeline。

### A3 / A4 / B4

- A3 `EnumeratedWorldSet`：exact correctness baseline；
- A4 ZDD：exact shadow/prototype，未获 production promotion；
- B4：isolated shadow；
- historical multi-night authority 尚未授权扩展。

## 9. 开发与 CI 策略

Behavior-changing PR：

```text
query live main / live PR head
-> focused branch
-> tests-only RED commit
-> real CI RED evidence
-> smallest GREEN implementation
-> focused tests
-> full Android unit tests + debug APK
-> ASP
-> Real Clingo
-> R2
-> exact diff audit
-> final Codex review / threads
-> explicit user merge authorization
```

对于 connector 无法安全修改的大文件，GREEN implementation 默认交付为最小 patch，由 Codex Luna 在本地完整 worktree 中 apply/test/commit/push，然后 ChatGPT 从 GitHub 接回后半段。

## 10. 关键历史证据

```text
R5.5 merge                                   7add8569e2484a350f6cf1512a730e9f4db469c5
PR #28 Drunk/Poison correctness              241cb34a848833b27842d1233c37daabea244899
PR #24 Semantic-History Foundation           9c1996dfc6b615a12014fb11dbb5ca9a43064b99
PR #29 Impaired Information merge            b2c0b2c7a91290670d908292b3db5719d6bd6ddb
PR #27 Global Observation merge              5bbb607ae408d5d9d25812825200304054a7aced
PR #39 Decision Foundation main baseline      faad0e52dbbe55e1a7cc09c642318d0f6ef99342
PR #40 final head                            4a083b45e1f0525ca49ff7d6968da7e6d373ca1e
PR #40 merge / current validated main         205473868b50e159977a8ad34e2cf239a711a79d
```

## 11. 新会话启动顺序

1. 读 `docs/README.md`；
2. 读 `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
3. 读 `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
4. 读本 `CURRENT_DEVELOPMENT_ROADMAP.md`；
5. 读 `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`；
6. 查询 live `main` / open PR；
7. 不重复实现 permanent writer；
8. 下一 product source slice 从 Historical Action + Observation Capture 的 audit / RED contracts 开始。

## 12. 文档维护规则

- 本文件是当前执行点唯一权威；
- handoff 服务下一次开发；
- specialized design 维护语义边界，不维护 live branch 状态；
- 历史 handoff / audit 不得覆盖本文件；
- 开发运行规范以 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` 和 Luna patch 文档为准。
