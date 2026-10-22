# GSP-R1C2B — Real Mayor Producer / Typed Recovery Causal Replay Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Repo: `Jazz0006/CampBoardGameHost`  
> Status: **GSP-R1C2B MAYOR VERTICAL COMPLETE / ACCEPTED; GSP-R1C overall IN PROGRESS**  
> Executable PR: [#258](https://github.com/Jazz0006/CampBoardGameHost/pull/258)  
> Squash merge: `7d6255922e8db33b7ce297958d6ee467c339a9f4`  
> Accepted exact head: `ca4fcf046e75c11a9d9f705db4651d5b3d6cbd12`  
> Independent checks: CI **#3874 GREEN** (T4: full Android JVM tests + debug APK assemble + ASP + Real Clingo), R2 **#3535 GREEN**.

## Accepted end-to-end current-game vertical

### First real producer: Trouble Brewing Mayor redirect

The Host's actual `onConfirmMayorRedirectTarget` path now:

1. Uses the rules-owned `PendingMayorRedirectDecision` and its legal candidate check. Confirmation does **not** invoke recommendation ranking or provider API.
2. Materializes a neutral `StorytellerProviderRequestV1` with the **current** Host-owned causal prior decisions, uses an identity scoped to night/round/game revision/player revision/global cursor, and calls `StorytellerCausalDecisionJournalV1.captureBeforeDecision` on the exact unmodified `ClocktowerSessionState`.
3. Commits the selected candidate and typed `targetSeat` outcome **before** reducing the Mayor checkpoint/advancing GameState revision.
4. If the same night already had a confirmed Mayor target and the Storyteller confirms a different target, appends a **new causal correction** linking the earlier commit to the new one. Old frozen requests still see the original commitment; subsequent requests see only the replacement.
5. Unchanged repeated confirmation does not fabricate a fresh event. Other scripts do not acquire Mayor-specific causal records.

This is the first real production producer; **Drunk, other pair/number/role information, claims/public narrative, multi-night registration rulings and other discretionary Host decisions are not yet production-wired into this causal journal**.

### Typed current-format Recovery

- `ClocktowerRecoveryHistory.causalDecisionJournal` is an optional typed sidecar. `null` means **older/unrecorded**, never verified empty historical evidence.
- `ClocktowerCausalJournalPersistence` serializes ordered typed capture/commit/correction records: exact game/decision IDs, revisions, predecision exclusive global cursor, immutable semantic TB snapshot, player context, legal candidate IDs and selected outcomes.
- For captures, a SHA-256 fingerprint of the **entire ordered semantic action/observation prefix** is checked during strict decode against the real restored canonical action timeline and epistemic observation log. No later action, observation or correction can enter the old prefix silently. This is an integrity/recovery consistency check, not evidence that missing producer dimensions are complete or that saves are adversarially authenticated.
- `RecoverySnapshotJsonCodec.encode` and `RecoverySnapshotStrictDecoder` handle the optional sidecar; `RecoveryRestorePlanner.prepare` verifies causal correction chains, role/script boundary, game/revision/cursor compatibility and source-history parity **before Ready**. Host restores journal after restoring the canonical session.
- `RecoveryRestorePlannerTest.causalDecisionRecoveryPreservesFrozenCutoffsAndCorrectionAsOf` tests real encode/decodeStrict/prepare/restore, two same-revision decisions separated by a poison action and private numeric result, later correction, original as-of projection, rejected forged legal candidate, rejected prefix digest alteration, and compatibility with Recovery snapshots lacking the new field.

**Important:** An observed numeric result `1` is an observation. No Spy/Recluse candidate witness is manufactured into an explicit registration ruling. PR #254's generic Result–Witness separation remains authoritative and registration-ruling producer coverage stays UNKNOWN.

## Retained boundaries / remaining next work

1. This is **one production decision family**, not every Host decision producer. The neutral provider request factory is now used for Mayor redirect, but a general LLM/network recommendation provider is **not** connected.
2. Legacy decision archive revisions cannot be used to infer causal order. Missing old sidecars are `UNRECORDED`, and `PRIOR_DECISIONS`/REGISTRATION coverage is not promoted to complete globally.
3. Producer/UI typed explicit registration rulings and ambiguity state in Recovery are still a dedicated gap; never parse localized RoleAction display text into durable semantic evidence.
4. Other setup and information decision families need a bounded audit to determine the correct precommit seam (Drunk late binding, pair information, numeric/FT/role reveal, Demon bluffs and dynamic night/day decisions) **without multiplying role-specific special cases**.
5. As new producer coverage is added, add generic causal contract tests and real Recovery parity/correction tests; the Host still works offline and manually without a provider.
6. Remaining R1C acceptance should be tracked as **GSP-R1C2C — broad producer coverage & typed explicit-registration history audit/implementation**, not silently marked COMPLETE based on Mayor alone. R2 player context and R3 cross-game diversity follow after R1's adequate producer coverage.

Authority: [R1C2A frozen-prefix acceptance](GSP_R1C2A_FROZEN_CAUSAL_DECISION_PREFIX_ACCEPTANCE_2026-10-08.md), [R1C1 Recovery acceptance](GSP_R1C1_RECOVERY_PREFIX_PARITY_AND_HISTORICAL_CUTOFF_ACCEPTANCE_2026-10-08.md), [Registration ambiguity contract](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md), [generic registration UI fix](GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md), AGENTS.md, TESTING_STRATEGY.md.
