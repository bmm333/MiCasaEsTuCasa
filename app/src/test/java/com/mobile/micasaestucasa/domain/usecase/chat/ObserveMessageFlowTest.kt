package com.mobile.micasaestucasa.domain.usecase.chat

import app.cash.turbine.test
import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ObserveMessageFlowTest {
    private lateinit var chatRepo: ChatRepo

    private fun makeMessage(id: String, text: String) = Message(
        id = id,
        conversationId = "conv1",
        senderId = "user1",
        text = text,
        timestamp = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        chatRepo = mockk()
    }

    @Test
    fun `flow emits initial message list`() = runTest {
        val messages = listOf(
            makeMessage("msg1", "hello"),
            makeMessage("msg2", "How are you?")
        )
        coEvery { chatRepo.observeMessages("conv1") } returns flowOf(messages)
        chatRepo.observeMessages("conv1").test {
            val out = awaitItem()
            assertEquals(2, out.size)
            assertEquals("hello", out[0].text)
            assertEquals("How are you?", out[1].text)
            awaitComplete()
        }
    }

    @Test
    fun `flow emits multiple messages in seq`() = runTest {
        val firstbatch = listOf(makeMessage("msg1", "Primo"))
        val secondbatch = listOf(makeMessage("msg1", "Primo"), makeMessage("msg2", "Secondo"))
        val thirdbatch = listOf(makeMessage("msg1", "Primo"), makeMessage("msg2", "Secondo"), makeMessage("msg3", "Terzo"))
        coEvery { chatRepo.observeMessages("conv1") } returns
            flow {
                emit(firstbatch)
                emit(secondbatch)
                emit(thirdbatch)
            }
        chatRepo.observeMessages("conv1").test {
            assertEquals(1, awaitItem().size) // first update is 1
            assertEquals(2, awaitItem().size) // incremental
            assertEquals(3, awaitItem().size)
            awaitComplete()
        }
    }

    @Test
    fun `empty list no msg emitted`() = runTest {
        coEvery { chatRepo.observeMessages("conv1") } returns flowOf(emptyList())
        chatRepo.observeMessages("conv1").test {
            val out = awaitItem()
            assertTrue(out.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `flow propogates frestore errors`() = runTest {
        coEvery { chatRepo.observeMessages("conv1") } returns
            flow { throw Exception("Firestore listener error") }
        chatRepo.observeMessages("conv1").test {
            val error = awaitError()
            assertEquals("Firestore listener error", error.message)
        }
    }

    @Test
    fun `correct message timestamp ordering`() = runTest {
        val messages = listOf(
            makeMessage("msg3", "Terzo").copy(timestamp = 3000L),
            makeMessage("msg1", "Primo").copy(timestamp = 1000L),
            makeMessage("msg2", "Secondo").copy(timestamp = 2000L)
        ).sortedBy { it.timestamp }
        coEvery { chatRepo.observeMessages("conv1") } returns flowOf(messages)
        chatRepo.observeMessages("conv1").test {
            val out = awaitItem()
            assertEquals("Primo", out[0].text)
            assertEquals("Secondo", out[1].text)
            assertEquals("Terzo", out[2].text)
            awaitComplete()
        }
    }
}
