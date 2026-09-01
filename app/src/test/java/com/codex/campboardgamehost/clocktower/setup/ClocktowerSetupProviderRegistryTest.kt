package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSetupProviderRegistryTest {
    @Test
    fun `candidate snapshots and canonicalizes pre-seat actual-role multiset`() {
        val callerRoles = mutableListOf(RoleId("Imp"), RoleId("Chef"), RoleId("Chef"))

        val candidate = SetupCandidate(
            script = ScriptId("trouble_brewing"),
            actualRoles = callerRoles,
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.TEMPLATE,
                providerId = "tb-presets-v2",
                candidateId = "tb-3-example",
            ),
        )
        callerRoles.clear()

        assertEquals(3, candidate.playerCount)
        assertEquals(
            listOf(RoleId("Chef"), RoleId("Chef"), RoleId("Imp")),
            candidate.actualRoles,
        )
    }

    @Test
    fun `provider returns matching script player-count and provider candidates`() {
        val provider = provider(
            script = "trouble_brewing",
            providerId = "tb-presets-v2",
        ) { request ->
            listOf(
                candidate(
                    script = request.script.value,
                    providerId = "tb-presets-v2",
                    roles = List(request.playerCount) { index -> "role-$index" },
                ),
            )
        }

        val result = provider.candidates(
            SetupCandidateRequest(
                script = ScriptId("trouble_brewing"),
                playerCount = 5,
                setupSeed = 42L,
            ),
        )

        assertEquals(1, result.size)
        assertEquals(5, result.single().playerCount)
        assertEquals("tb-presets-v2", result.single().provenance.providerId)
    }

    @Test
    fun `provider rejects a request for another script before invoking its source`() {
        var invoked = false
        val provider = provider(
            script = "trouble_brewing",
            providerId = "tb-presets-v2",
        ) {
            invoked = true
            emptyList()
        }

        assertThrows(IllegalArgumentException::class.java) {
            provider.candidates(
                SetupCandidateRequest(
                    script = ScriptId("no_greater_joy"),
                    playerCount = 5,
                    setupSeed = 1L,
                ),
            )
        }
        assertFalse(invoked)
    }

    @Test
    fun `provider rejects source candidates with mismatched player count`() {
        val provider = provider(
            script = "trouble_brewing",
            providerId = "tb-presets-v2",
        ) {
            listOf(
                candidate(
                    script = "trouble_brewing",
                    providerId = "tb-presets-v2",
                    roles = listOf("chef", "imp"),
                ),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            provider.candidates(
                SetupCandidateRequest(
                    script = ScriptId("trouble_brewing"),
                    playerCount = 5,
                    setupSeed = 1L,
                ),
            )
        }
    }

    @Test
    fun `provider rejects source candidates attributed to another provider`() {
        val provider = provider(
            script = "trouble_brewing",
            providerId = "tb-presets-v2",
        ) {
            listOf(
                candidate(
                    script = "trouble_brewing",
                    providerId = "other-provider",
                    roles = listOf("chef", "empath", "fortune_teller", "poisoner", "imp"),
                ),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            provider.candidates(
                SetupCandidateRequest(
                    script = ScriptId("trouble_brewing"),
                    playerCount = 5,
                    setupSeed = 1L,
                ),
            )
        }
    }

    @Test
    fun `registry resolves registered provider and leaves unregistered script explicit`() {
        val tb = provider("trouble_brewing", "tb-provider") { emptyList() }
        val ngj = provider("no_greater_joy", "generated-provider") { emptyList() }
        val registry = ClocktowerSetupProviderRegistry(listOf(tb, ngj))

        assertTrue(registry.find(ScriptId("trouble_brewing")) === tb)
        assertTrue(registry.find(ScriptId("no_greater_joy")) === ngj)
        assertNull(registry.find(ScriptId("future_script")))
    }

    @Test
    fun `registry rejects duplicate providers for one script`() {
        val first = provider("trouble_brewing", "provider-a") { emptyList() }
        val second = provider("trouble_brewing", "provider-b") { emptyList() }

        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerSetupProviderRegistry(listOf(first, second))
        }
    }

    @Test
    fun `request rejects non-positive player count`() {
        assertThrows(IllegalArgumentException::class.java) {
            SetupCandidateRequest(
                script = ScriptId("trouble_brewing"),
                playerCount = 0,
                setupSeed = 1L,
            )
        }
    }

    private fun provider(
        script: String,
        providerId: String,
        source: (SetupCandidateRequest) -> List<SetupCandidate>,
    ): ClocktowerSetupProvider = ClocktowerSetupProvider(
        script = ScriptId(script),
        providerId = providerId,
        candidateSource = SetupCandidateSource(source),
    )

    private fun candidate(
        script: String,
        providerId: String,
        roles: List<String>,
    ): SetupCandidate = SetupCandidate(
        script = ScriptId(script),
        actualRoles = roles.map(::RoleId),
        provenance = SetupProvenance(
            sourceKind = SetupSourceKind.GENERATED,
            providerId = providerId,
        ),
    )
}
