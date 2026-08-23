package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedEpistemicStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerHostSelectionSemanticsCharacterizationTest {
    private fun displayOption(
        label: String = "label",
        primary: String? = "primary",
        style: RecommendationStyle = RecommendationStyle.BALANCED,
        truthful: Boolean = true,
        isDefault: Boolean = false,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.None,
        displayTitle = "title",
        displayPrimary = primary,
        displaySecondary = "secondary",
        displayFooter = "footer",
        recommendationStyle = style,
        isTruthful = truthful,
        isDefaultRecommendation = isDefault,
    )

    @Test
    fun `information candidate id ignores label but remains semantic and truth sensitive`() {
        val baseline = displayOption(label = "first")
        val relabeled = displayOption(label = "second")
        val changedPrimary = displayOption(primary = "different")
        val changedTruth = displayOption(truthful = false)

        assertEquals(clocktowerInformationCandidateId(baseline), clocktowerInformationCandidateId(relabeled))
        assertNotEquals(clocktowerInformationCandidateId(baseline), clocktowerInformationCandidateId(changedPrimary))
        assertNotEquals(clocktowerInformationCandidateId(baseline), clocktowerInformationCandidateId(changedTruth))
    }

    @Test
    fun `decision pool keeps candidate identity tiers ranks and stable ranked order`() {
        val normal = ClocktowerDecisionOption(
            label = "normal",
            targetName = "Zulu",
            explanation = "normal",
            recommendationStyle = RecommendationStyle.BALANCED,
            reasonCodes = listOf("normal-reason"),
            warningCodes = listOf("normal-warning"),
        )
        val recommended = ClocktowerDecisionOption(
            label = "recommended",
            targetName = "Alpha",
            explanation = "recommended",
            recommendationStyle = RecommendationStyle.AGGRESSIVE,
            isDefaultRecommendation = true,
            reasonCodes = listOf("recommended-reason"),
        )

        assertNull(unifiedDecisionPool(emptyList(), "family"))
        val ranked = unifiedDecisionPool(listOf(normal, recommended), "family")!!.rankedCandidates

        assertEquals(listOf("AGGRESSIVE|Alpha", "BALANCED|Zulu"), ranked.map { it.candidateId })
        assertEquals(listOf(1_000_000L, 800_000L), ranked.map { it.rankFixedPoint })
        assertEquals(listOf(QualityTier.RECOMMENDED, QualityTier.ACCEPTABLE_WITH_WARNING), ranked.map { it.qualityTier })
        assertEquals(listOf("family", "family"), ranked.map { it.familyId })
        assertEquals(listOf(UnifiedCandidateLegality.LEGAL, UnifiedCandidateLegality.LEGAL), ranked.map { it.legality })
        assertEquals(listOf(UnifiedEpistemicStatus.VERIFIED, UnifiedEpistemicStatus.VERIFIED), ranked.map { it.epistemicStatus })
        assertEquals(listOf("recommended", "normal"), ranked.map { it.payload.label })
    }

    @Test
    fun `first night pool preserves the existing three rank bands`() {
        val default = displayOption(label = "default", style = RecommendationStyle.GENTLE, isDefault = true)
        val styleMatch = displayOption(label = "match", primary = "match", style = RecommendationStyle.AGGRESSIVE)
        val other = displayOption(label = "other", primary = "other", style = RecommendationStyle.BALANCED)

        val ranked = unifiedFirstNightInformationPool(
            options = listOf(other, styleMatch, default),
            familyId = "first-night",
            automaticStyle = RecommendationStyle.AGGRESSIVE,
        ).rankedCandidates

        assertEquals(listOf(1_000_000L, 900_000L, 800_000L), ranked.map { it.rankFixedPoint })
        assertEquals(listOf("default", "match", "other"), ranked.map { it.payload.label })
        assertEquals(listOf(UnifiedCandidateLegality.LEGAL, UnifiedCandidateLegality.LEGAL, UnifiedCandidateLegality.LEGAL), ranked.map { it.legality })
        assertEquals(listOf(UnifiedEpistemicStatus.VERIFIED, UnifiedEpistemicStatus.VERIFIED, UnifiedEpistemicStatus.VERIFIED), ranked.map { it.epistemicStatus })
    }

    @Test
    fun `registration pool keeps actual and special family identity and ranking`() {
        val actual = ClocktowerRegistrationRecommendationOption(
            label = "actual",
            usesSpecialRegistration = false,
            registeredRoleEnName = null,
            style = RecommendationStyle.BALANCED,
        )
        val special = ClocktowerRegistrationRecommendationOption(
            label = "special",
            usesSpecialRegistration = true,
            registeredRoleEnName = "Imp",
            style = RecommendationStyle.AGGRESSIVE,
            isDefaultRecommendation = true,
            reasonCodes = listOf("special-registration"),
        )

        assertNull(unifiedRegistrationPool(emptyList()))
        val ranked = unifiedRegistrationPool(listOf(actual, special))!!.rankedCandidates

        assertEquals(listOf("AGGRESSIVE|true|Imp", "BALANCED|false|"), ranked.map { it.candidateId })
        assertEquals(listOf("special-registration", "actual-registration"), ranked.map { it.familyId })
        assertEquals(listOf(1_000_000L, 800_000L), ranked.map { it.rankFixedPoint })
        assertEquals(listOf(QualityTier.RECOMMENDED, QualityTier.ACCEPTABLE_WITH_WARNING), ranked.map { it.qualityTier })
        assertEquals(listOf("special", "actual"), ranked.map { it.payload.label })
    }
}
