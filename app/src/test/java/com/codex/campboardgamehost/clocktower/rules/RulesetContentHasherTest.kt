package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RulesetContentHasherTest {
    private val knowledge by lazy {
        RulesetJsonLoader.parse(
            File("src/main/assets/rules/trouble_brewing.json").readText(Charsets.UTF_8),
        )
    }

    @Test
    fun `trouble brewing resource contains the complete script and corrected investigator text`() {
        assertEquals(22, knowledge.characters.size)
        assertEquals(
            "You start knowing that 1 of 2 players is a particular Minion.",
            knowledge.characters.single { it.roleId == RoleId("Investigator") }.abilityText,
        )
        assertTrue(knowledge.jinxes.isEmpty())
    }

    @Test
    fun `canonical hash is independent of input character ordering`() {
        val inPlay = setOf(RoleId("Imp"), RoleId("Chef"), RoleId("Poisoner"))
        val original = RulesetContentHasher.hash(knowledge, inPlay)
        val reordered = RulesetContentHasher.hash(
            knowledge.copy(characters = knowledge.characters.reversed()),
            inPlay,
        )

        assertEquals(original, reordered)
        assertTrue(original.matches(Regex("[0-9a-f]{32}")))
    }

    @Test
    fun `character text changes the content hash`() {
        val inPlay = setOf(RoleId("Investigator"), RoleId("Spy"), RoleId("Imp"))
        val original = RulesetContentHasher.hash(knowledge, inPlay)
        val modified = knowledge.copy(
            characters = knowledge.characters.map { character ->
                if (character.roleId == RoleId("Investigator")) {
                    character.copy(abilityText = character.abilityText + " changed")
                } else {
                    character
                }
            },
        )

        assertNotEquals(original, RulesetContentHasher.hash(modified, inPlay))
    }

    @Test
    fun `fixed trouble brewing setup has a golden content hash`() {
        val inPlay = setOf(
            "Chef",
            "Empath",
            "Fortune Teller",
            "Undertaker",
            "Virgin",
            "Drunk",
            "Scarlet Woman",
            "Imp",
        ).map(::RoleId).toSet()

        assertEquals(
            "e12f6425ece137da02477a642235c797",
            RulesetContentHasher.hash(knowledge, inPlay),
        )
    }
}
