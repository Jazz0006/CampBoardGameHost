# Next Development Handoff — Same-Night Effective State Continuation

> Date: 2026-08-27  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable main at handoff: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`

## 1. Startup contract

Before changing code, read:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
8. `docs/TESTING_STRATEGY.md`.

Then re-query live `main`, branch head, PR #54 state/head, and latest checks. Do not rely on this SHA list if GitHub has advanced.

## 2. Accepted production checkpoints

The last fully accepted same-night production checkpoint is:

```text
51179ecca667d5450550375735ca49aae932c06d
fix: materialize exact Demon successor at Dawn
```

Validation:

```text
focused GREEN
:app:testFast GREEN
git diff --check GREEN
R2 #675 SUCCESS
CI #748 SUCCESS
```

Immediately before that:

```text
5a94c63536c04382f59963843c2ac10544962b02
SNE-6B2.5 A–D stage checkpoint
R2 #673 SUCCESS
CI #746 SUCCESS
```

Do not regress these contracts:

- effective current-role actor lookup;
- Poisoner effect ends when source loses Poisoner role;
- Fortune Teller sees both old dead Demon and new current Demon;
- Spy/Recluse registration follows current role but retains even-if-dead behavior;
- Imp successor exact confirmed fact is projected same-night;
- Dawn materialization uses exact confirmed successor only;
- no draft/fallback first-Minon materialization;
- unresolved mandatory succession blocks premature outcome/Dawn.

## 3. Reverted 6C direction

A RED for Mayor redirect killing the Demon and same-night Scarlet Woman succession was briefly created at:

```text
173eb86967ef8503cc02eef8d10a7e367edf4f9b
```

It was intentionally reverted before any 6C production implementation:

```text
0d165250a5bc6a9dd6cd4edfc5d216663e99263e
revert: defer generic non-self Demon succession
```

Do not resurrect that RED during the current Trouble Brewing closeout.

## 4. New product decision — Mayor cannot redirect to Demon

Current product restriction:

```text
Mayor redirect target cannot be the current Demon.
```

This is an intentional house-rule/product simplification, not official BotC semantics.

Generic non-self Demon death / Scarlet Woman succession remains deferred for future arbitrary dynamic/custom scripts. See `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`.

## 5. Mayor restriction work already committed

After the 6C1 revert, ChatGPT created the following work on the same branch:

```text
b864db41c2c1aa46728b45925312a7fd10322316
  RED source-wiring contract:
  ClocktowerMayorDemonExclusionWiringTest.kt

30e00f28fd7b31f74e0b5aca5d8af2dac4aca8bd
  MayorRedirectLegality.kt

625ff5b03593860f920605bd9989281e50e6bb6b
  MayorRedirectRecommender excludes CharacterType.DEMON
  resolveOutcome rejects a direct Demon target

36eb5b1165752b0bc714abe305f342a484eed412
  MayorRedirectRecommenderTest adds Demon exclusion coverage
```

The source-wiring RED intentionally remains RED until the large Host/UI wiring below is completed.

## 6. Documentation/workflow closeout already committed

```text
eba6aadb021dd859cac789681cbfacd11c3f0aea
  AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md

62413a3bd7630acc0551651957007ff01c5ec65c
  DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md

c81673d8513a1b2075a3dad3dd239bdc11aa35c5
  SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md

9d3940e0e8ac316d54f036fbc3ae08428be4fa0b
  root AGENTS.md tightened

61db454d7133516c6f2a3bbe7e9cc2652c3e3396
  CURRENT_DEVELOPMENT_ROADMAP.md updated
```

The new workflow explicitly fixes the problems observed today:

- connector owns safe small/medium edits;
- Luna only executes large/truncated/local-only file changes;
- every Luna instruction is one deterministic fenced block;
- expected JUnit failure is RED PASS when specified;
- no duplicate rerun of a Luna-passed focused test;
- T0 per micro-slice, broad tests at logical checkpoint;
- no old-head CI wait between micro-slices;
- source-wiring tests lock semantic structure, not formatting.

## 7. Immediate remaining implementation — Mayor Host/UI wiring

At the time this handoff was written, the next required production edit is limited to two large files:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt
```

Required behavior:

### Host

Import:

```kotlin
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegality
```

After `mayorCanRedirect`, derive an independent manual legality set:

```kotlin
val mayorRedirectTargetCards = cards.filter { card ->
    card.name != mayorTarget?.name &&
        MayorRedirectLegality.canReceiveRedirect(
            targetIsDemon = card.clocktowerTeam == ClocktowerTeam.Demon,
        )
}
```

Validate the confirmed/restored Mayor target before it becomes mechanical authority:

```kotlin
val effectiveMayorRedirectTarget = mayorRedirectTarget
    ?.takeIf { confirmedName ->
        mayorRedirectTargetCards.any { it.name == confirmedName }
    }
```

Then `resolvedNightDeathName` must use `effectiveMayorRedirectTarget`, not raw `mayorRedirectTarget`:

```kotlin
val resolvedNightDeathName =
    if (mayorCanRedirect && effectiveMayorRedirectTarget != null) {
        effectiveMayorRedirectTarget
    } else {
        pendingNightDeath
    }
```

Pass `mayorRedirectTargetCards` into every `ClocktowerNightStepCardLocalized(...)` call immediately alongside the other target-card sets.

### Night UI

Add parameter:

```kotlin
mayorRedirectTargetCards: List<PlayerCard>,
```

In `ClocktowerNightAction.MayorRedirect`, replace the recommendation-derived manual card filter with:

```kotlin
cards = mayorRedirectTargetCards
```

The Mayor manual UI must not contain:

```kotlin
assistedDecisionOptions.map(ClocktowerDecisionOption::targetName)
```

as its legality source.

No App change is required for this slice: an invalid restored confirmed target may remain stored, but Host fails closed and treats it as non-authoritative until a legal target is confirmed.

## 8. Required focused validation for Mayor closeout

Before production edit, the focused source-wiring test should fail as expected.

After production edit, run only:

```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.codex.campboardgamehost.ClocktowerMayorDemonExclusionWiringTest" \
  --tests "com.codex.campboardgamehost.clocktower.recommendation.MayorRedirectRecommenderTest" \
  --rerun-tasks \
  --no-daemon
```

Then `git diff --check`.

Because this Mayor restriction is intended as the final end-of-day logical checkpoint, run `:app:testFast --rerun-tasks --no-daemon` once after the Host/UI GREEN patch. After push, ChatGPT should audit the remote diff and then use the latest-head GitHub CI/R2 as the checkpoint gate.

## 9. Next work after Mayor checkpoint

After Mayor Demon exclusion is GREEN and accepted, proceed to **SNE-7 final restore/recomposition matrix**. Do not reopen generic non-self Demon succession during this Trouble Brewing closeout.

SNE-7 must include the Mayor restriction restore case in addition to the existing succession/current-role matrix.

## 10. Do not do

- do not merge or mark PR #54 ready;
- do not resume App-root decomposition;
- do not broaden to arbitrary custom-script Demon death;
- do not let recommendations define Mayor legality;
- do not reintroduce early public role mutation;
- do not run broad CI/test gates after every micro-edit;
- do not ask Luna to redesign or audit the architecture.
