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

package com.gyugle.mitjul.server.app

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test

class QuoteApiTest {

    @Test
    fun quotes_askedForOneBook_leaveOutTheOtherBook() = serverTest {
        val token = client.signIn("me@example.com")
        val cosmos = UUID.randomUUID().toString()
        client.addQuote(token, cosmos, "별의 먼지")
        client.addQuote(token, UUID.randomUUID().toString(), "다른 한 권의 문장")
        assertThat(client.quotes(token, cosmos).column("text")).containsExactly("별의 먼지")
    }

    @Test
    fun quotes_ofAnotherReader_areNotListed() = serverTest {
        val mine = client.signIn("me@example.com")
        val other = client.signIn("you@example.com")
        val cosmos = UUID.randomUUID().toString()
        client.addQuote(mine, cosmos, "별의 먼지")
        assertThat(client.quotes(other, cosmos).column("text")).isEmpty()
    }

    @Test
    fun quote_ofAnotherReader_survivesAnEditAndADelete() = serverTest {
        val mine = client.signIn("me@example.com")
        val other = client.signIn("you@example.com")
        val cosmos = UUID.randomUUID().toString()
        val id = client.addQuote(mine, cosmos, "별의 먼지").field("id")
        client.editQuote(other, id, cosmos)
        client.removeQuote(other, id)
        assertThat(client.quotes(mine, cosmos).column("text")).containsExactly("별의 먼지")
    }

    @Test
    fun quotes_ofOneBook_sendTheTimeTheyAreSortedBy() = serverTest {
        val token = client.signIn("me@example.com")
        val cosmos = UUID.randomUUID().toString()
        listOf("가", "나", "다").forEach { client.addQuote(token, cosmos, it) }
        val times = client.quotes(token, cosmos).column("createdAt").map(Instant::parse)
        assertThat(times).isEqualTo(times.sortedDescending())
    }

    @Test
    fun quote_sentTwiceWithOneId_isStoredOnceAtTheTimeItWasWritten() = serverTest {
        val token = client.signIn("me@example.com")
        val cosmos = UUID.randomUUID().toString()
        val id = UUID.randomUUID().toString()
        val body = """{"id":"$id","bookId":"$cosmos","text":"별의 먼지","createdAt":"2026-09-01T09:00:00Z"}"""
        repeat(2) { client.postJson("/api/v1/quotes", body, token) }
        val quotes = client.quotes(token, cosmos)
        assertThat(quotes.column("id")).containsExactly(id)
        assertThat(quotes.column("createdAt")).containsExactly("2026-09-01T09:00:00Z")
    }

    @Test
    fun quoteId_ofAnotherReader_isNotTakenOver() = serverTest {
        val mine = client.signIn("me@example.com")
        val other = client.signIn("you@example.com")
        val cosmos = UUID.randomUUID().toString()
        val id = client.addQuote(mine, cosmos, "별의 먼지").field("id")
        val answer = client.postJson("/api/v1/quotes", """{"id":"$id","bookId":"$cosmos","text":"가로챈 문장"}""", other)
        assertThat(answer.status).isEqualTo(HttpStatusCode.Conflict)
        assertThat(client.quotes(mine, cosmos).column("text")).containsExactly("별의 먼지")
    }
}

private suspend fun HttpClient.addQuote(token: String, book: String, text: String): String =
    postJson("/api/v1/quotes", """{"bookId":"$book","text":"$text"}""", token).bodyAsText()

private suspend fun HttpClient.quotes(token: String, book: String): String =
    get("/api/v1/quotes?bookId=$book") { header(HttpHeaders.Authorization, token) }.bodyAsText()

private suspend fun HttpClient.editQuote(token: String, id: String, book: String) =
    put("/api/v1/quotes/$id") {
        header(HttpHeaders.Authorization, token)
        contentType(ContentType.Application.Json)
        setBody("""{"bookId":"$book","text":"가로챈 문장"}""")
    }

private suspend fun HttpClient.removeQuote(token: String, id: String) =
    delete("/api/v1/quotes/$id") { header(HttpHeaders.Authorization, token) }
