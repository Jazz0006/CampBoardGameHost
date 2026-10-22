# PROD-GLOBAL-3P — Prompt lessons and external project engineering review (2026-10-12)

Status: **ACCEPTED DESIGN LESSONS / NOT PRODUCTION ACCEPTANCE**. This note documents source-derived findings and separately records our own decisions. The authoritative runtime route remains [PROD-GLOBAL-3](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md).

## 1. Evidence: our Luna prompt experiments (two small independent smoke sets)

- Original 8-seat benchmark A: 58.54s, 6,932 total tokens. Joint recommendation includes Chef Drunk 0, Investigator 6/7 Baron, red herring Ravenkeeper 3.
- Initial compact structured B: 28.15s, 4,014 tokens. Chef Drunk 0, Investigator 7/8 Baron (both actual beginner evil players); strategyMemory warns about the pressure without revising the choice.
- Revised B2 same 8-seat setup: 34.34s, 4,286 tokens. Chef Drunk 0, Investigator 4/7 Baron and Recluse 6 red herring. Reduced direct dual-evil pressure; still needs rule-specific opportunity-cost reasoning.
- Revised B2 no-Drunk setup: 28.44s, 3,707 tokens. Correctly omitted Drunk selection, but did **not** explicitly list unconfirmed Poisoner target in deferred IDs (even though narrative knew it was unknown).
- Every arm used **one** paid gpt-5.6-luna call per fixture. All results are **non-statistical** and synthetic/not checked against real Host-emitted candidate IDs; they are not valid Android E2E acceptance.
- The production prompt should preserve the original benchmark's global board comparison, Good solvability, Evil playability, alternate worlds and self-critique while returning one concise typed plan + private bounded strategyMemory. Do not hardcode Investigator 6/7, Chef-as-Drunk, red-herring location or fixed bluff triples as evaluation oracles.
- Additional general criteria: consider legal mechanism overlap/opportunity costs (e.g., Recluse registration and red herring could overlap or complement); compare player-visible knowledge with Host true world; and explicitly encode every pending causal dependency such as Poisoner target, Fortune Teller query and Ravenkeeper death before query.
- No-Drunk is a first-class plan: NOT_APPLICABLE is different from UNCOMMITTED; no imaginary Drunk or Fortune Teller red herring when the actual ability is absent.

## 2. Source-derived observations: Olalall public repository

Inspected public source on 2026-10-12:
- [server/ai/setupAdvicePromptBuilder.ts](https://github.com/Olalall/AI-The-Storyteller-of-the-Blood-Stained-Clock-Tower/blob/main/server/ai/setupAdvicePromptBuilder.ts) defines task-specific `system` plus JSON `user` messages, compact candidate/role pool/experience and `roleKnowledge` / `roleResearch`, balancing good evidence vs evil survival, complexity and player level.
- [server/ai/nightSettlementPromptBuilder.ts](https://github.com/Olalall/AI-The-Storyteller-of-the-Blood-Stained-Clock-Tower/blob/main/server/ai/nightSettlementPromptBuilder.ts) provides a wake-item-focused settlement prompt with explicit `availableOutcomes`, `selectedTargets`, confirmed previous registration/targets, missing-input behavior, and no mutation authority.
- [server/ai/nightSettlementProvider.ts](https://github.com/Olalall/AI-The-Storyteller-of-the-Blood-Stained-Clock-Tower/blob/main/server/ai/nightSettlementProvider.ts) also enforces available `ready` outcome IDs and known missing inputs *in code* even when the LLM fails to mention them.
- [src/services/ai/aiContextLevel.ts](https://github.com/Olalall/AI-The-Storyteller-of-the-Blood-Stained-Clock-Tower/blob/main/src/services/ai/aiContextLevel.ts) and [server/ai/nightUnknownSeats.ts](https://github.com/Olalall/AI-The-Storyteller-of-the-Blood-Stained-Clock-Tower/blob/main/server/ai/nightUnknownSeats.ts) distinguish incomplete grimoire coverage (`minimal`, explicit `unknownSeatIds`) from known, complete identity coverage.
- [server/ai/reviewPromptBuilder.ts](https://github.com/Olalall/AI-The-Storyteller-of-the-Blood-Stained-Clock-Tower/blob/main/server/ai/reviewPromptBuilder.ts) is separate end-game review drafting.
- [server/ai/aiProviderClient.ts](https://github.com/Olalall/AI-The-Storyteller-of-the-Blood-Stained-Clock-Tower/blob/main/server/ai/aiProviderClient.ts) uses OpenAI-compatible Chat Completions with `temperature: 0.2`, `max_tokens: 3000`, `thinking: {type:'disabled'}`, JSON-object output. Do not assume any one provider honors all these knobs.

Their current public prompt paths cover candidate-ranking setup, **single wake-item** night settlement, and game review; inspected source did not reveal an equivalent one-shot Drunk + multi-role opening optimizer and explicit global strategic continuation.

## 3. Our decisions (NOT claims about the external project)

1. Keep PROD-GLOBAL-3: one joint precommit request across all applicable conditions, followed by event-driven **global residual** revision. **Do not adopt the external project's per-wake-item AI recommendation architecture.**
2. Host emits authoritative rules, legal IDs, no-Drunk vs unresolved facts, explicit knowledge coverage and required future event dependencies. Rules knowledge/role research stays in Host-facing structured context, not indefinitely expanding freeform instructions. LLM outputs only proposals; Host validates independently.
3. The model sees both canonical Host-known facts and carefully differentiated player-visible facts, unknown observations and hypothetical worlds; absence from data never implies a healthy/default player.
4. Keep human experience values only when recorded; unspecified experience is UNKNOWN, not assumed REGULAR/NORMAL. Keep truthful determined scalar output as **context**, not a selectable candidate.
5. Registration semantics remain result-first: a Chef/Empath number can have more than one legal Spy/Recluse witness. Do not fabricate a particular witness.
6. Compare complete bundles before selecting one. Preserve mechanism opportunity cost, Good-side solvability and Evil-side playability checks, but keep production output compact.
7. If conditional domains are missing, reject/refrain from publishing; **never** prompt the LLM to invent Host-legal candidate IDs.

## 4. Implementation checkpoints

- **3A**: User's subsequent Android smoke showed first-night recommended display successfully; earlier crash root cause remains unproved. Retest and double publication guard belong to 3F.
- **3B1**: Define a pure, version/fingerprint-bound Host scope for one conditional branch per legal Drunk or a single no-Drunk branch, per-branch deterministic/discretionary/deferred data, proficiency when known, and fail-closed selection validation. Pure scope does not create Host legal results.
- **3B2 NEXT**: Wire Host's actual first-night legal decision producers into each branch (including drunk information, Investigator/Chef/Empath, real Fortune Teller applicability, red herring and bluff legality) and prove full legal coverage. No fallback synthetic candidate arrays.
- **3C/3D NEXT**: Launch single Luna request concurrently with shown-only role dealing, strict `openingPlan + strategyMemory` and one-tap Host adoption. Do not add another standalone Drunk request.
- **3E**: Host-confirmed events trigger global remaining-plan updates; preserve observations already published.
- **3F**: Android E2E, full independent CI/R2 and manual/offline/failure gate before production acceptance.

Mini MCP experimental-directory enhancement is developed separately; Host should not modify its server/service in this phase.
