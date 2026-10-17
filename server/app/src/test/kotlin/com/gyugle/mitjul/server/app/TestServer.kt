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

import com.gyugle.mitjul.server.core.database.Db
import com.gyugle.mitjul.server.core.security.Secret
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.nio.file.Path
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun serverTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    val config = ServerConfig(
        host = "127.0.0.1",
        port = 0,
        databaseUrl = "jdbc:h2:mem:${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
        uploadDir = Path.of("build/test-images", UUID.randomUUID().toString()),
        signingKey = Secret(UUID.randomUUID().toString()),
    )
    application { module(config, Db.connect(config.databaseUrl)) }
    block()
}

suspend fun HttpClient.postJson(
    path: String,
    body: String,
    token: String? = null,
): HttpResponse = post(path) {
    contentType(ContentType.Application.Json)
    if (token != null) header(HttpHeaders.Authorization, token)
    setBody(body)
}

suspend fun HttpClient.signIn(email: String): String {
    val account = """{"email":"$email","password":"a-long-enough-password"}"""
    postJson("/api/v1/auth/signup", account)
    return "Bearer " + postJson("/api/v1/auth/login", account).bodyAsText().field("accessToken")
}

fun String.field(name: String): String =
    Json.parseToJsonElement(this).jsonObject.getValue(name).jsonPrimitive.content

fun String.column(name: String): List<String> =
    Json.parseToJsonElement(this).jsonArray.map { it.jsonObject.getValue(name).jsonPrimitive.content }
