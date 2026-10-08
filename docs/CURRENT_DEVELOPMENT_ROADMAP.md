# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-10-08 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**  
> Historical checkpoint detail belongs in completion/audit documents under `docs/archive/` or the linked slice audits, not in this live roadmap.

## 1. Program status

~~~text
D6 decomposition / ownership cleanup                  COMPLETE
EPI-MQ capability boundary                            COMPLETE
SDE-0 / SDE-1                                        COMPLETE
SDE-2D1 / 2D2 / 2D3                                  COMPLETE
SDE-2D4 5–15 correctness/performance                  COMPLETE
SDE-2D5 calibration / policy evidence                 MERGED CHECKPOINT / EVIDENCE CONTINUES
SDE-3A engine / feature / policy contract             COMPLETE
SDE-3B BEGINNER_CONSERVATIVE_V1                       COMPLETE / IMMUTABLE V1
SDE-3C DecisionTrace / shadow replay                  COMPLETE
CR-A / CR-B / CR-C                                    COMPLETE
C4 / SDE-3D2                                          COMPLETE
IF-D durable App replay capture/rebuild               COMPLETE
RH-E runtime persistence/timing hardening             COMPLETE
DLB-0 typed intermediate setup                         COMPLETE / ACCEPTED
DLB-1 visible-roster deal cutover                      COMPLETE / ACCEPTED
DLB-2 legal Drunk candidate + hypothetical projector   COMPLETE / ACCEPTED
DLB-3A shadow envelope + DecisionTrace/replay            COMPLETE / ACCEPTED
DLB-3B setup-level consequence feature bridge              COMPLETE / ACCEPTED
DLB-4 canonical Drunk commit before reveal                 COMPLETE / ACCEPTED
DLB-4A Experienced assisted Drunk selection                COMPLETE / ACCEPTED
DLB-5.1 / 5.2 / 5.3 / 5.4 / 5.5 staged dependency work    COMPLETE
DLB-5 overall                                               COMPLETE / ACCEPTED
DLB-5H1 first-night evil-information presentation extraction COMPLETE / ACCEPTED
DLB Drunk-assignment initial production cutover audit        COMPLETE / HISTORICAL NOT-PASSED VERDICT
TBGS-0 canonical TB snapshot contract                       COMPLETE / ACCEPTED
TBGS-1 Drunk snapshot vertical slice / EvidenceLab interop   COMPLETE / ACCEPTED
Post-TBGS-1 cutover recheck                                 COMPLETE / HISTORICAL NOT-PASSED VERDICT
C3-Q04 production-policy re-entry audit                     COMPLETE / EVIDENCE GATE PASSED
DRUNK_ASSIGNMENT_Q04_V1 implementation / replay              COMPLETE / ACCEPTED
Q04 V1 production cutover                                   COMPLETE / ACCEPTED
Beginner automatic Drunk authority                          COMPLETE / ACCEPTED — Q04 V1
DLB-6 old-contract retirement                               COMPLETE / ACCEPTED
DLB-7 final DLB acceptance                                  COMPLETE / ACCEPTED
DLB campaign                                                COMPLETE / ACCEPTED
TBGS-2 runtime recommendation projection migration           PAUSED AFTER 2E — 2A/2B/2C/2D/2E COMPLETE / ACCEPTED; NO 2F MIGRATION SELECTED
Bounded App/Host decomposition                         RES-5 A1/A2/H0/H1 + post-RES5 H-R/H-M/A-C/A-S/H-P/H-I COMPLETE / ACCEPTED — PR #244/#245/#246; A3 OPTIONAL
C5 evidence-backed policy evolution                   C5-A/C5-B/C5-C/C5-D/C5-E COMPLETE / ACCEPTED
HOST-ML0 ML readiness / ModelLab boundary              COMPLETE / ACCEPTED
HOST-ML1 typed neutral decision export                   COMPLETE / ACCEPTED — separate from C5-E
SDE-3E automatic production cutover                   HISTORICAL LIBRARIAN POLICY ACCEPTED / AUTO-AUTHORITY REVOKED BY GSP-1
INV1-A functioning Investigator cutover                      HISTORICAL PR #222 / AUTO-AUTHORITY REVOKED BY GSP-1
RSR-0 RecommendationStyle / player-level audit              COMPLETE / ACCEPTED
RSR-1A Storyteller-mode/style ownership decoupling           COMPLETE / ACCEPTED — PR #217
LRE-0 whole legacy heuristic recommendation-engine audit      COMPLETE / RETIREMENT AUTHORIZED
LRE-1 manual fallback / recommendation-authority cutoff        COMPLETE / ACCEPTED — executable 045b3a68; CI #3752 / R2 #3448 GREEN
LRE-P family-by-family special-policy loop                    SUPERSEDED / DO NOT CONTINUE
GSP-0 General Storyteller Policy route reset                  COMPLETE / DOCS-ONLY
GSP-1 special-policy automatic-authority revocation           COMPLETE / ACCEPTED — PR #225; merge 2a4884c; CI #3764 / R2 #3456 GREEN
GSP-2 provider + decision-specific global-context contract    FOUNDATIONS ACCEPTED — 2A/2B1/2B2 REUSED; OLD 2B3/2C/2D SUPERSEDED BY POST-RES RE-ENTRY
RES-0 engine/recommendation separation + purge audit          COMPLETE / ACCEPTED — PR #233
RES-1 engine-only pending-decision/legal-domain boundary      COMPLETE / ACCEPTED — PR #234/#235
RES-2 neutral provider-contract extraction                    COMPLETE / ACCEPTED — PR #236/#237; CI #3800 / R2 #3482 GREEN
RES-3 legacy heuristic/style/weighted physical purge          COMPLETE / ACCEPTED — checkpoint 5b4b2a94; CI #3815 / R2 #3495 GREEN
RES-4 named deterministic special-policy physical purge       COMPLETE / ACCEPTED — T4 00aefd1b; CI #3824 / R2 #3502 GREEN
RES-5 physical module/dependency boundary                     COMPLETE / ACCEPTED — T4 cdf5dba2; CI #3832 / R2 #3509 GREEN
GSP-R0 post-RES-5 re-entry audit                              COMPLETE / DOCS — current authority: GSP_POST_RES5_REENTRY_AUDIT_2026-10-08.md
GSP-R1A canonical capture/prefix coverage audit                CODE AUDIT COMPLETE — docs/GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md; no Android behavior change
GSP-R1B neutral typed immutable prefix materializer             COMPLETE / ACCEPTED — PR #250, CI #3849 / R2 #3518 GREEN (Android FAST + Clingo); GLOBAL_V1 live only
GSP-R1C1 Recovery parity / missing historical cutoff gate      COMPLETE / ACCEPTED — PR #252; CI #3853 / R2 #3520 GREEN (Android FAST + Clingo)
GSP-R1C2A frozen predecision + in-memory causal journal        COMPLETE / ACCEPTED — PR #256, CI #3864 / R2 #3527 GREEN; NOT durable/production-wired
GSP-R1C2B Mayor durable producer + Recovery causality          COMPLETE / ACCEPTED — PR #258; CI #3874 T4 / R2 #3535 GREEN; Mayor production vertical only
GSP-R1C2C remaining producers + registration ruling history    IN PROGRESS — C2C-0 #260 / 1A #263 / 1B #265 ACCEPTED; C2C-1C1 Slayer #267, C2C-1C2 Virgin #269 T4 ACCEPTED; C2C-1C3A Klutz verified-death alignment #271 T4 ACCEPTED; C2C-1C3B complete death-trigger provenance NEXT; R1C IN PROGRESS
~~~

### GSP-R1A capture gate — 2026-10-08

The code-level R1A inventory confirms canonical Global actions and some typed player observations, but incomplete registration/private/public semantic producer coverage and absent production/Recovery decision archive capture. Revision-filtered effective decisions **cannot** be interleaved with action/observation global sequence; correction events lack causal timestamps. R1B must project a neutral read-only prefix with per-dimension coverage/unknown and a captured exclusive cutoff, without pretending that missing observations, public claims or historical decision cutoffs are complete. See [R1A audit](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md). R2/R3/R4/GSP-3A order remains unchanged; no production LLM API before the benchmark.

### GSP-R1B accepted — 2026-10-08

[Accepted R1B executable closure](GSP_R1B_NEUTRAL_LIVE_HISTORY_PREFIX_ACCEPTANCE_2026-10-08.md): PR #250 squash `8e788db2f6ad0be1f3c8b1c844306d6210e66cd7`, CI #3849 / R2 #3518 GREEN, Android **FAST** and Real Clingo passed; full Android/T4 was not run. The Host's neutral provider context can carry an immutable, globally ordered current-game Action/Observation prefix bounded by the captured exclusive cursor and session revisions. Mechanical/private/public producer coverage remains PARTIAL; registration and prior-decision causal capture UNKNOWN. LegacyLocal chronology is explicitly unavailable, and no historical replay or production LLM provider was introduced. **GSP-R1C is next**, with real Recovery/frozen-prefix equality and missing-coverage/ambiguity gates. The result-first registration ambiguity authority remains a separate producer/UI follow-up.

### GSP-R1C2C-0 producer coverage + generic manual registration selection accepted — 2026-10-08

[Accepted C2C-0 scope and exact CI/R2](GSP_R1C2C0_GENERIC_REGISTRATION_CHOICE_ACCEPTANCE_2026-10-08.md): PR #260 squash `d75d00031bd83ecf340230041443e7612f2e080b`, CI #3880 Android tests GREEN / R2 #3539 GREEN (no T4). Live [producer coverage audit](GSP_R1C2C_PRODUCER_COVERAGE_AUDIT_2026-10-08.md) catalogs decision producers and separates Storyteller rulings from player actions, typed results, and localized events. The shared UI interaction now preserves explicit-only Spy/Recluse manual choice, including explicit false versus untouched, unspecified versus specified role, and multi-witness Number/YesNo/RoleReveal compatibility. **No typed ruling producer, Recovery recording, or complete decision family capture was added.** Next C2C-1 implements Host-confirmed typed interaction-local ruling + causal corrections + strict Recovery; other result and setup producers follow. **R1C2C and R1C remain IN PROGRESS.**

### GSP-R1C2C-1A typed registration-resolution / causal Recovery foundation accepted — 2026-10-08

[Accepted C2C-1A exact scope and CI/R2](GSP_R1C2C1A_TYPED_REGISTRATION_RECOVERY_ACCEPTANCE_2026-10-08.md): PR #263, squash `9d7b844d37c8ebad1448836ad3946b26af006d5c`; CI #3893 Android FAST GREEN / R2 #3549 GREEN, **no full Android T4**. Typed confirmation requires an actual global observation, role/subject legality and current frozen time; explicit special registrations are only admitted through a verified journal commit, survive strict Recovery, and can be corrected with historical as-of equality. **No UI confirmation wiring yet:** no live Host Spy/Recluse ruling is captured by this slice; historical unknown and multi-witness uncertainty remain protected. C2C-1B must wire real UI/Host confirmation with final shown-result witness compatibility and re-confirmation/Recovery tests. **R1C2C / R1C remain IN PROGRESS.**

### GSP-R1C2C-1B — actual Host private-night registration/Recovery accepted — 2026-10-08

[Exact C2C-1B acceptance](GSP_R1C2C1B_LIVE_REGISTRATION_ACCEPTANCE_2026-10-08.md) and [production coverage contract](GSP_R1C2C1B_LIVE_REGISTRATION_PRODUCER_SCOPE_2026-10-08.md): PR #265 squash `81ee09066a9f4648cc42f8ff1abe3a0d157f5ae7`, exact-head CI **#3914 T4 GREEN** (full Android JVM + debug APK, ASP, Real Clingo) and R2 **#3568 GREEN**. Confirmed numeric/boolean/role information with verified legal registration witnesses now has a real Host preflight, typed global publication, explicit/known-unresolved causal ruling, correction and Recovery path. A no-longer-present UI toggle cannot erase previously explicit history. Not all private result families have sufficient verified candidate coverage; no Day/Virgin/Klutz/Slayer mechanical registration producer exists yet. **C2C-1B private-night vertical COMPLETE; C2C-1 and overall R1C2C/R1C remain IN PROGRESS. Next C2C-1C audits and implements daytime/non-private registration events without inventing private observations.** No provider/LLM authority or GSP-R2 advancement.

### GSP-R1C2C-1C1 accepted public Slayer registration producer — 2026-10-08

[Exact accepted C2C-1C1](GSP_R1C2C1C1_SLAYER_PUBLIC_REGISTRATION_ACCEPTANCE_2026-10-08.md) and [Day registration inventory](GSP_R1C2C1C_DAY_REGISTRATION_AUDIT_2026-10-08.md): PR #267 squash `57cfa93c6638c8c4f9e6e4b6feb1af5ddda65ed0`; exact PR head `b43dc81d04e3d1e05b18ad84cc0e27967ce39c87`; **CI #3919 T4 GREEN** (Android FULL / debug APK / ASP / Real Clingo), **R2 #3571 GREEN**. Actual functioning Slayer's explicit Recluse-as-Demon selection now captures a frozen **day ability event** before the irreversible shot/Death; typed RegistrationFact, strict canonical validation and causal Recovery, with **no invented private observation**. Virgin/Spy Townsfolk-type and Klutz/Spy Good-alignment remain independent unimplemented day producers; no default role should be inferred. **C2C-1C1 COMPLETE; C2C-1C2 Virgin NEXT; C2C-1, R1C2C and R1C remain IN PROGRESS.**

### GSP-R1C2C-1C2 accepted Virgin first-nomination Townsfolk-type ruling — 2026-10-08

[Exact C2C-1C2 acceptance](GSP_R1C2C1C2_VIRGIN_TOWNSFOLK_TYPE_ACCEPTANCE_2026-10-08.md) and [scope contract](GSP_R1C2C1C2_VIRGIN_TOWNSFOLK_TYPE_SCOPE_2026-10-08.md): PR #269, squash `3e702dcd8ac16a9c693feab8ee88ee9c15c6b7dc`, exact head `8cdcabfa54cec9c1d6dbfae715eb56cadb8e13b5`; **CI #3925 T4 GREEN**, **R2 #3575 GREEN**. The real Virgin first-nomination confirmation now captures **explicit** Spy-as-Townsfolk `CHARACTER_TYPE` (no invented named role) or explicitly actual Spy, before execution/death/phase effects; untouched remains not recorded. UI default Washerwoman and named-role picker are removed for Virgin, and strict Recovery rejects changed type/role/witness. No fake private observation. **C2C-1C2 COMPLETE, 1C3 Klutz NEXT; C2C-1C/R1C2C/R1C still IN PROGRESS.**

### GSP-R1C2C-1C3A — Klutz/Spy Good alignment with proven death chronology accepted — 2026-10-08

[Exact accepted C2C-1C3A](GSP_R1C2C1C3A_KLUTZ_ALIGNMENT_ACCEPTANCE_2026-10-08.md) and [bounded proof contract](GSP_R1C2C1C3_KLUTZ_ALIGNMENT_DEATH_PROVENANCE_SCOPE_2026-10-08.md): PR #271, squash `e5653499200898cc0c74839d855370d10dba055c`, exact head `81775a83d8b40a5b38d24d55c143c2f4ca1b50db`, **CI #3932 T4 GREEN** (Android FULL/debug APK, ASP, Real Clingo), **R2 #3580 GREEN**. The real Klutz choice now separates player-selected target from the Storyteller's explicit Spy-as-GOOD **alignment-only** ruling, preserving untouched/explicit-actual and not forcing Washerwoman. When canonical death/execution and prior poison-target chronology positively establish a non-poisoned Klutz at death, the Host commits a distinct typed day causal ruling before win/loss or phase mutation; strict Recovery rechecks original death/poison IDs and frozen prefix. Without proven death-time state the game remains playable but typed registration abstains. **1C3A complete; 1C3B next** must add authoritative death-trigger ability provenance to cover games without known poison-action history and audit remaining day/ruling coverage. **C2C-1C, C2C-1, R1C2C/R1C remain IN PROGRESS.**

### GSP-R1C2B accepted Mayor vertical — 2026-10-08

[Accepted real production/Recovery causal replay](GSP_R1C2B_MAYOR_DURABLE_CAUSAL_RECOVERY_ACCEPTANCE_2026-10-08.md): PR #258 squash `7d6255922e8db33b7ce297958d6ee467c339a9f4`, CI #3874 **T4 GREEN** (Android FULL + debug APK, ASP, Real Clingo), R2 #3535 GREEN. Trouble Brewing Mayor target confirmation now captures the exact predecision GLOBAL_V1 prefix, persists the typed selected outcome, records later re-confirmation as a causal correction, and survives real Recovery JSON/strict decode/planner with as-of equality. Missing legacy sidecar remains explicitly unrecorded, not complete empty history. **Only Mayor producer is live.** Drunk, pair/numeric/FT/role and other discretionary decisions plus typed explicit Spy/Recluse registration capture are still incomplete; next **GSP-R1C2C** covers systematic remaining producer adoption. No LLM API or heuristic authority.

### GSP-R1C2A accepted — 2026-10-08

[Accepted frozen decision capture / causal journal contract](GSP_R1C2A_FROZEN_CAUSAL_DECISION_PREFIX_ACCEPTANCE_2026-10-08.md): PR #256, merge `38503670d93d925603e12cfc6388990550421c44`, CI #3864 and R2 #3527 GREEN (Android FAST; no T4). Exact GLOBAL_V1 predecision game/revision/snapshot/observation cutoff is frozen; an independent append-only **in-memory** decision/commit/correction chronology can replay as-of earlier captures without later correction leakage or inventing registration facts. **Not yet durable or production-wired**: no Host producer integrated with the neutral provider request, no Recovery codec or complete decision data coverage. Proceed to **GSP-R1C2B** real producer + typed durable Recovery/admission, then full R1C acceptance; no LLM API or heuristic authority.

### GSP-R1C1 acceptance — 2026-10-08

[Accepted R1C1 code/recovery parity report](GSP_R1C1_RECOVERY_PREFIX_PARITY_AND_HISTORICAL_CUTOFF_ACCEPTANCE_2026-10-08.md): PR #252, squash `fae562f0e52d2d8650b28b6b11964e64a41a3766`, CI #3853/R2 #3520 GREEN. Real Recovery encode/decodeStrict/restore planning plus `ClocktowerGameSession.restoreProduction` now has a passing same-cutoff neutral Provider history-prefix equivalence fixture across FirstNight/Day/OtherNight. An unfrozen historical decision request explicitly returns `HISTORICAL_CUTOFF_UNAVAILABLE` rather than using current state/revisions. A Spy/Recluse-adjacent Empath=1 test verifies the recorded result does not become a fabricated registration fact. **Not complete:** durable at-decision frozen cutoffs, decision/correction as-of history, full typed registration UI/producer, complete private/public coverage and Android full T4. R1C overall remains **IN PROGRESS**; R1C2 is next.

### GSP-R1B / R1C — ambiguous registration semantics (2026-10-08)

[Generic registration UI fix accepted](GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md): a player-visible result may have multiple legal Spy/Recluse witnesses. PR #254, merge `b4d6fd0eb0f0b00025e5f82aa45fd7dbdd6bce7a`, CI #3860 / R2 #3525 GREEN, corrected the **shared result-first grouping**, Host registration side effects, explicit-manual contradiction guard and Chef/Empath contributor visuals. This is **not** an Empath/Chef policy or a durable typed registration fact producer. [Canonical ambiguity authority](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md) still requires interaction-local typed explicit rulings, ambiguity/unknown provenance, Recovery preservation and optional advanced UI in a bounded follow-up. R1C2 remains NEXT for decision/correction causal prefix; no early LLM authority.

## 2. Current repository boundary

Current canonical baseline: `main`.

PR #157 — `SDE correctness repair: close CR-A CR-B CR-C` — was explicitly authorized and squash-merged on 2026-09-27.

PR #157 merged executable integration baseline:

`02845a470761a988f0041d8c1027b0e2c58a7e05`

Current canonical baseline is the live `main`; query its actual HEAD before executable work instead of copying a branch SHA into this live document.

PR #165 — `Recovery: enforce current-only minimal persistence` — was explicitly authorized and squash-merged on 2026-09-28 at historical merge checkpoint `b297484cd055b6aa5cfaf1c9b1c4093832af77da`. Recovery R0–R6 are complete; R7 remains a separate ownership follow-up and is not part of the completed cleanup boundary.

The previous `5d2982a7...` baseline is historical. The executable SDE policy boundary remains unchanged by #165.

The merged scope contains the accepted continuation through CR-A/B/C, C4/SDE-3D2, IF-D, RH-E, and the audited workflow/document integration. The previous continuation branch `codex/sde-history-prefix-route-closure` is now historical rather than the active development baseline.

There is currently **no active SDE production implementation PR**. Any documentation-only post-merge closure branch/PR is bookkeeping, not a new product-development authority.

The 2026-09-27 live-main integration audit found no production/gameplay/SDE semantic conflict. The accepted resolution preserved the newer GitHub Connector-first workflow in root `AGENTS.md` and archived the superseded 2026-09-24 M8G5 workflow document under `docs/archive/workflows/`.

Repository cleanup before RH-E:

- validation-only/superseded PRs #148, #154, #156, #158, #160 and #161 are closed without merge;
- diagnostic PR #109 is closed as obsolete for the current short-horizon Recovery contract after a fresh current-format atomicity audit;
- temporary validation remote branches from the first cleanup wave are gone;
- remaining historical branch pruning is hygiene only and must preserve unique archive material.

Always query live Git/PR state before executable work. Do not rely on a hard-coded live branch HEAD in this document.

## 3. Accepted executable checkpoints

DLB-0 accepted executable checkpoint: `f81350ec1341cb83f2b4c2f31d80b9c61c7cec52`; CI #3506 and R2 #3251 GREEN. DLB-0 added only the typed intermediate setup contract and owning test.

DLB-1 accepted executable checkpoint: `d065e21bcf1780fe3375a7fd380815259cc59e5c`; CI #3515 and R2 #3259 GREEN. Trouble Brewing production now seats visible identities into the DLB intermediate contract before any canonical Drunk-seat decision; current App/history remain behind an explicitly temporary compatibility bridge until DLB-4.

DLB-2 accepted executable checkpoint: `73de263327d7530e2d6ac128753aca2fd8766766`; CI #3521 and R2 #3264 GREEN. Every dealt Townsfolk seat is now a rules-legal peer Drunk candidate, and each legal choice can be projected into an immutable hypothetical GameState without session mutation or compatibility-bridge authority.

DLB-3A accepted checkpoint: `0a59c29af047b91b5d10b62ce4019f60df632e83`; CI #3526 and R2 #3268 GREEN. The shadow surface preserves the complete legal domain, candidate-specific hypothetical/ecology evidence, DecisionTrace and replay, while frozen V1 correctly defers with no Drunk recommendation.

TBGS-0 accepted executable checkpoint: `f367c0d3ec23ebf452c924ff7c0921cd978a800f`; CI #3618 and R2 #3343 GREEN. `TroubleBrewingGameSnapshotV1` now provides the TB-only immutable read/interchange contract with explicit `KNOWN / UNCOMMITTED / UNKNOWN / NOT_APPLICABLE` semantics, stable external role IDs, precommit/committed/runtime projectors, deterministic V1 JSON, and the G10 precommit golden fixture. No recommendation policy, mutable owner, Host/UI migration, Recovery behavior, or non-TB generalization changed.

TBGS-1A Host vertical slice accepted executable checkpoint: `ae4dc2400325d233da033d3c86d2863bde1bd485`; CI #3621 and R2 #3345 GREEN. The rules-owned Drunk legal domain can now derive stable `(seat, shownRoleId)` candidate refs directly from `TroubleBrewingGameSnapshotV1`; a typed policy-neutral `DrunkAssignmentDecisionContext` carries snapshot, freshness revision, legal candidates, decision identity and selection seed; the existing shadow path consumes that context without changing candidate order, DecisionTrace/replay behavior, `BEGINNER_CONSERVATIVE_V1`, or `DRUNK_ASSIGNMENT_SHADOW_V1` deferral semantics. The G10 V1 JSON fixture is consumed without Host setup objects.

TBGS-1B EvidenceLab interoperability is now complete at observed EvidenceLab checkpoint `970e7e6430f7088ac004cd1e5696759da4d52003`. EvidenceLab materializes the same G10 pre-Drunk V1 semantics from one historical prefix without copying Host legality or policy. A fresh Host-side comparison confirmed the Host and EvidenceLab G10 fixture payloads are byte-for-byte identical (2115 bytes each). TBGS-1 overall is therefore COMPLETE / ACCEPTED. The post-TBGS-1 cutover recheck remains NOT PASSED solely because no C3 Stage-1 VERIFIED candidate-comparison/rejection evidence and therefore no production-capable versioned Drunk policy exist yet.

These are historical acceptance identities, not current branch heads:

| Checkpoint | Accepted evidence |
| --- | --- |
| CR-A/B/C | validation head `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0`; CI #3459 + R2 #3212 GREEN |
| C4 / SDE-3D2 | formal `21206a8ca95896ffad83eaba5695a3a571c6cd74`, tree `a4701e0f4873ddd629dfa11163a94d2aa0cb9b8b`; exact-tree CI #3473 + R2 #3226 GREEN |
| IF-D | formal `4d1b6d7f39529402eb9ec1e6032eb80ef9a14e86`, tree `181a1d80d449c2d443a678e10033acda13c777d1`; exact-tree CI #3476 + R2 #3229 GREEN |
| RH-E | formal `f51a295983e8e203119dd50693af343c1ec23906`, tree `2babb1fba00b46dfc676efb6f090386b7a73826f`; validation-only PR #162 head `bf66363385420f507f92a729b496ff002791dee5`; exact-tree CI #3478 + R2 #3231 GREEN |

Later documentation-only changes must not be re-labelled as validating a different executable tree.

Completion/cleanup detail:

- `docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`
- `docs/archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`
- `docs/SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md`
- `docs/SDE_C5_G10_PAIR_FUTURE_FLEXIBILITY_REENTRY_AUDIT_2026-10-03.md` — C5 re-entry / SDE-3D3 evidence and replay authority
- `docs/SDE_C5E_LIBRARIAN_PRODUCTION_CUTOVER_GATE_AUDIT_2026-10-03.md` — functioning-Librarian production cutover acceptance authority

## 4. Current priority — GSP-1 closed; GSP-2 planned but not started

EvidenceLab `main@78f672868ea6603317aeefa20ad91686c5886db9` supplies the first C5-qualified E3 predicate through the G10 `16:52` functioning-Librarian pair decision. `docs/SDE_C5_G10_PAIR_FUTURE_FLEXIBILITY_REENTRY_AUDIT_2026-10-03.md` is the current C5 authority: re-entry is PASS and C5-A pair future-flexibility projection is COMPLETE / ACCEPTED at exact T4 checkpoint `5b516e10e0f01848a1dad7a06dd214e73faa5a97`, CI #3673 / R2 #3385 GREEN. C5-B pair SDE shadow + replay bridge is COMPLETE / ACCEPTED. The executable full-domain checkpoint `067310a79b74ac4a7eb558b179676e56b3e3f65d` passed CI #3682 / R2 #3392, including the optimized G10 40-candidate replay inside Android `testFull`; final exact-head T4 checkpoint `fdb24f67c010759a123a6e648bef8d4015832cb1` passed CI #3683 / R2 #3393 GREEN across Android FULL/assemble, ASP and Real Clingo. C5-C `BEGINNER_CONSERVATIVE_V2` is COMPLETE / ACCEPTED. The implementation checkpoint `d4c071fac09f446fb7b7f02f99808db61275112c` passed CI #3689 / R2 #3398 GREEN, and final exact-head T4 checkpoint `001330f32e151b67ead1760a67702450faaab685` passed CI #3690 / R2 #3399 GREEN across Android FULL/assemble, ASP and Real Clingo. V2 remains shadow/replay-only. C5-D canonical same-history V1/V2 replay is COMPLETE / ACCEPTED at exact-head `5241518e002e65993eed594632604e13e2979bfd`, CI #3693 / R2 #3401 GREEN: the same G10 40-candidate decision replays deterministically through frozen V1 and V2, actual expert choice remains historical metadata, the V2 delta is confined to the admitted generic future-flexibility predicate, and both traces survive archive round-trip. C5-E functioning-Librarian automatic production cutover is COMPLETE / ACCEPTED at exact-head `6afc3b08416eaf6f3fb74a53bb48b8e144044341`, CI #3717 / R2 #3421 GREEN across Android FULL/assemble, ASP and Real Clingo. After parallel HOST-ML1 mainline convergence, the rebased integrated tree `fdf3a2be07ed0c70753ba724f15b5572e0dea584` also passed CI #3720 / R2 #3424 GREEN across Android FULL/assemble, ASP and Real Clingo. The production fast path selects the same G10 candidate as the exact C5-D V2 oracle, rebinds through the current canonical pair legal/manual domain, preserves Experienced/manual authority and publication semantics, and falls back to the pre-C5-E selector when scope/context/current-domain checks fail. HOST-ML1 typed neutral decision export is COMPLETE / ACCEPTED after PR #208 + #209; final executable checkpoint `b91fcd298621e9064f11c26c1a0398c5c14fc13c`, CI #3710 / R2 #3416 GREEN, merged by PR #209 squash `2d4681115b9c373bfbfa85a7b385aba4e59e1062`. It preserves complete candidate semantics for mature Drunk-assignment and pair-information surfaces and remains separate from C5-E. E4 remains unproven and is not required for the first qualitative, weight-free predicate.

EvidenceLab EL-ML0 is COMPLETE / ARCHITECTURE ACCEPTED at observed `main@a575ecc05ccb77cf4aaddcad7f772b0fe920d3d6`. Host HOST-ML0 is COMPLETE / ACCEPTED, and HOST-ML1 now implements the typed policy-neutral `RecommendationDecisionExportV1` boundary for Drunk assignment and first-night pair information, including pair candidate shown-role/seat/truth/registration semantics. The frozen long-horizon ownership remains: EvidenceLab owns source-backed evidence; Host owns canonical pre-decision state, rules-owned legal domains, typed contexts, deterministic features, replay and neutral export; a future ModelLab owns dataset recipes and model training. Machine-readable corpus materialization is a separate future follow-up, deferred until a concrete offline consumer fixes the interchange requirement; EL-ML1 is triggered only if source-backed machine-readable evidence seeds are required. Authorities: `docs/HOST_ML_READINESS_MODELLAB_BOUNDARY_AUDIT_2026-10-03.md` and `docs/HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md`. This route must not broaden C5 or delay TBGS-2.

TBGS-2D Demon succession and TBGS-2E Mayor redirect are COMPLETE / ACCEPTED. RSR-0/1A retired the old global GENTLE/BALANCED/AGGRESSIVE style ownership, and LRE-0 classified the hand-tuned heuristic recommender as retirement-only. LRE-1 then established the accepted fail-closed Manual boundary at executable `045b3a6884f765149d4f1802d5e369671d38d938`, CI #3752 / R2 #3448 GREEN. After that checkpoint, PR #222 merged a narrow INV1-A functioning-Investigator auto-selector under the then-current special-policy route. The 2026-10-05 product decision now supersedes that family-by-family route: individual expert cases/predicates are evidence and benchmark material, not a durable production policy architecture. GSP-1 is COMPLETE / ACCEPTED via PR #225, squash merge `2a4884c856423d0268b020bd774dcb95954954de`, CI #3764 / R2 #3456 GREEN. Q04 Drunk, functioning Librarian V2 and INV1-A Investigator no longer hold discretionary production automatic authority; their policy/selector/replay material is retained for benchmark/shadow/reference use, while complete Manual legal domains and rules-deterministic unique outcomes remain intact. Current recommendation authority is `docs/GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md`. Android remains offline-first; manual blind LLM benchmark work precedes any API integration.

The product route changed on 2026-09-29 after the Drunk late-binding and App/Host decomposition audits were reconciled.

DLB-0 is implemented and accepted. The current implementation authority remains:

`docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`

The new Trouble Brewing setup contract makes Drunk assignment a post-seat Storyteller/SDE decision rather than a template-owned pre-seat input. DLB-0 established the typed intermediate state; DLB-1 cut production preparation over to visible-roster seating; DLB-2 provides the rules-owned legal Drunk-seat domain and pure hypothetical GameState projection; DLB-3 carries that domain through SetupPrecommit / DecisionTrace / replay and the dedicated shadow policy-experiment lane while frozen V1 remains unchanged. DLB-4 canonical Drunk commit is COMPLETE / ACCEPTED at `5606371c68b97beb01418ede2bb2c19db7e69053` with CI #3580 / R2 #3313 GREEN; DLB-4A Experienced assisted Drunk selection is COMPLETE / ACCEPTED at `c85448831e73c82868e118c7f47a0ed889267c0c` with CI #3586 / R2 #3318 GREEN. DLB-5 is COMPLETE through 5.1 typed dependency planning, 5.2 Red Herring latest-safe commitment, 5.3 Demon-bluff presentation-barrier commitment, 5.4 Poisoner / first-night information convergence audit, and 5.5 obsolete setup auto-apply cleanup. The latest production executable checkpoint remains `bdc31a31bc4652be13e31d7d4c8c6bacd68b9dd5` with CI #3600 / R2 #3331 GREEN; 5.4 found no production gap and required documentation only. Final exact-head T4 acceptance ran on `3fd1714ce2e18b969e5039bcf6a58f8775745b9d` with CI #3603 / R2 #3334 GREEN, including Android `testFull + assembleDebug`, ASP contracts and Real Clingo cross-validation. PR #183 was squash-merged into `main` at `6293a3bb94778338db61f5a1708a1d283d6e8b6f`. That historical Beginner compatibility fallback was superseded by the accepted Q04 V1 production cutover and has now been retired by DLB-6.

The 2026-09-28 App/Host decomposition audit remains a guardrail rather than a prerequisite campaign, but the former H2 projection item has now been refined by the TB snapshot audit:

- A1 preferences storage and A2 archive storage remain independent maintenance slices;
- H1 is COMPLETE / ACCEPTED through DLB-5H1;
- former H2 is split into **TBGS-0/1 complete** and **TBGS-2 incremental runtime migration**; TBGS-2A through 2E are COMPLETE / ACCEPTED and TBGS-2 is paused after 2E. RSR-0/1A established player-level/style ownership boundaries, and LRE-1 removed unsupported legacy automatic authority. GSP now owns recommendation continuation: special-registration typed history and other structured facts are useful only as context/diagnostics for Manual, benchmark or a future general provider, not as triggers for another special-case policy;
- A3 presentation catalog has been re-audited after DLB completion and is now READY as an independent maintenance slice, but is not a TBGS-2 prerequisite and must not be bundled into TBGS-2A;
- R3 generic transaction extraction and generic setup-effect ownership remain NO-GO.

TBGS-0/1 is not a broad Host decomposition campaign. It establishes the read-only semantic boundary required before the first new production-capable Drunk recommendation request is introduced.

Targeted EvidenceLab C1C Drunk-assignment acquisition is complete at 3 / 3 replayable cases, and C1D downstream replay is accepted. EvidenceLab now serves two distinct lanes:

1. DLB Drunk-assignment evidence/trace replay and consequence-contract calibration for the new decision surface;
2. the existing C5/V2 E3/E4 policy gate.

Do not conflate those gates. The historical C5 blocker is now superseded by the 2026-10-03 G10 Librarian E3 PASS: C5 re-entry and SDE-3D3 implementation are authorized, while production cutover remains separately blocked until the new projector, pair shadow/replay, V2 replay acceptance and surface-scoped cutover gate pass.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb Drunk candidate ordering/rejection semantics. Any production Drunk-selection policy delta requires an explicitly versioned decision-surface contract.

The 2026-09-30 Drunk-assignment production cutover audit remains the historical NOT-PASSED verdict. On 2026-10-02, EvidenceLab Q04 became the first primary-audio VERIFIED C3 Stage-1 conditional preference at checkpoint `08d95a0c258f687187c0476a0f430fa5ff8229cb`. The bounded ordering-evidence gate passed, `DRUNK_ASSIGNMENT_Q04_V1` was implemented as the smallest evidence-authorized Q04-only Empath -> Monk override with explicit baseline fallback, and its dedicated replay was accepted at `5cf72a54a62c87763279c02014485a847724b72e` with CI #3631 / R2 #3351 GREEN. Beginner production wiring was then completed at `0ef3760ee233b0e20fa0dd272b12380abe8ee4a4`. Final exact-head T4 acceptance `9762d5a759bf0eaa81a1f6cb5af1aa28281d3ac2` passed CI #3633 and R2 #3353, including Android `testFull + assembleDebug`, ASP contracts and Real Clingo. The Q04 production cutover gate is therefore PASS and Beginner automatic Drunk authority is accepted. `DRUNK_ASSIGNMENT_SHADOW_V1` remains deferral-only and `BEGINNER_CONSERVATIVE_V1` remains immutable. DLB-6 then retired the old DealPlan/pre-seat selector/scorer, `CompatibilityImmediate`, prepared-setup compatibility-candidate ownership and current-model `selectedDrunkShownRole`; final executable checkpoint `52c2e73ca455a62c31065ce0e6fca4edda8713ec` passed CI #3639 / R2 #3358 GREEN. DLB-6 is COMPLETE / ACCEPTED. DLB-7 then closed the full route-wide behavior matrix with dataset-wide 5–15 preset coverage plus explicit finalized-Drunk Recovery identity roundtrip. Exact T4 checkpoint `b760117351e57cfe78cc0bc9483b42306757c22f` passed CI #3643 / R2 #3361 GREEN, including Android `testFull + assembleDebug`, ASP contracts and Real Clingo. DLB-7 and the overall DLB campaign are COMPLETE / ACCEPTED. The 2026-10-03 TBGS-2 focused audit selected first-night Washerwoman / Librarian / Investigator natural-pair precompute as TBGS-2A because its required facts already fit `TroubleBrewingGameSnapshotV1`; TBGS-2A is now COMPLETE / ACCEPTED at exact T4 head `9021ef26b65033b69911fa4a79124fdd2137284d` with CI #3647 / R2 #3364 GREEN, including Android full + assemble, ASP contracts and Real Clingo. TBGS-2B pair manual/publication is COMPLETE / ACCEPTED at exact T4 head `fa07e382fdad1b03aeadc27ce0d0d939f66f8b67` with CI #3654 / R2 #3370 GREEN, including Android full + assemble, ASP contracts and Real Clingo. TBGS-2C setup coordination is COMPLETE / ACCEPTED at exact T4 head `a8116d8b333cc40e0a599ab208cf7a9a3ea80207` with CI #3663 / R2 #3378 GREEN, including Android full + assemble, ASP contracts and Real Clingo; its migration is limited to the setup recommendation mechanical/rules base, while locks/history remain explicit coordination inputs. The post-2C audit led to accepted TBGS-2D Demon succession and TBGS-2E Mayor redirect migrations. The post-2E fresh re-audit remains historical evidence that no TBGS-2F is selected, because legacy GENTLE/BALANCED/AGGRESSIVE recommendation-style behavior should retire rather than migrate and special registration remains blocked on typed-history production. That audit is archived at `docs/archive/TBGS_2_POST_2E_STYLE_RETIREMENT_REAUDIT_2026-10-04.md`. Current continuation authority is `docs/GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md`. A3 remains READY as a separate maintenance slice only.

## 5. Recovery product boundary

Recent Emergency Recovery is intentionally **short-horizon emergency continuation**, not a normal save-game product.

Current validity contract:

~~~text
current RecoverySnapshot format
+ exact current compatibility token
+ <= 4 hour age
-> validated current recovery plan
~~~

Do not add cross-version migration, old-format reconstruction, tolerant legacy repair or long-term save compatibility unless the product requirement changes explicitly.

The 2026-09-28 current-only Recovery cleanup removed those persistence-only compatibility/minimal-state residues through R0–R6 and was squash-merged as #165 at `b297484cd055b6aa5cfaf1c9b1c4093832af77da`. The completed contract and acceptance record live in `docs/CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md`. R7 bookkeeping/transport/lifecycle ownership is a separate follow-up, not unfinished cleanup.

PR #109 reproduced an old/abnormal event/observation half-state. Current production does not expose a physical persistence window for that intermediate state: the game event and semantic projection execute synchronously before later SideEffect/lifecycle Recovery persistence. #109 was therefore closed rather than converted into legacy compatibility code.

If a future **current-version** crash produces a fresh inconsistent current-format Recovery snapshot, treat it as a new current-format atomicity defect with new evidence.

## 6. Frozen architecture / policy decisions

- `BEGINNER_CONSERVATIVE_V1` remains immutable.
- No placeholder V2. The first real `BEGINNER_CONSERVATIVE_V2` is now evidence-authorized only as the bounded C5-C future-flexibility weak-preference slice after C5-A projector and C5-B pair shadow/replay prerequisites; V1 remains immutable.
- No numeric weights/thresholds without the evidence level required by the affected policy semantics.
- Legal candidate ownership remains in rules/domain owners; SDE ranks only legal alternatives.
- Canonical session/history owners remain the only mutable game truth.
- Long-horizon convergence target: `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md`. Setup Generation, Game Engine and Storyteller Recommendation Engine remain separately evolvable behind typed contracts; this is a guardrail, not a new broad refactor campaign.
- Trouble Brewing first converges through `TroubleBrewingGameSnapshotV1`, an immutable projection over canonical setup/session/history owners. It is the standard read boundary for Game/rules consumers, recommendation context builders and EvidenceLab interoperability; it is not a second mutable authority.
- The TB snapshot must distinguish `KNOWN(value)`, `UNCOMMITTED`, `UNKNOWN`, and `NOT_APPLICABLE`; EvidenceLab provenance/verification does not belong inside Host game state.
- Setup generation should ultimately expose setup composition plus explicit Drunk presence without choosing the seated Drunk; seating/final actual-vs-shown commit remains downstream.
- Storyteller recommendation remains read-only over already-legal candidates and typed decision-point context. Required mechanical/history context must be distinguished from optional enrichment such as player experience, recent role history or public/evil claims.
- Product-owner calibration from the Imp podcast semantic review is recorded in `docs/IMP_PODCAST_PRODUCT_POLICY_CALIBRATION_2026-10-02.md`. It is a revisable policy target only: it does not convert machine findings into VERIFIED evidence, does not satisfy the C3 cutover evidence gate, and does not mutate `BEGINNER_CONSERVATIVE_V1`.
- `ClocktowerGameSession` / its owned aggregate remains canonical truth; `PlayerCard`, UI state, replay and recommendation contexts must converge toward derived projections rather than parallel authorities.
- DecisionTrace/replay/export are read-only diagnostic/calibration projections.
- Red Herring legality/commit ownership is not moved into SDE policy.
- Drunk/Poisoned information may be true or false; impaired narrative uses the accepted shared perceived-functioning projection.
- Spy/Recluse registration remains interaction-scoped.
- Demon bluffs remain a joint SDE output until committed, but DLB stages their commitment at the latest safe presentation dependency rather than assuming one immutable setup-time bundle.
- Drunk seat assignment is now late-bound after shown identities are seated; rules/setup own its legal candidate domain, SDE may evaluate legal alternatives, and canonical setup/session owns the finalized commit.
- Red Herring commitment follows a generic observation/dependency barrier; do not encode a named `if (Spy)` policy shortcut.
- Player-controlled choices such as Poisoner target remain player-owned and may invalidate only still-uncommitted downstream plans.
- No fixture-specific or named-player policy branches.
- Traveller evidence remains outside the current mainline algorithm unless explicitly brought into scope.

## 7. Evidence track

ClocktowerEvidenceLab is the current policy-evidence lane. RH-E is already complete and no longer a gate.

Evidence stages remain:

- E1 — architecture/lifecycle evidence;
- E2 — semantic regression evidence;
- E3 — qualitative policy evidence strong enough to justify a typed preference/reason;
- E4 — quantitative calibration evidence when a policy genuinely requires numeric strength.

Observed expert choices without adequate rationale are not automatically policy labels. Complete real games and expert Storyteller rationale remain preferred over synthetic clean-corpus calibration.

C5 re-entry is now PASS because the G10 Librarian future-flexibility predicate satisfies E3. E4 remains required only for later semantics that introduce numeric thresholds, weights or calibrated tradeoff strength.

## 8. Immediate execution order

~~~text
query live main / workspace
-> DLB document authority convergence (2026-09-29 route)
-> DLB-0 typed intermediate setup COMPLETE
-> DLB-1 visible-roster deal cutover COMPLETE
-> DLB-2 legal Drunk candidate domain + hypothetical projector COMPLETE
-> DLB-3A shadow envelope + DecisionTrace/replay COMPLETE
-> DLB-3B setup-level consequence feature bridge COMPLETE / ACCEPTED
-> DLB-4 canonical Drunk commit before reveal COMPLETE / ACCEPTED
-> DLB-4A Experienced assisted selection UX COMPLETE / ACCEPTED
-> DLB-5.1 typed dependency planner COMPLETE / GREEN
-> DLB-5.2 Red Herring production cutover COMPLETE / GREEN
-> DLB-5.3 Demon bluff production cutover COMPLETE / GREEN
-> DLB-5.4 first-night information / poison convergence audit COMPLETE / NO PRODUCTION GAP
-> DLB-5.5 obsolete setup auto-apply cleanup COMPLETE / GREEN
-> DLB-5 final acceptance + PR #183 merge COMPLETE / ACCEPTED
-> DLB-5H1 narrow presentation extraction COMPLETE / ACCEPTED; executable checkpoint `47136b7d03d72452b70bf3defa847578b30fb011`, CI #3609 / R2 #3337 GREEN
-> Drunk-assignment production cutover gate audit COMPLETE / NOT PASSED
-> Drunk recommendation-context capability contract COMPLETE / POLICY-NEUTRAL
-> TBGS-0 canonical TB snapshot contract COMPLETE / ACCEPTED; executable checkpoint `f367c0d3ec23ebf452c924ff7c0921cd978a800f`, CI #3618 / R2 #3343 GREEN
-> TBGS-1A Host snapshot-backed Drunk decision context/shadow COMPLETE / ACCEPTED; `ae4dc2400325d233da033d3c86d2863bde1bd485`, CI #3621 / R2 #3345 GREEN
-> TBGS-1B EvidenceLab G10 historical materializer + cross-project semantic equivalence COMPLETE; observed EvidenceLab checkpoint `970e7e6430f7088ac004cd1e5696759da4d52003`, byte-for-byte Host fixture match
-> TBGS-1 overall COMPLETE / ACCEPTED
-> post-TBGS-1 cutover recheck COMPLETE / historical NOT-PASSED verdict
-> EvidenceLab C3-Q04 VERIFIED / Stage-1 accepted at `08d95a0c258f687187c0476a0f430fa5ff8229cb`
-> Q04 re-entry audit COMPLETE / evidence gate PASS
-> `DRUNK_ASSIGNMENT_Q04_V1` bounded Empath -> Monk override COMPLETE / ACCEPTED
-> dedicated replay COMPLETE / ACCEPTED; `5cf72a54a62c87763279c02014485a847724b72e`, CI #3631 / R2 #3351 GREEN
-> Beginner production wiring COMPLETE; `0ef3760ee233b0e20fa0dd272b12380abe8ee4a4`
-> Q04 production cutover T4 COMPLETE / ACCEPTED; `9762d5a759bf0eaa81a1f6cb5af1aa28281d3ac2`, CI #3633 / R2 #3353 GREEN
-> Beginner automatic Drunk authority COMPLETE / ACCEPTED
-> DLB-6 old-contract retirement COMPLETE / ACCEPTED; `52c2e73ca455a62c31065ce0e6fca4edda8713ec`, CI #3639 / R2 #3358 GREEN
-> DLB-7 final acceptance COMPLETE / ACCEPTED; `b760117351e57cfe78cc0bc9483b42306757c22f`, CI #3643 / R2 #3361 GREEN
-> DLB campaign COMPLETE / ACCEPTED
-> TBGS-2A first-night natural-pair snapshot context COMPLETE / ACCEPTED; `9021ef26b65033b69911fa4a79124fdd2137284d`, CI #3647 / R2 #3364 GREEN
-> TBGS-2B pair manual/publication COMPLETE / ACCEPTED; `fa07e382fdad1b03aeadc27ce0d0d939f66f8b67`, CI #3654 / R2 #3370 GREEN
-> TBGS-2C setup-recommendation mechanical/rules base-context migration COMPLETE / ACCEPTED; `a8116d8b333cc40e0a599ab208cf7a9a3ea80207`, CI #3663 / R2 #3378 GREEN
-> TBGS-2D Demon succession COMPLETE / ACCEPTED; final T4 `1f610a25a16c14a7ca7bd38562ff39425508c243`, CI #3726 / R2 #3429 GREEN
-> TBGS-2E Mayor redirect COMPLETE / ACCEPTED; final T4 `ce5b9943bf4c0d1a27b73c5de5d45a841349949f`, CI #3731 / R2 #3433 GREEN
-> post-2E fresh re-audit COMPLETE — no TBGS-2F selected
-> EvidenceLab G10 `16:52` Librarian future-flexibility predicate E3 PASS at `78f672868ea6603317aeefa20ad91686c5886db9`
-> C5 re-entry audit COMPLETE / PASS
-> C5-A/B/C/D/E COMPLETE / ACCEPTED; functioning-Librarian production cutover accepted historically
|| RSR-0 RecommendationStyle retirement / player-level audit COMPLETE / ACCEPTED
-> RSR-1A Storyteller-mode/style ownership decoupling COMPLETE / ACCEPTED; PR #217 merge `881d4252c1a242c5ea523718fb8f780f693e9ab5`
-> LRE-0 whole legacy heuristic recommender audit COMPLETE / RETIREMENT AUTHORIZED
-> LRE-1 manual fallback + fail-closed recommendation authority COMPLETE / ACCEPTED; executable `045b3a6884f765149d4f1802d5e369671d38d938`, CI #3752 / R2 #3448 GREEN
-> INV1-A functioning-Investigator special-policy cutover merged in PR #222 / historical executable reality
-> LRE-P family-by-family special-policy continuation SUPERSEDED
-> GSP-0 route reset COMPLETE / DOCS-ONLY
-> GSP-1 Q04 / Librarian V2 / INV1-A discretionary automatic authority revoked COMPLETE / ACCEPTED
-> GSP-2A structured provider contract COMPLETE / ACCEPTED — checkpoint aed23bad; CI #3769 / R2 #3459 GREEN
-> GSP-2B1 typed game context + decision episode COMPLETE / ACCEPTED — checkpoint 9ee47da; CI #3774 / R2 #3463 GREEN
-> GSP-2B2 session-owned player-context overrides COMPLETE / ACCEPTED — checkpoint 9d3bfdcb; CI #3778 / R2 #3466 GREEN
|| 2026-10-06 architecture reset: old GSP-2B3/2C/2D sequence SUPERSEDED; provider work targets the neutral RES boundary
-> RES-0 separation/purge audit + active-doc authority reset COMPLETE / ACCEPTED
-> RES-1 engine-only pending-decision/legal-domain boundary COMPLETE / ACCEPTED — pair + Mayor prove generic seam
-> RES-2 neutral script-aware provider contract extraction COMPLETE / ACCEPTED — PR #236 merge 19abd61; Drunk/context-memory closure PR #237 merge 7ae03105
|| stateless provider != memoryless recommendation: Host owns/rebuilds current-game longitudinal memory, relevant cross-game player history and soft recommendation-diversity history
-> RES-3 legacy heuristic/style/weighted recommender physical purge COMPLETE / ACCEPTED — checkpoint 5b4b2a94; CI #3815 / R2 #3495 GREEN
-> RES-4 evidence-case deterministic special-policy physical purge COMPLETE / ACCEPTED — T4 00aefd1b; CI #3824 / R2 #3502 GREEN
-> RES-5 physical module/dependency convergence COMPLETE / ACCEPTED — T4 `cdf5dba23eaad23f4fea3899ab54df97553a55cd`; CI #3832 / R2 #3509 GREEN
-> GSP-R0 post-RES-5 re-entry audit COMPLETE
-> GSP-R1 neutral current-game longitudinal context NEXT
-> GSP-R2 Storyteller player-context edit + durable experience profile
-> GSP-R3 generic cross-game recommendation/diversity context
-> GSP-R4 prompt/response materializer + local validator
-> GSP-3A manual blind benchmark BEFORE any API integration
|| Android product invariant: offline-first; network/LLM optional only
|| later player profile/management: BEGINNER / NORMAL / EXPERT; current default NORMAL
|| A3 presentation catalog remains READY / independent maintenance only
~~~

A1/A2 may proceed as separate maintenance PRs without blocking the active lanes. C5/V2 is now a separately re-entered evidence-backed lane; its production cutover remains surface-gated.

## 9. Current authorities

Read first:

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. this roadmap
5. `docs/NEXT_DEVELOPMENT_HANDOFF.md`
6. `docs/RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md` — current executable architecture/separation/purge authority
7. `docs/GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md` — downstream general-provider/benchmark route; paused where it conflicts with RES sequencing
8. `docs/LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md` — legacy heuristic retirement inventory/evidence
9. `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md` — current TB-only canonical snapshot / typed-decision interoperability foundation
10. `docs/CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md` — long-horizon ownership target, now activated by RES
11. completed DLB/C5/SDE audits — historical evidence only; read when a concrete ownership/replay issue requires them

The 2026-09-28 source audits, completed Recovery/C4 audits, SDE freeze/cutover audits, and older SDE checkpoints are historical or specialized evidence. Read them only when the current slice raises a concrete ownership/evidence question.

## 10. Testing cadence

Follow `docs/TESTING_STRATEGY.md`.

RH-E completed at T4 with exact-tree CI #3478 and R2 #3231 GREEN, including Android FULL + assemble, ASP contracts and Real Clingo cross-validation.

The #157 live-main integration head `371ebf624898c08747203aceaed1254647867214` also completed exact-head acceptance with CI #3479 and R2 #3232 GREEN. This integration changed workflow/documentation only relative to the previously accepted continuation head.

GitHub CI/R2 remains the normal Android execution and independent remote acceptance surface. The N3150 execution-host experiment remains retired from the default workflow unless the user explicitly reopens it.

A documentation-only compaction does not require Android regression by itself.

