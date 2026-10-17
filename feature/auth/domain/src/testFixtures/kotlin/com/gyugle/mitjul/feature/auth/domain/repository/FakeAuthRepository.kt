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

package com.gyugle.mitjul.feature.auth.domain.repository

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository(
    private val logInResult: EmptyResult<DataError> = Result.Success(Unit),
    private val signUpResult: EmptyResult<DataError.Network> = Result.Success(Unit),
    private val isLoggedIn: Flow<Boolean> = flowOf(false),
) : AuthRepository {

    var logInCalls = 0
        private set

    var signUpCalls = 0
        private set

    override suspend fun signUp(email: String, password: String): EmptyResult<DataError.Network> {
        signUpCalls++
        return signUpResult
    }

    override suspend fun logIn(email: String, password: String): EmptyResult<DataError> {
        logInCalls++
        return logInResult
    }

    override fun observeIsLoggedIn(): Flow<Boolean> = isLoggedIn

    override suspend fun logOut(): EmptyResult<DataError.Local> = Result.Success(Unit)
}
