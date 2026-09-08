# PS4.6 Final Validation Trigger

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112

This is the stable full-validation checkpoint for PS4 Persistence Cleanup.

Before this checkpoint:

- PS4.1 through PS4.5 are complete;
- Archive legacy `snapshot` decoding has been retired under the current-version-only persistence contract;
- the Archive `id -> archivedAtMillis` compatibility fallback has been retired;
- Recovery remains strict current-format v2 and fails old/unsupported formats closed;
- PS4.6 static architecture audit run `34169917902` passed;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md` has been synchronized to PS4.6;
- temporary PS4.6 architecture-audit and roadmap-sync workflows have been removed.

This checkpoint requests the full repository validation gate. Do not mutate the branch until the full CI and R2 results are recorded.

Required green evidence:

- full Android unit tests;
- debug APK assembly;
- ASP contract/static validation;
- Python oracle parity;
- real Clingo semantic/cross-validation;
- R2 structural/main-thread regression gate.

PR #112 must remain draft and unmerged during validation. PS5 must not begin until PS4.6 is fully green and documented complete.
