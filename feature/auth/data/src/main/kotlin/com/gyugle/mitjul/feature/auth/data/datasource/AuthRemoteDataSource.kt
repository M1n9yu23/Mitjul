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

package com.gyugle.mitjul.feature.auth.data.datasource

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.session.dto.TokenResponseDto
import com.gyugle.mitjul.feature.auth.data.dto.CredentialsDto

interface AuthRemoteDataSource {
    suspend fun signUp(credentials: CredentialsDto): EmptyResult<DataError.Network>
    suspend fun logIn(credentials: CredentialsDto): Result<TokenResponseDto, DataError.Network>
    suspend fun logOut(accessToken: String): EmptyResult<DataError.Network>
    fun clearCachedTokens()
}
