package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerEventRevisionPolicyTest {
    @Test
    fun `global confirmed facts and observations are not revisioned again by narration`() {
        val global = ClocktowerSemanticHistoryMode.GLOBAL_V1
        listOf(
            ClocktowerEventType.Nomination,
            ClocktowerEventType.Vote,
            ClocktowerEventType.Information,
            ClocktowerEventType.UnreliableInformation,
        ).forEach { type ->
            assertFalse("$type must not commit another revision", clocktowerNarrationNeedsGameRevision(type, global))
        }
        assertFalse(clocktowerNarrationNeedsGameRevision(
            ClocktowerEventType.RoleAction, global, confirmedRoleNarrationOnly = true,
        ))
        // Unmigrated night confirmations cannot lose their only revision owner.
        assertTrue(clocktowerNarrationNeedsGameRevision(
            ClocktowerEventType.RoleAction, global, confirmedRoleNarrationOnly = false,
        ))
        listOf(
            ClocktowerEventType.Phase,
            ClocktowerEventType.Execution,
            ClocktowerEventType.Death,
            ClocktowerEventType.RoleChange,
        ).forEach { type ->
            assertTrue("$type is not known to be separately confirmed by this callback",
                clocktowerNarrationNeedsGameRevision(type, global))
        }
    }

    @Test
    fun `legacy-local role-action callbacks retain their previous invalidation behavior`() {
        val legacy = ClocktowerSemanticHistoryMode.LEGACY_LOCAL
        assertTrue(clocktowerNarrationNeedsGameRevision(
            ClocktowerEventType.RoleAction, legacy, confirmedRoleNarrationOnly = true,
        ))
        assertTrue(clocktowerNarrationNeedsGameRevision(ClocktowerEventType.Nomination, legacy))
        assertTrue(clocktowerNarrationNeedsGameRevision(ClocktowerEventType.Vote, legacy))
        assertFalse(clocktowerNarrationNeedsGameRevision(ClocktowerEventType.Information, legacy))
        assertFalse(clocktowerNarrationNeedsGameRevision(ClocktowerEventType.UnreliableInformation, legacy))
    }
}
