package com.mobile.micasaestucasa.data.mapper.user

import com.mobile.micasaestucasa.data.dto.user.UserDTO
import com.mobile.micasaestucasa.domain.model.user.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class UserMapperTest {

    @Test
    fun `toDomain con dto nullo applica default sicuri`() {
        val dto = UserDTO()

        val result = dto.toDomain()

        assertEquals("", result.id)
        assertEquals("", result.name)
        assertEquals("", result.email)
        assertEquals(listOf(UserRole.GUEST), result.roles)
    }

    @Test
    fun `toDomain ignora ruoli non validi e mantiene quelli riconosciuti`() {
        val dto = UserDTO(
            id = "u1",
            name = "Mario",
            email = "mario@test.com",
            roles = listOf("guest", "INVALID", "ADMIN")
        )

        val result = dto.toDomain()

        assertEquals("u1", result.id)
        assertEquals(listOf(UserRole.GUEST, UserRole.ADMIN), result.roles)
    }

    @Test
    fun `toDomain con lista ruoli tutta invalida produce lista vuota`() {
        val dto = UserDTO(roles = listOf("UNKNOWN"))

        val result = dto.toDomain()

        assertEquals(emptyList<UserRole>(), result.roles)
    }
}
