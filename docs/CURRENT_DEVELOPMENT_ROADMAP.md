# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-25
> 文档角色：**CURRENT / 当前状态唯一权威**
> Repository: `Jazz0006/CampBoardGameHost`
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`
> Current priority: **same-night effective mechanical state correctness — tests-first**
> S9.2: **architecture audit complete / implementation deferred until same-night correctness is resolved**

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
PR #53 Information Decision authority hotfix       CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root S9.1 JSON primitives                      CLOSED / MERGED
Same-night effective mechanical state correctness  CURRENT / RED NEXT
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
A3 setup-snapshot ownership / persistence          DEFERRED / NOT STARTED
Production recommendation authority promotion      NOT STARTED / NOT AUTHORIZED
```

PR #53 is merged into stable `main` at `c8985cb4991f6c7e5ea02adedb932d2d86452da1`. The Information Decision production boundary now preserves confirmation provenance and no longer treats a naked information draft as durable authority.

Current `CampBoardGameHostApp.kt` size after S9.1 remains approximately:

```text
325,556 bytes original
-> 205,456 bytes after S9.1
```

The 50 KiB target is a maintainability guideline, not a hard requirement. Do not resume decomposition while the current correctness blocker is active.

## 2. Current correctness priority — same-night effective mechanical state

A high-impact Clocktower rules defect exists in the boundary between **confirmed night actions** and the **mechanical state consumed by later interactions in that same night**.

Production already resolves much of a Demon attack correctly:

```text
confirmed Demon target
+ Demon functioning/poison state
+ Monk protection
+ Soldier immunity
+ Mayor redirect
-> resolvedNightDeathName
-> nightDeathWillOccur
-> Ravenkeeper / Sage trigger facts
```

However, later consumers still frequently derive life/ability state from:

```text
PlayerCard.eliminatedRound == null
```

Night deaths are intentionally written to that public/persisted field only at dawn/finalization. Therefore the App can know that a player is mechanically dead while later roles still consume a stale alive state.

The defect is broader than one Empath calculation. It can affect:

- whether a later ordinary role acts at all;
- Empath living-neighbour selection and truth value;
- Chambermaid target legality;
- persistent effect lifetime, including Poisoner dying after poisoning someone;
- ability functioning for later roles;
- death-trigger exceptions such as Ravenkeeper / Sage;
- role changes / Imp succession;
- information-decision truth inputs;
- deterministic restore/recomposition.

Required architectural boundary:

```text
public/persisted base state
+ confirmed same-night mechanical facts
+ stable night interaction plan
+ current cursor
        ↓
EffectiveNightState authority/projector
        ↓
actor eligibility
ability functioning
active persistent effects
target legality
information evaluator input
conditional/death-trigger eligibility
current role
```

Mechanical death must remain separate from public death announcement. Do **not** write `eliminatedRound` early merely to repair later-night calculations.

Detailed external-project research, official-rule semantics and long-term dynamic-script/character architecture:

```text
docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md
```

Current implementation handoff:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_SAME_NIGHT_EFFECTIVE_STATE_CORRECTNESS.md
```

## 3. Dynamic script / character requirement

The current fix must not hard-code Trouble Brewing-only special cases that block later dynamic scripts or roles.

Accepted direction:

```text
CharacterDefinition
  metadata
  night order
  generic action contract

CharacterRuleHandler
  normal action eligibility
  legal targets
  mechanical resolution
  information legality/truth
  triggers/effects
```

A dynamic script assembled from already-supported role IDs should require no new behavior code.

For custom/homebrew roles:

```text
metadata-only
-> generic action-contract UI
-> executable registered rule handler
-> optional future declarative rule DSL
```

Unknown custom mechanics must fail safely to assisted/manual Storyteller handling. Never infer authoritative game mechanics from free-text ability prose at runtime.

External implementations are architecture references only. Official Blood on the Clocktower rules remain the gameplay behavior oracle.

## 4. Approved immediate sequence

```text
docs-only architecture/research checkpoint
-> Luna/local: build focused same-night RED
-> prove Imp -> Empath downstream failure
-> prove Poisoner source-death effect-lifetime failure
-> prove protection/immunity no-death controls
-> prove Ravenkeeper death-trigger exception
-> prove stable night cursor/no reindex
-> review RED checkpoint
-> minimal pure EffectiveNightState GREEN
-> wire Host/flow/information consumers to that authority
-> focused target-contract fixes: Fortune Teller dead target, Butler dead Master
-> T1 :app:testFast
-> affected T2 + assembleDebug
-> triggered T3 if required by TESTING_STRATEGY
-> PR T4 applicable gate
-> exact diff / architecture audit
-> merge only with explicit authorization
-> then reconsider S9.2
```

Do not mix this work with persistence extraction, decomposition, A3/A4/B4 or recommendation tuning.

## 5. Information Decision authority — closed hotfix, protected contract

PR #53 fixed the previous production authority defect. Preserve these contracts while building same-night state:

```text
legal candidate generation
-> InformationDecisionContext
-> MANUAL or RECOMMENDATION_ACCEPTED confirmation
-> ConfirmedInformationDecision
-> authorized display/durable observation
```

Hard semantic blocks remain:

```text
STALE_CONTEXT
ILLEGAL_CANDIDATE
NOT_RECOMMENDED
```

Do not add `InformationDecisionSource.AUTO`.

The new same-night work adds an upstream requirement: the information candidate/context truth space must be built from the **effective mechanical state at the current interaction cursor**, not from a pre-death/public-only snapshot.

The completed hotfix design remains in:

```text
docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md
```

The old Information Decision bug handoff is historical after PR #53 and is no longer the current task.

## 6. S9.2 — deferred Active Game Persistence Boundary

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

After the current correctness campaign and S9.2, re-audit Root. If no natural >=15–20 KiB low-coupling responsibility remains, **END App-root decomposition**.

## 7. Deferred A3 setup-snapshot work

A3/B4 historical hardening H1–H7 is complete and PR #48 is merged. The remaining architectural blocker is explicit immutable setup-snapshot ownership/persistence before any future production authority promotion.

This remains intentionally separate from the current bug and S9.2.

Do not promote B4/A4/recommendation authority or broaden historical-exact script support while fixing same-night mechanics.

Deferred A3 detail is kept in:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md
```

## 8. Protected architecture contracts

### Same-night mechanics

- mechanical state is time/cursor-relative;
- mechanical death and public death knowledge are distinct;
- confirmed action deltas and persistent state are distinct concepts;
- one rules-owned effective-state authority feeds later flow, targets and information;
- stable night interaction identity/order must not be reindexed because someone dies;
- persistent effects require explicit source/lifetime semantics;
- death-trigger / `even if dead` exceptions remain role-defined;
- restore/recomposition of the same confirmed facts/cursor must reproduce the same effective state;
- dynamic scripts use role IDs/metadata rather than script-name branches;
- unsupported custom-role mechanics fail closed to manual/assisted behavior.

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

`ClocktowerHostScreen.kt` remains under new-responsibility growth freeze. Same-night changes there must be narrow wiring to a rules/session-owned state authority, not new policy.

## 9. Validation workflow

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

For Luna/local Android work:

```bash
export GRADLE_USER_HOME="$PWD/.gradle-codex"
```

The next checkpoint is RED-only; do not jump directly to production GREEN.

## 10. Documentation authority / startup order

For a new development conversation:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. this `CURRENT_DEVELOPMENT_ROADMAP.md`;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_SAME_NIGHT_EFFECTIVE_STATE_CORRECTNESS.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`;
6. `docs/TESTING_STRATEGY.md`;
7. query live GitHub state before editing.

Historical handoffs/closeout documents are evidence, not current authority.

## 11. Working discipline

1. ChatGPT owns architecture/boundary judgment, RED design, exact-diff criteria and remote audit.
2. Luna/local Codex performs large-file mechanical edits in the user's full local worktree when required.
3. Keep tests-first RED/GREEN provenance explicit.
4. Do not broaden scope to opportunistic cleanup during correctness fixes.
5. Do not merge, mark ready, rebase or force-push without explicit user authorization.
