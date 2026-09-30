package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.TroubleBrewingGameSnapshotJsonCodec
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class DrunkAssignmentDecisionContextTest {
    @Test
    fun `G10 interchange snapshot derives the rules legal Drunk domain without Host setup objects`() {
        val snapshot = TroubleBrewingGameSnapshotJsonCodec.decode(
            File(
                "src/test/java/com/codex/campboardgamehost/clocktower/fixtures/" +
                    "g10-game2-precommit-tbgs-v1.json",
            ).readText(Charsets.UTF_8),
        )
        val context = DrunkAssignmentDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = canonicalRegistry(),
            sourceRevision = InformationDecisionRevision(0L, 0L),
        )

        assertEquals("setup:drunk-seat:evidence:c1d:g10-game2", context.decisionId)
        assertEquals(20_260_929L, context.selectionSeed)
        assertEquals(
            listOf(1, 3, 4, 6, 7, 8),
            context.legalCandidates.map { it.seat },
        )
        assertEquals(
            listOf("empath", "undertaker", "librarian", "monk", "mayor", "virgin"),
            context.legalCandidates.map { it.shownRoleId },
        )
        assertEquals(
            listOf(
                "setup:drunk-seat:seat-1",
                "setup:drunk-seat:seat-3",
                "setup:drunk-seat:seat-4",
                "setup:drunk-seat:seat-6",
                "setup:drunk-seat:seat-7",
                "setup:drunk-seat:seat-8",
            ),
            context.legalCandidateIds,
        )
    }

    private fun canonicalRegistry() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
}
