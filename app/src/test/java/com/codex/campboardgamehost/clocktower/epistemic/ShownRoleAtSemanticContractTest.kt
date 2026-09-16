package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class ShownRoleAtSemanticContractTest {
    @Test
    fun `shown role proposition has canonical stable json round trip`() {
        val proposition = InformationProposition.ShownRoleAt(
            seat = 3,
            role = RoleId("Chef"),
        )

        val encoded = EpistemicSemanticJson.encode(proposition)
        val json = JSONObject(encoded)

        assertEquals("shown-role-at", json.getString("kind"))
        assertEquals("Chef", json.getString("role"))
        assertEquals(3, json.getInt("seat"))
        assertEquals(encoded, EpistemicSemanticJson.encode(proposition))
        assertEquals(proposition, EpistemicSemanticJson.decodeInformationProposition(encoded))
    }
}
