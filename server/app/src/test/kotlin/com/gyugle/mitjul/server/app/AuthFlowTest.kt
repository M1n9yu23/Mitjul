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
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

private const val ACCOUNT = """{"email":"me@example.com","password":"a-long-enough-password"}"""
private const val WRONG = """{"email":"me@example.com","password":"not-the-right-password"}"""

class AuthFlowTest {

    @Test
    fun logIn_afterSignUp_returnsBothTokens() = serverTest {
        client.postJson("/api/v1/auth/signup", ACCOUNT)
        val response = client.postJson("/api/v1/auth/login", ACCOUNT)
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText().field("refreshToken")).isNotEmpty()
    }

    @Test
    fun logIn_wrongPassword_isRejected() = serverTest {
        client.postJson("/api/v1/auth/signup", ACCOUNT)
        val response = client.postJson("/api/v1/auth/login", WRONG)
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun refresh_afterLogOut_isRejected() = serverTest {
        client.postJson("/api/v1/auth/signup", ACCOUNT)
        val tokens = client.postJson("/api/v1/auth/login", ACCOUNT).bodyAsText()
        client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer ${tokens.field("accessToken")}")
        }
        val response = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Authorization, "Bearer ${tokens.field("refreshToken")}")
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun logIn_pastTheLimit_isThrottled() = serverTest {
        client.postJson("/api/v1/auth/signup", ACCOUNT)
        repeat(9) { client.postJson("/api/v1/auth/login", WRONG) }
        val response = client.postJson("/api/v1/auth/login", ACCOUNT)
        assertThat(response.status).isEqualTo(HttpStatusCode.TooManyRequests)
    }
}
