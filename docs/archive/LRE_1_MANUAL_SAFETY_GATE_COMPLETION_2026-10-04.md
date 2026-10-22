# LRE-1 Manual Safety Gate / Legacy Authority Revocation Completion — 2026-10-04

> Repository: `Jazz0006/CampBoardGameHost`
>
> Status: **COMPLETE / ACCEPTED**
>
> Accepted executable checkpoint: `045b3a6884f765149d4f1802d5e369671d38d938`
>
> Acceptance: CI #3752 GREEN / R2 #3448 GREEN

## 1. Accepted boundary

LRE-1 establishes a fail-closed Storyteller decision authority boundary:

```text
one legal outcome
→ RULE_DETERMINISTIC
→ automatic resolution permitted

multiple legal outcomes
+ accepted versioned policy for the exact scope
→ POLICY_READY(policyVersion)
→ recommendation / automatic selection permitted

multiple legal outcomes
+ no accepted versioned policy
→ MANUAL_REQUIRED
→ expose complete legal domain
→ never fall back to a legacy heuristic selector
```

## 2. Production authority removed in LRE-1

The accepted slice removes unsupported legacy automatic authority from:

- Mayor redirect;
- Demon successor;
- Spy / Recluse special registration;
- Demon bluff triplet selection;
- Red Herring setup-plan auto-selection;
- generic unreliable / poisoned / Drunk information fallback;
- Artist non-deterministic Yes/No rulings;
- legacy setup recommendation runtime prewarm/reveal consumption.

The old 10/90, 4/3/2/1, 90/10 and generic malfunction/dynamic selectors may still exist as code for later retirement work, but they no longer own unsupported production automatic decisions.

## 3. Manual safety coverage

Beginner mode is now allowed to pause for Storyteller input when no accepted policy owns a multi-choice decision.

LRE-1 added or confirmed:

- legal-only Demon bluff selection, requiring exactly three legal out-of-play good roles;
- manual Mayor / Demon successor selection when more than one legal target exists;
- manual Spy / Recluse registration without legacy recommendation buttons;
- complete manual information domains for numeric, Yes/No, role-reveal, pair-information and Sage surfaces;
- neutralized Manual presentation so legacy score/style/warning metadata does not masquerade as recommendation authority;
- canonical Red Herring manual selection at its latest-safe barrier.

## 4. Accepted policies preserved

LRE-1 does not revoke accepted evidence-backed authority:

- `DRUNK_ASSIGNMENT_Q04_V1`;
- `FunctioningLibrarianV2ProductionSelector` on its admitted scope.

If an accepted policy is unavailable or outside its admitted scope, the production path fails closed to Manual rather than falling back to the old ranking engine.

## 5. Legacy code that intentionally remains

Physical deletion is outside LRE-1.

Definitions, tests, benchmarks, shadow/evaluation helpers and retired recommendation infrastructure may remain until LRE-2 / LRE-3 / later cleanup. Their continued existence is not production authority.

In particular, old setup-ranking and dynamic-ranking classes can remain only as non-authoritative code until the staged deletion route removes them safely.

## 6. Historical pause / current GSP continuation

At the time of LRE-1 acceptance on 2026-10-04, recommendation-engine development was paused after LRE-1 and the planned resume path was an LRE-P family-by-family loop.

That planned continuation is now **superseded** by `docs/GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md`.

The LRE-1 safety result remains authoritative: unsupported multi-choice decisions must remain playable through the complete Manual domain and must never regain legacy heuristic authority. Current continuation is GSP-1, which extends that safety principle to the remaining special-case automatic policy islands before any general provider/model integration.
