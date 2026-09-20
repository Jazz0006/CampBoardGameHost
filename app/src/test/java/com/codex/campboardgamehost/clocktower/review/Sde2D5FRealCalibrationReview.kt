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
        )
    }
}
