# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-26 Australia/Sydney  
> Local continuation branch: `codex/sde-history-prefix-route-closure`; upstream development branch: `sde-3c-decision-trace-shadow-replay`
> C0–C3 integration code HEAD: `8855d4615a4c24a4c3141241f360cfe70d8d239e` — synchronized to both remote branches; GitHub full CI #3451 / run `36126033315` SUCCESS. R2 remains pending because the existing PR/main conflict prevented automatic PR checks.
> Draft PR: **#154 — `SDE-3C: add DecisionTrace shadow replay`**. Keep the PR **draft**; do not mark ready or merge unless the user explicitly says **“授权合并”**. Re-query the live branch/PR/checks before the next executable slice because documentation-only closeout commits may advance the branch HEAD.

## 1. Read first

Use this reduced current-authority set, in order:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md`
4. `docs/SDE_3D2_TRUTH_CREDIBILITY_RED_HERRING_ARCHITECTURE_AUDIT_2026-09-26.md`
5. `docs/SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md`
6. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
7. `docs/NEXT_DEVELOPMENT_HANDOFF.md`
8. `docs/SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`
9. `docs/SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`
10. `docs/SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md`
11. `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`
12. `docs/SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`
13. this handoff

Read older SDE-3A/B/C architecture/completion audits only when a specific implementation-owner or regression question requires them. They are completion evidence, not startup context, and their historical PR/`NEXT` statements are not current authority.

Evidence/provenance references for SDE-3D2 when needed:

- `docs/SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`
- `docs/SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`
- `docs/SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md`
- `docs/SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md`
- `docs/SDE_2D5F_B4F_SILVER_GENERALIZATION_AUDIT_2026-09-23.md`
- `docs/SDE_2D5F_TRAVELLER_MODEL_BOUNDARY_AUDIT_2026-09-22.md` only if Traveller-containing evidence becomes material.

Do not revive archived pre-SDE-3 execution routes as parallel authority.

## 2. Live-state startup rule

Before any executable edit:

1. inspect local Git status, branch and diff; preserve unrelated user files;
2. use the configured Mini MCP `clocktower` workspace by default, following root `AGENTS.md`; use other execution environments only when explicitly required by capability or user instruction;
3. work from `codex/sde-history-prefix-route-closure`; accepted production code checkpoint remains `8855d461`, while later audit/documentation commits may advance the branch; inspect live refs before assuming any recorded SHA is current;
4. before remote acceptance, independently query the exact PR head/base and CI/R2; local validation does not establish remote state;
5. never infer current implementation or acceptance from memory or a historical checkpoint.

Mini MCP is the default configured repository workspace when available. Memory remains advisory only; current code/docs/Git/GitHub state must be re-verified live.

The SDE-3C PR must stay **draft** once created.

## 3. Current program state

Engineering through SDE-2D4 is complete.

SDE-2D5 evidence/calibration has reached a stable checkpoint:

- two independent admitted GOLD decision slices:
  - Ben Burns / `A Stud In Scarlet` / `st-ben-burns`;
  - Evin 2019 first full playthrough / `st-evin`;
- A Fond Farewell is primary-verified but remains Traveller-execution-blocked;
- executable expert counterfactual evidence is green;
- executable ct-01 SILVER external-validity replay is green;
- B4F targeted evidence gaps are explicit;
- broad video/source discovery is no longer the default.

Evidence acquisition is now **parallel / external / continuous**. It no longer blocks SDE-3A/B/C.

Current route:

~~~text
SDE-3A engine / feature / policy contract                  COMPLETE
SDE-3B BEGINNER_CONSERVATIVE_V1 interpretable policy       COMPLETE / T4 ACCEPTED / PR #153 MERGED
SDE-3C shadow recommendation / DecisionTrace / replay       COMPLETE / historical checkpoint preserved
Post-audit correctness repair                               CR-A/CR-B/CR-C COMPLETE / COMBINED ACCEPTANCE GREEN
SDE-3D calibrated policy freeze                             IN PROGRESS / 3D0–3D1 COMPLETE / C4 AUDIT COMPLETE / EDITS AFTER REPAIR
SDE-3E automatic production cutover                         BLOCKED PER SURFACE ON 3D GATES
~~~

## 3A. 2026-09-26 checkpoint to resume from

Stop point for the next conversation:

- C4 / SDE-3D2 architecture/evidence/fanout audit is **COMPLETE** and recorded in `docs/SDE_3D2_TRUTH_CREDIBILITY_RED_HERRING_ARCHITECTURE_AUDIT_2026-09-26.md`.
- C4 production feature edits are no longer blocked by CR-A/B/C. This checkpoint persists the already-accepted repair on the formal development branch through a GitHub tree/commit/ref fast-forward fallback; current docs are synchronized in the same commit.
- CR-A is **COMPLETE / ACCEPTED**:
  - source-scoped perceived-functioning replay replaces reuse of mechanically-credible confirmation for impaired narrative;
  - canonical history remains unchanged and no second mutable perceived-world owner was introduced;
  - representative tests cover role-location, numeric, boolean, persistent Drunk, temporary Poison, accidental truth, and another impaired source staying mechanically credible;
  - the full gate exposed one malformed Fortune Teller fixture whose current NO was independently impossible because a queried seat was already fixed as Demon; the fixture was corrected to a Red-Herring-specific YES→NO conflict without changing production projector semantics.
- CR-B is **COMPLETE / ACCEPTED**:
  - `InformationDecisionRequestIdentity(gameId, requestId)` is propagated through production numeric/boolean adapters;
  - offline replay rejects cross-game same-revision contexts before exact evaluation;
  - runtime shadow rejects stale/mismatched request identity before feature evaluation or trace persistence;
  - production historical shadow checks typed game identity directly; no production parsing of `semanticIdentity`.
- CR-C is **COMPLETE / ACCEPTED**:
  - strict replay-specific nested validation covers action facts/points, observations/bindings, grimoire material and all current proposition variants;
  - unknown nested keys, fractional integer fields, missing required fields and wrong primitive types fail closed;
  - deterministic canonical round-trip remains green; legacy save compatibility was not globally tightened.
- Combined validation-only exact head: `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0`.
- CI #3459: Android FULL `:app:testFull :app:assembleDebug`, ASP contracts, Real Clingo and final CI gate all SUCCESS.
- R2 #3212: SUCCESS.
- PR #156 remains Draft and validation-only; do not merge it.
- No merge, ready transition, policy V2, production cutover, or C4 scoring edit is authorized.

The Oracle working tree is still intentionally dirty and remains behind the remote formal branch. Preserve the staged `AGENTS.md` change and all unrelated/uncommitted documentation; do not reset, clean, checkout-overwrite or bypass stale-state protection. Current `git_state` queries work again, but safe branch lifecycle mutation is still blocked by the dirty tree.

C4 implementation checkpoint:

- T0-A typed truth/credibility feature owner: **COMPLETE**.
- T0-B Red-Herring setup-precommit adapter: **COMPLETE**.
- Exact truth-danger source projector over existing exact epistemic authority: **COMPLETE**.
- Evin E2 semantic regression: **COMPLETE**.
- V1 invariance regression: **COMPLETE** — rejection reasons, survivor equivalence, `SEEDED_HASH_V1` selection and the frozen evidence checkpoint are unchanged when typed truth/credibility is projected.
- DecisionTrace typed persistence: **COMPLETE** — current trace schema is v2; archive envelope remains format v1; schema-v1 traces retain bounded read compatibility and migrate into the current in-memory schema without inventing typed provenance.
- Replay recomputation regression: **COMPLETE** — replay policy input comes from freshly recomputed canonical features rather than the historical trace feature snapshot.
- Latest validation-only Draft PR #158 exact head: `1299fba4fe9f884e468f85639e56ec0b0926794b`.
- CI #3470: Android FULL, ASP contracts, Real Clingo and final gate all SUCCESS.
- R2 #3223: SUCCESS.
- Formal branch schema/invariance checkpoint: `5e563b377df8988cca805d8e34aef7e5823e209f`.
- Earlier formal C4 implementation checkpoint: `d39b336145b19ba29690a5b2dd14ed38273a6cc5`.

Next executable order:

```text
preserve dirty Oracle working tree
-> keep #154, #156 and #158 Draft / unmerged
-> audit actual production caller fanout for truthCredibility / setup-precommit Red Herring
-> identify the minimum wiring needed to populate the typed feature in reachable production shadow/replay paths
-> add fanout/integration regressions while keeping BEGINNER_CONSERVATIVE_V1 policy-neutral
-> rerun exact-head FULL CI/R2 before declaring C4 production integration complete
```

Do not claim CI #3470 or R2 #3223 ran directly on formal commit `5e563b37`; those checks ran on blob-equivalent validation head `1299fba4`. The GitHub tree/commit/ref fallback remains the safe persistence path while the Oracle working tree cannot be cleanly fast-forwarded.

## 4. Why SDE-3 may proceed now

Current evidence is sufficient to establish architecture and qualitative policy dimensions, even though it is not sufficient to freeze final numeric weights / gates.

Stable foundations include:

- rules/canonical producers own legality;
- SDE owns policy among legal alternatives;
- player-controlled targets remain player-owned;
- Spy/Recluse registration is interaction-scoped;
- Drunk/poisoned information may be true or false;
- repeated impaired information needs one shared role-agnostic derived narrative projection over canonical history; no second mutable perceived-world/narrative owner is allowed. The 2026-09-25 audit found a semantic defect in the current projector, and CR-A has now repaired and accepted the shared owner through source-scoped perceived-functioning replay;
- Demon bluffs are a joint SDE output until committed;
- Red Herring is a contextual setup precommit;
- topology is important but not sufficient;
- confirmation chains, healthy-information utility, truth danger / credibility disruption, role-function exposure, bluff usability, narrative coherence and future flexibility remain separate dimensions;
- no opaque global scalar;
- no fixture-specific policy branches.

What remains under-evidenced is **how strongly to trade these dimensions off**, not whether the engine should represent them.

## 5. Current evidence maturity

### Cross-expert foundation

**Red Herring contextual utility**

- Ben: likely Fortune Teller target ecology;
- Evin: Chef truth danger / credibility disruption.

Do not collapse these into a fixed neighbour bonus, role bonus or scalar weight.

### Strong but not final

**Truth danger / credibility disruption**

- Evin primary GOLD explicit rationale;
- ct-02 SILVER explicit Storyteller rationale;
- repeated supporting qualitative evidence.

Keep the dimension. Do not create a deterministic “suppress the strongest truth” rule.

### Single-expert explicit + qualitative

**Impaired-information believability / perceived-world coherence**

Ben supplies explicit primary rationale, but independent GOLD confirmation is still missing.

The architecture may implement the shared persistent narrative mechanism now. Final preference severity remains calibratable.

### Observation-only / weak preference evidence

- role-function exposure severity;
- Demon-bluff triplet ordering;
- exact healthy-information floor;
- quantitative multi-axis tradeoffs.

These do not justify final weights or hard production cutover.

## 6. External evidence track

ClocktowerEvidenceLab should continue collecting full real games.

CampBoardGameHost should request new evidence only for named gaps in:

`docs/SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

Priority:

1. independent experienced Storyteller with explicit healthy-information floor / middle-band rationale;
2. independent-expert impaired-information believability rationale, ideally cross-night;
3. explicit role-function exposure rationale;
4. explicit Demon-bluff triplet rationale;
5. broader executable corpus for multi-axis tradeoffs.

Do not resume broad source collection in this repository.

## 7. Completed engineering checkpoint — SDE-3B

**Do not start by inventing scoring weights or treating unavailable features as neutral.**

First perform a live architecture/fanout audit.

PR/branch boundary:

- PR #150 is the merged SDE-2D5 evidence/calibration checkpoint;
- PR #151/#152 are merged SDE-3A checkpoints;
- PR #153 is the merged SDE-3B checkpoint;
- current branch is `sde-3c-decision-trace-shadow-replay`;
- Draft PR #154 exists on `sde-3c-decision-trace-shadow-replay`; it contains completed SDE-3C plus the current SDE-3D continuation and must remain draft until explicit project-owner authorization;
- historical V1 policy pre-flight is `docs/SDE_3B_BEGINNER_CONSERVATIVE_V1_POLICY_AUDIT_2026-09-23.md`; current V1 release authority is the SDE-3D1 frozen-baseline completion audit;
- completed 3B1 audit is `docs/SDE_3B1_HISTORICAL_LIFECYCLE_INPUT_BINDING_COMPLETION_AUDIT_2026-09-23.md`;
- completed 3B2 audit is `docs/SDE_3B2_CONFIRMATION_CHAIN_COMPLETION_AUDIT_2026-09-23.md`;
- completed 3B3 audit is `docs/SDE_3B3_IMPAIRED_NARRATIVE_COMPLETION_AUDIT_2026-09-23.md`;
- completed 3B4 audit is `docs/SDE_3B4_HEALTHY_INFORMATION_UTILITY_COMPLETION_AUDIT_2026-09-23.md`.
- completed 3B5 architecture audit is `docs/SDE_3B5_ROLE_FUNCTION_EXPOSURE_ARCHITECTURE_AUDIT_2026-09-23.md`;
- completed 3B5 implementation audit is `docs/SDE_3B5_ROLE_FUNCTION_EXPOSURE_COMPLETION_AUDIT_2026-09-24.md`.

Current implemented boundary:

- policy can defer when upstream features or the required strategic topology are unavailable;
- the only generic hard rejection is exact Evil-topology retention reaching zero;
- non-zero retention is never rejected through an invented threshold;
- unsupported preference dimensions remain explicit limitations;
- viable candidates remain one survivor equivalence band until richer feature projectors justify ordering;
- seeded selection is deterministic, weight-free and survivor-only;
- structured shadow carries policy evaluation but production recommendation/confirmation remains unchanged;
- source ability state and actual-state semantic truth are now projected from existing legal-candidate semantics without importing legacy score/probability;
- confirmation-chain impact is now projected per candidate from exact recipient-visible committed history;
- impaired-narrative lifecycle/coherence and healthy-information utility are projected as score-free diagnostics;
- healthy information uses upstream legal `TruthRelation`: both functioning `TRUE_TO_ACTUAL_STATE` and `TRUE_TO_REGISTERED_STATE` are healthy, while actual-state `SemanticTruth` remains a separate feature;
- V1 policy ordering remains strategic-only.

Revised architecture status after the ClocktowerEvidenceLab C0 handoff:

- **3B1 COMPLETE:** lifecycle-safe historical structured shadow, canonical committed-prefix refs, typed contextual input binding, and no-hindsight enforcement are implemented and audited;
- `ActionFactTimeline + EpistemicObservationLog` remain canonical history; no second mutable narrative state exists;
- external owners such as Red Herring / Demon bluffs remain external and are referenced through typed bindings rather than copied into SDE;
- upstream `AbilityState` and canonical action/setup history remain the actual impairment authority; player-facing observation reliability is not used to infer impairment;
- **3B2 COMPLETE:** generic exact confirmation-chain projection is wired into structured shadow with recipient visibility, strict no-hindsight, explicit capability/unavailable handling, bounded R02/R04-style semantic regression, and no policy preference change;
- **3B3 COMPLETE:** the shared score-free impaired-narrative projector derives persistent setup-bound and temporary action-bound episodes from canonical history plus authoritative impairment state; structured shadow integration, R04/R06 E2 regressions and policy non-consumption are green;
- **3B4 COMPLETE:** healthy-information utility derives usable/independent whole-table routes, redundancy/contradiction, route loss and last-route removal from canonical history plus existing exact/confirmation evidence; baseline-already-infeasible history is guarded and legal registered truth remains healthy; R01/R04 bounded E2 regressions are green; V1 policy remains unchanged;
- **3B5 COMPLETE:** generic score-free role-function exposure semantics cover direct/new/already/confirmation-amplified and forced/avoidable exposure; registration ambiguity is the first rules-backed mechanism; canonical history/confirmation are reused; bounded `goldcand-ben-03` E2 regression is green; V1 policy remains unchanged;
- **3B6 COMPLETE:** the E3 eligibility audit authorizes no new soft ordering at this checkpoint; `preference-evidence-not-authorized` is explicit, survivor ties remain intact, and no new policy reason was introduced.
- projector completion and semantic replay may make a dimension policy-eligible, but V1 is now immutable; any newly authorized candidate preference/rejection must enter through a new explicit policy version.

Evidence authority levels are now explicit:
- E1 architecture/feature existence — use now;
- E2 real-game semantic regression — use after structure/projector exists;
- E3 qualified expert qualitative policy — use only after feature stability;
- E4 numeric thresholds/tradeoffs — defer to SDE-3D.

## 8. Target SDE-3A contracts

The target typed architecture is:

~~~text
DecisionCandidate
    legal outcome at one exact lifecycle stage

DecisionFeatures
    interpretable independent consequence dimensions

PolicyEvaluation
    survivor / rejection / preference reasons
    policy version

DecisionTrace
    replayable diagnostic record
~~~

Feature families should remain explicit:

- strategic topology / cover;
- confirmation-chain structure;
- healthy-information utility;
- truth danger / credibility disruption;
- role-function exposure;
- impaired narrative coherence / detectability;
- bluff usability / route diversity;
- future flexibility.

V1 must not force them into one opaque score.

## 9. Target SDE-3B policy

First provisional profile:

`BEGINNER_CONSERVATIVE_V1`

Future policy design shape (not the frozen V1 implementation):

~~~text
legal candidates
    ↓
hard lifecycle / legality boundaries
    ↓
catastrophic / near-catastrophic rejection
    ↓
ordered interpretable soft priorities
    ↓
equivalence band
    ↓
seeded random selection
~~~

Allow conservative qualitative policy before final numeric calibration.

Historical design intentions below are future policy hypotheses, not accepted V1 behavior. Frozen V1 implements exact zero-topology rejection and `SEEDED_HASH_V1` survivor equivalence only; it has no ordered soft priorities. Additional predicates require qualified evidence and a new version:

- avoid catastrophic confirmation / Evil-topology collapse when alternatives exist;
- do not leave Good with effectively no usable information;
- preserve a coherent impaired perceived world;
- treat obvious role-function exposure as a contextual cost;
- evaluate Red Herring in downstream information context;
- prefer usable beginner bluff routes;
- preserve future flexibility;
- use randomness instead of fake precision when evidence does not distinguish survivors.

## 10. SDE-3C trace/replay requirement

Current implementation checkpoint:

- **3C0 COMPLETE:** architecture/fanout/persistence ownership audit is committed; DecisionTrace is explicitly diagnostic/replay state, never canonical game state.
- **3C1 COMPLETE:** structured production shadow carries deterministic V1 `PolicySelection?`; Ready selects a survivor using stable decision identity + canonical game seed, Deferred has no selection, and visible recommendation/confirmation remains untouched.
- **3C2 COMPLETE:** `DecisionTrace` and a policy-version-neutral Ready/Deferred policy snapshot are implemented with schema version, evidence checkpoint, lifecycle/revision, canonical prefix reference, complete candidate IDs, typed features, recommendation and pending/committed actual-choice shape.
- **3C3A COMPLETE:** `DecisionTraceArchive` is a separate immutable replay archive keyed by canonical game/decision/lifecycle/revision/policy identity. Only `Global` history-prefix traces may enter it; identical append is idempotent and same-key different-content append fails closed.
- 3C1/3C2/3C3A were accepted at remote HEAD `a75f482dd5a9aa5a128525c2cfb47be127ded72b` with CI #3432 and R2 #3187 SUCCESS.
- **3C3B COMPLETE:** strict deterministic `DecisionTraceArchiveJsonCodec`, stateless immutable-archive `DecisionTraceArchiveStore`, dedicated SharedPreferences transport, and tests-first malformed/version/duplicate/conflict/canonical-prefix/idempotence coverage are implemented. Persistence is not wired as an automatic side effect of the read-only shadow evaluator; actual authoritative-choice correlation remains 3C4.
- Oracle `test:fast` was invoked before and after the 3C3B implementation, but both attempts are blocked during Gradle configuration before Kotlin compilation because the Oracle host has no Android SDK. Independent GitHub acceptance at exact HEAD `9cffa4e94b088342e1808c9945febb790a0b0232` is CI #3433 SUCCESS and R2 #3188 SUCCESS.
- **3C4 COMPLETE:** authoritative correlation requires exact semantic identity, source revision, complete candidate domain, lifecycle, matching committed observation, and a canonical post-commit session view proving the record is actually persisted. `DecisionTraceArchive` permits only the narrow same-key `Pending -> Committed` actual-choice transition; exact retries are idempotent and conflicts fail closed. Independent GitHub acceptance at exact code HEAD `bf5c2c763e33f69ce6a12567be25e5224c10270b` is CI #3436 SUCCESS and R2 #3191 SUCCESS.
- Oracle `test:fast` was invoked before and after the 3C4 production implementation, but both attempts remain blocked during Gradle configuration before Kotlin compilation because the Oracle host has no Android SDK.
- **3C5 COMPLETE:** replay consumes freshly recomputed policy-neutral features from the canonical structured shadow path rather than persisted old trace features; explicit policy-version runners are selected through a fail-closed registry, production registers only real V1, replay preserves source identity/prefix/domain plus authoritative actual choice, and results are returned without mutating history or auto-persisting. Independent GitHub acceptance at exact code HEAD `db7d5575dd28dc5f584be34b3556b511ccaf3e35` is CI #3440 SUCCESS and R2 #3195 SUCCESS.
- Oracle `test:fast` was invoked before and after the 3C5 production implementation, but both attempts remain blocked during Gradle configuration before Kotlin compilation because the Oracle host has no Android SDK.

Before automatic cutover, persist a replayable diagnostic trace with:

- policy version;
- evidence/corpus checkpoint;
- lifecycle / state revision;
- complete legal candidate IDs;
- typed feature values;
- rejection/survival reasons;
- recommended candidate;
- actual committed candidate;
- manual override flag;
- optional structured/textual override reason.

A human override is useful calibration evidence but is **not automatically a quality label**.

Historical game state must be replayable under multiple policy versions without mutating historical truth.

## 11. Continuous calibration model

No uncontrolled production online learning.

Do not train on win/loss as a direct Storyteller-quality label.

Long-term loop:

~~~text
ClocktowerEvidenceLab / exported DecisionTrace
        ↓
versioned evidence corpus
        ↓
material DecisionSlices
        ↓
CampBoardGameHost production legality
        ↓
feature projection
        ↓
offline policy comparison / replay
        ↓
review / regression gate
        ↓
explicit new policy version
~~~

Policy evolution is expected and intentional.

## 12. SDE-3D / 3E gate boundary

SDE-3D is active. Its current authority is:

`docs/SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`

The accepted `BEGINNER_CONSERVATIVE_V1` semantics are now frozen as an immutable provisional baseline. Its production definition binds policy version `BEGINNER_CONSERVATIVE_V1`, evidence checkpoint `sde-3b-merged-2026-09-24`, and selector method `SEEDED_HASH_V1`. Do not mutate those semantics under V1 or create V2 until a real evidence-authorized candidate-ordering or rejection semantic exists.

Still evidence-gated:

- healthy-information floor / middle-band policy strength;
- exact role-function exposure severity;
- bluff-triplet preference ordering;
- independent-expert impaired-believability / cross-night continuity strength;
- quantitative multi-axis weights only if a future policy actually chooses to require them.

Do not require all of those gaps to close before every production surface can advance. SDE-3D/3E gates are surface-scoped.

### SDE-3E automatic production cutover

Do not make SDE the sole automatic authority for a decision surface before:

- the exact policy version for that surface is frozen;
- all features consumed by that policy surface are structurally stable;
- every active rejection/preference has evidence authority appropriate to its strength;
- canonical real-game replay is accepted for that version/surface;
- DecisionTrace / actual-choice correlation remains available;
- manual Experienced-mode override remains available;
- unsupported/unavailable dimensions have an explicit legacy/manual/deferred fallback;
- the old authority has a reviewed fanout-retirement plan.

A large V1 survivor equivalence class plus seeded selection is not, by itself, sufficient reason for automatic cutover.

## 13. Legacy cleanup boundary

Do not delete legacy recommendation authority at the beginning of SDE-3A.

Retirement order:

~~~text
typed SDE-3 contracts
→ shadow evaluation
→ provisional advisory policy
→ evidence-calibrated policy
→ production cutover
→ fanout retirement audit
→ delete obsolete legacy authority
~~~

Known retirement targets include:

- `recommendation/dynamic/ConsequenceEvaluator`;
- heuristic `evilAdvantage` / related stale pressure state where no longer authoritative;
- setup `bluffDifficulty` strategic authority after SDE bluff ownership is validated;
- superseded recommendation tests/scaffolding after stronger typed coverage exists.

## 14. Validation

Follow `AGENTS.md` and `TESTING_STRATEGY.md`.

For the next architecture/audit step, documentation-only findings do not require Android regression.

Once shared contracts change:

- perform mandatory producer/consumer fanout audit;
- use the narrowest durable typed test at the true owner;
- run T1/T2 according to affected semantics;
- use T3 evidence harnesses only when their evidence surface is affected;
- use `[full-ci]` at the logical acceptance checkpoint.

Do not manufacture RED for behavior-preserving refactoring.

## 15. Historical evidence checkpoint

The bounded B4F SILVER workload is:

`:app:sde2D5FB4FSilverGeneralization`

Accepted evidence run:

- run #3;
- head `17254848e47abf05c1b7648948ae8b47a89f9e6f`;
- success;
- about 7 minutes.

The old external-human auto-workflow that could take ~2h48m has been retired.

## 16. Next conversation task

SDE-3A and SDE-3B are merged. SDE-3C is structurally COMPLETE on Draft PR #154 and must remain draft/unmerged until explicit project-owner authorization.

SDE-3D0 and SDE-3D1 are COMPLETE. The current conclusion is:

- V1 is an immutable provisional baseline; trace/replay provenance now derives from its frozen definition;
- the V1 selector has a stable `SEEDED_HASH_V1` golden and must not silently change under the same version;
- SDE-3D is partially evidence-blocked, not globally blocked;
- production cutover is surface-scoped;
- quantitative Gap E is not mandatory unless a future policy explicitly adopts numeric weighting;
- no placeholder V2 is allowed.

Immediate next action:

C0–C3 remain accepted historical checkpoints at production code HEAD `8855d461`, with full CI #3451 GREEN. The later 2026-09-25 audit reproduced three correctness defects: impaired-narrative semantics can confuse mechanical impairment with player-believable continuity; replay does not explicitly bind decision context to the same game; nested replay decoding is not fully fail-closed. It also identified two non-blocking follow-ups: cold-start App replay request reconstruction and serialized diagnostic persistence/timing. See `SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md`.

1. re-query live branch/status and preserve unrelated user work;
2. implement **CR-A impaired-narrative semantic correctness** at the shared projector owner with role-agnostic cross-information-shape RED/GREEN coverage;
3. implement **CR-B typed game/request identity binding** and reject cross-game same-revision replay before exact evaluation/trace persistence;
4. implement **CR-C strict nested replay decoding** without globally breaking legacy save compatibility;
5. run affected regressions, FAST/debug assembly and `git diff --check`; re-audit producer/consumer fanout for any shared typed contract change;
6. only after CR-A/B/C are accepted, begin C4 / SDE-3D2 production feature work for truth danger / credibility disruption and contextual Red-Herring projection. The architecture/evidence/fanout audit may be prepared earlier;
7. track IF-D durable App replay capture/rebuild and RH-E serialized background persistence/complete timing as required follow-ups before runtime traces are treated as a durable calibration corpus or runtime scope is broadened;
8. keep `BEGINNER_CONSERVATIVE_V1` immutable, keep #154 draft, and treat main integration conflict, exact-head remote CI/R2 and applicable T4 as separate acceptance gates. No merge or ready transition without explicit user authorization.