package com.mobile.micasaestucasa.domain.model.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NotificationTest {

    @Test
    fun `creazione notifica con tutti i campi corretti`() {
        val notification = Notification(
            type = NotificationType.NEW_BOOKING_REQUEST,
            title = "Nuova prenotazione",
            body = "Hai ricevuto una richiesta di prenotazione",
            targetId = "booking-123"
        )

        assertEquals(NotificationType.NEW_BOOKING_REQUEST, notification.type)
        assertEquals("Nuova prenotazione", notification.title)
        assertEquals("Hai ricevuto una richiesta di prenotazione", notification.body)
        assertEquals("booking-123", notification.targetId)
    }

    @Test
    fun `notifiche uguali hanno stesso hashCode e equals`() {
        val n1 = Notification(NotificationType.NEW_MESSAGE, "Msg", "Body", "chat-1")
        val n2 = Notification(NotificationType.NEW_MESSAGE, "Msg", "Body", "chat-1")

        assertEquals(n1, n2)
        assertEquals(n1.hashCode(), n2.hashCode())
    }

    @Test
    fun `notifiche con tipo diverso non sono uguali`() {
        val n1 = Notification(NotificationType.BOOKING_ACCEPTED, "T", "B", "id")
        val n2 = Notification(NotificationType.BOOKING_REJECTED, "T", "B", "id")

        assertNotEquals(n1, n2)
    }

    @Test
    fun `copy modifica solo il campo specificato`() {
        val original = Notification(NotificationType.NEW_MESSAGE, "Titolo", "Corpo", "id-1")
        val copied = original.copy(targetId = "id-2")

        assertEquals("id-2", copied.targetId)
        assertEquals(original.type, copied.type)
        assertEquals(original.title, copied.title)
        assertEquals(original.body, copied.body)
    }
}

class NotificationTypeTest {

    @Test
    fun `enum contiene tutti e 5 i tipi richiesti`() {
        val types = NotificationType.entries

        assertEquals(5, types.size)
        assertEquals(NotificationType.NEW_BOOKING_REQUEST, types[0])
        assertEquals(NotificationType.NEW_MESSAGE, types[1])
        assertEquals(NotificationType.BOOKING_ACCEPTED, types[2])
        assertEquals(NotificationType.BOOKING_REJECTED, types[3])
        assertEquals(NotificationType.BOOKING_CANCELLED, types[4])
    }

    @Test
    fun `valueOf ritorna il tipo corretto`() {
        assertEquals(NotificationType.NEW_BOOKING_REQUEST, NotificationType.valueOf("NEW_BOOKING_REQUEST"))
        assertEquals(NotificationType.BOOKING_CANCELLED, NotificationType.valueOf("BOOKING_CANCELLED"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `valueOf con nome invalido lancia eccezione`() {
        NotificationType.valueOf("NON_ESISTE")
    }
}
