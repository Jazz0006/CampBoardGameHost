# Clue Recommendation & Manual Selection UX Decision

> Date: 2026-09-01 Australia/Sydney  
> Status: **APPROVED / ACTIVE PRODUCT AND ARCHITECTURE AUTHORITY**  
> Applies to: Clocktower information/clue presentation, manual selection, recommendation-provider integration  
> Current execution authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`

## 1. Product decision

The current global front-door choice:

```text
Automatic — Balanced
Automatic — Aggressive
Automatic — Conservative
Manual
```

is not the target product model.

The permanent interaction model is per clue interaction:

```text
complete legal semantic candidate domain
        |
        +--> shared information-decision authority
                |
                +--> Recommendation Provider
                |       -> primary recommendation
                |       -> 0–2 useful alternatives
                |
                +--> Manual selection

UI
        |
        +--> prominent primary recommendation when available
        +--> 0–2 visually separated alternatives
        +--> persistent direct manual control
```

The Storyteller should not need to select a persistent global recommendation style before receiving useful guidance, and should never lose manual control because a recommendation provider is absent or uncertain.

## 2. Authority separation

The permanent architecture is:

```text
rules / semantic legality
        ↓
CompleteLegalInformationDomain
        ↓
shared information-decision authority
        ├── manual selection
        └── Recommendation Provider
```

The Recommendation Provider ranks legal candidates. It does **not** define what clues the Storyteller is allowed to choose.

Manual candidate availability must therefore be independent of:

- recommendation shortlist coverage;
- Balanced/Aggressive/Conservative compatibility style;
- recommendation confidence;
- Productive Uncertainty rollout state;
- whether the current provider supports the interaction at all.

Unsupported recommendation situations degrade to correct manual play, not loss of functionality.

## 3. Shared confirmation lifecycle

Manual and recommendation acceptance are different provenance paths into the same decision authority, not different legality systems.

Target lifecycle:

```text
legal semantic candidates
        ↓
InformationDecisionContext-style authority
        ├── recommendedCandidateIds
        └── complete legalCandidateIds
        ↓
structured presentation model
        ↓
select candidateId
        ↓
confirm(
  MANUAL
  or RECOMMENDATION_ACCEPTED
)
        ↓
ConfirmedInformationDecision
        ↓
AbilityObservation / durable visible history
```

The shared authority must preserve:

- exact stable candidate identity;
- interaction-scoped Spy/Recluse registration facts;
- game/player-input revision freshness;
- stale-context rejection;
- illegal candidate rejection;
- recommendation-subset validation;
- manual-vs-recommendation provenance.

Do not build a second pair-only confirmation lifecycle when the existing information-decision Foundation can be reused or narrowly generalized.

## 4. Default clue-selection UX

### 4.1 Primary recommendation

The strongest supported contextual recommendation is the dominant visual action.

Conceptually:

```text
Recommended clue

+----------------------------------+
| strongest supported clue         |
| short reason / warning if useful |
|                                  |
|        [ Show to player ]        |
+----------------------------------+
```

This means “best recommendation for the current interaction”, not permanently “Balanced”.

### 4.2 Alternatives

Normal UI may show **0–2** alternatives.

Alternatives should preferably represent meaningfully different useful choices or world explanations rather than adjacent score values.

Useful diversity may include:

- different role/pair structures;
- different mistaken-world families;
- different registration explanations;
- different configuration implications;
- different pressure vs breakability trade-offs.

Do not invent a temporary legacy Top-3/diversification algorithm merely to fill two alternative slots. Fewer alternatives is correct when the provider cannot justify more.

### 4.3 Manual selection

Manual control is an affordance, not a global mode.

For large/combinatorial domains:

```text
----------------------------------
[ Manually choose clue ]
----------------------------------
```

opens a dedicated structured selection surface based on the complete legal semantic domain.

Manual selection remains available even when recommendation confidence is high.

## 5. Pair-information roles

Washerwoman, Librarian and Investigator establish the first complete combinatorial clue family.

### 5.1 Rules authority

Current semantic ownership:

```text
PairInformationDisplaySemantics
        ↓
PairInformationLegalDomain
        ├── RELIABLE -> truthful legal outcomes only
        └── DRUNK/POISONED -> complete legal display space
```

Presentation code must not duplicate these rules.

### 5.2 Washerwoman

Functioning Washerwoman:

- normally exposes only truthful Townsfolk clues;
- a legal Spy-as-Townsfolk registration may make a clue truthful even when the displayed Townsfolk is not actually in play;
- every role choice shown by the manual UI must have at least one legal pair candidate.

Drunk/Poisoned Washerwoman:

- current-script Townsfolk roles × every legal unordered player pair excluding the source;
- false-but-well-formed clues are allowed;
- no zero-character result.

### 5.3 Librarian

Functioning Librarian:

- with truthful Outsider candidates, expose only truthful clues;
- with zero actual Outsiders and no Spy registration truth, expose exactly `No Outsiders`;
- with zero actual Outsiders plus a legal Spy-as-Outsider registration, `No Outsiders` and registered truthful pair clues may coexist.

Drunk/Poisoned Librarian:

- current-script Outsider roles × every legal unordered player pair;
- plus `No Outsiders`.

The zero-result is semantic data supplied by the legal domain. UI must not contain `if (role == Librarian)` legality logic.

### 5.4 Investigator

Functioning Investigator:

- actual Minion truth;
- plus legal Recluse-as-Minion registration truth.

Drunk/Poisoned Investigator:

- current-script Minion roles × every legal unordered player pair.

Investigator never has a `No Minions` outcome.

### 5.5 Structured manual picker

The pair manual page should construct a legal clue rather than flattening the full Cartesian domain into hundreds of buttons.

Conceptually:

```text
Manually choose clue

1. Choose role
   [ Chef ]
   [ Empath ]
   [ Monk ]
   ...only roles with legal candidates...

2. Choose player pair
   [ #2 + #3 ]
   [ #2 + #4 ]
   [ #3 + #5 ]

[ Confirm ]
```

For Librarian, `No Outsiders` appears only when present as a legal zero-result candidate.

A structured choice resolves to an existing exact `candidateId`; the UI never manufactures a free-form clue outside the legal domain.

## 6. Small-domain specialization

### 6.1 Numeric domains

For Chef, Empath and other suitably small numeric domains:

```text
Recommended
[ 1 ]

Other legal values
[ 0 ] [ 2 ]
```

When all legal values already fit naturally on screen, those values provide complete manual control. A separate manual page is unnecessary.

The existing `StructuredNumberInformationUiModel` pattern is the preferred lifecycle model: all choices come from the validated decision context, recommendation acceptance and manual choice both confirm by candidate ID.

### 6.2 Yes/No domains

For a true two-value domain, show the recommended value prominently and the other legal value as a secondary action.

No dedicated manual screen is needed when the complete legal domain is already visible.

## 7. Critical migration sequencing

The original linear UX-R2 → UX-R3 → UX-R4 → UX-R5 sequence is refined to avoid transitional architecture.

### UX-R2A — pair semantic scenario contracts

Lock durable behavior for:

- reliable vs impaired Washerwoman;
- reliable zero-Outsider Librarian;
- Spy registration exceptions;
- reliable vs impaired Investigator;
- Recluse registration;
- zero-result role rules.

### UX-R2B — pair adoption of shared decision Foundation

Pair manual selection must confirm through the same information-decision lifecycle as other structured information choices.

If generic constraints must be widened, do so narrowly and preserve existing Number behavior.

### UX-R2C — pair production vertical slice

On a separate production-wiring PR:

```text
GameState + role definitions + source seat + reliability
-> PairInformationLegalDomain
-> shared decision context
-> PairInformationManualSelectionModel
-> structured UI
-> exact confirmation
-> durable observation
```

Do not compute pair legality in Compose or `ClocktowerInformationStepBuilder`.

### UX-R2D — manual-authority coverage audit

Before deleting the old global Manual entry point, confirm every currently supported major clue family has a correct manual path independent of recommendation coverage.

Audit at least:

- Number;
- Yes/No;
- pair role/player domains;
- supported role/category/reveal information families.

Fill only real authority/functionality gaps; do not redesign recommendation quality in this stage.

### UX-R3/R4 — remove global mode and establish final recommendation shell

Only after UX-R2D:

- remove normal user-facing Balanced/Aggressive/Conservative/Manual selector;
- recommendation becomes always-on where provider support exists;
- render primary + 0–2 alternatives + manual affordance;
- preserve low-confidence/no-recommendation states.

Balanced/Aggressive/Conservative may remain internal compatibility/scoring/diagnostic dimensions temporarily. Manual is not a style.

### UX-R5 — thin presentation polish

Specialize small-domain rendering and pair structured navigation without changing legal/recommendation authority.

## 8. Relationship to Productive Uncertainty

The stable UI/decision contract is intentionally provider-neutral.

Legacy ranking is temporary. Once the boundary above is stable, the primary algorithm campaign becomes:

```text
legal candidate
-> recipient PlayerWorldSet BEFORE
-> hypothetical visible observation
-> PlayerWorldSet AFTER
-> credibility
-> persistence
-> breakability
-> cross-role interaction
-> confirmation-lock risk
-> fairness / player agency
-> Productive Uncertainty ranking
```

The recommendation provider can then be replaced without another interaction-mode redesign.

This is why UX work must remain thin: establish the durable boundary, then return to cognitive-consistency quality rather than building another temporary recommendation engine.

## 9. Low-confidence behavior

Do not present a weak or unavailable evaluation as authoritative merely because a card layout expects a recommendation.

The provider must be able to express:

```text
No clearly superior recommendation

Possible choices
[ A ]
[ B ]

----------------------------------
[ Manually choose clue ]
----------------------------------
```

Absence of a recommendation must never remove manual play.

## 10. Testing guidance

Use risk-based tests-first according to root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

High-value contracts include:

- manual and recommendation consume one legal semantic authority;
- pair reliable/impaired domain behavior matches section 5;
- Spy/Recluse registration survives selection and confirmation exactly;
- structured manual choice cannot manufacture an illegal candidate;
- stale decision contexts cannot be confirmed;
- recommendation acceptance is limited to current recommended candidate IDs;
- recommendation absence still allows correct manual play;
- all supported clue families remain manually playable before global mode removal;
- small-domain UI exposes every legal value directly;
- normal combinatorial recommendation surface never requires more than primary + two alternatives.

Avoid tests that only assert class/helper/button source placement.

## 11. Current implementation state

As of the current Draft PR #63 foundation checkpoint:

- `PairInformationLegalDomain` is already the pair legality authority;
- `PairInformationManualSelection` already provides a typed structured projection and exact candidate preservation;
- `ClocktowerNightStepUi` / `ClocktowerInformationStepBuilder` can transport a precomputed typed model;
- pair production Host/UI wiring is intentionally not yet complete;
- the next foundation work is shared InformationDecision lifecycle adoption, not large Host editing.

Current live status and exact checkpoint SHAs belong in `docs/CURRENT_DEVELOPMENT_ROADMAP.md`, not in this long-lived product decision document.

## 12. Acceptance summary

The design is complete when a Storyteller can treat every supported information interaction as:

> **Use the strongest supported contextual recommendation, choose one of a small number of meaningful alternatives, or take full legal manual control — all through one semantic candidate/decision authority and without choosing a persistent global recommendation mode.**
