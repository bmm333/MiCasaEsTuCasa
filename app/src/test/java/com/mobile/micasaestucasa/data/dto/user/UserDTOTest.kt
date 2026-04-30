package com.mobile.micasaestucasa.data.dto.user

import UserDTO
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

        assertEquals("u1", dto.component1())
        assertEquals("Mario", dto.component2())
        assertEquals("mario@test.com", dto.component3())
        assertEquals(listOf("GUEST"), dto.component4())
        assertEquals("Luigi", copy.name)
        assertNotEquals(dto, copy)
    }
}
