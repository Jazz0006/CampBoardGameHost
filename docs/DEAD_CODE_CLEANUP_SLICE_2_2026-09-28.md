# Dead-code cleanup — slice 2

Baseline: main `311eaa472eebe627fd1ca9865ed4702fe28c43cc`. Independent of unmerged slice 1 / PR #166.

## Architecture pre-flight

- Current owner: AppGameModels declares supported game/role tags; App and shared UI render them; existing archive/recovery decoders interpret persisted names. SetupRecommendationService owns setup recommendations.
- Proposed responsibility: none added. Retire Werewolf feature residue and the unused RecommendationService forwarding object.
- Authoritative state owners: existing App and ClocktowerGameSession remain unchanged.
- Narrow typed input/output seam: GameKind/Role lose retired values; existing enum decoders reject unsupported names. Recommendation service test calls the existing setup owner directly.
- Keep in current owner / extract: preserve live owners, remove unused branches; retain RecommendationUiState in an accurately named file.
- Reason: the current UI can start only Undercover and Clocktower. No supported production producer creates Werewolf games or its roles. The facade has only a test caller.

## Shared-contract fan-out and intended behavior

Must inherit retirement: App role labels/outcomes/restart/reveal/recovery capture, deal/results/history/game-name UI, RecoveryPreview and RecoverySnapshotStrictDecoder. Archive decoding already returns null for unknown GameKind; card decoding rejects unknown Role. No name remapping or migration is introduced. Current supported payload names/schema stay unchanged.

Test fixtures using Werewolf or Villager incidentally will use supported equivalents while retaining their assertions. The existing recovery rejection test will use the raw retired wire string. Add archive tests before implementation to prove retired game/role tags are refused. Existing Clocktower round-trip coverage and an Undercover decode assertion protect supported behavior.

Intentionally exempt: historical documentation and raw retired strings in rejection tests. Clocktower roles (including Scarlet Woman), algorithm policies, evidence/replay tools, and active history/recovery remain in scope only as preserved consumers, not redesign targets.

RecommendationService has no production callers. Move its usable-plan-per-style test to SetupRecommendationService; retain all assertions. Keep RecommendationUiState and its consumers unchanged.

## Validation

First execute the archive tests against unchanged production through GitHub CI. The two assertion failures rejecting previously accepted retired values are expected RED evidence. Continue to production deletion after confirming those failures rather than treating this expected RED as a blocker.

After implementation: producer/consumer re-search, resource reference/XML checks, complete diff/allowlist audit, and git diff --check. Require full Android tests/build, R2, and full-checkpoint ASP/Real-Clingo validation. Local Android SDK is unavailable. No merge is authorized by this cleanup request.
