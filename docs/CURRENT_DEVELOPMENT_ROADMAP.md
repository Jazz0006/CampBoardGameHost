# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-29 Australia/Sydney  
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

accepted TBSP-1 head:
9ee6df0787af457266912e24d91576e68be4787f

current campaign:
TBSP — Trouble Brewing Setup Preset Integration

campaign handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md
```

The poison exactly-once hotfixes are now part of the required TBSP baseline and must be preserved.

Current TBSP implementation status at this roadmap checkpoint:

```text
TBSP-0 documentation / handoff: COMPLETE
TBSP-1A final dataset asset + typed parser: COMPLETE
TBSP-1B semantic validator: COMPLETE
PR #57 FAST CI at accepted 1B head: GREEN
TBSP-2 pure history-aware selector: NEXT
seat assignment / deal materialization: NOT STARTED
App wiring / persistence / A3 setup snapshot: NOT STARTED
```

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
- existing setup recommendation behavior for the remaining setup decisions;
- No Greater Joy current behavior;
- safe cross-game rotation without conflating it with A3 historical setup provenance.

Final external dataset selected for integration:

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

The exact final dataset is packaged as:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
```

Its verified Git blob identity is:

```text
a935474bec07577eb9e753bad2135a604add63f5
```

Do not reformat or regenerate the source dataset during integration; preserve its verified byte identity.

## 4. TBSP-1 accepted checkpoint

### TBSP-1A — final asset and parser — COMPLETE

The accepted parser slice owns only:

```text
final dataset asset
TroubleBrewingSetupPresetModels
TroubleBrewingSetupPresetJson
focused parser contract test
```

Executable contract:

```text
schema_version == 2
dataset_id == trouble_brewing_setup_presets_v2_final
status == final_ready_for_program_integration
pools 5..15 all exist
pool sizes match the declared final pool sizes
total presets == 480
```

The typed parser intentionally keeps dataset character IDs in their external lowercase representation. It does not silently manufacture canonical runtime `RoleId` values from strings such as `fortuneteller` or `scarletwoman`.

### TBSP-1B — semantic validator — COMPLETE

The validator checks all 480 presets for:

```text
unique preset IDs
preset.playerCount matches owning pool
total actual roles == player count
exactly one Demon
Demon == Imp
no duplicate actual role
all IDs resolve through the canonical Trouble Brewing character registry
role category matches canonical registry ownership
standard composition unless Baron
Baron applies outsider +2 / townsfolk -2 exactly once
5–6 curated defaults contain no Baron
Drunk absent -> empty drunk_as_options
Drunk present -> exactly three unique absent Townsfolk options
```

Dataset-level regression assertions preserve:

```text
208 Drunk presets
624 Drunk option slots
208 unique Drunk-option triples
```

PR #57 CI acceptance at `9ee6df0787af457266912e24d91576e68be4787f`:

```text
Classify changes: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
ASP contract tests: correctly skipped
Real Clingo cross-validation: correctly skipped
R2 main-thread boundary: GREEN
```

### TBSP-1 STOP — preserved

TBSP-1 did not implement:

```text
rotation history
runtime preset selector
seat assignment
Drunk runtime choice
CampBoardGameHostApp setup cutover
persistence
A3 immutable setup snapshot ownership
```

## 5. TBSP-2 next checkpoint — pure history-aware selector

TBSP-2 remains pure and must not wire production App setup yet.

The final dataset already carries the hard runtime rotation inputs:

```text
exact_repeat = reject
last-game overlap thresholds by player count
five recent-history weights = 1.00, 0.65, 0.40, 0.20, 0.10
fallback overlap-threshold relaxation = +0.05 repeatedly
```

These values should be parsed from `runtime_selection_policy`; do not duplicate them as a second hard-coded policy table in selector code.

Recommended tests-first sequence:

```text
TBSP-2A typed runtime-selection-policy parsing + player-count isolation/provenance
TBSP-2B deterministic preset + Drunk option replay independent of input iteration order
TBSP-2C exact previous real non-Demon composition rejection
TBSP-2D last-game overlap threshold eligibility/rejection
TBSP-2G fallback threshold relaxation to first non-empty level
```

The following weighting behavior remains blocked by the handoff's explicit OPEN product contract and must not be invented silently:

```text
how five-game history weights become candidate selection weights
same-minion-set soft penalty strength
repeated-primary-style trigger and strength
same-consecutive-Drunk-disguise penalty strength
additive vs multiplicative combination
minimum candidate weight / tie-probability details
```

Therefore TBSP-2 may establish deterministic hard filtering and fallback before that contract is frozen, but must stop before production weighting behavior that depends on unresolved numeric policy.

## 6. Current production setup authority audit

The current production setup path remains centered in `CampBoardGameHostApp.kt`.

Relevant existing helper:

```text
generateClocktowerAssignments(playerCount, script)
```

Current responsibilities include broad random composition generation, runtime Baron modification, role shuffle, and random Drunk shown-role selection.

Current production ordering is still:

```text
generateClocktowerAssignments(...)
-> newClocktowerSeed()
-> construct PlayerCards
```

TBSP must eventually reverse that ordering so a single game seed exists before every random setup decision:

```text
newClocktowerSeed()
-> select preset
-> select Drunk shown role when applicable
-> seeded seat shuffle
-> construct PlayerCards
-> remaining setup recommendation
```

The final presets already encode Baron setup modification. Production must never apply the old Baron transform again after consuming a preset.

The existing setup recommendation architecture remains separate. `SetupCoordinationRequest.lockedDecisions` is the intended seam for carrying the chosen `StorytellerDecision.DrunkShownRole` later in the campaign.

## 7. Sequence after TBSP

The remaining A3 blocker is immutable setup-snapshot ownership/persistence:

```text
docs/archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md
```

Current sequence remains deliberately:

```text
PR #54 correctness baseline        MERGED
PR #55 Dawn poison hotfix          MERGED
PR #56 Dusk poison hotfix          MERGED
        ↓
TBSP preset integration            CURRENT
        ↓
A3 setup snapshot ownership        DEFERRED UNTIL TBSP COMPLETE
```

A3 should harden the final production setup-origin contract rather than the broad-random generator that TBSP is replacing.

## 8. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED UNTIL TBSP COMPLETE |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |