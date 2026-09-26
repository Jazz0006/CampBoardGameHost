# SDE-3 — Provisional Policy and Continuous Calibration Route

> Updated: 2026-09-25 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT SDE-3 EXECUTION AUTHORITY**  
> Parent architecture: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`  
> Current evidence authority: `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`  
> Historical SDE-3A completion audit: `docs/SDE_3A_ENGINE_FEATURE_POLICY_CONTRACT_AUDIT_2026-09-23.md`  
> Historical SDE-3C completion reference: `docs/SDE_3C5_MULTI_POLICY_REPLAY_COMPLETION_AUDIT_2026-09-24.md`  
> Current SDE-3D gate audit: `docs/SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`  
> Frozen V1 baseline: `docs/SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md`  
> Targeted evidence contract: `docs/SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

## 1. Decision

Execution amendment (2026-09-25): the [integration closure contract](SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md) reached an accepted C0–C3 checkpoint at `8855d461` with full CI #3451 success. A later audit found three bounded correctness defects; [post-audit correctness repair](SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md) CR-A/B/C is now the hard production prerequisite before C4/3D2 feature edits. R2/base integration remains pending. Roadmap remains the single status authority.

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

External evidence collection continues in parallel rather than sitting on the critical engineering path.

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

Current control-plane state:

- PR #150 is merged as the SDE-2D5 evidence/calibration checkpoint;
- PR #151/#152 are merged SDE-3A checkpoints;
- PR #153 is merged SDE-3B / accepted V1 checkpoint;
- Draft PR #154 contains SDE-3C plus the current SDE-3D0/3D1 continuation and must remain draft until explicit user authorization;
- SDE-3C0–3C5 are COMPLETE;
- SDE-3D0 and SDE-3D1 are COMPLETE;
- the accepted V1 code checkpoint is `f562887cf4e90d02d364eef5534a1f709922a0f8`;
- documentation-only synchronization commits may advance the branch after the accepted code checkpoint; query the live branch/PR head and checks rather than relying on a hard-coded documentation SHA.

Recommended sequence:

0. **Integration C0–C3 historical checkpoint:** exact history correlation, durable replay transport, offline vertical slice, measured diagnostic runtime shadow, as specified in the integration closure contract;
1. **Post-audit correctness repair:** complete CR-A impaired-narrative semantic correctness, CR-B typed game/request identity binding, and CR-C strict nested replay decoding. These are hard prerequisites for SDE-3D2 production feature edits;
2. **SDE-3D2 — calibration-ready missing feature completion:** after CR-A/B/C, perform an architecture/evidence/fanout audit for truth danger / credibility disruption and contextual Red-Herring downstream policy input before production edits;
3. identify and reuse the existing owners for Red Herring setup commitment, legal candidate semantics, canonical history/confirmation context, healthy truthful-channel consequences, and exact/topology consequence projection;
4. define the smallest generic descriptive feature seam that represents downstream truth danger / credibility disruption without encoding named-seat, named-role or fixture-specific preference rules;
5. use existing Evin GOLD + SILVER and Ben contextual evidence as E1/E2 justification and semantic regression evidence; feature completion alone must not alter V1 ordering;
6. keep `BEGINNER_CONSERVATIVE_V1` immutable, including its frozen evidence checkpoint and `SEEDED_HASH_V1` selection contract;
7. complete IF-D durable App replay capture/rebuild before runtime traces are treated as a durable real-game calibration corpus, and RH-E persistence/timing hardening before runtime scope is broadened;
8. wait for a genuinely qualifying E3 predicate before creating the first real `BEGINNER_CONSERVATIVE_V2`; do not create a placeholder V2;
9. once a real V2 exists, use SDE-3C5 to replay V1 and V2 over the same canonical real-game histories without mutating historical truth;
10. freeze only cutover-eligible decision surfaces whose active predicates, projectors, evidence authority and replay gates are satisfied;
11. enter SDE-3E per surface, retaining legacy/manual/deferred fallback wherever material dimensions remain unsupported.

### Expert-evidence authority rule

Evidence may enter at four different stages and must not be promoted early:

- **E1 architecture evidence:** whole games justify feature existence/lifecycle dependencies;
- **E2 semantic regression evidence:** reconstructed prefixes validate generic projectors after structural seams exist;
- **E3 qualitative policy evidence:** explicit qualified rationale or strong cross-expert support may become typed soft reasons only after feature stability;
- **E4 calibration evidence:** numeric thresholds/weights/tradeoff strength are required only for policy semantics that actually depend on numeric calibration.

An observed expert choice without rationale is never, by itself, a preference label. Synthetic/extreme fixtures remain diagnostic rather than primary calibration truth, and modifier-rich real Trouble Brewing bundles must not be excluded merely to obtain cleaner metrics.

## 14. Success criteria for the next development conversation

Integration C0–C3 remains an accepted historical checkpoint at exact production code HEAD `8855d461`; full CI #3451 succeeded and R2/base integration remains pending. The next implementation conversation begins **CR-A / CR-B / CR-C correctness repair**. The C4 / SDE-3D2 architecture/evidence/fanout audit may be prepared in parallel, but production feature edits wait for the repair gate.

After CR-A/B/C are accepted, the SDE-3D2 conversation should produce, before production edits:

- one authoritative owner/reuse map for Red Herring commitment, legal candidate semantics, canonical history/confirmation context and downstream consequence projection;
- an explicit account of what current Evin/Ben/SILVER evidence supports at E1/E2 versus what remains below E3;
- one role-agnostic typed descriptive contract for truth danger / credibility disruption and contextual Red-Herring consequence;
- explicit proof that the proposed feature does not duplicate legality, mutate Red Herring ownership, or import legacy scalar scoring;
- a tests-first acceptance plan at the true feature owner plus bounded real-game semantic regression;
- confirmation that V1 remains policy-neutral with respect to the new feature.

No V2, numeric threshold, global weight, automatic cutover or Traveller expansion is authorized by SDE-3D2 itself.