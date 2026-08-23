package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerAdvanceNightStepTransactionOwnershipTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val advanceTransaction = source
        .substringAfter("val advanceNightStep = {")
        .substringBefore("\n\n        LaunchedEffect(")

    @Test
    fun `advance night step keeps confirmations audits registrations semantic record and finalization host-owned in order`() {
        val poisonConfirm = requiredIndex("onConfirmPoisonTarget()")
        val monkConfirm = requiredIndex("onConfirmMonkProtectedTarget()")
        val demonConfirm = requiredIndex("onConfirmDemonAttack()")
        val mayorBranch = requiredIndex("if (currentStep.action == ClocktowerNightAction.MayorRedirect) {")
        val mayorAudit = requiredIndex("selectionDistributionTelemetry.recordCommittedSelection(", mayorBranch)
        val mayorConfirm = requiredIndex("onConfirmMayorRedirectTarget()", mayorAudit)
        val successorBranch = requiredIndex(
            "if (currentStep.action == ClocktowerNightAction.DemonSuccessor && automaticStorytellerInfo) {",
            mayorConfirm,
        )
        val successorAudit = requiredIndex("selectionDistributionTelemetry.recordCommittedSelection(", successorBranch)
        val spyRegistration = requiredIndex("recordSpyRegistration(", successorAudit)
        val recluseRegistration = requiredIndex("recordRecluseRegistration(", spyRegistration)
        val semanticRecord = requiredIndex("recordNightStep(currentStep)", recluseRegistration)
        val stepAdvance = requiredIndex("nightStepIndex = currentStepIndex + 1", semanticRecord)
        val nightFinalize = requiredIndex("onConfirmNight()", stepAdvance)

        assertTrue(poisonConfirm < monkConfirm)
        assertTrue(monkConfirm < demonConfirm)
        assertTrue(demonConfirm < mayorBranch)
        assertTrue(mayorBranch < mayorAudit)
        assertTrue(mayorAudit < mayorConfirm)
        assertTrue(mayorConfirm < successorBranch)
        assertTrue(successorBranch < successorAudit)
        assertTrue(successorAudit < spyRegistration)
        assertTrue(spyRegistration < recluseRegistration)
        assertTrue(recluseRegistration < semanticRecord)
        assertTrue(semanticRecord < stepAdvance)
        assertTrue(stepAdvance < nightFinalize)
    }

    private fun requiredIndex(token: String, startIndex: Int = 0): Int {
        val index = advanceTransaction.indexOf(token, startIndex)
        assertTrue("Missing advanceNightStep transaction token: $token", index >= 0)
        return index
    }
}
