# RSR-1 Family Style-Neutral Policy Readiness Audit — 2026-10-04

> Repository: `Jazz0006/CampBoardGameHost`
>
> Baseline: `main@f5e61b999c70b8a9060d5e172968b6b764934d28`
>
> Status: **RSR-1 AUDIT COMPLETE / RSR-1A IMPLEMENTED — EXACT-HEAD T4 PENDING**
>
> Selected first bounded implementation: **RSR-1A — Storyteller-mode / legacy-ranking-style ownership decoupling**

## 1. Fresh family readiness result

RSR-0 established that global `RecommendationStyle.GENTLE / BALANCED / AGGRESSIVE` is obsolete product architecture, while future recommendation differentiation is per-player `BEGINNER / NORMAL / EXPERT` enrichment with current/default `NORMAL`.

RSR-1 now asks a different question:

> Which recommendation surfaces already possess a style-neutral versioned policy, and which still need new policy semantics before old style-dependent scoring can be removed?

The answer is uneven. Do not retire all style consumers together.

## 2. Already style-neutral policy authorities

### 2.1 Drunk assignment

The accepted production authority is versioned policy, not RecommendationStyle:

- `DRUNK_ASSIGNMENT_Q04_V1`;
- frozen fallback / replay contracts;
- canonical Drunk decision context;
- rules-owned legal domain.

Player experience may become optional enrichment in a future policy version, but the current Q04 policy does not need a global style.

No RSR production change is required for Drunk assignment now.

### 2.2 Functioning Librarian automatic surface

C5-E is already a style-neutral production authority on its admitted scope:

```text
TB / first night / round 1
+ functioning Librarian
+ reliable information
+ automatic Storyteller mode
-> FunctioningLibrarianV2ProductionSelector
-> BEGINNER_CONSERVATIVE_V2 preferred band
-> exact V1 seeded fallback
```

The selector accepts no `RecommendationStyle`.

It owns ranking only inside the already rules-legal truth-only Librarian domain.

The surrounding generic information pool, telemetry and lazy compatibility fallback still carry legacy style, but that is compatibility infrastructure rather than C5-E policy semantics.

Therefore C5-E is proof that a family can migrate from global style to an explicit versioned policy without mapping style to player experience. The historical policy name `BEGINNER_CONSERVATIVE_V1/V2` is a stable policy-version identity from the pre-player-level architecture; **BEGINNER in that identifier must not be interpreted as `PlayerExperienceLevel.BEGINNER`** or renamed casually because replay/export policy identity depends on version stability.

## 3. Families not ready for style collapse

### 3.1 Setup recommendation — BLOCKED ON SINGLE-POLICY SEMANTICS

The setup service evaluates the full candidate space under three different `RecommendationProfiles`.

Style changes real weights, deterministic selector seed and diversification.

The EvidenceLab corpus contains useful qualitative setup guidance and player-experience dimensions, but no accepted general replacement ranking policy that authorizes selecting one of the three old score tables or averaging them.

Verdict: **do not collapse yet**.

### 3.2 Malfunction / unreliable information — BLOCKED ON SINGLE-POLICY SEMANTICS

`MalfunctionPolicy`, `DynamicCandidateGenerator` and consequence logic use style for misinformation severity / continuity / pressure.

EvidenceLab now has useful qualitative misinformation findings, but most are NOT VERIFIED or bounded examples. They do not authorize one universal numeric replacement.

Verdict: **do not collapse yet**.

### 3.3 Mayor redirect — BLOCKED ON SINGLE-POLICY SEMANTICS

TBGS-2E cleaned the mechanical/legal context, but `MayorRedirectRecommender` still changes ranking materially by style.

The current evidence corpus does not supply an accepted general Mayor ranking policy.

Verdict: **mechanically ready, policy not ready**.

### 3.4 Demon succession — BLOCKED ON SINGLE-POLICY SEMANTICS

TBGS-2D cleaned the mechanical/legal context, but successor ranking remains style-dependent.

The current evidence corpus does not supply an accepted general successor ranking policy.

Verdict: **mechanically ready, policy not ready**.

### 3.5 Special registration — DOUBLE-BLOCKED

Registration still has:

1. style-dependent ranking / consequence scoring;
2. incomplete typed registration / misinformation history production.

EvidenceLab has useful Spy/Recluse qualitative guidance, including player-experience sensitivity, but no accepted universal replacement ranking policy.

Verdict: **do not collapse until both policy and history ownership are resolved**.

## 4. First safe implementation slice

Although the material recommendation families above cannot yet collapse, one ownership defect can be removed without changing recommendation output.

Current:

```text
StorytellerExperienceMode
  -> StorytellerRecommendationUxPolicy
      -> recommendationStyle = AGGRESSIVE
          -> CampBoardGameHostApp
              -> ClocktowerJudgeScreen.automaticStorytellerStyle
                  -> legacy style-dependent families
```

Both Storyteller modes currently inject the same `AGGRESSIVE` value.

This creates a false architecture statement: it implies that host/operator experience mode owns recommendation ranking style.

Target:

```text
StorytellerExperienceMode
  -> StorytellerRecommendationUxPolicy
      -> automaticExecution
      -> showManualAlternatives
      -> recommendedOptionLimit

legacy family compatibility boundary
  -> explicit compatibility-only RecommendationStyle.AGGRESSIVE
  -> old style-dependent families until individually migrated

future player profile
  -> per-player BEGINNER / NORMAL / EXPERT
  -> typed recommendation enrichment
  -> versioned policy that explicitly consumes it
```

This is **not** making AGGRESSIVE canonical.

It is explicitly quarantining the current old behavior behind a compatibility seam so the new product model no longer says Storyteller mode chooses ranking semantics.

## 5. RSR-1A bounded implementation contract

RSR-1A may:

1. remove `recommendationStyle` from `StorytellerRecommendationUxPolicy`;
2. update Storyteller-mode tests so they validate only UI/authority differences;
3. introduce one clearly named compatibility-only source for the current old automatic style value used by not-yet-migrated family adapters;
4. change `CampBoardGameHostApp` to source `automaticStorytellerStyle` from that compatibility seam rather than Storyteller mode;
5. document the seam as temporary and forbidden for new recommendation policy work.

Preferred compatibility shape is a tiny internal object/constant such as:

`LegacyRecommendationStyleCompatibility.automatic = RecommendationStyle.AGGRESSIVE`

The exact identifier may vary, but it must communicate that:

- this value preserves current old behavior only;
- it is not NORMAL-player semantics;
- it is not StorytellerExperienceMode semantics;
- no new family may depend on it.

## 6. Tests-first acceptance

RSR-1A tests must prove:

1. `StorytellerRecommendationUxPolicy` no longer requires or exposes RecommendationStyle;
2. BEGINNER still means:
   - automatic execution;
   - no manual alternatives;
   - one recommended option;
3. EXPERIENCED still means:
   - no automatic execution;
   - manual alternatives exposed;
   - three recommended options;
4. the legacy compatibility seam remains explicitly AGGRESSIVE so current outputs do not change in this slice;
5. App wiring uses the compatibility seam rather than a field on Storyteller UX policy;
6. no player-level enum/profile/UI/persistence is introduced;
7. no recommendation candidate/ranking/scoring code changes;
8. no rules/legal-domain, Recovery, replay, export or snapshot behavior changes.

Focused test target:

`StorytellerExperienceModeTest`

plus compile/typecheck and existing affected tests.

Final acceptance should use the normal code-change gate:

- Android FAST at implementation checkpoint;
- exact-head T4 before merge: Android FULL + assemble, ASP, Real Clingo, R2.

## 7. Explicit non-goals

RSR-1A does not:

- remove `RecommendationStyle`;
- alter setup recommendations;
- alter unreliable information;
- alter Mayor ranking;
- alter Demon successor ranking;
- alter registration ranking/history;
- change functioning Librarian V2;
- add `PlayerExperienceLevel` production types;
- add player profile/management UI;
- change persistence or Recovery;
- map NORMAL to AGGRESSIVE;
- claim AGGRESSIVE is the future default policy.

## 8. RSR-1A tests-first implementation evidence

RED checkpoint: `5645c6fbf2585ca23a6648e1c7e44e68cd03506d` on PR #217. CI #3738 failed Android FAST exactly on `StorytellerExperienceModeTest.storyteller UX policy does not expose legacy recommendation style`; 1 of 1,657 FAST tests failed. R2 #3437 and Real Clingo were GREEN, confirming the intended architecture assertion was the only observed blocker.

Implementation checkpoint: `e3e94136e3e6658f1d69ab5cd1e97114ada4238a`. CI #3739 / R2 #3438 are GREEN; Android FAST and Real Clingo passed. The implementation removes `recommendationStyle` from `StorytellerRecommendationUxPolicy`, routes unchanged legacy automatic behavior through `LegacyRecommendationStyleCompatibility.automatic = AGGRESSIVE`, and changes no recommendation ranking/scoring code.

A separate exact-head T4 checkpoint is still required before merge because ordinary PR synchronization selected Android FAST rather than FULL. The T4 checkpoint must explicitly run Android FULL + assemble, ASP and Real Clingo plus R2.

## 9. Sequence after RSR-1A

```text
RSR-0 architecture/fan-out/player-level contract   COMPLETE / ACCEPTED
RSR-1 readiness audit                              COMPLETE

-> RSR-1A Storyteller-mode/style ownership decoupling
                                                   IMPLEMENTED / T4 PENDING
-> RSR-1B family policy selection audit
   - leverage already style-neutral accepted surfaces first
   - require evidence/versioned policy before collapsing styleful families
-> RSR-2 family production style collapse
-> RSR-3 identity / telemetry / UI cleanup
-> RSR-4 final RecommendationStyle retirement

parallel later:
player profile / management
-> per-player BEGINNER / NORMAL / EXPERT
-> default NORMAL
```
