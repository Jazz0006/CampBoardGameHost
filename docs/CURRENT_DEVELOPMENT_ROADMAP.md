# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-25
> 文档角色：**CURRENT / 当前状态唯一权威**
> Repository: `Jazz0006/CampBoardGameHost`
> Stable `main`: `5367603d2d7150e7ba88f19d061eb04f8da20aeb`
> Current priority: **fix Information Decision correctness bug tests-first**
> S9.2: **architecture audit complete / implementation deferred until bug fix merges**

## 1. Current project state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
PR #39 Information Decision Foundation             CLOSED / MERGED
PR #40 Structured Manual UI                        CLOSED / MERGED
PR #42 Historical Action + Observation Capture     CLOSED / MERGED
PR #44 Drunk / Fortune Teller correctness hotfix   CLOSED / MERGED
PR #43 Clocktower Host decomposition A1–A13        CLOSED / MERGED
PR #48 historical multi-night exact baseline       CLOSED / MERGED
PR #49 App-root S7.1/S7.2                          CLOSED / MERGED
PR #50 test execution tiers + path-aware CI        CLOSED / MERGED
PR #51 App-root S7.3–S9.1                          CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root S9.1 JSON primitives                      CLOSED / MERGED
Information-decision correctness bug               CURRENT / NEXT IMPLEMENTATION
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
A3 setup-snapshot ownership / persistence          DEFERRED / NOT STARTED
Production recommendation authority promotion      NOT STARTED / NOT AUTHORIZED
```

PR #51 integrated the audited App-root stack through S9.1 with full Android unit tests + debug APK build and the CI gate GREEN.

Current `CampBoardGameHostApp.kt` size after S9.1 is approximately:

```text
325,556 bytes original
-> 205,456 bytes after S9.1
```

The 50 KiB target is a maintainability guideline, not a hard requirement. Do not resume 1–5 KiB micro-slices. Any further App-root decomposition must expose a genuine coherent responsibility of roughly >=15–20 KiB, preferably >=20–30 KiB.

## 2. Current correctness priority — Information Decision authority bug

A newly confirmed correctness defect exists in the Clocktower automatic-information path.

The intended authority is:

```text
legal candidate generation
-> InformationDecisionContext
-> MANUAL or RECOMMENDATION_ACCEPTED confirmation
-> ConfirmedInformationDecision
-> observation draft
-> display / durable session observation
```

The production Host currently has a weaker final seam: when `ClocktowerNightStepUi.informationDecisionDraft` is non-null, `recordReliablePrivateInformation(...)` can directly call the durable observation callback.

That bare draft does not, by itself, prove at commit time that it still belongs to the current immutable decision context/revision or that recommendation/legal-candidate membership was revalidated.

This matters because automatic recommendations may be generated under one state/input snapshot and survive until display after state/input changes or recomposition. Candidate IDs must remain bound to the immutable decision snapshot that produced the confirmation; an old candidate/draft must never be treated as valid merely because the same textual candidate ID still exists.

Required invariant:

```text
automatic recommendation
-> InformationDecisionContext.confirm(
       source = RECOMMENDATION_ACCEPTED,
       currentRevision = current revision
   )
-> Allowed confirmation only
-> durable observation
```

Hard blocks already defined by the semantic authority must remain effective in production:

```text
STALE_CONTEXT
ILLEGAL_CANDIDATE
NOT_RECOMMENDED
```

Do **not** add a new `InformationDecisionSource.AUTO`; automatic mode still means semantic `RECOMMENDATION_ACCEPTED`.

Detailed bug diagnosis, RED plan, scope and STOP gates:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_INFORMATION_DECISION_CORRECTNESS_BUG.md
```

Specialized semantic design authority:

```text
docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md
```

## 3. Approved immediate sequence

```text
current docs cleanup
-> create dedicated information-decision hotfix branch from live main
-> RED: automatic path cannot bypass stale/legal/recommendation confirmation
-> minimal GREEN
-> T1 :app:testFast
-> affected T2 + assembleDebug where applicable
-> PR full applicable gate
-> exact diff audit
-> merge correctness fix
-> then reconsider S9.2
```

Do not mix the correctness bug with persistence extraction or size-driven Host decomposition.

## 4. S9.2 — deferred Active Game Persistence Boundary

The post-S9.1 architecture audit concluded that persistence is the only remaining Root responsibility currently large/coherent enough to justify another structural slice.

Planning estimate only:

```text
raw persistence-related surface        ~45–55 KiB
safe expected Root reduction           ~25–35 KiB
```

Recommended seam:

```text
untrusted persisted JSON
-> completely validated immutable restore state
-> Root live Compose commit
```

Likely owners:

```text
persistence/ActiveGameSnapshotCodec.kt
persistence/ActiveGameRestoreParser.kt
optional coherent AppPreferencesStore / GameHistoryStore
```

Root retains persistence timing, Compose state capture/mutation, lifecycle effects, restore transaction commit, archive timing and A4 durability timing.

Detailed architecture, RED tests, allowlist and abort gates:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md
```

After S9.2, re-audit Root. If no natural >=15–20 KiB low-coupling responsibility remains, **END App-root decomposition**.

## 5. Deferred A3 setup-snapshot work

A3/B4 historical hardening H1–H7 is complete and PR #48 is merged. The remaining architectural blocker is explicit immutable setup-snapshot ownership/persistence before any future production authority promotion.

This remains intentionally separate from the current bug and S9.2.

Do not promote B4/A4/recommendation authority or broaden historical-exact script support merely while fixing the current information-decision bug.

Deferred A3 detail is kept in:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md
```

until that deferred task is either resumed or consolidated into a newer design handoff.

## 6. Protected architecture contracts

### Information decision / observation

- legal information space is rules-owned, not recommendation-owned;
- Drunk/Poison policy and registration semantics remain separate;
- manual and recommendation acceptance share one `InformationDecisionContext` authority;
- generated/recommended candidates are not durable facts;
- durable observations require a valid confirmation/display boundary;
- current revision and candidate snapshot identity must be respected;
- GLOBAL observation/history ordering remains unchanged.

### Historical exact / A3-B4

- durable observations are consumed exactly once by GLOBAL historical replay;
- setup-only knowledge constrains initial worlds only;
- mechanically identical possible worlds converge despite hidden provenance differences;
- exact historical construction remains Trouble Brewing-only and fails closed outside that boundary;
- `rolesBySeat` is immutable setup identity; `currentRolesBySeat` is dynamic historical role state;
- Storyteller-hidden targets never become player knowledge constraints.

### App-root / Host

Do not move merely for byte reduction:

- `CampBoardGameHostApp()` orchestration/state ownership;
- Compose effect lifetime;
- persistence/restore/archive timing;
- Clocktower live transaction ordering;
- `ClocktowerGameSession` authority;
- action/observation/history ordering;
- A4 lifecycle/cache/prewarm/rebuild;
- planner/projector/materializer authority;
- Host recommendation/registration transaction semantics.

`ClocktowerHostScreen.kt` remains under new-responsibility growth freeze.

## 7. Validation workflow

`docs/TESTING_STRATEGY.md` is authoritative.

Default escalation:

```text
T0 focused RED/GREEN
-> T1 :app:testFast
-> T2 affected regression / compile checks
-> triggered T3 only when semantic area requires
-> PR T4 applicable FULL gate
```

`UP-TO-DATE` / `FROM-CACHE` alone is not proof that required tests executed.

## 8. Documentation authority / startup order

For a new development conversation:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. this `CURRENT_DEVELOPMENT_ROADMAP.md`;
4. the one handoff for the active task;
5. `docs/TESTING_STRATEGY.md`;
6. only then read specialized design/reference docs required by that task;
7. query live GitHub state before editing.

Historical handoffs/closeout documents are not current authority and should not be recreated for every completed micro-step. Git history and merged PRs are the historical record.

## 9. Working discipline

1. ChatGPT owns architecture/boundary judgment, RED design, exact-diff criteria and remote audit.
2. Luna/local Codex performs large-file mechanical edits in the user's full local worktree when required.
3. Keep tests-first RED/GREEN provenance explicit.
4. Do not broaden scope to opportunistic cleanup during correctness fixes.
5. Do not merge, mark ready, rebase or force-push without explicit user authorization.
