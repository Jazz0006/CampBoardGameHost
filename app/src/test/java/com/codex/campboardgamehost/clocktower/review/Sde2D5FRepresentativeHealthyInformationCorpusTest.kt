package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2D5FRepresentativeHealthyInformationCorpusTest {
    private val corpus by lazy {
        Sde2D5FRepresentativeHealthyInformationCorpusBuilder.build()
    }

    @Test
    fun `representative corpus uses normal production presets across 7 to 9 players and all pair roles`() {
        assertEquals(7, corpus.scenarios.size)
        assertEquals(
            setOf(7, 8, 9),
            corpus.scenarios.mapTo(linkedSetOf()) { scenario -> scenario.playerCount },
        )
        assertEquals(
            setOf(RoleId("Washerwoman"), RoleId("Librarian"), RoleId("Investigator")),
            corpus.scenarios.mapTo(linkedSetOf()) { scenario -> scenario.activeRole },
        )
        assertTrue(
            corpus.scenarios.all { scenario ->
                scenario.complexity == "beginner" || scenario.complexity == "standard"
            },
        )
        assertTrue(corpus.scenarios.all { scenario -> scenario.candidates.isNotEmpty() })
        assertTrue(
            corpus.scenarios.all { scenario ->
                scenario.candidates.map { candidate -> candidate.candidateId }.distinct().size ==
                    scenario.candidates.size
            },
        )
    }

    @Test
    fun `production deal seating freezes external poisoner context without poisoning reviewed source`() {
        val poisonerScenarios = corpus.scenarios.filter { scenario ->
            scenario.poisonerTargetSeat != null
        }

        assertEquals(setOf("TB2_8_010", "TB2_9_008", "TB2_9_031"), poisonerScenarios.mapTo(linkedSetOf()) { it.presetId })
        poisonerScenarios.forEach { scenario ->
            val targetSeat = requireNotNull(scenario.poisonerTargetSeat)
            assertTrue(scenario.seating.single { it.seat == targetSeat }.poisoned)
            assertFalse(scenario.seating.single { it.seat == scenario.activeSourceSeat }.poisoned)
        }
    }

    @Test
    fun `investigator sample preserves real recluse registration alternatives and fixed numeric context`() {
        val scenario = corpus.scenarios.single { it.presetId == "TB2_8_012" }

        assertEquals(RoleId("Investigator"), scenario.activeRole)
        assertTrue(
            scenario.candidates.any { candidate ->
                candidate.truthBasis == Sde2D5FHealthyPairTruthBasis.RECLUSE_REGISTRATION
            },
        )
        assertEquals(
            setOf(RoleId("Chef"), RoleId("Empath")),
            scenario.fixedNumericContext.mapTo(linkedSetOf()) { context -> context.sourceRole },
        )
        assertTrue(
            scenario.fixedNumericContext.all { context ->
                context.fixedValue in context.legalHealthyValues
            },
        )
    }

    @Test
    fun `librarian sample exposes multiple same setup legal choices rather than a fixed no outsider result`() {
        val scenario = corpus.scenarios.single { it.presetId == "TB2_7_037" }

        assertEquals(RoleId("Librarian"), scenario.activeRole)
        assertTrue(scenario.candidates.size > 1)
        assertTrue(scenario.candidates.all { candidate -> candidate.shownRole != null })
        assertTrue(scenario.candidates.all { candidate -> candidate.candidateSeats.size == 2 })
    }

    @Test
    fun `renderer exposes table meaning before strategic diagnostics or review membership`() {
        val report = Sde2D5FRepresentativeHealthyInformationCorpusRenderer.renderMarkdown(corpus)

        assertTrue(report.contains("### Table seating"))
        assertTrue(report.contains("### Fixed Chef / Empath context"))
        assertTrue(report.contains("### Legal Storyteller options"))
        assertTrue(report.contains("TB2_8_012"))
        assertTrue(report.contains("truth basis=RECLUSE_REGISTRATION"))
        assertTrue(report.contains("Evil-player-controlled; not optimized by this corpus"))
        assertFalse(report.contains("rawWorld"))
        assertFalse(report.contains("REVIEWABLE"))

        val reportFile = File("build/reports/sde-2d5f-representative-healthy-information.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
}
