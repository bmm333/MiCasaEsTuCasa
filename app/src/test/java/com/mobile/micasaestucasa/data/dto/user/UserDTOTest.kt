package com.mobile.micasaestucasa.data.dto.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UserDTOTest {

    @Test
    fun `copy e component mantengono i valori attesi`() {
        val dto = UserDTO(
            id = "u1",
            name = "Mario",
            email = "mario@test.com",
            roles = listOf("GUEST")
        )

        val copy = dto.copy(name = "Luigi")

        assertEquals("u1", dto.id)
        assertEquals("Mario", dto.name)
        assertEquals("mario@test.com", dto.email)
        assertEquals(listOf("GUEST"), dto.roles)
        assertEquals("Luigi", copy.name)
        assertNotEquals(dto, copy)
    }
}
