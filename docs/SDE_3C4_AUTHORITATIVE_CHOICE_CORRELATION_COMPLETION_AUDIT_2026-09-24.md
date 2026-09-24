# SDE-3C4 — Authoritative Choice Correlation Completion Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Entry HEAD: `de707e689539db759567da2ec1e8c81a5cdc6724`  
> Status: **CODE COMPLETE / REMOTE ACCEPTANCE PENDING**

## 1. Scope completed

SDE-3C4 now provides the narrow one-way correlation boundary from a persisted pending
`DecisionTrace` to an authoritative committed actual choice.

Implemented:

- `DecisionTraceAuthoritativeChoiceCorrelator`
  - requires a pending trace;
  - matches exact semantic decision identity;
  - matches the source `InformationDecisionRevision`;
  - matches the complete ordered legal-candidate domain;
  - matches interaction phase / round / sequence;
  - requires the chosen candidate to belong to that traced domain;
  - requires the exact `ConfirmedInformationDecision` draft to match the returned committed
    `RecordedEpistemicObservation`;
  - requires a Global observation binding;
  - requires a post-commit `ClocktowerSessionView` for the same game;
  - requires unchanged game-state revision and advanced player-input revision;
  - requires the canonical post-commit observation log to contain the exact committed record;
  - requires the semantic timeline cursor to have advanced past that record.
- `DecisionTraceArchive.finalizeActualChoice`
  - preserves the SDE-3C3A arbitrary same-key conflict rule;
  - adds exactly one permitted same-key transition: identical trace content except
    `actualChoice: Pending -> Committed`;
  - exact repeated finalization is idempotent;
  - committed-to-different-committed and any non-choice mutation fail closed.
- `DecisionTraceArchiveStore.correlateCommittedChoice`
  - loads the exact existing trace by archive key;
  - reconstructs a pending comparison view for idempotent retries;
  - delegates semantic correlation to the correlator;
  - persists the complete replacement archive only when finalization changes the archive;
  - returns success without a second write for an exact retry.

## 2. Why post-commit session evidence is required

`ClocktowerGameSession.preflightGlobalEpistemicObservation` can produce a globally bound
`RecordedEpistemicObservation` without mutating canonical session state.

Therefore a Global record by itself is not proof that the authoritative commit happened.

3C4 requires the post-commit session view to prove that:

- the observation is present in the canonical `EpistemicObservationLog`;
- `playerInputRevision` advanced beyond the trace source revision;
- the game identity and game-state revision still match;
- the global timeline cursor advanced beyond the committed observation.

This prevents a preflight-only record from finalizing a trace.

## 3. Manual override semantics

During shadow mode the hidden SDE recommendation is not a recommendation the Storyteller can
explicitly accept or override.

Therefore:

- `InformationDecisionSource.RECOMMENDATION_ACCEPTED` remains the existing production-visible
  recommendation path;
- `InformationDecisionSource.MANUAL` remains the existing structured manual path;
- `manualOverride` is true exactly when the authoritative source is `MANUAL`;
- optional structured/textual override rationale is accepted only for a manual override;
- disagreement between hidden `policySelection` and `actualChoice.candidateId` remains observable
  calibration data but is not itself labelled a manual override.

A human override remains evidence, not an automatic quality label.

## 4. Tests-first contract

Added `DecisionTraceAuthoritativeChoiceCorrelationTest` covering:

1. matching authoritative commit -> committed actual choice;
2. manual authoritative choice + optional rationale -> manual override metadata;
3. decision identity mismatch rejection;
4. source revision mismatch rejection;
5. complete candidate-domain mismatch rejection;
6. lifecycle mismatch rejection;
7. committed observation content mismatch rejection;
8. LegacyLocal observation rejection;
9. preflight-only Global record rejection when canonical revision/log/cursor did not advance;
10. archive Pending -> Committed transition;
11. exact archive retry idempotence;
12. conflicting finalized content rejection;
13. durable store finalization;
14. exact store retry without a second write;
15. conflicting committed choice rejection without an additional write.

## 5. Local validation status

Mini MCP `test:fast` was invoked after the tests-first contract and again after production
implementation.

Both invocations stopped during Gradle configuration before Kotlin compilation:

```text
Could not determine the dependencies of task ':app:testFast'.
SDK location not found.
Define ANDROID_HOME or sdk.dir in /home/opc/repos/CampBoardGameHost/local.properties.
```

Therefore there is no local Kotlin RED/GREEN execution evidence for 3C4. This remains the known
Oracle Android SDK limitation, not a passed-test claim. GitHub CI/R2 is the required independent
Android acceptance surface.

## 6. Ownership / fanout result

3C4 does not move any canonical authority into SDE:

- `InformationDecisionContext.confirm` still owns structured confirmation;
- player-reveal authorization still owns stale snapshot/current-revision publication checks;
- `ClocktowerGameSession.commitGlobalEpistemicObservation` still owns canonical observation commit;
- `ActionFactTimeline + EpistemicObservationLog` remain canonical history;
- DecisionTrace persistence remains diagnostic/replay state only.

No UI confirmation path, visible recommendation path, canonical commit path, or game-state mutation
was changed by this slice.

The production app still does not auto-persist the read-only shadow evaluator as a side effect.
SDE-3C4 supplies the safe correlation boundary required when a pending trace is captured; production
cutover remains outside this slice.

## 7. Next gate

After commit/push and independent GitHub CI/R2 acceptance, SDE-3C4 can be marked fully COMPLETE.

Then proceed to **SDE-3C5 multi-policy replay**:

- evaluate multiple explicit policy versions against the same canonical committed prefix;
- preserve exact historical truth and actual-choice correlation;
- do not mutate production recommendation or canonical game history.
