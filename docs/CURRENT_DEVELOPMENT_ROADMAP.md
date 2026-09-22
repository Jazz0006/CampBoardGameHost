# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-22 Australia/Sydney  
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
SDE-2D5 calibration / policy evidence                 CURRENT / PR #150 draft
    D5A–D5E                                            COMPLETE
    D5F infrastructure                                COMPLETE
    D5F-B3 correction                                 HISTORICAL CHECKPOINT
    D5F-B4 expert-observed policy calibration         CURRENT
        B4A clean-corpus retirement                    COMPLETE
        B4B GOLD source discovery                     IN PROGRESS
        B4C expert Night-1 reconstruction              IN PROGRESS / 3 executable + A Fond primary-verified
        B4D legal counterfactual recovery              IN PROGRESS / 3 executable Ben cases
        B4E observed-vs-alternative analysis           IN PROGRESS / 3 reports artifacted
D5F-C gate/band derivation                            BLOCKED
sealed holdout                                        CLOSED
SDE-3                                                 BLOCKED
~~~

## 2. Current branch / PR

Branch: `sde-2d5-calibration-policy-evidence`

PR: **#150 — SDE-2D5: calibrate strategic policy evidence**

PR #150 remains **draft**. Do not merge unless the user explicitly says **“授权合并”**.

Always query live refs before executable edits.

## 3. Current authorities

- First-night policy: [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md)
- External evidence seed: [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv)
- Long-term pre-SDE-3 route: [`SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`](SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md)
- Global SDE architecture: [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- Traveller boundary audit for primary-verified A Fond: [`SDE_2D5F_TRAVELLER_MODEL_BOUNDARY_AUDIT_2026-09-22.md`](SDE_2D5F_TRAVELLER_MODEL_BOUNDARY_AUDIT_2026-09-22.md)
- Evin source audit: [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md)
- Evin primary reconstruction: [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md)

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

Current external catalog now contains SILVER / QUALITATIVE seed evidence plus five tracked expert-source cases. **Two primary-verified GOLD decision slices now exist from two independent Storytellers**: A Stud In Scarlet under `st-ben-burns`, and Evin's 2019 first full playthrough under `st-evin`. Evin's Demon bluff triplet remains unknown/not shown, but production semantics prove it is non-material to the admitted WW/Chef/RH/fixed-target-FT slice, so it is not inferred and does not block that slice. The other two executable Ben reconstructions still require primary-video verification. `A Fond Farewell` has primary-verified material Night-1 state and explicit choice-specific rationale, but its executable legal-counterfactual gate is blocked because the live table contains five Travellers and the current production rules/domain model has no Traveller surface.

Primary-source extraction on 2026-09-22 also completed `A Stud In Scarlet` verification from its primary video. The canonical provenance record is [`SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_A_STUD_IN_SCARLET_PRIMARY_RECONSTRUCTION_2026-09-22.md). The video directly verifies the player-visible outputs; hidden Recluse registration acts remain production-derived legal witnesses rather than primary-observed declarations.

Primary-source extraction on 2026-09-22 completed the material `A Fond Farewell` Night-1 reconstruction from the official video, including full radial seating/roles, Traveller alignments, Drunk shown Chef, Demon bluffs, Red Herring, Poisoner target, Washerwoman clue, Fortune Teller targets, and two explicit Ben rationales. The canonical record is [`SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md). This is primary verification, not GOLD admission: Traveller-aware production legality is still missing. The official 2019 Evin video has now been directly inspected. The primary reconstruction recovers the complete eight-seat role map plus the material Night-1 WW/Chef/RH/FT slice and is executable through production legality owners. Demon bluffs were not shown and remain explicitly unknown; a comment-level Recluse mention is not promoted to fact. See [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_SOURCE_AUDIT_2026-09-22.md) and [`SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`](SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md).

Interpretation rules:

- chosen A does not imply every unchosen B/C/D was bad;
- explicit rationale/rejection is stronger than silent choice;
- repeated comparable choices and cross-source consistency strengthen evidence;
- final game winner is not a Storyteller-quality label;
- human review is secondary: reconstruction checks, conflict adjudication, and BEGINNER-specific adaptation.

## 7. Immediate execution order

### D5F-B4A — retire obsolete clean calibration artifacts — COMPLETE

Deleted:

- `Sde2D5FRepresentativeHealthyInformationCorpus.kt`;
- `Sde2D5FRepresentativeHealthyInformationCorpusTest.kt`.

No unique durable coverage was found. Their pair/numeric/registration/deal contracts are already covered by owning typed tests. The report was emitted only by the deleted test; no independent task/resource wiring existed.

Canonical production legality/topology infrastructure remains intact.

### D5F-B4B — discover and verify GOLD sources — IN PROGRESS

The source catalog now tracks two admitted GOLD decision slices plus three remaining expert-source candidates:

- Ben Burns — `A Stud In Scarlet`;
- Ben Burns — `Human Remains Of The Day`;
- Ben Burns — `Live and Imp-Person`;
- Ben Burns — official `Trouble Brewing - A Fond Farewell`;
- Evin — early TPI Trouble Brewing playthrough.

Ben's experienced/trusted status is independently supported by TPI. `A Stud In Scarlet` is primary-verified and admitted GOLD; `Human Remains Of The Day` and `Live and Imp-Person` still require primary-video verification. `A Fond Farewell` has primary-verified material state and explicit rationale, but executable reconstruction is blocked by missing production Traveller semantics. Evin is now also primary-verified and admitted GOLD for the executable material decision slice, giving B4 its first cross-Storyteller GOLD evidence.

### D5F-B4C — reconstruct expert Night 1 decisions — IN PROGRESS

Executable canonical reconstructions now exist for `A Stud In Scarlet`, `Human Remains Of The Day`, `Live and Imp-Person`, and the Evin 2019 first playthrough. All four route through the dedicated `:app:sde2D5FExpertObservedCalibration` T3 workload. `A Stud In Scarlet` and Evin are `PRIMARY_VERIFIED` GOLD decision slices from independent Storytellers; the other two remain `PRIMARY_VERIFICATION_PENDING`.

`A Fond Farewell` is the complementary fourth Ben case: the primary material state is now verified, but it is intentionally **not** forced into the executable harness because five Travellers affect seating, adjacency, public alignment, and Chef semantics. The Traveller boundary audit confirms this is a production capability gap, not an evidence-fixture gap: `GameState` has only the four base character types, the TB script/catalog has no Traveller roles, and topology/setup-profile code treats every formal seat as a base setup seat. **Traveller implementation is deferred from B4**; do not compress the 20-seat table or add fixture-local Traveller arithmetic.

Latest accepted B4 executable checkpoint: branch HEAD `26b9afe72e72705f53dc225a79fd5be6e3b6195c` completed dedicated **SDE-2D5F expert-observed calibration run #31** successfully. The shared evidence layer now covers reliable/impaired numeric decisions and complete pair-domain descriptive prevalence without introducing scores, rankings, BAD labels, or evidence-tier promotion. PR #150 remains draft.

The independent Evin primary extraction is complete. Continue primary-video verification only for the two remaining secondary-reconstructed Ben cases when more same-Storyteller depth is useful; the immediate B4 value now comes from comparing the two independent GOLD slices and deciding whether evidence is sufficient to advance beyond source collection. Do not infer its game bag from generic recommended 8-player setup guidance or from a different early TPI recording. `A Fond Farewell` no longer needs additional material Night-1 extraction unless a source contradiction appears. Traveller support is now explicitly deferred from the current B4 slice; if later adopted as a product goal, it must begin as a separate canonical-domain capability with full fan-out audit rather than as evidence-harness code.

The reconstruction remains an evidence projection onto existing canonical state/legality owners. Do not hand-author a second legality model.

Real-case consequence evaluation is topology-first. A 9-player exhaustive exact possible-world probe remained multi-minute even after reducing it to one sampled stage, while the dedicated topology-first workflow covering A Stud, Live and Human Remains completes in about 3.5 minutes end-to-end. Broad topology/exhaustive correctness remains owned by the bounded D4 differential tests; the evidence bridge keeps exact evaluation only as an optional deep-audit hook.

### D5F-B4D — recover legal counterfactuals — IN PROGRESS

Use existing production legality owners to reconstruct the exact alternatives available at each decision point. Hold only the committed prefix and already-made player-controlled choices fixed; do not leak later expert choices backward into the counterfactual domain.

Current executable evidence:
- `A Stud In Scarlet`: Chef, Drunk-shown-Empath, and fixed-target Fortune Teller legal alternatives recovered;
- `Live and Imp-Person`: full Librarian pair domain, Chef values, and fixed-target Fortune Teller alternatives recovered;
- `Human Remains Of The Day`: complete 273-candidate poisoned-Washerwoman pair domain plus the forced fixed-target Fortune Teller result recovered;
- `A Fond Farewell`: primary state recovered, but legal-counterfactual execution is blocked until Traveller character/alignment/seating semantics have a production owner;
- registration witnesses remain interaction-scoped evidence metadata rather than player-visible facts.

### D5F-B4E — compare observed choices to alternatives — IN PROGRESS

Run existing whole-bundle/topology diagnostics over observed expert choices and their legal counterfactuals. Extract repeated interpretable preferences, not a black-box score.

The current reports are deliberately allowed to be non-confirmatory on the strategic quotient: A Stud stays 56 -> 56 across modeled alternatives; Live stays 252 -> 252 while still demonstrating interaction-scoped Recluse registration; Human Remains has 273 legal poisoned-Washerwoman candidates but only one 42 -> 42 strategic-after signature. Treat this as evidence that registration choice, impaired narrative, role-function exposure, confirmation structure and truth danger can matter even when the quotient is neutral; do not invent a scalar difference merely to rank the expert choice.

The shared descriptive feature report now covers A Stud's Drunk-shown-Empath through the same numeric evidence contract as Chef. Its current reconstructed candidates are `0=FALSE` (observed), `1=TRUE`, `2=FALSE`; all are mechanically credible under impairment and none needs special registration. Keep semantic truth, registration witness, and strategic topology as separate dimensions.

Pair-domain prevalence is now also explicit. Human Remains' 273 poisoned-Washerwoman candidates contain 249 false candidates, 42 Demon-bluff-role candidates, 143 candidates touching an actual Evil seat, 78 touching the Demon, 78 touching the Minion, and 13 whose complete pair is actually Evil. The observed false Empath clue to the actual Minion+Demon has a descriptive signature shared by only 2 candidates. This is descriptive structure, not evidence that rare combinations are preferred.

The current external catalog recurrence signal is strongest for narrative anchoring (8 tagged records), truth danger (6), and temporal consistency / impairment detectability (4 each). `A Fond Farewell` also adds the first explicit `PLAYER_CHOICE_LIKELIHOOD` and `BELIEVABLE_COUNTERWORLD` tags plus a third Red-Herring-trajectory record. Treat these as evidence-collection priorities only. The two Ben rationales are choice-specific evidence, but they still share `st-ben-burns` and do not establish cross-expert generality.

### D5F-B4F — generalize with SILVER evidence

Use ClockTracker/high-fidelity logs after GOLD patterns exist.

### D5F-B4G — bounded human adjudication

Use human review only for ambiguous/conflicting evidence and BEGINNER-product adaptation.

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
