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

package com.gyugle.mitjul.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import co.touchlab.kermit.Logger as AppLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

object HttpClientFactory {

    fun create(
        engine: HttpClientEngine,
        baseUrl: String,
        auth: AuthProvider,
        isDebugBuild: Boolean,
    ): HttpClient = HttpClient(engine) {
        defaults(baseUrl, isDebugBuild)
        install(Auth) {
            bearer {
                loadTokens { auth.currentTokens() }
                refreshTokens { auth.refresh(oldTokens) }
                sendWithoutRequest { true }
            }
        }
    }

    fun createPlain(
        engine: HttpClientEngine,
        baseUrl: String,
        isDebugBuild: Boolean,
    ): HttpClient = HttpClient(engine) {
        defaults(baseUrl, isDebugBuild)
    }

    private fun HttpClientConfig<*>.defaults(baseUrl: String, isDebugBuild: Boolean) {
        expectSuccess = false
        install(Resources)
        install(ContentNegotiation) { json(NetworkJson) }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            logger = object : KtorLogger {
                override fun log(message: String) = AppLogger.d(tag = "HTTP") { message }
            }
            level = if (isDebugBuild) LogLevel.HEADERS else LogLevel.NONE
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        defaultRequest { url(baseUrl.asBaseUrl()) }
    }
}

internal fun String.asBaseUrl(): String {
    require(startsWith("http://") || startsWith("https://")) { "URL이 http로 시작하지 않는다: $this" }
    return trimEnd('/') + "/"
}
