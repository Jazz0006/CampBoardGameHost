package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.session.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
class ClocktowerSuccessionTableUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun compactSuccessionUsesCanonicalSeatsAndInvalidatesPreviewAfterRevisionOrReload() {
        fun session(count: Int): ClocktowerGameSession {
            val game = GameState(ScriptId("trouble_brewing"), (1..count).map { seat ->
                val role = if (seat == 1) "Imp" else if (seat == 2) "Scarlet Woman" else "Chef"
                val type = if (seat == 1) CharacterType.DEMON else if (seat == 2) CharacterType.MINION else CharacterType.TOWNSFOLK
                PlayerState(seat, "P$seat", RoleId(role), if (seat <= 2) Alignment.EVIL else Alignment.GOOD, type)
            }, 27L)
            return ClocktowerGameSession.createProduction("succession-$count", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1).also { s ->
                s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", StorytellerPhase.FIRST_NIGHT, 1,
                    s.nextInputSequence, StorytellerPhase.DAY, 1), s.state.gameStateRevision)
                s.confirmNomination(3, 1, null)
                s.confirmVote(3, 1, (1..count).toList())
                s.confirmEndDay(1)
            }
        }
        var s = session(5)
        val state = mutableStateOf(s.state)
        val mounted = mutableStateOf(true)
        var confirmed = 0
        ui.setContent {
            if (mounted.value) Box(Modifier.size(360.dp, 448.dp)) {
                ClocktowerNewDemonHostScreen(
                    ClocktowerSessionPlayerCards(emptyList(), { state.value }, "en"), state.value,
                    onHostTools = {}, onConfirmNewDemon = { confirmed++ },
                )
            }
        }
        for (count in listOf(5, 15)) {
            ui.runOnIdle { s = session(count); state.value = s.state }
            ui.onNodeWithText("Wake seat 2").assertIsDisplayed()
            ui.onNode(hasAnyDescendant(hasText("P2")) and SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Current wake target")).assertExists()
            ui.onNodeWithText("Informed, continue").assertIsNotEnabled()
            ui.onNodeWithText("Show full screen to player").assertIsDisplayed().performClick()
            ui.onNodeWithText("You are now the Imp").assertIsDisplayed()
            ui.onNodeWithText("Wake seat 2").assertDoesNotExist()
            ui.onNodeWithText("P1", substring = true).assertDoesNotExist()
            ui.onNodeWithText("Close display card").performClick()
            ui.onNodeWithText("Informed, continue").assertIsEnabled()
            ui.runOnIdle { s.recordPlayerInput(); state.value = s.state }
            ui.onNodeWithText("Informed, continue").assertIsNotEnabled()
            ui.onNodeWithText("Show full screen to player").performClick()
            ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
            ui.onNodeWithText("Informed, continue").assertIsEnabled()
            ui.runOnIdle { mounted.value = false }
            ui.runOnIdle {
                s = ClocktowerGameSession.restore(ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
                state.value = s.state; mounted.value = true
            }
            ui.onNodeWithText("Informed, continue").assertIsNotEnabled()
            assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        }
        assertEquals(0, confirmed)
    }
}
