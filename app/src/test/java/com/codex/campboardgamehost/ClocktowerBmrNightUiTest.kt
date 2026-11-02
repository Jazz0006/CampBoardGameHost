package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A real private handoff through the production BMR table: no fake observation callbacks. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class ClocktowerBmrNightUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    @Test fun lunaticNightDisplayHidesRealIdentityAndNextCommitsOnlyTheLastShownInformation() {
        val roles = listOf("Fool", "Pacifist", "Tea Lady", "Minstrel", "Gossip", "Lunatic", "Assassin", "Po")
        val game = GameState(BadMoonRisingOpening.script, roles.mapIndexed { i, name ->
            val role = badMoonRisingRoles.single { it.enName == name }
            PlayerState(i + 1, "P${i + 1}", RoleId(name), if(role.team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)) Alignment.EVIL else Alignment.GOOD,
                CharacterType.valueOf(role.team.name.uppercase()), shownRole = if(name == "Lunatic") RoleId("Po") else null)
        }, 94L)
        val s = ClocktowerGameSession.createProduction("bmr-private-ui", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        val rules = BuiltInClocktowerRulesetCatalog { java.io.File("src/main/assets/$it").readText() }.ruleset(ClocktowerScript.BadMoonRising)
        s.commitDemonBluffs(listOf(RoleId("Grandmother"), RoleId("Sailor"), RoleId("Courtier")), s.state.phase, s.state.round, s.nextInputSequence)
        s.confirmEvilInformation(rules, s.state.nextBmrNightInteraction(rules)!!.id.value)
        val before = s.state
        var saves = 0
        s.persistWith { saves++ }
        val baseline = saves
        val shownState = mutableStateOf(s.state)
        ui.setContent { ClocktowerBmrNightControls(shownState.value, rules, {}) { action ->
            s.confirmBmrHostAction(action, StorytellerDecisionRevision(shownState.value.gameStateRevision, shownState.value.playerInputRevision), rules)
            shownState.value = s.state
        } }
        ui.onNodeWithText("P2", useUnmergedTree = true).performClick()
        fun choose(role: String) {
            ui.onNodeWithText("Choose shown characters").performScrollTo().performClick()
            ui.onNodeWithText(role, useUnmergedTree = true).performScrollTo().performClick()
        }
        choose("Grandmother"); choose("Sailor"); choose("Courtier")
        ui.onNodeWithText("Show information").performScrollTo().performClick()
        ui.onNodeWithText("Lunatic", substring = true, useUnmergedTree = true).assertDoesNotExist()
        ui.onNodeWithText("Outsider", useUnmergedTree = true).assertDoesNotExist()
        assertEquals(before, s.state)
        assertEquals(baseline, saves)
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("× Grandmother").performScrollTo().performClick()
        choose("Gambler")
        ui.onNodeWithText("Show information").performScrollTo().performClick()
        ui.onNodeWithText("Gambler", substring = true).assertExists()
        ui.onNodeWithText("Grandmother", substring = true).assertDoesNotExist()
        ui.onNodeWithText("Lunatic", substring = true, useUnmergedTree = true).assertDoesNotExist()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next").assertIsEnabled().performClick()
        ui.waitForIdle()
        assertEquals(baseline + 1, saves)
        val actual = s.state.epistemicObservationLog.records.last()
        assertEquals(setOf(6), actual.recipientSeats)
        val given = actual.proposition as InformationProposition.AllOf
        assertEquals(setOf("Sailor", "Courtier", "Gambler"), given.propositions.filterIsInstance<InformationProposition.RoleInPlay>().map { it.role.value }.toSet())
        assertEquals(1, s.state.epistemicObservationLog.records.count { it.sourceAbility == RoleId("Lunatic") })
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }
}
