# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-27 Australia/Sydney  
> Current baseline: `main@02845a470761a988f0041d8c1027b0e2c58a7e05`  
> Active SDE implementation PR: **none**  
> Next gate: **ClocktowerEvidenceLab evidence sync + E3/E4 qualification audit**

This file is deliberately compact. Completed checkpoint detail belongs in linked completion/audit/archive documents rather than being copied forward indefinitely.

## 1. Read first

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
5. this handoff
6. `docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`
7. `docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md`
8. `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`

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
- no PR merge, ready transition, rebase or force-push without explicit authorization.

The 2026-09-27 #157 integration and squash merge are complete. Do not redo or rewrite that history.

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

## 8. Next gate — evidence qualification

There is no automatic next SDE production slice.

Next work should:

1. sync or inspect the latest relevant Trouble Brewing evidence from ClocktowerEvidenceLab;
2. extract candidate policy predicates with explicit provenance and rationale;
3. grade each candidate against E3/E4 requirements;
4. start C5 only if at least one concrete predicate qualifies;
5. keep `BEGINNER_CONSERVATIVE_V1` immutable.

If no predicate qualifies, continue targeted evidence acquisition rather than creating placeholder V2 behavior.

## 9. Scope still blocked

Do not start `BEGINNER_CONSERVATIVE_V2`, add new policy weights/thresholds, broaden player-count/Traveller scope, or perform production cutover without the required E3/E4 evidence. C5 and SDE-3E remain blocked as recorded in the roadmap.
