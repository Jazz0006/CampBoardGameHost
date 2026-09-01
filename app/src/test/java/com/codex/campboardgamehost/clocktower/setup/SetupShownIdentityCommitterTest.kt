package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SetupShownIdentityCommitterTest {
    @Test
    fun `same candidate policy and setup seed commit the same legal shown identity`() {
        val candidate = candidate(
            actualRoles = listOf("Drunk", "Imp", "Poisoner", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Tf6", "Tf4", "Tf5"),
        )
        val committer = SetupShownIdentityCommitter()

        val first = committer.commit(candidate, policy, setupSeed = 20260831L)
        val second = committer.commit(candidate, policy, setupSeed = 20260831L)

        assertEquals(first, second)
        assertEquals(1, first.overrides.size)
        assertEquals(RoleId("Drunk"), first.overrides.single().actualRole)
        assertTrue(first.overrides.single().shownRole in policy.overrides.single().legalShownRoles)
        assertEquals(first.overrides.single().shownRole, first.shownRoleFor(RoleId("Drunk")))
        assertEquals(RoleId("Tf1"), first.shownRoleFor(RoleId("Tf1")))
    }

    @Test
    fun `candidate and option ordering do not affect canonical commitment`() {
        val firstCandidate = candidate(
            actualRoles = listOf("Tf3", "Drunk", "Imp", "Tf1", "Poisoner", "Tf2"),
        )
        val secondCandidate = candidate(
            actualRoles = listOf("Poisoner", "Tf2", "Tf1", "Imp", "Drunk", "Tf3"),
        )
        val firstPolicy = policy(
            override("Drunk", "Tf6", "Tf4", "Tf5"),
        )
        val secondPolicy = policy(
            override("Drunk", "Tf5", "Tf6", "Tf4"),
        )
        val committer = SetupShownIdentityCommitter()

        assertEquals(
            committer.commit(firstCandidate, firstPolicy, setupSeed = 771L),
            committer.commit(secondCandidate, secondPolicy, setupSeed = 771L),
        )
    }

    @Test
    fun `different setup seeds explore more than one legal shown identity when options exist`() {
        val candidate = candidate(
            actualRoles = listOf("Drunk", "Imp", "Poisoner", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Tf4", "Tf5", "Tf6"),
        )
        val committer = SetupShownIdentityCommitter()

        val selected = (0L until 128L).map { seed ->
            committer.commit(candidate, policy, setupSeed = seed).overrides.single().shownRole
        }.toSet()

        assertTrue(selected.size > 1)
        assertTrue(selected.all { it in policy.overrides.single().legalShownRoles })
    }

    @Test
    fun `single legal option commits directly without special fallback`() {
        val candidate = candidate(
            actualRoles = listOf("Drunk", "Imp", "Poisoner", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Tf4"),
        )

        val committed = SetupShownIdentityCommitter().commit(
            candidate = candidate,
            policy = policy,
            setupSeed = Long.MIN_VALUE,
        )

        assertEquals(RoleId("Tf4"), committed.shownRoleFor(RoleId("Drunk")))
    }

    @Test
    fun `explicit no override policy commits no replacement and preserves actual identities`() {
        val candidate = candidate(
            actualRoles = listOf("Imp", "Poisoner", "Saint", "Tf1", "Tf2", "Tf3"),
        )

        val committed = SetupShownIdentityCommitter().commit(
            candidate = candidate,
            policy = SetupShownIdentityPolicy.NO_OVERRIDE,
            setupSeed = 99L,
        )

        assertTrue(committed.overrides.isEmpty())
        candidate.actualRoles.forEach { actualRole ->
            assertEquals(actualRole, committed.shownRoleFor(actualRole))
        }
    }

    @Test
    fun `multiple override facts are canonical and independently deterministic`() {
        val candidate = candidate(
            actualRoles = listOf("AliasB", "Tf1", "AliasA", "Imp"),
        )
        val firstPolicy = policy(
            override("AliasB", "ShownB2", "ShownB1"),
            override("AliasA", "ShownA2", "ShownA1"),
        )
        val secondPolicy = policy(
            override("AliasA", "ShownA1", "ShownA2"),
            override("AliasB", "ShownB1", "ShownB2"),
        )
        val committer = SetupShownIdentityCommitter()

        val first = committer.commit(candidate, firstPolicy, setupSeed = 4321L)
        val second = committer.commit(candidate, secondPolicy, setupSeed = 4321L)

        assertEquals(first, second)
        assertEquals(listOf(RoleId("AliasA"), RoleId("AliasB")), first.overrides.map { it.actualRole })
        assertTrue(first.shownRoleFor(RoleId("AliasA")) in firstPolicy.overrides.first { it.actualRole == RoleId("AliasA") }.legalShownRoles)
        assertTrue(first.shownRoleFor(RoleId("AliasB")) in firstPolicy.overrides.first { it.actualRole == RoleId("AliasB") }.legalShownRoles)
    }

    @Test
    fun `commitment does not mutate candidate or legal policy`() {
        val candidate = candidate(
            actualRoles = listOf("Drunk", "Imp", "Poisoner", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Tf6", "Tf4", "Tf5"),
        )
        val rolesBefore = candidate.actualRoles.toList()
        val optionsBefore = policy.overrides.single().legalShownRoles.toList()

        SetupShownIdentityCommitter().commit(candidate, policy, setupSeed = 123L)

        assertEquals(rolesBefore, candidate.actualRoles)
        assertEquals(optionsBefore, policy.overrides.single().legalShownRoles)
    }

    @Test
    fun `override for a role absent from selected candidate fails closed`() {
        val candidate = candidate(
            actualRoles = listOf("Imp", "Poisoner", "Saint", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Tf4", "Tf5"),
        )

        expectIllegalArgument {
            SetupShownIdentityCommitter().commit(candidate, policy, setupSeed = 1L)
        }
    }

    @Test
    fun `shown option already present as an actual role fails closed`() {
        val candidate = candidate(
            actualRoles = listOf("Drunk", "Imp", "Poisoner", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Tf1", "Tf4"),
        )

        expectIllegalArgument {
            SetupShownIdentityCommitter().commit(candidate, policy, setupSeed = 1L)
        }
    }

    @Test
    fun `shown option equal to override actual role fails closed`() {
        val candidate = candidate(
            actualRoles = listOf("Drunk", "Imp", "Poisoner", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Drunk", "Tf4"),
        )

        expectIllegalArgument {
            SetupShownIdentityCommitter().commit(candidate, policy, setupSeed = 1L)
        }
    }

    @Test
    fun `seed participates in commitment rather than being ignored`() {
        val candidate = candidate(
            actualRoles = listOf("Drunk", "Imp", "Poisoner", "Tf1", "Tf2", "Tf3"),
        )
        val policy = policy(
            override("Drunk", "Tf4", "Tf5", "Tf6", "Tf7"),
        )
        val committer = SetupShownIdentityCommitter()

        val commitments = (0L until 256L).map { seed ->
            committer.commit(candidate, policy, setupSeed = seed)
        }.distinct()

        assertTrue(commitments.size > 1)
        assertNotEquals(
            emptySet<RoleId>(),
            commitments.flatMap { it.overrides }.map { it.shownRole }.toSet(),
        )
    }

    private fun candidate(
        actualRoles: List<String>,
        sourceKind: SetupSourceKind = SetupSourceKind.GENERATED,
        providerId: String = "generated-test-v1",
        candidateId: String? = null,
    ): SetupCandidate = SetupCandidate(
        script = SCRIPT_ID,
        actualRoles = actualRoles.map(::RoleId),
        provenance = SetupProvenance(
            sourceKind = sourceKind,
            providerId = providerId,
            candidateId = candidateId,
        ),
    )

    private fun policy(vararg overrides: ShownIdentityOverrideOptions): SetupShownIdentityPolicy =
        SetupShownIdentityPolicy(overrides.toList())

    private fun override(
        actualRole: String,
        vararg legalShownRoles: String,
    ): ShownIdentityOverrideOptions = ShownIdentityOverrideOptions(
        actualRole = RoleId(actualRole),
        legalShownRoles = legalShownRoles.map(::RoleId),
    )

    private fun expectIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected fail-closed contract.
        }
    }

    private companion object {
        val SCRIPT_ID = ScriptId("s6b_test_script")
    }
}
