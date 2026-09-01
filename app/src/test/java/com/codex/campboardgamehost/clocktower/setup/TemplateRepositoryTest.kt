package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateRepositoryTest {
    @Test
    fun `exact bucket lookup returns template candidates in canonical identity order`() {
        val script = ScriptId("trouble_brewing")
        val repository = TemplateRepository(
            buckets = mapOf(
                TemplateBucketKey(script, 5) to listOf(
                    candidate(script, 5, provider = "tb-presets", id = "tb-5-002"),
                    candidate(script, 5, provider = "tb-presets", id = "tb-5-001"),
                ),
                TemplateBucketKey(script, 6) to listOf(
                    candidate(script, 6, provider = "tb-presets", id = "tb-6-001"),
                ),
            ),
        )

        assertEquals(
            listOf("tb-5-001", "tb-5-002"),
            repository.find(script, 5).map { it.provenance.candidateId },
        )
    }

    @Test
    fun `absent script or player count returns empty candidates`() {
        val script = ScriptId("trouble_brewing")
        val repository = TemplateRepository(
            buckets = mapOf(
                TemplateBucketKey(script, 5) to listOf(
                    candidate(script, 5, provider = "tb-presets", id = "tb-5-001"),
                ),
            ),
        )

        assertTrue(repository.find(script, 6).isEmpty())
        assertTrue(repository.find(ScriptId("no_greater_joy"), 5).isEmpty())
    }

    @Test
    fun `repository is a setup candidate source and template lookup ignores setup seed`() {
        val script = ScriptId("trouble_brewing")
        val expected = candidate(script, 5, provider = "tb-presets", id = "tb-5-001")
        val source: SetupCandidateSource = TemplateRepository(
            buckets = mapOf(TemplateBucketKey(script, 5) to listOf(expected)),
        )

        val first = source.candidates(SetupCandidateRequest(script, playerCount = 5, setupSeed = 1L))
        val second = source.candidates(SetupCandidateRequest(script, playerCount = 5, setupSeed = 999L))

        assertEquals(listOf(expected), first)
        assertEquals(first, second)
    }

    @Test
    fun `repository snapshots caller bucket collections`() {
        val script = ScriptId("trouble_brewing")
        val key = TemplateBucketKey(script, 5)
        val mutableCandidates = mutableListOf(
            candidate(script, 5, provider = "tb-presets", id = "tb-5-001"),
        )
        val mutableBuckets = mutableMapOf(key to mutableCandidates)
        val repository = TemplateRepository(mutableBuckets)

        mutableCandidates.clear()
        mutableBuckets.clear()

        assertEquals(listOf("tb-5-001"), repository.find(script, 5).map { it.provenance.candidateId })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cross script candidate in a bucket is rejected`() {
        TemplateRepository(
            buckets = mapOf(
                TemplateBucketKey(ScriptId("trouble_brewing"), 5) to listOf(
                    candidate(
                        script = ScriptId("no_greater_joy"),
                        playerCount = 5,
                        provider = "templates",
                        id = "ngj-5-001",
                    ),
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cross count candidate in a bucket is rejected`() {
        val script = ScriptId("trouble_brewing")
        TemplateRepository(
            buckets = mapOf(
                TemplateBucketKey(script, 5) to listOf(
                    candidate(script, 6, provider = "tb-presets", id = "tb-6-001"),
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generated candidate cannot be stored as a template`() {
        val script = ScriptId("no_greater_joy")
        TemplateRepository(
            buckets = mapOf(
                TemplateBucketKey(script, 5) to listOf(
                    SetupCandidate(
                        script = script,
                        actualRoles = roles(5),
                        provenance = SetupProvenance(
                            sourceKind = SetupSourceKind.GENERATED,
                            providerId = "ruleset-generator",
                            candidateId = "generated-1",
                        ),
                    ),
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `template candidate requires durable candidate identity`() {
        val script = ScriptId("trouble_brewing")
        TemplateRepository(
            buckets = mapOf(
                TemplateBucketKey(script, 5) to listOf(
                    SetupCandidate(
                        script = script,
                        actualRoles = roles(5),
                        provenance = SetupProvenance(
                            sourceKind = SetupSourceKind.TEMPLATE,
                            providerId = "tb-presets",
                            candidateId = null,
                        ),
                    ),
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate provider and candidate identity in one bucket is rejected`() {
        val script = ScriptId("trouble_brewing")
        TemplateRepository(
            buckets = mapOf(
                TemplateBucketKey(script, 5) to listOf(
                    candidate(script, 5, provider = "tb-presets", id = "tb-5-001"),
                    candidate(script, 5, provider = "tb-presets", id = "tb-5-001"),
                ),
            ),
        )
    }

    private fun candidate(
        script: ScriptId,
        playerCount: Int,
        provider: String,
        id: String,
    ): SetupCandidate = SetupCandidate(
        script = script,
        actualRoles = roles(playerCount),
        provenance = SetupProvenance(
            sourceKind = SetupSourceKind.TEMPLATE,
            providerId = provider,
            candidateId = id,
        ),
    )

    private fun roles(playerCount: Int): List<RoleId> =
        (1..playerCount).map { RoleId("Role $it") }
}
