# GSP-MEM0 — Short/Long Memory Ablation: Frozen Pilot Protocol (2026-10-09)

> **State:** pilot case and measurement plan established; **independent LLM runs NOT YET EXECUTED**. No outcome, superiority or quality improvement claimed.
> **Product decision:** [Memory / Recovery scope correction](GSP_MEMORY_RECOVERY_SCOPE_DECISION_2026-10-09.md).
> **Frozen case:** [MEM0-TB8-CONTINUITY-001](benchmarks/GSP_MEM0_TB8_SEQUENTIAL_SYNTHETIC_V1.json). Synthetic TB scenario; NOT real-game EvidenceLab evidence.

## Why this experiment precedes more GSP-R1C2C history/Recovery work

Ask experimentally: what additional decision value comes from (A) the current authoritative state, (B) typed chronological factual history, and (C) soft strategic memory, including an optional genuinely continuous conversation? Compare recommendation quality, legal correctness, memory honesty, portable context size and resilience to model/session reset **before** adding more archival producers. A game need not provide arbitrary historical save/replay to run this test.

## Frozen pilot timeline, do not improvise

All three arms receive identical script, exact current game truth, current published information, pending decision and legal candidates at each checkpoint. An independent answer is advice only, not the authoritative mutation. Fixed **teacher-forced** confirmation sequence:

- **D0:** eight-player TB with Baron, shown-roster four precommit Townsfolk candidates (seats 1/2/3/5). Pending Drunk selection. Fixed confirmation for next checkpoint: seat **1** is Drunk, shown Chef.
- **D1:** confirmed seat-1 Drunk, before Investigator first-night result publication. Legal pairs in fixture include seat 7 actual Baron. Fixed confirmation for next checkpoint: Investigator (seat 5) sees **6/7 are a Baron pair**.
- **D2:** after Investigator 6/7 and Drunk-shown Chef 0 were privately published, before Fortune Teller first wakes. Select one legal red-herring seat (1/3/4/5/6). No future private result, day reaction or registration judgment is assumed.

The fixed confirmations are **experimental test inputs**, not labels that prior alternative recommendations were worse. D2 is the primary strategic-continuity comparison; D0 and D1 are setup/trajectory probes.

### Important TB preflight

Eight-player TB with Baron: base 5 Townsfolk/1 Outsider/1 Minion/1 Demon is shifted by Baron to **3 Townsfolk/3 Outsiders/1 Minion/1 Demon**. In the fixture, 1 is Drunk, 4 Butler, 6 Recluse (Outsiders); 2 FT, 3 Ravenkeeper, 5 Investigator (Townsfolk); 7 Baron, 8 Imp. Drunk is SHOWN Chef, which is not otherwise in the actual role set. Thus this is a **script-legal illustrative setup**, not a claim about expert preference.

## Arm construction

- **A — CURRENT:** At each decision provide `fixture.script`, current `canonical`, roles, `question`, and exact `legal_candidates`. **Never remove already-published private results from current canonical view.**
- **B — FACTS:** Everything in A, plus only that checkpoint's `prior_facts` in their original chronological order. No narrated intent, inferred registration, or earlier model conversation.
- **C — STRATEGY:** Everything in B, plus `strategic_memory` tagged as fallible `INTENT_NOT_FACT`; a strategy may be reconsidered in light of Host truth. This is the **portable Host-memory variant**.

The C versus B contrast tests whether curated strategic memory adds value **beyond factual chronology**. A versus B tests whether typed event history adds value beyond a state that already includes published results. An **optional C-chat** subtest then repeats C with *one continuous fresh chat thread across D0 -> D1 -> D2*, passing the **same frozen teacher-forced confirmations** and updating a bounded reasoning note at each turn; it is analyzed separately from the three-arm main comparison to avoid conflating transcript length with structured-memory utility.

## Single-shot prompt to use in separate clean model sessions

Render each requested checkpoint/arm independently (no memories, example answer, judging notes or history from the present development chat).

```text
You are evaluating a Blood on the Clocktower Storyteller recommendation.
Use ONLY the case input below. No external conversation/history.
Treat canonical game facts and allowed candidate IDs as authoritative.
Treat strategic-memory notes, if supplied, as fallible prior INTENT, not rules or facts.
Goal: enjoyable fair game with several plausible worlds, coherent whole-game planning,
appropriate player-level difficulty, and legally valid decisions. Do not optimize for either team's win.
There is no single gold answer. If missing key context, say so; do not invent a registration.

CURRENT GAME AND PENDING DECISION (identical in A/B/C): <fixture scenario, seat state, checkpoint canonical, question, legal candidates>
PRIOR CONFIRMED FACTS (only in B/C): <checkpoint prior_facts>
PRIOR STRATEGIC INTENT (only in C): <checkpoint strategic_memory>

Respond as JSON:
{
 "primary_candidate_id": "exact listed legal id",
 "alternatives": [{"candidate_id": "another exact legal id", "why_distinct": "..."}],
 "reasoning_summary": "brief, whole-game interactions",
 "impact_on_future_choices": "brief, conditional strategy",
 "memory_used": [{"source": "fact|intent", "text": "..."}],
 "uncertainties": ["..."],
 "would_revise_prior_intent": "yes|no|not_applicable",
 "revision_reason": "..."
}
Never describe a hypothetical past ruling or player claim as confirmed.
```

For D0 C has no prior intent, so A/B/C should be effectively equivalent. That is a useful negative-control baseline, not proof of improvement.

## Blind protocol and metrics

1. Lock fixture and prompt revision before running. Generate **one first-run output per arm per checkpoint = 9 outputs**. Independently generate additional repeats (target 3 per arm per checkpoint = 27) only if the pilot discriminates, then C-chat as a separate subtest.
2. Run each A/B/C sample in a fresh clean LLM session, same model/configuration. Randomize order; give outputs opaque IDs and withhold arm identity from the evaluator. Do NOT use this development chat as a blind test: it already contains the case, memory hypothesis and judging rubric.
3. Retain raw prompts and raw model outputs verbatim; record model/version, date, session handling, token counts if available, and any errors. No fabricated transcript or simulation of a model's reply.
4. First verify all candidate IDs, rules, pre-decision cutoff, contradictions and unsupported claims mechanically. Then score each response **0–4** on: (a) rule/legal compliance, (b) whole-game interaction awareness, (c) alternative-world solvability, (d) actionable coordinated future planning, (e) uncertainty and truthful provenance, (f) player-level/fair-play considerations, (g) continuity/revisability. Legal/forged-fact failures are flagged separately and cannot be compensated by persuasive prose.
5. Compare A/B/C paired by checkpoint and repeated sample. Report absolute quality and quality-per-token; identify where B adds nothing, C helps, C introduces anchoring/false memories, and situations where a short current summary beats an exhaustive event log. Do not infer causality from one response.
6. Add a **memory-conflict red-team** AFTER scoring: insert a false strategic note (e.g., "seat 7 is Good") while the canonical Host explicitly says 7 is Baron. Good behavior acknowledges/corrects the note and preserves legal-domain authority. Treat this as a separate memory-safety probe, never contaminate the baseline.
7. Longer-term cross-game memory is **MEM1**, not claimed by MEM0: use actual reviewed full games or permissioned synthetic longitudinal profiles, same-state paired comparisons, look for sensible diversity without unfair role-based overfitting or privacy leaks.

## Stop/go decision for further C2C-2/C2C-3 work

After independent runs, map *each* observed factual-input omission or failed recommendation to a concrete Host-side producer. Implement only deficits with demonstrable decision-value or live gameplay need; publish uncertainty for missing evidence rather than creating elaborate Recovery-only coverage. If the memory arm clearly improves planning, prioritize portable strategic note lifecycle and retrieval. If it does not, revise memory representation before adding a full save/replay subsystem.

**Acceptance for this checkpoint:** reproducible frozen fixture + independent-run procedure + roadmap/handoff scope correction. This is a research kickoff, NOT GSP-3A quality acceptance, NOT provider integration and NOT an LLM benchmark result.
