from pathlib import Path

HELPER = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerDynamicNightAdvance.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
TEST = Path("app/src/test/java/com/codex/campboardgamehost/ClocktowerDynamicNightAdvanceTest.kt")

for path in (HELPER, HOST, TEST):
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected line ending in {path}; refusing implicit normalization")

helper_old = '''package com.codex.campboardgamehost

internal sealed interface ClocktowerNightAdvanceDirective {
    data class MoveTo(val stepIndex: Int) : ClocktowerNightAdvanceDirective
    data object CompleteNight : ClocktowerNightAdvanceDirective
}

/**
 * Decides navigation from the list visible before the current step's confirmation mutates
 * checkpoint-derived flow. Dynamic flow mutations must not complete the night from that stale list.
 */
internal fun clocktowerNightAdvanceDirective(
    currentStepIndex: Int,
    currentStepCount: Int,
    flowMayExpandAfterConfirmation: Boolean,
): ClocktowerNightAdvanceDirective {
    require(currentStepCount > 0) { "Night advance requires at least one step." }
    require(currentStepIndex in 0 until currentStepCount) { "Current night step must be in range." }
    return when {
        currentStepIndex < currentStepCount - 1 ->
            ClocktowerNightAdvanceDirective.MoveTo(currentStepIndex + 1)
        flowMayExpandAfterConfirmation ->
            ClocktowerNightAdvanceDirective.MoveTo(currentStepIndex + 1)
        else -> ClocktowerNightAdvanceDirective.CompleteNight
    }
}

/**
 * A requested index at or past the refreshed list size proves that the confirmation did not insert
 * any new work at that slot. Otherwise the refreshed list owns the newly inserted trigger step.
 */
internal fun clocktowerDeferredNightAdvanceShouldComplete(
    requestedStepIndex: Int,
    refreshedStepCount: Int,
): Boolean {
    require(requestedStepIndex >= 0) { "Requested night step index must be non-negative." }
    require(refreshedStepCount > 0) { "Night advance reconciliation requires at least one step." }
    return requestedStepIndex >= refreshedStepCount
}
'''

helper_new = '''package com.codex.campboardgamehost

internal sealed interface ClocktowerNightAdvanceDirective {
    data class MoveTo(val stepIndex: Int) : ClocktowerNightAdvanceDirective
    data class AwaitRefreshedFlow(val currentStepIndex: Int) : ClocktowerNightAdvanceDirective
    data object CompleteNight : ClocktowerNightAdvanceDirective
}

/**
 * Decides navigation from the list visible before the current step's confirmation mutates
 * checkpoint-derived flow. Dynamic flow mutations keep the durable cursor on the current renderable
 * step until the refreshed flow can prove either a real next step or explicit night completion.
 */
internal fun clocktowerNightAdvanceDirective(
    currentStepIndex: Int,
    currentStepCount: Int,
    flowMayExpandAfterConfirmation: Boolean,
): ClocktowerNightAdvanceDirective {
    require(currentStepCount > 0) { "Night advance requires at least one step." }
    require(currentStepIndex in 0 until currentStepCount) { "Current night step must be in range." }
    return when {
        currentStepIndex < currentStepCount - 1 ->
            ClocktowerNightAdvanceDirective.MoveTo(currentStepIndex + 1)
        flowMayExpandAfterConfirmation ->
            ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(currentStepIndex)
        else -> ClocktowerNightAdvanceDirective.CompleteNight
    }
}

/** Resolves a pending dynamic advance only against a refreshed, renderable night-step list. */
internal fun clocktowerRefreshedNightAdvanceDirective(
    pending: ClocktowerNightAdvanceDirective.AwaitRefreshedFlow,
    refreshedStepCount: Int,
): ClocktowerNightAdvanceDirective {
    require(refreshedStepCount > 0) { "Night advance reconciliation requires at least one step." }
    require(pending.currentStepIndex in 0 until refreshedStepCount) {
        "Pending night cursor must remain renderable while flow refresh is pending."
    }
    val nextStepIndex = pending.currentStepIndex + 1
    return if (nextStepIndex < refreshedStepCount) {
        ClocktowerNightAdvanceDirective.MoveTo(nextStepIndex)
    } else {
        ClocktowerNightAdvanceDirective.CompleteNight
    }
}
'''

state_old = '''    var nightStarted by nightStartedState
    var nightStepIndex by nightStepIndexState
    var dayMode by dayModeState
    var deferredNightAdvanceIndex by remember(gameId, round, phase) { mutableStateOf<Int?>(null) }
    var nominatorName by remember(gameId, round) { mutableStateOf<String?>(null) }
'''

state_new = '''    var nightStarted by nightStartedState
    var nightStepIndex by nightStepIndexState
    var dayMode by dayModeState
    var pendingNightAdvance by remember(gameId, round, phase) {
        mutableStateOf<ClocktowerNightAdvanceDirective.AwaitRefreshedFlow?>(null)
    }
    var nominatorName by remember(gameId, round) { mutableStateOf<String?>(null) }
'''

advance_old = '''            when (
                val directive = clocktowerNightAdvanceDirective(
                    currentStepIndex = currentStepIndex,
                    currentStepCount = nightSteps.size,
                    flowMayExpandAfterConfirmation = flowMayExpandAfterConfirmation,
                )
            ) {
                is ClocktowerNightAdvanceDirective.MoveTo -> {
                    if (directive.stepIndex >= nightSteps.size) {
                        deferredNightAdvanceIndex = directive.stepIndex
                    }
                    nightStepIndex = directive.stepIndex
                }
                ClocktowerNightAdvanceDirective.CompleteNight -> onConfirmNight()
            }
        }

        LaunchedEffect(nightSteps.size, deferredNightAdvanceIndex) {
            val requestedIndex = deferredNightAdvanceIndex ?: return@LaunchedEffect
            if (
                clocktowerDeferredNightAdvanceShouldComplete(
                    requestedStepIndex = requestedIndex,
                    refreshedStepCount = nightSteps.size,
                )
            ) {
                deferredNightAdvanceIndex = null
                onConfirmNight()
            } else {
                deferredNightAdvanceIndex = null
            }
        }
'''

advance_new = '''            when (
                val directive = clocktowerNightAdvanceDirective(
                    currentStepIndex = currentStepIndex,
                    currentStepCount = nightSteps.size,
                    flowMayExpandAfterConfirmation = flowMayExpandAfterConfirmation,
                )
            ) {
                is ClocktowerNightAdvanceDirective.MoveTo -> nightStepIndex = directive.stepIndex
                is ClocktowerNightAdvanceDirective.AwaitRefreshedFlow -> pendingNightAdvance = directive
                ClocktowerNightAdvanceDirective.CompleteNight -> onConfirmNight()
            }
        }

        LaunchedEffect(nightSteps.size, pendingNightAdvance) {
            val pending = pendingNightAdvance ?: return@LaunchedEffect
            when (
                val directive = clocktowerRefreshedNightAdvanceDirective(
                    pending = pending,
                    refreshedStepCount = nightSteps.size,
                )
            ) {
                is ClocktowerNightAdvanceDirective.MoveTo -> {
                    pendingNightAdvance = null
                    nightStepIndex = directive.stepIndex
                }
                is ClocktowerNightAdvanceDirective.AwaitRefreshedFlow ->
                    error("Refreshed night advance cannot remain pending.")
                ClocktowerNightAdvanceDirective.CompleteNight -> {
                    pendingNightAdvance = null
                    onConfirmNight()
                }
            }
        }
'''

test_old = '''package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDynamicNightAdvanceTest {
    @Test
    fun `dynamic last step cannot expose a cursor outside the current renderable flow`() {
        val currentStepCount = 5
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = currentStepCount,
            flowMayExpandAfterConfirmation = true,
        )

        val exposedStepIndex = (directive as? ClocktowerNightAdvanceDirective.MoveTo)?.stepIndex
        assertTrue(
            "Pending dynamic refresh must not expose an out-of-range cursor: $directive",
            exposedStepIndex == null || exposedStepIndex in 0 until currentStepCount,
        )
    }

    @Test
    fun `ordinary last step still completes immediately`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = false,
        )

        assertEquals(ClocktowerNightAdvanceDirective.CompleteNight, directive)
    }

    @Test
    fun `existing next step advances normally`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 2,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = true,
        )

        assertEquals(ClocktowerNightAdvanceDirective.MoveTo(3), directive)
    }

    @Test
    fun `deferred advance keeps newly inserted trigger step`() {
        assertFalse(
            clocktowerDeferredNightAdvanceShouldComplete(
                requestedStepIndex = 5,
                refreshedStepCount = 6,
            ),
        )
    }

    @Test
    fun `deferred advance completes only after refreshed flow proves no step was inserted`() {
        assertTrue(
            clocktowerDeferredNightAdvanceShouldComplete(
                requestedStepIndex = 5,
                refreshedStepCount = 5,
            ),
        )
    }
}
'''

test_new = '''package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerDynamicNightAdvanceTest {
    @Test
    fun `dynamic last step keeps the current renderable cursor while awaiting refreshed flow`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = true,
        )

        assertEquals(ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(4), directive)
    }

    @Test
    fun `ordinary last step still completes immediately`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = false,
        )

        assertEquals(ClocktowerNightAdvanceDirective.CompleteNight, directive)
    }

    @Test
    fun `existing next step advances normally`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 2,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = true,
        )

        assertEquals(ClocktowerNightAdvanceDirective.MoveTo(3), directive)
    }

    @Test
    fun `refreshed dynamic flow moves only to a newly renderable next step`() {
        val directive = clocktowerRefreshedNightAdvanceDirective(
            pending = ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(4),
            refreshedStepCount = 6,
        )

        assertEquals(ClocktowerNightAdvanceDirective.MoveTo(5), directive)
    }

    @Test
    fun `refreshed dynamic flow completes when no next step was inserted`() {
        val directive = clocktowerRefreshedNightAdvanceDirective(
            pending = ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(4),
            refreshedStepCount = 5,
        )

        assertEquals(ClocktowerNightAdvanceDirective.CompleteNight, directive)
    }
}
'''

helper_text = HELPER.read_text(encoding="utf-8")
host_text = HOST.read_text(encoding="utf-8")
test_text = TEST.read_text(encoding="utf-8")

checks = [
    ("helper full-file anchor", helper_text.count(helper_old)),
    ("host state anchor", host_text.count(state_old)),
    ("host advance anchor", host_text.count(advance_old)),
    ("test full-file anchor", test_text.count(test_old)),
]
for label, count in checks:
    if count != 1:
        raise SystemExit(f"Expected exactly one {label}, found {count}")

helper_text = helper_text.replace(helper_old, helper_new, 1)
host_text = host_text.replace(state_old, state_new, 1).replace(advance_old, advance_new, 1)
test_text = test_text.replace(test_old, test_new, 1)

assert "MoveTo(currentStepIndex + 1)" not in helper_text.split("flowMayExpandAfterConfirmation ->", 1)[1].split("else ->", 1)[0]
assert "ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(currentStepIndex)" in helper_text
assert "clocktowerRefreshedNightAdvanceDirective(" in helper_text
assert "deferredNightAdvanceIndex" not in host_text
assert "pendingNightAdvance" in host_text
assert "nightStepIndex = directive.stepIndex" in host_text
assert "clocktowerDeferredNightAdvanceShouldComplete(" not in host_text
assert "AwaitRefreshedFlow(4)" in test_text

HELPER.write_text(helper_text, encoding="utf-8", newline="\n")
HOST.write_text(host_text, encoding="utf-8", newline="\n")
TEST.write_text(test_text, encoding="utf-8", newline="\n")
