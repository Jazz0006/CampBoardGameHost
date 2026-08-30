# CampBoardGameHost — TBSP-6 Production Cutover Handoff

> Updated: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign: **TBSP — Trouble Brewing Setup Preset Integration**  
> Status: **ACTIVE — TBSP-6G RED**  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR: **#57 — OPEN / DRAFT / NOT MERGED**  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`

## 1. Resume state

Live state immediately before this docs-only checkpoint was created:

```text
main:
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4

PR #57 code head before docs-only cleanup:
a26c221670fdea2612626f762d162b66091896af

last fully validated code checkpoint:
5c10cd29111449e1f8af2b8944609a2002048679

current code state:
TBSP-6G RED
```

Important distinction:

- `5c10cd29111449e1f8af2b8944609a2002048679` is the latest fully GREEN code/test checkpoint through TBSP-6F.
- `a26c221670fdea2612626f762d162b66091896af` adds the TBSP-6G RED contract.
- Any later commits produced solely by this documentation cleanup are **docs-only carriers on top of the RED code state**. A docs-only CI result must not be misreported as proving the 6G code GREEN.

Before any implementation in a new conversation, re-query live PR #57 head/state/checks and distinguish the latest docs-only head from these code checkpoints.

## 2. Completed TBSP ownership

### TBSP-1 — frozen asset / parser / validator — COMPLETE

Production asset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
preset count: 480
player counts: 5..15
```

Do not regenerate or reformat the final dataset.

### TBSP-2 — pure history-aware selector — COMPLETE

Accepted ownership includes:

```text
TroubleBrewingSetupPresetSelector
TroubleBrewingSetupPresetRotationScorer
TroubleBrewingSetupRotationHistory
```

The selector owns `selectedDrunkShownRole`; there is no downstream second Drunk draw.

Normative rotation policy remains:

```text
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md
```

### TBSP-3 — deterministic deal materialization — COMPLETE

Accepted contract:

```text
selected preset
+ game seed
+ ordered players
-> exact selected actual-role multiset
-> deterministic independent tb-seat-v1 assignment
-> actual Drunk remains Drunk
-> shown Drunk role equals selector-owned selection
```

Baron is not replayed as a second setup modifier.

### TBSP-4 — recommendation lock integration — COMPLETE

Accepted chain:

```text
selector-owned Drunk shown role
-> committed deal
-> TroubleBrewingSetupRecommendationLock
-> SetupCoordinationRequest.lockedDecisions
-> existing constrained recommendation
```

Later recommendation cannot replace the selected Drunk shown role. Compatible Investigator information remains generatable.

### TBSP-5 — durable cross-game rotation history — COMPLETE

Dedicated store:

```text
TroubleBrewingSetupRotationHistoryStore
```

Accepted semantics:

```text
stable gameId + same setup -> idempotent success
stable gameId + conflicting setup -> reject
newest-first
max five records per player count
dataset/schema/player-count isolation
malformed/unsupported history -> EMPTY
later valid completion can recover storage
```

Rotation history is not A3 immutable setup snapshot authority.

## 3. TBSP-6 completed production cutover slices

### TBSP-6A — active setup provenance codec — COMPLETE

`TroubleBrewingSetupProvenancePersistence` persists/reconstructs the exact selected preset provenance without selecting, shuffling or rerolling.

### TBSP-6B — production setup preparer — COMPLETE

Production orchestration has a typed owner that combines frozen dataset, history-aware selection and deterministic deal preparation.

### TBSP-6C — deal role resolver — COMPLETE

Exact actual/shown roles are resolved from the committed deal plan for production cards.

### TBSP-6D — Trouble Brewing production start cutover — COMPLETE

Trouble Brewing production start now follows:

```text
newClocktowerSeed()
-> load frozen final dataset
-> parse / validate dataset
-> load matching rotation history
-> prepare selected preset + Drunk shown role
-> resolve deterministic deal roles
-> commit PlayerCards
-> reset deal state with prepared seed
```

`startClocktowerGame()` branches Trouble Brewing before the legacy broad-random generator.

Therefore Trouble Brewing production start no longer uses:

```text
broad random role-composition generation
Baron post-generation mutation
independent random/later Drunk shown-role replacement
```

No Greater Joy retains the legacy setup-generation path in this campaign.

### TBSP-6E — active-game provenance persistence / restore — COMPLETE

Committed `TroubleBrewingSetupPresetSelection` is stored in active-game persistence and restored only by exact provenance decode.

Restore does not invoke selector/preparer.

Older supported snapshots without TBSP provenance restore with `committedTroubleBrewingSetupSelection == null`; they must not fabricate a preset selection from current cards.

### TBSP-6F — completed-game rotation-history production wiring — COMPLETE

Latest fully validated code checkpoint:

```text
5c10cd29111449e1f8af2b8944609a2002048679
```

Production completion behavior:

```text
not Clocktower -> no TB rotation write
not Trouble Brewing -> no TB rotation write
gameOutcome == null -> generic Restart/abandon does not record
missing committed provenance -> no fabricated history
true completed TB + committed provenance
    -> recordCompletedGame(clocktowerGameId, selection)
```

History persistence occurs before archive/save clearing. A failed durable write prevents the active save from being cleared so retry can converge.

Do not add another App-level completion-dedupe flag; store `gameId` semantics already own exactly-once behavior.

## 4. Current TBSP-6G RED

Code RED checkpoint:

```text
a26c221670fdea2612626f762d162b66091896af
```

Current focused test:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/session/
    TroubleBrewingSetupRecommendationPrewarmCoordinatorTest.kt
```

The RED defines a narrow typed coordinator contract:

```text
same committed SetupCoordinationRequest
-> build once
-> prewarm returns cached result
-> readyFor(same request) returns same result

changed committed request
-> readyFor misses
-> prewarm rebuilds for new request
-> old request is no longer ready
```

Expected production owner:

```text
TroubleBrewingSetupRecommendationPrewarmCoordinator
```

Do not broaden this RED into Compose lifecycle, First Night epistemic replay, A4/ZDD or persistence changes.

## 5. Revised TBSP-6 remaining sequence

The original broad phrase `background first-night/setup computation` is now deliberately split into separate engineering slices.

### 6G-A — setup recommendation prewarm core — CURRENT

Implement the minimum typed coordinator required by the existing RED.

Scope:

```text
SetupCoordinationRequest
-> SetupRecommendationService.ConstrainedResult
-> exact-request cache/reuse
```

Validation:

```text
exact T0 RED/GREEN with --rerun-tasks
-> :app:testFast at logical checkpoint
-> R2 / CI at checkpoint
```

Do not add production lifecycle wiring before the coordinator core is GREEN.

### 6G-B — identity-reveal production wiring

Create a separate RED proving:

```text
committed Trouble Brewing deal
-> enter PassPhone / RevealCard immediately
-> start setup recommendation prewarm off the main/UI thread during reveal
-> Judge / first consumer uses ready result only for the exact committed request
-> mismatched/stale request cannot be consumed as current
```

Required fallback:

```text
readyFor(current request) hit -> reuse
miss -> existing safe computation path
```

This slice protects non-blocking dealing and setup-result lifecycle only.

### 6H — First Night background precomputation

This is separate from `SetupRecommendationService` prewarming.

Create tests-first lifecycle coverage for expensive first-night information/world computation:

```text
RevealCard window begins
-> build exact committed-game first-night input
-> background computation
-> First Night begins
-> READY: consume
-> BUSY: safely await at point of use
-> MISS/stale: safe fallback/recompute
```

The background calculation must never:

```text
select another preset
reshuffle seats
reroll Drunk shown role
mutate actualRole
mutate shownRole
```

Do not broaden 6H into A3 immutable setup snapshot work or A4/ZDD redesign.

### 6I — cutover acceptance matrix

After 6G/6H behavior is GREEN, add/strengthen typed acceptance for the remaining production gaps:

```text
A. restore preserves exact preset/seed/cards/shown identities/seat mapping
B. restore never selects/materializes a second setup
C. legacy supported save without TB provenance never fabricates rotation history
D. invalid/rejected TB preset data never falls back to broad-random Trouble Brewing setup
E. navigation before Start does not commit a preset selection
F. recomposition/navigation cannot reroll an already-started setup
G. incomplete Restart/abandon/archive never enters rotation history
H. completed TB records the original initial selection exactly once
I. No Greater Joy setup behavior remains unchanged
J. identity reveal is entered without synchronously waiting for expensive setup/first-night computation
```

Prefer typed seams. Retain source-wiring tests only for coarse ownership gaps that cannot yet be exercised through typed production seams; retire brittle source-string checks when typed integration supersedes them.

### 6J — cleanup

Audit temporary/dormant cutover APIs after lifecycle ownership is established.

In particular, review any `preparedSetupPlan` or equivalent parameter that appears to be threaded through state-reset APIs but is not actually consumed. Either give it a real lifecycle owner or remove it; do not preserve misleading dormant parameters merely for historical shape.

### 6K — final TBSP acceptance

Final merge-gate sequence:

```text
all focused acceptance GREEN
-> :app:testFast
-> affected T2/T3 only where TESTING_STRATEGY requires
-> :app:testFull
-> R2
-> final GitHub CI
-> exact diff / scope audit
```

Do not mark PR #57 Ready and do not merge without explicit user authorization.

## 6. Production merge-blocking invariant status

Normative P1-P16 contract remains in `docs/CURRENT_DEVELOPMENT_ROADMAP.md` and `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`.

Current assessment:

```text
P1  preset-origin actual roles                         IMPLEMENTED
P2  no second Baron application                       IMPLEMENTED
P3  actual Drunk remains Drunk                        IMPLEMENTED
P4  Drunk shown role from selected preset options     IMPLEMENTED
P5  recommendation cannot replace Drunk shown role    IMPLEMENTED
P6  deterministic dataset/history/seed setup          IMPLEMENTED; final acceptance still required
P7  start selects/materializes once                    IMPLEMENTED; final acceptance still required
P8  recomposition cannot reroll started setup          NEEDS explicit acceptance
P9  navigation before Start does not commit selection  NEEDS explicit acceptance
P10 No Greater Joy unchanged                           NEEDS final regression acceptance
P11 restore does not select a new preset               IMPLEMENTED; strengthen end-to-end acceptance
P12 invalid TB data never falls back to broad random   NEEDS explicit typed acceptance
P13 identity dealing does not wait synchronously       IMPLEMENTED structurally; lock with 6G-B lifecycle test
P14 background work consumes committed deal only       OPEN — 6G/6H primary remaining risk
P15 only true completed TB enters rotation history     IMPLEMENTED
P16 retry-safe original-selection completion history   IMPLEMENTED
```

The largest remaining technical risk is now lifecycle correctness, not preset-selection mathematics.

Priority order:

```text
P14 async/reveal/first-night lifecycle
> P12 no-fallback acceptance
> restore/recomposition/navigation acceptance
> NGJ regression acceptance
```

Do not reopen TBSP-1 through TBSP-6F without concrete regression evidence.

## 7. Normative architecture that must not change silently

Required production model:

```text
frozen curated preset dataset
-> history-aware selector
-> selected preset + selector-owned Drunk shown role
-> deterministic deal materialization
-> commit actual/shown identity
-> immediate PassPhone / RevealCard
-> identity-reveal window performs background setup/first-night computation
-> relevant First Night consumer uses ready result or safely awaits/falls back at point of use
```

The expensive calculation must run off the UI/main thread and must not mutate committed identities.

Long-lived contract:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

`A4IdentityRevealPrewarmCoordinator` is an architectural precedent only, not the production TBSP implementation.

## 8. Testing cadence

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Default micro-cycle:

```text
RED
-> exact T0 RED
-> GREEN
-> exact T0 GREEN --rerun-tasks
-> git diff --check / exact remote diff audit
```

Use `:app:testFast` at logical checkpoints rather than after every micro-commit.

Run T4 `:app:testFull` once at final TBSP production acceptance unless a specific earlier risk justifies escalation.

A docs-only CI result on top of the current 6G RED code checkpoint is documentation evidence only; it is not code GREEN evidence.

## 9. Explicit out-of-scope boundary

Unless separately authorized, do not include:

```text
No Greater Joy behavior changes
A3 immutable setup snapshot implementation
A4/ZDD redesign
Mayor redirect work
Imp succession redesign
broad Host/App-root decomposition
recommendation weight retuning
preset dataset regeneration
```

Preserve accepted Dawn/Dusk poison exactly-once behavior.

## 10. New-conversation resume protocol

In the next conversation:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. read `docs/TESTING_STRATEGY.md`;
5. read `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`;
6. read `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md` only if selector/history semantics become relevant;
7. re-query live `main`, PR #57 head/state/checks and distinguish later docs-only head from code checkpoints;
8. verify current code still contains the 6G RED from `a26c221670fdea2612626f762d162b66091896af`;
9. continue tests-first from **TBSP-6G-A setup recommendation prewarm coordinator GREEN**;
10. do not merge or mark Ready.

Recommended new-chat instruction:

```text
请读取根目录 AGENTS.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md、docs/TESTING_STRATEGY.md 和 docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md。重新确认 live main、Draft PR #57 当前 head/state/checks，并区分 docs-only head、最后完整 GREEN code checkpoint 5c10cd29111449e1f8af2b8944609a2002048679 与 TBSP-6G RED code checkpoint a26c221670fdea2612626f762d162b66091896af。然后严格 tests-first 从 TBSP-6G-A setup recommendation prewarm coordinator GREEN 继续，不要扩大到 6G-B/6H、A3、A4/ZDD、No Greater Joy 行为变更，也不要 merge 或 mark Ready。
```
