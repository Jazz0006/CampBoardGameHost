package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WerewolfPresentationOwnershipTest {
    private fun findRepositoryRoot(): File {
        val knownHostSource = "app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt"
        val workingDirectory = System.getProperty("user.dir") ?: error("Working directory is unavailable")
        var directory = File(workingDirectory).absoluteFile
        while (true) {
            if (File(directory, knownHostSource).isFile) return directory
            val parent = directory.parentFile ?: error("Repository root not found from ${directory.path}")
            if (parent == directory) error("Repository root not found from ${directory.path}")
            directory = parent
        }
    }

    private val repoRoot = findRepositoryRoot()
    private val root = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    )
    private val werewolfHost = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfHostScreen.kt",
    )

    @Test
    fun `Werewolf presentation leftovers have Werewolf host owner`() {
        assertTrue("CampBoardGameHostApp.kt must exist", root.isFile)
        assertTrue("WerewolfHostScreen.kt must exist", werewolfHost.isFile)

        val rootText = root.readText()
        val werewolfHostText = werewolfHost.readText()

        listOf("WerewolfRoleLine", "WerewolfPlayerStatusRow").forEach { declaration ->
            assertTrue(
                "$declaration must be declared in WerewolfHostScreen.kt",
                werewolfHostText.contains("internal fun $declaration("),
            )
            assertFalse(
                "$declaration must no longer be declared in CampBoardGameHostApp.kt",
                rootText.contains("internal fun $declaration("),
            )
        }
    }

    @Test
    fun `Werewolf host retains role and player status consumers`() {
        val werewolfHostText = werewolfHost.readText()

        listOf(
            "WerewolfRoleLine(roleName = stringResource(R.string.role_werewolf)",
            "WerewolfRoleLine(roleName = stringResource(R.string.role_seer)",
            "WerewolfRoleLine(roleName = stringResource(R.string.role_witch)",
            "WerewolfRoleLine(roleName = stringResource(R.string.role_hunter)",
            "WerewolfPlayerStatusRow(card)",
        ).forEach { consumer ->
            assertTrue("WerewolfHostScreen.kt must retain $consumer", werewolfHostText.contains(consumer))
        }
    }
}
