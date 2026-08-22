package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerProductionAbilityFunctioningWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Monk selection remains interactive but only a functioning Monk protects a Demon target`() {
        assertTrue(hostSource.contains("AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), \"Monk\")"))
        assertTrue(appSource.contains("AbilityFunctioningSemantics.selectedMechanicalEffectApplies("))
        assertTrue(appSource.contains("role = \"Monk\","))
        assertTrue(appSource.contains("selectionMatches = clocktowerConfirmedMonkProtectedTarget == deathName"))
    }

    @Test
    fun `Ravenkeeper and Sage death interactions use perceived role and preserve impairment reliability`() {
        assertTrue(hostSource.contains("AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), \"Ravenkeeper\")"))
        assertTrue(hostSource.contains("AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), \"Sage\")"))
        assertTrue(hostSource.contains("AbilityFunctioningState.POISONED -> InformationReliability.POISONED"))
        assertTrue(hostSource.contains("AbilityFunctioningState.DRUNK -> InformationReliability.DRUNK"))
        assertTrue(hostSource.contains("ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT"))
        assertTrue(hostSource.contains("ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON"))
    }

    @Test
    fun `Soldier Slayer Virgin and Mayor mechanical branches require functioning actual abilities`() {
        assertTrue(appSource.contains("\"Soldier\","))
        assertTrue(appSource.contains("\"Slayer\","))
        assertTrue(hostSource.contains("AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), \"Virgin\")"))
        assertTrue(hostSource.contains("AbilityFunctioningSemantics.functionsAs(it.abilitySubject(poisonTarget), \"Virgin\")"))
        assertTrue(appSource.contains("\"Mayor\","))
        assertTrue(hostSource.contains("AbilityFunctioningSemantics.functionsAs(it.abilitySubject(poisonTarget), \"Mayor\")"))
    }
}
