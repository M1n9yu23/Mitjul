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

package com.gyugle.mitjul.server.feature.auth.api

import com.gyugle.mitjul.server.core.security.TokenService
import com.gyugle.mitjul.server.feature.auth.dto.LoginRequest
import com.gyugle.mitjul.server.feature.auth.dto.SignupRequest
import com.gyugle.mitjul.server.feature.auth.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import java.util.UUID

@Resource("/auth/signup")
class SignupResource

@Resource("/auth/login")
class LoginResource

@Resource("/auth/refresh")
class RefreshResource

@Resource("/auth/logout")
class LogoutResource

@OptIn(ExperimentalKtorApi::class)
fun Route.authRoutes(service: AuthService) {
    post<SignupResource> {
        service.signUp(call.receive<SignupRequest>())
        call.respond(HttpStatusCode.Created)
    }.describe {
        summary = "이메일과 비밀번호로 계정을 만든다."
    }
    post<LoginResource> {
        call.respond(service.logIn(call.receive<LoginRequest>()))
    }.describe {
        summary = "비밀번호를 확인하고 토큰 두 개를 발급한다."
    }
    authenticate(TokenService.REFRESH) {
        post<RefreshResource> {
            call.respond(service.refresh(call.userId()))
        }.describe {
            summary = "리프레시 토큰으로 새 토큰 두 개를 발급받는다."
        }
    }
    authenticate(TokenService.ACCESS) {
        post<LogoutResource> {
            service.logOut(call.userId())
            call.respond(HttpStatusCode.NoContent)
        }.describe {
            summary = "이 사용자에게 발급한 토큰을 전부 무효로 만든다."
        }
    }
}

private fun ApplicationCall.userId(): UUID =
    UUID.fromString(checkNotNull(principal<JWTPrincipal>()?.subject))
