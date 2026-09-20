package com.codex.campboardgamehost.clocktower.review

internal object Sde2D5FRealCalibrationReviewBuilder {
    fun build(
        baseline: Sde2D5CrossRegimeCalibrationEvidence,
        drunk: Sde2D5DrunkCalibrationContrast,
        bluff: Sde2D5DemonBluffRealCalibration,
        roleInformation: Sde2D5RoleInformationRealCalibration,
        sealedHoldoutScenarioCount: Int =
            FirstNightBundleBeginnerCorpusBuilder.SEALED_HOLDOUT_SCENARIO_COUNT,
    ): Sde2D5FCalibrationReviewMaterial {
        require(
            bluff.roleDomainCompleteness ==
                Sde2D5CalibrationRoleDomainCompleteness.FULL_SCRIPT_DOMAIN,
        ) {
            "D5F Demon-bluff review material requires FULL_SCRIPT_DOMAIN calibration evidence."
        }
        val selectedRoleInformation = listOf(
            roleInformation.topologyNeutral,
            roleInformation.closestRawDifferentTopology.first,
            roleInformation.closestRawDifferentTopology.second,
            roleInformation.strongestStrategicCollapse,
            roleInformation.weakestMechanicalInformation,
        ).distinctBy { it.point.pointId }

        return Sde2D5FCalibrationReviewBuilder.build(
            sealedHoldoutScenarioCount = sealedHoldoutScenarioCount,
            baselineReferences = baseline.points,
            drunkContrasts = listOf(drunk),
            bluffSelections = bluff.selected,
            roleInformationEvidence = selectedRoleInformation,
            confirmationSelections = roleInformation.confirmationChainSelections,
            // D5E deliberately uses one extreme 7-player diagnostic fixture. Human review
            // showed that Chef=1 + Empath=0 already leaves Evil with little practical room.
            // Keep these records for regression/diagnostic value, but do not let them
            // calibrate BEGINNER policy gates.
            roleInformationReviewability = Sde2D5FReviewability.DIAGNOSTIC_ONLY,
            confirmationReviewability = Sde2D5FReviewability.DIAGNOSTIC_ONLY,
        )
    }
}
