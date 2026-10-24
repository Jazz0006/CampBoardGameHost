package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
class ClocktowerPlayerDisplayPrivacyUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val cards = listOf(PlayerCard("P1"), PlayerCard("P2"), PlayerCard("P3"))

    @Test fun drunkUndertakerAndRavenkeeperRevealOnlyTheChosenRoleWithoutHostExplanations() {
        fun reveal(role: String, shown: String): ClocktowerNightStepUi {
            val actor = PlayerCard("P1", clocktowerRole = clocktowerRoleByName("Drunk"), clocktowerShownRole = clocktowerRoleByName(role))
            val builder = ClocktowerInformationStepBuilder(cards, "en", { _, en -> en }, { actor }, { "" }, { _, _ -> AbilityFunctioningState.DRUNK })
            val step = builder.build(role, role, null, "HOST secret reasoning",
                informationQuestion = ClocktowerInformationQuestion.Role(2))
            val choice = requireNotNull(clocktowerDirectRoleRevealChoice(step, 2, cards.size, shown,
                clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)) { it })
            val resolved = resolveClocktowerPlayerDisplay(step, choice.displayOption)
            return if (role == "Undertaker") clocktowerUndertakerPlayerDisplayStep(resolved, choice, cards, "en") else resolved
        }
        val step = mutableStateOf(reveal("Undertaker", "Chef"))
        ui.setContent { ClocktowerPlayerDisplayCardLocalized(step.value, cards) {} }
        for (role in listOf("Undertaker", "Ravenkeeper")) {
            ui.runOnIdle { step.value = reveal(role, "Chef") }
            ui.onNodeWithText("Chef").assertIsDisplayed()
            ui.onNodeWithText("HOST secret reasoning", substring = true).assertDoesNotExist()
            ui.onNodeWithText("actually the Drunk", substring = true).assertDoesNotExist()
            ui.onNodeWithText("Yesterday's executed player", substring = true).assertDoesNotExist()
            ui.onNodeWithText("$role information").assertDoesNotExist()
        }
        // Drunk is legitimate information about a TARGET; do not censor that actual result.
        ui.runOnIdle { step.value = reveal("Undertaker", "Drunk") }
        ui.onNodeWithText("Drunk").assertIsDisplayed()
        ui.onNodeWithText("actually the Drunk", substring = true).assertDoesNotExist()
    }

    @Test fun allPlayerDisplayFamiliesIgnoreBothHostExplanationAndExplicitFooters() {
        fun step(kind: ClocktowerDisplayKind, paired: Boolean) = ClocktowerNightStepUi(
            title = "Host step", actor = null, isRealAction = true, reason = "HOST reason",
            storytellerAction = "HOST instruction", tellPlayer = "Result", explanation = "HOST explanation: poisoned",
            displayKind = kind, displayTitle = "Information", displayPrimary = "Result",
            displaySecondary = if (paired) "2 / 3" else null, displayFooter = "HOST footer: Drunk",
            displayProposition = if (paired) when (kind) {
                ClocktowerDisplayKind.EitherOne -> InformationProposition.AnyOf(listOf(
                    InformationProposition.RoleAt(2, RoleId("Chef")), InformationProposition.RoleAt(3, RoleId("Chef"))))
                ClocktowerDisplayKind.Number -> InformationProposition.NumericResult(NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 1, listOf(2, 3), 1)
                else -> null
            } else null,
            presentationSubjectSeats = if (paired) listOf(2, 3) else emptyList(),
        )
        val value = mutableStateOf(step(ClocktowerDisplayKind.Number, false))
        ui.setContent { ClocktowerPlayerDisplayCardLocalized(value.value, cards) {} }
        for (kind in listOf(ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo, ClocktowerDisplayKind.EitherOne,
            ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain, ClocktowerDisplayKind.EvilInfo)) {
            for (paired in if (kind in setOf(ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo, ClocktowerDisplayKind.EitherOne))
                listOf(false, true) else listOf(false)) {
                ui.runOnIdle { value.value = step(kind, paired) }
                ui.onNodeWithText("HOST", substring = true).assertDoesNotExist()
                ui.onNodeWithText("Result").assertExists()
            }
        }
    }
}
