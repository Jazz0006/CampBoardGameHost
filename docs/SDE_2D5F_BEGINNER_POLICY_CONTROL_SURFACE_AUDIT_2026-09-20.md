# SDE-2D5F — BEGINNER Policy Control-Surface / APP-Adaptation Audit

> Date: 2026-09-20 Australia/Sydney
> Repository: Jazz0006/CampBoardGameHost
> Branch: sde-2d5-calibration-policy-evidence
> Status: DESIGN CORRECTION — EXTERNAL HUMAN SIGNALS MUST BE PROJECTED THROUGH APP OWNERSHIP

## 1. Problem

External human Storyteller evidence repeatedly supports a `HealthyTruthDanger / EvilTopologyCoupling` concept.

However, a human Storyteller may react to the physical draw while distributing tokens and decide that a dangerous information seat should instead be the Drunk shown as that role.

The APP lifecycle is different.

The APP first commits setup facts, including:

- actual roles;
- seat/player assignment;
- which player is the Drunk;
- the Drunk shown role.

After those facts are persistent, the Storyteller Decision Engine must not retroactively replace a healthy role with the Drunk.

Therefore external-human behavior must not be copied directly into the Night-1 information policy.

Core rule:

> Optimize only over variables owned and still controllable at the current lifecycle stage.
> Uncontrollable facts may be diagnostics / risk context, but must not be treated as decision variables.

## 2. Stage-specific interpretation of HealthyTruthDanger

Keep one underlying diagnostic:

~~~text
CounterfactualHealthyTruthDanger(role, setup)
    = how strongly this role's healthy truthful information would expose
      the actual Evil topology / create catastrophic confirmation
~~~

But consume it differently by lifecycle stage.

### 2.1 PRE-GAME SETUP stage

The setup owner may control some combination of:

- role composition/template choice;
- role-to-seat/player permutation;
- actual Drunk identity;
- Drunk shown role, before it becomes persistent;
- Red Herring, before persistence;
- Demon bluff triplet, before reveal/commit.

At this stage the relevant policy is:

~~~text
UNMITIGATED_HEALTHY_TRUTH_EXPOSURE
~~~

A proposed setup is risky when a functioning healthy role would produce a highly collapsing truth and no already-committed setup impairment legitimately removes that exposure.

Examples:

~~~text
Healthy Empath between two Evil players
    -> high setup risk
    -> reject/reroll/reassign before roles are revealed when possible

Drunk shown Empath between two Evil players
    -> not the same setup failure
    -> the dangerous healthy truth is counterfactual
    -> evaluate the later Drunk clue separately
~~~

Do not require every powerful healthy clue to disappear. The existing minimum healthy-information floor still applies. The goal is to avoid catastrophic truth exposure, not to suppress useful Good information.

### 2.2 NIGHT-1 INFORMATION stage after setup persistence

Once actual role / Drunk / shown-role facts are persistent:

- a healthy Empath remains healthy;
- SDE cannot convert that player into Drunk;
- a deterministic healthy Empath number is not a Storyteller decision;
- `HealthyTruthDanger` is therefore diagnostic only for that channel.

If a dangerous healthy deterministic clue survived setup generation, Night-1 policy must not pretend it can repair the setup by fabricating an illegal clue.

For an already-impaired role, the same metric becomes useful counterfactual context:

~~~text
actual player = Drunk shown Empath

CounterfactualHealthyTruthDanger
    -> how dangerous the truthful Empath result would have been

candidate impaired clue
    -> choose among all legal Drunk outputs using
       marginal pressure + narrative coherence + temporal/history axes
~~~

### 2.3 CROSS-NIGHT POISONED / DRUNK stage

The Poisoner target is chosen by an Evil player.

The APP/SDE does not own that target and must not assume at setup time that Poisoner will rescue a dangerous healthy information seat.

After Poisoner target confirmation:

- the target becomes a durable hidden fact;
- SDE reacts to the actual impairment;
- it may choose a legal malfunctioning clue where Storyteller choice exists;
- `CounterfactualHealthyTruthDanger` helps measure what healthy truth is being suppressed;
- temporal consistency / impairment detectability / live narrative coherence govern which legal clue is appropriate.

Thus:

~~~text
Poisoner possibility
    != setup mitigation

confirmed Poisoner target
    -> runtime context for impaired-clue selection
~~~

## 3. Control-surface matrix

| Decision/fact | Owner before commit | Runtime status | BEGINNER policy may optimize? |
|---|---|---|---|
| role composition/template | setup generator/template owner | persistent after setup | YES, pre-game only |
| role-to-seat/player assignment | setup owner | persistent after reveal | YES, pre-game only |
| actual Drunk player | setup owner | persistent | YES only before persistence |
| Drunk shown role | setup owner | persistent | YES only before persistence; never SDE-reselected later |
| healthy Chef result | rules/mechanics | determined | NO; setup can only avoid pathological arrangement |
| healthy Empath result | rules/mechanics | determined | NO; setup can only avoid pathological arrangement |
| Washerwoman/Librarian/Investigator legal shown pair | Storyteller choice within rules | planned until shown | YES |
| Fortune Teller chosen pair | player | current player input | NO |
| Fortune Teller Red Herring | setup/Storyteller before persistence | persistent | YES before persistence |
| Poisoner target | Evil player | durable after confirmation | NO |
| Drunk/Poisoned malfunctioning clue where rules permit | Storyteller/SDE | planned until shown | YES |
| Demon bluff triplet | setup/SDE before reveal | persistent after reveal | YES before commit only |

The exact production owner remains whatever the existing architecture defines; this table freezes the policy-control principle, not a new duplicate state authority.

## 4. SetupEvaluator implication

BEGINNER setup evaluation should gain a structural risk axis distinct from later clue scoring:

~~~text
SetupTruthExposureRisk
~~~

Conceptually evaluate the proposed persistent setup after role/shown-role resolution:

1. find functioning healthy information channels whose truth is mechanically determined or strongly constrained;
2. estimate how much their truthful information would collapse Demon cover / Evil topology or create hard confirmation;
3. account for real persistent setup impairment such as an actual Drunk shown as that role;
4. do not count possible future Poisoner targeting as mitigation;
5. reject / reroll / reassign only when exposure is beyond the calibrated beginner band;
6. simultaneously protect the minimum healthy-information floor.

This naturally creates a two-sided setup objective:

~~~text
too much healthy truth exposure
        BAD: beginner Evil collapses immediately

acceptable middle band
        GOOD: useful information + multiple live worlds

too little healthy reliable information
        BAD: beginner Good has nothing to reason from
~~~

## 5. Important distinction for Drunk shown-role selection

Existing architecture remains:

~~~text
Drunk shown role
    = persistent setup/session truth once chosen

unshown Drunk clue
    = SDE planned output until shown
~~~

This audit does not authorize the SDE to choose/rechoose the shown role after setup persistence.

If the setup generator currently resolves the shown role from a configured candidate set, BEGINNER setup evaluation may later score the resulting setup candidates before persistence.

Whether the setup owner should actively rank shown-role alternatives versus preserve existing random selection is a separate product-policy decision and must not be silently changed inside D5F-B2.

## 6. Revised use of external human evidence

External records where a human Storyteller says:

> I made X the Drunk because their healthy truth would be dangerous.

should be translated into two possible APP lessons, depending on what the APP still controls:

~~~text
APP pre-game:
    avoid / mitigate the dangerous role-seat arrangement structurally

APP post-setup:
    if that role is already legally impaired,
    use healthy-truth danger only as context for clue selection
~~~

Do not translate the human action into:

~~~text
post-setup SDE may decide that a healthy player was actually Drunk
~~~

That would violate the persistent setup ownership already frozen in SDE-2A/D1.

## 7. Canonical edge case

Consider:

~~~text
seat 4 = healthy Empath
seat 3 = Evil
seat 5 = Evil
~~~

Healthy result is 2.

For BEGINNER APP behavior:

~~~text
PRE-GAME candidate evaluation
    -> high SetupTruthExposureRisk
    -> prefer another legal role/seat assignment if available

AFTER roles revealed / setup committed
    -> Empath=2 is rules-determined
    -> no SDE override
    -> log/diagnose setup risk only
~~~

Now change only:

~~~text
seat 4 actual = Drunk
seat 4 shown = Empath
seat 3 = Evil
seat 5 = Evil
~~~

Then:

~~~text
PRE-GAME
    -> dangerous healthy truth exists only counterfactually
    -> setup may remain acceptable

NIGHT-1
    -> legal Drunk candidate outputs = 0 / 1 / 2
    -> compare candidates with:
       counterfactualHealthyTruthDanger
       impairedStrategicPressure
       healthyInformationFloorImpact
       crossChannelNarrativeCoherence
       temporal/history constraints
~~~

This is the intended APP-adapted interpretation.

## 8. D5F-B2 consequence

Update the external-human parameter vocabulary:

~~~text
CounterfactualHealthyTruthDanger
    evidence-derived diagnostic

SetupTruthExposureRisk
    PRE-GAME setup-policy axis

ImpairedClueSuppressionContext
    runtime context only when the role is already impaired
~~~

Do not freeze thresholds yet.

Before D5F-C, candidate gates/orderings must document:

- lifecycle stage;
- decision owner;
- controllable variables;
- diagnostic-only variables;
- persistence boundary.

Any proposed policy that attempts to optimize a variable not owned at that stage is invalid by construction.
