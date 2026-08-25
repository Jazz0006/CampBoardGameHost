package com.codex.campboardgamehost

import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppJsonPrimitivesTest {
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
    private val appJsonPrimitives = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/persistence/AppJsonPrimitives.kt",
    )

    private val helperDeclarations = listOf(
        "private inline fun <reified T : Enum<T>> enumByName(",
        "private fun JSONObject.putNullableString(",
        "private fun JSONObject.putNullableInt(",
        "private fun JSONObject.putNullableBoolean(",
        "private fun JSONObject.optNullableString(",
        "private fun JSONObject.optNullableInt(",
        "private fun JSONObject.optNullableBoolean(",
        "private fun stringsToJsonArray(",
        "private fun JSONArray.toStringList(",
    )

    @Test
    fun `JSON primitive helpers have dedicated owner`() {
        assertTrue("CampBoardGameHostApp.kt must exist", root.isFile)
        assertTrue("AppJsonPrimitives.kt must exist", appJsonPrimitives.isFile)

        val rootText = root.readText()
        val ownerText = appJsonPrimitives.readText()

        helperDeclarations.forEach { declaration ->
            val ownerDeclaration = declaration.replaceFirst("private", "internal")
            assertTrue("AppJsonPrimitives.kt must contain $ownerDeclaration", ownerText.contains(ownerDeclaration))
            assertFalse("CampBoardGameHostApp.kt must not contain $declaration", rootText.contains(declaration))
        }
    }

    @Test
    fun `JSON primitive owner contains no application authority`() {
        assertTrue("AppJsonPrimitives.kt must exist", appJsonPrimitives.isFile)
        val ownerText = appJsonPrimitives.readText()

        listOf(
            "android.content.Context",
            "getSharedPreferences",
            "remember(",
            "mutableStateOf(",
            "LaunchedEffect(",
            "DisposableEffect(",
            "SideEffect(",
            "ClocktowerGameSession",
            "ActiveGamePersistenceCoordinator",
            "PlayerCard",
            "ClocktowerRole",
            "EpistemicSemanticJson",
        ).forEach { forbidden ->
            assertFalse("AppJsonPrimitives.kt must not contain $forbidden", ownerText.contains(forbidden))
        }
    }

    @Test
    fun `enum lookup preserves null blank and unknown behavior`() {
        assertNull(enumByName<GameKind>(null))
        assertNull(enumByName<GameKind>(""))
        assertEquals(GameKind.Clocktower, enumByName<GameKind>("Clocktower"))
        assertNull(enumByName<GameKind>("not-a-game"))
    }

    @Test
    fun `nullable JSON primitives preserve values explicit null and missing keys`() {
        val json = JSONObject()
        json.putNullableString("stringValue", "alpha")
        json.putNullableString("stringNull", null)
        json.putNullableInt("intValue", 7)
        json.putNullableInt("intNull", null)
        json.putNullableBoolean("booleanValue", true)
        json.putNullableBoolean("booleanNull", null)

        assertEquals("alpha", json.optNullableString("stringValue"))
        assertTrue(json.isNull("stringNull"))
        assertNull(json.optNullableString("missingString"))
        assertEquals(7, json.optNullableInt("intValue"))
        assertTrue(json.isNull("intNull"))
        assertNull(json.optNullableInt("missingInt"))
        assertEquals(true, json.optNullableBoolean("booleanValue"))
        assertTrue(json.isNull("booleanNull"))
        assertNull(json.optNullableBoolean("missingBoolean"))
    }

    @Test
    fun `string arrays preserve source values and filter blank values on read`() {
        val json = stringsToJsonArray(listOf("alpha", "", "beta"))

        assertEquals(3, json.length())
        assertEquals("alpha", json.optString(0))
        assertEquals("", json.optString(1))
        assertEquals("beta", json.optString(2))
        assertEquals(listOf("alpha", "beta"), json.toStringList())
    }
}
