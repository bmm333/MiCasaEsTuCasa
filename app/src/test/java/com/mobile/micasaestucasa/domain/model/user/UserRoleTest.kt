package com.mobile.micasaestucasa.domain.model.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserRoleTest {

    @Test
    fun `fromString riconosce valori validi anche in lowercase`() {
        val result = UserRole.fromString("guest")

        assertEquals(UserRole.GUEST, result)
    }

    @Test
    fun `fromString ritorna null con valore sconosciuto`() {
        val result = UserRole.fromString("superadmin")

        assertNull(result)
    }
}
