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
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

private val JPEG_HEAD = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

class MediaApiTest {

    @Test
    fun upload_ofSomethingThatIsNotAnImage_isRejected() = serverTest {
        val token = client.signIn("me@example.com")
        val response = client.upload(token, "<html>hello</html>".toByteArray())
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun upload_oneByteOverTheLimit_isRejected() = serverTest {
        val token = client.signIn("me@example.com")
        val tooBig = JPEG_HEAD + ByteArray(2 * 1024 * 1024 - 2)
        assertThat(client.upload(token, tooBig).bodyAsText()).contains("too_large")
    }

    @Test
    fun image_ofAnotherReader_isNotFound() = serverTest {
        val mine = client.signIn("me@example.com")
        val other = client.signIn("you@example.com")
        val id = client.upload(mine, JPEG_HEAD).bodyAsText().field("id")
        val response = client.get("/api/v1/images/$id") { header(HttpHeaders.Authorization, other) }
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
    }
}

private suspend fun HttpClient.upload(token: String, bytes: ByteArray): HttpResponse =
    post("/api/v1/images") {
        header(HttpHeaders.Authorization, token)
        setBody(MultiPartFormDataContent(formData { append("file", bytes, fileName()) }))
    }

private fun fileName(): Headers = Headers.build {
    append(HttpHeaders.ContentDisposition, "filename=\"cover.jpg\"")
}
