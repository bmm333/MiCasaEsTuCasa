package com.mobile.micasaestucasa.domain.usecase.chat

import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.experimental.theories.suppliers.TestedOn

class GetOrCreateConversationUseCaseTest {
    private lateinit var chatRepo: ChatRepo
    private lateinit var useCase: GetOrCreateConversationUseCase

    private val mockConversation = Conversation(
        id ="conv1",
        hostId ="host1",
        renterId ="renter1",
        propertyId ="prop1"
    )
    @Before
    fun setUp()
    {
        chatRepo=mockk()
        useCase=GetOrCreateConversationUseCase(chatRepo)
    }
    @Test
    fun `Exisiting conversation returns correctly`()=runTest{
        coEvery {
            chatRepo.getOrCreateConversation("host1","renter1","prop1")
        }returns Result.success(mockConversation)
        val result=useCase("host1","renter1","prop1")
        assertTrue(result.isSuccess)
        assertEquals("conv1", result.getOrNull()?.id)
    }
    @Test
    fun `Idempotency - same result on each call`()=runTest {
        coEvery {
            chatRepo.getOrCreateConversation("host1","renter1","prop1")
        }returns Result.success(mockConversation)
        val result1=useCase("host1","renter1","prop1")
        val result2=useCase("host1","renter1","prop1")
        val result3=useCase("host1","renter1","prop1")
        assertTrue(result1.isSuccess && result2.isSuccess && result3.isSuccess)
        assertEquals(result1.getOrNull()?.id,result2.getOrNull()?.id)
        assertEquals(result2.getOrNull()?.id,result3.getOrNull()?.id)
        //repo is called thrice (haha) idempotency check is a repo responsability not usecase
        coVerify(exactly = 3) { chatRepo.getOrCreateConversation(any(),any(),any()) }
    }
    @Test
    fun `hostid empty`()=runTest{
        val result=useCase("","renter1","prop1")

        assertTrue(result.isFailure)
        assertEquals("Parametri non validi",result.exceptionOrNull()?.message)
        coVerify(inverse=true) { chatRepo.getOrCreateConversation(
            any(),any(),any()
        ) }
    }
    @Test
    fun `empty propetrty id` () = runTest{
        val result=useCase("host1","renter1","")
        assertTrue(result.isFailure)
        assertEquals("Parametri non validi",result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { chatRepo.getOrCreateConversation(any(),any(),any()) }
    }

    @Test
    fun `Business Rule hostid cannot be renterid`()=runTest {
        val result=useCase("user1","user1","prop1")
        assertTrue(result.isFailure)
        assertEquals("host e renter non possono essere uguali",result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { chatRepo.getOrCreateConversation(any(),any(),any())}
    }
    @Test
    fun `Firestore propogates error correctly` () = runTest {
        coEvery { chatRepo.getOrCreateConversation(any(),any(),any())
        }returns Result.failure(Exception("Firestore unavailable"))
        val result=useCase("host1","renter1","prop1")
        assertTrue(result.isFailure)
        assertEquals("Firestore unavailable",result.exceptionOrNull()?.message)
    }
}