package com.mobile.micasaestucasa.data.repository.property

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.mobile.micasaestucasa.data.dto.property.PropertyDto
import com.mobile.micasaestucasa.domain.model.property.Property
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirebasePropertyRepoTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var repo: FirebasePropertyRepo

    @Before
    fun setUp() {
        firestore = mockk()
        collection = mockk()
        every { firestore.collection("properties") } returns collection
        repo = FirebasePropertyRepo(firestore)
    }

    @Test
    fun `createProperty con dati validi ritorna id del documento`() = runTest {
        val document = mockk<DocumentReference>()
        val property = sampleProperty(id = "")

        every { collection.document() } returns document
        every { document.id } returns "new-id"
        every { document.set(any<PropertyDto>()) } returns Tasks.forResult(null)

        val result = repo.createProperty(property)

        assertTrue(result.isSuccess)
        assertEquals("new-id", result.getOrNull())
        verify(exactly = 1) {
            document.set(
                match<PropertyDto> { dto ->
                    dto.id == "new-id" && dto.title == property.title && dto.ownerId == property.ownerId
                }
            )
        }
    }

    @Test
    fun `createProperty propaga errore firestore`() = runTest {
        val document = mockk<DocumentReference>()
        val property = sampleProperty(id = "")

        every { collection.document() } returns document
        every { document.id } returns "new-id"
        every { document.set(any<PropertyDto>()) } returns Tasks.forException(RuntimeException("write failed"))

        val result = repo.createProperty(property)

        assertTrue(result.isFailure)
        assertEquals("write failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getPropertiesByOwner ritorna solo documenti convertibili`() = runTest {
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()

        every { collection.whereEqualTo("ownerId", "owner-1") } returns query
        every { query.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(doc1, doc2)
        every { doc1.toObject(PropertyDto::class.java) } returns sampleDto(id = "p1")
        every { doc2.toObject(PropertyDto::class.java) } returns null

        val result = repo.getPropertiesByOwner("owner-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("p1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun `getPropertiesByOwner propaga errore del task`() = runTest {
        val query = mockk<Query>()

        every { collection.whereEqualTo("ownerId", "owner-1") } returns query
        every { query.get() } returns Tasks.forException(IllegalStateException("offline"))

        val result = repo.getPropertiesByOwner("owner-1")

        assertTrue(result.isFailure)
        assertEquals("offline", result.exceptionOrNull()?.message)
    }

    @Test
    fun `searchProperties applica filtro per disponibilita e keywords`() = runTest {
        val cityQuery = mockk<Query>()
        val capacityQuery = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val docA = mockk<DocumentSnapshot>()
        val docB = mockk<DocumentSnapshot>()
        val docC = mockk<DocumentSnapshot>()

        every { collection.whereEqualTo("city", "Torino") } returns cityQuery
        every { cityQuery.whereGreaterThanOrEqualTo("capacity", 2) } returns capacityQuery
        every { capacityQuery.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns listOf(docA, docB, docC)

        every { docA.toObject(PropertyDto::class.java) } returns sampleDto(
            id = "ok",
            keywords = listOf("wifi", "balcone"),
            availableFrom = "2026-07-01",
            availableTo = "2026-07-31"
        )
        every { docB.toObject(PropertyDto::class.java) } returns sampleDto(
            id = "out-date",
            keywords = listOf("wifi"),
            availableFrom = "2026-08-01",
            availableTo = "2026-08-31"
        )
        every { docC.toObject(PropertyDto::class.java) } returns sampleDto(
            id = "no-keyword",
            keywords = listOf("garage"),
            availableFrom = "2026-07-01",
            availableTo = "2026-07-31"
        )

        val result = repo.searchProperties(
            city = "Torino",
            startDate = "2026-07-10",
            endDate = "2026-07-12",
            capacity = 2,
            keywords = listOf("wifi")
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("ok", result.getOrNull()?.first()?.id)
    }

    @Test
    fun `searchProperties con data richiesta non valida ritorna failure`() = runTest {
        val cityQuery = mockk<Query>()
        val capacityQuery = mockk<Query>()

        every { collection.whereEqualTo("city", "Torino") } returns cityQuery
        every { cityQuery.whereGreaterThanOrEqualTo("capacity", 2) } returns capacityQuery
        every { capacityQuery.get() } returns Tasks.forResult(mockk<QuerySnapshot>(relaxed = true))

        val result = repo.searchProperties(
            city = "Torino",
            startDate = "data-non-valida",
            endDate = "2026-07-12",
            capacity = 2,
            keywords = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPropertyById con dto nullo ritorna errore proprieta non trovata`() = runTest {
        val documentRef = mockk<DocumentReference>()
        val document = mockk<DocumentSnapshot>()

        every { collection.document("missing") } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(document)
        every { document.toObject(PropertyDto::class.java) } returns null

        val result = repo.getPropertyById("missing")

        assertTrue(result.isFailure)
        assertEquals("Proprietà non trovata", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getPropertyById con dto valido ritorna property`() = runTest {
        val documentRef = mockk<DocumentReference>()
        val document = mockk<DocumentSnapshot>()

        every { collection.document("p1") } returns documentRef
        every { documentRef.get() } returns Tasks.forResult(document)
        every { document.toObject(PropertyDto::class.java) } returns sampleDto(id = "p1")

        val result = repo.getPropertyById("p1")

        assertTrue(result.isSuccess)
        assertEquals("p1", result.getOrNull()?.id)
    }

    private fun sampleProperty(id: String): Property {
        return Property(
            id = id,
            ownerId = "owner-1",
            title = "Casa Torino",
            description = "desc",
            latitude = 45.0,
            longitude = 7.0,
            city = "Torino",
            pricePerDay = 100.0,
            capacity = 2,
            keywords = listOf("wifi"),
            imageUrls = emptyList(),
            availableFrom = "2026-07-01",
            availableTo = "2026-07-31"
        )
    }

    private fun sampleDto(
        id: String,
        keywords: List<String> = listOf("wifi"),
        availableFrom: String = "2026-07-01",
        availableTo: String = "2026-07-31"
    ): PropertyDto {
        return PropertyDto(
            id = id,
            ownerId = "owner-1",
            title = "Casa",
            description = "desc",
            latitude = 45.0,
            longitude = 7.0,
            city = "Torino",
            pricePerDay = 100.0,
            capacity = 2,
            keywords = keywords,
            imageUrls = emptyList(),
            availableFrom = availableFrom,
            availableTo = availableTo,
            rating = 0.0,
            reviewsCount = 0
        )
    }
}
