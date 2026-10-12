# PROD-GLOBAL-3C/3D — One precommit expert opening call, production cutover

> 2026-10-12 / ACTIVE. Branch `feature/prod-global-3c3d-single-precommit-opening-20261012` stacked after Draft #303, #302, #301, #300, #299. MUST recheck GitHub live state. No production cutover accepted without all gates.

## Confirmed previous gate
#303 exact HEAD `58303c130bf26f84355a0c684363b19dec8ea0f9`, CI #4120/R2 #3742 GREEN; earlier #302 CI #4118/R2 #3740 GREEN. Those are Host-interface/experiment tests, not end-to-end model or phone acceptance.

## 3C/3D-1: REAL Responses V2 transport + whole Host validator, not yet UI
- `InitialJointOpeningDirectV2` consumes the **actual** uncommitted setup snapshot and Host-created `HostOpeningRulesDomainV2` rather than a model-fabricated candidate domain.
- Provider input includes full shown roster and actual/uncommitted status, TB role catalogue and abilities, player experience UNKNOWN unless recorded, game + revisions + rosterFingerprint, only logical decision IDs and unresolved event labels. No exhaustive candidate/legal scope, no gold-answer example, no registration witness manufacturing.
- One output contains Drunk selection (or null), red herring if truly functioning FT present, offboard Demon bluffs, all information statuses/typed observations, deferred actions, and a compact private strategy memory. `PersonalDirectOpenAiV1` strictly sends V2 schema on the Responses API with store:false and existing private-key handling. `InitialJointOpeningDirectV2.decode` selects the Host-owned Drunk branch and delegates **whole bundle** acceptance to `HostOpeningSemanticGateV2`.
- Invalid/incomplete/stale/unknown Drunk result is rejected, never rewritten or substituted. This new transport does NOT commit an assignment, publish an observation, or trigger a paid API call simply by existing in the code.
- Contract tests check real roster/Drunk-uncommitted input, no enumerated candidates, proper Responses routing, no token leakage, bad outputs. Exact CI/R2 required.

## 3C/3D-2: App producer/consumer cutover implementation — experimental (CI pending)

- Personal-direct TB AI_ASSISTED and AI_AUTOMATIC start with exactly **one** `InitialJointOpeningDirectV2.recommend` request after shown roster lock and before Drunk actual commit, while two separate `ClocktowerJointPass/Reveal` screens show role cards from `intermediateSetup.shownSeatAssignments`. These preview cards set actual `clocktowerRole = null`: no invented actual Drunk, no session or archive from preview. Other connection modes and manual remain on their existing non-direct route until all control surfaces are audited.
- Private Host `ClocktowerJointReview` holds the full Host-accepted proposal: same pending game ID, fingerprint and typed branch. One-tap adopt uses the existing rules-owned `TroubleBrewingSetupCommitter`, **after** all roles were revealed, then publishes no information. Setup Red Herring and three off-board Demon bluffs are projected from validated Host selections. Missing/stale/illegal response fails closed to manual with no isolated Drunk AI retry. Manual takeover on a pre-dealt setup does not reveal the player cards again.
- The joint opening suppresses the legacy `FirstNightOneShotRequestV1` preflight and mandatory postcommit analysis for the selected personal-direct path. A new strict, separate `ClocktowerJointOpeningDisplayMatcherV2` compares real typed public information to *current* Host legal manual candidates by result, role, and target seats, disallowing forced registration witnesses and duplicated ambiguous display candidates. If no unique safe typed UI match, it falls back to genuine current Host manual resolution; it does **not** run another initial LLM or select a first candidate. Host confirmation/publication remains separately authoritative.
- Per-player levels are UNKNOWN unless supplied to `HostOpeningRulesDomainV2`. The production setup call currently does not connect UI-recorded per-player experience and cross-game optional summaries; complete this before strategic-quality acceptance.
- **Unfinished and not a pass:** Host-confirmed event journal -> 3B3D-2B residual gate still not connected; conditional planned future Poisoner/FT clues are advisory only and may not be directly published. Auto-mode automatic adoption/real-world quality, external gateway mode, recovery after precommit interruption, app navigation, actual OpenAI API schema acceptance/latency, and phone are not independently validated. Initial joint option may be mismatched if current Host UI semantics are missing: manual is intentional fail-closed. Do not merge/call production complete until CI and device pass.

## 3C/3D-3: remaining mandatory acceptance and cleanup

The original implementation TODO list below remains current for unfinished aspects (not a new competing route).

## Previous 3C/3D-2 plan

## 3C/3D-2: still REQUIRED before calling this a fixed user bug
1. Replace the production `startTroubleBrewingGame -> ClocktowerDrunkSelection -> ProductionDrunkAiGatewayV1.recommend` request path with single V2 submission at SHOWN_ROSTER_LOCKED. Foreground identity reveal must be **shown-only**, and must not use or persist a speculative actual Drunk during this time; no Host grimoire/session publication before actual commit.
2. Preserve the received plan/strategy at the same game identity+roster fingerprint; after identity reveal, Host privately confirms the plan and binds Drunk via existing committer. If the user overrides Drunk, invalidate all dependent still-open advice, no second *initial* request; use a later explicit global-residual correction if needed.
3. Adopt fully validated red herring + bluffs and **semantically** map each recommended first-night information to current Host manual options at disclosure time, with full registration ambiguity and confirm/revision CAS; no silent fallback to the first candidate. The postcommit `FirstNightOneShotRequestV1` may not fire on the V2 path.
4. Other modes (AI_AUTOMATIC, external Gateway, manual) must have clear non-regressive handling; do not accidentally retain a second mandatory initial LLM call on the V2 route. Remove fully superseded transport/algorithm only when replacement's producer/consumer/test fanout is accepted.
5. Wire 3B3D-2B to **actual atomic session+journal and observation log**, so conditional pending event strategies cannot publish without fresh Host validation. New full residual LLM request only if a real event changes pending plan; never re-plan committed observations.
6. Test Drunk/no Drunk, Spy/Recluse multi-witness, Poisoner & FT confirmation, 5–6 & 8–10 player first night, stale callback, manual interruption, cancellation, no duplicate paid call, original identity reveal, one-tap adoption, recovery and Android phone. CI/R2 and independently observed genuine Luna quality must precede production acceptance.

## Ownership guard
This project aims for expert human Storyteller quality: AI does its own global joint analysis. Host independently checks rules and timing. No case-specific policies/rankers, no single-role separate calls, no new compatibility storage burden for the single-user development app.
