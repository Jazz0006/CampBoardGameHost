package com.codex.campboardgamehost.clocktower.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class DemonNightAttackSemanticsTest {
    @Test
    fun `ordinary living target dies when Imp ability functions`() {
        assertEquals(
            DemonNightAttackOutcome.TARGET_DIES,
            resolve(target = subject("Empath")),
        )
    }

    @Test
    fun `dead target or nonfunctioning Imp produces no death`() {
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolve(target = subject("Empath", alive = false)),
        )
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolve(attacker = subject("Imp", poisoned = true), target = subject("Empath")),
        )
    }

    @Test
    fun `functioning Monk protection prevents every direct Imp death including self kill`() {
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolve(target = subject("Empath"), protectedByFunctioningMonk = true),
        )
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolve(
                attacker = subject("Imp"),
                target = subject("Imp"),
                targetIsAttacker = true,
                protectedByFunctioningMonk = true,
            ),
        )
    }

    @Test
    fun `functioning Soldier is safe but poisoned Soldier dies`() {
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolve(target = subject("Soldier")),
        )
        assertEquals(
            DemonNightAttackOutcome.TARGET_DIES,
            resolve(target = subject("Soldier", poisoned = true)),
        )
    }

    @Test
    fun `functioning Mayor requires storyteller death choice but poisoned Mayor dies directly`() {
        assertEquals(
            DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED,
            resolve(target = subject("Mayor")),
        )
        assertEquals(
            DemonNightAttackOutcome.TARGET_DIES,
            resolve(target = subject("Mayor", poisoned = true)),
        )
    }

    @Test
    fun `unprotected Imp self kill requires successor resolution`() {
        assertEquals(
            DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
            resolve(
                attacker = subject("Imp"),
                target = subject("Imp"),
                targetIsAttacker = true,
            ),
        )
    }

    private fun resolve(
        attacker: AbilitySubject = subject("Imp"),
        target: AbilitySubject,
        targetIsAttacker: Boolean = false,
        protectedByFunctioningMonk: Boolean = false,
    ): DemonNightAttackOutcome = DemonNightAttackSemantics.resolve(
        DemonNightAttackContext(
            attacker = attacker,
            target = target,
            targetIsAttacker = targetIsAttacker,
            targetProtectedByFunctioningMonk = protectedByFunctioningMonk,
        ),
    )

    private fun subject(
        role: String,
        poisoned: Boolean = false,
        alive: Boolean = true,
    ) = AbilitySubject(
        actualRole = role,
        shownRole = role,
        isPoisoned = poisoned,
        isAlive = alive,
    )
}
