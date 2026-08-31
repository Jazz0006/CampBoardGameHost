# TBSP-6H-B Production Wiring Checkpoint — 2026-08-31

## Scope

This checkpoint records the completed TBSP-6H-B First Night production-wiring slice only. It does not enter TBSP-6I and does not authorize merging or marking PR #57 Ready.

## Production code checkpoint

- Code commit: `ff1c99fe97552dc65f3d1bf8326bdb451c8e25a0`
- Production files changed by that commit only:
  - `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt`
  - `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt`
- One-shot workflow/script cleanup commit: `f12e6369e7b25519c97f69271619a32770eca60f`

## Implemented lifecycle

- The committed Trouble Brewing deal creates the First Night natural-pair precompute request from committed cards, the prepared seed, and `poisonedPlayerName = null`.
- Reveal entry remains immediate; First Night precompute is launched from the reveal lifecycle on `Dispatchers.Default`.
- The App owns a remembered `TroubleBrewingFirstNightPrecomputeCoordinator` and supplies TB-only ready/result providers to the Host.
- The Host first checks exact READY state, otherwise resolves BUSY/MISS through the suspend provider inside `withContext(Dispatchers.Default)`.
- Trouble Brewing First Night materialization consumes the precomputed natural Librarian/Investigator candidate baseline; the non-TB/provider-null path retains the previous synchronous fallback.
- The existing First Night confirmation boundary acts as the safe BUSY gate: a start request waits for the exact result instead of blocking the main thread.
- Existing special registration and unreliable-information branches remain outside this precomputed natural baseline.

## Validation already completed in the one-shot workflow

GitHub Actions run `33340182930` completed successfully with all of the following gates GREEN:

- exact bootstrap head/source-blob verification
- exact two-file mechanical production patch
- `:app:compileDebugKotlin`
- focused typed evidence:
  - `TroubleBrewingFirstNightPrecomputeCoordinatorTest`
  - `NaturalPairInformationCandidateGeneratorTest`
  - `FirstNightInformationLifecycleTest`
  - `FirstNightInformationMigrationTest`
  - `FirstNightPoisonLifecycleTest`
- `:app:testFast --rerun-tasks`
- `git diff --check`
- exact production-file scope audit
- remote-head recheck before push
- production code commit/push
- temporary workflow/script self-cleanup

## Final acceptance gate

The workflow-generated cleanup head reports GitHub Actions `action_required` with no jobs, rather than a code/test failure. This docs-only checkpoint is therefore used to create a normal PR synchronize event for final repository CI and R2 validation against the same production code tree.

TBSP-6H-B is accepted only when that final CI and R2 validation is GREEN. Stop after acceptance; do not begin TBSP-6I in this checkpoint.
