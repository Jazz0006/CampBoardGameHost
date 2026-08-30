# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline:
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4

main meaning:
PR #55 merged — Dawn poison exactly-once materialization
PR #56 merged — next-night / Dusk poison expiry exactly-once materialization

current branch:
codex/trouble-brewing-setup-presets-v2

current Draft PR / CI carrier:
PR #57 — TBSP: integrate Trouble Brewing setup presets
OPEN / DRAFT / NOT MERGED

last fully validated TBSP-2 checkpoint:
80a5b9306009a4d078623b997b5b42a88de21080

last fully validated TBSP-3 checkpoint:
8e918f69f6184a6389a23881af42127a3d761ef2

latest fully validated TBSP-4 code/test checkpoint:
f68d8326de6bf57ecfd632fef73689c4900f87a9

current campaign:
TBSP — Trouble Brewing Setup Preset Integration

current completed-slice handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_4_RECOMMENDATION_LOCK.md

campaign design baseline:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md

normative TBSP-2 rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

Current TBSP implementation status:

```text
TBSP-0 documentation / campaign plan: COMPLETE
TBSP-1A final dataset asset + typed parser: COMPLETE
TBSP-1B semantic validator: COMPLETE
TBSP-2 pure history-aware selector: COMPLETE
TBSP-3 pure deal materialization: COMPLETE
TBSP-4 setup recommendation lock integration: COMPLETE
TBSP-5 durable cross-game rotation-history storage: NOT STARTED
TBSP-6 production cutover / restore ownership: NOT STARTED
A3 immutable setup snapshot: DEFERRED UNTIL TBSP PRODUCTION ACCEPTANCE
```

At TBSP-4 checkpoint `f68d8326de6bf57ecfd632fef73689c4900f87a9`:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
full Android unit/build step: correctly skipped by normal FAST routing
ASP contract tests: correctly skipped
Real Clingo cross-validation: correctly skipped
```

No local developer-selected T0 was executed from this Chat runtime. A direct temporary Git checkout was attempted after direct Gradle execution was authorized, but the container could not resolve `github.com`; actual Gradle evidence therefore remains GitHub Actions `:app:testFast`. Do not rewrite that evidence as a local T0.

The poison exactly-once hotfixes remain part of the required TBSP baseline and must be preserved.

## 2. Accepted predecessor correctness baseline

### PR #54 — same-night correctness / GCR hardening

PR #54 is merged and closed. Its accepted correctness remains part of the baseline and must not be reopened merely because TBSP touches initial setup.

Important accepted contracts include:

- First Night Fortune Teller uses base/current-role authority rather than entering Other Night chronology projection.
- Other Night Fortune Teller continues to use canonical same-night effective-state projection.
- current living-Demon UI authority remains distinct from transient current-night reconstruction authority during pending Imp succession.
- old Imp mechanical death, pending successor identity and canonical ordering remain separate concerns.
- poisoned Spy uses the accepted fail-safe product policy: wake normally, but no fabricated Grimoire is produced and no false Grimoire observation is persisted.
- gameplay semantics remain primarily protected by typed tests rather than brittle source-string assertions.

### PR #55 — Dawn poison exactly-once

Merged into `main` at:

```text
160f730594d76c294542cd22a5220baeb73d1bc9
```

It established exactly-once Dawn poison materialization and retry convergence across ordinary and successor-Dawn paths.

### PR #56 — next-night / Dusk poison expiry exactly-once

Merged into `main` at:

```text
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4
```

It established typed Dusk poison-expiry ownership, stable history identity, restore/retry convergence, and completion before Night phase/round become durable.

TBSP must preserve both PR #55 and PR #56 behavior.

## 3. Current priority — TBSP Trouble Brewing Setup Preset Integration

Goal:

Replace broad random Trouble Brewing role-composition generation with selection from the final curated Trouble Brewing preset dataset while preserving:

- rules legality as an independent authority;
- deterministic/reproducible setup materialization;
- independent seat shuffling;
- one Drunk shown-role authority;
- existing setup recommendation behavior for remaining downstream information decisions;
- immediate identity dealing without synchronously waiting for expensive first-night/setup calculation;
- No Greater Joy current behavior;
- safe cross-game rotation without conflating it with A3 historical setup provenance.

Final external dataset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
status: final_ready_for_program_integration
player counts: 5..15
preset count: 480
pool sizes: 30,30,50,50,50,50,50,50,40,40,40
Drunk presets: 208
Drunk options per Drunk preset: exactly 3
Git blob: a935474bec07577eb9e753bad2135a604add63f5
```

Do not reformat or regenerate this dataset during later integration.

## 4. TBSP-1 accepted checkpoint — COMPLETE

### TBSP-1A — final asset and parser

Accepted ownership:

```text
final dataset asset
TroubleBrewingSetupPresetModels
TroubleBrewingSetupPresetJson
focused parser contract test
```

Executable contract includes:

```text
schema_version == 2
dataset_id == trouble_brewing_setup_presets_v2_final
status == final_ready_for_program_integration
pools 5..15 all exist
pool sizes match final dataset
total presets == 480
```

Dataset character IDs remain in external lowercase representation until canonical resolution is explicitly required.

### TBSP-1B — semantic validator

Accepted coverage includes:

```text
unique preset IDs
preset.playerCount matches owning pool
total actual roles == player count
exactly one Demon == Imp
no duplicate actual role
all IDs resolve through canonical Trouble Brewing registry
role category matches registry ownership
standard composition unless Baron
Baron outsider +2 / townsfolk -2 represented exactly once
5–6 curated defaults contain no Baron
Drunk absent -> empty drunk_as_options
Drunk present -> exactly three unique absent Townsfolk options
208 Drunk presets / 624 option slots / 208 unique option triples
```

TBSP-1 did not wire production setup.

## 5. TBSP-2 pure history-aware selector — COMPLETE

Accepted ownership includes:

```text
TroubleBrewingSetupPresetSelector.kt
TroubleBrewingSetupPresetRotationScorer.kt
TroubleBrewingSetupRotationHistory.kt
TroubleBrewingSetupPresetSelectorTest.kt
TroubleBrewingSetupPresetRotationScorerTest.kt
```

Accepted slices:

```text
TBSP-2A player-count isolation + provenance
TBSP-2B deterministic preset and Drunk-option replay
TBSP-2C exact previous real non-Demon composition rejection
TBSP-2D player-count-specific last-game overlap threshold
TBSP-2E five-game history decay
TBSP-2F soft rotation weighting + minimum weight floor
TBSP-2G +0.05 fallback to first non-empty level while exact repeat remains hard
```

Normative rotation values from `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`:

```text
history weights:
1.00, 0.65, 0.40, 0.20, 0.10

baseNoveltyWeight:
max(0.20, 1.0 - weightedOverlap)

soft multipliers:
same immediately-previous Minion set  × 0.70
primary style seen >=2 of previous 5   × 0.88
same consecutive Drunk shown role     × 0.40

final weight floor:
0.05

fallback:
+0.05 last-game overlap threshold repeatedly
stop at first non-empty eligible pool
exact-repeat composition never becomes eligible
maximum threshold 1.0
```

Deterministic namespaces include:

```text
tb-preset-v1
tb-drunk-v1
```

The candidate Drunk shown role used for soft weighting is the same selector-owned disguise carried forward; there is no second Drunk draw.

Validated checkpoint:

```text
80a5b9306009a4d078623b997b5b42a88de21080
```

## 6. TBSP-3 pure deal materialization — COMPLETE

Source ownership:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlanner.kt

app/src/test/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlannerTest.kt
```

Planner input/output authority:

```text
TroubleBrewingSetupPresetSelection
+ orderedPlayerNames
-> TroubleBrewingSetupDealPlan
   datasetId
   schemaVersion
   presetId
   playerCount
   gameSeed
   selectedDrunkShownRole
   assignments[]
       seat
       playerName
       actualRoleId
       shownRoleId
```

Accepted behavior:

```text
selected preset actual-role multiset is materialized exactly
Baron is never interpreted as another composition command
stable gameplay seat identity remains ordered player/card index + 1
role-list input order is canonicalized before seat materialization
seat assignment uses independent namespace tb-seat-v1
same selection + seed + players replays identically
role-list reordering does not change seat assignment
different suitable seeds can change seat assignment
Drunk remains actual role drunk
Drunk shownRoleId equals selector-owned selectedDrunkShownRole
selected Drunk shown role belongs to preset.drunkAsOptions
selected Drunk shown role is absent from actual in-play roles
all non-Drunk shownRoleId values equal actualRoleId
non-Drunk selections cannot carry a Drunk shown role
```

Seat key:

```text
tb-seat-v1|datasetId|playerCount|presetId|gameSeed|roleId
```

Tests-first history:

```text
3A RED   cb84e04db546c41494a87a1298d1ec48f4211c38
3A GREEN 39fcedfe57fd214479a1afad802a9f5cb4648f34

3B lock-in a45ad77f6beadda2275a82d98de1d44a48284069
GREEN immediately; do not invent RED

3C RED   20e383f1c30e62569ec4523c35ce2340082c6009
3C GREEN 3f8d7312075e872e25d6413bb7bf06f2a1b8ff10

3D RED   6fc96b642695ff70c5fec30ce77de63e08ff6fbf
3D GREEN 8e918f69f6184a6389a23881af42127a3d761ef2
```

Validated TBSP-3 checkpoint:

```text
8e918f69f6184a6389a23881af42127a3d761ef2
```

TBSP-3 changed no App production setup path, persistence, SetupCoordination wiring, No Greater Joy behavior or A3 setup snapshot ownership.

## 7. TBSP-4 setup recommendation lock integration — COMPLETE

New source ownership:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/
    TroubleBrewingSetupRecommendationLock.kt

app/src/test/java/com/codex/campboardgamehost/clocktower/session/
    TroubleBrewingSetupRecommendationLockTest.kt
```

Accepted authority chain:

```text
TroubleBrewingSetupPresetSelector
-> selectedDrunkShownRole
-> TroubleBrewingSetupDealPlanner
-> committed actual/shown identity
-> TroubleBrewingSetupRecommendationLock
-> locked StorytellerDecision.DrunkShownRole
-> SetupCoordinationRequest.lockedDecisions
-> existing constrained setup recommendation
```

Accepted behavior:

```text
selector/deal-plan Drunk shown role becomes exactly one downstream DrunkShownRole lock
external dataset ID such as investigator resolves to canonical RoleId("Investigator")
recommendation cannot replace/reroll the locked shown identity
locked Investigator still permits compatible DrunkInvestigatorInfo generation
all returned constrained plans preserve the same locked Investigator identity
non-Drunk deal contributes no Drunk recommendation lock
bridge performs no random draw and no recommendation call
recommendation scoring was not retuned
```

Existing `SetupRecommendationService` and `SetupCandidateGenerator` already supported `lockedDecisions`; TBSP-4 reused that capability rather than introducing another recommendation algorithm.

Tests-first history:

```text
4A RED:
ddb6e391c8fbd09e5e19cdee0817a28a78e81713
missing TroubleBrewingSetupRecommendationLock contract

4A GREEN:
dad24f91cea855e7a009ac5f173ef64a06e10668
bridge implemented; R2 / :app:testFast / CI gate GREEN

4B lock-in:
ae4140ec2643edfbe160ca77aeb3604ebee66c73
GREEN immediately because existing constrained recommender already honored locked decisions
DO NOT invent a RED

4C non-Drunk boundary / final TBSP-4 checkpoint:
f68d8326de6bf57ecfd632fef73689c4900f87a9
GREEN; no-Drunk deal produces no Drunk lock
```

TBSP-4 changed no App production start/deal path, persistence, scoring weights, No Greater Joy behavior, A3 ownership or reveal-window lifecycle.

## 8. Current production setup authority audit

The current production setup path remains centered in `CampBoardGameHostApp.kt` and has **not yet been cut over**.

Relevant legacy helper:

```text
generateClocktowerAssignments(playerCount, script)
```

Current Trouble Brewing production ordering remains:

```text
generateClocktowerAssignments(...)
-> newClocktowerSeed()
-> construct provisional PlayerCards
-> if Drunk, synchronously run SetupCoordination
-> possibly replace Drunk shown role
-> commit cards / enter deal flow
```

The legacy production path still owns three responsibilities that must leave Trouble Brewing at cutover:

```text
broad random role-composition generation
Baron +2 Outsider / -2 Townsfolk post-generation mutation
random/later replacement of Drunk fake/shown identity
```

The pure ownership chain is now ready outside App:

```text
newClocktowerSeed()
-> TroubleBrewingSetupPresetSelector
   -> selected preset + selectedDrunkShownRole
-> TroubleBrewingSetupDealPlanner
   -> deterministic tb-seat-v1 actual/shown assignments
-> TroubleBrewingSetupRecommendationLock
   -> selector-owned Drunk identity becomes locked downstream fact
```

Production must eventually continue:

```text
-> construct/commit deal-ready PlayerCards
-> enter PassPhone / RevealCard without waiting for expensive first-night/setup calculation
-> run remaining first-night/setup calculation in identity-reveal window off main thread
-> consume completed results when first-night information is needed
```

The existing `A4IdentityRevealPrewarmCoordinator` is an architectural precedent only; its DEBUG/5-player scope is not the production implementation.

The normative cutover contract remains:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

## 9. Remaining TBSP sequence

```text
TBSP-4 setup recommendation lock integration   COMPLETE
        ↓
TBSP-5 durable cross-game rotation history    NOT STARTED
        ↓
TBSP-6 production cutover / restore ownership NOT STARTED
        ↓
full acceptance checkpoint
        ↓
A3 immutable setup snapshot ownership
```

TBSP-5 is persistence work. It must persist enough completed-game setup history to reconstruct selector rotation inputs without conflating cross-game rotation history with A3 active-game setup provenance.

TBSP-5 is **not authorized by completion of TBSP-4**. Reconfirm scope before any persistence write.

TBSP-6 owns the production lifecycle change that allows identity reveal to begin before expensive first-night/setup computation completes.

A3 should harden the final production setup-origin contract, not the legacy broad-random generator.

## 10. Production merge-blocking invariants

Before TBSP campaign acceptance, typed tests must ultimately prove:

```text
P1  Trouble Brewing actual roles originate from selected preset.
P2  Baron is never applied a second time.
P3  Drunk actual identity remains Drunk.
P4  Drunk shown role comes only from selected preset options.
P5  Later recommendation cannot replace selected Drunk shown role.
P6  Same dataset/history/seed reproduces same initial setup.
P7  Start selects/materializes setup only once.
P8  Compose recomposition cannot reroll a started setup.
P9  Navigation before Start does not commit a preset selection.
P10 No Greater Joy behavior remains unchanged.
P11 Restoring an already-started game does not select a new preset.
P12 Invalid TB preset data never silently falls back to broad random TB setup.
P13 Trouble Brewing identity dealing does not synchronously wait for complex first-night/setup calculation.
P14 Background first-night/setup computation consumes the committed deal and cannot mutate/reroll actual or shown identities.
```

Current pure-layer support:

```text
TBSP-3 supports P1–P4 and the seat-materialization portion of P6.
TBSP-4 supports the recommendation-layer portion of P5.
```

These are not yet production-path claims until TBSP-6 App integration is tested.

P7–P14 remain production/persistence/cutover acceptance work as applicable.

## 11. Resume protocol for next conversation

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_4_RECOMMENDATION_LOCK.md`;
4. read `docs/TESTING_STRATEGY.md`;
5. read `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md` before TBSP-5 history design;
6. read `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md` before TBSP-6 planning;
7. re-query live `main`, PR #57 head/state/checks and branch comparison;
8. distinguish TBSP-2 checkpoint `80a5b9306009a4d078623b997b5b42a88de21080`, TBSP-3 checkpoint `8e918f69f6184a6389a23881af42127a3d761ef2`, TBSP-4 checkpoint `f68d8326de6bf57ecfd632fef73689c4900f87a9`, and later docs-only commits;
9. treat **TBSP-5 persistence as NOT STARTED** unless explicitly authorized;
10. keep PR #57 Draft;
11. do not merge or mark Ready without explicit authorization;
12. do not change No Greater Joy or resume A3 without separate authorization.

## 12. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED UNTIL TBSP PRODUCTION ACCEPTANCE |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
