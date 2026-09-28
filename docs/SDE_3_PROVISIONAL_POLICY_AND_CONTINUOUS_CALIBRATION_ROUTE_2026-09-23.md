# SDE-3 — Provisional Policy and Continuous Calibration Route

> Updated: 2026-09-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **LONG-LIVED SDE-3 ARCHITECTURE / CALIBRATION ROUTE; LIVE EXECUTION STATUS IS ROADMAP/HANDOFF**  
> Parent architecture: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`  
> Current evidence gate: `docs/SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md`  
> Evidence background: `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`  
> Historical SDE-3A completion audit: `docs/SDE_3A_ENGINE_FEATURE_POLICY_CONTRACT_AUDIT_2026-09-23.md`  
> Historical SDE-3C completion reference: `docs/SDE_3C5_MULTI_POLICY_REPLAY_COMPLETION_AUDIT_2026-09-24.md`  
> Current SDE-3D gate audit: `docs/SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`  
> Frozen V1 baseline: `docs/SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md`  
> Targeted evidence contract: `docs/SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`  
> Current lifecycle amendment: `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`

## 1. Decision

Execution amendment (updated 2026-09-29): the historical C0–C3 integration closure, CR-A/B/C correctness repair, C4/3D2, IF-D and RH-E are complete; the finished integration/correctness routes are archived under `archive/checkpoints/sde/`. Engineering prerequisite completion does not itself authorize a new policy version.

The 2026-09-29 DLB route is a newer **product lifecycle amendment**: Drunk seat assignment is late-bound after shown identities are seated, then canonically committed before identity reveal; first-night Storyteller-controlled decisions are staged at latest-safe dependency barriers. Older SDE documents that treated exact Drunk identity as an already committed setup input are historical for that ownership assumption.

This does **not** mutate `BEGINNER_CONSERVATIVE_V1`. DLB-3 begins shadow-only. Any Drunk-assignment preference/rejection consumed by production requires an explicitly versioned decision-surface contract and its own cutover gate. The roadmap remains the single live status authority.

The Storyteller project must continue even though final expert-policy calibration is incomplete.

Evidence shortage blocks **unsupported policy preferences and per-surface automatic production cutover**, not freezing the accepted V1 baseline or continuing the engineering architecture that makes future calibration possible.

The route is therefore split:

~~~text
SDE-3A engine / feature / policy contract                  COMPLETE / PR #151/#152
SDE-3B BEGINNER_CONSERVATIVE_V1 interpretable policy       COMPLETE / PR #153 MERGED
SDE-3C shadow recommendation / DecisionTrace / replay       COMPLETE / historical checkpoint preserved
Post-audit correctness repair                               CR-A / CR-B / CR-C BEFORE 3D2 PRODUCTION EDITS
SDE-3D calibrated policy freeze                             IN PROGRESS / 3D0-3D1 COMPLETE / PARTIALLY EVIDENCE-BLOCKED
SDE-3E automatic production cutover                         BLOCKED PER SURFACE ON 3D GATES
~~~

External evidence collection continues in parallel. For the DLB surface, reconstructable expert Drunk-assignment evidence feeds the shadow-to-production cutover gate; for C5/V2, the existing E3/E4 qualification rules remain unchanged. These are distinct authorization gates.

## 2. Product strategy

The first usable automatic Storyteller policy is not intended to imitate a world-class human Storyteller perfectly.

Its target is:

> **For ordinary / beginner Trouble Brewing tables, avoid clearly destructive Storyteller decisions, preserve playable information for Good, preserve credible Evil narratives, keep impaired information coherent, and make every recommendation explainable.**

Name the first provisional profile:

`BEGINNER_CONSERVATIVE_V1`

It is deliberately conservative and versioned.

Later improvements require a new qualified policy version; the frozen V1 definition itself must not change.

## 3. Stable architecture versus continuously calibrated policy

Separate the long-lived engine from the changeable policy.

~~~text
canonical rules / lifecycle state
        ↓
legal candidate generation
        ↓
hypothetical consequence projection
        ↓
typed interpretable feature vector
        ↓
versioned StorytellerPolicy
        ↓
accepted/rejected candidates + reasons
        ↓
seeded selection among healthy survivors
        ↓
DecisionTrace
~~~

The top three layers should become increasingly stable.

The policy layer is expected to evolve:

~~~text
BEGINNER_CONSERVATIVE_V1
BEGINNER_CONSERVATIVE_V2
...
~~~

A policy revision must not require rewriting rules or creating fixture-specific branches.

## 4. SDE-3A — typed candidate / feature / policy contract

### 4.1 Candidate contract

A candidate represents one legal Storyteller-controlled output at one exact lifecycle stage.

It must preserve:

- decision type;
- current lifecycle / round;
- source ability / interaction;
- committed state revision;
- already-made player-controlled choices;
- legal outcome identity;
- hypothetical semantic proposition/effect;
- production-owned legality provenance.

Do not mix illegal-candidate filtering with policy preference.

### 4.2 Feature contract

The engine should project explicit independent dimensions rather than one opaque score.

Candidate dimensions may include:

#### Strategic structure

- Demon cover retention;
- Evil topology retention;
- Evil cover retention;
- forced-Good / forced-Evil structure;
- confirmation-chain / multi-channel collapse.

#### Healthy information

- remaining actionable healthy information;
- role-information utility;
- confirmation value;
- whether the candidate would leave Good with too little usable structure.

The existence of this dimension is required now. Numeric healthy-information gates remain unfrozen.

#### Truth danger / credibility disruption

- how damaging a functioning truthful channel is to the actual Evil topology;
- whether a legal misinformation route can plausibly contaminate confidence in that channel;
- whether the effect is lifecycle-valid rather than hindsight leakage.

Evin's Red-Herring rationale plus SILVER rationale evidence justify retaining this dimension.

#### Role-function exposure

- whether the candidate directly exposes mechanics whose intended function depends on ambiguity, e.g. Spy / Recluse interactions.

This is a soft contextual dimension until stronger calibration exists.

#### Impaired narrative

- semantic truth / falsehood;
- consistency with previously committed impaired information;
- perceived-world continuity;
- impairment detectability;
- transition cost when the prior perceived world becomes impossible.

The shared owner must be role-agnostic. Named roles contribute legal domains / semantic propositions only.

#### Bluff usability

For Demon-bluff joint outputs:

- claim burden;
- cadence / ongoing maintenance burden;
- narrative-route diversity;
- collision with actual Good information;
- collision with Drunk shown identity;
- support from available misinformation channels;
- future recoverability / fallback routes.

No fixed role ranking is authorized.

#### Future flexibility

- whether the choice unnecessarily consumes future misinformation / registration / narrative options;
- whether it creates brittle commitment chains.

### 4.3 No opaque global scalar in V1

V1 must not invent arbitrary numeric weights merely to force total ordering.

Preferred structure:

~~~text
hard legality
    ↓
hard / near-hard safety gates
    ↓
ordered interpretable soft priorities
    ↓
equivalence band
    ↓
seeded random selection among healthy survivors
~~~

A later evidence-driven policy may introduce calibrated bands or pairwise ordering, but the feature surfaces must remain inspectable.

## 5. SDE-3B — BEGINNER_CONSERVATIVE_V1

Future versions may use qualified qualitative constraints without numeric thresholds; frozen V1 remains limited to its accepted zero-topology gate and seeded survivor equivalence.

### 5.1 Hard boundaries

Always preserve:

- production legality;
- lifecycle ownership;
- immutable committed history;
- player-controlled target ownership;
- setup persistence of shown identity / Red Herring / revealed Demon bluffs;
- interaction-local Spy/Recluse registration semantics.

### 5.2 Future policy hypotheses — not frozen V1 predicates

Frozen V1 rejects only exact zero Evil topology and otherwise preserves survivor equivalence. The following are future policy hypotheses requiring independently qualified evidence and a new version, not permissions to expand V1:

- catastrophic Evil-topology / confirmation collapse when healthy alternatives exist;
- a whole bundle that leaves Good with effectively no usable healthy information;
- an impaired output that needlessly contradicts an already-established believable perceived world;
- an avoidable direct role-function exposure with healthier alternatives;
- a Demon bluff triplet that is mechanically legal but obviously unusable for the target beginner because all routes impose high claim burden or direct collisions.

These conditions must be expressed through generic features, not named fixture branches.

### 5.3 Contextual soft priorities

V1 does not rank survivors by the following dimensions. Future evidence-backed versions may investigate:

- more credible Evil cover without making Good information inert;
- fewer destructive confirmation chains;
- coherent impaired narratives;
- contextual Red-Herring utility;
- believable misinformation rather than false-at-all-costs;
- usable, diverse bluff routes;
- preserved future flexibility.

If evidence cannot distinguish several survivors, use seeded randomness rather than fake precision.

## 6. SDE-3C — shadow recommendation, DecisionTrace and replay

SDE-3C is required before automatic cutover.

### 6.1 DecisionTrace

For every discretionary interaction, persist a replayable trace containing at least:

- policy version;
- evidence/corpus version or evidence checkpoint identifier;
- decision context / lifecycle revision;
- complete legal candidate IDs;
- projected interpretable features;
- rejection / survival reasons;
- recommended candidate;
- actual committed candidate;
- whether a human manually overrode the recommendation;
- optional structured override reason when supplied.

The trace is diagnostic/replay data, not a second canonical game state.

### 6.2 Human override data

Experienced/manual Storyteller mode may choose a different legal candidate.

An override can become useful future calibration evidence:

~~~text
engine recommended A
human selected B
reason = optional structured / textual rationale
~~~

Do not treat an override as automatically proving A was wrong or B was expert-quality.

### 6.3 Historical replay

A recorded real or app-hosted game should be replayable under multiple policy versions:

~~~text
same committed game history
    ↓
Policy V1 recommendation
Policy V2 recommendation
Policy V3 recommendation
~~~

This enables offline comparison without mutating historical truth.

## 7. Continuous offline calibration

Do not perform uncontrolled online learning in production.

Game outcome is not a Storyteller-decision quality label.

The supported loop is:

~~~text
ClocktowerEvidenceLab / exported app traces
        ↓
versioned evidence corpus
        ↓
material DecisionSlices
        ↓
CampBoardGameHost production legality reconstruction
        ↓
candidate feature projection
        ↓
offline policy comparison / replay
        ↓
evidence review / regression gate
        ↓
new explicit policy version
~~~

Policy updates are deliberate releases.

The app must not silently change weights from one local game result.

## 8. ClocktowerEvidenceLab boundary

ClocktowerEvidenceLab owns long-term external source collection and canonical real-game history.

CampBoardGameHost owns production legality, counterfactual candidate generation, feature projection and policy evaluation.

EvidenceLab should export facts such as:

- source / provenance;
- Storyteller independence key;
- complete or material committed game state;
- observed Storyteller choice;
- explicit rationale;
- timestamps;
- unknown fields;
- evidence tier / verification status.

It should **not** freeze CampBoardGameHost legal alternative sets as source truth.

CampBoardGameHost reconstructs those alternatives with its current production owners.

Current target gaps are defined in:

`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

Targeted evidence acquisition continues in parallel with SDE-3A/B/C.

## 9. SDE-3D — calibrated policy freeze — IN PROGRESS

SDE-3D0 established that calibration/freeze is **surface-scoped**, not one monolithic gate. The full decision is recorded in:

`SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`

The accepted `BEGINNER_CONSERVATIVE_V1` semantics are now frozen as an immutable provisional baseline. Its release definition binds `BEGINNER_CONSERVATIVE_V1`, evidence checkpoint `sde-3b-merged-2026-09-24`, and selection method `SEEDED_HASH_V1`. V1 must not silently absorb later evidence-driven ordering or selector changes.

Current evidence gaps still block specific policy semantics:

- healthy-information floor / middle-band preference strength;
- role-function exposure severity;
- independent-expert impaired-information believability / cross-night continuity strength;
- Demon-bluff triplet preference ordering;
- quantitative multi-axis tradeoffs **only if** a future policy chooses to require numeric weighting.

SDE-3D must still not:

- freeze arbitrary scalar weights;
- publish unsupported player-count thresholds;
- convert one expert observation into a deterministic rule;
- claim calibrated expert parity;
- create a placeholder V2 without a real semantic policy delta.

Current execution slices are:

1. 3D0 calibrated freeze / cutover-gate architecture — COMPLETE;
2. 3D1 V1 immutable baseline freeze — COMPLETE at `f562887cf4e90d02d364eef5534a1f709922a0f8`, CI #3446 / R2 #3201;
3. 3D2 calibration-ready missing feature completion — AFTER integration C1–C3; begin with truth danger / credibility disruption plus contextual Red-Herring descriptive projection while keeping V1 policy-neutral;
4. 3D3 first evidence-authorized policy delta / first real V2 — waiting for a qualifying E3 predicate;
5. 3D4 V1/V2 canonical real-corpus replay;
6. 3D5 surface-scoped calibrated freeze.

Evidence collection can progressively unblock individual policy dimensions without blocking unrelated surfaces.

## 10. SDE-3E — automatic production cutover — BLOCKED PER SURFACE

Automatic cutover is evaluated for each decision surface. A surface requires:

1. stable SDE-3A typed contracts;
2. regression-safe SDE-3B/3D policy semantics;
3. SDE-3C DecisionTrace / replay support;
4. an explicit frozen policy version for that surface;
5. stable projectors for every feature the policy actually consumes;
6. evidence authority appropriate to every active rejection/preference;
7. accepted canonical real-game replay for the policy version/surface;
8. product UX preserving manual Experienced-mode override;
9. explicit legacy/manual/deferred fallback for unsupported or unavailable dimensions;
10. a completed fanout-retirement plan before removing the old authority.

A technically available seeded V1 selection does not authorize cutover when material dimensions remain unresolved and the policy is choosing among a large equivalence class. Those surfaces remain shadow/advisory, manual, legacy-authoritative or explicitly deferred.

## 11. Legacy retirement boundary

Do not delete legacy behavior merely because SDE-3A begins.

Retirement is staged:

~~~text
new typed engine
    ↓
shadow parity / trace
    ↓
provisional advisory policy
    ↓
evidence-calibrated policy
    ↓
production cutover
    ↓
legacy fanout audit
    ↓
delete obsolete authority
~~~

`ConsequenceEvaluator`, legacy `evilAdvantage` heuristics, setup bluff-difficulty authority and related stale policy paths remain retirement targets, not immediate deletion targets.

## 12. Testing strategy

SDE-3A contract work should be tested at typed ownership boundaries.

Expected pattern:

- candidate contract → focused legality/orchestration tests;
- feature projector → deterministic typed feature tests;
- derived impaired-narrative projector → cross-role / cross-lifetime canonical-history tests;
- policy → table-driven qualitative ordering / rejection tests;
- DecisionTrace → persistence/replay contract tests;
- production shadow wiring → integration tests;
- expensive evidence replay → dedicated T3 harness, not ordinary FAST.

Do not turn expert source examples into fixture-specific production tests.

## 13. Immediate implementation order

Start every new shared-contract or production-feature slice with the AGENTS architecture/fanout pre-flight.

Execution-status boundary:

- this document remains the architectural / policy-evidence route for SDE-3;
- it is **not** the live PR/branch/checkpoint authority;
- SDE-3C, CR-A/B/C, C4/SDE-3D2 and IF-D are complete on the formal continuation;
- RH-E is the next executable integration-hardening slice;
- `BEGINNER_CONSERVATIVE_V1` remains immutable;
- C5 / the first real V2 remains blocked until a genuinely qualifying E3/E4 predicate exists;
- current branch, PR, acceptance and execution order belong only to `CURRENT_DEVELOPMENT_ROADMAP.md` and `NEXT_DEVELOPMENT_HANDOFF.md`.

Recommended architectural sequence from the current checkpoint:

1. complete RH-E serialized diagnostic persistence/timing/retention hardening without changing V1 policy;
2. continue targeted expert evidence acquisition independently;
3. wait for qualifying E3/E4 evidence before creating a real `BEGINNER_CONSERVATIVE_V2`;
4. compare policy versions on the same canonical histories through the existing replay machinery;
5. freeze and cut over only decision surfaces whose feature, evidence, replay and fallback gates are satisfied.

### Expert-evidence authority rule

Evidence may enter at four different stages and must not be promoted early:

- **E1 architecture evidence:** whole games justify feature existence/lifecycle dependencies;
- **E2 semantic regression evidence:** reconstructed prefixes validate generic projectors after structural seams exist;
- **E3 qualitative policy evidence:** explicit qualified rationale or strong cross-expert support may become typed soft reasons only after feature stability;
- **E4 calibration evidence:** numeric thresholds/weights/tradeoff strength are required only for policy semantics that actually depend on numeric calibration.

An observed expert choice without rationale is never, by itself, a preference label. Synthetic/extreme fixtures remain diagnostic rather than primary calibration truth, and modifier-rich real Trouble Brewing bundles must not be excluded merely to obtain cleaner metrics.

## 14. Live execution handoff

This route no longer carries a rolling “next conversation” checklist. That duplication made completed checkpoints appear current after the implementation had moved on.

For live work, use:

- `CURRENT_DEVELOPMENT_ROADMAP.md` — single status / priority authority;
- `NEXT_DEVELOPMENT_HANDOFF.md` — compact executable continuation;
- `docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md` — preserved pre-RH-E completion/cleanup evidence.

The architectural constraints above remain authoritative unless superseded explicitly. No V2, numeric threshold, global weight, automatic cutover or Traveller expansion is implied by feature completion alone.
