package com.mobile.micasaestucasa.domain.usecase.chat

import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


/**
 * Test for SendMessagEUseCAse
 *
 * verifies: Input validation, delegation to repo
 * message handling wiht only images, and error propagation.
 *
 * pero mi sto rendendo conto ora, scrivo i commenti in inglese e i test in italiano... non mi metto a convertire tutto ora, portero tutto da un lato una volta finito pero da adesso in poi sempre un 1 lato
 *
 * */
class SendMessageUseCaseTest {
    private lateinit var chatRepo: ChatRepo
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private val mockMessage= Message(
        id = "msg1",
        conversationId = "conv1",
        senderId = "user1",
        text = "Hello",
        imageUrl = null,
        timestamp = 1000L,
        isRead = false
    )
    @Before
    fun setUp(){
        chatRepo=mockk()
        sendMessageUseCase=SendMessageUseCase(chatRepo)
    }
    //happy tests
    @Test
    fun `messagio testo valido inviato correttamente`()= runTest {
        coEvery {
            chatRepo.sendMessage("conv1","user1","Hello",null)
        }returns  Result.success(mockMessage)
        val result=sendMessageUseCase("conv1","user1","Hello")
        assertTrue(result.isSuccess)
        assertEquals("msg1",result.getOrNull()?.id)
        coVerify(exactly = 1) { chatRepo.sendMessage("conv1","user1","Hello",null) }
    }

    @Test
    fun `messaggio con solo immagine inviato correttamente`() = runTest {
        val imageMessage = mockMessage.copy(text = "", imageUrl = "https://storage.url/img.jpg")
        coEvery {
            chatRepo.sendMessage("conv1", "user1", "", "https://storage.url/img.jpg")
        } returns Result.success(imageMessage)
        val result = sendMessageUseCase(
            conversationId ="conv1",
            senderId = "user1",
            text= "",
            imageUrl = "https://storage.url/img.jpg"
        )
        assertTrue(result.isSuccess)
        assertEquals("https://storage.url/img.jpg", result.getOrNull()?.imageUrl)
    }
    @Test
    fun `messaggio con testo e immagine inviato correttamente`() = runTest {
        val mixedMessage = mockMessage.copy(imageUrl = "https://storage.url/img.jpg")
        coEvery {
            chatRepo.sendMessage(any(), any(), any(), any())
        } returns Result.success(mixedMessage)
        val result = sendMessageUseCase("conv1", "user1", "Guarda qui", "https://storage.url/img.jpg")
        assertTrue(result.isSuccess)
    }

    //validation
    @Test
    fun `conservationId vuoto rituna failue`()=runTest{
        val result=sendMessageUseCase("","user1","Hello")
        assertTrue(result.isFailure)
        assertEquals("ConversationId non valido",result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { chatRepo.sendMessage(any(),any(),any())}
    }
    @Test
    fun `senderId vuoto ritonra failure`()=runTest {
        val result=sendMessageUseCase("conv1","","Hello" )
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { chatRepo.sendMessage(any(), any(), any(), any()) }
    }
    @Test
    fun `testo solo spazi senza immagine ritorna failure`() = runTest {
        val result = sendMessageUseCase("conv1", "user1", "   ", null)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { chatRepo.sendMessage(any(), any(), any(), any()) }
    }
    @Test
    fun `testo vuoto senza immagine ritorna failure`() = runTest {
        val result = sendMessageUseCase("conv1", "user1", "", null)

        assertTrue(result.isFailure)
        assertEquals("Messaggio non può essere vuoto", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { chatRepo.sendMessage(any(), any(), any(), any()) }
    }
    @Test
    fun `Firestore propogates error correctly`() = runTest {
        coEvery {
            chatRepo.sendMessage(any(), any(), any(), any())
        } returns Result.failure(Exception("Firestore unavailable"))
        val result = sendMessageUseCase("conv1", "user1", "Hello")
        assertTrue(result.isFailure)
        assertEquals("Firestore unavailable", result.exceptionOrNull()?.message)
    }
}
