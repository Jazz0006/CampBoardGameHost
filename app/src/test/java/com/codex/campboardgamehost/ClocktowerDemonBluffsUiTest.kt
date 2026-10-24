package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Compact real table: legal choices, visible identities, scroll, manual repair and Session commit. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
class ClocktowerDemonBluffsUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun compactTableKeepsDrunkIdentityAndConfirmsOnlyThreeSelectedCandidates() {
        val session = session(listOf("Chef", "Soldier", "Mayor", "Drunk", "Empath", "Baron", "Imp"), "Fortune Teller")
        var commits = 0
        ui.setContent {
            ClocktowerDarkTheme {
                ClocktowerDemonBluffsHostScreen(session.state, emptyList(), false, {}, { names ->
                    commits++
                    session.commitDemonBluffs(names.map(::RoleId), StorytellerPhase.FIRST_NIGHT, 1, session.nextInputSequence)
                })
            }
        }
        ui.onNodeWithText("Actual: Drunk").assertExists()
        ui.onNodeWithText("Shown: Fortune Teller").assertExists()
        listOf("Chef", "Soldier", "Mayor", "Drunk", "Empath", "Baron", "Imp", "Fortune Teller", "Poisoner").forEach {
            candidate(it).assertDoesNotExist()
        }
        confirm().assertIsNotEnabled().assertIsDisplayed()
        listOf("Monk", "Washerwoman", "Saint").forEach { candidate(it).performScrollTo().assertIsDisplayed().performClick() }
        confirm().assertIsEnabled().assertIsDisplayed()
        ui.runOnIdle { assertEquals(0, commits); assertEquals(emptyList<RoleId>(), session.state.demonBluffRoleIds) }
        ui.onNodeWithText("Monk ×").performClick()
        confirm().assertIsNotEnabled()
        candidate("Recluse").performScrollTo().performClick()
        confirm().performClick()
        ui.runOnIdle {
            assertEquals(1, commits)
            assertEquals(listOf("Washerwoman", "Saint", "Recluse").map(::RoleId), session.state.demonBluffRoleIds)
            assertEquals(1, session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.DemonBluffChoice>().size)
        }
    }

    @Test fun invalidAutomaticRecommendationRequiresExplicitConfirmationAfterManualRepair() {
        val session = session(listOf("Chef", "Soldier", "Mayor", "Drunk", "Empath", "Baron", "Imp"), "Monk")
        var commits = 0
        ui.setContent {
            ClocktowerDarkTheme {
                ClocktowerDemonBluffsHostScreen(session.state, listOf("Monk", "Chef", "Saint"), true, {}, { commits++ })
            }
        }
        ui.onNodeWithText("Demon bluffs · 1/3").assertExists()
        listOf("Recluse", "Butler").forEach { candidate(it).performScrollTo().performClick() }
        confirm().assertIsEnabled()
        ui.runOnIdle { assertEquals(0, commits) }
        confirm().performClick()
        ui.runOnIdle { assertEquals(1, commits) }
    }

    @Test fun fifteenSeatTableKeepsLegalSelectionAndConfirmationReachable() {
        val session = session(listOf("Washerwoman", "Librarian", "Investigator", "Chef", "Empath", "Fortune Teller",
            "Undertaker", "Monk", "Soldier", "Drunk", "Butler", "Saint", "Poisoner", "Baron", "Imp"), "Virgin")
        var committed = emptyList<String>()
        ui.setContent {
            ClocktowerDarkTheme {
                ClocktowerDemonBluffsHostScreen(session.state, emptyList(), false, {}, { committed = it })
            }
        }
        ui.onNodeWithText("P15").assertIsDisplayed()
        ui.onNodeWithText("Actual: Drunk").assertExists()
        candidate("Virgin").assertDoesNotExist()
        listOf("Ravenkeeper", "Slayer", "Mayor").forEach { candidate(it).performScrollTo().assertIsDisplayed().performClick() }
        confirm().assertIsDisplayed().assertIsEnabled().performClick()
        ui.runOnIdle { assertEquals(listOf("Ravenkeeper", "Slayer", "Mayor"), committed) }
    }

    @Test fun completeValidAutomaticRecommendationStillCommitsWithoutSkippingPrivateDisplay() {
        val session = session(listOf("Chef", "Soldier", "Mayor", "Drunk", "Empath", "Baron", "Imp"), "Monk")
        var committed = emptyList<String>()
        val recommendation = listOf("Washerwoman", "Recluse", "Saint")
        ui.setContent {
            ClocktowerDarkTheme {
                ClocktowerDemonBluffsHostScreen(session.state, recommendation, true, {}, { committed = it })
            }
        }
        ui.runOnIdle {
            assertEquals(recommendation, committed)
            assertEquals(0, session.state.epistemicObservationLog.records.size)
        }
    }

    private fun candidate(name: String) = ui.onNode(hasClickAction() and hasText(name))
    private fun confirm() = ui.onNodeWithText("Confirm Demon bluffs")

    private fun session(names: List<String>, drunkShown: String): ClocktowerGameSession {
        val players = names.mapIndexed { i, name ->
            val team = requireNotNull(clocktowerRoleByName(name)).team
            PlayerState(i + 1, "P${i + 1}", RoleId(name),
                if (team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)) Alignment.EVIL else Alignment.GOOD,
                when (team) {
                    ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
                    ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
                    ClocktowerTeam.Minion -> CharacterType.MINION
                    ClocktowerTeam.Demon -> CharacterType.DEMON
                }, if (name == "Drunk") RoleId(drunkShown) else null)
        }
        val game = GameState(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(), players, 19L)
        return ClocktowerGameSession.createProduction("bluff-ui", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }
}
