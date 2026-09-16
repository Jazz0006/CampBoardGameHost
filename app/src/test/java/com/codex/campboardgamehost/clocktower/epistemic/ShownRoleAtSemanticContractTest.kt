package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
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

        assertEquals("{\"kind\":\"shown-role-at\",\"role\":\"Chef\",\"seat\":3}", encoded)
        assertEquals(proposition, EpistemicSemanticJson.decodeInformationProposition(encoded))
    }
}
