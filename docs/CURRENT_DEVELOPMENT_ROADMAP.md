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

latest fully validated TBSP-3 code checkpoint:
8e918f69f6184a6389a23881af42127a3d761ef2

current campaign:
TBSP — Trouble Brewing Setup Preset Integration

current completed-slice handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_3_DEAL_MATERIALIZATION.md

campaign design baseline:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md

normative TBSP-2 rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

The poison exactly-once hotfixes remain part of the required TBSP baseline and must be preserved.

Current TBSP implementation status:

```text
TBSP-0 documentation / campaign plan: COMPLETE
TBSP-1A final dataset asset + typed parser: COMPLETE
TBSP-1B semantic validator: COMPLETE
TBSP-2 pure history-aware selector: COMPLETE
TBSP-3 pure deal materialization: COMPLETE
TBSP-4 setup recommendation integration: NOT STARTED
TBSP-5 durable cross-game rotation-history storage: NOT STARTED
TBSP-6 production cutover / restore ownership: NOT STARTED
A3 immutable setup snapshot: DEFERRED UNTIL TBSP PRODUCTION ACCEPTANCE
```

At latest TBSP-3 code checkpoint `8e918f69f6184a6389a23881af42127a3d761ef2`:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
full Android unit/build step: correctly skipped by normal FAST routing
ASP contract tests: correctly skipped
Real Clingo cross-validation: correctly skipped
```

An exact developer-selected local T0 was not executed in this Chat environment because no repository checkout / Gradle wrapper is mounted here. The code commits themselves were validated by the branch's GitHub Actions `:app:testFast` route. Do not rewrite that evidence as a local T0 run.

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

The production-cutover lifecycle and ownership contract is frozen in:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

Final external dataset:

```text
trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
status: final_ready_for_program_integration
```

Audited aggregate:

```text
player counts: 5..15
preset count: 480
pool sizes: 30,30,50,50,50,50,50,50,40,40,40
Drunk presets: 208
Drunk options per Drunk preset: exactly 3
```

Android asset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
```

Verified Git blob identity:

```text
a935474bec07577eb9e753bad2135a604add63f5
```

Do not reformat or regenerate the source dataset during integration.

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
pool sizes match the final dataset
total presets == 480
```

The parser intentionally keeps dataset character IDs in their external lowercase representation until canonical resolution is explicitly required.

### TBSP-1B — semantic validator

Accepted validator coverage includes:

```text
unique preset IDs
preset.playerCount matches owning pool
total actual roles == player count
exactly one Demon == Imp
no duplicate actual role
all IDs resolve through canonical Trouble Brewing registry
role category matches registry ownership
standard composition unless Baron
Baron outsider +2 / townsfolk -2 exactly once
5–6 curated defaults contain no Baron
Drunk absent -> empty drunk_as_options
Drunk present -> exactly three unique absent Townsfolk options
208 Drunk presets / 624 option slots / 208 unique option triples
```

TBSP-1 did not wire production setup.

## 5. TBSP-2 accepted checkpoint — COMPLETE

TBSP-2 is a pure history-aware selector and remains outside App production wiring.

Accepted ownership includes:

```text
TroubleBrewingSetupPresetSelector.kt
TroubleBrewingSetupPresetRotationScorer.kt
TroubleBrewingSetupRotationHistory.kt
TroubleBrewingSetupPresetSelectorTest.kt
TroubleBrewingSetupPresetRotationScorerTest.kt
```

Accepted tests-first slices:

```text
TBSP-2A player-count isolation + provenance
TBSP-2B deterministic preset and Drunk-option replay
TBSP-2C exact previous real non-Demon composition rejection
TBSP-2D last-game overlap threshold for all player counts 5..15
TBSP-2E five-game history decay
TBSP-2F soft rotation weighting + minimum weight floor
TBSP-2G +0.05 fallback to first non-empty level while exact repeat remains hard
```

The previously open soft-weight policy is frozen by:

```text
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md
```

Normative values:

```text
history weights:
1.00, 0.65, 0.40, 0.20, 0.10

baseNoveltyWeight:
max(0.20, 1.0 - weightedOverlap)

soft multipliers:
same immediately-previous Minion set  × 0.70
primary style seen >=2 of 5 x0.88
same Drunk shown x0.40

final weight floor:
0.05

fallback:
+0.05 last-game overlap threshold repeatedly
stop at first non-empty eligible pool
exact-repeat composition never becomes eligible
maximum threshold 1.0
```

Selector deterministic namespaces include:

```text
tb-preset-v1
tb-drunk-v1
```

The selector uses deterministic fixed-point weighted drawing. The candidate Drunk shown role used for soft weighting is the same selected disguise carried forward; there is no second Drunk authority.

Accepted final TBSP-2 code checkpoint:

```text
80a5b9306009a4d078623b997b5b42a88de21080
```

## 6. TBSP-3 pure deal materialization — COMPLETE

TBSP-3 introduced pure setup/deal materialization without App wiring or persistence.

Source ownership:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlanner.kt

app/src/test/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlannerTest.kt
```

### 6.1 Audit conclusions preserved

```text
PlayerCard has no explicit seat field.
Stable gameplay seat identity is existing card/player order: seat = index + 1.
PlayerState is a post-materialization gameplay type and is not appropriate planner input.
The pure planner therefore consumes ordered player identities/names and derives seat in output.
```

### 6.2 Accepted planner contract

The planner now consumes:

```text
TroubleBrewingSetupPresetSelection
+ orderedPlayerNames
```

and produces typed assignments containing:

```text
seat
playerName
actualRoleId
shownRoleId
```

plus plan provenance:

```text
datasetId
schemaVersion
presetId
playerCount
gameSeed
selectedDrunkShownRole
```

Accepted behavior:

```text
selected preset actual-role multiset is materialized exactly
Baron is never interpreted as a command to modify composition
role-list input order is canonicalized before seat materialization
seat assignment uses independent deterministic namespace tb-seat-v1
same selected preset + seed + ordered players replays identically
reordering role lists inside an equivalent preset does not change seat assignment
different suitable seeds can produce different seat assignments
Drunk remains actual role drunk
Drunk shownRoleId equals selection.selectedDrunkShownRole
selected Drunk shown role must belong to preset.drunkAsOptions
selected Drunk shown role must be absent from actual in-play roles
all non-Drunk shownRoleId values equal actualRoleId
non-Drunk selections must not carry a Drunk shown role
no second Drunk draw exists in the planner
```

Seat namespace:

```text
tb-seat-v1
```

Current materialization key includes:

```text
tb-seat-v1|datasetId|playerCount|presetId|gameSeed|roleId
```

### 6.3 Tests-first history

```text
TBSP-3A RED:
cb84e04db546c41494a87a1298d1ec48f4211c38
missing TroubleBrewingSetupDealPlanner contract

TBSP-3A GREEN:
39fcedfe57fd214479a1afad802a9f5cb4648f34
exact already-Baron-adjusted multiset preserved

TBSP-3B lock-in test:
a45ad77f6beadda2275a82d98de1d44a48284069
ordinary exact-role multiset test was GREEN immediately because the generalized 3A implementation already satisfied it
NO fake RED should be recorded for 3B

TBSP-3C RED:
20e383f1c30e62569ec4523c35ce2340082c6009
current planner ignored gameSeed, so deterministic independent seat-shuffle contract failed

TBSP-3C GREEN:
3f8d7312075e872e25d6413bb7bf06f2a1b8ff10
introduced canonical deterministic tb-seat-v1 seat materialization

TBSP-3D RED:
6fc96b642695ff70c5fec30ce77de63e08ff6fbf
shownRoleId / selectedDrunkShownRole output contract not yet implemented

TBSP-3D GREEN / TBSP-3 checkpoint:
8e918f69f6184a6389a23881af42127a3d761ef2
selector-owned Drunk shown role preserved without reroll
```

Final checkpoint validation:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
full Android unit/build step: skipped by normal FAST routing
ASP contract tests: skipped
Real Clingo cross-validation: skipped
```

TBSP-3 changed no App production setup path, persistence, SetupCoordination wiring, No Greater Joy behavior or A3 setup snapshot ownership.

## 7. Current production setup authority audit

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

The legacy helper currently owns three Trouble Brewing responsibilities that must leave the TB production path at cutover:

```text
broad random role-composition generation
Baron +2 Outsider / -2 Townsfolk post-generation mutation
random Drunk fake/shown-role selection
```

Final presets already encode Baron setup modification. Production must never apply the old Baron transform after consuming a preset.

The selector already owns the single deterministic Drunk shown-role choice, and TBSP-3 now carries it through pure deal materialization. Later recommendation may consume that identity as a locked fact, but it must not select or replace it.

The eventual Trouble Brewing production lifecycle is explicitly:

```text
newClocktowerSeed()
-> select preset + selector-owned Drunk shown role
-> deterministic seat materialization
-> construct/commit deal-ready PlayerCards
-> enter PassPhone / RevealCard without waiting for expensive first-night/setup calculation
-> run remaining first-night/setup calculation in the identity-reveal window off the main thread
-> consume completed results when first-night information is actually needed
```

The existing `A4IdentityRevealPrewarmCoordinator` is an architectural precedent for the reveal-window/background-execution seam, but its current DEBUG/5-player scope is not the production implementation.

The normative cutover contract is:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

## 8. Remaining TBSP sequence

```text
TBSP-3 pure deal materialization              COMPLETE
        ↓
TBSP-4 existing recommendation integration    NOT STARTED
        ↓
TBSP-5 durable cross-game rotation history    NOT STARTED
        ↓
TBSP-6 production cutover / restore ownership NOT STARTED
        ↓
full acceptance checkpoint
        ↓
A3 immutable setup snapshot ownership
```

TBSP-4 must turn the committed selector-owned Drunk shown role into a locked downstream fact rather than another recommendation choice.

TBSP-6 owns the production lifecycle change that allows identity reveal to begin before expensive first-night/setup computation completes.

A3 should harden the final production setup-origin contract, not the legacy broad-random generator.

The user previously scoped the current work away from setup recommendation wiring. Therefore, completion of TBSP-3 does **not** authorize starting TBSP-4 implementation in the same slice. Reconfirm scope before writing recommendation integration.

## 9. Production merge-blocking invariants still outstanding

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

TBSP-3 now provides pure typed coverage supporting P1–P4 and the seat-materialization portion of P6. Those invariants are not yet production-path claims until later App integration is tested.

P13–P14 remain later production-cutover requirements.

## 10. Resume protocol for next conversation

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_3_DEAL_MATERIALIZATION.md` as the completed TBSP-3 checkpoint;
4. read `docs/TESTING_STRATEGY.md`;
5. read `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md` before any recommendation/App/cutover planning;
6. re-query live `main`, PR #57 head/state/checks and branch comparison;
7. distinguish TBSP-2 checkpoint `80a5b9306009a4d078623b997b5b42a88de21080` from completed TBSP-3 checkpoint `8e918f69f6184a6389a23881af42127a3d761ef2` and later docs-only commits;
8. treat **TBSP-4 recommendation integration as NOT STARTED**;
9. do not begin TBSP-4 wiring until the current scope explicitly permits setup recommendation integration;
10. keep PR #57 Draft;
11. do not merge, mark Ready, change No Greater Joy, add persistence, or resume A3 without explicit authorization.

## 11. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED UNTIL TBSP PRODUCTION ACCEPTANCE |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |