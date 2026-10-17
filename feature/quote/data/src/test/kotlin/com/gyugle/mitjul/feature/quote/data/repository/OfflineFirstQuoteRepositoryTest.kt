/*
 * Copyright 2026 MinGyu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gyugle.mitjul.feature.quote.data.repository

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.network.HttpClientFactory
import com.gyugle.mitjul.feature.quote.data.datasource.FakeQuotesLocalDataSource
import com.gyugle.mitjul.feature.quote.data.datasource.KtorQuotesDataSource
import com.gyugle.mitjul.feature.quote.domain.QuoteSyncScheduler
import com.gyugle.mitjul.feature.quote.domain.model.NewQuote
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class OfflineFirstQuoteRepositoryTest {

    private val sent = mutableListOf<HttpRequestData>()
    private var scheduled = 0

    private fun repository(
        local: FakeQuotesLocalDataSource,
        vararg answers: Pair<HttpStatusCode, String>,
    ): OfflineFirstQuoteRepository {
        val engine = MockEngine { request ->
            sent += request
            val (status, body) = answers[sent.size - 1]
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClientFactory.createPlain(engine, "https://test.invalid/api/v1", isDebugBuild = false)
        return OfflineFirstQuoteRepository(local, KtorQuotesDataSource(client), QuoteSyncScheduler { scheduled++ })
    }

    private fun page(vararg ids: String): String = ids.joinToString(",", "[", "]") { one(it) }

    private fun one(id: String): String =
        """{"id":"$id","bookId":"b1","text":"별의 먼지","createdAt":"2026-09-01T09:00:00Z"}"""

    private fun pending(id: String) =
        Quote(id, "b1", "별의 먼지", null, null, Instant.parse("2026-09-01T09:00:00Z"), isPending = true)

    @Test
    fun refresh_twoPages_followsTheCursorAndReplacesTheCopy() = runTest {
        val local = FakeQuotesLocalDataSource()
        val ok = HttpStatusCode.OK
        val result = repository(local, ok to page("q1", "q2"), ok to page("q3"), ok to page()).refresh()

        assertThat(sent.map { it.url.toString() }).containsExactly(
            "https://test.invalid/api/v1/quotes?limit=200",
            "https://test.invalid/api/v1/quotes?after=q2&limit=200",
            "https://test.invalid/api/v1/quotes?after=q3&limit=200",
        )
        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(local.replaced?.map { it.id }).isNotNull().containsExactly("q1", "q2", "q3")
    }

    @Test
    fun refresh_secondPageFails_keepsTheCopy() = runTest {
        val local = FakeQuotesLocalDataSource()
        val result = repository(
            local,
            HttpStatusCode.OK to page("q1", "q2"),
            HttpStatusCode.ServiceUnavailable to "",
        ).refresh()

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVICE_UNAVAILABLE))
        assertThat(local.replaced).isNull()
    }

    @Test
    fun addQuote_offline_keepsItPendingAndSchedulesWithoutARequest() = runTest {
        val local = FakeQuotesLocalDataSource()
        val result = repository(local).addQuote(NewQuote("b1", "별의 먼지", page = 72, memo = null))

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(local.upserted.map { it.text to it.isPending }).containsExactly("별의 먼지" to true)
        assertThat(scheduled).isEqualTo(1)
        assertThat(sent).isEmpty()
    }

    @Test
    fun sendPending_secondFails_sendsTheIdAndKeepsTheSecondPending() = runTest {
        val local = FakeQuotesLocalDataSource(waiting = listOf(pending("q1"), pending("q2")))
        val result = repository(
            local,
            HttpStatusCode.Created to one("q1"),
            HttpStatusCode.ServiceUnavailable to "",
        ).sendPending()

        assertThat((sent.first().body as TextContent).text).contains(""""id":"q1"""")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVICE_UNAVAILABLE))
        assertThat(local.upserted.map { it.id to it.isPending }).containsExactly("q1" to false)
    }
}
