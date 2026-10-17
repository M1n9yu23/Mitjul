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

import com.gyugle.mitjul.server.core.security.TokenService
import com.gyugle.mitjul.server.feature.auth.api.authRoutes
import com.gyugle.mitjul.server.feature.auth.service.AuthService
import com.gyugle.mitjul.server.feature.book.api.bookRoutes
import com.gyugle.mitjul.server.feature.book.service.BookService
import com.gyugle.mitjul.server.feature.media.api.imageRoutes
import com.gyugle.mitjul.server.feature.media.service.ImageService
import com.gyugle.mitjul.server.feature.quote.api.quoteRoutes
import com.gyugle.mitjul.server.feature.quote.service.QuoteService
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi
import org.koin.ktor.ext.get

@OptIn(ExperimentalKtorApi::class)
fun Application.configureRouting() {
    val authService = get<AuthService>()
    val bookService = get<BookService>()
    val quoteService = get<QuoteService>()
    val imageService = get<ImageService>()
    install(Resources)
    routing {
        swaggerUI("/docs") {
            source = OpenApiDocSource.Routing()
            info = OpenApiInfo(title = "Mitjul API", version = "1.0")
        }
        get("/health") {
            call.respondText("ok")
        }.describe {
            summary = "서버가 살아 있는지 확인한다."
        }
        route("/api/v1") {
            rateLimit(RateLimitName(AUTH_LIMIT)) {
                authRoutes(authService)
            }
            authenticate(TokenService.ACCESS) {
                bookRoutes(bookService)
                quoteRoutes(quoteService)
                imageRoutes(imageService)
            }
        }
    }
}
