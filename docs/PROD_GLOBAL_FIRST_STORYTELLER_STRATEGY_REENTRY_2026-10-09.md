# PROD-GLOBAL-1 — Whole-game AI Storyteller strategy and table UX re-entry (2026-10-09)

> **ACTIVE DESIGN CORRECTION / PROD-1 MERGE BLOCKER.** Product-owner feedback replaces the decision-by-decision entry assumptions in GSP-PROD-AUTO. Do not treat PR #292's green tests as product acceptance. No MEM0 retest, Recovery expansion, retired heuristic, or extra standalone Drunk policy is needed. One strong LLM should reason about the **whole game first**, and every downstream recommendation is a constrained step within that continually revised plan.

## Product and user experience — independent of experience level

`StorytellerOperationMode = MANUAL | AI_ASSISTED | AI_AUTOMATIC` is orthogonal to the existing `StorytellerExperienceMode` (BEGINNER/EXPERIENCED); do **not** repurpose BEGINNER automatic or EXPERIENCE to mean the new mode.

| Mode | Setup/dealt-view analysis | Every pending choice | Failure |
| --- | --- | --- | --- |
| MANUAL | **No LLM invocation and no AI recommendations or AI controls in gameplay**. Existing offline game, rules, table and manual options untouched. | User chooses/Host verifies. | Entire experience remains offline. |
| AI_ASSISTED | One **private** whole-table situation analysis after the roster is fixed, presented to the human Storyteller **after player-card deal/reveal** (or as a Host-only pre-deal preview where initial Drunk needs resolving). Issue-first written summary + table relation overlay + conditional future roadmap. | Primary, genuinely different alternatives, rationale tied to global issues/future consequences; human Host confirmation. The same strategic snapshot is updated with new canonical facts. | Clear error or defer; continue manually; do not invent a default policy. |
| AI_AUTOMATIC | Same global diagnosis and plan internally; progress as soon as legally permissible without human confirmation; optional read-only host diagnosis, progress/pause view. | Host checks legal candidate/identity/revision and automatically commits ONLY via its existing transaction; replan if factual situation changed. | Pause, retry or explicit manual takeover; no legacy ranker or model-owned game truth. |

Mode selection belongs at game setup or a dedicated Storyteller control and is visible before play; mode changes and the pause boundary must not reinterpret already-committed facts. Only Host-only surfaces display hidden-role plans or evil pressure overlays. Never leak grimoire or strategic hints into player pass-phone/reveal views.

## Lifecycle: reconcile 'after dealing' with Drunk late binding

**There are two different events**: (A) visible character/seat assignment is fixed and (B) player cards are revealed. The current real production App chooses Drunk after A and **before** B / creation of `ClocktowerGameSession`. Therefore:

1. `PRECOMMIT_STRATEGIC_ASSESSMENT`: after A, build real `TroubleBrewingGameSnapshotProjector.fromIntermediate`. Analyze **all** shown roles/seats and known fixed evil; Townsfolk actual identity is explicitly `UNCOMMITTED`. Host includes the full Drunk legal domain; model analyses whole ecology and returns a **conditional/global plan plus current Drunk recommendation** as one coherent output. No fabricated already-committed Drunk.
2. Host (assisted or automatic) resolves legal Drunk through existing pending decision and `TroubleBrewingSetupCommitter`; manual mode skips AI completely. If there is no Drunk, proceed with normal deal and run full-game assessment at first complete Host state in assisted/automatic modes.
3. `POST_DEAL_STORYTELLER_OVERVIEW`: once player cards are dealt/shown, display the **confirmed**, Host-only situation/relationship analysis in AI_ASSISTED. Precommit strategic hypotheses must be reconciled with committed Drunk and first-night order before being rendered as actual game facts. AI_AUTOMATIC may expose a read-only explanation but does not wait for approval.
4. `PENDING_DECISION_UPDATE`: at each genuine next legal Host decision, pass current as-of snapshot, authoritative confirmed facts, **current strategic intent snapshot** and currently legal candidate IDs to the model. Ask it to diagnose what changed and choose/justify its next action **against the existing whole-game plan**. Validate and commit or pause.
5. `AFTER_COMMIT`: update strategy/projections and mark fulfilled/superseded intentions. Player claims/pressure/skill and public observations affect revisions only when actually entered; false-model explanations NEVER become canonical facts.

This is **not** one big irreversible sequence of planned decisions. Host can only execute the present legal action; later possibilities remain conditional because Poisoner, Demon, living players, votes and social information can change.

## Global-first prompt contract

The model input must be the full game-state projection (as-of, with explicit epistemic completeness), full seat order and **shown roles for every seat**, known actual roles only when genuinely known, player experience, optional known claims/pressure, key committed history, current legal candidates and prior strategic intent. It must not receive future truths, fake witness registrations or a preselected answer.

Instructional flow (ask for **concise auditable conclusions**, not private chain-of-thought):

1. **Diagnose the current game's most important vulnerabilities/opportunities before choosing any clue**: relative evil pressure and player experience; powerful/mutually reinforcing early Good information; collision/ambiguity opportunities; potential premature forced certainty; future information ecology; ability execution dependencies; what Good could reasonably deduce.
2. Identify the **key interactions across roles and seats** and construct multiple plausible interpretable worlds. Distinguish Host facts/registration possibilities from speculation and strategically risky claims.
3. Articulate whole-game objectives and 2–5 **conditional future coordination intentions** with triggers, tradeoffs, expected downstream effects and uncertainty; do not guarantee future illegal/unavailable choices.
4. **Only then** recommend the current legal candidate and a genuinely different alternative, referencing the global issues it addresses and the downstream strategy affected.
5. When facts change, compare **current situation vs prior intentions**: keep, revise or retire plans with the reason and explicit factual anchor; do not silently rewrite historical reasons.

Avoid answer contamination from prior benchmarks, policy-level named-role if/else, one-score-per-candidate, static good/evil win probability. The target is fair, interesting, solvable and appropriate for the actual player skill mix.

## Minimum structured contract (new cohesive types, no second recommender)

`StorytellerStrategicAssessmentV1` — Host-side **noncanonical strategic read model**, scoped to gameId, analysisId, as-of sourceRevision and provenance:
- `situationSummary` — main tension, opportunity, pitfalls, uncertainty.
- `issues[]` — issueId, priority, relevantSeats, relatedRoleIds, knownFactReferences, expectedGoodInformationEffects, alternativePlausibleWorlds, whyItMatters.
- `relations[]` — relationId, sourceSeat, targetSeat, kind (`ADJACENCY`, `INFO_OVERLAP`, `REGISTERED_AS_POSSIBLE`, `PRESSURE_INTERACTION`, `PROTECTION_OR_ATTACK_POTENTIAL`, `OTHER_STRATEGIC`), knownFactReferences, issueIds, explanatoryLabel, epistemicType (`HOST_VERIFIED` vs `MODEL_HYPOTHESIS`). The model **cannot** assert `HOST_VERIFIED` itself; Host projects factual edges. Model-provided references must resolve to present seats and verified or clearly hypothetical evidence; otherwise reject or omit.
- `intentions[]` — planItemId, goal, preconditions, contingentDecisionFamilies, expectedImpact, risk, status (`ACTIVE`, `REVISED`, `RETIRED`); these are **strategic preferences not game facts**.
- `nextDecisionAdvice?` — exact current Host decision ID/revision; legal primary + distinct alternative IDs with rationales and links to issue/plan IDs; omitted at informational-only checkpoints.
- `coverageAndUncertainty` — explicitly unavailable claims/history, unknown actual roles, unsupported future families.

The existing `StorytellerProviderRequestV1`, `StorytellerProviderResponseValidatorV1`, engine-created `PendingStorytellerDecision` and Host commit barrier are still used for executable *decisions*. A separate global assessment stage may be `ANALYSIS_ONLY` with no legal action. Never manufacture a dummy Host decision to trigger analysis. Prefer a new **neutral assessment envelope/typed projection** and the existing provider gateway over a role-specific `DrunkGateway` as final architecture. The model only proposes policy, not GameState, graph fact authority or committed action.

Continuity: keep a bounded, per-live-game `CurrentStrategicPlan` with `asOfRevision`, issue/intent statuses and brief prior model rationale, updated after legitimate Host commit and cleared on new game. If no reliable plan exists, rebuild from known Host facts; missing plans are not empty history. LLM conversational state can be an optional optimization, **never the only source** of strategy or mechanical facts. Do not begin a durable Recovery/memory subsystem in PROD-1.

## Square-table UX — reuse existing real geometry

Use the existing `HostTableLayout`, `HostTableSpatialSlot.centerX/centerY`, `ClocktowerSquareTableSeatSurface` and `ClocktowerHostSquareTableScaffold`:
- Storyteller overview: table sits centrally, seat cards remain their canonical order and shape; temporary **overlay** draws only 2–5 prioritized issue relations as geometric lines/arcs. Color, line treatment and legend differentiate factual adjacency from hypothesis/possible information link; do not use a line as proof that Spy/Recluse took one particular registration witness.
- Seat/edge taps select an issue and highlight implicated seats; nearby explanation panel summarises **why** Chef, Empath, Investigator, FT, Spy/Recluse, evil seats, etc. interact for this specific setup. Do not hardcode role combinations into the renderer. Toggle `全局分析 / 关键关系 / 后续策略`, and hide overlays to reduce clutter.
- The primary/alternative **current** action appears in a decision context subpanel tied to the strategic assessment; does not require switching to a separate unrelated policy page.
- In MANUAL no analysis tab, AI labels, recommendation pills or automatic HTTP calls. In AUTO no blocking confirm UI; expose pause/takeover.
- Presentation is derived from validated analysis and the Host's canonical square-table seat map; **not** model-invented coordinates or a new seat-order owner.

## Single-user credentials & transport (2026-10-09 revised preference)

The owner is willing to trade some complexity for single-user convenience and proposes compile-time API key embedding if 'not plaintext'. **Compile-time obfuscation/encryption does not make a key secure**: an APK that can make a request can leak the corresponding credential when inspected at runtime. Official OpenAI key-safety guidance discourages deploying API keys in mobile clients. Prefer avoiding a public gateway by running the already-coded secret-owning gateway on the Oracle developer machine over a **private authenticated HTTPS tunnel / Tailscale Serve**, if a trustworthy path is actually available. This retains no OpenAI API key in the APK; optionally store only the revocable **gateway access token** once using Android Keystore-backed encryption, and keep backend model configuration on Oracle. A local, disposable private direct-API build is an **explicit personal-risk alternative** only, with a separate limited-scope short-lived revocable project key and hard budget; do not portray it as encrypted protection or enable it for release. No remote access without TLS/auth or insecure hidden default credentials.

Prioritize **global prompt/strategy quality over deployment-path churn**. `tools/storyteller_gateway.py` and `ProductionDrunkAiGatewayV1` are transport/validation spikes, not the final role-specific recommendation abstraction.



## Required dynamic replanning trigger — Poisoner target + partial human acceptance (2026-10-10)

This is a **PROD-GLOBAL-1 product acceptance requirement, not a later heuristic optimization**. A night-wide strategy is provisional until its actual in-order upstream events have occurred. The Poisoner is an **evil player choice**, not a choice the Storyteller or model can precommit. The Poisoner's confirmed night target may change which abilities function and therefore the reliability, legal presentation, and mutual implications of *all still-undelivered information*. No model may treat the pre-Poisoner plan as final after the target is known.

Event-driven sequence:
1. Host plans only *conditional* first-night information intentions before Poisoner action. The first-night scheduler keeps its canonical dependency/order barriers.
2. After the Poisoner **actually chooses** and Host confirms the action, commit a typed `POISON_TARGET_CONFIRMED` fact with game/night, action order, target seat and revision. Independently obtain the **effective** poisoning/reliability from Host rules; chosen target and actual effect must not be conflated.
3. Invalidate recommendations prepared against the earlier revision and **reassess the whole game** with committed setup, effective ability reliability, prior confirmed information/registration outcomes, action/history prefix, player context and still-pending legal choices. Keep/revise/retire strategy and explicitly call out any changed assumptions. No stale pending decision may commit while assessment is running.
4. For each subsequent first-night information step, give the current Host-legal recommendation and meaningful alternatives; the human may accept, alter or decline it. Only an actually committed/shown information result becomes immutable game history. An overridden or corrected result triggers the same downstream replan; accepting an unchanged recommendation may reuse the existing strategy **only after checking the factual/revision dependency**, never based solely on matching display text.
5. Recalculate only *remaining* contingent advice. Never retroactively rewrite already delivered clues, fabricate a unique Spy/Recluse registration witness, or assume poisoned players must receive false information. Their ability may be nonfunctional but the displayed information can coincidentally be true.
6. Each night may select a different Poisoner target; newly committed targets and expired effects trigger the same dependent update, as do material demon/role/death/player-input changes. When AI is unavailable, assisted play continues with clearly marked unsupported/manual decisions; automatic mode pauses, never using stale pre-poison advice.

**Mandatory executable acceptance scenario**: initial multi-clue plan -> Poisoner chooses an information-bearing seat -> Host updates effective reliability -> remaining clue recommendations are recomputed -> first clue confirmed as recommended -> second clue manually overridden and confirmed -> subsequent clue is regenerated with both confirmed facts and revised plan. Assert no recommendation uses the pre-poison revision, no confirmed prior information is overwritten, and distinct candidates remain Host-legal. Add a control scenario where Poisoner chooses a non-information seat and the dependency planner still considers implications rather than hardcoding per-role recalculation.

This requirement sits **before** completion of the next real-decision strategy continuity milestone. Passing the Drunk/post-commit analysis tests alone does not satisfy it.

## 2026-10-09 implementation checkpoint — DONE / NEXT precisely distinguished

**Implemented in Draft #292**:
- Host-authoritative precommit roster -> global issue-first strategy and actionable legal Drunk recommendation; Host validates pending identity/revision/current choice, auto confirm only in explicit bounded AUTO mode.
- Host-authoritative **committed** roster -> independent `ANALYSIS_ONLY` model call, even with **no Drunk**; this call contains all actual/shown role IDs and never fabricates a pending decision or legal candidate. Earlier strategy is submitted as advisory fallible `priorStrategy` (with matching game identity) so the LLM must keep/revise/retire earlier intentions and reconcile human Drunk overrides.
- After-deal Host-only strategy overview with same canonical seat geometry, hypotheses clearly styled as dashed links; if analysis is pending/unavailable, the UI offers retry/explicit Manual takeover rather than passing off stale precommit advice as fact.
- Both provider and Android reject invalid seat/issue references and wrong analysis identity/revision; App rejects late callback after game identity/mode/first-night setup boundary changes.
- API key remains server-side; private per-session gateway URL/token is set before dealing. No production gateway deployment/real Android paid call is claimed.

**NOT IMPLEMENTED / still blocks PROD-1 acceptance**:
- Following **a second actual Host pending TB decision** (e.g., first-night information) through the **same** strategic intent, latest canonically observed facts and full legal candidates, to a fresh global diagnosis/recommendation with UI and Host confirmation. Postcommit analysis is an additional strategic checkpoint but does not yet cover the next discretion.
- Complete TB-first-night and subsequent AUTO coverage, more than bounded Drunk auto-confirm, and production end-to-end model deployment. Existing unsupported auto steps pause.
- Strong live-model smoke and trustworthy private TLS gateway reachability; green offline contract/Android CI is not equivalent to real API evidence.

Acceptance remains **NO-GO until the next real decision consumes strategy**, with no new role-specific heuristic or new Recovery subsystem.

## Re-entry and tests (one coherent PR, no repetitive audit)

1. Update current PR #292 scope and prevent merge as standalone Drunk; retain useful transport, legality, stale-request and setup-commit tests.
2. Generalize the model request to global issue-first assessment and one strategy snapshot. First decision uses same global answer; include at least one 8-player multi-role interaction (Chef+Investigator+Recluse or Chef+Empath+Poisoner) as a **test input**, not a hardcoded policy.
3. Implement Host-only global overview on the real square table and three explicit operation modes; manual path has **zero** AI UI/network. Ensure correct precommit/after-deal boundary.
4. Next pending TB decision consumes *same plan + new facts*, produces a plan-linked recommendation, and cannot commit stale or illegal IDs. AI_AUTOMATIC applies this via Host after validation; paused failure/resume/takeover.
5. T0: data completeness, issue-first prompt/typed references, forbidden future facts, under-specified candidate, stale versions, false registration assertion, no-model manual, fail-closed auto, relation map seat fidelity and strict transport secret separation. T1/T4 and GitHub independent CI/R2 on latest head; one real model/Host path through private TLS. No blind benchmark expansion.

**PROD-1 acceptance is not merely `Drunk candidateId -> model -> Host confirm`**. It is `whole game -> diagnosed tensions -> strategic plan -> one legal action + its effect on future plan -> private storyteller overview`. Only after this should the product claim first usable global recommendation; then advance fast to PROD-2 automatic loop.
