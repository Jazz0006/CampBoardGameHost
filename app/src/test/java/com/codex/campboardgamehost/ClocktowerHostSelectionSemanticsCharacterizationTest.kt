package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
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
    fun `first night drunk shown empath preserves every registration-valid numeric truth`() {
        val players = listOf(
            player(1, "Recluse", CharacterType.OUTSIDER),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val options = (0..2).map { value ->
            ClocktowerDisplayOption(
                label = value.toString(),
                displayKind = ClocktowerDisplayKind.Number,
                displayTitle = "Empath information",
                displayPrimary = value.toString(),
                displaySecondary = null,
                displayFooter = null,
                proposition = InformationProposition.NumericResult(
                    metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                    value = value,
                    subjectSeats = listOf(1, 3),
                ),
                isTruthful = value == 0,
                misinformationPressure = value,
            )
        }

        val firstNight = projectFirstNightNumericInformationOptions(
            phase = ClocktowerPhase.FirstNight,
            roleEnName = "Empath",
            sourceSeat = 2,
            players = players,
            options = options,
        )
        val otherNight = projectFirstNightNumericInformationOptions(
            phase = ClocktowerPhase.Night,
            roleEnName = "Empath",
            sourceSeat = 2,
            players = players,
            options = options,
        )

        assertEquals(setOf(0, 1), firstNight.filter { it.isTruthful }.map { it.displayPrimary!!.toInt() }.toSet())
        assertEquals(mapOf(0 to 0, 1 to 0, 2 to 1), firstNight.associate { it.displayPrimary!!.toInt() to it.misinformationPressure })
        assertEquals(options, otherNight)
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

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
