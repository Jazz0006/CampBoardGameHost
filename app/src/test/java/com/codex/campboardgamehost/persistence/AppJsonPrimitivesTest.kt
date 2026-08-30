package com.codex.campboardgamehost

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppJsonPrimitivesTest {
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
