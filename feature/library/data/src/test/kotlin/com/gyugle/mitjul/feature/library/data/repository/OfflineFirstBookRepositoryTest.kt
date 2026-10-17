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

package com.gyugle.mitjul.feature.library.data.repository

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.network.HttpClientFactory
import com.gyugle.mitjul.feature.library.data.datasource.FakeBooksLocalDataSource
import com.gyugle.mitjul.feature.library.data.datasource.FakeCoversLocalDataSource
import com.gyugle.mitjul.feature.library.data.datasource.KtorBooksDataSource
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class OfflineFirstBookRepositoryTest {

    private val sent = mutableListOf<HttpRequestData>()

    private fun repository(
        local: FakeBooksLocalDataSource,
        vararg answers: Pair<HttpStatusCode, String>,
        covers: FakeCoversLocalDataSource = FakeCoversLocalDataSource(),
    ): OfflineFirstBookRepository {
        val engine = MockEngine { request ->
            sent += request
            val (status, body) = answers[sent.size - 1]
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClientFactory.createPlain(engine, "https://test.invalid/api/v1", isDebugBuild = false)
        return OfflineFirstBookRepository(local, KtorBooksDataSource(client, "https://test.invalid/api/v1"), covers)
    }

    private fun page(vararg ids: String): String = ids.joinToString(",", "[", "]") {
        """{"id":"$it","title":"책","author":"저자","status":"READING",""" +
            """"totalPages":300,"currentPage":0,"createdAt":"2026-09-01T09:00:00Z"}"""
    }

    @Test
    fun refresh_twoPages_followsTheCursorAndReplacesTheCopy() = runTest {
        val local = FakeBooksLocalDataSource()
        val ok = HttpStatusCode.OK
        val result = repository(local, ok to page("b1", "b2"), ok to page("b3"), ok to page()).refresh()

        assertThat(sent.map { it.url.toString() }).containsExactly(
            "https://test.invalid/api/v1/books?limit=100",
            "https://test.invalid/api/v1/books?after=b2&limit=100",
            "https://test.invalid/api/v1/books?after=b3&limit=100",
        )
        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(local.replaced?.map { it.id }).isNotNull().containsExactly("b1", "b2", "b3")
    }

    @Test
    fun refresh_secondPageFails_keepsTheCopy() = runTest {
        val local = FakeBooksLocalDataSource()
        val result = repository(
            local,
            HttpStatusCode.OK to page("b1", "b2"),
            HttpStatusCode.ServiceUnavailable to "",
        ).refresh()

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVICE_UNAVAILABLE))
        assertThat(local.replaced).isNull()
    }

    @Test
    fun uploadCover_aroundTheServerLimit_sendsOnlyWhatFits() = runTest {
        val local = FakeBooksLocalDataSource()
        val tooLarge = repository(local, covers = FakeCoversLocalDataSource(COVER_MAX_BYTES + 1))
        assertThat(tooLarge.uploadCover("a.jpg") {}).isEqualTo(Result.Error(DataError.Network.PAYLOAD_TOO_LARGE))
        assertThat(sent).isEmpty()

        val created = HttpStatusCode.Created to """{"id":"c1","byteSize":$COVER_MAX_BYTES}"""
        val fits = repository(local, created, covers = FakeCoversLocalDataSource(COVER_MAX_BYTES))
        assertThat(fits.uploadCover("b.jpg") {}).isEqualTo(Result.Success("c1"))
        assertThat(sent.map { it.url.toString() }).containsExactly("https://test.invalid/api/v1/images")
    }
}
