# Same-Night Effective State — Product and Architecture Decisions

> Date: 2026-08-27  
> Scope: decisions made after SNE-6B2.6 while narrowing the remaining Trouble Brewing campaign.

## 1. Current Trouble Brewing Mayor restriction

For the current automatic-host product, Mayor night-death redirection **must not target the current Demon**.

This is an intentional product/house-rule restriction. It is not a claim about official Blood on the Clocktower rules.

Required implementation boundary:

```text
Mayor redirect rules legality
→ exclude Demon targets
→ manual UI shows all remaining legal targets
→ recommender may rank only inside that legal set
→ confirmed/restored Demon target fails closed and cannot become mechanical death authority
```

Do not implement the restriction only as a recommendation preference. A Demon target is illegal for this product slice.

## 2. Why this restriction exists

Without the restriction, Trouble Brewing can produce:

```text
Demon attacks Mayor
→ Mayor redirects the death to Demon
→ Demon dies during Mayor resolution
→ functioning Scarlet Woman at 5+ players must immediately become Demon
→ later same-night consumers must observe the new current role
→ public/persisted role still waits for the correct materialization boundary
```

Supporting that correctly is possible, but it introduces generic non-self Demon-death succession into a campaign whose current validated succession path is Imp self-kill.

The current product chooses a narrower legal-target rule instead of expanding that architecture now.

## 3. Deferred generic non-self Demon-death succession

Do **not** delete the underlying architectural requirement from long-term planning.

Before arbitrary dynamic/custom-script combinations are declared fully supported, the rules engine must support:

```text
ANY actual Demon death
+ functioning Scarlet Woman
+ 5+ players alive immediately before death
→ immediate same-night RoleChanged to the dead Demon type
```

This must be driven by the actual Demon-death cursor, not by a hard-coded Imp self-kill event or by UI step index.

Potential future sources of non-self night Demon death / Demon transfer include roles and flows such as:

- Assassin;
- Godfather;
- Gossip;
- Pit-Hag arbitrary deaths / Demon creation;
- Fang Gu transfer semantics;
- future/custom-script night-death effects.

The exact role set is not the architecture. The architecture requirement is generic Demon-death handling.

## 4. Deferred scope marker

Current Trouble Brewing campaign:

```text
Imp self-kill succession       SUPPORTED
Mayor redirect to Demon        PRODUCT-PROHIBITED
non-self Demon succession      DEFERRED
arbitrary custom combinations  NOT YET DECLARED COMPLETE
```

When the project resumes broad dynamic/custom-script correctness, reopen this deferred item explicitly rather than silently assuming the Mayor restriction solved the generic rule.
