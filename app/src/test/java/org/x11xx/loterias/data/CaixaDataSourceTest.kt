package org.x11xx.loterias.data

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.x11xx.loterias.domain.Modality
import java.io.IOException

class CaixaDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var source: CaixaDataSource

    @Before
    fun start() {
        server = MockWebServer()
        server.start()
        source = CaixaDataSource(OkHttpClient(), server.url("/portaldeloterias/api/"))
    }

    @After
    fun stop() {
        server.close()
    }

    @Test
    fun `latest asks for the modality alone`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())

        source.latest(Modality.MEGA_SENA)

        assertEquals("/portaldeloterias/api/megasena", server.takeRequest().target)
    }

    @Test
    fun `a contest is addressed by number`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())

        source.contest(Modality.QUINA, 7098)

        assertEquals("/portaldeloterias/api/quina/7098", server.takeRequest().target)
    }

    @Test
    fun `a 500 means the contest does not exist, not that the server broke`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(500).build())

        val thrown = runCatching { source.contest(Modality.MEGA_SENA, 999_999) }.exceptionOrNull()

        assertTrue("expected ContestNotFoundException, got $thrown", thrown is ContestNotFoundException)
    }

    @Test
    fun `any other failure stays an ordinary IO failure`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(503).build())

        val thrown = runCatching { source.latest(Modality.QUINA) }.exceptionOrNull()

        assertTrue(thrown is IOException)
        assertTrue(thrown !is ContestNotFoundException)
    }
}
