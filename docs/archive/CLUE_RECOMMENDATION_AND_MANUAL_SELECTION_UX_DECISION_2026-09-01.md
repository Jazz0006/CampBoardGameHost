# Clue Recommendation & Manual Selection UX Decision

> Date: 2026-09-01 Australia/Sydney  
> Status: **APPROVED PRODUCT / ARCHITECTURE DIRECTION — IMPLEMENT AFTER PR #61**  
> Applies to: Clocktower information/clue presentation and future Recommendation Provider integration  
> Related plans:
> - `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
> - `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
> - `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

## 1. Product decision

Remove the current user-facing global recommendation mode choice:

```text
Automatic — Balanced
Automatic — Aggressive
Automatic — Conservative
Manual
```

These modes should no longer be the primary user interaction model for clue selection.

The product should instead **always compute recommendations for the current interaction**, present the strongest recommendation most prominently, expose up to two useful alternative recommendations when appropriate, and always preserve direct Storyteller manual control.

The new interaction model is:

```text
complete legal semantic candidate domain
        |
        +--> Recommendation Provider
        |       |
        |       +--> primary recommendation (Top-1)
        |       +--> up to 2 differentiated alternatives
        |
        +--> Manual selection path

UI
        |
        +--> prominent primary recommendation
        +--> visually separated alternatives
        +--> persistent manual-selection affordance
```

## 2. Why the global mode setting should be removed

`BALANCED`, `AGGRESSIVE`, and `CONSERVATIVE` describe recommendation policy/style. `MANUAL` describes an interaction method. They are not the same product dimension and should not be combined into one user-facing mode selector.

A Storyteller should not need to decide before or during a game that all future information must use one global style. The correct strategic pressure depends on the current game state, information history, recipient knowledge, impairment state, interaction risk, and future breakability.

Long term, the recommendation system itself should decide when a more conservative or more disruptive clue is appropriate. Productive Uncertainty is intended to make that contextual decision from the current epistemic state rather than asking the user to select a global style.

Therefore:

- remove the global mode choice from normal product UI;
- do not require the user to understand Balanced/Aggressive/Conservative before receiving recommendations;
- preserve useful style/pressure dimensions internally as scoring features or diagnostics where they remain valuable;
- do not make those old styles the permanent Recommendation Provider API.

## 3. Default clue-selection UX contract

For every information interaction, the UI should follow this priority:

### 3.1 Primary recommendation

The best currently supported recommendation is the dominant visual action.

It should be easy for a new Storyteller to understand and accept without scanning the full legal candidate domain.

Conceptually:

```text
Recommended clue

+----------------------------------+
| strongest recommended clue       |
| short reason / warning if useful |
|                                  |
|        [ Show to player ]        |
+----------------------------------+
```

The primary recommendation means **best recommendation for the current situation**, not permanently “Balanced”.

### 3.2 Alternative recommendations

When useful, show at most two additional recommendations below or separately from the primary recommendation.

The alternatives should preferably represent meaningfully different strategic explanations or clue structures rather than simply the next two nearly-identical score values.

Examples of useful diversity include:

- different mistaken-world families;
- different player-pair structures;
- different role/configuration implications;
- different interaction/confirmation patterns;
- different pressure vs breakability trade-offs.

The product contract is **0–2 alternatives**, not a requirement to always fill two slots.

If no high-quality alternatives exist, show fewer.

### 3.3 Manual selection

Manual control is not a separate global mode. It is an affordance available at every relevant interaction.

For large candidate domains, place a clear manual-selection action in a stable location near the bottom of the interaction screen, visually separated from recommendations:

```text
----------------------------------
[ Manually choose clue ]
----------------------------------
```

Selecting it opens a dedicated clue-construction/selection surface based on the **complete legal semantic candidate domain**, not on the legacy recommendation shortlist.

Manual selection must remain available even when the recommendation engine is active or highly confident.

## 4. UI behavior by clue family

### 4.1 Small numeric domains

For numeric roles such as Chef, Empath, Clockmaker, and suitable Chambermaid results, avoid unnecessary navigation.

Show:

```text
Recommended
[ 1 ]

Other legal values
[ 0 ] [ 2 ]
```

If the complete legal numeric domain is already small enough to display directly, these buttons themselves provide full manual control and a separate manual page is unnecessary.

The recommendation remains visually strongest; the remaining legal values are secondary actions.

### 4.2 Yes / No domains

For a two-value domain, show the recommended result prominently and the other legal result as the secondary choice.

No separate manual page is required when the full legal domain is already visible.

### 4.3 Role + player / pair-combination clues

For Washerwoman, Librarian, Investigator and similar combinatorial clues:

```text
Primary recommendation
[ role / player pair ]

Other recommendations
[ alternative 1 ]
[ alternative 2 ]

----------------------------------
[ Manually choose clue ]
----------------------------------
```

The manual page should allow the Storyteller to construct any legal clue through structured role/player selection rather than forcing them to scroll through a combinatorial list of pre-expanded buttons.

### 4.4 Registration-sensitive interactions

Spy/Recluse registration belongs to semantic truth construction for the specific interaction.

The manual selection UI must expose or resolve the relevant registration choice where required, but must not turn registration into a hidden recommendation-only heuristic.

The same committed clue must carry the registration semantics used to evaluate its truth/falsehood.

## 5. Candidate-domain authority

The permanent architecture must be:

```text
CompleteLegalInformationDomain
        |
        +--> Manual clue selection
        |
        +--> Recommendation Provider
                 |
                 +--> legacy compatibility provider (temporary)
                 +--> cognitive-consistency / Productive Uncertainty provider
```

The Recommendation Provider ranks legal candidates. It does **not** define what the Storyteller is allowed to choose.

Therefore the complete legal/manual domain must remain available independently of:

- legacy shortlist curation;
- recommendation style;
- Recommendation Provider coverage;
- recommendation confidence;
- Productive Uncertainty rollout state.

Unsupported recommendation cases must degrade to legal manual selection rather than losing functionality.

## 6. Recommendation output contract

The future provider should conceptually return a ranked recommendation result rather than a global style-specific selection:

```text
RecommendationResult
- primary: Candidate?
- alternatives: List<Candidate>   // max 2 for normal UI
- confidence / quality tier
- explanation / reason codes
- warning codes
```

The exact Kotlin API is not frozen by this document.

Important behavioral requirements:

1. primary must come from the current legal semantic domain;
2. alternatives must also be legal;
3. normal UI renders at most two alternatives;
4. alternatives should be diversified when possible;
5. low-confidence recommendation must be representable;
6. absence of a recommendation must not remove manual operation.

## 7. Low-confidence behavior

Do not invent a strong-looking recommendation merely because the UI expects one.

The Recommendation Provider must be able to report that no candidate is meaningfully superior or that evaluation is degraded/unknown.

Suggested presentation:

```text
No clearly superior recommendation

Possible choices
[ A ]
[ B ]
[ C ]

----------------------------------
[ Manually choose clue ]
----------------------------------
```

This is preferable to presenting a weak or unavailable evaluation as authoritative.

## 8. Relationship to Productive Uncertainty

The future cognitive-consistency recommender should determine contextual strategy from the actual interaction rather than asking the Storyteller to choose a persistent Balanced/Aggressive/Conservative mode.

Conceptually:

```text
legal candidate
-> recipient PlayerWorldSet BEFORE
-> hypothetical visible observation
-> PlayerWorldSet AFTER
-> credibility
-> ambiguity / Productive Uncertainty
-> persistence
-> breakability
-> cross-role interaction
-> confirmation-lock risk
-> faction/fairness gates
-> ranked recommendation
```

Former “aggressive”, “balanced”, or “conservative” notions may survive as internal feature dimensions, diagnostics, test scenarios, or optional advanced policy parameters, but they should not remain the normal front-door UX.

## 9. Implementation route after PR #61

Do **not** implement this UX inside PR #61. PR #61 should remain a generic setup-architecture closeout PR.

After PR #61 is merged, use a fresh branch and proceed in this order:

```text
UX-R1  Audit current recommendation/manual UI and mode dependencies
       -> identify every user-facing Automatic/Manual/RecommendationStyle dependency

UX-R2  Establish legal-domain -> manual-selection UI contract
       -> manual authority independent from recommendation shortlist
       -> no recommendation-quality redesign yet

UX-R3  Replace global mode UX
       -> remove normal user-facing Balanced/Aggressive/Conservative/Manual selector
       -> recommendations are always computed when supported

UX-R4  Unified recommendation presentation
       -> prominent Top-1
       -> up to 2 differentiated alternatives
       -> persistent manual action for combinatorial domains

UX-R5  Small-domain specialization
       -> Number: primary + remaining legal numeric buttons
       -> Yes/No: primary + other legal result
       -> avoid unnecessary navigation

EPI-MQ / ALG
       -> PlayerKnowledgeSnapshot / PlayerWorldSet
       -> hypothetical observation BEFORE/AFTER
       -> epistemic metrics
       -> Productive Uncertainty
       -> cognitive-consistency Recommendation Provider

UX-R6  Replace legacy ranking behind the same UI contract
       -> no new interaction-mode redesign required during recommender rollout
```

UX-R1 through UX-R5 should be kept deliberately thin: their purpose is to establish the permanent product/authority boundary, not to build a second recommendation algorithm before Productive Uncertainty.

Where practical, UX-R1/R2 may be combined with the first post-PR epistemic branch if doing so avoids temporary architecture, but UI behavior and world-model correctness must remain separately testable.

## 10. Testing guidance

Use risk-based tests-first.

High-value behavior contracts include:

- manual selection remains available when recommendations are enabled;
- recommendation candidates and manual candidates share one legal semantic authority;
- selecting an alternative commits the exact selected clue/registration semantics;
- a small numeric domain exposes every legal value directly;
- combinatorial domains expose no more than three normal recommendations before manual navigation;
- removing the global mode does not silently change clue legality/truth;
- recommendation absence/low-confidence still permits correct manual play.

Avoid source-shape tests that only assert button/class/helper placement.

## 11. Non-goals of the first UX slice

Do not initially:

- tune Productive Uncertainty weights;
- add new legacy role/script-specific ranking heuristics;
- require exact parity with old Balanced/Aggressive/Conservative results;
- freeze a permanent Top-3 scoring formula;
- expand every legal combinatorial candidate as a button on the night-step card;
- make manual selection dependent on the recommendation engine.

## 12. Acceptance summary

The new clue-selection design is accepted when a Storyteller can treat every information interaction as:

> **Use the strongest contextual recommendation, choose one of a small number of meaningful alternatives, or take full manual control — without selecting a global recommendation mode first.**

This UI contract should remain stable while the recommendation implementation evolves from the legacy compatibility provider to the cognitive-consistency / Productive Uncertainty provider.
