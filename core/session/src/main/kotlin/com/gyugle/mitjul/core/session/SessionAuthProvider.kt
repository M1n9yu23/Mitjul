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

package com.gyugle.mitjul.core.session

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.network.AuthProvider
import com.gyugle.mitjul.core.network.safeCall
import com.gyugle.mitjul.core.session.api.RefreshRoute
import com.gyugle.mitjul.core.session.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.bearerAuth

class SessionAuthProvider(
    private val plainClient: HttpClient,
    private val store: TokenStore,
) : AuthProvider {

    override suspend fun currentTokens(): BearerTokens? = store.tokens()?.toBearerTokens()

    override suspend fun refresh(expired: BearerTokens?): BearerTokens? {
        val saved = store.tokens() ?: return null
        if (saved.accessToken != expired?.accessToken) return saved.toBearerTokens()

        val result = safeCall<TokenResponseDto> {
            plainClient.post(RefreshRoute()) { bearerAuth(saved.refreshToken) }
        }
        return when (result) {
            is Result.Success -> {
                val renewed = AuthTokens(result.data.accessToken, result.data.refreshToken)
                store.save(renewed)
                renewed.toBearerTokens()
            }
            is Result.Error -> {
                if (result.error == DataError.Network.UNAUTHORIZED) store.clear()
                null
            }
        }
    }
}

private fun AuthTokens.toBearerTokens() = BearerTokens(accessToken, refreshToken)
