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

import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTokenStore(
    initial: AuthTokens? = null,
    private val failOnSave: Boolean = false,
    private var lastAccount: String? = null,
) : TokenStore {

    private val stored = MutableStateFlow(initial)

    val saved: AuthTokens?
        get() = stored.value

    override suspend fun tokens(): AuthTokens? = stored.value

    override fun observeHasTokens(): Flow<Boolean> = stored.map { it != null }

    override suspend fun save(tokens: AuthTokens) {
        if (failOnSave) throw IOException("디스크에 쓰지 못했다")
        stored.value = tokens
        lastAccount = tokens.accountId() ?: lastAccount
    }

    override suspend fun clear() {
        stored.value = null
    }

    override suspend fun lastAccountId(): String? = lastAccount
}
