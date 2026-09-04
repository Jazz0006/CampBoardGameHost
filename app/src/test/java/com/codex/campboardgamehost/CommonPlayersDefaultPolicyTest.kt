package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class CommonPlayersDefaultPolicyTest {
    @Test
    fun `fresh install gets eight ordinary default players`() {
        assertEquals(
            listOf("Alice", "Bob", "Carol", "David", "Emma", "Frank", "Grace", "Henry"),
            resolveInitialCommonPlayers(
                hasStoredPlayers = false,
                storedPlayers = emptyList(),
            ),
        )
    }

    @Test
    fun `saved empty list remains empty`() {
        assertEquals(
            emptyList<String>(),
            resolveInitialCommonPlayers(
                hasStoredPlayers = true,
                storedPlayers = emptyList(),
            ),
        )
    }

    @Test
    fun `saved custom players remain authoritative`() {
        val stored = listOf("Jun", "Amy", "Leo")
        assertEquals(
            stored,
            resolveInitialCommonPlayers(
                hasStoredPlayers = true,
                storedPlayers = stored,
            ),
        )
    }
}
