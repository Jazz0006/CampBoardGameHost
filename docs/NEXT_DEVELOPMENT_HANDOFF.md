# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-28 Australia/Sydney  
> Current baseline: `main@b297484cd055b6aa5cfaf1c9b1c4093832af77da`  
> Active SDE implementation PR: **none**  
> Active maintenance branch: **none**  
> SDE next gate: **targeted EvidenceLab acquisition for a qualifying E3 case**

This file is deliberately compact. Completed checkpoint detail belongs in linked completion/audit/archive documents rather than being copied forward indefinitely.

## 1. Read first

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
5. this handoff
6. `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md` for the completed R0–R6 Recovery cleanup contract / acceptance record
7. `docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`
8. `docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md`
9. `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`

Only read older SDE slice audits when a concrete ownership/history question requires them.

## 2. Startup rule

Do not assume recorded branch/HEAD/PR state is still live.

Start by checking live remote state through the **GitHub Connector**: current `main`, any newly active branch/PR, and the relevant evidence inputs for the requested slice.

If the task depends on local unpushed state, also inspect the configured local workspace before editing so that current work is never overwritten or lost.

Requirements:

- preserve the current working tree;
- do not reset/discard unrelated work;
- treat merged PR #157 and its historical continuation branch as completed evidence, not as the current branch;
- create a fresh branch for any new C5 implementation slice;
- treat merged PR #165 and `codex/current-only-recovery-cleanup` as completed historical maintenance, not the active development branch;
- no PR merge, ready transition, rebase or force-push without explicit authorization.

The 2026-09-27 #157 integration and the 2026-09-28 #165 Recovery cleanup squash merges are complete. Do not redo or rewrite that history.

## 3. Current state

~~~text
SDE-3A                                  COMPLETE
SDE-3B / BEGINNER_CONSERVATIVE_V1      COMPLETE / FROZEN
SDE-3C DecisionTrace / replay          COMPLETE
CR-A / CR-B / CR-C                     COMPLETE
C4 / SDE-3D2                           COMPLETE
IF-D durable replay                    COMPLETE
RH-E                                    COMPLETE
C5 / V2                                BLOCKED ON E3/E4
SDE-3E cutover                         BLOCKED PER SURFACE
~~~

Repository cleanup state:

- validation-only PR #162 is closed without merge after exact-tree RH-E acceptance; older validation-only PRs remain closed;
- PR #109 is closed after confirming its old half-state is outside the current short-horizon Recovery contract;
- first-wave temporary validation remote branches are removed;
- PR #165 current-only Recovery cleanup is squash-merged at `b297484cd055b6aa5cfaf1c9b1c4093832af77da`; R0–R6 are complete and R7 is separate follow-up only;
- remaining branch pruning is hygiene only and must preserve unique historical documentation/evidence.

## 4. Recovery boundary

Recovery is for recent emergency process interruption only.

Accepted contract:

~~~text
current format
+ exact current compatibility token
+ <=4h freshness
-> current validated recovery
~~~

Do **not** introduce old-format compatibility, migration, tolerant reconstruction or long-term save semantics.

The 2026-09-28 current-only Recovery audit/cleanup is complete through R6 and merged in PR #165. The authority/acceptance record is `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md`. R7 bookkeeping/transport/lifecycle ownership remains a separate follow-up rather than unfinished cleanup.

#109 must not be resurrected as a compatibility project. A future bug requires a fresh reproduction from a current-format/current-version write.

## 5. RH-E accepted checkpoint

RH-E is COMPLETE without changing visible recommendation policy.

Formal executable checkpoint:

- commit `f51a295983e8e203119dd50693af343c1ec23906`;
- tree `2babb1fba00b46dfc676efb6f090386b7a73826f`.

Exact-tree remote acceptance used validation-only Draft PR #162 because #157 was still conflicted with live `main` at that time. The later workflow/document-only integration does not change or relabel that RH-E executable-tree evidence:

- validation head `bf66363385420f507f92a729b496ff002791dee5`;
- CI #3478 GREEN;
- R2 #3231 GREEN;
- Android `:app:testFull :app:assembleDebug` GREEN;
- ASP contract tests GREEN;
- Real Clingo cross-validation GREEN.

The accepted runtime contract now has one process-scoped serialized diagnostic persistence lane; ordered append/correlation; stale/cancellation protection; deterministic bounded retention; and separate evaluation / queue / persistence / total latency reporting. Canonical session commit remains authoritative and independent of diagnostic durability.

Detailed audit, concurrency reasoning and the first validation-only test repair are archived in:

`docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`

## 6. Current execution workflow

Use `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md` as authority:

- GitHub Connector is the default repository / PR / CI / result-analysis interface;
- Mini MCP + Luna is the large/truncated-file mechanical execution supplement after Chat fixes the design;
- strong Codex may be used read-only when complete local context is genuinely required before design;
- N3150 remains retired from the default validation path;
- GitHub CI/R2 remains the Android validation and independent acceptance surface.

## 7. #157 merge closure COMPLETE

Accepted integration head:

`371ebf624898c08747203aceaed1254647867214`

Final pre-merge docs-only head:

`ee3f3e32cd48be4bf634a9f8f70268d022dc84d2`

Squash-merged main commit:

`02845a470761a988f0041d8c1027b0e2c58a7e05`

Remote acceptance before merge:

- integration CI #3479 GREEN, including Android FULL + `assembleDebug`, ASP contracts, Real Clingo and CI gate;
- integration R2 #3232 GREEN;
- final docs-only CI #3480 GREEN;
- final docs-only R2 #3233 GREEN;
- unresolved review threads: 0.

PR #157 is closed/merged. The previous continuation branch is historical. Detailed integration evidence remains archived in `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`.

## 8. Active development lane

There is no automatic next SDE production slice.

The current product-development lane is targeted evidence acquisition / qualification. Recovery R0–R6 is complete and no longer runs as a parallel maintenance lane.

Do not start C5/V2 without qualifying E3/E4 evidence. If a future Recovery ownership problem justifies R7, open it as a fresh independent maintenance slice from current `main`.

## 9. E3/E4 qualification result — no policy delta authorized

The 2026-09-27 audit checked the current EvidenceLab TB corpus, targeted expert rationale, Red-Herring C4 evidence classification, and the SDE evidence-gap contract.

Result:

- healthy-information floor: E3 FAIL / E4 FAIL;
- impaired-information believability: E3 FAIL for a new preference / E4 FAIL;
- confirmation-chain importance: E1/E2 support only for current purpose;
- role-function exposure severity: E3 FAIL;
- truth danger / contextual Red Herring: E1/E2 strong, E3 FAIL for candidate ordering;
- Demon-bluff triplet ordering: E3 FAIL;
- multi-axis weights: E4 FAIL.

Therefore there is **no automatic C5 production slice** and no placeholder V2.

Next acquisition must be narrowly targeted to one of:

1. a reconstructable expert beginner/mixed TB game with explicit healthy-information balance rationale and recoverable legal alternatives;
2. a non-Ben expert Drunk/poison case with explicit believable/coherent choice-over-alternatives rationale;
3. a functioning Librarian/Investigator + Spy/Recluse case with healthy alternatives and explicit exposure rationale;
4. an expert Demon-bluff triplet choice explicitly compared with another legal set.

Stop treating a source as an E3 candidate once qualified Storyteller identity, committed state, observed choice, recoverable alternatives, or explicit rationale is missing.

Detailed audit: `docs/SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md`.

## 10. Scope still blocked

Do not start `BEGINNER_CONSERVATIVE_V2`, add new policy weights/thresholds, broaden player-count/Traveller scope, or perform production cutover without the required E3/E4 evidence. C5 and SDE-3E remain blocked as recorded in the roadmap.
