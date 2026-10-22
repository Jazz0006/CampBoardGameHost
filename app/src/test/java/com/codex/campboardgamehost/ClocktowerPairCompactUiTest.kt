package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.session.HostPairResolution
import com.codex.campboardgamehost.clocktower.session.HostPairRoleOption
import com.codex.campboardgamehost.clocktower.session.HostPairRoleSource
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The genuine shared square-table control must remain operable at the product's smallest phone size. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
class ClocktowerPairCompactUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun fifteenSeatPairSelectsPhysicalSeatsBeforeShowingSingleValidRole() {
        val seats = (1..15).map { number ->
            HostSeatPresentation(ClocktowerSeatId(number), "P" + number, isAlive = true)
        }
        val roles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)
        var confirmed = 0
        ui.setContent {
            ClocktowerDarkTheme {
                CompositionLocalProvider(LocalClocktowerNightNextEnabled provides false) {
                    ClocktowerPairInformationSquareTableDialog(
                        interactionKey = "small:stage4", recommendedOption = null,
                        automaticOnly = false, scriptRoles = roles,
                        abilityRoleEnName = "Washerwoman", seats = seats, actorSeat = 1,
                        abilityLabel = "Washerwoman", language = "en",
                        roleLabel = { it },
                        resolvePair = { first, second ->
                            HostPairResolution(
                                options = if (first != null && second != null) listOf(
                                    HostPairRoleOption(RoleId("Soldier"), HostPairRoleSource.IN_PLAY),
                                ) else emptyList(),
                                ordinaryCandidateSeats = setOf(2),
                                specialCandidateSeats = setOf(3),
                                canShowNoOutsiders = false,
                                sourceAbility = AbilityFunctioningState.FUNCTIONING,
                                productShortlist = false,
                            )
                        },
                        onHostTools = {}, onNext = {},
                        onConfirm = { confirmed++ }, onDraftChanged = {},
                    )
                }
            }
        }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Choose first player").assertExists()
        ui.onNodeWithText("P2").performClick()
        ui.onNodeWithText("Choose second player").assertExists()
        ui.onNodeWithText("P3").performClick()
        ui.onNode(hasClickAction() and hasText("Show to player: Soldier", substring = true))
            .assertExists().performClick()
        ui.runOnIdle { assertEquals(1, confirmed) }
    }
}
