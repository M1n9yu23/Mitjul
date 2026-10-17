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

package com.gyugle.mitjul.feature.auth.data.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.network.HttpClientFactory
import com.gyugle.mitjul.core.session.AuthTokens
import com.gyugle.mitjul.core.session.FakeTokenStore
import com.gyugle.mitjul.core.session.SessionAuthProvider
import com.gyugle.mitjul.feature.auth.data.api.AuthEndpoint
import com.gyugle.mitjul.feature.auth.data.datasource.KtorAuthDataSource
import com.gyugle.mitjul.feature.auth.domain.LocalCopyCleaner
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.io.encoding.Base64
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RemoteAuthRepositoryTest {

    private val sent = mutableListOf<HttpRequestData>()
    private val tokensJson = """{"accessToken":"access-1","refreshToken":"refresh-1"}"""
    private lateinit var authClient: HttpClient
    private var copyClears = 0

    private fun repository(
        status: HttpStatusCode,
        store: FakeTokenStore,
        body: String = "",
    ): RemoteAuthRepository {
        val engine = MockEngine { request ->
            sent += request
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClientFactory.createPlain(engine, "https://test.invalid/api/v1", isDebugBuild = false)
        authClient = HttpClientFactory.create(
            engine,
            "https://test.invalid/api/v1",
            SessionAuthProvider(client, store),
            isDebugBuild = false,
        )
        val localCopy = LocalCopyCleaner {
            copyClears++
            Result.Success(Unit)
        }
        return RemoteAuthRepository(KtorAuthDataSource(client, authClient), store, localCopy)
    }

    private fun tokensOf(account: String): String {
        val claims = Base64.UrlSafe.encode("""{"sub":"$account"}""".encodeToByteArray())
        return """{"accessToken":"head.$claims.sign","refreshToken":"refresh-1"}"""
    }

    @Test
    fun signUp_created_postsToSignupAndSavesNothing() = runTest {
        val store = FakeTokenStore()
        val result = repository(HttpStatusCode.Created, store).signUp("reader@test.invalid", "a".repeat(12))

        assertThat(sent.single().url.toString()).isEqualTo("https://test.invalid/api/v1/auth/signup")
        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(store.saved).isNull()
    }

    @Test
    fun logIn_ok_postsToLoginAndSavesTokens() = runTest {
        val store = FakeTokenStore()
        val result = repository(HttpStatusCode.OK, store, tokensJson).logIn("reader@test.invalid", "a".repeat(12))

        assertThat(sent.single().url.toString()).isEqualTo("https://test.invalid/api/v1/auth/login")
        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(store.saved).isEqualTo(AuthTokens("access-1", "refresh-1"))
    }

    @Test
    fun logIn_wrongPassword_isUnauthorizedAndSavesNothing() = runTest {
        val store = FakeTokenStore()
        val result = repository(HttpStatusCode.Unauthorized, store).logIn("reader@test.invalid", "a".repeat(12))

        assertThat(result).isEqualTo(Result.Error(DataError.Network.UNAUTHORIZED))
        assertThat(store.saved).isNull()
    }

    @Test
    fun logIn_storeFails_isLocalError() = runTest {
        val store = FakeTokenStore(failOnSave = true)
        val result = repository(HttpStatusCode.OK, store, tokensJson).logIn("reader@test.invalid", "a".repeat(12))

        assertThat(result).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
    }

    @Test
    fun logIn_anotherAccount_clearsThePreviousCopy() = runTest {
        val store = FakeTokenStore(lastAccount = "reader-a")
        repository(HttpStatusCode.OK, store, tokensOf("reader-b")).logIn("reader@test.invalid", "a".repeat(12))

        assertThat(copyClears).isEqualTo(1)
        assertThat(store.lastAccountId()).isEqualTo("reader-b")
    }

    @Test
    fun logIn_sameAccountAfterBeingSignedOut_keepsTheCopy() = runTest {
        val store = FakeTokenStore(lastAccount = "reader-a")
        repository(HttpStatusCode.OK, store, tokensOf("reader-a")).logIn("reader@test.invalid", "a".repeat(12))

        assertThat(copyClears).isEqualTo(0)
        assertThat(store.saved).isNotNull()
    }

    @Test
    fun logOut_serverUnavailable_clearsTokensAnyway() = runTest {
        val store = FakeTokenStore(initial = AuthTokens("access-1", "refresh-1"))
        val result = repository(HttpStatusCode.ServiceUnavailable, store).logOut()

        assertThat(sent.single().url.toString()).isEqualTo("https://test.invalid/api/v1/auth/logout")
        assertThat(sent.single().headers[HttpHeaders.Authorization]).isEqualTo("Bearer access-1")
        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(store.saved).isNull()
        assertThat(copyClears).isEqualTo(1)
    }

    @Test
    fun logOut_afterAuthorizedCall_nextCallCarriesNoOldToken() = runTest {
        val store = FakeTokenStore(initial = AuthTokens("access-1", "refresh-1"))
        val repository = repository(HttpStatusCode.NoContent, store)
        authClient.post(AuthEndpoint.Logout())

        repository.logOut()
        authClient.post(AuthEndpoint.Logout())

        assertThat(sent.last().headers[HttpHeaders.Authorization]).isNull()
    }
}
