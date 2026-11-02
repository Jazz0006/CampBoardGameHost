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
    @Test fun courtierClearedChoiceSkipsWithoutSpendingAndLaterSelectionSpendsOnce() {
        val roles = listOf("Courtier", "Fool", "Pacifist", "Assassin", "Po")
        val players = roles.mapIndexed { i, name ->
            val r = badMoonRisingRoles.single { it.enName == name }
            PlayerState(i + 1, "P${i + 1}", RoleId(name),
                if (r.team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)) Alignment.EVIL else Alignment.GOOD,
                CharacterType.valueOf(r.team.name.uppercase()))
        }
        val s = ClocktowerGameSession.createProduction("courtier-ui", 96L,
            GameState(BadMoonRisingOpening.script, players, 96L), ClocktowerSemanticHistoryMode.GLOBAL_V1)
        val rules = BuiltInClocktowerRulesetCatalog { java.io.File("src/main/assets/$it").readText() }.ruleset(ClocktowerScript.BadMoonRising)
        val shown = mutableStateOf(s.state)
        ui.setContent { ClocktowerBmrNightControls(shown.value, rules, {}) { action ->
            s.confirmBmrHostAction(action, StorytellerDecisionRevision(shown.value.gameStateRevision, shown.value.playerInputRevision), rules)
            shown.value = s.state
        } }
        fun choosePo() {
            ui.onNodeWithText("Choose a character").performScrollTo().performClick()
            ui.onNode(hasText("Po") and hasAnyAncestor(isPopup()), useUnmergedTree = true).performScrollTo().performClick()
        }
        val initial = s.state
        choosePo()
        ui.onNodeWithText("Clear selection").performScrollTo().performClick()
        assertEquals(initial, s.state)
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
        ui.waitForIdle()
        assertFalse(s.state.bmrUses(1, "Courtier").single().used)
        assertTrue(s.state.gameState.players.none { it.drunk })
        s.completeBmrNight(s.state.gameStateRevision, rules)
        s.confirmBmrEndDay(false, s.state.gameStateRevision)
        s.continueBmrAfterDay(s.state.gameStateRevision)
        ui.runOnIdle { shown.value = s.state }
        choosePo()
        val beforeConfirm = s.state
        assertTrue(beforeConfirm.gameState.players.none { it.drunk })
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
        ui.waitForIdle()
        assertEquals(1, s.state.bmrUses(1, "Courtier").count { it.used })
        assertEquals(RoleId("Po"), s.state.bmrUses(1, "Courtier").last().chosenRole)
        assertEquals(setOf(5), s.state.gameState.players.filter { it.drunk }.map { it.seat }.toSet())
        assertTrue(s.state.bmrNightInteractions(rules).none { it.roleId == RoleId("Courtier") })
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }

    @Test fun sailorTargetChangeClearsDiscretionAndSelfChoiceCommitsOnlySelfDrunkenness() {
        val roles = listOf("Sailor", "Fool", "Pacifist", "Assassin", "Po")
        val players = roles.mapIndexed { i, name ->
            val r = badMoonRisingRoles.single { it.enName == name }
            PlayerState(i + 1, "P${i + 1}", RoleId(name),
                if (r.team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)) Alignment.EVIL else Alignment.GOOD,
                CharacterType.valueOf(r.team.name.uppercase()))
        }
        val s = ClocktowerGameSession.createProduction("sailor-ui", 95L,
            GameState(BadMoonRisingOpening.script, players, 95L), ClocktowerSemanticHistoryMode.GLOBAL_V1)
        val rules = BuiltInClocktowerRulesetCatalog { java.io.File("src/main/assets/$it").readText() }.ruleset(ClocktowerScript.BadMoonRising)
        val before = s.state
        var saves = 0
        s.persistWith { saves++ }
        val baseline = saves
        val shown = mutableStateOf(s.state)
        ui.setContent { ClocktowerBmrNightControls(shown.value, rules, {}) { action ->
            s.confirmBmrHostAction(action, StorytellerDecisionRevision(shown.value.gameStateRevision, shown.value.playerInputRevision), rules)
            shown.value = s.state
        } }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("P2", useUnmergedTree = true).performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Chosen player: #2 P2 · Fool").performScrollTo().performClick().assertIsSelected()
        ui.onNodeWithText("Next →").assertIsEnabled()
        ui.onNodeWithText("P3", useUnmergedTree = true).performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Sailor: #1 P1 · Sailor").assertIsNotSelected()
        ui.onNodeWithText("P1", useUnmergedTree = true).performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        assertEquals(before, s.state)
        assertEquals(baseline, saves)
        ui.onNodeWithText("Next →").performClick()
        ui.waitForIdle()
        assertEquals(baseline + 1, saves)
        val use = s.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.BmrAbilityUse>().single { it.role == RoleId("Sailor") }
        assertEquals(listOf(1), use.targetSeats)
        assertEquals(setOf(1), s.state.gameState.players.filter { it.drunk }.map { it.seat }.toSet())
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }

    @Test fun lunaticNightDisplayHidesRealIdentityAndNextCommitsOnlyTheLastShownInformation() {
        val roles = listOf("Fool", "Pacifist", "Tea Lady", "Minstrel", "Gossip", "Lunatic", "Assassin", "Pukka")
        val game = GameState(BadMoonRisingOpening.script, roles.mapIndexed { i, name ->
            val role = badMoonRisingRoles.single { it.enName == name }
            PlayerState(i + 1, "P${i + 1}", RoleId(name), if(role.team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)) Alignment.EVIL else Alignment.GOOD,
                CharacterType.valueOf(role.team.name.uppercase()), shownRole = if(name == "Lunatic") RoleId("Pukka") else null)
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
            ui.onNodeWithText("Shown bluffs (", substring = true).performScrollTo().performClick()
            ui.onNodeWithText(role, useUnmergedTree = true).performScrollTo().performClick()
        }
        choose("Grandmother"); choose("Sailor"); choose("Courtier")
        ui.onNodeWithText("Show to player").performScrollTo().performClick()
        ui.onNodeWithText("Lunatic", substring = true, useUnmergedTree = true).assertDoesNotExist()
        ui.onNodeWithText("Outsider", useUnmergedTree = true).assertDoesNotExist()
        assertEquals(before, s.state)
        assertEquals(baseline, saves)
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("× Grandmother").performScrollTo().performClick()
        choose("Gambler")
        ui.onNodeWithText("Show to player").performScrollTo().performClick()
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

        fun showAndConfirm(checkDisplay: () -> Unit) {
            val beforeDisplay = s.state
            val savedBeforeDisplay = saves
            ui.onNodeWithText("Next →").assertIsNotEnabled()
            ui.onNodeWithText("Show to player").performClick()
            checkDisplay()
            assertEquals(beforeDisplay, s.state)
            assertEquals(savedBeforeDisplay, saves)
            ui.onNodeWithText("Close display card").performClick()
            assertEquals(beforeDisplay.nextBmrNightInteraction(rules), s.state.nextBmrNightInteraction(rules))
            ui.onNodeWithText("Next →").assertIsEnabled().performClick()
            ui.waitForIdle()
            assertEquals(savedBeforeDisplay + 1, saves)
            assertEquals(setOf(8), s.state.epistemicObservationLog.records.last().recipientSeats)
        }
        // The real Demon learns the Lunatic's identity before the simulated first-night choice.
        assertTrue(s.state.nextBmrNightInteraction(rules)!!.id.value.startsWith("bmr:lunatic-identity:"))
        showAndConfirm {
            ui.onNodeWithText("You are").assertExists()
            ui.onNodeWithText("Pukka").assertExists()
            ui.onNodeWithText("Lunatic").assertExists()
            ui.onNodeWithText("#6 P6").assertExists()
            ui.onNodeWithText("Outsider", substring = true).assertDoesNotExist()
        }
        val simulated = s.state.nextBmrNightInteraction(rules)!!
        assertEquals(6, simulated.actorSeat)
        assertEquals(RoleId("Pukka"), simulated.roleId)
        ui.onNodeWithText("P3", useUnmergedTree = true).performClick()
        ui.onNodeWithText("Next").assertIsEnabled().performClick()
        ui.waitForIdle()
        assertTrue(s.state.gameState.players.none { it.poisoned || !it.alive })
        assertTrue(s.state.nextBmrNightInteraction(rules)!!.id.value.startsWith("bmr:lunatic-targets:"))
        showAndConfirm {
            ui.onNodeWithText("Lunatic").assertExists()
            ui.onNodeWithText("#6 P6").assertExists()
            ui.onNodeWithText("#3 P3").assertExists()
            ui.onNodeWithText("Pacifist", substring = true).assertDoesNotExist()
        }
        assertEquals("first_night:system:demon_info", s.state.nextBmrNightInteraction(rules)!!.id.value)
        showAndConfirm {
            ui.onNodeWithText("#7 P7").assertExists()
            ui.onNodeWithText("Grandmother\nSailor\nCourtier").assertExists()
            // The real Demon receives actual Minions/bluffs, not the Lunatic's selected P2/Gambler.
            ui.onNodeWithText("P2", substring = true).assertDoesNotExist()
            ui.onNodeWithText("Gambler", substring = true).assertDoesNotExist()
            ui.onNodeWithText("Assassin", substring = true).assertDoesNotExist()
        }
        val real = s.state.nextBmrNightInteraction(rules)!!
        assertEquals(8, real.actorSeat)
        assertEquals(RoleId("Pukka"), real.roleId)
        ui.onNodeWithText("P4", useUnmergedTree = true).performClick()
        ui.onNodeWithText("Next").assertIsEnabled().performClick()
        ui.waitForIdle()
        assertEquals(setOf(4), s.state.gameState.players.filter { it.poisoned }.map { it.seat }.toSet())
        assertTrue(s.state.gameState.players.all { it.alive })
        assertNull(s.state.nextBmrNightInteraction(rules))
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }
}
