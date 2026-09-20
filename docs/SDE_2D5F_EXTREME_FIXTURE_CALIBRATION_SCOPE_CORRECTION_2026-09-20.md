# SDE-2D5F Extreme Fixture Calibration Scope Correction — 2026-09-20

## Decision

The fixed D5E 7-player role-information fixture remains valid as **diagnostic/regression evidence**, but it is no longer valid as a representative human-calibration source for D5F-C BEGINNER policy gates.

The affected setup is:

~~~text
1 Washerwoman
2 Chef
3 Empath
4 Fortune Teller
5 Investigator
6 Scarlet Woman
7 Imp
~~~

## Why the calibration scope was wrong

D5E intentionally selected contrasts for diagnostic purposes:

- largest mechanically informative topology-neutral marginal;
- near-raw pairs with different topology retention;
- strongest strategic collapse;
- weakest mechanical information;
- confirmation-chain extremes.

That is useful for proving that raw world count and strategic pressure are not equivalent. It is not representative sampling.

Human review then exposed the practical consequence: in this setup, rule-determined `Chef=1` plus `Empath=0` already leaves very little Evil survival space. Changing Washerwoman/Investigator legal outputs can alter the narrative, but cannot turn this into a normal BEGINNER calibration environment.

Therefore the question “is this particular Washerwoman/Chef/Empath marginal acceptable?” is mostly measuring a pathological setup that production templates would avoid, not the quality of the SDE's controllable choice.

## Scope correction

The following D5E-derived families are retained in the review export but marked `DIAGNOSTIC_ONLY`:

- `ROLE_INFORMATION_CONTRAST`
- `BUNDLE_CONFIRMATION_CHAIN`

They no longer participate in required human-label membership.

The old v2 manifest is preserved as:

`app/src/test/resources/review/sde-2d5f-human-label-manifest-v2-obsolete-extreme-fixture.tsv`

Those labels are historical evidence only and must not be used to derive D5F-C gates.

## Gate safety

A new coverage check distinguishes:

- a policy variable that exists on a Storyteller/SDE-owned control surface;
- representative `REVIEWABLE` calibration evidence for that variable.

If the first exists but the second does not, gate derivation remains incomplete.

After this correction the deliberate missing variable is:

`HEALTHY_BUNDLE_INFORMATION`

This prevents the four retained v3 labels from accidentally making D5F-C look complete.

## Replacement evidence strategy

Representative healthy-information calibration should come from:

1. setups that the app can actually recommend, preferably existing templates;
2. normal/playable 7–9 player configurations rather than information-dense extremes;
3. same-setup comparisons where only SDE-owned legal output changes;
4. external-human / ClockTracker evidence where available;
5. human-readable whole-bundle context;
6. a middle-band majority plus a small number of clear guardrail pathologies.

Rule-determined Chef/Empath results remain context, not decision variables.

For Washerwoman/Librarian/Investigator, the useful calibration question is:

> Given this already-committed playable setup, which legal clue choice best preserves a beginner-friendly information ecology?

That is the policy question D5F-C must eventually answer.

## Preserved human findings

The scope correction does not erase useful qualitative findings discovered during v2 review:

- topology-neutral does not imply table-neutral;
- cross-confirmation can invalidate nominal Evil decoys;
- the same Chef result can be acceptable in one bundle and overwhelming in another;
- player count matters for impaired information;
- Drunk multi-night information needs temporal narrative coherence.

These findings should guide feature design and sample construction, but the extreme fixture's labels should not define numeric policy bands.
