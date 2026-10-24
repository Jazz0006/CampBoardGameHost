# PROD-GLOBAL-3B2 — ChatGPT 5.5 Low/High 盲测参考记录（2026-10-12）

> **EXTERNAL USER-REPORTED REFERENCE / NOT LUNA API RESULTS / NOT A GOLDEN ANSWER.**
> This note must **never** be added to a Luna prompt, fixture.input, or a blind grader's model-facing data. It records two user-provided chat-interface results under one common, freeform Chinese benchmark prompt, distinct from our concise semantic JSON-output system prompt.

## Input and comparability

The user used the same 8-seat TB shown roster: 1 Chef, 2 Fortune Teller, 3 Ravenkeeper, 4 Butler, 5 Investigator, 6 Recluse, 7 Baron, 8 Imp; there is a Drunk whose seat is uncommitted, round-table adjacency 8–1. **All eight players are beginner-level**, know the rules but have little experience. This is a material difference from existing **E01** (mixed recorded/unknown experience). Preserve E01 for baseline comparisons and offer **optional E05** with all beginner levels. E05 changes *only player experience* in that roster and retains explicit unknown future actions. It is opt-in, excluded from default `--case all` paid batch.

The chat inputs required a long human-readable risk/choice/alternate/confidence analysis and 2+ alternatives. The manual 3B2 Luna experiment instead asks one semantic openingPlan plus compact strategyMemory under strict JSON Schema and more structured role rules. Models, effort levels, context, output budgets and transport also differ. Consequently, this is **qualitative reference evidence only**; do not infer a controlled A/B speed or quality win/loss.

## Reported primary bundles

| Component | Chat 5.5 Low | Chat 5.5 High |
|---|---|---|
| Drunk | seat 1 shown Chef | seat 1 shown Chef |
| Chef information | 0 | 0 |
| Investigator | 6/7, Baron | 6/7, Baron |
| Fortune Teller red herring | seat 4 Butler | seat 4 Butler |
| Demon bluff trio | Saint / Monk / Librarian | Washerwoman / Monk / Saint |
| Meaningfully different alternatives | 2, but one is mislabeled | 3, including a Recluse registration option |
| Confidence | medium | medium |

These are observations, **not** recommended default outcomes or test assertions. Both responses identified the pressure created by a real Chef=1 and an Investigator pair containing 7 Baron, especially for novice Evil. Both used Drunk Chef=0 to reduce early cross-confirmation, while retaining an Investigator true-information anchor and a separate Fortune Teller false-positive source. This is encouraging evidence for joint inter-role reasoning; it is not evidence that those particular seat choices are objectively optimal.

## Mechanism-level review

1. **Correct:** seat 1 is shown Townsfolk Chef and therefore can legally be Drunk. Chef is 0 if the Drunk is shown that fabricated result; actual evil pair 7–8 contributes 1 to true Chef information.
2. **Correct but needs conceptual precision:** an Investigator 6/7 Baron observation is lawful *because 7 actually is Baron*. Recluse 6 need not be invoked to make this observation lawful. Recluse can register as a Minion/Baron in other legal results, and players may entertain alternative explanations, but a possible registration witness is not a new canonical fact or mutually exclusive world in the Host grimoire.
3. **Low alternate B labeling error:** Drunk Investigator shown `2/7 Baron` is described as a “false pair,” but 7 is in fact Baron, so this proposed observation is true. Drunk information **may** be true or false; “Drunk” does not imply fabricated facts in every displayed clue. This is an explanatory/strategic-calibration failure, not an illegal use of the Drunk.
4. **High alternate C is potentially lawful:** Drunk Chef=0 plus a sober Investigator told `1/6 Baron` can be supported by Recluse seat 6 registering as Baron even though actual Baron seat 7 is outside the pair. The validity of that result is conditional on the Recluse registration at that interaction and must be checked through the Host's result-first legal domain; do not automatically commit a unique registration witness.
5. **Legal discretion, not a fixed solution:** High avoids Librarian as a Demon bluff while Low selects it. Both proposed triples use distinct Good roles absent from the shown roster. Their value to novice Evil/Good depends on claimed worlds, outsider count, timing and player behavior; do not hardcode a ban or preference for Librarian, Saint, or Washerwoman.
6. **Pressure is not automatic proof:** a sober Chef=1 confirms the *number* of adjacent evil pairs, not their location. Combined with Investigator 6/7 Baron it increases plausible attention on certain neighborhoods, but does not by itself uniquely identify 7/8; both Chat responses may overestimate novice Good players’ automatic ability to solve the exact evil pair from those two clues alone. Evaluate the actual hypothesis narrowing, not repeated words like “double-confirmed” or “locked.”
7. **Strategy beyond deterministic clues remains untested:** players have not made Fortune Teller queries, Butler master selections, Ravenkeeper death/target actions or public claims. Neither Chat result is proof that the proposed strategyMemory would stay consistent after actual actions.

Rules references: https://wiki.bloodontheclocktower.com/Drunk ; https://wiki.bloodontheclocktower.com/Recluse ; https://wiki.bloodontheclocktower.com/Investigator ; https://wiki.bloodontheclocktower.com/Fortune_Teller ; https://wiki.bloodontheclocktower.com/States .

## Evaluation implications

- **Avoid answer contamination:** the Luna system prompt, E05 input and raw model request must not contain the above recommendations, seat-specific warnings or words such as “expect Chef Drunk / Investigator 6/7 / RH 4”. The generic reference rules and separate hidden evaluation notes remain allowed.
- **Do not use agreement as ground truth:** matching Low/High primary packages may indicate a robust, obvious option or shared inductive bias. Critically score alternatives and mechanisms; allow different legal, high-quality outputs.
- **Add a correctness check independent from legal-status:** “the explanation describes true Investigator information as false,” “the claimed world assumes Recluse registration automatically happened,” and “future FT observation was invented” should count as separate explanation/epistemic quality problems even if the final selected bundle itself is mechanically legal.
- **The model should not be required to emit an essay in production** merely because the chat benchmark did. Keep concise private intentions and only enough choice rationale for human review.
- **No new Luna data yet.** Compare the future E05 output with user-reported results solely as an unblinded qualitative interpretation. B2 reference numbers remain separate and not an equal-condition controlled baseline.
