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



## 2026-10-10 product principle — simulate a real Storyteller's ongoing judgment, not event/role special cases

**Supersedes the prior Poisoner-specific acceptance subsection.** Poisoner targets, a Slayer shot (including a publicly claimed or attempted shot without a functioning Slayer ability), a Virgin nomination/ability interaction, confirmed information, death, resurrection, ability loss/change, day votes, new claims and pressure are **illustrations of a universal event stream**, never independent recommendation subengines or hardcoded policy triggers. Day events cannot retroactively alter already committed first-night information; they affect the live game and future decisions. The model should behave like a competent human Storyteller who continually watches what ACTUALLY happened, what people were shown or heard, what can plausibly be inferred, and what choices remain available.

### Single causal core, multiple epistemic views

The Game Engine, not the model, commits temporally ordered events and applies rules. Each event has immutable provenance and an as-of cutoff: `gameId, phase/night/day, round, sequence, sourceRevision, actor/target, eventKind, committed payload, affected information audience, effective rule consequences, visibility, confidence/recording coverage`. Do not invent events to fill unavailable inputs. **Separate the following planes**:

1. **Mechanical reality / Host truth**: actual identities and alignments, alive/dead, poisoned/drunk, ability functioning and consumption, selected targets, actual application/registration and legally committed outcomes. A target selected is not automatically an effect that succeeded; an announced action is not automatically a real functioning ability.
2. **Experienced / communicated information**: who was told what, public nominations, votes, public shots, deaths and outcomes, private player-facing observations. Persist the confirmed *displayed result* without inferring a unique Spy/Recluse registration witness or converting a player's claim into Host truth.
3. **Player-facing epistemic possibilities**: what an individual or group could reasonably believe given only what was visible to them, their claims and plausible worlds. This is strategic inference, not an omniscient fact table: the Host cannot know secret conversations or private mental beliefs that were not entered. Mark missing discussion/claims as `UNKNOWN`, not `NONE`.
4. **Storyteller goals / strategic memory**: a versioned, bounded, revisable global game plan: current pressure/tension, alternative plausible worlds, player skill, bluff plausibility, information interplay, player experience/fairness, repeating prior-game patterns, and contingent intentions. Each rationale references observed facts and uncertainty; strategic preferences never become rule facts.

The same Host snapshot/history projection feeds the LLM, with explicit **observation horizon** and causal prefix. The model must distinguish *mechanical consequences*, *who knows what*, and *likely strategic/social consequences* before proposing or revising intentions. It must neither optimize solely for Good/Evil victory nor solve a canned role-specific puzzle; a professional Storyteller aims for a coherent, fair, interpretable, engaging game across the entire table.

### Generic event-driven strategy loop

`confirmed player action OR confirmed Storyteller adjudication OR recorded public/private information OR relevant player-context edit -> Host validates/commits one canonical event -> derive current effective rules and visibility -> invalidate any recommendations whose frozen revision/prefix is superseded -> build latest as-of global assessment -> keep/revise/retire strategic intentions -> recommend only the next *current legal* decision with meaningful alternatives -> human confirms (ASSISTED) or Host verifies and commits (AUTO) -> repeat.`

- **Observe every meaningful committed event** (including an ineffectual ability claim or attempt that changes publicly available information); no `if role == Poisoner/Slayer/Virgin then replan` policy. The Engine alone uses script-specific abilities for *mechanical adjudication*. Strategist consumes normalized typed facts and their legal effects.
- **Replanning does not imply a paid call on every UI tap**. Draft selections, viewing a candidate and uncommitted speculation do not change truth. At the next relevant Host decision, revalidate all dependencies and source revision; if impact is confidently irrelevant, an explicit freshness/coverage check may reuse an unchanged plan. Initially prefer recomputation at each materially changed prefix instead of over-optimizing thresholds.
- **Minimize Host input burden**: derive action, decision, target, reveal and outcome events from existing normal hosting interactions automatically; never require the Storyteller to enter the same operation a second time solely to feed AI. Optional player claims, private-chat reports, player pressure and inferred motivations may be entered manually, but missing inputs remain explicitly unknown rather than falsely complete.
- **A genuine committed event can have no direct game-mechanical effect but still change social reasoning** (e.g., a fake Slayer shot). Conversely, a hidden Poisoner target can change mechanical reliability before anybody knows the reason. These are distinct inputs to the same loop.
- **Causal time is nonnegotiable**: a later day nomination/Virgin event can revise future-night plans and living-player worlds, but can never rewrite what was known or displayed during the already completed first night. Already shown information and confirmed public actions are historical anchors; revise only **pending** recommendations and conditional future intentions.
- **Source-of-truth separation**: Host supplies rules, legal candidates and authoritative effect; a false ability claim is an action/claim, not a true role; a trigger requires Host's actual functioning/eligibility checks; model guesses about intent, pressure or players' beliefs are marked hypotheses.
- **Modes share the same planning engine**: MANUAL no AI calls/UI; AI_ASSISTED shows the revised global diagnosis and offers actual legal alternatives while accepting partial manual overrides; AI_AUTOMATIC applies only fresh Host-verified legal results, and pauses rather than reverting to retired heuristics on unavailable model/unsupported decision.

### Product acceptance: multi-phase, mixed real/claimed-action scenario

One integrated, rules-backed TB game with real seat/role setup and multiple first-night information steps:
1. Global initial strategy and legal first pending result; a *player-originated* night target changes effective reliability; newly computed remaining advice references this confirmed mechanical effect without rewriting already displayed information. Do **not** special-case which role/seat was targeted.
2. Host publishes an information result as recommended. The human then changes a different *still-pending* result. The next recommendation must use **both actually confirmed player-facing results**, not the original proposals, and link to the same revised global strategy.
3. On a later day, publicly recorded Slayer-type action/claim (whether genuinely functioning, nonfunctioning, or merely claimed) must be represented with **separate public action vs actual rule effect**. It may influence future strategic advice even without a kill, while preserving uncertainty about other players' beliefs.
4. A subsequent nomination of a Virgin-role player records public nomination and the **real Host rule outcome** (if and only if actual eligibility/functioning triggers apply). Its social and mechanical implications affect future nights and player reasoning; no effect can reach backward into first night.
5. In every phase: stale responses cannot be committed, displayed-vs-true remains distinct, private/public visibility cannot leak through pass-phone UI, and any unentered conversation is UNKNOWN. Include a no-direct-mechanical-effect but public-social-impact case, an actual mechanical-impact case and a no-relevant-change control using the **same planner**, not bespoke policies.

These are **behavioural regression fixtures**, not role names that activate recommendation logic. Build generic event causality and strategic belief-state projection first; only after that expand coverage of actual supported decisions. Do not expand Recovery or legacy special recommendation code merely to pass the fixtures. This unified loop — not a Poisoner trigger or a standalone Drunk analysis — is the PROD-GLOBAL-1/2 architectural direction.

## 2026-10-09 implementation checkpoint — DONE / NEXT precisely distinguished

**Implemented in Draft #292**:
- Host-authoritative precommit roster -> global issue-first strategy and actionable legal Drunk recommendation; Host validates pending identity/revision/current choice, auto confirm only in explicit bounded AUTO mode.
- Host-authoritative **committed** roster -> independent `ANALYSIS_ONLY` model call, even with **no Drunk**; this call contains all actual/shown role IDs and never fabricates a pending decision or legal candidate. Earlier strategy is submitted as advisory fallible `priorStrategy` (with matching game identity) so the LLM must keep/revise/retire earlier intentions and reconcile human Drunk overrides.
- After-deal Host-only strategy overview with same canonical seat geometry, hypotheses clearly styled as dashed links; if analysis is pending/unavailable, the UI offers retry/explicit Manual takeover rather than passing off stale precommit advice as fact.
- Both provider and Android reject invalid seat/issue references and wrong analysis identity/revision; App rejects late callback after game identity/mode/first-night setup boundary changes.
- API key remains server-side; private per-session gateway URL/token is set before dealing. No production gateway deployment/real Android paid call is claimed.

**NOT IMPLEMENTED at the 2026-10-09 checkpoint (the first item is partially addressed by the 2026-10-10 slice below; remaining acceptance gates stay open)**:
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

## 2026-10-10 PROD-GLOBAL-1A checkpoint — first REAL live decision continuation, still Draft

Branch `prod-1-live-drunk-ai-assisted`, PR #292, initial code checkpoint `d72faee0c64fd099d056167ca4e668bd3d1d4148`.

- An **existing** session-owned `GLOBAL_V1` action/observation prefix, with a frozen game-wide cutoff and explicitly partial coverage, is now serialized for the global strategist. A confirmed private player action remains Host truth, while a received private/public information result is a separate observation; unrecorded claims and conversations remain **UNKNOWN**. No new event writer, Recovery pipeline, rule reducer or heuristic was introduced.
- The first subsequent Host-owned **pair-information** pending decision gets all legal candidate IDs from `PairInformationDecisionBoundary`/`StorytellerProviderRequestFactoryV1`, the current actual/shown roster, player inputs, causal prefix and the prior fallible global strategy. The gateway has a non-Drunk-specific continuation prompt and rejects invalid causal prefixes and nonlegal response candidate IDs.
- On the real first-night Host screen in `AI_ASSISTED`, supported existing pair-information interactions can request refreshed global advice after actual actions and confirmed prior information. The UI shows a seat-linked primary and alternatives, then leaves the existing Host publication/confirmation controls with the user. An override becomes a real subsequent observation and therefore enters the next request. A missing model/unsupported action does not create a fake result.
- Freshness checks include the **global event cursor** as well as game/player revisions and the exact pending/legal-candidate domain; neither an old recommendation nor a potential registration witness is made authoritative. `MANUAL` is unchanged, and this new pair continuation intentionally does **not** auto-commit in `AI_AUTOMATIC`.
- Targeted gateway tests cover accepted ordered facts/observations, future/reordered/false-history rejection and whole-game plan prompt. A Kotlin game-session test exercises action → first Host-legal information → new live request → manual legal override → third as-of prefix and stale-response rejection. Independently run CI/R2 must be checked against the latest commit, not assumed from pre-existing green checks.

**Remaining before PR #292 merge:** real Android / private TLS LLM smoke with model quality review; runtime/manual override interactive verification; unified continued planning for non-pair first-night legal families, day and subsequent nights; and validated automatic execution or explicit pause across actual supported decisions. The full multi-phase scenario in this document is **not** satisfied by the first-night pair slice. Keep PR #292 Draft until these are addressed and evidenced; do not create role-named recommendation branches to achieve them.

## 2026-10-10 PROD-GLOBAL-1B — extend SAME causal global advice across phase boundary

Code checkpoint: `1c278ffbbb8c4e1292be67f7089564e8c589b2c8` on PR #292 (Draft). **Scope is decision-domain plumbing, not a Mayor-specific recommender.**

- The already neutral `StorytellerProviderRequestFactoryV1.fromMayorRedirect` now feeds the **same** `StorytellerGlobalDecisionRequestV1` strategy envelope, event serializer, gateway, prior intent and current legal candidate response validator used for first-night pair information. The transport accepts two typed legal payload shapes (pair display, seat target) without heuristic selection or specialized event replan hooks.
- In an actual NIGHT Host pending redirection step, `AI_ASSISTED` can show latest full-game reasoning and verified target candidate IDs; the human remains responsible for the ordinary existing Host legal confirmation. The prior first-night private revelations, daytime public attempts/nominations, deaths and current-night mechanical actions enter the same as-of chronological prefix, subject to existing event-producer coverage and visibility. `MANUAL` never calls the LLM and `AUTO` remains unsupported for this new decision.
- Freshness proof has been factored into a common decision identity / game+player revisions / exact legal candidate IDs / global event-cursor check. New regression inputs include an 8-player engine-owned live game carrying a first-night private numeric observation, a public day nomination, a public ineffective shot, the subsequent night target choice and an invalidating later action, plus gateway tests of target payload and causal order.
- **This demonstrates an existing decision contract reused across nights, not complete night/day autonomy.** Numeric, Boolean and other first-night decisions still need typed Foundation legal inputs and same reusable planner without adding role-named recommendation policies. There is no Android→private TLS→real model smoke yet. The PR must remain Draft pending actual gameplay validation and AUTO control/fail-closed coverage. Verify every CI/R2 claim against the exact latest pushed head.

**Next focus:** extend the generalized request using **typed Foundation numeric/Boolean current legal decision sources** (not a curated UI recommendation pool), then acceptance-test partial human overrides and candidate target-draft changes; afterward close full first-night assisted loop and proceed to automatic Host-authorized execution.

## 2026-10-10 PROD-GLOBAL-1C — FIRST-NIGHT INFORMATION coverage checkpoint (PR #292 Draft)

New code checkpoint `57982dcd37e172bd2140593848d8d57be069e493` extends the **same** global causal LLM continuation to current TB numeric/Boolean information results, in addition to the existing pair-information coverage. Numeric/Boolean result values come from the existing Foundation typed legal choices when a standard current decision is available, or from the Host's complete result-first manual domain when Spy/Recluse permit multiple legitimate observable values. Duplicate legal witnesses with the same display value do not become separate alleged facts or a forced registration choice.

- Actual first-night supported information surfaces are: Washerwoman / Librarian / Investigator pair disclosures, Chef / Empath numeric disclosures, and Fortune Teller Yes/No after the **player actually chose both query seats**. There are **no** candidate-level role-specific AI ranking policies; role handling remains an existing rules/presentation legal-domain adapter.
- Provider serializes scalar `NUMBER`/`BOOLEAN`, metric and subject seats as neutral typed context. The same global situation diagnosis, private/public event prefix, prior contingent strategy, legal candidate validator and Host display confirmation remain in charge. In assisted mode **only**, an AI response is advice; manual Host choice can override each individual output. The next request consults real committed observations, not model proposals.
- Defensive boundaries: no preselected Fortune Teller subject pair is invented; a new pair/current legal result domain/new event cursor changes the decision identity / live freshness key and causes a refreshed request; no prior displayed result is rewritten. Gateway rejects fabricated numeric/Boolean representations and illegal/out-of-order events. Tests cover healthy numeric singleton, impaired complete numeric results, result-first witness ambiguity, Boolean pair-selection barrier and gateway schema/invalid ID.
- **Scope honesty**: This closes the planned first-night *informational result recommendation surface* in code after CI acceptance, **not** the entire automatic first-night host (night target actions, red herring, bluff choice, manual UI interaction, LIVE API quality and deployment are distinct). The actual model quality is untested in the Android game until a real HTTPS gateway is configured and exercised. PR #292 remains Draft until production end-to-end and product acceptance.

### Real LLM test entry and evaluation method

**Immediately after this checkpoint is CI/R2 GREEN**, execute a real model-backed **AI_ASSISTED** first-night quality smoke in an Android debug build connected to the Oracle developer-machine private HTTPS authenticated gateway. The model and budget are configurable server-side; no raw OpenAI key in Android. Verify one test 8-person TB actual setup with pair + scalar abilities and a Poisoner; run both initial strategy and incremental calls in real chronological order. Have Host present the primary/alternatives and first-night information; manually accept one, override a different result, and verify next model request sees actual committed results and current target poisoning effects. For Fortune Teller, select two real query seats before requesting the binary advice, then change the draft pair and ensure any old answer does not validate. Keep backend request/response JSON and exact model ID, sanitized event prefix, source revision, candidate IDs, latency, tokens/cost, errors and manual judgment.

Rate outputs against **whole-game coherence, legal/fair/playable clues, interaction of information, uncertainty handling, candidate legality and genuine alternative quality**, not a preference for one unique 'correct' clue. Compare a strong configurable model with baseline on the **same frozen Host decisions**, preferably blinded by model identity for subjective judgment; sample at least one multiple-info + poison/Drunk setup, a manual-override chain, and a second materially different setup. Fail-closed invalid responses and no-network manual hosting are separate functional acceptance gates. Do not call this a full multiplayer automated-game or statistically validated quality benchmark.

**Status:** code implementation and Gateway offline tests delivered; exact Android independent CI and actual private HTTPS LLM smoke must be verified separately.
