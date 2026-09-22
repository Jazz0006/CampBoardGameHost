# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-23 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Program status

~~~text
D6 decomposition / ownership cleanup                  COMPLETE
EPI-MQ capability boundary                            COMPLETE / PR #135
Exact historical hypothetical bundle seam             COMPLETE / PR #137
First-night experiment contract                       COMPLETE / PR #138
FN-BUNDLE-0 / 1 / 2                                  COMPLETE / PR #139/#140/#142
SDE-0 BEGINNER strategic corpus                       COMPLETE / PR #143
SDE-1 orchestration / lifecycle / shadow              COMPLETE / PR #144
SDE-2D1 Drunk whole-bundle                            COMPLETE / PR #145
SDE-2D2 Demon bluff joint-output                      COMPLETE / PR #146
SDE-2D3 strategic-world quotient                      COMPLETE / PR #147
SDE-2D4 5–15 correctness/performance                  COMPLETE / PR #149
SDE-2D5 calibration / policy evidence                 PARALLEL / PR #150 draft
    D5A–D5E                                            COMPLETE
    D5F infrastructure                                COMPLETE
    D5F-B3 correction                                 HISTORICAL CHECKPOINT
    D5F-B4 engineering / evidence checkpoint          COMPLETE UP TO EXTERNAL WAIT
        B4A clean-corpus retirement                    COMPLETE
        B4B GOLD source discovery                     DEMAND-DRIVEN
        B4C expert reconstruction                     COMPLETE FOR CURRENT ANCHORS
        B4D legal counterfactual recovery              COMPLETE FOR CURRENT EXECUTABLE ANCHORS
        B4E observed-vs-alternative analysis           CROSS-EXPERT CHECKPOINT COMPLETE
        B4F-A executable SILVER replay                 COMPLETE
        B4F-B documentary SILVER comparison            COMPLETE
        B4F-C target specification                     COMPLETE
        B4F-C targeted evidence acquisition            EXTERNAL / CONTINUOUS
D5F-C final gate/band derivation                      BLOCKED ON EVIDENCE
sealed holdout                                        CLOSED
SDE-3A engine / feature / policy contract             CURRENT
SDE-3B BEGINNER_CONSERVATIVE_V1                       NEXT
SDE-3C shadow / DecisionTrace / replay                 NEXT
SDE-3D calibrated policy freeze                       BLOCKED ON EVIDENCE
SDE-3E automatic production cutover                   BLOCKED ON 3D
~~~

## 2. Current branch / PR

Branch: `sde-2d5-calibration-policy-evidence`

PR: **#150 — SDE-2D5: calibrate strategic policy evidence**

PR #150 remains **draft**. Do not merge unless the user explicitly says **“授权合并”**.

Always query live refs before executable edits.

## 3. Current authorities

- First-night policy: [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md)
- External evidence seed: [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv)
- Current SDE-3 execution route: [`SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`](SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md)
- Global SDE architecture: [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- Traveller boundary audit for primary-verified A Fond: [`SDE_2D5F_TRAVELLER_MODEL_BOUNDARY_AUDIT_2026-09-22.md`](SDE_2D5F_TRAVELLER_MODEL_BOUNDARY_AUDIT_2026-09-22.md)
- Evin source audit: [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md)
- Evin primary reconstruction: [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md)
- Cross-expert convergence / B4F entry: [`SDE_2D5F_CROSS_EXPERT_CONVERGENCE_AND_B4F_ENTRY_AUDIT_2026-09-23.md`](SDE_2D5F_CROSS_EXPERT_CONVERGENCE_AND_B4F_ENTRY_AUDIT_2026-09-23.md)
- B4F bounded SILVER generalization: [`SDE_2D5F_B4F_SILVER_GENERALIZATION_AUDIT_2026-09-23.md`](SDE_2D5F_B4F_SILVER_GENERALIZATION_AUDIT_2026-09-23.md)
- B4F targeted evidence-gap contract: [`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`](SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md)

## 4. Frozen architecture / policy decisions

- Optimize only variables still controllable at the current lifecycle stage.
- Rules/canonical producers own legality; epistemic/topology layers own consequences; SDE owns policy/selection.
- Drunk shown identity is setup-persistent; committed information is immutable.
- Demon bluffs and Red Herring are persistent once revealed/committed.
- Fortune Teller target pair and Poisoner target are player-controlled.
- Spy/Recluse registration is per interaction.
- BEGINNER thematic prior: Spy normally registers as Good/Townsfolk/Outsider; Recluse normally registers as Evil/Minion/Demon.
- Actual registration is a fallback when the thematic default materially improves whole-bundle health.
- Avoid Librarian -> Recluse when a healthy alternative exists; strongly avoid Investigator -> Spy when a healthy alternative exists.
- Chef/Empath are rule-determined only when every legal registration branch yields the same healthy value.
- Drunk/poisoned information may accidentally be true; repeated/history-dependent impaired information must use a persistent role-agnostic narrative state so later outputs remain coherent with the perceived world already established.
- Strategic Evil topology is primary structural evidence, but role-information utility, confirmation chains, role-function exposure, bluff usability and information floor remain separate.
- No opaque global scalar.
- No named-role / known-fixture policy patches for cross-interaction coherence. Role-specific code owns legality only; shared semantic/history policy owns narrative continuity.

### Shared impaired-narrative implementation invariant

When D5F/SDE-3 reaches cross-night impaired information, implement one reusable persistent narrative-state mechanism over generic information propositions/history.

Do not create separate coherence algorithms for individual roles or calibration fixtures.

Acceptance for that future implementation must show:

- one shared owner for persistent impaired narrative intent/history;
- role adapters contribute legal domains / semantic propositions only;
- previous committed observations constrain later selection;
- the mechanism generalizes across multiple information shapes;
- tests prove the abstraction at its shared owner plus representative fanout, rather than hard-coding every named example.

## 5. Calibration correction

The previous D5F route over-relied on synthetic/isolated fixtures and one-person human labels.

Those paths are no longer active.

- The pathological 7-player fixture is historical diagnostic evidence only.
- The later seven “clean” 7–9 scenarios deliberately excluded Drunk and froze registration-dependent context; do not add diagnostics or labels to them.
- Their test-only implementation should be deleted if canonical generator/legality tests already protect the durable contracts.
- Old D5 correction/design documents have been removed from active docs; Git history preserves traceability.
- Existing human-label manifests are historical/compatibility artifacts, not current policy truth.

## 6. D5F-B4 — expert-observed policy calibration

Evidence hierarchy:

~~~text
GOLD
    verified experienced/trusted Storyteller
    reconstructable real game
    explicit rationale preferred

SILVER
    high-fidelity structured real-game record
    Storyteller expertise not independently verified

QUALITATIVE
    tutorials / postmortems / repeated experienced-community guidance

DIAGNOSTIC_ONLY
    synthetic / extreme / counterfactual fixtures
~~~

Current external catalog now contains SILVER / QUALITATIVE seed evidence plus five tracked expert-source cases. **Two primary-verified GOLD decision slices now exist from two independent Storytellers**: A Stud In Scarlet under `st-ben-burns`, and Evin's 2019 first full playthrough under `st-evin`. Evin's postgame primary image verifies Demon bluffs Recluse / Slayer / Soldier, and the postgame review gives explicit Red-Herring rationale: Doug/Chef was selected because Chef information was especially damaging to Evil and Red-Herring contamination could undermine its credibility. The other two executable Ben reconstructions still require primary-video verification. `A Fond Farewell` has primary-verified material Night-1 state and explicit choice-specific rationale, but its executable legal-counterfactual gate is blocked because the live table contains five Travellers and the current production rules/domain model has no Traveller surface.

Primary-source extraction on 2026-09-22 also completed `A Stud In Scarlet` verification from its primary video. The canonical provenance record is [`SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md). The video directly verifies the player-visible outputs; hidden Recluse registration acts remain production-derived legal witnesses rather than primary-observed declarations.

Primary-source extraction on 2026-09-22 completed the material `A Fond Farewell` Night-1 reconstruction from the official video, including full radial seating/roles, Traveller alignments, Drunk shown Chef, Demon bluffs, Red Herring, Poisoner target, Washerwoman clue, Fortune Teller targets, and two explicit Ben rationales. The canonical record is [`SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md). This is primary verification, not GOLD admission: Traveller-aware production legality is still missing. The official 2019 Evin video has now been directly inspected. The primary reconstruction recovers the complete eight-seat role map plus the material Night-1 WW/Chef/RH/FT slice and is executable through production legality owners. Postgame primary imagery also verifies Demon bluffs Recluse / Slayer / Soldier, and Evin explicitly explains the Red-Herring choice around Chef truth danger / credibility disruption. See [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md) and [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md).

Interpretation rules:

- chosen A does not imply every unchosen B/C/D was bad;
- explicit rationale/rejection is stronger than silent choice;
- repeated comparable choices and cross-source consistency strengthen evidence;
- final game winner is not a Storyteller-quality label;
- human review is secondary: reconstruction checks, conflict adjudication, and BEGINNER-specific adaptation.

## 7. Immediate execution order

### Evidence track — PARALLEL / EXTERNAL

Current evidence work has reached a stable checkpoint:

- two independent primary-verified GOLD anchors exist: Ben / A Stud and Evin 2019;
- executable expert and SILVER replay harnesses are green;
- Red Herring contextual utility is the strongest cross-expert foundation;
- truth danger / credibility disruption has GOLD + SILVER explicit rationale;
- targeted evidence gaps are explicitly defined;
- broad source collection is no longer the default.

Targeted evidence acquisition continues through ClocktowerEvidenceLab according to:

`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`

This track does **not** block SDE-3A/B/C.

### SDE-3A — engine / feature / policy contract — CURRENT

Authority:

`SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`

Start with a live fanout/ownership audit of the current StorytellerDecisionEngine and shadow wiring.

The next implementation slice must:

1. map current decision/candidate/context/result ownership;
2. map reusable legality, proposition, topology and bundle surfaces;
3. define minimal generic typed contracts for candidate, feature projection and policy evaluation;
4. keep legality separate from policy;
5. keep features interpretable and independent rather than forcing an opaque scalar;
6. prepare versioned DecisionTrace / replay without automatic production cutover.

### SDE-3B — BEGINNER_CONSERVATIVE_V1 — NEXT

Implement a conservative, explainable first policy using:

- hard legality/lifecycle boundaries;
- generic catastrophic / near-catastrophic rejection;
- healthy-information preservation;
- contextual Red-Herring utility;
- coherent impaired narrative;
- role-function exposure as a soft contextual cost;
- bluff usability / route diversity;
- seeded randomness among effectively equivalent healthy survivors.

Do not invent unsupported numeric weights.

### SDE-3C — shadow / DecisionTrace / replay — NEXT

Before cutover, capture:

- policy version;
- candidate set;
- typed features;
- rejection/survival reasons;
- recommendation;
- actual committed choice;
- optional human override/reason.

Historical replay must support comparing multiple policy versions against the same committed game history.

### SDE-3D / 3E — remain blocked

Final calibrated policy freeze and automatic production cutover still require stronger external evidence.

Current unresolved calibration gaps:

- healthy-information floor / middle-band thresholds;
- role-function exposure severity;
- independent-expert impaired-information believability;
- Demon-bluff triplet preference ordering;
- quantitative multi-axis tradeoff.

No automatic online learning is authorized. Policy improvement remains offline, versioned and evidence-reviewed.

## 8. D5F-C remains blocked

Do not derive/freeze gates from old human labels, inspect sealed holdout, tune on holdout, cut production policy, begin SDE-3, or merge PR #150.

D5F-C may begin only after expert-observed evidence covers the material first-night policy variables and legal-counterfactual comparisons support repeated interpretable constraints.

## 9. Compatibility code intentionally retained

Do not remove merely as part of D5F-B4 evidence work:

- impaired-information approximate 90/10 false-family bridge;
- legacy `MalfunctionPolicy`;
- legacy registration/scoring paths still used by production.

Their retirement belongs to later production cutover/cleanup.

## 10. Testing cadence

Follow root `AGENTS.md` and [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md).

Documentation-only cleanup needs exact diff/reference audit, not manufactured runtime tests. Deleting obsolete test-only calibration artifacts requires proving no unique durable contract is lost and then running the smallest affected test tier.
