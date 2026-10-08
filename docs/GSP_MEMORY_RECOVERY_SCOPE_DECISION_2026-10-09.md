# GSP-MEM — Strategic Memory and Recovery Scope Correction (2026-10-09)

> **Status:** accepted product/design decision; experiment design and execution are a separate evidence gate.  
> **Authority:** explicit owner review on 2026-10-09; applies ahead of stale R1C2C historical/recovery expansion wording.  
> **Repo:** `Jazz0006/CampBoardGameHost`.  
> **Baseline reviewed:** GitHub and Mini MCP `main@e1d64ab308f81f94ca9bf07bf2e1ececcc949405`, clean; no open PR at preflight.

## 1. Correction: second overinvestment in save/Recovery

The product has **twice** diverted substantial development into overly ambitious save/reconstruction/Recovery work. Do not start a third save-system expansion under the label of GSP history completeness. The user explicitly judges this a waste of development time relative to recommendation quality.

Existing 2026-09-28 current-only Recovery contract remains authoritative:
- **Emergency interruption only**, most recent current-game continuation;
- **current format + exact compatibility token + <=4h**;
- no long-term unfinished-game saves, next-day continuation, cross-version migration, obsolete archive compatibility, or full app/UI draft reconstruction;
- **restore the game, not the App**.

**Interpretation:** A durable current-game history needed by the recommendation engine is *not* identical to a traditional save-game or a strict, arbitrary historical replay database. Existing accepted canonical history, registration ambiguity, and safety work are retained; do **not** delete useful current behavior or reopen completed work.

## 2. Revised memory authority

Previous phrase `LLM/chat conversation memory is never correctness authority` means **model recollection is not the source of truth for rules or confirmed game facts**. It does **not** mean LLM memory is forbidden, that every call must be memoryless, or that the Host must reconstitute every prior inference exclusively through a perfect replay log.

Separate four layers:
1. **Host-authoritative truth:** actual/shown identities, script/setup, ability functioning and legal candidate domain, committed actions and player-facing published information. Engine validates every commit and provider output.
2. **Current-game tactical/strategic memory:** earlier reasons, whole-game coordination intent, hypotheses, discarded options, planned future follow-ups, unresolved tradeoffs, and changes of mind. LLM can maintain this; Host can retain portable structured summaries and revision/provenance for continuity.
3. **Cross-game soft memory:** player experience, role exposure, prior pressure, recommendation diversity and player feedback, subject to identity/privacy/retention design. Never silently turn inferred preferences into hard rules.
4. **Evidence/knowledge:** EvidenceLab complete real games, expert analysis and benchmark examples retrieved as contextual material rather than if/then policy.

Model memory is advisory and fallible. **When it conflicts with Host truth, Host wins.** Keep strategic notes explicitly marked as `INTENT/HYPOTHESIS/UNCERTAINTY`, never as `FACT`. Preserve provider replaceability: model-native conversations may help, but portable Host-owned strategic summaries must allow fallback/restart/provider switching. No autonomous provider commit.

Short-term continuous conversation, compacted memory and relevant cross-game retrieval are permitted research options. No promise that model memory perfectly remembers all information; test accuracy, leakage, contradiction handling, consistency, cost and portability.

## 3. History, Replay and Recovery: keep vs defer

| Domain | Keep / required | Do not expand solely for… |
| --- | --- | --- |
| Engine rules and live continuation | Canonical state, legal choices, consumed abilities, current confirmed actions, essential past received information | Superfluous proof of impossible histories already excluded by legal setup |
| Provider context | Correct current state, relevant prior events/results, explicit unknowns, snapshot at decision time, optional verified strategic summaries | Every UI event as a globally interleavable immutable replay event |
| Benchmark | Frozen `as-of` fixtures, no future-state leakage, stable candidate IDs and separate scoring | Universal historical rollback/reconstruction of every released app version |
| Recovery | Restore same currently supported snapshot within 4h; minimally test real interruption and essential consistency | Old legacy-format acceptance, hypothetical tampered archives without reachable risk, full decision chronology parity as an independent product objective |
| Cross-game | Small, selective, consent-aware experience and diversity signals | Long-lived incomplete-game save archives |

Where a new fact is useful to Host *or* LLM, capture it at its real producer/publish boundary. If also required to continue after an actual interruption, persist via the current Recovery contract. Otherwise **do not** make deep Recovery round-trip plus historical arbitrary-cutoff replay an unconditional per-family gate.

## 4. Concrete evidence / prior overreach

- Completed Recovery simplification PR #165 already established the short-horizon contract.
- PR #284 corrected an overstrict current Recovery first-Virgin assertion that rejected a **valid Drunk shown Virgin** TB scenario. This is a real correctness repair and remains valid.
- Draft PR #286 attempted to recheck the known Drunk shown-role setup uniqueness at Recovery ingress; owner review closed it **unmerged** because legal production setup already establishes the invariant and no reachable current-format defect was shown. Do not re-open without concrete evidence of a real ingress trust-boundary requirement.
- PR #276 removed production-unreachable synthetic TB+Klutz+Spy paths; this is useful dead-code cleanup. Do not confuse synthetic legacy fixture validation with live-script feature coverage.

## 5. Execution reprioritization / anti-regression gate

**Now:** GSP-MEM0 short-term strategic-memory ablation experiment, then GSP-MEM1 analysis. This experimental detour is intentional and bounded; it does not mark already merged R1C stages undone. **Pause** speculative C2C-2/C2C-3 typed-producer expansions *until* the memory experiment identifies which missing facts actually change recommendation quality.

Every proposed new history/Recovery work item must provide:
1. Concrete real supported-script/gameplay use case;
2. Specific required consumer: mechanical rule, next-game continuation, provider quality, or blinded evaluation;
3. Why existing current snapshot, an event fact or a compact strategic-memory note is insufficient;
4. What is *not* needed (legacy migration, arbitrary replay, duplicate setup checks);
5. Bounded test proportional to production risk.

No speculative P2/P3 validation patch purely to satisfy archive completeness. Real reachable failures and rules errors still receive ordinary priority. Retain offline Manual gameplay. **No new remote LLM production API** until independent GSP benchmark evidence and proper provider validation.

## 6. Immediate experiment

Run `GSP_MEMORY_ABLATION_EXPERIMENT_2026-10-09.md` with an explicitly **synthetic** TB multi-decision pilot first, then repeat on EvidenceLab whole-game cases where actually reconstructable. Primary comparison:

- A — current authoritative state and legal candidates only;
- B — A plus typed chronological factual history;
- C — B plus explicitly marked strategic-memory notes / optional same-conversation continuation subtest.

Use same canonical state, legal domains and decision time for all arms. Never make a preselected "expert answer" the sole success metric; focus on whole-game interaction, legality, continuity, alternative worlds, player experience, memory correctness and practical cost. **Do not claim improvement before independent runs exist.**
