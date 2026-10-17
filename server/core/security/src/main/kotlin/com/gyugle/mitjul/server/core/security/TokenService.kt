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

package com.gyugle.mitjul.server.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.UUID

class TokenService(signingKey: Secret) {

    private val algorithm = Algorithm.HMAC256(signingKey.reveal())

    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(ISSUER).build()

    fun issueAccess(userId: UUID, tokenVersion: Int): String =
        issue(userId, tokenVersion, ACCESS, ACCESS_SECONDS)

    fun issueRefresh(userId: UUID, tokenVersion: Int): String =
        issue(userId, tokenVersion, REFRESH, REFRESH_SECONDS)

    private fun issue(userId: UUID, tokenVersion: Int, kind: String, seconds: Long): String =
        JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId.toString())
            .withClaim(KIND, kind)
            .withClaim(VERSION, tokenVersion)
            .withExpiresAt(Instant.now().plusSeconds(seconds))
            .sign(algorithm)

    companion object {
        const val ACCESS = "access"
        const val REFRESH = "refresh"
        const val KIND = "kind"
        const val VERSION = "ver"
        private const val ISSUER = "mitjul"
        private const val ACCESS_SECONDS = 15L * 60
        private const val REFRESH_SECONDS = 30L * 24 * 60 * 60
    }
}
