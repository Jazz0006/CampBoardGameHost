# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-29 Australia/Sydney  
> Current baseline: live `main` — query exact HEAD at session start  
> Active product implementation PR: **#172 — DLB-3A shadow Drunk decision trace surface (Draft; accepted; standing merge authorization applies)**  
> Documentation convergence: **query live branch/PR state; do not hard-code the temporary docs branch after merge**  
> Product next gate: **DLB-3B setup-level Drunk consequence feature bridge — evidence-gated; DLB-3A is accepted; targeted EvidenceLab assignment acquisition continues in parallel**

This file is deliberately compact. Completed checkpoint detail belongs in linked completion/audit/archive documents rather than being copied forward indefinitely.

## 1. Read first

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
5. this handoff
6. `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md` — current DLB implementation authority
7. `docs/DRUNK_LATE_BINDING_AND_FIRST_NIGHT_DECISION_SEQUENCE_AUDIT_2026-09-28.md` — source architecture audit
8. `docs/APP_HOST_DECOMPOSITION_PLAN_AUDIT_2026-09-28.md` — decomposition guardrail audit
9. `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md` for the completed R0–R6 Recovery cleanup contract / acceptance record
10. `docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`
11. `docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md`
12. `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`

Only read older SDE slice audits when a concrete ownership/history question requires them.

## 2. Startup rule

Do not assume recorded branch/HEAD/PR state is still live.

Start by checking live remote state through the **GitHub Connector**: current `main`, any newly active branch/PR, and the relevant evidence inputs for the requested slice.

If the task depends on local unpushed state, also inspect the configured local workspace before editing so that current work is never overwritten or lost.

Requirements:

- preserve the current working tree;
- do not reset/discard unrelated work;
- treat merged PR #157 and its historical continuation branch as completed evidence, not as the current branch;
- DLB-3A is accepted in PR #172 and may merge under standing authorization; DLB-3B must start from the then-live `main` and must not convert fixed-Drunk misinformation evaluators into a seat-selection score without an explicit evidence-backed consequence contract;
- treat merged PR #165 and `codex/current-only-recovery-cleanup` as completed historical maintenance, not the active development branch;
- completed, fully accepted, mergeable PRs may be marked ready and merged directly under the standing 2026-09-29 authorization; rebase, force-push, destructive history changes, scope broadening, or merging incomplete/failing work still require explicit user authorization.

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
DLB-0 typed intermediate setup              COMPLETE / ACCEPTED
DLB-1 visible-roster deal cutover              COMPLETE / ACCEPTED
DLB-2 legal candidate + hypothetical projector   COMPLETE / ACCEPTED
DLB-3A shadow envelope + DecisionTrace            COMPLETE / ACCEPTED
DLB-3B setup-level consequence bridge              NEXT / EVIDENCE-GATED
App/Host bounded decomposition           GUARDRAIL / NO BROAD CAMPAIGN
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

## 5. Historical acceptance references

Completed RH-E / #157 / correctness-repair detail is historical evidence, not startup context:

- `docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md`
- `docs/archive/checkpoints/sde/SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md`

## 6. Current execution workflow

Use `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md` as authority:

- GitHub Connector is the default repository / PR / CI / result-analysis interface;
- Mini MCP + Luna is the large/truncated-file mechanical execution supplement after Chat fixes the design;
- strong Codex may be used read-only when complete local context is genuinely required before design;
- N3150 remains retired from the default validation path;
- GitHub CI/R2 remains the Android validation and independent acceptance surface.

## 7. Active development lane

DLB-0 accepted executable checkpoint: `f81350ec1341cb83f2b4c2f31d80b9c61c7cec52`; CI #3506 and R2 #3251 GREEN.

DLB-1 accepted executable checkpoint: `d065e21bcf1780fe3375a7fd380815259cc59e5c`; CI #3515 and R2 #3259 GREEN. DLB-1 establishes visible-roster seating as the production pre-Drunk path; the compatibility deal plan is temporary downstream wiring until DLB-4.

DLB-2 accepted executable checkpoint: `73de263327d7530e2d6ac128753aca2fd8766766`; CI #3521 and R2 #3264 GREEN. DLB-2 owns legal Drunk-seat enumeration and immutable hypothetical GameState projection.

DLB-3A accepted checkpoint: `0a59c29af047b91b5d10b62ce4019f60df632e83`; CI #3526 and R2 #3268 GREEN. The shadow setup-precommit surface now emits normal SDE candidates, ecology census, DecisionTrace and replay input; frozen V1 remains deferred and no Drunk candidate is selected.

The current product-development lane is **Drunk late-binding and staged first-night sequencing**.

Authority:

`docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`

Immediate sequence:

```text
DLB-0 typed intermediate setup COMPLETE
-> DLB-1 visible-roster deal cutover COMPLETE
-> DLB-2 legal Drunk candidate + hypothetical projector COMPLETE
-> DLB-3A shadow SDE decision / DecisionTrace COMPLETE
-> DLB-3B setup-level consequence bridge NEXT / EVIDENCE-GATED
-> DLB-4 canonical commit before reveal
-> DLB-4A Experienced assisted UI
-> DLB-5 latest-safe dependency barriers
-> cutover gate before Beginner automatic authority
-> DLB-6 old-contract retirement
-> DLB-7 acceptance
```

The App/Host decomposition audit is a constraint on this work, not a prerequisite campaign:

- A1 preferences and A2 archive storage are independent maintenance slices only;
- H1 waits for DLB-5 and may then extract presentation if the seam is genuinely narrow;
- H2 and A3 remain deferred for post-DLB re-audit;
- R3 generic transaction extraction and the generic setup-effect owner remain NO-GO.

Recovery R0–R6 stays complete. R7 remains separate.

Evidence acquisition continues in parallel. Drunk-assignment evidence may qualify the new DLB decision surface; the existing C5/V2 evidence gate remains separate and still blocks `BEGINNER_CONSERVATIVE_V2`.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb Drunk candidate preference/rejection semantics. DLB-3 is shadow-first; Beginner automatic Drunk authority requires the explicit cutover gate in the DLB route.

## 8. E3/E4 qualification result — no policy delta authorized

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

## 9. Scope still blocked

Do not start `BEGINNER_CONSERVATIVE_V2`, add new policy weights/thresholds, broaden player-count/Traveller scope, or perform production cutover without the required E3/E4 evidence. C5 and SDE-3E remain blocked as recorded in the roadmap.
