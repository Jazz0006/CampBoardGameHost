# TBSP and Predecessor Handoff Closeout Index — 2026-08-30

> Repository: `Jazz0006/CampBoardGameHost`  
> Purpose: remove completed execution handoffs/checkpoints from the active docs root while preserving a concise historical index.  
> Current status authority remains `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.

## 1. Why these files left the active root

The docs lifecycle policy requires the active root to contain one current `NEXT_DEVELOPMENT_HANDOFF_*.md`, long-lived semantic/design references, and normative workflow/test references.

The files listed below described completed predecessor work or completed TBSP slices. Their exact historical content remains recoverable from Git history and the corresponding PR history, but they are no longer current implementation instructions.

## 2. Closed predecessor execution handoffs

Removed from active docs root:

```text
NEXT_DEVELOPMENT_HANDOFF_2026-08-28_GLOBAL_CORRECTNESS_FOLLOWUP.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-29_PR54_RUNTIME_BLOCKERS.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-29_PR55_DAWN_POISON_EXACTLY_ONCE.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-29_PR56_DUSK_POISON_EXPIRY_EXACTLY_ONCE.md
```

Historical meaning:

- PR #54 same-night/GCR correctness was merged before TBSP became current.
- PR #55 Dawn poison exactly-once was merged and is now protected baseline behavior.
- PR #56 next-night/Dusk poison expiry exactly-once was merged and is now protected baseline behavior.
- Remaining non-blocking GCR follow-ups are tracked in the roadmap deferred registry rather than through an obsolete active handoff.

## 3. Closed TBSP planning / completed-slice handoffs

Removed from active docs root:

```text
NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_3_DEAL_MATERIALIZATION.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_4_RECOMMENDATION_LOCK.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_5_ROTATION_HISTORY_PERSISTENCE.md
```

Historical meaning:

- the original campaign handoff was written before TBSP implementation and now contains stale “not implemented” state;
- TBSP-1 through TBSP-5 are complete;
- TBSP-6A through TBSP-6F are also complete;
- the current active implementation handoff is now:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md
```

Long-lived policy extracted from the old campaign planning remains active in:

```text
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

## 4. Historical GCR checkpoint documents removed from active root

The following completed checkpoint/audit documents were also removed from the active root because they are historical evidence rather than current instructions:

```text
GCR3_SOURCE_STRING_RETIREMENT_AUDIT_2026-08-28.md
GCR_BLOCKER_ACCEPTANCE_CHECKPOINT_2026-08-28.md
```

Long-lived source-string retirement policy remains in:

```text
docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md
```

## 5. Deferred work is different from completed work

Unfinished future work is not deleted merely because it is inactive. It remains under:

```text
docs/archive/deferred/
```

In particular:

```text
NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md
```

These deferred handoffs may only be resumed after a fresh live-state audit and explicit roadmap reactivation.

## 6. Current continuation pointer

New conversations should ignore the historical files listed above unless investigating provenance.

Use:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md
docs/TESTING_STRATEGY.md
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

Current code checkpoints to distinguish:

```text
last fully GREEN code checkpoint:
5c10cd29111449e1f8af2b8944609a2002048679

TBSP-6G RED code checkpoint:
a26c221670fdea2612626f762d162b66091896af
```

Later docs-only cleanup commits on top of the RED checkpoint are not code-GREEN evidence.
