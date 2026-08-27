# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-27  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Current priority: **SNE-7 — Authoritative Night Transaction Boundary; behavior RED first, production extraction only after contracts are locked**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state              CURRENT / SNE-7 CLOSEOUT
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

Do not resume App-root decomposition, A3 setup-snapshot work, A4/B4 authority promotion, or recommendation tuning until this same-night campaign is closed or explicitly paused by the user.

SNE-7 does **not** reopen A3. It is the current same-night correctness closeout slice.

## 2. Accepted same-night checkpoints

The campaign now has accepted production through:

```text
SNE-1..6A      effective mechanical death / consumer foundations
SNE-6B1       current-role projection foundation
SNE-6B2.1     pure Demon succession semantics
SNE-6B2.2     legality separated from recommendations
SNE-6B2.3     confirmed successor transaction/checkpoint
SNE-6B2.4     confirmed successor RoleChanged production projection
SNE-6B2.5A    Poisoner source lifetime follows current role
SNE-6B2.5B    roleActor resolves from effective current role
SNE-6B2.5C    Fortune Teller detects current/new Demon role
SNE-6B2.5D    Spy/Recluse registration follows current role without alive gating
SNE-6B2.6     exact confirmed successor materializes at Dawn; no draft/fallback
Mayor closeout shared legality + Host/UI wiring COMPLETE
```

Stage checkpoints:

```text
5a94c63536c04382f59963843c2ac10544962b02
  SNE-6B2.5 A–D
  focused GREEN
  :app:testFast GREEN
  R2 #673 SUCCESS
  CI #746 SUCCESS

51179ecca667d5450550375735ca49aae932c06d
  SNE-6B2.6 exact Dawn materialization
  focused GREEN
  :app:testFast GREEN
  R2 #675 SUCCESS
  CI #748 SUCCESS

2e8cb6a6a4763f9926956e5407d1c465e112e2bd
  Mayor Demon-exclusion Host/UI production wiring
  focused Mayor wiring + recommender GREEN
  :app:testFast GREEN reported locally
  exact remote diff audit accepted
```

The attempted 6C1 RED for Mayor redirect killing the Demon was reverted before any 6C production implementation. Revert commit:

```text
0d165250a5bc6a9dd6cd4edfc5d216663e99263e
```

Later branch heads may be documentation-only. Always distinguish the last validated production code checkpoint from later docs-only commits.

## 3. Current Mayor product decision — production complete

For the current Trouble Brewing automatic-host implementation:

```text
Mayor redirect target
→ MUST NOT be the current Demon
```

This is an explicit product/house-rule restriction, not official Blood on the Clocktower semantics.

Implemented architecture:

```text
shared Mayor redirect legality
→ exclude Demon
→ manual UI consumes legal target set directly
→ recommender only ranks inside the legal set
→ stale/restored confirmed Demon target fails closed
```

Production now includes:

- `clocktower/rules/MayorRedirectLegality.kt` — shared Demon exclusion;
- `MayorRedirectRecommender.kt` — Demon candidates excluded and direct Demon outcome rejected;
- `ClocktowerHostScreen.kt` / `ClocktowerNightStepUi.kt` — Host/UI legal-target wiring;
- focused rule/recommender/source coverage from the Mayor closeout.

The Mayor production slice is no longer the immediate remaining action. The next work is SNE-7.

Detailed decision record:

```text
docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md
```

## 4. Deferred generic non-self Demon death

The Mayor restriction narrows the current Trouble Brewing product; it does **not** remove the long-term generic rule requirement.

Before arbitrary custom/dynamic-script combinations are declared fully supported, reopen:

```text
actual Demon dies from any cause
+ functioning Scarlet Woman
+ 5+ alive immediately before death
→ immediate same-night RoleChanged to the dead Demon type
```

Possible future sources include Assassin, Godfather, Gossip, Pit-Hag, Fang Gu transfer flows, and future/custom night-death effects.

Do not silently treat the Mayor restriction as a general official-rules solution.

## 5. Protected same-night architecture

```text
public/persisted base state
+ confirmed same-night mechanical facts
+ stable canonical interaction plan
+ checkpoint.nightStepIndex
→ derived current interaction / cursor
→ ClocktowerEffectiveNightState
→ actor eligibility
→ ability functioning
→ persistent-effect lifetime
→ target legality
→ information truth
→ triggers
→ current role
```

Hard contracts:

- mechanical death and public death announcement remain distinct;
- never write `eliminatedRound` early merely to make later-night logic work;
- stable seat/interaction identity never comes from re-indexing filtered views;
- draft UI state is never mechanical authority;
- same-night `RoleChanged` is projected before Dawn materialization;
- persistent effects follow source ability lifetime;
- death-trigger/even-if-dead exceptions are explicit;
- one canonical interaction plan prevents a newly-created Demon from receiving a second normal Demon action;
- game outcome is not evaluated mid-transaction while mandatory succession remains unresolved;
- recommendation ranking is downstream of rules legality;
- `ClocktowerNightCheckpoint` remains the sole durable night UI/transaction checkpoint state;
- `checkpoint.nightStepIndex` is the sole stored navigation position; no second `interactionIndex` authority;
- `MovePrevious` / `MoveNext` navigation alone never invalidates confirmed mechanical facts;
- editing a draft alone never invalidates confirmed mechanical facts;
- dependent invalidation occurs only when an upstream confirmed fact is reconfirmed to a changed authoritative value;
- `NightResolutionEvent`, if introduced, is transient command input, not a durable event log;
- night resolution may produce `DawnCommitIntent`, but existing `ClocktowerGameSession` / App transaction boundaries retain sequence, timeline, and durable commit authority.

The accepted SNE-7 design record is:

```text
docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md
```

## 6. Immediate completion sequence — SNE-7

The immediate next slice is **tests/contracts only**. Do not begin with production refactoring.

```text
SNE-7.1 behavior RED
  lock real cross-layer transaction failures
  no production-code change yet

→ SNE-7.2 extract NightCheckpointReducer
  draft / confirm / navigation / invalidation

→ SNE-7.3 extract NightDawnResolutionPlanner
  validated resolution + DawnCommitIntent

→ SNE-7.4 production typed seams
  Compose/App wiring consumes reducer/projector/planner

→ SNE-7.5 restore / process-death reconstruction matrix

→ SNE-7.6 2–4 Compose smoke/integration tests

→ SNE-7.7 delete source-string assertions only after real behavioral replacement is GREEN

→ SNE-7.8 retain only minimal architecture guards, preferring types/APIs over source inspection

→ logical checkpoint :app:testFast + triggered T2/T3
→ latest production-head GitHub CI/R2
→ exact campaign audit
→ PR remains draft until explicit user authorization
```

The SNE-7 behavior/restore matrix should prove at least:

- before successor confirmation restore → still Minion;
- after confirmation restore → successor effective role Demon;
- confirmed then Previous → confirmed mechanics remain;
- draft edit without Next/Confirm → confirmed fact unchanged;
- upstream reconfirm to a changed fact invalidates stale dependent confirmations;
- Poisoner→Demon ends poison while raw confirmed target may remain in checkpoint;
- new Demon Fortune Teller result → Yes;
- old dead Demon Fortune Teller result → Yes;
- exactly one normal Demon action;
- Monk-protected self-kill → no succession;
- healthy Scarlet Woman at 5+ self-kill path mandatory;
- poisoned Scarlet Woman at 5+ ordinary Minion choice;
- legacy draft-only restore invents no confirmation or `RoleChanged`;
- invalid confirmed successor fails closed / requires valid reconfirmation;
- missing interaction and out-of-range `nightStepIndex` restore safely;
- same persisted base + same confirmed checkpoint + same canonical plan/ruleset → identical reconstructed effective state;
- Mayor redirect legal target set excludes Demon and invalid restored Demon redirect fails closed;
- current effective role can differ from public/base role and reconstruct correctly.

Generic non-self Demon death / Scarlet Woman succession is excluded from this final Trouble Brewing matrix because of the explicit Mayor product restriction and remains deferred.

## 7. SNE-7 responsibility boundaries

Preferred seams:

```text
NightCheckpointReducer
  draft edit
  confirmation
  navigation
  dependent-fact invalidation

ClocktowerEffectiveNightStateProjector
  existing same-night derived mechanical state authority

NightDawnResolutionPlanner
  validated deaths
  succession consequences
  poison carry
  outcome gate
  DawnCommitIntent

ClocktowerGameSession / App transaction boundary
  durable sequence/timeline/role/death/history/phase commit
```

A thin facade may compose these seams, but must not become a second rules engine or persistence authority.

Use existing repository domain types such as `GameState` / `GameSnapshot`; do not introduce an overlapping `ClocktowerGameState` model.

Restore authority remains:

```text
GameState
+ decoded ClocktowerNightCheckpoint
+ reconstructed ruleset/canonical plan
→ derived effective night state
```

Do not introduce event sourcing by persisting UI commands.

## 8. Testing migration policy

Do **not** delete all source-string tests up front.

Migration rule:

```text
add behavior test and prove RED
→ implement typed production seam and prove GREEN
→ identify source assertion now fully superseded
→ delete only that covered assertion/test
→ retain only narrow architecture guards that cannot yet be expressed by types/APIs
```

High-value pure rules tests remain. Compose/UI tests should be few and prove wiring, not re-test every rule.

Avoid broad source bans such as forbidding `eliminatedRound` across an entire file. Prefer type/API constraints that force the specific consumer to receive effective-state data.

## 9. Development workflow authority

Current collaboration/testing rules are explicit in:

```text
AGENTS.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
docs/TESTING_STRATEGY.md
```

Operational cadence:

```text
micro-slice → T0 RED/GREEN only
related slices → remote diff audit, no old-head CI wait
logical checkpoint → T1 + triggered T2/T3
latest checkpoint head → GitHub CI/R2
merge → full required gate + explicit user authorization
```

Do not duplicate an exact Luna-passed focused test merely for a second local copy of the same evidence.

Known workflow/test pitfalls and corrections are recorded in:

```text
docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md
```

## 10. Deferred work after same-night correctness

After this campaign closes, re-audit priorities rather than automatically resuming old work.

Known deferred candidates:

1. App-root S9.2 Active Game Persistence Boundary;
2. A3 immutable setup-snapshot ownership/persistence;
3. broader dynamic/custom-script generic Demon-death succession;
4. production recommendation-authority promotion only if explicitly authorized.

Historical handoffs remain evidence, not current execution authority.

## 11. Startup order for the next conversation

Read in this order:

1. root `AGENTS.md`;
2. this `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md`;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`;
6. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
7. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
8. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
9. `docs/TESTING_STRATEGY.md`;
10. query live GitHub main / branch / PR #54 / checks before editing.

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
