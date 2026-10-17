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

package com.gyugle.mitjul.server.feature.auth.service

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import com.gyugle.mitjul.server.core.security.PasswordHasher
import com.gyugle.mitjul.server.core.security.TokenService
import com.gyugle.mitjul.server.feature.auth.dto.LoginRequest
import com.gyugle.mitjul.server.feature.auth.dto.SignupRequest
import com.gyugle.mitjul.server.feature.auth.dto.TokenResponse
import com.gyugle.mitjul.server.feature.auth.dto.validated
import com.gyugle.mitjul.server.feature.auth.repository.UserRepository
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.UUID

class AuthService(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val tokens: TokenService,
) {
    suspend fun signUp(request: SignupRequest) {
        val valid = request.validated()
        val hash = hasher.hash(valid.password)
        if (!users.insertIfEmailFree(valid.email, hash)) {
            throw ApiFailure.Duplicate("email")
        }
    }

    suspend fun logIn(request: LoginRequest): TokenResponse {
        val user = users.findByEmail(request.email.trim().lowercase())
        val matched = hasher.verify(request.password, user?.passwordHash)
        if (user == null || !matched) throw ApiFailure.Unauthorized()
        return issue(user.id, user.tokenVersion)
    }

    suspend fun refresh(userId: UUID): TokenResponse = issue(userId, users.bumpTokenVersion(userId))

    suspend fun logOut(userId: UUID) {
        users.bumpTokenVersion(userId)
    }

    suspend fun principalOf(credential: JWTCredential, kind: String): JWTPrincipal? {
        val payload = credential.payload
        if (payload.getClaim(TokenService.KIND).asString() != kind) return null
        val userId = runCatching { UUID.fromString(payload.subject) }.getOrNull() ?: return null
        val version = payload.getClaim(TokenService.VERSION).asInt()
        if (users.tokenVersionOf(userId) != version) return null
        return JWTPrincipal(payload)
    }

    private fun issue(userId: UUID, version: Int) = TokenResponse(
        accessToken = tokens.issueAccess(userId, version),
        refreshToken = tokens.issueRefresh(userId, version),
    )
}
