package com.codex.campboardgamehost.clocktower.rules

/**
 * Direct outcome of one Imp attack choice before Mayor redirect or Demon-succession branching.
 *
 * The two *_REQUIRED outcomes deliberately stop at the next rule-owned choice boundary. Callers
 * must branch those choices separately instead of smuggling Storyteller-selected targets into
 * player knowledge.
 */
internal enum class DemonNightAttackOutcome {
    NO_DEATH,
    TARGET_DIES,
    MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED,
    IMP_SELF_KILL_SUCCESSOR_REQUIRED,
}

internal data class DemonNightAttackContext(
    val attacker: AbilitySubject,
    val target: AbilitySubject,
    val targetIsAttacker: Boolean,
    val targetProtectedByFunctioningMonk: Boolean,
)

/** Trouble Brewing direct Demon-attack precedence shared by production and exact-world branching. */
internal object DemonNightAttackSemantics {
    fun resolve(context: DemonNightAttackContext): DemonNightAttackOutcome {
        if (!AbilityFunctioningSemantics.functionsAs(context.attacker, "Imp")) {
            return DemonNightAttackOutcome.NO_DEATH
        }
        if (!context.target.isAlive || context.targetProtectedByFunctioningMonk) {
            return DemonNightAttackOutcome.NO_DEATH
        }
        if (context.targetIsAttacker) {
            return DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED
        }
        if (AbilityFunctioningSemantics.functionsAs(context.target, "Soldier")) {
            return DemonNightAttackOutcome.NO_DEATH
        }
        if (AbilityFunctioningSemantics.functionsAs(context.target, "Mayor")) {
            return DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
        }
        return DemonNightAttackOutcome.TARGET_DIES
    }
}
