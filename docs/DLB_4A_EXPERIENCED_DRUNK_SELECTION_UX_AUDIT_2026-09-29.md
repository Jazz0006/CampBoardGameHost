# DLB-4A Experienced Drunk Selection UX Audit — 2026-09-29

> Status: **COMPLETE / ACCEPTED IMPLEMENTATION AUTHORITY**
> Baseline: `main@c80a1db119aa058bcab050a142a43a6a41d1e680`
> Accepted executable checkpoint: `c85448831e73c82868e118c7f47a0ed889267c0c`; CI #3586 and R2 #3318 GREEN.
> Scope: Experienced assisted Drunk selection before canonical commit. No Beginner policy cutover, no DLB-5 dependency barriers, no Recovery draft persistence, no broad App/Host decomposition.

## 1. Current ownership

- `TroubleBrewingDrunkCandidateDomain` owns the legal candidate set.
- `TroubleBrewingSetupCommitter` owns final candidate validation and canonical commit.
- `StorytellerExperienceMode` / `StorytellerRecommendationUxPolicy` own whether manual Storyteller authority is exposed.
- `DRUNK_ASSIGNMENT_SHADOW_V1` still always defers and has no production ordering/selection authority.
- App owns only transient pre-commit UI lifetime and navigation.

The UI must not reconstruct legality, rank candidates, or write any unconfirmed draft to Recovery.

## 2. Required DLB-4A route

```text
TroubleBrewingPreparedSetup
+ StorytellerExperienceMode
-> no Drunk:
     commit immediately with null candidate
-> Experienced + hasDrunk:
     legal candidates
     + optional authorized recommendation
     -> host-only Choose the Drunk screen
     -> explicit legal candidate confirmation
     -> shared canonical commit
-> Beginner + hasDrunk:
     preserve the existing DLB-4 compatibility-confirmed candidate only as a transitional baseline
     -> shared canonical commit
```

The Beginner path above is **not** the DLB production policy cutover. It preserves current playable behavior while the route's explicit cutover gate remains closed. It must not read `DRUNK_ASSIGNMENT_SHADOW_V1` as an authority or manufacture a recommendation.

DLB-4A replaces the compatibility fallback for the **Experienced** path. The remaining Beginner compatibility fallback is retired only when an evidence-authorized automatic Drunk-selection contract is adopted, or during DLB-6 if the product route changes first.

## 3. Typed UX-routing seam

Add one narrow pure seam beside the setup/DLB contracts:

```text
TroubleBrewingPreparedSetup
+ StorytellerExperienceMode
+ optional recommended TroubleBrewingDrunkCandidate
-> TroubleBrewingDrunkSelectionRoute

NoSelectionNeeded
ManualSelection(request)
CompatibilityImmediate(candidate)
```

The manual request contains the exact legal candidates. Any non-null recommendation must equal one current legal candidate; stale/illegal recommendations fail closed.

For the current implementation the recommendation input is null because no policy version is authorized to select or order Drunk candidates.

## 4. App lifecycle

Add a host-only pre-commit screen:

```text
ClocktowerSettings
-> prepare intermediate setup
-> Experienced + hasDrunk:
     pending in-memory selection request
     -> ClocktowerDrunkSelection
     -> choose legal candidate
     -> clear pending request
     -> shared commit/start continuation
     -> PassPhone
```

Back from `ClocktowerDrunkSelection` clears the pending request and returns to `ClocktowerSettings`.

The pending request is Compose/session-memory only. It is never included in `RecoverySnapshot`, rotation history, setup history, DecisionTrace, or durable settings.

## 5. Presentation contract

The selection screen shows every legal Townsfolk candidate with:

- seat;
- player name;
- shown Townsfolk identity;
- an optional Recommended marker only when an authorized recommendation exists.

When no recommendation exists, the screen explains that no automatic recommendation is currently authorized and the Storyteller may choose any legal candidate.

Do not expose actual-role truth beyond the Drunk choice being made, and do not begin player reveal/prewarm before confirmation.

## 6. Shared commit continuation

Both immediate and manual routes must enter one common production continuation:

```text
prepared setup + confirmed candidate
-> TroubleBrewingSetupCommitter
-> committed cards / canonical GameState
-> ClocktowerGameSession
-> committed rotation/setup publication
-> persistence
-> reveal
-> prewarm
```

Do not fork separate Beginner/Experienced commit logic.

## 7. Test strategy

Meaningful RED/GREEN belongs at the pure routing seam:

- Experienced + Drunk -> ManualSelection containing exactly the legal candidate domain.
- Experienced + no Drunk -> NoSelectionNeeded.
- Beginner + Drunk -> CompatibilityImmediate using the exact compatibility-confirmed legal candidate.
- Beginner + no Drunk -> NoSelectionNeeded.
- non-null recommendation must belong to the exact current legal domain.
- current null recommendation remains valid and produces no Recommended candidate.

UI rendering itself is presentation-only; compile/static evidence plus exact diff review is sufficient unless a reusable interaction seam exposes an uncovered stable behavior.

## 8. Acceptance

DLB-4A is complete when:

- Experienced Trouble Brewing with Drunk pauses before canonical commit and player reveal;
- every selectable option is rules-legal and every legal candidate is selectable;
- explicit confirmation feeds the existing canonical commit seam;
- no unconfirmed selection state is persisted;
- no SDE/shadow policy is promoted to production selection authority;
- Beginner behavior remains explicitly transitional and does not claim the later cutover gate;
- exact-head CI/R2 are green;
- route/roadmap/handoff docs move next to DLB-5.

## 9. Implementation outcome

DLB-4A is complete at executable checkpoint `c85448831e73c82868e118c7f47a0ed889267c0c`:

- `TroubleBrewingDrunkSelectionRouter` now owns the pure experience-mode routing seam without taking rules or policy authority;
- Experienced Drunk setups produce a `ManualSelection` request containing exactly the current legal candidate domain and do not depend on the compatibility-confirmed candidate;
- the new host-only selection screen shows seat, player and shown Townsfolk identity, supports optional future recommendation highlighting, and currently shows no recommendation because no production Drunk-ordering policy is authorized;
- confirmation enters the single existing DLB-4 canonical commit/start continuation; reveal, cards, session creation, Recovery persistence and prewarm remain downstream of that confirmation;
- backing out clears only the in-memory pending request and returns to Clocktower settings;
- Beginner keeps the pre-existing compatibility-confirmed candidate as a transitional baseline only. This does not satisfy or bypass the later automatic-authority cutover gate;
- `DRUNK_ASSIGNMENT_SHADOW_V1` and `BEGINNER_CONSERVATIVE_V1` remain unchanged;
- CI #3586 and R2 #3318 are GREEN for the executable UI checkpoint.

Next product slice: **DLB-5 — staged first-night dependency planner / latest-safe commitment barriers**.
