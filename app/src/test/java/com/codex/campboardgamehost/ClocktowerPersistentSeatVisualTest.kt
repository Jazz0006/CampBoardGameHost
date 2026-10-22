package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.Alignment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Host-private persistent facts must remain visible together, independent of target chrome. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
class ClocktowerPersistentSeatVisualTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    private fun show(isAlive: Boolean, isDrunk: Boolean, isPoisoned: Boolean) {
        ui.setContent {
            MaterialTheme {
                ClocktowerSquareTableSeatSurface(
                    seats = (1..5).map { seat ->
                        ClocktowerSquareTableSeatUiModel(
                            seatId = seat.toString(),
                            seatNumber = seat,
                            label = "Player $seat",
                            isAlive = if (seat == 1) isAlive else true,
                            hasUnspentGhostVote = seat == 1 && !isAlive,
                            isDrunk = seat == 1 && isDrunk,
                            isPoisoned = seat == 1 && isPoisoned,
                            actualAlignment = Alignment.GOOD,
                        )
                    },
                    modifier = Modifier.size(360.dp, 520.dp),
                )
            }
        }
    }

    @Test fun deathDrunkAndPoisonMarkersCanCoexistOnTheSameSmallSeat() {
        show(isAlive = false, isDrunk = true, isPoisoned = true)
        ui.onNodeWithText("亡").assertExists()
        ui.onNodeWithText("醉").assertExists()
        ui.onNodeWithText("毒").assertExists()
    }

    @Test fun poisonMarkerIsNotShownWhenConditionEnds() {
        show(isAlive = true, isDrunk = true, isPoisoned = false)
        ui.onNodeWithText("亡").assertDoesNotExist()
        ui.onNodeWithText("醉").assertExists()
        ui.onNodeWithText("毒").assertDoesNotExist()
    }
}
