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

import co.touchlab.kermit.Logger
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.session.AuthTokens
import com.gyugle.mitjul.core.session.TokenStore
import com.gyugle.mitjul.feature.auth.data.datasource.AuthRemoteDataSource
import com.gyugle.mitjul.feature.auth.data.dto.CredentialsDto
import com.gyugle.mitjul.feature.auth.domain.LocalCopyCleaner
import com.gyugle.mitjul.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow

class RemoteAuthRepository(
    private val remote: AuthRemoteDataSource,
    private val tokenStore: TokenStore,
    private val localCopy: LocalCopyCleaner,
) : AuthRepository {

    override suspend fun signUp(email: String, password: String): EmptyResult<DataError.Network> =
        remote.signUp(CredentialsDto(email, password))

    override suspend fun logIn(email: String, password: String): EmptyResult<DataError> {
        val tokens = when (val result = remote.logIn(CredentialsDto(email, password))) {
            is Result.Success -> AuthTokens(result.data.accessToken, result.data.refreshToken)
            is Result.Error -> return result
        }
        val account = tokens.accountId()
        if (account == null || account != tokenStore.lastAccountId()) {
            localCopy.clear().onFailure { return Result.Error(it) }
        }
        return try {
            tokenStore.save(tokens)
            Result.Success(Unit)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e { "받은 토큰을 저장하지 못했다: ${e::class.simpleName}" }
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override fun observeIsLoggedIn(): Flow<Boolean> = tokenStore.observeHasTokens()

    override suspend fun logOut(): EmptyResult<DataError.Local> {
        val tokens = tokenStore.tokens()
        try {
            tokenStore.clear()
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e { "저장된 토큰을 지우지 못했다: ${e::class.simpleName}" }
            return Result.Error(DataError.Local.UNKNOWN)
        }
        remote.clearCachedTokens()
        localCopy.clear().onFailure { Logger.w { "로컬 DB를 비우지 못했다: $it" } }
        if (tokens != null) {
            remote.logOut(tokens.accessToken)
                .onFailure { Logger.w { "서버에 로그아웃을 알리지 못했다: $it" } }
        }
        return Result.Success(Unit)
    }
}
