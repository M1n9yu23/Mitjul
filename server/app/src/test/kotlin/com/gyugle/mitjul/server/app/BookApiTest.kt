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
import assertk.assertions.isEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.time.Instant
import org.junit.jupiter.api.Test

class BookApiTest {

    @Test
    fun book_ofAnotherReader_isNotFound() = serverTest {
        val mine = client.signIn("me@example.com")
        val other = client.signIn("you@example.com")
        val id = client.addBook(mine, "코스모스").field("id")
        val response = client.get("/api/v1/books/$id") { header(HttpHeaders.Authorization, other) }
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
    }

    @Test
    fun page_afterTheLastOne_repeatsNothingAndSkipsNothing() = serverTest {
        val token = client.signIn("me@example.com")
        listOf("가", "나", "다").forEach { client.addBook(token, it) }
        val first = client.shelf(token, "sort=TITLE&limit=2")
        val next = client.shelf(token, "sort=TITLE&limit=2&after=${first.column("id").last()}")
        assertThat(first.column("title")).containsExactly("가", "나")
        assertThat(next.column("title")).containsExactly("다")
    }

    @Test
    fun addBook_doneWithoutTheDay_isRejected() = serverTest {
        val token = client.signIn("me@example.com")
        val body = """{"title":"코스모스","author":"칼 세이건","status":"DONE","totalPages":720}"""
        assertThat(client.postJson("/api/v1/books", body, token).status)
            .isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun addBook_doneWithTheDay_keepsTheDay() = serverTest {
        val token = client.signIn("me@example.com")
        val body = """{"title":"코스모스","author":"칼 세이건","status":"DONE",""" +
            """"totalPages":720,"finishedAt":"2026-09-12T09:00:00Z"}"""
        val saved = client.postJson("/api/v1/books", body, token).bodyAsText()
        assertThat(saved.field("finishedAt")).isEqualTo("2026-09-12T09:00:00Z")
    }

    @Test
    fun shelf_recent_sendsTheTimeItIsSortedBy() = serverTest {
        val token = client.signIn("me@example.com")
        listOf("가", "나", "다").forEach { client.addBook(token, it) }
        val times = client.shelf(token, "sort=RECENT").column("createdAt").map(Instant::parse)
        assertThat(times).isEqualTo(times.sortedDescending())
    }
}

private suspend fun HttpClient.addBook(token: String, title: String): String = postJson(
    path = "/api/v1/books",
    body = """{"title":"$title","author":"칼 세이건","status":"READING","totalPages":720}""",
    token = token,
).bodyAsText()

private suspend fun HttpClient.shelf(token: String, query: String): String =
    get("/api/v1/books?$query") { header(HttpHeaders.Authorization, token) }.bodyAsText()
