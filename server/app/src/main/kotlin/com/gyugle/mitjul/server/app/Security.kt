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

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import com.gyugle.mitjul.server.core.security.TokenService
import com.gyugle.mitjul.server.feature.auth.service.AuthService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.get

fun Application.configureSecurity() {
    val tokens = get<TokenService>()
    val auth = get<AuthService>()
    install(Authentication) {
        jwt(TokenService.ACCESS) { acceptOnly(TokenService.ACCESS, tokens, auth) }
        jwt(TokenService.REFRESH) { acceptOnly(TokenService.REFRESH, tokens, auth) }
    }
}

private fun JWTAuthenticationProvider.Config.acceptOnly(
    kind: String,
    tokens: TokenService,
    auth: AuthService,
) {
    verifier(tokens.verifier)
    validate { credential -> auth.principalOf(credential, kind) }
    challenge { _, _ -> throw ApiFailure.Unauthorized() }
}
