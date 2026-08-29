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

current campaign:
TBSP — Trouble Brewing Setup Preset Integration

campaign handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md
```

The poison exactly-once hotfixes are now part of the required TBSP baseline and must be preserved.

Current TBSP implementation status at this roadmap checkpoint:

```text
TBSP-0 documentation / handoff: COMPLETE
TBSP-1A parser RED: IMPLEMENTED
TBSP-1A minimum parser GREEN: IMPLEMENTED
final dataset Android asset: NOT YET ACCEPTED ON BRANCH
typed semantic validator: NOT STARTED
runtime selector / history / App wiring: NOT STARTED
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

The exact final dataset must be packaged as:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
```

Do not reformat or regenerate the source dataset during integration; preserve its verified byte identity.

## 4. TBSP-1 current checkpoint

### TBSP-1A — parser contract

The parser slice owns only:

```text
final dataset asset
TroubleBrewingSetupPresetModels
TroubleBrewingSetupPresetJson
focused parser contract test
```

Required executable contract:

```text
schema_version == 2
dataset_id == trouble_brewing_setup_presets_v2_final
status == final_ready_for_program_integration
pools 5..15 all exist
pool sizes match the declared final pool sizes
total presets == 480
```

The typed parser intentionally keeps dataset character IDs in their external lowercase representation. It must not silently manufacture canonical runtime `RoleId` values from strings such as `fortuneteller` or `scarletwoman`.

Runtime role resolution and Trouble Brewing legality belong to TBSP-1B semantic validation.

### TBSP-1B — semantic validator — NEXT AFTER 1A ACCEPTANCE

Validate all 480 presets for:

```text
unique preset IDs
preset.playerCount matches owning pool
total actual roles == player count
exactly one Demon
Demon == Imp
no duplicate actual role
all IDs resolve to Trouble Brewing characters
standard composition unless Baron
Baron applies outsider +2 / townsfolk -2 exactly once
5–6 curated defaults contain no Baron
Drunk absent -> empty drunk_as_options
Drunk present -> exactly three unique absent Townsfolk options
```

Optional dataset-level assertions may also preserve the audited aggregate:

```text
208 Drunk presets
624 Drunk option slots
208 unique Drunk-option triples
```

### TBSP-1 STOP

Do not implement in TBSP-1:

```text
rotation history
runtime preset selector
seat assignment
Drunk runtime choice
CampBoardGameHostApp setup cutover
persistence
A3 immutable setup snapshot ownership
```

## 5. Current production setup authority audit

The current production setup path remains centered in `CampBoardGameHostApp.kt`.

Relevant existing helper:

```text
generateClocktowerAssignments(playerCount, script)
```

Current responsibilities include broad random composition generation, runtime Baron modification, role shuffle, and random Drunk shown-role selection.

TBSP must eventually reverse the current ordering so a single game seed exists before every random setup decision:

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

## 6. Sequence after TBSP

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

## 7. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED UNTIL TBSP COMPLETE |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
