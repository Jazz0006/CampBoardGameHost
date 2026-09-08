# PS1 Archive / Recovery Separation Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> PR: #112 — Persistence Simplification: recent emergency recovery  
> Status: **PS1 COMPLETE / PS2 NOT STARTED**

## Result

PS1 separates long-lived Archive data from short-lived active Recovery data.

New archive writes now project live game state into a narrow `GameArchiveRecord` and encode it through `GameArchiveJsonCodec`. They no longer call or consume `activeGameSnapshotJson()`.

The archive payload contains review-visible data only:

- game kind;
- round;
- player/card state;
- elimination/host records;
- Clocktower events;
- outcome;
- archive metadata owned by the archive entry.

It does not carry active-Recovery-only fields such as recovery version, saved timestamp, UI screen, active-game identity, unfinished-night draft/checkpoint UI state, or A4 epistemic recovery state.

## Compatibility

Existing legacy archive entries shaped as `{ "snapshot": ... }` remain readable through an archive-only fallback. Archive review does not require current active-Recovery version or content-identity compatibility.

Active Recovery itself remains unchanged in PS1:

- `activeGameSnapshotJson()` still owns the current active-save payload;
- active save/restore semantics are unchanged;
- `ON_PAUSE` / `ON_STOP` persistence behavior is unchanged;
- A4 durability ordering is unchanged.

## Validation evidence

PS1 was validated with:

- a focused typed `GameArchiveJsonCodecTest` contract;
- explicit legacy archive compatibility coverage, including a deliberately unsupported active-Recovery version inside a legacy archive entry;
- `:app:testFast` passing after production wiring;
- `git diff --check`;
- exact App wiring audit showing only archive decode/store/restart anchors changed;
- temporary one-shot patch workflow/script removed after use.

A prior final cleanup commit was authored by `github-actions[bot]`, which caused GitHub's normal PR CI/R2 runs for that exact bot-authored head to report `action_required` without creating jobs. A normal repository-user checkpoint restored ordinary PR workflow execution. This final checkpoint requests the repository's `[full-ci]` gate so the complete accumulated PR diff is validated rather than treating the final docs-only synchronize commit as representative.

## Next

The next campaign slice is **PS2 — Minimal typed RecoverySnapshot**. It has not started.

Do not begin PS2 production changes without explicit user authorization. For PS2, use tests-first only for real behavior/durability contracts; do not manufacture RED tests for purely mechanical ownership moves or rewiring.
